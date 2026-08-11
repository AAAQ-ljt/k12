<template>
  <div class="student-dashboard">
    <div class="student-dashboard__main">
      <section class="student-dashboard__shortcut-row card">
        <article v-for="item in shortcuts" :key="item.title" class="student-dashboard__shortcut">
          <span class="student-dashboard__shortcut-icon" :class="item.theme">
            <i class="iconfont" :class="item.iconClass" />
          </span>
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.desc }}</p>
          </div>
        </article>
      </section>

      <section class="student-dashboard__courses card">
        <div class="student-dashboard__section-header">
          <div class="student-dashboard__section-tabs">
            <h3>我的课程</h3>
            <button v-for="tab in courseTabs" :key="tab.key" type="button" class="student-dashboard__tab"
              :class="{ 'is-active': tab.key === activeCourseTab }" @click="activeCourseTab = tab.key">
              {{ tab.label }}
            </button>
          </div>

          <button type="button" class="student-dashboard__link-button" @click="router.push('/courses')">
            全部课程
          </button>
        </div>

        <div v-if="loading" class="student-dashboard__empty">正在加载课程数据...</div>
        <div v-else-if="!displayCourses.length" class="student-dashboard__empty">暂无课程数据</div>
        <div v-else class="student-dashboard__course-grid">
          <article v-for="(course, index) in displayCourses" :key="course.courseId || course.courseName"
            class="student-dashboard__course-card" @click="openCourse(course)">
            <div class="student-dashboard__course-cover" :class="resolveCourseTheme(index)">
              <img v-if="buildCoverUrl(course.coverPath)" :src="buildCoverUrl(course.coverPath)"
                :alt="course.courseName">
            </div>
            <strong>{{ course.courseName }}</strong>
            <p>{{ course.teacherName || '未设置教师' }}</p>
            <div class="student-dashboard__progress">
              <span>学习进度</span>
              <span>{{ course.progress || 0 }}%</span>
            </div>
            <div class="student-dashboard__progress-bar">
              <span :style="{ width: `${course.progress || 0}%` }" />
            </div>
          </article>
        </div>
      </section>

      <section class="student-dashboard__summary card">
        <div class="student-dashboard__section-header">
          <h3>学习总览</h3>
          <button type="button" class="student-dashboard__range-button" @click="router.push('/analysis')">
            近7天
          </button>
        </div>

        <div class="student-dashboard__summary-stats">
          <article v-for="item in summaryStats" :key="item.label" class="student-dashboard__summary-stat">
            <span class="student-dashboard__summary-icon" :class="item.theme">
              <i class="iconfont" :class="item.iconClass" />
            </span>
            <div>
              <p>{{ item.label }}</p>
              <strong>{{ item.value }}</strong>
              <small>{{ item.tip }}</small>
            </div>
          </article>
        </div>

        <div v-if="!chartTrendItems.length" class="student-dashboard__empty">暂无学习趋势数据</div>
        <div v-else class="student-dashboard__chart">
          <BaseEChart :option="trendChartOption" />
        </div>
      </section>
    </div>

    <aside class="student-dashboard__side">
      <section class="student-dashboard__side-card card">
        <div class="student-dashboard__section-header">
          <h3>学习任务</h3>
          <button type="button" class="student-dashboard__link-button" @click="router.push('/plans')">
            更多
          </button>
        </div>

        <div v-if="!tasks.length" class="student-dashboard__empty">近期没有待执行的学习任务</div>
        <div v-else class="student-dashboard__task-list">
          <article v-for="task in tasks" :key="task.key" class="student-dashboard__task-item">
            <div class="student-dashboard__task-cover" :class="task.theme">
              <img v-if="task.coverUrl" :src="task.coverUrl" :alt="task.title">
              <span v-else class="student-dashboard__task-cover-text">{{ task.icon }}</span>
            </div>
            <div class="student-dashboard__task-body">
              <strong>{{ task.title }}</strong>
              <p>{{ task.desc }}</p>
            </div>
            <span class="student-dashboard__task-deadline">{{ task.deadline }}</span>
          </article>
        </div>
      </section>

      <section class="student-dashboard__side-card card">
        <div class="student-dashboard__section-header">
          <h3>日历</h3>
          <div class="student-dashboard__calendar-title">{{ calendarTitle }}</div>
        </div>

        <div class="student-dashboard__calendar">
          <div class="student-dashboard__calendar-week">
            <span v-for="week in weekdays" :key="week">{{ week }}</span>
          </div>

          <div class="student-dashboard__calendar-grid">
            <span v-for="item in calendarDays" :key="`${item.text}-${item.offset}`"
              class="student-dashboard__calendar-day" :class="{
                'is-muted': item.muted,
                'is-active': item.active,
                'is-dot': item.hasDot,
              }">
              {{ item.text }}
            </span>
          </div>
        </div>
      </section>

      <section class="student-dashboard__side-card card">
        <div class="student-dashboard__section-header">
          <h3>通知公告</h3>
        </div>

        <div v-if="!noticeList.length" class="student-dashboard__empty">
          暂无可展示的通知公告数据
        </div>
        <div v-else class="student-dashboard__notice-list">
          <article v-for="notice in noticeList" :key="notice.noticeId" class="student-dashboard__notice-item"
            @click="openNotice(notice)">
            <span class="student-dashboard__notice-tag" :class="Number(notice.isTop) === 1 ? 'is-red' : 'is-blue'">
              {{ Number(notice.isTop) === 1 ? '置顶' : '公告' }}
            </span>
            <div class="student-dashboard__notice-body">
              <strong>{{ notice.noticeTitle }}</strong>
              <p>{{ notice.publishTime || '暂未发布' }}</p>
            </div>
          </article>
          <button v-if="noticeHasMore" type="button" class="student-dashboard__notice-more" :disabled="noticeLoading"
            @click="loadMoreNotices">
            {{ noticeLoading ? '加载中...' : '加载更多' }}
          </button>
          <div v-else-if="noticeList.length > noticePageSize" class="student-dashboard__notice-end">已加载全部公告</div>
        </div>
      </section>
    </aside>

    <NoticePreviewDialog v-model:show="noticeDialogVisible" :notice="currentNotice" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseEChart from '@/components/BaseEChart.vue'
import NoticePreviewDialog from '@/components/NoticePreviewDialog.vue'
import { buildResourceFileUrl } from '@/utils/resource'
import { loadMyExamList } from '@/api/exam'
import { loadStudyPlanDashboard } from '@/api/plan'
import { loadLearningAnalysisDashboard } from '@/api/analysis'
import { getNoticeDetail, loadLatestNotices } from '@/api/notice'

const router = useRouter()
const activeCourseTab = ref('studying')
const loading = ref(false)
const examList = ref([])
const planDashboard = ref({
  totalPlanCount: 0,
  totalTaskCount: 0,
  completedTaskCount: 0,
  todayPlanList: [],
  calendarPlanList: [],
})
const analysisDashboard = ref({
  overview: {},
  behavior: {},
  trendList: [],
  courseList: [],
})
const noticeList = ref([])
const noticePageNo = ref(1)
const noticePageSize = 5
const noticeTotalCount = ref(0)
const noticeLoading = ref(false)
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const courseTabs = [
  { key: 'studying', label: '学习中' },
  { key: 'completed', label: '已完成' },
]
const weekdays = ['一', '二', '三', '四', '五', '六', '日']
const courseThemeList = ['is-cosmos', 'is-ice', 'is-violet', 'is-campus']

const shortcuts = computed(() => {
  const pendingExamCount = examList.value.filter(
    (item) => !item.submitted
  ).length
  const todayTotal = (planDashboard.value.todayPlanList || []).length
  const todayCompleted = (planDashboard.value.todayPlanList || []).filter(
    (item) => item.completed
  ).length
  return [
    {
      title: '我的课程',
      desc: `${courseAnalysisList.value.length} 门课程可学习`,
      iconClass: 'icon-courses',
      theme: 'is-blue',
    },
    {
      title: '在线考试',
      desc: `${pendingExamCount} 场考试待参加`,
      iconClass: 'icon-menu-exam',
      theme: 'is-green',
    },
    {
      title: '学习计划',
      desc: `今日任务 ${todayCompleted}/${todayTotal}`,
      iconClass: 'icon-menu-plan',
      theme: 'is-orange',
    },
    {
      title: '学习分析',
      desc: `近7天活跃 ${activeDays.value} 天`,
      iconClass: 'icon-analysis',
      theme: 'is-purple',
    },
  ]
})

const courseAnalysisList = computed(
  () => analysisDashboard.value.courseList || []
)
const studyingCourses = computed(() =>
  courseAnalysisList.value.filter((item) => Number(item.progress || 0) < 100)
)
const completedCourses = computed(() =>
  courseAnalysisList.value.filter((item) => Number(item.progress || 0) >= 100)
)
const displayCourses = computed(() =>
  (activeCourseTab.value === 'completed'
    ? completedCourses.value
    : studyingCourses.value
  ).slice(0, 4)
)

const activeDays = computed(() =>
  Number(analysisDashboard.value.behavior?.activeDays || 0)
)

const summaryStats = computed(() => {
  const overview = analysisDashboard.value.overview || {}
  const courseProgress = averageCourseProgress.value
  return [
    {
      label: '学习时长',
      value: `${Number(overview.totalStudyHours || 0).toFixed(1)} 小时`,
      tip: buildGrowthTip(overview.hoursGrowthRate, '较上周期'),
      iconClass: 'icon-time',
      theme: 'is-blue',
    },
    {
      label: '课程进度',
      value: `${courseProgress}%`,
      tip: `共 ${courseAnalysisList.value.length} 门课程参与学习`,
      iconClass: 'icon-progress',
      theme: 'is-green',
    },
    {
      label: '完成任务',
      value: `${Number(overview.completedTaskCount || 0)} 个`,
      tip: `总任务 ${Number(overview.totalTaskCount || 0)} 个`,
      iconClass: 'icon-completed',
      theme: 'is-purple',
    },
    {
      label: '平均成绩',
      value: `${Number(overview.averageScore || 0).toFixed(1)} 分`,
      tip: `已完成考试 ${Number(overview.completedExamCount || 0)} 场`,
      iconClass: 'icon-medal',
      theme: 'is-orange',
    },
  ]
})

const noticeHasMore = computed(() => noticeList.value.length < noticeTotalCount.value)

const averageCourseProgress = computed(
  () => Number(analysisDashboard.value.overview?.averageCourseProgress || 0)
)

const chartTrendItems = computed(() => {
  const source = analysisDashboard.value.trendList || []
  if (!source.length) {
    return []
  }
  return source.map((item, index) => {
    return {
      label: item.label || item.date || '',
      value: Number(item.value || 0),
      index,
    }
  })
})
const trendChartOption = computed(() => ({
  animationDuration: 450,
  grid: {
    left: 12,
    right: 28,
    top: 18,
    bottom: 34,
    containLabel: true,
  },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(20, 43, 82, 0.92)',
    borderWidth: 0,
    textStyle: {
      color: '#fff',
    },
    formatter: (params) => {
      const item = params?.[0]
      if (!item) {
        return ''
      }
      return `${item.axisValue}<br/>学习时长：${Number(item.value || 0).toFixed(1)} 小时`
    },
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartTrendItems.value.map((item) => item.label),
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
    axisLabel: {
      color: '#92a0b5',
      fontSize: 12,
      hideOverlap: true,
      margin: 12,
    },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
    axisLabel: {
      color: '#92a0b5',
      fontSize: 12,
      formatter: '{value}h',
    },
    splitLine: {
      lineStyle: {
        color: '#eef3fb',
      },
    },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      data: chartTrendItems.value.map((item) => item.value),
      lineStyle: {
        color: '#2d79ff',
        width: 4,
      },
      itemStyle: {
        color: '#2d79ff',
      },
      areaStyle: {
        color: 'rgba(45, 121, 255, 0.08)',
      },
    },
  ],
}))

const tasks = computed(() => {
  const source = [
    ...(planDashboard.value.todayPlanList || []),
    ...(planDashboard.value.calendarPlanList || []),
  ]
  const uniqueMap = new Map()
  source
    .sort((left, right) =>
      `${left.studyDate || ''} ${left.startTimeText || ''}`.localeCompare(
        `${right.studyDate || ''} ${right.startTimeText || ''}`
      )
    )
    .forEach((item) => {
      if (!item?.itemId || uniqueMap.has(item.itemId) || item.completed) {
        return
      }
      uniqueMap.set(item.itemId, item)
    })
  return Array.from(uniqueMap.values())
    .slice(0, 3)
    .map((item, index) => ({
      key: item.itemId,
      title: item.courseName || '未命名课程',
      desc: item.lessonName || item.chapterName || '未命名学习任务',
      deadline: formatTaskDeadline(item.studyDate),
      icon: (item.courseName || '学').slice(0, 1),
      coverUrl: buildCoverUrl(item.coverPath),
      theme: ['is-blue', 'is-green', 'is-orange'][index % 3],
    }))
})

const currentMonthDate = computed(() => {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), 1)
})
const calendarTitle = computed(
  () =>
    `${currentMonthDate.value.getFullYear()}年${
      currentMonthDate.value.getMonth() + 1
    }月`
)

const calendarDays = computed(() => {
  const monthStart = currentMonthDate.value
  const firstWeekday = monthStart.getDay() || 7
  const leadingCount = firstWeekday - 1
  const firstCellDate = new Date(monthStart)
  firstCellDate.setDate(monthStart.getDate() - leadingCount)
  const planDateSet = new Set(
    (planDashboard.value.calendarPlanList || []).map((item) => item.studyDate)
  )
  const todayKey = formatDateKey(new Date())
  const days = []
  for (let index = 0; index < 35; index += 1) {
    const date = new Date(firstCellDate)
    date.setDate(firstCellDate.getDate() + index)
    const dateKey = formatDateKey(date)
    days.push({
      text: date.getDate(),
      muted: date.getMonth() !== monthStart.getMonth(),
      active: dateKey === todayKey,
      hasDot: planDateSet.has(dateKey),
      offset: dateKey,
    })
  }
  return days
})

const buildCoverUrl = (path) => buildResourceFileUrl(path)

const resolveCourseTheme = (index) =>
  courseThemeList[index % courseThemeList.length]

const buildGrowthTip = (growthRate, prefix) => {
  const value = Number(growthRate || 0)
  if (!value) {
    return `${prefix} 持平`
  }
  return `${prefix} ${value > 0 ? '↑' : '↓'} ${Math.abs(value)}%`
}

const formatDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatTaskDeadline = (studyDate) => {
  if (!studyDate) {
    return '待安排'
  }
  const today = formatDateKey(new Date())
  const tomorrowDate = new Date()
  tomorrowDate.setDate(tomorrowDate.getDate() + 1)
  const tomorrow = formatDateKey(tomorrowDate)
  if (studyDate === today) {
    return '今天'
  }
  if (studyDate === tomorrow) {
    return '明天'
  }
  return studyDate.slice(5)
}

const getDefaultAnalysisRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  return {
    startDate: formatDateKey(start),
    endDate: formatDateKey(end),
  }
}

const openCourse = (course) => {
  if (!course?.courseId) {
    return
  }
  router.push(`/courses/${course.courseId}/study`)
}

const openNotice = async (notice) => {
  if (!notice?.noticeId) {
    return
  }
  const detail = await getNoticeDetail(notice.noticeId)
  if (!detail) {
    return
  }
  currentNotice.value = detail
  noticeDialogVisible.value = true
}

const applyNoticeResult = (result, pageNo) => {
  const list = Array.isArray(result) ? result : Array.isArray(result?.list) ? result.list : []
  noticeTotalCount.value = Array.isArray(result) ? list.length : Number(result?.totalCount || list.length)
  noticePageNo.value = Array.isArray(result) ? pageNo : Number(result?.pageNo || pageNo)
  noticeList.value = pageNo === 1 ? list : [...noticeList.value, ...list]
}

const loadNotices = async (pageNo = 1) => {
  noticeLoading.value = true
  try {
    const result = await loadLatestNotices({
      pageNo,
      pageSize: noticePageSize,
    })
    applyNoticeResult(result, pageNo)
  } finally {
    noticeLoading.value = false
  }
}

const loadMoreNotices = () => {
  if (noticeLoading.value || !noticeHasMore.value) {
    return
  }
  void loadNotices(noticePageNo.value + 1)
}

const loadHomeData = async () => {
  loading.value = true
  try {
    const [examResult, planResult, analysisResult, noticeResult] = await Promise.all([
      loadMyExamList(),
      loadStudyPlanDashboard(),
      loadLearningAnalysisDashboard(getDefaultAnalysisRange()),
      loadLatestNotices({
        pageNo: 1,
        pageSize: noticePageSize,
      }),
    ])
    examList.value = Array.isArray(examResult) ? examResult : []
    planDashboard.value = planResult || planDashboard.value
    analysisDashboard.value = analysisResult || analysisDashboard.value
    applyNoticeResult(noticeResult, 1)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHomeData()
})
</script>

<style>
.student-dashboard {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 314px;
  gap: 18px;
  width: 100%;
  min-height: 100%;
}

.card {
  border: 1px solid #e7eef9;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 40px rgba(49, 87, 148, 0.07);
}

.student-dashboard__main,
.student-dashboard__side {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.student-dashboard__shortcut-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 18px 20px;
}

.student-dashboard__shortcut {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.student-dashboard__shortcut strong {
  display: block;
  margin-bottom: 6px;
  color: #173458;
  font-size: 15px;
}

.student-dashboard__shortcut p {
  margin: 0;
  color: #71839d;
  font-size: 13px;
}

.student-dashboard__shortcut-icon,
.student-dashboard__task-icon,
.student-dashboard__summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
}

.student-dashboard__shortcut-icon .iconfont,
.student-dashboard__summary-icon .iconfont,
.student-dashboard__task-icon .iconfont {
  font-size: 20px;
}

.is-blue {
  background: #e9f2ff;
  color: #2d79ff;
}

.is-green {
  background: #e7fbef;
  color: #22a55b;
}

.is-orange {
  background: #fff3e6;
  color: #ff9928;
}

.is-purple {
  background: #f1ebff;
  color: #875cff;
}

.student-dashboard__courses,
.student-dashboard__summary,
.student-dashboard__side-card {
  padding: 22px;
}

.student-dashboard__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.student-dashboard__section-header h3 {
  margin: 0;
  color: #173458;
  font-size: 18px;
}

.student-dashboard__section-tabs {
  display: flex;
  align-items: center;
  gap: 18px;
}

.student-dashboard__tab,
.student-dashboard__link-button,
.student-dashboard__range-button,
.student-dashboard__more-link {
  border: 0;
  background: transparent;
  cursor: pointer;
}

.student-dashboard__tab {
  position: relative;
  padding: 0 0 8px;
  color: #7a8ca5;
  font-size: 14px;
}

.student-dashboard__tab.is-active {
  color: #2d79ff;
  font-weight: 700;
}

.student-dashboard__tab.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  border-radius: 6px;
  background: #2d79ff;
}

.student-dashboard__link-button,
.student-dashboard__range-button,
.student-dashboard__more-link {
  color: #7c8ea7;
  font-size: 13px;
}

.student-dashboard__course-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.student-dashboard__course-card {
  min-width: 0;
  cursor: pointer;
}

.student-dashboard__course-cover {
  position: relative;
  height: 150px;
  margin-bottom: 14px;
  border-radius: 6px;
  overflow: hidden;
}

.student-dashboard__course-cover img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-dashboard__course-cover.is-cosmos {
  background: radial-gradient(
      circle at center,
      rgba(113, 177, 255, 0.22),
      transparent 22%
    ),
    linear-gradient(135deg, #071d56 0%, #0d3f91 100%);
}

.student-dashboard__course-cover.is-ice {
  background: radial-gradient(
      circle at 42% 40%,
      rgba(255, 255, 255, 0.85),
      transparent 28%
    ),
    linear-gradient(135deg, #cde0ff 0%, #eaf2ff 100%);
}

.student-dashboard__course-cover.is-violet {
  background: radial-gradient(
      circle at 50% 40%,
      rgba(173, 138, 255, 0.2),
      transparent 24%
    ),
    linear-gradient(135deg, #2e255f 0%, #5f37b8 100%);
}

.student-dashboard__course-cover.is-campus {
  background: linear-gradient(
      180deg,
      rgba(165, 219, 255, 0.5),
      rgba(255, 255, 255, 0)
    ),
    linear-gradient(135deg, #d5f0d8 0%, #f9fff6 100%);
}

.student-dashboard__course-card strong {
  display: block;
  margin-bottom: 6px;
  color: #173458;
  font-size: 15px;
}

.student-dashboard__course-card p {
  margin: 0 0 16px;
  color: #71839d;
  font-size: 13px;
}

.student-dashboard__progress {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #7a8ba5;
  font-size: 12px;
}

.student-dashboard__progress-bar {
  height: 6px;
  border-radius: 6px;
  background: #edf3fb;
  overflow: hidden;
}

.student-dashboard__progress-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
}

.student-dashboard__summary-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.student-dashboard__summary-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 6px;
  background: #fbfdff;
}

.student-dashboard__summary-stat p {
  margin: 0 0 6px;
  color: #7b8da6;
  font-size: 13px;
}

.student-dashboard__summary-stat strong {
  display: block;
  margin-bottom: 6px;
  color: #173458;
  font-size: 16px;
}

.student-dashboard__summary-stat small {
  color: #7488a5;
  font-size: 12px;
}

.student-dashboard__chart {
  height: 220px;
}

.student-dashboard__task-list,
.student-dashboard__notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-dashboard__task-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.student-dashboard__task-cover {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.student-dashboard__task-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-dashboard__task-cover-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.student-dashboard__task-body {
  min-width: 0;
}

.student-dashboard__task-body strong,
.student-dashboard__notice-body strong {
  display: block;
  margin-bottom: 6px;
  color: #173458;
  font-size: 15px;
}

.student-dashboard__task-body p,
.student-dashboard__notice-body p {
  margin: 0;
  color: #71839d;
  font-size: 13px;
}

.student-dashboard__task-deadline {
  color: #8697ad;
  font-size: 12px;
  white-space: nowrap;
}

.student-dashboard__calendar-title {
  color: #173458;
  font-size: 14px;
  font-weight: 700;
}

.student-dashboard__calendar-week,
.student-dashboard__calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.student-dashboard__calendar-week {
  margin-bottom: 12px;
  color: #9ba8bb;
  font-size: 12px;
  text-align: center;
}

.student-dashboard__calendar-grid {
  gap: 10px 0;
}

.student-dashboard__calendar-day {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin: 0 auto;
  border-radius: 6px;
  color: #1d355c;
  font-size: 14px;
}

.student-dashboard__calendar-day.is-muted {
  color: #c1cbda;
}

.student-dashboard__calendar-day.is-active {
  background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
  color: #fff;
  box-shadow: 0 10px 18px rgba(59, 134, 255, 0.26);
}

.student-dashboard__calendar-day.is-dot:not(.is-active) {
  position: relative;
}

.student-dashboard__calendar-day.is-dot:not(.is-active)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #2d79ff;
}

.student-dashboard__notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
}

.student-dashboard__notice-item:hover .student-dashboard__notice-body strong {
  color: #2d79ff;
}

.student-dashboard__notice-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.student-dashboard__notice-tag.is-red {
  background: #ffe8e9;
  color: #ff5960;
}

.student-dashboard__notice-tag.is-orange {
  background: #fff0db;
  color: #ff9a24;
}

.student-dashboard__notice-tag.is-green {
  background: #e8fbef;
  color: #24a55c;
}

.student-dashboard__notice-tag.is-blue {
  background: #e9f2ff;
  color: #2d79ff;
}

.student-dashboard__notice-body {
  min-width: 0;
}

.student-dashboard__notice-more {
  width: 100%;
  height: 34px;
  border: 0;
  border-radius: 6px;
  background: #f3f7ff;
  color: #2d79ff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.student-dashboard__notice-more:disabled {
  color: #9aacbf;
  cursor: not-allowed;
}

.student-dashboard__notice-end {
  color: #9aacbf;
  font-size: 13px;
  text-align: center;
}

.student-dashboard__more-link {
  margin-top: 18px;
  padding: 0;
  color: #2d79ff;
  font-size: 14px;
  font-weight: 600;
  text-align: left;
}

.student-dashboard__empty {
  color: #71839d;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .student-dashboard {
    grid-template-columns: 1fr;
  }

  .student-dashboard__shortcut-row,
  .student-dashboard__course-grid,
  .student-dashboard__summary-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 780px) {
  .student-dashboard__shortcut-row,
  .student-dashboard__course-grid,
  .student-dashboard__summary-stats {
    grid-template-columns: 1fr;
  }

  .student-dashboard__section-header,
  .student-dashboard__section-tabs {
    align-items: flex-start;
    flex-direction: column;
  }

  .student-dashboard__task-item {
    grid-template-columns: 44px minmax(0, 1fr);
  }

  .student-dashboard__task-deadline {
    grid-column: 2;
  }
}
</style>
