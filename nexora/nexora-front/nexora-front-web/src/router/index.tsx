import { createBrowserRouter, Navigate, type RouteObject } from 'react-router-dom';
import AutoLogin from '@/components/layout/AutoLogin';
import MainLayout from '@/components/layout/MainLayout';
import ProtectedRoute from '@/components/layout/ProtectedRoute';
import AiTutor from '@/views/ai-tutor';
import LearningPath from '@/views/learning-path';
import CourseMaterial from '@/views/course-material';
import Coding from '@/views/coding';
import Profile from '@/views/profile';
import PictureBook from '@/views/picture-book';

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
      { path: '*', element: <Navigate to="/ai-tutor" replace /> },
    ],
  },
];

export const router = createBrowserRouter(routes);

export default router;
