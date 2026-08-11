<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="班级信息">
              <el-input v-model="filters.keyword" placeholder="请输入班级名称" clearable />
            </el-form-item>
            <el-form-item label="所属专业">
              <el-select v-model="filters.majorId" placeholder="请选择专业" clearable filterable @change="handleSearch">
                <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
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
            <el-button type="primary" @click="handleCreate">新增班级</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-classInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.className }}</strong>
          <span>{{ row.departmentName || '-' }}</span>
        </div>
      </template>

      <template #cell-majorInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.majorName || '-' }}</strong>
          <span>{{ row.gradeText || '-' }}</span>
        </div>
      </template>

      <template #cell-teacherInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.counselorName || '-' }}</strong>
          <span>班主任：{{ row.headTeacherName || '-' }}</span>
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

    <ClassFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :department-options="departmentOptions" :major-options="majorOptions" :grade-options="gradeOptions"
      :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteClasses,
  getBasicDataOptions,
  getClassList,
  getMajorSortList,
  saveClass,
} from '@/api/basicData'
import BaseDataTable from '@/components/BaseDataTable.vue'
import ClassFormDialog from '@/views/basic-data/components/ClassFormDialog.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const departmentOptions = ref([])
const majorOptions = ref([])
const gradeOptions = ref([])
const statusTextMap = ref({})

const filters = reactive({
  keyword: '',
  majorId: undefined,
  status: undefined,
})

const columns = [
  { key: 'classInfo', label: '班级信息', slot: 'cell-classInfo' },
  { key: 'majorInfo', label: '专业/学制', width: 280, slot: 'cell-majorInfo' },
  {
    key: 'teacherInfo',
    label: '辅导员/班主任',
    width: 320,
    slot: 'cell-teacherInfo',
  },
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
  tableData.value =
    (await getClassList({
      ...filters,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
}

const loadOptions = async () => {
  const [options, majorList] = await Promise.all([
    getBasicDataOptions(),
    getMajorSortList(),
  ])
  if (!options) {
    return
  }
  const majorExtraMap = (majorList || []).reduce((map, item) => {
    map[String(item.majorId)] = item
    return map
  }, {})
  departmentOptions.value = options.departmentOptions || []
  majorOptions.value = (options.majorOptions || []).map((item) => ({
    ...item,
    extra: {
      departmentId: majorExtraMap[String(item.value)]?.departmentId,
      educationalSystemType:
        majorExtraMap[String(item.value)]?.educationalSystemType,
    },
  }))
  gradeOptions.value = options.gradeOptions || []
  statusTextMap.value = options.statusTextMap || {}
}

const getStatusText = (status) =>
  statusTextMap.value?.[status] || (Number(status) === 1 ? '启用' : '停用')
const getStatusClass = (status) =>
  Number(status) === 1 ? 'enabled' : 'disabled'

watch([pageNo, pageSize], loadTableData)

const handleSearch = async () => {
  pageNo.value = 1
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
      `确定删除班级“${row.className}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  await deleteClasses([row.id])
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  ElMessage.success('班级已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 个班级吗？`,
      '批量删除确认',
      {
        type: 'warning',
      }
    )
  } catch {
    return
  }

  await deleteClasses(selectedRowKeys.value)
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await saveClass(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '班级创建成功' : '班级信息已更新'
  )
  pageNo.value = 1
  await loadTableData()
  await loadOptions()
}

onMounted(async () => {
  await loadOptions()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
