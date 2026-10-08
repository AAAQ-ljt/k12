import { useEffect, useRef, useState } from 'react';
import { App, Button, Progress, Tag } from 'antd';
import { Clapperboard, ExternalLink, RotateCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  getAnimationResource,
  getAnimationTask,
  parseAnimationScript,
  type AnimationScript,
} from '@/api/animation';
import SvgStepPlayer from './SvgStepPlayer';
import styles from './PictureBookChatCard.module.scss';

interface AnimationChatCardProps {
  /** 动画生成任务 ID（与「动画讲解」页共用状态机） */
  taskId: string;
  /** 主题（学生输入的知识点） */
  topic?: string;
}

/** 任务状态文案（与「动画讲解」页同口径） */
const STATUS_TEXT: Record<string, string> = {
  PENDING: '任务已提交，正在排队…',
  ANIMATION_GENERATING: '正在生成分步 SVG 画面…',
  COMPLETED: '生成完成',
  FAILED: '生成失败',
};

/** 轮询上限：约 4 分钟（超过就认为过期，避免无限转圈） */
const MAX_POLLS = 80;

/**
 * 对话内动画讲解卡片（2026-10-08，学生要求对齐绘本体验）。
 *
 * 生成走服务端异步任务：卡片先显示「生成中 + 进度」，切到其他页面也不会中断（任务在服务端跑），
 * 回到对话按 taskId 继续轮询；完成后**直接在对话里渲染动画播放器**，并保留去「动画讲解」页的入口。
 */
export default function AnimationChatCard({ taskId, topic }: AnimationChatCardProps) {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [task, setTask] = useState<Awaited<ReturnType<typeof getAnimationTask>> | null>(null);
  const [script, setScript] = useState<AnimationScript | null>(null);
  const [polls, setPolls] = useState(0);
  const [expired, setExpired] = useState(false);
  const loadedResourceRef = useRef<string | null>(null);

  const status = task?.status;
  const terminal = status === 'COMPLETED' || status === 'FAILED' || expired;

  // 轮询任务状态：到终态停止（任务体在服务端，切页回来重新挂载会继续轮询）
  useEffect(() => {
    if (!taskId || terminal) {
      return;
    }
    if (polls >= MAX_POLLS) {
      setExpired(true);
      return;
    }
    const timer = setTimeout(async () => {
      try {
        const snapshot = await getAnimationTask(taskId);
        setTask(snapshot);
        setPolls((value) => value + 1);
      } catch {
        // 任务过期/查询失败：按过期处理，给出明确出口
        setExpired(true);
      }
    }, 2500);
    return () => clearTimeout(timer);
  }, [taskId, task, polls, terminal]);

  // 完成后拉取脚本，直接在对话里播放
  useEffect(() => {
    const resourceId = task?.animationResourceId;
    if (status !== 'COMPLETED' || !resourceId || loadedResourceRef.current === resourceId) {
      return;
    }
    loadedResourceRef.current = resourceId;
    void getAnimationResource(resourceId)
      .then((resource) => {
        const parsed = parseAnimationScript(resource?.extJson);
        if (parsed && parsed.steps?.length) {
          setScript(parsed);
        } else {
          message.warning('动画脚本读取失败，可去「动画讲解」页查看');
        }
      })
      .catch(() => message.warning('动画脚本读取失败，可去「动画讲解」页查看'));
  }, [status, task?.animationResourceId, message]);

  const percent = status === 'COMPLETED' ? 100 : status === 'ANIMATION_GENERATING' ? 60 : 10;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <Clapperboard size={16} className={styles.icon} />
        <span className={styles.title}>
          {task?.title ? `动画讲解《${task.title}》` : topic ? `动画讲解《${topic}》` : '动画讲解'}
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
          <div className={styles.progressText}>{(status && STATUS_TEXT[status]) || '正在生成动画讲解…'}</div>
          <div className={styles.hint}>切到其他页面也会继续生成，回到对话可恢复进度。</div>
        </div>
      ) : null}

      {status === 'FAILED' || expired ? (
        <div className={styles.failed}>
          <div className={styles.failedText}>
            {expired ? '生成任务已过期，请重新发起。' : task?.message || '动画生成失败，可稍后重试。'}
          </div>
          <Button size="small" icon={<RotateCcw size={13} />} onClick={() => navigate('/animation')}>
            去动画讲解页重试
          </Button>
        </div>
      ) : null}

      {/* 完成后直接在对话里播放（学生要求：不用跳页就能看到画面） */}
      {script ? (
        <div style={{ marginTop: 4 }}>
          <SvgStepPlayer script={script} compact />
        </div>
      ) : null}

      <div className={styles.doneRow}>
        {status === 'COMPLETED' && !script ? (
          <span className={styles.doneText}>动画已生成，可在「动画讲解」页查看</span>
        ) : null}
        <Button size="small" icon={<ExternalLink size={13} />} onClick={() => navigate('/animation')}>
          去动画讲解页
        </Button>
      </div>
    </div>
  );
}
