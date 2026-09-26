import { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Modal, Progress, Tag } from 'antd';
import { BookImage, ExternalLink } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  getPictureBookInfo,
  getPictureBookTask,
  parsePictureBook,
  type PictureBookScript,
  type PictureBookTask,
} from '@/api/pictureBook';
import PictureBookReader from './PictureBookReader';
import { usePictureBookPageFix } from './usePictureBookPageFix';
import { usePictureBookAudio } from './usePictureBookAudio';
import styles from './PictureBookChatCard.module.scss';

/** 各任务阶段默认文案（后端 message 优先） */
const STATUS_TEXT: Record<string, string> = {
  PENDING: '任务排队中...',
  STORY_GENERATING: 'AI 正在编写故事...',
  STORY_DONE: '故事完成，准备绘制插图...',
  IMAGE_GENERATING: '正在绘制插图',
  AUDIO_GENERATING: '正在录制旁白',
};

/**
 * 对话内绘本卡片：按 taskId 每 2 秒轮询生成任务（与「绘本生成」页共用状态机），
 * 生成中展示进度，完成后拉取绘本脚本并提供「查看绘本」弹窗阅读；
 * 切页组件卸载任务仍在服务端继续，回到对话历史消息时重新挂载并恢复轮询，状态不丢。
 */
export default function PictureBookChatCard({ taskId, topic }: { taskId: string; topic?: string }) {
  const navigate = useNavigate();
  const [task, setTask] = useState<PictureBookTask | null>(null);
  const [script, setScript] = useState<PictureBookScript | null>(null);
  const [reading, setReading] = useState(false);
  /** 任务不存在/已过期（Redis TTL 2h）：结束轮询并展示友好兜底 */
  const [expired, setExpired] = useState(false);
  const settledRef = useRef(false);

  /** 单页补画完成：刷新阅读脚本（任务状态保持见 usePictureBookPageFix，切页可恢复） */
  const handleFixCompleted = useCallback(async (resourceId: string) => {
    try {
      const info = await getPictureBookInfo(resourceId);
      const fresh = parsePictureBook(info?.extJson);
      if (fresh) {
        setScript(fresh);
      }
    } catch {
      // 刷新失败不影响主流程
    }
  }, []);

  /** 旁白录制完成：刷新阅读脚本（新音频落库后阅读器自动重新加载） */
  const handleAudioCompleted = useCallback(async (resourceId: string) => {
    try {
      const info = await getPictureBookInfo(resourceId);
      const fresh = parsePictureBook(info?.extJson);
      if (fresh) {
        setScript(fresh);
      }
    } catch {
      // 刷新失败不影响主流程
    }
  }, []);

  const { fixingPage, fixPage } = usePictureBookPageFix(task?.bookResourceId, handleFixCompleted);
  const { audioTask, generateAudio } = usePictureBookAudio(task?.bookResourceId, handleAudioCompleted);

  const status = task?.status;
  const terminal = status === 'COMPLETED' || status === 'FAILED' || expired;
  const percent =
    task && task.total > 0 ? Math.min(100, Math.round((task.current / task.total) * 100)) : 0;

  useEffect(() => {
    if (terminal) {
      return;
    }
    const timer = window.setTimeout(async () => {
      try {
        const snapshot = await getPictureBookTask(taskId);
        setTask(snapshot);
        if (snapshot.status === 'COMPLETED') {
          settledRef.current = true;
          if (snapshot.bookResourceId) {
            try {
              const info = await getPictureBookInfo(snapshot.bookResourceId);
              const parsed = parsePictureBook(info?.extJson);
              if (parsed) {
                setScript(parsed);
              }
            } catch {
              // 详情拉取失败：卡片降级为「去绘本页查看」，不中断展示
            }
          }
        }
      } catch {
        // 任务不存在或已过期
        settledRef.current = true;
        setExpired(true);
      }
    }, 2000);
    return () => window.clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, expired, taskId]);

  const statusText =
    task?.message ||
    (status && STATUS_TEXT[status]) ||
    '绘本生成中...';

  const canRead = status === 'COMPLETED' && !!task?.bookResourceId && !!script;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <BookImage size={16} className={styles.icon} />
        <span className={styles.title}>
          {task?.title ? `绘本《${task.title}》` : '绘本创作'}
        </span>
        {status === 'COMPLETED' ? (
          <Tag color="success">已完成</Tag>
        ) : status === 'FAILED' ? (
          <Tag color="error">生成失败</Tag>
        ) : expired ? (
          <Tag>已过期</Tag>
        ) : (
          <Tag color="processing">生成中</Tag>
        )}
      </div>

      {topic ? <div className={styles.topic}>主题：{topic}</div> : null}

      {!terminal ? (
        <div className={styles.progress}>
          <Progress percent={percent} size="small" status="active" />
          <div className={styles.progressText}>
            {statusText}
            {task && task.total > 0 ? `（${task.current}/${task.total} 页）` : ''}
          </div>
          <div className={styles.hint}>切到其他页面也会继续生成，回到对话可恢复进度。</div>
        </div>
      ) : null}

      {status === 'FAILED' ? (
        <div className={styles.failed}>
          <div className={styles.failedText}>{statusText}</div>
          <Button size="small" onClick={() => navigate('/picture-book')}>
            去绘本生成页重试
          </Button>
        </div>
      ) : null}

      {expired ? (
        <div className={styles.failed}>
          <div className={styles.failedText}>生成任务已过期（超过 2 小时），请重新生成。</div>
          <Button size="small" onClick={() => navigate('/picture-book')}>
            去绘本生成页重新生成
          </Button>
        </div>
      ) : null}

      {status === 'COMPLETED' ? (
        <div className={styles.doneRow}>
          {canRead ? (
            <Button type="primary" size="small" icon={<BookImage size={13} />} onClick={() => setReading(true)}>
              查看绘本
            </Button>
          ) : (
            <>
              <span className={styles.doneText}>绘本已生成，可在「绘本生成」页查看</span>
              <Button size="small" icon={<ExternalLink size={13} />} onClick={() => navigate('/picture-book')}>
                去查看
              </Button>
            </>
          )}
        </div>
      ) : null}

      <Modal
        open={reading}
        title={task?.title ? `绘本《${task.title}》` : '绘本阅读'}
        footer={null}
        width={680}
        onCancel={() => setReading(false)}
        destroyOnClose
      >
        {canRead && task?.bookResourceId ? (
          <PictureBookReader
            resourceId={task.bookResourceId}
            script={script}
            fixingPage={fixingPage}
            onFixPage={(page) => void fixPage(page)}
            audioTask={audioTask}
            onGenerateAudio={(page, selectedVoice) => void generateAudio(page, selectedVoice || undefined)}
          />
        ) : null}
      </Modal>
    </div>
  );
}