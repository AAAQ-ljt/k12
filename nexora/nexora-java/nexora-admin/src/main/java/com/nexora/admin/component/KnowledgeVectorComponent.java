package com.nexora.admin.component;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 官方知识库 ES 向量操作。
 */
@Component
public class KnowledgeVectorComponent {

    @Resource
    private VectorStore vectorStore;

    public void saveChunks(String docId, String title, String stage, String knowledgePointId,
                           Integer difficulty, List<String> chunks) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("docId", docId);
            metadata.put("title", title == null ? "" : title);
            metadata.put("chunkIndex", i);
            metadata.put("dataType", "KNOWLEDGE");
            metadata.put("stage", stage == null ? "" : stage);
            metadata.put("knowledgePointId", knowledgePointId == null ? "" : knowledgePointId);
            metadata.put("difficulty", difficulty == null ? 1 : difficulty);
            documents.add(Document.builder()
                    .id(docId + "_" + i)
                    .text(chunks.get(i))
                    .metadata(metadata)
                    .build());
        }
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    public void deleteChunks(String docId, int maxCount) {
        if (maxCount <= 0) {
            return;
        }
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            ids.add(docId + "_" + i);
        }
        vectorStore.delete(ids);
    }

    public List<Document> search(String query, String stage, String knowledgePointId,
                                 Integer difficulty, int topK, double threshold) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold);
        List<String> filters = new ArrayList<>();
        if (stage != null && !stage.isBlank()) {
            filters.add("stage == '" + stage + "'");
        }
        if (knowledgePointId != null && !knowledgePointId.isBlank()) {
            filters.add("knowledgePointId == '" + knowledgePointId + "'");
        }
        if (difficulty != null) {
            filters.add("difficulty == " + difficulty);
        }
        if (!filters.isEmpty()) {
            builder.filterExpression(String.join(" && ", filters));
        }
        return vectorStore.similaritySearch(builder.build());
    }
}
