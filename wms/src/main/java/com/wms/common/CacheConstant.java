package com.wms.common;

public class CacheConstant {

    public static final String GOODS_LIST = "wms:goods:list";
    public static final String GOODS_NAME_PREFIX = "wms:goods:name:";
    public static final String GOODS_TYPE_PREFIX = "wms:goods:type:";
    public static final String GOODS_STORAGE_PREFIX = "wms:goods:storage:";
    public static final String GOODS_LOW_STOCK_PREFIX = "wms:goods:lowstock:";
    public static final String CATEGORY_LIST = "wms:category:list";
    public static final String STORAGE_LIST = "wms:storage:list";
    public static final String STOCK_SUMMARY = "wms:stock:summary";
    public static final String AI_CHAT_PREFIX = "wms:ai:chat:";

    public static final long TTL_GOODS_MINUTES = 10;
    public static final long TTL_CATEGORY_MINUTES = 30;
    public static final long TTL_STORAGE_MINUTES = 30;
    public static final long TTL_SUMMARY_MINUTES = 5;
    public static final long TTL_AI_CHAT_MINUTES = 30;
    public static final long TTL_TOOL_QUERY_MINUTES = 5;
}