package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeDocService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 学生个人知识页公共写操作组件：nexora-web（对话同步、资源中心、AI 处理）与 nexora-mcp（MCP 知识页工具）共用，
 * 统一归属校验、草稿态落库、向量清理与入库投递口径，避免两端各写一套实现产生行为漂移。
 *
 * 向量清理策略：本进程装配了 VectorStore（web / admin）时就地删除；未装配（nexora-mcp）或就地删除失败时，
 * 投递清理任务到 Redis 队列，由 web 端消费者异步执行——MCP 因此无需依赖向量库。
 */
@Component
public class WikiKnowledgeComponent {

    private static final Logger log = LoggerFactory.getLogger(WikiKnowledgeComponent.class);

    /** 学生知识页未关联知识点，统一 0 占位（与既有个人库约定一致） */
    public static final String PLACEHOLDER_KNOWLEDGE_POINT_ID = "0";

    /** 列表返回上限（MCP 工具与抽屉共用，防止超量数据注入模型上下文） */
    private static final int LIST_LIMIT = 100;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ObjectProvider<VectorStore> vectorStoreProvider;

    /**
     * 取本人知识页，不存在或非本人一律按"无权操作"拒绝
     */
    public KnowledgeDoc requireOwnedDoc(String userId, String docId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(docId)) {
            throw new BusinessException("知识页参数不完整");
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId.trim());
        if (doc == null || !userId.equals(doc.getOwnerId())) {
            throw new BusinessException("知识页不存在或无权操作");
        }
        return doc;
    }

    /**
     * 列表查询：按 owner 隔离；keyword 匹配标题；vectorStatus 为空表示不限状态
     */
    public List<KnowledgeDoc> listPages(String userId, String keyword, Integer vectorStatus) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("缺少用户标识");
        }
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        if (!StringTools.isEmpty(keyword)) {
            query.setTitleFuzzy(keyword.trim());
        }
        if (vectorStatus != null) {
            query.setVectorStatus(vectorStatus);
        }
        query.setOrderBy("update_time desc");
        List<KnowledgeDoc> list = knowledgeDocService.findListByParam(query);
        if (list != null && list.size() > LIST_LIMIT) {
            return list.subList(0, LIST_LIMIT);
        }
        return list;
    }

    /**
     * 新建知识页草稿（vectorStatus=0，不向量化）
     */
    public KnowledgeDoc createDraft(String userId, String stage, String title, String content, String sourceUrl) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("缺少用户标识");
        }
        if (StringTools.isEmpty(title)) {
            throw new BusinessException("知识页标题不能为空");
        }
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("知识页内容不能为空");
        }
        Date now = new Date();
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setDocId(UUID.randomUUID().toString().replace("-", ""));
        doc.setTitle(title.trim());
        doc.setStage(stage);
        doc.setOwnerId(userId);
        doc.setKnowledgePointId(PLACEHOLDER_KNOWLEDGE_POINT_ID);
        doc.setDifficulty(1);
        doc.setDataType("KNOWLEDGE");
        doc.setContent(content);
        doc.setSourceType(0);
        doc.setSourceResourceId(null);
        doc.setSourceUrl(sourceUrl);
        doc.setVectorStatus(0);
        doc.setVectorError(null);
        doc.setChunkCount(0);
        doc.setStatus(1);
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        knowledgeDocService.add(doc);
        return doc;
    }

    /**
     * 覆盖知识页内容并回到草稿态（旧向量就地清理或投递清理任务）
     */
    public KnowledgeDoc overwriteAsDraft(String userId, String docId, String title, String content) {
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("知识页内容不能为空");
        }
        KnowledgeDoc doc = requireOwnedDoc(userId, docId);
        clearVector(doc);
        KnowledgeDoc update = new KnowledgeDoc();
        if (!StringTools.isEmpty(title)) {
            update.setTitle(title.trim());
        }
        update.setContent(content);
        update.setVectorStatus(0);
        // vector_error 在 <if> 更新下无法置 NULL，统一用空串清空
        update.setVectorError("");
        update.setChunkCount(0);
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, doc.getDocId());
        return knowledgeDocService.getKnowledgeDocByDocId(doc.getDocId());
    }

    /**
     * 按来源去重保存草稿：resourceId / sourceUrl 命中已有页则覆盖并回草稿态，否则新建
     * （承载「原始资料生成 Wiki」「对话同步」「课程同步」等既有语义）
     */
    public KnowledgeDoc saveDraftBySource(String userId, String stage, String title, String resourceId,
                                         String content, String sourceUrl) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        if (!StringTools.isEmpty(resourceId)) {
            query.setSourceResourceId(resourceId);
        }
        if (!StringTools.isEmpty(sourceUrl)) {
            query.setSourceUrl(sourceUrl);
        }
        List<KnowledgeDoc> existing = knowledgeDocService.findListByParam(query);
        if (existing != null && !existing.isEmpty()) {
            KnowledgeDoc doc = existing.get(0);
            clearVector(doc);
            KnowledgeDoc update = new KnowledgeDoc();
            update.setTitle(title);
            update.setContent(content);
            update.setVectorStatus(0);
            update.setVectorError("");
            update.setChunkCount(0);
            if (!StringTools.isEmpty(sourceUrl)) {
                update.setSourceUrl(sourceUrl);
            }
            update.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(update, doc.getDocId());
            return knowledgeDocService.getKnowledgeDocByDocId(doc.getDocId());
        }
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setDocId(UUID.randomUUID().toString().replace("-", ""));
        doc.setTitle(title);
        doc.setStage(stage);
        doc.setOwnerId(userId);
        doc.setKnowledgePointId(PLACEHOLDER_KNOWLEDGE_POINT_ID);
        doc.setDifficulty(1);
        doc.setDataType("KNOWLEDGE");
        doc.setContent(content);
        doc.setSourceType(0);
        doc.setSourceResourceId(resourceId);
        doc.setSourceUrl(sourceUrl);
        doc.setVectorStatus(0);
        doc.setVectorError(null);
        doc.setChunkCount(0);
        doc.setStatus(1);
        Date now = new Date();
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        knowledgeDocService.add(doc);
        return doc;
    }

    /**
     * 提交入库：置处理中并入队，由 web 端消费者完成向量化（草稿态才可提交，重复提交按状态拒绝）
     */
    public KnowledgeDoc markIngest(String userId, String docId) {
        KnowledgeDoc doc = requireOwnedDoc(userId, docId);
        if (StringTools.isEmpty(doc.getContent())) {
            throw new BusinessException("知识页内容为空，无法入库");
        }
        if (doc.getVectorStatus() != null && doc.getVectorStatus() == 1) {
            throw new BusinessException("知识页正在向量化中，请稍候");
        }
        if (doc.getVectorStatus() != null && doc.getVectorStatus() == 2) {
            throw new BusinessException("知识页已入库，无需重复提交");
        }
        KnowledgeDoc update = new KnowledgeDoc();
        update.setVectorStatus(1);
        update.setVectorError("");
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
        redisComponent.leftPush(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_QUEUE, docId);
        return knowledgeDocService.getKnowledgeDocByDocId(docId);
    }

    /**
     * 删除本人知识页（含向量清理）
     */
    public void deleteOwned(String userId, String docId) {
        KnowledgeDoc doc = requireOwnedDoc(userId, docId);
        clearVector(doc);
        knowledgeDocService.deleteKnowledgeDocByDocId(docId);
    }

    /**
     * 向量清理：本进程有向量库则就地删除，否则投递清理队列（web 端消费者执行）
     */
    public void clearVector(KnowledgeDoc doc) {
        int chunkCount = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        if (chunkCount <= 0) {
            return;
        }
        if (vectorStoreProvider.getIfAvailable() != null) {
            try {
                knowledgeVectorComponent.deleteChunks(doc.getDocId(), chunkCount);
                return;
            } catch (Exception e) {
                log.warn("知识页向量就地清理失败，改投递清理任务 docId={}", doc.getDocId(), e);
            }
        }
        redisComponent.leftPush(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_CLEANUP_QUEUE, doc.getDocId());
    }

    /**
     * 向量状态文案（MCP 工具输出与前端展示口径一致）
     */
    public String statusText(Integer vectorStatus) {
        if (vectorStatus == null) {
            return "未知";
        }
        return switch (vectorStatus) {
            case 0 -> "草稿";
            case 1 -> "向量化中";
            case 2 -> "已入库";
            case 3 -> "向量化失败";
            case 4 -> "已过期";
            default -> "未知";
        };
    }

    /**
     * 来源文案：按 sourceUrl / sourceResourceId 推断知识页的来路
     */
    public String sourceText(KnowledgeDoc doc) {
        String sourceUrl = doc.getSourceUrl() == null ? "" : doc.getSourceUrl();
        if (sourceUrl.startsWith("agent-message:")) {
            return "AI 对话";
        }
        if (sourceUrl.startsWith("course:")) {
            return "课程同步";
        }
        if (sourceUrl.startsWith("ai-summary:")) {
            return "AI 摘要";
        }
        if (sourceUrl.startsWith("ai-organize:")) {
            return "AI 归档整合";
        }
        if (!StringTools.isEmpty(doc.getSourceResourceId())) {
            return "原始资料";
        }
        if (!StringTools.isEmpty(sourceUrl)) {
            return "链接来源";
        }
        return "手动创建";
    }
}
