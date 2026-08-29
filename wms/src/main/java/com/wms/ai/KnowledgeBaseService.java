package com.wms.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Value("${ai.knowledge-base.path:knowledge-base}")
    private String knowledgeBasePath;

    @Value("${ai.knowledge-base.chunk-size:300}")
    private int chunkSize;

    @Value("${ai.knowledge-base.chunk-overlap:30}")
    private int chunkOverlap;

    @Value("${ai.knowledge-base.max-results:3}")
    private int maxResults;

    @Value("${ai.knowledge-base.min-score:0.6}")
    private double minScore;

    @Value("${ai.knowledge-base.auto-load:true}")
    private boolean autoLoad;

    private final Set<String> indexedDocuments = ConcurrentHashMap.newKeySet();

    private volatile boolean enabled = false;

    @PostConstruct
    public void init() {
        if (embeddingModel == null) {
            log.warn("Embedding模型未初始化，RAG知识库功能已禁用");
            enabled = false;
            return;
        }
        enabled = true;
        if (autoLoad) {
            loadFromDirectory();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void loadFromDirectory() {
        if (!enabled) {
            log.warn("RAG知识库未启用，无法加载文档");
            return;
        }
        Path dir = Path.of(knowledgeBasePath);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                log.info("创建知识库目录: {}", dir.toAbsolutePath());
            } catch (IOException e) {
                log.error("创建知识库目录失败: {}", knowledgeBasePath, e);
                return;
            }
        }
        if (!Files.isDirectory(dir)) {
            log.error("知识库路径不是目录: {}", knowledgeBasePath);
            return;
        }
        try (Stream<Path> files = Files.walk(dir)) {
            List<Path> docFiles = files
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.toString().toLowerCase();
                        return name.endsWith(".txt") || name.endsWith(".md");
                    })
                    .collect(Collectors.toList());

            if (docFiles.isEmpty()) {
                log.info("知识库目录为空，无文档可加载: {}", knowledgeBasePath);
                return;
            }

            log.info("开始加载知识库文档，共 {} 个文件", docFiles.size());
            int totalSegments = 0;
            for (Path file : docFiles) {
                try {
                    int segments = importDocument(file.toString());
                    totalSegments += segments;
                } catch (Exception e) {
                    log.error("导入文档失败: {}", file, e);
                }
            }
            log.info("知识库加载完成，共导入 {} 个文档，生成 {} 个分块", indexedDocuments.size(), totalSegments);
        } catch (IOException e) {
            log.error("遍历知识库目录失败", e);
        }
    }

    public int importDocument(String filePath) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("RAG知识库未启用，请先配置智谱API Key");
        }
        Path path = Path.of(filePath);
        String content = Files.readString(path);
        String fileName = path.getFileName().toString();
        return importText(content, fileName);
    }

    public int importText(String content, String title) {
        if (!enabled) {
            throw new IllegalStateException("RAG知识库未启用，请先配置智谱API Key");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文档内容不能为空");
        }

        Document document = Document.from(content);

        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);

        indexedDocuments.add(title);
        log.info("导入文档成功: {}, 生成 {} 个分块", title, segments.size());
        return segments.size();
    }

    public String search(String query) {
        if (!enabled) {
            return "知识库功能未启用。请在application.yml中配置ai.zhipu.api-key后重启应用。";
        }
        if (indexedDocuments.isEmpty()) {
            return "知识库中暂无文档。请先通过 /knowledge/import 接口导入文档，或将文档放入 " + knowledgeBasePath + " 目录。";
        }

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        List<EmbeddingMatch<TextSegment>> matches = result.matches();

        if (matches.isEmpty()) {
            return "知识库中未找到与「" + query + "」相关的内容";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("从知识库检索到 ").append(matches.size()).append(" 条相关内容：\n\n");
        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            sb.append("【相关度: ").append(String.format("%.0f%%", match.score() * 100)).append("】\n");
            sb.append(match.embedded().text()).append("\n\n");
        }
        return sb.toString();
    }

    public Set<String> listDocuments() {
        return new HashSet<>(indexedDocuments);
    }

    public int getDocumentCount() {
        return indexedDocuments.size();
    }

    public void clearIndex() {
        indexedDocuments.clear();
        log.warn("已清除文档索引记录（注意：向量存储需重启应用才能完全清除）");
    }
}