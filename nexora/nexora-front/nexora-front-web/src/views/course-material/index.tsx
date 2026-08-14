import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Empty, Spin, Tag } from 'antd';
import { BookOpen, Layers, GraduationCap } from 'lucide-react';
import { loadMyCourses, type StudentCourseInfo } from '@/api/course';
import { getGradeText } from '@/types/common';
import { useAuthStore } from '@/stores/auth';
import styles from './index.module.scss';

export default function CourseMaterial() {
  const navigate = useNavigate();
  const userInfo = useAuthStore((state) => state.userInfo);
  const [loading, setLoading] = useState(true);
  const [courses, setCourses] = useState<StudentCourseInfo[]>([]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    loadMyCourses({ pageNo: 1, pageSize: 100 })
      .then((result) => {
        if (active) {
          setCourses(result.list || []);
        }
      })
      .catch(() => {
        // 请求层已统一提示
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  const gradeText = useMemo(() => getGradeText(userInfo), [userInfo]);

  const handleOpen = (course: StudentCourseInfo) => {
    navigate(`/course-material/${course.courseId}`);
  };

  return (
    <div className={styles.materialPage}>
      <header className={styles.pageHeader}>
        <div>
          <h2>课程教材</h2>
          <p>当前{gradeText || '年级'}可用的课程，点击课程进入学习</p>
        </div>
      </header>

      <div className={styles.resourceGrid}>
        {loading ? (
          <div className={styles.loadingBox}>
            <Spin />
          </div>
        ) : courses.length === 0 ? (
          <Empty description="当前年级暂无可用课程" className={styles.emptyBox} />
        ) : (
          courses.map((course) => {
            return (
              <button
                key={course.courseId}
                className={styles.resourceCard}
                onClick={() => handleOpen(course)}
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
                <div className={styles.cardDesc}>
                  {course.description || '暂无简介'}
                </div>
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
              </button>
            );
          })
        )}
      </div>
    </div>
  );
}
