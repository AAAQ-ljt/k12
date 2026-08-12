import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Form, Input, Button } from 'antd';
import { Mail, Lock, Sparkles } from 'lucide-react';
import { studentLogin } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import styles from '@/assets/styles/login.module.scss';

interface LoginFormValues {
  email: string;
  password: string;
}

export default function Login() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const setLoginData = useAuthStore((state) => state.setLoginData);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: LoginFormValues) => {
    setLoading(true);
    try {
      const result = await studentLogin(values);
      setLoginData({ token: result.token, userInfo: result.userInfo });
      const redirect = searchParams.get('redirect');
      navigate(redirect || '/ai-tutor', { replace: true });
    } catch {
      // 错误信息已由 request 拦截器统一弹出
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginPage}>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.logoIcon}>
            <Sparkles size={32} strokeWidth={2.2} />
          </span>
          <h1 className={styles.title}>Nexora AI 教学助手</h1>
          <p className={styles.subtitle}>你的 7×24 在线 AI 老师</p>
        </div>

        <Form<LoginFormValues>
          layout="vertical"
          onFinish={handleSubmit}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '邮箱格式不正确' },
            ]}
          >
            <Input
              prefix={<Mail size={18} strokeWidth={2} />}
              placeholder="请输入邮箱"
              autoComplete="email"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<Lock size={18} strokeWidth={2} />}
              placeholder="请输入密码"
              autoComplete="current-password"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className={styles.submitBtn}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
