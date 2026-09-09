"""工具集导出，对齐 com.wms.ai.WmsTools 的 9 个 @Tool 方法。"""
from app.tools.goods_tools import (
    query_all_categories,
    query_all_goods,
    query_all_storages,
    query_goods_by_name,
    query_goods_by_storage_name,
    query_goods_by_type_name,
    query_low_stock_goods,
    query_stock_summary,
)
from app.tools.knowledge_tool import search_knowledge_base

# 顺序与 WmsTools.java 中定义一致
all_tools = [
    query_all_goods,
    query_goods_by_name,
    query_low_stock_goods,
    query_goods_by_type_name,
    query_goods_by_storage_name,
    query_all_categories,
    query_all_storages,
    query_stock_summary,
    search_knowledge_base,
]
