import { create } from 'zustand';
import { applyTheme, resolveInitialTheme, setStoredTheme } from '@/utils/theme';
import type { ThemeMode } from '@/utils/theme';

interface ThemeState {
  mode: ThemeMode;
  setMode: (mode: ThemeMode) => void;
  toggle: () => void;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  mode: resolveInitialTheme(),

  setMode: (mode) => {
    applyTheme(mode);
    setStoredTheme(mode);
    set({ mode });
  },

  toggle: () => get().setMode(get().mode === 'dark' ? 'light' : 'dark'),
}));

// 模块加载即应用一次：main.tsx 会在渲染前 import 本模块，
// 这样首屏拿到的就是正确主题，而不是默认浅色令牌。
applyTheme(useThemeStore.getState().mode);
