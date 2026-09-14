"""业务数据访问层：httpx 调用 Spring Boot 业务 API。

对应 Java 端：
- POST /goods/listPage（分页查商品，body: {pageNum, pageSize, param:{name, goodstype, storage}}）
- GET /storage/list（全量仓库）
- GET /goodstype/list（全量分类）
返回统一格式 {code, msg, total, data}。

Python 端统一拉全量在内存过滤，靠 Redis 缓存抵消开销。
"""
import httpx

from app.config import Settings


class WmsClient:
    def __init__(self, settings: Settings):
        self.base = settings.wms_backend_url.rstrip("/")
        self.client = httpx.Client(timeout=10.0)

    def list_all_goods(self) -> list[dict]:
        """/goods 只有 listPage，用大 pageSize 取全量。

        字段：{id, name, storage(药房id), goodstype(分类id，小写非驼峰), count, remark}
        注意：后端 JSON 实际返回小写 goodstype，Python 取值必须用 'goodstype'。
        """
        resp = self.client.post(
            f"{self.base}/goods/listPage",
            json={"pageNum": 1, "pageSize": 100000, "param": {}},
        )
        resp.raise_for_status()
        body = resp.json()
        data = body.get("data") or {}
        # 分页接口 data 可能是 dict（含 records/total）或 list
        if isinstance(data, dict):
            return data.get("records") or []
        return data or []

    def list_all_storages(self) -> list[dict]:
        resp = self.client.get(f"{self.base}/storage/list")
        resp.raise_for_status()
        body = resp.json()
        return body.get("data") or []

    def list_all_categories(self) -> list[dict]:
        resp = self.client.get(f"{self.base}/goodstype/list")
        resp.raise_for_status()
        body = resp.json()
        return body.get("data") or []


# —— 模块级单例 ——
_wms: WmsClient | None = None


def get_wms() -> WmsClient:
    global _wms
    if _wms is None:
        from app.config import get_settings
        _wms = WmsClient(get_settings())
    return _wms
