package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.component.PictureBookGenerateComponent;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
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

    /** 补画任务体 TTL / 运行锁 TTL */
    private static final long PAGE_FIX_TTL_HOURS = 1;
    private static final long PAGE_FIX_RUNNING_MINUTES = 10;

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
        deletePageImages(book);
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
    }

    @Override
    public String pageImageFile(String resourceId, int pageIndex) {
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
        String imageFile = page == null ? null : page.getString("imageFile");
        if (StringTools.isEmpty(imageFile)) {
            return null;
        }
        return imageFile;
    }

    private void deletePageImages(ResourceInfo book) {
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
                String imageFile = page == null ? null : page.getString("imageFile");
                if (!StringTools.isEmpty(imageFile)) {
                    Files.deleteIfExists(Paths.get(projectFolder, imageFile));
                }
            }
        } catch (Exception e) {
            log.warn("删除绘本图片文件失败 resourceId={}", book.getResourceId(), e);
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
}