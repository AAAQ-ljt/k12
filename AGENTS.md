# Workspace Rules

K12 人工智能通识课教学助手（赛题 JBGS-2026-02）工作空间，使用 Codex 构建。

主项目为 `nexora/`（Nexora = Nexus 连接 + Aurora 极光）；`参考项目/smart-campus/`（教学平台底座，迁移来源）与 `参考项目/easymall/`（Spring AI 智能体骨架 + MCP 工程结构，借鉴来源）为参考源码，**只读，禁止修改**；`smart-campus提示词记录/` 为历史构建提示词存档（Claude Code 规范，仅供理解设计意图，不要照抄其中的 .claude 配置）。

# 技术规范

后端统一：

- SpringBoot 3.x（Java 21）
- MySQL 8
- Redis
- Elasticsearch（RAG 向量检索）
- Spring AI（ChatClient / MCP / VectorStore / 流式对话）
- Netty WebSocket（AI 回复实时推送）

前端统一：

- React 18 + TypeScript
- Vite
- antd v5（双端统一组件库）+ Zustand + React Router
- Markdown 渲染（react-markdown）/ WebSocket 聊天 UI / 图表 ECharts / 视频 ArtPlayer+HLS / 代码编辑器 Monaco / 浏览器端 Python 运行 Pyodide

# 接口规范

- 统一接口风格
- 接口名称必须带模块前缀
- 示例：courseInfo/loadCourseInfoList、courseInfo/getCourseInfo、agent/sendMessage

# 输出要求

- 所有代码必须可直接运行
- 禁止伪代码
- 禁止省略 import/require
- 优先复用现有模块、工具、枚举
- 新增文件必须明确标注完整路径

# 目录规则

- nexora：主项目
- nexora-java 内：admin 管理端 / web 用户端 / common 公共模块 / mcp MCP 教学工具服务
- nexora-front 内：front-admin 管理后台前端 / front-web 学生端前端
- knowledge：K12 知识库源文件（RAG 入库资产）
- 参考项目：smart-campus / easymall 源码，只读

# 开发原则

- 保持与现有代码风格完全一致
- 保持命名规范统一
- 严格分离 PO / DTO / VO
- 统一返回 ResponseVO<T>
ResponseVO<T> 统一返回结构:
{
    "status":"success",
    "code": 200,
    "info": "成功",
    "data": T
}
- 系统角色仅"学生 + 管理员"两类：无教师 / 班级 / 考试概念（相对 smart-campus 的领域裁剪），AI 智能体承担"7×24 在线教师"职责
- 学段（小学低 / 小学高 / 初中 / 高中）冗余在 user_info.stage，AI 提示词 / 知识检索 / 难度 / UI 全部按学段切换
- AI 相关：大模型调用、向量检索、MCP 工具调用统一封装，禁止散落到 Controller

# 约束体系（Codex 规范）

- 项目规则：`nexora/AGENTS.md`；后端：`nexora/nexora-java/AGENTS.md`；前端：`nexora/nexora-front/AGENTS.md`
- 技能（skills）：`.codex/skills/` 下 9 个 SKILL.md：java-module / react-admin-page / react-web-page / nexora-java / nexora-front / nexora-ai-assistant / nexora-mcp / nexora-rag / nexora-multimodal
- Codex 自动发现 AGENTS.md 与 `.codex/skills/`，无需 settings.json 之类的注册文件

# 禁止行为

- 禁止修改无关代码
- 禁止随意升级依赖
- 禁止破坏现有接口
- 禁止覆盖我手动修改的代码
- 禁止修改项目中数据库脚本 DDL
- 禁止修改 `参考项目/` 下的任何文件
- 禁止返回 Map，统一定义对象，比如 vo,dto
- 禁止在循环里调用数据库
- 禁止把大模型 API Key / Secret 硬编码进代码（必须走环境变量或外部化配置）
- 禁止在 Controller 直接调用 ChatClient / VectorStore / MCP 客户端
- 禁止让大模型直接执行任意 SQL / 文件操作（教学工具必须白名单化）
- 禁止把 AI 对话做成同步阻塞接口（必须 Netty WebSocket 流式 / 异步）
