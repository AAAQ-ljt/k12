import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Popconfirm, Tag } from 'antd';
import { GraduationCap, Map, Plus, Sparkles, Trash2 } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import {
  deleteLearningPath,
  generateLearningPath,
  loadMyLearningPaths,
  parseLearningPlan,
  type LearningPathRecord,
  type LearningPlanStep,
} from '@/api/learningPath';
import styles from './index.module.scss';

const KIND_META: Record<string, { label: string; color: string }> = {
  learn: { label: '学习', color: 'blue' },
  practice: { label: '练习', color: 'orange' },
  review: { label: '复习', color: 'green' },
};

const STAGE_LABELS: Record<string, string> = {
  PRIMARY_LOW: '小学低年级',
  PRIMARY_HIGH: '小学高年级',
  JUNIOR: '初中',
  SENIOR: '高中',
};

function formatTime(value?: string): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
}

export default function LearningPath() {
  const { message } = App.useApp();
  const userInfo = useAuthStore((state) => state.userInfo);
  const [list, setList] = useState<LearningPathRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setList(await loadMyLearningPaths());
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      const record = await generateLearningPath();
      message.success(`学习路径《${record.title || ''}》已生成`);
      await load();
    } catch {
      // 错误已统一提示
    } finally {
      setGenerating(false);
    }
  };

  const remove = async (recordId: string) => {
    try {
      await deleteLearningPath(recordId);
      message.success('学习路径已删除');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  const renderSteps = (steps: LearningPlanStep[]) => (
    <div className={styles.steps}>
      {steps.map((step, index) => {
        const meta = KIND_META[step.kind] || KIND_META.learn;
        return (
          <div key={`${step.title}-${index}`} className={styles.step}>
            <span className={styles.stepIndex}>{index + 1}</span>
            <div className={styles.stepBody}>
              <div className={styles.stepTitleRow}>
                <span className={styles.stepTitle}>{step.title}</span>
                <Tag color={meta.color}>{meta.label}</Tag>
              </div>
              <div className={styles.stepDesc}>{step.desc}</div>
            </div>
          </div>
        );
      })}
    </div>
  );

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <div className={styles.pageTitle}>
            <Map size={22} />
            <span>个性化学习路径</span>
          </div>
          <div className={styles.pageDesc}>
            AI 结合你的学习档案与已学知识生成专属学习计划，可在「我的学习档案」补充目标与兴趣
            {userInfo ? ` · 当前学段适配` : ''}
          </div>
        </div>
        <Button
          type="primary"
          icon={<Sparkles size={15} />}
          loading={generating}
          onClick={() => void handleGenerate()}
        >
          AI 生成学习路径
        </Button>
      </div>

      <div className={styles.body}>
        {list.length === 0 && !loading ? (
          <Empty description="还没有学习路径，点击右上角「AI 生成学习路径」吧">
            <Button icon={<Plus size={14} />} onClick={() => void handleGenerate()}>
              生成我的第一条路径
            </Button>
          </Empty>
        ) : (
          list.map((record) => {
            const plan = parseLearningPlan(record.content);
            return (
              <div key={record.recordId} className={styles.pathCard}>
                <div className={styles.pathHeader}>
                  <div className={styles.pathTitleRow}>
                    <GraduationCap size={17} />
                    <span className={styles.pathTitle}>{record.title || plan?.title || '学习路径'}</span>
                    <Tag color="purple">{STAGE_LABELS[record.stage || ''] || '学段'}</Tag>
                  </div>
                  <div className={styles.pathActions}>
                    <span className={styles.pathTime}>{formatTime(record.createTime)}</span>
                    <Popconfirm title="删除这条学习路径？" onConfirm={() => void remove(record.recordId)}>
                      <Button type="text" size="small" danger icon={<Trash2 size={14} />} />
                    </Popconfirm>
                  </div>
                </div>
                {plan ? (
                  renderSteps(plan.steps)
                ) : (
                  <div className={styles.emptySteps}>计划内容无法解析</div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}