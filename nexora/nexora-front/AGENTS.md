# Nexora 前端规则（nexora-front）

React 18 + TypeScript + Vite + antd v5 + Zustand + React Router + Axios + SCSS（CSS Modules）。

两个独立工程：

- `nexora-front-admin`：管理后台，服务管理员（内容运营）。token header 名 `adminToken`，代理目标为管理端后端服务（6061）。
- `nexora-front-web`：学生端，服务学生。token header 名 `studentToken`，代理目标为用户端后端服务（6060）；WebSocket 经 `/ws` 代理到 6062。

> 两个工程互不共享代码。共用约定与组件由各自工程独立维护，不要相互 import。

# 编码规范

- 全部函数组件 + Hooks + TSX；接口请求 / 响应的 TS 类型与 api 文件同域维护（`src/api/<module>.ts` 内导出）。
- 接口请求统一封装到 `src/api/<module>.ts`，由统一的 axios 实例发出。禁止在页面 / 组件直接 import axios。
- 接口路径与后端模块名保持一致，以 `/<模块名>/<动作>` 形式调用（baseURL `/api` 由 axios 实例配置）。
- AI 流式消息走统一的 WebSocket 封装（`src/utils/websocket.ts`：连接携带 token、按消息类型分发、断线重连、取消 / 错误回调），禁止每个页面自己拼 WebSocket / fetch。

# 目录约定（两端通用）

- `src/api/`：业务模块按文件拆分，并维护统一的请求实例与类型出口。
- `src/views/`：页面，按业务子目录组织。
- `src/components/`：公共组件。管理端的项目级标准组件以 `Base` 前缀命名。
- `src/components/multimodal/`（学生端）：多模态组件集中地（SVG 动画播放器 / 绘本翻页器 / 答题卡片 / 代码编辑器封装）。
- `src/stores/`：Zustand store，登录态 + 业务 store（学生端含 AI 会话 store）。
- `src/router/`：路由配置与守卫；管理端单独维护菜单配置文件。
- `src/utils/`：请求封装、token 工具、消息 / 确认框工具、WebSocket 工具、业务工具函数。
- `src/assets/`：样式、图标、图片。

# 请求层约定

- axios 实例 baseURL 固定为 `/api`，生产环境可由环境变量拼接外部域名前缀。
- 请求拦截器：从登录态读取 token，写入对应 header（管理端 / 用户端 header 名不同）。
- 响应拦截器：
  - 成功响应直接返回业务数据；
  - 登录失效响应码统一清登录态并跳转登录页（携带 `redirect`），业务页面不要重复处理；
  - 其他业务错误默认 `message.error` 弹错，调用方可通过参数关闭；
  - 网络异常 / 超时统一弹"网络异常"。
- 列表返回字段固定为统一结构（总数、页码、页大小、列表数组）；在 api 文件做兜底归一。

# 路由与权限

- 路由 `meta` 约定：是否需要登录（默认 true）、菜单编码（管理端）。
- 守卫流程：未登录跳登录页 → 已登录但无用户信息时拉取 → 校验菜单编码 → 通过则放行（用守卫组件 / loader 实现，不在页面里写跳转逻辑）。
- 按钮级权限：通过登录 store 暴露的 `hasMenuCode` 方法控制条件渲染，不自定义权限指令 / 组件。
- 学生端只校验登录，不校验菜单编码；另按 `userInfo.stage` 控制功能可见性（小学低年级不展示编程环境入口）。

# 状态管理

- 登录 store 负责：token、用户信息、菜单树、菜单编码列表。
- 学生端 AI 会话 store 负责：当前会话、消息列表、流式输出状态机（streaming / done / error / cancelled）、工具调用卡片数据。
- token 通过工具函数与 localStorage 同步读写；不引入持久化插件。

# UI 与样式

- antd v5；主题定制集中在 `ConfigProvider` theme token，不散落覆盖。
- 自定义样式统一 CSS Modules + SCSS；禁止内联样式堆叠。
- 学生端使用"极光"主题：多彩渐变主色、圆角卡片、大字号、图标化引导，适配低龄学生。
- 学段自适应 UI：按 `userInfo.stage` 切换布局密度与入口可见性——小低大按钮 / 图形化 / 语音朗读；高中信息密度高、代码优先。
- 全部中文界面，禁止生产页面残留英文 placeholder / label。

## 管理后台 UI 约束（强制）

- 页面三段式：搜索区 → 表格 → 分页。
- 列表必须分页；表格使用项目封装的标准表格组件（封装 antd `Table` + 分页 + 选中 + 排序）。
- 新增 / 编辑用 `Modal` 弹窗，且抽成独立组件文件，与列表页解耦。
- 删除必须二次确认（`Popconfirm` / `Modal.confirm`）。
- 操作列固定为查看 / 编辑 / 删除，按钮显隐由权限控制。
- 状态用 `Tag` 展示。
- 下拉筛选选择后立即触发搜索。

## 学生端 UI 约束

- 面向学习场景，使用卡片 / 列表 / 图表 / 对话等布局；禁止套用后台表格 UI。
- 页面结构固定 5 页：AI 助教（首页）/ 个性化学习路径 / 课程教材 / 编程环境 / 我的；练习以对话内答题卡片 + 路径节点快测承载，不单设页面。
- 视频统一走项目封装的视频播放组件（ArtPlayer + HLS + 播放进度续播），不要自己引入第三方播放器。
- AI 助教页：聊天气泡 + 流式打字效果 + react-markdown 渲染（代码高亮）+ 工具调用卡片（推荐材料 / 生成练习 / 记录进度）+ 取消 / 重发。
- 多模态组件：绘本（图文翻页）、SVG 动画（分步播放 + 下载）、AI 绘画（文生图创作与展示）、编程（Monaco 编辑 + Pyodide 运行）、答题卡片（即时反馈）。
- PC 优先；如确需移动端兼容，按需加媒体查询。

# 环境变量

- 后端域名、代理目标、超时时间、开发端口、WebSocket 地址等差异化配置走 `VITE_*` 环境变量（`import.meta.env.VITE_*` 读取）；禁止硬编码到代码里。

# 字段一致性原则

- 接口字段名严格沿用后端 PO / VO（驼峰），如 `courseId` / `lessonId` / `stage` / `knowledgePointId` / `messageId`。
- 列表返回结构固定（总数 / 页码 / 页大小 / 列表数组），api 层做兜底归一。
- 时间字段统一字符串 `yyyy-MM-dd HH:mm:ss`。
- 枚举值（学段、状态、意图类型等）保持后端定义，禁止前端自造同义词。

# 禁止行为

- 禁止页面 / 组件直接 import axios，必须走统一的 request 实例。
- 禁止硬编码后端地址或绝对 URL。
- 禁止把接口请求逻辑写在 `views/` 里（必须放 `api/`）。
- 禁止单文件组件过大（推荐 500 行内）；超过则拆子组件 / 抽自定义 hook。
- 禁止内联样式，统一用 CSS Modules + SCSS。
- 禁止学生端使用后台表格 UI；禁止管理后台引入移动端响应式方案。
- 禁止改动字段命名映射，必须与后端保持一致。
- 禁止把 WebSocket / 流式逻辑散落在各页面（统一走 `src/utils/websocket.ts`）。
