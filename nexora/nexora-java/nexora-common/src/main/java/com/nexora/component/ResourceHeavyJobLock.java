package com.nexora.component;

import com.nexora.constants.Constants;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 「重活」互斥锁（资源转码 / 文档预览转换）。
 *
 * 背景：服务器 8G 内存，Elasticsearch 1g 堆 + 三个 JVM 常驻已吃掉约 3.8G；
 * LibreOffice 单次转 PDF 峰值可达 300-800MB，ffmpeg 转码同样是 CPU/内存双高，
 * 两者并发叠加有 OOM 风险（会把 ES 或某个 JVM 拖死）。因此约定：
 * 任一时刻只允许一个「重活」在跑，锁 TTL 兜底防止持锁进程崩溃后死锁。
 *
 * 使用约定：预览转换侧「抢不到就让路（任务回队，下轮再试）」；视频转码侧「有限等待，超时告警继续」。
 */
@Component
public class ResourceHeavyJobLock {

    private static final Logger log = LoggerFactory.getLogger(ResourceHeavyJobLock.class);

    /** 兜底 TTL：持锁者异常退出后最多 12 分钟自动释放（预览转换单次上限 5 分钟） */
    private static final long LOCK_TTL_MINUTES = 12;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 尝试获取重活锁（不等待）
     *
     * @param owner 持有者标识（如 preview:resourceId / transcode:resourceId），仅用于日志排查
     */
    public boolean tryLock(String owner) {
        return redisComponent.setIfAbsent(Constants.REDIS_KEY_RESOURCE_HEAVY_LOCK, owner,
                LOCK_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 有限等待获取重活锁：每 5 秒重试一次，最多等 waitSeconds 秒
     *
     * @return 是否拿到锁
     */
    public boolean awaitLock(String owner, long waitSeconds) {
        long deadline = System.currentTimeMillis() + waitSeconds * 1000L;
        while (true) {
            if (tryLock(owner)) {
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 释放重活锁（务必放在 finally 中）
     */
    public void unlock() {
        try {
            redisComponent.removeKey(Constants.REDIS_KEY_RESOURCE_HEAVY_LOCK);
        } catch (Exception e) {
            log.warn("释放重活锁失败（将由 TTL 兜底过期）", e);
        }
    }
}
