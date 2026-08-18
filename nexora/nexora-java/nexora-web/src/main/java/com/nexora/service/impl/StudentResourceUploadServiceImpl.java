package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.dto.StudentResourceUploadSession;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentResourceUploadService;
import com.nexora.utils.StringTools;
import com.nexora.vo.StudentUploadSessionVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 学生个人资源分片上传实现：分片落盘、Redis 队列异步合并与处理
 */
@Service
public class StudentResourceUploadServiceImpl implements StudentResourceUploadService {

    private static final Logger log = LoggerFactory.getLogger(StudentResourceUploadServiceImpl.class);
    private static final int DEFAULT_SHARD_SIZE = 5 * 1024 * 1024;
    private static final List<String> VIDEO_EXTENSIONS = List.of("mp4", "avi", "mov", "mkv", "flv", "wmv", "webm", "m4v", "ts");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("md", "txt", "docx", "doc", "pdf", "ppt", "pptx");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg");

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.temp-dir}")
    private String resourceTempDir;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    @Value("${resource.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${resource.ffprobe-path:ffprobe}")
    private String ffprobePath;

    @Value("${resource.upload-session-ttl-minutes:120}")
    private long sessionTtlMinutes;

    @Value("${resource.student-quota-mb:300}")
    private long studentQuotaMb;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public StudentUploadSessionVO prepare(String resourceName, String resourceType, String originalFileName,
                                          Long fileSize, String directoryId, String stage, String ownerId) {
        if (StringTools.isEmpty(resourceName) || StringTools.isEmpty(resourceType)) {
            throw new BusinessException("资源名称和类型不能为空");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new BusinessException("文件大小不合法");
        }
        validateStudentUpload(resourceType, originalFileName, fileSize, ownerId);
        String resourceId = StringTools.getRandomNumber(Constants.LENGTH_15);
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        int totalShards = (int) Math.max(1, Math.ceil(fileSize * 1.0 / DEFAULT_SHARD_SIZE));
        String monthDir = new SimpleDateFormat("yyyyMM").format(new Date());
        String tempRelativeDir = resourceTempDir + "/student/" + monthDir + "/" + uploadId;
        Path tempAbsDir = Paths.get(projectFolder, tempRelativeDir);
        try {
            Files.createDirectories(tempAbsDir);
        } catch (IOException e) {
            log.error("创建学生上传临时目录失败", e);
            throw new BusinessException("创建上传临时目录失败");
        }

        ResourceInfo bean = new ResourceInfo();
        bean.setResourceId(resourceId);
        bean.setResourceName(resourceName);
        bean.setResourceType(resourceType);
        bean.setDirectoryId(normalizeDirectoryId(directoryId));
        bean.setStage(stage);
        bean.setOwnerId(ownerId);
        bean.setFileSize(fileSize);
        bean.setSource(0);
        bean.setStatus(0);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        resourceInfoService.add(bean);

        StudentResourceUploadSession session = new StudentResourceUploadSession();
        session.setUploadId(uploadId);
        session.setResourceId(resourceId);
        session.setResourceName(resourceName);
        session.setResourceType(resourceType);
        session.setOriginalFileName(originalFileName);
        session.setFileSize(fileSize);
        session.setDirectoryId(normalizeDirectoryId(directoryId));
        session.setStage(stage);
        session.setOwnerId(ownerId);
        session.setShardSize(DEFAULT_SHARD_SIZE);
        session.setTotalShards(totalShards);
        session.setTempDir(tempRelativeDir);
        redisComponent.setObject(Constants.REDIS_KEY_RESOURCE_UPLOAD_SESSION + uploadId,
                JSON.toJSONString(session), sessionTtlMinutes, TimeUnit.MINUTES);

        StudentUploadSessionVO vo = new StudentUploadSessionVO();
        vo.setUploadId(uploadId);
        vo.setResourceId(resourceId);
        vo.setShardSize(DEFAULT_SHARD_SIZE);
        vo.setTotalShards(totalShards);
        vo.setUploadedShardIndexes(readUploadedShards(uploadId));
        return vo;
    }

    @Override
    public void uploadShard(String uploadId, Integer shardIndex, MultipartFile shard) {
        if (StringTools.isEmpty(uploadId) || shardIndex == null || shard == null || shard.isEmpty()) {
            throw new BusinessException("上传参数不完整");
        }
        StudentResourceUploadSession session = getSession(uploadId);
        if (session == null) {
            throw new BusinessException("上传会话不存在或已过期，请重新选择文件");
        }
        if (shardIndex < 0 || shardIndex >= session.getTotalShards()) {
            throw new BusinessException("分片序号非法");
        }
        Path shardPath = Paths.get(projectFolder, session.getTempDir(), shardIndex + ".part");
        try {
            Files.createDirectories(shardPath.getParent());
            shard.transferTo(shardPath.toFile());
        } catch (Exception e) {
            log.error("学生分片写入失败 uploadId={} shardIndex={}", uploadId, shardIndex, e);
            throw new BusinessException("分片写入失败");
        }

        String shardKey = Constants.REDIS_KEY_RESOURCE_UPLOAD_SHARDS + uploadId;
        redisComponent.addToSet(shardKey, String.valueOf(shardIndex));
        Set<Object> uploaded = redisComponent.getSetMembers(shardKey);
        if (uploaded != null && uploaded.size() >= session.getTotalShards()) {
            String mergedKey = Constants.REDIS_KEY_RESOURCE_UPLOAD_MERGED + uploadId;
            if (redisComponent.setIfAbsent(mergedKey, "1", sessionTtlMinutes, TimeUnit.MINUTES)) {
                redisComponent.leftPush(Constants.REDIS_KEY_STUDENT_RESOURCE_UPLOAD_QUEUE, uploadId);
            }
        }
    }

    @Override
    public void process(String uploadId) {
        StudentResourceUploadSession session = getSession(uploadId);
        if (session == null) {
            log.warn("学生资源处理跳过，会话不存在 uploadId={}", uploadId);
            return;
        }
        Path tempAbsDir = Paths.get(projectFolder, session.getTempDir());
        Path mergedPath = tempAbsDir.resolve("merged.bin");
        try {
            mergeShards(tempAbsDir, session.getTotalShards(), mergedPath);
            if (session.getFileSize() != null && Files.size(mergedPath) != session.getFileSize()) {
                throw new BusinessException("分片合并后大小不一致");
            }
            String monthDir = new SimpleDateFormat("yyyyMM").format(new Date());
            String extension = extractExtension(session.getOriginalFileName());
            String filePath;
            String hlsPath = null;
            String cover = null;
            Integer duration = null;
            if (isVideo(session.getResourceType(), extension)) {
                String targetRelativeDir = resourceFileDir + "/student/" + monthDir + "/"
                        + UUID.randomUUID().toString().replace("-", "");
                Path targetAbsDir = Paths.get(projectFolder, targetRelativeDir);
                Files.createDirectories(targetAbsDir);
                String hlsRelative = targetRelativeDir + "/index.m3u8";
                String coverRelative = targetRelativeDir + "/cover.jpg";
                String originalRelative = targetRelativeDir + "/original" + extension;
                String segmentPattern = Paths.get(projectFolder, targetRelativeDir, "segment_%03d.ts").toString();
                executeCommand(List.of(ffmpegPath, "-y", "-i", mergedPath.toString(),
                        "-map", "0:v:0", "-map", "0:a?",
                        "-c:v", "libx264", "-preset", "veryfast", "-c:a", "aac",
                        "-hls_time", "10", "-hls_list_size", "0",
                        "-hls_segment_filename", segmentPattern,
                        Paths.get(projectFolder, hlsRelative).toString()));
                executeCommand(List.of(ffmpegPath, "-y", "-i", mergedPath.toString(),
                        "-ss", "1", "-vframes", "1", "-q:v", "2",
                        Paths.get(projectFolder, coverRelative).toString()));
                duration = probeDuration(mergedPath);
                Files.copy(mergedPath, Paths.get(projectFolder, originalRelative), StandardCopyOption.REPLACE_EXISTING);
                filePath = originalRelative;
                hlsPath = hlsRelative;
                cover = coverRelative;
            } else {
                Path targetAbsDir = Paths.get(projectFolder, resourceFileDir, "student", monthDir);
                Files.createDirectories(targetAbsDir);
                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                Path targetFile = targetAbsDir.resolve(fileName);
                Files.copy(mergedPath, targetFile, StandardCopyOption.REPLACE_EXISTING);
                filePath = resourceFileDir + "/student/" + monthDir + "/" + fileName;
            }
            ResourceInfo update = new ResourceInfo();
            update.setFilePath(filePath);
            update.setHlsPath(hlsPath);
            update.setCover(cover);
            update.setDuration(duration);
            update.setStatus(1);
            update.setUpdateTime(new Date());
            resourceInfoService.updateResourceInfoByResourceId(update, session.getResourceId());
            if ("DOCUMENT".equalsIgnoreCase(session.getResourceType())) {
                createStudentKnowledgeDoc(session);
            }
            log.info("学生资源处理完成 resourceId={} filePath={}", session.getResourceId(), filePath);
        } catch (Exception e) {
            log.error("学生资源处理失败 uploadId={} resourceId={}", uploadId, session.getResourceId(), e);
            ResourceInfo update = new ResourceInfo();
            update.setStatus(2);
            update.setUpdateTime(new Date());
            resourceInfoService.updateResourceInfoByResourceId(update, session.getResourceId());
        } finally {
            deleteDirectory(tempAbsDir);
            removeRedisSession(uploadId);
        }
    }

    private void createStudentKnowledgeDoc(StudentResourceUploadSession session) {
        Date now = new Date();
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setDocId(UUID.randomUUID().toString().replace("-", ""));
        doc.setTitle(session.getResourceName());
        doc.setStage(session.getStage());
        doc.setOwnerId(session.getOwnerId());
        // 学生上传文档暂未关联知识点，统一用 0 占位，避免违反 NOT NULL 约束
        doc.setKnowledgePointId("0");
        doc.setDifficulty(1);
        doc.setDataType("KNOWLEDGE");
        doc.setContent("");
        doc.setSourceType(1);
        doc.setSourceResourceId(session.getResourceId());
        doc.setVectorStatus(0);
        doc.setChunkCount(0);
        doc.setStatus(1);
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        knowledgeDocService.add(doc);
        redisComponent.leftPush(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_QUEUE, doc.getDocId());
    }

    private void mergeShards(Path tempDir, int totalShards, Path target) throws IOException {
        try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < totalShards; i++) {
                Path part = tempDir.resolve(i + ".part");
                if (!Files.exists(part)) {
                    throw new BusinessException("分片缺失: " + i);
                }
                Files.copy(part, out);
            }
        }
    }

    private void executeCommand(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
            try (InputStream input = process.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        });
        boolean finished = process.waitFor(30, TimeUnit.MINUTES);
        String output = "";
        try {
            output = outputFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // 输出读取失败不影响退出码判断
        }
        if (!finished) {
            process.destroyForcibly();
            throw new BusinessException("命令执行超时: " + String.join(" ", command));
        }
        if (process.exitValue() != 0) {
            String message = output.length() > 1000 ? output.substring(output.length() - 1000) : output;
            throw new BusinessException("命令执行失败: " + message);
        }
    }

    private Integer probeDuration(Path videoPath) {
        try {
            Process process = new ProcessBuilder(ffprobePath, "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1", videoPath.toString())
                    .redirectErrorStream(true).start();
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (InputStream input = process.getInputStream()) {
                    return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return "";
                }
            });
            if (!process.waitFor(2, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                return null;
            }
            String output = outputFuture.get(5, TimeUnit.SECONDS).trim();
            return (int) Math.ceil(Double.parseDouble(output));
        } catch (Exception e) {
            log.warn("获取视频时长失败 path={}", videoPath, e);
            return null;
        }
    }

    private Set<Integer> readUploadedShards(String uploadId) {
        Set<Object> members = redisComponent.getSetMembers(Constants.REDIS_KEY_RESOURCE_UPLOAD_SHARDS + uploadId);
        Set<Integer> indexes = new HashSet<>();
        if (members != null) {
            members.forEach(item -> indexes.add(Integer.parseInt(String.valueOf(item))));
        }
        return indexes;
    }

    private StudentResourceUploadSession getSession(String uploadId) {
        Object value = redisComponent.getObject(Constants.REDIS_KEY_RESOURCE_UPLOAD_SESSION + uploadId);
        if (value == null) {
            return null;
        }
        String json = value instanceof String stringValue ? stringValue : JSON.toJSONString(value);
        return JSON.parseObject(json, StudentResourceUploadSession.class);
    }

    private void removeRedisSession(String uploadId) {
        redisComponent.removeKey(Constants.REDIS_KEY_RESOURCE_UPLOAD_SESSION + uploadId);
        redisComponent.removeKey(Constants.REDIS_KEY_RESOURCE_UPLOAD_SHARDS + uploadId);
        redisComponent.removeKey(Constants.REDIS_KEY_RESOURCE_UPLOAD_MERGED + uploadId);
    }

    private String normalizeDirectoryId(String directoryId) {
        if (StringTools.isEmpty(directoryId) || "root".equals(directoryId)) {
            return null;
        }
        return directoryId;
    }

    private void validateStudentUpload(String resourceType, String originalFileName, Long fileSize, String ownerId) {
        String extension = extractExtension(originalFileName);
        if (!extension.isEmpty() && extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if ("IMAGE".equalsIgnoreCase(resourceType)) {
            if (!IMAGE_EXTENSIONS.contains(extension)) {
                throw new BusinessException("仅支持上传 jpg/jpeg/png/gif/webp/bmp/svg 图片");
            }
        } else if ("DOCUMENT".equalsIgnoreCase(resourceType)) {
            if (!DOCUMENT_EXTENSIONS.contains(extension)) {
                throw new BusinessException("仅支持上传 md/txt/docx/doc/pdf/ppt/pptx 文档");
            }
        } else if ("VIDEO".equalsIgnoreCase(resourceType)) {
            if (!VIDEO_EXTENSIONS.contains(extension)) {
                throw new BusinessException("仅支持上传 mp4/avi/mov/mkv/flv/wmv/webm/m4v/ts 视频");
            }
        } else {
            throw new BusinessException("学生个人知识库仅支持文档、图片和视频");
        }

        long quotaBytes = studentQuotaMb * 1024 * 1024;
        Long usedBytes = resourceInfoService.getUsedSizeByOwner(ownerId);
        long used = usedBytes == null ? 0L : usedBytes;
        if (used + fileSize > quotaBytes) {
            throw new BusinessException("存储空间不足，每人额度为 " + studentQuotaMb + "MB");
        }
    }

    private boolean isVideo(String resourceType, String extension) {
        if ("VIDEO".equalsIgnoreCase(resourceType)) {
            return true;
        }
        return VIDEO_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private void deleteDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 临时文件清理失败不影响主流程
                }
            });
        } catch (IOException ignored) {
            // 临时目录清理失败不影响主流程
        }
    }
}
