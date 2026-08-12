import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import type { MenuConfig } from '@/router/menus';
import { getIcon } from './iconMap';
import { ChevronDown } from 'lucide-react';

interface MenuItemProps {
  menu: MenuConfig;
  depth?: number;
}

function MenuItem({ menu, depth = 0 }: MenuItemProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const [expanded, setExpanded] = useState(false);
  
  const isActive = location.pathname === menu.path;
  const hasChildren = menu.children && menu.children.length > 0;
  
  // Auto-expand if current page is under this menu
  useEffect(() => {
    if (isActive) {
      setExpanded(true);
    }
  }, [location.pathname, isActive]);

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    if (hasChildren) {
      setExpanded(!expanded);
    } else {
      navigate(menu.path);
    }
  };

  return (
    <div className={`menu-item ${depth === 0 ? 'level-1' : 'level-2'}`} style={{ paddingLeft: `${depth * 16 + 8}px` }}>
      <div className="menu-item-content" onClick={handleClick}>
        {menu.icon && (() => {
          const IconComponent = getIcon(menu.icon);
          return IconComponent ? (
            <span className="menu-icon">{React.createElement(IconComponent)}</span>
          ) : null;
        })()}
        <span className="menu-label">{menu.label}</span>
        {hasChildren && (
          <ChevronDown 
            size={14} 
            className={`menu-expander ${expanded ? 'expanded' : ''}`}
          />
        )}
      </div>
      
      {/* Nested Children */}
      {hasChildren && expanded && (
        <div className="menu-children">
          {menu.children!.map((child) => (
            <MenuItem key={child.key} menu={child} depth={depth + 1} />
          ))}
        </div>
      )}
    </div>
  );
}

interface HierarchicalMenuProps {
  menus: MenuConfig[];
}

export default function HierarchicalMenu({ menus }: HierarchicalMenuProps) {
  return (
    <div className="hierarchical-menu">
      {menus.map((menu) => (
        <MenuItem key={menu.key} menu={menu} depth={0} />
      ))}
    </div>
  );
}
