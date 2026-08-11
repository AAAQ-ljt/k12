---
name: nexora-mcp
description: Nexora MCP 教学工具服务规范：服务端工程结构（WebFlux + Streamable HTTP）、@Tool 工具定义与注册、工具清单设计（教学域）、客户端接入与安全。TRIGGER when 新增 / 修改 MCP 工具（"让 AI 能查课程 / 生成练习 / 批改 / 记录进度 / 查掌握度 / 规划路径"）。SKIP when 不涉及工具调用。
---

# 服务端工程（nexora-mcp）

- WebFlux 响应式应用：`spring-ai-starter-mcp-server-webflux`。
- 配置：`spring.ai.mcp.server.protocol=STREAMABLE`，`mcp-endpoint=/mcp`，端口 8084。
- 依赖：nexora-common（复用 Service / Mapper / 实体），排除 spring-boot-starter-web / actuator（避免与 WebFlux 冲突）。
- 启动类：`@SpringBootApplication(scanBasePackages = "com.nexora")` + `@MapperScan("com.nexora.common.mappers")`。

# 工具定义

- 每个工具 = 一个 Service 方法，加 `@Tool(name=..., description=...)`，参数加 `@ToolParam(description=...)`。
- 注册：`MethodToolCallbackProvider.builder().toolObjects(xxxService).build()`，在 `@Configuration` 中返回 `ToolCallbackProvider` Bean。
- 返回类型统一 `String`（成功文案 / 业务错误文案 / 系统异常文案），工具方法内部 try-catch，不抛未捕获异常。

# 工具清单（教学域白名单，按需裁剪）

- `queryCourse(stage, keyword)`：按学段 / 关键词查课程（教材）。
- `queryLesson(courseId, lessonId)`：查课时与资源。
- `queryKnowledgePoint(stage, keyword)`：查知识点。
- `recommendResource(stage, knowledgePointId, type)`：推荐学习材料（视频 / 文档 / PPT / 绘本 / 练习）。
- `generateQuiz(stage, knowledgePointId, count, difficulty)`：生成游戏化练习（调用大模型；落题库时须带"待审核"状态，或直接返回题目 JSON）。
- `autoGrade(questionId, answer)`：客观题自动批改。
- `saveProgress(userId, lessonId, progress)`：记录学习进度（复用 Redis 缓冲链路）。
- `queryMastery(userId, stage)`：查询学生知识点掌握度（供学习分析 / 路径调整）。
- `planNextStep(userId, stage)`：基于学习画像推荐下一步学习内容。
- `createStudyPath(userId, stage, goal)`：创建 / 调整学习路径（主线 + 兴趣分支）。

# 工具实现约束

- 工具方法只调用 common 的 Service / 业务组件，不直接操作 Mapper（避免跨模块扫描问题）。
- 入参必须判空 / 校验；用户 ID 由 web 端传入（MCP 无登录上下文，不能信任前端传的用户 ID，web 端组装时用登录上下文替换）。
- 生成类工具（generateQuiz / planNextStep / createStudyPath）的 LLM 调用要在 mcp 模块内封装或复用 common 的 AI 组件；耗时操作注意超时与幂等。
- 所有工具输出可被 LLM 直接理解：结构化、简短、含关键 ID 与下一步建议。

# 客户端接入（nexora-web）

- 依赖 `spring-ai-starter-mcp-client`（阻塞式实现，适配 Servlet MVC 的 nexora-web）。`webflux` 变体面向响应式应用，在 MVC 工程使用会出现组件冲突 / 初始化问题，不采用。
- 配置 `spring.ai.mcp.client.streamable-http.connections.<name>.url=http://localhost:8084`（配置化）。
- 对话组件注入 `ToolCallbackProvider`，在 ChatClient 请求中 `.toolCallbacks(toolCallbackProvider)`。
- 客户端启动失败要降级：对话仍可用（仅工具调用不可用），不阻塞主流程。

# 安全

- MCP Server 不直接暴露公网；仅内网 / 本机供 web 端调用。
- 工具不提供任意 SQL / 文件 / 系统操作能力。
- 涉及写操作的工具（saveProgress / createStudyPath / generateQuiz 落库）必须校验 userId 合法性。

# 自检清单

- 工具都是教学域操作，没有通用系统工具。
- 工具方法 try-catch，返回 String，不抛未捕获异常。
- 工具注册 Bean 存在，且客户端 URL 配置化。
- 生成类工具考虑超时 / 幂等。
- 客户端失败可降级，不阻塞对话。

# 与其他 skill 的分工

- 本 skill：工具定义与接入。
- `nexora-ai-assistant`：对话链路如何调用工具。
- `nexora-rag`：知识检索（通常作为对话链路内部能力，也可作为工具）。
