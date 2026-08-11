<template>
  <div class="course-study">
    <el-image ref="imagePreviewRef" class="course-study__image-preview-anchor"
      :src="previewImageList[previewInitialIndex] || ''" :preview-src-list="previewImageList"
      :initial-index="previewInitialIndex" fit="contain" hide-on-click-modal preview-teleported />

    <div v-if="loading" class="course-study__state">正在加载课程详情...</div>

    <div v-else-if="!courseDetail" class="course-study__state is-empty">
      <strong>课程详情加载失败</strong>
      <p>请返回我的课程页重新进入。</p>
    </div>

    <template v-else>
      <nav class="course-study__breadcrumb">
        <button type="button" @click="router.push('/courses')">我的课程</button>
        <span>/</span>
        <span>{{ courseDetail.courseName }}</span>
        <span>/</span>
        <span>{{ currentChapter?.chapterName || '章节内容' }}</span>
        <span>/</span>
        <span>{{ currentLesson?.lessonName || '课时详情' }}</span>
      </nav>

      <section class="course-study__hero card">
        <div class="course-study__hero-cover">
          <img v-if="courseCoverUrl" :src="courseCoverUrl" :alt="courseDetail.courseName">
          <div v-else class="course-study__hero-cover-fallback">
            <span>{{ courseDetail.courseName?.slice(0, 2) || '课程' }}</span>
          </div>
        </div>

        <div class="course-study__hero-main">
          <h1>{{ courseDetail.courseName }}</h1>
          <div class="course-study__meta">
            <span>
              <i class="iconfont icon-user" />
              主讲老师：{{ courseDetail.teacherName || '-' }}
            </span>
            <span>
              <i class="iconfont icon-class" />
              适用班级：{{ courseDetail.classNames || '未配置班级' }}
            </span>
          </div>
          <div class="course-study__progress-row">
            <span>学习进度：{{ studyProgress }}%</span>
            <div class="course-study__progress">
              <span :style="{ width: `${studyProgress}%` }" />
            </div>
          </div>
        </div>

        <div class="course-study__hero-actions">
          <button type="button" class="course-study__primary-btn" @click="handleContinue">
            继续学习
          </button>
          <button type="button" class="course-study__secondary-btn" :class="{ 'is-active': isFavorite }"
            @click="toggleFavorite">
            <i class="iconfont icon-collection" />
            {{ isFavorite ? '已收藏' : '收藏课程' }}
          </button>
        </div>
      </section>

      <div class="course-study__main">
        <section class="course-study__content card">
          <div class="course-study__tabs">
            <button v-for="tab in tabs" :key="tab.key" type="button" class="course-study__tab"
              :class="{ 'is-active': activeTab === tab.key }" @click="activeTab = tab.key">
              {{ tab.label }}
            </button>
          </div>

          <div v-if="activeTab === 'content'" class="course-study__panel">
            <h2>{{ currentLesson?.lessonName || '暂无课时内容' }}</h2>
            <CourseVideoPlayer :url="currentVideoUrl" :poster="currentVideoPoster"
              :title="currentLesson?.lessonName || courseDetail.courseName" :initial-time="currentLessonResumeSeconds"
              @timeupdate="handlePlayerTimeUpdate" @ended="handleVideoEnded" />

            <div class="course-study__player-actions">
              <button type="button" class="course-study__ghost-btn" :disabled="!hasPrevLesson"
                @click="switchLesson(-1)">
                上一节
              </button>
              <button type="button" class="course-study__primary-btn is-small" :disabled="!hasNextLesson"
                @click="switchLesson(1)">
                下一节
              </button>
            </div>
          </div>

          <div v-else-if="activeTab === 'courseware'" class="course-study__panel">
            <div class="course-study__section-title">
              <h3>课件资料</h3>
            </div>
            <div v-if="currentLessonCourseware.length" class="course-study__resource-list">
              <div v-for="item in currentLessonCourseware" :key="item.resourceId" class="course-study__resource-item"
                tabindex="0" @click="openResource(item)" @keyup.enter="openResource(item)">
                <img class="course-study__resource-icon" :src="resolveResourceIconSrc(item.fileSuffix)"
                  :alt="resolveFileTag(item.fileSuffix)">
                <span class="course-study__resource-name" :title="item.resourceName">{{ item.resourceName }}</span>
                <span class="course-study__resource-size">{{ formatFileSize(item.fileSize) }}</span>
              </div>
            </div>
            <div v-else class="course-study__empty-panel">当前课时暂无课件资料</div>
          </div>

          <div v-else-if="activeTab === 'homework'" class="course-study__panel">
            <div class="course-study__section-title">
              <h3>课后作业</h3>
            </div>
            <div v-if="homeworkList.length" class="course-study__assignment-list">
              <article v-for="item in homeworkList" :key="item.lessonId" class="course-study__assignment-item"
                tabindex="0" @click="openHomework(item)" @keyup.enter="openHomework(item)">
                <div class="course-study__assignment-main">
                  <i class="iconfont icon-homework course-study__assignment-icon" />
                  <div class="course-study__assignment-copy">
                    <strong :title="item.title">{{ item.title }}</strong>
                    <p>{{ item.meta }}</p>
                  </div>
                </div>
                <div class="course-study__assignment-side">
                  <span v-if="item.status" class="course-study__assignment-status" :class="`is-${item.theme}`">{{
                    item.status }}</span>
                  <button v-if="item.actionLabel" type="button" class="course-study__assignment-action"
                    :class="`is-${item.theme}`" @click.stop="openHomework(item)">
                    {{ item.actionLabel }}
                  </button>
                </div>
              </article>
            </div>
            <div v-else class="course-study__empty-panel">当前课时暂无课后作业</div>
          </div>

          <div v-else class="course-study__panel">
            <div class="course-study__section-title">
              <h3>学习记录</h3>
            </div>
            <div class="course-study__record-compact">
              <div class="course-study__record-row">
                <span>上次学习</span>
                <strong>{{ studyRecordList[0]?.value }}</strong>
              </div>
              <div class="course-study__record-row">
                <span>学习时长</span>
                <strong>{{ studyRecordList[1]?.value }}</strong>
              </div>
              <div class="course-study__record-row is-progress">
                <div class="course-study__record-progress-meta">
                  <span>完成进度</span>
                  <strong>{{ studyRecordList[2]?.value }}</strong>
                </div>
                <div class="course-study__record-progress">
                  <span :style="{ width: `${studyProgress}%` }" />
                </div>
              </div>
            </div>
          </div>
        </section>

        <aside class="course-study__aside">
          <section class="course-study__side-card card">
            <div class="course-study__section-title">
              <h3>章节目录</h3>
            </div>
            <div class="course-study__chapter-list">
              <article v-for="chapter in chapterList" :key="chapter.chapterId" class="course-study__chapter">
                <header class="course-study__chapter-header" @click="toggleChapter(chapter.chapterId)">
                  <div class="course-study__chapter-title">
                    <span class="course-study__chapter-arrow"
                      :class="{ 'is-open': isChapterExpanded(chapter.chapterId) }" />
                    <span>{{ chapter.chapterName }}</span>
                  </div>
                </header>
                <div v-show="isChapterExpanded(chapter.chapterId)" class="course-study__chapter-lessons">
                  <div v-for="lesson in chapter.lessonList" :key="lesson.lessonId" class="course-study__lesson" :class="{
                    'is-active': lesson.lessonId === selectedLessonId,
                    'is-hovered': lesson.lessonId === hoveredLessonId,
                    'is-disabled': !canLearnLesson(lesson.lessonId),
                  }" :title="!canLearnLesson(lesson.lessonId) ? '请先完成前面的课时学习' : ''"
                    @click="selectLesson(lesson.lessonId)" @mouseenter="hoveredLessonId = lesson.lessonId"
                    @mouseleave="hoveredLessonId = ''">
                    <i class="iconfont" :class="resolveLessonIcon(lesson.lessonId)" />
                    <span class="course-study__lesson-title">{{ lesson.lessonName }}</span>
                    <i v-if="!canLearnLesson(lesson.lessonId)" class="iconfont course-study__lesson-status icon-lock" />
                    <i v-else class="iconfont course-study__lesson-status"
                      :class="isCompletedLesson(lesson.lessonId) ? 'icon-completed' : 'icon-time'" />
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section class="course-study__side-card card">
            <div class="course-study__section-title">
              <h3>课件资料</h3>
            </div>
            <div v-if="currentLessonCourseware.length" class="course-study__mini-resource-list">
              <div v-for="item in currentLessonCourseware" :key="`aside-${item.resourceId}`"
                class="course-study__mini-resource" tabindex="0" @click="openResource(item)"
                @keyup.enter="openResource(item)">
                <img class="course-study__resource-icon is-mini" :src="resolveResourceIconSrc(item.fileSuffix)"
                  :alt="resolveFileTag(item.fileSuffix)">
                <span class="course-study__mini-resource-name" :title="item.resourceName">{{ item.resourceName }}</span>
                <span class="course-study__mini-resource-size">{{ formatFileSize(item.fileSize) }}</span>
              </div>
            </div>
            <div v-else class="course-study__empty-panel is-side">暂无课件资料</div>
          </section>

          <section class="course-study__side-card card">
            <div class="course-study__section-title">
              <h3>课后作业</h3>
            </div>
            <div v-if="homeworkList.length" class="course-study__assignment-list is-side">
              <article v-for="item in homeworkList" :key="`side-${item.lessonId}`" class="course-study__assignment-item"
                tabindex="0" @click="openHomework(item)" @keyup.enter="openHomework(item)">
                <div class="course-study__assignment-main">
                  <i class="iconfont icon-homework course-study__assignment-icon" />
                  <div class="course-study__assignment-copy">
                    <strong :title="item.title">{{ item.title }}</strong>
                    <p>{{ item.meta }}</p>
                  </div>
                </div>
                <div class="course-study__assignment-side">
                  <span v-if="item.status" class="course-study__assignment-status" :class="`is-${item.theme}`">{{
                    item.status }}</span>
                  <button v-if="item.actionLabel" type="button" class="course-study__assignment-action"
                    :class="`is-${item.theme}`" @click.stop="openHomework(item)">
                    {{ item.actionLabel }}
                  </button>
                </div>
              </article>
            </div>
            <div v-else class="course-study__empty-panel is-side">暂无课后作业</div>
          </section>

          <section class="course-study__side-card card">
            <div class="course-study__section-title">
              <h3>学习记录</h3>
            </div>
            <div class="course-study__record-compact is-side">
              <div class="course-study__record-row">
                <span>上次学习</span>
                <strong>{{ studyRecordList[0]?.value }}</strong>
              </div>
              <div class="course-study__record-row">
                <span>学习时长</span>
                <strong>{{ studyRecordList[1]?.value }}</strong>
              </div>
              <div class="course-study__record-row is-progress">
                <div class="course-study__record-progress-meta">
                  <span>完成进度</span>
                  <strong>{{ studyRecordList[2]?.value }}</strong>
                </div>
                <div class="course-study__record-progress">
                  <span :style="{ width: `${studyProgress}%` }" />
                </div>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CourseVideoPlayer from '@/components/CourseVideoPlayer.vue'
import {
  getCourseHomeworkDetail,
  getMyCourseDetail,
  reportStudyProgress,
  saveCourseCollection,
} from '@/api/course'
import Message from '@/utils/Message'
import { buildResourceFileUrl } from '@/utils/resource'
import pdfIcon from '@/assets/pdf.png'
import pptIcon from '@/assets/ppt1.png'
import zipIcon from '@/assets/zip.png'
import wordIcon from '@/assets/word.png'
import excelIcon from '@/assets/excel.png'
import txtIcon from '@/assets/txt.png'
import videoIcon from '@/assets/video.png'
import imageIcon from '@/assets/image.png'
import fileIcon from '@/assets/ic_file2.png'

const STUDY_REPORT_INTERVAL_SECONDS = 5
const STUDY_STATUS_COMPLETED = 1

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const courseDetail = ref(null)
const activeTab = ref('content')
const selectedLessonId = ref('')
const hoveredLessonId = ref('')
const imagePreviewRef = ref()
const previewImageList = ref([])
const previewInitialIndex = ref(0)
const playerCurrentTime = ref(0)
const playerDuration = ref(0)
const pendingWatchSeconds = ref(0)
const lastTrackedTime = ref(0)
const currentStudySessionId = ref('')
const reportPromise = ref(null)
const markAsLearned = ref(false)
const expandedChapterIds = ref([])
const currentHomeworkDetail = ref(null)

const chapterList = computed(() => courseDetail.value?.chapterList || [])

const flatLessons = computed(() =>
  chapterList.value.flatMap((chapter, chapterIndex) =>
    (chapter.lessonList || []).map((lesson, lessonIndex) => ({
      ...lesson,
      chapterId: chapter.chapterId,
      chapterName: chapter.chapterName,
      chapterIndex,
      lessonIndex,
    }))
  )
)

const currentLessonIndex = computed(() =>
  flatLessons.value.findIndex(
    (item) => item.lessonId === selectedLessonId.value
  )
)

const currentLesson = computed(
  () => flatLessons.value[currentLessonIndex.value] || null
)

const currentChapter = computed(
  () =>
    chapterList.value.find(
      (item) => item.chapterId === currentLesson.value?.chapterId
    ) || null
)

const currentLessonCourseware = computed(
  () => currentLesson.value?.coursewareList || []
)
const currentLessonImageResourceList = computed(() =>
  currentLessonCourseware.value.filter((item) =>
    isImageResource(item?.fileSuffix)
  )
)
const currentLessonResumeSeconds = computed(() =>
  Math.max(0, Number(currentLesson.value?.lastPositionSeconds || 0))
)

const tabs = computed(() => [
  { key: 'content', label: '学习内容' },
  {
    key: 'courseware',
    label: `课件资料${currentLessonCourseware.value.length
      ? `（${currentLessonCourseware.value.length}）`
      : ''
      }`,
  },
  { key: 'homework', label: `课后作业（${homeworkList.value.length}）` },
  { key: 'record', label: '学习记录' },
])

const courseCoverUrl = computed(() =>
  buildResourceFileUrl(courseDetail.value?.coverPath)
)
const currentVideoUrl = computed(() =>
  buildResourceFileUrl(currentLesson.value?.videoFilePath)
)
const currentVideoPoster = computed(() =>
  buildResourceFileUrl(
    currentLesson.value?.videoCoverPath || courseDetail.value?.coverPath
  )
)

const completedLessonCount = computed(
  () =>
    flatLessons.value.filter(
      (item) => Number(item?.isCompleted || 0) === STUDY_STATUS_COMPLETED
    ).length
)

// 判断课时是否可以学习（必须前面的课时都已完成）
const canLearnLesson = (lessonId) => {
  const targetIndex = flatLessons.value.findIndex(
    (item) => item.lessonId === lessonId
  )
  if (targetIndex === -1) {
    return false
  }
  // 第一个课时总是可以学习
  if (targetIndex === 0) {
    return true
  }
  // 检查前面所有课时是否都已完成
  for (let i = 0; i < targetIndex; i++) {
    const lesson = flatLessons.value[i]
    if (Number(lesson?.isCompleted || 0) !== STUDY_STATUS_COMPLETED) {
      return false
    }
  }
  return true
}

const studyProgress = computed(() => {
  if (!flatLessons.value.length) {
    return 0
  }
  return Math.round(
    (completedLessonCount.value / flatLessons.value.length) * 100
  )
})

const studyHours = computed(() =>
  ((Number(courseDetail.value?.studySeconds || 0) || 0) / 3600).toFixed(1)
)
const isFavorite = computed(
  () => Number(courseDetail.value?.isCollected || 0) === 1
)
const hasPrevLesson = computed(() => currentLessonIndex.value > 0)
const hasNextLesson = computed(
  () =>
    currentLessonIndex.value > -1 &&
    currentLessonIndex.value < flatLessons.value.length - 1
)

const lessonSummary = computed(() => {
  const lessonName = currentLesson.value?.lessonName || '本节内容'
  const chapterName = currentChapter.value?.chapterName || '当前章节'
  return {
    intro: `${lessonName} 主要围绕 ${chapterName} 的核心概念展开讲解，帮助学生建立从定义到结构、再到应用场景的完整认知。`,
    detail:
      '页面中的学习进度、笔记、讨论和作业统计目前使用静态示例数据展示，便于先把学习页交互和布局跑通。',
    points: [
      `梳理 ${lessonName} 的基础概念与常见术语`,
      '结合图示理解结构关系与实际应用场景',
      '通过课件与练习巩固本节重点内容',
    ],
  }
})

const noteList = computed(() => {
  const lessonName = currentLesson.value?.lessonName || '当前课时'
  return [
    {
      title: '概念整理',
      content: `围绕 ${lessonName} 记录了定义、性质和关键术语。`,
      time: '2026-05-05 09:30',
    },
    {
      title: '易错点提醒',
      content: '对比相近概念，标注了容易混淆的知识点。',
      time: '2026-05-05 10:15',
    },
    {
      title: '复习提纲',
      content: '整理了适合课后回顾的三条主线。',
      time: '2026-05-05 11:05',
    },
  ]
})

const discussionList = computed(() => {
  const chapterName = currentChapter.value?.chapterName || '当前章节'
  return [
    {
      title: `${chapterName} 讨论 1`,
      content: '如何从实际场景理解本节结构关系？',
      time: '2 小时前',
    },
    {
      title: `${chapterName} 讨论 2`,
      content: '课上示例和作业题之间的解题思路是否一致？',
      time: '昨天 19:20',
    },
    {
      title: `${chapterName} 讨论 3`,
      content: '本节推荐先看视频还是先看讲义？',
      time: '昨天 16:48',
    },
  ]
})

const homeworkList = computed(() => {
  const paperId = currentLesson.value?.paperId
  if (!paperId) {
    return []
  }
  const submitStatus = Number(currentHomeworkDetail.value?.submitStatus ?? 0)
  const judgeStatus = Number(currentHomeworkDetail.value?.judgeStatus ?? 0)
  const submitted = Boolean(
    currentHomeworkDetail.value?.submitted || submitStatus === 3
  )
  const submittedStatus = submitted
    ? currentHomeworkDetail.value?.judgeStatusText || '待人工批改'
    : ''
  const submittedTheme =
    judgeStatus === 2 ? 'orange' : judgeStatus === 0 ? 'red' : 'green'
  const actionLabel = submitted
    ? ''
    : submitStatus === 1 || submitStatus === 2
      ? '继续答题'
      : '开始做题'
  return [
    {
      title:
        currentLesson.value?.paperName ||
        `${currentLesson.value?.lessonName || '当前课时'} 课后作业`,
      meta: currentLesson.value?.paperTypeText || '课后作业',
      status: submittedStatus,
      theme: submitted
        ? submittedTheme
        : submitStatus === 1 || submitStatus === 2
          ? 'blue'
          : 'orange',
      actionLabel,
      submitted,
      lessonId: currentLesson.value?.lessonId,
    },
  ]
})

const studyRecordList = computed(() => [
  {
    label: '上次学习',
    value: formatDateTime(courseDetail.value?.lastStudyTime) || '--',
  },
  { label: '学习时长', value: `${studyHours.value} 小时` },
  { label: '完成进度', value: `${studyProgress.value}%` },
])

const toggleFavorite = async () => {
  const courseId = String(route.params.courseId || '')
  if (!courseId) {
    return
  }
  const result = await saveCourseCollection({
    courseId,
    collectFlag: isFavorite.value ? 0 : 1,
  })
  if (result === null || !courseDetail.value) {
    return
  }
  courseDetail.value = {
    ...courseDetail.value,
    isCollected: Number(result || 0),
  }
}

const isChapterExpanded = (chapterId) =>
  expandedChapterIds.value.includes(chapterId)

const toggleChapter = (chapterId) => {
  if (!chapterId) {
    return
  }
  if (isChapterExpanded(chapterId)) {
    expandedChapterIds.value = expandedChapterIds.value.filter(
      (item) => item !== chapterId
    )
  } else {
    expandedChapterIds.value = [...expandedChapterIds.value, chapterId]
  }
}

const createStudySessionId = () =>
  `S${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`.toUpperCase()

const resetPlayerTracking = () => {
  playerCurrentTime.value = Math.max(
    0,
    Number(currentLesson.value?.lastPositionSeconds || 0)
  )
  playerDuration.value = Math.max(
    0,
    Number(currentLesson.value?.videoDurationSeconds || 0)
  )
  pendingWatchSeconds.value = 0
  lastTrackedTime.value = playerCurrentTime.value
  currentStudySessionId.value = createStudySessionId()
}

const findLessonRecord = (lessonId) => {
  for (const chapter of courseDetail.value?.chapterList || []) {
    const lesson = (chapter.lessonList || []).find(
      (item) => item.lessonId === lessonId
    )
    if (lesson) {
      return lesson
    }
  }
  return null
}

const mergeReportedProgress = (reportResult) => {
  if (!reportResult || !courseDetail.value) {
    return
  }
  courseDetail.value.studySeconds = Number(reportResult.courseStudySeconds || 0)
  courseDetail.value.lastStudyTime =
    reportResult.courseLastStudyTime || courseDetail.value.lastStudyTime
  courseDetail.value.studyStatus = Number(
    reportResult.courseStatus ?? courseDetail.value.studyStatus ?? 0
  )
  courseDetail.value.currentChapterId =
    reportResult.currentChapterId || courseDetail.value.currentChapterId
  courseDetail.value.currentLessonId =
    reportResult.currentLessonId || courseDetail.value.currentLessonId
  const lesson = findLessonRecord(reportResult.lessonId)
  if (!lesson) {
    return
  }
  lesson.studySeconds = Number(reportResult.lessonStudySeconds || 0)
  lesson.lastPositionSeconds = Number(reportResult.lastPositionSeconds || 0)
  lesson.maxPositionSeconds = Number(reportResult.maxPositionSeconds || 0)
  lesson.videoDurationSeconds = Number(reportResult.videoDurationSeconds || 0)
  lesson.isCompleted = Number(reportResult.isCompleted || 0)
  lesson.lastStudyTime =
    reportResult.lessonLastStudyTime || lesson.lastStudyTime
}

const flushStudyProgress = async ({ forceComplete = false } = {}) => {
  if (reportPromise.value) {
    await reportPromise.value
  }
  const lesson = currentLesson.value
  if (!courseDetail.value || !lesson?.lessonId || !lesson.videoResourceId) {
    pendingWatchSeconds.value = 0
    return true
  }
  const watchSeconds = Math.max(0, Math.floor(pendingWatchSeconds.value))
  const positionSeconds = Math.max(0, Math.floor(playerCurrentTime.value))
  const durationSeconds = Math.max(
    0,
    Math.floor(playerDuration.value || lesson.videoDurationSeconds || 0)
  )
  if (watchSeconds <= 0 && !forceComplete && !markAsLearned.value) {
    return true
  }
  const params = {
    sessionId: currentStudySessionId.value || createStudySessionId(),
    courseId: courseDetail.value.courseId,
    chapterId: lesson.chapterId,
    lessonId: lesson.lessonId,
    videoResourceId: lesson.videoResourceId,
    watchSeconds,
    positionSeconds,
    durationSeconds,
    forceComplete: forceComplete || markAsLearned.value ? 1 : 0,
  }
  reportPromise.value = reportStudyProgress(params)
  const result = await reportPromise.value
  reportPromise.value = null
  if (!result) {
    return false
  }
  pendingWatchSeconds.value = 0
  mergeReportedProgress(result)
  markAsLearned.value = false
  return true
}

const selectLesson = async (lessonId) => {
  if (lessonId === selectedLessonId.value) {
    return
  }

  // 检查是否可以学习该课时
  if (!canLearnLesson(lessonId)) {
    Message.warning('请先完成前面的课时学习')
    return
  }

  await flushStudyProgress()
  selectedLessonId.value = lessonId
  markAsLearned.value = false
  const lesson = flatLessons.value.find((item) => item.lessonId === lessonId)
  if (lesson?.chapterId) {
    expandedChapterIds.value = [lesson.chapterId]
  }
  resetPlayerTracking()
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      lessonId,
    },
  })
}

const switchLesson = (offset) => {
  const target = flatLessons.value[currentLessonIndex.value + offset]
  if (!target) {
    return
  }
  selectLesson(target.lessonId)
}

const openHomework = (item) => {
  if (!item?.lessonId) {
    return
  }
  router.push(`/courses/${route.params.courseId}/homework/${item.lessonId}`)
}

const handleContinue = () => {
  if (selectedLessonId.value) {
    return
  }
  if (flatLessons.value[0]) {
    selectLesson(flatLessons.value[0].lessonId)
  }
}

const handleVideoEnded = () => {
  markAsLearned.value = true
  playerCurrentTime.value = Math.max(
    playerCurrentTime.value,
    playerDuration.value
  )
  flushStudyProgress({ forceComplete: true })
}

const isCompletedLesson = (lessonId) => {
  const lesson = flatLessons.value.find((item) => item.lessonId === lessonId)
  return Number(lesson?.isCompleted || 0) === STUDY_STATUS_COMPLETED
}

const resolveLessonIcon = (lessonId) => {
  if (
    lessonId === selectedLessonId.value ||
    lessonId === hoveredLessonId.value
  ) {
    return 'icon-play-cover'
  }
  return 'icon-pending'
}

const handlePlayerTimeUpdate = ({ currentTime, duration }) => {
  const normalizedCurrentTime = Math.max(0, Number(currentTime || 0))
  const normalizedDuration = Math.max(0, Number(duration || 0))
  playerDuration.value = normalizedDuration
  if (lastTrackedTime.value > 0) {
    const delta = normalizedCurrentTime - lastTrackedTime.value
    if (delta > 0 && delta <= 2) {
      pendingWatchSeconds.value += delta
    }
  }
  playerCurrentTime.value = normalizedCurrentTime
  lastTrackedTime.value = normalizedCurrentTime
  if (pendingWatchSeconds.value >= STUDY_REPORT_INTERVAL_SECONDS) {
    flushStudyProgress()
  }
}

const isImageResource = (suffix) =>
  ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(
    String(suffix || '').toLowerCase()
  )

const previewImageResource = async (resource) => {
  const previewUrls = currentLessonImageResourceList.value
    .map((item) => buildResourceFileUrl(item?.filePath))
    .filter(Boolean)
  const targetUrl = buildResourceFileUrl(resource?.filePath)
  if (!previewUrls.length || !targetUrl) {
    Message.warning('当前图片资源不可预览')
    return
  }
  previewImageList.value = previewUrls
  previewInitialIndex.value = Math.max(
    0,
    previewUrls.findIndex((item) => item === targetUrl)
  )
  await nextTick()
  imagePreviewRef.value?.showPreview?.()
}

const openResource = async (resource) => {
  const url = buildResourceFileUrl(resource?.filePath)
  if (!url) {
    Message.warning('当前资料文件不可用')
    return
  }
  if (isImageResource(resource?.fileSuffix)) {
    await previewImageResource(resource)
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

const formatFileSize = (fileSize) => {
  const size = Number(fileSize || 0)
  if (!size) {
    return '--'
  }
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(2)} MB`
  }
  if (size >= 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${size} B`
}

const resolveFileTag = (suffix) => {
  const value = String(suffix || '').toUpperCase()
  return value || 'FILE'
}

const resolveResourceIconSrc = (suffix) => {
  const value = String(suffix || '').toLowerCase()
  if (value === 'pdf') {
    return pdfIcon
  }
  if (['ppt', 'pptx'].includes(value)) {
    return pptIcon
  }
  if (['zip', 'rar', '7z'].includes(value)) {
    return zipIcon
  }
  if (['doc', 'docx'].includes(value)) {
    return wordIcon
  }
  if (['xls', 'xlsx', 'csv'].includes(value)) {
    return excelIcon
  }
  if (['txt', 'md'].includes(value)) {
    return txtIcon
  }
  if (isImageResource(value)) {
    return imageIcon
  }
  if (['mp4', 'm3u8', 'avi', 'mov'].includes(value)) {
    return videoIcon
  }
  return fileIcon
}

const formatDateTime = (value) => {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

const pickInitialLesson = () => {
  if (!flatLessons.value.length) {
    selectedLessonId.value = ''
    expandedChapterIds.value = []
    return
  }
  const lessonId = String(route.query.lessonId || '')
  const matchedLesson = flatLessons.value.find(
    (item) => item.lessonId === lessonId
  )
  if (matchedLesson) {
    selectedLessonId.value = matchedLesson.lessonId
    expandedChapterIds.value = matchedLesson.chapterId
      ? [matchedLesson.chapterId]
      : []
    resetPlayerTracking()
    return
  }
  const currentLessonId = String(courseDetail.value?.currentLessonId || '')
  const currentLearningLesson = flatLessons.value.find(
    (item) => item.lessonId === currentLessonId
  )
  if (currentLearningLesson) {
    selectedLessonId.value = currentLearningLesson.lessonId
    expandedChapterIds.value = currentLearningLesson.chapterId
      ? [currentLearningLesson.chapterId]
      : []
    resetPlayerTracking()
    return
  }
  const fallbackLesson =
    flatLessons.value.find((item) => item.videoFilePath) || flatLessons.value[0]
  selectedLessonId.value = fallbackLesson.lessonId
  expandedChapterIds.value = fallbackLesson.chapterId
    ? [fallbackLesson.chapterId]
    : []
  resetPlayerTracking()
}

const loadCourseDetail = async () => {
  loading.value = true
  const result = await getMyCourseDetail(route.params.courseId)
  loading.value = false
  if (!result) {
    courseDetail.value = null
    return
  }
  courseDetail.value = result
  pickInitialLesson()
}

const loadCurrentHomeworkDetail = async () => {
  const lessonId = currentLesson.value?.lessonId
  const paperId = currentLesson.value?.paperId
  if (!lessonId || !paperId) {
    currentHomeworkDetail.value = null
    return
  }
  const result = await getCourseHomeworkDetail({
    courseId: route.params.courseId,
    lessonId,
  })
  if (!result || currentLesson.value?.lessonId !== lessonId) {
    return
  }
  currentHomeworkDetail.value = result
}

watch(
  () => route.params.courseId,
  async () => {
    await flushStudyProgress()
    loadCourseDetail()
  }
)

watch(
  () => [
    route.params.courseId,
    currentLesson.value?.lessonId,
    currentLesson.value?.paperId,
  ],
  () => {
    loadCurrentHomeworkDetail()
  },
  { immediate: true }
)

onMounted(() => {
  loadCourseDetail()
})

onBeforeUnmount(() => {
  flushStudyProgress()
})
</script>

<style scoped>
.course-study {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.course-study__image-preview-anchor {
  position: fixed;
  width: 0;
  height: 0;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
}

.card {
  border: 1px solid #e7eef9;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 40px rgba(49, 87, 148, 0.06);
}

.course-study__state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  border: 1px solid #e7eef9;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  color: #71839d;
  font-size: 14px;
}

.course-study__state.is-empty {
  flex-direction: column;
  gap: 8px;
}

.course-study__state.is-empty strong {
  color: #173458;
  font-size: 18px;
}

.course-study__state.is-empty p {
  margin: 0;
}

.course-study__breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8091a8;
  font-size: 13px;
}

.course-study__breadcrumb button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #2d79ff;
  cursor: pointer;
  font-size: 13px;
}

.course-study__hero {
  display: grid;
  grid-template-columns: 176px minmax(0, 1fr) 150px;
  gap: 20px;
  align-items: center;
  padding: 18px 20px;
}

.course-study__hero-cover,
.course-study__hero-cover img,
.course-study__hero-cover-fallback {
  width: 176px;
  height: 120px;
  border-radius: 6px;
}

.course-study__hero-cover {
  overflow: hidden;
  background: linear-gradient(135deg, #dceaff 0%, #edf4ff 100%);
}

.course-study__hero-cover img {
  display: block;
  object-fit: cover;
}

.course-study__hero-cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at center,
      rgba(113, 177, 255, 0.22),
      transparent 22%),
    linear-gradient(135deg, #071d56 0%, #0d3f91 100%);
  color: #fff;
}

.course-study__hero-cover-fallback span {
  padding: 8px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 22px;
  font-weight: 700;
}

.course-study__hero-main h1 {
  margin: 0 0 12px;
  color: #173458;
  font-size: 22px;
}

.course-study__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-bottom: 16px;
  color: #6f839d;
  font-size: 13px;
}

.course-study__meta span,
.course-study__progress-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.course-study__meta .iconfont {
  font-size: 14px;
}

.course-study__progress-row {
  gap: 14px;
  color: #6f839d;
  font-size: 13px;
}

.course-study__progress {
  flex: 1;
  max-width: 380px;
  height: 6px;
  border-radius: 6px;
  background: #edf3fb;
  overflow: hidden;
}

.course-study__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
}

.course-study__hero-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: stretch;
}

.course-study__primary-btn,
.course-study__secondary-btn,
.course-study__ghost-btn,
.course-study__link-btn {
  border: 0;
  border-radius: 6px;
  cursor: pointer;
}

.course-study__primary-btn {
  min-width: 112px;
  height: 40px;
  padding: 0 18px;
  background: linear-gradient(135deg, #317dff 0%, #2569f3 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 12px 20px rgba(49, 125, 255, 0.2);
}

.course-study__primary-btn.is-small {
  min-width: 96px;
  height: 36px;
}

.course-study__secondary-btn,
.course-study__ghost-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  background: #fff;
  color: #516985;
  font-size: 14px;
  box-shadow: 0 0 0 1px #dde6f3 inset;
}

.course-study__secondary-btn.is-active {
  color: #ffb32b;
}

.course-study__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.course-study__content {
  min-width: 0;
  padding: 0 0 22px;
}

.course-study__tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  border-bottom: 1px solid #edf2fb;
}

.course-study__tab {
  position: relative;
  padding: 16px 10px 14px;
  border: 0;
  background: transparent;
  color: #6f839d;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.course-study__tab.is-active {
  color: #2d79ff;
}

.course-study__tab.is-active::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 0;
  height: 3px;
  border-radius: 6px;
  background: #2d79ff;
}

.course-study__panel {
  padding: 18px 20px 0;
}

.course-study__panel h2,
.course-study__section-title h3,
.course-study__article h3 {
  margin: 0;
  color: #173458;
  font-size: 15px;
}

.course-study__panel h2 {
  margin-bottom: 14px;
  font-size: 18px;
}

.course-study__player-actions,
.course-study__section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.course-study__player-actions {
  margin-top: 14px;
}

.course-study__article,
.course-study__info-list,
.course-study__assignment-list,
.course-study__record-list,
.course-study__resource-list,
.course-study__empty-panel,
.course-study__record-compact {
  margin-top: 22px;
}

.course-study__article {
  padding: 20px;
  border-radius: 6px;
  background: #fbfdff;
}

.course-study__article p {
  margin: 14px 0 0;
  color: #5f7492;
  line-height: 1.8;
  font-size: 14px;
}

.course-study__article ul {
  margin: 14px 0 0;
  padding-left: 18px;
  color: #4f6482;
  line-height: 1.9;
}

.course-study__resource-list,
.course-study__mini-resource-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-study__resource-item,
.course-study__mini-resource {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f7faff;
  cursor: pointer;
  text-align: left;
}

.course-study__resource-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.course-study__resource-icon.is-mini {
  width: 18px;
  height: 18px;
}

.course-study__resource-name,
.course-study__mini-resource-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #173458;
  font-size: 13px;
}

.course-study__resource-size,
.course-study__mini-resource-size {
  color: #7d8ea7;
  font-size: 12px;
  white-space: nowrap;
}

.course-study__empty-panel {
  padding: 18px 16px;
  border-radius: 6px;
  background: #fbfdff;
  color: #7d8ea7;
  font-size: 13px;
}

.course-study__empty-panel.is-side {
  margin-top: 0;
}

.course-study__info-list,
.course-study__assignment-list,
.course-study__record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-study__info-item,
.course-study__assignment-item,
.course-study__record-item {
  padding: 16px;
  border-radius: 6px;
  background: #fbfdff;
}

.course-study__info-item strong,
.course-study__assignment-item strong {
  display: block;
  margin-bottom: 8px;
  color: #173458;
  font-size: 15px;
}

.course-study__info-item p,
.course-study__assignment-item p {
  margin: 0 0 8px;
  color: #6f839d;
  font-size: 13px;
  line-height: 1.7;
}

.course-study__info-item span,
.course-study__record-item span {
  color: #8a99af;
  font-size: 12px;
}

.course-study__assignment-item,
.course-study__record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.course-study__assignment-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.course-study__assignment-copy {
  min-width: 0;
  flex: 1;
}

.course-study__assignment-side {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.course-study__assignment-copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-study__assignment-icon {
  margin-top: 2px;
  flex: 0 0 auto;
  color: #7f90a7;
  font-size: 14px;
}

.course-study__assignment-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 58px;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.course-study__assignment-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  height: 32px;
  padding: 0 12px;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
}

.course-study__assignment-action.is-orange {
  background: #fff1de;
  color: #ff982a;
}

.course-study__assignment-action.is-blue {
  background: #edf4ff;
  color: #2d79ff;
}

.course-study__assignment-status.is-orange {
  background: #fff1de;
  color: #ff982a;
}

.course-study__assignment-status.is-green {
  background: #e8fbef;
  color: #22a55b;
}

.course-study__assignment-status.is-red {
  background: #ffe8e9;
  color: #ff5960;
}

.course-study__record-compact {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-study__record-compact.is-side {
  margin-top: 0;
}

.course-study__record-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #7488a5;
  font-size: 13px;
}

.course-study__record-row strong {
  color: #173458;
  font-size: 14px;
  font-weight: 600;
}

.course-study__record-row.is-progress {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.course-study__record-progress-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.course-study__record-progress {
  height: 6px;
  border-radius: 6px;
  background: #edf3fb;
  overflow: hidden;
}

.course-study__record-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #3b86ff 0%, #256cf2 100%);
}

.course-study__aside {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 16px;
  align-self: start;
}

.course-study__side-card {
  padding: 18px;
}

.course-study__link-btn {
  padding: 0;
  background: transparent;
  color: #2d79ff;
  font-size: 13px;
}

.course-study__chapter-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.course-study__chapter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  margin-bottom: 8px;
}

.course-study__chapter-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #173458;
  font-size: 14px;
  font-weight: 700;
  min-width: 0;
}

.course-study__chapter-arrow {
  width: 8px;
  height: 8px;
  border-right: 2px solid #96a6bc;
  border-bottom: 2px solid #96a6bc;
  transform: rotate(45deg);
  transition: transform 0.2s ease;
}

.course-study__chapter-arrow.is-open {
  transform: rotate(225deg);
}

.course-study__chapter-lessons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-study__lesson {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border-radius: 6px;
  background: transparent;
  color: #5b7390;
  cursor: pointer;
  text-align: left;
}

.course-study__lesson.is-active {
  background: #edf4ff;
  color: #2d79ff;
}

.course-study__lesson.is-hovered {
  color: #2d79ff;
}

.course-study__lesson.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f9fafb;
}

.course-study__lesson.is-disabled:hover {
  background: #f9fafb;
}

.course-study__lesson.is-disabled .course-study__lesson-title {
  color: #a8b4c6;
}

.course-study__lesson-status.icon-lock {
  color: #d1d5db;
}

.course-study__lesson:hover {
  background: #f5f9ff;
}

.course-study__lesson-title {
  min-width: 0;
  flex: 1;
  font-size: 13px;
}

.course-study__lesson .iconfont {
  font-size: 14px;
  color: #a8b4c6;
}

.course-study__lesson-status.icon-completed {
  color: #22a55b;
}

.course-study__lesson.is-active .iconfont,
.course-study__lesson.is-hovered .iconfont {
  color: #2d79ff;
}

.course-study__lesson.is-active .course-study__lesson-status.icon-completed,
.course-study__lesson.is-hovered .course-study__lesson-status.icon-completed {
  color: #22a55b;
}

.course-study__mini-resource {
  padding: 8px 10px;
}

.course-study__assignment-list.is-side,
.course-study__record-list.is-side {
  margin-top: 0;
}

.course-study__assignment-list.is-side {
  gap: 8px;
}

.course-study__assignment-list.is-side .course-study__assignment-item {
  padding: 10px 12px;
  background: #f7faff;
}

.course-study__assignment-list.is-side .course-study__assignment-item strong {
  margin-bottom: 4px;
  font-size: 13px;
}

.course-study__assignment-list.is-side .course-study__assignment-item p {
  margin-bottom: 0;
  font-size: 12px;
}

.course-study__assignment-list.is-side .course-study__assignment-status {
  min-width: 52px;
  height: 24px;
  padding: 0 8px;
  font-size: 11px;
}

.course-study__assignment-list.is-side .course-study__assignment-action {
  min-width: 72px;
  height: 24px;
  padding: 0 8px;
  font-size: 11px;
}

@media (max-width: 1200px) {
  .course-study__main {
    grid-template-columns: 1fr;
  }

  .course-study__aside {
    position: static;
  }
}

@media (max-width: 960px) {
  .course-study__hero {
    grid-template-columns: 1fr;
  }

  .course-study__hero-cover,
  .course-study__hero-cover img,
  .course-study__hero-cover-fallback {
    width: 100%;
    height: 200px;
  }

  .course-study__tabs {
    overflow-x: auto;
  }

  .course-study__player-actions {
    flex-wrap: wrap;
  }

  .course-study__checkbox {
    width: 100%;
    margin-left: 0;
  }
}
</style>
