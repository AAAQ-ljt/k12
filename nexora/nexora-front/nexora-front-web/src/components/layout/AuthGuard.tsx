import { type ReactNode, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuthStore } from '@/stores/auth';
import { getStudentInfo } from '@/api/auth';

interface AuthGuardProps {
  children: ReactNode;
}

/**
 * 登录守卫：校验 token，必要时拉取用户信息。
 * 学生端只校验登录，不校验菜单编码。
 */
export default function AuthGuard({ children }: AuthGuardProps) {
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const setUserInfo = useAuthStore((state) => state.setUserInfo);
  const clear = useAuthStore((state) => state.clear);

  useEffect(() => {
    if (token && !userInfo) {
      getStudentInfo()
        .then((info) => setUserInfo(info))
        .catch(() => clear());
    }
  }, [token, userInfo, setUserInfo, clear]);

  if (!token) {
    const redirect = window.location.pathname + window.location.search;
    return <Navigate to={`/login?redirect=${encodeURIComponent(redirect)}`} replace />;
  }

  if (!userInfo) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  return <>{children}</>;
}
