package com.nexora.service;

import com.nexora.vo.StudentUploadSessionVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生个人资源分片上传业务
 */
public interface StudentResourceUploadService {

    /**
     * 创建上传会话：落库为上传中并缓存到 Redis
     */
    StudentUploadSessionVO prepare(String resourceName, String resourceType, String originalFileName,
                                   Long fileSize, String directoryId, String stage, String ownerId, String email);

    /**
     * 写入分片；最后一片写入后自动入队合并
     */
    void uploadShard(String uploadId, Integer shardIndex, MultipartFile shard);

    /**
     * 合并分片并异步处理（视频转 HLS / 生成封面 / 获取时长，其他文件搬入正式目录）
     */
    void process(String uploadId);
}
