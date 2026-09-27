# Nexora 官网（nexora-site）工程约束

Astro 多页官网，**纯静态站点**，与 front-admin / front-web 完全独立、互不依赖。

# 定位与边界

- 只做产品展示与入口跳转：所有"进入产品"按钮跳转 `src/config/site.ts` 里的 `studentUrl` / `adminUrl`
- **禁止调用任何 `/api` 接口**：官网无后端依赖，禁止引入 axios/fetch 业务请求
- 禁止引入组件库（antd 等）与 CSS 框架（Tailwind 等）；交互用原生 TS script + 手写 CSS
- build 产物必须是纯静态 `dist/`，可直接挂 nginx；不部署任何 Node 服务

# 结构约定

- `src/config/site.ts`：对外信息唯一来源（域名/跳转地址/备案号/邮箱），**占位符集中在此**，上线时只改这一个文件与 `astro.config.mjs` 的 `site`
- `src/layouts/Base.astro`：全站唯一导航/页脚/SEO 骨架；新页面必须套用，不得自带页头页脚
- `src/styles/tokens.css`：设计令牌唯一来源（色彩/字体/图标/描边/圆角/投影），**页面禁止写死色值**
- `src/pages/`：文件即路由，目录式路由（`wiki/index.astro` → `/wiki/`）
- 交互组件（TypewriterDemo / StageSwitcher / WikiFlow 等）：数据内联在组件内或 JSON script，脚本为原生 TS

# 设计规范

- 视觉母题 = 极光暗色：夜空底 + `--gradient-aurora`（#667eea→#764ba2→#f093fb）+ 品牌紫 #863bff
- Logo 走 `components/Logo.astro` 组件（当前为过渡方案，重构 Logo 时只换该组件与 favicon.svg）
- 动效只用 CSS transition/animation + IntersectionObserver；仅 transform/opacity 走 GPU
- `prefers-reduced-motion` 必须降级；所有图标为内联 SVG，禁止外链图片与付费素材

# 文案约束

- 全部简体中文，取材自产品真实能力（以 `nexora/AGENTS.md`、`docs/产品说明书.md` 为准），禁止编造功能
- 每页必须有独立 title/description；发布前替换全部 `example.com` 占位域名与备案号
