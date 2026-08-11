import { Outlet, useLocation } from 'react-router-dom';
import { getMenuByPath } from '@/router/menus';
import TopBar from './TopBar';
import SideMenu from './SideMenu';
import styles from '@/assets/styles/layout.module.scss';

export default function MainLayout() {
  const location = useLocation();
  const currentMenu = getMenuByPath(location.pathname);
  const primary = currentMenu?.primary;
  const childMenus = primary?.children;
  const hasChildren = !!(childMenus && childMenus.length > 0);

  return (
    <div className={styles.layout}>
      <TopBar />
      <div className={styles.body}>
        {hasChildren && primary && childMenus ? (
          <div className={styles.sideCard}>
            <SideMenu title={primary.label} menus={childMenus} />
          </div>
        ) : null}
        <div className={styles.contentCard}>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
