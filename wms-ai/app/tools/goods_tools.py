"""8 个业务工具：对齐 com.wms.ai.WmsTools 中除 searchKnowledgeBase 外的工具。

每个工具：
- 用 @tool 装饰器，docstring 即工具描述（等价 @Tool("...")）
- 入参类型注解生成 JSON Schema 给 LLM
- Redis 缓存 key 与 TTL 逐字对齐 CacheConstant.java
- 输出格式逐字对齐 WmsTools.java 的中文格式化字符串

数据获取：通过 wms_client 拉全量后在内存过滤（与 Java 服务层逻辑等价，靠 Redis 抵消开销）。
"""
from langchain_core.tools import tool

from app.services.redis_service import (
    CATEGORY_LIST,
    GOODS_LIST,
    GOODS_LOW_STOCK_PREFIX,
    GOODS_NAME_PREFIX,
    GOODS_STORAGE_PREFIX,
    GOODS_TYPE_PREFIX,
    STORAGE_LIST,
    STOCK_SUMMARY,
    TTL_CATEGORY,
    TTL_GOODS,
    TTL_STORAGE,
    TTL_SUMMARY,
    TTL_TOOL_QUERY,
    get_redis,
)
from app.services.wms_client import get_wms


def _format_goods_line(g: dict, type_map: dict[int, str], storage_map: dict[int, str]) -> str:
    """格式化单条药品，对齐 WmsTools.java 第 53-58 行。"""
    remark = g.get("remark") or "无"
    return (
        f"- {g.get('name')} "
        f"| 分类: {type_map.get(g.get('goodsType'), '未知')} "
        f"| 药房: {storage_map.get(g.get('storage'), '未知')} "
        f"| 库存: {g.get('count')} "
        f"| 备注: {remark}"
    )


def _type_map() -> dict[int, str]:
    wms = get_wms()
    return {c["id"]: c["name"] for c in wms.list_all_categories()}


def _storage_map() -> dict[int, str]:
    wms = get_wms()
    return {s["id"]: s["name"] for s in wms.list_all_storages()}


@tool
def query_all_goods() -> str:
    """查询所有药品的库存信息，包括药品名、分类、药房、库存数量和备注。"""
    redis = get_redis()
    cached = redis.get(GOODS_LIST)
    if cached:
        return cached

    wms = get_wms()
    goods_list = wms.list_all_goods()
    tmap = _type_map()
    smap = _storage_map()

    lines = [f"共 {len(goods_list)} 种药品："]
    for g in goods_list:
        lines.append(_format_goods_line(g, tmap, smap))

    result = "\n".join(lines) + "\n"
    redis.set(GOODS_LIST, result, TTL_GOODS)
    return result


@tool
def query_goods_by_name(name: str) -> str:
    """根据药品名称模糊查询药品库存信息。"""
    cache_key = GOODS_NAME_PREFIX + name
    redis = get_redis()
    cached = redis.get(cache_key)
    if cached:
        return cached

    wms = get_wms()
    all_goods = wms.list_all_goods()
    goods_list = [g for g in all_goods if name in (g.get("name") or "")]
    if not goods_list:
        return f"未找到名称包含「{name}」的药品"

    tmap = _type_map()
    smap = _storage_map()
    lines = [f"找到 {len(goods_list)} 种药品："]
    for g in goods_list:
        lines.append(_format_goods_line(g, tmap, smap))

    result = "\n".join(lines) + "\n"
    redis.set(cache_key, result, TTL_TOOL_QUERY)
    return result


@tool
def query_low_stock_goods(threshold: int) -> str:
    """查询库存数量低于或等于指定阈值的药品，用于低库存预警。"""
    cache_key = GOODS_LOW_STOCK_PREFIX + str(threshold)
    redis = get_redis()
    cached = redis.get(cache_key)
    if cached:
        return cached

    wms = get_wms()
    all_goods = wms.list_all_goods()
    goods_list = [g for g in all_goods if (g.get("count") or 0) <= threshold]
    if not goods_list:
        return f"当前没有库存低于 {threshold} 的药品，库存充足！"

    tmap = _type_map()
    smap = _storage_map()
    lines = [f"以下 {len(goods_list)} 种药品库存低于等于 {threshold}，需要补货："]
    for g in goods_list:
        lines.append(_format_goods_line(g, tmap, smap))

    result = "\n".join(lines) + "\n"
    redis.set(cache_key, result, TTL_TOOL_QUERY)
    return result


@tool
def query_goods_by_type_name(typeName: str) -> str:
    """查询指定分类下的所有药品。"""
    cache_key = GOODS_TYPE_PREFIX + typeName
    redis = get_redis()
    cached = redis.get(cache_key)
    if cached:
        return cached

    wms = get_wms()
    categories = wms.list_all_categories()
    target = next((c for c in categories if c.get("name") == typeName), None)
    if not target:
        return f"未找到分类「{typeName}」，请检查分类名称"

    all_goods = wms.list_all_goods()
    goods_list = [g for g in all_goods if g.get("goodsType") == target["id"]]
    if not goods_list:
        return f"分类「{typeName}」下暂无药品"

    smap = _storage_map()
    lines = [f"分类「{typeName}」下共 {len(goods_list)} 种药品："]
    for g in goods_list:
        remark = g.get("remark") or "无"
        lines.append(
            f"- {g.get('name')} "
            f"| 药房: {smap.get(g.get('storage'), '未知')} "
            f"| 库存: {g.get('count')} "
            f"| 备注: {remark}"
        )

    result = "\n".join(lines) + "\n"
    redis.set(cache_key, result, TTL_TOOL_QUERY)
    return result


@tool
def query_goods_by_storage_name(storageName: str) -> str:
    """查询指定药房下的所有药品。"""
    cache_key = GOODS_STORAGE_PREFIX + storageName
    redis = get_redis()
    cached = redis.get(cache_key)
    if cached:
        return cached

    wms = get_wms()
    storages = wms.list_all_storages()
    target = next((s for s in storages if s.get("name") == storageName), None)
    if not target:
        return f"未找到药房「{storageName}」，请检查药房名称"

    all_goods = wms.list_all_goods()
    goods_list = [g for g in all_goods if g.get("storage") == target["id"]]
    if not goods_list:
        return f"药房「{storageName}」下暂无药品"

    tmap = _type_map()
    lines = [f"药房「{storageName}」下共 {len(goods_list)} 种药品："]
    for g in goods_list:
        remark = g.get("remark") or "无"
        lines.append(
            f"- {g.get('name')} "
            f"| 分类: {tmap.get(g.get('goodsType'), '未知')} "
            f"| 库存: {g.get('count')} "
            f"| 备注: {remark}"
        )

    result = "\n".join(lines) + "\n"
    redis.set(cache_key, result, TTL_TOOL_QUERY)
    return result


@tool
def query_all_categories() -> str:
    """查询所有药品分类列表。"""
    redis = get_redis()
    cached = redis.get(CATEGORY_LIST)
    if cached:
        return cached

    wms = get_wms()
    type_list = wms.list_all_categories()
    if not type_list:
        return "暂无药品分类数据"

    all_goods = wms.list_all_goods()
    lines = [f"共 {len(type_list)} 个分类："]
    for t in type_list:
        count = sum(1 for g in all_goods if g.get("goodsType") == t.get("id"))
        remark = t.get("remark")
        line = f"- {t.get('name')}（含 {count} 种药品）"
        if remark:
            line += f" | 备注: {remark}"
        lines.append(line)

    result = "\n".join(lines) + "\n"
    redis.set(CATEGORY_LIST, result, TTL_CATEGORY)
    return result


@tool
def query_all_storages() -> str:
    """查询所有药房/仓库列表。"""
    redis = get_redis()
    cached = redis.get(STORAGE_LIST)
    if cached:
        return cached

    wms = get_wms()
    storage_list = wms.list_all_storages()
    if not storage_list:
        return "暂无药房数据"

    all_goods = wms.list_all_goods()
    lines = [f"共 {len(storage_list)} 个药房："]
    for s in storage_list:
        count = sum(1 for g in all_goods if g.get("storage") == s.get("id"))
        remark = s.get("remark")
        line = f"- {s.get('name')}（含 {count} 种药品）"
        if remark:
            line += f" | 备注: {remark}"
        lines.append(line)

    result = "\n".join(lines) + "\n"
    redis.set(STORAGE_LIST, result, TTL_STORAGE)
    return result


@tool
def query_stock_summary() -> str:
    """查询库存统计摘要：总药品数、总库存量、低库存预警数。"""
    redis = get_redis()
    cached = redis.get(STOCK_SUMMARY)
    if cached:
        return cached

    wms = get_wms()
    goods_list = wms.list_all_goods()
    total_kinds = len(goods_list)
    total_stock = sum((g.get("count") or 0) for g in goods_list)
    low_stock_count = sum(1 for g in goods_list if (g.get("count") or 0) <= 10)
    zero_stock_count = sum(1 for g in goods_list if (g.get("count") or 0) <= 0)

    lines = [
        "库存统计摘要：",
        f"- 药品种类总数: {total_kinds}",
        f"- 库存总量: {total_stock}",
        f"- 低库存预警(≤10): {low_stock_count} 种",
        f"- 缺货(≤0): {zero_stock_count} 种",
        "",
    ]
    result = "\n".join(lines)
    redis.set(STOCK_SUMMARY, result, TTL_SUMMARY)
    return result
