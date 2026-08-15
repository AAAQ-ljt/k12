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
    label: '资源中心',
    icon: 'FolderOpen',
    path: '/resource',
    menuCode: 'resource',
  },
  {
    key: 'knowledge',
    label: '知识库管理（官方知识库）',
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
    ],
  },
  {
    key: 'learning',
    label: '学习分析',
    icon: 'BarChart3',
    path: '/learning',
    children: [
      { key: 'learning:overview', label: '学习分析', path: '/learning/overview', menuCode: 'learning:overview' },
    ],
  },
  {
    key: 'system',
    label: '系统设置',
    icon: 'Settings',
    path: '/system',
    children: [
      { key: 'system:admin', label: '管理员账号', path: '/system/admin', menuCode: 'system:admin' },
      { key: 'system:model', label: '模型与提示词', path: '/system/model', menuCode: 'system:model' },
      { key: 'system:config', label: '环境配置', path: '/system/config', menuCode: 'system:config' },
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
