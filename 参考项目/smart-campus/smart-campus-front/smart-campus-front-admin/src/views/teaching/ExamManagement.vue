<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="考试信息">
              <el-input v-model="filters.keyword" placeholder="请输入考试名称" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="所属课程">
              <el-select v-model="filters.courseId" placeholder="请选择课程" clearable filterable @change="handleSearch">
                <el-option v-for="item in courseOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filters.status" placeholder="请选择状态" clearable filterable @change="handleSearch">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增考试</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
              批量删除
            </el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-examInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.examName || '-' }}</strong>
          <span>{{ row.courseName || '-' }}</span>
        </div>
      </template>

      <template #cell-paperInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.paperName || '-' }}</strong>
          <span>{{ formatExamDuration(row.startTime, row.endTime) }}</span>
        </div>
      </template>

      <template #cell-classInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.classIdList?.length || 0 }} 个班级</strong>
          <span :title="row.classNames || '-'">{{ row.classNames || '-' }}</span>
        </div>
      </template>

      <template #cell-timeInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.startTime || '-' }}</strong>
          <span>{{ row.endTime || '-' }}</span>
        </div>
      </template>

      <template #cell-status="{ row }">
        <span :class="['status-tag', Number(row.status) === 1 ? 'enabled' : 'warning']">
          {{ row.statusText || '-' }}
        </span>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button v-if="Number(row.status) === 0" link type="success" @click.stop="handlePublish(row)">
            发布
          </el-button>
          <el-button link @click.stop="handleView(row)">查看</el-button>
          <el-button v-if="Number(row.status) === 1" link type="primary" @click.stop="handleReview(row)">
            批改
          </el-button>
          <el-button link type="primary" @click.stop="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
        </div>
      </template>
    </BaseDataTable>

    <ExamFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :readonly="dialogMode === 'view'" :course-options="courseOptions" :paper-options="paperOptions"
      :class-options="classOptions" @submit="handleSubmit" />

    <ExamPaperReviewDrawer v-model:show="reviewDrawerVisible" :exam="reviewExamRecord" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import { getClassSortList } from '@/api/basicData'
import { getCourseList } from '@/api/course'
import { getPaperList } from '@/api/paper'
import {
  deleteExams,
  getExamDetail,
  getExamList,
  publishExam,
  saveExam,
} from '@/api/exam'
import ExamFormDialog from '@/views/teaching/components/ExamFormDialog.vue'
import ExamPaperReviewDrawer from '@/views/teaching/components/ExamPaperReviewDrawer.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const reviewDrawerVisible = ref(false)
const reviewExamRecord = ref({})
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const classOptions = ref([])
const courseOptions = ref([])
const paperOptions = ref([])

const filters = reactive({
  keyword: '',
  courseId: undefined,
  status: undefined,
})

const columns = [
  { key: 'examInfo', label: '考试信息', slot: 'cell-examInfo' },
  { key: 'paperInfo', label: '试卷信息', slot: 'cell-paperInfo', width: 180 },
  { key: 'classInfo', label: '考试班级', slot: 'cell-classInfo' },
  { key: 'timeInfo', label: '考试时间', slot: 'cell-timeInfo', width: 320 },
  {
    key: 'status',
    label: '状态',
    slot: 'cell-status',
    width: 100,
    align: 'center',
  },
  {
    key: 'actions',
    label: '操作',
    slot: 'cell-actions',
    width: 300,
    align: 'center',
    fixed: 'right',
  },
]

const loadTableData = async () => {
  tableData.value =
    (await getExamList({
      ...filters,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
}

const loadOptions = async () => {
  const [classList, coursePage, paperPage] = await Promise.all([
    getClassSortList({ status: 1 }),
    getCourseList({ pageNo: 1, pageSize: 1000 }),
    getPaperList({ pageNo: 1, pageSize: 1000, paperType: 2 }),
  ])

  classOptions.value = (classList || []).map((item) => ({
    value: item.classId,
    label: `${item.className} / ${item.majorName || '-'} / ${
      item.departmentName || '-'
    }`,
  }))
  courseOptions.value = (coursePage?.list || []).map((item) => ({
    value: item.courseId,
    label: item.courseName,
  }))
  paperOptions.value = (paperPage?.list || []).map((item) => ({
    value: item.paperId,
    label: item.paperName,
  }))
}

const formatExamDuration = (startTime, endTime) => {
  if (!startTime || !endTime) {
    return '-'
  }
  const start = new Date(startTime)
  const end = new Date(endTime)
  if (
    Number.isNaN(start.getTime()) ||
    Number.isNaN(end.getTime()) ||
    end <= start
  ) {
    return '-'
  }
  const minutes = Math.round((end.getTime() - start.getTime()) / 60000)
  return `${minutes} 分钟`
}

watch([pageNo, pageSize], loadTableData)

const handleSearch = async () => {
  pageNo.value = 1
  await loadTableData()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  currentRecord.value = {}
  dialogVisible.value = true
}

const openDetailDialog = async (row, mode) => {
  const detail = await getExamDetail(row.examId)
  if (!detail) {
    return
  }
  dialogMode.value = mode
  currentRecord.value = detail
  dialogVisible.value = true
}

const handleView = (row) => openDetailDialog(row, 'view')
const handleEdit = (row) => openDetailDialog(row, 'edit')

const handleReview = (row) => {
  reviewExamRecord.value = row
  reviewDrawerVisible.value = true
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定发布考试“${row.examName}”吗？`,
      '发布确认',
      {
        type: 'warning',
      }
    )
  } catch {
    return
  }
  const result = await publishExam(row.examId)
  if (!result) {
    return
  }
  ElMessage.success('考试已发布')
  await loadTableData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除考试“${row.examName}”吗？`,
      '删除确认',
      {
        type: 'warning',
      }
    )
  } catch {
    return
  }
  const result = await deleteExams([row.examId])
  if (result === null) {
    return
  }
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.examId
  )
  ElMessage.success('考试已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 场考试吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteExams(selectedRowKeys.value)
  if (result === null) {
    return
  }
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await saveExam(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '考试创建成功' : '考试信息已更新'
  )
  pageNo.value = 1
  await loadTableData()
}

onMounted(async () => {
  await loadOptions()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
