"""FastAPI 入口：lifespan 加载知识库（对齐 Java @PostConstruct autoLoad）+ 挂载路由。"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.routers import ai_chat, knowledge
from app.services.knowledge_base import KnowledgeBaseService
from app.state import AppState


@asynccontextmanager
async def lifespan(app: FastAPI):
    """启动时初始化知识库，等价 Java @PostConstruct init + autoLoad。"""
    settings = get_settings()
    kb = KnowledgeBaseService(settings)
    kb.init()
    app.state.kb = kb
    AppState.get().kb = kb  # 供 tools/knowledge_tool.py 运行时读取
    yield
    app.state.kb = None
    AppState.get().kb = None


app = FastAPI(title="WMS AI", lifespan=lifespan)

# CORS：对齐 Java CorsConfig，允许前端（8080）跨域访问
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

app.include_router(ai_chat.router)
app.include_router(knowledge.router)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    settings = get_settings()
    uvicorn.run("main:app", host="0.0.0.0", port=settings.ai_port, reload=False)
