# Nexora 后端规则（nexora-java）

Spring Boot 3 + MyBatis（自定义泛型 Mapper，不是 MyBatis Plus）+ MySQL 8 + Redis + Elasticsearch + Spring AI。

# 模块与包路径

| 模块 | 包路径 | 职责 |
|---|---|---|
| nexora-common | `com.nexora.*`（easycode 生成层） | PO / DTO / VO / Query、Service / Mapper、Redis 组件、AI 公共组件、异常、枚举、工具类 |
| nexora-admin | `com.nexora.admin.*` | 管理端 Controller、Biz、管理端专用 DTO/VO；独立启动 |
| nexora-web | `com.nexora.*`（Netty WS 子包 `com.nexora.websocket.*`） | 用户端 Controller、Biz、AI 对话组件、MCP Client；独立启动 |
| nexora-mcp | `com.nexora.*` | MCP 教学工具服务（Streamable HTTP）；独立启动 |

依赖方向：`admin → common`、`web → common`、`mcp → common`。common 不允许反向依赖端模块。

启动类位于各端模块根包；Mapper 扫描指向 `com.nexora.mappers`。

# 分层规范

Controller → Biz → Service → Mapper → 数据库

- **Controller**（admin / web 模块）：参数接收、参数校验、调用 Biz、返回统一响应。不写业务逻辑。
- **Biz**（admin / web 模块）：业务编排层。承担权限校验、跨 Service 组合、组装返回 VO、调用 Redis 组件等编排职责。命名形如 `XxxAdminBiz` / `XxxWebBiz`。
- **Service / ServiceImpl**（common 模块）：单领域业务逻辑，事务边界落在这一层。Service 接口与实现类分两个包（`service` / `service.impl`）。
- **Mapper**（common 模块）：仅做单表 CRUD 或简单连表。复杂查询写在 XML，XML 放 common 模块的 `resources/mappers/` 下。
- **PO**：与表一对一映射，字段名 / 类型保持与表一致。

# 实体类分工

- **PO**：表映射对象，放 common 的 `entity/po`。Date 字段使用统一的 JSON 格式化注解（`yyyy-MM-dd HH:mm:ss`，时区 GMT+8）。
- **DTO**：接收前端入参，放 common 的 `entity/dto`；仅某一端使用的入参 DTO 放对应端模块的 `dto`（如 `nexora-admin/dto`）。需要校验时使用分组校验注解。
- **VO**：返回前端的数据结构，放 common 的 `entity/vo`；若仅服务于某一端的复杂返回结构，可放对应端模块下的 `entity/vo`。
- **Query**：分页查询参数，继承统一分页基类，放 common 的 `entity/query`。模糊匹配字段以 `Fuzzy` 后缀约定。

主键策略：业务实体多为 String UUID（课程 / 章节 / 课时 / 资源 / 会话 / 消息），基础数据多为自增整数（用户等）。

# Controller 规范

- 类注解：`@RestController`、`@RequestMapping("/<模块名>")`、`@Validated`。管理端再加权限注解（类级提供默认编码，方法级覆盖）。
- 继承公共基础 Controller，使用其提供的成功 / 业务错误返回方法，不要 new 响应对象。
- 路径中不要写 `/api/admin/` 或 `/api/web/`：`/api` 由 context-path 提供，`admin` / `web` 由服务隔离。
- 入参：列表查询用 Query 对象，写操作用 DTO 对象 + `@RequestBody`。
- 出参：始终是统一的响应包装类型。
- AI 对话接口：发送走 HTTP 并返回 messageId，AI 回复经 Netty WebSocket 实时推送（借鉴 easymall 的 Netty 服务 + MessageHandler），禁止同步等待模型回复。

# 鉴权与登录态

- 管理端：登录拦截器读 header `adminToken`，查 Redis 登录组件验证，写入登录上下文持有器（ThreadLocal）。
- 用户端：登录拦截器读 header `studentToken`，逻辑同上；**公开接口（登录 / 注册 / 公开浏览）放行，受保护接口未登录返回统一 401**，前端收到 401 弹出登录弹窗（Codex 模式，已确认）。
- 业务代码取当前登录用户只能从登录上下文持有器取，禁止再解析 token。
- 权限拒绝：抛业务异常（带权限错误码），由全局异常处理统一转响应。

# AI 调用规范（nexora-web）

- **模型接入**：Spring AI ChatClient（OpenAI 兼容协议）。对话模型 = DeepSeek API（`NEXORA_DEEPSEEK_API_KEY`，负责对话 / RAG 回答 / SVG 动画 / 编程辅助 / 学习路径生成）；api-key / base-url 走环境变量或外部配置，禁止硬编码。
- **Embedding**：阿里百炼 qwen3.7-text-embedding（`NEXORA_DASHSCOPE_API_KEY`，维度与 ES 索引一致，默认 1024）；向量库用 ES VectorStore。
- **文生图（绘本）**：阿里百炼 qwen-image-3.0（`NEXORA_DASHSCOPE_API_KEY`），低年级绘本插图。
- **意图路由**：统一 `UserIntent` 结构（intent / data / stage），先路由再执行；路由失败兜底 CHAT。
- **流式输出**：回复必须流式，经 Netty WebSocket 消息推送组件（MessageHandler / ChannelContextUtils，借鉴 easymall）下发增量；前端可取消（`cancelMessage`）。
- **WebSocket 推送**：学生端 Netty WebSocket 服务（独立端口 6062），登录后携带 token 连接，按 userId 路由消息；发送消息走 HTTP，回复经 WS 推送。
- **提示词管理**：提示词默认值放枚举，Redis 可覆盖（管理端可配置），按学段取模板。
- **RAG**：检索加 `dataType == 'xxx'` 与学段 metadata 过滤，topK + 相似度阈值；无结果时明确告知"知识库暂无相关内容"，不编造。
- **MCP 工具**：web 端 MCP Client 连接 `nexora-mcp`；工具结果结构化返回，失败返回友好错误文案。
- **多模态产出**：动画讲解 = LLM 生成分步 SVG 脚本 JSON（前端逐步播放）；绘本 = 分页 JSON + 逐页文生图；产物统一落 `ai_generation_record`，文件入资源模块。

# MCP 工具服务规范（nexora-mcp）

- 工程：WebFlux 响应式应用，`spring-ai-starter-mcp-server-webflux`，端点 `/mcp`（Streamable HTTP）。
- 工具定义：Service 方法加 `@Tool(name=..., description=...)` + `@ToolParam(description=...)`，通过 `MethodToolCallbackProvider` 注册。
- 工具范围（教学域白名单）：`queryCourse`、`queryLesson`、`queryKnowledgePoint`、`recommendResource`、`generateQuiz`、`autoGrade`、`saveProgress`、`queryMastery`、`planNextStep`、`createStudyPath`。
- 工具入参必须校验；所有工具返回 `String`（成功 / 业务错误 / 系统异常），不抛未捕获异常。
- 禁止暴露任意 SQL / 文件 / 系统类工具。

# 统一返回与异常

- 所有 Controller 返回统一响应包装：包含状态、错误码、提示信息、业务数据四个字段。
- 业务错误码集中维护在响应码枚举里。
- 业务异常一律抛自定义业务异常，由全局异常处理转响应。禁止在 Controller / Service 里 catch 后吞掉错误。
- AI 异步链路（模型调用、MCP 调用）的异常必须捕获并转成用户可见的错误消息 + 落库状态，不能静默丢失。

# 分页

- Query 对象继承统一分页基类，含页码、页大小、排序字段、内部分页对象。
- 查询流程：先 count，再按页大小构造分页对象、塞回 Query，再查列表，最后封装为统一分页结果 VO。

# 数据库与字段

- 库名 `nexora`，开启下划线到驼峰自动映射。
- 公共字段：创建时间、更新时间；多数表有状态字段（整数枚举）。
- 知识库 / 向量字段命名稳定（docId、dataType、stage、knowledgePointId、difficulty），前端与入库脚本依赖，禁止改名。
- DDL 与基础数据 SQL 放在后端工程根目录（`nexora.sql`），作为版本化初始化脚本。

# Redis 使用

- 登录态、学习进度、资源上传 session、资源处理任务队列、提示词覆盖均有现成 Redis 组件，直接复用，不要新造工具类。
- Service / Biz 通过依赖注入使用这些组件；禁止 Controller 直接操作 RedisTemplate。

# 开发规范

> 当前进度：以 `docs/开发流程.md` 为唯一执行依据；已实现账号体系、资源中心、官方 RAG、课程/习题/试卷、学习分析与系统设置，个人知识库与多模态能力持续推进。

- Controller 只做"收 → 派发 → 回"；业务逻辑在 Biz 或 Service。
- 跨 Service 的组合放 Biz；单表 / 单领域事务边界放 Service。
- Mapper 复杂查询写 XML；禁止用字符串拼接 SQL。
- 公共能力（异常、枚举、常量、工具、AI 配置）下沉到 common；端专用类放对应端模块。

# 禁止行为

- 禁止在 Controller 写 SQL 或操作 RedisTemplate / ChatClient / VectorStore。
- 禁止直接返回 Entity / PO 给前端（应通过响应包装类返回）。
- 禁止在循环里调 Mapper（N+1）。
- 禁止吞异常 / 自定义响应结构绕过统一返回。
- 禁止在 common 模块新增仅一端使用的类。
- 禁止管理端 Controller 不带权限注解。
- 禁止在 Controller / Service 自行解析 token。
- 禁止使用 MyBatis Plus 风格 API（Wrapper / IService 等）。
- 禁止 admin 复用 web 的 Controller，反之亦然。
- 禁止在代码中硬编码大模型 API Key / Secret。
- 禁止同步阻塞式 AI 回复接口。

# 子模块约束

各模块根目录下可再放 `AGENTS.md` 作更细化的补充约束，与本文件冲突时以更细化的约束为准。
