---
name: nexora-java
description: Nexora 后端业务专项规则（学段与知识点 / 课程 / 习题 / 试卷 / 考试 / 学习记录 / 游戏化练习 / 个性化路径 / 资源 / 知识库 / 权限菜单 / AI 集成）。TRIGGER when 涉及具体业务（"在 admin 加知识点管理接口" / "学生端拉取我的课程列表" / "学习进度记录" / "试卷自动评分" / "AI 生成练习后落库"），需要清楚表关系、学段规则、权限编码、登录态、Redis 组件用法。SKIP when 任务是纯脚手架式新增 CRUD（用 `java-module`）或与业务领域无关的纯通用问题。
---

# 适用范围

本 skill 假设你已经知道项目分层规范（详见 `nexora-java/AGENTS.md`），重点提供业务领域知识与归属判定规则。

# 业务域 → 表的对应

- 用户：`user_info`（roleType：ADMIN / STUDENT；学生冗余字段 stage / grade / interests / 学习风格标签）。无教师、班级概念。
- 学段与知识点：`knowledge_point`（知识点，含学段 / 难度 / 学科，可关联课时 / 题目 / 知识文档）。
- 课程结构：课程主表（教材，含学段，管理员运营）→ 章节 → 课时 → 课时资源（4 级关联）。
- 题库与练习：题目主表 + 题目选项（AI 出题落库须审核）；`practice_record`（学生、知识点、题目、作答、得分、用时、是否掌握）。无试卷 / 考试链路。
- 学习数据：课程级进度、课时级进度（含视频播放位置）、学习日志（按时段写入）。
- 个性化路径：`learning_path`（学习分类 / 目标、来源 AI / 规则、状态、进度）+ `learning_path_item`（知识点节点：MAIN / BRANCH 分支、LEARN / REVIEW 类型、到期时间、状态）；`knowledge_mastery`（学生 × 知识点的掌握度、最近 / 下次复习时间、复习阶段）。
- 对话智能体：`agent_session` + `agent_message`（会话、消息、意图、工具调用、状态）。
- 多模态内容：`ai_generation_record`（SVG 动画 / 绘本 / 绘画 / PPT / Word / 代码；区分来源：学生生成 / 管理员预置）。
- 知识库：`knowledge_doc`（内容、学段、知识点、难度、向量状态、来源资源）。
- 资源：统一的资源信息表（视频 / 文档 / 图片 / 绘本），关联到课时。
- 提示词：`prompt_template`（按学段 × 意图 / 场景，Redis 缓存覆盖默认值）。
- 消息 / 通知：系统通知 + 站内消息（消息主表 + 用户消息关联表）。
- 权限：菜单表 + 角色菜单关联表。

实现接口前，先看 `nexora.sql` 确认表结构，避免凭印象写字段。

# 角色边界（业务必须显式判定）

- 管理员（内容运营）：课程体系 + 资源 + 知识库 + 题库（含 AI 出题审核）+ 提示词 + AI 生成内容质检 + 学生管理 + 权限菜单。
- 学生：按学段访问课程；可学习课时、AI 对话、游戏化练习、编程实践、记录学习进度；只能读写本人数据（进度 / 练习 / 会话 / 路径 / 生成内容）。

不能仅依赖前端隐藏按钮——后端 Biz 层必须显式校验"学生 ID = 当前登录用户"这类归属关系。

# 学段规则（强制）

- 学段枚举：`PRIMARY_LOW` / `PRIMARY_HIGH` / `JUNIOR` / `SENIOR`。
- 学生操作（课程列表、知识点检索、AI 对话上下文、练习难度）必须带学段。
- 知识库检索与推荐必须按学段过滤（metadata.stage）。
- AI 提示词按学段取模板；接口返回内容按学段裁剪（如小低不返回复杂代码）。

# 鉴权与登录态

- 管理端：登录拦截器 + 管理端登录 Redis 组件，结合权限注解做细粒度控制。
- 用户端：登录拦截器仅校验登录态，无细粒度权限。
- 当前登录用户从登录上下文持有器取，禁止自行解析 token。
- 无权限：抛业务异常（带权限错误码）。

# Redis 组件直接复用

公共 Redis 组件已提供以下场景能力，不要重复造工具类：

- 管理端 / 用户端登录态缓存。
- 学习进度的写入与聚合缓冲。
- 大文件分片上传 session。
- 资源处理任务队列（视频转码、缩略图等异步任务）。
- 提示词覆盖（管理端配置写 Redis，AI 读取时优先）。

# 业务约束（实施细节）

## 课程

- 课程（教材）由管理员运营维护，按学段组织；学生端按登录学生学段过滤可见课程。
- 删除课程要级联清理章节、课时、课时资源，以及该课程的学习进度数据。

## 知识点

- 知识点必须带学段与难度；同一知识点可跨学段复用（如"排序算法"）。
- 知识点可关联课时 / 题目 / 知识文档，作为 RAG 检索与推荐的主键。

## 题库 / 练习

- 客观题（单选 / 多选 / 判断 / 填空）自动评分，评分逻辑放 Biz 层。
- AI 生成题目落题库时必须带"待审核"状态，管理员审核上架后才进入推荐与练习池。
- 游戏化练习：作答记录落 `practice_record`，批改后回写 `knowledge_mastery`（掌握度 / 是否掌握）。

## 学习记录

- 学习时长：按时段写入学习日志，再聚合到课程级与课时级进度表。
- 视频续播：课时级进度表中保存最后播放时间点，前端按 lessonId 取值续播。
- 写入路径优先经 Redis 缓冲后异步落库。

## AI 对话

- 对话链路：`AgentController.sendMessage` → `ChatComponent`（意图路由）→ 分支执行（RAG / MCP 工具 / 通用对话）→ Netty WebSocket 流式回推。
- 消息落库：用户消息立即落库，AI 消息流式完成后落库；取消 / 错误也要落状态。
- 意图路由结果（intent / data / stage）必须持久化，便于学习分析。
- 禁止在 Controller 里直接调用 ChatClient / VectorStore。

## MCP 工具

- 工具在 `nexora-mcp` 模块；`@Tool` 方法只做教学域操作，入参校验，返回字符串。
- 工具内部复用 common 的 Service，不直接写 SQL。
- web 端 MCP Client 连接 `http://localhost:8084`（配置化，禁止硬编码）。

## 个性化路径

- 学生可自建学习分类；AI（`createStudyPath` / `planNextStep` 工具）或规则按学段生成主线 + 兴趣分支路径。
- 遗忘曲线：知识点掌握后按 1/3/7/15 天间隔生成 REVIEW 复习节点；快测未过 → `knowledge_mastery` 回炉"进行中"并回补路径。
- 路径节点状态（未解锁 / 进行中 / 已掌握）由 `knowledge_mastery` 驱动，前端节点地图按此渲染。

## 资源

- 上传走分片 session（Redis 组件提供）+ 异步任务队列处理。
- 视频字段（URL、HLS 索引、时长等）命名稳定，前端依赖，禁止改名。

# 接口命名

- 模块名小驼峰，对应主业务实体：`courseInfo`、`courseChapter`、`knowledgePoint`、`questionInfo`、`resourceInfo`、`learningPath`、`knowledgeMastery`、`agent`、`practice`、`knowledgeDoc`、`aiGeneration`、`promptTemplate` 等。
- 动作：`loadDataList`、`add`、`update`、`delete`、`detail`、`getXxxOptions`、`submitAnswer`、`sendMessage`、`cancelMessage` 等。
- 完整路径形如：`/api/<模块>/<动作>`。

# 字段一致性自检

- 后端字段命名与前端 API 文件调用保持一致。
- 列表分页结果字段固定（总数 / 页码 / 页大小 / 列表数组）。
- 时间字段统一字符串格式，不返回时间戳。
- 学段 / 难度 / 意图等枚举值前后端统一。

# 检查清单（业务专项）

- 学生视角接口是否校验"学生 ID = 当前登录用户"，并按学段裁剪内容范围。
- 管理端接口权限编码与菜单表是否一致。
- AI 出题是否落库为待审核状态。
- 练习批改后是否回写 knowledge_mastery。
- 学习记录写入是否走了 Redis 缓冲。
- AI 对话是否流式、是否落库、意图是否持久化。
- MCP 工具是否白名单化、入参是否校验。

# 与其他 skill 的分工

- 新增一整套 CRUD 模块 → 用 `java-module` 生成骨架，再回到本 skill 套业务规则。
- 改既有业务接口 / 加细分动作 → 直接看本 skill 的领域映射。
- AI 对话细节 → `nexora-ai-assistant`；MCP 工具 → `nexora-mcp`；知识库 → `nexora-rag`；多模态 → `nexora-multimodal`。
