import { useCallback, useEffect, useMemo, useState } from 'react';
import { App, Button, Empty, Popconfirm, Space, Tag } from 'antd';
import { Clapperboard, PlaySquare, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  deleteAnimationResource,
  loadMyAnimationList,
  parseAnimationScript,
  sanitizeAnimationSvg,
  type AnimationResource,
  type AnimationScript,
} from '@/api/animation';
import styles from './index.module.scss';

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
        <div className={styles.pageDesc}>AI 生成的分步 SVG 动画讲解会存入你的个人知识库，在这里全屏观看。</div>
      </div>
      <div className={styles.animationBody}>
        {list.length === 0 ? (
          <Empty description="暂无动画讲解，去 AI 助教说「生成一个 XX 的动画讲解」吧" />
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