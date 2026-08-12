import { Outlet } from 'react-router-dom';
import { menus } from '@/router/menus';
import TopBar from './TopBar';
import HierarchicalMenu from './HierarchicalMenu';

export default function MainLayout() {
  return (
    <div className="main-layout">
      {/* Top Section - Only Logo + User Info */}
      <header className="layout-header">
        <TopBar />
      </header>

      {/* Bottom Section - Left Sidebar + Right Content */}
      <div className="layout-body">
        {/* Left Sidebar */}
        <aside className="layout-sidebar">
          <HierarchicalMenu menus={menus} />
        </aside>

        {/* Right Content Area */}
        <section className="layout-content">
          <Outlet />
        </section>
      </div>
    </div>
  );
}
