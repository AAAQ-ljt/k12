import { get, post } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 学生端课程信息 */
export interface StudentCourseInfo {
  courseId: string;
  courseName: string;
  cover?: string;
  stage: string;
  grade?: string;
  subject: string;
  difficulty?: number;
  description?: string;
  intro?: string;
  lessonCount?: number;
  studyCount?: number;
  status: number;
  createTime?: string;
  updateTime?: string;
}

/** 课时资源 */
export interface StudentLessonResource {
  id: number;
  lessonId: string;
  courseId: string;
  resourceId: string;
  resourceName?: string;
  resourceType?: string;
  description?: string;
  cover?: string;
  duration?: number;
  sort?: number;
}

/** 课时详情 */
export interface StudentLessonDetail {
  lesson: {
    lessonId: string;
    chapterId: string;
    courseId: string;
    lessonName: string;
    summary?: string;
    videoDuration?: number;
    sort?: number;
  };
  resources: StudentLessonResource[];
}

/** 课时通关测验题目 */
export interface LessonQuizQuestion {
  questionId: string;
  questionType: number;
  title: string;
  score: number;
  options: {
    optionId: number;
    optionLabel: string;
    optionContent: string;
  }[];
}

/** 课时通关测验答题面板数据 */
export interface LessonQuizData {
  lessonId: string;
  quizMode: number;
  passScore: number;
  unlockNext: number;
  questions: LessonQuizQuestion[];
}

/** 课时测验提交结果 */
export interface LessonQuizSubmitResult {
  passed: boolean;
  correctCount: number;
  totalCount: number;
  score: number;
  totalScore: number;
  passScore: number;
  results: {
    questionId: string;
    title: string;
    userAnswer: string;
    correctAnswer: string;
    correct: boolean;
    /** 主观题不参与自动判分 */
    subjective?: boolean;
    score: number;
    /** 该题满分（主观题也返回其配置分值） */
    questionScore: number;
    analysis?: string;
  }[];
}

/** 课时测验与解锁状态 */
export interface LessonQuizStatus {
  lessonId: string;
  hasQuiz: boolean;
  strict: boolean;
  passed: boolean;
  unlocked: boolean;
}

/** 章节详情 */
export interface StudentChapterDetail {
  chapter: {
    chapterId: string;
    courseId: string;
    chapterName: string;
    sort?: number;
  };
  lessons: StudentLessonDetail[];
}

/** 课程详情 */
export interface StudentCourseDetail {
  course: StudentCourseInfo;
  chapters: StudentChapterDetail[];
  /** 当前学生是否已加入课程；未加入时章节为空 */
  enrolled?: boolean;
}

/** 加载我已加入的课程 */
export function loadMyCourses(query: PageParam): Promise<PageResult<StudentCourseInfo>> {
  return get('/courseInfo/loadMyCourses', query);
}

/** 加载可加入课程（同年级上架且未加入） */
export function loadJoinCourses(query: PageParam): Promise<PageResult<StudentCourseInfo>> {
  return get('/courseInfo/loadJoinCourses', query);
}

/** 加入课程（幂等） */
export function joinCourse(courseId: string): Promise<void> {
  return post('/courseInfo/join', null, { params: { courseId } });
}

/** 获取课程详情 */
export function getCourseDetail(courseId: string): Promise<StudentCourseDetail> {
  return get('/courseInfo/getDetail', { courseId });
}

/** 获取课时通关测验答题面板数据（未配置返回 null） */
export function getLessonQuiz(lessonId: string): Promise<LessonQuizData | null> {
  return get('/courseInfo/lessonQuiz', { lessonId });
}

/** 提交课时通关测验（duration 为答题用时秒） */
export function submitLessonQuiz(
  lessonId: string,
  answers: { questionId: string; answer: string }[],
  duration?: number,
): Promise<LessonQuizSubmitResult> {
  return post('/courseInfo/lessonQuizSubmit', { lessonId, answers, duration });
}

/** 获取课程内课时测验与解锁状态 */
export function getLessonQuizStatus(courseId: string): Promise<LessonQuizStatus[]> {
  return get('/courseInfo/lessonQuizStatus', { courseId });
}
