import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Spin } from 'antd';
import type { ReactNode } from 'react';
import { useAuthStore } from '@/stores/auth';
import { getAdminInfo } from '@/api/auth';

interface AuthGuardProps {
  children: ReactNode;
}

/**
 * 路由守卫：
 * - 无 token → 跳转登录页（携带 redirect）
 * - 有 token 但无 userInfo → 调用 getAdminInfo 拉取用户信息
 * - 有 token 且有 userInfo → 渲染 children
 */
export default function AuthGuard({ children }: AuthGuardProps) {
  const token = useAuthStore((s) => s.token);
  const userInfo = useAuthStore((s) => s.userInfo);
  const setLoginData = useAuthStore((s) => s.setLoginData);
  const clear = useAuthStore((s) => s.clear);
  const location = useLocation();
  const [loading, setLoading] = useState(!userInfo && !!token);

  useEffect(() => {
    if (token && !userInfo) {
      let cancelled = false;
      setLoading(true);
      getAdminInfo()
        .then((info) => {
          if (!cancelled) {
            setLoginData({ token, userInfo: info });
            setLoading(false);
          }
        })
        .catch(() => {
          if (!cancelled) {
            clear();
            setLoading(false);
          }
        });
      return () => {
        cancelled = true;
      };
    }
  }, [token, userInfo, setLoginData, clear]);

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (loading) {
    return (
      <div className="fullPageLoading">
        <Spin size="large" />
      </div>
    );
  }

  return <>{children}</>;
}
