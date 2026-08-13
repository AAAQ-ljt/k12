# AI 环境变量配置

本项目所有 AI API Key 都通过环境变量注入，`application.yml` 只写占位引用，禁止把真实密钥提交到代码或文档。

## 密钥

| 用途 | 环境变量 | 获取位置 |
| --- | --- | --- |
| DeepSeek 对话 / 意图路由 / SVG 动画 / 编程辅助 | `NEXORA_DEEPSEEK_API_KEY` | DeepSeek 开放平台 |
| 知识向量化（qwen3.7-text-embedding） | `NEXORA_EMBEDDING_API_KEY` | 阿里云百炼 |
| 文生图 / 绘本插图（qwen-image-3.0） | `NEXORA_IMAGE_API_KEY` | 阿里云百炼 |

DeepSeek 和阿里云百炼是两个不同平台的密钥，不能混用。通义的向量模型和生图模型共用同一个阿里云百炼 API Key，因此也可以只配置：

| 统一变量 | 说明 |
| --- | --- |
| `NEXORA_DASHSCOPE_API_KEY` | 阿里云百炼 API Key，作为 embedding / image 的回退值 |

读取优先级：

- embedding：`NEXORA_EMBEDDING_API_KEY` > `NEXORA_DASHSCOPE_API_KEY`
- image：`NEXORA_IMAGE_API_KEY` > `NEXORA_DASHSCOPE_API_KEY`

## 模型与地址配置

| 变量名 | 默认值 | 说明 |
| --- | --- | --- |
| `NEXORA_CHAT_MODEL` | `deepseek-v4-flash` | DeepSeek 对话模型 |
| `NEXORA_CHAT_BASE_URL` | `https://api.deepseek.com` | DeepSeek 接口地址 |
| `NEXORA_CHAT_COMPLETIONS_PATH` | `/chat/completions` | DeepSeek 补全路径 |
| `NEXORA_EMBEDDING_MODEL` | `qwen3.7-text-embedding` | 百炼向量模型 |
| `NEXORA_EMBEDDING_BASE_URL` | `https://dashscope.aliyuncs.com/compatible-mode/` | 百炼 OpenAI 兼容地址 |
| `NEXORA_EMBEDDINGS_PATH` | `/v1/embeddings` | 向量接口路径 |
| `NEXORA_EMBEDDING_DIMENSIONS` | `1024` | 向量维度，与 ES 索引一致 |
| `NEXORA_IMAGE_PROVIDER` | `dashscope` | 文生图服务商，目前仅 dashscope |
| `NEXORA_IMAGE_MODEL` | `qwen-image-3.0` | 文生图模型 |
| `NEXORA_IMAGE_BASE_URL` | `https://dashscope.aliyuncs.com` | 文生图服务地址 |
| `NEXORA_IMAGE_PATH` | `/api/v1/services/aigc/multimodal-generation/generation` | 文生图接口路径 |

切换模型的示例：

- 免费额度用完换旗舰生图：设置 `NEXORA_IMAGE_MODEL=qwen-image-3.0-pro`
- 换成其他文生图服务：修改 `NEXORA_IMAGE_PROVIDER`、`NEXORA_IMAGE_BASE_URL`、`NEXORA_IMAGE_PATH` 和 `NEXORA_IMAGE_MODEL`

## 百炼业务空间地址

官方文档推荐使用业务空间专属域名，例如华北2（北京）：

```text
https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com
```

如果使用该地址：

- `NEXORA_EMBEDDING_BASE_URL=https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com/compatible-mode`
- `NEXORA_IMAGE_BASE_URL=https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com`

不配置时使用默认的 `dashscope.aliyuncs.com` 公共地址。

## Windows 本机配置

### 1. 当前终端临时生效（重启终端后失效）

```powershell
$env:NEXORA_DEEPSEEK_API_KEY = "sk-..."
$env:NEXORA_EMBEDDING_API_KEY = "sk-..."
$env:NEXORA_IMAGE_API_KEY = "sk-..."
```

### 2. 用户级永久配置（推荐）

```powershell
setx NEXORA_DEEPSEEK_API_KEY "sk-..."
setx NEXORA_EMBEDDING_API_KEY "sk-..."
setx NEXORA_IMAGE_API_KEY "sk-..."
```

`setx` 设置后只有新打开的终端 / IDE 才会读到，已经运行的进程不会自动更新。
已运行的 IntelliJ/IDEA 不会自动刷新：必须完全退出并重新打开，或直接在 `Run/Debug Configurations -> Environment variables` 中填写。只重启 Spring Boot 服务仍然会继承 IDEA 启动时的旧环境，导致 `401 Unauthorized`。

### 3. IDE 启动配置

IDEA：`Run / Debug Configurations` -> `Environment variables` 中填写，例如：

```text
NEXORA_DEEPSEEK_API_KEY=sk-...;NEXORA_EMBEDDING_API_KEY=sk-...;NEXORA_IMAGE_API_KEY=sk-...
```

VS Code：在 `.vscode/launch.json` 的 `env` 中添加同名变量。

## 验证

不要直接打印完整 Key，只验证长度即可：

```powershell
$env:NEXORA_DEEPSEEK_API_KEY.Length
$env:NEXORA_EMBEDDING_API_KEY.Length
$env:NEXORA_IMAGE_API_KEY.Length
```

重启后端后，如果日志中不再出现：

```text
401 Unauthorized from POST https://api.deepseek.com/chat/completions
```

说明 DeepSeek Key 配置成功。

启动日志也会输出环境变量自检：`AI 环境变量已生效: NEXORA_DEEPSEEK_API_KEY (sk-...xxxx)`；如果显示 `AI 环境变量缺失或未生效`，说明当前 JVM 没有读到该变量，请先完全重启 IDEA。

## 当前 yml 模型分工

| 模块 | 模型 | 服务 |
| --- | --- | --- |
| chat | deepseek-v4-flash | DeepSeek |
| embedding | qwen3.7-text-embedding | 阿里云百炼 |
| image | qwen-image-3.0 | 阿里云百炼 |

注意：qwen-image-3.0 使用百炼原生多模态生成接口，不走 OpenAI 的 `/v1/images/generations`，配置位于 `project.ai.image`，与 Spring AI 的 OpenAI 配置解耦。
