import { defineConfig } from 'astro/config';
import sitemap from '@astrojs/sitemap';

// site 为占位域名：上线前替换成官网真实域名（同时更新 public/robots.txt 里的 Sitemap 行），
// sitemap 与各页 canonical/og:url 会自动跟随。
export default defineConfig({
  // 关闭 astro dev 自带的浏览器调试工具栏
  devToolbar: { enabled: false },
  site: 'https://nexora-site.example.com',
  integrations: [sitemap()],
  build: {
    // 关键 CSS 内联，首屏更快；其余仍走外部文件
    inlineStylesheets: 'auto',
  },
});
