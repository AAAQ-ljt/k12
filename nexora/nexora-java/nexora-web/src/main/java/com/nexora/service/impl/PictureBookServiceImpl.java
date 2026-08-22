package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
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

/**
 * 学生绘本业务实现：绘本产物存 resource_info.ext_json（PICTURE_BOOK）；生成由异步任务编排
 */
@Service
public class PictureBookServiceImpl implements PictureBookService {

    private static final Logger log = LoggerFactory.getLogger(PictureBookServiceImpl.class);

    @Value("${project.folder}")
    private String projectFolder;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

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
}