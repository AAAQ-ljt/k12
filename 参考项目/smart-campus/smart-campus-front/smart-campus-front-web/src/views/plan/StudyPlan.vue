<template>
  <div class="study-plan-page">
    <section class="study-plan-page__banner">
      <div class="study-plan-page__banner-content">
        <h1>学习计划</h1>
        <p>制定学习计划，合理安排时间，提高学习效率</p>
      </div>
    </section>

    <section class="study-plan-card study-plan-page__overview">
      <div class="study-plan-card__title">计划概览</div>
      <div v-if="loading" class="study-plan-page__loading">正在加载学习计划...</div>
      <div v-else class="overview-grid">
        <article v-for="item in overviewCards" :key="item.label" class="overview-card">
          <div class="overview-card__icon" :class="item.theme">
            <i class="iconfont" :class="item.icon" />
          </div>
          <div class="overview-card__content">
            <span>{{ item.label }}</span>
            <div class="overview-card__value">
              <strong>{{ item.value }}</strong>
              <em v-if="item.unit">{{ item.unit }}</em>
            </div>
            <small>{{ item.tip }}</small>
          </div>
        </article>
      </div>
    </section>

    <section class="study-plan-page__middle">
      <div class="study-plan-card">
        <div class="study-plan-card__head">
          <div class="study-plan-card__title">今日计划</div>
        </div>

        <div v-if="!todayPlans.length" class="study-plan-page__empty-small">
          今天还没有安排学习计划
        </div>

        <div v-else class="today-plan-list">
          <article v-for="item in todayPlans" :key="item.itemId" class="today-plan-item"
            :class="{ 'is-completed': item.completed }">
            <div class="today-plan-item__check">
              <el-checkbox :model-value="item.completed" @change="toggleTodayPlan(item)" />
            </div>

            <div class="today-plan-item__cover">
              <img v-if="buildCoverUrl(item.coverPath)" :src="buildCoverUrl(item.coverPath)" :alt="item.courseName">
              <span v-else>{{ item.courseName?.slice(0, 2) || '课程' }}</span>
            </div>

            <div class="today-plan-item__content">
              <strong>{{ item.courseName }}</strong>
              <p>{{ item.lessonName || item.chapterName || '未命名课时' }}</p>
            </div>

            <span class="today-plan-item__time">{{ item.timeRangeText }}</span>
          </article>
        </div>
      </div>

      <div class="study-plan-card">
        <div class="study-plan-card__head">
          <div class="study-plan-card__title">学习日程</div>
          <div class="schedule-head">
            <span class="schedule-head__range">本周</span>
            <div class="schedule-head__actions">
              <button type="button" class="schedule-head__nav" @click="switchWeek(-1)">
                <i class="iconfont icon-arrow-left" />
              </button>
              <button type="button" class="schedule-head__nav" @click="switchWeek(1)">
                <i class="iconfont icon-arrow-right" />
              </button>
            </div>
          </div>
        </div>

        <div class="schedule-week">
          <button v-for="day in weekDays" :key="day.date" type="button" class="schedule-week__day"
            :class="{ 'is-active': day.date === activeDate }" @click="activeDate = day.date">
            <span class="schedule-week__weekday">{{ day.weekday }}</span>
            <span class="schedule-week__date">{{ day.day }}</span>
            <span v-if="day.hasDot" class="schedule-week__dot" />
          </button>
        </div>

        <div v-if="!activeScheduleList.length" class="study-plan-page__empty-small">
          当天暂无学习安排
        </div>

        <div v-else class="schedule-list">
          <article v-for="item in activeScheduleList" :key="item.itemId" class="schedule-item">
            <span class="schedule-item__time">{{ item.timeRangeText }}</span>
            <div class="schedule-item__content">
              <strong>{{ item.courseName }}</strong>
              <span>{{ item.lessonName || item.chapterName || '未命名课时' }}</span>
            </div>
            <em class="schedule-item__tag" :class="`is-${item.completed ? 'green' : 'blue'}`">
              {{ item.completed ? '完成' : '学习' }}
            </em>
          </article>
        </div>
      </div>
    </section>

    <section class="study-plan-card">
      <div class="study-plan-card__head">
        <div class="study-plan-card__title">我的学习计划</div>
        <button type="button" class="study-plan-link" @click="openCreateDrawer">
          + 新建计划
        </button>
      </div>

      <div v-if="!planTableData.length" class="study-plan-page__empty">
        <el-empty description="还没有创建学习计划" />
      </div>

      <div v-else class="plan-table">
        <div class="plan-table__head">
          <span>课程</span>
          <span>计划进度</span>
          <span>任务数</span>
          <span>已完成</span>
          <span>预计完成时间</span>
          <span>操作</span>
        </div>

        <article v-for="item in planTableData" :key="item.planId" class="plan-table__row">
          <div class="plan-table__course">
            <div class="plan-table__cover">
              <img v-if="buildCoverUrl(item.coverPath)" :src="buildCoverUrl(item.coverPath)" :alt="item.courseName">
              <span v-else>{{ item.courseName?.slice(0, 2) || '课程' }}</span>
            </div>
            <div class="plan-table__course-info">
              <strong>{{ item.courseName }}</strong>
              <p>{{ item.description || '按章节拆分学习目标，逐项完成。' }}</p>
            </div>
          </div>

          <div class="plan-table__progress">
            <div class="plan-table__bar">
              <span :style="{ width: `${item.progress || 0}%` }" />
            </div>
            <em>{{ item.progress || 0 }}%</em>
          </div>

          <span class="plan-table__cell">{{ item.taskCount || 0 }}</span>
          <span class="plan-table__cell">{{ item.completedCount || 0 }}</span>
          <span class="plan-table__cell">{{ item.deadline || '-' }}</span>

          <div class="plan-table__actions">
            <button type="button" class="study-plan-link" @click="openDetailDrawer(item.planId)">
              查看计划
            </button>
            <button type="button" class="study-plan-link" @click="openEditDrawer(item.planId)">
              调整计划
            </button>
          </div>
        </article>
      </div>
    </section>

    <StudyPlanDetailDrawer v-model:show="detailDrawerVisible" :loading="detailLoading" :detail-data="detailData"
      @toggle-status="toggleDetailItemStatus" />

    <StudyPlanFormDrawer v-model:show="formDrawerVisible" :mode="formMode" :loading="chapterLoading"
      :form-data="formData" :course-options="courseOptions" :lesson-plan-groups="lessonPlanGroups"
      :planned-lesson-ids="plannedLessonIds" @close="resetForm"
      @course-change="handleCourseChange" @submit="submitPlanForm" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import Message from '@/utils/Message'
import { buildResourceFileUrl } from '@/utils/resource'
import { getMyCourseDetail, loadMyCourseList } from '@/api/course'
import StudyPlanDetailDrawer from './components/StudyPlanDetailDrawer.vue'
import StudyPlanFormDrawer from './components/StudyPlanFormDrawer.vue'
import {
  getStudyPlanDetail,
  loadStudyPlanDashboard,
  saveStudyPlan,
  updateStudyPlanItemStatus,
  getPlannedLessonIds,
} from '@/api/plan'

const loading = ref(false)
const detailLoading = ref(false)
const chapterLoading = ref(false)
const dashboard = ref({
  totalPlanCount: 0,
  totalTaskCount: 0,
  completedTaskCount: 0,
  inProgressPlanCount: 0,
  totalStudyHours: 0,
  todayPlanList: [],
  calendarPlanList: [],
  planList: [],
})
const detailDrawerVisible = ref(false)
const formDrawerVisible = ref(false)
const detailData = ref(null)
const courseOptions = ref([])
const lessonPlans = ref([])
const formMode = ref('create')
const activeDate = ref('')
const currentWeekStart = ref(startOfWeek(new Date()))

const formData = ref({
  planId: '',
  courseId: '',
  description: '',
})
const plannedLessonIds = ref([])

const overviewCards = computed(() => [
  {
    label: '计划任务',
    value: dashboard.value.totalTaskCount || 0,
    unit: '个',
    tip: `进行中 ${dashboard.value.inProgressPlanCount || 0} 个`,
    theme: 'is-blue',
    icon: 'icon-menu-plan',
  },
  {
    label: '已完成任务',
    value: dashboard.value.completedTaskCount || 0,
    unit: '个',
    tip: `已完成计划 ${planTableData.value.filter((item) => Number(item.status) === 2).length
      } 个`,
    theme: 'is-green',
    icon: 'icon-completed',
  },
  {
    label: '总学习时长',
    value: Number(dashboard.value.totalStudyHours || 0).toFixed(1),
    unit: '小时',
    tip: '按已学习课程累计统计',
    theme: 'is-orange',
    icon: 'icon-time',
  },
  {
    label: '进行中计划',
    value: dashboard.value.inProgressPlanCount || 0,
    unit: '个',
    tip: `已完成计划 ${(dashboard.value.totalPlanCount || 0) -
      (dashboard.value.inProgressPlanCount || 0)
      } 个`,
    theme: 'is-purple',
    icon: 'icon-analysis',
  },
])

const todayPlans = computed(() => dashboard.value.todayPlanList || [])
const planTableData = computed(() => dashboard.value.planList || [])
const calendarItems = computed(() => dashboard.value.calendarPlanList || [])
const lessonPlanGroups = computed(() => {
  const groupMap = new Map()
  lessonPlans.value.forEach((item) => {
    if (!groupMap.has(item.chapterId)) {
      groupMap.set(item.chapterId, {
        chapterId: item.chapterId,
        chapterName: item.chapterName,
        lessonList: [],
      })
    }
    groupMap.get(item.chapterId).lessonList.push(item)
  })
  return Array.from(groupMap.values())
})

const weekDays = computed(() => {
  const labels = ['一', '二', '三', '四', '五', '六', '日']
  return Array.from({ length: 7 }).map((_, index) => {
    const date = new Date(currentWeekStart.value)
    date.setDate(currentWeekStart.value.getDate() + index)
    const key = formatDateKey(date)
    return {
      date: key,
      day: date.getDate(),
      weekday: labels[index],
      hasDot: calendarItems.value.some((item) => item.studyDate === key),
    }
  })
})

const activeScheduleList = computed(() =>
  calendarItems.value.filter((item) => item.studyDate === activeDate.value)
)

const buildCoverUrl = (path) => buildResourceFileUrl(path)

function startOfWeek(date) {
  const value = new Date(date)
  const day = value.getDay()
  const offset = day === 0 ? -6 : 1 - day
  value.setHours(0, 0, 0, 0)
  value.setDate(value.getDate() + offset)
  return value
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getCurrentMinPlanTime() {
  const now = new Date()
  if (now.getSeconds() > 0 || now.getMilliseconds() > 0) {
    now.setMinutes(now.getMinutes() + 1)
  }
  now.setSeconds(0, 0)
  if (formatDateKey(now) !== formatDateKey(new Date())) {
    return '23:59'
  }
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const result = await loadStudyPlanDashboard()
    dashboard.value = result || dashboard.value
    const today = formatDateKey(new Date())
    activeDate.value =
      weekDays.value.find((item) => item.date === today)?.date ||
      weekDays.value.find((item) => item.hasDot)?.date ||
      weekDays.value[0]?.date ||
      ''
  } finally {
    loading.value = false
  }
}

const loadCourseOptions = async () => {
  const result = await loadMyCourseList()
  courseOptions.value = Array.isArray(result) ? result : []
}

const switchWeek = (step) => {
  const next = new Date(currentWeekStart.value)
  next.setDate(next.getDate() + step * 7)
  currentWeekStart.value = next
  activeDate.value =
    weekDays.value.find((item) => item.hasDot)?.date ||
    weekDays.value[0]?.date ||
    ''
}

const openDetailDrawer = async (planId) => {
  detailDrawerVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await getStudyPlanDetail(planId)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDrawer = () => {
  resetForm()
  formMode.value = 'create'
  formDrawerVisible.value = true
}

const openEditDrawer = async (planId) => {
  formMode.value = 'edit'
  formDrawerVisible.value = true
  detailLoading.value = true
  try {
    const detail = await getStudyPlanDetail(planId)
    detailData.value = detail
    formData.value = {
      planId: detail.planId,
      courseId: detail.courseId,
      description: detail.description || '',
    }
    await loadCourseStructure(detail.courseId, detail.itemList || [], detail.planId)
  } finally {
    detailLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    planId: '',
    courseId: '',
    description: '',
  }
  lessonPlans.value = []
  plannedLessonIds.value = []
  chapterLoading.value = false
  formMode.value = 'create'
}

const normalizeLessonPlans = (courseDetail, selectedItemList = []) => {
  const selectedMap = new Map(
    selectedItemList.map((item) => [item.lessonId, item])
  )
  return (courseDetail?.chapterList || []).flatMap((chapter) =>
    (chapter.lessonList || []).map((lesson) => {
      const matched = selectedMap.get(lesson.lessonId)
      const timeRange =
        matched?.startTimeText && matched?.endTimeText
          ? [matched.startTimeText, matched.endTimeText]
          : []
      return {
        chapterId: chapter.chapterId,
        chapterName: chapter.chapterName,
        lessonId: lesson.lessonId,
        lessonName: lesson.lessonName,
        enabled: Boolean(matched),
        studyDate: matched?.studyDate || '',
        timeRange,
      }
    })
  )
}

const loadCourseStructure = async (courseId, selectedItemList = [], excludePlanId = '') => {
  if (!courseId) {
    lessonPlans.value = []
    plannedLessonIds.value = []
    return
  }
  chapterLoading.value = true
  try {
    const [detail, planned] = await Promise.all([
      getMyCourseDetail(courseId),
      getPlannedLessonIds(courseId, excludePlanId),
    ])
    plannedLessonIds.value = Array.isArray(planned) ? planned : []
    lessonPlans.value = normalizeLessonPlans(detail, selectedItemList)
  } finally {
    chapterLoading.value = false
  }
}

const handleCourseChange = async (courseId) => {
  await loadCourseStructure(courseId, [], '')
}

const collectFormItems = () => {
  const enabledList = lessonPlans.value.filter((item) => item.enabled)
  if (!enabledList.length) {
    Message.warning('请至少选择一个学习课时')
    return null
  }
  for (const item of enabledList) {
    if (!item.studyDate) {
      Message.warning(`请为 ${item.lessonName} 选择学习日期`)
      return null
    }
    if (!Array.isArray(item.timeRange) || item.timeRange.length !== 2) {
      Message.warning(`请为 ${item.lessonName} 选择开始和结束时间`)
      return null
    }
    const today = formatDateKey(new Date())
    if (item.studyDate === today && item.timeRange[0] < getCurrentMinPlanTime()) {
      Message.warning(`${item.lessonName} 的开始时间不能早于当前时间`)
      return null
    }
  }
  return enabledList.map((item) => ({
    chapterId: item.chapterId,
    lessonId: item.lessonId,
    studyDate: item.studyDate,
    startTime: item.timeRange[0],
    endTime: item.timeRange[1],
  }))
}

const submitPlanForm = async () => {
  if (!formData.value.courseId) {
    Message.warning('请先选择课程')
    return
  }
  const itemList = collectFormItems()
  if (!itemList) {
    return
  }
  const result = await saveStudyPlan({
    planId: formMode.value === 'edit' ? formData.value.planId : undefined,
    courseId: formData.value.courseId,
    description: formData.value.description,
    itemList,
  })
  if (!result) {
    return
  }
  Message.success(
    formMode.value === 'edit' ? '学习计划已更新' : '学习计划已创建'
  )
  formDrawerVisible.value = false
  await loadDashboardData()
}

const toggleDetailItemStatus = async (item) => {
  const nextStatus = Number(item.status) === 2 ? 0 : 2
  const detail = await updateStudyPlanItemStatus({
    itemId: item.itemId,
    status: nextStatus,
  })
  detailData.value = detail
  await loadDashboardData()
}

const toggleTodayPlan = async (item) => {
  await updateStudyPlanItemStatus({
    itemId: item.itemId,
    status: item.completed ? 0 : 2,
  })
  await loadDashboardData()
  if (detailData.value?.planId === item.planId) {
    detailData.value = await getStudyPlanDetail(item.planId)
  }
}

onMounted(async () => {
  await Promise.all([loadDashboardData(), loadCourseOptions()])
})
</script>

<style scoped>
.study-plan-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.study-plan-card {
  border: 1px solid #e7eef9;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 14px 30px rgba(52, 85, 141, 0.06);
}

.study-plan-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.study-plan-card__title {
  color: #182f56;
  font-size: 18px;
  font-weight: 700;
}

.study-plan-link {
  border: 0;
  background: transparent;
  color: #2d73f5;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.study-plan-page__banner {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: center;
  border-radius: 6px;
}

.study-plan-page__banner-content h1 {
  margin: 0 0 10px;
  color: #132b53;
  font-size: 28px;
  line-height: 1.1;
}

.study-plan-page__banner-content p {
  margin: 0;
  color: #5f7494;
  font-size: 14px;
}

.study-plan-page__banner-visual {
  display: flex;
  justify-content: center;
}

.calendar-illustration {
  position: relative;
  width: 230px;
  height: 120px;
}

.calendar-illustration__card {
  position: absolute;
  right: 28px;
  top: 0;
  width: 124px;
  height: 96px;
  border-radius: 18px;
  background: linear-gradient(180deg, #64a5ff 0%, #2d73f5 100%);
  box-shadow: 0 18px 28px rgba(52, 115, 245, 0.25);
  transform: rotate(10deg);
}

.calendar-illustration__rings span {
  position: absolute;
  top: -8px;
  width: 8px;
  height: 20px;
  border-radius: 999px;
  background: #2a67d9;
}

.calendar-illustration__rings span:first-child {
  left: 22px;
}

.calendar-illustration__rings span:last-child {
  right: 22px;
}

.calendar-illustration__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  padding: 24px 18px 0;
}

.calendar-illustration__grid span {
  height: 12px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.92);
}

.calendar-illustration__check {
  position: absolute;
  left: 44px;
  bottom: 18px;
  width: 36px;
  height: 20px;
  border-left: 6px solid #fff;
  border-bottom: 6px solid #fff;
  transform: rotate(-45deg);
}

.calendar-illustration__clock {
  position: absolute;
  right: 0;
  bottom: 2px;
  width: 56px;
  height: 56px;
  border: 6px solid #2d73f5;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 12px 20px rgba(45, 115, 245, 0.18);
}

.calendar-illustration__clock-minute,
.calendar-illustration__clock-hour {
  position: absolute;
  left: 50%;
  top: 50%;
  border-radius: 999px;
  background: #2d73f5;
  transform-origin: center top;
}

.calendar-illustration__clock-minute {
  width: 3px;
  height: 16px;
  margin-left: -1.5px;
  margin-top: -15px;
}

.calendar-illustration__clock-hour {
  width: 3px;
  height: 11px;
  margin-left: -1.5px;
  margin-top: -10px;
  transform: rotate(60deg);
}

.study-plan-page__overview {
  padding: 18px 18px 16px;
}

.study-plan-page__loading,
.study-plan-page__empty-small {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 96px;
  color: #6f839d;
  font-size: 14px;
}

.study-plan-page__empty {
  padding: 24px 0 12px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #edf2fb;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
}

.overview-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 6px;
}

.overview-card__icon .iconfont {
  font-size: 24px;
}

.overview-card__icon.is-blue {
  background: #eaf2ff;
  color: #2d73f5;
}

.overview-card__icon.is-green {
  background: #e9fbef;
  color: #25af64;
}

.overview-card__icon.is-orange {
  background: #fff2e4;
  color: #ff9b35;
}

.overview-card__icon.is-purple {
  background: #f0ebff;
  color: #7e57ff;
}

.overview-card__content span {
  display: block;
  margin-bottom: 8px;
  color: #7486a2;
  font-size: 14px;
}

.overview-card__value {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.overview-card__value strong {
  color: #152e57;
  font-size: 38px;
  line-height: 1;
}

.overview-card__value em {
  color: #4f6483;
  font-size: 18px;
  font-style: normal;
}

.overview-card__content small {
  color: #6d829d;
  font-size: 14px;
}

.study-plan-page__middle {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.study-plan-page__middle .study-plan-card,
.study-plan-page>.study-plan-card:last-of-type {
  padding: 16px 18px 18px;
}

.today-plan-list {
  display: grid;
}

.today-plan-item {
  display: grid;
  grid-template-columns: 34px 72px minmax(0, 1fr) 120px;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  border-top: 1px solid #eef3fb;
}

.today-plan-item:first-child {
  border-top: 0;
}

.today-plan-item__check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  min-height: 24px;
}

.today-plan-item__cover,
.plan-table__cover,
.plan-detail__cover {
  overflow: hidden;
  background: linear-gradient(135deg, #edf4ff 0%, #dbe8ff 100%);
}

.today-plan-item__cover,
.plan-table__cover,
.plan-detail__cover {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: #2d73f5;
  font-weight: 700;
}

.today-plan-item__cover {
  height: 54px;
}

.plan-table__cover {
  width: 48px;
  height: 48px;
}

.plan-detail__cover {
  width: 72px;
  height: 72px;
}

.today-plan-item__cover img,
.plan-table__cover img,
.plan-detail__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.today-plan-item__content strong {
  display: block;
  margin-bottom: 6px;
  color: #182f56;
  font-size: 16px;
  line-height: 1.4;
}

.today-plan-item__content p {
  margin: 0;
  color: #667c98;
  font-size: 14px;
}

.today-plan-item__time {
  color: #2d73f5;
  font-size: 15px;
  text-align: right;
}

.schedule-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.schedule-head__range {
  color: #506480;
  font-size: 14px;
  font-weight: 600;
}

.schedule-head__actions {
  display: flex;
  gap: 8px;
}

.schedule-head__nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #e7eef9;
  border-radius: 999px;
  background: #fff;
  color: #8496b0;
  cursor: pointer;
}

.schedule-week {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 18px;
}

.schedule-week__day {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 4px 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
}

.schedule-week__weekday {
  color: #8395b0;
  font-size: 14px;
}

.schedule-week__date {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  color: #1a3158;
  font-size: 24px;
  line-height: 1;
}

.schedule-week__day.is-active .schedule-week__date {
  background: #2d73f5;
  color: #fff;
}

.schedule-week__dot {
  position: absolute;
  bottom: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2d73f5;
}

.schedule-list {
  border: 1px solid #edf2fb;
  border-radius: 6px;
  overflow: hidden;
}

.schedule-item {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr) 52px;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-top: 1px solid #eef3fb;
  background: #fff;
}

.schedule-item:first-child {
  border-top: 0;
}

.schedule-item__time {
  color: #5d7290;
  font-size: 14px;
}

.schedule-item__content {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: #5d7290;
  font-size: 14px;
}

.schedule-item__content strong {
  color: #162f58;
  font-size: 15px;
}

.schedule-item__tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  height: 24px;
  padding: 0 8px;
  border-radius: 8px;
  font-size: 12px;
  font-style: normal;
}

.schedule-item__tag.is-blue {
  background: #edf4ff;
  color: #2d73f5;
}

.schedule-item__tag.is-green {
  background: #ecfbf2;
  color: #23a862;
}

.plan-table__head,
.plan-table__row {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1.2fr) 90px 90px 150px 160px;
  align-items: center;
  gap: 16px;
}

.plan-table__head {
  padding: 0 0 12px;
  color: #7d90ab;
  font-size: 14px;
}

.plan-table__row {
  padding: 16px 0;
  border-top: 1px solid #eef3fb;
}

.plan-table__course {
  display: flex;
  align-items: center;
  gap: 14px;
}

.plan-table__course-info strong {
  display: block;
  margin-bottom: 6px;
  color: #162f58;
  font-size: 15px;
}

.plan-table__course-info p {
  margin: 0;
  color: #7386a0;
  font-size: 12px;
  line-height: 1.6;
}

.plan-table__progress {
  display: flex;
  align-items: center;
  gap: 14px;
}

.plan-table__bar {
  width: 180px;
  height: 6px;
  border-radius: 999px;
  background: #edf2fb;
  overflow: hidden;
}

.plan-table__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #2d73f5 0%, #4d92ff 100%);
}

.plan-table__progress em,
.plan-table__cell {
  color: #556984;
  font-size: 14px;
  font-style: normal;
}

.plan-table__actions {
  display: flex;
  gap: 14px;
}

:deep(.today-plan-item__check .el-checkbox) {
  margin-right: 0;
}

@media (max-width: 1280px) {

  .overview-grid,
  .study-plan-page__middle {
    grid-template-columns: 1fr 1fr;
  }

  .plan-table__head {
    display: none;
  }

  .plan-table__row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 960px) {

  .study-plan-page__banner,
  .overview-grid,
  .study-plan-page__middle {
    grid-template-columns: 1fr;
  }

  .today-plan-item {
    grid-template-columns: 34px 60px minmax(0, 1fr);
  }

  .today-plan-item__time {
    grid-column: 2 / 4;
    text-align: left;
  }

  .schedule-item,
  .plan-detail__head {
    grid-template-columns: 1fr;
    display: grid;
  }

  .schedule-item__content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
