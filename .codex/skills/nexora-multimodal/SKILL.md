---
name: nexora-multimodal
description: Nexora 多模态内容生成与前端渲染规范：SVG 动画讲解、绘本生成、AI 绘画、Word / PPT 生成、在线编程（Pyodide）、游戏化练习。TRIGGER when 开发"动画讲解 / 绘本生成 / 生成 PPT / Word / 在线编程环境 / 游戏化练习"相关能力。SKIP when 只做普通文本对话或纯 CRUD。
---

# 目标（赛题要求至少 3 种）

对话问答（基础）+ 多模态教学（视频 / Word / PPT）+ SVG 动画讲解 + 绘本生成 + AI 绘画 + 编程环境 + 游戏化练习。优先实现：SVG 动画、绘本、游戏化练习、在线编程、AI 绘画。

# 通用生成链路

1. AI 生成结构化内容（JSON / Markdown / SVG 脚本）。
2. 落库 `ai_generation_record`（type、content、fileUrl、source[STUDENT / ADMIN_PRESET]、status、studentId、knowledgePointId）。
3. 需要图片时调用文生图能力（如百炼文生图 API），图片上传到资源模块统一管理。
4. 对话消息以 bizType + bizData 携带产物，前端按类型渲染卡片（全屏体验走独立页面 / 弹层）。

# SVG 动画讲解（Animation）

- 生成：LLM 产出分步脚本 JSON：`{ title, steps: [{ svg, narration }] }`——每步一段 SVG（可含 SMIL / CSS 动画）+ 讲解文案。排序算法 / 神经网络原理等抽象概念按步骤演化。
- 渲染：前端 `SvgAnimationPlayer` 逐步播放（上一步 / 下一步 / 重播），支持导出下载 SVG、默认保存到"我的-已保存内容"。
- 入口：输入框"动画讲解"模式直接路由 ANIMATION 意图。
- 兜底：复杂动画可退回"视频资源 + 讲解文本"。

# 绘本生成（PictureBook）

- 生成：LLM 产出绘本 JSON：`{ title, pages: [{ imagePrompt, text }] }`；逐页文生图 + 文本。
- 渲染：`PictureBookViewer` 翻页组件（图片 + 大字文本），支持朗读（浏览器 SpeechSynthesis，零成本）。
- 预置资源库：管理员可生成 / 上传绘本并发布（source=ADMIN_PRESET），低龄学生可浏览童话 / 科普故事库。
- 学段：主要面向小低 / 小高；文本短句、画面童趣。

# AI 绘画（Drawing）

- 生成：文生图（提示词 → 图片），落 `ai_generation_record`（type=DRAW），图片上传资源模块统一管理。
- 应用：独立创作展示，也可作为绘本配图 / 课件插图复用。
- 学段适配：小低自由涂鸦引导，小高指定主题，初中以上可指定风格（插画 / 像素 / 海报）。

# Word / PPT 生成（多模态教学）

- 生成：LLM 产出大纲 / 内容 JSON → Apache POI 生成 .docx / .pptx。
- 产物：文件上传到资源模块，返回 URL；在课程资源中关联。
- 用途：管理员后台上传现有资源为主，AI 按知识点生成初稿为辅。

# 在线编程（CodeLab）

- 前端：Monaco 封装组件 + 题目说明 + 运行结果 / 控制台；预置代码框架按学段提供（初中：Python 基础；高中：算法实现）。
- 运行：**Python 用 Pyodide 在浏览器端执行**（WASM 沙箱，无服务端执行风险）；JS 走 sandboxed iframe。
- 联动：学习路径进入编程知识点时 AI 引导进入并预置代码框架；练习结果回写 `knowledge_mastery`，作为掌握判定依据之一。
- 学段可见性：小学低年级不展示入口。

# 游戏化练习（Quiz / Practice）

- 形态：对话内答题卡片（`QuizCard`）+ 学习路径节点快测，不单设页面。
- 生成：`generateQuiz` 工具或管理端按知识点出题；题型单选 / 多选 / 判断 / 填空。
- 交互：逐题作答 + 即时反馈（正确 / 错误 + 解析）+ 激励（进度条 / 得分 / 星星 / 徽章）。
- 批改：客观题自动评分；作答落 `practice_record`，回写 `knowledge_mastery`（掌握度 / 是否掌握）。
- 学段难度：难度与学段映射（如小低 1 颗星、高中 3 颗星），出题时按 stage 约束。

# 自检清单

- 生成内容结构化落库（ai_generation_record），可追溯可管理。
- 图片 / 文件走资源模块统一管理，不存散乱路径。
- 编程执行在浏览器沙箱（Pyodide / iframe），服务端不执行学生代码。
- 练习即时反馈 + 回写掌握度。
- 学段适配：低龄图形化、高龄代码 / 深度。

# 与其他 skill 的分工

- 本 skill：多模态能力本身（生成 + 渲染约定）。
- `nexora-ai-assistant`：对话中触发多模态生成（意图 PICTURE_BOOK / DRAW / ANIMATION / QUIZ / CODING）。
- `react-web-page`：具体页面与卡片组件实现。
- `nexora-java`：ai_generation_record / practice_record / knowledge_mastery 表与接口。
