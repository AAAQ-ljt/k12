import { create } from 'zustand';
import type { UserInfo } from '@/types/common';
import { getToken, setToken, removeToken } from '@/utils/token';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  menuCodes: string[];
  setLoginData: (data: { token: string; userInfo: UserInfo }) => void;
  setMenuCodes: (codes: string[]) => void;
  clear: () => void;
  hasMenuCode: (code: string) => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: getToken(),
  userInfo: null,
  menuCodes: [],

  setLoginData: ({ token, userInfo }) => {
    setToken(token);
    set({ token, userInfo });
  },

  setMenuCodes: (codes) => set({ menuCodes: codes }),

  clear: () => {
    removeToken();
    set({ token: null, userInfo: null, menuCodes: [] });
  },

  hasMenuCode: (code) => get().menuCodes.includes(code),
}));
