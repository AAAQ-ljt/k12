import { get } from './request';
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
}

/** 加载当前年级可用课程 */
export function loadMyCourses(query: PageParam): Promise<PageResult<StudentCourseInfo>> {
  return get('/courseInfo/loadMyCourses', query);
}

/** 获取课程详情 */
export function getCourseDetail(courseId: string): Promise<StudentCourseDetail> {
  return get('/courseInfo/getDetail', { courseId });
}
