<template>
  <div class="profile-page">
    <section class="profile-page__hero card-shell">
      <div class="profile-page__user">
        <div class="profile-page__avatar">
          <img v-if="profileView.avatarUrl" :src="profileView.avatarUrl" alt="头像">
          <span v-else>{{ avatarText }}</span>
        </div>
        <div class="profile-page__summary">
          <div class="profile-page__name-row">
            <h1>{{ profileView.realName }}</h1>
            <el-tag type="primary" effect="light" round>学生</el-tag>
            <button type="button" class="profile-link profile-page__inline-link" @click="openProfileDrawer">
              <i class="iconfont icon-menu-user" />
              编辑个人信息
            </button>
            <button type="button" class="profile-link profile-page__inline-link" @click="handlePasswordAction">
              <i class="iconfont icon-password" />
              修改密码
            </button>
          </div>
          <p>{{ profileView.majorName }} · {{ profileView.gradeName }}</p>
          <p>{{ profileView.email }}</p>
          <p>学号：{{ profileView.userNo }}</p>
        </div>
      </div>

      <div class="profile-page__hero-stats">
        <article v-for="item in heroStats" :key="item.label" class="profile-page__hero-stat">
          <div class="profile-page__hero-stat-icon" :class="item.theme">
            <i class="iconfont" :class="item.iconClass" />
          </div>
          <div class="profile-page__hero-stat-content">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.tip }}</small>
          </div>
        </article>
      </div>
    </section>

    <section class="profile-page__overview card-shell">
      <button v-for="item in overviewCards" :key="item.label" type="button" class="profile-page__overview-card"
        @click="handleOverviewClick(item)">
        <div class="profile-page__overview-icon" :class="item.theme">
          <i class="iconfont" :class="item.iconClass" />
        </div>
        <div class="profile-page__overview-content">
          <strong>{{ item.label }}</strong>
          <span>{{ item.desc }}</span>
        </div>
      </button>
    </section>

    <section class="profile-page__content">
      <div class="profile-page__main">
        <div class="card-shell profile-panel">
          <div class="profile-panel__head">
            <h2>最近学习记录</h2>
            <button type="button" class="profile-link" @click="router.push('/courses')">
              查看全部
            </button>
          </div>

          <div v-if="loading" class="profile-page__state">正在加载个人中心...</div>
          <div v-else-if="!recentCourses.length" class="profile-page__state is-empty">
            暂无学习记录
          </div>

          <div v-else class="recent-course-list">
            <article v-for="item in recentCourses" :key="item.courseId" class="recent-course-item">
              <div class="recent-course-item__cover">
                <img v-if="buildCoverUrl(item.coverPath)" :src="buildCoverUrl(item.coverPath)" :alt="item.courseName">
                <div v-else class="recent-course-item__cover-fallback" :class="item.coverTheme">
                  <span>{{ item.courseName?.slice(0, 2) || '课程' }}</span>
                </div>
              </div>

              <div class="recent-course-item__content">
                <strong>{{ item.courseName }}</strong>
                <p>{{ item.lastStudyLabel }}</p>
                <div class="recent-course-item__progress-meta">
                  <span>学习进度</span>
                  <em>{{ item.progress }}%</em>
                </div>
                <div class="recent-course-item__progress">
                  <span :style="{ width: `${item.progress}%` }" />
                </div>
              </div>

              <button type="button" class="profile-link recent-course-item__action"
                @click="router.push(`/courses/${item.courseId}/study`)">
                继续学习
              </button>
            </article>
          </div>
        </div>
      </div>

      <aside class="profile-page__aside">
        <div class="card-shell profile-panel">
          <div class="profile-panel__head">
            <h2>我的成就</h2>
          </div>

          <div class="achievement-list">
            <article v-for="item in achievementList" :key="item.title" class="achievement-item"
              :class="{ 'is-locked': !item.unlocked }">
              <div class="achievement-item__badge" :class="item.theme">
                <i class="iconfont icon-medal" />
              </div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </article>
          </div>
        </div>
      </aside>
    </section>

    <el-dialog v-model="homeworkDialogVisible" title="选择作业" width="720px" class="profile-page__homework-dialog"
      destroy-on-close>
      <div v-if="homeworkLoading" class="profile-page__dialog-state">
        正在加载作业列表...
      </div>
      <div v-else-if="!homeworkOptions.length" class="profile-page__dialog-state is-empty">
        当前还没有可进入的课后作业
      </div>
      <div v-else class="profile-page__homework-list">
        <button v-for="item in homeworkOptions" :key="`${item.courseId}-${item.lessonId}`" type="button"
          class="profile-page__homework-item" @click="openHomeworkDetail(item)">
          <div class="profile-page__homework-main">
            <strong>{{ item.paperName }}</strong>
            <p>{{ item.courseName }} · {{ item.chapterName }} · {{ item.lessonName }}</p>
          </div>
          <span class="profile-page__homework-action">进入作业</span>
        </button>
      </div>
    </el-dialog>

    <ProfileEditDrawer v-model:show="profileDrawerVisible" :model-value="profileForm" :avatar-text="avatarText"
      @save="saveProfilePrefs" />
    <ProfilePasswordDrawer v-model:show="passwordDrawerVisible" @save="savePassword" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Message from '@/utils/Message'
import { updatePassword, updateProfile, uploadAvatar } from '@/api/auth'
import { buildResourceFileUrl } from '@/utils/resource'
import { getMyCourseDetail, loadMyCourseList } from '@/api/course'
import { loadMyExamList, normalizeExamItem } from '@/api/exam'
import { loadStudyPlanDashboard } from '@/api/plan'
import ProfileEditDrawer from './components/ProfileEditDrawer.vue'
import ProfilePasswordDrawer from './components/ProfilePasswordDrawer.vue'

const PROFILE_PREFS_KEY = 'studentProfilePreferences'
const NOTE_COUNT_KEY = 'studentNoteCount'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const courseList = ref([])
const examList = ref([])
const dashboard = ref({
  totalTaskCount: 0,
  completedTaskCount: 0,
  totalStudyHours: 0,
  todayPlanList: [],
  calendarPlanList: [],
  planList: [],
})
const profilePrefs = ref({
  email: '',
  majorName: '计算机科学与技术',
  gradeName: '大三',
  motto: '坚持学习，遇见更好的自己',
  avatarPath: '',
})
const noteCount = ref(0)
const profileDrawerVisible = ref(false)
const passwordDrawerVisible = ref(false)
const homeworkDialogVisible = ref(false)
const homeworkLoading = ref(false)
const homeworkOptions = ref([])

const profileForm = ref({
  realName: '',
  email: '',
  majorName: '',
  gradeName: '',
  motto: '',
  avatarPath: '',
  avatarUrl: '',
  avatarFile: null,
})

const createCourseTheme = (courseId = '') => {
  const themes = ['is-cosmos', 'is-ice', 'is-violet', 'is-campus']
  const seed = String(courseId)
    .split('')
    .reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return themes[seed % themes.length]
}

const buildCourseView = (course = {}) => {
  const progress = Math.max(0, Math.min(100, Number(course.progress || 0)))
  const chapterCount = Number(course.chapterCount || 0)
  const currentChapterIndex = Number(course.currentChapterIndex || 0)
  const chapterText = chapterCount
    ? `第 ${Math.min(chapterCount, Math.max(1, currentChapterIndex || (progress > 0 ? 1 : 0) || 1))} 章 / 共 ${chapterCount} 章`
    : '暂无章节'
  return {
    ...course,
    progress,
    coverTheme: createCourseTheme(course.courseId),
    lastStudyLabel: course.lastStudyTime
      ? `上次学习：${String(course.lastStudyTime).slice(0, 16)}`
      : '暂未开始学习',
    chapterText,
  }
}

const formatDate = (value) => {
  if (!value) {
    return ''
  }
  return String(value).slice(0, 10)
}

const parseJsonStorage = (key, fallback) => {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

const buildCoverUrl = (path) => buildResourceFileUrl(path)
const buildAvatarUrl = (path) => buildResourceFileUrl(path)

const avatarText = computed(() => {
  const name = authStore.userInfo?.realName || '同学'
  return name.slice(0, 1)
})

const profileView = computed(() => {
  const userInfo = authStore.userInfo || {}
  const userNo = userInfo.userNo || '未设置'
  return {
    realName: userInfo.realName || '张同学',
    userNo,
    email:
      userInfo.email ||
      profilePrefs.value.email ||
      `${String(userNo).toLowerCase()}@campus.edu.cn`,
    majorName: profilePrefs.value.majorName || '计算机科学与技术',
    gradeName: profilePrefs.value.gradeName || '大三',
    avatarUrl: buildAvatarUrl(userInfo.avatar || profilePrefs.value.avatarPath),
  }
})

const pendingExamCount = computed(
  () => examList.value.filter((item) => !item.submitted).length
)
const submittedExamCount = computed(
  () => examList.value.filter((item) => item.submitted).length
)
const favoriteCount = computed(
  () => courseList.value.filter((item) => Number(item.isCollected || 0) === 1).length
)
const planCount = computed(() => (dashboard.value.planList || []).length)
const totalTaskCount = computed(() =>
  Number(dashboard.value.totalTaskCount || 0)
)
const completedTaskCount = computed(() =>
  Number(dashboard.value.completedTaskCount || 0)
)
const totalStudyHours = computed(() =>
  Number(dashboard.value.totalStudyHours || 0).toFixed(1)
)

const studyDateSet = computed(() => {
  const result = new Set()
    ; (dashboard.value.calendarPlanList || []).forEach((item) => {
      if (item.studyDate) {
        result.add(item.studyDate)
      }
    })
  courseList.value.forEach((item) => {
    const key = formatDate(item.updateTime)
    if (key) {
      result.add(key)
    }
  })
  return result
})

const studiedDays = computed(() => studyDateSet.value.size)

const continuousDays = computed(() => {
  const dateSet = studyDateSet.value
  if (!dateSet.size) {
    return 0
  }
  const cursor = new Date()
  cursor.setHours(0, 0, 0, 0)
  let count = 0
  while (dateSet.has(formatDate(cursor.toISOString()))) {
    count += 1
    cursor.setDate(cursor.getDate() - 1)
  }
  return count
})

const heroStats = computed(() => [
  {
    label: '累计学习时长',
    value: `${totalStudyHours.value} 小时`,
    tip: `超过 ${Math.min(
      98,
      60 + Math.round(Number(totalStudyHours.value) * 0.8)
    )}% 的同学`,
    iconClass: 'icon-time',
    theme: 'is-blue',
  },
  {
    label: '连续学习天数',
    value: `${continuousDays.value} 天`,
    tip: continuousDays.value >= 3 ? '继续保持哦！' : '从今天开始建立节奏',
    iconClass: 'icon-completed',
    theme: 'is-green',
  },
])

const overviewCards = computed(() => [
  {
    key: 'courses',
    label: '我的课程',
    desc: `${courseList.value.length} 门课程`,
    iconClass: 'icon-courses',
    theme: 'is-blue',
  },
  {
    key: 'plans',
    label: '学习计划',
    desc: `${planCount.value} 个计划`,
    iconClass: 'icon-menu-plan',
    theme: 'is-green',
  },
  {
    key: 'homework',
    label: '我的作业',
    desc: `${completedTaskCount.value}/${totalTaskCount.value} 项完成`,
    iconClass: 'icon-menu-exam',
    theme: 'is-orange',
  },
  {
    key: 'exams',
    label: '我的考试',
    desc: `${pendingExamCount.value} 场待参加`,
    iconClass: 'icon-analysis',
    theme: 'is-purple',
  },
  {
    key: 'favorites',
    label: '我的收藏',
    desc: `${favoriteCount.value} 个内容`,
    iconClass: 'icon-collection',
    theme: 'is-gold',
  },
])

const recentCourses = computed(() =>
  [...courseList.value]
    .filter((item) => item.lastStudyTime)
    .sort((a, b) =>
      String(b.lastStudyTime || '').localeCompare(String(a.lastStudyTime || ''))
    )
    .slice(0, 4)
)

const achievementList = computed(() => [
  {
    title: '学习新星',
    desc: `累计学习 ${Math.max(studiedDays.value, 1)} 天`,
    theme: 'is-blue',
    unlocked: studiedDays.value >= 1,
  },
  {
    title: '勤奋学习',
    desc: `累计学习 ${totalStudyHours.value} 小时`,
    theme: 'is-green',
    unlocked: Number(totalStudyHours.value) >= 5,
  },
  {
    title: '计划达人',
    desc: `完成 ${completedTaskCount.value} 个计划任务`,
    theme: 'is-orange',
    unlocked: completedTaskCount.value >= 3,
  },
  {
    title: '作业之星',
    desc: `参与 ${submittedExamCount.value} 次考试/作业`,
    theme: 'is-gold',
    unlocked: submittedExamCount.value >= 1,
  },
])

function syncProfileForm() {
  profileForm.value = {
    realName: authStore.userInfo?.realName || '',
    email: profileView.value.email,
    majorName: profilePrefs.value.majorName || '',
    gradeName: profilePrefs.value.gradeName || '',
    motto: profilePrefs.value.motto || '',
    avatarPath: authStore.userInfo?.avatar || profilePrefs.value.avatarPath || '',
    avatarUrl: buildAvatarUrl(authStore.userInfo?.avatar || profilePrefs.value.avatarPath),
    avatarFile: null,
  }
}

function loadLocalPrefs() {
  const localPrefs = parseJsonStorage(PROFILE_PREFS_KEY, {})
  profilePrefs.value = {
    ...profilePrefs.value,
    ...localPrefs,
    avatarPath:
      localPrefs.avatarPath ||
      (localPrefs.avatarUrl && !String(localPrefs.avatarUrl).startsWith('data:')
        ? localPrefs.avatarUrl
        : ''),
  }
  noteCount.value = Number(localStorage.getItem(NOTE_COUNT_KEY) || 0)
}

async function loadData() {
  loading.value = true
  try {
    await authStore.fetchLoginInfo()
    loadLocalPrefs()
    syncProfileForm()

    const [courseResult, planResult, examResult] = await Promise.all([
      loadMyCourseList(),
      loadStudyPlanDashboard(),
      loadMyExamList(),
    ])

    courseList.value = (Array.isArray(courseResult) ? courseResult : []).map(
      (item) => buildCourseView(item)
    )
    dashboard.value = planResult || dashboard.value
    examList.value = (Array.isArray(examResult) ? examResult : []).map((item) =>
      normalizeExamItem(item)
    )
  } finally {
    loading.value = false
  }
}

function handleOverviewClick(item) {
  if (item.key === 'courses') {
    router.push('/courses')
    return
  }
  if (item.key === 'homework') {
    openHomeworkPicker()
    return
  }
  if (item.key === 'plans') {
    router.push('/plans')
    return
  }
  if (item.key === 'exams') {
    router.push('/exams')
    return
  }
  if (item.key === 'favorites') {
    router.push({ path: '/courses', query: { tab: 'favorite' } })
    return
  }
  handleComingSoon(item.label)
}

function openProfileDrawer() {
  syncProfileForm()
  profileDrawerVisible.value = true
}

function handlePasswordAction() {
  passwordDrawerVisible.value = true
}

function handleComingSoon(title) {
  Message.warning(`${title} 功能正在接入中`)
}

async function openHomeworkPicker() {
  homeworkDialogVisible.value = true
  if (homeworkLoading.value || homeworkOptions.value.length) {
    return
  }
  homeworkLoading.value = true
  try {
    const detailList = await Promise.all(
      courseList.value.map((item) => getMyCourseDetail(item.courseId))
    )
    const result = []
    detailList.forEach((courseDetail) => {
      if (!courseDetail?.courseId) {
        return
      }
      ; (courseDetail.chapterList || []).forEach((chapter) => {
        ; (chapter.lessonList || []).forEach((lesson) => {
          if (!lesson?.paperId || Number(lesson.paperType || 0) !== 1) {
            return
          }
          result.push({
            courseId: courseDetail.courseId,
            courseName: courseDetail.courseName || '未命名课程',
            chapterId: chapter.chapterId,
            chapterName: chapter.chapterName || '未命名章节',
            lessonId: lesson.lessonId,
            lessonName: lesson.lessonName || '未命名课时',
            paperName: lesson.paperName || lesson.lessonName || '课后作业',
          })
        })
      })
    })
    homeworkOptions.value = result
  } finally {
    homeworkLoading.value = false
  }
}

function openHomeworkDetail(item) {
  if (!item?.courseId || !item?.lessonId) {
    return
  }
  homeworkDialogVisible.value = false
  router.push(`/courses/${item.courseId}/homework/${item.lessonId}`)
}

async function saveProfilePrefs(formData) {
  if (!formData?.realName) {
    Message.warning('请填写姓名')
    return
  }
  if (!formData?.email) {
    Message.warning('请填写邮箱')
    return
  }
  let avatarPath =
    formData.avatarPath ||
    authStore.userInfo?.avatar ||
    profilePrefs.value.avatarPath ||
    ''
  if (formData.avatarFile) {
    const uploadResult = await uploadAvatar(formData.avatarFile)
    if (!uploadResult) {
      return
    }
    avatarPath = uploadResult
  }
  const userInfo = await updateProfile({
    realName: formData.realName,
    email: formData.email,
    avatar: avatarPath,
  })
  if (!userInfo) {
    return
  }
  profilePrefs.value = {
    email: formData.email,
    majorName: formData.majorName,
    gradeName: formData.gradeName,
    motto: formData.motto,
    avatarPath,
  }
  localStorage.setItem(PROFILE_PREFS_KEY, JSON.stringify(profilePrefs.value))
  authStore.updateUserInfo({
    ...userInfo,
    realName: formData.realName,
    email: formData.email,
    avatar: avatarPath,
  })
  profileForm.value = {
    ...formData,
    avatarPath,
    avatarUrl: buildAvatarUrl(avatarPath),
    avatarFile: null,
  }
  Message.success('个人信息已更新')
  profileDrawerVisible.value = false
}

async function savePassword(formData) {
  const result = await updatePassword(formData)
  if (!result) {
    return
  }
  Message.success('密码已修改，请重新登录')
  passwordDrawerVisible.value = false
  await authStore.logout()
  router.replace('/login')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.card-shell {
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 14px 30px rgba(49, 87, 148, 0.06);
}

.profile-link {
  border: 0;
  background: transparent;
  color: #2d73f5;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.profile-page__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
  gap: 24px;
  align-items: center;
  padding: 22px 24px;
  background: linear-gradient(120deg,
      rgba(236, 244, 255, 0.98),
      rgba(255, 255, 255, 0.98)),
    radial-gradient(circle at right top,
      rgba(111, 164, 255, 0.16),
      transparent 24%);
}

.profile-page__user {
  display: flex;
  gap: 22px;
  min-width: 0;
}

.profile-page__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 108px;
  height: 108px;
  overflow: hidden;
  border: 4px solid rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  background: linear-gradient(135deg, #8bb8ff 0%, #2d73f5 100%);
  color: #fff;
  font-size: 36px;
  box-shadow: 0 16px 28px rgba(45, 115, 245, 0.18);

  span {
    font-size: 40px;
  }

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
  }
}

.profile-page__summary {
  min-width: 0;

  p {
    margin: 0 0 8px;
    color: #5d7392;
    font-size: 14px;
  }
}

.profile-page__name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  h1 {
    margin: 0;
    color: #162f58;
    font-size: 28px;
  }
}

.profile-page__inline-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;

  .iconfont {
    font-size: 14px;
  }
}

.profile-page__hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.profile-page__hero-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.profile-page__hero-stat+.profile-page__hero-stat {
  padding-left: 16px;
  border-left: 1px solid #edf2fb;
}

.profile-page__hero-stat-icon,
.profile-page__overview-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 6px;

  .iconfont {
    font-size: 22px;
  }

  &.is-blue {
    background: #eaf2ff;
    color: #2d73f5;
  }

  &.is-green {
    background: #e8fbef;
    color: #25ae64;
  }

  &.is-orange {
    background: #fff2e4;
    color: #ff9b35;
  }

  &.is-purple {
    background: #f0ebff;
    color: #865dff;
  }

  &.is-blue-soft {
    background: #ecf4ff;
    color: #4590ff;
  }

  &.is-gold {
    background: #fff7df;
    color: #ffb52f;
  }
}

.profile-page__hero-stat-content {
  min-width: 0;

  span,
  small {
    display: block;
    color: #7385a1;
    font-size: 13px;
  }

  strong {
    display: block;
    margin: 6px 0;
    color: #162f58;
    font-size: 20px;
    line-height: 1.1;
  }
}

.profile-page__overview {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  overflow: hidden;
}

.profile-page__overview-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 18px;
  border: 0;
  border-left: 1px solid #edf2fb;
  background: transparent;
  cursor: pointer;
  text-align: left;

  &:first-child {
    border-left: 0;
  }
}

.profile-page__overview-content {

  strong,
  span {
    display: block;
  }

  strong {
    margin-bottom: 6px;
    color: #162f58;
    font-size: 16px;
  }

  span {
    color: #6f839d;
    font-size: 13px;
  }
}

.profile-page__content {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) 360px;
  gap: 16px;
  align-items: start;
}

.profile-panel {
  padding: 18px 20px;
}

.profile-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    color: #182f56;
    font-size: 18px;
  }
}

.profile-page__state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #6f839d;
  font-size: 14px;

  &.is-empty {
    min-height: 160px;
  }
}

.recent-course-list {
  display: grid;
}

.recent-course-item {
  display: grid;
  grid-template-columns: 136px minmax(0, 1fr) 90px;
  gap: 18px;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid #edf2fb;

  &:first-child {
    border-top: 0;
    padding-top: 0;
  }
}

.recent-course-item__cover {
  height: 82px;
  overflow: hidden;
  border-radius: 10px;
  background: linear-gradient(135deg, #dceaff 0%, #edf4ff 100%);

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.recent-course-item__cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;

  span {
    padding: 8px 14px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.14);
    font-size: 22px;
    font-weight: 700;
  }

  &.is-cosmos {
    background: radial-gradient(circle at center,
        rgba(113, 177, 255, 0.22),
        transparent 22%),
      linear-gradient(135deg, #071d56 0%, #0d3f91 100%);
  }

  &.is-ice {
    color: #2a4f85;
    background: radial-gradient(circle at 42% 40%,
        rgba(255, 255, 255, 0.85),
        transparent 28%),
      linear-gradient(135deg, #cde0ff 0%, #eaf2ff 100%);
  }

  &.is-violet {
    background: radial-gradient(circle at 50% 40%,
        rgba(173, 138, 255, 0.2),
        transparent 24%),
      linear-gradient(135deg, #2e255f 0%, #5f37b8 100%);
  }

  &.is-campus {
    color: #294b6d;
    background: linear-gradient(180deg,
        rgba(165, 219, 255, 0.5),
        rgba(255, 255, 255, 0)),
      linear-gradient(135deg, #d5f0d8 0%, #f9fff6 100%);
  }
}

.recent-course-item__content {
  min-width: 0;

  strong {
    display: block;
    margin-bottom: 8px;
    color: #162f58;
    font-size: 17px;
  }

  p {
    margin: 0 0 12px;
    color: #6f839d;
    font-size: 14px;
  }
}

.recent-course-item__progress-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
  color: #6f839d;
  font-size: 13px;

  em {
    color: #162f58;
    font-style: normal;
    font-weight: 700;
  }
}

.recent-course-item__progress {
  height: 6px;
  border-radius: 999px;
  background: #edf2fb;
  overflow: hidden;

  span {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
  }
}

.recent-course-item__action {
  justify-self: end;
}

.achievement-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.achievement-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 10px 8px;
  text-align: center;

  &.is-locked {
    opacity: 0.5;
  }

  strong {
    color: #162f58;
    font-size: 16px;
  }

  p {
    margin: 0;
    color: #6f839d;
    font-size: 13px;
    line-height: 1.6;
  }
}

.achievement-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  clip-path: polygon(50% 0%,
      90% 18%,
      100% 58%,
      74% 100%,
      26% 100%,
      0% 58%,
      10% 18%);
  color: #fff;
  box-shadow: 0 16px 26px rgba(55, 94, 160, 0.16);

  .iconfont {
    font-size: 28px;
  }

  &.is-blue {
    background: linear-gradient(135deg, #58a1ff 0%, #2f6cf2 100%);
  }

  &.is-green {
    background: linear-gradient(135deg, #79d95d 0%, #31ae47 100%);
  }

  &.is-orange {
    background: linear-gradient(135deg, #ffcb58 0%, #ff9728 100%);
  }

  &.is-gold {
    background: linear-gradient(135deg, #ffd557 0%, #ffb22b 100%);
  }
}

.profile-page__dialog-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  color: #6f839d;
  font-size: 14px;

  &.is-empty {
    min-height: 140px;
  }
}

.profile-page__homework-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-page__homework-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid #e8eef8;
  border-radius: 6px;
  background: #fbfdff;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease, box-shadow 0.2s ease,
    transform 0.2s ease;

  &:hover {
    border-color: #cfe0ff;
    box-shadow: 0 12px 24px rgba(49, 87, 148, 0.08);
    transform: translateY(-1px);
  }
}

.profile-page__homework-main {
  min-width: 0;

  strong {
    display: block;
    margin-bottom: 6px;
    color: #162f58;
    font-size: 16px;
  }

  p {
    margin: 0;
    color: #6f839d;
    font-size: 13px;
  }
}

.profile-page__homework-action {
  flex: 0 0 auto;
  color: #2d73f5;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 1280px) {

  .profile-page__hero,
  .profile-page__content {
    grid-template-columns: 1fr;
  }

  .profile-page__overview {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .profile-page__overview-card:nth-child(4) {
    border-left: 0;
  }

  .profile-page__overview-card:nth-child(n + 4) {
    border-top: 1px solid #edf2fb;
  }
}

@media (max-width: 960px) {

  .profile-page__hero-stats,
  .achievement-list {
    grid-template-columns: 1fr;
  }

  .profile-page__hero-stat+.profile-page__hero-stat {
    padding-left: 0;
    border-left: 0;
  }

  .profile-page__overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .profile-page__overview-card:nth-child(3) {
    border-left: 0;
  }

  .profile-page__overview-card:nth-child(n + 3) {
    border-top: 1px solid #edf2fb;
  }

  .recent-course-item {
    grid-template-columns: 1fr;
  }

  .recent-course-item__cover {
    height: 180px;
  }

  .recent-course-item__action {
    justify-self: start;
  }
}

@media (max-width: 720px) {

  .profile-page__user,
  .profile-page__overview {
    grid-template-columns: 1fr;
  }

  .profile-page__user {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-page__overview-card {
    border-left: 0;
    border-top: 1px solid #edf2fb;

    &:first-child {
      border-top: 0;
    }
  }
}
</style>
