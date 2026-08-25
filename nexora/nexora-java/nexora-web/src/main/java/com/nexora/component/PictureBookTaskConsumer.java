package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.constants.Constants;
import com.nexora.dto.PictureBookTaskVO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.UserInfo;
import com.nexora.service.PictureBookService;
import com.nexora.service.PictureBookTaskService;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 绘本生成异步任务消费者：
 * 状态机 PENDING → STORY_GENERATING → STORY_DONE → IMAGE_GENERATING → COMPLETED / FAILED；
 * 并发策略由 ImageProvider.maxConcurrency() 声明：百炼 1（串行）、豆包 3（并发）；
 * 单页失败不废弃整本，进度与结果持久化到 Redis 任务体。
 */
@Component
public class PictureBookTaskConsumer {

    private static final Logger log = LoggerFactory.getLogger(PictureBookTaskConsumer.class);

    /** 图片并发生成线程池（并发上限由供应商 maxConcurrency 控制） */
    private static final ExecutorService IMAGE_POOL = Executors.newFixedThreadPool(3);

    /** 并发模式单页生成等待上限（内部已含限流排队与 3 次重试，此处兜底放宽） */
    private static final long PAGE_WAIT_SECONDS = 600;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private PictureBookTaskService pictureBookTaskService;

    @Resource
    private PictureBookGenerateComponent pictureBookGenerateComponent;

    @Resource
    private PictureBookService pictureBookService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ImageProvider imageProvider;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_PICTURE_BOOK_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        PictureBookTaskVO task;
        try {
            task = pictureBookTaskService.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("绘本任务读取失败 taskId={}", taskId, e);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("绘本生成任务执行异常 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setMessage("绘本生成失败：" + e.getMessage());
            pictureBookTaskService.update(task);
        }
    }

    private void execute(PictureBookTaskVO task) throws Exception {
        // 0. 主题净化：解构版权角色为原创元素（如"米老鼠"→"爱冒险的可爱小老鼠"）
        String sanitized = pictureBookGenerateComponent.sanitizeTopic(task.getTopic());
        if (!sanitized.equals(task.getTopic())) {
            task.setTopic(sanitized);
            task.setMessage("主题已自动改编为原创版本...");
            pictureBookTaskService.update(task);
        }

        // 1. 故事编写（分页文案）
        task.setStatus("STORY_GENERATING");
        task.setMessage("AI 正在编写故事...");
        pictureBookTaskService.update(task);

        PictureBookGenerateComponent.StoryScript story =
                pictureBookGenerateComponent.generateStory(task.getStage(), task.getTopic());
        if (story.pages().isEmpty()) {
            throw new IllegalStateException("故事生成结果为空");
        }
        JSONArray pages = new JSONArray();
        for (String text : story.pages()) {
            JSONObject page = new JSONObject();
            page.put("text", text);
            page.put("imageFile", "");
            pages.add(page);
        }
        task.setTitle(story.title());
        task.setTotal(story.pages().size());
        task.setPages(pages.toJSONString());
        task.setStatus("STORY_DONE");
        task.setMessage("故事完成，开始生成插图...");
        pictureBookTaskService.update(task);

        // 2. 分镜（文本已分页；提示词在 generatePageImage 内部拼装）+ 生图
        task.setStatus("IMAGE_GENERATING");
        pictureBookTaskService.update(task);

        UserInfo user = userInfoService.getUserInfoByUserId(task.getUserId());
        String email = user == null ? null : user.getEmail();
        List<String> texts = story.pages();
        AtomicReference<String> lastImageError = new AtomicReference<>();

        List<JSONObject> pageObjects = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            pageObjects.add(pages.getJSONObject(i));
        }
        int maxConcurrency = imageProvider.maxConcurrency();
        if (maxConcurrency <= 1) {
            generateImagesSequentially(task, email, story, texts, pageObjects, lastImageError);
        } else {
            generateImagesConcurrently(task, email, story, texts, pageObjects, lastImageError, maxConcurrency);
        }

        // 3. 组装产物并落库
        JSONObject ext = new JSONObject();
        ext.put("type", "PICTURE_BOOK");
        ext.put("pages", pages);
        boolean allFailed = pageObjects.stream()
                .allMatch(page -> StringTools.isEmpty(page.getString("imageFile")));
        if (allFailed && lastImageError.get() != null) {
            ext.put("imageError", lastImageError.get());
        }

        ResourceInfo book = pictureBookService.saveBook(
                task.getUserId(), task.getStage(), task.getTitle(), ext.toJSONString());
        task.setStatus("COMPLETED");
        task.setMessage("绘本生成完成");
        task.setBookResourceId(book.getResourceId());
        pictureBookTaskService.update(task);
        log.info("绘本异步任务完成 taskId={} userId={} title={} pages={}",
                task.getTaskId(), task.getUserId(), task.getTitle(), story.pages().size());
    }

    /**
     * maxConcurrency=1 的供应商：逐页串行，内部保持请求间隔。
     */
    private void generateImagesSequentially(PictureBookTaskVO task, String email,
                                            PictureBookGenerateComponent.StoryScript story,
                                            List<String> texts, List<JSONObject> pageObjects,
                                            AtomicReference<String> lastImageError) {
        for (int i = 0; i < texts.size(); i++) {
            String imageFile = null;
            try {
                imageFile = pictureBookGenerateComponent.generatePageImage(
                        email, task.getStage(), texts.get(i), story.title(), i);
            } catch (Exception e) {
                log.warn("绘本分页插图异常 page={} title={}", i + 1, story.title(), e);
            }
            updatePageResult(i, imageFile, task, pageObjects, lastImageError);
        }
    }

    /**
     * maxConcurrency>1 的供应商：按声明的并发上限生成，跨页自动排队。
     */
    private void generateImagesConcurrently(PictureBookTaskVO task, String email,
                                            PictureBookGenerateComponent.StoryScript story,
                                            List<String> texts, List<JSONObject> pageObjects,
                                            AtomicReference<String> lastImageError,
                                            int maxConcurrency) throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[texts.size()];
        Semaphore gate = new Semaphore(maxConcurrency);
        for (int i = 0; i < texts.size(); i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    gate.acquire();
                    try {
                        return pictureBookGenerateComponent.generatePageImage(
                                email, task.getStage(), texts.get(index), story.title(), index);
                    } finally {
                        gate.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("绘本分页插图任务被中断 page={} title={}", index + 1, story.title(), e);
                    return null;
                } catch (Exception e) {
                    log.warn("绘本分页插图异常 page={} title={}", index + 1, story.title(), e);
                    return null;
                }
            }, IMAGE_POOL);
        }
        for (int i = 0; i < futures.length; i++) {
            String imageFile = futures[i].get(PAGE_WAIT_SECONDS, TimeUnit.SECONDS);
            updatePageResult(i, imageFile, task, pageObjects, lastImageError);
        }
    }

    private void updatePageResult(int index, String imageFile, PictureBookTaskVO task,
                                  List<JSONObject> pageObjects,
                                  AtomicReference<String> lastImageError) {
        String failure = pictureBookGenerateComponent.getLastFailureReason();
        if (failure != null) {
            lastImageError.set(failure);
        }
        pageObjects.get(index).put("imageFile", imageFile == null ? "" : imageFile);
        task.setCurrent(index + 1);
        pictureBookTaskService.update(task);
    }
}
