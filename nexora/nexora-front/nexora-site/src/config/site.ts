/**
 * Nexora 官网全局配置 —— 全站唯一需要修改的对外信息
 * 页面与组件一律从这里取值，禁止在页面里硬编码链接/邮箱/备案号。
 */
export const SITE = {
  /** 官网对外地址：当前为阿里云 ECS 80 端口直入（frp 映射），绑定域名后换 https 域名
   * （与 astro.config.mjs 的 site 保持一致，替换后 sitemap/canonical 自动跟随） */
  origin: 'http://121.40.149.155',

  name: 'Nexora',
  /** 品牌释义（来自产品定义：Nexus 连接 + Aurora 极光） */
  brandLine: 'Nexus（连接）+ Aurora（极光）—— 连接学生与 AI 知识的桥梁',

  /** 学生端地址：官网所有「进入产品」按钮的跳转目标（已上线，Cloudflare 代理） */
  studentUrl: 'https://user.liyekai.dpdns.org',
  /** 管理端地址：页脚「管理端入口」（已上线，Cloudflare 代理） */
  adminUrl: 'https://admin.liyekai.dpdns.org',

  /** 联系邮箱（占位） */
  email: 'hello@nexora.example.com',

  /** 版权行（发布时手动更新年份） */
  copyright: '© 2026 Nexora',

  /** 站点默认 SEO 描述（各页可用自己的覆盖） */
  description:
      'Nexora 是面向 K12 的人工智能通识课教学助手：7×24 在线 AI 助教、会自己长大的个人知识库、'
      + '按学段自适应的个性化学习路径，以及 SVG 动画、AI 绘本、AI 绘画等多模态学习体验。',
};

/** 全站导航（含当前页高亮所需的 path；highlight = 导航星标项） */
export interface NavItem {
  label: string;
  href: string;
  highlight?: boolean;
}

export const NAV_ITEMS: NavItem[] = [
  { label: '产品能力', href: '/features/' },
  { label: 'AI 知识库', href: '/wiki/', highlight: true },
  { label: '学段适配', href: '/stages/' },
  { label: '多模态体验', href: '/experience/' },
  { label: '技术架构', href: '/tech/' },
  { label: '常见问题', href: '/faq/' },
  { label: '关于', href: '/about/' },
];
