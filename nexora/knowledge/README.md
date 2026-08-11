# K12 知识库源文件

本目录存放 K12 AI 通识课的知识库源文件，作为 RAG 向量检索的入库资产。

## 目录结构

- `primary-low/` — 小学低年级
- `primary-high/` — 小学高年级
- `junior/` — 初中
- `senior/` — 高中

## 文档命名规范

每篇文档命名 `知识点-主题.md`，头部 YAML front-matter：

```yaml
---
stage: PRIMARY_LOW          # 学段
knowledgePointId: KP_PL_001  # 知识点ID
difficulty: 1                # 难度 1-3
title: 什么是人工智能         # 标题
dataType: MARKDOWN           # 数据类型
---
```

## 覆盖主题

- 什么是 AI
- 机器学习
- 神经网络
- 计算机视觉
- 自然语言处理
- 排序算法
- Python 编程基础
- AI 伦理

## 入库流程

详见 skill `nexora-rag`：源文件批量导入；管理端上传资料（Word / PPT / PDF / 视频）经"解析 → 文本化 → 向量化"管线入库。
