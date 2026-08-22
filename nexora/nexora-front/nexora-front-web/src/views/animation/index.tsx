import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Popconfirm, Space, Tag } from 'antd';
import { Clapperboard, PlaySquare, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  deleteAnimationResource,
  loadMyAnimationList,
  parseAnimationScript,
  type AnimationResource,
} from '@/api/animation';
import styles from './index.module.scss';

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
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
    const script = parseAnimationScript(record.extJson);
    if (!script) {
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

  const scriptCache: Record<string, ReturnType<typeof parseAnimationScript>> = {};
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
            {list.map((record) => {
              const script = scriptCache[record.resourceId];
              return (
                <div key={record.resourceId} className={styles.animationCard}>
                  <div className={styles.cardTitle}>{record.resourceName || '未命名动画'}</div>
                  <div className={styles.cardMeta}>
                    <Tag color="green">动画</Tag>
                    <span>{script?.steps.length ?? 0} 步</span>
                    <span>{formatTime(record.createTime)}</span>
                  </div>
                  <div className={styles.cardActions}>
                    <Space size={8}>
                      <Button type="primary" size="small" icon={<PlaySquare size={14} />} onClick={() => openPlayer(record)}>
                        全屏播放
                      </Button>
                      <Popconfirm title="删除该动画？" onConfirm={() => void remove(record.resourceId)}>
                        <Button size="small" danger icon={<Trash2 size={14} />} />
                      </Popconfirm>
                    </Space>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}