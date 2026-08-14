package com.nexora.component;

import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.service.KnowledgeDocService;
import com.nexora.utils.TextChunker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 学生端官方知识库检索：ES 向量优先，MySQL 关键词回退。
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

    public String buildRagData(String stage, String question) {
        if (question == null || question.isBlank()) {
            return "";
        }
        try {
            List<RagHit> hits = vectorSearch(stage, question);
            if (hits.isEmpty()) {
                hits = keywordSearch(stage, question);
            }
            if (hits.isEmpty()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("=== 官方知识库参考内容 ===\n");
            for (RagHit hit : hits) {
                builder.append("【").append(hit.title()).append("】\n");
                builder.append(hit.content()).append("\n\n");
            }
            return builder.toString().trim();
        } catch (Exception e) {
            log.warn("知识库检索失败，回退普通对话: {}", e.getMessage());
            return "";
        }
    }

    private List<RagHit> vectorSearch(String stage, String question) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .similarityThreshold(THRESHOLD);
        if (stage != null && !stage.isBlank()) {
            builder.filterExpression("stage == '" + stage + "'");
        }
        List<Document> documents = vectorStore.similaritySearch(builder.build());
        return documents.stream()
                .map(doc -> new RagHit(
                        asString(doc.getMetadata().get("docId")),
                        asString(doc.getMetadata().get("title")),
                        doc.getText(),
                        doc.getScore() == null ? 0 : doc.getScore()))
                .toList();
    }

    private List<RagHit> keywordSearch(String stage, String question) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setStage(stage);
        query.setContentFuzzy(question);
        query.setStatus(1);
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
                hits.add(new RagHit(doc.getDocId(), doc.getTitle(), chunk, score));
            }
        }
        hits.sort(Comparator.comparing(RagHit::score).reversed());
        return hits.stream().limit(TOP_K).toList();
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

    public record RagHit(String docId, String title, String content, double score) {
    }
}
