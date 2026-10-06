import { createBrowserRouter, Navigate, type RouteObject } from 'react-router-dom';
import AutoLogin from '@/components/layout/AutoLogin';
import AppErrorPage from '@/components/layout/AppErrorPage';
import MainLayout from '@/components/layout/MainLayout';
import ProtectedRoute from '@/components/layout/ProtectedRoute';
import StageGuard, { ANIMATION_STAGES, CODING_STAGES, PATH_STAGES } from '@/components/layout/StageGuard';
import AiTutor from '@/views/ai-tutor';
import LearningPath from '@/views/learning-path';
import LearningPathDetailPage from '@/views/learning-path/detail';
import CourseMaterial from '@/views/course-material';
import CourseDetail from '@/views/course-material/course';
import CourseMaterialDetail from '@/views/course-material/detail';
import Coding from '@/views/coding';
import Profile from '@/views/profile';
import PictureBook from '@/views/picture-book';
import ResourceCenter from '@/views/resource-center';
import Animation from '@/views/animation';
import AnimationPlay from '@/views/animation/play';

const routes: RouteObject[] = [
  {
    path: '/login',
    element: <Navigate to="/ai-tutor" replace />,
  },
  {
    path: '/',
    element: (
      <AutoLogin>
        <MainLayout />
      </AutoLogin>
    ),
    // 路由级错误兜底：任何子路由渲染/提交异常都落到友好页，不再显示 React Router 默认开发者报错页
    errorElement: <AppErrorPage />,
    children: [
      { index: true, element: <Navigate to="/ai-tutor" replace /> },
      { path: 'ai-tutor', element: <AiTutor /> },
      {
        path: 'learning-path',
        element: (
          <StageGuard allowStages={PATH_STAGES} description="学习路径面向初中及以上，先去 AI 助教聊聊吧">
            <ProtectedRoute title="个性化学习路径" description="登录后查看你的个性化学习路径">
              <LearningPath />
            </ProtectedRoute>
          </StageGuard>
        ),
      },
      {
        path: 'learning-path/:pathId',
        element: (
          <StageGuard allowStages={PATH_STAGES} description="学习路径面向初中及以上，先去 AI 助教聊聊吧">
            <ProtectedRoute title="学习路线详情" description="登录后查看你的学习路线">
              <LearningPathDetailPage />
            </ProtectedRoute>
          </StageGuard>
        ),
      },
      {
        path: 'course-material',
        element: (
          <ProtectedRoute title="课程教材" description="登录后查看课程教材">
            <CourseMaterial />
          </ProtectedRoute>
        ),
      },
      {
        path: 'course-material/:courseId',
        element: (
          <ProtectedRoute title="课程详情" description="登录后查看课程章节与课时">
            <CourseDetail />
          </ProtectedRoute>
        ),
      },
      {
        path: 'course-material/resource/:resourceId',
        element: (
          <ProtectedRoute title="课程教材" description="登录后查看课程教材">
            <CourseMaterialDetail />
          </ProtectedRoute>
        ),
      },
      {
        path: 'coding',
        element: (
          <StageGuard allowStages={CODING_STAGES} description="趣味编程面向小学高年级及以上，先去 AI 助教聊聊吧">
            <ProtectedRoute title="趣味编程" description="登录后使用趣味编程（在线题库 + 编程比赛）">
              <Coding />
            </ProtectedRoute>
          </StageGuard>
        ),
      },
      {
        path: 'profile',
        element: (
          <ProtectedRoute title="我的" description="登录后查看个人中心">
            <Profile />
          </ProtectedRoute>
        ),
      },
      { path: 'picture-book', element: <PictureBook /> },
      {
        path: 'animation',
        element: (
          <StageGuard allowStages={ANIMATION_STAGES} description="动画讲解面向初中及以上，先去 AI 助教聊聊吧">
            <ProtectedRoute title="动画讲解" description="登录后查看动画讲解">
              <Animation />
            </ProtectedRoute>
          </StageGuard>
        ),
      },
      {
        path: 'animation/:resourceId',
        element: (
          <StageGuard allowStages={ANIMATION_STAGES} description="动画讲解面向初中及以上，先去 AI 助教聊聊吧">
            <ProtectedRoute title="动画讲解" description="登录后查看动画讲解">
              <AnimationPlay />
            </ProtectedRoute>
          </StageGuard>
        ),
      },
      {
        path: 'resource-center',
        element: (
          <ProtectedRoute title="知识中心" description="登录后管理你的个人知识库">
            <ResourceCenter />
          </ProtectedRoute>
        ),
      },
      { path: '*', element: <Navigate to="/ai-tutor" replace /> },
    ],
  },
];

export const router = createBrowserRouter(routes);

export default router;
