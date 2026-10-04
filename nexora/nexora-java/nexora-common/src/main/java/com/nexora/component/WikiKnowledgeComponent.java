package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.ResourceDirectoryService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    /** 知识页系统目录类型（resource_directory.dir_type），子文件夹挂在其下 */
    private static final String WIKI_DIR_TYPE = "wiki";

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ObjectProvider<VectorStore> vectorStoreProvider;

    @Resource
    private ResourceDirectoryService resourceDirectoryService;

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
        return listPages(userId, keyword, vectorStatus, null);
    }

    /**
     * 列表查询（2026-10-04 增加子文件夹过滤）：
     * folderId 为空=全部；"root"=仅根目录；其它=该子文件夹内
     */
    public List<KnowledgeDoc> listPages(String userId, String keyword, Integer vectorStatus, String folderId) {
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
        if (!StringTools.isEmpty(folderId)) {
            if ("root".equals(folderId)) {
                query.setFolderIdNull(Boolean.TRUE);
            } else {
                query.setFolderId(folderId);
            }
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

    // ==================== 知识页子文件夹（2026-10-04） ====================

    /**
     * 取学生的「知识页」系统目录（dirType=wiki），不存在返回 null
     */
    public ResourceDirectory getWikiRootDirectory(String ownerId) {
        if (StringTools.isEmpty(ownerId)) {
            return null;
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(ownerId);
        List<ResourceDirectory> dirs = resourceDirectoryService.findListByParam(query);
        return (dirs == null ? List.<ResourceDirectory>of() : dirs).stream()
                .filter(dir -> WIKI_DIR_TYPE.equals(dir.getDirType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 知识页子文件夹清单：wiki 根目录 + 其全部子文件夹（自建），按名称排序；
     * folderId 为 null 表示根目录。无 wiki 系统目录（未初始化知识库）返回空列表
     */
    public List<ResourceDirectory> listWikiFolders(String ownerId) {
        ResourceDirectory root = getWikiRootDirectory(ownerId);
        if (root == null) {
            return List.of();
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(ownerId);
        List<ResourceDirectory> dirs = resourceDirectoryService.findListByParam(query);
        List<ResourceDirectory> folders = new ArrayList<>();
        folders.add(root);
        Set<String> wikiIds = new HashSet<>();
        wikiIds.add(root.getDirId());
        // 多轮收拢：先把根的直接子目录收进来，再迭代收录其子目录（深度有限，防环）
        boolean changed = true;
        List<ResourceDirectory> rest = new ArrayList<>(dirs == null ? List.<ResourceDirectory>of() : dirs);
        while (changed) {
            changed = false;
            for (ResourceDirectory dir : new ArrayList<>(rest)) {
                String parent = dir.getParentId() == null ? "0" : dir.getParentId();
                if (wikiIds.contains(parent)) {
                    wikiIds.add(dir.getDirId());
                    folders.add(dir);
                    rest.remove(dir);
                    changed = true;
                }
            }
        }
        folders.sort((a, b) -> String.valueOf(a.getDirName()).compareTo(String.valueOf(b.getDirName())));
        return folders;
    }

    /**
     * 校验 folderId 是该学生知识页子树内的目录（含根目录本身）；通过返回目录，否则 null
     */
    public ResourceDirectory requireWikiFolder(String ownerId, String folderId) {
        if (StringTools.isEmpty(folderId)) {
            return null;
        }
        for (ResourceDirectory folder : listWikiFolders(ownerId)) {
            if (folderId.equals(folder.getDirId())) {
                return folder;
            }
        }
        return null;
    }

    /**
     * 新建知识页子文件夹：parentFolderId 为空挂在 wiki 根目录下，
     * 否则必须是该学生 wiki 子树内的目录；同名不校验（与资源中心自建目录行为一致）
     *
     * @return 新文件夹 dirId
     */
    public String createWikiFolder(String ownerId, String name, String parentFolderId) {
        ResourceDirectory root = getWikiRootDirectory(ownerId);
        if (root == null) {
            throw new BusinessException("请先在资源中心初始化个人知识库");
        }
        if (StringTools.isEmpty(name)) {
            throw new BusinessException("文件夹名称不能为空");
        }
        String parentId = root.getDirId();
        if (!StringTools.isEmpty(parentFolderId)) {
            ResourceDirectory parent = requireWikiFolder(ownerId, parentFolderId.trim());
            if (parent == null) {
                throw new BusinessException("父文件夹不存在或不属于知识页目录");
            }
            parentId = parent.getDirId();
        }
        Date now = new Date();
        ResourceDirectory folder = new ResourceDirectory();
        folder.setDirId(UUID.randomUUID().toString().replace("-", ""));
        folder.setDirName(name.trim());
        folder.setParentId(parentId);
        // dirType 留空：自建目录可重命名/删除；通过 parentId 链挂在知识页系统目录下
        folder.setOwnerId(ownerId);
        folder.setSort(0);
        folder.setCreateTime(now);
        folder.setUpdateTime(now);
        resourceDirectoryService.add(folder);
        return folder.getDirId();
    }

    /**
     * 移动知识页到子文件夹：folderId 为空 = 移回知识页根目录；
     * 校验知识页归属与目标文件夹归属，防止越权与跨目录类型移动。
     * 置空必须走专用更新（通用 update 的 &lt;if&gt; 会把 null 字段跳过，移回根目录会失效）
     */
    public void moveDocToFolder(String userId, String docId, String folderId) {
        KnowledgeDoc doc = requireOwnedDoc(userId, docId);
        String targetFolderId = null;
        if (!StringTools.isEmpty(folderId)) {
            ResourceDirectory folder = requireWikiFolder(userId, folderId.trim());
            if (folder == null) {
                throw new BusinessException("目标文件夹不存在或不属于知识页目录");
            }
            targetFolderId = folder.getDirId();
        }
        knowledgeDocService.updateKnowledgeDocFolder(targetFolderId, new Date(), doc.getDocId());
    }

    /**
     * 删除知识页子文件夹：其整棵子树（子文件夹及其中的知识页）一并处理，数据不丢失——
     * 子树内所有知识页回到根目录，子树内全部自建文件夹删除；
     * 仅允许删除该学生 wiki 子树内的自建文件夹（系统目录不可删）
     */
    public void deleteWikiFolder(String ownerId, String folderId) {
        ResourceDirectory folder = requireWikiFolder(ownerId, folderId);
        if (folder == null || WIKI_DIR_TYPE.equals(folder.getDirType())) {
            throw new BusinessException("文件夹不存在或不可删除");
        }
        // 待删子树：folderId 及其全部后代（一次取回目录清单在内存里收拢，不逐层查库）
        Set<String> subtree = new HashSet<>();
        subtree.add(folderId);
        List<ResourceDirectory> allFolders = listWikiFolders(ownerId);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ResourceDirectory dir : allFolders) {
                String parent = dir.getParentId() == null ? "0" : dir.getParentId();
                if (subtree.contains(parent) && subtree.add(dir.getDirId())) {
                    changed = true;
                }
            }
        }
        // 子树内知识页一次性回到根目录（folder_id 置 NULL，需专用批量更新）
        KnowledgeDocQuery docQuery = new KnowledgeDocQuery();
        docQuery.setOwnerId(ownerId);
        docQuery.setFolderIds(new ArrayList<>(subtree));
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(docQuery);
        if (docs != null && !docs.isEmpty()) {
            List<String> docIds = docs.stream().map(KnowledgeDoc::getDocId).toList();
            knowledgeDocService.updateKnowledgeDocFolderToNullBatch(docIds, new Date());
        }
        // 子树内目录整体删除（含自身；一条批量删除）
        ResourceDirectoryQuery deleteQuery = new ResourceDirectoryQuery();
        deleteQuery.setOwnerId(ownerId);
        deleteQuery.setDirIds(new ArrayList<>(subtree));
        resourceDirectoryService.deleteByParam(deleteQuery);
    }
}
