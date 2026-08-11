<template>
  <div :class="['basic-page', 'resource-page', { 'resource-page--embedded': selectorMode }]">
    <div class="resource-layout">
      <aside class="resource-sidebar">
        <div class="resource-sidebar__header">
          <strong>文件目录</strong>
          <span>{{ folderTree.length }} 个目录</span>
        </div>

        <div class="resource-sidebar__body">
          <el-tree :data="folderTreeWithRoot" node-key="id" :props="treeProps" :current-node-key="currentFolderId"
            highlight-current default-expand-all :expand-on-click-node="false" @node-click="handleTreeNodeClick">
            <template #default="{ data }">
              <div class="folder-tree-node">
                <img class="folder-tree-node__icon" :src="folderIcon" alt="folder" />
                <span>{{ data.resourceName }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </aside>

      <section class="resource-main">
        <section class="toolbar-panel">
          <div class="toolbar-panel__top">
            <div class="toolbar-panel__filters">
              <el-form :model="filters" inline label-width="72px">
                <el-form-item label="资源名称">
                  <el-input v-model="filters.keyword" placeholder="支持模糊搜索" clearable @keyup.enter="handleSearch" />
                </el-form-item>
                <el-form-item label="类型">
                  <el-select v-model="filters.resourceType" placeholder="请选择类型" clearable filterable
                    :disabled="fixedResourceType != null" @change="handleSearch">
                    <el-option v-for="item in resourceTypeOptions" :key="item.value" :label="item.label"
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
                <el-button type="primary" @click="openCreateFolder">新建目录</el-button>
                <el-button type="warning" :disabled="selectedRows.length === 0" @click="openMoveDialogBySelection">
                  转移文件
                </el-button>
                <el-button type="success" @click="openUploadDialog">上传资源</el-button>
              </el-button-group>
            </div>
          </div>

          <div class="resource-page__pathbar">
            <el-button link :disabled="!canGoUp" @click="goToParentFolder">返回上一级</el-button>
            <el-breadcrumb separator=">">
              <el-breadcrumb-item>
                <a href="javascript:void(0)" @click.prevent="goRoot">全部文件</a>
              </el-breadcrumb-item>
              <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.id">
                <a href="javascript:void(0)" @click.prevent="goToBreadcrumb(item.id)">{{ item.resourceName }}</a>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </section>

        <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
          :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
          @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event" :extendHeight="extendHeight"
          @row-sort="handleSortChange">
          <template #cell-resourceName="{ row }">
            <div class="resource-name-cell" @click="handleRowNameClick(row)">
              <div class="resource-name-cell__icon">
                <img class="resource-name-cell__icon-image" :class="{ 'is-thumbnail': isThumbnailRow(row) }"
                  :src="getResourceThumb(row)" :alt="row.resourceName" />
                <div v-if="isVideoRow(row)" class="resource-name-cell__play">
                  <i class="iconfont icon-play-cover" />
                </div>
              </div>
              <div class="info-cell">
                <strong :title="row.resourceName">{{ row.resourceName }}</strong>
                <span v-if="Number(row.nodeType) === NODE_TYPE.RESOURCE">{{ row.resourceTypeText }}</span>
              </div>
            </div>
          </template>

          <template #cell-status="{ row }">
            <span :class="['status-tag', getStatusClass(row)]">{{ getStatusText(row) }}</span>
          </template>

          <template #cell-fileSize="{ row }">
            <div class="info-cell">
              <strong>{{ row.nodeType === NODE_TYPE.FOLDER ? '-' : formatFileSize(row.fileSize) }}</strong>
              <span>{{ row.fileSuffix ? `.${row.fileSuffix}` : '-' }}</span>
            </div>
          </template>

          <template #cell-timeInfo="{ row }">
            <div class="info-cell">
              <strong>创建时间：{{ row.createTime || '-' }}</strong>
              <span>更新时间：{{ row.updateTime || '-' }}</span>
            </div>
          </template>

          <template #cell-actions="{ row }">
            <div class="action-group">
              <el-button v-if="selectorMode && canSelectResource(row)" link type="primary"
                @click.stop="handleSelectResource(row)">
                选择
              </el-button>
              <el-button v-if="canShowDownload(row)" link type="primary" @click.stop="handleDownload(row)">
                下载
              </el-button>
              <el-button link type="primary" @click.stop="openRenameDialog(row)">重命名</el-button>
              <el-button v-if="row.nodeType === NODE_TYPE.RESOURCE" link type="primary"
                @click.stop="openReuploadDialog(row)">
                重新上传
              </el-button>
              <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </BaseDataTable>
      </section>
    </div>

    <ResourceFolderDialog v-model:show="folderDialogVisible" :mode="folderDialogMode" :model="editingNode"
      @submit="handleFolderSubmit" />

    <ResourceMoveDialog v-model:show="moveDialogVisible" :current-ids="moveTargetIds"
      :current-parent-id="moveDialogParentId" :tree-data="folderTree" @submit="handleMoveSubmit" />

    <ResourceUploadDialog ref="uploadPanelRef" @success="handleUploadSuccess" />
    <ResourcePreviewDialog v-model:show="previewDialogVisible" :resource="previewingResource" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import excelIcon from '@/assets/excel.png'
import exeIcon from '@/assets/exe.png'
import folderIcon from '@/assets/folder.png'
import fileIcon from '@/assets/ic_file2.png'
import othersIcon from '@/assets/others.png'
import pdfIcon from '@/assets/pdf.png'
import pptIcon from '@/assets/ppt1.png'
import txtIcon from '@/assets/txt.png'
import videoIcon from '@/assets/video.png'
import wordIcon from '@/assets/word.png'
import zipIcon from '@/assets/zip.png'
import {
  buildResourceFileUrl,
  canDownloadResource,
  downloadResourceFile,
  isPreviewableImage,
  isPreviewableVideo,
} from '@/utils/resource'
import {
  NODE_TYPE,
  RESOURCE_TYPE_OPTIONS,
  createFolder,
  deleteResources,
  getFolderTree,
  getResourceList,
  moveResource,
  renameResource,
} from '@/api/resource'
import ResourceFolderDialog from '@/views/resource/components/ResourceFolderDialog.vue'
import ResourceMoveDialog from '@/views/resource/components/ResourceMoveDialog.vue'
import ResourcePreviewDialog from '@/views/resource/components/ResourcePreviewDialog.vue'
import ResourceUploadDialog from '@/views/resource/components/ResourceUploadDialog.vue'
import '@/assets/icon/iconfont.css'

const props = defineProps({
  selectorMode: {
    type: Boolean,
    default: false,
  },
  fixedResourceType: {
    type: Number,
    default: undefined,
  },
  extendHeight: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['select-resource', 'selection-resource-change'])

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const selectedRows = ref([])
const currentFolderId = ref(0)
const breadcrumbList = ref([])
const folderTree = ref([])
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const folderDialogVisible = ref(false)
const folderDialogMode = ref('create')
const moveDialogVisible = ref(false)
const editingNode = ref(null)
const uploadPanelRef = ref(null)
const moveTargetIds = ref([])
const previewDialogVisible = ref(false)
const previewingResource = ref(null)

const filters = reactive({
  keyword: '',
  resourceType: props.fixedResourceType,
})

const selectorMode = computed(() => props.selectorMode)
const fixedResourceType = computed(() =>
  props.fixedResourceType == null ? undefined : Number(props.fixedResourceType)
)
const resourceTypeOptions = computed(() => {
  if (fixedResourceType.value == null) {
    return RESOURCE_TYPE_OPTIONS
  }
  return RESOURCE_TYPE_OPTIONS.filter(
    (item) => Number(item.value) === fixedResourceType.value
  )
})
const videoSuffixList = [
  'mp4',
  'avi',
  'mov',
  'mkv',
  'wmv',
  'flv',
  'webm',
  'm3u8',
]
const pdfSuffixList = ['pdf']
const pptSuffixList = ['ppt', 'pptx', 'pps', 'ppsx', 'key']
const wordSuffixList = ['doc', 'docx', 'docm', 'dot', 'dotx']
const excelSuffixList = ['xls', 'xlsx', 'xlsm', 'csv']
const txtSuffixList = ['txt', 'md', 'rtf']
const zipSuffixList = ['zip', 'rar', '7z', 'tar', 'gz']
const exeSuffixList = ['exe', 'msi', 'bat', 'cmd']
const treeProps = { children: 'children', label: 'resourceName' }

const columns = computed(() => [
  { key: 'resourceName', label: '资源名称', slot: 'cell-resourceName', sortable: true },
  {
    key: 'status',
    prop: 'status',
    label: '状态',
    width: 110,
    align: 'center',
    slot: 'cell-status',
  },
  { key: 'fileSize', label: '资源大小', width: 180, slot: 'cell-fileSize' },
  { key: 'timeInfo', label: '时间', width: 260, slot: 'cell-timeInfo' },
  {
    key: 'actions',
    label: '操作',
    width: selectorMode.value ? 330 : 280,
    align: 'left',
    slot: 'cell-actions',
    fixed: 'right',
  },
])

const canGoUp = computed(() => currentFolderId.value !== 0)
const moveDialogParentId = computed(() => {
  if (!selectedRows.value.length) {
    return currentFolderId.value
  }
  const parentIdSet = new Set(
    selectedRows.value.map((item) => Number(item.parentId ?? 0))
  )
  return parentIdSet.size === 1
    ? selectedRows.value[0].parentId ?? 0
    : currentFolderId.value
})
const folderTreeWithRoot = computed(() => [
  {
    id: 0,
    resourceId: 0,
    resourceName: '全部文件',
    children: folderTree.value,
  },
])

const loadTableData = async (options = {}) => {
  const result = await getResourceList(
    {
      parentId: currentFolderId.value,
      keyword: filters.keyword,
      resourceType: filters.resourceType,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    },
    options
  )
  if (result) {
    tableData.value = result
  }
  refreshSelectedRows()
}

const loadFolderTree = async () => {
  const result = await getFolderTree()
  if (result) {
    folderTree.value = result
    syncBreadcrumbByFolderId(currentFolderId.value)
  }
}

const RESOURCE_STATUS_TEXT_MAP = {
  1: '\u4e0a\u4f20\u4e2d',
  2: '\u8f6c\u7801\u4e2d',
  3: '\u4e0a\u4f20\u6210\u529f',
  4: '\u8f6c\u7801\u5931\u8d25',
  5: '\u4e0a\u4f20\u5931\u8d25',
}

function getStatusText(row) {
  if (Number(row.nodeType) === NODE_TYPE.FOLDER) {
    return '-'
  }
  return RESOURCE_STATUS_TEXT_MAP[Number(row.status)] || '-'
}

function getStatusClass(row) {
  if (Number(row.nodeType) === NODE_TYPE.FOLDER) {
    return 'neutral'
  }
  const status = Number(row.status)
  if (status === 1 || status === 2) {
    return 'processing'
  }
  if (status === 3) {
    return 'enabled'
  }
  return 'disabled'
}

function getResourceIcon(row) {
  if (Number(row.nodeType) === NODE_TYPE.FOLDER) {
    return folderIcon
  }

  const fileSuffix = String(row.fileSuffix || '').toLowerCase()
  const resourceType = Number(row.resourceType)

  if (resourceType === 1 || videoSuffixList.includes(fileSuffix)) {
    return videoIcon
  }
  if (pdfSuffixList.includes(fileSuffix)) {
    return pdfIcon
  }
  if (pptSuffixList.includes(fileSuffix)) {
    return pptIcon
  }
  if (wordSuffixList.includes(fileSuffix)) {
    return wordIcon
  }
  if (excelSuffixList.includes(fileSuffix)) {
    return excelIcon
  }
  if (txtSuffixList.includes(fileSuffix)) {
    return txtIcon
  }
  if (resourceType === 4 || zipSuffixList.includes(fileSuffix)) {
    return zipIcon
  }
  if (exeSuffixList.includes(fileSuffix)) {
    return exeIcon
  }
  if (resourceType === 3) {
    return fileIcon
  }
  return othersIcon
}

function getResourceThumb(row) {
  if (Number(row.nodeType) === NODE_TYPE.FOLDER) {
    return folderIcon
  }
  if (row.coverPath) {
    return buildResourceFileUrl(row.coverPath)
  }
  if (Number(row.resourceType) === 2 && row.filePath) {
    return buildResourceFileUrl(row.filePath)
  }
  return getResourceIcon(row)
}

function isThumbnailRow(row) {
  return (
    Number(row.nodeType) === NODE_TYPE.RESOURCE &&
    !!(row.coverPath || Number(row.resourceType) === 2)
  )
}

function isVideoRow(row) {
  return (
    Number(row.nodeType) === NODE_TYPE.RESOURCE &&
    Number(row.resourceType) === 1
  )
}

function formatFileSize(size = 0) {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let index = 0
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024
    index += 1
  }
  return `${value.toFixed(index === 0 ? 0 : 2)} ${units[index]}`
}

function refreshSelectedRows() {
  selectedRows.value = tableData.value.list.filter((item) =>
    selectedRowKeys.value.includes(item.id)
  )
}

function findFolderPath(treeList, folderId, path = []) {
  for (const item of treeList) {
    const nextPath = [...path, { id: item.id, resourceName: item.resourceName }]
    if (Number(item.id) === Number(folderId)) {
      return nextPath
    }
    if (Array.isArray(item.children) && item.children.length) {
      const childPath = findFolderPath(item.children, folderId, nextPath)
      if (childPath.length) {
        return childPath
      }
    }
  }
  return []
}

function syncBreadcrumbByFolderId(folderId) {
  if (!folderId) {
    breadcrumbList.value = []
    return
  }
  breadcrumbList.value = findFolderPath(folderTree.value, folderId)
}

async function switchFolder(folderId) {
  currentFolderId.value = Number(folderId) || 0
  syncBreadcrumbByFolderId(currentFolderId.value)
  pageNo.value = 1
  selectedRowKeys.value = []
  await loadTableData()
}

watch(selectedRowKeys, refreshSelectedRows)
watch([pageNo, pageSize], loadTableData)
watch(
  selectedRows,
  (rows) => {
    if (!selectorMode.value) {
      return
    }
    emit('selection-resource-change', rows[0] ? { ...rows[0] } : null)
  },
  { deep: true }
)
watch(
  fixedResourceType,
  (value) => {
    filters.resourceType = value
  },
  { immediate: true }
)
watch(
  selectedRowKeys,
  (keys) => {
    if (!selectorMode.value) {
      return
    }
    if (!Array.isArray(keys) || keys.length <= 1) {
      return
    }
    selectedRowKeys.value = [keys[keys.length - 1]]
  },
  { deep: true }
)

function handleTreeNodeClick(node) {
  switchFolder(node.id)
}

function handleRowNameClick(row) {
  if (Number(row.nodeType) === NODE_TYPE.FOLDER) {
    switchFolder(row.id)
    return
  }
  if (canPreview(row)) {
    openPreviewDialog(row)
  }
}

function goRoot() {
  switchFolder(0)
}

function goToParentFolder() {
  if (!canGoUp.value) {
    return
  }
  const parent = breadcrumbList.value[breadcrumbList.value.length - 2]
  switchFolder(parent?.id ?? 0)
}

function goToBreadcrumb(folderId) {
  switchFolder(folderId)
}

async function handleSearch() {
  pageNo.value = 1
  await loadTableData()
}

function handleSortChange({ prop, order }) {
  if (!tableData.value.list || !tableData.value.list.length) {
    return
  }

  const list = [...tableData.value.list]

  if (!order) {
    // 取消排序，恢复原始顺序
    loadTableData()
    return
  }

  // 前端排序
  list.sort((a, b) => {
    const nameA = (a.resourceName || '').toLowerCase()
    const nameB = (b.resourceName || '').toLowerCase()

    if (order === 'ascending') {
      return nameA.localeCompare(nameB, 'zh-CN')
    } else {
      return nameB.localeCompare(nameA, 'zh-CN')
    }
  })

  tableData.value = {
    ...tableData.value,
    list
  }
}

function openCreateFolder() {
  folderDialogMode.value = 'create'
  editingNode.value = { parentId: currentFolderId.value, resourceName: '' }
  folderDialogVisible.value = true
}

function openRenameDialog(row) {
  folderDialogMode.value = 'rename'
  editingNode.value = { ...row }
  folderDialogVisible.value = true
}

async function handleFolderSubmit(payload) {
  const success =
    folderDialogMode.value === 'rename'
      ? await renameResource(payload)
      : await createFolder({ ...payload, parentId: currentFolderId.value })

  if (!success) {
    return
  }

  folderDialogVisible.value = false
  ElMessage.success(
    folderDialogMode.value === 'rename' ? '重命名成功' : '目录创建成功'
  )
  await loadFolderTree()
  await loadTableData()
}

function openMoveDialogBySelection() {
  if (!selectedRows.value.length) {
    return
  }
  moveTargetIds.value = selectedRows.value.map((item) => item.id)
  moveDialogVisible.value = true
}

async function handleMoveSubmit(payload) {
  const moveIds = [...moveTargetIds.value]
  for (const resourceId of moveIds) {
    const success = await moveResource({
      resourceId,
      targetParentId: payload.targetParentId,
    })
    if (!success) {
      return
    }
  }
  moveDialogVisible.value = false
  moveTargetIds.value = []
  selectedRowKeys.value = []
  ElMessage.success('移动成功')
  await loadFolderTree()
  await loadTableData()
}

function openUploadDialog() {
  uploadPanelRef.value?.openFileDialog({
    parentId: currentFolderId.value,
  })
}

function openReuploadDialog(row) {
  uploadPanelRef.value?.openFileDialog({
    parentId: currentFolderId.value,
    replaceTarget: { ...row },
  })
}

async function handleUploadSuccess() {
  await loadFolderTree()
  await loadTableData({ showLoading: false })
}

function canPreview(row) {
  if (Number(row.nodeType) !== NODE_TYPE.RESOURCE) {
    return false
  }
  return isPreviewableVideo(row) || isPreviewableImage(row)
}

function canShowDownload(row) {
  return canDownloadResource(row) && !canPreview(row)
}

function canSelectResource(row) {
  if (Number(row.nodeType) !== NODE_TYPE.RESOURCE) {
    return false
  }
  if (fixedResourceType.value == null) {
    return true
  }
  return Number(row.resourceType) === fixedResourceType.value
}

function handleSelectResource(row) {
  if (selectorMode.value) {
    selectedRowKeys.value = [row.id]
    refreshSelectedRows()
    emit('selection-resource-change', { ...row })
  }
  emit('select-resource', { ...row })
}

function openPreviewDialog(row) {
  previewingResource.value = { ...row }
  previewDialogVisible.value = true
}

function handleDownload(row) {
  downloadResourceFile(row)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除“${row.resourceName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  const success = await deleteResources([row.id])
  if (!success) {
    return
  }
  ElMessage.success('删除成功')
  selectedRowKeys.value = selectedRowKeys.value.filter(
    (item) => item !== row.id
  )
  await loadFolderTree()
  await loadTableData()
}

onMounted(async () => {
  await loadFolderTree()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
<style lang="scss" scoped>
.resource-page {
  min-height: calc(100vh - 120px);
}

.resource-page--embedded {
  height: 100%;
  min-height: 0;
}

.resource-layout {
  display: grid;
  min-height: 0;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 14px;
}

.resource-page--embedded .resource-layout {
  height: 100%;
}

.resource-sidebar {
  display: flex;
  min-height: 0;
  flex-direction: column;
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
  box-shadow: 0 18px 36px rgba(30, 49, 86, 0.06);
  overflow: hidden;
}

.resource-sidebar__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 16px 18px 14px;
  border-bottom: 1px solid #edf2fb;
}

.resource-sidebar__header strong {
  color: #24304a;
  font-size: 15px;
}

.resource-sidebar__header span {
  color: #7c8aa4;
  font-size: 12px;
}

.resource-sidebar__body {
  min-height: 0;
  flex: 1;
  padding: 12px;
  overflow: auto;
}

.resource-main {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  gap: 10px;
}

.resource-page--embedded .resource-main {
  height: 100%;
}

.resource-page__pathbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.folder-tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.folder-tree-node span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-tree-node__icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.resource-name-cell {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
  cursor: pointer;
}

.resource-name-cell__icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  flex: 0 0 52px;
  border-radius: 6px;
  background: #f5f8fd;
  overflow: hidden;
}

.resource-name-cell__icon-image {
  display: block;
  width: 30px;
  height: 30px;
  object-fit: contain;

  &.is-thumbnail {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.resource-name-cell__play {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg,
      rgba(8, 16, 28, 0.08) 0%,
      rgba(8, 16, 28, 0.24) 100%);
  color: #fff;
  pointer-events: none;

  .iconfont {
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.92);
    box-shadow: 0 6px 18px rgba(24, 44, 88, 0.22);
    color: #4a7df2;
    font-size: 25px;
    width: 22px;
    height: 22px;
  }
}

.status-tag.processing {
  background: #eef6ff;
  color: #2d79f3;
}

.status-tag.neutral {
  background: #f4f6fa;
  color: #7c8aa4;
}

:deep(.resource-name-cell .info-cell strong) {
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.resource-sidebar .el-tree) {
  background: transparent;
}

:deep(.resource-sidebar .el-tree-node__content) {
  height: 36px;
  border-radius: 8px;
  color: #44516b;
}

:deep(.resource-sidebar .el-tree-node__content:hover) {
  background: #eef4ff;
}

:deep(.resource-sidebar .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: #e7f0ff;
  color: #2d5fd3;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .resource-layout {
    grid-template-columns: 1fr;
  }

  .resource-sidebar {
    max-height: 280px;
  }
}
</style>
