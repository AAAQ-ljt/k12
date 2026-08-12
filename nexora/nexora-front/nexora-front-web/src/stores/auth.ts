import { create } from 'zustand';
import type { UserInfo } from '@/types/common';
import { getToken, setToken, removeToken } from '@/utils/token';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  setLoginData: (data: { token: string; userInfo: UserInfo }) => void;
  setUserInfo: (userInfo: UserInfo) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: getToken(),
  userInfo: null,
  setLoginData: ({ token, userInfo }) => {
    setToken(token);
    set({ token, userInfo });
  },
  setUserInfo: (userInfo) => set({ userInfo }),
  clear: () => {
    removeToken();
    set({ token: null, userInfo: null });
  },
}));
