<template>
  <div class="course-page">
    <section class="course-page__header">
      <div class="course-page__heading">
        <h1>我的课程</h1>
      </div>
      <div class="course-page__tools">
        <label class="course-page__search">
          <input v-model.trim="keyword" type="text" placeholder="搜索我的课程">
          <span class="course-page__search-icon">
            <i class="iconfont icon-search" />
          </span>
        </label>

        <div class="course-page__sort-wrap">
          <el-select v-model="sortType" class="course-page__sort" popper-class="course-page__sort-popper">
            <el-option label="最新学习" value="latest" />
            <el-option label="课程名称" value="name" />
            <el-option label="进度优先" value="progress" />
            <el-option label="章节优先" value="chapter" />
          </el-select>
        </div>
      </div>
    </section>

    <section class="course-page__filter card-shell">
      <el-tabs v-model="activeTab" class="course-page__el-tabs">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :label="tab.label" :name="tab.key" />
      </el-tabs>
    </section>

    <section class="course-page__stats">
      <article v-for="item in statsCards" :key="item.label" class="course-page__stats-card">
        <div class="course-page__stats-icon" :class="item.theme">
          <i class="iconfont" :class="item.iconClass" />
        </div>
        <div class="course-page__stats-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.tip }}</small>
        </div>
      </article>
    </section>

    <section class="course-page__body">
      <div v-if="loading" class="course-page__state">正在加载课程...</div>

      <div v-else-if="!displayCourses.length" class="course-page__state is-empty">
        <strong>{{ courseList.length ? '没有匹配到课程' : '当前班级还没有可学习的课程' }}</strong>
        <p>{{ courseList.length ? '试试切换标签或输入别的关键词。' : '请先在后台为当前学生班级配置课程。' }}</p>
      </div>

      <div v-else class="course-page__list">
        <article v-for="course in displayCourses" :key="course.courseId" class="course-page__item">
          <div class="course-page__cover">
            <img v-if="buildCoverUrl(course.coverPath)" :src="buildCoverUrl(course.coverPath)" :alt="course.courseName">
            <div v-else class="course-page__cover-fallback" :class="course.coverTheme">
              <span>{{ course.courseName?.slice(0, 2) || '课程' }}</span>
            </div>
          </div>

          <div class="course-page__course-info">
            <h3>{{ course.courseName || '未命名课程' }}</h3>
            <div class="course-page__subline">
              <span>
                <i class="iconfont icon-user" />
                {{ course.teacherName || '-' }}
              </span>
              <span>
                <i class="iconfont icon-class" />
                {{ course.classNames || '未配置班级' }}
              </span>
            </div>
            <div class="course-page__progress-labels">
              <span>{{ course.chapterText }}</span>
              <span>{{ course.progress }}%</span>
            </div>
            <div class="course-page__progress">
              <span :style="{ width: `${course.progress}%` }" />
            </div>
          </div>

          <div class="course-page__metric">
            <label>
              <i class="iconfont icon-time" />
              学习时长
            </label>
            <strong>{{ course.studyHours }} 小时</strong>
          </div>

          <div class="course-page__metric">
            <label>
              <i class="iconfont icon-courses" />
              结构
            </label>
            <strong>{{ course.structureText }}</strong>
          </div>

          <div class="course-page__metric">
            <label>
              <i class="iconfont icon-calendar" />
              最后学习时间
            </label>
            <strong>{{ formatDate(course.updateTime) }}</strong>
          </div>

          <button type="button" class="course-page__favorite"
            :class="{ 'is-active': Number(course.isCollected || 0) === 1 }" @click="toggleFavorite(course.courseId)">
            <i class="iconfont icon-collection" />
          </button>

          <button type="button" class="course-page__study-btn" @click="handleStudy(course)">继续学习</button>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loadMyCourseList, saveCourseCollection } from '@/api/course'
import { buildResourceFileUrl } from '@/utils/resource'

const loading = ref(false)
const keyword = ref('')
const activeTab = ref('learning')
const sortType = ref('latest')
const courseList = ref([])
const router = useRouter()
const route = useRoute()

const tabs = [
  { key: 'all', label: '全部课程' },
  { key: 'learning', label: '学习中' },
  { key: 'completed', label: '已完成' },
  { key: 'favorite', label: '收藏' },
]

const createCourseTheme = (courseId = '') => {
  const themes = ['is-cosmos', 'is-ice', 'is-violet', 'is-campus']
  const seed = String(courseId || '')
    .split('')
    .reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return themes[seed % themes.length]
}

const STUDY_STATUS_COMPLETED = 2

const formatStudyHours = (studySeconds) => {
  const seconds = Math.max(0, Number(studySeconds || 0))
  return (seconds / 3600).toFixed(1)
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
    chapterText,
    structureText: `${chapterCount} 章 / ${course.lessonCount || 0} 课时`,
    studyHours: formatStudyHours(course.studySeconds),
    isCompleted: Number(course.studyStatus || 0) === STUDY_STATUS_COMPLETED,
  }
}

const learningCourseCount = computed(
  () => courseList.value.filter((item) => !item.isCompleted).length
)
const completedCourseCount = computed(
  () => courseList.value.filter((item) => item.isCompleted).length
)
const totalHours = computed(() =>
  courseList.value
    .reduce((sum, item) => sum + Number(item.studyHours || 0), 0)
    .toFixed(1)
)
const averageProgress = computed(() => {
  if (!courseList.value.length) {
    return 0
  }
  const total = courseList.value.reduce(
    (sum, item) => sum + Number(item.progress || 0),
    0
  )
  return Math.round(total / courseList.value.length)
})

const statsCards = computed(() => [
  {
    label: '学习中课程',
    value: `${learningCourseCount.value} 门`,
    tip: `较上周 +${Math.max(1, Math.min(learningCourseCount.value, 2))} 门`,
    iconClass: 'icon-courses',
    theme: 'is-blue',
  },
  {
    label: '已完成课程',
    value: `${completedCourseCount.value} 门`,
    tip: `较上周 +${completedCourseCount.value ? 1 : 0} 门`,
    iconClass: 'icon-completed',
    theme: 'is-green',
  },
  {
    label: '总学习时长',
    value: `${totalHours.value} 小时`,
    tip: '较上周 +12%',
    iconClass: 'icon-time',
    theme: 'is-orange',
  },
  {
    label: '平均进度',
    value: `${averageProgress.value}%`,
    tip: '较上周 +8%',
    iconClass: 'icon-analysis',
    theme: 'is-purple',
  },
])

const displayCourses = computed(() => {
  let list = [...courseList.value]

  if (activeTab.value === 'learning') {
    list = list.filter((item) => !item.isCompleted)
  } else if (activeTab.value === 'completed') {
    list = list.filter((item) => item.isCompleted)
  } else if (activeTab.value === 'favorite') {
    list = list.filter((item) => Number(item.isCollected || 0) === 1)
  }

  if (keyword.value) {
    const text = keyword.value.toLowerCase()
    list = list.filter((item) =>
      [item.courseName, item.teacherName, item.classNames, item.description]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(text))
    )
  }

  list.sort((a, b) => {
    if (sortType.value === 'name') {
      return String(a.courseName || '').localeCompare(
        String(b.courseName || ''),
        'zh-CN'
      )
    }
    if (sortType.value === 'progress') {
      return Number(b.progress || 0) - Number(a.progress || 0)
    }
    if (sortType.value === 'chapter') {
      return Number(b.chapterCount || 0) - Number(a.chapterCount || 0)
    }
    return String(b.updateTime || '').localeCompare(String(a.updateTime || ''))
  })

  return list
})

const buildCoverUrl = (path) => buildResourceFileUrl(path)

const formatDate = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).slice(0, 10)
}

const toggleFavorite = async (courseId) => {
  if (!courseId) {
    return
  }
  const course = courseList.value.find((item) => item.courseId === courseId)
  if (!course) {
    return
  }
  const result = await saveCourseCollection({
    courseId,
    collectFlag: Number(course.isCollected || 0) === 1 ? 0 : 1,
  })
  if (result === null) {
    return
  }
  courseList.value = courseList.value.map((item) =>
    item.courseId === courseId ? { ...item, isCollected: Number(result || 0) } : item
  )
}

const handleStudy = (course) => {
  if (!course?.courseId) {
    return
  }
  router.push(`/courses/${course.courseId}/study`)
}

const loadCourses = async () => {
  if (loading.value) {
    return
  }
  loading.value = true
  const result = await loadMyCourseList()
  loading.value = false
  const list = Array.isArray(result) ? result : []
  courseList.value = list.map((item) => buildCourseView(item))
}

const syncActiveTabFromRoute = () => {
  const tab = String(route.query.tab || '')
  if (tabs.some((item) => item.key === tab)) {
    activeTab.value = tab
    return
  }
  activeTab.value = 'all'
}

const syncRouteTab = (tab) => {
  const normalizedTab = tabs.some((item) => item.key === tab)
    ? tab
    : 'all'
  if (route.query.tab === normalizedTab) {
    return
  }
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: normalizedTab,
    },
  })
}

onMounted(() => {
  syncActiveTabFromRoute()
  syncRouteTab(activeTab.value)
  loadCourses()
})

watch(
  () => route.query.tab,
  () => {
    syncActiveTabFromRoute()
  }
)

watch(
  activeTab,
  (value) => {
    syncRouteTab(value)
  }
)
</script>

<style scoped>
.course-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.card-shell {
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 30px rgba(49, 87, 148, 0.06);
}

.course-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 2px 0;
}

.course-page__heading h1 {
  margin: 0 0 14px;
  color: #182f56;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.05;
}

.course-page__tools {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 2px;
}

.course-page__filter {
  padding: 0 18px;
}

.course-page :deep(.course-page__el-tabs .el-tabs__header) {
  margin: 0;
}

.course-page :deep(.course-page__el-tabs .el-tabs__nav-wrap::after) {
  background-color: #edf2fb;
}

.course-page :deep(.course-page__el-tabs .el-tabs__item) {
  height: 54px;
  color: #7b8ba4;
  font-size: 14px;
  font-weight: 600;
}

.course-page :deep(.course-page__el-tabs .el-tabs__item.is-active) {
  color: #2a66f6;
}

.course-page :deep(.course-page__el-tabs .el-tabs__active-bar) {
  height: 3px;
  border-radius: 6px;
  background: #2a66f6;
}

.course-page :deep(.course-page__el-tabs .el-tabs__content) {
  display: none;
}

.course-page__search {
  display: flex;
  align-items: center;
  width: 270px;
  height: 38px;
  padding: 0 14px 0 16px;
  border: 1px solid #dde6f3;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 20px rgba(57, 84, 140, 0.04);
}

.course-page__search input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #274064;
  font-size: 13px;
}

.course-page__search input::placeholder {
  color: #97a6bc;
}

.course-page__search-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #7e8ea8;
}

.course-page__search-icon .iconfont {
  font-size: 16px;
}

.course-page__sort-wrap {
  width: 126px;
}

.course-page__sort {
  width: 100%;
}

.course-page :deep(.course-page__sort .el-select__wrapper) {
  min-height: 38px;
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dde6f3 inset;
}

.course-page__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 30px rgba(49, 87, 148, 0.06);
  overflow: hidden;
}

.course-page__stats-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px;
}

.course-page__stats-card+.course-page__stats-card {
  border-left: 1px solid #edf2fb;
}

.course-page__stats-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 6px;
}

.course-page__stats-icon .iconfont {
  font-size: 20px;
}

.course-page__stats-icon.is-blue {
  background: #e9f2ff;
  color: #2d79ff;
}

.course-page__stats-icon.is-green {
  background: #e8fbef;
  color: #23a55c;
}

.course-page__stats-icon.is-orange {
  background: #fff2e3;
  color: #ff9728;
}

.course-page__stats-icon.is-purple {
  background: #f0ebff;
  color: #7f5bff;
}

.course-page__stats-content span {
  display: block;
  margin-bottom: 8px;
  color: #7b8da6;
  font-size: 12px;
}

.course-page__stats-content strong {
  display: block;
  margin-bottom: 6px;
  color: #172f56;
  font-size: 20px;
  line-height: 1;
}

.course-page__stats-content small {
  color: #7b8da6;
  font-size: 12px;
}

.course-page__body {
  min-height: 280px;
}

.course-page__state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  color: #6f839d;
  font-size: 14px;
}

.course-page__state.is-empty {
  flex-direction: column;
  gap: 10px;
}

.course-page__state.is-empty strong {
  color: #183055;
  font-size: 18px;
}

.course-page__state.is-empty p {
  margin: 0;
}

.course-page__list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.course-page__item {
  display: grid;
  grid-template-columns: 188px minmax(0, 1.8fr) 0.95fr 1fr 1.1fr 52px 120px;
  gap: 18px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #e7edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 10px 22px rgba(65, 92, 145, 0.05);
}

.course-page__cover {
  height: 118px;
  border-radius: 6px;
  overflow: hidden;
  background: linear-gradient(135deg, #dceaff 0%, #edf4ff 100%);
}

.course-page__cover img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-page__cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #fff;
}

.course-page__cover-fallback span {
  padding: 8px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.course-page__cover-fallback.is-cosmos {
  background: radial-gradient(circle at center,
      rgba(113, 177, 255, 0.22),
      transparent 22%),
    linear-gradient(135deg, #071d56 0%, #0d3f91 100%);
}

.course-page__cover-fallback.is-ice {
  color: #2a4f85;
  background: radial-gradient(circle at 42% 40%,
      rgba(255, 255, 255, 0.85),
      transparent 28%),
    linear-gradient(135deg, #cde0ff 0%, #eaf2ff 100%);
}

.course-page__cover-fallback.is-violet {
  background: radial-gradient(circle at 50% 40%,
      rgba(173, 138, 255, 0.2),
      transparent 24%),
    linear-gradient(135deg, #2e255f 0%, #5f37b8 100%);
}

.course-page__cover-fallback.is-campus {
  color: #294b6d;
  background: linear-gradient(180deg,
      rgba(165, 219, 255, 0.5),
      rgba(255, 255, 255, 0)),
    linear-gradient(135deg, #d5f0d8 0%, #f9fff6 100%);
}

.course-page__course-info {
  min-width: 0;
  padding-right: 20px;
}

.course-page__course-info h3 {
  margin: 0 0 8px;
  color: #1a3158;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
}

.course-page__subline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 18px;
  color: #6d809d;
  font-size: 13px;
}

.course-page__subline span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.course-page__subline .iconfont {
  font-size: 14px;
}

.course-page__progress-labels {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
  color: #6f829d;
  font-size: 13px;
}

.course-page__progress {
  height: 6px;
  border-radius: 6px;
  background: #edf3fb;
  overflow: hidden;
}

.course-page__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
}

.course-page__metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.course-page__metric label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8a99af;
  font-size: 12px;
}

.course-page__metric label .iconfont {
  font-size: 14px;
}

.course-page__metric strong {
  color: #173458;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
}

.course-page__favorite {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #9aa8ba;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.course-page__favorite .iconfont {
  font-size: 18px;
}

.course-page__favorite:hover {
  background: #f4f7fc;
}

.course-page__favorite.is-active {
  color: #ffb32b;
}

.course-page__study-btn {
  min-width: 112px;
  height: 40px;
  padding: 0 18px;
  border: 0;
  border-radius: 6px;
  background: linear-gradient(135deg, #317dff 0%, #2569f3 100%);
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 12px 20px rgba(49, 125, 255, 0.2);
}

@media (max-width: 1360px) {
  .course-page__item {
    grid-template-columns: 188px minmax(0, 1.6fr) repeat(3, minmax(92px, 0.9fr)) 52px 112px;
  }
}

@media (max-width: 1280px) {
  .course-page__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .course-page__stats-card:nth-child(3) {
    border-left: 0;
  }

  .course-page__stats-card:nth-child(3),
  .course-page__stats-card:nth-child(4) {
    border-top: 1px solid #edf2fb;
  }

  .course-page__item {
    grid-template-columns: 188px minmax(0, 1fr) repeat(3, minmax(96px, 1fr));
  }

  .course-page__favorite,
  .course-page__study-btn {
    justify-self: start;
  }
}

@media (max-width: 960px) {
  .course-page__header {
    flex-direction: column;
    align-items: stretch;
  }

  .course-page__heading h1 {
    font-size: 28px;
  }

  .course-page__tools {
    flex-direction: column;
    align-items: stretch;
  }

  .course-page__search,
  .course-page__sort-wrap {
    width: 100%;
  }

  .course-page__item {
    grid-template-columns: 1fr;
  }

  .course-page__cover {
    height: 190px;
  }

  .course-page__favorite,
  .course-page__study-btn {
    justify-self: start;
  }
}

@media (max-width: 720px) {
  .course-page__stats {
    grid-template-columns: 1fr;
  }

  .course-page__stats-card+.course-page__stats-card {
    border-left: 0;
    border-top: 1px solid #edf2fb;
  }
}
</style>
