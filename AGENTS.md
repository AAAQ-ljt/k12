# Workspace Rules

K12 人工智能通识课教学助手（赛题 JBGS-2026-02）工作空间，使用 Codex 构建。

主项目为 `nexora/`（Nexora = Nexus 连接 + Aurora 极光）；`参考项目/easymall/`（手敲严谨版：Spring AI 智能体骨架 + Netty WebSocket + MCP 工程结构，借鉴来源）为参考源码，**只读，禁止修改**（smart-campus 已弃用、不再保留）。

> 执行依据：开发排期见 `nexora/docs/开发排期.md`（P0/P1 已定稿，P2–P7 路线图阶段前再细化）；代码现状与本文档不一致时，以校准后的描述为准。

# 技术规范

后端统一：

- SpringBoot 3.x（Java 21）
- MySQL 8
- Redis
- Elasticsearch（RAG 向量检索）
- Spring AI（ChatClient / MCP / VectorStore / 流式对话）
- Netty WebSocket（AI 回复实时推送）

前端统一：

- React 19 + TypeScript
- Vite
- antd v6（双端统一组件库）+ Zustand + React Router
- Markdown 渲染（react-markdown）/ WebSocket 聊天 UI / 图表 ECharts / 视频 ArtPlayer+HLS / 代码编辑器 Monaco / 浏览器端 Python 运行 Pyodide

# 接口规范

- 统一接口风格：路径 = `/api/<模块名>/<动作>`，模块名小驼峰
- 动作命名统一：分页列表 `loadDataList`、详情 `getInfo`、新增 `add`、修改 `update`、删除 `del`、状态变更 `changeStatus`、下拉选项 `getXxxOptions`
- 示例：`/api/courseInfo/loadDataList`、`/api/userInfo/getInfo`、`/api/studentInfo/login`、`/api/agent/sendMessage`

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
- 禁止随意升级依赖（新增依赖须先说明用途并经确认）
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
- 禁止针对单个问题先启动浏览器验证；先在代码中直接定位并修复，浏览器验证仅用于整页 / 跨页 / 交互链路或用户明确要求时

# 编译 / 启动 / 验证协作流程（2026-08-22 起生效，替代旧的“禁止自行编译”条款）

- Codex 负责：后端 `mvn` 与前端 `npm` 的 compile / install / build、服务启动与停止、端口验证。用户负责最终验收启动。
- 每次编译 / 启动前：确认相关端口未被占用；若被占用且是旧服务进程，先停掉再继续（用户也会提前停）。
- 启动服务验证功能后：主动关闭自己启动的服务与端口，把环境交还给用户（用户再自行启动验收）。
- 编译 / 启动期间遇到问题可以反复修复重试；不修改 maven / npm 公共配置（settings.xml、registry 等），如需变更先说明。
