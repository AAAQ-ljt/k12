import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

export interface LearningOverview {
  studentCount: number;
  courseActiveStudents: number;
  courseAvgProgress: number;
  courseTotalDuration: number;
  practiceTotal: number;
  practiceCorrect: number;
  practiceAccuracy: number;
  wikiResourceCount: number;
  wikiActiveUsers: number;
  aiMessageCount: number;
  aiActiveUsers: number;
  aiTotalTokens: number;
  masteryAvgScore: number;
}

export interface LearningUserQuery extends PageParam {
  /** 账号 / 昵称 / 邮箱统一模糊搜索 */
  usernameFuzzy?: string;
  nickNameFuzzy?: string;
  emailFuzzy?: string;
  stage?: string;
  grade?: string;
  status?: number;
}

export interface LearningUserSummary {
  userId: string;
  username: string;
  email: string;
  nickName?: string;
  stage?: string;
  grade?: string;
  status: number;
  lastLoginTime?: string;
  courseCourseCount: number;
  courseFinishedCount: number;
  courseAvgProgress: number;
  practiceCount: number;
  practiceCorrectCount: number;
  practiceAccuracy: number;
  wikiResourceCount: number;
  wikiResourceBytes: number;
  aiSessionCount: number;
  aiMessageCount: number;
  aiTokenCount: number;
  masteryAvgScore: number;
  masteryMasteredCount: number;
}

export interface LearningUserInfo {
  userId: string;
  username: string;
  email: string;
  nickName?: string;
  roleType: number;
  stage?: string;
  grade?: string;
  sex?: number;
  status: number;
  lastLoginTime?: string;
  createTime?: string;
}

export interface CourseStudyProgressItem {
  courseId: string;
  courseName?: string;
  studiedLessons: number;
  totalLessons: number;
  progress: number;
  studyDuration: number;
  finishTime?: string;
  updateTime?: string;
}

export interface PracticeKnowledgePointItem {
  knowledgePointId: string;
  knowledgePointName?: string;
  practiceCount: number;
  correctCount: number;
  accuracy: number;
}

export interface PracticeQuestionTypeItem {
  questionType: number;
  practiceCount: number;
  correctCount: number;
  accuracy: number;
}

export interface KnowledgeResourceTypeItem {
  resourceType: string;
  resourceCount: number;
  sizeMb: number;
}

export interface KnowledgeResourceItem {
  resourceId: string;
  resourceName: string;
  resourceType: string;
  fileSize?: number;
  ownerId?: string;
  status: number;
  createTime?: string;
}

export interface AiIntentItem {
  intent: string;
  messageCount: number;
  tokenCount: number;
}

export interface AiRecentMessageItem {
  messageId: string;
  sessionId: string;
  intent?: string;
  userMessage?: string;
  status?: number;
  promptTokens?: number;
  completionTokens?: number;
  createTime?: string;
}

export interface KnowledgeMasteryItem {
  knowledgePointId: string;
  knowledgePointName?: string;
  masteryScore: number;
  status: number;
  practiceCount: number;
  correctCount: number;
  accuracy: number;
  lastPracticeTime?: string;
  nextReviewTime?: string;
}

export interface LearningUserDetail {
  userInfo: LearningUserInfo;
  courseCount: number;
  courseFinishedCount: number;
  courseAvgProgress: number;
  courseStudyDuration: number;
  practiceCount: number;
  practiceCorrectCount: number;
  practiceAccuracy: number;
  practiceTotalScore: number;
  wikiResourceCount: number;
  wikiResourceBytes: number;
  wikiResourceUsedMb: number;
  wikiQuotaPercent: number;
  aiSessionCount: number;
  aiMessageCount: number;
  aiTokenCount: number;
  aiAverageTokens: number;
  masteryAvgScore: number;
  masteryMasteredCount: number;
  masteryInProgressCount: number;
  masteryLockedCount: number;
  courseList: CourseStudyProgressItem[];
  practiceKnowledgePoints: PracticeKnowledgePointItem[];
  practiceQuestionTypes: PracticeQuestionTypeItem[];
  knowledgeResources: KnowledgeResourceItem[];
  knowledgeResourceTypes: KnowledgeResourceTypeItem[];
  aiIntents: AiIntentItem[];
  aiRecentMessages: AiRecentMessageItem[];
  masteryList: KnowledgeMasteryItem[];
}

export function loadLearningOverview(): Promise<LearningOverview> {
  return request.get('/learningAnalysis/overview');
}

export function loadLearningUserList(query: LearningUserQuery): Promise<PageResult<LearningUserSummary>> {
  return request.get('/learningAnalysis/loadDataList', { params: query });
}

export function getStudentDetail(userId: string): Promise<LearningUserDetail> {
  return request.get('/learningAnalysis/getStudentDetail', { params: { userId } });
}

/** 学生个人知识库向量化文档 */
export interface StudentKnowledgeDoc {
  docId: string;
  title: string;
  stage?: string;
  knowledgePointId?: string;
  ownerId?: string;
  difficulty?: number;
  dataType?: string;
  content?: string;
  sourceType?: number;
  sourceResourceId?: string;
  sourceUrl?: string;
  vectorStatus: number;
  vectorError?: string;
  chunkCount?: number;
  status: number;
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

export interface StudentKnowledgeDocQuery extends PageParam {
  titleFuzzy?: string;
  stage?: string;
  vectorStatus?: number;
}

export interface StudentKnowledgeSearchResult {
  docId: string;
  title: string;
  stage?: string;
  knowledgePointId?: string;
  difficulty?: number;
  chunkIndex?: number;
  content?: string;
  score?: number;
  searchMode?: string;
  sourceUrl?: string;
  ownerId?: string;
  sourceResourceId?: string;
}

export interface StudentSearchTestParams {
  question: string;
  stage?: string;
  knowledgePointId?: string;
  difficulty?: number;
  topK?: number;
  threshold?: number;
}

export function loadStudentDocList(
  userId: string,
  query: StudentKnowledgeDocQuery,
): Promise<PageResult<StudentKnowledgeDoc>> {
  return request.get('/learningAnalysis/studentDocList', { params: { userId, ...query } });
}

export function loadStudentDocDetail(
  userId: string,
  docId: string,
): Promise<StudentKnowledgeDoc> {
  return request.get('/learningAnalysis/studentDocDetail', { params: { userId, docId } });
}

export function studentSearchTest(
  userId: string,
  params: StudentSearchTestParams,
): Promise<StudentKnowledgeSearchResult[]> {
  return request.post('/learningAnalysis/searchTest', params, { params: { userId } });
}
