import dayjs from 'dayjs';

/**
 * 编程实验室共用工具：难度文案、时间窗/倒计时/用时格式化、比赛阶段判断。
 * 编程页与「我的」页共用，避免两处各写一套时间解析规则。
 */

/** 难度选项（与后端 difficulty 1-4 对齐） */
export const CODING_DIFFICULTY_OPTIONS = [
  { value: 1, label: '★ 入门' },
  { value: 2, label: '★★ 基础' },
  { value: 3, label: '★★★ 进阶' },
  { value: 4, label: '★★★★ 挑战' },
] as const;

/** 难度星级（1-4 星，越界钳制到边界） */
export function difficultyStars(difficulty?: number | null): string {
  const value = Math.min(4, Math.max(1, difficulty ?? 1));
  return '★'.repeat(value);
}

/** 难度文案（入门 / 基础 / 进阶 / 挑战） */
export function difficultyLabel(difficulty?: number | null): string {
  const value = Math.min(4, Math.max(1, difficulty ?? 1));
  return ['入门', '基础', '进阶', '挑战'][value - 1];
}

/** 解析后端时间字符串（yyyy-MM-dd HH:mm:ss，dayjs 兼容空格分隔格式） */
export function parseTime(value?: string | null): number | null {
  if (!value) {
    return null;
  }
  const time = dayjs(value).valueOf();
  return Number.isNaN(time) ? null : time;
}

/** 时间窗文案：MM-DD HH:mm ~ MM-DD HH:mm */
export function formatTimeWindow(startTime?: string | null, endTime?: string | null): string {
  const start = startTime ? dayjs(startTime).format('MM-DD HH:mm') : '';
  const end = endTime ? dayjs(endTime).format('MM-DD HH:mm') : '';
  return start && end ? `${start} ~ ${end}` : start || end || '时间待定';
}

/** 倒计时文案：不足 1 小时显示 mm:ss，否则 hh:mm:ss */
export function formatCountdown(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000));
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const seconds = total % 60;
  const pad = (value: number) => String(value).padStart(2, '0');
  return hours > 0 ? `${pad(hours)}:${pad(minutes)}:${pad(seconds)}` : `${pad(minutes)}:${pad(seconds)}`;
}

/** 用时文案（秒 → x 分 y 秒 / y 秒） */
export function formatDuration(seconds?: number | null): string {
  if (seconds == null) {
    return '—';
  }
  const total = Math.max(0, Math.floor(seconds));
  const minutes = Math.floor(total / 60);
  const rest = total % 60;
  return minutes > 0 ? `${minutes} 分 ${rest} 秒` : `${rest} 秒`;
}

/** 比赛阶段：upcoming 未开始 / running 进行中 / ended 已结束 */
export type ContestPhase = 'upcoming' | 'running' | 'ended';

/** 按 startTime / endTime 与当前时间判断比赛阶段 */
export function contestPhaseOf(
  contest: { startTime?: string | null; endTime?: string | null },
  now: number = Date.now(),
): ContestPhase {
  const start = parseTime(contest.startTime);
  const end = parseTime(contest.endTime);
  if (start != null && now < start) {
    return 'upcoming';
  }
  if (end != null && now >= end) {
    return 'ended';
  }
  return 'running';
}

/** 比赛截止时间：endTime 与「进入时间 + 限时」取更早者 */
export function contestDeadlineOf(
  contest: { endTime?: string | null; durationMinutes?: number | null },
  startedAt?: number | null,
): number | null {
  const end = parseTime(contest.endTime);
  const minutes = contest.durationMinutes;
  if (minutes != null && minutes > 0 && startedAt != null) {
    const byDuration = startedAt + minutes * 60 * 1000;
    return end == null ? byDuration : Math.min(end, byDuration);
  }
  return end;
}
