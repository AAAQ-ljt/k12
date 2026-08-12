package com.nexora.admin.controller;

import com.nexora.admin.utils.FileUploadUtils;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 资源管理 Controller（含文件上传/下载/删除）
 */
@RestController("resourceInfoController")
@RequestMapping("/resourceInfo")
public class ResourceInfoController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceInfoController.class);

    private final ResourceInfoService resourceInfoService;

    @Value("${project.folder}")
    private String projectFolder;

    public ResourceInfoController(ResourceInfoService resourceInfoService) {
        this.resourceInfoService = resourceInfoService;
    }

    /**
     * 分页查询资源列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO loadDataList(ResourceInfoQuery query) {
        return getSuccessResponseVO(resourceInfoService.findListByPage(query));
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/getInfo")
    public ResponseVO getInfo(@RequestParam String resourceId) {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resourceInfo == null) {
            throw new BusinessException("资源不存在");
        }
        return getSuccessResponseVO(resourceInfo);
    }

    /**
     * 新增资源（含文件上传）
     */
    @PostMapping("/add")
    public ResponseVO add(@RequestParam("file") MultipartFile file,
                          @RequestParam("resourceName") String resourceName,
                          @RequestParam("resourceType") String resourceType,
                          @RequestParam(value = "stage", required = false) String stage,
                          @RequestParam(value = "knowledgePointId", required = false) String knowledgePointId,
                          @RequestParam(value = "tags", required = false) String tags,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam(value = "cover", required = false) String cover,
                          @RequestParam(value = "duration", required = false) Integer duration) {
        // 保存文件到本地
        String relativePath = FileUploadUtils.saveFile(file, projectFolder);

        // 构建资源对象
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName(resourceName);
        resourceInfo.setResourceType(resourceType);
        resourceInfo.setStage(stage);
        resourceInfo.setKnowledgePointId(knowledgePointId);
        resourceInfo.setTags(tags);
        resourceInfo.setDescription(description);
        resourceInfo.setCover(cover);
        resourceInfo.setDuration(duration);
        resourceInfo.setFilePath(relativePath);
        resourceInfo.setFileSize(file.getSize());
        resourceInfo.setSource(0);
        resourceInfo.setStatus(1);
        resourceInfo.setCreateTime(new Date());
        resourceInfo.setUpdateTime(new Date());

        resourceInfoService.add(resourceInfo);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑资源（不含文件）
     */
    @PutMapping("/update")
    public ResponseVO update(@RequestBody ResourceInfo resourceInfo) {
        if (resourceInfo.getResourceId() == null || resourceInfo.getResourceId().isEmpty()) {
            throw new BusinessException("资源ID不能为空");
        }
        resourceInfo.setUpdateTime(new Date());
        resourceInfoService.updateResourceInfoByResourceId(resourceInfo, resourceInfo.getResourceId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除资源（同时删除本地文件）
     */
    @DeleteMapping("/del")
    public ResponseVO del(@RequestParam String resourceId) {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resourceInfo == null) {
            throw new BusinessException("资源不存在");
        }
        // 删除本地文件
        if (resourceInfo.getFilePath() != null && !resourceInfo.getFilePath().isEmpty()) {
            FileUploadUtils.deleteFile(projectFolder, resourceInfo.getFilePath());
        }
        // 删除数据库记录
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
        return getSuccessResponseVO(null);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(@RequestParam String resourceId, HttpServletResponse response) {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resourceInfo == null) {
            throw new BusinessException("资源不存在");
        }
        if (resourceInfo.getFilePath() == null || resourceInfo.getFilePath().isEmpty()) {
            throw new BusinessException("文件路径不存在");
        }
        String fullPath = FileUploadUtils.getFullPath(projectFolder, resourceInfo.getFilePath());
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }
        String fileName = resourceInfo.getResourceName();
        if (fileName == null || fileName.isEmpty()) {
            fileName = file.getName();
        } else {
            String extension = FileUploadUtils.getFileExtension(file.getName());
            if (!extension.isEmpty() && !fileName.contains(".")) {
                fileName = fileName + "." + extension;
            }
        }
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=\"" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
            response.setContentLengthLong(file.length());
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (IOException e) {
            logger.error("文件下载失败", e);
            throw new BusinessException("文件下载失败");
        }
    }
}
