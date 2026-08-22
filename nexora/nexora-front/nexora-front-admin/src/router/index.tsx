import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '@/components/layout/MainLayout';
import AuthGuard from '@/components/layout/AuthGuard';
import Login from '@/views/login';
import Dashboard from '@/views/dashboard';
import UserManagement from '@/views/user/UserManagement';
import CourseManagement from '@/views/course/CourseManagement';
import QuestionManagement from '@/views/question/QuestionManagement';
import PaperManagement from '@/views/paper/PaperManagement';
import ResourceManagement from '@/views/resource/ResourceManagement';
import KnowledgeOverview from '@/views/knowledge/KnowledgeOverview';
import KnowledgeCatalog from '@/views/knowledge/KnowledgeCatalog';
import KnowledgeTest from '@/views/knowledge/KnowledgeTest';
import AIDocArrange from '@/views/knowledge/AIDocArrange';
import LearningAnalysis from '@/views/learning/LearningAnalysis';
import LearningUser from '@/views/learning/LearningUser';
import AdminAccount from '@/views/system/AdminAccount';
import ModelPrompt from '@/views/system/ModelPrompt';
import EnvConfig from '@/views/system/EnvConfig';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: (
      <AuthGuard>
        <MainLayout />
      </AuthGuard>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      {
        path: 'user',
        element: <UserManagement />,
      },
      {
        path: 'resource',
        element: <ResourceManagement />,
      },
      {
        path: 'knowledge',
        children: [
          {
            index: true,
            element: <Navigate to="/knowledge/overview" replace />,
          },
          {
            path: 'overview',
            element: <KnowledgeOverview />,
          },
          {
            path: 'catalog',
            element: <KnowledgeCatalog />,
          },
          {
            path: 'test',
            element: <KnowledgeTest />,
          },
          {
            path: 'arrange',
            element: <AIDocArrange />,
          },
        ],
      },
      {
        path: 'teaching',
        children: [
          {
            index: true,
            element: <Navigate to="/teaching/course" replace />,
          },
          {
            path: 'course',
            element: <CourseManagement />,
          },
          {
            path: 'question',
            element: <QuestionManagement />,
          },
          {
            path: 'paper',
            element: <PaperManagement />,
          },
        ],
      },
      {
        path: 'learning',
        children: [
          {
            index: true,
            element: <Navigate to="/learning/overview" replace />,
          },
          {
            path: 'overview',
            element: <LearningAnalysis />,
          },
          {
            path: 'user',
            element: <LearningUser />,
          },
        ],
      },
      {
        path: 'system',
        children: [
          {
            index: true,
            element: <Navigate to="/system/admin" replace />,
          },
          {
            path: 'admin',
            element: <AdminAccount />,
          },
          {
            path: 'model',
            element: <ModelPrompt />,
          },
          {
            path: 'config',
            element: <EnvConfig />,
          },
        ],
      },
      {
        path: '*',
        element: <Navigate to="/dashboard" replace />,
      },
    ],
  },
]);

export default router;
