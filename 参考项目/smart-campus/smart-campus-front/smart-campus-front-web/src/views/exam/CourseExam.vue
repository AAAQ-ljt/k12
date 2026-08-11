<template>
  <div class="course-exam">
    <div v-if="loading" class="course-exam__state">正在加载考试详情...</div>

    <div v-else-if="!detail" class="course-exam__state is-empty">
      <strong>考试详情加载失败</strong>
      <p>请返回在线考试后重新进入。</p>
    </div>

    <template v-else>
      <nav class="course-exam__breadcrumb">
        <button type="button" @click="router.push('/exams')">在线考试</button>
        <span>/</span>
        <span>{{ detail.examName }}</span>
      </nav>

      <div class="course-exam__layout">
        <div class="course-exam__main">
          <section class="course-exam__summary card">
            <div class="course-exam__summary-mark">
              <span />
            </div>

            <div class="course-exam__summary-main">
              <div class="course-exam__summary-head">
                <div>
                  <p class="course-exam__summary-subtitle">在线考试</p>
                  <h1>{{ detail.examName }}</h1>
                </div>
                <button v-if="!detail.started && detail.editable" type="button" class="course-exam__summary-start-btn"
                  :disabled="starting" @click="handleStart">
                  {{ starting ? '开始中...' : '开始考试' }}
                </button>
                <span v-else class="course-exam__status" :class="`is-${statusTheme}`">
                  {{ detail.examStatusText || detail.submitStatusText }}
                </span>
              </div>

              <div class="course-exam__summary-meta">
                <span>课程：{{ detail.courseName || '-' }}</span>
                <span>试卷：{{ detail.paperName || '-' }}</span>
                <span>题目：{{ detail.questionCount || 0 }}题</span>
                <span>总分：{{ formatScore(detail.totalScore) }}分</span>
                <span>开始时间：{{ formatDateTime(detail.startTime) || '-' }}</span>
                <span>结束时间：{{ formatDateTime(detail.endTime) || '-' }}</span>
                <span>考试时长：{{ examDurationText }}</span>
              </div>
            </div>
          </section>

          <section class="course-exam__paper card">
            <header class="course-exam__paper-head">
              <h2>{{ detail.paperName || detail.examName }}</h2>
              <div class="course-exam__paper-meta">
                <span>总分：{{ formatScore(detail.totalScore) }} 分</span>
                <span>时长：{{ examDurationText }}</span>
              </div>
              <p>请认真审题并独立完成答卷，系统会自动保存你的作答内容。</p>
            </header>

            <div class="course-exam__paper-body">
              <PaperQuestionRenderer
                :sections="detail.sectionList || []"
                :answer-state="answerState"
                :can-edit="canEdit"
                :submitted="Boolean(detail.submitted)"
                :saving-question-id="savingQuestionId"
                :resolve-section-title="resolveSectionTitle"
                @single-change="handleSingleChoiceGroupChange"
                @multi-change="handleMultipleChoiceGroupChange"
                @text-input="handleTextInput"
                @text-blur="handleTextBlur"
                @question-focus="handleQuestionFocus"
              />
            </div>
          </section>
        </div>

        <aside class="course-exam__aside">
          <section class="course-exam__side-card card">
            <h3>考试信息</h3>
            <div class="course-exam__info-list">
              <div class="course-exam__info-item">
                <span>考试状态：</span>
                <span class="course-exam__info-status" :class="`is-${statusTheme}`">
                  {{ detail.examStatusText || '-' }}
                </span>
              </div>
              <div class="course-exam__info-item">
                <span>提交状态：</span>
                <strong>{{ detail.submitStatusText || '-' }}</strong>
              </div>
              <div class="course-exam__info-item">
                <span>批改状态：</span>
                <strong>{{ detail.judgeStatusText || '-' }}</strong>
              </div>
              <div class="course-exam__info-item">
                <span>开始作答：</span>
                <strong>{{ formatDateTime(detail.startedTime) || '未开始' }}</strong>
              </div>
              <div class="course-exam__info-item">
                <span>提交时间：</span>
                <strong>{{ formatDateTime(detail.submitTime) || '未提交' }}</strong>
              </div>
              <div class="course-exam__info-time">
                <span class="course-exam__info-time-label">剩余时间：</span>
                <div class="course-exam__info-time-group">
                  <span class="course-exam__info-time-cell">{{ remainClock.hour }}</span>
                  <em>:</em>
                  <span class="course-exam__info-time-cell">{{ remainClock.minute }}</span>
                  <em>:</em>
                  <span class="course-exam__info-time-cell">{{ remainClock.second }}</span>
                </div>
              </div>
              <div class="course-exam__info-time">
                <span class="course-exam__info-time-label">已用时间：</span>
                <div class="course-exam__info-time-group">
                  <span class="course-exam__info-time-cell is-used">{{ usedClock.hour }}</span>
                  <em>:</em>
                  <span class="course-exam__info-time-cell is-used">{{ usedClock.minute }}</span>
                  <em>:</em>
                  <span class="course-exam__info-time-cell is-used">{{ usedClock.second }}</span>
                </div>
              </div>
              <div class="course-exam__info-progress">
                <span>完成进度：</span>
                <strong>{{ detail.answeredCount || 0 }} / {{ detail.questionCount || 0 }} 题</strong>
                <div class="course-exam__progress">
                  <span :style="{ width: `${progressPercent}%` }" />
                </div>
              </div>
              <div v-if="detail.teacherComment" class="course-exam__info-comment">
                <span class="course-exam__info-comment-label">老师评语：</span>
                <p>{{ detail.teacherComment }}</p>
              </div>
            </div>
          </section>

          <section class="course-exam__side-card card">
            <h3>答题卡</h3>
            <div class="course-exam__sheet">
              <div v-for="question in flatQuestionList" :key="question.paperQuestionId" class="course-exam__sheet-item"
                :class="{
                  'is-active': activeQuestionId === question.paperQuestionId,
                  'is-answered': isQuestionAnswered(question),
                }" @click="scrollToQuestion(question.paperQuestionId)">
                {{ question.orderNo }}
              </div>
            </div>
          </section>

          <section class="course-exam__side-card card">
            <h3>提交考试</h3>
            <textarea v-model="submitContent" class="course-exam__submit-textarea" :disabled="!canEdit"
              placeholder="可填写补充说明" />

            <button type="button" class="course-exam__action-btn is-primary"
              :disabled="!canEdit || !detail.submitId || submitting" @click="handleSubmit">
              {{ submitting ? '提交中...' : '提交试卷' }}
            </button>

            <button type="button" class="course-exam__draft-btn" :disabled="!canEdit || !detail.submitId || draftSaving"
              @click="handleSaveDraft">
              {{ draftSaving ? '保存中...' : '保存草稿' }}
            </button>
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import PaperQuestionRenderer from '@/components/PaperQuestionRenderer.vue'
import {
  getCourseExamDetail,
  startCourseExam,
  saveCourseExamAnswer,
  saveCourseExamDraft,
  submitCourseExam,
  normalizeExamDetail,
} from '@/api/exam'
import Message from '@/utils/Message'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const starting = ref(false)
const submitting = ref(false)
const draftSaving = ref(false)
const detail = ref(null)
const answerState = ref({})
const savingQuestionId = ref(null)
const submitContent = ref('')
const remainSeconds = ref(0)
const usedSeconds = ref(0)
const activeQuestionId = ref(null)
let timer = null

const canEdit = computed(() =>
  Boolean(detail.value?.editable && detail.value?.submitId)
)

const flatQuestionList = computed(() => {
  const result = []
  let orderNo = 1
  for (const section of detail.value?.sectionList || []) {
    for (const question of section.questionList || []) {
      result.push({
        ...question,
        orderNo,
        sectionName: section.sectionName,
      })
      orderNo += 1
    }
  }
  return result
})

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

const statusTheme = computed(() => {
  if (detail.value?.submitted) {
    return 'green'
  }
  if (
    Number(detail.value?.submitStatus) === 2 ||
    Number(detail.value?.submitStatus) === 1
  ) {
    return 'blue'
  }
  if (detail.value?.editable) {
    return 'orange'
  }
  return 'gray'
})

const remainClock = computed(() => formatClock(remainSeconds.value))
const usedClock = computed(() => formatClock(usedSeconds.value))
const examDurationText = computed(() =>
  formatExamDuration(detail.value?.startTime, detail.value?.endTime)
)

const formatScore = (value) => Number(value || 0).toFixed(0)

const formatClock = (seconds) => {
  const total = Math.max(0, Number(seconds || 0))
  const hour = String(Math.floor(total / 3600)).padStart(2, '0')
  const minute = String(Math.floor((total % 3600) / 60)).padStart(2, '0')
  const second = String(total % 60).padStart(2, '0')
  return { hour, minute, second }
}

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

const formatExamDuration = (startTime, endTime) => {
  if (!startTime || !endTime) {
    return '-'
  }
  const start = startTime instanceof Date ? startTime : new Date(startTime)
  const end = endTime instanceof Date ? endTime : new Date(endTime)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end <= start) {
    return '-'
  }
  return `${Math.round((end.getTime() - start.getTime()) / 60000)} 分钟`
}

const handleTextInput = (question, value) => {
  answerState.value[question.paperQuestionId] = value
}

const handleQuestionFocus = (question) => {
  activeQuestionId.value = question.paperQuestionId
}

const resolveQuestionTypeText = (questionType, questionTypeText) => {
  if (questionTypeText) {
    return questionTypeText
  }
  if (Number(questionType) === 1) return '单选题'
  if (Number(questionType) === 2) return '多选题'
  if (Number(questionType) === 3) return '判断题'
  if (Number(questionType) === 4) return '简答题'
  return '题目'
}

const resolveSectionTitle = (section, sectionIndex) => {
  const prefix = `${toChineseSection(sectionIndex + 1)}、`
  if (section.sectionName) {
    return `${prefix}${section.sectionName}`
  }
  const firstQuestion = section.questionList?.[0]
  return `${prefix}${resolveQuestionTypeText(
    firstQuestion?.questionType,
    firstQuestion?.questionTypeText
  )}`
}

const toChineseSection = (order) => {
  const list = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return list[order - 1] || `${order}`
}

const syncAnswerState = () => {
  const state = {}
  for (const question of flatQuestionList.value) {
    state[question.paperQuestionId] = parseAnswerValue(question)
  }
  answerState.value = state
  submitContent.value = detail.value?.submitContent || ''
  remainSeconds.value = Number(detail.value?.remainingSeconds ?? 0)
  usedSeconds.value = Number(detail.value?.usedSeconds ?? 0)
  activeQuestionId.value = flatQuestionList.value[0]?.paperQuestionId || null
}

const parseAnswerValue = (question) => {
  if (Number(question.questionType) === 2) {
    return splitMultiAnswer(question.answerContent)
  }
  return question.answerContent || ''
}

const splitMultiAnswer = (value) =>
  String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

const normalizeAnswerValue = (question) => {
  const currentValue = answerState.value[question.paperQuestionId]
  if (Number(question.questionType) === 2) {
    return (Array.isArray(currentValue) ? currentValue : [])
      .map((item) => String(item))
      .join(',')
  }
  return String(currentValue || '').trim()
}

const isChoiceQuestion = (question) =>
  [1, 2, 3].includes(Number(question?.questionType))

const questionOptions = (question) => {
  if (Array.isArray(question?.optionList) && question.optionList.length) {
    return question.optionList
  }
  if (Number(question?.questionType) === 3) {
    return [
      { optionId: 'T', optionContent: '正确' },
      { optionId: 'F', optionContent: '错误' },
    ]
  }
  return []
}

const getSingleChoiceValue = (question) =>
  answerState.value[question.paperQuestionId] || ''

const getMultiChoiceValue = (question) => {
  const value = answerState.value[question.paperQuestionId]
  return Array.isArray(value) ? value.map((item) => String(item)) : []
}

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

const judgeStatusText = (status) => {
  if (Number(status) === 1) return '已自动判分'
  if (Number(status) === 2) return '待人工批改'
  if (Number(status) === 3) return '人工批改完成'
  return '未判题'
}

const questionResultText = (question) => {
  if ([1, 2, 3].includes(Number(question.questionType))) {
    return Number(question.finalScore || 0) >=
      Number(question.questionScore || 0)
      ? '回答正确'
      : '回答错误'
  }
  return Number(question.judgeStatus) === 3 ? '已批改' : '待批改'
}

const questionResultTheme = (question) => {
  if ([1, 2, 3].includes(Number(question.questionType))) {
    return Number(question.finalScore || 0) >=
      Number(question.questionScore || 0)
      ? 'is-correct'
      : 'is-wrong'
  }
  return Number(question.judgeStatus) === 3 ? 'is-reviewed' : 'is-pending'
}

const getUnansweredQuestions = () =>
  flatQuestionList.value.filter((question) => !isQuestionAnswered(question))

const startTimer = () => {
  stopTimer()
  timer = window.setInterval(async () => {
    if (remainSeconds.value > 0 && canEdit.value) {
      remainSeconds.value -= 1
      usedSeconds.value += 1
      if (remainSeconds.value <= 0) {
        await handleSubmit(true)
      }
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

const syncAnsweredCount = () => {
  if (!detail.value) {
    return
  }
  detail.value.answeredCount = flatQuestionList.value.filter((question) =>
    isQuestionAnswered(question)
  ).length
}

const loadDetail = async () => {
  loading.value = true
  try {
    const result = await getCourseExamDetail({ examId: route.params.examId })
    detail.value = result ? normalizeExamDetail(result) : null
    syncAnswerState()
    syncAnsweredCount()
    if (detail.value?.editable) {
      startTimer()
    } else {
      stopTimer()
    }
  } finally {
    loading.value = false
  }
}

const saveAnswer = async (question, answerContent) => {
  if (!detail.value?.submitId || !canEdit.value) {
    return
  }
  savingQuestionId.value = question.paperQuestionId
  try {
    await saveCourseExamAnswer({
      submitId: detail.value.submitId,
      paperQuestionId: question.paperQuestionId,
      questionId: question.questionId,
      answerContent,
    })
    question.answerContent = answerContent
    question.answered = Boolean(String(answerContent || '').trim())
    syncAnsweredCount()
  } finally {
    savingQuestionId.value = null
  }
}

const handleSingleChoiceChange = async (question, optionId) => {
  answerState.value[question.paperQuestionId] = String(optionId)
  activeQuestionId.value = question.paperQuestionId
  await saveAnswer(question, String(optionId))
}

const handleSingleChoiceGroupChange = async (question, value) => {
  await handleSingleChoiceChange(question, String(value))
}

const handleMultipleChoiceGroupChange = async (question, value) => {
  const next = Array.isArray(value) ? value.map((item) => String(item)) : []
  answerState.value[question.paperQuestionId] = next
  activeQuestionId.value = question.paperQuestionId
  await saveAnswer(question, next.join(','))
}

const handleTextBlur = async (question) => {
  activeQuestionId.value = question.paperQuestionId
  await saveAnswer(question, normalizeAnswerValue(question))
}

const handleStart = async () => {
  starting.value = true
  try {
    await startCourseExam({ examId: route.params.examId })
    await loadDetail()
  } finally {
    starting.value = false
  }
}

const handleSaveDraft = async () => {
  if (!detail.value?.submitId || !canEdit.value) {
    return
  }
  draftSaving.value = true
  try {
    await saveCourseExamDraft({
      submitId: detail.value.submitId,
      submitContent: submitContent.value,
      usedSeconds: usedSeconds.value,
    })
    Message.success('草稿已保存')
    await loadDetail()
  } finally {
    draftSaving.value = false
  }
}

const handleSubmit = async (forced = false) => {
  if (!detail.value?.submitId || !canEdit.value) {
    return
  }
  const unansweredQuestions = getUnansweredQuestions()
  if (!forced && unansweredQuestions.length) {
    const unansweredText = unansweredQuestions
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
  try {
    await submitCourseExam({
      submitId: detail.value.submitId,
      submitContent: submitContent.value,
      usedSeconds: usedSeconds.value,
    })
    Message.success(forced ? '考试时间结束，系统已自动交卷' : '考试已提交')
    await loadDetail()
  } finally {
    submitting.value = false
  }
}

const scrollToQuestion = (paperQuestionId) => {
  activeQuestionId.value = paperQuestionId
  const target = document.getElementById(`question-${paperQuestionId}`)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

onMounted(loadDetail)
onBeforeUnmount(stopTimer)
</script>

<style>
.course-exam {
  display: flex;
  flex-direction: column;
  gap: 18px;
  color: #23314d;
}

.course-exam__state {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #edf1f8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  color: #6c7d96;
}

.course-exam__state.is-empty {
  flex-direction: column;
  gap: 8px;
}

.course-exam__breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #8a97ab;
  font-size: 13px;
}

.course-exam__breadcrumb button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #2d79ff;
  cursor: pointer;
  font-size: 13px;
}

.course-exam__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
  align-items: start;
}

.course-exam__main,
.course-exam__aside {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.course-exam__aside {
  position: sticky;
  top: 20px;
  align-self: start;
}

.card {
  border: 1px solid #edf1f8;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18px 34px rgba(42, 77, 138, 0.06);
}

.course-exam__summary {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 22px;
  padding: 20px 24px;
  align-items: center;
}

.course-exam__summary-mark {
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: linear-gradient(180deg, #eff5ff 0%, #e6efff 100%);
}

.course-exam__summary-mark span {
  position: relative;
  width: 42px;
  height: 54px;
  border-radius: 6px;
  background: linear-gradient(180deg, #4d95ff 0%, #2f71f1 100%);
  box-shadow: 0 10px 20px rgba(48, 111, 241, 0.24);
}

.course-exam__summary-mark span::before,
.course-exam__summary-mark span::after {
  content: '';
  position: absolute;
  left: 9px;
  right: 9px;
  height: 4px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.95);
}

.course-exam__summary-mark span::before {
  top: 13px;
}

.course-exam__summary-mark span::after {
  top: 23px;
  box-shadow: 0 10px 0 rgba(255, 255, 255, 0.95);
}

.course-exam__summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.course-exam__summary-subtitle {
  margin: 0 0 6px;
  color: #7f8ea4;
  font-size: 13px;
}

.course-exam__summary-head h1 {
  margin: 0;
  color: #1e2a44;
  font-size: 20px;
  font-weight: 700;
}

.course-exam__summary-start-btn {
  min-width: 108px;
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

.course-exam__summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 28px;
  margin-top: 16px;
  color: #586983;
  font-size: 14px;
}

.course-exam__status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  height: 32px;
  padding: 0 14px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
}

.course-exam__status.is-green,
.course-exam__info-status.is-green {
  color: #13a05f;
  background: #eafaf1;
}

.course-exam__status.is-blue,
.course-exam__info-status.is-blue {
  color: #2d79ff;
  background: #edf4ff;
}

.course-exam__status.is-orange,
.course-exam__info-status.is-orange {
  color: #ff8b28;
  background: #fff3e5;
}

.course-exam__status.is-gray,
.course-exam__info-status.is-gray {
  color: #7d8ea7;
  background: #f4f6fb;
}

.course-exam__paper-head {
  padding: 34px 32px 18px;
  text-align: center;
  border-bottom: 1px solid #eef2f8;
}

.course-exam__paper-head h2 {
  margin: 0;
  color: #1f2d49;
  font-size: 26px;
  font-weight: 700;
}

.course-exam__paper-meta {
  display: inline-flex;
  align-items: center;
  gap: 28px;
  margin-top: 18px;
  color: #6f8098;
  font-size: 14px;
}

.course-exam__paper-head p {
  margin: 14px 0 0;
  color: #7787a0;
  font-size: 14px;
}

.course-exam__paper-body {
  padding: 28px 32px 38px;
}

.course-exam__section-block + .course-exam__section-block {
  margin-top: 38px;
}

.course-exam__section-title {
  margin-bottom: 24px;
  color: #23314d;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.course-exam__section-title span {
  color: #6f8098;
  font-size: 14px;
  font-weight: 500;
}

.course-exam__question-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.course-exam__question {
  padding-bottom: 8px;
  border-bottom: 0;
}

.course-exam__question:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.course-exam__question-head {
  margin-bottom: 14px;
}

.course-exam__question-title {
  display: block;
}

.course-exam__question-title h3 {
  margin: 0;
  color: #1f2d49;
  font-size: 16px;
  line-height: 1.8;
  font-weight: 600;
}

.course-exam__question-index {
  margin-right: 4px;
  color: #1f2d49;
  font-weight: 700;
}

.course-exam__question-content {
  color: #1f2d49;
}

.course-exam__question-score-inline {
  margin-left: 8px;
  color: #7082a1;
  font-size: 14px;
  font-weight: 500;
}

.course-exam__option-list {
  display: block;
}

.course-exam__option-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 36px;
}

.course-exam__option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: #4d607d;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.course-exam__option:hover {
  background: transparent;
}

.course-exam__option.is-selected {
  color: #2d79ff;
}

.course-exam__option.is-disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.course-exam__option .el-checkbox {
  margin-right: 0;
  height: auto;
}

.course-exam__option .el-radio {
  margin-right: 0;
  height: auto;
}

.course-exam__option .el-checkbox__label {
  display: none;
}

.course-exam__option .el-radio__label {
  display: none;
}

.course-exam__option-text {
  display: inline-flex;
  align-items: flex-start;
  gap: 4px;
  flex: 1;
  line-height: 1.8;
}

.course-exam__option-key {
  min-width: 18px;
  color: currentColor;
}

.course-exam__textarea-wrap textarea,
.course-exam__submit-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e4ebf7;
  border-radius: 6px;
  background: #fbfdff;
  color: #253451;
  resize: vertical;
  outline: none;
}

.course-exam__textarea-wrap textarea {
  min-height: 160px;
  padding: 16px;
  background: #fff;
}

.course-exam__question-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #7f8ea4;
  font-size: 13px;
}

.course-exam__question-result {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7faff;
}

.course-exam__question-result-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.course-exam__question-result-label {
  color: #7f8ea4;
  font-size: 13px;
}

.course-exam__question-result-score {
  color: #24304a;
  font-size: 13px;
}

.course-exam__question-result-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.course-exam__question-result-tag.is-correct {
  background: #edf8ee;
  color: #33a25b;
}

.course-exam__question-result-tag.is-wrong {
  background: #fff2f0;
  color: #d9574a;
}

.course-exam__question-result-tag.is-reviewed {
  background: #eef4ff;
  color: #2d79ff;
}

.course-exam__question-result-tag.is-pending {
  background: #fff5e8;
  color: #d78727;
}

.course-exam__side-card {
  padding: 20px 22px;
}

.course-exam__side-card h3 {
  margin: 0 0 18px;
  color: #1f2d49;
  font-size: 18px;
}

.course-exam__info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.course-exam__info-item,
.course-exam__info-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667790;
  font-size: 14px;
}

.course-exam__info-item strong,
.course-exam__info-progress strong {
  color: #2a3854;
  font-weight: 600;
}

.course-exam__info-status {
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

.course-exam__info-time {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-exam__info-time-label {
  color: #667790;
  font-size: 14px;
}

.course-exam__info-time-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.course-exam__info-time-group em {
  color: #ff5b52;
  font-style: normal;
  font-size: 20px;
}

.course-exam__info-time-cell {
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

.course-exam__info-time-cell.is-used {
  color: #1f2d49;
}

.course-exam__info-progress {
  gap: 10px;
}

.course-exam__progress {
  flex: 1;
  min-width: 120px;
  height: 6px;
  border-radius: 999px;
  background: #edf2f8;
  overflow: hidden;
}

.course-exam__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2f7aff 0%, #71a5ff 100%);
}

.course-exam__info-comment {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-exam__info-comment-label {
  color: #667790;
  font-size: 14px;
}

.course-exam__info-comment p {
  margin: 0;
  color: #24304a;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.course-exam__sheet {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.course-exam__sheet-item {
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dbe4f4;
  border-radius: 6px;
  background: #fff;
  color: #5f7391;
  cursor: pointer;
  transition: all 0.2s ease;
}

.course-exam__sheet-item.is-active {
  border-color: #2d79ff;
  color: #2d79ff;
  background: #eef5ff;
}

.course-exam__sheet-item.is-answered {
  border-color: #2f7aff;
  background: linear-gradient(135deg, #2f7aff 0%, #2468f3 100%);
  color: #fff;
}

.course-exam__submit-textarea {
  min-height: 110px;
  padding: 14px 16px;
}

.course-exam__action-btn,
.course-exam__draft-btn {
  width: 100%;
  height: 40px;
  margin-top: 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
}

.course-exam__action-btn.is-primary {
  border: 0;
  background: linear-gradient(90deg, #2f7aff 0%, #2468f3 100%);
  color: #fff;
  box-shadow: 0 12px 22px rgba(47, 106, 243, 0.2);
}

.course-exam__draft-btn {
  border: 1px solid #dce5f3;
  background: #fff;
  color: #2d79ff;
}

.course-exam__action-btn:disabled,
.course-exam__draft-btn:disabled,
.course-exam__summary-start-btn:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

@media (max-width: 1200px) {
  .course-exam__layout {
    grid-template-columns: 1fr;
  }

  .course-exam__aside {
    position: static;
  }
}

@media (max-width: 900px) {
  .course-exam__summary {
    grid-template-columns: 1fr;
  }

  .course-exam__summary-head,
  .course-exam__info-progress,
  .course-exam__info-time {
    flex-direction: column;
    align-items: flex-start;
  }

  .course-exam__option-group {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .course-exam__paper-head,
  .course-exam__paper-body,
  .course-exam__side-card {
    padding: 18px 16px;
  }

  .course-exam__summary-meta {
    gap: 10px 16px;
    font-size: 13px;
  }

  .course-exam__sheet {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .course-exam__paper-meta {
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
