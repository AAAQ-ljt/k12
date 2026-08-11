<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="学生信息">
              <el-input v-model="filters.keyword" placeholder="请输入学生姓名或学号" clearable />
            </el-form-item>
            <el-form-item label="班级">
              <el-select v-model="filters.classId" placeholder="请选择班级" clearable filterable @change="handleSearch">
                <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-button type="primary" @click="handleCreate">新增学生</el-button>
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
      <template #cell-studentInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.realName }}{{ row.genderText ? `（${row.genderText}）` : '' }}</strong>
          <span>{{ row.userNo }}</span>
        </div>
      </template>
      <template #cell-majorInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.departmentName || '-' }}</strong>
          <span>{{ row.majorName || '-' }} / {{ row.className || '-' }}</span>
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

    <StudentFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :department-options="departmentOptions" :major-options="majorOptions" :class-options="classOptions"
      :readonly="dialogMode === 'view'" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteStudents,
  getBasicDataOptions,
  getClassSortList,
  getMajorSortList,
  getStudentList,
  saveStudent,
} from '@/api/basicData'
import BaseDataTable from '@/components/BaseDataTable.vue'
import StudentFormDialog from '@/views/basic-data/components/StudentFormDialog.vue'

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
const statusTextMap = ref({})
const filters = reactive({
  keyword: '',
  departmentId: undefined,
  classId: undefined,
  status: undefined,
})

const columns = [
  {
    key: 'studentInfo',
    label: '学生信息',
    slot: 'cell-studentInfo',
  },
  {
    key: 'contactInfo',
    label: '联系方式',
    slot: 'cell-contactInfo',
  },
  {
    key: 'majorInfo',
    label: '院系/专业/班级',
    slot: 'cell-majorInfo',
  },
  {
    key: 'status',
    prop: 'status',
    label: '状态',
    align: 'center',
    slot: 'cell-status',
  },
  {
    key: 'lastLoginTime',
    prop: 'lastLoginTime',
    label: '最后登录时间',
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
    (await getStudentList({
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
      `确定删除学生“${row.realName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteStudents([row.id])
  if (result === null) {
    return
  }
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  ElMessage.success('学生已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除已选中的 ${selectedRowKeys.value.length} 名学生吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const result = await deleteStudents(selectedRowKeys.value)
  if (result === null) {
    return
  }
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleImport = () => ElMessage.info('已预留学生 Excel 导入入口。')
const handleExport = () => ElMessage.success('已预留学生导出接口接入点。')
const handleDownloadTemplate = () =>
  ElMessage.info('已预留学生导入模板下载入口。')

const handleSubmit = async (payload) => {
  const result = await saveStudent(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'create' ? '学生创建成功' : '学生信息已更新'
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
