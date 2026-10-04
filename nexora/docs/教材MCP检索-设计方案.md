# 教材知识检索 MCP 工具设计方案（v3.2，待确认）

> 目标：解决「指名教材/章节」类问题（"高中政治必修一第一课讲讲"）在现有 RAG 下失败的问题。
> 用户决策：① 指名书/章节的精确检索走 MCP 工具辅助——模型自主"查书目 → 看目录 → 读该节"，
> 现有向量 RAG 保持不变（语义型问题零影响）；② **在 `nexora-mcp` 模块的 service 包里新加一个独立的
> 工具 Service（与 `KnowledgeToolService` 平级、风格一致），8084 这个 MCP 服务本身不变**，
> 安全规范参考 `KnowledgeToolService` 的既有标准；③ **检索强制加学段限定**（学生登录学段，
> 初一→初中、高二→高中，不做全库大海捞针）；④ **顺带审计修复 `TeachingToolService` 的 5 个旧工具
> 并实测确保可用**（用户已手动删除 queryKnowledgePoint，尊重该改动，基于剩余 5 工具修）；
> ⑤ **MCP 能力说明动态注入主线 system prompt——只在工具真实挂载时追加，MCP 关闭时一个字不提**，
> 让模型知道自己有哪些工具、用户问"你能干什么"得到如实清单（不吹牛）。
> 本文为设计稿，确认后开工。

---

## 1. 背景与已核实事实

| 事实 | 证据 |
|---|---|
| 现有检索是单路向量 top-10（个人库优先、学段过滤、阈值 0.5），无"书/章节"概念 | `RagSearchComponent.buildRagResult`；MySQL 关键词兜底按整句子串 indexOf，自然语言几乎永不命中 |
| 官方教材库 149 本（status=1，owner_id IS NULL），单本 8~26 万字 / 113~350 个分块，全库约 3 万分块 | 服务器 `knowledge_doc` 实测（2026-10-02） |
| 《高中政治必修一》在库（doc `4f02515b…`，113 块），但 AI 回答"库里没有收录必修一" | zhangsan 对话截图 + 服务器库实据——top-10 相似度轮不到它 |
| 教材正文是 Markdown（AI 整理管线统一产出），标题两种形态 | 正文实测：`# 第一章·1.1 数据、信息与知识`（带 #，信息技术）；`第二课·1 新民主主义革命的胜利`（裸行，政治）；子级 `## 摘要` 等 |
| 标题存在错别字/变体 | 库内实测《高中物理选择性**必须**一》 |
| MCP 链路现成：mcp 服务端 @Tool + `ServerRegisterConfig` 注册（每 Service 一个 provider bean）；web 端 `KnowledgeAgentToolComponent.MCP_TOOL_SPECS` 按名包装挂对话，userId/学段经 ToolContext 注入（模型不可见不可伪造）；MCP 未启用时返回空数组、对话自动降级 | `ServerRegisterConfig`（teachingToolProvider / knowledgeToolProvider 两个 bean 的现成模式）、`KnowledgeAgentToolComponent`、web `application.yml`（单连接 localhost:8084/mcp，懒握手） |

## 2. nexora-mcp 模块改动（不新建模块、不动现有两个 Service）

```
nexora-mcp/src/main/java/com/nexora/
├── service/TextbookToolService.java      新增：3 个只读 @Tool（与 KnowledgeToolService 平级、同风格，~250 行）
├── utils/TextbookContentUtils.java       新增：书名归一化 + MD 目录解析（~120 行）
└── config/ServerRegisterConfig.java      新增 1 个 provider bean（照 knowledgeToolProvider 抄，3 行）
```

- `KnowledgeToolService`、`TeachingToolService`、启动类、pom **全部不动**；
- 工具经 8084 同一端点暴露，web 端**不需要加任何新连接**。

**安全规范（逐条对齐 KnowledgeToolService 既有标准）**：
1. 白名单数据操作：只查 MySQL `knowledge_doc` 官方库（`owner_id IS NULL AND status=1`），无 SQL/文件操作、工具内不调用大模型；
2. 只读：不提供任何新增/修改/删除类工具；
3. 入参防线：`docId` 存在性校验；学段由 web 端经 ToolContext 注入（模型不可见、不可伪造），三工具都按学段过滤/校验，与现有 RAG 同口径；
4. 输出防线：书目 ≤20 条（兜底节选 ≤15）、目录 ≤200 条、单节 ≤8000 字符（offset 续读），防止撑爆模型上下文；
5. 返回结构化字符串、内部 try-catch 不抛未捕获异常、失败 `log.warn` 返回友好文案。

## 3. 工具设计（3 个只读工具）

### 工具 1：`searchTextbooks`（查书目）
- 入参：`keyword`（书名关键词，如"必修一"、"政治"、"信息技术"）；`stage`（注入）
- 逻辑：标题归一化（必须↔必修、全半角）后模糊匹配打分，按学段过滤，最多 20 条
  （序号/书名/docId/学段/字数/章节级数）；未命中时返回"未找到"并附**同学段书目节选（≤15 本）**，模型可如实转述"库里有什么"。
- 模型可多轮换关键词重试（比正则预解析更稳）。

### 工具 2：`getTextbookToc`（读章节目录）
- 入参：`docId`；`stage`（注入，校验 doc.stage 一致）
- 逻辑：取 content → `TextbookContentUtils` 解析：① 行首 ATX 标题（`#{1,3}`）② 裸行课节标记
  （`第X[课单元章节讲框]`开头、行长 <60 字符防误伤正文，支持 `·N` 框序）→ 有序目录
  `[{序号, 层级, 标题, 字符区间}]`，上限 200 条，超出提示按序号分段看。
- 实时正则扫描（26 万字毫秒级），v1 不做缓存。

### 工具 3：`readTextbookSection`（读某节内容）
- 入参：`docId`；`section`（目录序号或章节标题，标题按归一化模糊匹配）；`offset`（可选，续读偏移，默认 0）；`stage`（注入，校验）
- 逻辑：定位目录项 → 切片 `content[本节起点, 下一同级标题起点)` → 从 offset 起最多返回 8000 字符；
  截断时注明"本节共 X 字，已返回 Y~Z，续读传 offset=Z"。
- 返回：书名 + 节标题 + 正文（Markdown 原样）。

### 典型调用链
- 指名讲解："高中政治必修一第一课讲讲" → `searchTextbooks("必修一")` → `getTextbookToc(docId)`
  → `readTextbookSection(docId, "第一课")` → 结合参考内容讲解并注明出处。2~3 次工具调用，每次毫秒级 MySQL + 正则。
- 库存盘点："你在学校知识库里有什么？" → `searchTextbooks` 分学科查 → 如实汇总（现状是凭 ≤10 个分块标题脑补）。

## 4. web 端接线（改动集中在 KnowledgeAgentToolComponent + AgentChatComponent 两个文件）

1. `MCP_TOOL_SPECS` 追加 3 条 spec（模型可见的描述/入参 schema **不含 stage**，走既有 injectStage 注入模式）；
   描述里写清使用链"用户指名教材/章节时：先 searchTextbooks 找书 → getTextbookToc 看目录 → readTextbookSection 读该节"；
   `buildCallbacks()` 的按名查找对同一 8084 连接透明生效，**无需任何新连接/新组件**。
2. `McpToolSpec` record 增加一个 `markWiki` 标志位：知识页 5 工具维持 `true`（打 WIKI 标记刷新抽屉），
   教材 3 工具为 `false`（只读检索，不触发知识页抽屉刷新）——`markWikiOp` 按标志位执行，一行判断。
3. **MCP 能力说明动态注入（用户要求⑤，AgentChatComponent）**：
   - 现状问题：工具在 prompt 组装**之后**才构建（`assistantAnswer` 第 377 行组装 prompt、第 385 行才 buildCallbacks），
     模型只能靠工具描述"偶遇"能力；MCP 关闭时模型若被问"你能干什么"只能凭空想象。
   - 改法：① 工具构建**提前**到 prompt 组装之前；② `resolvePromptWithRag` 增加 `ToolCallback[] tools` 参数，
     组装完既有内容（模板 + RAG 数据 + 引用规则）后，**tools 非空才追加**"当前挂载的 MCP 工具"能力块；
     ③ 能力块内容从**真实挂载的工具名**推导（静态 `name → 中文说明` 映射表与挂载列表取交集），
     构造上保证"提示词说了的，就一定真挂着"；未挂载时零追加。
   - 能力块草稿（按真实交集生成，示例为 MCP 全开时）：
     ```
     ## 当前可用的 MCP 工具（本轮对话已真实挂载）
     - 知识页工具：listKnowledgePages 查个人知识页清单 / readKnowledgePage 读某页全文 /
       createKnowledgePage 新建草稿 / updateKnowledgePage 覆盖草稿 / ingestKnowledgePage 入库向量化 /
       aiSummarizeKnowledgePage、aiRewriteKnowledgePage、aiOrganizeKnowledgePages（AI 摘要/改写/归档整合，一律落草稿）
     - 教材检索工具：searchTextbooks 查官方教材书目 / getTextbookToc 读某本书章节目录 / readTextbookSection 读指定章节正文
     使用要求：
     1. 用户问"你能做什么/有哪些工具"时，只依据本节如实回答，严禁声称本节之外的能力；
     2. 用户指名教材/章节而上方参考内容未覆盖时，用教材检索工具查证后再答，引用注明《书名》第X课；
     3. 涉及学生个人知识页的改动一律先落草稿，用户明确要求才 ingest 入库。
     ```
   - 生效范围：所有走到流式回复的意图（工具本就无条件挂载），含动画/出题/绘本降级 CHAT 的回复；
     意图分析、动画概念解析等内部模型调用不带工具，自然不受影响。
   - 原 v3.1 计划的"RAG_CITATION_RULE 追加教材口径 1 条"取消——教材使用/引用口径并入能力块第 2 条，
     天然满足"挂了才说"；`RAG_CITATION_RULE` 本体不动。
4. 部署后复核：`prompt_template` 表与 Redis 无 EXPLAIN/CHAT 覆盖行（v3 已实测无覆盖，部署时再核一次）。

## 5. 部署前提（需要你确认）

| 项 | 说明 |
|---|---|
| **nexora-mcp 必须运行** | 新工具经 8084 暴露；服务器上该服务"已建未启动"。需 web 侧 `NEXORA_MCP_CLIENT_ENABLED=true` + `systemctl enable --now nexora-mcp` |
| 内存 | 8G 机器：ES 1g 堆 + admin 1024m + web 1280m，mcp +512m ≈ 3.8g JVM（部署文档备忘"要起 mcp 先看内存"）。工具是纯 MySQL 读 + 正则，512m 足够 |
| 本地联调 | 本地同样要跑 nexora-mcp（web 连 localhost:8084），与录屏方案 §0 清单一致 |
| 不开启时 | 教材 3 工具缺席，对话与现状完全一致（自动降级，零回归） |

## 6. 改动清单（无 DDL、无新依赖、不新建模块）

| 模块 | 文件 | 改动 |
|---|---|---|
| nexora-mcp | `service/TextbookToolService.java`（新） | 3 个 @Tool（~250 行） |
| nexora-mcp | `utils/TextbookContentUtils.java`（新） | 归一化 + MD 目录解析（~120 行） |
| nexora-mcp | `utils/StageNormalizer.java`（新） | 学段归一化（~50 行，见 §9） |
| nexora-mcp | `service/TeachingToolService.java` | 按 §9 审计结论修复（~40 行改动，尊重已删除 queryKnowledgePoint 的现状） |
| nexora-mcp | `config/ServerRegisterConfig.java` | +1 个 provider bean（3 行） |
| nexora-common | `resources/mappers/LearningAnalysisMapper.xml` | 新增 `selectMasteryListByStage`（原 selectMasteryList 与 admin 调用方零改动） |
| nexora-common | `mappers/LearningAnalysisMapper.java` | +1 个方法声明 |
| nexora-web | `component/KnowledgeAgentToolComponent.java` | MCP_TOOL_SPECS +3 项、McpToolSpec 加 markWiki 标志位（~45 行） |
| nexora-web | `component/AgentChatComponent.java` | 工具构建提前并传入 resolvePromptWithRag；MCP 能力块按真实挂载动态追加（~50 行，见 §4.3） |
| 不动 | `KnowledgeToolService` / `TeachingToolService` 既有方法签名 / 启动类 / pom / `RagSearchComponent` / 表结构 | — |

## 7. 代价、风险与边界

- **延迟**：指名型多 2~3 次工具往返（每次毫秒级 MySQL+正则 + 模型决策），回答总时长约 +5~15s（流式可接受）；语义型零新增。
- **token**：书目 ≤20 行、目录 ≤200 行、节内容 ≤8000 字，单轮增量可控。
- **风险**：
  1. 书名变体/错别字（"选择性必须一"）→ 归一化 + 模型换关键词重试 + 未命中返回书目节选兜底；
  2. 无 MD 结构的文档（人工粘贴纯文本）→ 目录解析退化（只剩 ATX 标题或空），仍可用查书目 + 按书读内容兜底；
  3. 模型滥用工具 → 工具描述写明"仅指名书/章节时使用"，且三工具只读无副作用；
  4. 学段隔离：跨学段提问（高一问初一教材）查不到——与现有 RAG 同口径，如要放开再单独议。
- **本次不做**：个人知识库的"书"（知识页工具已有）；章节感知入库改造（二期）；任何写操作；
  TeachingToolService 的只读工具接入对话（属"课程推荐 MCP"专题，等该设计文档确认后一起接，本次只保证工具本身正确可用）。

## 8. 学段限定方案（用户要求①，统一口径）

**原则：学段一律以学生登录档案 `user_info.stage` 为准、由服务端注入，不从问题文本里猜**；
问题文本里的年级词（初一/高二/五年级）只参与书名/课程名**关键词匹配**（如书名《初一数学上》）。

| 链路 | 学段限定现状 | 本次动作 |
|---|---|---|
| 教材 3 工具（新） | 已设计为 web 注入 + 三工具强制过滤/校验 | 维持（149 本 → 只搜本学段几十本） |
| `RagSearchComponent` 向量检索 | 已按登录学段过滤 | 不动 |
| `queryCourse` / `recommendResource` | stage 由模型传参，传错就静默空结果 | 归一化 + 非法值返回有效编码提示 |
| `queryMastery` | **stage 参数收了但完全没用** | mapper 新增按学段查询并真正生效 |
| `saveLearningRecord` | 无学段语义 | 不涉及 |

`StageNormalizer`（nexora-mcp/utils）：StageEnum 编码大小写兼容 + 中文别名（小学低年级/小学高年级/初中/高中）
+ 年级词映射（初一/初二/初三→JUNIOR；高一/高二/高三→SENIOR；一/二年级→PRIMARY_LOW；三~六年级→PRIMARY_HIGH）；
裸"小学"歧义（跨两个编码）返回提示请指明低/高年级；非法输入返回"有效学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR"。

## 9. `TeachingToolService` 审计结论与修复（用户要求②）

> 基线：尊重工作区已删除 `queryKnowledgePoint` 的手动改动，基于剩余 5 工具修复；
> 修复后 5 工具全部经真实 MCP 链路实测（见 §10）。

| 工具 | 审计结论 | 修复 |
|---|---|---|
| `queryCourse` | status=1（上架）/ 过滤 / 排序均正确；①stage 无归一化；②输出缺学科/难度/课时数/学习人数/简介（推荐决策的关键信号，course_info 都有）；③无条数上限 | stage 归一化；输出补 subject/difficulty/lesson_count/study_count/简介截断；上限 20 条 |
| `queryLesson` | `setStatus(0)` **正确**（DDL：课时 0=正常 1=停用，反直觉，补注释防误改）；①courseId 未校验课程存在；②输出缺 summary/videoDuration | 课程存在性校验；列表/详情补 summary 与视频时长 |
| `recommendResource` | stage/knowledge_point_id/resource_type 过滤字段全部真实存在（DDL 核对过）；①工具描述的类型枚举与库不符——描述写"LINK"但 DDL 是 VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK（另有运行时写入的 ANIMATION）；②stage 无归一化 | 修正描述枚举；stage 归一化 |
| `queryMastery` | status==2 已掌握、mastery_score 0-100 语义正确（DDL 核对）；❌ **stage 参数收了但完全没用**（selectMasteryList 仅按 userId） | mapper 新增 `selectMasteryListByStage`（knowledge_mastery 本就有 stage 冗余列），stage 归一化后生效；输出补"总数 + 未解锁/进行中/已掌握分布" |
| `saveLearningRecord` | ❌ **detail 参数收了但被丢弃**（student_learning_record 表无该列，误导模型）；⚠️ duration 恒 0；⚠️ AI_CHAT 在白名单但全项目从未写入、DDL 注释也无（判定为对话场景预留，保留并注明）；目标识别三连查符合"禁循环查库"约束 | 删 detail 参数；加可选 `duration`（秒）参数并落库；返回记录ID；AI_CHAT 注明"预留" |
| 共性 | stage 归一化缺失 | 统一走 `StageNormalizer` |

## 10. 测试方案（修复与新增工具同一套）

1. **静态**：nexora-mcp / nexora-common / nexora-web 编译通过（JDK21 + Maven 3.9.11）。
2. **逻辑自测（不起服务）**：反射直调编译产物——
   `TextbookContentUtils`：`# 第一章·1.1` 与裸行 `第二课·1` 双形态解析、长行不误伤、200 条截断、区间切片与 offset；
   `StageNormalizer`：SENIOR/高中/高二/JUNIOR/初一/五年级/非法值/"小学"歧义 全用例；
   **MCP 能力块**：传空工具数组 → prompt 不含"MCP 工具"字样；传部分工具名 → 能力块只出现交集条目
   （如只挂知识页工具时不出现"教材检索工具"段），保证"说了必有"。
3. **真实 MCP 链路**：本地起 nexora-mcp（8084，连本地 MySQL/Redis）→ Python 脚本走 streamable-http JSON-RPC
   （initialize → tools/list 确认注册 → tools/call）**逐个实调 8 个工具**（教材 3 + 教学 5）断言返回；
   这一步同时验证 ServerRegisterConfig 注册与 web 端将依赖的协议链路。
4. **数据核对**：执行时先查本地库课程/掌握度/教材数据是否够测（录屏期造过数据；缺则先造最小数据并告知）。
5. **写工具闭环**：`saveLearningRecord` 写入后在本地库核对该行，测完删除测试数据。

## 11. 端到端验收用例（本地双端起来后逐条测）

1. "高中政治必修一第一课讲讲"（高一账号）→ 教材工具链命中必修一第一课，讲解注明《高中政治必修一》第一课，不再说"库里没有"；
2. "你先在学校知识库里有什么？"→ 如实按学段盘点书目（只列高中教材，不含初中/小学）；
3. "讲讲高中物理选择性必修一"→ 归一化命中错别字书名；
4. "推荐几门适合我的课"→ `queryCourse` 按学段返回含学习人数/课时数的课程清单（该工具已可用，为课程推荐专题铺路）；
5. **"你能帮我做什么？"（MCP 开）→ 回答如实列出知识页/教材检索工具能力，与能力块一致**；
6. **"你能帮我做什么？"（MCP 关）→ 不声称具备知识页整理/教材检索等 MCP 能力（能力块未注入，不吹牛）**；
7. "什么是机器学习"→ 不触发教材工具，现有 RAG 行为不变（回归）；
8. 停掉 nexora-mcp 再测 1~5、7 → 对话正常、无工具报错（降级回归）；
9. 服务健康：8084 `/mcp` 可握手、fat jar 校验通过、`journalctl -u nexora-mcp` 无错误。
