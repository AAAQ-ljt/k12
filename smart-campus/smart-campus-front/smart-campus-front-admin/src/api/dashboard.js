import request from '@/api/request'

const limitPercent = (value) => Math.min(100, Math.max(0, Number(value ?? 0)))

const normalizeMetric = (item = {}) => ({
  key: item.key ?? '',
  label: item.title || '未命名指标',
  value: Number(item.value ?? 0).toLocaleString(),
  trend: item.unit ? `单位：${item.unit}` : '单位：--',
  trendType: 'is-normal',
  iconClass: item.icon ?? 'icon-user',
  theme: item.theme ?? 'is-blue',
})

const normalizeTrend = (item = {}) => ({
  day: item.day ?? '',
  label: item.label ?? '',
  course: Number(item.course ?? 0),
  exam: Number(item.exam ?? 0),
  homework: Number(item.homework ?? 0),
})

const normalizeResource = (item = {}) => {
  const percent = limitPercent(item.percent)
  return {
    typeKey: item.typeKey ?? '',
    label: item.typeName || '其他资源',
    value: Number(item.count ?? 0).toLocaleString(),
    ratio: `${percent}%`,
    percent,
    theme: item.theme ?? 'is-blue',
    iconClass: item.icon ?? 'icon-attachment',
  }
}

const normalizeTodo = (item = {}) => ({
  key: item.key ?? '',
  tag: item.tag || '待',
  title: item.title || '待办事项',
  desc: item.desc || '暂无待办说明',
  count: Number(item.count ?? 0),
  theme: item.theme ?? 'is-blue',
  routePath: item.routePath ?? '',
})

const normalizeActivity = (item = {}) => ({
  id: item.id ?? '',
  title: item.title || '平台动态',
  desc: item.desc || '动态更新',
  time: item.time || '--',
  theme: item.theme ?? 'is-blue',
  routePath: item.routePath ?? '',
})

export const normalizeDashboard = (data = {}) => ({
  metricCards: Array.isArray(data.metricCards) ? data.metricCards.map(normalizeMetric) : [],
  teachingTrend: Array.isArray(data.teachingTrend) ? data.teachingTrend.map(normalizeTrend) : [],
  resourceStats: Array.isArray(data.resourceStats) ? data.resourceStats.map(normalizeResource) : [],
  todoList: Array.isArray(data.todoList) ? data.todoList.map(normalizeTodo) : [],
  activityList: Array.isArray(data.activityList) ? data.activityList.map(normalizeActivity) : [],
  totalResourceCount: Number(data.totalResourceCount ?? 0),
  storageUsagePercent: limitPercent(data.storageUsagePercent),
})

export const loadDashboard = async () => {
  const result = await request.post('/dashboard/loadDashboard', {}, { showLoading: false })
  return normalizeDashboard(result ?? {})
}
