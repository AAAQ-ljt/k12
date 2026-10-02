# 课程推荐（MCP 工具）设计方案（v1，待确认）

> 目标：补齐 AI 助教的「课程级推荐」能力——学生在对话里说"推荐几门适合我的课"，AI 调用 MCP 工具拿到**真实课程候选**（按年级/兴趣/热度确定性打分），给出有理由的推荐；可选在对话里直接出课程卡片，点击进课程详情。
> 本文为设计稿，确认后再开工。

---

## 1. 现状（已核实）

| 事实 | 证据 |
|---|---|
| MCP 已有 11 个工具：6 教学（含 `queryCourse` 精确查询、`recommendResource` 资源卡）+ 5 知识页 | `nexora-mcp/.../TeachingToolService.java`、`KnowledgeToolService.java` |
| **6 个教学工具目前没有接给对话模型**——web 侧只包装了 5 个知识页工具 + 3 个 AI 处理工具 | `KnowledgeAgentToolComponent.MCP_TOOL_SPECS`（只含 listKnowledgePages 等 5 项） |
| 现有 `queryCourse` 只是「学段 + 课程名模糊 + status=1」的平铺查询，无个性化、无排除已加入、无推荐理由 | `TeachingToolService.queryCourse` |
| `course_info` 有可用的推荐信号：stage / grade / subject / difficulty / description / intro / lesson_count / **study_count（学习人数）** / sort | 表结构实测 |
| 个性化来源现成：`user_wiki_profile`（学习目标/关键问题/感兴趣学科/术语）、`course_enrollment`（已加入）、`course_study_progress`（进度） | 表结构实测 |
| 课程内容不在 ES、RAG 检索不到课程 → 课程推荐必须走工具，不能靠检索 | 上一轮实测：ES 只有 6 篇知识文档 |
| MCP 依赖 nexora-common，课程/档案/进度 Service 都在 common | `nexora-mcp/pom.xml`、common/service 目录 |

## 2. 交互形态

学生：**「推荐几门适合我的课」** → （模型调用 recommendCourses）→ AI 回答：「根据你的学习档案（兴趣：人工智能；编程、目标：学会 Python 基础），推荐这 3 门：
1. **Python 入门**（初一 · 信息技术 · 12 课时 · 86 人学过）——匹配你的兴趣「编程」，同学段……
…」
→ （Phase 2）回答下方出现 3 张**课程卡片**，点击直达课程详情页。

## 3. 总体数据流

```
学生提问
  → 意图分类（RECOMMEND / CHAT）
  → 模型看到工具 recommendCourses（描述里写明何时调用）
  → 模型发起工具调用
  → web 包装层（CourseAgentToolComponent）注入 userId + stage（模型不可见，防越权）
  → MCP 工具执行 → common 组件 CourseRecommendComponent 打分 → 返回 JSON
  → 包装层：① 把 JSON 转成人话文本回给模型 ② 结构化课程列表缓存进 ToolContext
  → 模型写推荐话术（只允许引用工具返回的课程）
  → 流结束：缓存非空 → 推送 course 卡片事件 + 落库 bizType=COURSE_RECOMMEND
  → 前端渲染课程卡片，点击跳 /course-material/{courseId}
```

## 4. MCP 工具设计：`recommendCourses`

### 4.1 入参（工具 schema 只暴露后 5 个；userId 由 web 注入）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userId` | string | 是（web 注入） | 学生ID；用于排除已加入课程、读取学习档案与本人年级。**不出现在给模型的 schema 里**（沿用知识页工具的安全模式） |
| `grade` | string | 否 | 精确年级（如"高一"），缺省取用户当前年级 |
| `subject` | string | 否 | 学科过滤（如"信息技术"） |
| `interest` | string | 否 | 兴趣/目标关键词（分号分隔）；缺省自动取学习档案的 interest_subjects + learning_goal + key_questions |
| `limit` | int | 否 | 返回条数，默认 3，上限 5 |
| `includeEnrolled` | bool | 否 | 默认 false（排除已加入课程）；true 时返回并可标注进度（用于"继续学习"类提问） |

### 4.2 返回（JSON 字符串；MCP 直连客户端也可机读）

```json
{
  "courses": [{
    "courseId": "725713169268052",
    "courseName": "五年级课程",
    "grade": "五年级", "stage": "PRIMARY_HIGH",
    "subject": "数学", "difficulty": 1,
    "lessonCount": 1, "studyCount": 42,
    "description": "面向五年级的数学启蒙……",
    "enrolled": false, "progress": null,
    "matchReasons": ["同学段匹配", "匹配兴趣：数学", "热门课程"]
  }],
  "totalCandidates": 6,
  "fallbackNote": "本年级暂无更多课程，已放宽到同学段推荐"
}
```

### 4.3 算法（确定性、可解释；纯 DB 逻辑，放 common 的 `CourseRecommendComponent`，MCP 工具只做壳）

```
候选集：course_info WHERE status=1 AND grade=用户年级
  若候选数 < limit×2 → 放宽为 stage=用户学段，fallbackNote 注明
  除非 includeEnrolled，否则排除 course_enrollment 中该用户的课程

打分（满分约 8）：
  学科命中兴趣关键词          +3       （subject 与关键词双向包含；可配小同义词表：人工智能→信息技术/信息科技）
  课程名/简介/介绍命中关键词   +2/词    （档案关键词按 ；;、，, 切分，长度≥2 才计）
  年级精确匹配                +2 ；仅学段匹配 +1
  热度                        +0~1     （候选集内 study_count 归一化，避免跨年级量纲不均）
  sort 升序                   平分兜底

排序取 top(limit)。无候选 → 返回空数组 + 说明，模型如实告知，不许编造。
```

### 4.4 边界处理

- 学习档案为空：只用「年级/学段 + 热度」排，回答话术自然降级为"热门课程"；
- 已加入课程：默认排除；`includeEnrolled=true` 时带 `progress`（来自 course_study_progress），模型可说"你已加入，进度 30%，可以继续"；
- 用户不存在：直接返回错误文案（沿用写工具的"校验用户存在"约束，虽是只读也校验）；
- 年级无课且同学段也无课：返回空 + 说明，模型如实说「暂时没有适合的课程，可以去课程教材看看」。

### 4.5 为什么不新开 `getCourseOutline`

推荐后追问「这门课讲什么」目前可由**已有的 `queryLesson`**（按 courseId 查课时）+ 推荐返回里的 description/lessonCount 覆盖，一期不新增工具，控制工具数量（工具越多模型选错概率越高）；二期若确有必要再加。

## 5. Web 侧接入设计

### 5.1 新组件 `CourseAgentToolComponent`（新建，与 KnowledgeAgentToolComponent 同模式）

- 维护 spec 白名单（1 项）：`recommendCourses`，schema 去掉 userId、description 写明调用时机（"用户询问推荐课程/学什么课/我适合学哪门课时调用"）；
- `call()` 里：注入 userId + stage → 调 MCP → 解析 JSON → **返回人话文本给模型**、**结构化列表存进 ToolContext 的 AtomicReference**（与现有 wikiOps 计数同一机制）；
- MCP 未启用 / 工具缺失 → 返回空数组并 log.warn，对话照常降级。

### 5.2 `AgentChatComponent` 改动

1. 合并工具：`courseTools` + 现有 `knowledgeTools` 一起挂到 `requestSpec.toolCallbacks(...)`；
2. 提示词：新增 `COURSE_RECOMMEND_RULES` 常量，在 `resolvePromptWithRag` 末尾追加（与 RAG 归属规则同一手法，**抗管理端提示词模板覆盖**）：
   ```
   ## 课程推荐规则
   1. 用户询问"推荐课程 / 学什么课 / 我适合学哪门课"时，先调用 recommendCourses 工具；
   2. 只推荐工具返回的课程，禁止编造课程名称或课程ID；
   3. 推荐时逐条说明理由（取自返回的 matchReasons），不要编造"学习人数/进度"以外的数据；
   4. 工具无结果时如实说明，可建议去「课程教材」页浏览，不要硬凑。
   ```
   仅在工具可用时追加（MCP 关闭 → 不加规则、对话不受影响）。
3. `finishMessage`：若本轮课程缓存非空 → 消息 bizType=`COURSE_RECOMMEND`、bizData=课程 JSON 落库，并复用 `push` 通道在流结束前补推一次 `type=recommend`（bizType=COURSE_RECOMMEND）事件。与现有 RAG 资源卡通道互不冲突（资源卡在流开始前推）。

### 5.3 新增 VO（common/vo，遵守"禁止返回 Map"）

`CourseRecommendCardVO`：courseId / courseName / grade / subject / stage / lessonCount / studyCount / description / enrolled / progress / matchReasons。

## 6. 前端设计（Phase 2，建议做——录屏演示价值大）

- `ai-tutor/index.tsx` 消息模型加 `courseRecommends?: CourseRecommendCardVO[]`；
- 历史回放：`item.bizType === 'COURSE_RECOMMEND' ? parseCourseRecommends(item.bizData) : []`（镜像现有 RESOURCE_RECOMMEND 的写法）；
- WS 事件：复用 `type: 'recommend'` 分支，按 bizType 分流到 courseRecommends；
- 卡片 UI：复用 `recommendCard` 样式 + `BookOpen` 图标，主标题课程名、副标题「高一 · 信息技术 · 12 课时 · 86 人学过」、右侧 badge「课程」；点击 `navigate('/course-material/' + courseId)`；
- 卡片出现在 AI 回答下方（与资源卡同一视觉语言），历史消息刷新后仍在。

## 7. 安全约束（沿用 MCP 既有红线）

- userId 由 web 端注入，模型不可传、不可越权（复用知识页工具的已验证模式）；
- 工具**只读**，不产生写入；不提供任意 SQL / 文件操作；
- 返回字段白名单：不含 create_by / 内部排序等运维字段；
- 限流：候选集查询 limit 上限 5、单查询条数封顶（如 200），防止大表全扫。

## 8. 涉及文件清单（预估）

| 模块 | 文件 | 改动 |
|---|---|---|
| common | `component/CourseRecommendComponent.java` | 新建：候选查询 + 打分 + VO 组装 |
| common | `entity/vo/CourseRecommendCardVO.java` | 新建 |
| nexora-mcp | `service/TeachingToolService.java` | 新增 `@Tool recommendCourses`（委托 common 组件，返回 JSON） |
| nexora-web | `component/CourseAgentToolComponent.java` | 新建：spec + userId 注入 + JSON 转文本 + 卡片缓存 |
| nexora-web | `component/AgentChatComponent.java` | 合并工具、追加提示词规则、finishMessage 落卡片 |
| front-web | `views/ai-tutor/index.tsx`、`api/agent.ts` | 卡片渲染 + 历史回放 + 类型 |
| 文档 | 产品说明书 §3.3、联调验收 3.11（工具数 11→12） | 同步 |

规模：后端约 4 个文件、前端约 2 个文件；无 DDL、无新增依赖。

## 9. 验证计划

1. **MCP 层**：`tools/list` 含 recommendCourses；直接 call 验证 4 组用例——高一（有档案/有课）、四年级（绘本学段）、档案为空、已加入课程（includeEnrolled 两种取值）；
2. **端到端**：高一账号问「推荐几门适合我的课」→ 观察工具调用日志 → 回答只含真实课程 → 卡片出现 → 点击进课程详情 → 刷新历史卡片仍在；
3. **越权**：问「给用户 B 推荐课程」应无效（模型只能拿到自己的 userId）；
4. **降级**：nexora-mcp 停掉 / `NEXORA_MCP_CLIENT_ENABLED=false` 时对话正常、不出现"课程推荐规则"导致的幻觉；
5. **回归**：知识页工具、RAG 资源卡、意图分类不受影响。

## 9.5 附：TeachingToolService 现状核查（2026-10-02 实测）

6 个教学工具**全部有实现、实测全部可用**（MCP 8084 上真实调用验证；写入工具的测试记录已清理），但深浅不一，发现如下问题，**建议纳入本次改动顺手修掉**（都是小改动）：

| 工具 | 实测结果 | 问题 |
|---|---|---|
| queryKnowledgePoint | 正常返回 |
| queryCourse | 正常返回 | 无年级/学科过滤（SENIOR 下高一高三混排）；**无条数上限**；返回信息只有名称/年级/ID，没有简介与学习人数——模型拿不到推荐理由素材 |
| queryLesson | 正常返回 | "按课时ID查详情"只返回课时名+ID（无章节/时长/绑定资源）；按课程查列表不校验课程是否上架；无上限 |
| recommendResource | 正常返回 | 名叫"推荐"实为"最新优先查询"；**stage 为空时全学段混合**（实测不带 stage 返回的视频无法判断学段）；无个性化 |
| queryMastery | 正常返回（平均分 60，与库里数据一致） | **stage 参数声明但未使用**（SQL 只有 WHERE user_id） |
| saveLearningRecord | 合法写入成功、非法类型被白名单拦截、不存在用户被拒 | **detail 参数是死参数**：PO（recordId/userId/resourceId/courseId/lessonId/actionType/duration/createTime）没有 detail 字段，收下即丢；duration 恒为 0；无幂等 |

共性问题：
1. **6 个工具没有一个接给对话模型**——web 侧 `KnowledgeAgentToolComponent` 只包装了 5 个知识页工具，教学工具目前只有直连 MCP 的客户端能用；
2. 全部返回中文文本而非 JSON，机读需正则解析（卡片化改造要转结构化）；
3. 异常一律 `e.getMessage()` 回给调用方，可能泄露内部细节；
4. 统一缺条数上限/分页（当前数据量小——知识点 78、课程 2、官方资源 12——暂无感，属潜在风险）。

> 说明：这些问题不阻塞课程推荐开发，但"死参数"和"stage 缺失致全学段混合"属于会误导调用方的问题，建议本次一并修掉。

## 10. 待确认决策点（请拍板）

1. **范围**：只做「文本推荐」（Phase 1）还是含「课程卡片」（Phase 1+2）？→ **建议 1+2**，卡片对录屏和评委观感提升大，工作量约多半天。
2. **推荐范围回退**：同年级优先、不足放宽到同学段（建议）还是严格只推同年级？
3. **已加入课程**：默认排除（建议），还是允许推"继续学习"？（我建议默认排除 + 参数可开，两种话术都保留）
4. **同义词表**：一期是否配「人工智能→信息技术/信息科技」这类小映射（建议配，6-8 条即可，明显提升命中率）？
