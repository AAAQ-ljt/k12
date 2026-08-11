package com.smart.campus.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.redis.ResourceTaskQueueRedisComponent;
import com.smart.campus.redis.ResourceUploadSessionRedisComponent;
import com.smart.campus.config.AppConfig;
import com.smart.campus.entity.constants.Constants;
import com.smart.campus.entity.dto.*;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.entity.enums.ResourceNodeTypeEnum;
import com.smart.campus.entity.enums.ResourceStatusEnum;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.query.ResourceInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.vo.ResourceTreeNodeVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.utils.FfmpegUtils;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceAdminBiz {

    private static final String ORDER_BY_ASC = "r.resource_id desc";

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern(DateTimePatternEnum.YYYYMM.getPattern());

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ResourceUploadSessionRedisComponent resourceUploadSessionRedisComponent;

    @Resource
    private ResourceTaskQueueRedisComponent resourceTaskQueueRedisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private FfmpegUtils ffmpegUtils;

    public PaginationResultVO<ResourceInfo> loadDataList(ResourceInfoQuery query) {
        LoginUserVO loginUser = getRequiredTeacher();
        ResourceInfoQuery request = query == null ? new ResourceInfoQuery() : query;
        request.setTeacherId(loginUser.getUserId());
        request.setParentId(normalizeParentId(request.getParentId()));
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(Constants.DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(Constants.DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(ORDER_BY_ASC);
        return resourceInfoService.findListByPage(request);
    }

    public List<ResourceTreeNodeVO> loadFolderTree() {
        LoginUserVO loginUser = getRequiredTeacher();
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setTeacherId(loginUser.getUserId());
        query.setNodeType(ResourceNodeTypeEnum.FOLDER.getCode());
        query.setOrderBy("r.resource_id asc");
        List<ResourceInfo> folderList = resourceInfoService.findListByParam(query);
        Map<Integer, ResourceTreeNodeVO> nodeMap = new LinkedHashMap<>();
        for (ResourceInfo item : folderList) {
            nodeMap.put(item.getResourceId(), buildFolderNode(item));
        }
        List<ResourceTreeNodeVO> rootList = new ArrayList<>();
        for (ResourceInfo item : folderList) {
            ResourceTreeNodeVO node = nodeMap.get(item.getResourceId());
            if (item.getParentId() == null || item.getParentId() == Constants.ROOT_PARENT_ID) {
                rootList.add(node);
                continue;
            }
            ResourceTreeNodeVO parent = nodeMap.get(item.getParentId());
            if (parent == null) {
                rootList.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return rootList;
    }

    public List<ResourceInfo> getResourceListByIds(String ids) {
        LoginUserVO loginUser = getRequiredTeacher();
        List<Integer> idList = StringTools.convertIds2List(ids);
        if (idList.isEmpty()) {
            return List.of();
        }
        List<String> normalizedIdList = idList.stream()
                .map(String::valueOf)
                .toList();
        return resourceInfoService.getResourceInfoByResourceIdList(normalizedIdList).stream()
                .filter(item -> Objects.equals(item.getTeacherId(), loginUser.getUserId()))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResourceInfo addFolder(AddFolderDTO dto) {
        LoginUserVO loginUser = getRequiredTeacher();
        Integer parentId = normalizeParentId(dto.getParentId());
        requireValidParentFolder(loginUser.getUserId(), parentId);
        String resourceName = StringTools.trim(dto.getResourceName());

        ResourceInfo folder = new ResourceInfo();
        folder.setTeacherId(loginUser.getUserId());
        folder.setParentId(parentId);
        folder.setNodeType(ResourceNodeTypeEnum.FOLDER.getCode());
        folder.setResourceName(resourceName);
        folder.setFileSize(0L);
        folder.setStatus(ResourceStatusEnum.SUCCESS.getCode());
        resourceInfoService.add(folder);
        return folder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rename(RenameResourceDTO dto) {
        LoginUserVO loginUser = getRequiredTeacher();
        ResourceInfo resourceInfo = requireOwnedResource(loginUser.getUserId(), dto.getResourceId());
        String resourceName = StringTools.trim(dto.getResourceName());

        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setResourceName(resourceName);
        resourceInfoService.updateResourceInfoByResourceId(updateBean, resourceInfo.getResourceId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void move(MoveResourceDTO dto) {
        LoginUserVO loginUser = getRequiredTeacher();
        ResourceInfo resourceInfo = requireOwnedResource(loginUser.getUserId(), dto.getResourceId());
        Integer targetParentId = normalizeParentId(dto.getTargetParentId());
        requireValidParentFolder(loginUser.getUserId(), targetParentId);
        if (Objects.equals(resourceInfo.getParentId(), targetParentId)) {
            return;
        }
        if (ResourceNodeTypeEnum.FOLDER.getCode().equals(resourceInfo.getNodeType())) {
            checkMoveCycle(loginUser.getUserId(), resourceInfo.getResourceId(), targetParentId);
        }

        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setParentId(targetParentId);
        resourceInfoService.updateResourceInfoByResourceId(updateBean, resourceInfo.getResourceId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        LoginUserVO loginUser = getRequiredTeacher();
        List<Integer> idList = StringTools.convertIds2List(ids);
        List<String> normalizedIdList = idList.stream()
                .map(String::valueOf)
                .toList();
        List<ResourceInfo> resourceList = resourceInfoService.getResourceInfoByResourceIdList(normalizedIdList);
        if (resourceList.size() != idList.size()) {
            throw new BusinessException("存在无效资源记录，无法删除");
        }
        for (ResourceInfo item : resourceList) {
            if (!Objects.equals(item.getTeacherId(), loginUser.getUserId())) {
                throw new BusinessException("存在不属于当前老师的资源，无法删除");
            }
        }

        List<Integer> folderIdList = resourceList.stream()
                .filter(item -> ResourceNodeTypeEnum.FOLDER.getCode().equals(item.getNodeType()))
                .map(ResourceInfo::getResourceId)
                .toList();
        if (!folderIdList.isEmpty()) {
            ResourceInfoQuery childQuery = new ResourceInfoQuery();
            childQuery.setTeacherId(loginUser.getUserId());
            childQuery.setParentIdList(folderIdList);
            childQuery.setSimplePage(new SimplePage(0, 1));
            List<ResourceInfo> childList = resourceInfoService.findListByParam(childQuery);
            if (!childList.isEmpty()) {
                throw new BusinessException("所选目录下仍存在子目录或资源，无法删除");
            }
        }

        ResourceInfoQuery deleteQuery = new ResourceInfoQuery();
        deleteQuery.setTeacherId(loginUser.getUserId());
        deleteQuery.setResourceIdList(idList);
        resourceInfoService.deleteByParam(deleteQuery);
        resourceList.stream()
                .filter(item -> ResourceNodeTypeEnum.RESOURCE.getCode().equals(item.getNodeType()))
                .forEach(item -> {
                    deleteStoredFile(item.getFilePath());
                    deleteStoredFile(item.getCoverPath());
                });
    }

    public String initUpload(UploadInitDTO dto) {
        LoginUserVO loginUser = getRequiredTeacher();
        ResourceUploadSessionDTO session = buildAndPersistUploadSession(loginUser, dto);
        saveUploadSession(session);
        return session.getUploadId();
    }

    private ResourceUploadSessionDTO buildAndPersistUploadSession(LoginUserVO loginUser, UploadInitDTO dto) {
        ResourceUploadSessionDTO session = new ResourceUploadSessionDTO();
        session.setUploadId(UUID.randomUUID().toString().replace("-", ""));
        session.setTeacherId(loginUser.getUserId());
        session.setParentId(normalizeParentId(dto.getParentId()));
        session.setResourceName(StringTools.trim(dto.getResourceName()));
        session.setResourceType(dto.getResourceType());
        session.setFileName(dto.getFileName());
        session.setFileSize(dto.getFileSize());
        session.setMergeQueued(false);
        if (dto.getResourceId() == null) {
            session.setReUpload(false);
            session.setResourceId(createUploadRecord(loginUser, session));
        } else {
            session.setReUpload(true);
            session.setResourceId(dto.getResourceId());
            prepareReuploadRecord(loginUser, session);
        }
        return session;
    }

    private Integer createUploadRecord(LoginUserVO loginUser, ResourceUploadSessionDTO session) {
        Integer parentId = normalizeParentId(session.getParentId());
        requireValidParentFolder(loginUser.getUserId(), parentId);
        ResourceInfo resourceInfo = buildUploadResourceInfo(
                loginUser,
                session,
                parentId,
                session.getResourceName(),
                session.getResourceType()
        );
        resourceInfoService.add(resourceInfo);
        return resourceInfo.getResourceId();
    }

    private void prepareReuploadRecord(LoginUserVO loginUser, ResourceUploadSessionDTO session) {
        ResourceInfo resourceInfo = requireOwnedResource(loginUser.getUserId(), session.getResourceId());
        session.setOldFilePath(resourceInfo.getFilePath());
        session.setOldCoverPath(resourceInfo.getCoverPath());
        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setResourceName(session.getResourceName());
        updateBean.setResourceType(session.getResourceType());
        updateBean.setFileName(session.getFileName());
        updateBean.setFileSuffix(getFileSuffix(session.getFileName()));
        updateBean.setFileSize(session.getFileSize());
        updateBean.setStatus(ResourceStatusEnum.UPLOADING.getCode());
        updateBean.setDuration(0);
        updateBean.setCoverPath("");
        resourceInfoService.updateResourceInfoByResourceId(updateBean, resourceInfo.getResourceId());
    }

    private void saveUploadSession(ResourceUploadSessionDTO session) {
        resourceUploadSessionRedisComponent.save(
                session.getUploadId(),
                session,
                Duration.ofMinutes(appConfig.getResourceUploadSessionTtlMinutes())
        );
    }

    public void uploadChunk(String uploadId, Integer chunkIndex, Integer chunkCount, MultipartFile file) throws IOException {
        LoginUserVO loginUser = getRequiredTeacher();

        //获取上传信息
        ResourceUploadSessionDTO session = getValidUploadSession(loginUser.getUserId(), uploadId);
        //校验分片
        validateUploadChunkParams(chunkIndex, chunkCount, file);
        // 所有分片都按 uploadId 写入同一个临时目录，服务端只认 uploadId，不依赖前端重复传文件元信息。
        saveChunk(session.getUploadId(), chunkIndex, file);
        if (chunkIndex.equals(chunkCount - 1)) {
            // 最后一片上传完成后，由后端直接进入后续处理流程。
            queueUploadProcessAfterLastChunk(loginUser, session, chunkCount);
        }
    }

    private LoginUserVO getRequiredTeacher() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return loginUser;
    }

    private void validateUploadChunkParams(Integer chunkIndex, Integer chunkCount, MultipartFile file) {
        if (chunkIndex == null || chunkIndex < 0 || chunkCount == null || chunkCount < 1 || chunkIndex >= chunkCount || file == null || file.isEmpty()) {
            throw new BusinessException("分片参数不正确");
        }
    }

    private void saveChunk(String uploadId, Integer chunkIndex, MultipartFile file) throws IOException {
        //获取分片目录
        Path chunkDir = getChunkDir(uploadId);
        //创建分片目录
        Files.createDirectories(chunkDir);
        //生成分片文件
        Path chunkPath = chunkDir.resolve(String.valueOf(chunkIndex));
        //保存分片文件
        file.transferTo(chunkPath);
    }

    public void handleQueueTask(ResourceQueueTaskDTO task) throws IOException, InterruptedException {
        if (task == null || task.getTaskType() == null) {
            return;
        }
        switch (task.getTaskType()) {
            case ResourceQueueTaskDTO.TYPE_MERGE_UPLOAD,
                 ResourceQueueTaskDTO.TYPE_REUPLOAD_RESOURCE -> processMergeTask(task);
            case ResourceQueueTaskDTO.TYPE_TRANSCODE_VIDEO -> processTranscodeTask(task);
            default -> {
            }
        }
    }

    public void markTaskFailed(ResourceQueueTaskDTO task) {
        if (task == null) {
            return;
        }
        if (task.getUploadId() != null && !task.getUploadId().isBlank()) {
            deleteQuietly(getChunkDir(task.getUploadId()));
            deleteUploadSession(task.getUploadId());
        }
        if (task.getMergedTempPath() != null && !task.getMergedTempPath().isBlank()) {
            deleteQuietly(Paths.get(task.getMergedTempPath()));
        }
        if (task.getResourceId() == null) {
            return;
        }
        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setStatus(resolveFailedStatus(task).getCode());
        resourceInfoService.updateResourceInfoByResourceId(updateBean, task.getResourceId());
    }

    private void processMergeTask(ResourceQueueTaskDTO task) throws IOException, InterruptedException {
        ResourceUploadSessionDTO session = getUploadSession(task.getUploadId());
        if (session == null) {
            throw new BusinessException("上传会话已失效，无法合并文件");
        }

        // 第一步：先把所有分片在 temp/merged 下合并成完整文件。
        int chunkCount = session.getChunkCount() == null ? resolveChunkCount(session.getFileSize()) : session.getChunkCount();
        String mergedTempPath = mergeChunks(task.getUploadId(), session.getFileName(), chunkCount);
        if (Objects.equals(session.getResourceType(), 1)) {
            // 视频合并完成后先把状态切到“转码中”，再异步做编码校验、取时长和 HLS 切片。
            ResourceInfo updateBean = new ResourceInfo();
            updateBean.setStatus(ResourceStatusEnum.TRANSCODING.getCode());
            resourceInfoService.updateResourceInfoByResourceId(updateBean, task.getResourceId());
            pushTask(buildTranscodeTask(
                    task.getResourceId(),
                    mergedTempPath,
                    session.getFileName(),
                    session.getOldFilePath(),
                    session.getOldCoverPath()
            ));
            deleteUploadSession(task.getUploadId());
            return;
        }

        // 普通文件合并后直接复制到正式目录，上传链路到这里就结束，只更新状态即可。
        String finalPath = copyMergedFileToSource(
                mergedTempPath,
                session.getFileName(),
                getFileSuffix(session.getFileName())
        );
        String coverPath = resolveStaticResourceCoverPath(session.getResourceType(), finalPath);
        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setFilePath(finalPath);
        updateBean.setCoverPath(coverPath);
        updateBean.setStatus(ResourceStatusEnum.SUCCESS.getCode());
        resourceInfoService.updateResourceInfoByResourceId(updateBean, task.getResourceId());
        deleteUploadSession(task.getUploadId());
        deleteStoredFile(session.getOldFilePath());
        deleteStoredFile(session.getOldCoverPath());
    }

    private void processTranscodeTask(ResourceQueueTaskDTO task) throws IOException, InterruptedException {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(task.getResourceId());
        if (resourceInfo == null) {
            deleteQuietly(Paths.get(task.getMergedTempPath()));
            return;
        }

        // 视频文件在临时目录完成合并后，先用 ffprobe 读取时长和编码，再决定是否先转标准 MP4。
        String finalPath;
        Integer duration = 0;
        String coverPath = null;
        try {
            duration = ffmpegUtils.probeDurationSeconds(task.getMergedTempPath());
            coverPath = generateVideoCover(task.getMergedTempPath());
            finalPath = convertVideoToTs(
                    task.getMergedTempPath(),
                    task.getSourceFileName()
            );
        } finally {
            deleteQuietly(Paths.get(task.getMergedTempPath()));
        }
        ResourceInfo updateBean = new ResourceInfo();
        updateBean.setFilePath(finalPath);
        updateBean.setCoverPath(coverPath);
        updateBean.setFileSuffix(Constants.FILE_SUFFIX_M3U8);
        updateBean.setDuration(duration);
        updateBean.setStatus(ResourceStatusEnum.SUCCESS.getCode());
        resourceInfoService.updateResourceInfoByResourceId(updateBean, task.getResourceId());
        deleteStoredFile(task.getOldFilePath());
        deleteStoredFile(task.getOldCoverPath());
    }


    private ResourceUploadSessionDTO getValidUploadSession(Integer teacherId, String uploadId) {
        ResourceUploadSessionDTO session = getUploadSession(uploadId);
        if (session == null || !Objects.equals(session.getTeacherId(), teacherId)) {
            throw new BusinessException("上传会话不存在或已失效");
        }
        return session;
    }


    private ResourceInfo buildUploadResourceInfo(
            LoginUserVO loginUser,
            ResourceUploadSessionDTO session,
            Integer parentId,
            String resourceName,
            Integer resourceType
    ) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setTeacherId(loginUser.getUserId());
        resourceInfo.setParentId(parentId);
        resourceInfo.setNodeType(ResourceNodeTypeEnum.RESOURCE.getCode());
        resourceInfo.setResourceName(resourceName);
        resourceInfo.setResourceType(resourceType);
        resourceInfo.setFileName(session.getFileName());
        resourceInfo.setFileSuffix(getFileSuffix(session.getFileName()));
        resourceInfo.setFileSize(session.getFileSize());
        resourceInfo.setStatus(ResourceStatusEnum.UPLOADING.getCode());
        resourceInfo.setDuration(0);
        return resourceInfo;
    }

    private ResourceTreeNodeVO buildFolderNode(ResourceInfo item) {
        ResourceTreeNodeVO node = new ResourceTreeNodeVO();
        node.setId(item.getResourceId());
        node.setResourceId(item.getResourceId());
        node.setParentId(item.getParentId());
        node.setResourceName(item.getResourceName());
        node.setNodeType(item.getNodeType());
        node.setResourceType(item.getResourceType());
        node.setStatus(item.getStatus());
        return node;
    }

    private ResourceQueueTaskDTO buildQueueTask(String taskType, String uploadId, Integer resourceId) {
        ResourceQueueTaskDTO task = new ResourceQueueTaskDTO();
        task.setTaskType(taskType);
        task.setUploadId(uploadId);
        task.setResourceId(resourceId);
        return task;
    }

    private ResourceQueueTaskDTO buildTranscodeTask(
            Integer resourceId,
            String mergedTempPath,
            String sourceFileName,
            String oldFilePath,
            String oldCoverPath
    ) {
        ResourceQueueTaskDTO task = buildQueueTask(ResourceQueueTaskDTO.TYPE_TRANSCODE_VIDEO, null, resourceId);
        task.setMergedTempPath(mergedTempPath);
        task.setSourceFileName(sourceFileName);
        task.setOldFilePath(oldFilePath);
        task.setOldCoverPath(oldCoverPath);
        return task;
    }


    private Integer normalizeParentId(Integer parentId) {
        return parentId == null ? Constants.ROOT_PARENT_ID : parentId;
    }


    private void queueUploadProcessAfterLastChunk(LoginUserVO loginUser, ResourceUploadSessionDTO session, Integer chunkCount) {
        if (Boolean.TRUE.equals(session.getMergeQueued())) {
            return;
        }
        session.setChunkCount(chunkCount);
        session.setMergeQueued(true);
        // 先把最后一片后的会话状态写回 Redis，再入异步队列，避免消费者取到旧会话导致状态和临时文件处理错乱。
        saveUploadSession(session);
        validateQueuedResource(loginUser, session);
        pushTask(buildQueueTask(
                Boolean.TRUE.equals(session.getReUpload()) ? ResourceQueueTaskDTO.TYPE_REUPLOAD_RESOURCE : ResourceQueueTaskDTO.TYPE_MERGE_UPLOAD,
                session.getUploadId(),
                session.getResourceId()
        ));
    }

    private void validateQueuedResource(LoginUserVO loginUser, ResourceUploadSessionDTO session) {
        if (Boolean.TRUE.equals(session.getReUpload())) {
            requireOwnedResource(loginUser.getUserId(), session.getResourceId());
            return;
        }
        requireValidParentFolder(loginUser.getUserId(), normalizeParentId(session.getParentId()));
    }

    private ResourceUploadSessionDTO getUploadSession(String uploadId) {
        return resourceUploadSessionRedisComponent.get(uploadId, ResourceUploadSessionDTO.class);
    }

    private void deleteUploadSession(String uploadId) {
        resourceUploadSessionRedisComponent.delete(uploadId);
    }

    private void pushTask(ResourceQueueTaskDTO task) {
        resourceTaskQueueRedisComponent.offer(JSON.toJSONString(task));
    }

    private ResourceInfo requireOwnedResource(Integer teacherId, Integer resourceId) {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resourceInfo == null || !Objects.equals(resourceInfo.getTeacherId(), teacherId)) {
            throw new BusinessException("资源不存在");
        }
        return resourceInfo;
    }

    private void requireValidParentFolder(Integer teacherId, Integer parentId) {
        if (parentId == null || parentId == Constants.ROOT_PARENT_ID) {
            return;
        }
        ResourceInfo parent = requireOwnedResource(teacherId, parentId);
        if (!ResourceNodeTypeEnum.FOLDER.getCode().equals(parent.getNodeType())) {
            throw new BusinessException("目标父节点不是目录");
        }
    }


    private void checkMoveCycle(Integer teacherId, Integer currentId, Integer targetParentId) {
        if (targetParentId == null || targetParentId == Constants.ROOT_PARENT_ID) {
            return;
        }
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setTeacherId(teacherId);
        query.setNodeType(ResourceNodeTypeEnum.FOLDER.getCode());
        List<ResourceInfo> folderList = resourceInfoService.findListByParam(query);
        Map<Integer, ResourceInfo> folderMap = folderList.stream()
                .collect(Collectors.toMap(ResourceInfo::getResourceId, item -> item));
        Integer cursor = targetParentId;
        while (cursor != null && cursor != Constants.ROOT_PARENT_ID) {
            if (Objects.equals(cursor, currentId)) {
                throw new BusinessException("不能将目录移动到自己的子目录下");
            }
            ResourceInfo parent = folderMap.get(cursor);
            cursor = parent == null ? Constants.ROOT_PARENT_ID : parent.getParentId();
        }
    }

    private int resolveChunkCount(Long fileSize) {
        return Math.max((int) Math.ceil((double) fileSize / Constants.RESOURCE_CHUNK_SIZE), 1);
    }


    private String mergeChunks(String uploadId, String fileName, int chunkCount) throws IOException {
        Path mergedDir = getMergedDir();
        Files.createDirectories(mergedDir);
        String suffix = FilenameUtils.getExtension(fileName);
        String mergedFileName = uploadId + (suffix == null || suffix.isBlank() ? "" : "." + suffix);
        Path mergedPath = mergedDir.resolve(mergedFileName);
        // 分片按照 chunkIndex 顺序写入一个完整临时文件，合并完成后立即删除分片目录。
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(mergedPath.toFile()))) {
            for (int index = 0; index < chunkCount; index++) {
                Path chunkPath = getChunkDir(uploadId).resolve(String.valueOf(index));
                try (InputStream inputStream = Files.newInputStream(chunkPath)) {
                    inputStream.transferTo(outputStream);
                }
            }
        }
        deleteQuietly(getChunkDir(uploadId));
        return mergedPath.toString();
    }

    private String copyMergedFileToSource(String sourcePath, String fileName, String fileSuffix) throws IOException {
        String relativePath = buildFinalRelativePath(fileName, fileSuffix);
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(Paths.get(sourcePath), targetPath);
        deleteQuietly(Paths.get(sourcePath));
        return normalizeRelativePath(relativePath);
    }

    private String resolveStaticResourceCoverPath(Integer resourceType, String finalFilePath) throws IOException {
        if (Objects.equals(resourceType, 2)) {
            return generateImageCover(finalFilePath);
        }
        return null;
    }

    private String convertVideoToTs(String sourcePath, String sourceFileName) throws IOException, InterruptedException {
        String relativePath = buildVideoPlaylistRelativePath();
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        Files.createDirectories(targetPath.getParent());
        Path transcodeSourcePath = Paths.get(sourcePath);
        Path tempMp4Path = null;
        if (ffmpegUtils.requiresMp4Transcode(sourcePath)) {
            tempMp4Path = buildTempMp4Path(sourceFileName);
            Files.createDirectories(tempMp4Path.getParent());
            ffmpegUtils.convertToMp4(sourcePath, tempMp4Path.toString());
            transcodeSourcePath = tempMp4Path;
        }
        try {
            ffmpegUtils.convertToTsSegments(transcodeSourcePath.toString(), targetPath.toString());
        } finally {
            if (tempMp4Path != null) {
                deleteQuietly(tempMp4Path);
            }
        }
        return normalizeRelativePath(relativePath);
    }

    private String generateImageCover(String finalFilePath) throws IOException {
        String relativePath = buildCoverRelativePath();
        Path sourcePath = Paths.get(appConfig.getProjectFolder(), finalFilePath);
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            ffmpegUtils.generateImageThumbnail(sourcePath.toString(), targetPath.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("图片缩略图生成被中断", e);
        }
        return normalizeRelativePath(relativePath);
    }

    private String generateVideoCover(String sourcePath) throws IOException, InterruptedException {
        String relativePath = buildCoverRelativePath();
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        Files.createDirectories(targetPath.getParent());
        ffmpegUtils.generateVideoCover(sourcePath, targetPath.toString());
        return normalizeRelativePath(relativePath);
    }

    private ResourceStatusEnum resolveFailedStatus(ResourceQueueTaskDTO task) {
        return ResourceQueueTaskDTO.TYPE_TRANSCODE_VIDEO.equals(task.getTaskType())
                ? ResourceStatusEnum.TRANSCODE_FAILED
                : ResourceStatusEnum.UPLOAD_FAILED;
    }

    private String getFileSuffix(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    private void deleteStoredFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        if (targetPath.getFileName() != null && Constants.FILE_SUFFIX_M3U8.equalsIgnoreCase(FilenameUtils.getExtension(targetPath.getFileName().toString()))) {
            deleteQuietly(targetPath.getParent());
            return;
        }
        deleteQuietly(targetPath);
    }

    private void deleteQuietly(Path path) {
        try {
            FileUtils.deleteQuietly(path.toFile());
        } catch (Exception ignored) {
        }
    }

    private Path getChunkDir(String uploadId) {
        return Paths.get(appConfig.getProjectFolder(), folderName(Constants.FOLDER_FILE), folderName(Constants.FOLDER_TEMP), uploadId);
    }

    private Path getMergedDir() {
        return Paths.get(appConfig.getProjectFolder(), folderName(Constants.FOLDER_FILE), folderName(Constants.FOLDER_TEMP), "merged");
    }

    private Path buildTempMp4Path(String fileName) {
        String baseName = FilenameUtils.getBaseName(fileName);
        String tempFileName = baseName + "-" + UUID.randomUUID().toString().replace("-", "") + ".mp4";
        return getMergedDir().resolve(tempFileName);
    }

    private String buildFinalRelativePath(String fileName, String fileSuffix) {
        String suffix = fileSuffix == null || fileSuffix.isBlank() ? getFileSuffix(fileName) : fileSuffix;
        String ext = suffix == null || suffix.isBlank() ? "" : "." + suffix;
        String finalName = UUID.randomUUID().toString().replace("-", "") + ext;
        return Paths.get(
                folderName(Constants.FOLDER_FILE),
                folderName(Constants.FOLDER_SOURCE),
                LocalDate.now().format(MONTH_FORMATTER),
                finalName
        ).toString();
    }

    private String buildVideoPlaylistRelativePath() {
        String folderName = UUID.randomUUID().toString().replace("-", "");
        return Paths.get(
                folderName(Constants.FOLDER_FILE),
                folderName(Constants.FOLDER_SOURCE),
                LocalDate.now().format(MONTH_FORMATTER),
                folderName,
                "index." + Constants.FILE_SUFFIX_M3U8
        ).toString();
    }

    private String buildCoverRelativePath() {
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        return Paths.get(
                folderName(Constants.FOLDER_FILE),
                "cover",
                LocalDate.now().format(MONTH_FORMATTER),
                fileName
        ).toString();
    }

    private String folderName(String folder) {
        return folder.replace("/", "").replace("\\", "");
    }

    private String normalizeRelativePath(String relativePath) {
        return relativePath.replace("\\", "/");
    }
}
