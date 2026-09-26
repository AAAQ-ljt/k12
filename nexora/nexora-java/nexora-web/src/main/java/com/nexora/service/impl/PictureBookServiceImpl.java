package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.component.PictureBookAudioComponent;
import com.nexora.component.PictureBookGenerateComponent;
import com.nexora.component.RedisComponent;
import com.nexora.component.TtsProvider;
import com.nexora.constants.Constants;
import com.nexora.dto.PictureBookAudioTaskVO;
import com.nexora.dto.PictureBookPageFixVO;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 学生绘本业务实现：绘本产物存 resource_info.ext_json（PICTURE_BOOK）；生成由异步任务编排
 */
@Service
public class PictureBookServiceImpl implements PictureBookService {

    private static final Logger log = LoggerFactory.getLogger(PictureBookServiceImpl.class);

    /** 单页补画执行池 */
    private static final ExecutorService PAGE_FIX_POOL = Executors.newFixedThreadPool(2);

    /** 旁白合成任务执行池（同时最多两本书在录旁白；书内逐页串行，保证进度与落库顺序） */
    private static final ExecutorService AUDIO_TASK_POOL = Executors.newFixedThreadPool(2);

    /** 补画任务体 TTL / 运行锁 TTL */
    private static final long PAGE_FIX_TTL_HOURS = 1;
    private static final long PAGE_FIX_RUNNING_MINUTES = 10;

    /** 旁白任务体 TTL / 运行锁 TTL（整本最坏 8 页 × 3 次重试，锁放宽到 60 分钟） */
    private static final long AUDIO_TASK_TTL_HOURS = 1;
    private static final long AUDIO_TASK_RUNNING_MINUTES = 60;

    @Value("${project.folder}")
    private String projectFolder;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private PictureBookGenerateComponent pictureBookGenerateComponent;

    @Resource
    private PictureBookAudioComponent pictureBookAudioComponent;

    @Override
    public ResourceInfo saveBook(String userId, String stage, String title, String extJson) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(title)) {
            throw new BusinessException("绘本保存参数不完整");
        }
        String resourceId = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        ResourceInfo bean = new ResourceInfo();
        bean.setResourceId(resourceId);
        bean.setResourceName(title);
        bean.setResourceType("PICTURE_BOOK");
        bean.setExtJson(extJson);
        ResourceDirectory attachments = studentKnowledgeBaseService
                .getSystemDirectory(userId, StudentKnowledgeBaseService.DIR_TYPE_ATTACHMENTS);
        bean.setDirectoryId(attachments == null ? null : attachments.getDirId());
        bean.setStage(stage);
        bean.setOwnerId(userId);
        bean.setSource(1);
        bean.setStatus(1);
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        resourceInfoService.add(bean);
        log.info("绘本产物落库 userId={} title={}", userId, title);
        return bean;
    }

    @Override
    public List<ResourceInfo> myList(String userId) {
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setOwnerId(userId);
        query.setResourceType("PICTURE_BOOK");
        query.setOrderBy("create_time desc");
        return resourceInfoService.findListByParam(query);
    }

    @Override
    public ResourceInfo getInfo(String userId, String resourceId) {
        return assertOwned(userId, resourceId);
    }

    @Override
    public void delete(String userId, String resourceId) {
        ResourceInfo book = assertOwned(userId, resourceId);
        deletePageFiles(book);
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
    }

    @Override
    public String pageImageFile(String resourceId, int pageIndex) {
        return pageFile(resourceId, pageIndex, "imageFile");
    }

    @Override
    public String pageAudioFile(String resourceId, int pageIndex) {
        return pageFile(resourceId, pageIndex, "audioFile");
    }

    /** 从 ext_json 取指定页的文件相对路径（imageFile / audioFile 共用逻辑） */
    private String pageFile(String resourceId, int pageIndex, String fileField) {
        ResourceInfo book = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (book == null || !"PICTURE_BOOK".equalsIgnoreCase(book.getResourceType())) {
            return null;
        }
        String extJson = book.getExtJson();
        if (StringTools.isEmpty(extJson)) {
            return null;
        }
        JSONObject ext = JSON.parseObject(extJson);
        JSONArray pages = ext == null ? null : ext.getJSONArray("pages");
        if (pages == null || pageIndex < 0 || pageIndex >= pages.size()) {
            return null;
        }
        JSONObject page = pages.getJSONObject(pageIndex);
        String file = page == null ? null : page.getString(fileField);
        return StringTools.isEmpty(file) ? null : file;
    }

    private void deletePageFiles(ResourceInfo book) {
        String extJson = book.getExtJson();
        if (StringTools.isEmpty(extJson)) {
            return;
        }
        try {
            JSONObject ext = JSON.parseObject(extJson);
            JSONArray pages = ext == null ? null : ext.getJSONArray("pages");
            if (pages == null) {
                return;
            }
            for (int i = 0; i < pages.size(); i++) {
                JSONObject page = pages.getJSONObject(i);
                if (page == null) {
                    continue;
                }
                String imageFile = page.getString("imageFile");
                if (!StringTools.isEmpty(imageFile)) {
                    Files.deleteIfExists(Paths.get(projectFolder, imageFile));
                }
                String audioFile = page.getString("audioFile");
                if (!StringTools.isEmpty(audioFile)) {
                    pictureBookAudioComponent.deleteAudioFile(audioFile);
                }
            }
        } catch (Exception e) {
            log.warn("删除绘本产物文件失败 resourceId={}", book.getResourceId(), e);
        }
    }

    private ResourceInfo assertOwned(String userId, String resourceId) {
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || !userId.equals(resource.getOwnerId())) {
            throw new BusinessException("绘本不存在或无权操作");
        }
        return resource;
    }

    @Override
    public PictureBookPageFixVO submitPageFix(String userId, String resourceId, int page) {
        ResourceInfo book = assertOwned(userId, resourceId);
        JSONArray pages = parsePages(book);
        if (pages == null || page < 0 || page >= pages.size()) {
            throw new BusinessException("页码不存在");
        }
        JSONObject pageObj = pages.getJSONObject(page);
        if (pageObj == null) {
            throw new BusinessException("页码数据异常");
        }
        if (!StringTools.isEmpty(pageObj.getString("imageFile"))) {
            throw new BusinessException("该页已有插图，无需补画");
        }
        String bodyKey = fixBodyKey(resourceId, page);
        String runningKey = Constants.REDIS_KEY_PICTURE_BOOK_PAGE_FIX_RUNNING + resourceId + ":" + page;

        // 已有终态结果直接返回（前端刷新后不再触发重复提交）
        String existing = redisComponent.getString(bodyKey);
        if (!StringTools.isEmpty(existing)) {
            PictureBookPageFixVO old = JSON.parseObject(existing, PictureBookPageFixVO.class);
            if (old != null && ("COMPLETED".equals(old.getStatus()) || "FAILED".equals(old.getStatus()))) {
                return old;
            }
        }
        PictureBookPageFixVO vo = new PictureBookPageFixVO();
        vo.setResourceId(resourceId);
        vo.setPage(page);
        boolean locked = redisComponent.setIfAbsent(runningKey, "1", PAGE_FIX_RUNNING_MINUTES, TimeUnit.MINUTES);
        if (!locked) {
            vo.setStatus("RUNNING");
            vo.setMessage("该页补画进行中，请稍候...");
            return vo;
        }
        vo.setStatus("RUNNING");
        vo.setMessage("正在为第 " + (page + 1) + " 页补画插图，通常需要一两分钟...");
        saveFix(bodyKey, vo);
        final ResourceInfo bookRef = book;
        PAGE_FIX_POOL.execute(() -> executePageFix(userId, bookRef, page, pages, bodyKey, runningKey));
        return vo;
    }

    @Override
    public PictureBookPageFixVO getPageFix(String userId, String resourceId, int page) {
        assertOwned(userId, resourceId);
        String body = redisComponent.getString(fixBodyKey(resourceId, page));
        if (StringTools.isEmpty(body)) {
            PictureBookPageFixVO none = new PictureBookPageFixVO();
            none.setResourceId(resourceId);
            none.setPage(page);
            none.setStatus("FAILED");
            none.setMessage("补画任务不存在或已过期，请重新点击补画");
            return none;
        }
        return JSON.parseObject(body, PictureBookPageFixVO.class);
    }

    /**
     * 单页补画执行：生图（与整本同一条 ImageProvider 链路）→ 更新 ext_json → 落库，结果写回任务体
     */
    private void executePageFix(String userId, ResourceInfo book, int page, JSONArray pages,
                                String bodyKey, String runningKey) {
        PictureBookPageFixVO vo = new PictureBookPageFixVO();
        vo.setResourceId(book.getResourceId());
        vo.setPage(page);
        try {
            ResourceInfo fresh = resourceInfoService.getResourceInfoByResourceId(book.getResourceId());
            if (fresh == null) {
                vo.setStatus("FAILED");
                vo.setMessage("绘本不存在或已删除");
                return;
            }
            JSONObject pageObj = pages.getJSONObject(page);
            String text = pageObj == null ? "" : pageObj.getString("text");
            UserInfo user = userInfoService.getUserInfoByUserId(userId);
            String email = user == null ? null : user.getEmail();
            String imageFile = pictureBookGenerateComponent.generatePageImage(
                    email, fresh.getStage(), text, fresh.getResourceName(), page);
            if (imageFile == null) {
                String reason = pictureBookGenerateComponent.getLastFailureReason();
                vo.setStatus("FAILED");
                vo.setMessage(reason == null ? "补画失败，请稍后重试" : reason);
                return;
            }
            pageObj.put("imageFile", imageFile);
            ResourceInfo update = new ResourceInfo();
            update.setExtJson(rebuildExtJson(fresh, pages));
            update.setUpdateTime(new Date());
            resourceInfoService.updateResourceInfoByResourceId(update, fresh.getResourceId());
            vo.setStatus("COMPLETED");
            vo.setMessage("第 " + (page + 1) + " 页插图已补画完成");
        } catch (Exception e) {
            log.warn("绘本单页补画执行异常 resourceId={} page={}", book.getResourceId(), page, e);
            vo.setStatus("FAILED");
            vo.setMessage("补画失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        } finally {
            saveFix(bodyKey, vo);
            redisComponent.removeKey(runningKey);
            log.info("绘本单页补画完成 resourceId={} page={} status={}",
                    book.getResourceId(), page, vo.getStatus());
        }
    }

    /** 以最新 pages 重排 ext_json（保留 type 与其余字段） */
    private String rebuildExtJson(ResourceInfo book, JSONArray pages) {
        JSONObject ext = JSON.parseObject(book.getExtJson());
        if (ext == null) {
            ext = new JSONObject();
        }
        ext.put("type", "PICTURE_BOOK");
        ext.put("pages", pages);
        return ext.toJSONString();
    }

    private JSONArray parsePages(ResourceInfo book) {
        if (StringTools.isEmpty(book.getExtJson())) {
            return null;
        }
        JSONObject ext = JSON.parseObject(book.getExtJson());
        return ext == null ? null : ext.getJSONArray("pages");
    }

    private String fixBodyKey(String resourceId, int page) {
        return Constants.REDIS_KEY_PICTURE_BOOK_PAGE_FIX_PREFIX + resourceId + ":" + page;
    }

    private void saveFix(String bodyKey, PictureBookPageFixVO vo) {
        redisComponent.setString(bodyKey, JSON.toJSONString(vo), PAGE_FIX_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public PictureBookAudioTaskVO submitAudioTask(String userId, String resourceId, Integer page, String voice) {
        ResourceInfo book = assertOwned(userId, resourceId);
        if (!pictureBookAudioComponent.isTtsAvailable()) {
            throw new BusinessException("语音服务未配置：请先设置环境变量 NEXORA_MIMO_TTS_API_KEY");
        }
        if (voice != null && !voice.isBlank() && !TtsProvider.isValidVoice(voice.trim())) {
            throw new BusinessException("不支持的音色");
        }
        JSONArray pages = parsePages(book);
        if (pages == null || pages.isEmpty()) {
            throw new BusinessException("绘本页面数据异常");
        }
        if (page != null && (page < 0 || page >= pages.size())) {
            throw new BusinessException("页码不存在");
        }
        // 运行锁值为任务ID：进行中重复提交直接返回原任务，前端无感
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String bodyKey = Constants.REDIS_KEY_PICTURE_BOOK_AUDIO_TASK_PREFIX + taskId;
        String runningKey = Constants.REDIS_KEY_PICTURE_BOOK_AUDIO_RUNNING + resourceId;
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            String existing = redisComponent.getString(
                    Constants.REDIS_KEY_PICTURE_BOOK_AUDIO_TASK_PREFIX + runningTaskId);
            if (!StringTools.isEmpty(existing)) {
                return JSON.parseObject(existing, PictureBookAudioTaskVO.class);
            }
        }

        String finalVoice = pictureBookAudioComponent.resolveVoice(book.getStage(), voice);
        PictureBookAudioTaskVO vo = new PictureBookAudioTaskVO();
        vo.setTaskId(taskId);
        vo.setResourceId(resourceId);
        vo.setPage(page);
        vo.setVoice(finalVoice);
        vo.setStatus("RUNNING");
        vo.setMessage(page == null ? "正在按「" + finalVoice + "」音色录制全书旁白..." : "正在按「" + finalVoice + "」音色录制本页旁白...");
        redisComponent.setString(runningKey, taskId, AUDIO_TASK_RUNNING_MINUTES, TimeUnit.MINUTES);
        saveAudioTask(bodyKey, vo);
        final ResourceInfo bookRef = book;
        AUDIO_TASK_POOL.execute(() -> executeAudioTask(userId, bookRef, page, pages, finalVoice, taskId, bodyKey, runningKey));
        return vo;
    }

    @Override
    public PictureBookAudioTaskVO getAudioTask(String userId, String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        String body = redisComponent.getString(Constants.REDIS_KEY_PICTURE_BOOK_AUDIO_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(body)) {
            throw new BusinessException("任务不存在或已过期，请重新发起录制");
        }
        PictureBookAudioTaskVO vo = JSON.parseObject(body, PictureBookAudioTaskVO.class);
        assertOwned(userId, vo == null ? null : vo.getResourceId());
        return vo;
    }

    /**
     * 旁白合成执行：逐页 TTS → 覆盖 ext_json（旧音频文件同步删除）→ 一次性落库，进度实时写回任务体。
     * 整本任务跳过「已有同音色音频」的页；部分页失败不阻断，完成后提示可重试
     */
    private void executeAudioTask(String userId, ResourceInfo book, Integer page, JSONArray pages,
                                  String voice, String taskId, String bodyKey, String runningKey) {
        PictureBookAudioTaskVO vo = new PictureBookAudioTaskVO();
        vo.setTaskId(taskId);
        vo.setResourceId(book.getResourceId());
        vo.setPage(page);
        vo.setVoice(voice);
        vo.setStatus("RUNNING");
        try {
            ResourceInfo fresh = resourceInfoService.getResourceInfoByResourceId(book.getResourceId());
            if (fresh == null) {
                vo.setStatus("FAILED");
                vo.setMessage("绘本不存在或已删除");
                return;
            }
            UserInfo user = userInfoService.getUserInfoByUserId(userId);
            String email = user == null ? null : user.getEmail();

            // 计算本次需合成的页：单页任务=指定页；整本任务=缺音频页 + 异音色页
            List<Integer> targets = new java.util.ArrayList<>();
            if (page != null) {
                targets.add(page);
            } else {
                for (int i = 0; i < pages.size(); i++) {
                    JSONObject pageObj = pages.getJSONObject(i);
                    String audioFile = pageObj == null ? null : pageObj.getString("audioFile");
                    String audioVoice = pageObj == null ? null : pageObj.getString("audioVoice");
                    if (StringTools.isEmpty(audioFile) || !voice.equals(audioVoice)) {
                        targets.add(i);
                    }
                }
            }
            vo.setTotal(targets.size());
            vo.setCurrent(0);
            if (targets.isEmpty()) {
                vo.setStatus("COMPLETED");
                vo.setMessage("所有页面均已是「" + voice + "」音色旁白，无需重新生成");
                return;
            }

            int successCount = 0;
            String lastReason = null;
            for (int i = 0; i < targets.size(); i++) {
                int index = targets.get(i);
                JSONObject pageObj = pages.getJSONObject(index);
                String oldAudioFile = pageObj.getString("audioFile");
                String text = pageObj.getString("text");
                String audioFile = pictureBookAudioComponent.generatePageAudio(
                        email, text == null ? "" : text, voice, fresh.getResourceName(), index);
                if (audioFile == null) {
                    lastReason = pictureBookAudioComponent.getLastFailureReason();
                    log.warn("绘本旁白合成失败 resourceId={} page={} 第{}/{}页 原因={}",
                            book.getResourceId(), index + 1, i + 1, targets.size(), lastReason);
                } else {
                    if (!StringTools.isEmpty(oldAudioFile) && !oldAudioFile.equals(audioFile)) {
                        pictureBookAudioComponent.deleteAudioFile(oldAudioFile);
                    }
                    pageObj.put("audioFile", audioFile);
                    pageObj.put("audioVoice", voice);
                    successCount++;
                }
                vo.setCurrent(i + 1);
                vo.setMessage("正在录制旁白 " + (i + 1) + "/" + targets.size() + " 页...");
                saveAudioTask(bodyKey, vo);
            }

            // 一次性落库：整本任务回写书级 voice；全部页面都有音频时清掉历史 audioError
            // （以 fresh 的 ext 为底、嫁接本次变异后的 pages，书级字段在此之上追加）
            JSONObject ext = JSON.parseObject(fresh.getExtJson());
            if (ext == null) {
                ext = new JSONObject();
            }
            ext.put("type", "PICTURE_BOOK");
            ext.put("pages", pages);
            if (page == null) {
                ext.put("voice", voice);
            }
            boolean allPagesHaveAudio = true;
            for (int i = 0; i < pages.size(); i++) {
                JSONObject pageObj = pages.getJSONObject(i);
                if (pageObj == null || StringTools.isEmpty(pageObj.getString("audioFile"))) {
                    allPagesHaveAudio = false;
                    break;
                }
            }
            if (allPagesHaveAudio) {
                ext.remove("audioError");
            }
            ResourceInfo update = new ResourceInfo();
            update.setExtJson(ext.toJSONString());
            update.setUpdateTime(new Date());
            resourceInfoService.updateResourceInfoByResourceId(update, fresh.getResourceId());

            if (successCount == targets.size()) {
                vo.setStatus("COMPLETED");
                vo.setMessage(page == null ? "全书旁白已按「" + voice + "」音色生成完成"
                        : "第 " + (page + 1) + " 页旁白已按「" + voice + "」音色生成完成");
            } else if (successCount == 0) {
                vo.setStatus("FAILED");
                vo.setMessage(lastReason == null ? "旁白合成失败，请稍后重试" : lastReason);
            } else {
                vo.setStatus("COMPLETED");
                vo.setMessage("旁白已生成 " + successCount + "/" + targets.size()
                        + " 页，失败页可稍后在阅读页重试");
            }
        } catch (Exception e) {
            log.warn("绘本旁白合成任务执行异常 resourceId={} page={}", book.getResourceId(), page, e);
            vo.setStatus("FAILED");
            vo.setMessage("旁白合成失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        } finally {
            saveAudioTask(bodyKey, vo);
            redisComponent.removeKey(runningKey);
            log.info("绘本旁白合成任务完成 resourceId={} page={} voice={} status={}",
                    book.getResourceId(), page, voice, vo.getStatus());
        }
    }

    private void saveAudioTask(String bodyKey, PictureBookAudioTaskVO vo) {
        redisComponent.setString(bodyKey, JSON.toJSONString(vo), AUDIO_TASK_TTL_HOURS, TimeUnit.HOURS);
    }
}