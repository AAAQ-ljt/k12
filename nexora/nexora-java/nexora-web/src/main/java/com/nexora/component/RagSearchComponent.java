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
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
 */
@Slf4j
@Component
public class RagSearchComponent {

    private static final int TOP_K = 10;
    private static final double THRESHOLD = 0.5;
    private static final int CHUNK_SIZE = 500;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private ResourceInfoService resourceInfoService;

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
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .similarityThreshold(THRESHOLD);
        String actualOwnerId = ownerId == null ? "" : ownerId;
        builder.filterExpression("ownerId == '" + actualOwnerId + "'");
        if (stage != null && !stage.isBlank()) {
            builder.filterExpression("stage == '" + stage + "' && ownerId == '" + actualOwnerId + "'");
        }
        List<Document> documents = vectorStore.similaritySearch(builder.build());
        return documents.stream()
                .map(doc -> new RagHit(
                        asString(doc.getMetadata().get("docId")),
                        asString(doc.getMetadata().get("title")),
                        doc.getText(),
                        doc.getScore() == null ? 0 : doc.getScore(),
                        asString(doc.getMetadata().get("sourceResourceId")),
                        asString(doc.getMetadata().get("sourceUrl"))))
                .toList();
    }

    private List<RagHit> keywordSearch(String ownerId, String stage, String question) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setStage(stage);
        query.setContentFuzzy(question);
        query.setStatus(1);
        if (ownerId != null && !ownerId.isBlank()) {
            query.setOwnerId(ownerId);
        } else {
            query.setOwnerIdNull(Boolean.TRUE);
        }
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        List<RagHit> hits = new ArrayList<>();
        String lowerQuery = question.toLowerCase();
        for (KnowledgeDoc doc : docs) {
            List<String> chunks = TextChunker.split(doc.getContent(), CHUNK_SIZE);
            for (String chunk : chunks) {
                int hitsCount = countHits(chunk.toLowerCase(), lowerQuery);
                if (hitsCount <= 0) {
                    continue;
                }
                double score = Math.min(1.0, 0.5 + hitsCount * 0.1);
                hits.add(new RagHit(doc.getDocId(), doc.getTitle(), chunk, score,
                        doc.getSourceResourceId(), doc.getSourceUrl()));
            }
        }
        hits.sort(Comparator.comparing(RagHit::score).reversed());
        return hits.stream().limit(TOP_K).toList();
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
            if (doc != null) {
                if (StringTools.isEmpty(sourceResourceId)) {
                    sourceResourceId = doc.getSourceResourceId();
                }
                if (StringTools.isEmpty(sourceUrl)) {
                    sourceUrl = doc.getSourceUrl();
                }
            }
            if (!StringTools.isEmpty(sourceResourceId)) {
                ResourceInfo resource = finalResourceMap.get(sourceResourceId);
                if (resource == null || resource.getStatus() == null || resource.getStatus() != 1
                        || !stageMatches(resource.getStage(), stage)) {
                    sourceResourceId = null;
                }
            }
            return new RagHit(hit.docId(), hit.title(), hit.content(), hit.score(),
                    sourceResourceId, sourceUrl);
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

    private String buildRagData(List<RagHit> hits) {
        StringBuilder builder = new StringBuilder();
        builder.append("=== 知识库参考内容 ===\n");
        for (RagHit hit : hits) {
            builder.append("【").append(hit.title()).append("】\n");
            builder.append(hit.content()).append("\n\n");
        }
        return builder.toString().trim();
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

    public record RagHit(String docId, String title, String content, double score,
                         String sourceResourceId, String sourceUrl) {
    }

    public record RagSearchResult(String ragData, List<ResourceRecommendVO> recommendations) {
    }

    private record RagEnrichResult(List<RagHit> hits, Map<String, ResourceInfo> resourceMap) {
    }
}
