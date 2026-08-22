import { del, get } from './request';

export interface AnimationStep {
  title: string;
  explain: string;
  svg?: string;
}

export interface AnimationScript {
  title: string;
  steps: AnimationStep[];
}

/** 动画产物（个人知识库 ANIMATION 资源） */
export interface AnimationResource {
  resourceId: string;
  resourceName?: string;
  /** 动画脚本 JSON */
  extJson?: string;
  stage?: string;
  status?: number;
  createTime?: string;
}

export function parseAnimationScript(content?: string): AnimationScript | null {
  if (!content) {
    return null;
  }
  try {
    const parsed = JSON.parse(content);
    if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.steps)) {
      return null;
    }
    return parsed as AnimationScript;
  } catch {
    return null;
  }
}

export function loadMyAnimationList(): Promise<AnimationResource[]> {
  return get('/animation/myList');
}

export function getAnimationResource(resourceId: string): Promise<AnimationResource> {
  return get('/animation/getInfo', { resourceId });
}

export function deleteAnimationResource(resourceId: string): Promise<void> {
  return del('/animation/del', { resourceId });
}