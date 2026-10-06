package com.nexora.admin.component;

import com.nexora.component.RedisComponent;
import com.nexora.component.ResourcePreviewRenderer;
import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.service.ResourceInfoService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * 资源在线预览产物生成消费者（统一由 nexora-admin 承担，web 端只入队）。
 *
 * 为什么集中在管理端：LibreOffice 转换是「重活」（单次峰值 300-800MB，8G 机器上必须串行），
 * 由单一进程消费可天然串行，避免 admin/web 各跑一个 LibreOffice 把内存打爆；
 * 两端文件同处一个 project.folder，产物谁生成谁读取都一样。
 *
 * 两类来源：
 * 1) 队列 {@link Constants#REDIS_KEY_RESOURCE_PREVIEW_QUEUE}：资源处理完成后入队（新上传）；
 * 2) 启动自愈 + 每 10 分钟扫描：补齐历史存量（含上线的这批课件）与失败重试，每轮最多 1 个。
 *
 * 与视频转码共用 {@link com.nexora.component.ResourceHeavyJobLock}：抢不到锁就跳过本轮，交存量扫描重试。
 */
@Component
public class ResourcePreviewQueueListener {

    private static final Logger log = LoggerFactory.getLogger(ResourcePreviewQueueListener.class);

    /** 单轮存量扫描最多处理数：转一个 88MB 课件要 1-2 分钟，不宜一次堆太多 */
    private static final int SWEEP_BATCH = 1;

    /** 存量扫描候选集合上限（按更新时间倒序取最近的 N 个文档资源） */
    private static final int SWEEP_CANDIDATE_LIMIT = 100;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ResourcePreviewRenderer resourcePreviewRenderer;

    @Resource
    private ResourceInfoService resourceInfoService;

    /**
     * 启动自愈：补齐存量 Office 资源的预览产物
     */
    @PostConstruct
    public void init() {
        sweepMissingPreviews();
    }

    /**
     * 定时扫描：每 10 分钟补一批（失败次数达上限的不再重试）
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 120_000)
    public void sweepMissingPreviews() {
        try {
            ResourceInfoQuery query = new ResourceInfoQuery();
            query.setStatus(1);
            query.setResourceType("DOCUMENT");
            List<ResourceInfo> candidates = resourceInfoService.findListByParam(query);
            if (candidates == null || candidates.isEmpty()) {
                return;
            }
            int handled = 0;
            int scanned = 0;
            for (ResourceInfo resource : candidates) {
                if (scanned++ >= SWEEP_CANDIDATE_LIMIT || handled >= SWEEP_BATCH) {
                    break;
                }
                if (tryRender(resource)) {
                    handled++;
                }
            }
        } catch (Exception e) {
            log.warn("资源预览存量扫描失败", e);
        }
    }

    /**
     * 队列消费：5 秒一轮，单线程串行
     */
    @Scheduled(fixedDelay = 5000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_RESOURCE_PREVIEW_QUEUE);
        if (task == null) {
            return;
        }
        String resourceId = String.valueOf(task);
        try {
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
            if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                log.info("预览生成跳过（资源不存在或非可用）resourceId={}", resourceId);
                return;
            }
            if (!tryRender(resource)) {
                // 抢不到重活锁或本次无需处理：不重排队，交给 10 分钟的存量扫描重试
                log.info("预览生成本轮未执行，交存量扫描重试 resourceId={}", resourceId);
            }
        } catch (Exception e) {
            log.error("预览生成异常 resourceId={}", resourceId, e);
        }
    }

    /**
     * 对单个资源尝试生成预览；返回是否真正执行了一次转换
     */
    private boolean tryRender(ResourceInfo resource) {
        String filePath = resource.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        if (!resourcePreviewRenderer.isOfficeDocument(fileName)) {
            return false;
        }
        Path source = resourcePreviewRenderer.resolveSourcePath(filePath);
        if (!resourcePreviewRenderer.isInsideProjectFolder(source)) {
            return false;
        }
        if (!resourcePreviewRenderer.needRender(filePath)) {
            return false;
        }
        String owner = "preview:" + resource.getResourceId();
        if (!resourcePreviewRenderer.tryLockHeavy(owner)) {
            return false;
        }
        try {
            log.info("开始生成资源预览 resourceId={} file={}", resource.getResourceId(), fileName);
            long begin = System.currentTimeMillis();
            boolean ok = resourcePreviewRenderer.render(filePath);
            log.info("资源预览{} resourceId={} 耗时={}ms", ok ? "完成" : "失败", resource.getResourceId(),
                    System.currentTimeMillis() - begin);
            return true;
        } finally {
            redisComponent.removeKey(Constants.REDIS_KEY_RESOURCE_HEAVY_LOCK);
        }
    }
}
