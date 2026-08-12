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
      // Token 无效或过期，清除本地数据
      console.log('Token 验证失败，已清除本地登录信息');
      clear();
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
