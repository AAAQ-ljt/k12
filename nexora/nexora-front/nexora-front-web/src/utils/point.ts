/**
 * 积分展示口径（二期 A-10 学段适配）。
 *
 * 同一套积分数据，按学段换文案：
 * - 小学段（小低 / 小高）：叫「星星」⭐，只显示自己的星星与徽章，不开放排行榜；
 * - 初高段：叫「积分」，配段位（青铜 → 王者），开放本学段排行榜。
 *
 * 服务端只回中性数值（见 PointAccountVO 注释），命名一律在前端收口，
 * 避免同一口径散落在多个页面里各写一套。
 */
import type { PointBadgeVO } from '@/api/point';

/** 用星星计数的学段（小学低年级 + 小学高年级） */
export const STAR_STAGES = ['PRIMARY_LOW', 'PRIMARY_HIGH'];

/** 是否小学段（星星口径 + 不排榜） */
export function isStarStage(stage?: string | null): boolean {
  return !!stage && STAR_STAGES.includes(stage);
}

/** 积分单位：星星 / 积分 */
export function pointUnit(stage?: string | null): string {
  return isStarStage(stage) ? '星星' : '积分';
}

/**
 * 等级称谓：
 * - 小学段：「3 颗星」
 * - 初高段：「黄金 5 段」（段位名由服务端按 LEVEL_STEP 折算下发）
 */
export function levelTitle(stage: string | null | undefined, level?: number | null, levelName?: string | null): string {
  if (!level || level <= 0) {
    return isStarStage(stage) ? '0 颗星' : '青铜 1 段';
  }
  return isStarStage(stage) ? `${level} 颗星` : `${levelName || '青铜'} ${level} 段`;
}

/** 排行榜单行称谓（榜上他人只按显示名 + 段位） */
export function rankLevelTitle(row: { stage?: string; level?: number; levelName?: string | null }, stage?: string | null): string {
  return levelTitle(row.stage || stage, row.level, row.levelName);
}

/** 积分来源文案（流水展示；未登记来源统一显示「学习奖励」，避免露出内部枚举） */
const BIZ_TYPE_LABELS: Record<string, string> = {
  SIGN_IN: '每日签到',
  STREAK: '连续学习奖励',
  LESSON_QUIZ: '课时测验',
  PATH_TEST: '学习路径小测',
  CODING_PROBLEM: '趣味编程通关',
  PICTURE_BOOK: '绘本创作',
  ANIMATION: '动画学习',
  WIKI_CONFIRM: '知识页入库',
  MASTERY: '掌握知识点',
  BADGE: '徽章奖励',
};

/** 来源文案 */
export function bizTypeLabel(bizType?: string): string {
  return BIZ_TYPE_LABELS[bizType || ''] || '学习奖励';
}

/** 成长环（等级进度）百分比：本级已得 / 本级所需 */
export function levelPercent(account: {
  level?: number | null;
  levelFloor?: number | null;
  nextLevelPoints?: number | null;
  totalPoints?: number | null;
}): number {
  const total = account.totalPoints ?? 0;
  const floor = account.levelFloor ?? 0;
  const remain = account.nextLevelPoints ?? 0;
  if (remain <= 0) {
    return 100;
  }
  const span = total - floor + remain;
  if (span <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round(((total - floor) / span) * 100)));
}

/** 今日积分上限百分比 */
export function dailyPercent(account: { todayPoints?: number | null; dailyCap?: number | null }): number {
  const cap = account.dailyCap ?? 0;
  if (cap <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round(((account.todayPoints ?? 0) / cap) * 100)));
}

/** 徽章进度文案（服务端未给时前端兜底；COMBO_MAX 规则待接入 → 「敬请期待」） */
export function badgeProgressText(badge: PointBadgeVO): string {
  if (badge.unlocked) {
    return '已解锁';
  }
  if (badge.ruleType === 'COMBO_MAX') {
    return '敬请期待';
  }
  if (badge.progressText) {
    return badge.progressText;
  }
  return `${Math.min(badge.progress ?? 0, badge.ruleValue ?? 0)}/${badge.ruleValue ?? 0}`;
}

/** 徽章进度百分比（用于进度条） */
export function badgePercent(badge: PointBadgeVO): number {
  if (badge.unlocked) {
    return 100;
  }
  if (badge.ruleType === 'COMBO_MAX' || !badge.ruleValue) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round((badge.progress / badge.ruleValue) * 100)));
}
