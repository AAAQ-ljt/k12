import { useEffect } from 'react';
import type { ReactNode } from 'react';
import { Button } from 'antd';
import { Lock } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import styles from '@/assets/styles/utilities.module.scss';

interface ProtectedRouteProps {
  children: ReactNode;
  title: string;
  description: string;
}

/**
 * 受保护页面路由守卫（Codex 模式）：
 * 未登录访问时自动弹出登录弹窗，同时展示可复用的登录引导页；
 * 登录成功后自动渲染原页面
 */
export default function ProtectedRoute({ children, title, description }: ProtectedRouteProps) {
  const token = useAuthStore((state) => state.token);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  useEffect(() => {
    if (!token) {
      openLoginModal();
    }
  }, [token, openLoginModal]);

  if (!token) {
    return (
      <div className={styles.pagePlaceholder}>
        <h2>{title}</h2>
        <p>{description}</p>
        <Button type="primary" icon={<Lock size={16} />} onClick={openLoginModal} style={{ marginTop: 12 }}>
          登录后查看
        </Button>
      </div>
    );
  }
  return <>{children}</>;
}
