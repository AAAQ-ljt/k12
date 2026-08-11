package com.easymall.configuration;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;


public class CustomMultimodalEmbeddingModel implements EmbeddingModel {

    private RestClient restClient;

    private String modelName;

    private String embeddingsPath;

    public CustomMultimodalEmbeddingModel(String baseUrl, String embeddingsPath, String apiKey, String modelName) {
        this.embeddingsPath = embeddingsPath;
        this.modelName = modelName;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {

        // 2. 构建符合豆包API的请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", this.modelName);
        List<Map<String, String>> textInputs = new ArrayList<>();
        textInputs.add(Map.of("type", "text", "text", request.getInstructions().stream().collect(Collectors.joining("，"))));
        requestBody.put("input", textInputs);
        ResponseEntity<Map> responseEntity = restClient.post()
                .uri(embeddingsPath) // 使用您配置中的路径
                .body(requestBody)
                .retrieve()
                .toEntity(Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        return parseResponse(responseBody);
    }

    @Override
    public float[] embed(Document document) {
        return new float[0];
    }

    @Override
    public List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
        Assert.notNull(documents, "Documents must not be null");
        List<float[]> embeddings = new ArrayList(documents.size());
        List<String> texts = documents.stream().collect(Collectors.mapping(Document::getText, Collectors.toList()));
        EmbeddingRequest request = new EmbeddingRequest(texts, options);
        EmbeddingResponse response = this.call(request);
        embeddings.add(response.getResult().getOutput());
        return embeddings;
    }

    private EmbeddingResponse parseResponse(Map<String, Object> responseBody) {
        // 1. 解析返回的向量数据列表
        LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) responseBody.get("data");
        List<Embedding> embeddings = new ArrayList<>();
        List<Object> embeddingList = (List<Object>) dataMap.get("embedding");
        float[] embeddingArray = new float[embeddingList.size()];
        for (int j = 0; j < embeddingList.size(); j++) {
            try {
                if (embeddingList.get(j) instanceof Integer) {
                    embeddingArray[j] = (Integer) embeddingList.get(j);
                } else if (embeddingList.get(j) instanceof Double) {
                    embeddingArray[j] = ((Double) embeddingList.get(j)).floatValue();
                }
            } catch (Exception e) {
                embeddingList.get(j);
            }
        }
        // 使用当前索引作为Embedding的index
        Embedding embedding = new Embedding(embeddingArray, 0, EmbeddingResultMetadata.EMPTY);
        embeddings.add(embedding);
        return new EmbeddingResponse(embeddings);
    }
}