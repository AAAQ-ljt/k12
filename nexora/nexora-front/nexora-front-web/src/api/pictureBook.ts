import { del, get, post } from './request';

export interface PictureBookPage {
  text: string;
  imageFile?: string;
}

export interface PictureBookScript {
  type: string;
  pages: PictureBookPage[];
  /** 全部页面插图均失败时的原因（用户可读） */
  imageError?: string;
}

export interface PictureBookItem {
  resourceId: string;
  resourceName?: string;
  stage?: string;
  /** 绘本分页 JSON */
  extJson?: string;
  createTime?: string;
}

export function parsePictureBook(extJson?: string): PictureBookScript | null {
  if (!extJson) {
    return null;
  }
  try {
    const parsed = JSON.parse(extJson);
    if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.pages)) {
      return null;
    }
    return parsed as PictureBookScript;
  } catch {
    return null;
  }
}

/** 绘本生成任务（异步编排，前端轮询） */
export interface PictureBookTask {
  taskId: string;
  userId?: string;
  stage?: string;
  topic?: string;
  /** PENDING / STORY_GENERATING / STORY_DONE / IMAGE_GENERATING / COMPLETED / FAILED */
  status: string;
  current: number;
  total: number;
  message?: string;
  title?: string;
  bookResourceId?: string;
}

/**
 * 提交绘本生成任务：立即返回 taskId，不再阻塞等待
 */
export function generatePictureBook(topic: string): Promise<PictureBookTask> {
  return post('/pictureBook/generate', { topic });
}

/**
 * 查询绘本生成任务进度
 */
export function getPictureBookTask(taskId: string): Promise<PictureBookTask> {
  return get('/pictureBook/task', { taskId });
}

export function loadMyPictureBooks(): Promise<PictureBookItem[]> {
  return get('/pictureBook/myList');
}

export function deletePictureBook(resourceId: string): Promise<void> {
  return del('/pictureBook/del', { resourceId });
}

/** 绘本页插图（公开直连） */
export function pictureBookImageUrl(resourceId: string, page: number): string {
  return `/api/pictureBook/image/${resourceId}?page=${page}`;
}