# nexora-site · Nexora 产品官网

Astro 多页官网（纯静态）。视觉母题 = 极光暗色（夜空底 + 极光渐变 + 品牌紫），
主打特色为「会自己长大的 AI 知识库（Wiki）」。工程约束见 [AGENTS.md](./AGENTS.md)。

## 页面

| 路由 | 内容 |
|---|---|
| `/` | 首页（Hero + AI 助教打字机演示 + 数据条 + 能力概览 + Wiki 亮点 + 学段切换 + 多模态画廊） |
| `/wiki/` | ★ AI 个人知识库（主打车） |
| `/features/` | 产品能力总览 |
| `/stages/` | 学段适配（四学段切换交互） |
| `/experience/` | 多模态体验画廊 |
| `/tech/` | 技术架构（评委向） |
| `/faq/` | 常见问题 |
| `/about/` | 关于 |
| `/legal/terms/` `/legal/privacy/` | 用户协议 / 隐私政策（占位框架，待法务文本） |

## 命令

```bash
npm install       # 安装依赖
npm run dev       # 本地开发（默认 http://localhost:4321）
npm run build     # 构建 → dist/（纯静态）
npm run preview   # 本地预览构建产物
npm run check     # astro check 类型检查
```

## 上线前必须替换的占位符

集中在 **两处**：

1. `src/config/site.ts` —— `origin`、`studentUrl`、`adminUrl`、`icp`、`email`
2. `astro.config.mjs` 的 `site` 与 `public/robots.txt` 里的 Sitemap 行（三处域名保持一致）

另：`src/pages/legal/` 两页为占位框架，正式文本需法务审核后替换；
当前 Logo 为产品过渡方案，重构时替换 `public/favicon.svg` 与 `src/components/Logo.astro` 即可。

## 部署

`npm run build` 产物为 `dist/` 纯静态文件，拷到任意静态服务器即可（nginx 示例）：

```nginx
server {
    listen 80;
    server_name your-domain.com;        # 替换为官网域名
    root /var/www/nexora-site/dist;     # 构建产物目录
    location / { try_files $uri $uri/ /index.html; }
}
```

产品跳转：官网自身不部署任何后端；「进入产品」按钮指向 `site.ts` 里配置的
学生端 / 管理端对外地址（可用现有 frp 端口或独立域名）。

## 设计规范速查（详见 src/styles/tokens.css）

- 色彩：夜空底 `#070812`、极光渐变 `#667eea→#764ba2→#f093fb`、品牌紫 `#863bff`
- 字体：系统栈（PingFang SC / Microsoft YaHei 兜底），标题 1.18 行高、正文 1.78
- 图标：内联 SVG，统一描边宽 1.7、着色浅紫 `#c9b8ff`
- 圆角：chip 8px / 卡片 14px / 大卡 20px / 区块容器 30px
- 描边：常态 `rgba(255,255,255,.08)`、悬停 `.16`、品牌态 `rgba(157,107,255,.45)`
- 投影：四档（sm/md/lg/brand-glow），悬停另有极光色投影
