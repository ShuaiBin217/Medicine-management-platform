# DrugWMS 药品智能管理平台

基于 Spring Boot + Vue 前后端分离架构的药品仓储管理系统，集成 LangChain4j + DeepSeek 大模型实现智能问答与 RAG 知识库检索功能。

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
- **智能小药助手**：基于 DeepSeek 大模型 + LangChain4j 的 AI 问答
- **Function Calling 工具集**：AI 可自动调用库存查询、低库存预警、分类查询、药房查询、库存统计等工具
- **RAG 知识库**：基于智谱 Embedding 模型 + 向量检索的药品说明书 / 用药指南问答
- **Redis 缓存**：AI 对话与工具查询结果缓存，降低调用成本

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 7 | 缓存 |
| LangChain4j | 0.36.2 | AI 应用框架 |
| DeepSeek | deepseek-chat | 对话大模型 |
| 智谱 GLM | embedding-2 | 向量嵌入模型 |
| Java | 17 | 运行环境 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.11 | 前端框架 |
| Element UI | 2.15.9 | UI 组件库 |
| Vue Router | 3.5.4 | 路由 |
| Vuex | 3.0.0 | 状态管理 |
| Axios | 0.27.2 | HTTP 请求 |
| ECharts | 5.6.0 | 图表 |

## 项目结构

```
springboot_vue_wms/
├── wms/                        # 后端 Spring Boot 项目
│   ├── src/main/java/com/wms/
│   │   ├── WmsApplication.java        # 启动类
│   │   ├── ai/                        # AI 模块
│   │   │   ├── LangChain4jConfig.java # AI 配置
│   │   │   ├── WmsAiService.java      # AI 服务接口
│   │   │   ├── WmsTools.java          # Function Calling 工具
│   │   │   └── KnowledgeBaseService.java # RAG 知识库
│   │   ├── controller/                # 控制器
│   │   ├── entity/                    # 实体
│   │   ├── mapper/                    # 数据访问
│   │   ├── service/                   # 业务层
│   │   └── common/                    # 通用组件
│   ├── src/main/resources/
│   │   ├── application.yml            # 应用配置
│   │   └── sql/table.sql             # 建表脚本
│   ├── knowledge-base/                # RAG 知识库文档
│   ├── Dockerfile                     # 后端容器构建
│   └── pom.xml
├── wms-web/                   # 前端 Vue 项目
│   ├── src/
│   │   ├── components/                # 业务组件
│   │   │   ├── Login.vue
│   │   │   ├── Index.vue
│   │   │   ├── ChatNurse.vue         # AI 助手组件
│   │   │   ├── admin/                # 管理员管理
│   │   │   ├── user/                 # 用户管理
│   │   │   ├── storage/              # 仓库管理
│   │   │   ├── goodstype/            # 分类管理
│   │   │   ├── goods/                # 物品管理
│   │   │   └── record/               # 记录管理
│   │   ├── router/
│   │   └── main.js
│   └── vue.config.js
└── .github/workflows/         # CI/CD 配置
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
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

### 2. 启动后端

修改 [application.yml](wms/src/main/resources/application.yml) 中的数据库连接、Redis 地址及 AI API Key，或通过环境变量配置：

```bash
cd wms
mvn clean package -DskipTests
java -jar target/wms-0.0.1-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8090`

### 3. 启动前端

```bash
cd wms-web
npm install
npm run serve
```

前端开发模式默认运行在 `http://localhost:8080`

### 4. 生产构建

```bash
cd wms-web
VUE_APP_API_URL=http://你的服务器地址:8090 npm run build
```

构建产物在 `wms-web/dist` 目录，由 Nginx 托管。

## 环境变量配置

后端支持以下环境变量（优先级高于 application.yml）：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_DATASOURCE_URL | MySQL 连接地址 | jdbc:mysql://localhost:3306/wms02?... |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | 12345678 |
| SPRING_REDIS_HOST | Redis 地址 | localhost |
| SPRING_REDIS_PORT | Redis 端口 | 6379 |
| DEEPSEEK_API_KEY | DeepSeek API Key | - |
| ZHIPU_API_KEY | 智谱 API Key | - |

## API 接口

### 业务接口

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

### AI 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /ai/chat | POST | 智能问答 |
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

### 后端容器构建

```bash
cd wms
docker build -t wms-backend .
```

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

### 启动后端容器

```bash
docker run -d --name wms-backend --network host \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=12345678 \
  -e "SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/wms02?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8" \
  -e SPRING_REDIS_HOST=127.0.0.1 \
  -e DEEPSEEK_API_KEY=你的key \
  -e ZHIPU_API_KEY=你的key \
  wms-backend
```

### 前端 Nginx 配置

```nginx
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html/wms;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## AI 功能说明

### 智能小药助手

前端右下角悬浮的 AI 助手，支持自然语言对话。后端通过 LangChain4j 的 `AiServices` 构建，具备：

- **多轮对话记忆**：保留最近 20 条上下文
- **工具自动调用**：AI 根据用户意图自动选择合适的 Function Calling 工具
- **Redis 缓存**：相同问题直接返回缓存结果

### Function Calling 工具集

[WmsTools.java](wms/src/main/java/com/wms/ai/WmsTools.java) 中定义了以下工具：

| 工具 | 功能 |
|------|------|
| queryAllGoods | 查询所有药品库存 |
| queryGoodsByName | 按名称模糊查询药品 |
| queryLowStockGoods | 低库存预警查询 |
| queryGoodsByTypeName | 按分类查询药品 |
| queryGoodsByStorageName | 按药房查询药品 |
| queryAllCategories | 查询所有分类 |
| queryAllStorages | 查询所有药房 |
| queryStockSummary | 库存统计摘要 |
| searchKnowledgeBase | 知识库语义检索 |

### RAG 知识库

- 文档存放于 [wms/knowledge-base/](wms/knowledge-base/) 目录，支持 `.txt` / `.md` 格式
- 启动时自动加载并分块向量化
- 使用智谱 `embedding-2` 模型生成向量，`InMemoryEmbeddingStore` 存储
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

线上演示地址（阿里云 ECS）：

- 前端：`http://服务器IP`
- 后端 API：`http://服务器IP:8090`

## 目录说明

- [wms](wms)：后端 Spring Boot 项目
- [wms-web](wms-web)：前端 Vue 项目
- [.github/workflows](.github/workflows)：GitHub Actions CI/CD 配置

## License

本项目仅用于学习交流。