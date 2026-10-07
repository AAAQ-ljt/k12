import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { App, Avatar, Button, Dropdown } from 'antd';
import { Sparkles, MessageSquare, Route, BookOpen, BookImage, Code, User, LogOut, PlaySquare, FolderOpen } from 'lucide-react';
import { useEffect, type ComponentType } from 'react';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import { usePointStore } from '@/stores/point';
import { studentLogout } from '@/api/auth';
import PointToast from '@/components/point/PointToast';
import OnboardingGuide from '@/components/onboarding/OnboardingGuide';
import { getGradeText, getStageOption } from '@/types/common';

/** 积分到账轮询间隔：积分只由服务端学习事件结算，前端定时感知「变多了」即飘字 */
const POINT_POLL_MS = 30_000;

interface TabItem {
  path: string;
  label: string;
  icon: ComponentType<{ size?: number; strokeWidth?: number }>;
}

const TABS: TabItem[] = [
  { path: '/ai-tutor', label: 'AI 助教', icon: MessageSquare },
  { path: '/learning-path', label: '学习路径', icon: Route },
  { path: '/course-material', label: '课程教材', icon: BookOpen },
  { path: '/coding', label: '趣味编程', icon: Code },
  { path: '/resource-center', label: '知识中心', icon: FolderOpen },
  { path: '/profile', label: '我的', icon: User },
];

const ANIMATION_TAB: TabItem = { path: '/animation', label: '动画讲解', icon: PlaySquare };
const PICTURE_BOOK_TAB: TabItem = { path: '/picture-book', label: '绘本生成', icon: BookImage };

// 小学低年级：无动画讲解、无趣味编程（见 AGENTS「小学低年级不展示趣味编程入口」）
const PRIMARY_LOW_TABS: TabItem[] = [
  TABS[0],
  PICTURE_BOOK_TAB,
  TABS[2],
  TABS[4],
  TABS[5],
];

// 小学高年级：含趣味编程（面向小高及以上），绘本仍可见
const PRIMARY_HIGH_TABS: TabItem[] = [
  TABS[0],
  PICTURE_BOOK_TAB,
  TABS[2],
  TABS[3],
  TABS[4],
  TABS[5],
];

const JUNIOR_SENIOR_TABS: TabItem[] = [
  TABS[0],
  ANIMATION_TAB,
  TABS[2],
  TABS[3],
  TABS[4],
  TABS[1],
  TABS[5],
];

const GUEST_TABS: TabItem[] = [TABS[0], TABS[2], TABS[5]];

export default function MainLayout() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const token = useAuthStore((state) => state.token);
  const userInfo = useAuthStore((state) => state.userInfo);
  const clear = useAuthStore((state) => state.clear);
  const openLoginModal = useUiStore((state) => state.openLoginModal);
  const stageOption = userInfo ? getStageOption(userInfo.stage) : undefined;
  const stage = userInfo?.stage;
  const visibleTabs = !stage
    ? GUEST_TABS
    : stage === 'PRIMARY_LOW'
      ? PRIMARY_LOW_TABS
      : stage === 'PRIMARY_HIGH'
        ? PRIMARY_HIGH_TABS
        : JUNIOR_SENIOR_TABS;

  /** 退出登录 */
  const handleLogout = async () => {
    try {
      await studentLogout();
    } finally {
      clear();
      usePointStore.getState().reset();
      message.success('已退出登录');
      navigate('/ai-tutor');
    }
  };

  // 积分到账感知：登录后立即拉一次（首次只记基线不飘字），之后定时 + 回到前台时刷新
  const refreshPoints = usePointStore((state) => state.refresh);
  useEffect(() => {
    if (!token) {
      return;
    }
    void refreshPoints();
    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') {
        void refreshPoints();
      }
    }, POINT_POLL_MS);
    const onVisible = () => {
      if (document.visibilityState === 'visible') {
        void refreshPoints();
      }
    };
    document.addEventListener('visibilitychange', onVisible);
    return () => {
      window.clearInterval(timer);
      document.removeEventListener('visibilitychange', onVisible);
    };
  }, [token, refreshPoints]);

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
            {visibleTabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <NavLink
                  key={tab.path}
                  to={tab.path}
                  data-tour={`nav-${tab.path.replace('/', '')}`}
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
            {/* 新手指引（计划 D1）：常驻顶栏最右侧，首次自动弹欢迎卡，忘记操作随时重看 */}
            <OnboardingGuide />
            {(userInfo?.grade || stageOption) && (
              <span className="grade-badge" style={{ '--stage-color': stageOption?.color } as React.CSSProperties}>
                {getGradeText(userInfo)}
              </span>
            )}
          </div>
        </header>

        {/* Main Content Area (核心功能区) */}
        <main className="main-content">
          <Outlet />
        </main>
      </div>

      {/* 积分到账飘字（全局，任何页面学习得分都能看到） */}
      <PointToast />
    </div>
  );
}
