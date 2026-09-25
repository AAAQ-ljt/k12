/** 主题模式：浅色 / 暗色 */
export type ThemeMode = 'light' | 'dark';

const THEME_KEY = 'nexoraAdminTheme';

/** 读取本地保存的主题；没存过返回 null（交给系统偏好决定） */
export function getStoredTheme(): ThemeMode | null {
  const value = localStorage.getItem(THEME_KEY);
  return value === 'light' || value === 'dark' ? value : null;
}

export function setStoredTheme(mode: ThemeMode): void {
  localStorage.setItem(THEME_KEY, mode);
}

/** 系统偏好（首次访问时的默认值） */
export function getSystemTheme(): ThemeMode {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

/** 初始主题：本地存储优先，其次跟随系统 */
export function resolveInitialTheme(): ThemeMode {
  return getStoredTheme() ?? getSystemTheme();
}

/**
 * 把主题写到 <html> 上：
 * - data-theme 驱动 tokens 里的 CSS 变量覆盖（自定义样式的深色适配全靠它）
 * - color-scheme 让浏览器原生控件、滚动条、表单跟随（antd 暗色组件也依赖它）
 */
export function applyTheme(mode: ThemeMode): void {
  const root = document.documentElement;
  root.setAttribute('data-theme', mode);
  root.style.colorScheme = mode;
}
