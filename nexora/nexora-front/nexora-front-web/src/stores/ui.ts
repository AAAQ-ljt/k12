import { create } from 'zustand';

interface UiState {
  /** 全局登录弹窗是否打开 */
  loginModalOpen: boolean;
  openLoginModal: () => void;
  closeLoginModal: () => void;
}

/** 全局 UI 状态（登录弹窗等） */
export const useUiStore = create<UiState>((set) => ({
  loginModalOpen: false,
  openLoginModal: () => set({ loginModalOpen: true }),
  closeLoginModal: () => set({ loginModalOpen: false }),
}));
