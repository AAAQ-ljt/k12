import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { App, Button, Empty, Input, Popconfirm, Space, Tag } from 'antd';
import { Clapperboard, PlaySquare, Sparkles, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import {
  deleteAnimationResource,
  generateAnimation,
  getAnimationTask,
  loadMyAnimationList,
  parseAnimationScript,
  sanitizeAnimationSvg,
  type AnimationResource,
  type AnimationScript,
} from '@/api/animation';
import styles from './index.module.scss';

/** 动画生成任务持久化（sessionStorage），切页不丢进度 */
const ANIM_TASK_KEY = 'anim:task';

interface AnimationTaskSnapshot {
  taskId: string;
  status: string;
  animationResourceId?: string;
  title?: string;
  message?: string;
}

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
}

/** 动画列表卡片：首步 SVG 缩略预览 + 标题信息 + 操作 */
function AnimationCard({
  record,
  script,
  onOpen,
  onRemove,
}: {
  record: AnimationResource;
  script: AnimationScript | null;
  onOpen: (record: AnimationResource) => void;
  onRemove: (resourceId: string) => void;
}) {
  const preview = useMemo(() => sanitizeAnimationSvg(script?.steps?.[0]?.svg), [script]);
  return (
    <div className={styles.animationCard}>
      <div className={styles.cardPreview}>
        {preview ? (
          <div className={styles.cardSvg} dangerouslySetInnerHTML={{ __html: preview }} />
        ) : (
          <div className={styles.cardPreviewEmpty}>
            <Clapperboard size={30} />
          </div>
        )}
      </div>
      <div className={styles.cardTitle}>{record.resourceName || '未命名动画'}</div>
      <div className={styles.cardMeta}>
        <Tag color="green">动画</Tag>
        <span>{script?.steps.length ?? 0} 步</span>
        <span>{formatTime(record.createTime)}</span>
      </div>
      <div className={styles.cardActions}>
        <Space size={8}>
          <Button type="primary" size="small" icon={<PlaySquare size={14} />} onClick={() => onOpen(record)}>
            全屏播放
          </Button>
          <Popconfirm title="删除该动画？" onConfirm={() => onRemove(record.resourceId)}>
            <Button size="small" danger icon={<Trash2 size={14} />} />
          </Popconfirm>
        </Space>
      </div>
    </div>
  );
}

export default function Animation() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  /** 组件是否挂载：切页后轮询只写 sessionStorage，切回时恢复 */
  const mountedRef = useRef(true);
  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const [topic, setTopic] = useState('');
  const [generating, setGenerating] = useState(false);
  const [genProgress, setGenProgress] = useState('');
  const [list, setList] = useState<AnimationResource[]>([]);

  const load = useCallback(async () => {
    try {
      setList(await loadMyAnimationList());
    } catch {
      // 错误已统一提示
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const openPlayer = (record: AnimationResource) => {
    if (!parseAnimationScript(record.extJson)) {
      message.warning('该动画脚本无法解析');
      return;
    }
    navigate(`/animation/${record.resourceId}`);
  };

  const remove = async (resourceId: string) => {
    try {
      await deleteAnimationResource(resourceId);
      message.success('动画已删除');
      void load();
    } catch {
      // 错误已统一提示
    }
  };

  const handleGenerate = async () => {
    const text = topic.trim();
    if (!text) {
      message.warning('先输入一个想讲解的知识点，比如「冒泡排序」');
      return;
    }
    // 先清残留任务快照，避免旧任务的 FAILED/COMPLETED 状态在恢复 effect 中复活跳转
    sessionStorage.removeItem(ANIM_TASK_KEY);
    setGenerating(true);
    setGenProgress('任务提交中...');
    setTopic('');
    try {
      // 异步任务：提交后轮询状态，生成完成时自动打开播放页；切页不丢任务
      const task = await generateAnimation(text);
      sessionStorage.setItem(ANIM_TASK_KEY, JSON.stringify({ taskId: task.taskId, status: task.status }));
      await pollLoop(task.taskId);
    } catch {
      // 提交失败已统一提示
      setGenerating(false);
      setGenProgress('');
      sessionStorage.removeItem(ANIM_TASK_KEY);
    }
  };

  /** 轮询一次任务状态：更新进度/持久化；返回 true 表示已到终态 */
  const pollOnce = async (taskId: string): Promise<boolean> => {
    const snapshot = await getAnimationTask(taskId);
    sessionStorage.setItem(
      ANIM_TASK_KEY,
      JSON.stringify({
        taskId,
        status: snapshot.status,
        animationResourceId: snapshot.animationResourceId,
        title: snapshot.title,
        message: snapshot.message,
      }),
    );
    // 页面已切走：仅持久化，切回时恢复
    if (!mountedRef.current) {
      return snapshot.status === 'COMPLETED' || snapshot.status === 'FAILED';
    }
    if (snapshot.status === 'PENDING' || snapshot.status === 'ANIMATION_GENERATING') {
      setGenProgress(
        snapshot.status === 'ANIMATION_GENERATING'
          ? 'AI 正在编排分步动画（约需十几秒）...'
          : '任务已提交，正在排队执行...',
      );
      return false;
    }
    if (snapshot.status === 'COMPLETED') {
      sessionStorage.removeItem(ANIM_TASK_KEY);
      message.success(`动画《${snapshot.title || ''}》生成完成`);
      navigate(`/animation/${snapshot.animationResourceId}`);
      return true;
    }
    sessionStorage.removeItem(ANIM_TASK_KEY);
    message.warning(snapshot.message || '动画生成失败，请稍后重试');
    return true;
  };

  /** 轮询循环：组件卸载时停止，任务进度由 sessionStorage 保存，切回页面时恢复 */
  const pollLoop = async (taskId: string) => {
    while (mountedRef.current) {
      try {
        if (await pollOnce(taskId)) {
          setGenerating(false);
          setGenProgress('');
          return;
        }
      } catch {
        sessionStorage.removeItem(ANIM_TASK_KEY);
        if (mountedRef.current) {
          message.warning('生成状态查询失败，请刷新页面查看');
        }
        setGenerating(false);
        setGenProgress('');
        return;
      }
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }
  };

  /** 切回页面时恢复未完成任务（进度/完成跳转/失败提示） */
  useEffect(() => {
    if (!token) {
      return;
    }
    const raw = sessionStorage.getItem(ANIM_TASK_KEY);
    if (!raw) {
      return;
    }
    let saved: AnimationTaskSnapshot;
    try {
      saved = JSON.parse(raw) as AnimationTaskSnapshot;
    } catch {
      sessionStorage.removeItem(ANIM_TASK_KEY);
      return;
    }
    if (saved.status === 'COMPLETED') {
      sessionStorage.removeItem(ANIM_TASK_KEY);
      if (saved.animationResourceId) {
        navigate(`/animation/${saved.animationResourceId}`);
      }
    } else if (saved.status === 'FAILED') {
      sessionStorage.removeItem(ANIM_TASK_KEY);
      message.warning(saved.message || '动画生成失败，请稍后重试');
      void load();
    } else if (saved.taskId) {
      setGenerating(true);
      setGenProgress('恢复生成进度...');
      void pollLoop(saved.taskId);
    }
  }, [token]); // eslint-disable-line react-hooks/exhaustive-deps

  const scriptCache: Record<string, AnimationScript | null> = {};
  list.forEach((record) => {
    scriptCache[record.resourceId] = parseAnimationScript(record.extJson);
  });

  return (
    <div className={styles.animationPage}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          <Clapperboard size={22} />
          <span>动画讲解</span>
        </div>
        <div className={styles.pageDesc}>输入知识点，AI 生成分步 SVG 动画直观讲解；生成的动画会存入你的个人知识库。</div>
      </div>
      <div className={styles.generateBar}>
        <Input
          size="large"
          placeholder="输入想讲解的知识点，例如：冒泡排序 / 植物如何进行光合作用"
          value={topic}
          onChange={(event) => setTopic(event.target.value)}
          onPressEnter={() => void handleGenerate()}
          maxLength={50}
        />
        <Button
          type="primary"
          size="large"
          icon={<Sparkles size={16} />}
          loading={generating}
          onClick={() => void handleGenerate()}
        >
          {generating ? (genProgress || 'AI 生成动画中...') : '生成动画讲解'}
        </Button>
      </div>
      <div className={styles.animationBody}>
        {list.length === 0 ? (
          <Empty description="还没有动画讲解，输入知识点生成第一支吧" />
        ) : (
          <div className={styles.animationGrid}>
            {list.map((record) => (
              <AnimationCard
                key={record.resourceId}
                record={record}
                script={scriptCache[record.resourceId]}
                onOpen={openPlayer}
                onRemove={(resourceId) => void remove(resourceId)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}