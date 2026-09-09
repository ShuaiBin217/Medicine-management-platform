"""Redis 服务：缓存 key 与 TTL 对齐 com.wms.common.CacheConstant。

复刻 Java String.hashCode() 以保证 wms:ai:chat:{hash} 一致。
Java 端 RedisTemplate 用 GenericJackson2JsonRedisSerializer，但迁移后 Java 仅 delete 工具
缓存 key（不 read），故 Python 用纯字符串值无序列化冲突。
"""
import redis

from app.config import Settings


# —— 缓存 key 常量（逐字对齐 CacheConstant.java）——
GOODS_LIST = "wms:goods:list"
GOODS_NAME_PREFIX = "wms:goods:name:"
GOODS_TYPE_PREFIX = "wms:goods:type:"
GOODS_STORAGE_PREFIX = "wms:goods:storage:"
GOODS_LOW_STOCK_PREFIX = "wms:goods:lowstock:"
CATEGORY_LIST = "wms:category:list"
STORAGE_LIST = "wms:storage:list"
STOCK_SUMMARY = "wms:stock:summary"
AI_CHAT_PREFIX = "wms:ai:chat:"

# —— TTL（秒，逐字对齐 CacheConstant.java 的 *_MINUTES）——
TTL_GOODS = 600          # 10 min
TTL_CATEGORY = 1800      # 30 min
TTL_STORAGE = 1800       # 30 min
TTL_SUMMARY = 300        # 5 min
TTL_AI_CHAT = 1800       # 30 min
TTL_TOOL_QUERY = 300    # 5 min


def java_string_hashcode(s: str) -> int:
    """复刻 Java String.hashCode()：h = 31 * h + char，处理 32 位溢出与负数。

    仅对 BMP 字符（中文+ASCII）精确；含 emoji 等增补平面字符会有偏差
    （Java 用 UTF-16 码元，Python 用码点）。本场景用户消息基本无 emoji。
    """
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    if h >= 0x80000000:
        h -= 0x100000000
    return h


class RedisService:
    def __init__(self, settings: Settings):
        self.r = redis.Redis(
            host=settings.spring_redis_host,
            port=settings.spring_redis_port,
            password=settings.spring_redis_password or None,
            db=settings.redis_db,
            decode_responses=True,  # 字符串直读
            socket_timeout=3,
            protocol=2,  # 兼容低版本 Redis（不支持 HELLO 命令的 RESP3 握手）
        )

    def get(self, key: str) -> str | None:
        return self.r.get(key)

    def set(self, key: str, val: str, ttl_seconds: int) -> None:
        self.r.setex(key, ttl_seconds, val)

    def delete(self, key: str) -> int:
        return self.r.delete(key)


# —— 模块级单例 ——
_redis: RedisService | None = None


def get_redis() -> RedisService:
    global _redis
    if _redis is None:
        from app.config import get_settings
        _redis = RedisService(get_settings())
    return _redis
