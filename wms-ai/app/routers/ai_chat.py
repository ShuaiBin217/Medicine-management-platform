"""POST /ai/chat 与 /ai/chat/stream 路由，对齐 com.wms.controller.AiChatController。

- /ai/chat：一次性返回（兼容保留），Redis 缓存 key wms:ai:chat:{hashCode(message)} TTL 30min。
- /ai/chat/stream：SSE 流式返回，事件类型 token / tool_start / tool_end / done / error。
  完整回答结束后同样写入 Redis 缓存；命中缓存则整段下发。
异常时仍以 error 事件下发（对齐 Java「异常仍返回 200」的语义）。
"""
import json

from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.schemas import Result
from app.services.agent_service import build_history, get_agent_executor
from app.services.redis_service import AI_CHAT_PREFIX, TTL_AI_CHAT, get_redis
from app.services.redis_service import java_string_hashcode
from app.tools import all_tools

router = APIRouter(prefix="/ai")

# 工具名 → 中文描述（取 docstring 首行），供前端展示"正在查询：xxx"
_TOOL_LABELS = {
    t.name: (t.description or "").strip().splitlines()[0] if (t.description or "").strip() else t.name
    for t in all_tools
}


def _sse(payload: dict) -> str:
    return f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"


@router.post("/chat")
def chat(body: dict) -> Result:
    message = (body.get("message") or "").strip()
    history = body.get("history") or []
    if not message:
        return Result.fail()

    redis = get_redis()
    try:
        cache_key = AI_CHAT_PREFIX + str(java_string_hashcode(message))
        cached = redis.get(cache_key)
        if cached:
            return Result.suc({"reply": cached})

        executor = get_agent_executor()
        result = executor.invoke({
            "input": message,
            "history": build_history(history),
        })
        reply = result.get("output", "")
        redis.set(cache_key, reply, TTL_AI_CHAT)
        return Result.suc({"reply": reply})
    except Exception as e:
        # 对齐 Java：异常仍返回 200；真实错误只记服务端日志，不下发用户
        print(f"[AI] 非流式对话异常: {e!r}")
        return Result.suc({"reply": "抱歉，智能助手暂时无法回答，请稍后再试。"})


@router.post("/chat/stream")
async def chat_stream(body: dict):
    message = (body.get("message") or "").strip()
    history = body.get("history") or []

    async def event_gen():
        if not message:
            yield _sse({"type": "error", "content": "消息不能为空"})
            yield _sse({"type": "done"})
            return

        redis = get_redis()
        try:
            cache_key = AI_CHAT_PREFIX + str(java_string_hashcode(message))
            cached = redis.get(cache_key)
            if cached:
                yield _sse({"type": "token", "content": cached})
                yield _sse({"type": "done"})
                return

            executor = get_agent_executor()
            full_reply: list[str] = []
            async for event in executor.astream_events(
                {"input": message, "history": build_history(history)},
                version="v2",
            ):
                kind = event["event"]
                if kind == "on_chat_model_stream":
                    chunk = event["data"]["chunk"]
                    # 工具调用轮次只有 tool_call_chunks，无正文，跳过
                    if getattr(chunk, "tool_call_chunks", None):
                        continue
                    content = chunk.content
                    if isinstance(content, str) and content:
                        full_reply.append(content)
                        yield _sse({"type": "token", "content": content})
                elif kind == "on_tool_start":
                    name = event.get("name", "")
                    yield _sse({"type": "tool_start", "name": name, "label": _TOOL_LABELS.get(name, name)})
                elif kind == "on_tool_end":
                    name = event.get("name", "")
                    yield _sse({"type": "tool_end", "name": name, "label": _TOOL_LABELS.get(name, name)})

            reply = "".join(full_reply)
            if reply:
                redis.set(cache_key, reply, TTL_AI_CHAT)
            yield _sse({"type": "done"})
        except Exception as e:
            print(f"[AI] 流式对话异常: {e!r}")
            yield _sse({"type": "error", "content": "抱歉，智能助手暂时无法回答，请稍后再试。"})
            yield _sse({"type": "done"})

    return StreamingResponse(
        event_gen(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",  # Nginx 代理时不缓冲，保证流式
        },
    )
