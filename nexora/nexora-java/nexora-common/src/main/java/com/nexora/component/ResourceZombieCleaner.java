package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.service.ResourceInfoService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 资源「处理中」僵尸记录清扫（common，由 admin / web 两端启动与定时触发）。
 *
 * 根因：资源分片上传在 prepare 阶段即落一行 status=0（处理中），后续依赖 Redis 会话与队列收敛状态；
 * 若服务在「分片上传 / 合并 / 转码」中途被重启或杀死（部署重启最常见），或用户中途放弃上传，
 * 该资源不会再有任何链路把它推进到终态，列表将永久显示「处理中」（僵尸记录）。
 *
 * 判定口径（双条件，缺一不可）：
 * 1) status=0 且 update_time 早于「上传会话 TTL + 冗余 30 分钟」——会话必然已过期；
 * 2) Redis 反查键 resource:upload:session:byResource:{resourceId} 不存在——该资源已无进行中的上传
 *    （反查键与上传会话同 TTL，由 prepare 写入、process 收尾删除，是最可靠的探活信号）。
 * 命中即批量置为 2（失败），前端展示「失败」并可手动删除，不再无限「处理中」。
 */
@Component
public class ResourceZombieCleaner {

    private static final Logger log = LoggerFactory.getLogger(ResourceZombieCleaner.class);

    /** 与上传会话 TTL 同源（分钟），仅作候选粗筛；真正判定看 Redis 反查键 */
    @Value("${resource.upload-session-ttl-minutes:120}")
    private long sessionTtlMinutes;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 清扫一轮僵尸资源，返回本次标记失败的数量
     */
    public int cleanZombies() {
        Date cutoff = new Date(System.currentTimeMillis() - (sessionTtlMinutes + 30) * 60_000L);
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setStatus(0);
        query.setUpdateTimeBefore(cutoff);
        List<ResourceInfo> candidates = resourceInfoService.findListByParam(query);
        if (candidates == null || candidates.isEmpty()) {
            return 0;
        }
        List<String> zombieIds = new ArrayList<>();
        for (ResourceInfo resource : candidates) {
            // Redis 探活（不查库）：反查键仍在 = 该资源有进行中的上传会话，跳过
            Object uploadId = redisComponent.getObject(
                    Constants.REDIS_KEY_RESOURCE_UPLOAD_SESSION_BY_RESOURCE + resource.getResourceId());
            if (uploadId == null) {
                zombieIds.add(resource.getResourceId());
            }
        }
        if (zombieIds.isEmpty()) {
            return 0;
        }
        resourceInfoService.updateStatusBatch(zombieIds, 2);
        log.warn("资源僵尸记录清理：已把 {} 条无进行中上传的「处理中」资源置为失败 resourceIds={}",
                zombieIds.size(), zombieIds);
        return zombieIds.size();
    }
}