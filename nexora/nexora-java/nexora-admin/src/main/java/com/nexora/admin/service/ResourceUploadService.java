package com.nexora.admin.service;

import com.nexora.admin.vo.ResourceUploadSessionVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源分片上传业务
 */
public interface ResourceUploadService {

    /**
     * 创建上传会话：落库为上传中并缓存到 Redis
     */
    ResourceUploadSessionVO prepare(String resourceName, String resourceType, String originalFileName,
                                    Long fileSize, String directoryId, String stage);

    /**
     * 写入分片；最后一片写入后自动入队合并
     */
    void uploadShard(String uploadId, Integer shardIndex, MultipartFile shard);

    /**
     * 客户端主动放弃上传（关闭 / 刷新页面、分片重试耗尽）：立即把资源收敛为「失败」并清理会话，
     * 不必再等僵尸清扫窗口
     */
    void abandon(String uploadId, String resourceId);

    /**
     * 合并分片并异步处理（视频转 HLS / 生成封面 / 获取时长，其他文件搬入正式目录）
     */
    void process(String uploadId);
}
