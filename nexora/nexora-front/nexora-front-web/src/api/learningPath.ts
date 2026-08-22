import { del, get, post } from './request';

export interface LearningPlanStep {
  title: string;
  desc: string;
  /** learn 学习 / practice 练习 / review 复习 */
  kind: 'learn' | 'practice' | 'review';
}

export interface LearningPlan {
  title: string;
  steps: LearningPlanStep[];
}

export interface LearningPathRecord {
  recordId: string;
  userId?: string;
  stage?: string;
  type?: string;
  title?: string;
  /** 计划 JSON */
  content?: string;
  status?: number;
  createTime?: string;
}

export function parseLearningPlan(content?: string): LearningPlan | null {
  if (!content) {
    return null;
  }
  try {
    const parsed = JSON.parse(content);
    if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.steps)) {
      return null;
    }
    return parsed as LearningPlan;
  } catch {
    return null;
  }
}

export function generateLearningPath(): Promise<LearningPathRecord> {
  return post('/learningPath/generate');
}

export function loadMyLearningPaths(): Promise<LearningPathRecord[]> {
  return get('/learningPath/myList');
}

export function deleteLearningPath(recordId: string): Promise<void> {
  return del('/learningPath/del', { recordId });
}