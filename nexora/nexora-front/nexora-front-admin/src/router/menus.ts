export interface MenuConfig {
  key: string;
  label: string;
  icon?: string; // lucide-react 图标名
  path: string;
  menuCode?: string;
  children?: MenuConfig[];
}

export const menus: MenuConfig[] = [
  {
    key: 'dashboard',
    label: '工作台',
    icon: 'LayoutDashboard',
    path: '/dashboard',
  },
  {
    key: 'user',
    label: '用户管理',
    icon: 'Users',
    path: '/user',
    menuCode: 'user',
  },
  {
    key: 'resource',
    label: '资源管理',
    icon: 'FolderOpen',
    path: '/resource',
    menuCode: 'resource',
  },
  {
    key: 'knowledge',
    label: '知识库管理',
    icon: 'Database',
    path: '/knowledge',
    children: [
      { key: 'knowledge:overview', label: '知识总览', path: '/knowledge/overview', menuCode: 'knowledge:overview' },
      { key: 'knowledge:catalog', label: '知识目录', path: '/knowledge/catalog', menuCode: 'knowledge:catalog' },
      { key: 'knowledge:test', label: '问答测试', path: '/knowledge/test', menuCode: 'knowledge:test' },
    ],
  },
  {
    key: 'teaching',
    label: '教学业务',
    icon: 'GraduationCap',
    path: '/teaching',
    children: [
      { key: 'teaching:course', label: '课程管理', path: '/teaching/course', menuCode: 'teaching:course' },
      { key: 'teaching:question', label: '习题管理', path: '/teaching/question', menuCode: 'teaching:question' },
      { key: 'teaching:paper', label: '试卷管理', path: '/teaching/paper', menuCode: 'teaching:paper' },
      { key: 'teaching:exam', label: '考试管理', path: '/teaching/exam', menuCode: 'teaching:exam' },
    ],
  },
  {
    key: 'animation',
    label: '动画管理',
    icon: 'Film',
    path: '/animation',
    children: [
      { key: 'animation:template', label: '模板库', path: '/animation/template', menuCode: 'animation:template' },
      { key: 'animation:record', label: '生成记录', path: '/animation/record', menuCode: 'animation:record' },
      { key: 'animation:review', label: '动画审核', path: '/animation/review', menuCode: 'animation:review' },
    ],
  },
  {
    key: 'agent',
    label: '智能体配置',
    icon: 'Bot',
    path: '/agent',
    children: [
      { key: 'agent:prompt', label: '提示词管理', path: '/agent/prompt', menuCode: 'agent:prompt' },
      { key: 'agent:model', label: '模型与RAG配置', path: '/agent/model', menuCode: 'agent:model' },
      { key: 'agent:intent', label: '意图路由', path: '/agent/intent', menuCode: 'agent:intent' },
    ],
  },
  {
    key: 'learning',
    label: '学习分析',
    icon: 'BarChart3',
    path: '/learning',
    children: [
      { key: 'learning:path', label: '学习路径', path: '/learning/path', menuCode: 'learning:path' },
      { key: 'learning:mastery', label: '掌握度分析', path: '/learning/mastery', menuCode: 'learning:mastery' },
    ],
  },
  {
    key: 'system',
    label: '系统设置',
    icon: 'Settings',
    path: '/system',
    children: [
      { key: 'system:config', label: '系统配置', path: '/system/config', menuCode: 'system:config' },
      { key: 'system:menu', label: '菜单管理', path: '/system/menu', menuCode: 'system:menu' },
      { key: 'system:notice', label: '系统公告', path: '/system/notice', menuCode: 'system:notice' },
    ],
  },
];

/**
 * 根据当前路径查找匹配的一级菜单和二级菜单。
 * 优先匹配子菜单路径，再匹配一级菜单路径。
 */
export function getMenuByPath(
  pathname: string,
): { primary: MenuConfig; secondary?: MenuConfig } | null {
  for (const menu of menus) {
    // 优先匹配子菜单
    if (menu.children) {
      for (const child of menu.children) {
        if (pathname === child.path || pathname.startsWith(child.path + '/')) {
          return { primary: menu, secondary: child };
        }
      }
    }
    // 匹配一级菜单
    if (pathname === menu.path || pathname.startsWith(menu.path + '/')) {
      return { primary: menu };
    }
  }
  return null;
}
