<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="题目信息">
              <el-input v-model="filters.keyword" placeholder="请输入题目标题" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="题目类型">
              <el-select v-model="filters.questionType" placeholder="请选择题目类型" clearable filterable
                @change="handleSearch">
                <el-option v-for="item in QUESTION_TYPE_OPTIONS" :key="item.value" :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="难度等级">
              <el-select v-model="filters.difficultyLevel" placeholder="请选择难度等级" clearable filterable
                @change="handleSearch">
                <el-option v-for="item in DIFFICULTY_LEVEL_OPTIONS" :key="item.value" :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增题目</el-button>
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
      <template #cell-questionInfo="{ row }">
        <div class="info-cell">
          <strong :title="row.questionTitle">{{ row.questionTitle || '-' }}</strong>
          <span>{{ row.questionTypeText || '-' }}</span>
        </div>
      </template>

      <template #cell-difficultyInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.difficultyLevelText || '-' }}</strong>
          <el-button
            v-if="row.questionType === QUESTION_TYPE_ESSAY"
            link
            type="primary"
            class="answer-link"
            @click.stop="handleView(row)"
          >
            查看答案
          </el-button>
          <span v-else>{{ row.answerDisplayText || '-' }}</span>
        </div>
      </template>

      <template #cell-resourceInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.imageCount || 0 }} 张配图</strong>
          <span>{{ row.optionCount || 0 }} 个选项</span>
        </div>
      </template>

      <template #cell-correctAnswer="{ row }">
        <div class="answer-cell" :title="row.updateTime || '-'">
          {{ row.updateTime || '-' }}
        </div>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button link @click.stop="handleView(row)">查看</el-button>
          <el-button link type="primary" @click.stop="handleEdit(row)">
            编辑
          </el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">
            删除
          </el-button>
        </div>
      </template>
    </BaseDataTable>

    <QuestionFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import {
  deleteQuestions,
  DIFFICULTY_LEVEL_OPTIONS,
  getQuestionDetail,
  getQuestionList,
  QUESTION_TYPE_ESSAY,
  QUESTION_TYPE_OPTIONS,
  saveQuestion,
} from '@/api/question'
import QuestionFormDialog from '@/views/teaching/components/QuestionFormDialog.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })

const filters = reactive({
  keyword: '',
  questionType: undefined,
  difficultyLevel: undefined,
})

const columns = [
  {
    key: 'questionInfo',
    label: '题目信息',
    slot: 'cell-questionInfo',
  },
  {
    key: 'difficultyInfo',
    label: '难度/答案',
    width: 160,
    slot: 'cell-difficultyInfo',
  },
  {
    key: 'resourceInfo',
    label: '配图/选项',
    width: 160,
    slot: 'cell-resourceInfo',
  },
  {
    key: 'correctAnswer',
    label: '更新时间',
    slot: 'cell-correctAnswer',
    width: 180,
  },
  {
    key: 'actions',
    label: '操作',
    width: 170,
    align: 'center',
    slot: 'cell-actions',
    fixed: 'right',
  },
]

const loadTableData = async () => {
  tableData.value =
    (await getQuestionList({
      ...filters,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
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
  const detail = await getQuestionDetail(row.questionId)
  if (!detail) {
    return
  }
  dialogMode.value = mode
  currentRecord.value = detail
  dialogVisible.value = true
}

const handleView = (row) => openDetailDialog(row, 'view')
const handleEdit = (row) => openDetailDialog(row, 'edit')

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除题目“${row.questionTitle}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteQuestions([row.questionId])
  if (result === null) {
    return
  }
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.questionId
  )
  ElMessage.success('题目已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 道题目吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteQuestions(selectedRowKeys.value)
  if (result === null) {
    return
  }
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await saveQuestion(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '题目创建成功' : '题目信息已更新'
  )
  pageNo.value = 1
  await loadTableData()
}

onMounted(loadTableData)
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
<style lang="scss" scoped>
.answer-link {
  padding: 0;
}

.answer-cell {
  display: -webkit-box;
  overflow: hidden;
  color: #4c5f7d;
  line-height: 1.6;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
