import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '@/components/layout/MainLayout';
import AuthGuard from '@/components/layout/AuthGuard';
import Login from '@/views/login';
import Dashboard from '@/views/dashboard';
import UserManagement from '@/views/user/UserManagement';
import CourseManagement from '@/views/course/CourseManagement';
import ResourceManagement from '@/views/course/ResourceManagement';
import KnowledgeDoc from '@/views/knowledge/KnowledgeDoc';
import KnowledgePoint from '@/views/knowledge/KnowledgePoint';
import VectorStatus from '@/views/knowledge/VectorStatus';
import QuestionList from '@/views/question/QuestionList';
import QuestionReview from '@/views/question/QuestionReview';
import TemplateLibrary from '@/views/animation/TemplateLibrary';
import GenerationRecord from '@/views/animation/GenerationRecord';
import AnimationReview from '@/views/animation/AnimationReview';
import PromptTemplate from '@/views/agent/PromptTemplate';
import ModelConfig from '@/views/agent/ModelConfig';
import IntentRouting from '@/views/agent/IntentRouting';
import LearningPath from '@/views/learning/LearningPath';
import MasteryAnalysis from '@/views/learning/MasteryAnalysis';
import SystemConfig from '@/views/system/SystemConfig';
import MenuManagement from '@/views/system/MenuManagement';
import NoticeManagement from '@/views/system/NoticeManagement';

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
        path: 'course',
        children: [
          {
            index: true,
            element: <Navigate to="/course/list" replace />,
          },
          {
            path: 'list',
            element: <CourseManagement />,
          },
          {
            path: 'resource',
            element: <ResourceManagement />,
          },
        ],
      },
      {
        path: 'knowledge',
        children: [
          {
            index: true,
            element: <Navigate to="/knowledge/doc" replace />,
          },
          {
            path: 'doc',
            element: <KnowledgeDoc />,
          },
          {
            path: 'point',
            element: <KnowledgePoint />,
          },
          {
            path: 'vector',
            element: <VectorStatus />,
          },
        ],
      },
      {
        path: 'question',
        children: [
          {
            index: true,
            element: <Navigate to="/question/list" replace />,
          },
          {
            path: 'list',
            element: <QuestionList />,
          },
          {
            path: 'review',
            element: <QuestionReview />,
          },
        ],
      },
      {
        path: 'animation',
        children: [
          {
            index: true,
            element: <Navigate to="/animation/template" replace />,
          },
          {
            path: 'template',
            element: <TemplateLibrary />,
          },
          {
            path: 'record',
            element: <GenerationRecord />,
          },
          {
            path: 'review',
            element: <AnimationReview />,
          },
        ],
      },
      {
        path: 'agent',
        children: [
          {
            index: true,
            element: <Navigate to="/agent/prompt" replace />,
          },
          {
            path: 'prompt',
            element: <PromptTemplate />,
          },
          {
            path: 'model',
            element: <ModelConfig />,
          },
          {
            path: 'intent',
            element: <IntentRouting />,
          },
        ],
      },
      {
        path: 'learning',
        children: [
          {
            index: true,
            element: <Navigate to="/learning/path" replace />,
          },
          {
            path: 'path',
            element: <LearningPath />,
          },
          {
            path: 'mastery',
            element: <MasteryAnalysis />,
          },
        ],
      },
      {
        path: 'system',
        children: [
          {
            index: true,
            element: <Navigate to="/system/config" replace />,
          },
          {
            path: 'config',
            element: <SystemConfig />,
          },
          {
            path: 'menu',
            element: <MenuManagement />,
          },
          {
            path: 'notice',
            element: <NoticeManagement />,
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
