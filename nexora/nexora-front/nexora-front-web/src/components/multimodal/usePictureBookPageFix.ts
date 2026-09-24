import { useCallback, useEffect, useRef, useState } from 'react';
import { App } from 'antd';
import {
  getPictureBookPageFix,
  regeneratePictureBookPage,
} from '@/api/pictureBook';

/** 单页补画任务快照（sessionStorage 持久化，切页不丢） */
interface PageFixSnapshot {
  page: number;
  status: string;
  message?: string;
}

const KEY_PREFIX = 'pb:pagefix:';

function readSnapshot(resourceId: string): PageFixSnapshot | null {
  try {
    const raw = sessionStorage.getItem(KEY_PREFIX + resourceId);
    return raw ? (JSON.parse(raw) as PageFixSnapshot) : null;
  } catch {
    return null;
  }
}

function writeSnapshot(resourceId: string, snap: PageFixSnapshot | null) {
  try {
    if (snap) {
      sessionStorage.setItem(KEY_PREFIX + resourceId, JSON.stringify(snap));
    } else {
      sessionStorage.removeItem(KEY_PREFIX + resourceId);
    }
  } catch {
    // 存储失败不影响使用
  }
}

/**
 * 绘本单页补画任务 hook：提交异步补画 → 2s 轮询状态 → 完成/失败回调；
 * 任务快照存 sessionStorage，切到其他页面后重新挂载会自动恢复轮询（服务端 Redis 任务继续跑），状态不丢。
 *
 * @param resourceId 当前绘本资源ID（未打开阅读时为空，恢复逻辑挂载后按需触发）
 * @param onCompleted 补画完成回调（入参 resourceId，由调用方刷新阅读脚本）
 */
export function usePictureBookPageFix(
  resourceId: string | undefined,
  onCompleted: (resourceId: string) => Promise<void> | void,
) {
  const { message } = App.useApp();
  const [fixingPage, setFixingPage] = useState<number | null>(null);
  const resourceRef = useRef(resourceId);
  resourceRef.current = resourceId;

  const poll = useCallback(
    async (page: number) => {
      const rid = resourceRef.current;
      if (!rid) {
        return;
      }
      // eslint-disable-next-line no-constant-condition
      while (true) {
        await new Promise((resolve) => window.setTimeout(resolve, 2000));
        let fix;
        try {
          fix = await getPictureBookPageFix(rid, page);
        } catch {
          message.error('补画状态查询失败，请稍后重试');
          writeSnapshot(rid, null);
          setFixingPage(null);
          return;
        }
        if (fix.status === 'COMPLETED') {
          writeSnapshot(rid, null);
          setFixingPage(null);
          message.success(fix.message || '本页插图已补画完成');
          try {
            await onCompleted(rid);
          } catch {
            // 刷新失败不影响主流程
          }
          return;
        }
        if (fix.status === 'FAILED') {
          writeSnapshot(rid, null);
          setFixingPage(null);
          message.error(fix.message || '补画失败，请稍后重试');
          return;
        }
        writeSnapshot(rid, { page, status: fix.status, message: fix.message });
      }
    },
    [message, onCompleted],
  );

  const fixPage = useCallback(
    async (page: number) => {
      const rid = resourceRef.current;
      if (!rid || fixingPage !== null) {
        return;
      }
      setFixingPage(page);
      writeSnapshot(rid, { page, status: 'RUNNING' });
      try {
        await regeneratePictureBookPage(rid, page);
        await poll(page);
      } catch {
        writeSnapshot(rid, null);
        setFixingPage(null);
        message.error('补画请求失败，请稍后重试');
      }
    },
    [fixingPage, poll, message],
  );

  /** 挂载恢复：存在 RUNNING 快照（切页回来）→ 恢复该页补画轮询；终态快照直接清理 */
  useEffect(() => {
    if (!resourceId) {
      return;
    }
    if (fixingPage !== null) {
      // 切换阅读目标且当前资源没有 RUNNING 快照时，清掉上一个阅读目标的补画占位（任务仍在服务端继续）
      if (!readSnapshot(resourceId)) {
        setFixingPage(null);
      }
      return;
    }
    const snap = readSnapshot(resourceId);
    if (snap && snap.status === 'RUNNING') {
      setFixingPage(snap.page);
      void poll(snap.page);
    } else if (snap) {
      // 已完成/失败：结果已落库或已提示过，清理快照即可（重新拉取详情自然拿到新图）
      writeSnapshot(resourceId, null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resourceId, fixingPage, poll]);

  return { fixingPage, fixPage };
}