import { useEffect } from 'react';
import { usePointStore } from '@/stores/point';
import { useAuthStore } from '@/stores/auth';
import { isStarStage } from '@/utils/point';
import styles from './PointToast.module.scss';

/** 飘字停留时长（与 CSS 动画时长一致） */
const VISIBLE_MS = 3200;

/**
 * 积分到账飘字（二期 A-6 即时反馈）。
 *
 * 小学段显示「+N 颗星」、初高段显示「+N 积分」；
 * 数据来自 PointStore 轮询到的账户增量，不由前端自行加分。
 */
export default function PointToast() {
  const gain = usePointStore((state) => state.gain);
  const clearGain = usePointStore((state) => state.clearGain);
  const stage = useAuthStore((state) => state.userInfo?.stage);
  const stars = isStarStage(stage);

  useEffect(() => {
    if (!gain) {
      return;
    }
    const timer = window.setTimeout(clearGain, VISIBLE_MS);
    return () => window.clearTimeout(timer);
  }, [gain, clearGain]);

  if (!gain) {
    return null;
  }

  return (
    <div className={styles.pointToast} role="status" aria-live="polite">
      <span className={styles.icon}>{stars ? '⭐' : '🏅'}</span>
      <span className={styles.amount}>
        {stars ? `+${gain.points} 颗星` : `+${gain.points} 积分`}
      </span>
      <span className={styles.divider} />
      <span className={styles.total}>
        {stars ? `共 ${gain.totalPoints} 颗星` : `累计 ${gain.totalPoints}`}
      </span>
    </div>
  );
}
