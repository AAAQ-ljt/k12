import { NavLink, Outlet } from 'react-router-dom';
import { Avatar } from 'antd';
import { Sparkles, MessageSquare, Route, BookOpen, Code, User } from 'lucide-react';
import type { ComponentType } from 'react';
import { useAuthStore } from '@/stores/auth';
import { getStageOption } from '@/types/common';

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
    <div className="main-layout">
      {/* Left Sidebar */}
      <aside className="sidebar-wrapper">
        {/* A. Top Brand Area (顶部品牌区) */}
        <div className="brand-section">
          <div className="brand-logo">
            <span className="logo-icon">
              <Sparkles size={20} strokeWidth={2.2} />
            </span>
            <span className="brand-name">K12 AI 通识课</span>
          </div>
        </div>

        {/* B. Middle Navigation Area (中部导航区) */}
        <nav className="nav-section">
          <div className="nav-menu-list">
            {TABS.map((tab) => {
              const Icon = tab.icon;
              return (
                <NavLink
                  key={tab.path}
                  to={tab.path}
                  className={({ isActive }) =>
                    isActive ? `nav-item active` : 'nav-item'
                  }
                >
                  <span className="nav-icon">
                    <Icon size={20} strokeWidth={2} />
                  </span>
                  <span className="nav-label">{tab.label}</span>
                </NavLink>
              );
            })}
          </div>
        </nav>

        {/* C. Bottom User Area (底部用户区) */}
        <div className="user-section">
          <div className="user-card">
            <Avatar src={userInfo?.avatar} size={36} className="user-avatar">
              {userInfo?.username?.[0]?.toUpperCase()}
            </Avatar>
            <div className="user-info">
              <div className="user-name">{userInfo?.username ?? '同学'}</div>
              <div className="user-name">{stageOption?.label || ''}</div>
            </div>
          </div>
        </div>
      </aside>

      {/* Right Content Section */}
      <div className="content-wrapper">
        {/* Header Bar (顶部通栏) */}
        <header className="header-bar">
          <div className="breadcrumb-area">
            K12 AI 通识课教学平台
          </div>
          <div className="header-actions">
            {stageOption && (
              <span className="stage-badge" style={{ '--stage-color': stageOption.color } as React.CSSProperties}>
                {stageOption.label}
              </span>
            )}
          </div>
        </header>

        {/* Main Content Area (核心功能区) */}
        <main className="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
