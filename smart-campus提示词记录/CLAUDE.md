# Workspace Rules

K12 人工智能通识课教学助手（赛题 JBGS-2026-02）工作空间。
主项目为 `nexora/`（Nexora = Nexus 连接 + Aurora 极光）；`smart-campus/` 与 `easymall/` 为参考源码（迁移 / 借鉴用，只读）。

# 技术规范

后端统一：

- SpringBoot 3.x（Java 21）
- MySQL 8
- Redis
- Elasticsearch（RAG 向量检索）
- Spring AI（ChatClient / MCP / VectorStore / 流式对话）
- Netty WebSocket（AI 回复实时推送）

前端统一：

- Vue3 + JavaScript
- Vite
- Element Plus
- Markdown 渲染 / WebSocket 聊天 UI / 图表 ECharts / 视频 ArtPlayer+HLS

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
- 学段（小学低 / 小学高 / 初中 / 高中）冗余在 user_info.stage，AI 提示词 / 知识检索 / 难度 / UI 全部按学段切换
- AI 相关：大模型调用、向量检索、MCP 工具调用统一封装，禁止散落到 Controller

# 禁止行为

- 禁止修改无关代码
- 禁止随意升级依赖
- 禁止破坏现有接口
- 禁止覆盖我手动修改的代码
- 禁止修改项目中数据库脚本 DDL
- 禁止返回 Map，统一定义对象，比如 vo,dto
- 禁止在循环里调用数据库
- 禁止把大模型 API Key / Secret 硬编码进代码（必须走环境变量或外部化配置）
- 禁止在 Controller 直接调用 ChatClient / VectorStore / MCP 客户端
- 禁止让大模型直接执行任意 SQL / 文件操作（教学工具必须白名单化）
- 禁止把 AI 对话做成同步阻塞接口（必须 Netty WebSocket 流式 / 异步）
