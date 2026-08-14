package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.StudentResourceVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 学生端课程教材资源 Controller：登录后可查看列表/详情，媒体流接口开放供播放器直连。
 */
@RestController
@RequestMapping("/resourceInfo")
@GlobalInterceptor(checkLogin = true)
public class StudentResourceController extends ABaseController {

    private static final Pattern VIDEO_SEGMENT_PATTERN = Pattern.compile("^segment_\\d{3}\\.ts$");

    @Resource
    private ResourceInfoService resourceInfoService;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir:resource/files}")
    private String resourceFileDir;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<StudentResourceVO>> loadDataList(ResourceInfoQuery query) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(20);
        }
        query.setStatus(1);
        if (current != null && !StringTools.isEmpty(current.getStage())) {
            query.setStage(current.getStage());
        }
        PaginationResultVO<ResourceInfo> page = resourceInfoService.findListByPage(query);
        List<StudentResourceVO> list = page.getList().stream().map(this::toVO).toList();
        PaginationResultVO<StudentResourceVO> result = new PaginationResultVO<>(
                page.getTotalCount(), page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return getSuccessResponseVO(result);
    }

    @GetMapping("/getInfo")
    public ResponseVO<StudentResourceVO> getInfo(@RequestParam String resourceId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        ResourceInfo resource = getReadyResource(resourceId);
        if (resource == null || !stageMatches(resource.getStage(), current == null ? null : current.getStage())) {
            throw new BusinessException("资源不存在或暂不可用");
        }
        return getSuccessResponseVO(toVO(resource));
    }

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

    private boolean stageMatches(String resourceStage, String userStage) {
        if (StringTools.isEmpty(resourceStage) || StringTools.isEmpty(userStage)) {
            return true;
        }
        return resourceStage.equals(userStage);
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
        vo.setSource(resource.getSource());
        vo.setStatus(resource.getStatus());
        vo.setCreateTime(resource.getCreateTime());
        vo.setUpdateTime(resource.getUpdateTime());
        return vo;
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
