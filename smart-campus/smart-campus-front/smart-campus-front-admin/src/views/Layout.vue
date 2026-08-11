<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="brand-area">
        <div class="brand-logo">智</div>
        <div class="brand-copy">
          <div class="brand-title">智慧校园后台</div>
        </div>
      </div>

      <nav class="top-nav">
        <a v-for="group in menuGroups" :key="group.key"
          :class="['top-nav-item', { active: group.key === activeTopKey }]" href="javascript:void(0)"
          @click="goTo(group.defaultPath)">
          {{ group.title }}
        </a>
      </nav>

      <div class="user-area">
        <span class="user-name">{{ authStore.displayName || roleLabel }}</span>
        <a class="logout-link" href="javascript:void(0)" @click="handleLogout">退出登录</a>
        <div class="user-avatar">{{ avatarText }}</div>
      </div>
    </header>

    <div class="admin-body">
      <aside class="sidebar">
        <div class="sidebar-card">
          <el-scrollbar class="sidebar-scrollbar">
            <div class="sidebar-menu">
              <template v-for="section in currentSections" :key="section.key">
                <div class="sidebar-group">{{ section.title }}</div>

                <button v-for="item in section.items" :key="item.index"
                  :class="['sidebar-item', { active: activeMenu === item.index }]" type="button"
                  @click="goTo(item.index)">
                  {{ item.title }}
                </button>
              </template>
            </div>
          </el-scrollbar>
        </div>
      </aside>

      <section class="content-area">
        <main class="content-main">
          <RouterView />
        </main>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Message from '@/utils/Message'
import { useAuthStore } from '@/stores/auth'
import { adminMenuGroups } from '@/router/adminMenu'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const roleTextMap = {
  0: '系统管理员',
  1: '教师账号',
  2: '学生账号',
}

const currentUser = computed(() => authStore.userInfo)
const menuGroups = computed(() => adminMenuGroups.map((group) => {
  const sections = (group.sections || []).map((section) => ({
    ...section,
    items: (section.items || []).filter((item) => authStore.hasMenu(item.menuCode)),
  })).filter((section) => section.items.length)
  const firstPath = sections[0]?.items?.[0]?.index
  return firstPath ? { ...group, defaultPath: firstPath, sections } : null
}).filter(Boolean))
const activeMenu = computed(() => {
  if (route.path.startsWith('/teaching/paper/editor/')) {
    return '/teaching/paper'
  }
  return route.path
})
const roleLabel = computed(
  () => roleTextMap[currentUser.value?.roleType] || '系统管理员'
)
const avatarText = computed(() => {
  const name = currentUser.value?.realName || authStore.displayName || '智'
  return name.slice(0, 1) || '智'
})

const currentTopGroup = computed(() => {
  return menuGroups.value.find((group) => group.match(route.path)) || menuGroups.value[0]
})

const activeTopKey = computed(() => currentTopGroup.value?.key)
const currentSections = computed(() => currentTopGroup.value?.sections || [])

const goTo = (path) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const handleLogout = async () => {
  await authStore.logout()
  Message.success('已退出登录')
  router.replace('/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  min-height: 100vh;
  background: radial-gradient(
      circle at left top,
      rgba(160, 194, 255, 0.24),
      transparent 24%
    ),
    linear-gradient(180deg, #f4f7fd 0%, #edf2fb 100%);
  color: #24314d;
}

.admin-header {
  display: flex;
  align-items: center;
  height: 72px;
  padding: 0 26px;
  border-bottom: 1px solid #dde6f3;
  background: rgba(255, 255, 255, 0.88);
}

.brand-area {
  display: flex;
  align-items: center;
  width: 240px;
  gap: 12px;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #7ea8ff 0%, #99c0ff 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 10px 20px rgba(119, 156, 255, 0.24);
}

.brand-title {
  color: #22314e;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.1;
  white-space: nowrap;
}

.brand-subtitle {
  margin-top: 5px;
  color: #8090ad;
  font-size: 12px;
  letter-spacing: 0.24em;
  white-space: nowrap;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 32px;
  height: 100%;
  margin-left: 28px;
  flex: 1;
}

.top-nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 100%;
  color: #1f2d49;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 13px;
    height: 4px;
    border-radius: 999px;
    background: transparent;
    transition: background 0.2s ease;
  }

  &.active::after {
    background: #223250;
  }
}

.user-area {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  width: 240px;
  flex-shrink: 0;
  white-space: nowrap;
}

.user-name {
  color: #4f607f;
  font-size: 14px;
}

.logout-link {
  color: #6f93fb;
  font-size: 14px;
  text-decoration: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #88b8ff 0%, #79a8ff 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.admin-body {
  display: flex;
  gap: 10px;
  min-height: calc(100vh - 72px);
  padding: 10px;
}

.sidebar {
  width: 246px;
  flex-shrink: 0;
}

.sidebar-card {
  height: 100%;
  min-height: calc(100vh - 110px);
  border: 1px solid #dbe5f4;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 10px 30px rgba(97, 126, 178, 0.08);
  overflow: hidden;
}

.sidebar-scrollbar {
  height: 100%;
}

.sidebar-menu {
  padding: 18px 14px;
}

.sidebar-group {
  margin: 0 10px 10px;
  color: #93a0ba;
  font-size: 14px;
  line-height: 1.5;
}

.sidebar-item {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 42px;
  margin-bottom: 8px;
  padding: 0 18px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #2a3959;
  font-size: 15px;
  text-align: left;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
  appearance: none;
  outline: none;

  &:hover {
    background: rgba(125, 168, 255, 0.08);
  }

  &.active {
    background: linear-gradient(
      90deg,
      rgba(193, 219, 255, 0.92) 0%,
      rgba(232, 241, 255, 0.98) 100%
    );
    color: #4f84ef;
    box-shadow: inset 0 0 0 1px rgba(163, 195, 255, 0.35);
  }
}

.content-area {
  min-width: 0;
  flex: 1;
}

.content-main {
  width: 100%;
  height: 100%;
}

@media (max-width: 1280px) {
  .admin-header {
    padding: 0 18px;
  }

  .brand-area,
  .user-area {
    width: 210px;
  }

  .top-nav {
    gap: 22px;
    margin-left: 18px;
  }

  .sidebar {
    width: 220px;
  }

  .admin-body {
    padding: 16px;
    gap: 16px;
  }
}

@media (max-width: 960px) {
  .admin-header {
    flex-wrap: wrap;
    height: auto;
    padding: 14px 16px;
    gap: 14px;
  }

  .brand-area,
  .user-area {
    width: 100%;
  }

  .top-nav {
    width: 100%;
    height: 44px;
    margin-left: 0;
    overflow-x: auto;
  }

  .user-area {
    justify-content: flex-start;
  }

  .admin-body {
    flex-direction: column;
    min-height: auto;
  }

  .sidebar {
    width: 100%;
  }
}
</style>
