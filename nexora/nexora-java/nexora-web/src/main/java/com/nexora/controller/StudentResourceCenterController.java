package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.constants.Constants;
import com.nexora.dto.StudentDirectorySortDTO;
import com.nexora.dto.StudentResourceUpdateDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceDirectoryService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
import com.nexora.service.StudentResourceUploadService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.StudentStorageVO;
import com.nexora.vo.StudentResourceVO;
import com.nexora.vo.StudentUploadSessionVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 学生个人资源中心：目录、文件、分片上传，全部按 ownerId 隔离。
 */
@RestController
@RequestMapping("/studentResource")
@GlobalInterceptor(checkLogin = true)
public class StudentResourceCenterController extends ABaseController {

    @Resource
    private ResourceDirectoryService resourceDirectoryService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentResourceUploadService studentResourceUploadService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

    @GetMapping("/storage")
    public ResponseVO<StudentStorageVO> storage() {
        return getSuccessResponseVO(studentKnowledgeBaseService.getStorageInfo(currentUserId()));
    }

    @PostMapping("/initKnowledgeBase")
    public ResponseVO<Void> initKnowledgeBase() {
        studentKnowledgeBaseService.initIfAbsent(currentUserId());
        return getSuccessResponseVO(null);
    }

    @GetMapping("/directoryTree")
    public ResponseVO<List<ResourceDirectory>> directoryTree() {
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(currentUserId());
        query.setOrderBy("parent_id asc, sort asc");
        return getSuccessResponseVO(resourceDirectoryService.findListByParam(query));
    }

    @PostMapping("/directory")
    public ResponseVO<String> addDirectory(@RequestBody ResourceDirectory bean) {
        if (StringTools.isEmpty(bean.getDirName())) {
            throw new BusinessException("目录名称不能为空");
        }
        if (StringTools.isEmpty(bean.getParentId()) || "root".equals(bean.getParentId())) {
            bean.setParentId("0");
        } else {
            assertOwnedDirectory(bean.getParentId());
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setParentId(bean.getParentId());
        query.setOwnerId(currentUserId());
        query.setOrderBy("sort desc");
        List<ResourceDirectory> list = resourceDirectoryService.findListByParam(query);
        int maxSort = list.isEmpty() || list.get(0).getSort() == null ? 0 : list.get(0).getSort();
        bean.setDirId(StringTools.getRandomNumber(Constants.LENGTH_15));
        bean.setOwnerId(currentUserId());
        bean.setSort(maxSort + 1);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        resourceDirectoryService.add(bean);
        return getSuccessResponseVO(bean.getDirId());
    }

    @PutMapping("/directory")
    public ResponseVO<Void> updateDirectory(@RequestBody ResourceDirectory bean) {
        if (StringTools.isEmpty(bean.getDirId()) || StringTools.isEmpty(bean.getDirName())) {
            throw new BusinessException("目录ID和名称不能为空");
        }
        assertOwnedDirectory(bean.getDirId());
        ResourceDirectory update = new ResourceDirectory();
        update.setDirName(bean.getDirName());
        update.setUpdateTime(new Date());
        resourceDirectoryService.updateResourceDirectoryByDirId(update, bean.getDirId());
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/directory")
    public ResponseVO<Void> deleteDirectory(@RequestParam String dirId) {
        assertOwnedDirectory(dirId);
        if ("0".equals(dirId) || "root".equals(dirId)) {
            throw new BusinessException("根目录不能删除");
        }
        ResourceDirectoryQuery childQuery = new ResourceDirectoryQuery();
        childQuery.setParentId(dirId);
        childQuery.setOwnerId(currentUserId());
        if (resourceDirectoryService.findCountByParam(childQuery) > 0) {
            throw new BusinessException("目录下存在子目录，不能删除");
        }
        ResourceInfoQuery fileQuery = new ResourceInfoQuery();
        fileQuery.setDirectoryId(dirId);
        fileQuery.setOwnerId(currentUserId());
        if (resourceInfoService.findCountByParam(fileQuery) > 0) {
            throw new BusinessException("目录下存在文件，不能删除");
        }
        resourceDirectoryService.deleteResourceDirectoryByDirId(dirId);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/directory/sort")
    public ResponseVO<Void> sortDirectory(@RequestBody StudentDirectorySortDTO dto) {
        if (dto == null || dto.getDirIds() == null || dto.getDirIds().isEmpty()) {
            throw new BusinessException("排序数据不能为空");
        }
        List<ResourceDirectory> list = new ArrayList<>();
        for (int i = 0; i < dto.getDirIds().size(); i++) {
            String dirId = dto.getDirIds().get(i);
            assertOwnedDirectory(dirId);
            ResourceDirectory item = new ResourceDirectory();
            item.setDirId(dirId);
            item.setSort(i);
            list.add(item);
        }
        resourceDirectoryService.updateSortBatch(list);
        return getSuccessResponseVO(null);
    }

    @GetMapping("/list")
    public ResponseVO<PaginationResultVO<StudentResourceVO>> list(ResourceInfoQuery query) {
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(20);
        }
        query.setOwnerId(currentUserId());
        query.setOrderBy("create_time desc");
        PaginationResultVO<ResourceInfo> page = resourceInfoService.findListByPage(query);
        List<StudentResourceVO> list = page.getList().stream().map(this::toVO).toList();
        PaginationResultVO<StudentResourceVO> result = new PaginationResultVO<>(
                page.getTotalCount(), page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return getSuccessResponseVO(result);
    }

    @GetMapping("/getInfo")
    public ResponseVO<StudentResourceVO> getInfo(@RequestParam String resourceId) {
        return getSuccessResponseVO(toVO(assertOwnedResource(resourceId)));
    }

    @PostMapping("/prepareUpload")
    public ResponseVO<StudentUploadSessionVO> prepareUpload(@RequestParam String resourceName,
                                                            @RequestParam String resourceType,
                                                            @RequestParam String fileName,
                                                            @RequestParam Long fileSize,
                                                            @RequestParam(required = false) String directoryId) {
        if (!StringTools.isEmpty(directoryId)) {
            assertOwnedDirectory(directoryId);
        }
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(studentResourceUploadService.prepare(resourceName, resourceType, fileName,
                fileSize, directoryId, current.getStage(), current.getUserId()));
    }

    @PostMapping("/uploadShard")
    public ResponseVO<Void> uploadShard(@RequestParam String uploadId,
                                        @RequestParam Integer shardIndex,
                                        @RequestParam("file") MultipartFile file) {
        studentResourceUploadService.uploadShard(uploadId, shardIndex, file);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody StudentResourceUpdateDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getResourceId())) {
            throw new BusinessException("资源ID不能为空");
        }
        ResourceInfo current = assertOwnedResource(dto.getResourceId());
        if (!StringTools.isEmpty(dto.getDirectoryId())) {
            assertOwnedDirectory(dto.getDirectoryId());
        }
        ResourceInfo update = new ResourceInfo();
        if (!StringTools.isEmpty(dto.getResourceName())) {
            update.setResourceName(dto.getResourceName());
        }
        update.setDescription(dto.getDescription());
        update.setDirectoryId(StringTools.isEmpty(dto.getDirectoryId())
                ? current.getDirectoryId() : dto.getDirectoryId());
        update.setUpdateTime(new Date());
        resourceInfoService.updateResourceInfoByResourceId(update, dto.getResourceId());
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String resourceId) {
        assertOwnedResource(resourceId);
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
        return getSuccessResponseVO(null);
    }

    private ResourceDirectory assertOwnedDirectory(String dirId) {
        ResourceDirectory directory = resourceDirectoryService.getResourceDirectoryByDirId(dirId);
        if (directory == null || !currentUserId().equals(directory.getOwnerId())) {
            throw new BusinessException("目录不存在或无权操作");
        }
        return directory;
    }

    private ResourceInfo assertOwnedResource(String resourceId) {
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || !currentUserId().equals(resource.getOwnerId())) {
            throw new BusinessException("资源不存在或无权操作");
        }
        return resource;
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }

    private StudentResourceVO toVO(ResourceInfo resource) {
        StudentResourceVO vo = new StudentResourceVO();
        vo.setResourceId(resource.getResourceId());
        vo.setResourceName(resource.getResourceName());
        vo.setResourceType(resource.getResourceType());
        vo.setTags(resource.getTags());
        vo.setDescription(resource.getDescription());
        vo.setFileSize(resource.getFileSize());
        vo.setCover(resource.getCover());
        vo.setDuration(resource.getDuration());
        vo.setStage(resource.getStage());
        vo.setKnowledgePointId(resource.getKnowledgePointId());
        vo.setDirectoryId(resource.getDirectoryId());
        vo.setSource(resource.getSource());
        vo.setStatus(resource.getStatus());
        vo.setCreateTime(resource.getCreateTime());
        vo.setUpdateTime(resource.getUpdateTime());
        return vo;
    }
}
