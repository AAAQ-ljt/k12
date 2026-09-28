import { defineConfig } from 'astro/config';
import sitemap from '@astrojs/sitemap';

// 公网入口：阿里云 ECS 80 端口（frp 映射，见 F:/AIworker_program/school_frp_connnect/README.md）。
// 之后若绑定官网域名（如 www.liyekai.dpdns.org 过 Cloudflare），把这里与
// src/config/site.ts 的 origin、public/robots.txt 的 Sitemap 行一并换成 https 域名，
// sitemap 与各页 canonical/og:url 会自动跟随。
export default defineConfig({
  // 关闭 astro dev 自带的浏览器调试工具栏
  devToolbar: { enabled: false },
  site: 'http://121.40.149.155',
  integrations: [sitemap()],
  build: {
    // 关键 CSS 内联，首屏更快；其余仍走外部文件
    inlineStylesheets: 'auto',
  },
});
