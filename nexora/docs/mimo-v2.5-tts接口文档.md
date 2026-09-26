# MiMo-v2.5-TTS 接口文档与延迟实测

> 官方文档：https://mimo.mi.com/docs/zh-CN/api/audio/tts
> 实测日期：2026-09-26　实测机型/网络：本机开发机（上海，公网直连 `api.xiaomimimo.com`）
> 压测脚本：`nexora/tools/mimo-tts-latency/mimo_tts_latency.py`（**本地工具，不入库** —— `nexora/tools/` 已在仓库根 `.gitignore` 中排除，需在本机留存该目录才能复现）

本文分两部分：**一~五**整理官方接口说明（含官方未公开、由实测补全的音频参数），**六~八**是本次真实调用的延迟实测结论与集成建议。

---

## 一、基本信息

| 项 | 说明 |
|---|---|
| 接口地址 | `https://api.xiaomimimo.com/v1/chat/completions` |
| 请求方法 | `POST`，`Content-Type: application/json` |
| 鉴权方式 | 请求头 `api-key: <API_KEY>`；官方同时提供 `Authorization: Bearer <API_KEY>`（Bearer 鉴权） |
| 接口风格 | OpenAI Chat Completions 兼容（音频以 base64 放在 `choices[].message.audio.data`） |
| 模型 | `mimo-v2.5-tts`（预置音色）／`mimo-v2.5-tts-voicedesign`（音色设计）／`mimo-v2.5-tts-voiceclone`（音色克隆） |
| 流式 | 支持，`stream: true` 时走 SSE |

> 集成要求（项目规范）：API Key 必须走环境变量或外部化配置，禁止硬编码进代码。

---

## 二、请求参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `model` | string | 是 | 取值为三个模型之一 |
| `messages` | array | 是 | 见下方「messages 语义」 |
| `audio` | object | 否 | 音频参数，见下方「audio 对象」 |
| `stream` | boolean | 否 | 默认 `false`；`true` 返回 SSE 流 |

### messages 语义

TTS 复用了 Chat 协议的两个角色，含义与普通对话不同：

| role | content 含义 | 是否必填 |
|---|---|---|
| `user` | **语气 / 风格指令**（不是要读的内容） | `voicedesign` 模型必填；`mimo-v2.5-tts` 可省略 |
| `assistant` | **真正要合成的文本** | 必填（`voicedesign` 且 `optimize_text_preview=true` 时除外） |

也就是说，要读的话放在 `assistant` 里。实测只传 `assistant` 即可正常合成；传了 `user` 则作为语气控制生效。

### audio 对象

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `format` | string | `wav` | `wav` / `mp3` / `pcm` / `pcm16`；`stream: true` 时默认变为 `pcm` |
| `voice` | string | `mimo_default` | 预置音色 ID 或 base64 音频样本（音色克隆）。`voiceclone` 必填且仅接受 base64 的 mp3/wav |
| `optimize_text_preview` | boolean | `false` | 仅 `voicedesign` 使用 |

> `pcm` 与 `pcm16` 是同一件事，都表示 16bit PCM。`voicedesign` 不支持 `voice` 字段。

---

## 三、音色列表

`mimo-v2.5-tts` 可用音色（`voice` 省略时为 `mimo_default`）：

| 中文音色 | 英文音色 |
|---|---|
| `mimo_default`、`冰糖`、`茉莉`、`苏打`、`白桦` | `Mia`、`Chloe`、`Milo`、`Dean` |

共 9 个。**实测 9 个音色对延迟无显著影响**（见 6.2）。

---

## 四、响应格式

### 非流式（`stream: false`，默认）

标准 OpenAI Chat Completion JSON，音频为 base64 字符串：

```json
{
  "choices": [{
    "message": {
      "role": "assistant",
      "audio": { "data": "<BASE64 音频>" }
    }
  }]
}
```

取音频：`choices[0].message.audio.data` → base64 解码后直接落盘即为完整音频文件。

### 流式（`stream: true`）

SSE，逐块返回。每块的 `object` 为 `chat.completion.chunk`，音频增量在 `choices[].delta.audio.data`：

```
data: {"object":"chat.completion.chunk","choices":[{"delta":{"audio":{"data":"<BASE64 片段>"}}}]}
...
data: [DONE]
```

要点：

- 音频是**分片 base64**，需要逐块解码后按字节顺序拼接（不要先把字符串连起来再解码，各块可能单独补过 padding）。
- `choices[].finish_reason` 取值为 `stop` / `length` / `content_filter`。
- 各 chunk 共享同一 `id`、`created`、`model`。
- 收到 `data: [DONE]` 后**仍需把响应体读到 EOF**，否则 HTTP 长连接不会回到可复用状态（本脚本踩过这个坑，见第八节）。

官方文档未给出的细节（采样率、码率、限流），由本次实测补全于第五节。

---

## 五、实测补全：音频参数（官方文档未公开）

| 项 | 实测结果 |
|---|---|
| 采样率 | **24000 Hz** |
| 声道 | **单声道（1ch）** |
| 位深 | **16 bit**（PCM 有符号小端） |
| 比特率（裸流） | 384 kbps（24000 × 16 × 1） |
| 语速 | 约 **5 字/秒**（中文、默认语速；实测 199 字 → 约 39 秒音频） |

采样率的判定依据有三条，互相印证：

1. 非流式 `wav` 的 RIFF 头明确写着 `24000 Hz / 1ch / 16bit`。
2. 同一段文本请求 `mp3` 格式时，**服务端自己编码出的 mp3 也是 24000 Hz / 单声道**（`ffprobe` 复核），这是与服务端内部管线一致的独立证据。
3. 按 24 kHz 解释流式 PCM 时，中文语速约 5 字/秒，处于正常人声区间（若按 16 kHz 解释则为 3.4 字/秒，明显偏慢）。

音频内容特征（`ffprobe` + 波形统计复核）：有声帧占比 78–80%，峰值 0.63–0.76（有充足余量、无削顶），直流偏移可忽略，确认为正常语音而非噪声。

> ⚠️ **流式 wav 的头部长度字段是占位值**：即使 `audio.format=wav` 且 `stream=true`，返回体虽以 `RIFF` 开头，但头里 `data` 块长度固定写成 `7680`，与真实数据量无关。用一般 wav 解析库读它会得到 0.16 秒这种错误时长。**流式一律按裸 PCM 处理**（跳过 44 字节头，用实际字节数 ÷ 48000 得秒数）。

---

## 六、延迟实测

### 测试条件

| 项 | 值 |
|---|---|
| 模型 / 音色 | `mimo-v2.5-tts` / `mimo_default`（除 6.2） |
| 测速文本 | 短 12 字、中 56 字、长 199 字（中文，见脚本 `TEXT_PRESETS`） |
| 重复次数 | 每组 5 次（先预热 1 次，不计入统计） |
| 流式格式 | `pcm16`；非流式格式 `wav` |
| 测量口径 | 从「请求发出」到对应事件；`connect` 为 TCP+TLS 建连耗时，单列不计入 |

指标含义：**TTFB** = 首字节到达（服务端开始响应）；**TTFA** = 首个含音频数据的 SSE 分块到达，即"用户听到第一声"的等待；**RTF** = 总耗时 ÷ 音频时长，< 1 表示生成快于播放。

### 6.1 核心结论：文本长度 × 流式/非流式

| 模式 | 文本 | 字数 | 音频时长 | TTFA p50 / p90 (ms) | TTFB p50 (ms) | 总耗时 p50 / p90 / max (ms) | RTF p50 |
|---|---|---:|---:|---:|---:|---:|---:|
| 流式 | 短 | 12 | 2.56 s | **625** / 858 | 493 | 1114 / 1324 / 1324 | 0.43 |
| 流式 | 中 | 56 | 10.88 s | **593** / 721 | 536 | 2692 / 2860 / 2860 | 0.24 |
| 流式 | 长 | 199 | 37.12 s | **828** / 895 | 594 | 7926 / 8787 / 8787 | 0.21 |
| 非流式 | 短 | 12 | 2.24 s | —（无分块） | 1204 | 1331 / 1819 / 1819 | 0.62 |
| 非流式 | 中 | 56 | 11.36 s | — | 2800 | 3596 / 5228 / 5228 | 0.33 |
| 非流式 | 长 | 199 | 39.20 s | — | 8107 | 10930 / 12346 / 12346 | 0.28 |

**这条数据是本次实测最有价值的部分：**

1. **流式的 TTFA 基本恒定在 0.6–0.9 秒，与文本长度无关**（12 字 625 ms，199 字 828 ms）。流水线是「先出首块音频，边生成边推」，所以长文本不会让用户多等。
2. **非流式则线性劣化**：12 字首字节 1.2 s，199 字要等 **8.1 s** 才拿到第一个字节，整包 10.9 s 才结束。长文本下非流式完全不可用于对话。
3. **非流式的 TTFB 与总耗时之间有明显空档**（长文本 8.1 s vs 10.9 s，约 2.8 s），这段是 base64 编码与整包传输；流式没有这个空档。
4. 生成速度本身很快：RTF 0.21–0.43，即 1 秒能生成 2.3–4.8 秒音频，**瓶颈不在生成，而在等待与传输方式**。

### 6.2 音色对延迟的影响（流式，中文本，每组 3 次）

| 音色 | 音频时长 (s) | TTFA p50 (ms) | 总耗时 p50 (ms) | RTF p50 |
|---|---:|---:|---:|---:|
| `mimo_default` | 11.20 | 625 | 3036 | 0.27 |
| `冰糖` | 12.00 | 730 | 2947 | 0.25 |
| `茉莉` | 11.04 | 643 | 2836 | 0.26 |
| `苏打` | 11.36 | 602 | 2948 | 0.26 |
| `白桦` | 11.04 | 616 | 2816 | 0.24 |
| `Mia` | 11.84 | 694 | 3060 | 0.26 |
| `Chloe` | 11.68 | 632 | 2946 | 0.25 |
| `Milo` | 10.88 | 791 | 2887 | 0.26 |
| `Dean` | 10.24 | 660 | 2718 | 0.26 |

**结论：音色对延迟无实质影响**，TTFA 全部落在 602–791 ms 区间（波动属正常网络/推理抖动），可以按音色效果自由选型，不需要为延迟做取舍。音频时长差异（10.2–12.0 s）反映的是不同音色的语速差异。

### 6.3 连接复用（keep-alive）

| 场景 | connect 中位 (ms) | TTFA p50 (ms) | 总耗时 p50 (ms) |
|---|---:|---:|---:|
| 每请求新建连接 | 114 | 593 | 2692 |
| 复用长连接 | 0 | 684 | 3312 |

单次 TCP+TLS 建连约 **110–150 ms**。复用长连接后 TTFA 的差异（593 vs 684 ms）落在同组样本的正常抖动范围内，**说明建连开销相比模型推理（约 0.6 s）占比小，复用与否对首声延迟影响有限**；但在高并发下复用能显著减少握手与 TLS 握手次数，**生产环境仍应使用连接池**。

### 6.4 并发压力（流式，短文本，每档 2 轮）

| 并发数 | 成功/总数 | TTFA p50 (ms) | TTFA p90 (ms) | TTFA max (ms) | 总耗时 p50 (ms) | 墙钟 p50 (ms) | 相对单请求 |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 2/2 | 665 | 738 | 738 | 1092 | 1223 | 1.00× |
| 5 | 10/10 | 822 | 1177 | 1179 | 1253 | 1685 | 1.23× |
| 10 | 20/20 | 657 | 957 | 1244 | 1166 | 1836 | 0.99× |
| 20 | 40/40 | 1008 | 1432 | 1478 | 1522 | 2239 | **1.51×** |

**结论：到 20 路并发为止服务端没有出现排队雪崩、也没有触发限流（72 次请求 100% 成功）。** 并发 10 时 TTFA 与单请求持平，并发 20 时劣化到约 1.5 倍（1.0 s），仍远低于人类可接受的对话延迟阈值。需要注意这是短文本的结果，长文本下的并发上限未测。

### 6.5 音频体积（约 11 秒音频）

| 格式 | 体积 | 相对 wav |
|---|---:|---:|
| `wav`（非流式） | 约 522–545 KB | 1.0× |
| `pcm16`（流式裸流） | 约 499–568 KB | 约 1.0× |
| `mp3`（非流式） | 约 82–88 KB | **0.16×（小 6.4 倍）** |

流式走 `pcm16` 时没有压缩，带宽占用 384 kbps。若客户端带宽紧张、且不需要边收边播，`mp3` 非流式是更省流量的选择（体积降到 1/6）。

---

## 七、集成建议（本项目选型）

结合 K12 教学助手的场景（AI 老师"7×24 在线"、学生端要边听边播）：

1. **一律用流式 `stream: true` + `audio.format = pcm16`**。TTFA 稳定在 0.6–0.9 s，长回答也不会让用户干等；非流式在长文本下要等 8 秒以上首字节，不可接受。
2. **客户端按 24000 Hz / 16bit / 单声道** 播放流式 PCM。Web 端可用 `AudioContext({sampleRate: 24000})` + `AudioBuffer` 直接喂 PCM，或按 48 kHz 重采样；不要误按 16 kHz 解析，否则声音会变慢。
3. **WebSocket 推给前端时按 SSE 分块边界转发**，前端收到首块即可开播（首声约 0.6–0.9 s），不必等整句合成完。
4. **服务端使用 HTTP 连接池**（复用长连接），并注意流式响应必须读到 EOF 才算读完。
5. **音频缓存/回放存 `mp3`**，比 wav 省 6 倍存储与带宽。
6. **需要"温柔/严肃"等语气控制时，用 `user` 消息传风格指令**，要读的内容放 `assistant`。
7. **单实例可承受 20 路并发**（TTFA 约 1 s）；若要支撑整班同时朗读，建议按班级规模留出余量并做连接复用，长文本并发上限需另行压测。

---

## 八、已知坑与注意事项

1. **流式 `wav` 的头部长度是假的**（固定 7680 字节）。`stream: true` 时不要用 wav 解析库算时长，按裸 PCM 处理（第五节）。
2. **收到 `[DONE]` 后必须把响应体读完**再关闭/复用连接，否则 `http.client` 的自定义连接复用会直接抛 `Request-sent`。本脚本用「循环读到 EOF」修复。
3. **流式音频分块要逐块 base64 解码再拼字节**，各块可能各自补过 padding，先拼字符串再解码会错位。
4. **`pcm` 和 `pcm16` 是同义词**，不会返回不同格式。
5. **流式下 `format` 默认值是 `pcm` 而不是 `wav`**（非流式默认 `wav`），显式指定更稳妥。
6. 同一段文本每次生成的音频时长会有正常波动（实测 199 字在 35.0–40.8 s 之间），不是接口异常。
7. 官方文档未提供错误码表与限流说明，本次实测 72 次并发请求未触发限流。

---

## 九、调用示例

### curl（非流式）

```bash
curl --location --request POST 'https://api.xiaomimimo.com/v1/chat/completions' \
  --header "api-key: $MIMO_API_KEY" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "model": "mimo-v2.5-tts",
    "messages": [
      {"role": "user", "content": "用温柔亲切的语气，语速稍慢"},
      {"role": "assistant", "content": "同学们好，今天我们来认识人工智能。"}
    ],
    "audio": {"format": "wav", "voice": "冰糖"}
  }'
```

### curl（流式）

```bash
curl -N --location --request POST 'https://api.xiaomimimo.com/v1/chat/completions' \
  --header "api-key: $MIMO_API_KEY" \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "model": "mimo-v2.5-tts",
    "messages": [{"role": "assistant", "content": "同学们好。"}],
    "audio": {"format": "pcm16", "voice": "mimo_default"},
    "stream": true
  }'
```

### Python（流式，标准库）

```python
import base64, http.client, json, os

conn = http.client.HTTPSConnection("api.xiaomimimo.com", 443, timeout=180)
payload = {
    "model": "mimo-v2.5-tts",
    "messages": [{"role": "assistant", "content": "同学们好，今天我们来认识人工智能。"}],
    "audio": {"format": "pcm16", "voice": "mimo_default"},
    "stream": True,
}
conn.request("POST", "/v1/chat/completions", body=json.dumps(payload).encode(),
             headers={"api-key": os.environ["MIMO_API_KEY"],
                      "Content-Type": "application/json",
                      "Accept": "text/event-stream"})
resp = conn.getresponse()

pcm = bytearray()
while True:  # 读到 EOF，不要见到 [DONE] 就 break
    line = resp.readline()
    if not line:
        break
    line = line.decode("utf-8").strip()
    if not line.startswith("data:"):
        continue
    data = line[5:].strip()
    if data == "[DONE]":
        continue
    delta = (json.loads(data).get("choices") or [{}])[0].get("delta") or {}
    piece = (delta.get("audio") or {}).get("data")
    if piece:
        pcm += base64.b64decode(piece + "=" * (-len(piece) % 4))  # 逐块解码再拼字节

open("out.pcm", "wb").write(bytes(pcm))  # 24000Hz / 16bit / 单声道
```

---

## 十、压测脚本

脚本路径：`nexora/tools/mimo-tts-latency/mimo_tts_latency.py`（纯标准库，Python 3.9+，无需安装依赖）。

> ⚠️ `nexora/tools/` 是**本地工具目录，不入库**（已在仓库根 `.gitignore` 排除），仓库里没有这份脚本，需在本机留存或从作者处获取。

```bash
export MIMO_API_KEY=sk-xxx      # Windows cmd 用 set MIMO_API_KEY=sk-xxx

cd nexora/tools/mimo-tts-latency

# 复现本文 6.1：文本长度 × 流式/非流式
python mimo_tts_latency.py --mode both --presets short,medium,long --repeat 5 --out-dir out/bench1

# 复现 6.2：9 个音色对比
python mimo_tts_latency.py --mode stream --presets medium \
  --voice-list "mimo_default,冰糖,茉莉,苏打,白桦,Mia,Chloe,Milo,Dean" --repeat 3 --out-dir out/bench2

# 复现 6.4：并发梯度
python mimo_tts_latency.py --concurrency 1,5,10,20 --presets short --repeat 2 --out-dir out/bench5

# 换自己的文本与语气指令
python mimo_tts_latency.py --mode stream --repeat 3 \
  --text "同学们好，今天我们来认识人工智能。" \
  --tone "用温柔亲切的语气，语速稍慢"
```

输出：终端打印 Markdown 汇总表，同时在 `out/` 下留 CSV 明细与音频文件。详细参数见脚本 `--help`。
