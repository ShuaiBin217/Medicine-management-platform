"""RAG 知识库：对齐 com.wms.ai.KnowledgeBaseService。

LangChain4j → LangChain Python 映射：
- OpenAiEmbeddingModel(智谱)        → OpenAIEmbeddings(api_key, base_url=zhipu, model)
- DocumentSplitters.recursive(300,30) → RecursiveCharacterTextSplitter(chunk_size, chunk_overlap)
- InMemoryEmbeddingStore             → InMemoryVectorStore
- EmbeddingSearchRequest(max, minScore) → similarity_search_with_score(query, k) + 客户端过滤
- @PostConstruct autoLoad           → FastAPI lifespan 启动加载
"""
import os
from pathlib import Path

from langchain_community.vectorstores import InMemoryVectorStore
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import Settings


class KnowledgeBaseService:
    def __init__(self, settings: Settings):
        self.settings = settings
        self.embeddings = None
        self.store: InMemoryVectorStore | None = None
        self.indexed: set[str] = set()
        self.enabled = False

    # —— 对齐 Java @PostConstruct init ——
    def init(self) -> None:
        if not self.settings.zhipu_api_key or self.settings.zhipu_api_key in ("your-zhipu-api-key", "YOUR_ZHIPU_API_KEY"):
            print("[KB] 智谱 API Key 未配置，RAG 知识库功能不可用")
            self.enabled = False
            return

        try:
            from langchain_openai import OpenAIEmbeddings
            self.embeddings = OpenAIEmbeddings(
                api_key=self.settings.zhipu_api_key,
                base_url=self.settings.zhipu_base_url,
                model=self.settings.zhipu_embedding_model,
            )
            # 触发一次轻量调用，验证可用性
            self.embeddings.embed_query("ping")
            self.store = InMemoryVectorStore(self.embeddings)
            self.enabled = True
            print(f"[KB] 初始化智谱 Embedding 成功: model={self.settings.zhipu_embedding_model}")
        except Exception as e:
            # 回退：尝试 langchain-community 的 ZhipuAIEmbeddings
            try:
                from langchain_community.embeddings import ZhipuAIEmbeddings
                self.embeddings = ZhipuAIEmbeddings(
                    zhipuai_api_key=self.settings.zhipu_api_key,
                    model=self.settings.zhipu_embedding_model,
                )
                self.embeddings.embed_query("ping")
                self.store = InMemoryVectorStore(self.embeddings)
                self.enabled = True
                print(f"[KB] 回退到 ZhipuAIEmbeddings 成功: {e}")
            except Exception as e2:
                print(f"[KB] Embedding 初始化失败，RAG 禁用: {e2}")
                self.enabled = False
                return

        if self.settings.kb_auto_load:
            self.load_from_directory()

    def is_enabled(self) -> bool:
        return self.enabled

    # —— 对齐 Java loadFromDirectory ——
    def load_from_directory(self) -> None:
        if not self.enabled:
            print("[KB] RAG 知识库未启用，无法加载文档")
            return
        kb_path = Path(self.settings.kb_path)
        if not kb_path.exists():
            kb_path.mkdir(parents=True, exist_ok=True)
            print(f"[KB] 创建知识库目录: {kb_path.resolve()}")
            return
        if not kb_path.is_dir():
            print(f"[KB] 知识库路径不是目录: {kb_path}")
            return

        doc_files = [
            p for p in kb_path.rglob("*")
            if p.is_file() and p.suffix.lower() in (".txt", ".md")
        ]
        if not doc_files:
            print(f"[KB] 知识库目录为空: {kb_path}")
            return

        print(f"[KB] 开始加载知识库文档，共 {len(doc_files)} 个文件")
        total_segments = 0
        for file in doc_files:
            try:
                total_segments += self.import_document(str(file))
            except Exception as e:
                print(f"[KB] 导入文档失败 {file}: {e}")
        print(f"[KB] 知识库加载完成，共导入 {len(self.indexed)} 个文档，生成 {total_segments} 个分块")

    def import_document(self, file_path: str) -> int:
        if not self.enabled:
            raise IllegalStateException("RAG 知识库未启用，请先配置 ZHIPU_API_KEY")
        path = Path(file_path)
        content = path.read_text(encoding="utf-8")
        return self.import_text(content, path.name)

    def import_text(self, content: str, title: str) -> int:
        if not self.enabled:
            raise IllegalStateException("RAG 知识库未启用，请先配置 ZHIPU_API_KEY")
        if not content or not content.strip():
            raise ValueError("文档内容不能为空")

        splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.settings.kb_chunk_size,
            chunk_overlap=self.settings.kb_chunk_overlap,
        )
        docs = splitter.split_documents([Document(page_content=content)])
        # InMemoryVectorStore.add_documents 内部会 embed
        self.store.add_documents(docs)
        self.indexed.add(title)
        print(f"[KB] 导入文档成功: {title}, 生成 {len(docs)} 个分块")
        return len(docs)

    # —— 对齐 Java search ——
    def search(self, query: str) -> str:
        if not self.enabled:
            return "知识库功能未启用。请在 .env 中配置 ZHIPU_API_KEY 后重启应用。"
        if not self.indexed:
            return f"知识库中暂无文档。请先通过 /knowledge/import 接口导入文档，或将文档放入 {self.settings.kb_path} 目录。"

        # similarity_search_with_score 返回 [(Document, score)]
        # 注意：InMemoryVectorStore 返回的是余弦距离/相似度，与 LangChain4j 的 score 刻度可能略有差异
        matches = self.store.similarity_search_with_score(query, k=self.settings.kb_max_results)
        # 按 min_score 过滤
        filtered = [(d, s) for d, s in matches if s >= self.settings.kb_min_score]
        if not filtered:
            return f"知识库中未找到与「{query}」相关的内容"

        lines = [f"从知识库检索到 {len(filtered)} 条相关内容：", ""]
        for doc, score in filtered:
            lines.append(f"【相关度: {score * 100:.0f}%】")
            lines.append(doc.page_content)
            lines.append("")
        return "\n".join(lines)

    def list_documents(self) -> set[str]:
        return set(self.indexed)

    def get_document_count(self) -> int:
        return len(self.indexed)

    def clear_index(self) -> None:
        self.indexed.clear()
        # InMemoryVectorStore 无 clear API，重启才能完全清空向量
        print("[KB] 已清除文档索引记录（注意：向量存储需重启应用才能完全清除）")


class IllegalStateException(Exception):
    pass
