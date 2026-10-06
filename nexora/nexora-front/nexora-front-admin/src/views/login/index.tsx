import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ConfigProvider, Form, Input, Button, App } from 'antd';
import type { ThemeConfig } from 'antd';
import { User, Lock } from 'lucide-react';
import { adminLogin } from '@/api/auth';
import type { LoginParams } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import VectorWordmark from './VectorWordmark';
import styles from '@/assets/styles/login.module.scss';

/**
 * 登录页局部主题：聚焦态电光蓝 + 10px 圆角，仅作用于本页组件，不影响内页的 antd 主色。
 * 登录按钮的深蓝→青蓝渐变在 login.module.scss 中单独覆盖。
 */
const LOGIN_THEME: ThemeConfig = {
  token: { colorPrimary: '#118aff', borderRadius: 10 },
};

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
    <div className={styles.loginPage}>
      <aside className={styles.brandPanel}>
        <VectorWordmark
          text="NEXORA"
          background="#030f24"
          textColor="#e8f2ff"
          shade="#12356b"
          accent="rgba(56, 157, 255, 0.45)"
          font={{
            fontFamily:
              "Inter, 'SF Pro Display', 'PingFang SC', 'Microsoft YaHei', system-ui, sans-serif",
            fontWeight: 800,
            fontSize: '200px',
            lineHeight: '1em',
            letterSpacing: '-0.02em',
            textAlign: 'left',
          }}
          style={{ position: 'absolute', inset: 0 }}
        />
        {/* 品牌信息叠加层：不响应指针，保证字标画布的指针交互不被遮挡 */}
        <div className={styles.brandOverlay}>
          <div className={styles.brandTop}>
            <div className={styles.brandMark}>N</div>
            <span className={styles.brandName}>Nexora</span>
          </div>
          <div className={styles.brandBottom}>
            <p className={styles.brandSlogan}>AI 教学知识平台</p>
            <p className={styles.brandCopyright}>
              © {new Date().getFullYear()} Nexora
            </p>
          </div>
        </div>
      </aside>
      <main className={styles.formPanel}>
        <ConfigProvider theme={LOGIN_THEME}>
          <div className={styles.formBox}>
            <div className={styles.formHeader}>
              <div className={styles.formMark}>N</div>
              <div>
                <h2 className={styles.formTitle}>欢迎回来，管理员</h2>
                <p className={styles.formSubtitle}>
                  请登录以进入多模态教学助手管理控制台
                </p>
              </div>
            </div>
            <Form<LoginParams> onFinish={handleLogin} size="large" autoComplete="off">
              <Form.Item
                name="username"
                rules={[
                  { required: true, message: '请输入管理员账号' },
                ]}
              >
                <Input prefix={<User size={16} color="var(--color-text-secondary)" />} placeholder="管理员账号" />
              </Form.Item>
              <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
              >
                <Input.Password
                  prefix={<Lock size={16} color="var(--color-text-secondary)" />}
                  placeholder="登录密码"
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
        </ConfigProvider>
      </main>
    </div>
  );
}
