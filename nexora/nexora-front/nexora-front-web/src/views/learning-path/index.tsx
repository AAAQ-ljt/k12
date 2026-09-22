import { useCallback, useEffect, useState } from 'react';
import { App, Button, Empty, Popconfirm, Progress, Tag } from 'antd';
import { GraduationCap, Map, Plus, Sparkles, Target, Trash2 } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import {
  deleteLearningPath,
  generateLearningPath,
  loadMyLearningPaths,
  parseLearningPlan,
  type LearningPathRecord,
  type LearningPlanStep,
} from '@/api/learningPath';
import { loadMyMasteryOverview, type MasteryOverview } from '@/api/knowledgeMastery';
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

/** 掌握状态：0未解锁 1进行中 2已掌握 */
const MASTERY_STATUS: Record<number, { label: string; color: string }> = {
  0: { label: '未解锁', color: 'default' },
  1: { label: '进行中', color: 'blue' },
  2: { label: '已掌握', color: 'green' },
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
  const [overview, setOverview] = useState<MasteryOverview | null>(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setList(await loadMyLearningPaths());
      try {
        setOverview(await loadMyMasteryOverview());
      } catch {
        // 学习进度属于附加信息，失败不影响路径列表展示（错误已统一提示）
      }
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

      <div className={styles.progressCard}>
        <div className={styles.progressHeader}>
          <div className={styles.progressTitle}>
            <Target size={17} />
            <span>学习进度</span>
          </div>
          <div className={styles.progressMeta}>
            {overview && overview.dueReviewCount > 0 ? (
              <Tag color="red">待复习 {overview.dueReviewCount}</Tag>
            ) : null}
            <span className={styles.progressHint}>数据来自课时测验判分与主观题批阅</span>
          </div>
        </div>
        {overview && overview.totalCount > 0 ? (
          <>
            <div className={styles.statRow}>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{overview.masteredCount}</span>
                <span className={styles.statLabel}>已掌握知识点</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{overview.learningCount}</span>
                <span className={styles.statLabel}>进行中</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{overview.avgMasteryScore}%</span>
                <span className={styles.statLabel}>平均掌握度</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{overview.correctRate}%</span>
                <span className={styles.statLabel}>
                  正确率（{overview.totalCorrect}/{overview.totalPractice}）
                </span>
              </div>
            </div>
            <div className={styles.masteryList}>
              {overview.items.map((item) => {
                const meta = MASTERY_STATUS[item.status] || MASTERY_STATUS[1];
                return (
                  <div key={item.knowledgePointId} className={styles.masteryItem}>
                    <div className={styles.masteryName}>
                      <span className={styles.masteryPoint}>{item.knowledgePointName}</span>
                      <Tag color={meta.color}>{meta.label}</Tag>
                      {item.due ? <Tag color="red">该复习了</Tag> : null}
                    </div>
                    <div className={styles.masteryBar}>
                      <Progress
                        percent={item.masteryScore}
                        showInfo={false}
                        size="small"
                        strokeColor={item.status === 2 ? '#52c41a' : '#1677ff'}
                      />
                    </div>
                    <div className={styles.masteryScore}>{item.masteryScore}%</div>
                    <div className={styles.masteryInfo}>
                      练习 {item.practiceCount} 次 · 答对 {item.correctCount} 次
                      {item.nextReviewTime ? ` · 下次复习 ${item.nextReviewTime}` : ''}
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        ) : (
          <div className={styles.progressEmpty}>
            还没有掌握度数据：去做课时通关测验，答完题这里就会显示真实进度（主观题批阅后也会计入）
          </div>
        )}
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