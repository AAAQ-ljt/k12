import { del, get, post } from './request';

export interface PictureBookPage {
  text: string;
  imageFile?: string;
  /** 本页旁白音频相对路径（后端落盘，公开直连接口播放） */
  audioFile?: string;
  /** 本页旁白实际使用的音色（换音色重录的判断依据） */
  audioVoice?: string;
}

export interface PictureBookScript {
  type: string;
  pages: PictureBookPage[];
  /** 全部页面插图均失败时的原因（用户可读） */
  imageError?: string;
  /** 全部页面旁白均失败时的原因（用户可读） */
  audioError?: string;
  /** 最近一次整本合成所用的音色（阅读器音色选择器初始值） */
  voice?: string;
}

/** MiMo 预置音色（mimo-v2.5-tts），与后端 TtsProvider 白名单一致 */
export const PICTURE_BOOK_VOICES = ['mimo_default', '冰糖', '茉莉', '苏打', '白桦', 'Mia', 'Chloe', 'Milo', 'Dean'];

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
    // 数据净化：过滤 pages 中的 null/非对象元素，防止渲染崩溃
    parsed.pages = parsed.pages.filter((p: unknown) => p && typeof p === 'object');
    if (parsed.pages.length === 0) {
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
  /** PENDING / STORY_GENERATING / STORY_DONE / IMAGE_GENERATING / AUDIO_GENERATING / COMPLETED / FAILED */
  status: string;
  current: number;
  total: number;
  message?: string;
  title?: string;
  bookResourceId?: string;
}

/**
 * 提交绘本生成任务：立即返回 taskId，不再阻塞等待（voice 为旁白音色，可选）
 */
export function generatePictureBook(topic: string, voice?: string): Promise<PictureBookTask> {
  return post('/pictureBook/generate', { topic, voice: voice || undefined });
}

/** 查询绘本生成任务进度 */
export function getPictureBookTask(taskId: string): Promise<PictureBookTask> {
  return get('/pictureBook/task', { taskId });
}

/** 查询绘本详情（含分页脚本 extJson） */
export function getPictureBookInfo(resourceId: string): Promise<PictureBookItem> {
  return get('/pictureBook/getInfo', { resourceId });
}

/** 绘本单页补画任务状态（前端 2s 轮询） */
export interface PictureBookPageFix {
  resourceId: string;
  page: number;
  /** RUNNING / COMPLETED / FAILED */
  status: string;
  message?: string;
}

/** 提交指定页补画任务（异步） */
export function regeneratePictureBookPage(resourceId: string, page: number): Promise<PictureBookPageFix> {
  return post('/pictureBook/regeneratePage', { resourceId, page });
}

/** 查询指定页补画任务状态 */
export function getPictureBookPageFix(resourceId: string, page: number): Promise<PictureBookPageFix> {
  return get('/pictureBook/pageFixTask', { resourceId, page });
}

/** 绘本旁白合成任务状态（前端 2s 轮询） */
export interface PictureBookAudioTask {
  taskId: string;
  resourceId: string;
  /** 页码；null 表示整本任务 */
  page?: number | null;
  /** 本次合成使用的音色 */
  voice?: string;
  /** RUNNING / COMPLETED / FAILED */
  status: string;
  current: number;
  total: number;
  message?: string;
}

/**
 * 提交旁白合成任务（异步）：page 为空=整本补录（跳过同音色已合成页，异音色页重录覆盖），
 * page 给定=单页（重）录制；voice 为用户自选音色（空=学段默认）
 */
export function generatePictureBookAudio(
  resourceId: string,
  page?: number | null,
  voice?: string,
): Promise<PictureBookAudioTask> {
  return post('/pictureBook/generateAudio', {
    resourceId,
    page: page ?? undefined,
    voice: voice || undefined,
  });
}

/** 查询旁白合成任务状态 */
export function getPictureBookAudioTask(taskId: string): Promise<PictureBookAudioTask> {
  return get('/pictureBook/audioTask', { taskId });
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

/** 绘本页旁白音频（公开直连；version 用于重录后击穿浏览器缓存） */
export function pictureBookAudioUrl(resourceId: string, page: number, version = 0): string {
  const query = version > 0 ? `?page=${page}&v=${version}` : `?page=${page}`;
  return `/api/pictureBook/audio/${resourceId}${query}`;
}