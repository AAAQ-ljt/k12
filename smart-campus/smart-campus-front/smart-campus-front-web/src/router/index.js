import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/Home.vue'),
        },
        {
          path: 'courses',
          name: 'my-course',
          component: () => import('@/views/course/MyCourse.vue'),
        },
        {
          path: 'courses/:courseId/study',
          name: 'course-study',
          component: () => import('@/views/course/CourseStudy.vue'),
        },
        {
          path: 'courses/:courseId/homework/:lessonId',
          name: 'course-homework',
          component: () => import('@/views/course/CourseHomework.vue'),
        },
        {
          path: 'exams',
          name: 'exam-list',
          component: () => import('@/views/exam/ExamList.vue'),
        },
        {
          path: 'exams/:examId',
          name: 'course-exam',
          component: () => import('@/views/exam/CourseExam.vue'),
        },
        {
          path: 'plans',
          name: 'study-plan',
          component: () => import('@/views/plan/StudyPlan.vue'),
        },
        {
          path: 'analysis',
          name: 'learning-analysis',
          component: () => import('@/views/analysis/LearningAnalysis.vue'),
        },
        {
          path: 'messages',
          name: 'message-center',
          component: () => import('@/views/MessageCenter.vue'),
        },
        {
          path: 'profile',
          name: 'profile-center',
          component: () => import('@/views/profile/ProfileCenter.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta?.public) {
    if (to.path === '/login' && authStore.token) {
      const userInfo = authStore.userInfo || await authStore.fetchLoginInfo()
      if (userInfo) {
        return { path: '/' }
      }
    }
    return true
  }

  if (!authStore.token) {
    return {
      path: '/login',
      query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : undefined,
    }
  }

  const userInfo = authStore.userInfo || await authStore.fetchLoginInfo()
  if (!userInfo) {
    authStore.clearAuth()
    return {
      path: '/login',
      query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : undefined,
    }
  }

  return true
})

export default router
