---
name: nexora-rag
description: Nexora 知识库与 RAG 规范：知识文档组织、后台上传资料解析入库（Word/PPT/PDF/视频）、ES 向量索引、embedding 配置、按学段 / 知识点 / 难度的检索策略。TRIGGER when 涉及知识库文档管理、资料上传解析、向量入库、检索增强回答、"根据知识库回答"。SKIP when 不涉及向量检索。
---

# 核心决策：不用多模态向量模型

统一走"解析 → 文本化 → 文本向量"管线，单一向量空间（text-embedding-v4，1024 维）+ ES 单一索引 + metadata 过滤。理由：

- K12 知识问答是"文本问题 → 文本知识"，文本向量 + 学段 / 知识点 metadata 过滤精度最高、可解释（评分标准要求知识科学性与准确性）。
- 多模态向量模型（图文混排嵌入）适合"以图搜图 / 视频帧检索"，赛题无此需求；混合检索只会徒增工程复杂度。
- 图片（绘本配图、课件插图）不进知识库，走资源模块管理。

# 两条入库路径

1. **知识库源文件（knowledge/ 目录）**：按学段组织 `primary-low` / `primary-high` / `junior` / `senior`；每篇 `知识点-主题.md`，头部 YAML front-matter：stage、knowledgePointId、difficulty、title、dataType。批量导入。
2. **管理端上传资料**：文件本体存 `resource_info`（课程教材页可见 / 可下载）→ 解析抽取文本 → 生成 / 更新 `knowledge_doc`（记录 sourceResourceId）→ 向量化入库。

# 上传资料解析管线（按文件类型）

- Word（doc/docx）、PPT（ppt/pptx）：Apache POI 抽取文本（PPT 按幻灯片逐页抽，保留页序）。
- PDF：PDFBox 抽取文本。
- Markdown / TXT：直读。
- 教学视频 / 音频：v1 仅用标题 / 简介 / 关联知识点的标注文本入库（视频本体经转码 HLS 供播放）；可选增强：ASR（阿里百炼 paraformer）转写字幕后走同一文本管线，转写任务进资源处理队列异步执行。
- 抽取文本 → 清洗（去页眉页脚 / 多余空白）→ 分块（按标题 / 段落，单块 ≤ 500 字）→ embedding → ES。

# 向量化与索引

- Embedding：阿里百炼 `text-embedding-v4`（维度 1024），与 ES 索引 dimensions 一致。
- ES 索引：`nexora-index-vectorstore`，`initialize-schema: true`。
- Document metadata 固定字段：`dataType`（如 `KNOWLEDGE`）、`stage`、`knowledgePointId`、`difficulty`；命名稳定，禁止改名。
- 入库：管理端"重新入库"接口（读 knowledge_doc → 切分 → vectorStore.add）；幂等（重复入库先按 docId 删旧块再加新块）。
- 向量状态：`knowledge_doc.vectorStatus`（未入库 / 已入库 / 过期）供管理端展示；内容更新后置为过期。

# 检索策略

- 构造 `SearchRequest`：query = 用户问题，`filterExpression` 按学段（`stage == 'JUNIOR'`）与 dataType 过滤，`topK` 10~15，`similarityThreshold` 0.5。
- 召回后按文档去重，拼接为 ragData 注入提示词。
- 无结果：提示词明确告知"知识库中暂无相关内容"，模型不得编造。
- 学段过滤优先：同一知识点不同学段的文档必须能被 metadata.stage 准确区分。

# 知识库质量

- 内容按教材 / 权威来源整理，标注来源；避免低质网络文本。
- 管理端提供"检索测试"入口：输入问题实时查看召回块与相似度，用于验收入库质量。

# 自检清单

- metadata 包含 dataType / stage / knowledgePointId / difficulty。
- 索引维度与 embedding 模型一致（1024）。
- 检索按学段过滤 + 相似度阈值。
- 无结果时不编造。
- 入库接口幂等（重复入库不产生重复脏数据）。
- 上传资料解析失败有明确错误提示与重试入口。

# 与其他 skill 的分工

- 本 skill：知识库与检索。
- `nexora-ai-assistant`：对话链路如何消费 ragData。
- `nexora-java`：knowledge_doc 表与管理接口。
