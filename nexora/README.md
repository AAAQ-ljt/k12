# Nexora

> Nexora = Nexus（连接）+ Aurora（极光）——连接学生与 AI 知识的桥梁，极光象征多元、多彩的多模态交互体验。

K12 人工智能通识课教学助手对话智能体（赛题 JBGS-2026-02）。面向小学至高中全学段学生，以 AI 智能体承担"7×24 在线教师"职责，提供自适应课程学习、AI 对话辅导、游戏化练习、编程实践与个性化学习路径。

## 技术栈

**后端**

- Spring Boot 3.x（Java 21）
- MySQL 8
- Redis
- Elasticsearch（RAG 向量检索）
- Spring AI（ChatClient / MCP / VectorStore / 流式对话）
- Netty WebSocket（AI 回复实时推送）

**前端**

- React 19 + TypeScript
- Vite
- antd v6（双端统一组件库）
- Zustand + React Router
- Markdown 渲染（react-markdown）/ WebSocket 聊天 UI / 图表 ECharts / 视频 ArtPlayer+HLS / 代码编辑器 Monaco / 浏览器端 Python 运行 Pyodide

## 项目结构

```
nexora/
├── nexora-java/              # 后端（Maven 多模块）
│   ├── nexora-common/        # 公共模块（PO / DTO / VO / Service / Mapper / AI 组件）
│   ├── nexora-admin/         # 管理端服务（端口 6061）
│   ├── nexora-web/           # 学生端服务（端口 6060 + WS 6062）
│   └── nexora-mcp/           # MCP 教学工具服务（端口 8084）
├── nexora-front/             # 前端
│   ├── nexora-front-admin/   # 管理后台前端（端口 3001）
│   └── nexora-front-web/     # 学生端前端（端口 3000）
├── knowledge/                # K12 知识库源文件（RAG 入库资产）
└── docs/                     # 项目文档
```

## 启动顺序

### 1. 启动基础设施

- **MySQL**：建库 `nexora`，执行 `nexora-java/nexora.sql`
- **Redis**：默认 `127.0.0.1:6379`
- **Elasticsearch**：默认 `http://localhost:9200`

### 2. 后端构建

```bash
cd nexora-java
mvn clean install -DskipTests
```

### 3. 依次启动后端服务

按依赖顺序启动：

1. **nexora-mcp**（MCP 教学工具服务，端口 8084）
2. **nexora-admin**（管理端服务，端口 6061）
3. **nexora-web**（学生端服务，端口 6060 + WebSocket 6062）

### 4. 启动前端

```bash
# 管理后台前端
cd nexora-front/nexora-front-admin
npm install && npm run dev    # http://localhost:3001

# 学生端前端
cd nexora-front/nexora-front-web
npm install && npm run dev    # http://localhost:3000
```

## 环境变量

| 变量名 | 说明 |
|--------|------|
| `NEXORA_DEEPSEEK_API_KEY` | DeepSeek API Key（日常对话） |
| `NEXORA_EMBEDDING_API_KEY` | 阿里百炼 API Key（知识向量化，qwen3.7-text-embedding） |
| `NEXORA_IMAGE_API_KEY` | 阿里百炼 API Key（绘本插图，qwen-image-3.0） |
| `NEXORA_MAIL_USERNAME` | 邮箱账号 |
| `NEXORA_MAIL_PASSWORD` | 邮箱授权码 |

完整启动步骤与环境变量配置见 `docs/本地启动指南.md`。

## 演示账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 学生（小学低） | student_low@nexora.com | 123456 |
| 学生（小学高） | student_high@nexora.com | 123456 |
| 学生（初中） | student_junior@nexora.com | 123456 |
| 学生（高中） | student_senior@nexora.com | 123456 |
