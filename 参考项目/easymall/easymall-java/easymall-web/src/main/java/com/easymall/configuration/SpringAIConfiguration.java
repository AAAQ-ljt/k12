package com.easymall.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringAIConfiguration {

    @Value("${spring.ai.otherai.base-url:}")
    private String otheraiBaseUrl;

    @Value("${spring.ai.otherai.api-key:}")
    private String otheraiApiKey;

    @Value("${spring.ai.otherai.embedding.model:}")
    private String otheraiEmbeddingModel;

    @Value("${spring.ai.otherai.embeddingsPath:}")
    private String otheraiEmbeddingsPath;

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        Map<String, Object> extraBody = new HashMap<>();
        extraBody.put("enable_thinking", false);
        return ChatClient.builder(openAiChatModel).defaultOptions(OpenAiChatOptions.builder().extraBody(extraBody).build()).build();
    }

    @Primary
    @Bean
    public EmbeddingModel embeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }


    //如果 使用其他的向量模型,比如豆包的就开启下面这个,豆包没有按照标准的openai标准,所以需要自定义向量模型解析
    /*
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new CustomMultimodalEmbeddingModel(otheraiBaseUrl, otheraiEmbeddingsPath,otheraiApiKey, otheraiEmbeddingModel);
    }
*/

    //本地ollama 配置
/*
    @Bean
    @Primary
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaChatOptions.builder().disableThinking().build())//禁用思考
                .build();
    }
*/

/*    @Bean
    @Primary
    public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }*/
}

