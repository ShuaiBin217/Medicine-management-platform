# DrugWMS 药品智能管理平台

基于 Spring Boot + Vue 3 前后端分离架构的药品仓储管理系统，集成 LangChain + DeepSeek 大模型实现智能问答与 RAG 知识库检索功能。

## 项目简介

DrugWMS 是一套面向药房、医药仓库的智能化管理平台，覆盖药品出入库、库存预警、分类管理、操作记录等核心业务，并内置 AI 智能助手「小药」，支持自然语言查询库存、药品说明书及用药指南。

## 功能特性

### 基础业务模块
- **用户登录与权限控制**：基于角色的动态菜单（超级管理员 / 管理员 / 普通用户）
- **管理员管理**：账号的增删改查
- **用户管理**：业务用户维护
- **仓库管理**：药房 / 仓库信息维护
- **物品分类管理**：药品分类维护
- **物品管理**：药品库存维护，支持按名称、分类、仓库筛选
- **记录管理**：入库 / 出库流水记录，自动更新库存数量

### AI 智能模块
- **智能小药助手**：基于 DeepSeek 大模型 + LangChain 的 AI 问答，SSE 流式输出
- **Function Calling 工具集**：AI 可自动调用库存查询、低库存预警、分类查询、药房查询、库存统计等工具
- **RAG 知识库**：基于智谱 Embedding 模型 + 向量检索的药品说明书 / 用药指南问答
- **Redis 缓存**：AI 对话与工具查询结果缓存，降低调用成本

## 技术栈

### 后端（Java）
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 7 | 缓存 |
| Java | 17 | 运行环境 |

### AI 服务（Python）
| 技术 | 版本 | 说明 |
|------|------|------|
| FastAPI | 0.115+ | Web 框架 |
| LangChain | 0.3.x | AI 应用框架 |
| LangChain-OpenAI | 0.3.x | DeepSeek 模型接入 |
| DeepSeek | deepseek-chat | 对话大模型 |
| 智谱 GLM | embedding-2 | 向量嵌入模型 |
| Redis | 7 | 对话缓存 |
| Python | 3.10+ | 运行环境 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 前端框架 |
| Element Plus | 2.14.x | UI 组件库 |
| Vue Router | 4.x | 路由 |
| Pinia | 3.x | 状态管理 |
| Vite | 7.x | 构建工具 |
| Axios | 0.27.2 | HTTP 请求 |
| ECharts | 5.6.0 | 图表 |

## 项目结构

```
springboot_vue_wms/
├── wms/                        # 后端 Spring Boot 项目
│   ├── src/main/java/com/wms/
│   │   ├── WmsApplication.java        # 启动类
│   │   ├── controller/                # 控制器
│   │   ├── entity/                    # 实体
│   │   ├── mapper/                    # 数据访问
│   │   ├── service/                   # 业务层
│   │   └── common/                    # 通用组件
│   ├── src/main/resources/
│   │   ├── application.yml            # 应用配置
│   │   └── sql/table.sql             # 建表脚本
│   └── pom.xml
├── wms-ai/                     # AI 服务 Python 项目
│   ├── main.py                        # 启动入口
│   ├── app/
│   │   ├── config.py                  # 配置
│   │   ├── routers/                   # 路由
│   │   │   ├── ai_chat.py             # AI 对话（SSE 流式）
│   │   │   └── knowledge.py           # 知识库管理
│   │   ├── services/                  # 业务服务
│   │   │   ├── agent_service.py       # Agent 编排
│   │   │   ├── knowledge_base.py      # RAG 知识库
│   │   │   ├── redis_service.py      # 缓存
│   │   │   └── wms_client.py          # Java 后端 HTTP 调用
│   │   ├── tools/                     # Function Calling 工具
│   │   └── prompts/                   # 系统提示词
│   ├── knowledge-base/                # RAG 知识库文档
│   ├── requirements.txt
│   └── nginx.conf                     # AI 服务 Nginx 配置参考
├── wms-web/                   # 前端 Vue 3 项目
│   ├── src/
│   │   ├── components/                # 业务组件
│   │   │   ├── Login.vue
│   │   ├── Index.vue
│   │   │   ├── ChatNurse.vue         # AI 助手组件
│   │   │   ├── FIcon.vue             # 图标兼容组件
│   │   │   ├── admin/                # 管理员管理
│   │   │   ├── user/                 # 用户管理
│   │   │   ├── storage/              # 仓库管理
│   │   │   ├── goodstype/            # 分类管理
│   │   │   ├── goods/                # 物品管理
│   │   │   └── record/               # 记录管理
│   │   ├── stores/                    # Pinia 状态管理
│   │   ├── router/                    # Vue Router 4
│   │   ├── utils/                     # 工具（图标映射等）
│   │   └── main.js
│   ├── vite.config.js                 # Vite 构建配置
│   ├── eslint.config.mjs
│   └── index.html
└── .github/workflows/         # CI/CD 配置
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+（推荐 22，Vite 7 要求）
- Python 3.10+
- MySQL 8.0+
- Redis 7+

### 1. 初始化数据库

创建数据库 `wms02`，字符集 `utf8mb4`，并执行建表脚本：

```sql
CREATE DATABASE wms02 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wms02;
-- 执行 wms/src/main/resources/sql/table.sql
```

默认账号：`sa` / 密码：`123`

### 2. 启动 Java 后端

修改 [application.yml](wms/src/main/resources/application.yml) 中的数据库连接和 Redis 地址，或通过环境变量配置：

```bash
cd wms
mvn clean package -DskipTests
java -jar target/wms-0.0.1-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8090`

### 3. 启动 AI 服务

```bash
cd wms-ai
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

AI 服务默认运行在 `http://localhost:8091`

### 4. 启动前端

```bash
cd wms-web
npm install
npm run dev
```

前端开发模式默认运行在 `http://localhost:8080`

### 5. 生产构建

```bash
cd wms-web
# 设置环境变量
export VITE_API_URL=http://你的服务器地址:8090
export VITE_AI_URL=""    # 空串 = 走 Nginx 同源 /ai/ 代理

npm run build
```

构建产物在 `wms-web/dist` 目录，由 Nginx 托管。

## 环境变量配置

### 前端环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| VITE_API_URL | Java 后端地址 | http://118.31.104.148:8090 |
| VITE_AI_URL | AI 服务地址（空串走 Nginx 代理） | "" 或 http://localhost:8091 |

### Java 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_DATASOURCE_URL | MySQL 连接地址 | jdbc:mysql://localhost:3306/wms02?... |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | 12345678 |
| SPRING_REDIS_HOST | Redis 地址 | localhost |
| SPRING_REDIS_PORT | Redis 端口 | 6379 |

### AI 服务环境变量

配置文件位于 `wms-ai/.env` 或 `wms-ai/app/config.py`：

| 变量名 | 说明 |
|--------|------|
| DEEPSEEK_API_KEY | DeepSeek API Key |
| ZHIPU_API_KEY | 智谱 API Key |
| WMS_BACKEND_URL | Java 后端地址（默认 http://localhost:8090） |
| REDIS_URL | Redis 连接（默认 redis://localhost:6379/0） |

## API 接口

### 业务接口（Java 后端 :8090）

| 模块 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 用户 | /user/login | POST | 登录 |
| 用户 | /user/listPageC1 | POST | 用户分页查询 |
| 菜单 | /menu/list | GET | 按角色获取菜单 |
| 仓库 | /storage/listPage | POST | 仓库分页 |
| 分类 | /goodstype/listPage | POST | 分类分页 |
| 物品 | /goods/listPage | POST | 物品分页 |
| 记录 | /record/listPage | POST | 记录分页 |
| 记录 | /record/save | POST | 入库 / 出库 |

### AI 接口（Python AI 服务 :8091）

| 接口 | 方法 | 说明 |
|------|------|------|
| /ai/chat/stream | POST | 智能问答（SSE 流式输出） |
| /health | GET | 健康检查 |
| /knowledge/import/file | POST | 导入知识库文件 |
| /knowledge/import/text | POST | 导入知识库文本 |
| /knowledge/search | GET | 知识库检索 |
| /knowledge/list | GET | 知识库列表 |
| /knowledge/reload | POST | 重新加载知识库 |

统一返回结构：

```json
{
  "code": 200,
  "msg": "成功",
  "total": 0,
  "data": {}
}
```

## Docker 部署

### 启动依赖服务

```bash
docker run -d --name mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=12345678 \
  -e MYSQL_DATABASE=wms02 \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --lower-case-table-names=1

docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### 启动 Java 后端容器

```bash
cd wms
docker build -t wms-backend .
docker run -d --name wms-backend --network host \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=12345678 \
  -e "SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/wms02?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8" \
  -e SPRING_REDIS_HOST=127.0.0.1 \
  wms-backend
```

### 启动 AI 服务容器

```bash
cd wms-ai
docker build -t wms-ai .
docker run -d --name wms-ai --network host \
  -e DEEPSEEK_API_KEY=你的key \
  -e ZHIPU_API_KEY=你的key \
  -e WMS_BACKEND_URL=http://127.0.0.1:8090 \
  -e REDIS_URL=redis://127.0.0.1:6379/0 \
  wms-ai
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html/wms;
    index index.html;

    # 前端 SPA
    location / {
        try_files $uri $uri/ /index.html;
    }

    # AI 服务代理（SSE 需关闭缓冲）
    location /ai/ {
        proxy_pass http://127.0.0.1:8091;
        proxy_set_header Host $host;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
    }

    # 知识库接口代理
    location /knowledge/ {
        proxy_pass http://127.0.0.1:8091;
        proxy_set_header Host $host;
    }

    # AI 健康检查
    location /health {
        proxy_pass http://127.0.0.1:8091;
    }
}
```

## AI 功能说明

### 智能小药助手

前端右下角悬浮的 AI 助手，支持自然语言对话，通过 SSE（Server-Sent Events）流式输出，逐字显示回复。后端基于 LangChain 的 Agent 架构，具备：

- **多轮对话记忆**：保留上下文
- **工具自动调用**：AI 根据用户意图自动选择合适的 Function Calling 工具
- **Redis 缓存**：相同问题直接返回缓存结果

### Function Calling 工具集

[wms-ai/app/tools/goods_tools.py](wms-ai/app/tools/goods_tools.py) 中定义了以下工具：

| 工具 | 功能 |
|------|------|
| query_all_goods | 查询所有药品库存 |
| query_goods_by_name | 按名称模糊查询药品 |
| query_low_stock_goods | 低库存预警查询 |
| query_goods_by_type_name | 按分类查询药品 |
| query_goods_by_storage_name | 按药房查询药品 |
| query_all_categories | 查询所有分类 |
| query_all_storages | 查询所有药房 |
| query_stock_summary | 库存统计摘要 |
| search_knowledge_base | 知识库语义检索 |

### RAG 知识库

- 文档存放于 [wms-ai/knowledge-base/](wms-ai/knowledge-base/) 目录，支持 `.txt` / `.md` 格式
- 启动时自动加载并分块向量化
- 使用智谱 `embedding-2` 模型生成向量
- 检索时按相关度排序，最低分阈值 0.6

## 数据库表结构

| 表名 | 说明 |
|------|------|
| user | 用户表（账号、密码、角色） |
| menu | 菜单表（动态权限菜单） |
| storage | 仓库 / 药房表 |
| goodsType | 物品分类表 |
| goods | 物品表（库存数量） |
| record | 出入库记录表 |

详细建表语句见 [table.sql](wms/src/main/resources/sql/table.sql)。

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| sa | 123 | 超级管理员 |

## 部署示例

线上演示地址（阿里云 ECS）：http://118.31.104.148/

## 目录说明

- [wms](wms)：后端 Spring Boot 项目
- [wms-ai](wms-ai)：AI 服务 Python FastAPI 项目
- [wms-web](wms-web)：前端 Vue 3 项目
- [.github/workflows](.github/workflows)：GitHub Actions CI/CD 配置

## License

本项目仅用于学习交流。
