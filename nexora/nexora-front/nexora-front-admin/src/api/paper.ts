import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 试卷实体 */
export interface PaperInfo {
  paperId: string;
  paperName: string;
  paperType: number; // 0练习卷 1考试卷
  stage?: string;
  grade?: string;
  totalScore?: number;
  status?: number;
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 试卷查询参数 */
export interface PaperInfoQuery extends PageParam {
  paperNameFuzzy?: string;
  grade?: string;
  paperType?: number;
  status?: number;
}

/** 试卷题目保存项 */
export interface PaperQuestionItem {
  questionId: string;
  score: number;
}

/** 试卷大题保存项 */
export interface PaperGroupItem {
  groupId?: string;
  groupName: string;
  questions: PaperQuestionItem[];
}

/** 试卷保存参数 */
export interface PaperSaveDTO {
  paper: Partial<PaperInfo>;
  groups: PaperGroupItem[];
}

/** 试卷预览题目 */
export interface PaperQuestionVO {
  questionId: string;
  title: string;
  questionType: number;
  difficulty: number;
  score: number;
  sort: number;
}

/** 试卷预览大题 */
export interface PaperGroupVO {
  groupId: string;
  groupName: string;
  questions: PaperQuestionVO[];
}

/** 试卷详情 */
export interface PaperDetail {
  paper: PaperInfo;
  groups: PaperGroupVO[];
}

/** 分页加载试卷 */
export function loadDataList(query: PaperInfoQuery): Promise<PageResult<PaperInfo>> {
  return request.get('/paperInfo/loadDataList', { params: query });
}

/** 获取试卷详情 */
export function getInfo(paperId: string): Promise<PaperDetail> {
  return request.get('/paperInfo/getInfo', { params: { paperId } });
}

/** 保存试卷（含大题和题目） */
export function savePaper(data: PaperSaveDTO): Promise<void> {
  return request.post('/paperInfo/save', data);
}

/** 删除试卷 */
export function delPaper(paperId: string): Promise<void> {
  return request.delete('/paperInfo/del', { params: { paperId } });
}
