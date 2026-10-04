import { del, get, post, put } from './request';

/** 知识页状态：vectorStatus 0草稿 1向量化中 2已入库 3失败 */
export type WikiVectorStatus = 0 | 1 | 2 | 3;

export interface StudentWikiDoc {
  docId: string;
  title: string;
  stage: string;
  knowledgePointId: string;
  ownerId?: string;
  difficulty: number;
  dataType: string;
  content: string;
  sourceType: number;
  sourceResourceId?: string;
  sourceUrl?: string;
  vectorStatus?: WikiVectorStatus;
  vectorError?: string;
  chunkCount: number;
  status: number;
  /** 所属知识页子文件夹；空=根目录 */
  folderId?: string;
  createTime?: string;
  updateTime?: string;
}

export interface StudentWikiProfile {
  userId: string;
  learningGoal?: string;
  keyQuestions?: string;
  interestSubjects?: string;
  aliasTerms?: string;
  updateTime?: string;
}

export function generateStudentWiki(resourceId: string): Promise<StudentWikiDoc> {
  // AI 整理为同步调用(长文档分段多轮),超时给足 5 分钟
  return post('/studentWiki/generate', null, { params: { resourceId }, timeout: 300000 });
}

export function updateStudentWikiDraft(docId: string, content: string): Promise<StudentWikiDoc> {
  return put('/studentWiki/draft', { docId, content });
}

export function confirmStudentWiki(docId: string): Promise<StudentWikiDoc> {
  return post('/studentWiki/confirm', null, { params: { docId } });
}

export function getStudentWiki(docId: string): Promise<StudentWikiDoc> {
  return get('/studentWiki/getInfo', { docId });
}

export function loadStudentWikiList(
  resourceId?: string,
  folderId?: string,
): Promise<StudentWikiDoc[]> {
  const params: Record<string, string> = {};
  if (resourceId) {
    params.resourceId = resourceId;
  }
  if (folderId) {
    params.folderId = folderId;
  }
  return get('/studentWiki/list', Object.keys(params).length ? params : undefined);
}

/** 新建知识页子文件夹（parentFolderId 不传=建在知识页根目录下），返回文件夹ID */
export function createWikiFolder(name: string, parentFolderId?: string): Promise<string> {
  return post('/studentWiki/folder/create', null, { params: { name, parentFolderId } });
}

/** 移动知识页到子文件夹（folderId 不传=移回知识页根目录） */
export function moveWikiDoc(docId: string, folderId?: string): Promise<void> {
  return post('/studentWiki/move', null, { params: { docId, folderId } });
}

/** 删除知识页子文件夹（其中知识页自动回到根目录） */
export function deleteWikiFolder(folderId: string): Promise<void> {
  return del('/studentWiki/folder', { folderId });
}

export function deleteStudentWiki(docId: string): Promise<void> {
  return del('/studentWiki/del', { docId });
}

export function loadStudentWikiProfile(): Promise<StudentWikiProfile | null> {
  return get('/studentWiki/profile');
}

export function saveStudentWikiProfile(data: {
  learningGoal?: string;
  keyQuestions?: string;
  interestSubjects?: string;
  aliasTerms?: string;
}): Promise<StudentWikiProfile> {
  return put('/studentWiki/profile', data);
}

/** 同步 AI 对话消息为知识页草稿（L3） */
export function syncStudentWikiFromMessage(messageId: string): Promise<StudentWikiDoc> {
  return post('/studentWiki/syncFromMessage', null, { params: { messageId }, timeout: 300000 });
}

/** 同步课程教材为知识页草稿（主线 6，按课程去重） */
export function syncStudentWikiFromCourse(courseId: string): Promise<StudentWikiDoc> {
  return post('/courseInfo/syncWiki', null, { params: { courseId }, timeout: 300000 });
}