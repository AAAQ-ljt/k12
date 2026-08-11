<template>
  <BaseDrawer v-model:show="visible" :title="drawerTitle" width="1500px" :padding="10" :buttons="[]"
    :show-cancel="false">
    <div class="basic-page exam-review-page">
      <section class="exam-overview-panel">
        <div class="exam-overview-panel__content">
          <div class="exam-overview-panel__title">
            <strong>{{ exam.examName || '试卷批改' }}</strong>
            <span>{{ exam.courseName || '-' }}</span>
          </div>
          <div class="exam-overview-panel__meta">
            <span>试卷：{{ exam.paperName || '-' }}</span>
            <span>考试班级：{{ classList.length }}</span>
            <span>考试时间：{{ exam.startTime || '-' }} 至 {{ exam.endTime || '-' }}</span>
          </div>
        </div>
      </section>

      <div class="exam-review-layout">
        <aside class="exam-review-layout__aside">
          <div class="class-panel">
            <div class="class-panel__header">
              <strong>考试班级</strong>
              <span>{{ classList.length }} 个班级</span>
            </div>
            <el-empty v-if="!classList.length" description="当前考试暂无班级" :image-size="72" />
            <div v-else class="class-list">
              <button v-for="item in classList" :key="item.classId" type="button" :class="[
                  'class-list__item',
                  { 'class-list__item--active': Number(item.classId) === Number(filters.classId) },
                ]" @click="handleClassSelect(item)">
                <strong>{{ item.className || '-' }}</strong>
                <span>{{ item.majorName || '-' }} / {{ item.departmentName || '-' }}</span>
                <em>{{ item.submittedCount }}/{{ item.studentCount }} 已提交 · {{ item.waitJudgeCount }} 待批改</em>
              </button>
            </div>
          </div>
        </aside>

        <section class="exam-review-layout__main">
          <section class="toolbar-panel">
            <div class="toolbar-panel__top toolbar-panel__top--compact">
              <div class="toolbar-panel__filters">
                <el-form :model="filters" inline label-width="68px">
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
              <strong>{{ selectedClassName }}</strong>
              <span>点击学生可查看答题情况，已提交试卷支持人工批改主观题</span>
            </div>
            <div class="result-summary-panel__meta">
              <span>结果数：{{ tableData.totalCount || 0 }}</span>
              <span>筛选班级：{{ selectedClassName }}</span>
            </div>
          </section>

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
                <span>客观 {{ formatScore(row.objectiveScore) }} / 主观 {{ formatScore(row.subjectiveScore) }}</span>
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

      <ExamPaperReviewDialog v-model:show="reviewDialogVisible" :exam-id="exam.examId"
        :student-id="reviewRecord.studentId" :mode="reviewMode" @saved="handleReviewSaved" />
    </div>
  </BaseDrawer>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseDrawer from '@/components/BaseDrawer.vue'
import BaseDataTable from '@/components/BaseDataTable.vue'
import { getExamSubmitClassList, getExamSubmitList } from '@/api/exam'
import ExamPaperReviewDialog from '@/views/teaching/components/ExamPaperReviewDialog.vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  exam: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:show'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const drawerTitle = computed(() =>
  props.exam?.examName ? `试卷批改 - ${props.exam.examName}` : '试卷批改'
)
const exam = computed(() => props.exam || {})
const pageNo = ref(1)
const pageSize = ref(15)
const classList = ref([])
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const reviewDialogVisible = ref(false)
const reviewMode = ref('view')
const reviewRecord = ref({ studentId: undefined })

const filters = reactive({
  classId: undefined,
  keyword: '',
  submitStatus: undefined,
  judgeStatus: undefined,
})

const columns = [
  { key: 'studentInfo', label: '学生信息', slot: 'cell-studentInfo' },
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
  { key: 'scoreInfo', label: '得分情况', width: 170, slot: 'cell-scoreInfo' },
  { key: 'submitTime', label: '提交时间', width: 220, slot: 'cell-submitTime' },
  {
    key: 'actions',
    label: '操作',
    width: 150,
    align: 'center',
    slot: 'cell-actions',
    fixed: 'right',
  },
]

const selectedClassName = computed(() => {
  const current = classList.value.find(
    (item) => Number(item.classId) === Number(filters.classId)
  )
  return current?.className || '暂无'
})

const resetState = () => {
  classList.value = []
  tableData.value = { totalCount: 0, pageNo: 1, pageSize: 15, list: [] }
  pageNo.value = 1
  filters.classId = undefined
  filters.keyword = ''
  filters.submitStatus = undefined
  filters.judgeStatus = undefined
  reviewDialogVisible.value = false
  reviewRecord.value = { studentId: undefined }
}

const loadClassList = async () => {
  if (!exam.value.examId) {
    return
  }
  classList.value = await getExamSubmitClassList(exam.value.examId)
  filters.classId = Number(classList.value[0]?.classId || undefined)
}

const loadTableData = async () => {
  if (!exam.value.examId || !filters.classId) {
    tableData.value = {
      totalCount: 0,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      list: [],
    }
    return
  }
  tableData.value =
    (await getExamSubmitList({
      examId: exam.value.examId,
      classId: filters.classId,
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

const handleClassSelect = async (item) => {
  filters.classId = item.classId
  await handleSearch()
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
  reviewRecord.value = { studentId: row.studentId }
  reviewDialogVisible.value = true
}

const handleReviewSaved = async () => {
  await loadClassList()
  await loadTableData()
}

watch([pageNo, pageSize], loadTableData)

watch(
  () => props.show,
  async (value) => {
    if (!value) {
      resetState()
      return
    }
    await loadClassList()
    await loadTableData()
  }
)
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>

<style lang="scss" scoped>
.exam-review-page {
  height: calc(100vh - 67px);
}

.exam-overview-panel {
  padding: 14px 18px;
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
}

.exam-overview-panel__title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  strong {
    color: #24304a;
    font-size: 18px;
  }

  span {
    color: #6f7f98;
    font-size: 13px;
  }
}

.exam-overview-panel__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 22px;
  color: #7c8aa4;
  font-size: 13px;
}

.exam-review-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 10px;
  min-height: 0;
  flex: 1;
}

.exam-review-layout__aside,
.exam-review-layout__main,
.class-panel {
  min-height: 0;
}

.exam-review-layout__main,
.class-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.class-panel {
  height: 100%;
  padding: 14px;
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
}

.class-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #24304a;

  span {
    color: #7c8aa4;
    font-size: 12px;
  }
}

.class-list {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.class-list__item {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 5px;
  padding: 12px;
  border: 1px solid #e6edf7;
  border-radius: 6px;
  background: #fbfcff;
  color: #55657f;
  text-align: left;
  cursor: pointer;

  strong {
    color: #24304a;
  }

  span,
  em {
    font-size: 12px;
    font-style: normal;
  }
}

.class-list__item--active {
  border-color: #2d79ff;
  background: #edf4ff;
}

.result-summary-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
}

.result-summary-panel__title {
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    color: #24304a;
  }

  span {
    color: #7c8aa4;
    font-size: 12px;
  }
}

.result-summary-panel__meta {
  display: flex;
  gap: 14px;
  color: #7c8aa4;
  font-size: 12px;
}
</style>
