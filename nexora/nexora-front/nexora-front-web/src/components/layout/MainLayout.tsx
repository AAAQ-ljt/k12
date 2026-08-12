import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { App, Avatar, Button, Dropdown } from 'antd';
import { Sparkles, MessageSquare, Route, BookOpen, Code, User, LogOut } from 'lucide-react';
import type { ComponentType } from 'react';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import { studentLogout } from '@/api/auth';
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
  const navigate = useNavigate();
  const { message } = App.useApp();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const clear = useAuthStore((state) => state.clear);
  const openLoginModal = useUiStore((state) => state.openLoginModal);
  const stageOption = userInfo ? getStageOption(userInfo.stage) : undefined;

  /** 退出登录 */
  const handleLogout = async () => {
    try {
      await studentLogout();
    } finally {
      clear();
      message.success('已退出登录');
      navigate('/ai-tutor');
    }
  };

  /** 用户下拉菜单 */
  const userMenuItems = [
    { key: 'profile', icon: <User size={14} />, label: '个人中心' },
    { type: 'divider' as const },
    { key: 'logout', icon: <LogOut size={14} />, label: '退出登录' },
  ];

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

        {/* C. Bottom User Area (底部用户区，Codex 模式登录入口) */}
        <div className="user-section">
          {token ? (
            <Dropdown
              menu={{
                items: userMenuItems,
                onClick: ({ key }) => {
                  if (key === 'profile') {
                    navigate('/profile');
                  } else if (key === 'logout') {
                    void handleLogout();
                  }
                },
              }}
              placement="topLeft"
            >
              <div className="user-card">
                <Avatar src={userInfo?.avatar} size={36} className="user-avatar">
                  {userInfo?.username?.[0]?.toUpperCase()}
                </Avatar>
                <div className="user-info">
                  <div className="user-name">{userInfo?.username ?? '同学'}</div>
                  <div className="user-stage">{stageOption?.label || ''}</div>
                </div>
              </div>
            </Dropdown>
          ) : (
            <Button type="primary" block onClick={openLoginModal} className="user-login-btn">
              登录
            </Button>
          )}
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
