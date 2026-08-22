import { del, get, post } from './request';

export interface PictureBookPage {
  text: string;
  imageFile?: string;
}

export interface PictureBookScript {
  type: string;
  pages: PictureBookPage[];
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

export function generatePictureBook(topic: string): Promise<PictureBookItem> {
  return post('/pictureBook/generate', { topic });
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