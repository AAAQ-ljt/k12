import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Form, Input, Button, App } from 'antd';
import { User, Lock, Sparkles } from 'lucide-react';
import { adminLogin } from '@/api/auth';
import type { LoginParams } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import styles from '@/assets/styles/login.module.scss';

export default function Login() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const setLoginData = useAuthStore((s) => s.setLoginData);
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);

  const handleLogin = async (values: LoginParams) => {
    setLoading(true);
    try {
      const result = await adminLogin(values);
      setLoginData({ token: result.token, userInfo: result.userInfo });
      message.success('登录成功');
      const redirect = searchParams.get('redirect');
      navigate(redirect || '/dashboard', { replace: true });
    } catch {
      // 错误信息已由请求拦截器统一提示
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginCard}>
        <div className={styles.loginHeader}>
          <div className={styles.loginLogo}>
            <Sparkles size={24} />
          </div>
          <h1 className={styles.loginTitle}>Nexora AI 教学助手后台</h1>
        </div>
        <Form<LoginParams> onFinish={handleLogin} size="large" autoComplete="off">
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
            ]}
          >
            <Input prefix={<User size={16} color="var(--color-text-secondary)" />} placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<Lock size={16} color="var(--color-text-secondary)" />}
              placeholder="请输入密码"
            />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              className={styles.loginButton}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
