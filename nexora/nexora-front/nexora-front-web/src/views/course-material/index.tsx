import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { App, Button, Empty, Spin, Tabs, Tag } from 'antd';
import { BookOpen, Layers, GraduationCap, PlusCircle } from 'lucide-react';
import {
  joinCourse,
  loadJoinCourses,
  loadMyCourses,
  type StudentCourseInfo,
} from '@/api/course';
import { getGradeText } from '@/types/common';
import { useAuthStore } from '@/stores/auth';
import styles from './index.module.scss';

export default function CourseMaterial() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const userInfo = useAuthStore((state) => state.userInfo);
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState<string | null>(null);
  const [myCourses, setMyCourses] = useState<StudentCourseInfo[]>([]);
  const [joinCourses, setJoinCourses] = useState<StudentCourseInfo[]>([]);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [myResult, joinResult] = await Promise.all([
        loadMyCourses({ pageNo: 1, pageSize: 100 }),
        loadJoinCourses({ pageNo: 1, pageSize: 100 }),
      ]);
      setMyCourses(myResult.list || []);
      setJoinCourses(joinResult.list || []);
    } catch {
      // 请求层已统一提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadAll();
  }, [loadAll]);

  const gradeText = useMemo(() => getGradeText(userInfo), [userInfo]);

  const handleOpen = (course: StudentCourseInfo) => {
    navigate(`/course-material/${course.courseId}`);
  };

  const handleJoin = async (course: StudentCourseInfo) => {
    setJoining(course.courseId);
    try {
      await joinCourse(course.courseId);
      message.success(`已加入课程「${course.courseName}」，开始学习吧`);
      await loadAll();
    } catch {
      // 请求层已统一提示
    } finally {
      setJoining(null);
    }
  };

  const renderCourseCard = (course: StudentCourseInfo, joinable: boolean) => (
    <button
      key={course.courseId}
      className={styles.resourceCard}
      onClick={() => (joinable ? undefined : handleOpen(course))}
    >
      <div className={styles.cardTop}>
        <span className={styles.cardIcon}>
          <BookOpen size={22} />
        </span>
        <Tag color="geekblue" className={styles.typeTag}>
          {course.subject}
        </Tag>
      </div>
      <div className={styles.cardTitle}>{course.courseName}</div>
      <div className={styles.cardDesc}>{course.description || '暂无简介'}</div>
      <div className={styles.cardFooter}>
        {course.grade ? (
          <span>
            <GraduationCap size={13} />
            {course.grade}
          </span>
        ) : null}
        {course.lessonCount !== undefined ? (
          <span>
            <Layers size={13} />
            {course.lessonCount} 课时
          </span>
        ) : null}
      </div>
      {joinable ? (
        <Button
          type="primary"
          size="small"
          block
          style={{ marginTop: 10 }}
          icon={<PlusCircle size={14} />}
          loading={joining === course.courseId}
          onClick={(e) => {
            e.stopPropagation();
            void handleJoin(course);
          }}
        >
          加入课程
        </Button>
      ) : null}
    </button>
  );

  return (
    <div className={styles.materialPage}>
      <header className={styles.pageHeader}>
        <div>
          <h2>课程教材</h2>
          <p>当前{gradeText || '年级'}的课程，加入课程后即可开始学习</p>
        </div>
      </header>

      {loading ? (
        <div className={styles.loadingBox}>
          <Spin />
        </div>
      ) : (
        <Tabs
          defaultActiveKey="my"
          items={[
            {
              key: 'my',
              label: `我的课程 (${myCourses.length})`,
              children: (
                <div className={styles.resourceGrid}>
                  {myCourses.length === 0 ? (
                    <Empty
                      description="还没有加入任何课程，去「可加入课程」看看吧"
                      className={styles.emptyBox}
                    />
                  ) : (
                    myCourses.map((course) => renderCourseCard(course, false))
                  )}
                </div>
              ),
            },
            {
              key: 'join',
              label: `可加入课程 (${joinCourses.length})`,
              children: (
                <div className={styles.resourceGrid}>
                  {joinCourses.length === 0 ? (
                    <Empty description="当前年级的课程都已加入" className={styles.emptyBox} />
                  ) : (
                    joinCourses.map((course) => renderCourseCard(course, true))
                  )}
                </div>
              ),
            },
          ]}
        />
      )}
    </div>
  );
}
