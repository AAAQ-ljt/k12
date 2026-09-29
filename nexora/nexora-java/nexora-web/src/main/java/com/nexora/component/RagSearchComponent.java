package com.nexora.component;

import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import com.nexora.utils.TextChunker;
import com.nexora.vo.ResourceRecommendVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生端官方知识库检索：ES 向量优先，MySQL 关键词回退，命中后生成资料推荐卡片。
 *
 * 两级检索口径：先学生个人知识库（ownerId 非空），无命中再回退官方课程知识库（ownerId 为空）。
 * 命中下发模型前按 ownerId 标注来源分组（见 {@link #buildRagData}），
 * 因为官方课本文档（管理端维护）与学生自己上传的资料在模型眼里都是"一段参考文本"，
 * 不标注就会被说成"你提供的知识库"。
 */
@Slf4j
@Component
public class RagSearchComponent {

    /** 来源分组标题：管理端后台维护的官方教材 / 课程资料 */
    private static final String SOURCE_SECTION_OFFICIAL = "平台课程知识库";

    /** 来源分组标题：学生本人上传或整理的知识页 */
    private static final String SOURCE_SECTION_PERSONAL = "学生个人知识库";

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private SystemConfigComponent systemConfigComponent;

    /** 检索召回条数（管理端「RAG 配置」可调，缺省 10） */
    private int topK() {
        return systemConfigComponent.getIntValue(SystemConfigComponent.GROUP_RAG,
                SystemConfigComponent.KEY_RAG_TOP_K, SystemConfigComponent.DEFAULT_RAG_TOP_K);
    }

    /** 相似度阈值（管理端「RAG 配置」可调，缺省 0.5） */
    private double threshold() {
        return systemConfigComponent.getDoubleValue(SystemConfigComponent.GROUP_RAG,
                SystemConfigComponent.KEY_RAG_SIMILARITY_THRESHOLD, SystemConfigComponent.DEFAULT_RAG_SIMILARITY_THRESHOLD);
    }

    /** 关键词回退检索的分块大小（管理端「RAG 配置」可调，缺省 500） */
    private int chunkSize() {
        return systemConfigComponent.getIntValue(SystemConfigComponent.GROUP_RAG,
                SystemConfigComponent.KEY_RAG_CHUNK_SIZE, SystemConfigComponent.DEFAULT_RAG_CHUNK_SIZE);
    }

    public String buildRagData(String userId, String stage, String question) {
        return buildRagResult(userId, stage, question).ragData();
    }

    public RagSearchResult buildRagResult(String userId, String stage, String question) {
        if (question == null || question.isBlank()) {
            return new RagSearchResult("", List.of());
        }
        try {
            List<RagHit> hits = vectorSearch(userId, stage, question);
            if (hits.isEmpty()) {
                hits = keywordSearch(userId, stage, question);
            }
            if (hits.isEmpty() && !StringTools.isEmpty(userId)) {
                hits = vectorSearch("", stage, question);
                if (hits.isEmpty()) {
                    hits = keywordSearch("", stage, question);
                }
            }
            if (hits.isEmpty()) {
                return new RagSearchResult("", List.of());
            }
            RagEnrichResult enrichResult = enrichHits(hits, stage, userId);
            List<RagHit> enriched = enrichResult.hits();
            String ragData = buildRagData(enriched);
            List<ResourceRecommendVO> recommends = buildRecommends(enriched, enrichResult.resourceMap());
            return new RagSearchResult(ragData, recommends);
        } catch (Exception e) {
            log.warn("知识库检索失败，回退普通对话: {}", e.getMessage());
            return new RagSearchResult("", List.of());
        }
    }

    private List<RagHit> vectorSearch(String ownerId, String stage, String question) {
        // 复用 KnowledgeVectorComponent.search：官方库（ownerId 为空）不下发 ownerId 过滤，
        // 由组件内超量取回后按 metadata 过滤（ES 过滤表达式不支持空字符串等值）
        List<Document> documents = knowledgeVectorComponent.search(
                question, stage, null, null, ownerId, topK(), threshold());
        return documents.stream()
                .map(doc -> new RagHit(
                        asString(doc.getMetadata().get("docId")),
                        asString(doc.getMetadata().get("title")),
                        doc.getText(),
                        doc.getScore() == null ? 0 : doc.getScore(),
                        asString(doc.getMetadata().get("sourceResourceId")),
                        asString(doc.getMetadata().get("sourceUrl")),
                        asString(doc.getMetadata().get("ownerId"))))
                .toList();
    }

    private List<RagHit> keywordSearch(String ownerId, String stage, String question) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setStage(stage);
        query.setContentFuzzy(question);
        query.setStatus(1);
        if (ownerId != null && !ownerId.isBlank()) {
            query.setOwnerId(ownerId);
            // 个人库两段式：仅检索已确认入库（vectorStatus=2）的知识页，草稿不被检索
            query.setVectorStatus(2);
        } else {
            query.setOwnerIdNull(Boolean.TRUE);
        }
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        List<RagHit> hits = new ArrayList<>();
        String lowerQuery = question.toLowerCase();
        // 配置读取放循环外：避免每篇文档都查一次配置表
        int chunkSize = chunkSize();
        int topK = topK();
        for (KnowledgeDoc doc : docs) {
            List<String> chunks = TextChunker.split(doc.getContent(), chunkSize);
            for (String chunk : chunks) {
                int hitsCount = countHits(chunk.toLowerCase(), lowerQuery);
                if (hitsCount <= 0) {
                    continue;
                }
                double score = Math.min(1.0, 0.5 + hitsCount * 0.1);
                hits.add(new RagHit(doc.getDocId(), doc.getTitle(), chunk, score,
                        doc.getSourceResourceId(), doc.getSourceUrl(), doc.getOwnerId()));
            }
        }
        hits.sort(Comparator.comparing(RagHit::score).reversed());
        return hits.stream().limit(topK).toList();
    }

    private RagEnrichResult enrichHits(List<RagHit> hits, String stage, String userId) {
        List<String> docIds = hits.stream()
                .map(RagHit::docId)
                .filter(id -> !StringTools.isEmpty(id))
                .distinct()
                .toList();
        Map<String, KnowledgeDoc> docMap = new HashMap<>();
        if (!docIds.isEmpty()) {
            KnowledgeDocQuery docQuery = new KnowledgeDocQuery();
            docQuery.setDocIds(docIds);
            Map<String, KnowledgeDoc> foundDocs = knowledgeDocService.findListByParam(docQuery).stream()
                    .collect(Collectors.toMap(KnowledgeDoc::getDocId, doc -> doc, (a, b) -> a));
            docMap.putAll(foundDocs);
        }

        List<String> resourceIds = hits.stream()
                .map(hit -> resolveResourceId(hit, docMap))
                .filter(id -> !StringTools.isEmpty(id))
                .distinct()
                .toList();
        Map<String, ResourceInfo> resourceMap = new HashMap<>();
        if (!resourceIds.isEmpty()) {
            ResourceInfoQuery resourceQuery = new ResourceInfoQuery();
            resourceQuery.setResourceIds(resourceIds);
            resourceQuery.setStatus(1);
            resourceQuery.setOwnerIdNull(Boolean.TRUE);
            Map<String, ResourceInfo> foundResources = resourceInfoService.findListByParam(resourceQuery).stream()
                    .collect(Collectors.toMap(ResourceInfo::getResourceId, resource -> resource, (a, b) -> a));
            resourceMap.putAll(foundResources);
            if (!StringTools.isEmpty(userId)) {
                ResourceInfoQuery personalQuery = new ResourceInfoQuery();
                personalQuery.setResourceIds(resourceIds);
                personalQuery.setStatus(1);
                personalQuery.setOwnerId(userId);
                Map<String, ResourceInfo> personalResources = resourceInfoService.findListByParam(personalQuery).stream()
                        .collect(Collectors.toMap(ResourceInfo::getResourceId, resource -> resource, (a, b) -> a));
                resourceMap.putAll(personalResources);
            }
        }

        Map<String, KnowledgeDoc> finalDocMap = docMap;
        Map<String, ResourceInfo> finalResourceMap = resourceMap;
        List<RagHit> enriched = hits.stream().map(hit -> {
            KnowledgeDoc doc = finalDocMap.get(hit.docId());
            String sourceResourceId = hit.sourceResourceId();
            String sourceUrl = hit.sourceUrl();
            // 来源归属以库里的知识页记录为准（ES metadata 只作兜底）：ownerId 为空 = 官方库，非空 = 学生个人库
            String ownerId = hit.ownerId();
            if (doc != null) {
                if (StringTools.isEmpty(sourceResourceId)) {
                    sourceResourceId = doc.getSourceResourceId();
                }
                if (StringTools.isEmpty(sourceUrl)) {
                    sourceUrl = doc.getSourceUrl();
                }
                ownerId = doc.getOwnerId();
            }
            if (!StringTools.isEmpty(sourceResourceId)) {
                ResourceInfo resource = finalResourceMap.get(sourceResourceId);
                if (resource == null || resource.getStatus() == null || resource.getStatus() != 1
                        || !stageMatches(resource.getStage(), stage)) {
                    sourceResourceId = null;
                }
            }
            return new RagHit(hit.docId(), hit.title(), hit.content(), hit.score(),
                    sourceResourceId, sourceUrl, ownerId);
        }).toList();
        return new RagEnrichResult(enriched, resourceMap);
    }

    private String resolveResourceId(RagHit hit, Map<String, KnowledgeDoc> docMap) {
        if (!StringTools.isEmpty(hit.sourceResourceId())) {
            return hit.sourceResourceId();
        }
        KnowledgeDoc doc = docMap.get(hit.docId());
        return doc == null ? null : doc.getSourceResourceId();
    }

    private boolean stageMatches(String resourceStage, String userStage) {
        if (StringTools.isEmpty(resourceStage) || StringTools.isEmpty(userStage)) {
            return true;
        }
        return resourceStage.equals(userStage);
    }

    private List<ResourceRecommendVO> buildRecommends(List<RagHit> hits, Map<String, ResourceInfo> resourceMap) {
        List<ResourceRecommendVO> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (RagHit hit : hits) {
            if (!StringTools.isEmpty(hit.sourceResourceId())) {
                ResourceInfo resource = resourceMap.get(hit.sourceResourceId());
                if (resource == null || !seen.add("resource:" + hit.sourceResourceId())) {
                    continue;
                }
                ResourceRecommendVO vo = new ResourceRecommendVO();
                vo.setDocId(hit.docId());
                vo.setTitle(StringTools.isEmpty(resource.getResourceName()) ? hit.title() : resource.getResourceName());
                vo.setResourceId(resource.getResourceId());
                vo.setResourceType(resource.getResourceType());
                vo.setSourceUrl(hit.sourceUrl());
                result.add(vo);
            } else if (!StringTools.isEmpty(hit.sourceUrl()) && seen.add("url:" + hit.sourceUrl())) {
                ResourceRecommendVO vo = new ResourceRecommendVO();
                vo.setDocId(hit.docId());
                vo.setTitle(hit.title());
                vo.setResourceType("LINK");
                vo.setSourceUrl(hit.sourceUrl());
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 组装下发模型的知识库参考内容：按来源分组，两组各带一句口径说明（外层标题由调用方补）。
     * 单次检索通常只命中其中一组（个人库优先、无命中才回退官方库），
     * 但两组都写清楚，模型才不会凭标题猜"这是学生自己传的"。
     */
    private String buildRagData(List<RagHit> hits) {
        List<RagHit> official = hits.stream().filter(hit -> !hit.personal()).toList();
        List<RagHit> personal = hits.stream().filter(RagHit::personal).toList();
        StringBuilder builder = new StringBuilder();
        appendSourceSection(builder, SOURCE_SECTION_OFFICIAL,
                "（学校/平台在管理端后台统一维护的教材与课程资料，面向全体学生，不是学生本人上传的）", official);
        appendSourceSection(builder, SOURCE_SECTION_PERSONAL,
                "（该学生本人上传或整理的知识页，仅本人可见）", personal);
        return builder.toString().trim();
    }

    private void appendSourceSection(StringBuilder builder, String sectionName, String sectionDesc, List<RagHit> hits) {
        if (hits.isEmpty()) {
            return;
        }
        builder.append("### 来源：").append(sectionName).append(sectionDesc).append("\n");
        for (RagHit hit : hits) {
            builder.append("【").append(hit.title()).append("】\n");
            builder.append(hit.content()).append("\n\n");
        }
    }

    private int countHits(String text, String query) {
        if (query == null || query.isBlank()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(query, idx)) >= 0) {
            count++;
            idx += query.length();
        }
        return count;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 检索命中；ownerId 空 = 官方课程知识库（管理端维护），非空 = 该学生的个人知识库
     */
    public record RagHit(String docId, String title, String content, double score,
                         String sourceResourceId, String sourceUrl, String ownerId) {

        public boolean personal() {
            return !StringTools.isEmpty(ownerId);
        }
    }

    public record RagSearchResult(String ragData, List<ResourceRecommendVO> recommendations) {
    }

    private record RagEnrichResult(List<RagHit> hits, Map<String, ResourceInfo> resourceMap) {
    }
}
