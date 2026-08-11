---
name: nexora-front
description: Nexora 前端工程入口与跨页面话题（React 18 + TS + antd v5 + Zustand）：请求层封装、登录与权限态、WebSocket 工具、环境变量与代理、字段一致性原则、admin 与 web 两个工程的差异。TRIGGER when 涉及登录登出、请求 / 响应拦截器、axios 实例、路由守卫、菜单权限、WebSocket 封装、env 配置、proxy、字段对齐策略等"跨多个页面 / 工程级"话题。SKIP when 任务只是写单个页面——管理后台页面用 `react-admin-page`，学生端页面用 `react-web-page`。
---

# 工程定位

两个独立工程：

- nexora-front-admin：管理后台，服务管理员（内容运营）。token header 名 `adminToken`，代理目标为管理端后端服务（6061）。
- nexora-front-web：学生端，服务学生。token header 名 `studentToken`，代理目标为用户端后端服务（6060）；WebSocket 经 `/ws` 代理到 6062。

两个工程共享技术栈与目录约定，差异主要在 token header、角色权限、UI 走向。两个工程之间不共享代码。

# 关键模块

每个工程都应有以下几块"公共基建"：

- 请求封装：统一的 axios 实例 + 请求 / 响应拦截器（`src/utils/request.ts`）。
- token 工具：与 localStorage 同步的读 / 写 / 清三个函数。
- 登录 store：Zustand auth store，保存 token、用户信息、菜单树、菜单编码列表，暴露 `hasMenuCode`。
- 路由配置：react-router 路由表 + 守卫；管理端再多一个菜单配置文件。
- 消息 / 确认工具：对 antd `message` / `Modal.confirm` 的统一封装。
- 业务通用工具：日期格式化、文件大小格式化、字符串处理等。
- WebSocket 工具（学生端）：`src/utils/websocket.ts`，负责连接（携带 token）、消息分发、断线重连、取消与错误回调。

# 请求层规范

- baseURL 固定 `/api`；生产环境可由 `VITE_*` 环境变量拼接外部域名前缀。
- 超时默认 10 秒；通过环境变量可调。
- 请求拦截器：从登录态读 token，写入对应 header（管理端 / 用户端 header 名不同）。
- 响应拦截器：成功直接返回业务数据；登录失效清登录态 + 跳转登录页（带 redirect），业务页面不重复处理；其他业务错误默认弹错，调用方可关闭。
- 错误兜底：网络异常 / 超时统一弹"网络异常"。
- 列表返回兜底：api 文件里做归一化（总数 / 页码 / 页大小 / 列表数组）。

# WebSocket / 流式规范（学生端）

- 统一封装在 `src/utils/websocket.ts`：WebSocket 连接（携带 token）、按消息类型分发、回调增量文本、完成 / 错误 / 取消事件；发送消息走 HTTP 接口。
- AI 助教页只与封装交互，不直接写 fetch / WebSocket。
- 断线 / 取消必须通知后端（`cancelMessage`），并清理 UI 状态。

# 登录态规范

- 登录成功：调用 store 的登录 action → 后端返回 `{ token, userInfo, menuList }` → 写入 store + localStorage。
- 刷新页面恢复：路由守卫检测到有 token 但无 userInfo 时，调用 store 的"拉取登录信息"action。
- 登出：清 store + 清 localStorage + 跳转登录页。
- 登录失效与未登录跳转，只在响应拦截器里发生一次。

# 路由与权限

- 路由 `meta` 约定两个字段：是否需要登录（默认 true）、菜单编码（管理端）。
- 守卫流程：未登录跳登录页 → 已登录但无用户信息时拉取 → 校验菜单编码 → 通过则放行。
- 按钮级权限：通过登录 store 的 `hasMenuCode` 控制条件渲染，不自定义权限指令 / 组件。
- 学生端只校验登录，不校验菜单编码；另按 `userInfo.stage` 控制入口可见性（小学低年级隐藏编程环境）。

# 环境变量

每个工程在 `.env.development` / `.env.production` 中按需声明：后端域名前缀、接口 baseURL、请求超时、开发端口、开发代理目标、WebSocket 地址。

禁止把后端地址 / 端口硬编码进代码。

# 字段一致性原则

- 接口字段名严格沿用后端 PO / VO（驼峰），如 `courseId` / `lessonId` / `stage` / `knowledgePointId` / `messageId` / `assistantMessage`。
- 列表返回结构固定，api 层做兜底归一。
- 时间字段统一字符串 `yyyy-MM-dd HH:mm:ss`。
- 枚举值（学段、状态、意图类型等）保持后端定义，禁止前端自造同义词。

# 工程级开发流程

1. 确认任务对象：单页改造 / 新页面 → 转 `react-admin-page` 或 `react-web-page`；跨页面公共逻辑 → 留在本 skill。
2. 改请求层：先在 request 文件里调整，再回头检查所有调用方。
3. 改登录态：token 工具、登录 store、路由守卫三处必须同步。
4. 增菜单 / 路由：管理端在路由 + 菜单配置文件登记，同步在后端菜单表对齐编码。
5. 引入新依赖：先确认对端是否已有同类依赖；视频统一一个播放组件；富文本仅管理端使用。

# 自检清单（跨页面话题专属）

- 业务页面没有直接 import axios，全部走统一 request 实例。
- 没有在多个地方手写 token header。
- 没有在业务页面手动跳转登录页。
- 环境差异通过 `import.meta.env.VITE_*` 控制。
- 新增菜单时同步配 `meta.menuCode` 与后端菜单表。
- AI 流式只走统一 WebSocket 封装。
- admin 页面用项目封装的标准表格组件；web 页面用卡片 + 项目封装的视频播放组件。

# 与其他 skill 的分工

- 本 skill：跨页面 / 工程级（请求、登录、路由、env、WebSocket、字段约定）。
- `react-admin-page`：admin 单页面结构与三段式实现。
- `react-web-page`：web 单页面结构与学习 / AI / 多模态实现。
- 涉及具体页面时，本 skill 提供"环境与登录上下文"，单页面 skill 提供"页面结构"。
