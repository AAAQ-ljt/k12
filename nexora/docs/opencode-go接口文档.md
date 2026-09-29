# OpenCode Go 接口文档

> 官方文档：https://opencode.ai/docs/zh-cn/go/ （页面最近更新 2026-09-28）
> 相关页面：OpenCode Zen 总览 https://opencode.ai/docs/zh-cn/zen/ ；Key 控制台 https://opencode.ai/auth
> 整理与实测日期：2026-09-29　实测环境：本机开发机，公网直连 `opencode.ai`（Cloudflare 边缘节点 ORD / LAX）
> 来源标注：**官方** = 官方页面原文整理；**实测** = 本次对线上端点的真实请求结果；**待测** = 拿到 API Key 后必须补做的验证

本文用于把 OpenCode Go 接入本项目（Nexora）的 AI 对话链路：一~六节是接口规格，第七节是无 Key 探测实测，
第八节是本项目的落地方案，第九节是拿到 Key 后要补的验证清单。

---

## 一、基本信息

| 项 | 说明 |
|---|---|
| 服务 | OpenCode Go（OpenCode Zen 体系下的订阅制套餐：Go $10/月、Go Plus $40/月） |
| API 根地址 | `https://opencode.ai/zen/go/v1` |
| 请求方式 | `POST`，`Content-Type: application/json` |
| 鉴权 | 按端点分两种头，见第二节（OpenAI 协议用 `Authorization: Bearer`，Anthropic 协议用 `x-api-key`）；**另有强制要求的会话头 `x-opencode-session`，缺失直接 400**，见第五节 |
| 协议风格 | 三套标准协议并存：OpenAI Chat Completions、OpenAI Responses、Anthropic Messages，**无私有字段** |
| 流式 | 沿用所选协议的标准 SSE（`stream: true`）；官方页面未对 Go 单独说明流式细节 |
| 模型清单 | `GET https://opencode.ai/zen/go/v1/models`（实测**免鉴权**可访问） |
| 服务定位 | 官方原文：「该服务主要面向国际用户，并提供稳定的全球访问」 |
| 订阅限制 | 官方原文：「每个工作空间只能有一名成员订阅 OpenCode Go 或 Go Plus」 |

**结论**：OpenCode Go 是一个标准的 OpenAI / Anthropic 协议网关，不是私有 SDK。本项目现在用 Spring AI 的
OpenAI 兼容客户端连 DeepSeek 官方 API（`https://api.deepseek.com`），接入 Go 只需把 base-url 指向
`https://opencode.ai/zen/go/v1` 并换 Key，**不需要新增任何依赖、不需要改代码**（见第八节）。

---

## 二、鉴权（实测确认）

官方页面没有写鉴权头格式，我用无效 Key 对三个端点各发了一次请求，从报错反推出下面的矩阵
（`Invalid API key.` 说明网关读到了该头、只是 Key 不合法；`Missing API key.` 说明该头根本没被识别）：

| 端点 | `Authorization: Bearer <KEY>` | `x-api-key: <KEY>` | 不带任何鉴权头 |
|---|---|---|---|
| `POST /chat/completions` | ✅ 被识别 → `Invalid API key.` | ❌ 未识别 → `Missing API key.` | `Missing API key.` |
| `POST /responses` | ✅ 被识别 → `Invalid API key.` | ❌ 未识别 → `Missing API key.` | `Missing API key.` |
| `POST /messages` | ❌ 未识别 → `Missing API key.` | ✅ 被识别 → `Invalid API key.` | `Missing API key.` |
| `GET /models` | 无需鉴权（不带任何头即 200） | 无需 | 200 |

- 走 OpenAI 协议（`/chat/completions`、`/responses`）→ `Authorization: Bearer <KEY>`。
- 走 Anthropic 协议（`/messages`）→ `x-api-key: <KEY>`，与 Anthropic 官方 API 的约定一致。
- `/messages` **不需要**额外带 `anthropic-version` 头（实测带与不带都能通过鉴权进入 Key 校验）。
- 本项目走 `/chat/completions`，因此是 Bearer —— Spring AI 的 OpenAI 客户端默认就是 Bearer，天然对齐。

---

## 三、端点与模型映射（官方）

下表为官方给出的「模型 → 模型 ID → 端点 → AI SDK 包」对应关系（原文照录）：

| 模型 | 模型 ID | 端点 | AI SDK 包 |
|---|---|---|---|
| Grok 4.7 | `grok-4.7` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| Grok 4.6 | `grok-4.6` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| GPT 6 Luna | `gpt-6-luna` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| GPT 5.6 Luna | `gpt-5.6-luna` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| GLM-5.3-Flash | `glm-5.3-flash` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| GLM-5.3 | `glm-5.3` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| GLM-5.2 | `glm-5.2` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| Kimi K3 | `kimi-k3` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| Kimi K2.7 Code | `kimi-k2.7-code` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| Kimi K2.6 | `kimi-k2.6` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| LongCat-2.0 | `longcat-2.0` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| LongCat 2.5 Preview Free | `longcat-2.5-preview-free` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| DeepSeek V4.1 Flash | `deepseek-v4.1-flash` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| DeepSeek V4 Pro | `deepseek-v4-pro` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| DeepSeek V4 Flash | `deepseek-v4-flash` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| DeepSeek V4 Flash Vision Exp | `deepseek-v4-flash-vision-exp` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| MiMo-V2.6-Flash | `mimo-v2.6-flash` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| MiMo-V2.6-Pro | `mimo-v2.6-pro` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| MiMo-V2.5 | `mimo-v2.5` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| MiMo-V2.5-Pro | `mimo-v2.5-pro` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| MiniMax M3 | `minimax-m3` | https://opencode.ai/zen/go/v1/messages | @ai-sdk/anthropic |
| MiniMax M2.7 | `minimax-m2.7` | https://opencode.ai/zen/go/v1/messages | @ai-sdk/anthropic |
| Muse Spark 1.3 Contributor | `muse-spark-1.3-contributor` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| Muse Spark 1.2 Contributor | `muse-spark-1.2-contributor` | https://opencode.ai/zen/go/v1/responses | @ai-sdk/openai |
| Qwen3.8 Max | `qwen3.8-max` | https://opencode.ai/zen/go/v1/messages | @ai-sdk/anthropic |
| Qwen3.8 Flash | `qwen3.8-flash` | https://opencode.ai/zen/go/v1/messages | @ai-sdk/anthropic |
| Qwen3.7 Plus | `qwen3.7-plus` | https://opencode.ai/zen/go/v1/messages | @ai-sdk/anthropic |
| Hy4 preview | `hy4-preview` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| Hy3 | `hy3` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |
| Space Bunny Free | `space-bunny-free` | https://opencode.ai/zen/go/v1/chat/completions | @ai-sdk/openai-compatible |

### 3.1 模型 ID 前缀的坑（官方）

在 OpenCode TUI / 配置文件里，模型 ID 写作 `opencode-go/<model-id>`，例如 `opencode-go/kimi-k3`；
**直接调 API 时用不带前缀的裸 ID**（`kimi-k3`）。不要把这个前缀带到 HTTP 请求体里。

### 3.2 模型清单实测：`/models` 比 Go 表格多出 13 个

`GET https://opencode.ai/zen/go/v1/models` 实测返回 **43** 个模型，响应结构为标准
`{"object":"list","data":[{"id","object","created","owned_by"}]}`（与 OpenAI 的 `/v1/models` 一致）。
官方 Go 表格里只列了 30 个，多出来的 13 个大概率属于 Zen 侧、**不保证在 Go 套餐内**：

| 仅在 `/models` 出现、未列入 Go 官方表格（待实测确认是否可用） |
|---|
| `minimax-m2.5` |
| `kimi-k2.5` |
| `glm-5.1` |
| `glm-5` |
| `deepseek-flash` |
| `qwen3.7-max` |
| `qwen3.6-plus` |
| `qwen3.5-plus` |
| `mimo-v2-pro` |
| `mimo-v2-omni` |
| `hy3-preview` |
| `grok-4.5` |
| `omen-alpha` |

**对本项目的直接影响**：`nexora-web` 的 `application.yml` 默认 chat 模型是 `deepseek-flash`，
它只在 `/models` 里出现、**不在 Go 官方表格内**；而视觉模型 `deepseek-v4-flash-vision-exp`
在 Go 表格内（明确可用，限额 $15/月）。切到 Go 后建议把 chat 模型换成表格内的 `deepseek-v4-flash`
（$30/月）或 `deepseek-v4.1-flash`（$60/月）。

### 3.3 哪些模型能识图（实测）

官方表格**只标注了模型 → 端点的对应关系，没有给任何模型的输入模态**，`/models` 也不返回多模态元数据
（只有 `id` / `object` / `created` / `owned_by` 四个字段）。因此只能实测。实测结论：

| 模型 | 能否识图 | 依据 |
|---|---|---|
| `glm-5.3-flash` | ✅ **能** | 发送一张纯红 PNG，返回「红色」，`reasoning_content` 为 `Image is red.`，输入 token 由纯文本的 17 涨到 40（图片确实进入了上下文） |
| `deepseek-v4-flash-vision-exp` | ✅ 能 | 模型名自带 vision，对照测试同样识别正确 |

**GLM-5.3-Flash 能识图这一点值得特别注意**：官方文档把它和其他纯文本模型混在一张表里，没有任何视觉标注；
Z.ai 自家的 GLM-5.3 文档更是明确写着"currently supports text-only inputs"，**与实测结论相反**
（可能因为 Flash 变体与旗舰版模态不同，也可能因为该文档未更新）。以实测为准。

> 若要用其他模型识图，先按本文第九节的实测方法自测一遍再接入 —— 官方不提供模态元数据，无法靠文档确认。

---

## 四、请求与响应格式

三个端点都是标准协议，请求体/响应体与 OpenAI、Anthropic 官方一致，**没有自定义字段**。
下面以本项目要用的 `/chat/completions` 为主。

### 4.1 OpenAI Chat Completions（本项目走这条）

```bash
curl https://opencode.ai/zen/go/v1/chat/completions \
  -H "Authorization: Bearer $OPENCODE_API_KEY" \
  -H "Content-Type: application/json" \
  -H "x-opencode-session: nexora-web-0001" \
  -H "User-Agent: nexora/1.0" \
  -d '{
    "model": "deepseek-v4-flash",
    "messages": [
      {"role": "system", "content": "你是 K12 人工智能通识课助教……"},
      {"role": "user", "content": "什么是机器学习？"}
    ],
    "stream": false
  }'
```

> `x-opencode-session` **不是可选项**：漏掉它网关直接返回
> `400 {"type":"error","error":{"type":"MissingSessionID",...}}`（实测，见第七节）。

响应为标准的 OpenAI Chat Completion JSON（`id` / `object` / `created` / `model` / `choices[]` / `usage`），
流式（`stream: true`）时返回标准 SSE，每块形如 `data: {...}`，以 `data: [DONE]` 结束。
（**待测**：本项目 WebSocket 流式对话依赖 SSE，拿到 Key 后需实测确认。）

### 4.2 Anthropic Messages（MiniMax / Qwen 系列必须走这个端点）

```bash
curl https://opencode.ai/zen/go/v1/messages \
  -H "x-api-key: $OPENCODE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "minimax-m3",
    "max_tokens": 1024,
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

注意：`max_tokens` 在 Anthropic 协议里是**必填**，`system` 是顶层字段而不是 messages 里的角色。

### 4.3 OpenAI Responses（Grok / GPT Luna / Muse Spark 走这个端点）

```bash
curl https://opencode.ai/zen/go/v1/responses \
  -H "Authorization: Bearer $OPENCODE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model": "grok-4.7", "input": "你好"}'
```

> 本项目只走 4.1（Spring AI OpenAI 兼容客户端）。4.2 / 4.3 记录在此，是为了以后要用
> MiniMax M3、Qwen3.8、Grok 这些便宜模型时知道该换哪个端点 —— **换端点意味着换协议，
> 需要另配一套客户端，不能只改模型名**。选型时优先考虑 4.1 端点内的模型。

---

## 五、客户端要求与会话头（官方）

官方对客户端的要求（原文）：

- 发送典型的编程 Agent 流量；
- 使用自身专属的 user agent 标识（例如 `my-coding-agent/1.0`），而不是通用的 SDK 或 HTTP 库名称；
- 为每段对话在 `x-opencode-session` 中发送稳定的会话 ID，以便官方优化路由和提示词缓存。

官方把 ZCode 列为已验证客户端，并说明「已不再需要发送这一特定请求头」。

> **实测更正（2026-09-29）**：官方页面把该头描述成"优化项"，但带 Key 实测发现它是**硬性要求**——
> 三个端点在不带 `x-opencode-session` 时一律返回
> `400 {"type":"error","error":{"type":"MissingSessionID","message":"Request is missing x-opencode-session and cannot be routed efficiently."}}`，
> 带上之后立即 200。官方那句"已不再需要发送"只适用于 OpenCode 自家客户端（它们已内置会话头），
> 自研客户端必须自己带。

**本项目做法（已落地）**：在 `OpenAiApi` 构造时用 `.headers(...)` 统一带上 `x-opencode-session`，
值为进程启动时生成的稳定会话 ID（`nexora-web-<12位随机>`，可用 `NEXORA_OPENCODE_GO_SESSION_ID` 覆盖）。
网关只要求该头存在且稳定，不要求每会话唯一；若以后要实现按会话粒度的路由/提示词缓存，
可用 `OpenAiChatOptions.httpHeaders(...)` 按请求覆盖，但**不要与 API 层同时设置**——
Spring AI 对两处头是 `addAll` 追加而非覆盖，同时设置会发两个同名头。

---

## 六、用量限额与计费（官方）

- **套餐**：Go `$10/月`；Go Plus `$40/月`（Go Plus 各模型限额更高）。两者 **token 单价相同，只有限额不同**。
- **限额规则**（原文）：「每个模型都有以下使用限制：5 小时 — 每月限制的 20%；每周 — 50%；每月 — 100%。」
  即「每月限制」是这张表的基准值，5 小时额度只有它的 1/5，突发流量很容易先撞 5 小时窗口。
- **超限后**：仍可继续使用免费模型（`space-bunny-free`、`longcat-2.5-preview-free`）；
  若 Zen 余额里有积分，可在控制台开启「使用余额（Use balance）」，超限后 Go 自动改走 Zen 余额而不拦截请求。
- **峰谷时段**（仅 DeepSeek 四个模型）：原文「Peak 时段为周一至周五的 01:00-04:00 和 06:00-10:00 UTC；
  其他所有时段（包括周末）均为 Off-Peak」。**换算北京时间 = 工作日 09:00-12:00 与 14:00-18:00 为 Peak**，
  恰好覆盖国内 K12 学生上课与写作业的高峰 —— 成本估算要按 Peak 价算（Peak 单价是 Off-Peak 的 2 倍）。
- **图片计费**：`DeepSeek V4 Flash Vision Exp` 的图片会按尺寸换算成 token，与文本 token 一并计入输入计费。
- 官方也提供「预估请求数」表，但那是按**编程 Agent 的 token 分布**（单次请求几十 K 缓存 token）估算的；
  本项目 K12 对话的分布完全不同（长系统提示 + RAG 上下文、短输出、无大规模缓存），
  该表**不能直接套用**，实际可支撑量需按本项目自己的 prompt 长度重算。

### 6.1 Go 方案（$10/月）价格与每月限额

单位：美元 / 每 1M tokens。`-` 表示该模型不支持缓存写入计费。

| 模型 | 输入 | 输出 | 缓存读取 | 缓存写入 | 每月限制 |
|---|---|---|---|---|---|
| GLM-5.3-Flash | $0.15 | $0.50 | $0.03 | - | $60 |
| GLM-5.3 | $1.40 | $4.40 | $0.26 | - | $15 |
| GLM-5.2 | $1.40 | $4.40 | $0.26 | - | $60 |
| Kimi K3 | $3.00 | $15.00 | $0.30 | - | $15 |
| Kimi K2.7 Code | $0.95 | $4.00 | $0.19 | - | $60 |
| Kimi K2.6 | $0.95 | $4.00 | $0.16 | - | $60 |
| LongCat-2.0 | $0.30 | $1.20 | $0.006 | - | $60 |
| LongCat 2.5 Preview Free | 免费 | 免费 | 免费 | - | 无限制 限时 |
| MiMo-V2.6-Flash | $0.14 | $0.28 | $0.0028 | - | $60 |
| MiMo-V2.6-Pro | $0.435 | $0.87 | $0.003625 | - | $15 |
| MiMo-V2.5 | $0.14 | $0.28 | $0.0028 | - | $60 |
| MiMo-V2.5-Pro | $0.435 | $0.87 | $0.003625 | - | $15 |
| MiniMax M3 | $0.30 | $1.20 | $0.06 | - | $60 |
| MiniMax M2.7 | $0.30 | $1.20 | $0.06 | $0.375 | $60 |
| Muse Spark 1.3 Contributor | $0.10 | $0.20 | $0.002 | - | $60 |
| Muse Spark 1.2 Contributor | $0.10 | $0.20 | $0.002 | - | $60 |
| Qwen3.8 Max | $2.00 | $6.00 | $0.25 | $2.50 | $15 |
| Qwen3.8 Flash | $0.15 | $0.47 | $0.016 | $0.20 | $30 |
| Qwen3.7 Plus (≤ 256K tokens) | $0.40 | $1.60 | $0.04 | $0.50 | $60 |
| Qwen3.7 Plus (> 256K tokens) | $1.20 | $4.80 | $0.12 | $1.50 | $60 |
| DeepSeek V4.1 Flash (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $60 |
| DeepSeek V4.1 Flash (Peak) | $0.30 | $1.20 | $0.006 | - | $60 |
| DeepSeek V4 Pro (Off-Peak) | $0.66 | $1.98 | $0.022 | - | $15 |
| DeepSeek V4 Pro (Peak) | $1.32 | $3.96 | $0.044 | - | $15 |
| DeepSeek V4 Flash (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $30 |
| DeepSeek V4 Flash (Peak) | $0.30 | $1.20 | $0.006 | - | $30 |
| DeepSeek V4 Flash Vision Exp (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $15 |
| DeepSeek V4 Flash Vision Exp (Peak) | $0.30 | $1.20 | $0.006 | - | $15 |
| Hy4 preview | $0.834 | $2.501 | $0.042 | - | $30 |
| Hy3 | $0.14 | $0.58 | $0.035 | - | $60 |
| Space Bunny Free | 免费 | 免费 | 免费 | - | 无限制 限时 |
| Grok 4.7 (≤ 200K tokens) | $2.00 | $6.00 | $0.50 | - | $15 |
| Grok 4.7 (> 200K tokens) | $4.00 | $12.00 | $1.00 | - | $15 |
| Grok 4.6 (≤ 200K tokens) | $2.00 | $6.00 | $0.50 | - | $15 |
| Grok 4.6 (> 200K tokens) | $4.00 | $12.00 | $1.00 | - | $15 |
| GPT 6 Luna (≤ 272K tokens) | $0.10 | $0.50 | $0.01 | $0.125 | $15 |
| GPT 6 Luna (> 272K tokens) | $0.20 | $0.75 | $0.02 | $0.25 | $15 |
| GPT 5.6 Luna (≤ 272K tokens) | $0.20 | $1.20 | $0.02 | $0.25 | $15 |
| GPT 5.6 Luna (> 272K tokens) | $0.40 | $1.80 | $0.04 | $0.50 | $15 |

### 6.2 Go Plus 方案（$40/月）价格与每月限额

单价与 Go 相同，仅「每月限制」不同（各模型提升幅度不一致，**不是统一的倍数**）：

| 模型 | 输入 | 输出 | 缓存读取 | 缓存写入 | 每月限制 |
|---|---|---|---|---|---|
| GLM-5.3-Flash | $0.15 | $0.50 | $0.03 | - | $180 |
| GLM-5.3 | $1.40 | $4.40 | $0.26 | - | $120 |
| GLM-5.2 | $1.40 | $4.40 | $0.26 | - | $180 |
| Kimi K3 | $3.00 | $15.00 | $0.30 | - | $60 |
| Kimi K2.7 Code | $0.95 | $4.00 | $0.19 | - | $180 |
| Kimi K2.6 | $0.95 | $4.00 | $0.16 | - | $240 |
| LongCat-2.0 | $0.30 | $1.20 | $0.006 | - | $240 |
| LongCat 2.5 Preview Free | 免费 | 免费 | 免费 | - | 无限制 限时 |
| MiMo-V2.6-Flash | $0.14 | $0.28 | $0.0028 | - | $120 |
| MiMo-V2.6-Pro | $0.435 | $0.87 | $0.003625 | - | $60 |
| MiMo-V2.5 | $0.14 | $0.28 | $0.0028 | - | $120 |
| MiMo-V2.5-Pro | $0.435 | $0.87 | $0.003625 | - | $60 |
| MiniMax M3 | $0.30 | $1.20 | $0.06 | - | $180 |
| MiniMax M2.7 | $0.30 | $1.20 | $0.06 | $0.375 | $240 |
| Muse Spark 1.3 Contributor | $0.10 | $0.20 | $0.002 | - | $120 |
| Muse Spark 1.2 Contributor | $0.10 | $0.20 | $0.002 | - | $120 |
| Qwen3.8 Max | $2.00 | $6.00 | $0.25 | $2.50 | $60 |
| Qwen3.8 Flash | $0.15 | $0.47 | $0.016 | $0.20 | $90 |
| Qwen3.7 Plus (≤ 256K tokens) | $0.40 | $1.60 | $0.04 | $0.50 | $180 |
| Qwen3.7 Plus (> 256K tokens) | $1.20 | $4.80 | $0.12 | $1.50 | $180 |
| DeepSeek V4.1 Flash (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $120 |
| DeepSeek V4.1 Flash (Peak) | $0.30 | $1.20 | $0.006 | - | $120 |
| DeepSeek V4 Pro (Off-Peak) | $0.66 | $1.98 | $0.022 | - | $60 |
| DeepSeek V4 Pro (Peak) | $1.32 | $3.96 | $0.044 | - | $60 |
| DeepSeek V4 Flash (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $120 |
| DeepSeek V4 Flash (Peak) | $0.30 | $1.20 | $0.006 | - | $120 |
| DeepSeek V4 Flash Vision Exp (Off-Peak) | $0.15 | $0.60 | $0.003 | - | $60 |
| DeepSeek V4 Flash Vision Exp (Peak) | $0.30 | $1.20 | $0.006 | - | $60 |
| Hy4 preview | $0.834 | $2.501 | $0.042 | - | $120 |
| Hy3 | $0.14 | $0.58 | $0.035 | - | $240 |
| Space Bunny Free | 免费 | 免费 | 免费 | - | 无限制 限时 |
| Grok 4.7 (≤ 200K tokens) | $2.00 | $6.00 | $0.50 | - | $60 |
| Grok 4.7 (> 200K tokens) | $4.00 | $12.00 | $1.00 | - | $60 |
| Grok 4.6 (≤ 200K tokens) | $2.00 | $6.00 | $0.50 | - | $60 |
| Grok 4.6 (> 200K tokens) | $4.00 | $12.00 | $1.00 | - | $60 |
| GPT 6 Luna (≤ 272K tokens) | $0.10 | $0.50 | $0.01 | $0.125 | $60 |
| GPT 6 Luna (> 272K tokens) | $0.20 | $0.75 | $0.02 | $0.25 | $60 |
| GPT 5.6 Luna (≤ 272K tokens) | $0.20 | $1.20 | $0.02 | $0.25 | $60 |
| GPT 5.6 Luna (> 272K tokens) | $0.40 | $1.80 | $0.04 | $0.50 | $60 |

### 6.3 隐私与数据留存（官方）

本项目涉及学生对话数据，这一栏需要留档：

| 模型 | 模型训练 | 数据留存 |
|---|---|---|
| Grok 4.7 | 不使用 | 30 天 |
| Grok 4.6 | 不使用 | 30 天 |
| GPT 6 Luna | 不使用 | 30 天 |
| GPT 5.6 Luna | 不使用 | 30 天 |
| GLM-5.3-Flash | 不使用 | 0 天 |
| GLM-5.3 | 不使用 | 0 天 |
| GLM-5.2 | 不使用 | 0 天 |
| Kimi K3 | 不使用 | 0 天 |
| Kimi K2.7 Code | 不使用 | 0 天 |
| Kimi K2.6 | 不使用 | 0 天 |
| LongCat-2.0 | 不使用 | 0 天 |
| MiMo-V2.6-Pro | 不使用 | 0 天 |
| MiMo-V2.6-Flash | 不使用 | 0 天 |
| MiMo-V2.5-Pro | 不使用 | 0 天 |
| MiMo-V2.5 | 不使用 | 0 天 |
| Qwen3.8 Max | 不使用 | 0 天 |
| Qwen3.8 Flash | 不使用 | 0 天 |
| Qwen3.7 Plus | 不使用 | 0 天 |
| MiniMax M3 | 不使用 | 0 天 |
| MiniMax M2.7 | 不使用 | 0 天 |
| Muse Spark 1.3 Contributor | 是 | 非 ZDR |
| Muse Spark 1.2 Contributor | 是 | 非 ZDR |
| DeepSeek V4.1 Flash | 不使用 | 0 天* |
| DeepSeek V4 Pro | 不使用 | 0 天* |
| DeepSeek V4 Flash | 不使用 | 0 天* |
| DeepSeek V4 Flash Vision Exp | 不使用 | 0 天* |
| Hy4 preview | 不使用 | 0 天 |
| Hy3 | 不使用 | 0 天 |
| Space Bunny Free | 不使用 | 0 天 |
| LongCat 2.5 Preview Free | 不使用 | 0 天 |

要点：除 Muse Spark Contributor 系列（**明确用于训练**，非 ZDR）外，其余模型均「不使用」训练数据；
GLM / Kimi / LongCat / MiMo / Qwen / MiniMax / Hy / Space Bunny 等为 **0 天留存**；
Grok 与 GPT Luna 系列为 30 天；DeepSeek 系列为 0 天留存，但 ZDR 协议每月续签（官方页面标注当前有效期至 2026-10-31）。
**选型建议：学生真实对话不要走 Muse Spark 系列。**

---

## 七、实测记录（2026-09-29）

### 7.1 无 Key 探测（鉴权矩阵与错误体）

| # | 探测请求 | 结果 |
|---|---|---|
| 1 | `GET https://opencode.ai/zen/go/v1/models`（不带任何鉴权头） | `200`，返回 43 个模型 |
| 2 | `GET https://opencode.ai/zen/go/v1/` | `404`（根路径没有内容，必须带具体端点） |
| 3 | `POST /chat/completions` + `Authorization: Bearer invalid-test-key` | `401`，`{"type":"error","error":{"type":"AuthError","message":"Invalid API key."}}` |
| 4 | `POST /chat/completions` + `x-api-key: invalid` | `401`，`message":"Missing API key."`（该头不被识别） |
| 5 | `POST /responses` + `Authorization: Bearer invalid` | `401`，`Invalid API key.` |
| 6 | `POST /messages` + `Authorization: Bearer invalid` | `401`，`Missing API key.`（该头不被识别） |
| 7 | `POST /messages` + `x-api-key: invalid`（不带 `anthropic-version`） | `401`，`Invalid API key.`（不带该版本头也能通过鉴权） |

**错误体格式**（实测，三个端点一致）：HTTP `401`，`Content-Type: text/plain;charset=UTF-8`，
响应体是 Anthropic 风格的错误信封：

```json
{"type":"error","error":{"type":"AuthError","message":"Invalid API key."}}
```

注意 `Content-Type` 是 `text/plain` 而不是 `application/json` —— 如果本项目后面要写基于 HTTP 客户端的
降级/重试逻辑，**不要用 Content-Type 判断响应是否可解析**，直接尝试 JSON 反序列化更稳。

### 7.2 带 Key 实测（2026-09-29，模型 `glm-5.3-flash`）

| # | 验证项 | 结果 |
|---|---|---|
| 1 | 不带 `x-opencode-session` | ❌ `400`，`{"type":"error","error":{"type":"MissingSessionID",...}}` —— 该头是硬性要求 |
| 2 | 带会话头 + 文本非流式 | ✅ `200`，返回「收到」，1.6s，`usage.prompt_tokens=17` |
| 3 | 带会话头 + `stream: true` | ✅ `200`，标准 SSE，`chat.completion.chunk` 分片，`delta.content` 逐块输出 |
| 4 | `glm-5.3-flash` 带图（纯红 PNG） | ✅ 返回「红色」，`reasoning_content` 为 `Image is red.`，`prompt_tokens` 17 → 40 |
| 5 | `deepseek-v4-flash-vision-exp` 带图（对照） | ✅ 识别正确 |
| 6 | `reasoning_effort: high` | ✅ 被接受（`200`），但**推理会占用输出 token**：`max_tokens=16` 时 16 个 token 全花在 `reasoning_content`、正文返回空且 `finish_reason=length` |
| 7 | Spring AI 客户端（项目实际用法，镜像 `OpenCodeGoChatProvider` 的装配） | ✅ 三项全通过：非流式 / 流式（`AgentChatComponent` 的 `.stream().chatResponse()` 路径）/ 带图（`Media` + data URL 构造方式） |

响应体附加字段：消息里多一个非标准的 `reasoning_content`（推理内容），Spring AI 会忽略，不影响解析。

**未验证**：限流 `429` 的错误体结构（需主动触发限额，成本上不划算）。

### 7.3 延迟实测与 reasoning_effort（重要：默认档位会让回答"卡住"）

`glm-5.3-flash` 是**强制思考模型**：传 `reasoning_effort: none` / `minimal` 会被网关直接拒绝
（HTTP 400 `[1210] This model always engages in thinking and cannot be disabled; please use low, high, or max`）；
而**不传该参数**则走网关默认档位（相当于 max），实测表现就是"长时间不出正文"：

| 档位 | 首正文 | 总耗时 | 思考字数 | 正文 | 场景 |
|---|---|---|---|---|---|
| 不传（走网关默认） | **32.93s** | 33.55s | — | 136 字 | Spring AI 生产路径（无 max_tokens） |
| 不传（走网关默认） | 3/3 次**无正文** | 12.7 / 15.9 / 15.9s | 1968~2349 字 | **0 字** | 直连网关，max_tokens=600 被思考耗尽，`finish_reason=length` |
| `low` | **1.15s / 0.61s** | 6.56s / 2.06s | 0 字 | 171 / 137 字 | Spring AI 生产路径（无 max_tokens） |
| `low`（5 次重复） | 1.0~1.6s | 2.0~4.6s | 0 字 | 135~231 字 | 5/5 稳定，无正文缺失 |
| `low`（带图 4 次） | 0.9~2.2s | — | — | 「红色」正确 | 降档不影响识图 |

对照：同一网关的 `deepseek-v4-flash` 做同一问题总耗时 **2.12s**（它也会思考，但只 106 字）。
网络不是瓶颈——两者首字节都在 1.1~1.8s，差异全部来自模型的思考长度。

**结论**：`NEXORA_OPENCODE_GO_REASONING_EFFORT` **必须显式设为 `low`**（配置文件已默认 low）。
留空 = 把档位交给网关默认 = 学生端要盯着空气泡等 30 秒以上，输出 token 预算紧张时甚至一个字的正文都拿不到。

**为什么观感比数字更糟**：`AgentChatComponent` 只把 `content` 推给前端，`reasoning_content` 不推送，
所以思考期间学生端界面上**完全没有任何动静**，看起来像"卡死了"。

---

## 八、本项目（Nexora）落地实现（2026-09-29 已实施）

原方案是"改 `spring.ai.openai.chat.base-url` 切到 Go"，但实测发现网关**强制要求 `x-opencode-session` 头**
（Spring AI 的自动装配客户端无法配置自定义头），因此改为按文生图 `ImageProvider` 的同一套模式，
在 `nexora-web` 内新增一层对话供应商体系，DeepSeek 原链路保持不变。

### 8.1 新增文件

对话供应商本体（`nexora-web`，不影响 admin / mcp 的运行）：

| 文件 | 作用 |
|---|---|
| `com/nexora/component/ChatProvider.java` | 供应商接口：`code / label / current / chatClient / textModel / visionModel / reasoningEffort(withImage)`；是否传该参数由供应商自己决定（DeepSeek 带图必须省略、GLM 带图必须传 low） |
| `com/nexora/component/DeepSeekChatProvider.java` | 默认供应商：复用 `spring.ai.openai.chat.*` 自动装配的 ChatClient，行为与改造前完全一致 |
| `com/nexora/component/OpenCodeGoChatProvider.java` | OpenCode Go 供应商：内部构造 `OpenAiApi` + `OpenAiChatModel`，默认模型 `glm-5.3-flash`，统一带会话头 |
| `com/nexora/component/ChatProviderRouter.java` | `@Primary` 路由，按 system_config 的 `AI_MODEL.chat_provider` 运行时选择，兜底 deepseek |

管理端切换入口（`nexora-admin`）：

| 文件 | 作用 |
|---|---|
| `admin/dto/ChatProviderSwitchDTO.java` | 切换入参（白名单编码） |
| `admin/vo/ChatProviderOptionsVO.java` | 当前生效值 + 可选项列表 |
| `SystemSettingBiz` 新增 | `chatProviderOptions()` / `switchChatProvider()`，读写 `AI_MODEL.chat_provider` |
| `SystemSettingController` 新增 | `GET/POST /systemSetting/chatProvider` |
| 前端 `views/system/EnvConfig.tsx` | 「对话模型供应商」切换卡片（与文生图那张共用抽出的 `ProviderSwitchCard` 组件） |

改动文件：`AgentChatComponent`（改为从路由取客户端与模型名）、`SystemConfigComponent`（新增 chat_provider 白名单）、
web 的 `application.yml` / `application-local.yml` 与 admin 的 `application.yml`（新增配置块）、
`AiChatConfig`（启动日志增加新 Key 的环境变量检查）。

> 管理端页面需要展示「对话 base-url / 对话 Key / 视觉模型」在学生端生效的那一套值，所以 admin 的
> `application.yml` 也配了同一组属性（含 `NEXORA_OPENCODE_GO_API_KEY` 的掩码读取），但 admin 自身
> **不装配**对话路由，不发起 OpenCode Go 调用。

两个刻意的实现取舍，避免踩坑：

1. **OpenCode Go 的模型不注册为 Spring Bean**。Spring AI 的 `OpenAiChatModel` 自动装配带
   `@ConditionalOnMissingBean`，把它暴露成 Bean 会顶掉自动装配的 DeepSeek 客户端，直接破坏默认链路。
   因此客户端在供应商内部懒构造，未配置 Key 时应用照常启动、只有真正发起对话才报错。
2. **基于自动装配模型拷贝构造**（`new OpenAiChatModel.Builder(现有模型).openAiApi(新Api)`），
   继承其工具调用管理器 / 重试模板 / 观测注册表，保证 MCP 知识页工具在 GLM 下照常可用。

### 8.2 如何切换到 GLM-5.3-Flash

**方式一（推荐，点一下即可）**：管理端 → 系统设置 → 环境配置 → 「对话模型供应商（切换立即生效）」卡片，
选中 `OpenCode Go（GLM-5.3-Flash）` 后保存。保存即写库、学生端下一次对话就走新供应商，**不需要重启**。
接口：`GET/POST /systemSetting/chatProvider`。

**方式二（直接改库，等价于方式一）**：往 `system_config` 表写一行即可运行时切换，该行由方式一的卡片维护。

```sql
-- config_group = AI_MODEL, config_key = chat_provider, config_value = opencode-go
-- 值只认 deepseek / opencode-go，非法值回落 deepseek；删除该行 = 跟随启动配置
```

**方式三（启动配置兜底）**：表里没有该行时，跟随启动配置。以下环境变量配在 **nexora-web** 上：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `NEXORA_OPENCODE_GO_API_KEY` | 空 | **必填**：OpenCode Go 的 API Key（`oc_sk_...`） |
| `NEXORA_CHAT_PROVIDER` | `deepseek` | 设为 `opencode-go` 启用 GLM |
| `NEXORA_OPENCODE_GO_MODEL` | `glm-5.3-flash` | 文本与视觉共用该模型 |
| `NEXORA_OPENCODE_GO_SESSION_ID` | 进程随机 | 网关强制的会话头；留空用启动时生成的稳定值 |
| `NEXORA_OPENCODE_GO_REASONING_EFFORT` | `low` | **不要留空**（留空 = 网关默认档位，正文要等 30 秒以上，见 §7.3）；可选 `low` / `high` / `max` |

### 8.3 注意事项

1. **Key 只走环境变量**，禁止写入代码或配置文件（根 AGENTS.md 硬性约束）。
2. **回滚零成本**：把 `chat_provider` 改回 `deepseek`（或删掉那行）即可，DeepSeek 链路未做任何改动。
3. **当前只覆盖主对话链路**：`AgentChatComponent` 走供应商路由，而意图识别、出题、绘本、动画、学习路径等
   辅助组件仍直接注入 `ChatClient`（即固定走 DeepSeek）。切换供应商后这些环节不会跟着切 —— 如需全覆盖，
   需把这些组件也改为注入 `ChatProvider`。
4. **5 小时窗口是最大风险**：限额规则是「5 小时 = 每月额度的 20%」。以 `deepseek-v4-flash` 为例，
   每月 $30 额度 → 5 小时窗口只有 $6。K12 场景是**明显的课上课下潮汐流量**，
   在 09:00-12:00 / 14:00-18:00（北京时间 Peak）很容易集中撞窗口，建议：
   接一个失败重试 + 降级到免费模型（`space-bunny-free` / `longcat-2.5-preview-free`）的兜底逻辑。
5. **不要用 Muse Spark 系列承载学生真实对话**（该系列用于训练，见 §6.3）。

---

## 九、遗留待办

已完成的验证见 §7.2；以下是本次没条件做、需要后续补的：

| # | 项 | 说明 |
|---|---|---|
| 1 | 应用内端到端回归 | 本次做到「编译通过 + 用项目 classpath 镜像生产装配路径的独立验证」。但本机 Redis(6379) / ES(9200) 未启动，无法完整启动 `nexora-web` 跑「学生端发消息 → Netty WS 推送」的链路回归，需在依赖齐备时补 |
| 2 | 限流 `429` 错误体结构 | 需主动触发限额，成本上不划算，暂缺 |
| 3 | 辅助 AI 组件覆盖 | 见 §8.3 第 3 条 |
| 4 | 每会话粒度的 session ID | 当前是进程级稳定值；若要做按会话路由/提示词缓存，改用 `OpenAiChatOptions.httpHeaders(...)` 按请求下发 |

> 本文档只含接口与配置说明，**不含任何 Key 明文**，可以正常入库。
