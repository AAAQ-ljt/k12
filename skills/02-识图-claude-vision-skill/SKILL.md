---
name: claude-vision-skill
description: 图片识别/识图技能。当用户发送、分享图片（本地文件路径、附件或网络 URL），或要求分析、描述、识别图片内容（如“看看这张图”“图片里有什么”“describe this image”）时使用。把图片交给外部 vision 模型（阿里云百炼 qwen3.7-flash-2026-07-15）返回文字描述，弥补当前模型没有原生识图能力的问题。
---

# 识图能力（Image Recognition）

你的底层模型不具备原生识图能力。遇到图片时，不要直接说“无法读取图片”，改用本技能目录下的 `vision.js` 调用外部 vision 模型识图，把返回的文字描述作为你对图片的理解。

## 怎么用

本地图片路径：
```
node "C:\Users\luo20\.codex\skills\claude-vision-skill\vision.js" "<图片路径>" "<问题>"
```

网络图片 URL：
```
node "C:\Users\luo20\.codex\skills\claude-vision-skill\vision.js" --url "<图片URL>" "<问题>"
```

## 触发场景

- 用户分享本地图片路径（如 `C:\...\xxx.png`、`D:\...\photo.jpg`）
- 消息中出现 “Saved attachments:” 或类似图片附件路径/URL
- 用户要求分析、描述、识别图片内容（中英文均可）

## 规则

- 对每张图片依次运行一次 `vision.js`，拿到所有文字描述后再统一回复，不要中途打断。
- 用户没有指定问题时的默认提示词：`请详细描述这张图片的内容。`
- 用户有具体要求时作为第二个参数传入，例如 `图片里有什么文字？`、`这是什么物体？`。
- 图片路径用英文双引号包裹（Windows 路径可能含空格）。
- 脚本失败时（输出 “识图失败: ...”），把错误信息原样反馈给用户，不要编造图片内容。
- 不要向用户输出 base64 数据；只汇报描述结果。
