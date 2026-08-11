<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="公告标题">
              <el-input v-model="filters.keyword" placeholder="请输入公告标题" clearable />
            </el-form-item>
            <el-form-item label="发布范围">
              <el-select v-model="filters.targetType" placeholder="请选择范围" clearable filterable @change="handleSearch">
                <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filters.status" placeholder="请选择状态" clearable filterable @change="handleSearch">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增公告</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-noticeInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.noticeTitle }}</strong>
          <span>{{ row.createUserName || '-' }}</span>
        </div>
      </template>

      <template #cell-targetType="{ row }">
        <span>{{ getTargetTypeText(row.targetType) }}</span>
      </template>

      <template #cell-status="{ row }">
        <span :class="['status-tag', getStatusClass(row.status)]">{{ getStatusText(row.status) }}</span>
      </template>

      <template #cell-isTop="{ row }">
        <el-tag :type="Number(row.isTop) === 1 ? 'warning' : 'info'" effect="light">
          {{ Number(row.isTop) === 1 ? '置顶' : '普通' }}
        </el-tag>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button link @click.stop="handleView(row)">查看</el-button>
          <el-button link type="primary" @click.stop="handleEdit(row)">编辑</el-button>
          <el-button v-if="Number(row.status) !== 1" link type="success" @click.stop="handlePublish(row)">发布</el-button>
          <el-button v-else link type="warning" @click.stop="handleOffline(row)">下线</el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
        </div>
      </template>
    </BaseDataTable>

    <SystemNoticeFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
      :class-options="classOptions" :major-options="majorOptions" :readonly="dialogMode === 'view'"
      @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import { getBasicDataOptions } from '@/api/basicData'
import {
  deleteNotice,
  deleteNotices,
  getNoticeDetail,
  getNoticeList,
  offlineNotice,
  publishNotice,
  saveNotice,
} from '@/api/notice'
import SystemNoticeFormDialog from '@/views/system/components/SystemNoticeFormDialog.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const classOptions = ref([])
const majorOptions = ref([])

const filters = reactive({
  keyword: '',
  targetType: undefined,
  status: undefined,
})

const targetTypeOptions = [
  { label: '全部学生', value: 1 },
  { label: '指定班级', value: 2 },
  { label: '指定专业', value: 3 },
]
const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下线', value: 2 },
]
const columns = [
  { key: 'noticeInfo', label: '公告信息', minWidth: 260, slot: 'cell-noticeInfo' },
  { key: 'targetType', label: '发布范围', width: 120, align: 'left', slot: 'cell-targetType' },
  { key: 'status', label: '状态', width: 110, align: 'left', slot: 'cell-status' },
  { key: 'isTop', label: '置顶', width: 100, align: 'left', slot: 'cell-isTop' },
  { key: 'publishTime', prop: 'publishTime', label: '发布时间', width: 180 },
  { key: 'viewCount', prop: 'viewCount', label: '浏览量', width: 100, align: 'left' },
  { key: 'actions', label: '操作', width: 260, align: 'center', slot: 'cell-actions', fixed: 'right' },
]

const loadTableData = async () => {
  tableData.value = (await getNoticeList({
    ...filters,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
  })) || tableData.value
}

const loadOptions = async () => {
  const options = await getBasicDataOptions()
  classOptions.value = options?.classOptions || []
  majorOptions.value = options?.majorOptions || []
}

const getTargetTypeText = (targetType) =>
  targetTypeOptions.find((item) => Number(item.value) === Number(targetType))?.label || '未知范围'
const getStatusText = (status) =>
  statusOptions.find((item) => Number(item.value) === Number(status))?.label || '草稿'
const getStatusClass = (status) => {
  if (Number(status) === 1) return 'enabled'
  if (Number(status) === 2) return 'disabled'
  return 'warning'
}

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

const loadDetail = async (row) => getNoticeDetail(row.noticeId || row.id)

const handleView = async (row) => {
  const detail = await loadDetail(row)
  if (!detail) return
  dialogMode.value = 'view'
  currentRecord.value = detail
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  const detail = await loadDetail(row)
  if (!detail) return
  dialogMode.value = 'edit'
  currentRecord.value = detail
  dialogVisible.value = true
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(`确定发布公告“${row.noticeTitle}”吗？`, '发布确认', { type: 'warning' })
  } catch {
    return
  }
  await publishNotice(row.noticeId)
  ElMessage.success('公告已发布')
  await loadTableData()
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm(`确定下线公告“${row.noticeTitle}”吗？`, '下线确认', { type: 'warning' })
  } catch {
    return
  }
  await offlineNotice(row.noticeId)
  ElMessage.success('公告已下线')
  await loadTableData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告“${row.noticeTitle}”吗？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteNotice(row.noticeId)
  selectedRowKeys.value = selectedRowKeys.value.filter((item) => item !== row.noticeId)
  ElMessage.success('公告已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除已选中的 ${selectedRowKeys.value.length} 个公告吗？`, '批量删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteNotices(selectedRowKeys.value)
  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await saveNotice(payload)
  if (!result) return
  dialogVisible.value = false
  ElMessage.success(dialogMode.value === 'create' ? '公告创建成功' : '公告信息已更新')
  pageNo.value = 1
  await loadTableData()
}

onMounted(async () => {
  await loadOptions()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
