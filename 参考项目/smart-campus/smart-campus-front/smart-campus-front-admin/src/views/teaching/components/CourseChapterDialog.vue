<template>
  <BaseDrawer v-model:show="visible" :title="drawerTitle" width="80%" :padding="0" body-overflow="hidden"
    :buttons="dialogButtons" :show-cancel="true" @close="handleClose">
    <div v-loading="loading" class="chapter-dialog">
      <section class="chapter-workspace">
        <aside class="chapter-sidebar">
          <div class="chapter-sidebar__panel">
            <div class="chapter-sidebar__title">
              <strong>{{ readonly ? '章节信息' : '章节管理' }}</strong>
              <p>{{ courseInfo.courseName || courseName || '课程章节维护' }}</p>
            </div>
            <header class="chapter-sidebar__header">
              <strong>章节列表</strong>
              <el-button v-if="!readonly" link type="primary" class="chapter-link-btn" @click="handleAddChapter">
                <i class="iconfont icon-add" />
                新增章节
              </el-button>
            </header>

            <div class="chapter-sidebar__body">
              <el-empty v-if="!chapterList.length" :image-size="80" description="暂无章节" />

              <div v-else class="chapter-sidebar__list">
                <article v-for="(chapter, chapterIndex) in chapterList"
                  :key="chapter.chapterId || `chapter-${chapterIndex}`" class="chapter-nav-card" :class="{
                    'is-active': selectedChapterIndex === chapterIndex,
                    'is-drag-over': dragChapterOverIndex === chapterIndex,
                  }" @click="selectChapter(chapterIndex)" @dragover.prevent="handleChapterDragOver(chapterIndex)"
                  @drop="handleChapterDrop(chapterIndex)">
                  <div v-if="!readonly" class="drag-handle chapter-nav-card__drag" draggable="true" @click.stop
                    @dragstart="handleChapterDragStart($event, chapterIndex)" @dragend="resetChapterDrag">
                    <i class="iconfont icon-drag" />
                  </div>

                  <div class="chapter-nav-card__content">
                    <strong>
                      章节{{ chapterIndex + 1 }}：{{ chapter.chapterName || '未命名章节' }}
                    </strong>
                    <span>
                      {{ chapter.lessonList.length }} 个课时 · 排序 {{ chapter.sortOrder }}
                    </span>
                  </div>
                </article>
              </div>
            </div>
          </div>
        </aside>

        <section class="chapter-main">
          <div v-if="!currentChapter" class="chapter-main__empty">
            <el-empty :image-size="88" description="请先在左侧新增章节" />
          </div>

          <template v-else>
            <section class="chapter-editor">
              <section class="chapter-info-card">
                <header class="chapter-main__header">
                  <div class="chapter-main__header-title">
                    <strong>
                      章节{{ selectedChapterIndex + 1 }}：{{
                        currentChapter.chapterName || '未命名章节'
                      }}
                    </strong>
                  </div>
                  <div class="chapter-main__header-actions">
                    <el-button v-if="!readonly" link type="danger" class="chapter-link-btn chapter-link-btn--danger"
                      @click="handleRemoveChapter(selectedChapterIndex)">
                      <i class="iconfont icon-remove" />
                      删除章节
                    </el-button>
                  </div>
                </header>

                <section class="chapter-form">
                  <div class="chapter-form__grid">
                    <el-form-item label="章节名称" label-width="90px" required>
                      <el-input v-model="currentChapter.chapterName" :disabled="readonly" placeholder="请输入章节名称" />
                    </el-form-item>
                    <el-form-item label="章节说明" label-width="82px" class="chapter-form__description">
                      <el-input v-model="currentChapter.description" :disabled="readonly" type="textarea" :rows="3" resize="none"
                        placeholder="请输入章节说明" />
                    </el-form-item>
                  </div>
                </section>
              </section>

              <section class="lesson-board">
                <div class="lesson-board__header">
                  <div class="lesson-board__title">课时列表</div>
                  <el-button v-if="!readonly" link type="primary" class="chapter-link-btn"
                    @click="handleAddLesson(selectedChapterIndex)">
                    <i class="iconfont icon-add" />
                    新增课时
                  </el-button>
                </div>
                <div v-if="!currentChapter.lessonList.length" class="lesson-list__empty">
                  <el-empty :image-size="72" description="当前章节暂无课时" />
                </div>

                <div v-else class="lesson-list">
                  <article v-for="(lesson, lessonIndex) in currentChapter.lessonList"
                    :key="lesson.lessonId || `lesson-${selectedChapterIndex}-${lessonIndex}`" class="lesson-card"
                    :class="{ 'is-drag-over': !readonly && dragLessonOverIndex === lessonIndex }"
                    @dragover.prevent="handleLessonDragOver(selectedChapterIndex, lessonIndex)"
                    @drop="handleLessonDrop(selectedChapterIndex, lessonIndex)">
                    <header class="lesson-card__header">
                      <div class="lesson-card__head-inline">
                        <div v-if="!readonly" class="drag-handle" draggable="true"
                          @dragstart="handleLessonDragStart($event, selectedChapterIndex, lessonIndex)"
                          @dragend="resetLessonDrag">
                          <i class="iconfont icon-drag" />
                        </div>
                        <strong>课时{{ lessonIndex + 1 }}</strong>
                      </div>

                      <div class="lesson-card__fields">
                        <el-input v-model="lesson.lessonName" :disabled="readonly" class="lesson-card__name" placeholder="请输入课时名称" />
                      </div>

                      <div class="lesson-card__header-actions">
                        <el-button v-if="!readonly" link type="primary" class="chapter-link-btn"
                          @click="openVideoSelector(selectedChapterIndex, lessonIndex)">
                          {{ lesson.videoResourceId ? '更换视频' : '选择视频' }}
                        </el-button>
                        <button v-if="lesson.videoResourceId" type="button" class="lesson-video-chip"
                          :title="lesson.videoResourceName || '未命名视频资源'" @click="previewResource(lesson)">
                          <span class="lesson-video-chip__label">已选视频</span>
                          <span class="lesson-video-chip__name">
                            {{ lesson.videoResourceName || '未命名视频资源' }}
                          </span>
                          <i class="iconfont icon-play-cover" />
                        </button>
                        <el-button v-if="!readonly && lesson.videoResourceId" link class="chapter-link-btn"
                          @click="clearLessonVideo(selectedChapterIndex, lessonIndex)">
                          清空视频
                        </el-button>
                        <el-button v-if="!readonly" link type="danger" class="chapter-link-btn chapter-link-btn--danger"
                          @click="handleRemoveLesson(selectedChapterIndex, lessonIndex)">
                          删除
                        </el-button>
                      </div>
                    </header>

                    <div class="lesson-card__resources">
                      <section class="lesson-resource-section">
                        <div class="lesson-resource-section__header">
                          <strong>课件</strong>
                        </div>
                        <div class="lesson-resource-section__body">
                          <div v-for="(courseware, coursewareIndex) in lesson.coursewareList"
                            :key="courseware.resourceId || `courseware-${coursewareIndex}`" class="courseware-tile">
                            <div class="courseware-tile__cover">
                              <img v-if="getCoursewareCover(courseware)" :src="getCoursewareCover(courseware)"
                                :alt="courseware.resourceName" />
                              <img v-else class="courseware-tile__icon" :src="getCoursewareIcon(courseware)"
                                :alt="courseware.resourceName || '课件图标'" />
                            </div>
                            <div class="courseware-tile__name" :title="courseware.resourceName">
                              {{ courseware.resourceName || '未命名课件' }}
                            </div>
                            <el-button v-if="!readonly" link type="danger" class="courseware-tile__remove"
                              @click="removeCourseware(selectedChapterIndex, lessonIndex, coursewareIndex)">
                              <i class="iconfont icon-del" />
                            </el-button>
                          </div>

                          <button v-if="!readonly" type="button" class="courseware-tile courseware-tile--adder"
                            @click="openCoursewareSelector(selectedChapterIndex, lessonIndex)">
                            <span>+选择课件</span>
                          </button>
                        </div>
                      </section>

                      <section class="lesson-resource-section lesson-resource-section--paper">
                        <div class="lesson-resource-section__header">
                          <strong>作业</strong>
                        </div>
                        <div class="lesson-resource-section__paper">
                          <button type="button" class="lesson-paper-tile" :disabled="readonly" @click="openPaperSelector(selectedChapterIndex, lessonIndex)">
                            <span class="lesson-paper-tile__label">
                              {{ readonly ? '查看作业' : lesson.paperId ? '更换作业' : '选择作业' }}
                            </span>
                            <span class="lesson-paper-tile__name">
                              {{ lesson.paperName || '暂未选择课后作业' }}
                            </span>
                          </button>
                          <el-button v-if="!readonly && lesson.paperId" link class="chapter-link-btn"
                            @click="clearLessonPaper(selectedChapterIndex, lessonIndex)">
                            清空作业
                          </el-button>
                        </div>
                      </section>
                    </div>
                  </article>
                </div>
              </section>
            </section>
          </template>
        </section>
      </section>
    </div>

    <ResourceSelectorDialog v-model:show="resourceSelectorVisible" :resource-type="selectorResourceType"
      @select="handleResourceSelected" />
    <PaperSelectorDialog v-model:show="paperSelectorVisible" @select="handlePaperSelected" />
    <ResourcePreviewDialog v-model:show="previewDialogVisible" :resource="previewingResource" />
  </BaseDrawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import excelIcon from '@/assets/excel.png'
import exeIcon from '@/assets/exe.png'
import fileIcon from '@/assets/ic_file2.png'
import othersIcon from '@/assets/others.png'
import pdfIcon from '@/assets/pdf.png'
import pptIcon from '@/assets/ppt1.png'
import txtIcon from '@/assets/txt.png'
import videoIcon from '@/assets/video.png'
import wordIcon from '@/assets/word.png'
import zipIcon from '@/assets/zip.png'
import BaseDrawer from '@/components/BaseDrawer.vue'
import { getCourseDetail, saveCourseStructure } from '@/api/course'
import { buildResourceFileUrl } from '@/utils/resource'
import PaperSelectorDialog from '@/views/teaching/components/PaperSelectorDialog.vue'
import ResourcePreviewDialog from '@/views/resource/components/ResourcePreviewDialog.vue'
import ResourceSelectorDialog from '@/views/resource/components/ResourceSelectorDialog.vue'
import '@/assets/icon/iconfont.css'

const props = defineProps({
  show: Boolean,
  courseId: {
    type: String,
    default: '',
  },
  courseName: {
    type: String,
    default: '',
  },
  teacherName: {
    type: String,
    default: '',
  },
  readonly: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:show', 'saved'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const readonly = computed(() => props.readonly)
const drawerTitle = computed(() => (readonly.value ? '查看章节' : '章节管理'))

const loading = ref(false)
const courseInfo = ref({})
const chapterList = ref([])
const selectedChapterIndex = ref(0)
const resourceSelectorVisible = ref(false)
const paperSelectorVisible = ref(false)
const currentResourceTarget = ref(null)
const currentPaperTarget = ref(null)
const previewDialogVisible = ref(false)
const previewingResource = ref(null)

const dragChapterFromIndex = ref(-1)
const dragChapterOverIndex = ref(-1)
const dragLessonChapterIndex = ref(-1)
const dragLessonFromIndex = ref(-1)
const dragLessonOverIndex = ref(-1)

const currentChapter = computed(() => {
  if (
    selectedChapterIndex.value < 0 ||
    selectedChapterIndex.value >= chapterList.value.length
  ) {
    return null
  }
  return chapterList.value[selectedChapterIndex.value]
})

const selectorResourceType = computed(() => {
  if (currentResourceTarget.value?.mode === 'video') {
    return 1
  }
  return undefined
})

const dialogButtons = computed(() =>
  readonly.value
    ? []
    : [
        {
          text: '保存',
          type: 'primary',
          click: handleSave,
        },
      ]
)

const normalizeRelationResourceId = (value) =>
  value == null || value === '' ? undefined : String(value)

const normalizeNumberValue = (value) =>
  value == null || value === '' ? undefined : Number(value)

const resolveFileSuffix = (resource = {}) => {
  const directSuffix = String(resource.fileSuffix || '')
    .trim()
    .toLowerCase()
  if (directSuffix) {
    return directSuffix
  }
  const filePath = String(resource.filePath || '').trim()
  if (!filePath) {
    return ''
  }
  const cleanPath = filePath.split('?')[0]
  const suffix = cleanPath.split('.').pop()
  return suffix ? String(suffix).toLowerCase() : ''
}

const normalizeLesson = (lesson = {}, index = 0) => ({
  lessonId: lesson.lessonId ?? '',
  lessonName: lesson.lessonName ?? '',
  sortOrder: lesson.sortOrder == null ? index + 1 : Number(lesson.sortOrder),
  videoResourceId: normalizeNumberValue(lesson.videoResourceId),
  videoResourceName: lesson.videoResourceName ?? '',
  videoFilePath: lesson.videoFilePath ?? '',
  videoCoverPath: lesson.videoCoverPath ?? '',
  paperId: lesson.paperId ?? '',
  paperName: lesson.paperName ?? '',
  paperType: lesson.paperType == null ? undefined : Number(lesson.paperType),
  paperTypeText: lesson.paperTypeText ?? '',
  coursewareList: Array.isArray(lesson.coursewareList)
    ? lesson.coursewareList.map((resource) => ({
        resourceId: normalizeRelationResourceId(resource.resourceId),
        resourceType:
          resource.resourceType == null
            ? undefined
            : Number(resource.resourceType),
        resourceName: resource.resourceName ?? '',
        fileSuffix: resolveFileSuffix(resource),
        filePath: resource.filePath ?? '',
        coverPath: resource.coverPath ?? '',
      }))
    : [],
})

const normalizeChapter = (chapter = {}, index = 0) => ({
  chapterId: chapter.chapterId ?? '',
  chapterName: chapter.chapterName ?? '',
  description: chapter.description ?? '',
  sortOrder: chapter.sortOrder == null ? index + 1 : Number(chapter.sortOrder),
  lessonList: Array.isArray(chapter.lessonList)
    ? chapter.lessonList.map((lesson, lessonIndex) =>
        normalizeLesson(lesson, lessonIndex)
      )
    : [],
})

const syncSortOrder = () => {
  chapterList.value.forEach((chapter, chapterIndex) => {
    chapter.sortOrder = chapterIndex + 1
    chapter.lessonList.forEach((lesson, lessonIndex) => {
      lesson.sortOrder = lessonIndex + 1
    })
  })
}

const ensureSelectedChapterIndex = () => {
  if (!chapterList.value.length) {
    selectedChapterIndex.value = 0
    return
  }
  if (selectedChapterIndex.value >= chapterList.value.length) {
    selectedChapterIndex.value = chapterList.value.length - 1
  }
  if (selectedChapterIndex.value < 0) {
    selectedChapterIndex.value = 0
  }
}

const resetState = () => {
  courseInfo.value = {}
  chapterList.value = []
  selectedChapterIndex.value = 0
  currentResourceTarget.value = null
  currentPaperTarget.value = null
  resourceSelectorVisible.value = false
  paperSelectorVisible.value = false
  previewDialogVisible.value = false
  previewingResource.value = null
  resetChapterDrag()
  resetLessonDrag()
}

const loadCourseDetail = async () => {
  const courseId = String(props.courseId || '').trim()
  if (!courseId) {
    return
  }
  loading.value = true
  try {
    const detail = await getCourseDetail(courseId)
    if (!detail) {
      visible.value = false
      return
    }
    courseInfo.value = detail
    chapterList.value = Array.isArray(detail.chapterList)
      ? detail.chapterList.map((chapter, index) =>
          normalizeChapter(chapter, index)
        )
      : []
    syncSortOrder()
    selectedChapterIndex.value = 0
    ensureSelectedChapterIndex()
  } finally {
    loading.value = false
  }
}

const selectChapter = (index) => {
  selectedChapterIndex.value = index
}

const previewResource = (lesson) => {
  if (!lesson?.videoFilePath) {
    return
  }
  previewingResource.value = {
    resourceId: lesson.videoResourceId,
    resourceType: 1,
    resourceName: lesson.videoResourceName || '视频预览',
    filePath: lesson.videoFilePath,
    coverPath: lesson.videoCoverPath || '',
  }
  previewDialogVisible.value = true
}

const getCoursewareCover = (resource = {}) => {
  if (resource.coverPath) {
    return buildResourceFileUrl(resource.coverPath)
  }
  if (Number(resource.resourceType) === 2 && resource.filePath) {
    return buildResourceFileUrl(resource.filePath)
  }
  return ''
}

const getCoursewareIcon = (resource = {}) => {
  const fileSuffix = String(resource.fileSuffix || '').toLowerCase()
  const resourceType = Number(resource.resourceType)

  if (resourceType === 1) {
    return videoIcon
  }
  if (['pdf'].includes(fileSuffix)) {
    return pdfIcon
  }
  if (['ppt', 'pptx', 'pps', 'ppsx', 'key'].includes(fileSuffix)) {
    return pptIcon
  }
  if (['doc', 'docx', 'docm', 'dot', 'dotx'].includes(fileSuffix)) {
    return wordIcon
  }
  if (['xls', 'xlsx', 'xlsm', 'csv'].includes(fileSuffix)) {
    return excelIcon
  }
  if (['txt', 'md', 'rtf'].includes(fileSuffix)) {
    return txtIcon
  }
  if (
    ['zip', 'rar', '7z', 'tar', 'gz'].includes(fileSuffix) ||
    resourceType === 4
  ) {
    return zipIcon
  }
  if (['exe', 'msi', 'bat', 'cmd'].includes(fileSuffix)) {
    return exeIcon
  }
  if (resourceType === 3) {
    return fileIcon
  }
  return othersIcon
}

const handleAddChapter = () => {
  chapterList.value.push(
    normalizeChapter(
      { chapterName: '', description: '', lessonList: [] },
      chapterList.value.length
    )
  )
  syncSortOrder()
  selectedChapterIndex.value = chapterList.value.length - 1
}

const handleRemoveChapter = (index) => {
  chapterList.value.splice(index, 1)
  syncSortOrder()
  ensureSelectedChapterIndex()
}

const handleAddLesson = (chapterIndex) => {
  chapterList.value[chapterIndex].lessonList.push(
    normalizeLesson(
      { lessonName: '' },
      chapterList.value[chapterIndex].lessonList.length
    )
  )
  syncSortOrder()
}

const handleRemoveLesson = (chapterIndex, lessonIndex) => {
  chapterList.value[chapterIndex].lessonList.splice(lessonIndex, 1)
  syncSortOrder()
}

const handleChapterDragStart = (event, index) => {
  dragChapterFromIndex.value = index
  dragChapterOverIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
}

const handleChapterDragOver = (index) => {
  dragChapterOverIndex.value = index
}

const handleChapterDrop = (index) => {
  const fromIndex = dragChapterFromIndex.value
  if (fromIndex < 0 || fromIndex === index) {
    resetChapterDrag()
    return
  }
  const nextList = [...chapterList.value]
  const [moved] = nextList.splice(fromIndex, 1)
  nextList.splice(index, 0, moved)
  chapterList.value = nextList
  selectedChapterIndex.value = index
  syncSortOrder()
  resetChapterDrag()
}

const resetChapterDrag = () => {
  dragChapterFromIndex.value = -1
  dragChapterOverIndex.value = -1
}

const handleLessonDragStart = (event, chapterIndex, lessonIndex) => {
  dragLessonChapterIndex.value = chapterIndex
  dragLessonFromIndex.value = lessonIndex
  dragLessonOverIndex.value = lessonIndex
  event.dataTransfer.effectAllowed = 'move'
}

const handleLessonDragOver = (chapterIndex, lessonIndex) => {
  if (dragLessonChapterIndex.value !== chapterIndex) {
    return
  }
  dragLessonOverIndex.value = lessonIndex
}

const handleLessonDrop = (chapterIndex, lessonIndex) => {
  if (dragLessonChapterIndex.value !== chapterIndex) {
    resetLessonDrag()
    return
  }
  const fromIndex = dragLessonFromIndex.value
  if (fromIndex < 0 || fromIndex === lessonIndex) {
    resetLessonDrag()
    return
  }
  const lessonList = [...chapterList.value[chapterIndex].lessonList]
  const [moved] = lessonList.splice(fromIndex, 1)
  lessonList.splice(lessonIndex, 0, moved)
  chapterList.value[chapterIndex].lessonList = lessonList
  syncSortOrder()
  resetLessonDrag()
}

const resetLessonDrag = () => {
  dragLessonChapterIndex.value = -1
  dragLessonFromIndex.value = -1
  dragLessonOverIndex.value = -1
}

const openVideoSelector = (chapterIndex, lessonIndex) => {
  currentResourceTarget.value = { chapterIndex, lessonIndex, mode: 'video' }
  resourceSelectorVisible.value = true
}

const openCoursewareSelector = (chapterIndex, lessonIndex) => {
  currentResourceTarget.value = {
    chapterIndex,
    lessonIndex,
    mode: 'courseware',
  }
  resourceSelectorVisible.value = true
}

const openPaperSelector = (chapterIndex, lessonIndex) => {
  const lesson = chapterList.value[chapterIndex]?.lessonList?.[lessonIndex]
  if (!lesson) {
    return
  }
  currentPaperTarget.value = {
    chapterIndex,
    lessonIndex,
  }
  paperSelectorVisible.value = true
}

const clearLessonVideo = (chapterIndex, lessonIndex) => {
  const lesson = chapterList.value[chapterIndex].lessonList[lessonIndex]
  lesson.videoResourceId = undefined
  lesson.videoResourceName = ''
  lesson.videoFilePath = ''
  lesson.videoCoverPath = ''
}

const clearLessonPaper = (chapterIndex, lessonIndex) => {
  const lesson = chapterList.value[chapterIndex].lessonList[lessonIndex]
  lesson.paperId = ''
  lesson.paperName = ''
  lesson.paperType = undefined
  lesson.paperTypeText = ''
}

const removeCourseware = (chapterIndex, lessonIndex, coursewareIndex) => {
  chapterList.value[chapterIndex].lessonList[lessonIndex].coursewareList.splice(
    coursewareIndex,
    1
  )
}

const handleResourceSelected = (resource) => {
  const target = currentResourceTarget.value
  if (!target) {
    return
  }
  const lesson =
    chapterList.value[target.chapterIndex]?.lessonList?.[target.lessonIndex]
  if (!lesson) {
    return
  }

  const normalizedResource = {
    resourceId: normalizeRelationResourceId(resource.resourceId ?? resource.id),
    resourceType:
      resource.resourceType == null ? undefined : Number(resource.resourceType),
    resourceName: resource.resourceName || '',
    fileSuffix: resolveFileSuffix(resource),
    filePath: resource.filePath || '',
    coverPath: resource.coverPath || '',
  }

  if (target.mode === 'video') {
    const oldVideoName = lesson.videoResourceName
    lesson.videoResourceId = normalizeNumberValue(normalizedResource.resourceId)
    lesson.videoResourceName = normalizedResource.resourceName
    lesson.videoFilePath = normalizedResource.filePath
    lesson.videoCoverPath = normalizedResource.coverPath
    if (!lesson.lessonName || lesson.lessonName === oldVideoName) {
      lesson.lessonName = normalizedResource.resourceName
    }
    return
  }

  if (
    lesson.coursewareList.some(
      (item) => String(item.resourceId || '') === String(normalizedResource.resourceId || '')
    )
  ) {
    ElMessage.warning('该课件已经添加过了')
    return
  }
  lesson.coursewareList.push(normalizedResource)
}

const handlePaperSelected = (paper) => {
  const target = currentPaperTarget.value
  if (!target) {
    return
  }
  const lesson =
    chapterList.value[target.chapterIndex]?.lessonList?.[target.lessonIndex]
  if (!lesson) {
    return
  }
  lesson.paperId = String(paper.paperId || '')
  lesson.paperName = paper.paperName || ''
  lesson.paperType = paper.paperType == null ? undefined : Number(paper.paperType)
  lesson.paperTypeText = paper.paperTypeText || ''
}

const validateStructure = () => {
  for (
    let chapterIndex = 0;
    chapterIndex < chapterList.value.length;
    chapterIndex += 1
  ) {
    const chapter = chapterList.value[chapterIndex]
    if (!chapter.chapterName?.trim()) {
      ElMessage.warning(`请填写第 ${chapterIndex + 1} 个章节名称`)
      selectedChapterIndex.value = chapterIndex
      return false
    }
    for (
      let lessonIndex = 0;
      lessonIndex < chapter.lessonList.length;
      lessonIndex += 1
    ) {
      const lesson = chapter.lessonList[lessonIndex]
      if (!lesson.lessonName?.trim()) {
        selectedChapterIndex.value = chapterIndex
        ElMessage.warning(
          `请填写第 ${chapterIndex + 1} 个章节下第 ${
            lessonIndex + 1
          } 个课时名称`
        )
        return false
      }
      if (!lesson.videoResourceId) {
        selectedChapterIndex.value = chapterIndex
        ElMessage.warning(
          `请为第 ${chapterIndex + 1} 个章节下第 ${
            lessonIndex + 1
          } 个课时选择视频资源`
        )
        return false
      }
    }
  }
  return true
}

async function handleSave() {
  if (!validateStructure()) {
    return
  }
  syncSortOrder()
  loading.value = true
  try {
    const result = await saveCourseStructure({
      courseId: courseInfo.value.courseId || props.courseId,
      chapterList: chapterList.value,
    })
    if (!result) {
      return
    }
    ElMessage.success('章节结构已保存')
    visible.value = false
    emit('saved')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  resetState()
}

watch(
  () => props.show,
  async (show) => {
    if (show) {
      await loadCourseDetail()
      return
    }
    resetState()
  }
)
</script>

<style lang="scss">
.chapter-dialog {
  display: flex;
  height: calc(100vh - 104px);
  flex-direction: column;
  background: linear-gradient(180deg, #f6f8fd 0%, #eef3fb 100%);
}

.chapter-workspace {
  display: grid;
  min-height: 0;
  flex: 1;
  grid-template-columns: 318px minmax(0, 1fr);
  gap: 20px;
  padding: 15px;
}

.chapter-sidebar,
.chapter-main {
  display: flex;
  min-height: 0;
  flex-direction: column;
  background: transparent;
}

.chapter-sidebar__panel {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(36, 56, 94, 0.08);
}

.chapter-sidebar__title {
  padding: 16px 12px 18px;

  strong {
    display: block;
    color: #2b2f36;
    font-size: 20px;
    font-weight: 700;
    line-height: 1.3;
  }

  p {
    margin: 6px 0 0;
    color: #7e8ca6;
    font-size: 12px;
    line-height: 1.5;
  }
}

.chapter-sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 12px 18px;
  border-bottom: 1px solid #e6ebf5;

  strong {
    color: #2b2f36;
    font-size: 18px;
    font-weight: 700;
  }
}

.chapter-sidebar__body {
  min-height: 0;
  flex: 1;
  padding: 12px 8px 8px;
  overflow: auto;
}

.chapter-sidebar__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-nav-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 10px;
  border: 1px solid #d7dde9;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;

  &.is-active {
    border-color: #6c9cff;
    background: #f4f8ff;
    box-shadow: 0 8px 18px rgba(76, 127, 231, 0.14);
  }

  &.is-drag-over {
    border-color: #8db2ff;
    box-shadow: 0 0 0 3px rgba(77, 125, 242, 0.12);
  }
}

.chapter-nav-card__drag {
  flex: 0 0 32px;
  margin-top: 2px;
}

.chapter-nav-card__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 3px;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #2b2f36;
    font-size: 14px;
    font-weight: 700;
    line-height: 1.4;
  }

  span {
    color: #7e8ca6;
    font-size: 12px;
  }
}

.chapter-main__empty {
  display: flex;
  min-height: 0;
  flex: 1;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
}

.chapter-main__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 0 18px;
  background: transparent;
  box-shadow: none;
  border-radius: 0;

  strong {
    color: #2b2f36;
    font-size: 22px;
    font-weight: 700;
  }
}

.chapter-main__header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.chapter-main__header-title {
  min-width: 0;
}

.chapter-editor {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 18px;
  padding: 0;
  overflow: hidden;
}

.chapter-info-card {
  padding: 18px 24px 22px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(36, 56, 94, 0.08);
}

.chapter-form {
  padding: 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.chapter-form__grid {
  display: grid;
  grid-template-columns: minmax(0, 400px) minmax(0, 1fr);
  gap: 28px 36px;
  align-items: start;
}

.chapter-form__description .el-form-item__content {
  align-items: flex-start;
}

.lesson-list {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 26px;
  overflow: auto;
  padding-right: 4px;
}

.lesson-list__empty {
  padding: 40px 0;
  border: 1px dashed #d9e3f3;
  border-radius: 8px;
  background: #fff;
}

.lesson-board {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 18px;
  padding: 16px;
  border-radius: 8px;
  background: #fff;
}

.lesson-board__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.lesson-board__title {
  flex: 0 0 auto;
  color: #2b2f36;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
}

.lesson-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e1e6ef;
  border-radius: 8px;
  background: #fff;
  padding: 16px 18px 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &.is-drag-over {
    border-color: #8db2ff;
    box-shadow: 0 0 0 3px rgba(77, 125, 242, 0.1);
  }
}

.lesson-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.lesson-card__head-inline,
.lesson-card__header-actions {
  display: flex;
  align-items: center;
}

.lesson-card__head-inline {
  gap: 10px;
  flex-shrink: 0;

  strong {
    color: #2b2f36;
    font-size: 16px;
    font-weight: 700;
  }
}

.lesson-card__fields {
  display: flex;
  min-width: 0;
  flex: 1;
  gap: 20px;
  align-items: center;
}

.lesson-card__header-actions {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  flex-shrink: 0;
  justify-content: flex-end;
  gap: 12px;
}

.lesson-card__name {
  max-width: 350px;
}

.lesson-card__type {
  width: 100px;
}

.lesson-card__sort {
  color: #7d8ba6;
  font-size: 12px;
}

.drag-handle {
  display: inline-flex;
  height: 28px;
  width: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f1f5ff;
  color: #7c9dea;
  cursor: grab;
  user-select: none;

  &:active {
    cursor: grabbing;
  }

  .iconfont {
    font-size: 17px;
    line-height: 1;
  }
}

.lesson-card__resources {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-left: 38px;
}

.lesson-resource-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lesson-resource-section__header {
  strong {
    color: #2b2f36;
    font-size: 13px;
    font-weight: 700;
  }
}

.lesson-resource-section__body {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.lesson-resource-section__paper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chapter-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  font-size: 13px;
  font-weight: 400;
  line-height: 1;

  .iconfont {
    font-size: 13px;
    line-height: 1;

    &::before {
      margin-right: 3px;
    }
  }
}

.chapter-link-btn--danger {
  color: #ff5b57;
}

.courseware-tile {
  display: flex;
  flex-direction: column;
  width: 126px;
  height: 95px;
  border: 1px solid #d9dee7;
  border-radius: 8px;
  background: #fff;
  color: #2b2f36;
  text-align: left;
  position: relative;
}

.courseware-tile__cover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 64px;
  border-radius: 8px 8px 0 0;
  background: #f5f7fb;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.courseware-tile__icon {
  width: 34px !important;
  height: 34px !important;
  object-fit: contain !important;
}

.courseware-tile__name {
  margin-top: 5px;
  color: #4d5870;
  font-size: 12px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.courseware-tile__remove {
  position: absolute;
  top: -6px;
  right: -6px;
  padding: 0;
  color: #ff6b6b;
  font-size: 12px;
  line-height: 1;

  .iconfont {
    font-size: 12px;
    line-height: 1;
    border-radius: 50%;
  }
}

.courseware-tile--adder {
  align-items: center;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: #4d7fff;
  background: #fbfcff;
}

.lesson-paper-tile {
  display: flex;
  min-width: 0;
  width: 320px;
  min-height: 58px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 4px;
  padding: 10px 14px;
  border: 1px dashed #cfd8ea;
  border-radius: 8px;
  background: #fbfcff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;

  &:hover {
    border-color: #6c9cff;
    background: #f7faff;
    box-shadow: 0 6px 16px rgba(77, 125, 242, 0.08);
  }
}

.lesson-paper-tile__label {
  color: #4d7fff;
  font-size: 13px;
  font-weight: 600;
}

.lesson-paper-tile__name {
  width: 100%;
  overflow: hidden;
  color: #4d5870;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lesson-video-chip {
  display: inline-flex;
  min-width: 0;
  max-width: 280px;
  height: 32px;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  border: 1px solid #d8e1f1;
  border-radius: 8px;
  background: #f8fbff;
  color: #4d5870;
  cursor: pointer;

  .iconfont {
    flex: 0 0 auto;
    color: #4d7fff;
    font-size: 14px;
    line-height: 1;
  }
}

.lesson-video-chip__label {
  flex: 0 0 auto;
  color: #7b88a1;
  font-size: 12px;
}

.lesson-video-chip__name {
  min-width: 0;
  overflow: hidden;
  color: #2b2f36;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-dialog .el-form-item {
  margin-bottom: 0;
}

.chapter-dialog .el-form-item__label {
  color: #2b2f36;
  font-weight: 600;
}

.chapter-dialog .el-input__wrapper,
.chapter-dialog .el-select__wrapper,
.chapter-dialog .el-textarea__inner {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d8dde8 inset;
}

.chapter-dialog .el-input__wrapper.is-focus,
.chapter-dialog .el-select__wrapper.is-focused,
.chapter-dialog .el-textarea__inner:focus {
  box-shadow: 0 0 0 1px #6c9cff inset, 0 0 0 4px rgba(108, 156, 255, 0.12);
}

.chapter-dialog .el-button--primary.is-plain {
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.chapter-dialog .el-button.is-link .iconfont {
  font-size: 12px;
}

@media (max-width: 1400px) {
  .chapter-workspace {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .lesson-card__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .lesson-card__fields {
    width: 100%;
  }
}

@media (max-width: 1200px) {
  .chapter-workspace {
    grid-template-columns: 1fr;
  }

  .chapter-sidebar {
    max-height: 280px;
  }

  .chapter-form__grid,
  .lesson-card__fields {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .lesson-card__name,
  .lesson-card__type {
    max-width: none;
    width: 100%;
  }

  .lesson-card__resources {
    padding-left: 0;
  }

  .lesson-card__header-actions {
    justify-content: flex-start;
  }
}
</style>
