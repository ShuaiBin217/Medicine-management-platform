package com.wms.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Value("${ai.deepseek.temperature}")
    private Double temperature;

    @Value("${ai.deepseek.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.deepseek.max-memory-messages}")
    private Integer maxMemoryMessages;

    @Value("${ai.zhipu.api-key:}")
    private String zhipuApiKey;

    @Value("${ai.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String zhipuBaseUrl;

    @Value("${ai.zhipu.embedding-model:embedding-2}")
    private String zhipuEmbeddingModel;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        if (zhipuApiKey == null || zhipuApiKey.isEmpty() || "YOUR_ZHIPU_API_KEY".equals(zhipuApiKey)) {
            log.warn("智谱API Key未配置，RAG知识库功能不可用。请在application.yml中配置ai.zhipu.api-key");
            return null;
        }
        log.info("初始化智谱Embedding模型: baseUrl={}, model={}", zhipuBaseUrl, zhipuEmbeddingModel);
        return OpenAiEmbeddingModel.builder()
                .apiKey(zhipuApiKey)
                .baseUrl(zhipuBaseUrl)
                .modelName(zhipuEmbeddingModel)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public WmsAiService wmsAiService(OpenAiChatModel chatModel, WmsTools wmsTools) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMemoryMessages)
                .build();

        return AiServices.builder(WmsAiService.class)
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .tools(wmsTools)
                .build();
    }
}