import { request } from './request';
import type { QuestionOption } from './question';

/** 某难度下单一题型的生成数量 */
export interface QuestionTypeCount {
  questionType: number;
  count: number;
}

/** AI 出题配置 */
export interface QuestionGenerateParams {
  grade: string;
  knowledgePointId: string;
  description?: string;
  resourceIds?: string[];
  easyDistribution: QuestionTypeCount[];
  mediumDistribution: QuestionTypeCount[];
  hardDistribution: QuestionTypeCount[];
}

/** 题目结构化草稿 */
export interface QuestionDraft {
  grade: string;
  stage: string;
  knowledgePointId: string;
  knowledgePointName: string;
  difficulty: number;
  questionType: number;
  score: number;
  title: string;
  answer: string;
  analysis?: string;
  questionImage?: string;
  options?: QuestionOption[];
}

/** AI 生成题目 Markdown */
export function generateQuestions(data: QuestionGenerateParams): Promise<string> {
  return request.post('/questionGenerate/generate', data, { timeout: 180000 });
}

/** 题目 Markdown -> 结构化草稿 */
export function parseQuestions(markdown: string): Promise<QuestionDraft[]> {
  return request.post('/questionGenerate/parseMd', { markdown });
}
