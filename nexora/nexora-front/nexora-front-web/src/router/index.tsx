import { createBrowserRouter, Navigate, type RouteObject } from 'react-router-dom';
import AutoLogin from '@/components/layout/AutoLogin';
import MainLayout from '@/components/layout/MainLayout';
import AiTutor from '@/views/ai-tutor';
import LearningPath from '@/views/learning-path';
import CourseMaterial from '@/views/course-material';
import Coding from '@/views/coding';
import Profile from '@/views/profile';

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
      { path: 'learning-path', element: <LearningPath /> },
      { path: 'course-material', element: <CourseMaterial /> },
      { path: 'coding', element: <Coding /> },
      { path: 'profile', element: <Profile /> },
      { path: '*', element: <Navigate to="/ai-tutor" replace /> },
    ],
  },
];

export const router = createBrowserRouter(routes);

export default router;
