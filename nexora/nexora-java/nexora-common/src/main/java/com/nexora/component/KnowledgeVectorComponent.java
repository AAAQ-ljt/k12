package com.nexora.component;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库 ES 向量操作：官方知识库与学生个人知识库共用。
 */
@Component
public class KnowledgeVectorComponent {

    /**
     * 向量化接口单批上限建议 10-15 条，避开 20 条限制并留余量。
     */
    private static final int EMBEDDING_BATCH_SIZE = 15;

    @Autowired
    private ObjectProvider<VectorStore> vectorStoreProvider;

    public void saveChunks(String docId, String title, String stage, String knowledgePointId,
                           Integer difficulty, String sourceUrl, String sourceResourceId,
                           String ownerId, List<String> chunks) {
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
            metadata.put("ownerId", ownerId == null ? "" : ownerId);
            if (sourceUrl != null && !sourceUrl.isBlank()) {
                metadata.put("sourceUrl", sourceUrl);
            }
            if (sourceResourceId != null && !sourceResourceId.isBlank()) {
                metadata.put("sourceResourceId", sourceResourceId);
            }
            documents.add(Document.builder()
                    .id(docId + "_" + i)
                    .text(chunks.get(i))
                    .metadata(metadata)
                    .build());
        }
        if (!documents.isEmpty()) {
            VectorStore vectorStore = vectorStoreProvider.getObject();
            for (int i = 0; i < documents.size(); i += EMBEDDING_BATCH_SIZE) {
                int end = Math.min(i + EMBEDDING_BATCH_SIZE, documents.size());
                vectorStore.add(new ArrayList<>(documents.subList(i, end)));
            }
        }
    }

    public void deleteChunks(String docId, int maxCount) {
        if (maxCount <= 0) {
            return;
        }
        VectorStore vectorStore = vectorStoreProvider.getObject();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            ids.add(docId + "_" + i);
        }
        vectorStore.delete(ids);
    }

    /**
     * ES 向量检索过滤表达式转 query_string 时不支持空字符串等值（ownerId == '' 会生成非法查询 metadata.ownerId:），
     * 官方库检索不下发 ownerId 过滤，改为超量取回后在内存按 metadata 过滤。
     */
    private static final int OFFICIAL_SEARCH_OVER_FETCH = 3;

    public List<Document> search(String query, String stage, String knowledgePointId,
                                 Integer difficulty, String ownerId, int topK, double threshold) {
        boolean officialSearch = ownerId == null || ownerId.isBlank();
        List<String> filters = new ArrayList<>();
        if (!officialSearch) {
            filters.add("ownerId == '" + ownerId + "'");
        }
        if (stage != null && !stage.isBlank()) {
            filters.add("stage == '" + stage + "'");
        }
        if (knowledgePointId != null && !knowledgePointId.isBlank()) {
            filters.add("knowledgePointId == '" + knowledgePointId + "'");
        }
        if (difficulty != null) {
            filters.add("difficulty == " + difficulty);
        }
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(officialSearch ? Math.min(topK * OFFICIAL_SEARCH_OVER_FETCH, 50) : topK)
                .similarityThreshold(threshold);
        if (!filters.isEmpty()) {
            builder.filterExpression(String.join(" && ", filters));
        }
        VectorStore vectorStore = vectorStoreProvider.getObject();
        List<Document> documents = vectorStore.similaritySearch(builder.build());
        if (!officialSearch) {
            return documents;
        }
        return documents.stream()
                .filter(doc -> String.valueOf(doc.getMetadata().getOrDefault("ownerId", "")).isEmpty())
                .limit(topK)
                .toList();
    }
}
