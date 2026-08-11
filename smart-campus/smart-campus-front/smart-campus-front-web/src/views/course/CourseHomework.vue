<template>
  <div class="course-homework">
    <div v-if="loading" class="course-homework__state">正在加载作业详情...</div>

    <div v-else-if="!detail" class="course-homework__state is-empty">
      <strong>作业详情加载失败</strong>
      <p>请返回课程页后重新进入。</p>
    </div>

    <template v-else>
      <nav class="course-homework__breadcrumb">
        <button type="button" @click="router.push('/courses')">我的课程</button>
        <span>/</span>
        <button type="button"
          @click="router.push(`/courses/${route.params.courseId}/study?lessonId=${detail.lessonId}`)">
          {{ detail.courseName }}
        </button>
        <span>/</span>
        <span>课后作业</span>
        <span>/</span>
        <span>{{ detail.paperName }}</span>
      </nav>

      <div class="course-homework__layout">
        <div class="course-homework__main">
          <section class="course-homework__summary card">
            <div class="course-homework__summary-icon">
              <div class="course-homework__summary-icon-sheet" />
            </div>

            <div class="course-homework__summary-main">
              <div class="course-homework__summary-head">
                <h1>{{ detail.paperName }}</h1>
                <button v-if="!detail.started && detail.editable" type="button"
                  class="course-homework__summary-start-btn" :disabled="starting" @click="handleStart">
                  {{ starting ? '开始中...' : '开始作答' }}
                </button>
                <span v-else class="course-homework__status"
                  :class="`is-${statusTheme}`">{{ detail.submitStatusText }}</span>
              </div>

              <div class="course-homework__summary-meta">
                <span>章节：{{ detail.chapterName || '-' }}</span>
                <span>课时：{{ detail.lessonName || '-' }}</span>
                <span>题目数量：{{ detail.questionCount || 0 }}题</span>
                <span>总分：{{ formatScore(detail.totalScore) }}分</span>
                <span>开始时间：{{ formatDateTime(detail.startedTime) || '未开始' }}</span>
                <span>提交时间：{{ formatDateTime(detail.submitTime) || '未提交' }}</span>
              </div>
            </div>
          </section>

          <section class="course-homework__paper card">
            <div class="course-homework__tabs">
              <button type="button" class="course-homework__tab" :class="{ 'is-active': activeTab === 'questions' }"
                @click="activeTab = 'questions'">
                题目列表
              </button>
              <button type="button" class="course-homework__tab" :class="{ 'is-active': activeTab === 'requirements' }"
                @click="activeTab = 'requirements'">
                作业要求
              </button>
            </div>

            <div v-if="activeTab === 'questions'" class="course-homework__paper-body">
              <PaperQuestionRenderer
                :sections="detail.sectionList || []"
                :answer-state="answerState"
                :can-edit="canEdit"
                :submitted="Boolean(showQuestionJudgeResult)"
                :saving-question-id="savingQuestionId"
                :resolve-section-title="resolveSectionTitle"
                @single-change="handleSingleChoiceGroupChange"
                @multi-change="handleMultipleChoiceGroupChange"
                @text-input="handleTextInput"
                @text-blur="handleTextBlur"
                @question-focus="handleQuestionFocus"
              />
            </div>

            <div v-else class="course-homework__requirement">
              <h3>作答说明</h3>
              <p>{{ requirementText }}</p>
              <ul>
                <li>系统会在切换选项、输入框失焦时自动保存当前答案。</li>
                <li>完成后可先保存草稿，再确认提交；提交后答案会被锁定。</li>
                <li>当前作业共 {{ detail.questionCount || 0 }} 题，总分 {{ formatScore(detail.totalScore) }} 分。</li>
              </ul>
            </div>
          </section>
        </div>

        <aside class="course-homework__aside">
          <section class="course-homework__side-card card">
            <h3>作业信息</h3>
            <div class="course-homework__info-list">
              <div class="course-homework__info-item">
                <span>作业状态：</span>
                <span class="course-homework__info-status"
                  :class="`is-${statusTheme}`">{{ detail.submitStatusText }}</span>
              </div>
              <div class="course-homework__info-item">
                <span>提交状态：</span>
                <strong>{{ detail.submitted ? '已提交' : '暂未提交' }}</strong>
              </div>
              <div class="course-homework__info-item">
                <span>开始作答：</span>
                <strong>{{ formatDateTime(detail.startedTime) || '未开始' }}</strong>
              </div>
              <div class="course-homework__info-item">
                <span>提交时间：</span>
                <strong>{{ formatDateTime(detail.submitTime) || '未提交' }}</strong>
              </div>
              <div class="course-homework__info-item">
                <span>总分：</span>
                <strong>{{ formatScore(detail.totalScore) }} 分</strong>
              </div>
              <div class="course-homework__info-item">
                <span>已得分：</span>
                <strong>{{ detail.submitted ? `${formatScore(totalScored)} 分` : '--' }}</strong>
              </div>
              <div class="course-homework__info-time">
                <span class="course-homework__info-time-label">已用时间：</span>
                <div class="course-homework__info-time-group">
                  <span class="course-homework__info-time-cell">{{ elapsedClock.hour }}</span>
                  <em>:</em>
                  <span class="course-homework__info-time-cell">{{ elapsedClock.minute }}</span>
                  <em>:</em>
                  <span class="course-homework__info-time-cell">{{ elapsedClock.second }}</span>
                </div>
              </div>
              <div class="course-homework__info-progress">
                <span>完成进度：</span>
                <strong>{{ detail.answeredCount || 0 }} / {{ detail.questionCount || 0 }} 题</strong>
                <div class="course-homework__progress">
                  <span :style="{ width: `${progressPercent}%` }" />
                </div>
              </div>
              <div v-if="detail.submitted" class="course-homework__info-item course-homework__info-item--history">
                <span>批改状态：</span>
                <strong>{{ detail.judgeStatusText || '待批改' }}</strong>
              </div>

              <div
                v-if="detail.teacherComment"
                class="course-homework__info-comment"
              >
                <span class="course-homework__info-comment-label">老师评语：</span>
                <p>{{ detail.teacherComment }}</p>
              </div>
            </div>
          </section>

          <section class="course-homework__side-card card">
            <h3>提交作业</h3>
            <textarea v-model="submitContent" class="course-homework__submit-textarea" :disabled="!canEdit"
              placeholder="可填写提交说明、答题补充等内容" />

            <button type="button" class="course-homework__action-btn is-primary"
              :disabled="!detail.editable || !detail.submitId || submitting" @click="handleSubmit">
              {{ submitting ? '提交中...' : '提交作业' }}
            </button>

            <div class="course-homework__submit-footer">
              <span>最近保存：{{ latestSaveText }}</span>
              <button type="button" class="course-homework__draft-btn"
                :disabled="!detail.editable || !detail.submitId || draftSaving" @click="handleSaveDraft">
                {{ draftSaving ? '保存中...' : '保存草稿' }}
              </button>
            </div>
          </section>

        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import PaperQuestionRenderer from '@/components/PaperQuestionRenderer.vue'
import {
  getCourseHomeworkDetail,
  saveCourseHomeworkAnswer,
  saveCourseHomeworkDraft,
  startCourseHomework,
  submitCourseHomework,
} from '@/api/course'
import Message from '@/utils/Message'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const starting = ref(false)
const submitting = ref(false)
const draftSaving = ref(false)
const savingQuestionId = ref(null)
const detail = ref(null)
const answerState = ref({})
const submitContent = ref('')
const activeQuestionId = ref(null)
const touchedQuestionMap = ref({})
const activeTab = ref('questions')
const lastSavedAt = ref('')
const clockTick = ref(Date.now())
let clockTimer = null

const flatQuestionList = computed(() => {
  const result = []
  let orderNo = 1
  for (const section of detail.value?.sectionList || []) {
    const questionList = section.questionList || []
    for (const question of questionList) {
      result.push({
        ...question,
        orderNo,
        sectionName: section.sectionName,
        sectionTotalScore: section.totalScore,
        sectionQuestionCount: questionList.length,
      })
      orderNo += 1
    }
  }
  return result
})

const canEdit = computed(() =>
  Boolean(detail.value?.editable && detail.value?.submitId)
)

const progressPercent = computed(() => {
  if (!detail.value?.questionCount) {
    return 0
  }
  return Math.round(
    (Number(detail.value.answeredCount || 0) /
      Number(detail.value.questionCount || 1)) *
      100
  )
})

const elapsedSeconds = computed(() => {
  clockTick.value
  const startedTime = detail.value?.startedTime
  const baseUsedSeconds = Number(detail.value?.usedSeconds || 0)
  if (!startedTime || detail.value?.submitted) {
    return baseUsedSeconds
  }
  const startedAt = new Date(startedTime).getTime()
  if (Number.isNaN(startedAt)) {
    return baseUsedSeconds
  }
  return Math.max(baseUsedSeconds, Math.floor((Date.now() - startedAt) / 1000))
})

const elapsedClock = computed(() => {
  const totalSeconds = Math.max(0, Number(elapsedSeconds.value || 0))
  const hour = String(Math.floor(totalSeconds / 3600)).padStart(2, '0')
  const minute = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
  const second = String(totalSeconds % 60).padStart(2, '0')
  return { hour, minute, second }
})

const totalScored = computed(() => {
  let total = 0
  for (const question of flatQuestionList.value) {
    total += Number(question.finalScore || 0)
  }
  return total
})

const showQuestionJudgeResult = computed(() => Boolean(detail.value?.submitted))

const statusTheme = computed(() => {
  const status = Number(detail.value?.submitStatus || 0)
  if (status === 3) {
    return 'green'
  }
  if (status === 2) {
    return 'blue'
  }
  if (status === 1) {
    return 'orange'
  }
  return 'gray'
})

const requirementText = computed(() => {
  if (detail.value?.submitContent) {
    return detail.value.submitContent
  }
  return `${
    detail.value?.paperName || '当前作业'
  } 需要围绕本节学习内容完成作答，请按题目顺序认真填写并及时提交。`
})

const latestSaveText = computed(
  () => lastSavedAt.value || formatDateTime(detail.value?.startedTime) || '暂无'
)

const unansweredQuestionList = computed(() =>
  flatQuestionList.value.filter((question) => !isQuestionAnswered(question))
)

const applySubmitState = (result, options = {}) => {
  if (!detail.value || !result) {
    return
  }
  const { forceAnswering = false, startedTime } = options
  detail.value.submitId = result.submitId ?? detail.value.submitId
  detail.value.judgeStatus = result.judgeStatus ?? detail.value.judgeStatus
  detail.value.judgeStatusText = result.judgeStatusText || detail.value.judgeStatusText
  detail.value.editable = true
  detail.value.started = true
  detail.value.submitted = false
  detail.value.startedTime = startedTime || detail.value.startedTime || new Date()
  if (forceAnswering) {
    detail.value.submitStatus = 1
    detail.value.submitStatusText = '作答中'
    return
  }
  detail.value.submitStatus = result.submitStatus ?? detail.value.submitStatus
  detail.value.submitStatusText = result.submitStatusText || detail.value.submitStatusText
}

const loadDetail = async () => {
  loading.value = true
  const result = await getCourseHomeworkDetail({
    courseId: route.params.courseId,
    lessonId: route.params.lessonId,
  })
  loading.value = false
  if (!result) {
    return
  }
  detail.value = result
  submitContent.value = result.submitContent || ''
  const answerMap = {}
  const touchedMap = {}
  for (const question of flatQuestionList.value) {
    answerMap[question.paperQuestionId] = parseAnswerValue(question)
    touchedMap[question.paperQuestionId] = Boolean(question.answerContent)
  }
  answerState.value = answerMap
  touchedQuestionMap.value = touchedMap
  activeQuestionId.value = flatQuestionList.value[0]?.paperQuestionId || null
}

const parseAnswerValue = (question) => {
  if (Number(question.questionType) === 2) {
    return splitMultiAnswer(question.answerContent)
  }
  return question.answerContent || ''
}

const splitMultiAnswer = (value) => {
  if (!value) {
    return []
  }
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const normalizeAnswerValue = (question) => {
  const currentValue = answerState.value[question.paperQuestionId]
  if (Number(question.questionType) === 2) {
    return (Array.isArray(currentValue) ? currentValue : [])
      .map((item) => String(item))
      .sort((a, b) => Number(a) - Number(b))
      .join(',')
  }
  return String(currentValue || '').trim()
}

const isChoiceQuestion = (question) =>
  [1, 2, 3].includes(Number(question.questionType))

const questionOptions = (question) => {
  if (Array.isArray(question.optionList) && question.optionList.length) {
    return question.optionList
  }
  if (Number(question.questionType) === 3) {
    return [
      { optionId: 'T', optionKey: 'A', optionContent: '正确' },
      { optionId: 'F', optionKey: 'B', optionContent: '错误' },
    ]
  }
  return []
}

const getSingleChoiceValue = (question) =>
  answerState.value[question.paperQuestionId] || ''

const isMultiOptionChecked = (question, optionId) => {
  const value = answerState.value[question.paperQuestionId]
  return Array.isArray(value) && value.map(String).includes(String(optionId))
}

const isOptionSelected = (question, optionId) => {
  if (Number(question.questionType) === 2) {
    return isMultiOptionChecked(question, optionId)
  }
  return String(getSingleChoiceValue(question)) === String(optionId)
}

const handleStart = async () => {
  starting.value = true
  const result = await startCourseHomework({
    courseId: route.params.courseId,
    lessonId: route.params.lessonId,
  })
  starting.value = false
  if (!result) {
    return
  }
  applySubmitState(result, {
    forceAnswering: true,
    startedTime: new Date(),
  })
  lastSavedAt.value = formatDateTime(new Date())
  Message.success('作业已开始')
}

const handleQuestionFocus = async (question) => {
  activeQuestionId.value = question.paperQuestionId
  if (!canEdit.value || touchedQuestionMap.value[question.paperQuestionId]) {
    return
  }
  const result = await saveAnswer(
    question,
    normalizeAnswerValue(question),
    false
  )
  if (result) {
    touchedQuestionMap.value[question.paperQuestionId] = true
  }
}

const handleTextInput = (question, value) => {
  answerState.value = {
    ...answerState.value,
    [question.paperQuestionId]: value,
  }
}

const saveAnswer = async (question, answerContent, showMessage = false) => {
  if (!detail.value?.submitId || !detail.value?.editable) {
    return false
  }
  savingQuestionId.value = question.paperQuestionId
  const result = await saveCourseHomeworkAnswer({
    submitId: detail.value.submitId,
    paperQuestionId: question.paperQuestionId,
    questionId: question.questionId,
    answerContent,
  })
  savingQuestionId.value = null
  if (!result) {
    return false
  }
  detail.value.submitStatus = result.submitStatus
  detail.value.submitStatusText = result.submitStatusText
  detail.value.judgeStatus = result.judgeStatus
  detail.value.judgeStatusText = result.judgeStatusText
  touchedQuestionMap.value[question.paperQuestionId] = true
  syncAnsweredCount()
  lastSavedAt.value = formatDateTime(new Date())
  if (showMessage) {
    Message.success('答案已保存')
  }
  return true
}

const handleSingleChoiceChange = async (question, optionId) => {
  answerState.value = {
    ...answerState.value,
    [question.paperQuestionId]: String(optionId),
  }
  await saveAnswer(question, normalizeAnswerValue(question))
}

const handleSingleChoiceGroupChange = async (question, value) => {
  await handleSingleChoiceChange(question, String(value))
}

const handleMultipleChoiceGroupChange = async (question, value) => {
  answerState.value = {
    ...answerState.value,
    [question.paperQuestionId]: Array.isArray(value)
      ? value.map((item) => String(item))
      : [],
  }
  await saveAnswer(question, normalizeAnswerValue(question))
}

const handleTextBlur = async (question) => {
  await saveAnswer(question, normalizeAnswerValue(question))
}

const handleSaveDraft = async () => {
  if (!detail.value?.submitId || !detail.value?.editable) {
    return
  }
  draftSaving.value = true
  const result = await saveCourseHomeworkDraft({
    submitId: detail.value.submitId,
    usedSeconds: elapsedSeconds.value,
    submitContent: submitContent.value,
  })
  draftSaving.value = false
  if (!result) {
    return
  }
  detail.value.submitStatus = result.submitStatus
  detail.value.submitStatusText = result.submitStatusText
  detail.value.judgeStatus = result.judgeStatus
  detail.value.judgeStatusText = result.judgeStatusText
  detail.value.usedSeconds = elapsedSeconds.value
  lastSavedAt.value = formatDateTime(new Date())
  Message.success('草稿已保存')
}

const handleSubmit = async () => {
  if (!detail.value?.submitId || !detail.value?.editable) {
    return
  }
  if (unansweredQuestionList.value.length) {
    const unansweredText = unansweredQuestionList.value
      .map((question) => `第 ${question.orderNo} 题：${question.questionTitle}`)
      .join('<br/>')
    try {
      await ElMessageBox.confirm(
        `以下题目尚未作答：<br/>${unansweredText}<br/><br/>确认仍要提交吗？`,
        '未作答提示',
        {
          confirmButtonText: '仍然提交',
          cancelButtonText: '继续作答',
          type: 'warning',
          dangerouslyUseHTMLString: true,
        }
      )
    } catch {
      return
    }
  }
  submitting.value = true
  const result = await submitCourseHomework({
    submitId: detail.value.submitId,
    usedSeconds: elapsedSeconds.value,
    submitContent: submitContent.value,
  })
  submitting.value = false
  if (!result) {
    return
  }
  Message.success('作业提交成功')
  await loadDetail()
}

const syncAnsweredCount = () => {
  if (!detail.value) {
    return
  }
  detail.value.answeredCount = flatQuestionList.value.filter((question) =>
    isQuestionAnswered(question)
  ).length
}

const isQuestionAnswered = (question) => Boolean(normalizeAnswerValue(question))

const questionAnsweredText = (question) =>
  isQuestionAnswered(question) ? '已作答' : '未作答'

const questionOrder = (section, index) => {
  const currentQuestion = section.questionList[index]
  return (
    flatQuestionList.value.find(
      (item) => item.paperQuestionId === currentQuestion.paperQuestionId
    )?.orderNo || index + 1
  )
}

const resolveQuestionTypeText = (questionType) => {
  if (Number(questionType) === 1) {
    return '单选题'
  }
  if (Number(questionType) === 2) {
    return '多选题'
  }
  if (Number(questionType) === 3) {
    return '判断题'
  }
  return '简答题'
}

const resolveSectionTitle = (section, sectionIndex) =>
  `${toChineseSection(sectionIndex + 1)}、${section.sectionName || resolveQuestionTypeText(section.questionList?.[0]?.questionType)}`

const toChineseSection = (order) => {
  const list = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return list[order - 1] || `${order}`
}

const judgeStatusText = (judgeStatus) => {
  if (Number(judgeStatus) === 1) {
    return '已自动判分'
  }
  if (Number(judgeStatus) === 2) {
    return '待人工批改'
  }
  if (Number(judgeStatus) === 3) {
    return '人工批改完成'
  }
  return '未判题'
}

const isObjectiveQuestion = (question) => [1, 2, 3].includes(Number(question?.questionType))

const questionResultText = (question) => {
  if (!showQuestionJudgeResult.value) {
    return ''
  }
  if (isObjectiveQuestion(question)) {
    return Number(question.finalScore || 0) >= Number(question.questionScore || 0)
      ? '回答正确'
      : '回答错误'
  }
  if (Number(question.judgeStatus) === 3) {
    return '已批改'
  }
  return '待批改'
}

const questionResultTheme = (question) => {
  if (isObjectiveQuestion(question)) {
    return Number(question.finalScore || 0) >= Number(question.questionScore || 0)
      ? 'is-correct'
      : 'is-wrong'
  }
  return Number(question.judgeStatus) === 3 ? 'is-reviewed' : 'is-pending'
}

const formatScore = (value) => Number(value || 0).toFixed(0)

const formatDateTime = (value) => {
  if (!value) {
    return ''
  }
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value).replace('T', ' ').slice(0, 19)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

watch(
  () => [route.params.courseId, route.params.lessonId],
  () => {
    loadDetail()
  }
)

onMounted(() => {
  clockTimer = window.setInterval(() => {
    clockTick.value = Date.now()
  }, 1000)
  loadDetail()
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.course-homework {
  display: flex;
  flex-direction: column;
  gap: 18px;
  color: #23314d;
}

.course-homework__state {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #edf1f8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  color: #6c7d96;
}

.course-homework__state.is-empty {
  flex-direction: column;
  gap: 8px;
}

.course-homework__breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #8a97ab;
  font-size: 13px;
}

.course-homework__breadcrumb button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #2d79ff;
  cursor: pointer;
  font-size: 13px;
}

.course-homework__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
  align-items: start;
}

.course-homework__main,
.course-homework__aside {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.course-homework__aside {
  position: sticky;
  top: 20px;
  align-self: start;
}

.card {
  border: 1px solid #edf1f8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 34px rgba(42, 77, 138, 0.06);
}

.course-homework__summary {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 22px;
  padding: 20px 24px;
  align-items: center;
}

.course-homework__summary-icon {
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: linear-gradient(180deg, #eff5ff 0%, #e6efff 100%);
}

.course-homework__summary-icon-sheet {
  position: relative;
  width: 44px;
  height: 56px;
  border-radius: 6px;
  background: linear-gradient(180deg, #4d95ff 0%, #2f71f1 100%);
  box-shadow: 0 10px 20px rgba(48, 111, 241, 0.24);
}

.course-homework__summary-icon-sheet::before,
.course-homework__summary-icon-sheet::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 4px;
}

.course-homework__summary-icon-sheet::before {
  top: 12px;
  height: 4px;
}

.course-homework__summary-icon-sheet::after {
  top: 22px;
  height: 4px;
  box-shadow: 0 10px 0 rgba(255, 255, 255, 0.96);
}

.course-homework__summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.course-homework__summary-head h1 {
  margin: 0;
  color: #1e2a44;
  font-size: 18px;
  font-weight: 700;
}

.course-homework__summary-start-btn {
  min-width: 104px;
  height: 36px;
  padding: 0 16px;
  border: 0;
  border-radius: 6px;
  background: linear-gradient(90deg, #2f7aff 0%, #2468f3 100%);
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 12px 22px rgba(47, 106, 243, 0.2);
}

.course-homework__summary-start-btn:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.course-homework__summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 28px;
  margin-top: 16px;
  color: #586983;
  font-size: 14px;
}

.course-homework__status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 82px;
  height: 32px;
  padding: 0 14px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
}

.course-homework__status.is-green {
  color: #13a05f;
  background: #eafaf1;
}

.course-homework__status.is-blue {
  color: #2d79ff;
  background: #edf4ff;
}

.course-homework__status.is-orange {
  color: #ff8b28;
  background: #fff3e5;
}

.course-homework__status.is-gray {
  color: #7d8ea7;
  background: #f4f6fb;
}

.course-homework__paper {
  overflow: hidden;
}

.course-homework__tabs {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 18px;
  border-bottom: 1px solid #edf1f7;
}

.course-homework__tab {
  position: relative;
  height: 54px;
  padding: 0 12px;
  border: 0;
  background: transparent;
  color: #6d7d95;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
}

.course-homework__tab.is-active {
  color: #2d79ff;
}

.course-homework__tab.is-active::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 0;
  height: 3px;
  border-radius: 6px;
  background: #2d79ff;
}

.course-homework__paper-body {
  padding: 24px;
}

.course-homework__section-title {
  margin-bottom: 22px;
  color: #23314d;
  font-size: 16px;
  font-weight: 700;
}

.course-homework__section-block + .course-homework__section-block {
  margin-top: 32px;
}

.course-homework__question-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.course-homework__question {
  padding: 4px 0 0;
}

.course-homework__question-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.course-homework__question-title {
  display: flex;
  gap: 14px;
}

.course-homework__question-index {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #2f7aff 0%, #2468f3 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.course-homework__question-title h2 {
  margin: 0 0 10px;
  color: #1f2d49;
  font-size: 18px;
  line-height: 1.6;
}

.course-homework__question-type-inline {
  color: #394966;
  font-weight: 400;
}

.course-homework__question-score-inline {
  margin-left: 8px;
  color: #7082a1;
  font-size: 14px;
  font-weight: 500;
}

.course-homework__option-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}

.course-homework__option {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 5px;
  border-radius: 6px;
  background: #fff;
  color: #394966;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.course-homework__option:hover {
  background: #f6f9ff;
}

.course-homework__option.is-selected {
  background: linear-gradient(90deg, #f5f9ff 0%, #eff5ff 100%);
  color: #2d79ff;
}

.course-homework__option.is-disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.course-homework__option input {
  margin: 0;
}

.course-homework__option :deep(.el-checkbox) {
  margin-right: 0;
  height: auto;
}

.course-homework__option :deep(.el-checkbox__label) {
  display: none;
}

.course-homework__option :deep(.el-checkbox__input) {
  line-height: 1;
}

.course-homework__option-text {
  flex: 1;
  line-height: 1.5;
}

.course-homework__textarea-wrap textarea,
.course-homework__submit-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e4ebf7;
  border-radius: 6px;
  background: #fbfdff;
  color: #253451;
  resize: vertical;
  outline: none;
}

.course-homework__textarea-wrap textarea {
  min-height: 180px;
  padding: 16px;
}

.course-homework__question-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #7f8ea4;
  font-size: 13px;
}

.course-homework__question-result {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7faff;
}

.course-homework__question-result-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.course-homework__question-result-label {
  color: #7f8ea4;
  font-size: 13px;
}

.course-homework__question-result-score {
  color: #24304a;
  font-size: 13px;
}

.course-homework__question-result-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.course-homework__question-result-tag.is-correct {
  background: #edf8ee;
  color: #33a25b;
}

.course-homework__question-result-tag.is-wrong {
  background: #fff2f0;
  color: #d9574a;
}

.course-homework__question-result-tag.is-reviewed {
  background: #eef4ff;
  color: #2d79ff;
}

.course-homework__question-result-tag.is-pending {
  background: #fff5e8;
  color: #d78727;
}

.course-homework__requirement {
  padding: 24px;
  color: #536580;
  line-height: 1.9;
}

.course-homework__requirement h3 {
  margin: 0 0 14px;
  color: #1f2d49;
  font-size: 18px;
}

.course-homework__requirement p {
  margin: 0 0 12px;
}

.course-homework__requirement ul {
  margin: 0;
  padding-left: 18px;
}

.course-homework__side-card {
  padding: 20px 22px;
}

.course-homework__side-card h3 {
  margin: 0 0 18px;
  color: #1f2d49;
  font-size: 18px;
}

.course-homework__info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.course-homework__info-item,
.course-homework__info-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667790;
  font-size: 14px;
}

.course-homework__info-item strong,
.course-homework__info-progress strong {
  color: #2a3854;
  font-weight: 600;
}

.course-homework__info-item--history {
  align-items: flex-start;
}

.course-homework__info-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
}

.course-homework__info-status.is-green {
  color: #13a05f;
  background: #eafaf1;
}

.course-homework__info-status.is-blue {
  color: #2d79ff;
  background: #edf4ff;
}

.course-homework__info-status.is-orange {
  color: #ff8b28;
  background: #fff3e5;
}

.course-homework__info-status.is-gray {
  color: #7d8ea7;
  background: #f4f6fb;
}

.course-homework__info-item strong.is-green {
  color: #13a05f;
}

.course-homework__info-item strong.is-blue {
  color: #2d79ff;
}

.course-homework__info-item strong.is-orange {
  color: #ff8b28;
}

.course-homework__info-item strong.is-gray {
  color: #7d8ea7;
}

.course-homework__info-progress {
  gap: 10px;
}

.course-homework__info-time {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-homework__info-time-label {
  color: #667790;
  font-size: 14px;
}

.course-homework__info-time-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.course-homework__info-time-group em {
  color: #ff5b52;
  font-style: normal;
  font-size: 20px;
}

.course-homework__info-time-cell {
  min-width: 38px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eef1f7;
  border-radius: 6px;
  background: #fff;
  color: #ff4f46;
  font-size: 17px;
  font-weight: 700;
  box-shadow: 0 8px 16px rgba(54, 92, 151, 0.05);
}

.course-homework__progress {
  flex: 1;
  min-width: 120px;
  height: 6px;
  border-radius: 999px;
  background: #edf2f8;
  overflow: hidden;
}

.course-homework__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #d7deea 0%, #c8d3e6 100%);
}

.course-homework__info-comment {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-homework__info-comment-label {
  color: #667790;
  font-size: 14px;
}

.course-homework__info-comment p {
  margin: 0;
  color: #24304a;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.course-homework__submit-textarea {
  min-height: 110px;
  padding: 14px 16px;
}

.course-homework__action-btn {
  width: 100%;
  height: 40px;
  margin-top: 18px;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
}

.course-homework__action-btn.is-primary {
  background: linear-gradient(90deg, #2f7aff 0%, #2468f3 100%);
  color: #fff;
  box-shadow: 0 12px 22px rgba(47, 106, 243, 0.2);
}

.course-homework__action-btn:disabled,
.course-homework__draft-btn:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.course-homework__submit-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #65758f;
  font-size: 14px;
}

.course-homework__draft-btn {
  min-width: 96px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid #dce5f3;
  border-radius: 6px;
  background: #fff;
  color: #2d79ff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.course-homework__history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-homework__history-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f8fbff;
  color: #7787a0;
  font-size: 13px;
}

.course-homework__history-item strong {
  flex: 0 0 65px;
  color: #1f2d49;
  font-size: 13px;
  line-height: 1.5;
}

.course-homework__history-item span {
  flex: 1;
  min-width: 0;
  line-height: 1.5;
  word-break: break-all;
}

.course-homework__history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 116px;
  color: #8e9cb0;
  font-size: 14px;
}

.course-homework__history-empty-icon {
  position: relative;
  width: 38px;
  height: 44px;
  border: 2px solid #ccd5e3;
  border-radius: 6px;
}

.course-homework__history-empty-icon::before,
.course-homework__history-empty-icon::after {
  content: '';
  position: absolute;
  left: 9px;
  right: 9px;
  height: 2px;
  background: #ccd5e3;
}

.course-homework__history-empty-icon::before {
  top: 14px;
}

.course-homework__history-empty-icon::after {
  top: 22px;
}

@media (max-width: 1200px) {
  .course-homework__layout {
    grid-template-columns: 1fr;
  }

  .course-homework__aside {
    position: static;
  }
}

@media (max-width: 900px) {
  .course-homework__question-head,
  .course-homework__submit-footer,
  .course-homework__info-progress,
  .course-homework__info-time {
    flex-direction: column;
    align-items: flex-start;
  }

  .course-homework__summary {
    grid-template-columns: 1fr;
  }

  .course-homework__summary-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .course-homework__option-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .course-homework__paper-body,
  .course-homework__requirement,
  .course-homework__side-card {
    padding: 18px 16px;
  }

  .course-homework__tabs {
    overflow-x: auto;
  }
  .course-homework__summary-meta {
    gap: 10px 16px;
    font-size: 13px;
  }
}
</style>
