import { useLocation, useNavigate } from 'react-router-dom';
import type { MenuConfig } from '@/router/menus';
import { getIcon } from './iconMap';

interface SideMenuProps {
  title: string;
  menus: MenuConfig[];
}

export default function SideMenu({ title, menus: items }: SideMenuProps) {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div className="side-menu">
      <div className="side-menu-title">{title}</div>
      <div className="menu-list">
        {items.map((item) => {
          const isActive = location.pathname === item.path;
          const IconComponent = getIcon(item.icon);
          return (
            <div
              key={item.key}
              className={
                isActive
                  ? 'menu-item menu-item-active'
                  : 'menu-item'
              }
              onClick={() => navigate(item.path)}
            >
              {IconComponent && <IconComponent size={18} className="menu-icon" />}
              <span className="menu-label">{item.label}</span>
              {isActive && <span className="menu-indicator" />}
            </div>
          );
        })}
      </div>
    </div>
  );
}
