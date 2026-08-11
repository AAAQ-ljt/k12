<template>
  <BaseDrawer v-model:show="visible" :title="drawerTitle" width="1500px" :padding="10" :buttons="[]"
    :show-cancel="false" @close="handleDrawerClose">
    <div class="basic-page homework-page">
      <section class="course-overview-panel">
        <div class="course-overview-panel__content">
          <div class="course-overview-panel__title">
            <strong>{{ courseDetail.courseName || '学生作业' }}</strong>
            <span>{{ authStore.displayName || courseDetail.teacherName || '-' }}</span>
          </div>
          <div class="course-overview-panel__meta">
            <span>授课班级：{{ selectedClassSummary }}</span>
            <span>章节数：{{ courseTreeData.length }}</span>
            <span>作业课时：{{ homeworkLessonCount }}</span>
          </div>
        </div>
      </section>

      <div class="homework-layout">
        <aside class="homework-layout__aside">
          <div class="structure-panel">
            <div class="structure-panel__header">
              <strong>课程章节与课时</strong>
              <span>{{ courseTreeData.length }} 个章节</span>
            </div>

            <el-empty v-if="!courseTreeData.length" description="当前课程暂无章节数据" :image-size="72" />

            <el-tree v-else ref="treeRef" :data="courseTreeData" node-key="id" highlight-current default-expand-all
              :expand-on-click-node="false" :current-node-key="selectedNode.id" @node-click="handleNodeSelect">
              <template #default="{ data }">
                <div :class="[
                    'structure-node',
                    `structure-node--${data.type}`,
                    {
                      'structure-node--disabled': data.disabled,
                    },
                  ]">
                  <div class="structure-node__main">
                    <span class="structure-node__title">{{ data.label }}</span>
                    <span v-if="data.type === 'lesson'" class="structure-node__meta">
                      {{ data.paperName || '未配置作业' }}
                    </span>
                  </div>
                  <span v-if="data.type === 'chapter'" class="structure-node__badge">
                    {{ data.homeworkCount }} 个作业
                  </span>
                </div>
              </template>
            </el-tree>
          </div>
        </aside>

        <section class="homework-layout__main">
          <section class="toolbar-panel">
            <div class="toolbar-panel__top toolbar-panel__top--compact">
              <div class="toolbar-panel__filters">
                <el-form :model="filters" inline label-width="68px">
                  <el-form-item label="班级">
                    <el-select v-model="filters.classId" placeholder="请选择班级" clearable filterable
                      @change="handleFilterChange">
                      <el-option v-for="item in classOptions" :key="item.value" :label="item.label"
                        :value="item.value" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="学生姓名">
                    <el-input v-model="filters.keyword" placeholder="请输入学生姓名或学号" clearable
                      @keyup.enter="handleSearch" />
                  </el-form-item>
                  <el-form-item label="提交状态">
                    <el-select v-model="filters.submitStatus" placeholder="请选择提交状态" clearable filterable
                      @change="handleFilterChange">
                      <el-option label="待开始" :value="0" />
                      <el-option label="作答中" :value="1" />
                      <el-option label="草稿" :value="2" />
                      <el-option label="已提交" :value="3" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="批改状态">
                    <el-select v-model="filters.judgeStatus" placeholder="请选择批改状态" clearable filterable
                      @change="handleFilterChange">
                      <el-option label="未批改" :value="0" />
                      <el-option label="自动判分完成" :value="1" />
                      <el-option label="待人工批改" :value="2" />
                      <el-option label="人工批改完成" :value="3" />
                    </el-select>
                  </el-form-item>
                  <el-form-item class="toolbar-panel__search-actions">
                    <el-button type="primary" @click="handleSearch">搜索</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </section>

          <section class="result-summary-panel">
            <div class="result-summary-panel__title">
              <strong>{{ selectedNode.title || '请选择课时' }}</strong>
              <span>{{ selectedNode.subtitle || '左侧展开章节后，点击课时查看班级学生作业完成情况' }}</span>
            </div>
            <div class="result-summary-panel__meta">
              <span>当前层级：课时</span>
              <span>筛选班级：{{ selectedClassSummary }}</span>
              <span>结果数：{{ tableData.totalCount || 0 }}</span>
            </div>
          </section>

          <el-alert v-if="initialized && !selectedNode.lessonId" title="请先在左侧展开章节并选择一个已配置作业的课时" type="info"
            :closable="false" class="homework-alert" />

          <el-alert v-else-if="initialized && selectedNode.disabled" title="当前课时未配置课后作业，请选择其他节点" type="info"
            :closable="false" class="homework-alert" />

          <BaseDataTable :columns="columns" :data="tableData" :header-height="40" @update:pageNo="pageNo = $event"
            @update:pageSize="pageSize = $event">
            <template #cell-studentInfo="{ row }">
              <div class="info-cell">
                <strong>{{ row.studentName || '-' }}</strong>
                <span>{{ row.studentNo || '-' }}</span>
              </div>
            </template>

            <template #cell-submitStatus="{ row }">
              <span :class="['status-tag', getSubmitStatusClass(row.submitStatus)]">
                {{ row.submitStatusText || '-' }}
              </span>
            </template>

            <template #cell-judgeStatus="{ row }">
              <span :class="['status-tag', getJudgeStatusClass(row.judgeStatus)]">
                {{ row.judgeStatusText || '-' }}
              </span>
            </template>

            <template #cell-scoreInfo="{ row }">
              <div class="info-cell">
                <strong>{{ formatScore(row.finalScore) }}</strong>
                <span>
                  客观 {{ formatScore(row.objectiveScore) }} / 主观
                  {{ formatScore(row.subjectiveScore) }}
                </span>
              </div>
            </template>

            <template #cell-submitTime="{ row }">
              <div class="info-cell">
                <strong>{{ row.submitTime || '-' }}</strong>
                <span>用时：{{ formatDuration(row.usedSeconds) }}</span>
              </div>
            </template>

            <template #cell-actions="{ row }">
              <div class="action-group">
                <el-button link @click.stop="openReviewDialog(row, 'view')">查看</el-button>
                <el-button v-if="Number(row.submitStatus) === 3" link type="primary"
                  @click.stop="openReviewDialog(row, 'judge')">
                  批改
                </el-button>
              </div>
            </template>
          </BaseDataTable>
        </section>
      </div>

      <CourseHomeworkReviewDialog v-model:show="reviewDialogVisible" :course-id="reviewRecord.courseId"
        :lesson-id="reviewRecord.lessonId" :student-id="reviewRecord.studentId" :mode="reviewMode"
        @saved="handleReviewSaved" />
    </div>
  </BaseDrawer>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BaseDrawer from '@/components/BaseDrawer.vue'
import BaseDataTable from '@/components/BaseDataTable.vue'
import { useAuthStore } from '@/stores/auth'
import { getClassSortList } from '@/api/basicData'
import { getCourseDetail, getCourseHomeworkSubmitList } from '@/api/course'
import CourseHomeworkReviewDialog from '@/views/teaching/components/CourseHomeworkReviewDialog.vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: true,
  },
  courseId: {
    type: String,
    default: '',
  },
  classId: {
    type: [String, Number],
    default: '',
  },
})
const emit = defineEmits(['update:show'])

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const innerVisible = ref(props.show)
const visible = computed({
  get: () => innerVisible.value,
  set: (value) => {
    innerVisible.value = value
    emit('update:show', value)
  },
})
const drawerTitle = computed(() =>
  courseDetail.value.courseName
    ? `学生作业 - ${courseDetail.value.courseName}`
    : '学生作业'
)

const treeRef = ref()
const pageNo = ref(1)
const pageSize = ref(15)
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const courseDetail = ref({})
const classOptions = ref([])
const courseTreeData = ref([])
const initialized = ref(false)
const reviewDialogVisible = ref(false)
const reviewMode = ref('view')
const reviewRecord = ref({
  courseId: '',
  lessonId: '',
  studentId: undefined,
})

const selectedNode = reactive({
  id: '',
  type: 'lesson',
  chapterId: '',
  lessonId: '',
  title: '',
  subtitle: '',
  disabled: false,
})

const resolvedCourseId = computed(() =>
  String(props.courseId || route.query.courseId || '').trim()
)

const resolvedClassId = computed(() => {
  if (props.classId !== '' && props.classId != null) {
    return Number(props.classId)
  }
  return Number(route.query.classId || 0)
})

const filters = reactive({
  classId: undefined,
  keyword: '',
  submitStatus: undefined,
  judgeStatus: undefined,
})

const columns = [
  {
    key: 'studentInfo',
    label: '学生信息',
    slot: 'cell-studentInfo',
  },
  {
    key: 'submitStatus',
    label: '提交状态',
    width: 120,
    align: 'center',
    slot: 'cell-submitStatus',
  },
  {
    key: 'judgeStatus',
    label: '批改状态',
    width: 130,
    align: 'center',
    slot: 'cell-judgeStatus',
  },
  {
    key: 'scoreInfo',
    label: '得分情况',
    width: 170,
    slot: 'cell-scoreInfo',
  },
  {
    key: 'submitTime',
    label: '提交时间',
    width: 220,
    slot: 'cell-submitTime',
  },
  {
    key: 'actions',
    label: '操作',
    width: 150,
    align: 'center',
    slot: 'cell-actions',
    fixed: 'right',
  },
]

const selectedClassSummary = computed(() => {
  const current = classOptions.value.find(
    (item) => Number(item.value) === Number(filters.classId)
  )
  return current?.label || '暂无'
})

const homeworkLessonCount = computed(() =>
  courseTreeData.value.reduce(
    (count, chapter) => count + Number(chapter.homeworkCount || 0),
    0
  )
)

const buildClassOptions = (detail = {}, allClassOptions = []) => {
  const classIdSet = new Set(
    Array.isArray(detail.classIdList)
      ? detail.classIdList.map((item) => Number(item))
      : []
  )
  return allClassOptions.filter((item) => classIdSet.has(Number(item.value)))
}

const buildCourseTreeData = (detail = {}) =>
  (detail.chapterList || []).map((chapter) => {
    const lessonList = (chapter.lessonList || []).map((lesson) => {
      const hasHomework = Boolean(
        lesson.paperId && Number(lesson.paperType) === 1
      )
      return {
        id: `lesson-${lesson.lessonId}`,
        type: 'lesson',
        label: lesson.lessonName,
        chapterId: chapter.chapterId,
        lessonId: lesson.lessonId,
        paperName: hasHomework ? lesson.paperName : '',
        disabled: !hasHomework,
        homeworkCount: 0,
      }
    })
    return {
      id: `chapter-${chapter.chapterId}`,
      type: 'chapter',
      label: chapter.chapterName,
      chapterId: chapter.chapterId,
      lessonId: '',
      disabled: lessonList.every((item) => item.disabled),
      homeworkCount: lessonList.filter((item) => !item.disabled).length,
      children: lessonList,
    }
  })

const findInitialNode = () => {
  const queryLessonId = String(route.query.lessonId || '')

  if (queryLessonId) {
    for (const chapter of courseTreeData.value) {
      const targetLesson = (chapter.children || []).find(
        (item) => item.lessonId === queryLessonId && !item.disabled
      )
      if (targetLesson) {
        return targetLesson
      }
    }
  }

  for (const chapter of courseTreeData.value) {
    const firstLesson = (chapter.children || []).find((item) => !item.disabled)
    if (firstLesson) {
      return firstLesson
    }
  }

  return null
}

const applySelectedNode = (node) => {
  if (!node) {
    selectedNode.id = ''
    selectedNode.type = 'lesson'
    selectedNode.chapterId = ''
    selectedNode.lessonId = ''
    selectedNode.title = ''
    selectedNode.subtitle = ''
    selectedNode.disabled = false
    return
  }
  selectedNode.id = node.id
  selectedNode.type = node.type
  selectedNode.chapterId = node.chapterId || ''
  selectedNode.lessonId = node.lessonId || ''
  selectedNode.disabled = Boolean(node.disabled)
  selectedNode.title = node.label
  selectedNode.subtitle = node.paperName || '当前课时未配置作业'
}

const loadPageOptions = async () => {
  const courseId = resolvedCourseId.value
  if (!courseId) {
    ElMessage.warning('课程参数缺失')
    return false
  }
  const [detail, classList] = await Promise.all([
    getCourseDetail(courseId),
    getClassSortList({ status: 1 }),
  ])
  if (!detail) {
    return false
  }
  courseDetail.value = detail
  const allClassOptions = (classList || []).map((item) => ({
    value: item.classId,
    label: `${item.className} / ${item.majorName || '-'} / ${
      item.departmentName || '-'
    }`,
  }))
  classOptions.value = buildClassOptions(detail, allClassOptions)
  courseTreeData.value = buildCourseTreeData(detail)
  filters.classId =
    resolvedClassId.value || Number(classOptions.value[0]?.value || undefined)
  applySelectedNode(findInitialNode())
  return true
}

const loadTableData = async () => {
  if (
    !initialized.value ||
    !filters.classId ||
    !selectedNode.lessonId ||
    selectedNode.disabled
  ) {
    tableData.value = {
      totalCount: 0,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      list: [],
    }
    return
  }

  tableData.value =
    (await getCourseHomeworkSubmitList({
      courseId: courseDetail.value.courseId,
      classId: filters.classId,
      chapterId: selectedNode.chapterId,
      lessonId: selectedNode.lessonId,
      keyword: filters.keyword,
      submitStatus: filters.submitStatus,
      judgeStatus: filters.judgeStatus,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
}

const handleSearch = async () => {
  pageNo.value = 1
  await loadTableData()
}

const handleFilterChange = async () => {
  await handleSearch()
}

const handleNodeSelect = async (node, treeNode) => {
  if (node.type === 'chapter') {
    if (treeNode.expanded) {
      treeNode.collapse()
    } else {
      treeNode.expand()
    }
    await nextTick()
    treeRef.value?.setCurrentKey(selectedNode.id || null)
    return
  }
  applySelectedNode(node)
  pageNo.value = 1
  await loadTableData()
}

const getSubmitStatusClass = (status) => {
  if (Number(status) === 3) {
    return 'enabled'
  }
  if (Number(status) === 2) {
    return 'warning'
  }
  if (Number(status) === 1) {
    return 'processing'
  }
  return 'disabled'
}

const getJudgeStatusClass = (status) => {
  if (Number(status) === 3) {
    return 'enabled'
  }
  if (Number(status) === 2) {
    return 'danger'
  }
  if (Number(status) === 1) {
    return 'processing'
  }
  return 'disabled'
}

const formatScore = (value) => Number(value ?? 0).toFixed(2)

const formatDuration = (seconds) => {
  const total = Number(seconds || 0)
  const minute = Math.floor(total / 60)
  const second = total % 60
  if (minute > 0) {
    return `${minute}分${second}秒`
  }
  return `${second}秒`
}

const openReviewDialog = (row, mode) => {
  reviewMode.value = mode
  reviewRecord.value = {
    courseId: courseDetail.value.courseId,
    lessonId: row.lessonId,
    studentId: row.studentId,
  }
  reviewDialogVisible.value = true
}

const handleReviewSaved = async () => {
  await loadTableData()
}

const handleDrawerClose = () => {
  if (route.name === 'teachingCourseHomework') {
    router.push({ name: 'teachingCourse' })
  }
}

watch([pageNo, pageSize], loadTableData)
watch(
  () => props.show,
  (value) => {
    innerVisible.value = value
  }
)

const initializePage = async () => {
  const success = await loadPageOptions()
  initialized.value = success
  if (!success) {
    return
  }
  await loadTableData()
}

onMounted(async () => {
  await initializePage()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
<style lang="scss" scoped>
.homework-page {
  gap: 14px;
}

.course-overview-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid #e7edf7;
  border-radius: 10px;
  background: linear-gradient(135deg, #f7faff 0%, #ffffff 100%);
}

.course-overview-panel__title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
  color: #24304a;
}

.course-overview-panel__title strong {
  font-size: 18px;
}

.course-overview-panel__title span {
  color: #6f7f99;
  font-size: 13px;
}

.course-overview-panel__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: #5f708d;
  font-size: 13px;
}

.homework-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 14px;
  min-height: 0;
}

.homework-layout__aside,
.homework-layout__main {
  min-width: 0;
}

.homework-layout__main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.structure-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  padding: 16px;
  border: 1px solid #dde6f5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
  box-shadow: 0 18px 36px rgba(30, 49, 86, 0.06);
}

.structure-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #24304a;
}

.structure-panel__header strong {
  font-size: 15px;
}

.structure-panel__header span {
  color: #7c8aa4;
  font-size: 12px;
}

.structure-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 6px 0;
}

.structure-node__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 3px;
}

.structure-node__title {
  color: #24304a;
  font-size: 13px;
  font-weight: 600;
}

.structure-node__meta {
  color: #7d8ca8;
  font-size: 12px;
}

.structure-node__badge {
  flex: 0 0 auto;
  padding: 2px 8px;
  border-radius: 999px;
  background: #edf3ff;
  color: #4b6bff;
  font-size: 12px;
  font-weight: 600;
}

.structure-node--disabled .structure-node__title,
.structure-node--disabled .structure-node__meta {
  color: #a7b3c7;
}

.result-summary-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid #e7edf7;
  border-radius: 10px;
  background: #fff;
}

.result-summary-panel__title {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.result-summary-panel__title strong {
  color: #24304a;
  font-size: 16px;
}

.result-summary-panel__title span {
  color: #7a8aa5;
  font-size: 13px;
}

.result-summary-panel__meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px 18px;
  color: #5f708d;
  font-size: 13px;
}

.homework-alert {
  margin-bottom: 0;
}

.toolbar-panel__top--compact {
  align-items: flex-start;
}

:deep(.el-tree) {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 6px 0;
  border-radius: 8px;
}

:deep(.el-tree-node:focus > .el-tree-node__content),
:deep(.el-tree-node__content:hover) {
  background: #f4f7fe;
}

:deep(
    .el-tree--highlight-current
      .el-tree-node.is-current
      > .el-tree-node__content
  ) {
  background: #edf4ff;
}

:deep(.status-tag.warning) {
  color: #c27d18;
  background: #fff5e8;
}

:deep(.status-tag.danger) {
  color: #d84a4a;
  background: #fff0f0;
}

@media (max-width: 1200px) {
  .homework-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .course-overview-panel,
  .result-summary-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-summary-panel__meta {
    justify-content: flex-start;
  }
}
</style>
