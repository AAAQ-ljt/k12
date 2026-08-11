<template>
  <div v-show="panelVisible" class="upload-panel">
    <div class="upload-panel__header">
      <strong>{{ headerText }}</strong>
      <div class="upload-panel__actions">
        <button class="upload-panel__clear" type="button" @click="clearTaskList">清空列表</button>
        <button class="upload-panel__icon" type="button" @click="collapsed = !collapsed">
          <i :class="['iconfont', collapsed ? 'icon-max' : 'icon-min']" />
        </button>
        <button class="upload-panel__icon" type="button" @click="panelVisible = false">
          <i class="iconfont icon-close" />
        </button>
      </div>
    </div>

    <div v-show="!collapsed" class="upload-panel__body">
      <div v-if="!taskList.length" class="upload-panel__empty">选择文件后会在这里显示上传进度</div>

      <div v-for="task in taskList" :key="task.uid" class="upload-item">
        <div class="upload-item__row">
          <div class="upload-item__name" :title="task.file.name">{{ task.file.name }}</div>
          <button class="upload-item__delete" type="button" @click="removeTask(task.uid)">
            <i class="iconfont icon-del" />
          </button>
        </div>

        <div class="upload-item__status" :class="`is-${task.status}`">
          <span class="upload-item__dot" />
          <span>{{ task.message }}</span>
        </div>

        <div class="upload-item__progress">
          <div class="upload-item__progress-bar" :style="{ width: `${task.progress}%` }" />
        </div>
      </div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      class="upload-panel__input"
      :multiple="!currentContext.replaceTarget"
      @change="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { initChunkUpload, uploadChunk } from '@/api/resource'
import '@/assets/icon/iconfont.css'

const CHUNK_SIZE = 1 * 1024 * 1024
const MAX_CONCURRENT_UPLOADS = 5

const emit = defineEmits(['success'])

const fileInputRef = ref()
const panelVisible = ref(false)
const collapsed = ref(false)
const taskList = ref([])
const activeWorkerCount = ref(0)
const currentContext = ref({
  parentId: 0,
  replaceTarget: null,
})

const finishedCount = computed(() => taskList.value.filter((item) => item.status === 'success').length)
const totalCount = computed(() => taskList.value.length)
const headerText = computed(() => {
  if (!totalCount.value) {
    return '上传列表'
  }
  if (finishedCount.value === totalCount.value) {
    return `上传完成 ${finishedCount.value}/${totalCount.value}`
  }
  return `正在上传 ${finishedCount.value}/${totalCount.value}`
})

function updateTaskProgress(task, completedChunks, chunkCount, currentChunkPercent = 0) {
  const safeChunkCount = Math.max(Number(chunkCount) || 0, 1)
  const normalizedPercent = Math.max(0, Math.min(Number(currentChunkPercent) || 0, 1))
  const progress = Math.min(
    99,
    Math.max(0, Math.round(((completedChunks + normalizedPercent) / safeChunkCount) * 100)),
  )
  task.progress = progress
  task.message = `上传中 ${progress}%`
}

function inferResourceType(fileName = '') {
  const suffix = fileName.split('.').pop()?.toLowerCase()
  if (['mp4', 'avi', 'mov', 'mkv', 'wmv', 'flv', 'webm', 'm3u8'].includes(suffix)) return 1
  if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(suffix)) return 2
  if (['pdf', 'doc', 'docx', 'txt', 'xls', 'xlsx', 'ppt', 'pptx', 'key'].includes(suffix)) return 3
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(suffix)) return 4
  return 5
}

function buildTask(file) {
  const replaceTarget = currentContext.value.replaceTarget
  return {
    uid: `${Date.now()}_${Math.random().toString(16).slice(2)}`,
    file,
    parentId: currentContext.value.parentId,
    replaceTargetId: replaceTarget?.id ?? null,
    resourceName: replaceTarget?.resourceName || file.name.replace(/\.[^.]+$/, ''),
    resourceType: replaceTarget?.resourceType || inferResourceType(file.name),
    progress: 0,
    status: 'waiting',
    message: '等待上传',
  }
}

function openFileDialog(options = {}) {
  currentContext.value = {
    parentId: options.parentId ?? 0,
    replaceTarget: options.replaceTarget ?? null,
  }
  fileInputRef.value?.click()
}

function handleFileSelect(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) {
    return
  }

  const newTasks = currentContext.value.replaceTarget
    ? [buildTask(files[0])]
    : files.map((file) => buildTask(file))
  taskList.value = currentContext.value.replaceTarget
    ? [
        ...newTasks,
        ...taskList.value.filter(
          (item) => item.status !== 'waiting' && item.status !== 'uploading' && item.status !== 'preparing',
        ),
      ]
    : [...newTasks, ...taskList.value]
  panelVisible.value = true
  collapsed.value = false
  event.target.value = ''
  startUploadQueue()
}

function removeTask(uid) {
  const target = taskList.value.find((item) => item.uid === uid)
  if (target?.status === 'uploading' || target?.status === 'preparing') {
    ElMessage.warning('上传中的文件暂不支持移除')
    return
  }
  taskList.value = taskList.value.filter((item) => item.uid !== uid)
}

function clearTaskList() {
  const hasUploading = taskList.value.some((item) => item.status === 'uploading' || item.status === 'preparing')
  if (hasUploading) {
    ElMessage.warning('请等待当前上传任务完成后再清空')
    return
  }
  taskList.value = []
}

async function uploadSingleTask(task) {
  task.status = 'uploading'
  task.message = '初始化上传中...'

  const uploadId = await initChunkUpload({
    resourceId: task.replaceTargetId,
    parentId: task.parentId,
    resourceName: task.resourceName,
    resourceType: task.resourceType,
    fileName: task.file.name,
    fileSize: task.file.size,
  })

  if (!uploadId) {
    task.status = 'error'
    task.message = '初始化失败'
    return false
  }

  // 初始化成功即代表资源记录已经入库，这里立即刷新右侧列表。
  emit('success')

  const chunkCount = Math.max(Math.ceil(task.file.size / CHUNK_SIZE), 1)
  updateTaskProgress(task, 0, chunkCount)
  for (let chunkIndex = 0; chunkIndex < chunkCount; chunkIndex += 1) {
    const start = chunkIndex * CHUNK_SIZE
    const end = Math.min(task.file.size, start + CHUNK_SIZE)
    const chunkFile = task.file.slice(start, end)

    const chunkResult = await uploadChunk(
      {
        uploadId,
        chunkIndex,
        chunkCount,
        file: chunkFile,
      },
      (event) => {
        const percent = event.total ? event.loaded / event.total : 0
        updateTaskProgress(task, chunkIndex, chunkCount, percent)
      },
    )

    if (!chunkResult && chunkResult !== '') {
      task.status = 'error'
      task.message = '上传失败'
      return false
    }
    updateTaskProgress(task, chunkIndex + 1, chunkCount)
  }

  task.progress = 100
  task.status = 'success'
  task.message = '分片上传完成，后台处理中'
  emit('success')
  window.setTimeout(() => emit('success'), 1200)
  return true
}

async function runWorker() {
  activeWorkerCount.value += 1
  try {
    while (true) {
      const nextTask = taskList.value.find((item) => item.status === 'waiting')
      if (!nextTask) {
        return
      }
      nextTask.status = 'preparing'
      await uploadSingleTask(nextTask)
    }
  } finally {
    activeWorkerCount.value = Math.max(0, activeWorkerCount.value - 1)
    startUploadQueue()
  }
}

function startUploadQueue() {
  const waitingCount = taskList.value.filter((item) => item.status === 'waiting').length
  if (!waitingCount) {
    return
  }
  const availableSlots = Math.max(0, MAX_CONCURRENT_UPLOADS - activeWorkerCount.value)
  const workerCount = Math.min(availableSlots, waitingCount)
  for (let index = 0; index < workerCount; index += 1) {
    runWorker()
  }
}

defineExpose({
  openFileDialog,
})
</script>

<style scoped lang="scss">
.upload-panel {
  position: fixed;
  right: 20px;
  bottom: 92px;
  z-index: 2000;
  display: flex;
  width: 380px;
  max-height: 480px;
  flex-direction: column;
  border: 1px solid #d9d9d9;
  border-radius: 8px 8px 0 0;
  background: #fff;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.16);
  overflow: hidden;
}

.upload-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #f1f1f1;
  color: #1f1f1f;
  font-size: 14px;
}

.upload-panel__header strong {
  font-weight: 600;
}

.upload-panel__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-panel__clear,
.upload-panel__icon,
.upload-item__delete {
  border: none;
  background: transparent;
  cursor: pointer;
}

.upload-panel__clear {
  color: #1677ff;
  font-size: 13px;
}

.upload-panel__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  color: #595959;
}

.upload-panel__icon .iconfont,
.upload-item__delete .iconfont {
  font-size: 16px;
  line-height: 1;
}

.upload-panel__body {
  display: flex;
  flex: 1;
  min-height: 0;
  max-height: 360px;
  flex-direction: column;
  gap: 16px;
  padding: 14px;
  overflow-y: auto;
  overflow-x: hidden;
}

.upload-panel__empty {
  padding: 28px 0;
  color: #8c8c8c;
  text-align: center;
  font-size: 13px;
}

.upload-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.upload-item__row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-item__name {
  flex: 1;
  overflow: hidden;
  color: #1f1f1f;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-item__delete {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #595959;
}

.upload-item__status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.upload-item__status.is-success,
.upload-item__status.is-uploading,
.upload-item__status.is-preparing {
  color: #52c41a;
}

.upload-item__status.is-error {
  color: #ff4d4f;
}

.upload-item__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: currentColor;
}

.upload-item__progress {
  height: 3px;
  border-radius: 999px;
  background: #f0f0f0;
  overflow: hidden;
}

.upload-item__progress-bar {
  height: 100%;
  background: #52c41a;
  transition: width 0.2s ease;
}

.upload-panel__input {
  display: none;
}

@media (max-width: 768px) {
  .upload-panel {
    right: 12px;
    bottom: 84px;
    width: calc(100vw - 24px);
  }
}
</style>
