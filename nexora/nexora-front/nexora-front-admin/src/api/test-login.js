import { request } from './request';

// 测试登录请求
const testLogin = async () => {
  try {
    // 方式 1: JSON (正确)
    const result = await request.post('/account/login', {
      username: 'admin',
      password: '123456'
    });
    console.log('✅ Login success:', result);
  } catch (error) {
    console.error('❌ Login failed:', error.response?.data || error.message);
  }
};

testLogin();
