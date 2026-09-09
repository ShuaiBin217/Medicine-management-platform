"""配置管理：环境变量名对齐 Java application.yml 与 README 中的约定。"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # DeepSeek（对齐 ai.deepseek.*）
    deepseek_api_key: str = "sk-86aa01fc83e14f449184fe0b162b6142"
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-chat"
    deepseek_temperature: float = 0.5
    deepseek_max_tokens: int = 2048
    deepseek_max_memory_messages: int = 20

    # 智谱（对齐 ai.zhipu.*）
    zhipu_api_key: str = "be8fbef57f1949f88d203e7d1fb99e71.WOlso4yKnrfV2E9w"
    zhipu_base_url: str = "https://open.bigmodel.cn/api/paas/v4"
    zhipu_embedding_model: str = "embedding-2"

    # Spring Boot 业务后端
    wms_backend_url: str = "http://localhost:8090"

    # Redis（对齐 SPRING_REDIS_* 环境变量名）
    spring_redis_host: str = "localhost"
    spring_redis_port: int = 6379
    spring_redis_password: str = ""
    redis_db: int = 0

    # 知识库
    kb_path: str = "knowledge-base"
    kb_chunk_size: int = 300
    kb_chunk_overlap: int = 30
    kb_max_results: int = 3
    kb_min_score: float = 0.4
    kb_auto_load: bool = True

    # 服务
    ai_port: int = 8091

    class Config:
        env_file = ".env"
        extra = "ignore"


_settings: Settings | None = None


def get_settings() -> Settings:
    global _settings
    if _settings is None:
        _settings = Settings()
    return _settings
