import { createRouter, createWebHistory } from 'vue-router'
import pinia from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { adminMenuGroups, findFirstMenuPath } from '@/router/adminMenu'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/',
    name: 'layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '看板',
          breadcrumb: ['看板'],
          menuCode: 'dashboard',
        },
      },
      {
        path: 'basic-data/department',
        name: 'basicDataDepartment',
        component: () => import('@/views/basic-data/DepartmentManagement.vue'),
        meta: {
          title: '院系管理',
          breadcrumb: ['基础数据', '院系管理'],
          menuCode: 'basic-data:department',
        },
      },
      {
        path: 'basic-data/major',
        name: 'basicDataMajor',
        component: () => import('@/views/basic-data/MajorManagement.vue'),
        meta: {
          title: '专业管理',
          breadcrumb: ['基础数据', '专业管理'],
          menuCode: 'basic-data:major',
        },
      },
      {
        path: 'basic-data/class',
        name: 'basicDataClass',
        component: () => import('@/views/basic-data/ClassManagement.vue'),
        meta: {
          title: '班级管理',
          breadcrumb: ['基础数据', '班级管理'],
          menuCode: 'basic-data:class',
        },
      },
      {
        path: 'basic-data/student',
        name: 'basicDataStudent',
        component: () => import('@/views/basic-data/StudentManagement.vue'),
        meta: {
          title: '学生管理',
          breadcrumb: ['基础数据', '学生管理'],
          menuCode: 'basic-data:student',
        },
      },
      {
        path: 'basic-data/teacher',
        name: 'basicDataTeacher',
        component: () => import('@/views/basic-data/TeacherManagement.vue'),
        meta: {
          title: '教师管理',
          breadcrumb: ['基础数据', '教师管理'],
          menuCode: 'basic-data:teacher',
        },
      },
      {
        path: 'resource/manage',
        name: 'resourceManage',
        component: () => import('@/views/resource/ResourceManagement.vue'),
        meta: {
          title: '资源管理',
          breadcrumb: ['资源管理'],
          menuCode: 'resource:manage',
        },
      },
      {
        path: 'teaching/course',
        name: 'teachingCourse',
        component: () => import('@/views/teaching/CourseManagement.vue'),
        meta: {
          title: '课程管理',
          breadcrumb: ['教学业务', '课程管理'],
          menuCode: 'teaching:course',
        },
      },
      {
        path: 'teaching/course/chapter',
        name: 'teachingCourseChapter',
        component: () => import('@/views/teaching/CourseChapterManagement.vue'),
        meta: {
          title: '章节管理',
          breadcrumb: ['教学业务', '课程管理', '章节管理'],
          menuCode: 'teaching:course',
        },
      },
      {
        path: 'teaching/course/homework',
        name: 'teachingCourseHomework',
        component: () => import('@/views/teaching/components/CourseHomeworkManagement.vue'),
        meta: {
          title: '学生作业',
          breadcrumb: ['教学业务', '课程管理', '学生作业'],
          menuCode: 'teaching:course',
        },
      },
      {
        path: 'teaching/exercise',
        name: 'teachingExercise',
        component: () => import('@/views/teaching/ExerciseManagement.vue'),
        meta: {
          title: '习题管理',
          breadcrumb: ['教学业务', '习题管理'],
          menuCode: 'teaching:exercise',
        },
      },
      {
        path: 'teaching/paper',
        name: 'teachingPaper',
        component: () => import('@/views/teaching/PaperManagement.vue'),
        meta: {
          title: '试卷管理',
          breadcrumb: ['教学业务', '试卷管理'],
          menuCode: 'teaching:paper',
        },
      },
      {
        path: 'teaching/exam',
        name: 'teachingExam',
        component: () => import('@/views/teaching/ExamManagement.vue'),
        meta: {
          title: '考试管理',
          breadcrumb: ['教学业务', '考试管理'],
          menuCode: 'teaching:exam',
        },
      },
      {
        path: 'system/notice',
        name: 'systemNotice',
        component: () => import('@/views/system/SystemNoticeManagement.vue'),
        meta: {
          title: '公告管理',
          breadcrumb: ['系统管理', '公告管理'],
          menuCode: 'system:notice',
        },
      },
      {
        path: 'system/permission',
        name: 'systemPermission',
        component: () => import('@/views/system/SystemPermissionManagement.vue'),
        meta: {
          title: '权限管理',
          breadcrumb: ['系统管理', '权限管理'],
          menuCode: 'system:permission',
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

const filterMenuGroupsByAuth = (authStore) => adminMenuGroups.map((group) => {
  const sections = (group.sections || []).map((section) => ({
    ...section,
    items: (section.items || []).filter((item) => authStore.hasMenu(item.menuCode)),
  })).filter((section) => section.items.length)
  const firstPath = sections[0]?.items?.[0]?.index
  return firstPath ? { ...group, defaultPath: firstPath, sections } : null
}).filter(Boolean)

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)
  authStore.restoreToken()

  if (to.meta.requiresAuth === false) {
    if (to.path === '/login' && authStore.token) {
      const userInfo = authStore.userInfo || await authStore.fetchLoginInfo()
      if (userInfo) {
        return String(to.query.redirect || findFirstMenuPath(filterMenuGroupsByAuth(authStore)))
      }
    }
    return true
  }

  if (!authStore.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  const userInfo = authStore.userInfo || await authStore.fetchLoginInfo()
  if (!userInfo) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.path === '/') {
    return findFirstMenuPath(filterMenuGroupsByAuth(authStore))
  }

  if (!to.meta.menuCode || authStore.hasMenu(to.meta.menuCode)) {
    return true
  }

  const firstMenuPath = findFirstMenuPath(filterMenuGroupsByAuth(authStore))
  return firstMenuPath === to.path ? true : firstMenuPath
})

export default router
