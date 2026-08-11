export const adminMenuGroups = [
  {
    key: 'dashboard',
    title: '首页',
    defaultPath: '/dashboard',
    match: (path) => path === '/dashboard',
    sections: [
      {
        key: 'dashboard',
        title: '首页',
        items: [{ index: '/dashboard', title: '数据看板', menuCode: 'dashboard' }],
      },
    ],
  },
  {
    key: 'basic-data',
    title: '基础数据',
    defaultPath: '/basic-data/department',
    match: (path) => path.startsWith('/basic-data/'),
    sections: [
      {
        key: 'basic-data',
        title: '基础数据',
        items: [
          { index: '/basic-data/department', title: '院系管理', menuCode: 'basic-data:department' },
          { index: '/basic-data/major', title: '专业管理', menuCode: 'basic-data:major' },
          { index: '/basic-data/class', title: '班级管理', menuCode: 'basic-data:class' },
          { index: '/basic-data/student', title: '学生管理', menuCode: 'basic-data:student' },
          { index: '/basic-data/teacher', title: '教师管理', menuCode: 'basic-data:teacher' },
        ],
      },
    ],
  },
  {
    key: 'resource',
    title: '资源中心',
    defaultPath: '/resource/manage',
    match: (path) => path.startsWith('/resource/'),
    sections: [
      {
        key: 'resource',
        title: '资源中心',
        items: [{ index: '/resource/manage', title: '资源管理', menuCode: 'resource:manage' }],
      },
    ],
  },
  {
    key: 'teaching',
    title: '教学业务',
    defaultPath: '/teaching/course',
    match: (path) => path.startsWith('/teaching/'),
    sections: [
      {
        key: 'teaching',
        title: '教学业务',
        items: [
          { index: '/teaching/course', title: '课程管理', menuCode: 'teaching:course' },
          { index: '/teaching/exercise', title: '习题管理', menuCode: 'teaching:exercise' },
          { index: '/teaching/paper', title: '试卷管理', menuCode: 'teaching:paper' },
          { index: '/teaching/exam', title: '考试管理', menuCode: 'teaching:exam' },
        ],
      },
    ],
  },
  {
    key: 'system',
    title: '系统管理',
    defaultPath: '/system/notice',
    match: (path) => path.startsWith('/system/'),
    sections: [
      {
        key: 'system',
        title: '系统管理',
        items: [
          { index: '/system/notice', title: '公告管理', menuCode: 'system:notice' },
          { index: '/system/permission', title: '权限管理', menuCode: 'system:permission' },
        ],
      },
    ],
  },
]

export const findFirstMenuPath = (groups = adminMenuGroups) => {
  for (const group of groups) {
    for (const section of group.sections || []) {
      const item = section.items?.[0]
      if (item?.index) {
        return item.index
      }
    }
  }
  return '/dashboard'
}
