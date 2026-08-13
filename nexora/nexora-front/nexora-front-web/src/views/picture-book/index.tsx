import { Button, Tag } from 'antd';
import { BookImage, Lock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import styles from './index.module.scss';

export default function PictureBook() {
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const openLoginModal = useUiStore((state) => state.openLoginModal);

  if (!token) {
    return (
      <div className={styles.accessBlock}>
        <div className={styles.accessIcon}>
          <Lock size={30} />
        </div>
        <h2>登录后查看绘本</h2>
        <p>绘本仅面向小学低年级学生，登录后即可进入互动绘本。</p>
        <Button type="primary" icon={<BookImage size={16} />} onClick={openLoginModal}>
          去登录
        </Button>
      </div>
    );
  }

  if (userInfo?.stage !== 'PRIMARY_LOW') {
    return (
      <div className={styles.accessBlock}>
        <div className={styles.accessIcon}>
          <BookImage size={30} />
        </div>
        <h2>绘本仅面向小学低年级</h2>
        <p>当前学段暂不支持查看绘本，请到「我的」页面切换学段。</p>
        <Button onClick={() => navigate('/learning-path')}>返回学习路径</Button>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <div className={styles.pageTitle}>互动绘本</div>
          <div className={styles.pageDesc}>图文翻页式互动绘本</div>
        </div>
        <Tag color="success">小学低年级</Tag>
      </div>
      <div className={styles.placeholder}>
        <BookImage size={40} />
        <h3>绘本创作功能开发中</h3>
        <p>后续将在这里展示图文翻页式互动绘本，可以按主题生成故事。</p>
        <span>敬请期待</span>
      </div>
    </div>
  );
}
