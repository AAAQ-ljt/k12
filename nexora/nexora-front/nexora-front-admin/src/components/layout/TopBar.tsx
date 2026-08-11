import { Avatar, Dropdown, Menu, App } from 'antd';
import { ChevronDown, Sparkles } from 'lucide-react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { adminLogout } from '@/api/auth';
import { menus, getMenuByPath } from '@/router/menus';
import { getIcon } from './iconMap';
import styles from '@/assets/styles/topbar.module.scss';

export default function TopBar() {
  const navigate = useNavigate();
  const location = useLocation();
  const userInfo = useAuthStore((s) => s.userInfo);
  const clear = useAuthStore((s) => s.clear);
  const { message } = App.useApp();

  const currentMenu = getMenuByPath(location.pathname);
  const selectedKey = currentMenu?.primary.key;

  const handleMenuClick = ({ key }: { key: string }) => {
    const menu = menus.find((m) => m.key === key);
    if (!menu) return;
    if (menu.children && menu.children.length > 0) {
      navigate(menu.children[0].path);
    } else {
      navigate(menu.path);
    }
  };

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

  const menuItems = menus.map((menu) => {
    const Icon = getIcon(menu.icon);
    return {
      key: menu.key,
      label: (
        <span className={styles.menuLabel}>
          {Icon && <Icon size={16} />}
          <span>{menu.label}</span>
        </span>
      ),
    };
  });

  const userMenuItems = [
    { key: 'password', label: '修改密码' },
    { key: 'logout', label: '退出登录' },
  ];

  const username = userInfo?.username || '管理员';

  return (
    <div className={styles.topBar}>
      <div className={styles.logo}>
        <span className={styles.logoIcon}>
          <Sparkles size={18} />
        </span>
        <span className={styles.logoText}>Nexora AI 教学助手后台</span>
      </div>
      <Menu
        className={styles.menu}
        mode="horizontal"
        selectedKeys={selectedKey ? [selectedKey] : []}
        items={menuItems}
        onClick={handleMenuClick}
      />
      <div className={styles.rightArea}>
        <Dropdown
          menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
          placement="bottomRight"
        >
          <div className={styles.userInfo}>
            <Avatar size={28} style={{ backgroundColor: '#1677ff', flexShrink: 0 }}>
              {username.charAt(0).toUpperCase()}
            </Avatar>
            <span className={styles.username}>{username}</span>
            <ChevronDown size={14} color="#999" />
          </div>
        </Dropdown>
      </div>
    </div>
  );
}
