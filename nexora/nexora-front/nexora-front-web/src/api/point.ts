import { get, post } from './request';

/**
 * 学生端积分（二期·积分游戏化）。
 *
 * 与后端 PointInfoController 一一对应：
 * - /pointInfo/getMyAccount：成长中心概览（等级/进度/连续天数/今日积分）
 * - /pointInfo/loadDataList：我的积分明细
 * - /pointInfo/getBadgeList：徽章墙（含进度）
 * - /pointInfo/getRankList：排行榜（type=week 周榜 / 缺省累计榜；小学段不开放）
 *
 * 积分全部由服务端按学习事件结算，前端只读，没有任何加分接口。
 */

/** 积分账户概览（学段化命名由前端决定：小学段叫星星，初高段叫积分 + 段位） */
export interface PointAccountVO {
  /** 累计获得积分（只增不减） */
  totalPoints: number;
  /** 可用积分（兑换后减少） */
  availablePoints: number;
  /** 当前等级（1 起） */
  level: number;
  /** 段位名（初高段：青铜 → 王者；小学段为空，展示「N 颗星」） */
  levelName?: string | null;
  /** 本级起点积分（成长环进度条的左端） */
  levelFloor: number;
  /** 升到下一级还需的积分；已满级为 0 */
  nextLevelPoints: number;
  /** 连续学习天数 */
  streakDays: number;
  /** 今日已获积分（只统计受每日上限约束的来源） */
  todayPoints: number;
  /** 每日积分上限 */
  dailyCap: number;
}

/** 积分流水 */
export interface PointRecordVO {
  recordId: number;
  userId?: string;
  stage?: string;
  /** 来源：SIGN_IN/PATH_TEST/LESSON_QUIZ/CODING_PROBLEM/PICTURE_BOOK/WIKI_CONFIRM/MASTERY/BADGE… */
  bizType: string;
  bizId: string;
  /** 积分（负数表示扣减） */
  points: number;
  balanceAfter?: number;
  /** 获得原因（面向学生的一句话说明） */
  reason?: string;
  createTime: string;
}

/** 徽章（徽章墙） */
export interface PointBadgeVO {
  badgeId: string;
  name: string;
  description: string;
  /** emoji 图标 */
  icon?: string;
  ruleType: string;
  ruleValue: number;
  rewardPoints: number;
  unlocked: boolean;
  /** 解锁时间（未解锁为 null） */
  unlockedTime?: string | null;
  /** 当前进度值 */
  progress: number;
  /** 进度文案，如「3/7 天」 */
  progressText?: string;
}

/** 排行榜单行（昵称已脱敏，服务端不下发他人 userId） */
export interface PointRankItemVO {
  rankNo: number;
  displayName: string;
  stage?: string;
  level?: number;
  /** 段位名（小学段为空，展示「N 颗星」） */
  levelName?: string | null;
  points: number;
  me?: boolean;
}

/** 排行榜结果 */
export interface PointRankResultVO {
  type: string;
  scope: string;
  /** 小学段不开放排行榜时为 false */
  enabled: boolean;
  list: PointRankItemVO[];
  myRank?: number | null;
  myPoints?: number;
  tip?: string;
}

/** 我的积分账户概览 */
export function getMyPointAccount() {
  return get<PointAccountVO>('/pointInfo/getMyAccount');
}

/** 我的积分明细（按时间倒序） */
export function loadMyPointRecords(params?: {
  bizType?: string;
  createTimeStart?: string;
  createTimeEnd?: string;
  pageSize?: number;
}) {
  return get<PointRecordVO[]>('/pointInfo/loadDataList', params);
}

/** 我的徽章墙 */
export function loadMyBadges() {
  return get<PointBadgeVO[]>('/pointInfo/getBadgeList');
}

/** 排行榜：type=week 周榜 / 缺省累计榜 */
export function loadPointRank(type?: 'total' | 'week') {
  return get<PointRankResultVO>('/pointInfo/getRankList', { type });
}

/** 兑换商品（上传额度扩容 / 朗读音色解锁） */
export interface PointExchangeItemVO {
  itemCode: string;
  itemName: string;
  /** 效果说明 */
  effect: string;
  costPoints: number;
  /** 最多可兑换次数（0=不限） */
  maxTimes: number;
  usedTimes: number;
  /** 还能兑换几次（-1=不限次） */
  remainingTimes: number;
  /** 可用积分是否够 */
  affordable: boolean;
  /** 是否已解锁（音色类） */
  unlocked: boolean;
  /** UPLOAD_QUOTA 容量类 / VOICE 音色类 */
  category: string;
}

/** 我的兑换记录 */
export interface PointExchangeRecordVO {
  exchangeId: string;
  itemCode: string;
  itemName: string;
  costPoints: number;
  balanceAfter: number;
  createTime: string;
}

/** 兑换页数据 */
export interface PointExchangeListVO {
  availablePoints: number;
  items: PointExchangeItemVO[];
  records: PointExchangeRecordVO[];
}

/** 兑换结果 */
export interface PointExchangeResultVO {
  exchangeId: string;
  itemName: string;
  costPoints: number;
  availablePoints: number;
  tip?: string;
}

/** 兑换页：商品 + 我的兑换记录（可用积分以账户接口为准） */
export function loadExchangeList() {
  return get<PointExchangeListVO>('/pointInfo/getExchangeList');
}

/** 兑换权益：只扣可用积分，累计积分不变 */
export function exchangeItem(itemCode: string) {
  return post<PointExchangeResultVO>('/pointInfo/exchange', { itemCode });
}
