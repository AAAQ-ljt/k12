import { create } from 'zustand';
import type { UserInfo } from '@/types/common';
import { getToken, setToken, removeToken } from '@/utils/token';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  isLoading: boolean;
  setLoginData: (data: { token: string; userInfo: UserInfo }) => void;
  setUserInfo: (userInfo: UserInfo) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: getToken(),
  userInfo: null,
  isLoading: true,
  setLoginData: ({ token, userInfo }) => {
    setToken(token);
    set({ token, userInfo, isLoading: false });
  },
  setUserInfo: (userInfo) => set({ userInfo, isLoading: false }),
  clear: () => {
    removeToken();
    set({ token: null, userInfo: null, isLoading: false });
  },
}));
