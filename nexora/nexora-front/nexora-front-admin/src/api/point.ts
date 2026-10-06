import { request } from './request';

/**
 * 管理端积分运营（二期 A-8）接口。
 *
 * 与后端 PointInfoAdminController 一一对应：
 * - /pointInfo/accountList：学生积分总览（累计积分倒序，含昵称/学段/等级）
 * - /pointInfo/loadDataList：积分流水审计（学生/来源/时间筛选）
 * - /pointInfo/add：人工补分（正数、必填原因，走服务端唯一发分入口）
 *
 * 积分规则（GAME 组）复用 /systemSetting/configList 与 /systemSetting/config，不另开接口。
 */

/** 积分流水（管理端审计视图，与学生端同一张表） */
export interface PointRecordItem {
  recordId: number;
  userId: string;
  stage?: string;
  /** 来源：SIGN_IN/SIGN_IN/LESSON_QUIZ/PATH_TEST/CODING_PROBLEM/PICTURE_BOOK/WIKI_CONFIRM/MASTERY/BADGE/ADJUST */
  bizType: string;
  /** 业务幂等键（审计用，能看到同一事件的唯一标识） */
  bizId: string;
  points: number;
  balanceAfter?: number;
  reason?: string;
  createTime?: string;
}

/** 学生积分总览行 */
export interface PointAccountRow {
  userId: string;
  nickName?: string;
  stage?: string;
  level?: number;
  points: number;
}

/** 流水审计返回（时间倒序最近 N 条 + 命中筛选总数） */
export interface PointRecordPage {
  totalCount: number;
  pageSize: number;
  pageNo: number;
  pageTotal: number;
  list: PointRecordItem[];
}

/** 学生积分总览（累计积分倒序，最多 200 条） */
export function loadPointAccounts() {
  return request.get<PointAccountRow[]>('/pointInfo/accountList');
}

/** 积分流水审计 */
export function loadPointRecords(params: {
  userId?: string;
  bizType?: string;
  createTimeStart?: string;
  createTimeEnd?: string;
  pageSize?: number;
}) {
  return request.get<PointRecordPage>('/pointInfo/loadDataList', { params });
}

/** 人工补分：返回本次实际发放的积分（0 表示被幂等/每日上限拦下） */
export function addPoints(data: { userId: string; stage?: string; points: number; reason: string }) {
  return request.post<number>('/pointInfo/add', data);
}
