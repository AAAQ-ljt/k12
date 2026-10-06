import { Button } from 'antd';
import { useNavigate, useRouteError } from 'react-router-dom';
import { AlertTriangle, Home, RotateCcw } from 'lucide-react';
import styles from './AppErrorPage.module.scss';

/**
 * 路由级错误兜底页：替换 React Router 的默认开发者报错页（"意外申请错误 / Hey developer"）。
 *
 * 触发场景（2026-10-07）：第三方组件改动 React 管理的 DOM 后，React 卸载时抛
 * `NotFoundError: Failed to execute 'removeChild'…`。这类错误只能在错误边界兜住，
 * 兜住之后给用户可操作的出口（重新加载 / 回 AI 助教），而不是一页吓人的堆栈。
 */
export default function AppErrorPage() {
  const error = useRouteError();
  const navigate = useNavigate();
  const message = error instanceof Error ? error.message : String(error ?? '');

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <span className={styles.icon}>
          <AlertTriangle size={26} />
        </span>
        <h2>页面出了点小问题</h2>
        <p className={styles.tip}>
          刚才这一步没走通，你的学习记录和对话都还在。可以先回 AI 助教继续，或重新加载本页。
        </p>
        <div className={styles.actions}>
          <Button type="primary" icon={<Home size={15} />} onClick={() => navigate('/ai-tutor', { replace: true })}>
            回到 AI 助教
          </Button>
          <Button icon={<RotateCcw size={15} />} onClick={() => window.location.reload()}>
            重新加载
          </Button>
        </div>
        {message ? (
          <details className={styles.detail}>
            <summary>技术细节（给开发同学）</summary>
            <pre>{message}</pre>
          </details>
        ) : null}
      </div>
    </div>
  );
}
