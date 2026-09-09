"""知识库检索工具：对齐 WmsTools.searchKnowledgeBase(query)。

通过 app.state.AppState 获取 KnowledgeBaseService 单例。
"""
from langchain_core.tools import tool

from app.state import get_kb


@tool
def search_knowledge_base(query: str) -> str:
    """从药品知识库中检索药品说明书、用药指南、用法用量、禁忌症、不良反应等专业文档内容。"""
    kb = get_kb()
    if kb is None:
        return "知识库服务未初始化。"
    return kb.search(query)
