import dayjs from 'dayjs';
import { GRADE_OPTIONS, STAGE_OPTIONS, gradeToStage } from '@/types/common';

/** 编程题库学段：仅小学高 / 初中 / 高中（编程题不含小学低年级） */
export const CODING_STAGE_OPTIONS = STAGE_OPTIONS.filter(
  (option) => option.value !== 'PRIMARY_LOW',
);

/** 编程题可选年级：过滤掉映射到小学低的年级（一、二年级） */
export const CODING_GRADE_OPTIONS = GRADE_OPTIONS.filter(
  (option) => gradeToStage(option.value) !== 'PRIMARY_LOW',
);

/** 编程题难度：1入门 2基础 3进阶 4挑战 */
export const CODING_DIFFICULTY_OPTIONS = [
  { label: '入门', value: 1 },
  { label: '基础', value: 2 },
  { label: '进阶', value: 3 },
  { label: '挑战', value: 4 },
];

/** 难度映射（彩色 Tag） */
export const CODING_DIFFICULTY_MAP: Record<string, { text: string; color: string }> = {
  '1': { text: '入门', color: 'green' },
  '2': { text: '基础', color: 'blue' },
  '3': { text: '进阶', color: 'orange' },
  '4': { text: '挑战', color: 'red' },
};

/** 难度 → 建议积分（与后端 CodingProblemController 保持一致） */
export const DIFFICULTY_SUGGEST_SCORE: Record<number, number> = {
  1: 10,
  2: 20,
  3: 30,
  4: 50,
};

/** 编程题状态：1上架 0下架 */
export const CODING_PROBLEM_STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
];

/** 编程题状态映射 */
export const CODING_PROBLEM_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '1': { text: '上架', color: 'green' },
  '0': { text: '下架', color: 'red' },
};

/** 判定方式：1关键词包含 2输出精确匹配 3正则
 *  选型建议（对齐判分公平性口径，见 docs/二期规划设计 §5.1.5-C）：
 *  - 有唯一确定输出（计算结果、格式化输出）→ 输出精确匹配，且必须写「输出要求 + 输出示例」；
 *  - 打印中间过程（排序过程、循环过程）→ 关键词包含，需填写全部要出现的内容；
 *  - 要点必须出现但顺序自由 → 正则匹配。 */
export const JUDGE_TYPE_OPTIONS = [
  { label: '关键词包含（输出需出现全部关键词）', value: 1 },
  { label: '输出精确匹配（需填输出要求+示例）', value: 2 },
  { label: '正则匹配', value: 3 },
];

/** 判定方式映射 */
export const JUDGE_TYPE_MAP: Record<string, { text: string; color: string }> = {
  '1': { text: '关键词包含', color: 'geekblue' },
  '2': { text: '输出精确匹配', color: 'cyan' },
  '3': { text: '正则匹配', color: 'purple' },
};

/** 比赛状态：0草稿 1已发布 2已结束 */
export const CONTEST_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已结束', value: 2 },
];

/** 比赛状态映射 */
export const CONTEST_STATUS_MAP: Record<string, { text: string; color: string }> = {
  '0': { text: '草稿', color: 'default' },
  '1': { text: '已发布', color: 'green' },
  '2': { text: '已结束', color: 'red' },
};

export interface ContestTimePhase {
  text: string;
  color: string;
}

/** 按当前时间计算比赛时间相位：未开始 / 进行中 / 已结束（status=2 提前结束时固定已结束） */
export function getContestTimePhase(
  startTime?: string,
  endTime?: string,
  status?: number,
): ContestTimePhase {
  if (status === 2) {
    return { text: '已结束', color: 'default' };
  }
  if (!startTime || !endTime) {
    return { text: '时间待定', color: 'default' };
  }
  const now = dayjs();
  if (now.isBefore(dayjs(startTime))) {
    return { text: '未开始', color: 'blue' };
  }
  if (now.isAfter(dayjs(endTime))) {
    return { text: '已结束', color: 'default' };
  }
  return { text: '进行中', color: 'gold' };
}
