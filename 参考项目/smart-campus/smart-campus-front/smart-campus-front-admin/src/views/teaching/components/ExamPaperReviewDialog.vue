<template>
  <BaseDialog
    v-model:show="dialogVisible"
    :title="dialogTitle"
    width="1100px"
    :buttons="dialogButtons"
    :show-cancel="true"
    @close="handleClose"
  >
    <div v-loading="loading" class="exam-review-dialog">
      <template v-if="detail">
        <section class="exam-review-dialog__summary">
          <div class="summary-card">
            <div class="summary-card__title">
              <strong>{{ detail.lessonName || detail.paperName }}</strong>
              <span :class="['status-tag', getSubmitStatusClass(detail.submitStatus)]">
                {{ detail.submitStatusText || '-' }}
              </span>
              <span :class="['status-tag', getJudgeStatusClass(detail.judgeStatus)]">
                {{ detail.judgeStatusText || '-' }}
              </span>
            </div>
            <div class="summary-card__meta">
              <span>学生：{{ detail.studentName || '-' }} / {{ detail.studentNo || '-' }}</span>
              <span>班级：{{ detail.className || '-' }}</span>
              <span>试卷：{{ detail.paperName || '-' }}</span>
            </div>
            <div class="summary-card__meta">
              <span>题目数：{{ detail.questionCount || 0 }}</span>
              <span>已作答：{{ detail.answeredCount || 0 }}</span>
              <span>总分：{{ formatScore(detail.totalScore) }}</span>
              <span>客观分：{{ formatScore(detail.objectiveScore) }}</span>
              <span>主观分：{{ formatScore(detail.subjectiveScore) }}</span>
              <span>最终得分：{{ formatScore(detail.finalScore) }}</span>
            </div>
            <div class="summary-card__meta">
              <span>已用时：{{ formatDuration(detail.usedSeconds) }}</span>
              <span>提交时间：{{ detail.submitTime || '-' }}</span>
              <span>批改时间：{{ detail.judgeTime || '-' }}</span>
            </div>
            <div v-if="detail.submitContent" class="summary-card__extra">
              <div class="summary-card__label">学生说明</div>
              <div class="summary-card__content">{{ detail.submitContent }}</div>
            </div>
            <div v-if="showTeacherCommentBlock" class="summary-card__extra">
              <div class="summary-card__label">教师评语</div>
              <el-input
                v-if="isJudgeMode && detail.canJudge"
                v-model="teacherComment"
                type="textarea"
                :rows="3"
                placeholder="请输入教师评语"
                maxlength="300"
                show-word-limit
              />
              <div v-else class="summary-card__content">
                {{ detail.teacherComment || '暂无评语' }}
              </div>
            </div>
          </div>
        </section>

        <section
          v-for="section in detail.sectionList"
          :key="section.sectionId || section.sectionName"
          class="question-section"
        >
          <div class="question-section__header">
            <strong>{{ section.sectionName || '题目列表' }}</strong>
            <span>{{ formatScore(section.totalScore) }} 分</span>
          </div>

          <div
            v-for="(question, index) in section.questionList"
            :key="`${section.sectionId}-${question.questionId}-${index}`"
            class="question-card"
          >
            <div class="question-card__header">
              <div class="question-card__title">
                <span>{{ index + 1 }}.</span>
                <span>{{ question.questionTitle || '-' }}</span>
              </div>
              <div class="question-card__score">
                <span>题目分值：{{ formatScore(question.questionScore) }}</span>
                <template v-if="isJudgeMode && detail.canJudge && isSubjectiveQuestion(question)">
                  <span>批改得分：</span>
                  <el-input-number
                    v-model="questionScoreMap[question.questionId]"
                    :min="0"
                    :max="Number(question.questionScore || 0)"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                  />
                </template>
                <template v-else>
                  <span>得分：{{ formatScore(question.finalScore) }}</span>
                </template>
              </div>
            </div>

            <div
              v-if="Array.isArray(question.optionList) && question.optionList.length"
              class="question-card__options"
            >
              <div
                v-for="option in question.optionList"
                :key="option.optionId || option.optionKey"
                :class="[
                  'question-option',
                  {
                    'question-option--selected': isOptionSelected(option, question.answerContent),
                  },
                ]"
              >
                <span class="question-option__key">{{ option.optionKey }}</span>
                <span class="question-option__content">{{ option.optionContent }}</span>
              </div>
            </div>

            <div class="question-card__meta-list">
              <div class="question-card__meta-item">
                <span class="question-card__meta-label">学生答案</span>
                <span class="question-card__meta-value">
                  {{ formatAnswerContent(question) }}
                </span>
              </div>
              <div class="question-card__meta-item">
                <span class="question-card__meta-label">标准答案</span>
                <span class="question-card__meta-value">
                  {{ question.correctAnswerText || '暂无' }}
                </span>
              </div>
              <div v-if="question.answerAnalysis" class="question-card__meta-item">
                <span class="question-card__meta-label">答案解析</span>
                <span class="question-card__meta-value">
                  {{ question.answerAnalysis }}
                </span>
              </div>
            </div>
          </div>
        </section>
      </template>

      <el-empty v-else description="暂无试卷详情" />
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseDialog from '@/components/BaseDialog.vue'
import { getExamSubmitDetail, judgeExamSubmit } from '@/api/exam'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  examId: {
    type: String,
    default: '',
  },
  studentId: {
    type: Number,
    default: undefined,
  },
  mode: {
    type: String,
    default: 'view',
  },
})

const emit = defineEmits(['update:show', 'saved'])

const dialogVisible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const loading = ref(false)
const detail = ref(null)
const teacherComment = ref('')
const questionScoreMap = reactive({})

const isJudgeMode = computed(() => props.mode === 'judge')
const dialogTitle = computed(() => (isJudgeMode.value ? '批改学生试卷' : '查看学生试卷'))
const showTeacherCommentBlock = computed(
  () => isJudgeMode.value || Boolean(detail.value?.teacherComment)
)

const dialogButtons = computed(() => {
  if (!isJudgeMode.value || !detail.value?.canJudge) {
    return []
  }
  return [
    {
      text: '保存批改',
      type: 'primary',
      click: handleSave,
    },
  ]
})

const resetState = () => {
  detail.value = null
  teacherComment.value = ''
  Object.keys(questionScoreMap).forEach((key) => {
    delete questionScoreMap[key]
  })
}

const buildAnswerTokens = (answerContent) => {
  if (!answerContent) {
    return []
  }
  return String(answerContent)
    .replace(/\[|\]|"|'/g, '')
    .split(/[,，]/)
    .map((item) => item.trim().toUpperCase())
    .filter(Boolean)
}

const isOptionSelected = (option, answerContent) => {
  const tokens = buildAnswerTokens(answerContent)
  const optionKey = String(option?.optionKey || '').trim().toUpperCase()
  const optionId = String(option?.optionId || '').trim().toUpperCase()
  return tokens.includes(optionKey) || tokens.includes(optionId)
}

const isSubjectiveQuestion = (question) => Number(question?.questionType) === 4
const formatScore = (value) => Number(value ?? 0).toFixed(2)

const formatDuration = (seconds) => {
  const total = Number(seconds || 0)
  const hour = Math.floor(total / 3600)
  const minute = Math.floor((total % 3600) / 60)
  const second = total % 60
  if (hour > 0) {
    return `${hour}小时${minute}分${second}秒`
  }
  if (minute > 0) {
    return `${minute}分${second}秒`
  }
  return `${second}秒`
}

const formatAnswerContent = (question) => {
  if (!question?.answerContent) {
    return '未作答'
  }
  const tokens = buildAnswerTokens(question.answerContent)
  if (Array.isArray(question.optionList) && question.optionList.length && tokens.length) {
    const optionTextList = question.optionList
      .filter((option) => isOptionSelected(option, question.answerContent))
      .map((option) => `${option.optionKey}. ${option.optionContent}`)
    if (optionTextList.length) {
      return optionTextList.join('；')
    }
  }
  return String(question.answerContent)
}

const getSubmitStatusClass = (status) => {
  if (Number(status) === 3) {
    return 'enabled'
  }
  if (Number(status) === 2) {
    return 'warning'
  }
  if (Number(status) === 1) {
    return 'processing'
  }
  return 'disabled'
}

const getJudgeStatusClass = (status) => {
  if (Number(status) === 3) {
    return 'enabled'
  }
  if (Number(status) === 2) {
    return 'danger'
  }
  if (Number(status) === 1) {
    return 'processing'
  }
  return 'disabled'
}

const loadDetail = async () => {
  if (!props.examId || !props.studentId) {
    resetState()
    return
  }
  loading.value = true
  const result = await getExamSubmitDetail({
    examId: props.examId,
    studentId: props.studentId,
  })
  loading.value = false
  if (!result) {
    resetState()
    return
  }
  detail.value = result
  teacherComment.value = result.teacherComment || ''
  Object.keys(questionScoreMap).forEach((key) => {
    delete questionScoreMap[key]
  })
  ;(result.sectionList || []).forEach((section) => {
    ;(section.questionList || []).forEach((question) => {
      if (isSubjectiveQuestion(question)) {
        questionScoreMap[question.questionId] = Number(question.finalScore ?? 0)
      }
    })
  })
}

const handleSave = async () => {
  if (!detail.value?.submitId) {
    ElMessage.warning('当前试卷尚未提交，无法批改')
    return
  }
  const subjectiveQuestionList = (detail.value.sectionList || []).flatMap((section) =>
    (section.questionList || []).filter((question) => isSubjectiveQuestion(question))
  )
  const result = await judgeExamSubmit({
    submitId: detail.value.submitId,
    teacherComment: teacherComment.value,
    questionScoreList: subjectiveQuestionList.map((question) => ({
      questionId: question.questionId,
      score: Number(questionScoreMap[question.questionId] ?? 0),
    })),
  })
  if (!result) {
    return
  }
  ElMessage.success('批改已保存')
  emit('saved')
  await loadDetail()
}

const handleClose = () => {
  resetState()
}

watch(
  () => props.show,
  async (value) => {
    if (!value) {
      resetState()
      return
    }
    await loadDetail()
  }
)
</script>

<style lang="scss" scoped>
.exam-review-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 180px;
}

.exam-review-dialog__summary {
  display: flex;
}

.summary-card {
  width: 100%;
  padding: 16px 18px;
  border: 1px solid #e8eef7;
  border-radius: 10px;
  background: #f9fbfe;
}

.summary-card__title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  color: #24304a;
}

.summary-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-bottom: 8px;
  color: #5a6b8a;
  font-size: 13px;
}

.summary-card__extra {
  margin-top: 12px;
}

.summary-card__label {
  margin-bottom: 8px;
  color: #24304a;
  font-size: 13px;
  font-weight: 600;
}

.summary-card__content {
  color: #5a6b8a;
  line-height: 1.7;
  white-space: pre-wrap;
}

.question-section {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.question-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #eef3fb;
  background: #f7faff;
  color: #24304a;
}

.question-card {
  padding: 18px;
  border-top: 1px solid #f0f3f8;
}

.question-card:first-of-type {
  border-top: none;
}

.question-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.question-card__title {
  display: flex;
  gap: 8px;
  color: #24304a;
  line-height: 1.7;
}

.question-card__score {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #5a6b8a;
  font-size: 13px;
}

.question-card__options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.question-option {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fbfcff;
  color: #44536f;
}

.question-option--selected {
  border-color: #bfd7ff;
  background: #edf4ff;
}

.question-option__key {
  color: #2457d6;
  font-weight: 600;
}

.question-option__content {
  flex: 1;
  line-height: 1.6;
}

.question-card__meta-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-card__meta-item {
  display: flex;
  gap: 10px;
  color: #4b5d7d;
  line-height: 1.7;
}

.question-card__meta-label {
  width: 72px;
  flex: 0 0 72px;
  color: #7c8ca7;
}

.question-card__meta-value {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
