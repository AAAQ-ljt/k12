import { useEffect, useState } from 'react';
import { Button } from 'antd';
import { useNavigate, useRouteError } from 'react-router-dom';
import { AlertTriangle, Home, RotateCcw } from 'lucide-react';
import { reportClientError } from '@/api/clientError';
import { useAuthStore } from '@/stores/auth';
import styles from './AppErrorPage.module.scss';

/**
 * 路由级错误兜底页：替换 React Router 的默认开发者报错页（"意外申请错误 / Hey developer"）。
 *
 * 触发场景（2026-10-07）：第三方组件改动 React 管理的 DOM 后，React 卸载时抛
 * `NotFoundError: Failed to execute 'removeChild'…`。这类错误只能在错误边界兜住，
 * 兜住之后给用户可操作的出口（重新加载 / 回 AI 助教），而不是一页吓人的堆栈。
 *
 * 2026-10-08 增补：把错误原文**上报到服务端**（此前只能在用户截图的"技术细节"里看到，
 * 线上排查全靠猜）；并默认展开技术细节，方便用户直接把原文贴给开发。
 */
export default function AppErrorPage() {
  const error = useRouteError();
  const navigate = useNavigate();
  const stage = useAuthStore((state) => state.userInfo?.stage);
  const [detailOpen, setDetailOpen] = useState(true);
  const message = error instanceof Error ? error.message : String(error ?? '');
  const stack = error instanceof Error ? error.stack || '' : '';

  // 只上报一次（StrictMode 双跑与重渲染都不会重复刷日志）
  useEffect(() => {
    if (!message) {
      return;
    }
    void reportClientError({
      message: stack ? `${message}\n${stack.slice(0, 900)}` : message,
      stage,
    }).catch(() => undefined);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [message]);

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <span className={styles.icon}>
          <AlertTriangle size={26} />
        </span>
        <h2>页面出了点小问题</h2>
        <p className={styles.tip}>
          刚才这一步没走通，你的学习记录和对话都还在。可以先回 AI 助教继续，或重新加载本页。
          <br />
          <span style={{ fontSize: 12 }}>
            这个问题已自动上报给开发同学；想帮忙更快定位的话，把下面的「技术细节」原文发给他们即可。
          </span>
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
          <details className={styles.detail} open={detailOpen} onToggle={(event) => setDetailOpen((event.target as HTMLDetailsElement).open)}>
            <summary>技术细节（给开发同学）</summary>
            <pre>{message}</pre>
          </details>
        ) : null}
      </div>
    </div>
  );
}
