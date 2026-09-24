package com.nexora.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.nexora.admin.dto.KnowledgeImportTaskVO;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 知识库解析入库异步任务业务：Redis 任务体 + 队列解耦 + 文档级运行锁互斥。
 * 同一 docId 运行中重复提交直接返回进行中任务，防止重复解析/重复向量化。
 */
@Slf4j
@Service
public class KnowledgeImportTaskBiz {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** 运行锁保留 10 分钟（解析入库一般数秒~数十秒，余量充足；超时自动释放兜底） */
    private static final long RUNNING_TTL_SECONDS = 600;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 提交解析入库任务：docId 已存在未完成任务时直接返回该任务，不再重复提交
     */
    public KnowledgeImportTaskVO submit(String docId, String resourceId, Integer sourceType) {
        if (StringTools.isEmpty(docId)) {
            throw new BusinessException("文档ID不能为空");
        }
        String runningKey = runningKey(docId);
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            KnowledgeImportTaskVO existing = loadInternal(runningTaskId);
            if (existing != null) {
                return existing;
            }
            redisComponent.removeKey(runningKey);
        }

        Date now = new Date();
        KnowledgeImportTaskVO task = new KnowledgeImportTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setDocId(docId);
        task.setResourceId(resourceId);
        task.setSourceType(sourceType);
        task.setStatus("PENDING");
        task.setMessage("任务已提交，等待执行");
        task.setProgress(0);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.setString(runningKey, task.getTaskId(), RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
        redisComponent.leftPush(Constants.REDIS_KEY_KNOWLEDGE_IMPORT_TASK_QUEUE, task.getTaskId());
        log.info("知识库解析入库任务已提交 docId={} taskId={} sourceType={}", docId, task.getTaskId(), sourceType);
        return task;
    }

    /**
     * 查询任务状态
     */
    public KnowledgeImportTaskVO get(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        KnowledgeImportTaskVO task = loadInternal(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在或已过期");
        }
        return task;
    }

    /**
     * 内部读取任务体（消费者使用，任务不存在返回 null）
     */
    public KnowledgeImportTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_KNOWLEDGE_IMPORT_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, KnowledgeImportTaskVO.class);
    }

    /**
     * 更新任务体（消费进度/终态）
     */
    public void update(KnowledgeImportTaskVO task) {
        if (task == null || StringTools.isEmpty(task.getTaskId())) {
            return;
        }
        task.setUpdateTime(new Date());
        save(task);
    }

    /**
     * 任务到达终态后释放文档运行锁（仅当锁仍指向本任务时释放）
     */
    public void releaseRunning(String docId, String taskId) {
        String key = runningKey(docId);
        Object current = redisComponent.getObject(key);
        if (current != null && taskId.equals(current.toString())) {
            redisComponent.removeKey(key);
        }
    }

    private void save(KnowledgeImportTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_KNOWLEDGE_IMPORT_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private String runningKey(String docId) {
        return Constants.REDIS_KEY_KNOWLEDGE_IMPORT_RUNNING_PREFIX + docId;
    }
}