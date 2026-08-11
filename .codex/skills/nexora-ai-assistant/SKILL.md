---
name: nexora-ai-assistant
description: Nexora AI 对话智能体实现规范（Spring AI）：ChatClient 配置、意图路由、Netty WebSocket 流式输出、提示词管理、主动引导、材料推荐卡片、会话与消息持久化、取消与错误处理。TRIGGER when 开发 / 改造 AI 对话链路（"AI 助教回复" / "意图路由" / "流式输出" / "取消回复" / "提示词配置" / "会话历史" / "主动引导"）。SKIP when 只是写普通 CRUD 或静态页面。
---

# 总体链路

AgentController.sendMessage（HTTP 返回 messageId）→ ChatComponent.sendMessage（落库 + 异步执行）→ analyzeUserIntent（意图路由）→ 分支执行（RAG / MCP 工具 / 通用对话）→ Netty WebSocket 增量回推 → 完成 / 取消 / 错误落库。

# 技术要点

- 模型接入：Spring AI ChatClient（OpenAI 兼容对接阿里百炼 qwen 系列）。api-key / base-url / model 从配置读取，禁止硬编码。
- 流式：使用 `chatClient.prompt().... .stream().chatResponse()` 订阅增量，经 Netty WebSocket 消息推送组件下发；禁止同步 `call()` 阻塞接口。
- 意图路由：先用一次轻量调用把用户消息映射为 `UserIntent(intent, data, stage)`；意图类型见下方清单；解析失败兜底 CHAT。
- 历史消息：组装最近 N 条（用户 + AI）作为上下文；会话按 `agent_session` 隔离，切换学段 / 知识点可新建会话。
- 取消：Redis 记录取消标记，异步任务在关键节点检查；取消后更新消息状态。
- 提示词：默认值放枚举（按学段 / 场景），`prompt_template` 表 + Redis 覆盖（管理端可编辑）。
- 结构化输出：意图解析用 `call().entity(UserIntent.class)`；工具调用结果按约定 JSON 返回。

# 意图类型（教学域）

- EXPLAIN：讲解概念 / 知识点。
- RECOMMEND：推荐学习材料（视频 / 文档 / PPT / 绘本），结果以卡片数据返回（bizType=RESOURCE_LIST）。
- QUIZ：生成游戏化练习 / 测验（答题卡片数据）。
- PICTURE_BOOK：生成互动绘本（低龄入口显著）。
- DRAW：AI 绘画创作（生成插图 / 海报 / 概念图，可复用为绘本配图）。
- ANIMATION：生成 SVG 动画讲解（输入框"动画讲解"模式直接路由到此意图）。
- CODING：编程辅导 / 代码调试（可引导进入编程环境并预置代码框架）。
- PLAN：规划 / 调整学习路径（自建分类、阶段目标）。
- PROGRESS：查询学习进度 / 学习分析。
- CHAT：通用对话（问候、闲聊、兜底）。

# 主动引导与推荐（赛题要求）

- 主动引导：学生进入页面 / 课时 / 路径节点时，AI 基于 `learning_path` 当前节点主动发起开场对话（引导进入当前章节内容），不被动等待提问。
- 材料推荐：每轮回答结束后，AI 询问"是否需要推荐相关学习材料"；学生确认后走 RECOMMEND 分支，经 `recommendResource` 工具取资源，以卡片数据下发，前端点击跳转课程教材页对应资源。

# 提示词模板（按学段切换）

- 每个意图模板都要带学段说明（学生当前学段、期望的交互风格、难度上限）。
- 示例约束：小低 → 短句、比喻、表情 / 图；小高 → 图文结合、简单例子；初中 → 概念 + 代码入门；高中 → 算法原理 + 项目实践。
- 提示词必须包含"知识库无相关内容时如实说明，不要编造"。

# 消息与状态

- `agent_message` 字段：messageId、sessionId、userId、userMessage、assistantMessage、intent、bizType、bizData、status（处理中 / 完成 / 取消 / 错误）、createTime。
- 用户消息立即落库；AI 回复流式完成后落库；取消 / 错误必须更新状态。
- 多模态产物消息：bizType（ANIMATION / PICTURE_BOOK / QUIZ / RESOURCE_LIST / CODE）+ bizData（结构化 JSON），前端按 bizType 渲染卡片。

# 自检清单

- 对话回复是 WebSocket 流式，不是同步阻塞。
- API Key 未硬编码。
- 意图路由已持久化；取消标记已检查。
- 提示词按学段切换；无 RAG 结果时如实告知。
- 工具调用走 MCP Client，不在对话组件里写 SQL。
- 主动引导与材料推荐链路可用。
- 错误路径已落库并回推用户可见消息。

# 与其他 skill 的分工

- 本 skill：对话链路本身（ChatClient / 意图 / 流式 / 提示词 / 消息）。
- `nexora-rag`：知识检索细节。
- `nexora-mcp`：工具定义与客户端接入。
- `nexora-java`：业务表与落库规范。
