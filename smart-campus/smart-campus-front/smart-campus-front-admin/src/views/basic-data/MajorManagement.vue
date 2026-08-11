<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="专业信息">
              <el-input v-model="filters.keyword" placeholder="请输入专业名称或编码" clearable />
            </el-form-item>
            <el-form-item label="所属院系">
              <el-select v-model="filters.departmentId" placeholder="请选择院系" clearable filterable @change="handleSearch">
                <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="学制">
              <el-select v-model="filters.educationalSystemType" placeholder="请选择学制" clearable filterable
                @change="handleSearch">
                <el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增专业</el-button>
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
      <template #cell-majorInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.majorName }}</strong>
          <span>{{ row.majorCode }}</span>
        </div>
      </template>

      <template #cell-departmentInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.departmentName || '-' }}</strong>
          <span>{{ row.educationalSystemTypeText || '-' }}</span>
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

    <MajorFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :department-options="departmentOptions" :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteMajors,
  getBasicDataOptions,
  getMajorSortList,
  saveMajor,
  updateMajorSortOrder,
} from '@/api/basicData'
import BaseDataTable from '@/components/BaseDataTable.vue'
import MajorFormDialog from '@/views/basic-data/components/MajorFormDialog.vue'

const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableRows = ref([])
const departmentOptions = ref([])
const gradeOptions = ref([])
const statusTextMap = ref({})
const sorting = ref(false)

const filters = reactive({
  keyword: '',
  departmentId: undefined,
  educationalSystemType: undefined,
})

const columns = [
  { key: 'majorInfo', label: '专业信息', width: 220, slot: 'cell-majorInfo' },
  {
    key: 'departmentInfo',
    label: '院系/学制',
    width: 180,
    slot: 'cell-departmentInfo',
  },
  { key: 'description', prop: 'description', label: '专业简介' },
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
  tableRows.value = (await getMajorSortList(filters)) || []
}

const loadOptions = async () => {
  const options = await getBasicDataOptions()
  if (!options) {
    return
  }
  departmentOptions.value = options.departmentOptions || []
  gradeOptions.value = options.gradeOptions || []
  statusTextMap.value = options.statusTextMap || {}
}

const getStatusText = (status) =>
  statusTextMap.value?.[status] || (Number(status) === 1 ? '启用' : '停用')
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
      `确定删除专业“${row.majorName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  await deleteMajors([row.id])
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  ElMessage.success('专业已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 个专业吗？`,
      '批量删除确认',
      {
        type: 'warning',
      }
    )
  } catch {
    return
  }

  await deleteMajors(selectedRowKeys.value)
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  await saveMajor(payload)
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '专业创建成功' : '专业信息已更新'
  )
  await loadTableData()
  await loadOptions()
}

const handleSortChange = async ({ list }) => {
  if (sorting.value) {
    return
  }

  sorting.value = true
  try {
    const ids = list.map((item) => item.id)
    const result = await updateMajorSortOrder(ids)
    if (result !== null) {
      tableRows.value = list
      ElMessage.success('专业排序已更新')
      await loadTableData()
    }
  } finally {
    sorting.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
