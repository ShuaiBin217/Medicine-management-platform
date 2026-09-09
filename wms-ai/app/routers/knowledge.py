"""/knowledge/* 路由，对齐 com.wms.controller.KnowledgeBaseController。

6 个接口：
- POST /knowledge/import/file?filePath=xxx  → {filePath, segments}
- POST /knowledge/import/text  body:{title, content}  → {title, segments}
- GET  /knowledge/search?query=xxx  → {result}
- GET  /knowledge/list  → {documents, count, enabled}
- POST /knowledge/reload  → {count, enabled}
- DELETE /knowledge/clear  → null
"""
from fastapi import APIRouter, Request

from app.schemas import Result

router = APIRouter(prefix="/knowledge")


def _kb(request: Request):
    return request.app.state.kb


@router.post("/import/file")
def import_file(file_path: str, request: Request) -> Result:
    try:
        kb = _kb(request)
        segments = kb.import_document(file_path)
        return Result.suc({"filePath": file_path, "segments": segments})
    except Exception:
        return Result.fail()


@router.post("/import/text")
def import_text(body: dict, request: Request) -> Result:
    title = body.get("title")
    content = body.get("content")
    if not title or not content or not content.strip():
        return Result.fail()
    try:
        kb = _kb(request)
        segments = kb.import_text(content, title)
        return Result.suc({"title": title, "segments": segments})
    except Exception:
        return Result.fail()


@router.get("/search")
def search(query: str, request: Request) -> Result:
    kb = _kb(request)
    result = kb.search(query)
    return Result.suc({"result": result})


@router.get("/list")
def list_docs(request: Request) -> Result:
    kb = _kb(request)
    documents = kb.list_documents()
    return Result.suc({
        "documents": documents,
        "count": len(documents),
        "enabled": kb.is_enabled(),
    })


@router.post("/reload")
def reload(request: Request) -> Result:
    kb = _kb(request)
    kb.load_from_directory()
    return Result.suc({
        "count": kb.get_document_count(),
        "enabled": kb.is_enabled(),
    })


@router.delete("/clear")
def clear(request: Request) -> Result:
    kb = _kb(request)
    kb.clear_index()
    return Result.suc()
