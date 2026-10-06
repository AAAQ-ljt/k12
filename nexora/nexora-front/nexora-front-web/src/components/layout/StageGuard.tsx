import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Empty } from 'antd';
import { MessageSquare } from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import styles from './StageGuard.module.scss';

/**
 * 学段口径常量（与 MainLayout.tsx 的 tab 配置保持一致，全局只此一份，禁止各处硬编码散落）：
 * 小学低年级：仅 AI 助教 / 绘本生成 / 课程教材 / 知识中心 / 我的
 * 小学高年级：在小学低年级基础上增加 趣味编程
 * 初中、高中：全部入口（趣味编程 + 动画讲解 + 学习路径）
 */

/** 趣味编程开放学段（小学高年级及以上） */
export const CODING_STAGES: string[] = ['PRIMARY_HIGH', 'JUNIOR', 'SENIOR'];

/** 动画讲解开放学段（初中 / 高中） */
export const ANIMATION_STAGES: string[] = ['JUNIOR', 'SENIOR'];

/** 学习路径开放学段（初中 / 高中；小学段无导航入口，待复习统一转 AI 助教对话） */
export const PATH_STAGES: string[] = ['JUNIOR', 'SENIOR'];

interface StageGuardProps {
  /** 允许访问的学段列表（用本文件导出的常量，勿硬编码） */
  allowStages: string[];
  children: ReactNode;
  /** 不满足学段时的占位说明文案 */
  description?: string;
}

/**
 * 学段守卫：学段在允许列表内渲染 children，否则渲染友好占位页。
 * 未登录或 userInfo.stage 为空时直接放行——登录引导由 ProtectedRoute 负责，避免两层守卫互相干扰。
 */
export default function StageGuard({ allowStages, children, description }: StageGuardProps) {
  const navigate = useNavigate();
  const stage = useAuthStore((state) => state.userInfo?.stage);

  if (!stage || allowStages.includes(stage)) {
    return <>{children}</>;
  }

  return (
    <div className={styles.stagePlaceholder}>
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description={description ?? '该功能面向更高学段开放，先去 AI 助教聊聊吧'}
      >
        <Button type="primary" icon={<MessageSquare size={16} />} onClick={() => navigate('/ai-tutor')}>
          返回 AI 助教
        </Button>
      </Empty>
    </div>
  );
}
