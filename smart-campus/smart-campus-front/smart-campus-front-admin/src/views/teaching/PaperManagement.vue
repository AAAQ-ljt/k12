<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="试卷信息">
              <el-input v-model="filters.keyword" placeholder="请输入试卷名称" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="试卷类型">
              <el-select v-model="filters.paperType" placeholder="请选择试卷类型" clearable filterable @change="handleSearch">
                <el-option v-for="item in PAPER_TYPE_OPTIONS" :key="item.value" :label="item.label"
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
            <el-button type="primary" @click="handleCreate">新增试卷</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
              批量删除
            </el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" :row-height="108" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-paperInfo="{ row }">
        <div class="info-cell">
          <strong :title="row.paperName">{{ row.paperName || '-' }}</strong>
          <span>{{ row.paperTypeText || '-' }}</span>
        </div>
      </template>

      <template #cell-structureInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.sectionCount || 0 }} 个分组 / {{ row.questionCount || 0 }} 道题</strong>
          <span>总分 {{ row.totalScore ?? 0 }} 分</span>
        </div>
      </template>

      <template #cell-durationInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.paperTypeText || '-' }}</strong>
          <span>{{ row.updateTime || '-' }}</span>
        </div>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button link type="primary" @click.stop="handleEditStructure(row)">
            编排试卷
          </el-button>
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

    <PaperFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :readonly="dialogMode === 'view'" @submit="handleSubmit" />

    <PaperEditorDrawer v-model:show="editorVisible" :paper-id="currentEditorPaperId" @close="handleEditorClose"
      @saved="handleEditorSaved" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import {
  deletePapers,
  getPaperDetail,
  getPaperList,
  PAPER_TYPE_OPTIONS,
  savePaper,
} from '@/api/paper'
import PaperEditorDrawer from '@/views/teaching/components/PaperEditorDrawer.vue'
import PaperFormDialog from '@/views/teaching/components/PaperFormDialog.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const editorVisible = ref(false)
const currentEditorPaperId = ref('')
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })

const filters = reactive({
  keyword: '',
  paperType: undefined,
})

const columns = [
  {
    key: 'paperInfo',
    label: '试卷信息',
    slot: 'cell-paperInfo',
  },
  {
    key: 'structureInfo',
    label: '组卷信息',
    slot: 'cell-structureInfo',
  },
  {
    key: 'durationInfo',
    label: '时长/更新时间',
    slot: 'cell-durationInfo',
  },
  {
    key: 'actions',
    label: '操作',
    width: 260,
    align: 'center',
    slot: 'cell-actions',
    fixed: 'right',
  },
]

const loadTableData = async () => {
  tableData.value =
    (await getPaperList({
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
  const detail = await getPaperDetail(row.paperId)
  if (!detail) {
    return
  }
  dialogMode.value = mode
  currentRecord.value = detail
  dialogVisible.value = true
}

const handleView = (row) => openDetailDialog(row, 'view')
const handleEdit = (row) => openDetailDialog(row, 'edit')

const handleEditStructure = (row) => {
  currentEditorPaperId.value = String(row.paperId || '')
  editorVisible.value = true
}

const handleEditorClose = async () => {
  currentEditorPaperId.value = ''
  await loadTableData()
}

const handleEditorSaved = async () => {
  await loadTableData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除试卷“${row.paperName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deletePapers([row.paperId])
  if (result === null) {
    return
  }
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.paperId
  )
  ElMessage.success('试卷已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 份试卷吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deletePapers(selectedRowKeys.value)
  if (result === null) {
    return
  }
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await savePaper(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '试卷创建成功' : '试卷信息已更新'
  )
  pageNo.value = 1
  await loadTableData()
}

onMounted(loadTableData)
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
