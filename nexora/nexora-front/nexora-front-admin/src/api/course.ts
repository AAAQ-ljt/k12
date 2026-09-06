import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 课程实体（对应后端 CourseInfo PO） */
export interface CourseInfo {
  courseId: string;
  courseName: string;
  cover?: string;
  stage: string;
  grade?: string;
  subject: string;
  difficulty?: number; // 1-3
  description?: string;
  intro?: string;
  lessonCount?: number;
  studyCount?: number;
  sort?: number;
  status: number; // 0=下架 1=上架
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 课程查询参数 */
export interface CourseInfoQuery extends PageParam {
  courseName?: string;
  stage?: string;
  grade?: string;
  status?: number;
}

/** 章节实体 */
export interface CourseChapter {
  chapterId: string;
  courseId: string;
  chapterName: string;
  sort?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

/** 课时实体 */
export interface CourseChapterLesson {
  lessonId: string;
  chapterId: string;
  courseId: string;
  lessonName: string;
  summary?: string;
  videoDuration?: number;
  sort?: number;
  status?: number;
  /** 是否已配置通关测验（课程详情返回） */
  quizEnabled?: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 课时绑定资源（对外视图） */
export interface CourseLessonResource {
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
  createTime?: string;
}

/** 课时详情 */
export interface CourseLessonDetail {
  lesson: CourseChapterLesson;
  resources: CourseLessonResource[];
}

/** 章节详情 */
export interface CourseChapterDetail {
  chapter: CourseChapter;
  lessons: CourseLessonDetail[];
}

/** 课程详情树 */
export interface CourseDetail {
  course: CourseInfo;
  chapters: CourseChapterDetail[];
}

/** 分页加载课程列表 */
export function loadDataList(query: CourseInfoQuery): Promise<PageResult<CourseInfo>> {
  return request.get('/courseInfo/loadDataList', { params: query });
}

/** 获取课程详情 */
export function getInfo(courseId: string): Promise<CourseInfo> {
  return request.get('/courseInfo/getInfo', { params: { courseId } });
}

/** 获取课程详情（课程 + 章节 + 课时 + 资源） */
export function getDetail(courseId: string): Promise<CourseDetail> {
  return request.get('/courseInfo/getDetail', { params: { courseId } });
}

/** 新增课程 */
export function add(data: Partial<CourseInfo>): Promise<void> {
  return request.post('/courseInfo/add', data);
}

/** 修改课程 */
export function update(data: Partial<CourseInfo>): Promise<void> {
  return request.put('/courseInfo/update', data);
}

/** 删除课程 */
export function del(courseId: string): Promise<void> {
  return request.delete('/courseInfo/del', { params: { courseId } });
}

/** 加载课程章节 */
export function loadChapterList(courseId: string): Promise<CourseChapter[]> {
  return request.get('/courseChapter/loadDataList', { params: { courseId } });
}

/** 新增章节 */
export function addChapter(data: Partial<CourseChapter>): Promise<string> {
  return request.post('/courseChapter/add', data);
}

/** 修改章节 */
export function updateChapter(data: Partial<CourseChapter>): Promise<void> {
  return request.put('/courseChapter/update', data);
}

/** 删除章节 */
export function delChapter(chapterId: string): Promise<void> {
  return request.delete('/courseChapter/del', { params: { chapterId } });
}

/** 加载课时列表 */
export function loadLessonList(params: { chapterId?: string; courseId?: string }): Promise<CourseChapterLesson[]> {
  return request.get('/courseChapterLesson/loadDataList', { params });
}

/** 新增课时 */
export function addLesson(data: Partial<CourseChapterLesson>): Promise<string> {
  return request.post('/courseChapterLesson/add', data);
}

/** 修改课时 */
export function updateLesson(data: Partial<CourseChapterLesson>): Promise<void> {
  return request.put('/courseChapterLesson/update', data);
}

/** 删除课时 */
export function delLesson(lessonId: string): Promise<void> {
  return request.delete('/courseChapterLesson/del', { params: { lessonId } });
}

/** 加载课时资源 */
export function loadLessonResources(lessonId: string): Promise<CourseLessonResource[]> {
  return request.get('/courseChapterLessonResource/loadDataList', { params: { lessonId } });
}

/** 批量绑定课时资源 */
export function bindLessonResources(lessonId: string, resourceIds: string[]): Promise<void> {
  return request.post('/courseChapterLessonResource/bind', { lessonId, resourceIds });
}

/** 解绑课时资源 */
export function unbindLessonResource(id: number): Promise<void> {
  return request.delete('/courseChapterLessonResource/del', { params: { id } });
}

/** 课时通关测验配置 */
export interface CourseLessonQuiz {
  lessonId: string;
  courseId?: string;
  quizMode: number; // 0关闭 1题库选题 2AI生成
  questionIds?: string;
  questionCount?: number;
  difficulty?: number;
  passScore?: number;
  unlockNext: number; // 0宽松 1严格门禁
  quizConfig?: string;
  status?: number;
}

/** 课时通关测验详情 */
export interface LessonQuizDetail {
  quiz?: CourseLessonQuiz | null;
  questions?: QuestionLite[];
  /** 每题分值（questionId → 分） */
  questionScores?: Record<string, number>;
  /** 多选题漏选（未错选）按比例部分给分 */
  partialCredit?: boolean;
}

/** 关联题目（轻量回显） */
export interface QuestionLite {
  questionId: string;
  questionType?: number;
  title?: string;
  score?: number;
  difficulty?: number;
  status?: number;
}

/** 课时通关测验保存参数 */
export interface LessonQuizSaveData {
  lessonId: string;
  quizMode: number;
  questionIds?: string[];
  /** 每题分值（questionId → 分） */
  questionScores?: Record<string, number>;
  questionCount?: number;
  difficulty?: number;
  passScore: number;
  unlockNext: number;
  topic?: string;
  /** 多选题漏选（未错选）按比例部分给分 */
  partialCredit?: boolean;
  /** AI 模式挂载的知识点（必选） */
  knowledgePointId?: string;
}

/** AI 出题异步任务状态 */
export interface LessonQuizTask {
  taskId: string;
  lessonId: string;
  /** PENDING / GENERATING / SUCCESS / FAILED */
  status: string;
  step?: string;
  generated?: number;
  total?: number;
  message?: string;
}

/** 获取课时通关测验详情 */
export function getLessonQuizDetail(lessonId: string): Promise<LessonQuizDetail> {
  return request.get('/courseChapterLesson/quizDetail', { params: { lessonId } });
}

/** 保存课时通关测验（quizMode=2 时后端 AI 出题） */
export function saveLessonQuiz(data: LessonQuizSaveData): Promise<void> {
  return request.post('/courseChapterLesson/quizSave', data);
}

/** 关闭课时通关测验 */
export function deleteLessonQuiz(lessonId: string): Promise<void> {
  return request.delete('/courseChapterLesson/quizDel', { params: { lessonId } });
}

/** AI 出题异步任务提交：返回 taskId，轮询 getQuizTask 获取进度 */
export function generateQuizAsync(data: LessonQuizSaveData): Promise<LessonQuizTask> {
  return request.post('/courseChapterLesson/quizGenerateAsync', data);
}

/** 查询 AI 出题任务进度 */
export function getQuizTask(taskId: string): Promise<LessonQuizTask> {
  return request.get('/courseChapterLesson/quizTask', { params: { taskId } });
}
