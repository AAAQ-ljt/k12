<template>
  <div class="exam-page">
    <section class="exam-page__hero">
      <div>
        <h1>在线考试</h1>
      </div>

      <div class="exam-page__hero-tools">
        <el-select v-model="courseFilter" class="exam-page__filter" popper-class="exam-page__filter-popper">
          <el-option label="全部课程" value="all" />
          <el-option v-for="item in courseOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </section>

    <section class="exam-page__tabs card-shell">
      <el-tabs v-model="activeTab" class="exam-page__el-tabs">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :label="tab.label" :name="tab.key" />
      </el-tabs>
    </section>

    <section class="exam-page__stats">
      <article v-for="item in statsCards" :key="item.label" class="exam-page__stats-card">
        <div class="exam-page__stats-icon" :class="item.theme">
          <i class="iconfont" :class="item.iconClass" />
        </div>
        <div class="exam-page__stats-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="exam-page__content">
      <div class="exam-page__main">
        <div class="card-shell exam-list-panel">
          <div class="exam-list-panel__head">
            <h2>考试列表</h2>
          </div>

          <div v-if="loading" class="exam-page__state">正在加载考试列表...</div>
          <div v-else-if="!filteredExamList.length" class="exam-page__state is-empty">
            <strong>{{ examList.length ? '没有匹配的考试' : '暂无在线考试' }}</strong>
            <p>{{ examList.length ? '试试切换筛选条件。' : '当前还没有可参加的考试。' }}</p>
          </div>

          <div v-else class="exam-list-panel__body">
            <article v-for="item in displayedExamList" :key="item.examId" class="exam-row">
              <span class="exam-row__status" :class="`is-${item.statusTheme}`">
                {{ item.statusText }}
              </span>

              <div class="exam-row__icon">
                <i class="iconfont icon-menu-exam" />
              </div>

              <div class="exam-row__content">
                <strong>{{ item.examName }}</strong>
                <p>{{ item.courseName || '-' }}</p>
                <span>{{ formatExamTimeRange(item.startTime, item.endTime) }}</span>
              </div>

              <div class="exam-row__meta">
                <template v-if="item.displayMode === 'upcoming'">
                  <span>距离开始 {{ item.countdownText }}</span>
                </template>
                <template v-else-if="item.displayMode === 'running'">
                  <span>
                    剩余时间
                    <em>{{ item.countdownText }}</em>
                  </span>
                </template>
                <template v-else-if="item.displayMode === 'submitted'">
                  <span>
                    得分：
                    <em>{{ formatScore(item.finalScore) }}分</em>
                  </span>
                </template>
                <template v-else>
                  <span>已结束</span>
                </template>
              </div>

              <button type="button" class="exam-row__action" :class="{ 'is-plain': item.actionPlain }"
                @click="handleOpen(item)">
                {{ item.actionText }}
              </button>
            </article>
          </div>

          <div v-if="filteredExamList.length > previewLimit" class="exam-list-panel__footer">
            <span class="exam-list-panel__footer-text">
              当前共 {{ filteredExamList.length }} 场考试
            </span>
            <button type="button" class="study-link" @click="toggleExamList">
              {{ expandedExamList ? '收起考试列表' : '查看全部考试' }}
            </button>
          </div>
        </div>
      </div>

      <aside class="exam-page__aside">
        <div class="card-shell exam-side-card">
          <div class="exam-side-card__head">
            <h3>近期考试日历</h3>
          </div>

          <div class="exam-calendar">
            <div class="exam-calendar__week">
              <span v-for="label in weekdayLabels" :key="label">{{ label }}</span>
            </div>
            <div class="exam-calendar__days">
              <button v-for="item in calendarDays" :key="item.date" type="button" class="exam-calendar__day"
                :class="{ 'is-active': item.date === activeCalendarDate }" @click="activeCalendarDate = item.date">
                <span>{{ item.day }}</span>
                <em v-if="item.hasExam" />
              </button>
            </div>
          </div>

          <div class="exam-side-card__list">
            <article v-for="item in activeCalendarExams" :key="item.examId" class="exam-side-card__list-item">
              <span class="exam-side-card__dot" />
              <strong>{{ item.examName }}</strong>
              <span>{{ monthDayTime(item.startTime) }}</span>
            </article>
            <div v-if="!activeCalendarExams.length" class="exam-side-card__empty">
              当天暂无考试安排
            </div>
          </div>
        </div>

        <div class="card-shell exam-side-card">
          <div class="exam-side-card__head">
            <h3>考试小贴士</h3>
          </div>

          <div class="exam-tip-list">
            <article v-for="item in tips" :key="item.title" class="exam-tip-item">
              <div class="exam-tip-item__icon">
                <i class="iconfont" :class="item.icon" />
              </div>
              <div class="exam-tip-item__content">
                <strong>{{ item.title }}</strong>
                <p>{{ item.desc }}</p>
              </div>
            </article>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loadMyExamList, normalizeExamItem } from '@/api/exam'

const router = useRouter()
const loading = ref(false)
const examList = ref([])
const activeTab = ref('all')
const courseFilter = ref('all')
const activeCalendarDate = ref('')
const expandedExamList = ref(false)
const previewLimit = 4
const nowTick = ref(Date.now())
let timer = null

const weekdayLabels = ['日', '一', '二', '三', '四', '五', '六']

const tips = [
  {
    title: '提前准备',
    desc: '提前进入考场，检查设备和网络',
    icon: 'icon-menu-plan',
  },
  {
    title: '诚信考试',
    desc: '遵守考试纪律，诚信应考',
    icon: 'icon-completed',
  },
  {
    title: '时间管理',
    desc: '合理分配时间，先易后难',
    icon: 'icon-time',
  },
  {
    title: '检查提交',
    desc: '完成后仔细检查，确认提交',
    icon: 'icon-menu-exam',
  },
]

const formatScore = (value) => Number(value ?? 0).toFixed(0)

const toDate = (value) => {
  if (!value) {
    return null
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const pad = (value) => String(value).padStart(2, '0')

const dateKey = (value) => {
  const date = toDate(value)
  if (!date) {
    return ''
  }
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}`
}

const startOfDay = (value) => {
  const date = toDate(value) || new Date(value)
  if (!date || Number.isNaN(date.getTime())) {
    return null
  }
  date.setHours(0, 0, 0, 0)
  return date
}

const timeOnly = (value) => {
  if (!value) {
    return '--:--'
  }
  return String(value).slice(11, 16)
}

const monthDayTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).slice(5, 16).replace('T', ' ')
}

const formatExamTimeRange = (startTime, endTime) => {
  const start = toDate(startTime)
  const end = toDate(endTime)
  if (!start || !end) {
    return '-'
  }
  const dateText = `${start.getFullYear()}-${pad(start.getMonth() + 1)}-${pad(
    start.getDate()
  )}`
  const startText = `${pad(start.getHours())}:${pad(start.getMinutes())}`
  const endText = `${pad(end.getHours())}:${pad(end.getMinutes())}`
  return `${dateText} ${startText} ~ ${endText}`
}

const countdownText = (targetTime, mode) => {
  const target = toDate(targetTime)
  if (!target) {
    return '-'
  }
  const diff = target.getTime() - nowTick.value
  if (diff <= 0) {
    return mode === 'running' ? '00:00:00' : '0天'
  }
  if (mode === 'running') {
    const totalSeconds = Math.floor(diff / 1000)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
  }
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.max(1, Math.ceil(diff / (60 * 1000)))
    return `${minutes}分钟`
  }
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.max(1, Math.ceil(diff / (60 * 60 * 1000)))
    return `${hours}小时`
  }
  const days = Math.ceil(diff / (24 * 60 * 60 * 1000))
  return `${days}天`
}

const normalizeStatus = (item) => {
  const start = toDate(item.startTime)
  const end = toDate(item.endTime)
  const now = nowTick.value

  if (item.submitted) {
    return {
      key: 'done',
      text: '已完成',
      theme: 'done',
      displayMode: 'submitted',
      actionText: '查看结果',
      actionPlain: true,
      countdownText: '',
    }
  }

  if (start && end && start.getTime() <= now && end.getTime() >= now) {
    return {
      key: 'running',
      text: '进行中',
      theme: 'running',
      displayMode: 'running',
      actionText: '进入考试',
      actionPlain: false,
      countdownText: countdownText(item.endTime, 'running'),
    }
  }

  if (start && start.getTime() > now) {
    return {
      key: 'upcoming',
      text: '未开始',
      theme: 'upcoming',
      displayMode: 'upcoming',
      actionText: '查看详情',
      actionPlain: true,
      countdownText: countdownText(item.startTime, 'upcoming'),
    }
  }

  if (end && end.getTime() < now) {
    return {
      key: 'done',
      text: '已过期',
      theme: 'expired',
      displayMode: 'submitted',
      actionText: '查看结果',
      actionPlain: true,
      countdownText: '',
    }
  }

  return {
    key: 'upcoming',
    text: item.submitStatusText || '未开始',
    theme: 'upcoming',
    displayMode: 'upcoming',
    actionText: '查看详情',
    actionPlain: true,
    countdownText: '',
  }
}

const enrichedExamList = computed(() =>
  examList.value.map((item) => {
    const status = normalizeStatus(item)
    return {
      ...item,
      statusKey: status.key,
      statusText: status.text,
      statusTheme: status.theme,
      displayMode: status.displayMode,
      actionText: status.actionText,
      actionPlain: status.actionPlain,
      countdownText: status.countdownText,
    }
  })
)

const courseOptions = computed(() => {
  const map = new Map()
  enrichedExamList.value.forEach((item) => {
    if (item.courseId && item.courseName && !map.has(item.courseId)) {
      map.set(item.courseId, item.courseName)
    }
  })
  return [...map.entries()].map(([value, label]) => ({ value, label }))
})

const tabs = computed(() => {
  const count = (key) =>
    key === 'all'
      ? enrichedExamList.value.length
      : enrichedExamList.value.filter((item) => item.statusKey === key).length
  return [
    { key: 'all', label: `全部考试 ${count('all')}` },
    { key: 'upcoming', label: `未开始 ${count('upcoming')}` },
    { key: 'running', label: `进行中 ${count('running')}` },
    { key: 'done', label: `已完成 ${count('done')}` },
  ]
})

const filteredExamList = computed(() => {
  let list = [...enrichedExamList.value]
  if (activeTab.value !== 'all') {
    list = list.filter((item) => item.statusKey === activeTab.value)
  }
  if (courseFilter.value !== 'all') {
    list = list.filter((item) => item.courseId === courseFilter.value)
  }
  return list.sort((a, b) =>
    String(b.startTime || '').localeCompare(String(a.startTime || ''))
  )
})

const displayedExamList = computed(() =>
  expandedExamList.value
    ? filteredExamList.value
    : filteredExamList.value.slice(0, previewLimit)
)

const statsCards = computed(() => {
  const total = enrichedExamList.value.length
  const upcoming = enrichedExamList.value.filter(
    (item) => item.statusKey === 'upcoming'
  ).length
  const running = enrichedExamList.value.filter(
    (item) => item.statusKey === 'running'
  ).length
  const done = enrichedExamList.value.filter(
    (item) => item.statusKey === 'done'
  ).length
  return [
    {
      label: '全部考试',
      value: `${total} 场`,
      iconClass: 'icon-menu-exam',
      theme: 'is-blue',
    },
    {
      label: '未开始',
      value: `${upcoming} 场`,
      iconClass: 'icon-collection',
      theme: 'is-green',
    },
    {
      label: '进行中',
      value: `${running} 场`,
      iconClass: 'icon-time',
      theme: 'is-purple',
    },
    {
      label: '已完成',
      value: `${done} 场`,
      iconClass: 'icon-completed',
      theme: 'is-orange',
    },
  ]
})

const recentCalendarExamList = computed(() =>
  [...enrichedExamList.value]
    .filter((item) => {
      const end = toDate(item.endTime)
      const start = toDate(item.startTime)
      if (end) {
        return end.getTime() >= nowTick.value
      }
      return start && start.getTime() >= nowTick.value
    })
    .sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
)

const calendarDays = computed(() => {
  const today = startOfDay(nowTick.value) || new Date()
  const nearestExamDate = startOfDay(recentCalendarExamList.value[0]?.startTime)
  let base = new Date(today)
  if (nearestExamDate) {
    const diffDays = Math.floor(
      (nearestExamDate.getTime() - today.getTime()) / (24 * 60 * 60 * 1000)
    )
    if (diffDays > 6) {
      base = nearestExamDate
    }
  }
  return Array.from({ length: 7 }).map((_, index) => {
    const date = new Date(base)
    date.setDate(base.getDate() + index)
    const key = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
      date.getDate()
    )}`
    return {
      date: key,
      day: date.getDate(),
      hasExam: enrichedExamList.value.some(
        (item) => dateKey(item.startTime) === key
      ),
    }
  })
})

const activeCalendarExams = computed(() =>
  enrichedExamList.value
    .filter((item) => dateKey(item.startTime) === activeCalendarDate.value)
    .sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)))
    .slice(0, 3)
)

const resolveDefaultCalendarDate = () => {
  const firstExamDate = calendarDays.value.find((item) => item.hasExam)?.date
  if (firstExamDate) {
    return firstExamDate
  }
  return calendarDays.value[0]?.date || ''
}

const handleOpen = (item) => {
  router.push(`/exams/${item.examId}`)
}

const toggleExamList = () => {
  expandedExamList.value = !expandedExamList.value
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await loadMyExamList()
    examList.value = Array.isArray(result)
      ? result.map((item) => normalizeExamItem(item))
      : []
    activeCalendarDate.value = resolveDefaultCalendarDate()
    expandedExamList.value = false
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  timer = window.setInterval(() => {
    nowTick.value = Date.now()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.exam-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.card-shell {
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 30px rgba(49, 87, 148, 0.06);
}

.exam-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.exam-page__hero h1 {
  margin: 0;
  color: #182f56;
  font-size: 28px;
}

.exam-page__filter {
  width: 140px;
}

.exam-page__tabs {
  padding: 0 18px;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__header) {
  margin: 0;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__nav-wrap::after) {
  background-color: #edf2fb;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__item) {
  height: 54px;
  color: #7b8ba4;
  font-size: 14px;
  font-weight: 600;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__item.is-active) {
  color: #2a66f6;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__active-bar) {
  height: 3px;
  border-radius: 6px;
  background: #2a66f6;
}

.exam-page :deep(.exam-page__el-tabs .el-tabs__content) {
  display: none;
}

.exam-page__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.exam-page__stats-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e6edf8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 14px 30px rgba(49, 87, 148, 0.06);
}

.exam-page__stats-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 6px;
}

.exam-page__stats-icon .iconfont {
  font-size: 24px;
}

.exam-page__stats-icon.is-blue {
  background: #eaf2ff;
  color: #2d73f5;
}

.exam-page__stats-icon.is-green {
  background: #e9fbef;
  color: #28b267;
}

.exam-page__stats-icon.is-purple {
  background: #f1ecff;
  color: #875dff;
}

.exam-page__stats-icon.is-orange {
  background: #fff2e4;
  color: #ff9a33;
}

.exam-page__stats-content span {
  display: block;
  margin-bottom: 8px;
  color: #7385a1;
  font-size: 14px;
}

.exam-page__stats-content strong {
  display: block;
  margin-bottom: 8px;
  color: #162f58;
  font-size: 24px;
  line-height: 1.1;
}

.exam-page__stats-content small {
  color: #1ea45f;
  font-size: 14px;
}

.exam-page__content {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) 320px;
  gap: 16px;
  align-items: start;
}

.exam-list-panel {
  overflow: hidden;
}

.exam-list-panel__head {
  padding: 18px 20px;
  border-bottom: 1px solid #edf2fb;
}

.exam-list-panel__head h2 {
  margin: 0;
  color: #182f56;
  font-size: 18px;
}

.exam-list-panel__body {
  display: grid;
}

.exam-row {
  display: grid;
  grid-template-columns: 72px 82px minmax(0, 1fr) 180px 120px;
  align-items: center;
  gap: 18px;
  padding: 22px 20px;
  border-bottom: 1px solid #edf2fb;
}

.exam-row__status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 8px;
  font-size: 14px;
}

.exam-row__status.is-upcoming {
  background: #fff3e7;
  color: #ff932f;
}

.exam-row__status.is-running {
  background: #ebfbf1;
  color: #22a35f;
}

.exam-row__status.is-done {
  background: #edf4ff;
  color: #2d73f5;
}

.exam-row__status.is-expired {
  background: #f2f4f8;
  color: #7d8ea8;
}

.exam-row__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 82px;
  height: 72px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f1f6ff 0%, #fbfdff 100%);
  color: #2d73f5;
  box-shadow: inset 0 0 0 1px #e8eef9;
}

.exam-row__icon .iconfont {
  font-size: 32px;
}

.exam-row__content {
  min-width: 0;
}

.exam-row__content strong {
  display: block;
  margin-bottom: 8px;
  color: #162f58;
  font-size: 18px;
  line-height: 1.35;
}

.exam-row__content p,
.exam-row__content span {
  display: block;
  margin: 0;
  color: #657c9b;
  font-size: 14px;
}

.exam-row__content p {
  margin-bottom: 8px;
}

.exam-row__meta {
  color: #556984;
  font-size: 15px;
  text-align: right;
}

.exam-row__meta em {
  color: #2d73f5;
  font-style: normal;
  font-weight: 700;
}

.exam-row__action {
  width: 110px;
  height: 40px;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #2f6bf2 0%, #5a98ff 100%);
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.exam-row__action.is-plain {
  border: 1px solid #bdd2fb;
  background: #fff;
  color: #2d73f5;
}

.exam-list-panel__footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 18px 20px;
}

.exam-list-panel__footer-text {
  color: #6f829d;
  font-size: 14px;
}

.study-link {
  border: 0;
  background: transparent;
  color: #2d73f5;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
}

.exam-side-card {
  padding: 16px 18px 18px;
}

.exam-side-card+.exam-side-card {
  margin-top: 16px;
}

.exam-side-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.exam-side-card__head h3 {
  margin: 0;
  color: #182f56;
  font-size: 16px;
}

.exam-calendar__week,
.exam-calendar__days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 4px;
}

.exam-calendar__week {
  margin-bottom: 6px;
  color: #8396b0;
  font-size: 13px;
  text-align: center;
}

.exam-calendar__day {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 35px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #172f57;
  cursor: pointer;
  font-size: 16px;
}

.exam-calendar__day.is-active {
  background: #2d73f5;
  color: #fff;
}

.exam-calendar__day em {
  position: absolute;
  bottom: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2d73f5;
}

.exam-calendar__day.is-active em {
  background: #fff;
}

.exam-side-card__list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.exam-side-card__list-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) 84px;
  gap: 10px;
  align-items: center;
  color: #5c7290;
  font-size: 14px;
}

.exam-side-card__list-item strong {
  color: #172f57;
  font-size: 15px;
  font-weight: 500;
}

.exam-side-card__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2d73f5;
}

.exam-side-card__empty {
  color: #8294ad;
  font-size: 13px;
}

.exam-tip-list {
  display: grid;
  gap: 16px;
}

.exam-tip-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
}

.exam-tip-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #edf4ff;
  color: #2d73f5;
}

.exam-tip-item__icon .iconfont {
  font-size: 18px;
}

.exam-tip-item__content strong {
  display: block;
  margin-bottom: 6px;
  color: #172f57;
  font-size: 15px;
}

.exam-tip-item__content p {
  margin: 0;
  color: #6f829d;
  font-size: 13px;
  line-height: 1.7;
}

.exam-page__state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  color: #6f839d;
  font-size: 14px;
}

.exam-page__state.is-empty {
  flex-direction: column;
  gap: 10px;
}

.exam-page__state.is-empty strong {
  color: #183055;
  font-size: 18px;
}

.exam-page__state.is-empty p {
  margin: 0;
}

@media (max-width: 1280px) {

  .exam-page__stats,
  .exam-page__content {
    grid-template-columns: 1fr 1fr;
  }

  .exam-page__content {
    align-items: stretch;
  }

  .exam-row {
    grid-template-columns: 62px 82px minmax(0, 1fr);
  }

  .exam-row__meta,
  .exam-row__action {
    justify-self: start;
  }
}

@media (max-width: 960px) {

  .exam-page__hero,
  .exam-page__stats,
  .exam-page__content {
    grid-template-columns: 1fr;
    display: grid;
  }

  .exam-page__hero {
    gap: 12px;
  }

  .exam-page__hero-tools {
    width: 100%;
  }

  .exam-page__filter {
    width: 100%;
  }

  .exam-row {
    grid-template-columns: 1fr;
  }

  .exam-row__meta,
  .exam-row__action {
    justify-self: start;
    text-align: left;
  }

  .exam-list-panel__footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
