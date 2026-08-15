import { createBrowserRouter, Navigate, type RouteObject } from 'react-router-dom';
import AutoLogin from '@/components/layout/AutoLogin';
import MainLayout from '@/components/layout/MainLayout';
import ProtectedRoute from '@/components/layout/ProtectedRoute';
import AiTutor from '@/views/ai-tutor';
import LearningPath from '@/views/learning-path';
import CourseMaterial from '@/views/course-material';
import CourseDetail from '@/views/course-material/course';
import CourseMaterialDetail from '@/views/course-material/detail';
import Coding from '@/views/coding';
import Profile from '@/views/profile';
import PictureBook from '@/views/picture-book';
import ResourceCenter from '@/views/resource-center';
import Animation from '@/views/animation';

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
    children: [
      { index: true, element: <Navigate to="/ai-tutor" replace /> },
      { path: 'ai-tutor', element: <AiTutor /> },
      {
        path: 'learning-path',
        element: (
          <ProtectedRoute title="个性化学习路径" description="登录后查看你的个性化学习路径">
            <LearningPath />
          </ProtectedRoute>
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
          <ProtectedRoute title="编程环境" description="登录后使用在线编程环境">
            <Coding />
          </ProtectedRoute>
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
          <ProtectedRoute title="动画讲解" description="登录后查看动画讲解">
            <Animation />
          </ProtectedRoute>
        ),
      },
      {
        path: 'resource-center',
        element: (
          <ProtectedRoute title="资源中心" description="登录后管理你的个人知识库">
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
