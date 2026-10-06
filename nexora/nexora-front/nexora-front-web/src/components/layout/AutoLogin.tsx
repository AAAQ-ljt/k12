import { useEffect, useCallback } from 'react';
import { useAuthStore } from '@/stores/auth';
import { getStudentInfo } from '@/api/auth';

interface AutoLoginProps {
  children: React.ReactNode;
}

/**
 * 自动登录守卫
 * 应用加载时自动尝试恢复登录状态
 */
export default function AutoLogin({ children }: AutoLoginProps) {
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const setUserInfo = useAuthStore((state) => state.setUserInfo);
  const clear = useAuthStore((state) => state.clear);

  const fetchUserInfo = useCallback(async () => {
    try {
      const info = await getStudentInfo();
      setUserInfo(info);
    } catch (error) {
      // 登录失效（401）已由 api/request.ts 的响应拦截器清理登录态（token 变为 null）；
      // 网络异常 / 服务不可用时只放弃本次补用户信息，保留 token，等后续请求或刷新再拉，避免把用户误踢出登录
      const status = (error as { response?: { status?: number } } | undefined)?.response?.status;
      if (status === 401 || !useAuthStore.getState().token) {
        clear();
      } else {
        console.warn('获取用户信息失败（非登录失效），保留本地登录态，稍后重试', error);
      }
    }
  }, [setUserInfo, clear]);

  useEffect(() => {
    // 如果有 token 但没有用户信息，获取用户信息
    if (token && !userInfo) {
      fetchUserInfo();
    }
  }, [token, userInfo, fetchUserInfo]);

  return <>{children}</>;
}
