import { useEffect, useState } from 'react';
import { Modal, Form, Input, Button, App } from 'antd';
import { useUiStore } from '@/stores/ui';
import { useAuthStore } from '@/stores/auth';
import { studentCheckCode, studentLogin, studentRegister } from '@/api/auth';
import type { LoginParams, RegisterParams } from '@/api/auth';
import styles from './LoginModal.module.scss';

type Mode = 'login' | 'register';

/** 全局登录 / 注册弹窗（Codex 模式） */
export default function LoginModal() {
  const open = useUiStore((state) => state.loginModalOpen);
  const closeLoginModal = useUiStore((state) => state.closeLoginModal);
  const setLoginData = useAuthStore((state) => state.setLoginData);
  const { message } = App.useApp();

  const [mode, setMode] = useState<Mode>('login');
  const [loading, setLoading] = useState(false);
  const [checkCodeKey, setCheckCodeKey] = useState('');
  const [checkCodeBase64, setCheckCodeBase64] = useState('');
  const [form] = Form.useForm();

  /** 拉取图形验证码 */
  const refreshCheckCode = async () => {
    try {
      const res = await studentCheckCode();
      setCheckCodeKey(res.checkCodeKey);
      setCheckCodeBase64(res.checkCodeBase64);
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  useEffect(() => {
    if (open) {
      form.resetFields();
      refreshCheckCode();
    }
  }, [open, form]);

  /** 切换登录 / 注册 */
  const switchMode = (next: Mode) => {
    setMode(next);
    form.resetFields();
    refreshCheckCode();
  };

  const handleLogin = async (values: Record<string, string>) => {
    setLoading(true);
    try {
      const params: LoginParams = {
        email: values.email,
        password: values.password,
        checkCodeKey,
        checkCode: values.checkCode,
      };
      const result = await studentLogin(params);
      setLoginData({ token: result.token, userInfo: result.userInfo });
      message.success('登录成功');
      closeLoginModal();
    } catch {
      refreshCheckCode();
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (values: Record<string, string>) => {
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    setLoading(true);
    try {
      const params: RegisterParams = {
        username: values.username,
        email: values.email,
        password: values.password,
        checkCodeKey,
        checkCode: values.checkCode,
      };
      // 注册即登录：后端直接返回 token + userInfo
      const result = await studentRegister(params);
      setLoginData({ token: result.token, userInfo: result.userInfo });
      message.success('注册成功，已自动登录');
      closeLoginModal();
    } catch {
      refreshCheckCode();
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      open={open}
      onCancel={closeLoginModal}
      footer={null}
      width={400}
      title={mode === 'login' ? '登录' : '注册'}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" onFinish={mode === 'login' ? handleLogin : handleRegister}>
        {mode === 'register' && (
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" maxLength={50} />
          </Form.Item>
        )}
        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '邮箱格式不正确' },
          ]}
        >
          <Input placeholder="请输入邮箱" maxLength={100} />
        </Form.Item>
        <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
          <Input.Password placeholder="请输入密码" maxLength={50} />
        </Form.Item>
        {mode === 'register' && (
          <Form.Item name="confirmPassword" label="确认密码" rules={[{ required: true, message: '请再次输入密码' }]}>
            <Input.Password placeholder="请再次输入密码" maxLength={50} />
          </Form.Item>
        )}
        <Form.Item name="checkCode" label="验证码" rules={[{ required: true, message: '请输入验证码' }]}>
          <Input
            placeholder="请输入验证码"
            maxLength={4}
            suffix={
              checkCodeBase64 ? (
                <img
                  src={checkCodeBase64}
                  alt="验证码"
                  title="点击刷新"
                  className={styles.checkCodeImg}
                  onClick={refreshCheckCode}
                />
              ) : null
            }
          />
        </Form.Item>
        <Button type="primary" htmlType="submit" block loading={loading}>
          {mode === 'login' ? '登录' : '注册'}
        </Button>
        <div className={styles.switchRow}>
          {mode === 'login' ? (
            <span>
              没有账号？<a onClick={() => switchMode('register')}>去注册</a>
            </span>
          ) : (
            <span>
              已有账号？<a onClick={() => switchMode('login')}>去登录</a>
            </span>
          )}
        </div>
      </Form>
    </Modal>
  );
}
