import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { App, Avatar, Button, Card, Progress, Select, Space, Spin, Tag, Tooltip } from 'antd';
import {
  BookImage, Compass, FolderOpen, GraduationCap, PenLine, PlaySquare, Route, Sparkles, LogOut,
  CalendarCheck, ChevronDown, ChevronRight, ChevronUp, Flame, Target,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { studentLogout, updateStudentGrade } from '@/api/auth';
import { GRADE_OPTIONS, gradeToStage, getGradeText, getStageOption } from '@/types/common';
import {
  loadMyMasteryOverview, loadMyLearningTrend, locateReviewPoint,
  type LearningTrend, type ReviewLocate,
} from '@/api/knowledgeMastery';
import { loadMyLearningPaths, type LearningPathSummary } from '@/api/learningPath';
import { loadMyAnimationList } from '@/api/animation';
import { loadMyPictureBooks } from '@/api/pictureBook';
import { loadStudentWikiList } from '@/api/studentWiki';
import { loadMyCourseProgress, type CourseProgress } from '@/api/course';
import LearningProfileModal from '@/components/profile/LearningProfileModal';
import styles from './index.module.scss';

/**
 * 我的页面：学生个人学习中枢（左右双栏，窄屏自动收单列）
 * 左栏：个人信息与数据（用户卡 / 学习概览 / 学习趋势 / 我的创作）
 * 右栏：学习事务（今日待办 / 最近路线 / 课程学习进度 / 账号）
 * 数据复用现有接口 + knowledgeMastery/loadMyTrend、locateReview、courseInfo/loadMyCourseProgress，
 * 任一失败不影响整页。
 */

/** 今日待办默认展示条数：超出折叠为「展开全部」，避免待办多时右栏溢出难看 */
const DUE_PREVIEW_COUNT = 8;

export default function Profile() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const userInfo = useAuthStore((state) => state.userInfo);
  const setUserInfo = useAuthStore((state) => state.setUserInfo);
  const clear = useAuthStore((state) => state.clear);

  const [overview, setOverview] = useState<{ masteredCount: number; learningCount: number; avgMasteryScore: number; correctRate: number; dueReviewCount: number; totalCorrect: number; totalPractice: number; dueItems: { knowledgePointId: string; knowledgePointName: string }[] } | null>(null);
  const [trend, setTrend] = useState<LearningTrend | null>(null);
  const [paths, setPaths] = useState<LearningPathSummary[]>([]);
  const [courseProgress, setCourseProgress] = useState<CourseProgress[]>([]);
  const [animationCount, setAnimationCount] = useState(0);
  const [bookCount, setBookCount] = useState(0);
  const [wikiCount, setWikiCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [locatingId, setLocatingId] = useState<string | null>(null);
  /** 今日待办是否展开全部（默认折叠展示前 DUE_PREVIEW_COUNT 条） */
  const [dueExpanded, setDueExpanded] = useState(false);

  const stage = userInfo?.stage;
  const stageLabel = stage ? getStageOption(stage)?.label : '';
  const gradeText = getGradeText(userInfo);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      // 并行拉取各模块数据：任一失败不影响整页（错误已统一提示）
      const [ov, tr, pathList, courses, anims, books, wikis] = await Promise.all([
        loadMyMasteryOverview(200),
        loadMyLearningTrend(),
        loadMyLearningPaths(),
        loadMyCourseProgress().catch(() => [] as CourseProgress[]),
        loadMyAnimationList().catch(() => [] as never[]),
        loadMyPictureBooks().catch(() => [] as never[]),
        loadStudentWikiList().catch(() => [] as never[]),
      ]);
      setOverview({
        masteredCount: ov.masteredCount,
        learningCount: ov.learningCount,
        avgMasteryScore: ov.avgMasteryScore,
        correctRate: ov.correctRate,
        dueReviewCount: ov.dueReviewCount,
        totalCorrect: ov.totalCorrect,
        totalPractice: ov.totalPractice,
        dueItems: (ov.items ?? [])
          .filter((item) => item.due)
          .map((item) => ({ knowledgePointId: item.knowledgePointId, knowledgePointName: item.knowledgePointName })),
      });
      setTrend(tr);
      setPaths(pathList ?? []);
      setCourseProgress(courses ?? []);
      setAnimationCount(anims?.length ?? 0);
      setBookCount(books?.length ?? 0);
      setWikiCount(wikis?.length ?? 0);
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  /** 修改年级（学段由年级推导） */
  const handleGradeChange = async (grade: string) => {
    try {
      await updateStudentGrade(grade);
      if (userInfo) {
        setUserInfo({ ...userInfo, grade, stage: gradeToStage(grade) ?? userInfo.stage });
      }
      message.success('年级已更新');
    } catch {
      // 错误已由请求拦截器统一提示
    }
  };

  /** 退出登录 */
  const handleLogout = async () => {
    try {
      await studentLogout();
    } finally {
      clear();
      message.success('已退出登录');
      navigate('/ai-tutor');
    }
  };

  /** 待复习知识点：命中学习路径节点 → 跳路线做节点快测；未命中 → AI 助教对话复习 */
  const handleReviewPoint = async (point: { knowledgePointId: string; knowledgePointName: string }) => {
    if (locatingId) {
      return;
    }
    setLocatingId(point.knowledgePointId);
    try {
      const locate: ReviewLocate = await locateReviewPoint(point.knowledgePointId);
      if (locate.located && locate.pathId) {
        message.success(`已定位到你的学习路线「${locate.knowledgePointName || point.knowledgePointName}」`);
        navigate(`/learning-path/${locate.pathId}`, { state: { focusItemId: locate.itemId } });
      } else {
        navigate('/ai-tutor', {
          state: { presetQuestion: `点开我的掌握度里有一个「${point.knowledgePointName}」需要复习，帮我出几道题复习巩固一下` },
        });
      }
    } catch {
      // 错误已统一提示
    } finally {
      setLocatingId(null);
    }
  };

  /** 我的创作宫格：按学段裁剪（与侧边栏导航一致） */
  const creationTiles = useMemo(() => {
    const tiles: { key: string; label: string; icon: ReactNode; count?: number; to: string; show: boolean }[] = [
      { key: 'animation', label: '动画讲解', icon: <PlaySquare size={22} />, count: animationCount, to: '/animation', show: stage === 'JUNIOR' || stage === 'SENIOR' },
      { key: 'book', label: 'AI 绘本', icon: <BookImage size={22} />, count: bookCount, to: '/picture-book', show: stage === 'PRIMARY_LOW' || stage === 'PRIMARY_HIGH' },
      { key: 'coding', label: '编程作品', icon: <Sparkles size={22} />, to: '/coding', show: stage === 'PRIMARY_HIGH' || stage === 'JUNIOR' || stage === 'SENIOR' },
      { key: 'wiki', label: '个人知识库', icon: <FolderOpen size={22} />, count: wikiCount, to: '/resource-center', show: true },
    ];
    return tiles.filter((tile) => tile.show);
  }, [stage, animationCount, bookCount, wikiCount]);

  if (loading && !overview) {
    return (
      <div className={styles.profileWrapper}>
        <div className={styles.loadingBlock}><Spin /> <span>正在加载我的学习档案...</span></div>
      </div>
    );
  }

  return (
    <div className={styles.profileWrapper}>
      <div className={styles.profileCols}>
        {/* ==================== 左栏：个人信息与数据 ==================== */}
        <div className={styles.colLeft}>
          <Card>
            <div className={styles.userHeader}>
              <Avatar size={56} src={userInfo?.avatar}>{userInfo?.username?.[0]?.toUpperCase()}</Avatar>
              <div className={styles.userMeta}>
                <div className={styles.userName}>{userInfo?.username}</div>
                <div className={styles.userEmail}>{userInfo?.email}</div>
                <Space size={8} className={styles.userTags}>
                  {stageLabel ? <Tag color="purple">{stageLabel}</Tag> : null}
                  <Tag>学生</Tag>
                  {overview && overview.dueReviewCount > 0 ? <Tag color="red">待复习 {overview.dueReviewCount}</Tag> : null}
                </Space>
              </div>
            </div>
            <div className={styles.gradeRow}>
              <span>年级</span>
              <Select
                value={userInfo?.grade}
                onChange={handleGradeChange}
                style={{ width: '100%' }}
                options={[...GRADE_OPTIONS]}
              />
            </div>
            <Button block icon={<PenLine size={14} />} style={{ marginTop: 10 }} onClick={() => setProfileOpen(true)}>
              编辑学习档案
            </Button>
          </Card>

          <Card>
            <div className={styles.cardTitle}>
              <GraduationCap size={16} />
              <span>学习概览</span>
            </div>
            {overview && overview.totalPractice > 0 ? (
              <div className={styles.statRow2}>
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
                  <span className={styles.statLabel}>正确率</span>
                </div>
              </div>
            ) : (
              <div className={styles.emptyTip}>
                还没有做题数据：去做课时通关测验或节点快测，这里会显示真实掌握度。
              </div>
            )}
          </Card>

          <Card>
            <div className={styles.cardTitle}>
              <CalendarCheck size={16} />
              <span>学习趋势</span>
            </div>
            {trend ? (
              <>
                <div className={styles.trendMeters}>
                  <div className={styles.meter}>
                    <b>{trend.studyDays}</b>
                    <span>学习天数</span>
                  </div>
                  <div className={styles.meter}>
                    <b>{trend.streakDays}</b>
                    <span>连续打卡</span>
                  </div>
                  <div className={styles.meter}>
                    <b>{trend.totalPractice}</b>
                    <span>累计练习</span>
                  </div>
                </div>
                <div className={styles.weekChart}>
                  {(trend.weekTrend ?? []).map((d) => (
                    <div key={d.day} className={styles.weekCol}>
                      <div className={styles.weekBarWrap}>
                        <div
                          className={`${styles.weekBar} ${d.count > 0 ? styles.weekBarActive : ''}`}
                          style={{ height: `${Math.max(6, Math.min(100, (d.count / 6) * 100))}%` }}
                        />
                      </div>
                      <span className={styles.weekDay}>{d.day}</span>
                      <span className={styles.weekCount}>{d.count}</span>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className={styles.emptyTip}>暂无练习数据，做几道题后这里会出现学习趋势。</div>
            )}
          </Card>

          <Card>
            <div className={styles.cardTitle}>
              <Compass size={16} />
              <span>我的创作</span>
            </div>
            <div className={styles.tileGrid}>
              {creationTiles.map((tile) => (
                <button key={tile.key} type="button" className={styles.tile} onClick={() => navigate(tile.to)}>
                  <span className={styles.tileIcon}>{tile.icon}</span>
                  {tile.count !== undefined ? <span className={styles.tileCount}>{tile.count}</span> : null}
                  <span className={styles.tileLabel}>{tile.label}</span>
                </button>
              ))}
            </div>
          </Card>
        </div>

        {/* ==================== 右栏：学习事务 ==================== */}
        <div className={styles.colRight}>
          <Card>
            <div className={styles.cardTitle}>
              <Target size={16} />
              <span>今日待办 · 待复习知识点</span>
            </div>
            {overview && overview.dueItems.length > 0 ? (
              <div className={styles.dueWrap}>
                <div className={styles.dueGrid}>
                  {(dueExpanded ? overview.dueItems : overview.dueItems.slice(0, DUE_PREVIEW_COUNT)).map((item) => (
                    <Tooltip key={item.knowledgePointId} title={item.knowledgePointName}>
                      <button
                        type="button"
                        className={styles.dueCard}
                        disabled={locatingId !== null}
                        onClick={() => void handleReviewPoint(item)}
                      >
                        <Flame size={14} className={styles.dueFlame} />
                        <span className={styles.dueName}>{item.knowledgePointName}</span>
                        <span className={styles.dueArrow}>
                          {locatingId === item.knowledgePointId ? <Spin size="small" /> : <ChevronRight size={14} />}
                        </span>
                      </button>
                    </Tooltip>
                  ))}
                </div>
                {overview.dueItems.length > DUE_PREVIEW_COUNT ? (
                  <Button
                    type="link"
                    size="small"
                    className={styles.dueToggle}
                    onClick={() => setDueExpanded((v) => !v)}
                  >
                    {dueExpanded ? '收起待复习列表' : `展开全部 ${overview.dueItems.length} 个待复习`}
                    {dueExpanded ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
                  </Button>
                ) : null}
                <div className={styles.dueTip}>点击会在学习路线中定位该知识点做节点快测；不在路线内则转 AI 助教对话复习</div>
              </div>
            ) : (
              <div className={styles.emptyTip}>今天没有待复习的知识点，继续保持！</div>
            )}
          </Card>

          <Card>
            <div className={styles.cardTitle}>
              <Route size={16} />
              <span>最近学习路线</span>
            </div>
            {paths.length > 0 ? (
              <div className={styles.pathItem}>
                <div className={styles.pathRow}>
                  <span className={styles.pathTitle}>{paths[0].title}</span>
                  <Button size="small" type="primary" onClick={() => navigate(`/learning-path/${paths[0].pathId}`)}>
                    继续学习
                  </Button>
                </div>
                {paths[0].goal ? <div className={styles.pathGoal}>{paths[0].goal}</div> : null}
                <Progress percent={paths[0].progress || 0} size="small" />
                <div className={styles.pathMeta}>
                  <span>已完成 {paths[0].finishedItems || 0}/{paths[0].totalItems || 0} 个节点</span>
                  {paths[0].currentNodeName ? <span>当前：{paths[0].currentNodeName}</span> : null}
                </div>
                {paths.length > 1 ? (
                  <Button type="link" size="small" style={{ padding: 0 }} onClick={() => navigate('/learning-path')}>
                    共 {paths.length} 条路线，查看全部 →
                  </Button>
                ) : null}
              </div>
            ) : (
              <div className={styles.emptyTip}>
                还没有学习路线，
                <Button type="link" size="small" style={{ padding: 0 }} onClick={() => navigate('/learning-path')}>
                  去生成 AI 学习路径
                </Button>
              </div>
            )}
          </Card>

          <Card>
            <div className={styles.cardTitle}>
              <GraduationCap size={16} />
              <span>课程学习进度</span>
              {courseProgress.length > 0 ? <Tag style={{ marginLeft: 'auto' }}>{courseProgress.length} 门</Tag> : null}
            </div>
            {courseProgress.length > 0 ? (
              <div className={styles.courseList}>
                {courseProgress.slice(0, 3).map((item) => (
                  <button
                    key={item.courseId}
                    type="button"
                    className={styles.courseItem}
                    onClick={() => navigate(`/course-material/${item.courseId}`)}
                  >
                    <div className={styles.courseRow}>
                      <span className={styles.courseName}>{item.courseName}</span>
                      <span className={styles.coursePercent}>{item.progress}%</span>
                    </div>
                    <Progress percent={item.progress} size="small" strokeColor="#1677ff" />
                    <div className={styles.courseMeta}>
                      已完成 {item.finishedLessons}/{item.lessonCount} 课时
                    </div>
                  </button>
                ))}
                <div className={styles.centerLink}>
                  <Button type="link" size="small" onClick={() => navigate('/course-material')}>
                    查看全部课程 →
                  </Button>
                </div>
              </div>
            ) : (
              <div className={styles.emptyTip}>
                还没有课程，
                <Button type="link" size="small" style={{ padding: 0 }} onClick={() => navigate('/course-material')}>
                  去课程教材加入
                </Button>
              </div>
            )}
          </Card>

          <Card>
            <div className={styles.accountRow}>
              <div>
                <div className={styles.userName}>账号信息</div>
                <div className={styles.userEmail}>
                  学段 {stageLabel || '未设置'} · 年级 {gradeText || '未设置'}
                </div>
              </div>
              <Button danger icon={<LogOut size={14} />} onClick={() => void handleLogout()}>退出登录</Button>
            </div>
          </Card>
        </div>
      </div>

      <LearningProfileModal
        open={profileOpen}
        onClose={() => setProfileOpen(false)}
        onSaved={() => {
          setProfileOpen(false);
          void load();
        }}
      />
    </div>
  );
}