import { useLocation, useNavigate } from 'react-router-dom';
import type { MenuConfig } from '@/router/menus';
import styles from '@/assets/styles/sidemenu.module.scss';

interface SideMenuProps {
  title: string;
  menus: MenuConfig[];
}

export default function SideMenu({ title, menus: items }: SideMenuProps) {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div className={styles.sideMenu}>
      <div className={styles.title}>{title}</div>
      <div className={styles.menuList}>
        {items.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <div
              key={item.key}
              className={
                isActive
                  ? `${styles.menuItem} ${styles.menuItemActive}`
                  : styles.menuItem
              }
              onClick={() => navigate(item.path)}
            >
              {item.label}
            </div>
          );
        })}
      </div>
    </div>
  );
}
