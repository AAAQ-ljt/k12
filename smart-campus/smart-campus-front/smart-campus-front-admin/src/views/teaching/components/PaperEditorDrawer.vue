<template>
  <BaseDrawer v-model:show="visible" title="编排试卷" width="90%" :padding="10" @close="handleClose">
    <div class="paper-editor-page">
      <section class="editor-hero">
        <div class="editor-hero__left">
          <h1>{{ paperDetail.paperName || '试卷编排' }}</h1>
          <p>{{ paperDetail.description || '通过分组、选题、拖拽排序来排版整份试卷。' }}</p>
        </div>
        <div class="editor-hero__right">
          <div class="metric-card">
            <strong>{{ totalQuestionCount }}</strong>
            <span>题目数量</span>
          </div>
          <div class="metric-card">
            <strong>{{ totalScore.toFixed(2) }}</strong>
            <span>总分</span>
          </div>
          <div class="metric-card">
            <strong>{{ paperDetail.paperTypeText || '-' }}</strong>
            <span>{{ paperDetail.paperTypeText || '-' }}</span>
          </div>
          <el-button type="primary" :loading="saving" @click="handleSaveStructure">
            保存编排
          </el-button>
        </div>
      </section>

      <div class="editor-layout" v-loading="loading">
        <section class="editor-canvas">
          <div class="editor-toolbar">
            <div class="editor-toolbar__actions">
              <el-button type="primary" plain @click="handleAddSection">
                新增分组
              </el-button>
              <span class="editor-toolbar__hint">
                支持拖拽分组和题目顺序，题目可在不同分组之间移动。
              </span>
            </div>
          </div>

          <div v-if="!sectionList.length" class="empty-layout">
            <div class="empty-layout__title">当前还没有分组</div>
            <div class="empty-layout__desc">
              先新增分组，再从题库中挑选题目进行组卷排版。
            </div>
            <el-button type="primary" @click="handleAddSection">新增第一个分组</el-button>
          </div>

          <div v-else class="section-list">
            <article v-for="(section, sectionIndex) in sectionList" :key="section.localKey" class="section-card"
              :class="getSectionDragClass(sectionIndex)"
              :draggable="sectionDragReadyIndex === sectionIndex" @dragstart="handleSectionDragStart(sectionIndex)"
              @dragover.prevent="handleSectionDragOver($event, sectionIndex)"
              @drop.prevent="handleSectionDrop(sectionIndex)"
              @dragend="resetDragState">
              <header class="section-card__header">
                <div class="section-card__drag" draggable="true" @mousedown.stop="armSectionDrag(sectionIndex)"
                  @mouseup.stop="clearSectionDragReady">
                  <i class="iconfont icon-drag" />
                </div>
                <div class="section-card__title">
                  <el-input v-model="section.sectionName" maxlength="64" placeholder="请输入分组名称" />
                  <span>{{ section.questionList.length }} 道题 / {{ getSectionScore(section).toFixed(2) }} 分</span>
                </div>
                <div class="section-card__actions">
                  <el-button link type="primary" @click="openQuestionSelector(sectionIndex)">
                    添加题目
                  </el-button>
                  <el-button link type="danger" @click="removeSection(sectionIndex)">
                    删除分组
                  </el-button>
                </div>
              </header>

              <div class="question-dropzone" :class="getQuestionContainerClass(sectionIndex)"
                @dragover.prevent="handleQuestionContainerDragOver(sectionIndex)"
                @drop.prevent="handleQuestionContainerDrop(sectionIndex)">
                <div v-for="(question, questionIndex) in section.questionList" :key="question.localKey"
                  class="question-card" :class="getQuestionDragClass(sectionIndex, questionIndex)"
                  :draggable="isQuestionDragReady(sectionIndex, questionIndex)"
                  @dragstart="handleQuestionDragStart(sectionIndex, questionIndex)"
                  @dragover.prevent="handleQuestionDragOver($event, sectionIndex, questionIndex)"
                  @drop.prevent="handleQuestionDrop(sectionIndex, questionIndex)" @dragend="resetDragState">
                  <div class="question-card__bar">
                    <div class="question-card__drag" draggable="true"
                      @mousedown.stop="armQuestionDrag(sectionIndex, questionIndex)"
                      @mouseup.stop="clearQuestionDragReady">
                      <i class="iconfont icon-drag" />
                    </div>
                    <div class="question-card__meta">
                      <strong>{{ question.questionTypeText || '-' }}</strong>
                      <span>{{ question.difficultyLevelText || '-' }}</span>
                    </div>
                    <div class="question-card__score">
                      <span>分值</span>
                      <el-input-number v-model="question.questionScore" :min="0" :step="1" :precision="2" />
                    </div>
                    <el-button link type="danger" @click="removeQuestion(sectionIndex, questionIndex)">
                      删除
                    </el-button>
                  </div>

                  <div class="question-card__title">
                    {{ questionIndex + 1 }}. {{ question.questionTitle || '未命名题目' }}
                  </div>

                  <div v-if="question.optionList?.length" class="question-card__options">
                    <div v-for="option in question.optionList" :key="option.optionId || option.optionKey"
                      class="question-card__option">
                      {{ option.optionKey }}. {{ option.optionContent }}
                    </div>
                  </div>

                  <div class="question-card__answer">
                    <strong>答案：</strong>{{
                      formatQuestionAnswerText(
                        question.questionType,
                        question.correctAnswerText
                      ) || '-'
                    }}
                  </div>
                </div>

                <div v-if="!section.questionList.length" class="empty-section">
                  <span>当前分组暂无题目</span>
                  <el-button type="primary" plain @click="openQuestionSelector(sectionIndex)">
                    从题库选题
                  </el-button>
                </div>
              </div>
            </article>
          </div>
        </section>

        <aside class="paper-preview">
          <div class="paper-sheet">
            <div class="paper-sheet__title">{{ paperDetail.paperName || '试卷名称' }}</div>
            <div class="paper-sheet__meta">
              <span>总分：{{ totalScore.toFixed(2) }} 分</span>
              <span>
                类型：{{ paperDetail.paperTypeText || '-' }}
              </span>
            </div>
            <div v-if="paperDetail.description" class="paper-sheet__desc">
              {{ paperDetail.description }}
            </div>

            <div v-if="!sectionList.length" class="paper-sheet__empty">
              编排区暂无内容，保存后这里会呈现接近纸质试卷的排版效果。
            </div>

            <section v-for="(section, sectionIndex) in sectionList" :key="`${section.localKey}-preview`"
              class="preview-section">
              <h3>
                {{ toChineseOrder(sectionIndex + 1) }}、{{ section.sectionName || '未命名分组' }}
                <small>（共 {{ section.questionList.length }} 题，{{ getSectionScore(section).toFixed(2) }} 分）</small>
              </h3>

              <article v-for="(question, questionIndex) in section.questionList" :key="`${question.localKey}-preview`"
                class="preview-question">
                <div class="preview-question__title">
                  {{ questionIndex + 1 }}. {{ question.questionTitle || '-' }}
                  <span>（{{ question.questionScore ?? 0 }} 分）</span>
                </div>
                <div v-if="question.optionList?.length" class="preview-question__options">
                  <div v-for="option in question.optionList" :key="option.optionId || option.optionKey"
                    class="preview-question__option">
                    {{ option.optionKey }}. {{ option.optionContent }}
                  </div>
                </div>
                <div class="preview-question__answer">
                  参考答案：{{
                    formatQuestionAnswerText(
                      question.questionType,
                      question.correctAnswerText
                    ) || '-'
                  }}
                </div>
              </article>
            </section>
          </div>
        </aside>
      </div>

      <QuestionSelectorDialog v-model:show="selectorVisible" @confirm="handleSelectQuestions" />
    </div>
  </BaseDrawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseDrawer from '@/components/BaseDrawer.vue'
import { getPaperDetail, savePaperStructure } from '@/api/paper'
import { formatQuestionAnswerText } from '@/api/question'
import QuestionSelectorDialog from '@/views/teaching/components/QuestionSelectorDialog.vue'
import '@/assets/icon/iconfont.css'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  paperId: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:show', 'saved', 'close'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const loading = ref(false)
const saving = ref(false)
const selectorVisible = ref(false)
const activeSectionIndex = ref(-1)
const paperDetail = ref({
  paperId: '',
  paperName: '',
  paperType: 1,
  paperTypeText: '',
  description: '',
  totalScore: 0,
  sectionList: [],
})
const sectionList = ref([])
const sectionDragReadyIndex = ref(-1)
const questionDragReadyKey = ref('')
const dragState = ref({
  type: '',
  fromSectionIndex: -1,
  fromQuestionIndex: -1,
  overSectionIndex: -1,
  overQuestionIndex: -1,
  overPosition: 'before',
})

const totalScore = computed(() =>
  sectionList.value.reduce(
    (total, section) => total + getSectionScore(section),
    0
  )
)
const totalQuestionCount = computed(() =>
  sectionList.value.reduce(
    (count, section) => count + section.questionList.length,
    0
  )
)

const createSection = (section = {}, index = 0) => ({
  id: section.id,
  localKey:
    section.localKey || `section-${Date.now()}-${index}-${Math.random()}`,
  sectionName: section.sectionName || `第 ${index + 1} 部分`,
  sortOrder: Number(section.sortOrder ?? index + 1),
  questionList: Array.isArray(section.questionList)
    ? section.questionList.map((question, questionIndex) => ({
        ...question,
        localKey:
          question.localKey ||
          `question-${
            question.questionId || question.id
          }-${questionIndex}-${Math.random()}`,
        questionScore: Number(question.questionScore ?? 0),
        sortOrder: Number(question.sortOrder ?? questionIndex + 1),
      }))
    : [],
})

function syncSortOrder() {
  sectionList.value = sectionList.value.map((section, sectionIndex) => ({
    ...section,
    sortOrder: sectionIndex + 1,
    questionList: section.questionList.map((question, questionIndex) => ({
      ...question,
      sortOrder: questionIndex + 1,
    })),
  }))
}

function resetState() {
  loading.value = false
  saving.value = false
  selectorVisible.value = false
  activeSectionIndex.value = -1
  paperDetail.value = {
    paperId: '',
    paperName: '',
    paperType: 1,
    paperTypeText: '',
    description: '',
    totalScore: 0,
    sectionList: [],
  }
  sectionList.value = []
  clearSectionDragReady()
  clearQuestionDragReady()
  resetDragState()
}

function buildQuestionDragKey(sectionIndex, questionIndex) {
  return `${sectionIndex}-${questionIndex}`
}

function armSectionDrag(sectionIndex) {
  sectionDragReadyIndex.value = sectionIndex
}

function clearSectionDragReady() {
  sectionDragReadyIndex.value = -1
}

function armQuestionDrag(sectionIndex, questionIndex) {
  questionDragReadyKey.value = buildQuestionDragKey(sectionIndex, questionIndex)
}

function clearQuestionDragReady() {
  questionDragReadyKey.value = ''
}

function isQuestionDragReady(sectionIndex, questionIndex) {
  return (
    questionDragReadyKey.value ===
    buildQuestionDragKey(sectionIndex, questionIndex)
  )
}

function getSectionDragClass(sectionIndex) {
  if (dragState.value.type !== 'section') {
    return {}
  }
  return {
    'is-dragging': dragState.value.fromSectionIndex === sectionIndex,
    'is-drop-before':
      dragState.value.overSectionIndex === sectionIndex &&
      dragState.value.overPosition === 'before',
    'is-drop-after':
      dragState.value.overSectionIndex === sectionIndex &&
      dragState.value.overPosition === 'after',
  }
}

function getQuestionDragClass(sectionIndex, questionIndex) {
  if (
    dragState.value.type !== 'question' ||
    dragState.value.fromSectionIndex === -1
  ) {
    return {}
  }
  return {
    'is-dragging':
      dragState.value.fromSectionIndex === sectionIndex &&
      dragState.value.fromQuestionIndex === questionIndex,
    'is-drop-before':
      dragState.value.overSectionIndex === sectionIndex &&
      dragState.value.overQuestionIndex === questionIndex &&
      dragState.value.overPosition === 'before',
    'is-drop-after':
      dragState.value.overSectionIndex === sectionIndex &&
      dragState.value.overQuestionIndex === questionIndex &&
      dragState.value.overPosition === 'after',
  }
}

function getQuestionContainerClass(sectionIndex) {
  if (dragState.value.type !== 'question') {
    return {}
  }
  return {
    'is-over-empty':
      dragState.value.overSectionIndex === sectionIndex &&
      dragState.value.overQuestionIndex === -1,
  }
}

function resolveDropPosition(event) {
  const rect = event.currentTarget?.getBoundingClientRect?.()
  if (!rect) {
    return 'after'
  }
  const offsetY = event.clientY - rect.top
  return offsetY <= rect.height / 2 ? 'before' : 'after'
}

function getSectionScore(section) {
  return section.questionList.reduce(
    (total, question) => total + Number(question.questionScore ?? 0),
    0
  )
}

function toChineseOrder(value) {
  const list = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  if (value <= 10) {
    return list[value - 1] || String(value)
  }
  return String(value)
}

async function loadPaperData() {
  const paperId = String(props.paperId || '').trim()
  if (!paperId) {
    return
  }
  loading.value = true
  const detail = await getPaperDetail(paperId)
  loading.value = false
  if (!detail) {
    visible.value = false
    return
  }
  paperDetail.value = detail
  sectionList.value = Array.isArray(detail.sectionList)
    ? detail.sectionList.map((section, index) => createSection(section, index))
    : []
  syncSortOrder()
}

function handleAddSection() {
  sectionList.value = [
    ...sectionList.value,
    createSection({}, sectionList.value.length),
  ]
  syncSortOrder()
}

async function removeSection(sectionIndex) {
  const section = sectionList.value[sectionIndex]
  if (!section) {
    return
  }
  try {
    await ElMessageBox.confirm(
      section.questionList.length
        ? `确定删除分组“${section.sectionName}”及其下全部题目吗？`
        : `确定删除分组“${section.sectionName}”吗？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  sectionList.value.splice(sectionIndex, 1)
  sectionList.value = [...sectionList.value]
  syncSortOrder()
}

function removeQuestion(sectionIndex, questionIndex) {
  sectionList.value[sectionIndex]?.questionList.splice(questionIndex, 1)
  sectionList.value = [...sectionList.value]
  syncSortOrder()
}

function openQuestionSelector(sectionIndex) {
  activeSectionIndex.value = sectionIndex
  selectorVisible.value = true
}

function handleSelectQuestions(rows) {
  const section = sectionList.value[activeSectionIndex.value]
  if (!section) {
    return
  }
  const existingQuestionIdSet = new Set(
    sectionList.value.flatMap((item) =>
      item.questionList.map((question) => Number(question.questionId))
    )
  )
  let skippedCount = 0
  const nextQuestions = [...section.questionList]
  rows.forEach((row) => {
    const questionId = Number(row.questionId)
    if (existingQuestionIdSet.has(questionId)) {
      skippedCount++
      return
    }
    existingQuestionIdSet.add(questionId)
    nextQuestions.push({
      ...row,
      localKey: `question-${questionId}-${Date.now()}-${Math.random()}`,
      questionScore: 5,
      sortOrder: nextQuestions.length + 1,
    })
  })
  section.questionList = nextQuestions
  sectionList.value = [...sectionList.value]
  syncSortOrder()
  if (skippedCount) {
    ElMessage.warning(`已跳过 ${skippedCount} 道重复题目`)
  }
}

function handleSectionDragStart(sectionIndex) {
  if (sectionDragReadyIndex.value !== sectionIndex) {
    return
  }
  dragState.value = {
    type: 'section',
    fromSectionIndex: sectionIndex,
    fromQuestionIndex: -1,
    overSectionIndex: sectionIndex,
    overQuestionIndex: -1,
    overPosition: 'before',
  }
}

function handleSectionDragOver(event, sectionIndex) {
  if (dragState.value.type !== 'section') {
    return
  }
  dragState.value.overSectionIndex = sectionIndex
  dragState.value.overPosition = resolveDropPosition(event)
}

function handleSectionDrop(sectionIndex) {
  if (dragState.value.type !== 'section') {
    return
  }
  const fromIndex = dragState.value.fromSectionIndex
  if (fromIndex < 0 || fromIndex === sectionIndex) {
    resetDragState()
    return
  }
  const nextList = [...sectionList.value]
  const [movedSection] = nextList.splice(fromIndex, 1)
  let insertIndex =
    dragState.value.overPosition === 'after' ? sectionIndex + 1 : sectionIndex
  if (fromIndex < insertIndex) {
    insertIndex -= 1
  }
  nextList.splice(insertIndex, 0, movedSection)
  sectionList.value = nextList
  syncSortOrder()
  resetDragState()
}

function handleQuestionDragStart(sectionIndex, questionIndex) {
  if (!isQuestionDragReady(sectionIndex, questionIndex)) {
    return
  }
  dragState.value = {
    type: 'question',
    fromSectionIndex: sectionIndex,
    fromQuestionIndex: questionIndex,
    overSectionIndex: sectionIndex,
    overQuestionIndex: questionIndex,
    overPosition: 'before',
  }
}

function handleQuestionDragOver(event, sectionIndex, questionIndex) {
  if (dragState.value.type !== 'question') {
    return
  }
  dragState.value.overSectionIndex = sectionIndex
  dragState.value.overQuestionIndex = questionIndex
  dragState.value.overPosition = resolveDropPosition(event)
}

function handleQuestionDrop(sectionIndex, questionIndex) {
  moveDraggedQuestion(sectionIndex, questionIndex)
}

function handleQuestionContainerDragOver(sectionIndex) {
  if (dragState.value.type !== 'question') {
    return
  }
  dragState.value.overSectionIndex = sectionIndex
  dragState.value.overQuestionIndex = -1
  dragState.value.overPosition = 'after'
}

function handleQuestionContainerDrop(sectionIndex) {
  moveDraggedQuestion(sectionIndex, -1)
}

function moveDraggedQuestion(targetSectionIndex, targetQuestionIndex) {
  if (dragState.value.type !== 'question') {
    return
  }
  const { fromSectionIndex, fromQuestionIndex } = dragState.value
  if (fromSectionIndex < 0 || fromQuestionIndex < 0) {
    resetDragState()
    return
  }
  const nextSections = sectionList.value.map((section) => ({
    ...section,
    questionList: [...section.questionList],
  }))
  const sourceQuestions = nextSections[fromSectionIndex].questionList
  const [movedQuestion] = sourceQuestions.splice(fromQuestionIndex, 1)
  const targetQuestions = nextSections[targetSectionIndex].questionList
  if (targetQuestionIndex < 0 || targetQuestionIndex > targetQuestions.length) {
    targetQuestions.push(movedQuestion)
  } else {
    let insertIndex =
      dragState.value.overPosition === 'after'
        ? targetQuestionIndex + 1
        : targetQuestionIndex
    if (
      fromSectionIndex === targetSectionIndex &&
      fromQuestionIndex < insertIndex
    ) {
      insertIndex -= 1
    }
    targetQuestions.splice(insertIndex, 0, movedQuestion)
  }
  sectionList.value = nextSections
  syncSortOrder()
  resetDragState()
}

function resetDragState() {
  clearSectionDragReady()
  clearQuestionDragReady()
  dragState.value = {
    type: '',
    fromSectionIndex: -1,
    fromQuestionIndex: -1,
    overSectionIndex: -1,
    overQuestionIndex: -1,
    overPosition: 'before',
  }
}

async function handleSaveStructure() {
  if (!paperDetail.value.paperId) {
    return
  }
  if (
    sectionList.value.some(
      (section) => !String(section.sectionName || '').trim()
    )
  ) {
    ElMessage.warning('请先填写完整的分组名称')
    return
  }
  saving.value = true
  const result = await savePaperStructure({
    paperId: paperDetail.value.paperId,
    sectionList: sectionList.value.map((section, sectionIndex) => ({
      id: section.id,
      sectionName: section.sectionName,
      sortOrder: sectionIndex + 1,
      questionList: section.questionList.map((question, questionIndex) => ({
        id: question.id,
        questionId: question.questionId,
        questionScore: Number(question.questionScore ?? 0),
        sortOrder: questionIndex + 1,
      })),
    })),
  })
  saving.value = false
  if (!result) {
    return
  }
  ElMessage.success('试卷编排已保存')
  await loadPaperData()
  emit('saved', paperDetail.value.paperId)
}

function handleClose() {
  emit('close')
}

watch(
  () => props.show,
  async (show) => {
    if (show) {
      await loadPaperData()
      return
    }
    resetState()
  }
)
</script>

<style lang="scss" scoped>
.paper-editor-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.editor-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 10px;
  border: 1px solid #d9e4f5;
  border-radius: 10px;
  background: radial-gradient(
      circle at top left,
      rgba(135, 174, 255, 0.18),
      transparent 28%
    ),
    linear-gradient(135deg, #fafdff 0%, #eef4ff 100%);
}

.editor-hero__left {
  display: flex;
  flex-direction: column;
  gap: 8px;

  h1 {
    margin: 0;
    color: #22314e;
    font-size: 28px;
    line-height: 1.2;
  }

  p {
    margin: 0;
    color: #687a98;
    font-size: 14px;
    line-height: 1.6;
  }
}

.editor-hero__right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.metric-card {
  display: flex;
  min-width: 110px;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid rgba(167, 191, 236, 0.36);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.82);

  strong {
    color: #20304d;
    font-size: 22px;
    line-height: 1;
  }

  span {
    color: #6c7d99;
    font-size: 12px;
  }
}

.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(340px, 0.9fr);
  gap: 18px;
}

.editor-canvas,
.paper-preview {
  min-width: 0;
}

.editor-toolbar,
.section-card,
.paper-sheet {
  border: 1px solid #dce5f3;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(95, 121, 169, 0.08);
}

.editor-toolbar {
  margin-bottom: 16px;
  padding: 16px 18px;
}

.editor-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.editor-toolbar__hint {
  color: #73829f;
  font-size: 13px;
}

.empty-layout {
  display: flex;
  min-height: 300px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border: 1px dashed #bfd0ed;
  border-radius: 12px;
  background: linear-gradient(180deg, #f9fbff 0%, #f2f7ff 100%);
}

.empty-layout__title {
  color: #23324f;
  font-size: 18px;
  font-weight: 700;
}

.empty-layout__desc {
  color: #7888a2;
  font-size: 13px;
}

.section-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  position: relative;
  padding: 18px;
  transition: border-color 0.2s ease, transform 0.2s ease,
    box-shadow 0.2s ease;

  &.is-dragging {
    opacity: 0.42;
    transform: scale(0.985);
  }

  &.is-drop-before,
  &.is-drop-after {
    border-color: #8eb4ff;
    box-shadow: 0 0 0 3px rgba(103, 151, 255, 0.12);
    background: linear-gradient(180deg, #ffffff 0%, #f6f9ff 100%);
  }

  &.is-drop-before::before,
  &.is-drop-after::after {
    content: '';
    position: absolute;
    left: 18px;
    right: 18px;
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, #5f8cff 0%, #7aa6ff 100%);
    box-shadow: 0 0 0 2px rgba(95, 140, 255, 0.1);
  }

  &.is-drop-before::before {
    top: -8px;
  }

  &.is-drop-after::after {
    bottom: -8px;
  }
}

.section-card__header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.section-card__drag,
.question-card__drag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  min-width: 30px;
  color: #91a4c5;
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

.section-card__title {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 14px;

  span {
    color: #6d7f9d;
    font-size: 13px;
    white-space: nowrap;
  }
}

.section-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-dropzone {
  display: flex;
  min-height: 90px;
  flex-direction: column;
  gap: 12px;

  &.is-over-empty {
    padding: 10px;
    border: 1px dashed #9dbfff;
    border-radius: 10px;
    background: #f6f9ff;
    box-shadow: inset 0 0 0 2px rgba(95, 140, 255, 0.08);
  }
}

.question-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4ebf7;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  transition: border-color 0.2s ease, transform 0.2s ease,
    box-shadow 0.2s ease, background 0.2s ease;

  &.is-dragging {
    opacity: 0.38;
    transform: scale(0.985);
    box-shadow: none;
  }

  &.is-drop-before,
  &.is-drop-after {
    border-color: #8eb4ff;
    background: #f3f8ff;
    box-shadow: 0 0 0 3px rgba(103, 151, 255, 0.12);
  }

  &.is-drop-before::before,
  &.is-drop-after::after {
    content: '';
    position: absolute;
    left: 16px;
    right: 16px;
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, #5f8cff 0%, #7aa6ff 100%);
    box-shadow: 0 0 0 2px rgba(95, 140, 255, 0.1);
  }

  &.is-drop-before::before {
    top: -8px;
  }

  &.is-drop-after::after {
    bottom: -8px;
  }
}

.question-card__bar {
  display: flex;
  align-items: center;
  gap: 14px;
}

.question-card__meta {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;

  strong {
    color: #24304a;
    font-size: 14px;
  }

  span {
    color: #789;
    font-size: 12px;
  }
}

.question-card__score {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #5e6f8d;
  font-size: 13px;
}

.question-card__title {
  color: #253451;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.7;
}

.question-card__options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}

.question-card__option {
  color: #4f617f;
  font-size: 13px;
  line-height: 1.6;
}

.question-card__answer {
  color: #697b99;
  font-size: 13px;
  line-height: 1.6;
}

.empty-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border: 1px dashed #cad9f0;
  border-radius: 10px;
  background: #f9fbff;
  color: #7c8dab;
  font-size: 13px;
}

.paper-preview {
  position: sticky;
  top: 0;
  align-self: start;
}

.paper-sheet {
  padding: 34px 34px 40px;
  background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.98),
      rgba(248, 250, 255, 0.98)
    ),
    repeating-linear-gradient(
      180deg,
      transparent 0,
      transparent 31px,
      rgba(212, 223, 244, 0.42) 31px,
      rgba(212, 223, 244, 0.42) 32px
    );
}

.paper-sheet__title {
  margin-bottom: 14px;
  color: #1d2c49;
  font-size: 26px;
  font-weight: 800;
  text-align: center;
  letter-spacing: 0.06em;
}

.paper-sheet__meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 12px;
  color: #5f6f8c;
  font-size: 13px;
}

.paper-sheet__desc {
  margin-bottom: 18px;
  color: #546581;
  font-size: 13px;
  line-height: 1.7;
}

.paper-sheet__empty {
  padding: 24px 0;
  color: #7e8da7;
  font-size: 13px;
  line-height: 1.8;
  text-align: center;
}

.preview-section {
  margin-top: 20px;

  h3 {
    margin: 0 0 12px;
    color: #22324e;
    font-size: 17px;
    line-height: 1.6;
  }

  small {
    color: #6f809b;
    font-size: 12px;
    font-weight: 400;
  }
}

.preview-question {
  margin-bottom: 16px;
  color: #253451;
  font-size: 14px;
  line-height: 1.8;
}

.preview-question__title {
  font-weight: 600;

  span {
    color: #6d7f9d;
    font-weight: 400;
  }
}

.preview-question__options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 18px;
  margin: 8px 0 0 18px;
}

.preview-question__option,
.preview-question__answer {
  color: #4e607e;
  font-size: 13px;
}

@media (max-width: 1360px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }

  .paper-preview {
    position: static;
  }
}

@media (max-width: 960px) {
  .editor-hero {
    flex-direction: column;
  }

  .editor-hero__right {
    flex-wrap: wrap;
  }

  .section-card__header,
  .question-card__bar {
    flex-wrap: wrap;
  }

  .question-card__options,
  .preview-question__options {
    grid-template-columns: 1fr;
  }
}
</style>
