package com.nexora.admin.controller;

import com.nexora.admin.dto.ResourceMoveDTO;
import com.nexora.admin.service.ResourceUploadService;
import com.nexora.admin.vo.ResourceUploadSessionVO;
import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 资源文件管理 Controller
 */
@RestController
@RequestMapping("/resourceInfo")
public class ResourceInfoController extends ABaseController {

    private static final Pattern VIDEO_SEGMENT_PATTERN = Pattern.compile("^segment_\\d{3}\\.ts$");

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ResourceUploadService resourceUploadService;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    /**
     * 分页查询资源
     */
    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<ResourceInfo>> loadDataList(ResourceInfoQuery query) {
        return getSuccessResponseVO(resourceInfoService.findListByPage(query));
    }

    /**
     * 获取 HLS 播放列表
     */
    @GetMapping("/video/{resourceId}/index.m3u8")
    public ResponseEntity<FileSystemResource> videoPlaylist(@PathVariable String resourceId) throws IOException {
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || StringTools.isEmpty(resource.getHlsPath())) {
            return ResponseEntity.notFound().build();
        }
        Path playlist = resolveResourcePath(resource.getHlsPath());
        if (!Files.exists(playlist)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(new FileSystemResource(playlist));
    }

    /**
     * 获取 HLS 视频分片
     */
    @GetMapping("/video/{resourceId}/{segment}")
    public ResponseEntity<FileSystemResource> videoSegment(@PathVariable String resourceId,
                                                          @PathVariable String segment) throws IOException {
        if (!VIDEO_SEGMENT_PATTERN.matcher(segment).matches()) {
            return ResponseEntity.notFound().build();
        }
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || StringTools.isEmpty(resource.getHlsPath())) {
            return ResponseEntity.notFound().build();
        }
        Path hlsFile = resolveResourcePath(resource.getHlsPath());
        Path hlsDir = hlsFile.getParent();
        if (hlsDir == null) {
            return ResponseEntity.notFound().build();
        }
        Path segmentFile = hlsDir.resolve(segment).normalize();
        if (!segmentFile.startsWith(hlsDir) || !Files.exists(segmentFile)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(new FileSystemResource(segmentFile));
    }

    /**
     * 图片预览
     */
    @GetMapping("/image/{resourceId}")
    public ResponseEntity<FileSystemResource> image(@PathVariable String resourceId) throws IOException {
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || StringTools.isEmpty(resource.getFilePath())) {
            return ResponseEntity.notFound().build();
        }
        Path imageFile = resolveResourcePath(resource.getFilePath());
        if (!Files.exists(imageFile)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(resolveImageMediaType(imageFile))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(new FileSystemResource(imageFile));
    }

    /**
     * 通用文件预览（供 jit-viewer 直接拉取原始文档）
     */
    @GetMapping("/file/{resourceId}")
    public ResponseEntity<FileSystemResource> file(@PathVariable String resourceId) throws IOException {
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || StringTools.isEmpty(resource.getFilePath())) {
            return ResponseEntity.notFound().build();
        }
        Path file = resolveResourcePath(resource.getFilePath());
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(resolveFileMediaType(file))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(new FileSystemResource(file));
    }

    /**
     * 下载原始文件
     */
    @GetMapping("/download/{resourceId}")
    public ResponseEntity<FileSystemResource> download(@PathVariable String resourceId) throws IOException {
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || StringTools.isEmpty(resource.getFilePath())) {
            return ResponseEntity.notFound().build();
        }
        Path file = resolveResourcePath(resource.getFilePath());
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        String fileName = StringTools.isEmpty(resource.getResourceName())
                ? file.getFileName().toString()
                : resource.getResourceName();
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .body(new FileSystemResource(file));
    }

    /**
     * 上传资源
     */
    @PostMapping("/add")
    public ResponseVO<Void> add(@RequestParam("file") MultipartFile file,
                                @RequestParam String resourceName,
                                @RequestParam String resourceType,
                                @RequestParam(required = false) String directoryId,
                                @RequestParam(required = false) String stage) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String relativeDir = resourceFileDir + "/" + dateDir;
        Path targetDir = Paths.get(projectFolder, relativeDir);
        Files.createDirectories(targetDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = targetDir.resolve(fileName);
        file.transferTo(target.toFile());

        ResourceInfo bean = new ResourceInfo();
        bean.setResourceId(StringTools.getRandomNumber(Constants.LENGTH_15));
        bean.setResourceName(resourceName);
        bean.setResourceType(resourceType);
        bean.setDirectoryId(directoryId);
        bean.setStage(stage);
        bean.setFilePath(relativeDir + "/" + fileName);
        bean.setFileSize(file.getSize());
        bean.setSource(0);
        bean.setStatus(1);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        resourceInfoService.add(bean);
        return getSuccessResponseVO(null);
    }

    /**
     * 创建分片上传会话
     */
    @PostMapping("/prepareUpload")
    public ResponseVO<ResourceUploadSessionVO> prepareUpload(@RequestParam String resourceName,
                                                             @RequestParam String resourceType,
                                                             @RequestParam String fileName,
                                                             @RequestParam Long fileSize,
                                                             @RequestParam(required = false) String directoryId,
                                                             @RequestParam(required = false) String stage) {
        return getSuccessResponseVO(resourceUploadService.prepare(resourceName, resourceType, fileName,
                fileSize, directoryId, stage));
    }

    /**
     * 上传分片，最后一片由后端自动触发合并
     */
    @PostMapping("/uploadShard")
    public ResponseVO<Void> uploadShard(@RequestParam String uploadId,
                                        @RequestParam Integer shardIndex,
                                        @RequestParam("file") MultipartFile file) {
        resourceUploadService.uploadShard(uploadId, shardIndex, file);
        return getSuccessResponseVO(null);
    }

    /**
     * 修改资源（重命名 / 转移等）
     */
    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody ResourceInfo bean) {
        if (StringTools.isEmpty(bean.getResourceId())) {
            throw new BusinessException("资源ID不能为空");
        }
        bean.setUpdateTime(new Date());
        resourceInfoService.updateResourceInfoByResourceId(bean, bean.getResourceId());
        return getSuccessResponseVO(null);
    }

    /**
     * 批量转移文件目录
     */
    @PutMapping("/move")
    public ResponseVO<Void> move(@RequestBody ResourceMoveDTO dto) {
        if (dto == null || dto.getResourceIds() == null || dto.getResourceIds().isEmpty()) {
            throw new BusinessException("请选择要转移的文件");
        }
        if (StringTools.isEmpty(dto.getDirectoryId())) {
            throw new BusinessException("目标目录不能为空");
        }
        Date now = new Date();
        List<ResourceInfo> list = dto.getResourceIds().stream().map(resourceId -> {
            ResourceInfo item = new ResourceInfo();
            item.setResourceId(resourceId);
            item.setDirectoryId(dto.getDirectoryId());
            item.setUpdateTime(now);
            return item;
        }).toList();
        resourceInfoService.updateDirectoryBatch(list);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String resourceId) {
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
        return getSuccessResponseVO(null);
    }

    private ResourceInfo getReadyResource(String resourceId) {
        if (StringTools.isEmpty(resourceId)) {
            return null;
        }
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
            return null;
        }
        return resource;
    }

    private Path resolveResourcePath(String relativePath) throws IOException {
        Path root = Paths.get(projectFolder).toAbsolutePath().normalize();
        Path path = Paths.get(projectFolder, relativePath).toAbsolutePath().normalize();
        if (!path.startsWith(root)) {
            throw new IOException("非法文件路径");
        }
        return path;
    }

    private MediaType resolveImageMediaType(Path imageFile) {
        String name = imageFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (name.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (name.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (name.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (name.endsWith(".bmp")) {
            return MediaType.parseMediaType("image/bmp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private MediaType resolveFileMediaType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (name.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (name.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        if (name.endsWith(".pptx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        }
        if (name.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        }
        if (name.endsWith(".xls")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        }
        if (name.endsWith(".ppt")) {
            return MediaType.parseMediaType("application/vnd.ms-powerpoint");
        }
        if (name.endsWith(".md") || name.endsWith(".markdown")) {
            return MediaType.parseMediaType("text/markdown");
        }
        if (name.endsWith(".txt")) {
            return MediaType.parseMediaType("text/plain");
        }
        if (name.endsWith(".csv")) {
            return MediaType.parseMediaType("text/csv");
        }
        return resolveImageMediaType(file);
    }
}
