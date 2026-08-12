import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ConfigProvider, App as AntApp } from 'antd';
import type { ThemeConfig } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import App from '@/App';
import '@/assets/styles/global.scss';

const theme: ThemeConfig = {
  token: {
    colorPrimary: '#5b6ef5',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#0891b2',
    colorText: '#1f1f2c',
    colorTextSecondary: '#8c8c99',
    colorBgLayout: '#f5f5f7',
    colorBorder: '#e8e8ef',
    borderRadius: 8,
    fontFamily: "'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Helvetica, Arial, sans-serif",
    fontSize: 14,
  },
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider locale={zhCN} theme={theme}>
      <AntApp>
        <App />
      </AntApp>
    </ConfigProvider>
  </StrictMode>,
);
