import { useCallback, useEffect, useRef, useState } from 'react';
import { Alert, App, Button, Collapse, Empty, Popconfirm, Progress, Space, Tag } from 'antd';
import { BookOpen, Compass, GraduationCap, Plus, Sparkles, Target, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import {
  deleteLearningPath,
  deleteLearningPathHistory,
  generateLearningPath,
  loadLearningPathHistory,
  loadMyLearningPaths,
  parseLegacyPlan,
  type LearningPathHistoryItem,
  type LearningPathSummary,
} from '@/api/learningPath';
import { loadMyMasteryOverview, type MasteryOverview } from '@/api/knowledgeMastery';
import { loadStudentWikiProfile, type StudentWikiProfile } from '@/api/studentWiki';
import LearningProfileModal from '@/components/profile/LearningProfileModal';
import styles from './index.module.scss';

const STAGE_LABELS: Record<string, string> = {
  PRIMARY_LOW: '小学低年级',
  PRIMARY_HIGH: '小学高年级',
  JUNIOR: '初中',
  SENIOR: '高中',
};

const MASTERY_STATUS: Record<number, { label: string; color: string }> = {
  0: { label: '未解锁', color: 'default' },
  1: { label: '进行中', color: 'blue' },
  2: { label: '已掌握', color: 'green' },
};

function formatTime(value?: string | null): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
}

/**
 * 个性化学习路径 —— 路线库：
 * 一卡一条路线（目标一句话 + 进度 + 当前节点），点卡片进入路线详情页；
 * 顶部可维护「我的学习档案」；没有学习目标时先拦下并引导填写（空档案生成的路线过于空泛）。
 */
export default function LearningPath() {
  const { message, modal } = App.useApp();
  const navigate = useNavigate();
  const userInfo = useAuthStore((state) => state.userInfo);
  const [list, setList] = useState<LearningPathSummary[]>([]);
  const [overview, setOverview] = useState<MasteryOverview | null>(null);
  const [history, setHistory] = useState<LearningPathHistoryItem[]>([]);
  const [profile, setProfile] = useState<StudentWikiProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  /** 因缺少学习目标被拦下时置位：档案保存后自动继续生成 */
  const pendingGenerateRef = useRef(false);

  const hasGoal = !!profile?.learningGoal?.trim();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setList(await loadMyLearningPaths());
      try {
        setOverview(await loadMyMasteryOverview());
      } catch {
        // 学习进度属于附加信息，失败不影响路线展示（错误已统一提示）
      }
      try {
        setHistory(await loadLearningPathHistory());
      } catch {
        // 历史计划失败不影响主流程
      }
      try {
        setProfile(await loadStudentWikiProfile());
      } catch {
        // 档案读取失败不影响列表（生成时后端会再校验一次）
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

  const doGenerate = useCallback(async () => {
    setGenerating(true);
    try {
      const detail = await generateLearningPath();
      message.success(`学习路线《${detail.title}》已生成`);
      navigate(`/learning-path/${detail.pathId}`);
    } catch {
      // 错误已统一提示
    } finally {
      setGenerating(false);
    }
  }, [message, navigate]);

  const handleGenerate = () => {
    if (!hasGoal) {
      modal.confirm({
        title: '先补一下学习目标',
        content: 'AI 需要知道你的学习目标才能规划出贴合的路线；目标为空时它给出的内容会很泛。现在去填写？',
        okText: '去填写',
        cancelText: '取消',
        onOk: () => {
          pendingGenerateRef.current = true;
          setProfileOpen(true);
        },
      });
      return;
    }
    void doGenerate();
  };

  const handleProfileSaved = async () => {
    setProfileOpen(false);
    await load();
    if (pendingGenerateRef.current) {
      pendingGenerateRef.current = false;
      await doGenerate();
    }
  };

  const remove = async (pathId: string) => {
    try {
      await deleteLearningPath(pathId);
      message.success('学习路线已删除');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  const removeHistory = async (recordId: string) => {
    try {
      await deleteLearningPathHistory(recordId);
      message.success('历史计划已删除');
      await load();
    } catch {
      // 错误已统一提示
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <div className={styles.pageTitle}>
            <Compass size={22} />
            <span>个性化学习路径</span>
          </div>
          <div className={styles.pageDesc}>
            每条路线围绕一个学习目标规划；节点状态由掌握度驱动，做完测验就会自动推进
            {userInfo ? ' · 当前学段适配' : ''}
          </div>
        </div>
        <Space>
          <Button icon={<BookOpen size={15} />} onClick={() => setProfileOpen(true)}>
            我的学习档案
          </Button>
          <Button type="primary" icon={<Sparkles size={15} />} loading={generating} onClick={handleGenerate}>
            AI 生成新路线
          </Button>
        </Space>
      </div>

      {!hasGoal ? (
        <Alert
          type="info"
          showIcon
          message="还没有填写学习目标"
          description="补全「我的学习档案」里的学习目标与兴趣，AI 才能规划出针对你的路线；现在点生成会先引导你填写。"
          action={
            <Button size="small" onClick={() => setProfileOpen(true)}>
              去填写
            </Button>
          }
        />
      ) : null}

      <div className={styles.progressCard}>
        <div className={styles.progressHeader}>
          <div className={styles.progressTitle}>
            <GraduationCap size={17} />
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
        <div className={styles.sectionTitle}>
          <Target size={16} />
          <span>我的学习路线（{list.length}）</span>
        </div>
        {list.length === 0 && !loading ? (
          <Empty description="还没有学习路线，点右上角「AI 生成新路线」开始规划">
            <Button icon={<Plus size={14} />} onClick={handleGenerate}>
              生成我的第一条路线
            </Button>
          </Empty>
        ) : (
          <div className={styles.routeGrid}>
            {list.map((path) => (
              <div key={path.pathId} className={styles.routeCard}>
                <div className={styles.routeHeader}>
                  <span className={styles.routeTitle}>{path.title}</span>
                  <Tag color={path.status === 1 ? 'green' : 'blue'}>
                    {path.status === 1 ? '已完成' : '进行中'}
                  </Tag>
                  {path.legacy ? <Tag>旧版生成</Tag> : null}
                </div>
                <div className={styles.routeGoal}>
                  {path.goal || '（这条路线生成于旧版本，没有目标与阶段信息）'}
                </div>
                <div className={styles.routeMeta}>
                  <Tag color="purple">{STAGE_LABELS[path.stage || ''] || '学段'}</Tag>
                  <span>
                    节点 {path.finishedItems || 0}/{path.totalItems || 0}
                  </span>
                  {path.stageCount ? <span>{path.stageCount} 个阶段</span> : null}
                  <span>{formatTime(path.updateTime || path.createTime)}</span>
                </div>
                <Progress percent={path.progress || 0} size="small" />
                <div className={styles.routeFooter}>
                  <span className={styles.routeCurrent}>
                    {path.currentNodeName ? `当前：${path.currentNodeName}` : '已完成全部节点'}
                  </span>
                  <Space size={4}>
                    <Button
                      type="primary"
                      size="small"
                      icon={<Sparkles size={13} />}
                      onClick={() => navigate(`/learning-path/${path.pathId}`)}
                    >
                      继续学习
                    </Button>
                    <Popconfirm title="删除这条学习路线？" onConfirm={() => void remove(path.pathId)}>
                      <Button type="text" size="small" danger icon={<Trash2 size={13} />} />
                    </Popconfirm>
                  </Space>
                </div>
              </div>
            ))}
          </div>
        )}

        {history.length > 0 ? (
          <Collapse
            ghost
            items={[
              {
                key: 'history',
                label: `历史计划（旧版生成，${history.length} 条）`,
                children: (
                  <div className={styles.historyList}>
                    {history.map((item) => {
                      const plan = parseLegacyPlan(item.content);
                      return (
                        <div key={item.recordId} className={styles.historyItem}>
                          <div className={styles.historyHeader}>
                            <span className={styles.historyTitle}>
                              {item.title || plan?.title || '学习计划'}
                            </span>
                            <span className={styles.pathTime}>{formatTime(item.createTime)}</span>
                            <Popconfirm
                              title="删除这条历史计划？"
                              onConfirm={() => void removeHistory(item.recordId)}
                            >
                              <Button type="text" size="small" danger icon={<Trash2 size={13} />} />
                            </Popconfirm>
                          </div>
                          {plan ? (
                            <div className={styles.historySteps}>
                              {plan.steps.map((step, index) => (
                                <span key={`${step.title}-${index}`} className={styles.historyStep}>
                                  {index + 1}. {step.title}
                                </span>
                              ))}
                            </div>
                          ) : null}
                        </div>
                      );
                    })}
                  </div>
                ),
              },
            ]}
          />
        ) : null}
      </div>

      <LearningProfileModal
        open={profileOpen}
        onClose={() => {
          pendingGenerateRef.current = false;
          setProfileOpen(false);
        }}
        onSaved={() => void handleProfileSaved()}
      />
    </div>
  );
}
