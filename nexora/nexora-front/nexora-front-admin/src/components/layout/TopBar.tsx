import { Button, Dropdown, App, Tooltip } from 'antd';
import { ChevronDown, Sparkles, Sun } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { adminLogout } from '@/api/auth';

export default function TopBar() {
  const navigate = useNavigate();
  const userInfo = useAuthStore((s) => s.userInfo);
  const clear = useAuthStore((s) => s.clear);
  const { message } = App.useApp();

  const handleLogout = async () => {
    try {
      await adminLogout();
    } finally {
      clear();
      navigate('/login');
    }
  };

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      void handleLogout();
    } else if (key === 'password') {
      message.info('修改密码功能开发中');
    }
  };

  const userMenuItems = [
    { key: 'password', label: '修改密码' },
    { key: 'logout', label: '退出登录' },
  ];

  const username = userInfo?.username || '管理员';

  return (
    <div className="top-bar-wrapper">
      <div className="top-bar-left">
        <div className="top-bar-logo">
          <span className="top-bar-logo-icon">
            <Sparkles size={18} />
          </span>
          <span className="top-bar-logo-text">Nexora AI 教学助手后台</span>
        </div>
      </div>
      <div className="top-bar-right-area" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <Tooltip title="切换主题">
          <Button
            type="text"
            aria-label="切换主题"
            icon={<Sun size={16} />}
            onClick={() => message.info('主题切换功能开发中')}
          />
        </Tooltip>
        <Dropdown
          menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
          placement="bottomRight"
        >
          <div className="top-bar-user-info">
            <span className="top-bar-username">{username}</span>
            <ChevronDown size={14} className="top-bar-user-info-icon" />
          </div>
        </Dropdown>
      </div>
    </div>
  );
}
