<template>
  <div class="student-layout">
    <aside class="student-layout__sidebar">
      <div class="student-layout__brand">
        <div class="student-layout__brand-mark">
          <span />
        </div>
        <div>
          <strong>智慧校园</strong>
        </div>
      </div>

      <nav class="student-layout__nav">
        <button v-for="item in navItems" :key="item.key" type="button" class="student-layout__nav-item"
          :class="{ 'is-active': item.key === activeNavKey }" @click="handleNavClick(item)">
          <span class="student-layout__nav-icon">
            <i class="iconfont" :class="item.iconClass" />
          </span>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="student-layout__promo">
        <h3>让学习成为一种习惯</h3>
        <p>每天进步一点点，未来可期</p>
        <div class="student-layout__promo-illustration">
          <img :src="tipsImage" alt="学习提示">
        </div>
      </div>
    </aside>

    <div class="student-layout__main">
      <header class="student-layout__header">
        <div class="student-layout__header-actions">
          <button type="button" class="student-layout__icon-button" @click="router.push('/messages')">
            <span v-if="unreadCount > 0" class="student-layout__notice-badge">{{ unreadCount }}</span>
            <i class="iconfont icon-menu-message" />
          </button>
          <el-dropdown trigger="click" @command="handleProfileCommand">
            <button type="button" class="student-layout__profile">
              <div class="student-layout__avatar">
                <img v-if="avatarUrl" :src="avatarUrl" alt="头像">
                <span v-else>{{ avatarText }}</span>
              </div>
              <span>{{ authStore.displayName }}</span>
              <i class="iconfont icon-arrow-down" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="student-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Message from '@/utils/Message'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { buildResourceFileUrl } from '@/utils/resource'
import tipsImage from '@/assets/tips.png'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const messageStore = useMessageStore()
const unreadCount = computed(() => messageStore.unreadCount)

const navItems = [
  { key: 'home', label: '首页', iconClass: 'icon-menu-home', path: '/' },
  {
    key: 'course',
    label: '我的课程',
    iconClass: 'icon-courses',
    path: '/courses',
  },
  {
    key: 'exam',
    label: '在线考试',
    iconClass: 'icon-menu-exam',
    path: '/exams',
  },
  {
    key: 'plan',
    label: '学习计划',
    iconClass: 'icon-menu-plan',
    path: '/plans',
  },
  {
    key: 'analysis',
    label: '学习分析',
    iconClass: 'icon-analysis',
    path: '/analysis',
  },
  {
    key: 'message',
    label: '消息中心',
    iconClass: 'icon-menu-message',
    path: '/messages',
  },
  {
    key: 'profile',
    label: '个人中心',
    iconClass: 'icon-menu-user',
    path: '/profile',
  },
]

const activeNavKey = computed(() => {
  if (route.name === 'home') {
    return 'home'
  }
  if (route.name === 'my-course') {
    return 'course'
  }
  if (route.name === 'course-study') {
    return 'course'
  }
  if (route.name === 'course-homework') {
    return 'course'
  }
  if (route.name === 'exam-list' || route.name === 'course-exam') {
    return 'exam'
  }
  if (route.name === 'study-plan') {
    return 'plan'
  }
  if (route.name === 'learning-analysis') {
    return 'analysis'
  }
  if (route.name === 'message-center') {
    return 'message'
  }
  if (route.name === 'profile-center') {
    return 'profile'
  }
  return ''
})

const avatarText = computed(() => {
  const name = authStore.displayName || '同学'
  return name.slice(0, 1)
})

const avatarUrl = computed(() =>
  buildResourceFileUrl(authStore.userInfo?.avatar)
)

const handleComingSoon = (title) => {
  Message.warning(`${title} 功能正在接入中`)
}

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path)
    return
  }
  handleComingSoon(item.label)
}

const handleProfileCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    await handleLogout()
  }
}

const handleLogout = async () => {
  await authStore.logout()
  Message.success('已退出登录')
  router.replace('/login')
}

onMounted(() => {
  messageStore.fetchUnreadCount()
})
</script>

<style>
.student-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  height: 100vh;
  min-height: 100vh;
  background: linear-gradient(180deg, #f4f8ff 0%, #eef5ff 100%);
  overflow: hidden;
}

.student-layout__sidebar {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 16px 10px 18px;
  border-right: 1px solid rgba(221, 231, 247, 0.88);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 10px 0 30px rgba(69, 98, 151, 0.05);
  overflow: auto;
}

.student-layout__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 8px 18px;
}

.student-layout__brand strong {
  color: #142f57;
  font-size: 16px;
  font-weight: 800;
}

.student-layout__brand-mark {
  position: relative;
  width: 38px;
  height: 38px;
  border-radius: 6px;
  background: linear-gradient(135deg, #55a1ff 0%, #2f6bf2 100%);
  box-shadow: 0 12px 22px rgba(47, 107, 242, 0.2);
}

.student-layout__brand-mark::before,
.student-layout__brand-mark::after,
.student-layout__brand-mark span {
  position: absolute;
  content: '';
}

.student-layout__brand-mark::before {
  left: 8px;
  right: 8px;
  top: 10px;
  height: 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.95);
  clip-path: polygon(0 38%, 50% 0, 100% 38%, 100% 100%, 0 100%);
}

.student-layout__brand-mark::after {
  left: 14px;
  right: 14px;
  top: 14px;
  height: 3px;
  border-radius: 6px;
  background: #2d79ff;
}

.student-layout__brand-mark span {
  left: 16px;
  bottom: 8px;
  width: 6px;
  height: 6px;
  border-radius: 6px;
  background: #fff;
}

.student-layout__nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.student-layout__nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 44px;
  padding: 0 14px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #4b628a;
  cursor: pointer;
  font-size: 15px;
  text-align: left;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.student-layout__nav-item:hover {
  background: rgba(52, 116, 255, 0.08);
  color: #255ee9;
}

.student-layout__nav-item.is-active {
  background: linear-gradient(135deg, #317dff 0%, #2569f3 100%);
  color: #fff;
  box-shadow: 0 16px 24px rgba(49, 125, 255, 0.24);
}

.student-layout__nav-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
}

.student-layout__nav-icon .iconfont {
  font-size: 18px;
}

.student-layout__promo {
  margin-top: auto;
  padding: 18px 16px 16px;
  border-radius: 6px;
  background: linear-gradient(180deg, #f1f6ff 0%, #e7f0ff 100%);
}

.student-layout__promo h3 {
  margin: 0 0 8px;
  color: #173458;
  font-size: 15px;
}

.student-layout__promo p {
  margin: 0;
  color: #6e809b;
  font-size: 12px;
  line-height: 1.7;
}

.student-layout__promo-illustration {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px auto 0px auto;
}

.student-layout__promo-illustration img {
  display: block;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.student-layout__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.student-layout__header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  height: 74px;
  padding: 14px 28px 12px 26px;
  border-bottom: 1px solid rgba(221, 231, 247, 0.88);
  background: rgba(255, 255, 255, 0.84);
}

.student-layout__header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.student-layout__icon-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 6px;
  background: #fff;
  color: #274064;
  cursor: pointer;
  font-size: 14px;
  box-shadow: 0 10px 20px rgba(69, 98, 151, 0.08);
}

.student-layout__icon-button .iconfont {
  font-size: 18px;
}

.student-layout__notice-badge {
  position: absolute;
  top: -4px;
  right: -1px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 6px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
}

.student-layout__profile {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0 4px 6px;
  border: 0;
  background: transparent;
  cursor: pointer;
  color: #274064;
}

.student-layout__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #62a7ff 0%, #2d6bf2 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  box-shadow: 0 10px 18px rgba(57, 112, 224, 0.2);
}

.student-layout__avatar img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-layout__profile span {
  font-size: 15px;
  font-weight: 600;
}

.student-layout__profile .iconfont {
  color: #7e8fa7;
  font-size: 12px;
}

.student-layout__content {
  flex: 1;
  min-height: 0;
  padding: 24px 24px 28px;
  overflow: auto;
  background: radial-gradient(circle at top right,
      rgba(120, 169, 255, 0.08),
      transparent 18%),
    linear-gradient(180deg, #f6f9ff 0%, #f2f7ff 100%);
}

@media (max-width: 1080px) {
  .student-layout {
    grid-template-columns: 1fr;
  }

  .student-layout__sidebar {
    gap: 14px;
    border-right: 0;
    border-bottom: 1px solid rgba(221, 231, 247, 0.88);
  }

  .student-layout__nav {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .student-layout__promo {
    display: none;
  }
}

@media (max-width: 720px) {
  .student-layout__header {
    flex-direction: column;
    align-items: stretch;
    height: auto;
    padding: 16px;
  }

  .student-layout__header-actions {
    justify-content: flex-end;
  }

  .student-layout__nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .student-layout__content {
    padding: 16px;
  }
}
</style>
