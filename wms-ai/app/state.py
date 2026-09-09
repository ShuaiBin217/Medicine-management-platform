"""全局状态容器，避免循环导入。

由 main.py 在 lifespan 中设置，供 tools/knowledge_tool.py 运行时读取。
"""


class AppState:
    """全局应用状态单例。"""

    _instance: "AppState | None" = None

    def __init__(self):
        self.kb = None  # KnowledgeBaseService 实例，由 main.py lifespan 设置

    @classmethod
    def get(cls) -> "AppState":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance


def get_kb():
    """获取 KnowledgeBaseService 单例。"""
    return AppState.get().kb
