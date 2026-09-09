# WMS AI 微服务（Python FastAPI + LangChain）

由 Java LangChain4j 迁移而来的独立 AI 微服务，提供智能助手「小药」对话与 RAG 知识库检索。

## 架构

```
前端 (Vue) → Nginx ─┬─ /ai/* /knowledge/* → Python FastAPI (8091, 本服务)
                     └─ /goods/* /storage/* ... → Java Spring Boot (8090, 业务)

Python 服务 → HTTP 调用 Java 业务 API 获取数据
            → Redis 缓存（与 Java 共享同一实例与 key 命名空间）
            → DeepSeek (对话) + 智谱 embedding-2 (RAG 向量化)
```

## 快速开始

### 1. 安装依赖

```bash
cd wms-ai
python -m venv .venv
.venv\Scripts\activate     # Windows
# source .venv/bin/activate  # Linux/Mac
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，填入：
- `DEEPSEEK_API_KEY`：DeepSeek API Key
- `ZHIPU_API_KEY`：智谱 API Key（用于 RAG embedding）
- `WMS_BACKEND_URL`：Spring Boot 后端地址（默认 http://localhost:8090）
- `SPRING_REDIS_HOST` / `SPRING_REDIS_PORT`：Redis 地址

### 3. 启动服务

```bash
python main.py
# 或
uvicorn main:app --host 0.0.0.0 --port 8091
```

服务运行在 `http://localhost:8091`，启动时自动加载 `knowledge-base/` 目录的 `.txt/.md` 文档到向量库。

### 4. 验证

```bash
# 健康检查
curl http://localhost:8091/health

# 知识库列表
curl http://localhost:8091/knowledge/list

# 知识库检索
curl "http://localhost:8091/knowledge/search?query=布洛芬用法用量"

# AI 对话（需 Java 后端 8090 已启动，工具才能查到业务数据）
curl -X POST http://localhost:8091/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"当前有哪些药品库存不足？","history":[]}'
```

## 功能模块

### AI 对话 `POST /ai/chat`
- 入参：`{message, history}`，history 为 `[{role, content}]` 多轮上下文
- DeepSeek + 9 个 Function Calling 工具，Redis 缓存对话 30min
- 返回：`{code:200, msg:"成功", total:0, data:{reply: "..."}}`

### 9 个 Function Calling 工具
| 工具 | 功能 | Redis 缓存 key | TTL |
|---|---|---|---|
| query_all_goods | 查所有药品库存 | wms:goods:list | 10min |
| query_goods_by_name | 按名模糊查药品 | wms:goods:name:{name} | 5min |
| query_low_stock_goods | 低库存预警 | wms:goods:lowstock:{n} | 5min |
| query_goods_by_type_name | 按分类查药品 | wms:goods:type:{name} | 5min |
| query_goods_by_storage_name | 按药房查药品 | wms:goods:storage:{name} | 5min |
| query_all_categories | 查所有分类 | wms:category:list | 30min |
| query_all_storages | 查所有药房 | wms:storage:list | 30min |
| query_stock_summary | 库存统计摘要 | wms:stock:summary | 5min |
| search_knowledge_base | 知识库检索 | 无 | - |

工具通过 HTTP 调用 Java 业务 API（`/goods/listPage`、`/storage/list`、`/goodstype/list`）获取数据，在内存过滤后返回。

### 知识库 `/knowledge/*`
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /knowledge/import/file?filePath=xxx | 导入文件 |
| POST | /knowledge/import/text | 导入文本（body: {title, content}） |
| GET | /knowledge/search?query=xxx | 语义检索 |
| GET | /knowledge/list | 列出已索引文档 |
| POST | /knowledge/reload | 重新加载目录 |
| DELETE | /knowledge/clear | 清空索引 |

RAG 参数：分块 300/30、检索 maxResults=3、minScore=0.6（跨框架 score 刻度可能需实测校准）。

## Docker 部署

```bash
docker build -t wms-ai .
docker run -d --name wms-ai --network host \
  -e DEEPSEEK_API_KEY=你的key \
  -e ZHIPU_API_KEY=你的key \
  -e WMS_BACKEND_URL=http://127.0.0.1:8090 \
  -e SPRING_REDIS_HOST=127.0.0.1 \
  wms-ai
```

## Nginx 配置

见 [nginx.conf](nginx.conf)：`/ai/` 和 `/knowledge/` 转发到 8091，业务 API 转发到 8090，静态资源由 Nginx 托管。

## 项目结构

```
wms-ai/
├── main.py                 # FastAPI 入口
├── requirements.txt
├── Dockerfile
├── nginx.conf              # Nginx 反向代理配置
├── .env.example
├── knowledge-base/         # RAG 知识库文档
├── app/
│   ├── config.py           # 环境变量配置（对齐 Java）
│   ├── schemas.py          # Result 信封（对齐 Java Result.java）
│   ├── prompts/system.py   # 「小药」系统提示（逐字复制）
│   ├── routers/            # FastAPI 路由
│   │   ├── ai_chat.py       # POST /ai/chat
│   │   └── knowledge.py     # /knowledge/* 全套
│   ├── services/           # 业务服务
│   │   ├── agent_service.py # ChatOpenAI + AgentExecutor
│   │   ├── wms_client.py    # httpx 调 Java 业务 API
│   │   ├── redis_service.py # Redis + java_string_hashcode
│   │   └── knowledge_base.py # RAG 向量库
│   └── tools/              # 9 个 Function Calling 工具
│       ├── goods_tools.py
│       └── knowledge_tool.py
└── README.md
```

## 与 Java 端的契约

1. **Redis 缓存 key**：与 Java `CacheConstant.java` 完全一致，Java 业务 Controller 在 save/update/del 时仍会 `delete` 这些 key 做失效
2. **接口返回格式**：`{code, msg, total, data}` 对齐 Java `Result.java`
3. **Java String.hashCode()**：聊天缓存 key `wms:ai:chat:{hash}` 复刻 Java 哈希算法
4. **系统提示词**：逐字复制 Java `WmsAiService.java` 的 `@SystemMessage`

## 注意事项

- Java 后端必须先启动（8090），否则工具调用会失败
- Redis 必须可用，缓存契约依赖共享实例
- 智谱 embedding 若 OpenAI 兼容端点失败，会回退到 `ZhipuAIEmbeddings`（需 `pip install zhipuai`）
- RAG 的 `min_score=0.6` 跨框架 score 刻度可能需实测校准
