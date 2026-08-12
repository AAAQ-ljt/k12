import { useEffect } from 'react';
import type { ReactNode } from 'react';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * 受保护页面路由守卫（Codex 模式）：
 * 未登录访问时自动弹出登录弹窗，登录成功后自动渲染原页面
 */
export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = useAuthStore((state) => state.token);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  useEffect(() => {
    if (!token) {
      openLoginModal();
    }
  }, [token, openLoginModal]);

  if (!token) {
    return null;
  }
  return <>{children}</>;
}
