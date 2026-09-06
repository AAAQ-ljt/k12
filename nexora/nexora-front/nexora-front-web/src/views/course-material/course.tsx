import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { App, Button, Empty, Skeleton, Space, Tag } from 'antd';
import {
  ArrowLeft,
  BookOpen,
  CheckCircle2,
  FileText,
  Film,
  GraduationCap,
  Image as ImageIcon,
  Layers,
  Link2,
  Lock,
  PlusCircle,
  Sparkles,
} from 'lucide-react';
import {
  getCourseDetail,
  getLessonQuizStatus,
  joinCourse,
  type LessonQuizStatus,
  type StudentCourseDetail,
  type StudentLessonResource,
} from '@/api/course';
import { syncStudentWikiFromCourse } from '@/api/studentWiki';
import LessonQuizModal from './components/LessonQuizModal';
import styles from './course.module.scss';

const RESOURCE_META: Record<string, { label: string; icon: typeof FileText; color: string }> = {
  VIDEO: { label: '视频', icon: Film, color: '#1677ff' },
  IMAGE: { label: '图片', icon: ImageIcon, color: '#52c41a' },
  DOCUMENT: { label: '文档', icon: FileText, color: '#fa8c16' },
  PPT: { label: '文档', icon: FileText, color: '#fa8c16' },
  WORD: { label: '文档', icon: FileText, color: '#fa8c16' },
  PDF: { label: '文档', icon: FileText, color: '#fa8c16' },
  PICTURE_BOOK: { label: '绘本', icon: ImageIcon, color: '#eb2f96' },
  LINK: { label: '链接', icon: Link2, color: '#722ed1' },
};

function resourceMeta(type?: string) {
  return RESOURCE_META[type || ''] || RESOURCE_META.DOCUMENT;
}

export default function CourseDetail() {
  const { courseId = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [detail, setDetail] = useState<StudentCourseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [joining, setJoining] = useState(false);
  /** 课时测验/解锁状态 map（lessonId → 状态） */
  const [quizStatusMap, setQuizStatusMap] = useState<Record<string, LessonQuizStatus>>({});
  /** 做题/回看的课时（mode: answer 做题 / result 回看最近一次结果） */
  const [quizLesson, setQuizLesson] = useState<{
    lessonId: string;
    lessonName: string;
    mode: 'answer' | 'result';
  } | null>(null);

  const loadDetail = useCallback(
    (active: { current: boolean }) => {
      setLoading(true);
      setNotFound(false);
      getCourseDetail(courseId)
        .then((data) => {
          if (active.current) {
            setDetail(data);
          }
        })
        .catch(() => {
          if (active.current) {
            setNotFound(true);
          }
        })
        .finally(() => {
          if (active.current) {
            setLoading(false);
          }
        });
    },
    [courseId],
  );

  useEffect(() => {
    const active = { current: true };
    loadDetail(active);
    getLessonQuizStatus(courseId)
      .then((list) => {
        if (active.current) {
          const map: Record<string, LessonQuizStatus> = {};
          list.forEach((item) => {
            map[item.lessonId] = item;
          });
          setQuizStatusMap(map);
        }
      })
      .catch(() => undefined);
    return () => {
      active.current = false;
    };
  }, [courseId, loadDetail]);

  /** 加入课程后重新拉取详情（章节解锁展示） */
  const handleJoin = async () => {
    setJoining(true);
    try {
      await joinCourse(courseId);
      message.success('加入课程成功，开始学习吧');
      loadDetail({ current: true });
    } catch {
      // 请求层已统一提示
    } finally {
      setJoining(false);
    }
  };

  /** 测验通过后刷新解锁状态（下一课时随之解锁） */
  const refreshQuizStatus = () => {
    getLessonQuizStatus(courseId)
      .then((list) => {
        const map: Record<string, LessonQuizStatus> = {};
        list.forEach((item) => {
          map[item.lessonId] = item;
        });
        setQuizStatusMap(map);
      })
      .catch(() => undefined);
  };

  const totalLessons = useMemo(
    () => detail?.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0) ?? 0,
    [detail],
  );

  const openResource = (resource: StudentLessonResource) => {
    if (resource.resourceType === 'LINK') {
      const url = resource.description?.startsWith('http') ? resource.description : '';
      if (url) {
        window.open(url, '_blank', 'noopener,noreferrer');
      } else {
        message.info('该资料暂无可用链接');
      }
      return;
    }
    // 携带来源与课时：预览页返回课程详情，并上报学习进度
    navigate(`/course-material/resource/${resource.resourceId}`, {
      state: { from: `/course-material/${courseId}`, lessonId: resource.lessonId },
    });
  };

  const handleSyncWiki = async () => {
    setSyncing(true);
    try {
      const doc = await syncStudentWikiFromCourse(courseId);
      message.success(`已生成知识页草稿《${doc.title || ''}》，可在「资源中心 → 知识页」查看并确认入库`);
    } catch {
      // 错误已统一提示
    } finally {
      setSyncing(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <Skeleton active paragraph={{ rows: 8 }} />
      </div>
    );
  }

  if (notFound || !detail) {
    return (
      <div className={styles.page}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/course-material')}>
          返回课程列表
        </Button>
        <Empty description="课程不存在或暂不可用" style={{ marginTop: 80 }} />
      </div>
    );
  }

  const course = detail.course;

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/course-material')}>
          返回课程列表
        </Button>
        <div className={styles.titleRow}>
          <span className={styles.titleIcon}>
            <BookOpen size={24} />
          </span>
          <div className={styles.titleMeta}>
            <h2>{course.courseName}</h2>
            <div className={styles.subMeta}>
              <Tag color="geekblue">{course.subject}</Tag>
              {course.grade ? (
                <span>
                  <GraduationCap size={13} />
                  {course.grade}
                </span>
              ) : null}
              <span>
                <Layers size={13} />
                {totalLessons} 课时
              </span>
            </div>
          </div>
          {detail.enrolled === false ? null : (
            <Button
              className={styles.syncButton}
              type="primary"
              ghost
              icon={<Sparkles size={15} />}
              loading={syncing}
              onClick={() => void handleSyncWiki()}
            >
              生成知识页草稿
            </Button>
          )}
        </div>
      </header>

      {course.description ? (
        <section className={styles.descriptionBox}>
          <h3>课程简介</h3>
          <p>{course.description}</p>
        </section>
      ) : null}

      {detail.enrolled === false ? (
        <section className={styles.descriptionBox}>
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="加入课程后即可查看章节、课时与学习资料"
            style={{ padding: '20px 0' }}
          >
            <Button
              type="primary"
              size="large"
              icon={<PlusCircle size={16} />}
              loading={joining}
              onClick={() => void handleJoin()}
            >
              加入课程
            </Button>
          </Empty>
        </section>
      ) : (
        <div className={styles.chapterList}>
          {detail.chapters.map((chapter, chapterIndex) => (
          <section key={chapter.chapter.chapterId} className={styles.chapterCard}>
            <div className={styles.chapterHeader}>
              <span className={styles.chapterIndex}>{chapterIndex + 1}</span>
              <div>
                <h3>{chapter.chapter.chapterName}</h3>
                <p>{chapter.lessons.length} 个课时</p>
              </div>
            </div>
            <div className={styles.lessonList}>
              {chapter.lessons.length === 0 ? (
                <Empty description="暂无课时" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              ) : (
                chapter.lessons.map((lesson, lessonIndex) => {
                  const status = quizStatusMap[lesson.lesson.lessonId];
                  const locked = !!status && !status.unlocked;
                  const passed = !!status && status.passed;
                  return (
                    <div
                      key={lesson.lesson.lessonId}
                      className={`${styles.lessonCard}${locked ? ` ${styles.lessonLocked}` : ''}`}
                    >
                      <div className={styles.lessonTitle}>
                        <span>
                          {chapterIndex + 1}.{lessonIndex + 1}
                        </span>
                        <h4>{lesson.lesson.lessonName}</h4>
                        {status?.hasQuiz ? (
                          <Tag color={passed ? 'success' : 'gold'}>{passed ? '测验已通过' : '含通关测验'}</Tag>
                        ) : null}
                        {passed ? (
                          <span className={styles.passedMark}>
                            <CheckCircle2 size={16} />
                          </span>
                        ) : null}
                      </div>
                      {lesson.lesson.summary ? (
                        <p className={styles.lessonSummary}>{lesson.lesson.summary}</p>
                      ) : null}
                      <div className={styles.resourceList}>
                        {lesson.resources.length === 0 ? (
                          <span className={styles.noResource}>暂无学习资料</span>
                        ) : (
                          lesson.resources.map((resource) => {
                            const meta = resourceMeta(resource.resourceType);
                            const Icon = meta.icon;
                            return (
                              <button
                                key={resource.id}
                                className={styles.resourceItem}
                                disabled={locked}
                                onClick={() => openResource(resource)}
                              >
                                <span className={styles.resourceIcon} style={{ '--icon-color': meta.color } as React.CSSProperties}>
                                  <Icon size={17} />
                                </span>
                                <span className={styles.resourceText}>
                                  <span>{resource.resourceName || '未命名资料'}</span>
                                  <Tag color={meta.color}>{meta.label}</Tag>
                                </span>
                              </button>
                            );
                          })
                        )}
                      </div>
                      <div className={styles.quizBar}>
                        {locked ? (
                          <span className={styles.lockTip}>
                            <Lock size={13} />
                            需先通过上一课时的通关测验
                          </span>
                        ) : status?.hasQuiz ? (
                          <Space size={8} wrap>
                            {status.hasAttempt && status.totalScore ? (
                              <span style={{ fontSize: 12, color: 'rgba(0,0,0,0.55)' }}>
                                最近得分 <b style={{ color: passed ? '#389e0d' : '#d46b08' }}>{status.lastScore}</b>
                                /{status.totalScore} 分（及格线 {status.passScore}）
                              </span>
                            ) : null}
                            {!passed ? (
                              <Button
                                type="primary"
                                size="small"
                                onClick={() => setQuizLesson({ lessonId: lesson.lesson.lessonId, lessonName: lesson.lesson.lessonName, mode: 'answer' })}
                              >
                                做通关测验
                              </Button>
                            ) : null}
                            {status.hasAttempt ? (
                              <Button
                                size="small"
                                onClick={() => setQuizLesson({ lessonId: lesson.lesson.lessonId, lessonName: lesson.lesson.lessonName, mode: 'result' })}
                              >
                                查看结果
                              </Button>
                            ) : null}
                          </Space>
                        ) : null}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </section>
        ))}
        </div>
      )}

      <LessonQuizModal
        open={!!quizLesson}
        lessonId={quizLesson?.lessonId}
        lessonName={quizLesson?.lessonName}
        initialMode={quizLesson?.mode}
        onClose={() => setQuizLesson(null)}
        onPassed={() => {
          message.success('测验通过，该课时已完成');
          refreshQuizStatus();
        }}
      />
    </div>
  );
}
