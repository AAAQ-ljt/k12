# gpt-image-2 文生图接口文档

> 来源：https://imgdoc.zikl.dev/doc.html
> 本机后续使用 **gpt-image-2 文生图** 模型，本文重点整理文生图与图生图两个场景，其余模型仅速览。

## 一、基本信息

| 项 | 说明 |
|---|---|
| 接口地址 | https://img.zikl.dev |
| 优化线路 | https://img.mzfe.de |
| 鉴权方式 | 请求头 `Authorization: Bearer <API_KEY>` |
| 模型 | 固定 `gpt-image-2` |
| 兼容风格 | OpenAI Images API 兼容（`/v1/images/generations`、`/v1/images/edits`） |

> 集成要求（项目规范）：API Key 必须走环境变量或外部化配置，禁止硬编码进代码。

---

## 二、文生图（核心）

仅使用文字提示词生成图片，请求体为 JSON。

```
POST https://img.zikl.dev/v1/images/generations
```

### 请求参数

| 参数 | 必填 | 说明 |
|---|---|---|
| model | 是 | 固定为 `gpt-image-2` |
| prompt | 是 | 文字提示词，中英文皆可 |
| size | 否 | 1K 分组最高支持 1K；4K 分组支持 1K / 2K / 4K；默认 `auto` |
| quality | 否 | high 分组支持 `high`；其他分组默认 `medium` |
| n | 是 | 请固定传 `1` |
| response_format | 否 | `url`（默认）或 `b64_json` |

> 说明：`size` 具体可选值由所持 API Key 的分组决定（1K 分组 / 4K 分组）；示例中使用了 `1024x1536` 这类「宽 x 高」格式。

### 请求示例（curl）

```bash
curl https://img.zikl.dev/v1/images/generations \
  -H "Authorization: Bearer <API_KEY>" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-image-2",
    "prompt": "湖蓝色调的山谷,清晨薄雾,极简插画风",
    "size": "1024x1536",
    "n": 1
  }'
```

### 响应格式

网址格式（默认 `response_format=url`）：

```json
{ "created": 1781837823, "data": [{ "url": "https://xxx.png" }] }
```

图片编码格式（`response_format=b64_json`）：

```json
{ "created": 1781837823, "data": [{ "b64_json": "<BASE64>" }] }
```

> - `url` 默认保存 **15 分钟**，建议尽快下载、转存。
> - 传 `response_format=b64_json` 时，图片编码位于 `data[0].b64_json`。

---

## 三、图生图

上传一张或多张参考图进行编辑，请求体为表单（multipart/form-data）。

```
POST https://img.zikl.dev/v1/images/edits
```

### 请求参数

| 参数 | 必填 | 说明 |
|---|---|---|
| model | 是 | 固定为 `gpt-image-2` |
| image / image[] | 是 | PNG / JPEG / WebP 文件，可重复传入 |
| prompt | 是 | 编辑要求；多图可用「第一张 / 第二张」指代 |
| size | 否 | 不传则沿用参考图尺寸；1K 分组最高支持 1K；4K 分组支持 1K / 2K / 4K |
| quality | 否 | high 分组支持 `high`；其他分组默认 `medium` |
| n | 是 | 请固定传 `1` |
| response_format | 否 | `url`（默认）或 `b64_json` |

### 请求示例（curl）

单张参考图：

```bash
curl https://img.zikl.dev/v1/images/edits \
  -H "Authorization: Bearer <API_KEY>" \
  -F "model=gpt-image-2" -F "image=@otter.png" -F "n=1" \
  -F "prompt=给这只海獭戴上一顶贝雷帽"
```

多张参考图：

```bash
curl https://img.zikl.dev/v1/images/edits \
  -H "Authorization: Bearer <API_KEY>" \
  -F "model=gpt-image-2" -F "image[]=@teapot.png" -F "image[]=@duck.png" -F "n=1" \
  -F "prompt=把第二张图的小鸭子放在第一张图的茶壶旁边"
```

### 响应格式

同文生图：

```json
{ "created": 1781837823, "data": [{ "url": "https://xxx.png" }] }
```

```json
{ "created": 1781837823, "data": [{ "b64_json": "<BASE64>" }] }
```

> `url` 默认保存 15 分钟；`response_format=b64_json` 时编码位于 `data[0].b64_json`。

---

## 四、其他模型速览

接口文档中还包含以下模型，如需可再查阅原文：

| 模型 | 用途 | 接口要点 |
|---|---|---|
| 香蕉 Pro（gemini-3-pro-image-preview） | Gemini 图像生成 | `POST /v1beta/models/{model}:generateContent`，画质档 |
| 香蕉2（gemini-3.1-flash-image-preview） | Gemini 图像生成（速度档） | 同上，额外支持 8:1 / 4:1 / 1:4 / 1:8 超宽长条 |
| veo-3.1（fast / generate / ref） | 异步视频生成 | `POST /v1/videos`，提交返回 id 后轮询 `GET /v1/videos/{id}` |
| kling-3.0-omni（720p / 1080p） | 可灵视频生成 | `POST /v1/videos`，异步轮询，时长 5 / 10 / 15 秒 |