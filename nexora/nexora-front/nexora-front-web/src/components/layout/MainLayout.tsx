import { NavLink, Outlet } from 'react-router-dom';
import { Avatar } from 'antd';
import { Sparkles, MessageSquare, Route, BookOpen, Code, User } from 'lucide-react';
import type { ComponentType } from 'react';
import { useAuthStore } from '@/stores/auth';
import { getStageOption } from '@/types/common';
import styles from '@/assets/styles/layout.module.scss';

interface TabItem {
  path: string;
  label: string;
  icon: ComponentType<{ size?: number; strokeWidth?: number }>;
}

const TABS: TabItem[] = [
  { path: '/ai-tutor', label: 'AI 助教', icon: MessageSquare },
  { path: '/learning-path', label: '学习路径', icon: Route },
  { path: '/course-material', label: '课程教材', icon: BookOpen },
  { path: '/coding', label: '编程环境', icon: Code },
  { path: '/profile', label: '我的', icon: User },
];

export default function MainLayout() {
  const userInfo = useAuthStore((state) => state.userInfo);

  const stageOption = userInfo ? getStageOption(userInfo.stage) : undefined;

  return (
    <div className={styles.layout}>
      {/* 顶部导航 */}
      <header className={styles.header}>
        <div className={styles.brand}>
          <span className={styles.logoIcon}>
            <Sparkles size={24} strokeWidth={2.2} />
          </span>
          <span className={styles.brandName}>Nexora AI 教学助手</span>
        </div>
        <div className={styles.userArea}>
          {stageOption && (
            <span className={styles.stageTag} style={{ color: stageOption.color, borderColor: stageOption.color }}>
              {stageOption.label}
            </span>
          )}
          <Avatar src={userInfo?.avatar} size={32} className={styles.avatar}>
            {userInfo?.username?.[0]?.toUpperCase()}
          </Avatar>
          <span className={styles.username}>{userInfo?.username ?? '同学'}</span>
        </div>
      </header>

      {/* 内容区 */}
      <main className={styles.content}>
        <Outlet />
      </main>

      {/* 底部 Tab 栏 */}
      <nav className={styles.tabBar}>
        {TABS.map((tab) => {
          const Icon = tab.icon;
          return (
            <NavLink
              key={tab.path}
              to={tab.path}
              className={({ isActive }) =>
                isActive ? `${styles.tabItem} ${styles.tabItemActive}` : styles.tabItem
              }
            >
              <Icon size={22} strokeWidth={2} />
              <span className={styles.tabLabel}>{tab.label}</span>
            </NavLink>
          );
        })}
      </nav>
    </div>
  );
}
