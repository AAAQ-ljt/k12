# Nexora 项目规则

K12 人工智能通识课教学助手（赛题 JBGS-2026-02）。Nexora = Nexus（连接）+ Aurora（极光）：连接学生与 AI 知识的桥梁，极光象征多元、多彩的多模态交互体验。

前后端分离，后端拆成独立服务（管理端 + 用户端 + MCP 工具服务），各自带独立拦截器与登录态。

# 项目结构

后端 `nexora-java/`（Maven 多模块）：

- `nexora-common`：公共模块，包路径 `com.nexora`（easycode 生成层）。承载 PO / DTO / VO / Query、Service / Mapper、Redis 组件、异常、枚举、工具类；AI 公共组件（ChatClient 配置、意图路由、流式输出）后续也沉淀在此层。
- `nexora-admin`：管理端服务，包路径 `com.nexora.admin`。承载 Controller、Biz、管理端专用 DTO/VO。独立启动，运行在管理端端口 6061。
- `nexora-web`：学生端服务，包路径 `com.nexora`（Netty WS 子包 `com.nexora.websocket`）。承载 Controller、Biz、AI 对话组件（Netty WebSocket 流式）、MCP Client、学生端专用 DTO/VO。独立启动，运行在用户端端口 6060（WebSocket 6062）。*当前为骨架：登录拦截器 / 学生接口 / AI 链路按开发排期 P1/P3 落地。*
- `nexora-mcp`：MCP 教学工具服务，包路径 `com.nexora`。Spring AI MCP Server（Streamable HTTP），通过 @Tool 暴露教学域工具。独立启动，运行在 8084。

依赖方向：`admin → common`、`web → common`、`mcp → common`。common 不允许反向依赖任何端。

前端 `nexora-front/`：

- `nexora-front-admin`：管理后台前端工程（端口 3001）。
- `nexora-front-web`：学生端前端工程（端口 3000）。页面结构固定 5 页：AI 助教（首页 / 对话区）/ 个性化学习路径 / 课程教材 / 编程环境（小高及以上可见）/ 我的；多模态产物（SVG 动画、绘本、答题卡片）以对话内卡片 + 全屏体验承载。

> 包名提醒：common 用 `com.nexora`，admin 用 `com.nexora.admin`，web 用 `com.nexora`（websocket 在 `com.nexora.websocket`），mcp 用 `com.nexora`。新增类必须落到正确的包路径，不要混。

# 系统角色

- 管理员（内容运营）：课程体系 / 资源 / 知识库 / 题库（含 AI 出题审核）/ 提示词 / 生成内容质检 / 学生管理 / 系统配置。教师职能由管理员吸收，"教学"职责由 AI 智能体承担（7×24 在线教师）。
- 学生：按学段学习课程、AI 对话、游戏化练习、编程实践、学习分析。

角色字段约定在 `UserInfo.roleType`（ADMIN / STUDENT）；学生学段字段直接冗余在 `UserInfo.stage`（另含 `grade` / `interests` / 学习风格标签）。无教师、班级、考试概念。

# 学段与自适应（核心业务约束）

- 学段枚举：`PRIMARY_LOW`（小学低年级）/ `PRIMARY_HIGH`（小学高年级）/ `JUNIOR`（初中）/ `SENIOR`（高中）。
- 学生档案必须携带学段；AI 提示词、知识库检索（metadata 过滤）、交互风格、难度梯度全部按学段切换。
- 交互映射：小低 → 语音 / 绘本 / 游戏；小高 → 图文 + 简单动画 + 闯关；初中 → 项目式 + 编程入门；高中 → 算法讲解 + 编程实践 + 项目式学习。

# 接口与路径

- 全局上下文：`/api`，由 `server.servlet.context-path` 提供，路径中不再重复写 `admin` / `web`。
- 路径形式：`/api/<模块名>/<动作>`，模块名小驼峰；常用动作：`loadDataList`、`add`、`update`、`delete`、`detail`、`getXxxOptions`（下拉用）。
- AI 对话：`/api/agent/sendMessage`（HTTP 发送 + Netty WebSocket 回推）、`/api/agent/cancelMessage`、`/api/agent/loadHistoryMessage`、`/api/agent/loadSessions`。
- 管理接口归 admin 模块、用户接口归 web 模块；两端互不复用 Controller。

# 鉴权与权限

- 管理端 token 走 header `adminToken`，由管理端登录拦截器校验，并配合 Redis 登录组件做有效期管理。
- 用户端 token 走 header `studentToken`，由用户端登录拦截器校验。
- 管理端接口必须登录（登录拦截器校验 adminToken）；权限注解体系规划在 P7 系统收尾落地，落地后接口必须带权限注解，编码与系统菜单表一一对应。
- 当前登录用户统一从登录上下文持有器（ThreadLocal）取，禁止在 Controller / Service 里再次手动解析 token。
- AI 对话接口必须登录；MCP 工具服务不直接对外，仅由 web 端 MCP Client 调用。
- 用户端登录交互（参考 Codex 模式，已确认）：公开页面（AI 助手、编程环境）免登录可浏览；受保护页面 / 接口未登录时前端弹出登录弹窗、后端统一返回 401；左下角固定登录入口（未登录显示「登录」按钮，已登录显示头像/昵称 + 下拉：个人中心、退出登录）。

# 核心业务域

- 基础数据：学生（含学段 / 年级 / 兴趣 / 学习风格标签）、学段配置。
- 教学业务：AI 通识课程（教材）→ 章节 → 课时 → 课时资源；知识点（学段 / 难度）与课程课时关联。
- 题库与练习：题库、题目选项（AI 出题落库须审核）；游戏化练习按知识点与学段难度生成，即时批改与反馈。无试卷 / 考试链路。
- 对话智能体：会话 + 消息、意图路由、RAG 知识问答、MCP 教学工具调用、Netty WebSocket 流式输出、主动引导对话。
- 多模态内容：SVG 动画讲解 / 绘本 / AI 绘画 / PPT / Word / 代码，AI 生成记录统一入 `ai_generation_record`（区分来源：学生生成 / 管理员预置）。
- 个性化学习路径：知识点节点地图（主线 + 兴趣分支）、短期目标清单、AI 动态调整、遗忘曲线复习（掌握度驱动）。
- 知识库：`knowledge_doc` 管理 + ES 向量索引（学段 / 知识点 / 难度 metadata）；后台上传资料经"解析 → 文本化 → 向量化"管线入库。
- 资源：资源信息（视频 / 文档 / 图片 / 绘本），上传走分片 + Redis 队列异步处理。
- 系统：菜单、角色菜单、系统通知、站内消息。

# 业务规则

- 课程：由管理员运营维护，无教师归属；学生按学段获得课程可见范围。
- 章节：两级结构（章节 → 课时），课时下挂资源。
- 练习：客观题自动评分；AI 生成题目落题库后须审核上架；作答落 `practice_record` 并回写知识点掌握度。
- 学习记录：服务端记录学习时长、视频时间点；前端按课时恢复播放进度。
- 学习路径：学生可自建学习分类，AI 生成主线 + 兴趣分支路径；已掌握知识点按遗忘曲线间隔（1/3/7/15 天）自动排复习节点，快测未过回炉"进行中"。
- AI 回复链路：先意图路由 → 教学动作走 MCP 工具 → 知识问答走 RAG → 其余走通用对话；回复必须流式。
- AI 模型分工（已确认）：对话 / 意图路由 / SVG 动画 / 编程辅助 / 学习路径生成 = DeepSeek API（`NEXORA_DEEPSEEK_API_KEY`）；知识向量化 = 阿里百炼 text-embedding-v4（`NEXORA_DASHSCOPE_API_KEY`）；绘本插图 = 阿里百炼文生图（`NEXORA_DASHSCOPE_API_KEY`）。
- MCP 工具：只做教学域操作（查课程 / 查进度 / 查掌握度 / 出题 / 批改 / 记录进度 / 规划路径 / 推荐资源），入参校验，返回结构化字符串。

# 字段与返回约定

- 后端统一返回 `ResponseVO<T>`，不直接返回 Entity / PO。
- 分页参数继承统一基类，分页结果使用统一的分页结果 VO；前后端分页字段保持一致。
- 数据库字段下划线、Java 字段驼峰，由 MyBatis 配置自动映射；前端字段保持与后端驼峰一致，禁止前端起别名。
- 时间字段统一格式化为 `yyyy-MM-dd HH:mm:ss`（GMT+8），禁止返回时间戳。
- 主键：业务实体多为 String UUID（课程 / 章节 / 课时 / 资源 / 会话 / 消息），基础数据多为自增整数。

# 禁止行为

- 禁止前后端字段不一致或私自重命名 PO 字段。
- 禁止生成 mock 数据，联调以真实接口为准。
- 禁止管理端接口不登录；P7 权限注解体系落地后，禁止管理端接口不带权限注解。
- 禁止用户端接口暴露管理端字段（创建人、排序权重、审核状态等）。
- 禁止把仅一端使用的类放进 common 模块。
- 禁止 Controller / Biz / Service / Mapper 越层。
- 禁止在 Controller 直接调用 ChatClient / VectorStore / MCP Client。
- 禁止把 AI 对话做成同步阻塞接口；回复必须走 Netty WebSocket 流式 / 异步。

# 子规则索引

- 后端通用：`nexora-java/AGENTS.md`
- 前端通用：`nexora-front/AGENTS.md`
- AI 专项：skill `nexora-ai-assistant` / `nexora-mcp` / `nexora-rag` / `nexora-multimodal`
- 各子模块 / 前端工程根目录下可再放 `AGENTS.md` 作补充约束。
