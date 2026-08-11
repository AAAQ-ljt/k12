<template>
  <div class="admin-dashboard-page" v-loading="loading">
    <section class="metric-grid">
      <article v-for="item in metricCards" :key="item.label" class="metric-card">
        <span class="metric-card__icon" :class="item.theme">
          <i class="iconfont" :class="item.iconClass" />
        </span>
        <div class="metric-card__body">
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
          <small :class="item.trendType">{{ item.trend }}</small>
        </div>
      </article>
    </section>

    <section class="dashboard-main-grid">
      <article class="dashboard-panel trend-panel">
        <div class="panel-header">
          <div>
            <h3>教学业务趋势</h3>
            <p>近 7 天课程、考试、作业提交数据</p>
          </div>
          <span class="panel-header__tag">本周 <i>⌄</i></span>
        </div>
        <div ref="trendChartRef" class="trend-chart" />
      </article>

      <article class="dashboard-panel resource-panel">
        <div class="panel-header">
          <div>
            <h3>资源分布</h3>
            <p>平台资源类型占比</p>
          </div>
        </div>
        <div class="resource-list">
          <div v-for="item in resourceStats" :key="item.label" class="resource-list__item">
            <span class="resource-list__icon" :class="item.theme">
              <i class="iconfont" :class="item.iconClass" />
            </span>
            <div class="resource-list__body">
              <div class="resource-list__row">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }} <em>({{ item.ratio }})</em></strong>
              </div>
              <div class="resource-list__bar">
                <span :class="item.theme" :style="{ width: `${item.percent}%` }" />
              </div>
            </div>
          </div>
        </div>
        <div class="resource-panel__footer">
          <span>总资源文件 <strong>{{ totalResourceCountText }}</strong></span>
          <span>存储占用 <strong>{{ storageUsagePercent }}%</strong></span>
        </div>
      </article>
    </section>

    <section class="dashboard-sub-grid">
      <article class="dashboard-panel todo-panel">
        <div class="panel-header">
          <div>
            <h3>待办事项</h3>
            <p>建议优先处理的运营事项</p>
          </div>
        </div>
        <div v-if="todoList.length" class="todo-list">
          <div
            v-for="item in todoList"
            :key="item.title"
            class="todo-list__item"
            :class="{ 'is-clickable': item.routePath }"
            @click="handleNavigate(item.routePath)"
          >
            <el-tag :class="item.theme" effect="plain">{{ item.tag }}</el-tag>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </div>
            <i>›</i>
          </div>
        </div>
        <el-empty v-else class="dashboard-empty" description="暂无待办事项" />
      </article>

      <article class="dashboard-panel activity-panel">
        <div class="panel-header">
          <div>
            <h3>最新动态</h3>
            <p>平台内教学、资源与系统公告更新</p>
          </div>
        </div>
        <div v-if="activityList.length" class="activity-list">
          <div
            v-for="item in activityList"
            :key="item.title"
            class="activity-list__item"
            :class="{ 'is-clickable': item.routePath }"
            @click="handleNavigate(item.routePath)"
          >
            <span class="activity-list__dot" :class="item.theme" />
            <div>
              <strong>{{ item.title }}</strong>
              <el-tag :class="item.theme" effect="plain">{{ item.desc }}</el-tag>
            </div>
            <time>{{ item.time }}</time>
          </div>
        </div>
        <el-empty v-else class="dashboard-empty" description="暂无最新动态" />
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { loadDashboard } from '@/api/dashboard'
import '@/assets/icon/iconfont.css'

echarts.use([BarChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const defaultDashboard = {
  metricCards: [
    { label: '在校学生', value: '0', trend: '单位：人', trendType: 'is-normal', iconClass: 'icon-user', theme: 'is-blue' },
    { label: '授课教师', value: '0', trend: '单位：人', trendType: 'is-normal', iconClass: 'icon-geren', theme: 'is-green' },
    { label: '开设课程', value: '0', trend: '单位：门', trendType: 'is-normal', iconClass: 'icon-xinrenkecheng', theme: 'is-purple' },
    { label: '资源文件', value: '0', trend: '单位：个', trendType: 'is-normal', iconClass: 'icon-attachment', theme: 'is-orange' },
  ],
  teachingTrend: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'].map((day) => ({ day, course: 0, exam: 0, homework: 0 })),
  resourceStats: [
    { label: '文档资料', value: '0', ratio: '0%', percent: 0, theme: 'is-blue', iconClass: 'icon-attachment' },
    { label: '教学视频', value: '0', ratio: '0%', percent: 0, theme: 'is-purple', iconClass: 'icon-play-cover' },
    { label: '教学图片', value: '0', ratio: '0%', percent: 0, theme: 'is-green', iconClass: 'icon-calendar' },
    { label: '压缩资料', value: '0', ratio: '0%', percent: 0, theme: 'is-orange', iconClass: 'icon-collection' },
  ],
  todoList: [],
  activityList: [],
  totalResourceCount: 0,
  storageUsagePercent: 0,
}

const metricCards = ref(defaultDashboard.metricCards)
const teachingTrend = ref(defaultDashboard.teachingTrend)
const resourceStats = ref(defaultDashboard.resourceStats)
const todoList = ref(defaultDashboard.todoList)
const activityList = ref(defaultDashboard.activityList)
const totalResourceCount = ref(defaultDashboard.totalResourceCount)
const storageUsagePercent = ref(defaultDashboard.storageUsagePercent)
const loading = ref(false)
const trendChartRef = ref(null)
const router = useRouter()
let trendChart = null
let resizeObserver = null

const totalResourceCountText = computed(() => totalResourceCount.value.toLocaleString())

const applyDashboard = (data = defaultDashboard) => {
  metricCards.value = data.metricCards?.length ? data.metricCards : defaultDashboard.metricCards
  teachingTrend.value = data.teachingTrend?.length ? data.teachingTrend : defaultDashboard.teachingTrend
  resourceStats.value = data.resourceStats?.length ? data.resourceStats : defaultDashboard.resourceStats
  todoList.value = data.todoList ?? []
  activityList.value = data.activityList ?? []
  totalResourceCount.value = Number(data.totalResourceCount ?? 0)
  storageUsagePercent.value = Number(data.storageUsagePercent ?? 0)
  renderTrendChart()
}

const buildTrendChartOption = () => ({
  color: ['#2d79ff', '#815cf6', '#24ad65'],
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
      shadowStyle: {
        color: 'rgba(45, 121, 255, 0.06)',
      },
    },
  },
  legend: {
    bottom: 0,
    left: 42,
    itemWidth: 10,
    itemHeight: 10,
    textStyle: {
      color: '#52647f',
      fontSize: 14,
      fontWeight: 700,
    },
  },
  grid: {
    top: 10,
    right: 8,
    bottom: 44,
    left: 42,
  },
  xAxis: {
    type: 'category',
    data: teachingTrend.value.map((item) => item.day),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      color: '#566984',
      fontSize: 13,
    },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitNumber: 4,
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      color: '#6f819d',
      fontSize: 13,
    },
    splitLine: {
      lineStyle: {
        color: '#edf2f8',
      },
    },
  },
  series: [
    buildTrendSeries('课程学习', 'course'),
    buildTrendSeries('考试参与', 'exam'),
    buildTrendSeries('作业提交', 'homework'),
  ],
})

const buildTrendSeries = (name, key) => ({
  name,
  type: 'bar',
  barWidth: 12,
  barGap: '55%',
  data: teachingTrend.value.map((item) => Number(item[key] ?? 0)),
  itemStyle: {
    borderRadius: [6, 6, 0, 0],
  },
})

const renderTrendChart = async () => {
  await nextTick()
  if (!trendChartRef.value) {
    return
  }
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  trendChart.setOption(buildTrendChartOption(), true)
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const data = await loadDashboard()
    applyDashboard(data)
  } catch (error) {
    applyDashboard(defaultDashboard)
    ElMessage.error('首页数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleNavigate = (routePath) => {
  if (!routePath) {
    return
  }
  router.push(routePath)
}

const bindChartResize = () => {
  if (!trendChartRef.value) {
    return
  }
  resizeObserver = new ResizeObserver(() => {
    trendChart?.resize()
  })
  resizeObserver.observe(trendChartRef.value)
  window.addEventListener('resize', handleWindowResize)
}

const handleWindowResize = () => {
  trendChart?.resize()
}

onMounted(() => {
  renderTrendChart()
  bindChartResize()
  loadDashboardData()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  window.removeEventListener('resize', handleWindowResize)
  trendChart?.dispose()
  trendChart = null
})
</script>

<style lang="scss" scoped>
.admin-dashboard-page {
  display: flex;
  height: calc(100vh - 92px);
  flex-direction: column;
  gap: 14px;
  overflow: visible;
}

.metric-card,
.dashboard-panel {
  border: 1px solid rgba(231, 238, 249, 0.95);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18px 38px rgba(54, 86, 138, 0.07);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  flex-shrink: 0;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 22px;
  min-width: 0;
  height: 104px;
  padding: 0 26px;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 6px;
  flex-shrink: 0;
  font-size: 28px;
}

.metric-card__body {
  min-width: 0;
}

.metric-card__body p {
  margin: 0 0 8px;
  color: #7f8da5;
  font-size: 14px;
}

.metric-card__body strong {
  display: block;
  margin-bottom: 8px;
  color: #13254a;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.metric-card__body small {
  font-size: 13px;
  font-weight: 700;
}

.metric-card__body small.is-up {
  color: #26b978;
}

.metric-card__body small.is-normal {
  color: #7d8da6;
}

.dashboard-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.85fr) minmax(340px, 1fr);
  gap: 14px;
  min-height: 0;
  flex: 1.35;
}

.dashboard-sub-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.85fr) minmax(0, 1.4fr);
  gap: 14px;
  min-height: 0;
  flex: 1;
}

.dashboard-panel {
  min-width: 0;
  min-height: 0;
  padding: 20px 22px;
  overflow: visible;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h3 {
  margin: 0 0 7px;
  color: #142448;
  font-size: 20px;
  font-weight: 900;
}

.panel-header p,
.todo-list__item p,
.activity-list__item p {
  margin: 0;
  color: #8b99ad;
  font-size: 14px;
}

.panel-header__tag,
.panel-header button {
  border: 0;
  border-radius: 6px;
  background: #eef5ff;
  color: #2d79ff;
  font-size: 13px;
  font-weight: 800;
}

.panel-header__tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
}

.panel-header button {
  height: 30px;
  padding: 0 12px;
  cursor: pointer;
}

.todo-panel,
.activity-panel,
.trend-panel {
  display: flex;
  flex-direction: column;
}

.trend-chart {
  width: 100%;
  min-height: 0;
  flex: 1;
}

.resource-panel {
  display: flex;
  flex-direction: column;
}

.resource-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
}

.resource-list__item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.resource-list__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 6px;
  flex-shrink: 0;
  font-size: 20px;
}

.resource-list__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 9px;
}

.resource-list__row span {
  color: #3d4e69;
  font-size: 15px;
  font-weight: 800;
}

.resource-list__row strong {
  color: #162646;
  font-size: 15px;
  font-weight: 900;
}

.resource-list__row em {
  font-style: normal;
}

.resource-list__bar {
  height: 8px;
  border-radius: 6px;
  background: #edf2f8;
  overflow: hidden;
}

.resource-list__bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.resource-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  color: #8492a8;
  font-size: 15px;
}

.resource-panel__footer strong {
  color: #13254a;
  font-size: 20px;
  font-weight: 900;
}

.todo-list,
.activity-list {
  display: flex;
  min-height: 0;
  padding-right: 6px;
  flex: 1;
  flex-direction: column;
  overflow-y: auto;
}

.dashboard-empty {
  flex: 1;
}

.todo-list::-webkit-scrollbar,
.activity-list::-webkit-scrollbar {
  width: 6px;
}

.todo-list::-webkit-scrollbar-thumb,
.activity-list::-webkit-scrollbar-thumb {
  border-radius: 6px;
  background: #d8e2f0;
}

.todo-list {
  gap: 18px;
}

.todo-list__item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 18px;
  gap: 16px;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
}

.todo-list__item>.el-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  flex-shrink: 0;
  border-color: transparent;
  font-size: 16px;
  font-weight: 900;
}

.todo-list__item strong,
.activity-list__item strong {
  display: block;
  margin-bottom: 7px;
  color: #142448;
  font-size: 15px;
  font-weight: 900;
}

.todo-list__item>i {
  color: #9aa9bd;
  font-size: 26px;
  font-style: normal;
}

.activity-list {
  gap: 0;
}

.activity-list__item {
  display: grid;
  grid-template-columns: 12px minmax(0, 1fr) 72px;
  gap: 18px;
  align-items: center;
  padding: 8px 8px 14px;
  border-bottom: 1px solid #edf2f8;
  border-radius: 6px;
}

.todo-list__item.is-clickable,
.activity-list__item.is-clickable {
  cursor: pointer;
}

.todo-list__item.is-clickable:hover,
.activity-list__item.is-clickable:hover {
  background: #f7faff;
}

.activity-list__item+.activity-list__item {
  padding-top: 14px;
}

.activity-list__item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.activity-list__item .el-tag {
  width: fit-content;
  border-color: transparent;
}

.activity-list__item time {
  color: #8290a6;
  font-size: 14px;
  text-align: right;
  white-space: nowrap;
}

.activity-list__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.is-blue {
  background: #eaf3ff;
  color: #2d79ff;
}

.is-green {
  background: #e9fbf1;
  color: #24ad65;
}

.is-purple {
  background: #f1edff;
  color: #815cf6;
}

.is-orange {
  background: #fff3e3;
  color: #f29324;
}

.resource-list__bar .is-blue,
.activity-list__dot.is-blue {
  background: #2d79ff;
}

.resource-list__bar .is-purple,
.activity-list__dot.is-purple {
  background: #815cf6;
}

.resource-list__bar .is-green,
.activity-list__dot.is-green {
  background: #24ad65;
}

.resource-list__bar .is-orange,
.activity-list__dot.is-orange {
  background: #f29324;
}

@media (max-width: 1180px) {
  .admin-dashboard-page {
    height: auto;
    overflow: visible;
  }

  .metric-grid,
  .dashboard-main-grid,
  .dashboard-sub-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 780px) {

  .metric-grid,
  .dashboard-main-grid,
  .dashboard-sub-grid {
    grid-template-columns: 1fr;
  }

  .metric-card {
    height: auto;
    padding: 18px;
  }

  .dashboard-panel {
    padding: 18px;
  }

  .panel-header,
  .activity-list__item {
    align-items: flex-start;
    flex-direction: column;
  }

  .trend-chart {
    min-height: 260px;
  }
}
</style>
