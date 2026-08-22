package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.component.PictureBookGenerateComponent;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import com.nexora.vo.PictureBookVO;
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

/**
 * 学生绘本业务实现：故事 + 插图产物存 resource_info.ext_json（PICTURE_BOOK），单页插图失败降级纯文字
 */
@Service
public class PictureBookServiceImpl implements PictureBookService {

    private static final Logger log = LoggerFactory.getLogger(PictureBookServiceImpl.class);

    @Value("${project.folder}")
    private String projectFolder;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private PictureBookGenerateComponent pictureBookGenerateComponent;

    @Override
    public PictureBookVO generate(String userId, String stage, String topic) {
        if (StringTools.isEmpty(topic)) {
            throw new BusinessException("请先输入绘本主题");
        }
        // 1. 生成故事分页文案
        PictureBookGenerateComponent.StoryScript story = pictureBookGenerateComponent.generateStory(stage, topic);
        if (story.pages().isEmpty()) {
            throw new BusinessException("绘本故事生成失败，请稍后重试");
        }
        // 2. 逐页生成插图（失败降级纯文字，不阻断），插图按学生邮箱分目录存储
        UserInfo user = userInfoService.getUserInfoByUserId(userId);
        String email = user == null ? null : user.getEmail();
        JSONArray pages = new JSONArray();
        for (int i = 0; i < story.pages().size(); i++) {
            String imageFile = pictureBookGenerateComponent.generatePageImage(
                    email, stage, story.pages().get(i), story.title(), i);
            JSONObject page = new JSONObject();
            page.put("text", story.pages().get(i));
            page.put("imageFile", imageFile == null ? "" : imageFile);
            pages.add(page);
        }
        JSONObject ext = new JSONObject();
        ext.put("type", "PICTURE_BOOK");
        ext.put("pages", pages);

        // 3. 落资源中心（PICTURE_BOOK 类型，owner 隔离）
        String resourceId = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        ResourceInfo bean = new ResourceInfo();
        bean.setResourceId(resourceId);
        bean.setResourceName(story.title());
        bean.setResourceType("PICTURE_BOOK");
        bean.setExtJson(ext.toJSONString());
        bean.setStage(null);
        bean.setOwnerId(userId);
        bean.setSource(1);
        bean.setStatus(1);
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        resourceInfoService.add(bean);

        log.info("绘本生成完成 userId={} title={} pages={}", userId, story.title(), story.pages().size());
        return toVO(bean);
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

    private PictureBookVO toVO(ResourceInfo resource) {
        PictureBookVO vo = new PictureBookVO();
        vo.setResourceId(resource.getResourceId());
        vo.setResourceName(resource.getResourceName());
        vo.setStage(resource.getStage());
        vo.setExtJson(resource.getExtJson());
        vo.setCreateTime(resource.getCreateTime());
        return vo;
    }
}