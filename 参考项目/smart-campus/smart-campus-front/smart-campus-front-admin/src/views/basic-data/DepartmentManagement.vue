<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="院系信息">
              <el-input v-model="filters.keyword" placeholder="请输入院系名称或编码" clearable />
            </el-form-item>
            <el-form-item label="负责人">
              <el-input v-model="filters.leaderName" placeholder="请输入负责人姓名" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filters.status" placeholder="请选择状态" clearable filterable @change="handleSearch">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增院系</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
              批量删除
            </el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable draggable :columns="columns" :data="tableRows" :header-height="40" :show-pagination="false" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @row-sort="handleSortChange">
      <template #cell-departmentInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.departmentName }}</strong>
          <span>{{ row.departmentCode }}</span>
        </div>
      </template>

      <template #cell-contactInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.leaderName || '-' }}</strong>
          <span>{{ row.contactPhone || '-' }}</span>
        </div>
      </template>

      <template #cell-status="{ row }">
        <span :class="['status-tag', getStatusClass(row.status)]">{{ getStatusText(row.status) }}</span>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button link @click.stop="handleView(row)">查看</el-button>
          <el-button link type="primary" @click.stop="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
        </div>
      </template>
    </BaseDataTable>

    <DepartmentFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteDepartments,
  getDepartmentSortList,
  saveDepartment,
  updateDepartmentSortOrder,
} from '@/api/basicData'
import BaseDataTable from '@/components/BaseDataTable.vue'
import DepartmentFormDialog from '@/views/basic-data/components/DepartmentFormDialog.vue'

const STATUS_TEXT_MAP = {
  1: '启用',
  0: '停用',
}

const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableRows = ref([])
const sorting = ref(false)

const filters = reactive({
  keyword: '',
  leaderName: '',
  status: undefined,
})

const columns = [
  {
    key: 'departmentInfo',
    label: '院系信息',
    width: 220,
    slot: 'cell-departmentInfo',
  },
  {
    key: 'contactInfo',
    label: '负责人/电话',
    width: 180,
    slot: 'cell-contactInfo',
  },
  {
    key: 'majorCount',
    prop: 'majorCount',
    label: '专业数量',
    width: 100,
    align: 'center',
  },
  { key: 'description', prop: 'description', label: '院系说明' },
  {
    key: 'status',
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    slot: 'cell-status',
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
  tableRows.value = (await getDepartmentSortList(filters)) || []
}

const getStatusText = (status) =>
  STATUS_TEXT_MAP[status] || (Number(status) === 1 ? '启用' : '停用')
const getStatusClass = (status) =>
  Number(status) === 1 ? 'enabled' : 'disabled'

const handleSearch = async () => {
  selectedRowKeys.value = []
  await loadTableData()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  currentRecord.value = {}
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogMode.value = 'view'
  currentRecord.value = { ...row }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  currentRecord.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除院系“${row.departmentName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  await deleteDepartments([row.id])
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  ElMessage.success('院系已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 个院系吗？`,
      '批量删除确认',
      {
        type: 'warning',
      }
    )
  } catch {
    return
  }

  await deleteDepartments(selectedRowKeys.value)
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  await saveDepartment(payload)
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '院系创建成功' : '院系信息已更新'
  )
  await loadTableData()
}

const handleSortChange = async ({ list }) => {
  if (sorting.value) {
    return
  }

  sorting.value = true
  try {
    const ids = list.map((item) => item.id)
    const result = await updateDepartmentSortOrder(ids)
    if (result !== null) {
      tableRows.value = list
      ElMessage.success('院系排序已更新')
      await loadTableData()
    }
  } finally {
    sorting.value = false
  }
}

onMounted(async () => {
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
