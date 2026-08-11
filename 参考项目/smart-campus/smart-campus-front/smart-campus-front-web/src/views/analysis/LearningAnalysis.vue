<template>
  <div class="learning-analysis-page">
    <section class="learning-analysis-page__hero">
      <div class="learning-analysis-page__hero-copy">
        <h1>学习分析</h1>
        <p>从课程、计划和考试三个维度，持续观察自己的学习节奏与成长变化。</p>
      </div>

      <div class="learning-analysis-page__hero-tools">
        <el-date-picker v-model="dateRange" type="daterange" unlink-panels range-separator="~" start-placeholder="开始日期"
          end-placeholder="结束日期" value-format="YYYY-MM-DD" class="learning-analysis-page__date-picker" />
      </div>
    </section>

    <section class="analysis-tabs card-shell">
      <el-tabs v-model="activeTab" class="analysis-tabs__tabs">
        <el-tab-pane v-for="item in tabOptions" :key="item.key" :label="item.label" :name="item.key" />
      </el-tabs>
    </section>

    <div v-if="loading" class="analysis-state card-shell">正在加载学习分析数据...</div>

    <div v-else class="analysis-content">
      <template v-if="activeTab === 'overview'">
        <section class="overview-stats">
          <article v-for="item in overviewCards" :key="item.label" class="overview-stats__card card-shell">
            <div class="overview-stats__icon" :class="item.theme">
              <i class="iconfont" :class="item.icon" />
            </div>
            <div class="overview-stats__body">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small :class="{ 'is-growth': item.trendPositive }">{{ item.tip }}</small>
            </div>
          </article>
        </section>

        <section class="overview-grid">
          <article class="card-shell panel">
            <div class="panel__head">
              <h3>学习时长趋势</h3>
              <button type="button" class="link-button" @click="setQuickRange(7)">
                近7天
              </button>
            </div>
            <p class="panel__remark">{{ trendDataRemark }}</p>
            <div v-if="!trendData.length" class="panel__empty">暂无趋势数据</div>
            <div v-else class="line-chart">
              <div class="line-chart__legend">
                <span class="line-chart__legend-dot" />
                <span>学习时长（小时）</span>
              </div>
              <div class="line-chart__canvas">
                <BaseEChart :option="trendChartOption" />
              </div>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>课程学习分布</h3>
              <button type="button" class="link-button" @click="jumpToCourseTab">
                更多
              </button>
            </div>
            <div v-if="!courseDistribution.length" class="panel__empty">暂无课程数据</div>
            <div v-else class="distribution-card">
              <div class="distribution-card__donut">
                <BaseEChart :option="courseDistributionOption" />
              </div>
              <div class="distribution-card__legend">
                <article v-for="item in courseDistribution" :key="item.courseId || item.courseName"
                  class="distribution-card__legend-item">
                  <span class="distribution-card__legend-dot" :style="{ background: item.color }" />
                  <div class="distribution-card__legend-name">{{ item.courseName }}</div>
                  <div class="distribution-card__legend-value">
                    {{ item.hoursText }}h ({{ item.percent }}%)
                  </div>
                </article>
              </div>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>每日学习时长</h3>
              <button type="button" class="link-button" @click="setQuickRange(7)">
                近7天
              </button>
            </div>
            <div v-if="!dailyBarItems.length" class="panel__empty">暂无学习时长数据</div>
            <div v-else class="bar-chart">
              <BaseEChart :option="dailyBarChartOption" />
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>章节掌握情况</h3>
              <button type="button" class="link-button" @click="activeTab = 'knowledge'">
                全部章节
              </button>
            </div>
            <p class="panel__remark">{{ knowledgeDataRemark }}</p>
            <div v-if="!knowledgeItems.length" class="panel__empty">暂无章节数据</div>
            <div v-else class="mastery-list">
              <article v-for="item in knowledgeItems.slice(0, 4)" :key="item.key" class="mastery-list__item">
                <div class="mastery-list__meta">
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.mastery }}%</span>
                </div>
                <div class="mastery-list__track">
                  <span :style="{ width: `${item.mastery}%` }" />
                </div>
              </article>
            </div>
          </article>
        </section>
      </template>

      <template v-else-if="activeTab === 'course'">
        <section class="course-analysis-layout">
          <article class="card-shell panel panel--stack">
            <div class="panel__head">
              <h3>课程进度分析</h3>
              <button type="button" class="link-button" @click="jumpToCourseList">
                我的课程
              </button>
            </div>
            <div v-if="!courseAnalysisList.length" class="panel__empty">暂无课程分析数据</div>
            <div v-else class="course-analysis-list">
              <article v-for="item in courseAnalysisList" :key="item.courseId" class="course-analysis-item">
                <div class="course-analysis-item__main">
                  <div class="course-analysis-item__title-row">
                    <strong>{{ item.courseName }}</strong>
                    <span>{{ item.teacherName || '未设置教师' }}</span>
                  </div>
                  <div class="course-analysis-item__progress-row">
                    <span>{{ item.chapterText }}</span>
                    <span>{{ item.progress }}%</span>
                  </div>
                  <div class="course-analysis-item__progress">
                    <span :style="{ width: `${item.progress}%` }" />
                  </div>
                </div>
                <div class="course-analysis-item__meta">
                  <label>学习时长</label>
                  <strong>{{ item.studyHours }} 小时</strong>
                  <span>{{ item.structureText }}</span>
                </div>
                <div class="course-analysis-item__meta">
                  <label>掌握状态</label>
                  <strong>{{ item.statusText }}</strong>
                  <span>{{ item.lastStudyText }}</span>
                </div>
              </article>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>课程投入占比</h3>
            </div>
            <div v-if="!courseDistribution.length" class="panel__empty">暂无课程投入数据</div>
            <div v-else class="ranking-list">
              <article v-for="(item, index) in courseDistribution" :key="item.courseId || item.courseName"
                class="ranking-list__item">
                <span class="ranking-list__index">{{ String(index + 1).padStart(2, '0') }}</span>
                <div class="ranking-list__content">
                  <div class="ranking-list__meta">
                    <strong>{{ item.courseName }}</strong>
                    <span>{{ item.hoursText }} 小时 / {{ item.percent }}%</span>
                  </div>
                  <div class="ranking-list__track">
                    <span :style="{ width: `${item.percent}%`, background: item.color }" />
                  </div>
                </div>
              </article>
            </div>
          </article>
        </section>
      </template>

      <template v-else-if="activeTab === 'knowledge'">
        <section class="knowledge-analysis-layout">
          <article class="card-shell panel panel--stack">
            <div class="panel__head">
              <h3>章节掌握分析</h3>
              <button type="button" class="link-button" @click="handleComingSoon('章节详情')">
                查看详情
              </button>
            </div>
            <p class="panel__remark">{{ knowledgeDataRemark }}</p>
            <div v-if="!knowledgeItems.length" class="panel__empty">暂无章节掌握数据</div>
            <div v-else class="knowledge-list">
              <article v-for="item in knowledgeItems" :key="item.key" class="knowledge-list__item">
                <div class="knowledge-list__main">
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.courseName }}</span>
                </div>
                <div class="knowledge-list__progress">
                  <div class="knowledge-list__progress-track">
                    <span :style="{ width: `${item.mastery}%` }" />
                  </div>
                  <em>{{ item.mastery }}%</em>
                </div>
                <div class="knowledge-list__level" :class="item.levelTheme">
                  {{ item.levelText }}
                </div>
              </article>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>章节掌握分层</h3>
            </div>
            <div class="segment-stats">
              <article v-for="item in knowledgeSegmentCards" :key="item.label" class="segment-stats__item">
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
                <small>{{ item.tip }}</small>
              </article>
            </div>
          </article>
        </section>
      </template>

      <template v-else-if="activeTab === 'behavior'">
        <section class="behavior-analysis-layout">
          <article class="card-shell panel">
            <div class="panel__head">
              <h3>学习行为分布</h3>
            </div>
            <div class="behavior-grid">
              <article v-for="item in behaviorCards" :key="item.label" class="behavior-grid__item">
                <div class="behavior-grid__icon" :class="item.theme">
                  <i class="iconfont" :class="item.icon" />
                </div>
                <div class="behavior-grid__body">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <small>{{ item.tip }}</small>
                </div>
              </article>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>时间偏好</h3>
            </div>
            <div v-if="!timePreferenceItems.length" class="panel__empty">暂无行为时段数据</div>
            <div v-else class="time-preference">
              <article v-for="item in timePreferenceItems" :key="item.label" class="time-preference__item">
                <div class="time-preference__meta">
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.value }}%</span>
                </div>
                <div class="time-preference__track">
                  <span :style="{ width: `${item.value}%` }" />
                </div>
              </article>
            </div>
          </article>
        </section>
      </template>

      <template v-else>
        <section class="report-layout">
          <article class="card-shell panel panel--stack">
            <div class="panel__head">
              <h3>学习报告</h3>
              <button type="button" class="link-button" @click="handleComingSoon('导出报告')">
                导出报告
              </button>
            </div>
            <div class="report-hero">
              <div class="report-hero__score">
                <span>本周期学习评分</span>
                <strong>{{ reportScore }}</strong>
                <em>分</em>
              </div>
              <div class="report-hero__summary">
                <p>{{ reportSummary }}</p>
              </div>
            </div>
            <div class="report-tags">
              <span v-for="item in reportTags" :key="item" class="report-tags__item">
                {{ item }}
              </span>
            </div>
          </article>

          <article class="card-shell panel">
            <div class="panel__head">
              <h3>学习建议</h3>
            </div>
            <div class="advice-list">
              <article v-for="item in reportAdviceList" :key="item.title" class="advice-list__item">
                <div class="advice-list__index">{{ item.index }}</div>
                <div class="advice-list__body">
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.desc }}</p>
                </div>
              </article>
            </div>
          </article>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import BaseEChart from '@/components/BaseEChart.vue'
import Message from '@/utils/Message'
import { loadLearningAnalysisDashboard } from '@/api/analysis'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('overview')
const dateRange = ref([])
const dashboard = ref({
  startDate: '',
  endDate: '',
  overview: null,
  behavior: null,
  report: null,
  trendList: [],
  dailyStudyList: [],
  courseList: [],
  courseDistributionList: [],
  knowledgeList: [],
  timePreferenceList: [],
})

const tabOptions = [
  { key: 'overview', label: '学习概览' },
  { key: 'course', label: '课程分析' },
  { key: 'knowledge', label: '章节掌握分析' },
  { key: 'behavior', label: '学习行为分析' },
  { key: 'report', label: '学习报告' },
]

const chartColors = ['#2d73f5', '#43c77a', '#ff9f32', '#ff6c57', '#8e66ff', '#28b9ff']

const pad = (value) => String(value).padStart(2, '0')

const formatDateKey = (date) => {
  const value = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(value.getTime())) {
    return ''
  }
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
}

const createDateRange = (days) => {
  const end = new Date()
  end.setHours(0, 0, 0, 0)
  const start = new Date(end)
  start.setDate(end.getDate() - (days - 1))
  return [formatDateKey(start), formatDateKey(end)]
}

const safeNumber = (value, digits = 0) => {
  const number = Number(value || 0)
  if (digits <= 0) {
    return Number.isFinite(number) ? Math.round(number) : 0
  }
  return Number.isFinite(number) ? number.toFixed(digits) : (0).toFixed(digits)
}

const overview = computed(() => dashboard.value.overview || {})
const behavior = computed(() => dashboard.value.behavior || {})
const report = computed(() => dashboard.value.report || {})
const trendDataRemark = computed(
  () => dashboard.value.trendDataRemark || '按学习记录汇总统计'
)
const knowledgeDataRemark = computed(
  () => dashboard.value.knowledgeDataRemark || '当前按章节口径统计'
)
const courseAnalysisList = computed(() => dashboard.value.courseList || [])
const courseDistribution = computed(() =>
  (dashboard.value.courseDistributionList || []).map((item, index) => ({
    ...item,
    color: chartColors[index % chartColors.length],
    hoursText: Number(item.studyHours || 0).toFixed(1),
  }))
)
const knowledgeItems = computed(() => dashboard.value.knowledgeList || [])
const timePreferenceItems = computed(() => dashboard.value.timePreferenceList || [])

const overviewHours = computed(() =>
  Number(overview.value.totalStudyHours || 0).toFixed(1)
)

const overviewCards = computed(() => [
  {
    label: '学习时长',
    value: overviewHours.value,
    tip: `较上周期 ${Number(overview.value.hoursGrowthRate || 0) >= 0 ? '↑' : '↓'
      } ${Math.abs(Number(overview.value.hoursGrowthRate || 0))}%`,
    icon: 'icon-time',
    theme: 'is-blue',
    trendPositive: Number(overview.value.hoursGrowthRate || 0) >= 0,
  },
  {
    label: '完成任务',
    value: `${safeNumber(overview.value.completedTaskCount || 0)}`,
    tip: `计划任务 ${safeNumber(overview.value.totalTaskCount || 0)} 个`,
    icon: 'icon-completed',
    theme: 'is-green',
    trendPositive: true,
  },
  {
    label: '学习课程',
    value: `${safeNumber(overview.value.courseCount || 0)}`,
    tip: `进行中 ${safeNumber(overview.value.inProgressCourseCount || 0)} 门`,
    icon: 'icon-courses',
    theme: 'is-purple',
    trendPositive: true,
  },
  {
    label: '平均成绩',
    value: `${safeNumber(overview.value.averageScore || 0, 1)}`,
    tip: safeNumber(overview.value.completedExamCount || 0)
      ? `共完成 ${safeNumber(overview.value.completedExamCount || 0)} 场考试`
      : '暂无考试成绩',
    icon: 'icon-collection',
    theme: 'is-orange',
    trendPositive: Number(overview.value.averageScore || 0) >= 60,
  },
])

const trendData = computed(() => dashboard.value.trendList || [])

const trendChartOption = computed(() => ({
  animationDuration: 450,
  grid: {
    left: 16,
    right: 16,
    top: 18,
    bottom: 26,
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
    data: trendData.value.map((item) => item.label),
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
    axisLabel: {
      color: '#93a2b6',
      fontSize: 12,
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
      color: '#93a2b6',
      fontSize: 12,
      formatter: '{value}h',
    },
    splitLine: {
      lineStyle: {
        color: '#edf2fb',
      },
    },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      data: trendData.value.map((item) => Number(item.value || 0)),
      lineStyle: {
        color: '#2d73f5',
        width: 4,
      },
      itemStyle: {
        color: '#2d73f5',
      },
      areaStyle: {
        color: 'rgba(45, 115, 245, 0.08)',
      },
    },
  ],
}))

const dailyBarItems = computed(() => {
  return (dashboard.value.dailyStudyList || []).slice(-7).map((item) => ({
    ...item,
    value: Number(item.value || 0),
  }))
})

const dailyBarChartOption = computed(() => ({
  animationDuration: 450,
  grid: {
    left: 12,
    right: 12,
    top: 18,
    bottom: 20,
    containLabel: true,
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
    },
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
    data: dailyBarItems.value.map((item) => item.label),
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
    axisLabel: {
      color: '#7285a0',
      fontSize: 13,
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
      color: '#93a2b6',
      fontSize: 12,
      formatter: '{value}h',
    },
    splitLine: {
      lineStyle: {
        color: '#edf2fb',
      },
    },
  },
  series: [
    {
      type: 'bar',
      barWidth: 28,
      data: dailyBarItems.value.map((item) => Number(item.value || 0)),
      itemStyle: {
        borderRadius: [10, 10, 6, 6],
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#4d92ff' },
            { offset: 1, color: '#2d73f5' },
          ],
        },
      },
    },
  ],
}))

const courseDistributionOption = computed(() => ({
  animationDuration: 450,
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(20, 43, 82, 0.92)',
    borderWidth: 0,
    textStyle: {
      color: '#fff',
    },
    formatter: ({ name, value, percent }) =>
      `${name}<br/>学习时长：${Number(value || 0).toFixed(1)} 小时<br/>占比：${percent}%`,
  },
  title: {
    text: `{label|总学习时长}\n{value|${overviewHours.value}}\n{unit|小时}`,
    left: 'center',
    top: 'center',
    textStyle: {
      rich: {
        label: {
          color: '#7c8ea7',
          fontSize: 13,
          fontWeight: 400,
          lineHeight: 20,
        },
        value: {
          color: '#162f58',
          fontSize: 24,
          fontWeight: 700,
          lineHeight: 32,
        },
        unit: {
          color: '#5a6f8d',
          fontSize: 14,
          fontWeight: 400,
          lineHeight: 20,
        },
      },
    },
  },
  series: [
    {
      type: 'pie',
      radius: ['58%', '78%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
      },
      labelLine: {
        show: false,
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 2,
      },
      data: courseDistribution.value.map((item) => ({
        name: item.courseName,
        value: Number(item.studyHours || 0),
        itemStyle: {
          color: item.color,
        },
      })),
    },
  ],
}))

const knowledgeSegmentCards = computed(() => {
  const excellent = knowledgeItems.value.filter((item) => Number(item.mastery || 0) >= 85).length
  const good = knowledgeItems.value.filter((item) => {
    const mastery = Number(item.mastery || 0)
    return mastery >= 65 && mastery < 85
  }).length
  const reinforce = knowledgeItems.value.filter((item) => Number(item.mastery || 0) < 65).length
  return [
    { label: '掌握优秀', value: excellent, tip: '可进入进阶学习' },
    { label: '掌握良好', value: good, tip: '继续保持学习频率' },
    { label: '需重点巩固', value: reinforce, tip: '建议复习薄弱章节' },
  ]
})

const behaviorCards = computed(() => [
  {
    label: '任务完成率',
    value: `${safeNumber(behavior.value.taskCompletionRate || 0)}%`,
    tip: `共跟进 ${safeNumber(behavior.value.totalTaskCount || 0)} 个任务`,
    icon: 'icon-completed',
    theme: 'is-green',
  },
  {
    label: '活跃学习天数',
    value: `${safeNumber(behavior.value.activeDays || 0)} 天`,
    tip: '按当前筛选周期统计',
    icon: 'icon-calendar',
    theme: 'is-blue',
  },
  {
    label: '已完成考试',
    value: `${safeNumber(behavior.value.completedExamCount || 0)} 场`,
    tip: Number(overview.value.averageScore || 0)
      ? `平均成绩 ${safeNumber(overview.value.averageScore || 0, 1)} 分`
      : '当前暂无成绩数据',
    icon: 'icon-menu-exam',
    theme: 'is-purple',
  },
  {
    label: '学习计划数',
    value: `${safeNumber(behavior.value.totalPlanCount || 0)} 个`,
    tip: `已完成 ${safeNumber(behavior.value.completedTaskCount || 0)} 个周期任务`,
    icon: 'icon-menu-plan',
    theme: 'is-orange',
  },
])

const reportScore = computed(() => safeNumber(report.value.score || 0))
const reportSummary = computed(() => report.value.summary || '暂无学习报告')
const reportTags = computed(() => report.value.tags || [])
const reportAdviceList = computed(() => report.value.adviceList || [])

const setQuickRange = (days) => {
  dateRange.value = createDateRange(days)
}

const jumpToCourseTab = () => {
  activeTab.value = 'course'
}

const jumpToCourseList = () => {
  router.push('/courses')
}

const handleComingSoon = (title) => {
  Message.warning(`${title} 功能正在接入中`)
}

const loadAnalysisData = async () => {
  if (!Array.isArray(dateRange.value) || dateRange.value.length !== 2) {
    return
  }
  loading.value = true
  try {
    const result = await loadLearningAnalysisDashboard({
      startDate: dateRange.value[0],
      endDate: dateRange.value[1],
    })
    dashboard.value = result || dashboard.value
  } finally {
    loading.value = false
  }
}

watch(
  dateRange,
  (value) => {
    if (Array.isArray(value) && value.length === 2 && value[0] && value[1]) {
      loadAnalysisData()
    }
  },
  { deep: true }
)

onMounted(() => {
  setQuickRange(7)
})
</script>

<style scoped lang="scss">
.learning-analysis-page {
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

.learning-analysis-page__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.learning-analysis-page__hero-copy {
  h1 {
    margin: 0 0 10px;
    color: #182f56;
    font-size: 28px;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #6f839d;
    font-size: 14px;
  }
}

.learning-analysis-page__hero-tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.learning-analysis-page__date-picker {
  width: 286px;
}

.analysis-tabs {
  padding: 0 18px;
}

.analysis-tabs__tabs {
  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    background-color: #edf2fb;
  }

  :deep(.el-tabs__item) {
    height: 54px;
    color: #7b8ba4;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.el-tabs__item.is-active) {
    color: #2a66f6;
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 6px;
    background: #2a66f6;
  }

  :deep(.el-tabs__content) {
    display: none;
  }
}

.analysis-state,
.panel__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  color: #6f839d;
  font-size: 14px;
}

.analysis-content,
.overview-grid,
.course-analysis-layout,
.knowledge-analysis-layout,
.behavior-analysis-layout,
.report-layout {
  display: grid;
  gap: 16px;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.overview-stats__card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.overview-stats__icon,
.behavior-grid__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 6px;

  .iconfont {
    font-size: 24px;
  }

  &.is-blue {
    background: #eaf2ff;
    color: #2d73f5;
  }

  &.is-green {
    background: #e8fbef;
    color: #23a55c;
  }

  &.is-purple {
    background: #f0ebff;
    color: #875cff;
  }

  &.is-orange {
    background: #fff2e4;
    color: #ff9a33;
  }
}

.overview-stats__body,
.behavior-grid__body {
  span {
    display: block;
    margin-bottom: 8px;
    color: #7385a1;
    font-size: 14px;
  }

  strong {
    display: block;
    margin-bottom: 8px;
    color: #162f58;
    font-size: 24px;
    line-height: 1.1;
  }

  small {
    color: #7c8ea7;
    font-size: 13px;

    &.is-growth {
      color: #20a45e;
    }
  }
}

.overview-grid {
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.95fr);
}

.panel {
  padding: 18px 20px;

  &--stack {
    min-width: 0;
  }
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;

  h3 {
    margin: 0;
    color: #182f56;
    font-size: 18px;
  }
}

.panel__remark {
  margin: -8px 0 14px;
  color: #8a99af;
  font-size: 12px;
  line-height: 1.7;
}

.link-button {
  border: 0;
  background: transparent;
  color: #2d73f5;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.line-chart {
  display: grid;
  gap: 16px;
}

.line-chart__legend {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #6f829d;
  font-size: 14px;
}

.line-chart__legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #2d73f5;
}

.line-chart__canvas {
  height: 260px;
}

.distribution-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 22px;
  align-items: center;
}

.distribution-card__donut {
  height: 220px;
}

.distribution-card__legend {
  display: grid;
  gap: 14px;
}

.distribution-card__legend-item {
  display: grid;
  grid-template-columns: 12px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.distribution-card__legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.distribution-card__legend-name {
  color: #172f57;
  font-size: 15px;
}

.distribution-card__legend-value {
  color: #5a6f8d;
  font-size: 14px;
}

.bar-chart {
  height: 240px;
}

.mastery-list,
.knowledge-list,
.time-preference,
.advice-list {
  display: grid;
  gap: 14px;
}

.mastery-list__item,
.time-preference__item {
  display: grid;
  gap: 10px;
}

.mastery-list__meta,
.time-preference__meta,
.ranking-list__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  strong {
    color: #172f57;
    font-size: 15px;
  }

  span {
    color: #5d7290;
    font-size: 14px;
  }
}

.mastery-list__track,
.time-preference__track,
.ranking-list__track,
.knowledge-list__progress-track,
.course-analysis-item__progress {
  height: 8px;
  border-radius: 999px;
  background: #edf2fb;
  overflow: hidden;

  span {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(135deg, #2d73f5 0%, #4e92ff 100%);
  }
}

.course-analysis-layout,
.knowledge-analysis-layout,
.behavior-analysis-layout,
.report-layout {
  grid-template-columns: minmax(0, 1.3fr) 340px;
}

.course-analysis-list,
.ranking-list {
  display: grid;
  gap: 14px;
}

.course-analysis-item {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 150px 150px;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border: 1px solid #edf2fb;
  border-radius: 6px;
  background: #fbfdff;
}

.course-analysis-item__title-row,
.course-analysis-item__progress-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.course-analysis-item__title-row {
  margin-bottom: 10px;

  strong {
    color: #182f56;
    font-size: 16px;
  }

  span {
    color: #70839d;
    font-size: 13px;
  }
}

.course-analysis-item__progress-row {
  margin-bottom: 8px;
  color: #6f829c;
  font-size: 13px;
}

.course-analysis-item__meta {
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    color: #8a99af;
    font-size: 12px;
  }

  strong {
    color: #162f58;
    font-size: 16px;
  }

  span {
    color: #6f839d;
    font-size: 12px;
  }
}

.ranking-list__item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.ranking-list__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #eef4ff;
  color: #2d73f5;
  font-size: 15px;
  font-weight: 700;
}

.ranking-list__content {
  display: grid;
  gap: 10px;
}

.knowledge-list__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 0.8fr) 92px;
  gap: 16px;
  align-items: center;
  padding: 16px 18px;
  border: 1px solid #edf2fb;
  border-radius: 6px;
  background: #fbfdff;
}

.knowledge-list__main {
  strong {
    display: block;
    margin-bottom: 6px;
    color: #172f57;
    font-size: 15px;
  }

  span {
    color: #70839d;
    font-size: 13px;
  }
}

.knowledge-list__progress {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;

  em {
    color: #556984;
    font-size: 14px;
    font-style: normal;
  }
}

.knowledge-list__level {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;

  &.is-high {
    background: #e8fbef;
    color: #21a35c;
  }

  &.is-middle {
    background: #edf4ff;
    color: #2d73f5;
  }

  &.is-low {
    background: #fff2e4;
    color: #ff9a33;
  }
}

.segment-stats {
  display: grid;
  gap: 14px;
}

.segment-stats__item {
  padding: 18px;
  border-radius: 6px;
  background: linear-gradient(180deg, #fbfdff 0%, #f4f8ff 100%);

  strong {
    display: block;
    margin-bottom: 8px;
    color: #162f58;
    font-size: 24px;
  }

  span {
    display: block;
    margin-bottom: 8px;
    color: #182f56;
    font-size: 15px;
    font-weight: 600;
  }

  small {
    color: #7385a1;
    font-size: 13px;
  }
}

.behavior-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.behavior-grid__item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid #edf2fb;
  border-radius: 6px;
  background: #fbfdff;
}

.report-hero {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
}

.report-hero__score {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  border-radius: 6px;
  background: linear-gradient(135deg, #2d73f5 0%, #5a98ff 100%);
  color: #fff;

  span {
    font-size: 14px;
  }

  strong {
    margin-top: 10px;
    font-size: 56px;
    line-height: 1;
  }

  em {
    margin-top: 8px;
    font-size: 16px;
    font-style: normal;
  }
}

.report-hero__summary {
  p {
    margin: 0;
    color: #556984;
    font-size: 15px;
    line-height: 1.9;
  }
}

.report-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.report-tags__item {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: #edf4ff;
  color: #2d73f5;
  font-size: 13px;
  font-weight: 600;
}

.advice-list__item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 14px;
  align-items: flex-start;
}

.advice-list__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #eef4ff;
  color: #2d73f5;
  font-size: 16px;
  font-weight: 700;
}

.advice-list__body {
  strong {
    display: block;
    margin-bottom: 8px;
    color: #182f56;
    font-size: 16px;
  }

  p {
    margin: 0;
    color: #667c98;
    font-size: 14px;
    line-height: 1.8;
  }
}

@media (max-width: 1280px) {

  .overview-stats,
  .overview-grid,
  .course-analysis-layout,
  .knowledge-analysis-layout,
  .behavior-analysis-layout,
  .report-layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .course-analysis-item,
  .knowledge-list__item {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {

  .learning-analysis-page__hero,
  .overview-stats,
  .overview-grid,
  .course-analysis-layout,
  .knowledge-analysis-layout,
  .behavior-analysis-layout,
  .report-layout,
  .behavior-grid,
  .distribution-card,
  .report-hero {
    grid-template-columns: 1fr;
    display: grid;
  }

  .learning-analysis-page__hero-tools,
  .learning-analysis-page__date-picker {
    width: 100%;
  }

  .course-analysis-item {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {

  .panel,
  .overview-stats__card,
  .behavior-grid__item {
    padding: 16px;
  }
}
</style>
