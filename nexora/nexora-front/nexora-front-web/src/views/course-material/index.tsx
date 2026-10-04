import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { App, Button, Empty, Input, Progress, Select, Spin, Tabs, Tag } from 'antd';
import { BookOpen, Layers, GraduationCap, PlusCircle, Search } from 'lucide-react';
import {
  joinCourse,
  loadJoinCourses,
  loadMyCourses,
  loadMyCourseProgress,
  type CourseProgress,
  type StudentCourseInfo,
} from '@/api/course';
import { getGradeText, getStageOption } from '@/types/common';
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
  /** 课程学习进度（按 courseId 关联到我的课程卡） */
  const [courseProgress, setCourseProgress] = useState<Record<string, CourseProgress>>({});

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [myResult, joinResult] = await Promise.all([
        loadMyCourses({ pageNo: 1, pageSize: 100 }),
        loadJoinCourses({ pageNo: 1, pageSize: 100 }),
      ]);
      setMyCourses(myResult.list || []);
      setJoinCourses(joinResult.list || []);
      // 学习进度失败不影响课程列表展示
      try {
        const progressList = await loadMyCourseProgress();
        const map: Record<string, CourseProgress> = {};
        for (const item of progressList || []) {
          map[item.courseId] = item;
        }
        setCourseProgress(map);
      } catch {
        setCourseProgress({});
      }
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

  /** 「我的课程」筛选：关键词 + 学段 + 学科（前端过滤；学生加入的课程量级小，即时筛选体验更好） */
  const [myKeyword, setMyKeyword] = useState('');
  const [myStage, setMyStage] = useState<string>();
  const [mySubject, setMySubject] = useState<string>();

  const myStageOptions = useMemo(() => {
    const stages = Array.from(new Set(myCourses.map((course) => course.stage).filter(Boolean)));
    return stages.map((value) => ({ value, label: getStageOption(value)?.label || value }));
  }, [myCourses]);

  const mySubjectOptions = useMemo(() => {
    const subjects = Array.from(new Set(myCourses.map((course) => course.subject).filter(Boolean)));
    return subjects.map((value) => ({ value, label: value }));
  }, [myCourses]);

  const filteredMyCourses = useMemo(() => {
    const keyword = myKeyword.trim().toLowerCase();
    return myCourses.filter((course) => {
      if (myStage && course.stage !== myStage) {
        return false;
      }
      if (mySubject && course.subject !== mySubject) {
        return false;
      }
      return !keyword || (course.courseName || '').toLowerCase().includes(keyword);
    });
  }, [myCourses, myStage, mySubject, myKeyword]);

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
      {!joinable ? (
        <div className={styles.cardProgress}>
          <Progress
            percent={courseProgress[course.courseId]?.progress || 0}
            size="small"
            showInfo
            strokeColor="#1677ff"
          />
          <span className={styles.cardProgressTip}>
            {courseProgress[course.courseId]
              ? `已完成 ${courseProgress[course.courseId].finishedLessons}/${courseProgress[course.courseId].lessonCount} 课时`
              : '学习展开课时后自动记录'}
          </span>
        </div>
      ) : null}
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
                <div>
                  <div className={styles.filterBar}>
                    <Input
                      allowClear
                      className={styles.searchInput}
                      prefix={<Search size={14} />}
                      placeholder="搜索课程名称"
                      value={myKeyword}
                      onChange={(e) => setMyKeyword(e.target.value)}
                    />
                    <Select
                      allowClear
                      placeholder="全部学段"
                      className={styles.filterSelect}
                      options={myStageOptions}
                      value={myStage}
                      onChange={(value) => setMyStage(value)}
                    />
                    <Select
                      allowClear
                      placeholder="全部学科"
                      className={styles.filterSelect}
                      options={mySubjectOptions}
                      value={mySubject}
                      onChange={(value) => setMySubject(value)}
                    />
                    <span className={styles.filterCount}>共 {filteredMyCourses.length} 门</span>
                  </div>
                  <div className={styles.resourceGrid}>
                    {myCourses.length === 0 ? (
                      <Empty
                        description="还没有加入任何课程，去「可加入课程」看看吧"
                        className={styles.emptyBox}
                      />
                    ) : filteredMyCourses.length === 0 ? (
                      <Empty description="没有符合筛选条件的课程" className={styles.emptyBox} />
                    ) : (
                      filteredMyCourses.map((course) => renderCourseCard(course, false))
                    )}
                  </div>
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
