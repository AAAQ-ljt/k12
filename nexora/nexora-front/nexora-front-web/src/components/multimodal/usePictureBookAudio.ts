import { useCallback, useEffect, useRef, useState } from 'react';
import { App } from 'antd';
import {
  generatePictureBookAudio,
  getPictureBookAudioTask,
  type PictureBookAudioTask,
} from '@/api/pictureBook';

/** 旁白任务快照（sessionStorage 持久化，切页不丢） */
interface AudioTaskSnapshot {
  taskId: string;
  status: string;
  message?: string;
}

const KEY_PREFIX = 'pb:audio:';

function readSnapshot(resourceId: string): AudioTaskSnapshot | null {
  try {
    const raw = sessionStorage.getItem(KEY_PREFIX + resourceId);
    return raw ? (JSON.parse(raw) as AudioTaskSnapshot) : null;
  } catch {
    return null;
  }
}

function writeSnapshot(resourceId: string, snap: AudioTaskSnapshot | null) {
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
 * 绘本旁白合成任务 hook：提交异步录制（整本或单页）→ 2s 轮询状态 → 完成/失败回调；
 * 任务快照存 sessionStorage，切到其他页面后重新挂载会自动恢复轮询（服务端任务继续跑），状态不丢。
 *
 * @param resourceId 当前绘本资源ID（未打开阅读时为空，恢复逻辑挂载后按需触发）
 * @param onCompleted 录制完成回调（入参 resourceId，由调用方刷新阅读脚本）
 */
export function usePictureBookAudio(
  resourceId: string | undefined,
  onCompleted: (resourceId: string) => Promise<void> | void,
) {
  const { message } = App.useApp();
  const [audioTask, setAudioTask] = useState<PictureBookAudioTask | null>(null);
  const resourceRef = useRef(resourceId);
  resourceRef.current = resourceId;

  const poll = useCallback(
    async (taskId: string) => {
      const rid = resourceRef.current;
      if (!rid) {
        return;
      }
      // eslint-disable-next-line no-constant-condition
      while (true) {
        await new Promise((resolve) => window.setTimeout(resolve, 2000));
        let task;
        try {
          task = await getPictureBookAudioTask(taskId);
        } catch {
          message.error('旁白任务状态查询失败，请稍后重试');
          writeSnapshot(rid, null);
          setAudioTask(null);
          return;
        }
        setAudioTask(task);
        if (task.status === 'COMPLETED') {
          writeSnapshot(rid, null);
          setAudioTask(null);
          message.success(task.message || '旁白录制完成');
          try {
            await onCompleted(rid);
          } catch {
            // 刷新失败不影响主流程
          }
          return;
        }
        if (task.status === 'FAILED') {
          writeSnapshot(rid, null);
          setAudioTask(null);
          message.error(task.message || '旁白录制失败，请稍后重试');
          return;
        }
        writeSnapshot(rid, { taskId, status: task.status, message: task.message });
      }
    },
    [message, onCompleted],
  );

  /**
   * 发起旁白录制：page 为空=整本补录，给定=单页（重）录制；voice 为空=学段默认
   */
  const generateAudio = useCallback(
    async (page: number | null, voice?: string) => {
      const rid = resourceRef.current;
      if (!rid || audioTask !== null) {
        return;
      }
      try {
        const task = await generatePictureBookAudio(rid, page, voice);
        setAudioTask(task);
        writeSnapshot(rid, { taskId: task.taskId, status: task.status, message: task.message });
        await poll(task.taskId);
      } catch {
        writeSnapshot(rid, null);
        setAudioTask(null);
        // 提交失败已统一提示（如语音服务未配置）
      }
    },
    [audioTask, poll],
  );

  /** 挂载恢复：存在 RUNNING 快照（切页回来）→ 恢复轮询；终态快照直接清理 */
  useEffect(() => {
    if (!resourceId) {
      return;
    }
    if (audioTask !== null) {
      // 切换阅读目标且当前资源没有 RUNNING 快照时，清掉上一个目标的任务占位（服务端任务仍在继续）
      if (!readSnapshot(resourceId)) {
        setAudioTask(null);
      }
      return;
    }
    const snap = readSnapshot(resourceId);
    if (snap && snap.status === 'RUNNING') {
      void poll(snap.taskId);
    } else if (snap) {
      writeSnapshot(resourceId, null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resourceId, audioTask, poll]);

  return { audioTask, generateAudio };
}
