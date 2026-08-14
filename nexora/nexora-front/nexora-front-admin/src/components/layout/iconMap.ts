import type { ElementType } from 'react';
import {
  LayoutDashboard,
  Users,
  BookOpen,
  Database,
  FileQuestion,
  Film,
  FolderOpen,
  Bot,
  BarChart3,
  Settings,
  Sparkles,
  GraduationCap,
  ClipboardList,
  CalendarCheck,
} from 'lucide-react';

export const iconMap: Record<string, ElementType> = {
  LayoutDashboard,
  Users,
  BookOpen,
  Database,
  FileQuestion,
  Film,
  FolderOpen,
  Bot,
  BarChart3,
  Settings,
  Sparkles,
  GraduationCap,
  ClipboardList,
  CalendarCheck,
};

/** 根据图标名称获取 lucide-react 图标组件 */
export function getIcon(name?: string): ElementType | null {
  if (!name) return null;
  return iconMap[name] ?? null;
}
