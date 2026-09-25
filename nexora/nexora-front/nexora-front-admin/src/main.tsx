import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ConfigProvider, App as AntApp, theme as antdTheme } from 'antd';
import type { ThemeConfig } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import App from '@/App';
import { useThemeStore } from '@/stores/theme';
import '@/assets/styles/global.scss';

/** 品牌令牌：浅色 / 暗色共用（颜色类差异由 algorithm + CSS 变量层负责） */
const BRAND_TOKENS: ThemeConfig['token'] = {
  colorPrimary: '#5b6ef5',
  colorSuccess: '#52c41a',
  colorWarning: '#faad14',
  colorError: '#ff4d4f',
  colorInfo: '#0891b2',
  borderRadius: 8,
  fontFamily:
    "'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Helvetica, Arial, sans-serif",
  fontSize: 14,
};

/**
 * 浅色下额外对齐设计令牌的几个中性色。
 * 暗色不能带这些值——写死的深色文字配暗色底会直接不可读，
 * 该交给 darkAlgorithm 推导（自定义样式由 tokens 的 [data-theme='dark'] 覆盖层负责）。
 */
const LIGHT_NEUTRALS: ThemeConfig['token'] = {
  colorText: '#1f1f2c',
  colorTextSecondary: '#8c8c99',
  colorBgLayout: '#f5f5f7',
  colorBorder: '#e8e8ef',
};

function Root() {
  const mode = useThemeStore((s) => s.mode);
  const isDark = mode === 'dark';

  const theme: ThemeConfig = {
    algorithm: isDark ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
    token: { ...BRAND_TOKENS, ...(isDark ? {} : LIGHT_NEUTRALS) },
  };

  return (
    <ConfigProvider locale={zhCN} theme={theme}>
      <AntApp>
        <App />
      </AntApp>
    </ConfigProvider>
  );
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
);
