import { Card } from 'antd';
import type { ReactNode } from 'react';
import styles from '@/assets/styles/statCard.module.scss';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  trend?: number;
  color?: string;
}

export default function StatCard({
  title,
  value,
  icon,
  trend,
  color = '#1677ff',
}: StatCardProps) {
  return (
    <Card>
      <div className={styles.statCard}>
        <div className={styles.statIcon} style={{ backgroundColor: `${color}1a`, color }}>
          {icon}
        </div>
        <div className={styles.statContent}>
          <div className={styles.statValue}>{value}</div>
          <div className={styles.statTitle}>{title}</div>
          {trend !== undefined && (
            <div
              className={`${styles.statTrend} ${trend >= 0 ? styles.trendUp : styles.trendDown}`}
            >
              {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}%
            </div>
          )}
        </div>
      </div>
    </Card>
  );
}
