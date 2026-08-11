<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="教师信息">
              <el-input v-model="filters.keyword" placeholder="请输入教师姓名或工号" clearable />
            </el-form-item>
            <el-form-item label="职称">
              <el-select v-model="filters.titleName" placeholder="请选择职称" clearable filterable @change="handleSearch">
                <el-option v-for="item in titleOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-button type="primary" @click="handleCreate">新增教师</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="success" @click="handleImport">Excel导入</el-button>
            <el-button type="success" @click="handleExport">导出数据</el-button>
            <el-button type="success" @click="handleDownloadTemplate">下载模板</el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-teacherInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.realName }}{{ row.genderText ? `（${row.genderText}）` : '' }}</strong>
          <span>{{ row.userNo }}</span>
        </div>
      </template>
      <template #cell-departmentInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.departmentName || '-' }}</strong>
          <span>{{ row.titleName || '-' }}</span>
        </div>
      </template>
      <template #cell-classInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.className || '-' }}</strong>
        </div>
      </template>
      <template #cell-contactInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.phone || '-' }}</strong>
          <span>{{ row.email || '-' }}</span>
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

    <TeacherFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :department-options="departmentOptions" :major-options="majorOptions" :class-options="classOptions"
      :title-options="titleOptions" :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteTeachers,
  getBasicDataOptions,
  getClassSortList,
  getMajorSortList,
  getTeacherList,
  saveTeacher,
} from '@/api/basicData'
import BaseDataTable from '@/components/BaseDataTable.vue'
import TeacherFormDialog from '@/views/basic-data/components/TeacherFormDialog.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const departmentOptions = ref([])
const majorOptions = ref([])
const classOptions = ref([])
const titleOptions = ref([])
const statusTextMap = ref({})
const filters = reactive({
  keyword: '',
  departmentId: undefined,
  titleName: '',
  status: undefined,
})

const columns = [
  {
    key: 'teacherInfo',
    label: '教师信息',
    width: 220,
    slot: 'cell-teacherInfo',
  },
  {
    key: 'contactInfo',
    label: '联系方式',
    width: 220,
    slot: 'cell-contactInfo',
  },
  {
    key: 'departmentInfo',
    label: '院系/职称',
    width: 180,
    slot: 'cell-departmentInfo',
  },
  { key: 'classInfo', label: '授课班级', slot: 'cell-classInfo' },
  {
    key: 'status',
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    slot: 'cell-status',
  },
  {
    key: 'lastLoginTime',
    prop: 'lastLoginTime',
    label: '最后登录时间',
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
    (await getTeacherList({
      ...filters,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
}

const loadOptions = async () => {
  const [options, majorList, classList] = await Promise.all([
    getBasicDataOptions(),
    getMajorSortList(),
    getClassSortList(),
  ])
  if (!options) return

  const majorExtraMap = (majorList || []).reduce((map, item) => {
    map[String(item.majorId)] = item
    return map
  }, {})
  const classExtraMap = (classList || []).reduce((map, item) => {
    map[String(item.classId)] = item
    return map
  }, {})

  departmentOptions.value = options.departmentOptions || []
  majorOptions.value = (options.majorOptions || []).map((item) => ({
    ...item,
    extra: {
      departmentId: majorExtraMap[String(item.value)]?.departmentId,
    },
  }))
  classOptions.value = (options.classOptions || []).map((item) => ({
    ...item,
    extra: {
      departmentId: classExtraMap[String(item.value)]?.departmentId,
      majorId: classExtraMap[String(item.value)]?.majorId,
    },
  }))
  titleOptions.value = options.teacherTitleOptions || []
  statusTextMap.value = options.statusTextMap || {}
}

const getStatusText = (status) =>
  statusTextMap.value?.[status] || (Number(status) === 1 ? '启用' : '停用')
const getStatusClass = (status) =>
  Number(status) === 1 ? 'enabled' : 'disabled'

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
      `确定删除教师“${row.realName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteTeachers([row.id])
  if (result === null) {
    return
  }
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  ElMessage.success('教师已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 名教师吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteTeachers(selectedRowKeys.value)
  if (result === null) {
    return
  }
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleImport = () => ElMessage.info('已预留教师 Excel 导入入口。')
const handleExport = () => ElMessage.success('已预留教师导出接口接入点。')
const handleDownloadTemplate = () =>
  ElMessage.info('已预留教师导入模板下载入口。')

const handleSubmit = async (payload) => {
  const result = await saveTeacher(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '教师创建成功' : '教师信息已更新'
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
