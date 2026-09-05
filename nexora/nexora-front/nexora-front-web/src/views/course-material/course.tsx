import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { App, Button, Empty, Skeleton, Tag } from 'antd';
import {
  ArrowLeft,
  BookOpen,
  FileText,
  Film,
  GraduationCap,
  Image as ImageIcon,
  Layers,
  Link2,
  Sparkles,
} from 'lucide-react';
import { getCourseDetail, type StudentCourseDetail, type StudentLessonResource } from '@/api/course';
import { syncStudentWikiFromCourse } from '@/api/studentWiki';
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

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotFound(false);
    getCourseDetail(courseId)
      .then((data) => {
        if (active) {
          setDetail(data);
        }
      })
      .catch(() => {
        if (active) {
          setNotFound(true);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [courseId]);

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
    navigate(`/course-material/resource/${resource.resourceId}`, {
      state: { from: `/course-material/${courseId}` },
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
        </div>
      </header>

      {course.description ? (
        <section className={styles.descriptionBox}>
          <h3>课程简介</h3>
          <p>{course.description}</p>
        </section>
      ) : null}

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
                chapter.lessons.map((lesson, lessonIndex) => (
                  <div key={lesson.lesson.lessonId} className={styles.lessonCard}>
                    <div className={styles.lessonTitle}>
                      <span>
                        {chapterIndex + 1}.{lessonIndex + 1}
                      </span>
                      <h4>{lesson.lesson.lessonName}</h4>
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
                  </div>
                ))
              )}
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}
