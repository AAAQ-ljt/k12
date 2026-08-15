import { Empty } from 'antd';
import { PlaySquare } from 'lucide-react';
import styles from './index.module.scss';

export default function Animation() {
  return (
    <div className={styles.animationPage}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          <PlaySquare size={22} />
          <span>动画讲解</span>
        </div>
        <div className={styles.pageDesc}>AI 生成的 SVG 动画会保存在你的资源中心，在这里统一观看。</div>
      </div>
      <div className={styles.animationBody}>
        <Empty description="暂无动画讲解" />
      </div>
    </div>
  );
}
