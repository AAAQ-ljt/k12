<template>
  <div class="paper-question-renderer">
    <section
      v-for="(section, sectionIndex) in sections"
      :key="section.sectionId || `${section.sectionName}-${sectionIndex}`"
      class="paper-question-renderer__section-block"
    >
      <div class="paper-question-renderer__section-title">
        {{ resolveSectionTitle(section, sectionIndex) }}
        <span>
          （共{{ section.questionList?.length || 0 }}题，共{{ formatScore(section.totalScore) }}分）
        </span>
      </div>

      <div class="paper-question-renderer__question-list">
        <article
          v-for="(question, index) in section.questionList"
          :id="`question-${question.paperQuestionId}`"
          :key="question.paperQuestionId"
          class="paper-question-renderer__question"
        >
          <div class="paper-question-renderer__question-head">
            <div class="paper-question-renderer__question-title">
              <h3>
                <span class="paper-question-renderer__question-index">
                  {{ questionOrder(section, index) }}.
                </span>
                <span class="paper-question-renderer__question-content">
                  {{ question.questionTitle }}
                </span>
                <span class="paper-question-renderer__question-score-inline">
                  （{{ formatScore(question.questionScore) }}分）
                </span>
              </h3>
            </div>
          </div>

          <div v-if="isChoiceQuestion(question)" class="paper-question-renderer__option-list">
            <el-checkbox-group
              v-if="Number(question.questionType) === 2"
              :model-value="getMultiChoiceValue(question)"
              class="paper-question-renderer__option-group"
              :disabled="!canEdit || savingQuestionId === question.paperQuestionId"
              @change="emit('multi-change', question, $event)"
            >
              <label
                v-for="option in questionOptions(question)"
                :key="option.optionId"
                class="paper-question-renderer__option"
                :class="{
                  'is-selected': isOptionSelected(question, option.optionId),
                  'is-disabled': !canEdit,
                }"
              >
                <el-checkbox :value="String(option.optionId)" />
                <span class="paper-question-renderer__option-text">
                  {{ option.optionContent }}
                </span>
              </label>
            </el-checkbox-group>

            <el-radio-group
              v-else
              :model-value="String(getSingleChoiceValue(question))"
              class="paper-question-renderer__option-group"
              :disabled="!canEdit || savingQuestionId === question.paperQuestionId"
              @change="emit('single-change', question, $event)"
            >
              <label
                v-for="option in questionOptions(question)"
                :key="option.optionId"
                class="paper-question-renderer__option"
                :class="{
                  'is-selected': isOptionSelected(question, option.optionId),
                  'is-disabled': !canEdit,
                }"
              >
                <el-radio :value="String(option.optionId)" />
                <span class="paper-question-renderer__option-text">
                  {{ option.optionContent }}
                </span>
              </label>
            </el-radio-group>
          </div>

          <div v-else class="paper-question-renderer__textarea-wrap">
            <textarea
              :model-value="getTextValue(question)"
              :value="getTextValue(question)"
              :disabled="!canEdit || savingQuestionId === question.paperQuestionId"
              placeholder="请输入答案"
              @input="emit('text-input', question, $event.target.value)"
              @focus="emit('question-focus', question)"
              @blur="emit('text-blur', question)"
            />
          </div>

          <div class="paper-question-renderer__question-foot">
            <span>
              {{ savingQuestionId === question.paperQuestionId ? '正在保存...' : questionAnsweredText(question) }}
            </span>
            <span v-if="submitted">{{ judgeStatusText(question.judgeStatus) }}</span>
          </div>

          <div v-if="submitted" class="paper-question-renderer__question-result">
            <div class="paper-question-renderer__question-result-item">
              <span class="paper-question-renderer__question-result-label">本题得分</span>
              <strong class="paper-question-renderer__question-result-score">
                {{ formatScore(question.finalScore) }} / {{ formatScore(question.questionScore) }}
              </strong>
            </div>
            <div class="paper-question-renderer__question-result-item">
              <span class="paper-question-renderer__question-result-label">判题结果</span>
              <strong
                :class="[
                  'paper-question-renderer__question-result-tag',
                  questionResultTheme(question),
                ]"
              >
                {{ questionResultText(question) }}
              </strong>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  sections: { type: Array, default: () => [] },
  answerState: { type: Object, default: () => ({}) },
  canEdit: Boolean,
  submitted: Boolean,
  savingQuestionId: {
    type: [Number, String, null],
    default: null,
  },
  resolveSectionTitle: {
    type: Function,
    required: true,
  },
})

const emit = defineEmits([
  'single-change',
  'multi-change',
  'text-input',
  'text-blur',
  'question-focus',
])

const flatQuestionList = computed(() => {
  const result = []
  let orderNo = 1
  for (const section of props.sections || []) {
    for (const question of section.questionList || []) {
      result.push({
        paperQuestionId: question.paperQuestionId,
        orderNo,
      })
      orderNo += 1
    }
  }
  return result
})

const formatScore = (value) => Number(value || 0).toFixed(0)

const isChoiceQuestion = (question) => [1, 2, 3].includes(Number(question?.questionType))

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

const getSingleChoiceValue = (question) => props.answerState?.[question.paperQuestionId] || ''

const getMultiChoiceValue = (question) => {
  const value = props.answerState?.[question.paperQuestionId]
  return Array.isArray(value) ? value.map((item) => String(item)) : []
}

const getTextValue = (question) => props.answerState?.[question.paperQuestionId] || ''

const normalizeAnswerValue = (question) => {
  const currentValue = props.answerState?.[question.paperQuestionId]
  if (Number(question.questionType) === 2) {
    return (Array.isArray(currentValue) ? currentValue : [])
      .map((item) => String(item))
      .join(',')
  }
  return String(currentValue || '').trim()
}

const isMultiOptionChecked = (question, optionId) => {
  const value = props.answerState?.[question.paperQuestionId]
  return Array.isArray(value) && value.map(String).includes(String(optionId))
}

const isOptionSelected = (question, optionId) => {
  if (Number(question.questionType) === 2) {
    return isMultiOptionChecked(question, optionId)
  }
  return String(getSingleChoiceValue(question)) === String(optionId)
}

const questionAnsweredText = (question) =>
  normalizeAnswerValue(question) ? '已作答' : '未作答'

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
    return Number(question.finalScore || 0) >= Number(question.questionScore || 0)
      ? '回答正确'
      : '回答错误'
  }
  return Number(question.judgeStatus) === 3 ? '已批改' : '待批改'
}

const questionResultTheme = (question) => {
  if ([1, 2, 3].includes(Number(question.questionType))) {
    return Number(question.finalScore || 0) >= Number(question.questionScore || 0)
      ? 'is-correct'
      : 'is-wrong'
  }
  return Number(question.judgeStatus) === 3 ? 'is-reviewed' : 'is-pending'
}
</script>

<style scoped>
.paper-question-renderer__section-block + .paper-question-renderer__section-block {
  margin-top: 38px;
}

.paper-question-renderer__section-title {
  margin-bottom: 24px;
  color: #23314d;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.paper-question-renderer__section-title span {
  color: #6f8098;
  font-size: 14px;
  font-weight: 500;
}

.paper-question-renderer__question-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.paper-question-renderer__question {
  padding-bottom: 8px;
}

.paper-question-renderer__question-head {
  margin-bottom: 14px;
}

.paper-question-renderer__question-title h3 {
  margin: 0;
  color: #6f8098;
  font-size: 16px;
  line-height: 1.8;
  font-weight: 400;
}

.paper-question-renderer__question-index,
.paper-question-renderer__question-content,
.paper-question-renderer__question-score-inline {
  color: #6f8098;
  font-weight: 400;
}

.paper-question-renderer__question-index {
  margin-right: 4px;
}

.paper-question-renderer__question-score-inline {
  margin-left: 8px;
  font-size: 14px;
}

.paper-question-renderer__option-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 36px;
}

.paper-question-renderer__option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: #4d607d;
  cursor: pointer;
  transition: color 0.2s ease;
}

.paper-question-renderer__option.is-selected {
  color: #2d79ff;
}

.paper-question-renderer__option.is-disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.paper-question-renderer__option :deep(.el-checkbox),
.paper-question-renderer__option :deep(.el-radio) {
  margin-right: 0;
  height: auto;
}

.paper-question-renderer__option :deep(.el-checkbox__label),
.paper-question-renderer__option :deep(.el-radio__label) {
  display: none;
}

.paper-question-renderer__option-text {
  flex: 1;
  line-height: 1.8;
}

.paper-question-renderer__textarea-wrap textarea {
  width: 100%;
  min-height: 160px;
  box-sizing: border-box;
  padding: 16px;
  border: 1px solid #e4ebf7;
  border-radius: 6px;
  background: #fff;
  color: #253451;
  resize: vertical;
  outline: none;
}

.paper-question-renderer__question-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #7f8ea4;
  font-size: 13px;
}

.paper-question-renderer__question-result {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7faff;
}

.paper-question-renderer__question-result-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.paper-question-renderer__question-result-label {
  color: #7f8ea4;
  font-size: 13px;
}

.paper-question-renderer__question-result-score {
  color: #24304a;
  font-size: 13px;
}

.paper-question-renderer__question-result-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.paper-question-renderer__question-result-tag.is-correct {
  background: #edf8ee;
  color: #33a25b;
}

.paper-question-renderer__question-result-tag.is-wrong {
  background: #fff2f0;
  color: #d9574a;
}

.paper-question-renderer__question-result-tag.is-reviewed {
  background: #eef4ff;
  color: #2d79ff;
}

.paper-question-renderer__question-result-tag.is-pending {
  background: #fff5e8;
  color: #d78727;
}

@media (max-width: 900px) {
  .paper-question-renderer__option-group {
    grid-template-columns: 1fr;
  }
}
</style>
