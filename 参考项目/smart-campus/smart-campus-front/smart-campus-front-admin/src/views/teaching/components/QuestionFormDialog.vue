<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="960px" :buttons="dialogButtons"
    :show-cancel="!readonly" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="question-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="题目类型" prop="questionType">
            <el-select v-model="form.questionType" placeholder="请选择题目类型" :disabled="readonly">
              <el-option v-for="item in QUESTION_TYPE_OPTIONS" :key="item.value" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="难度等级" prop="difficultyLevel">
            <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级" :disabled="readonly">
              <el-option v-for="item in DIFFICULTY_LEVEL_OPTIONS" :key="item.value" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="题目标题" prop="questionTitle">
            <el-input v-model="form.questionTitle" :type="isLongQuestionType ? 'textarea' : 'text'"
              :rows="isLongQuestionType ? 4 : undefined" maxlength="255" show-word-limit placeholder="请输入题目标题"
              :disabled="readonly" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="题目配图">
        <div class="question-image-panel">
          <div class="question-image-panel__list">
            <div v-for="(item, index) in imageResourceList" :key="item.resourceId" class="question-image-card">
              <el-button v-if="!readonly" link type="danger" class="question-image-card__remove"
                @click="removeImage(index)">
                <i class="iconfont icon-del" />
              </el-button>
              <div class="question-image-card__preview" role="button" tabindex="0" @click="handlePreviewResource(item)"
                @keydown.enter.prevent="handlePreviewResource(item)">
                <img v-if="item.filePath" :src="buildResourceFileUrl(item.filePath)" :alt="item.resourceName" />
                <div v-else class="question-image-card__fallback">图片</div>
              </div>
              <div class="question-image-card__name" :title="item.resourceName">
                {{ item.resourceName }}
              </div>
            </div>
            <button v-if="!readonly && imageResourceList.length < MAX_IMAGE_COUNT" type="button"
              class="question-image-card question-image-card--adder" @click="selectorVisible = true">
              <i class="iconfont icon-add" />
              <span>选择配图</span>
              <small>最多 {{ MAX_IMAGE_COUNT }} 张</small>
            </button>
          </div>
          <div class="question-image-panel__hint">
            已选择 {{ imageResourceList.length }}/{{ MAX_IMAGE_COUNT }} 张
          </div>
        </div>
      </el-form-item>

      <el-form-item v-if="isChoiceType" label="题目选项" prop="optionList">
        <div class="option-editor">
          <div v-for="(item, index) in form.optionList" :key="`${item.optionId || 'option'}-${index}`"
            class="option-editor__row">
            <div class="option-editor__key">{{ getOptionKey(index) }}</div>
            <el-input v-model="item.optionContent" maxlength="1000" placeholder="请输入选项内容" :disabled="readonly" />
            <el-button v-if="!readonly" link type="danger" :disabled="form.optionList.length <= 2"
              @click="removeOption(index)">
              删除
            </el-button>
          </div>
          <el-button v-if="!readonly" type="primary" plain @click="addOption">
            新增选项
          </el-button>
        </div>
      </el-form-item>

      <el-form-item v-if="isSingleAnswerType" label="标准答案" prop="correctOptionKeyList">
        <el-radio-group v-model="singleCorrectOptionKey" :disabled="readonly">
          <el-radio v-for="(item, index) in currentAnswerOptions" :key="item.optionKey || getOptionKey(index)"
            :label="item.optionKey || getOptionKey(index)">
            {{ item.optionKey || getOptionKey(index) }}. {{ item.optionContent }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-else-if="isMultiChoice" label="标准答案" prop="correctOptionKeyList">
        <el-checkbox-group v-model="form.correctOptionKeyList" :disabled="readonly">
          <el-checkbox v-for="(item, index) in currentAnswerOptions" :key="item.optionKey || getOptionKey(index)"
            :label="item.optionKey || getOptionKey(index)">
            {{ item.optionKey || getOptionKey(index) }}. {{ item.optionContent }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item v-else-if="isJudgeType" label="标准答案" prop="correctAnswerText">
        <el-radio-group v-model="form.correctAnswerText" :disabled="readonly">
          <el-radio label="T">正确</el-radio>
          <el-radio label="F">错误</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-else label="标准答案" prop="correctAnswerText">
        <el-input v-model="form.correctAnswerText" type="textarea" :rows="4" maxlength="2000" show-word-limit
          placeholder="请输入标准答案" :disabled="readonly" />
      </el-form-item>

      <el-form-item label="答案解析">
        <el-input v-model="form.answerAnalysis" type="textarea" :rows="5" maxlength="2000" show-word-limit
          placeholder="请输入答案解析" :disabled="readonly" />
      </el-form-item>
    </el-form>
  </BaseDialog>

  <ResourceSelectorDialog v-model:show="selectorVisible" :resource-type="2" @select="handleSelectImage" />
  <ResourcePreviewDialog v-model:show="previewVisible" :resource="previewResource" />
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseDialog from '@/components/BaseDialog.vue'
import ResourcePreviewDialog from '@/views/resource/components/ResourcePreviewDialog.vue'
import ResourceSelectorDialog from '@/views/resource/components/ResourceSelectorDialog.vue'
import {
  DIFFICULTY_LEVEL_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  normalizeQuestionDetail,
} from '@/api/question'
import { getResourceListByIds } from '@/api/resource'
import { buildResourceFileUrl } from '@/utils/resource'
import '@/assets/icon/iconfont.css'

const QUESTION_TYPE_SINGLE = 1
const QUESTION_TYPE_MULTI = 2
const QUESTION_TYPE_JUDGE = 3
const QUESTION_TYPE_FILL = 4
const MAX_IMAGE_COUNT = 5

const createDefaultForm = () => ({
  questionId: undefined,
  questionType: QUESTION_TYPE_SINGLE,
  questionTitle: '',
  questionImageResourceIdList: [],
  difficultyLevel: 3,
  correctOptionKeyList: [],
  correctAnswerText: '',
  answerAnalysis: '',
  optionList: createDefaultChoiceOptions(),
})

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'create',
  },
  model: {
    type: Object,
    default: () => ({}),
  },
  readonly: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:show', 'submit'])

const formRef = ref(null)
const selectorVisible = ref(false)
const previewVisible = ref(false)
const previewResource = ref(null)
const imageResourceList = ref([])
const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const form = reactive(createDefaultForm())

const rules = {
  questionType: [
    { required: true, message: '请选择题目类型', trigger: 'change' },
  ],
  questionTitle: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
  ],
  difficultyLevel: [
    { required: true, message: '请选择难度等级', trigger: 'change' },
  ],
  optionList: [
    {
      validator: (_, value, callback) => {
        if (!isChoiceType.value) {
          callback()
          return
        }
        if (!Array.isArray(value) || value.length < 2) {
          callback(new Error('选择题至少需要两个选项'))
          return
        }
        if (value.some((item) => !String(item.optionContent || '').trim())) {
          callback(new Error('请填写完整的选项内容'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  correctOptionKeyList: [
    {
      validator: (_, value, callback) => {
        if (!isChoiceAnswerType.value) {
          callback()
          return
        }
        if (!Array.isArray(value) || !value.length) {
          callback(new Error('请选择标准答案'))
          return
        }
        if (
          form.questionType === QUESTION_TYPE_SINGLE ||
          form.questionType === QUESTION_TYPE_JUDGE
        ) {
          if (value.length !== 1) {
            callback(new Error('当前题型只能选择一个标准答案'))
            return
          }
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  correctAnswerText: [
    {
      validator: (_, value, callback) => {
        if (isChoiceAnswerType.value) {
          callback()
          return
        }
        if (isJudgeType.value) {
          if (value !== 'T' && value !== 'F') {
            callback(new Error('判断题标准答案只能选择 T 或 F'))
            return
          }
          callback()
          return
        }
        if (!String(value || '').trim()) {
          callback(new Error('请输入标准答案'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

const dialogTitle = computed(() => {
  if (props.mode === 'view') {
    return '查看题目'
  }
  return props.mode === 'edit' ? '编辑题目' : '新增题目'
})

const dialogButtons = computed(() =>
  props.readonly
    ? []
    : [
        {
          text: '保存',
          type: 'primary',
          click: handleSubmit,
        },
      ]
)

const isChoiceType = computed(
  () =>
    form.questionType === QUESTION_TYPE_SINGLE ||
    form.questionType === QUESTION_TYPE_MULTI
)
const isMultiChoice = computed(() => form.questionType === QUESTION_TYPE_MULTI)
const isJudgeType = computed(() => form.questionType === QUESTION_TYPE_JUDGE)
const isLongQuestionType = computed(
  () => form.questionType === QUESTION_TYPE_FILL
)
const isSingleAnswerType = computed(
  () => form.questionType === QUESTION_TYPE_SINGLE
)
const isChoiceAnswerType = computed(
  () =>
    form.questionType === QUESTION_TYPE_SINGLE ||
    form.questionType === QUESTION_TYPE_MULTI
)
const currentAnswerOptions = computed(() => {
  return form.optionList.map((item, index) => ({
    optionKey: item.optionKey || getOptionKey(index),
    optionContent: item.optionContent,
  }))
})

const singleCorrectOptionKey = computed({
  get: () => form.correctOptionKeyList[0] || '',
  set: (value) => {
    form.correctOptionKeyList = value ? [value] : []
  },
})

function createDefaultChoiceOptions() {
  return Array.from({ length: 4 }, (_, index) => ({
    optionContent: '',
    sortOrder: index + 1,
  }))
}

function getOptionKey(index) {
  return String.fromCharCode(65 + index)
}

function resetFormState(detail = {}) {
  const source = normalizeQuestionDetail(detail)
  const nextState = {
    ...createDefaultForm(),
    ...source,
  }
  nextState.optionList =
    Array.isArray(source.optionList) && source.optionList.length
      ? source.optionList.map((item, index) => ({
          optionId: item.optionId,
          optionKey: item.optionKey || getOptionKey(index),
          optionContent: item.optionContent || '',
          sortOrder: Number(item.sortOrder ?? index + 1),
        }))
      : createDefaultChoiceOptions()
  if (source.questionType === QUESTION_TYPE_JUDGE) {
    nextState.optionList = []
    nextState.correctOptionKeyList = []
    nextState.correctAnswerText = source.correctAnswerText === 'F' ? 'F' : 'T'
  }
  Object.assign(form, nextState)
}

async function loadSelectedImages() {
  if (!form.questionImageResourceIdList.length) {
    imageResourceList.value = []
    return
  }
  const list = await getResourceListByIds(form.questionImageResourceIdList)
  const resourceMap = new Map(
    (Array.isArray(list) ? list : []).map((item) => [
      Number(item.resourceId),
      item,
    ])
  )
  imageResourceList.value = form.questionImageResourceIdList
    .map((item) => resourceMap.get(Number(item)))
    .filter(Boolean)
  syncImageIds()
}

function syncImageIds() {
  form.questionImageResourceIdList = imageResourceList.value.map(
    (item) => item.resourceId
  )
}

function addOption() {
  form.optionList.push({
    optionContent: '',
    sortOrder: form.optionList.length + 1,
  })
}

function removeOption(index) {
  if (form.optionList.length <= 2) {
    return
  }
  const removedKey = getOptionKey(index)
  form.optionList.splice(index, 1)
  form.optionList = form.optionList.map((item, itemIndex) => ({
    ...item,
    sortOrder: itemIndex + 1,
  }))
  form.correctOptionKeyList = form.correctOptionKeyList
    .filter((item) => item !== removedKey)
    .map((item) => {
      const originalIndex = item.charCodeAt(0) - 65
      return originalIndex > index ? getOptionKey(originalIndex - 1) : item
    })
}

function normalizeAnswerStateByType(questionType) {
  if (questionType === QUESTION_TYPE_SINGLE) {
    form.optionList = form.optionList.length
      ? form.optionList
      : createDefaultChoiceOptions()
    form.correctAnswerText = ''
    form.correctOptionKeyList = form.correctOptionKeyList[0]
      ? [form.correctOptionKeyList[0]]
      : []
    return
  }
  if (questionType === QUESTION_TYPE_MULTI) {
    form.optionList = form.optionList.length
      ? form.optionList
      : createDefaultChoiceOptions()
    form.correctAnswerText = ''
    form.correctOptionKeyList = [...new Set(form.correctOptionKeyList)]
    return
  }
  if (questionType === QUESTION_TYPE_JUDGE) {
    form.optionList = []
    form.correctOptionKeyList = []
    form.correctAnswerText = form.correctAnswerText === 'F' ? 'F' : 'T'
    return
  }
  form.optionList = []
  form.correctOptionKeyList = []
}

function handleSelectImage(resource) {
  if (!resource?.resourceId) {
    return
  }
  if (imageResourceList.value.length >= MAX_IMAGE_COUNT) {
    ElMessage.warning(`题目配图最多只能选择 ${MAX_IMAGE_COUNT} 张`)
    selectorVisible.value = false
    return
  }
  if (
    imageResourceList.value.some(
      (item) => Number(item.resourceId) === Number(resource.resourceId)
    )
  ) {
    selectorVisible.value = false
    return
  }
  imageResourceList.value = [...imageResourceList.value, resource]
  syncImageIds()
  selectorVisible.value = false
}

function removeImage(index) {
  imageResourceList.value.splice(index, 1)
  imageResourceList.value = [...imageResourceList.value]
  syncImageIds()
}

function handlePreviewResource(resource) {
  previewResource.value = resource
  previewVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', {
    questionId: form.questionId,
    questionType: form.questionType,
    questionTitle: form.questionTitle,
    questionImageResourceIdList: [...form.questionImageResourceIdList],
    difficultyLevel: form.difficultyLevel,
    correctOptionKeyList: isChoiceAnswerType.value
      ? [...form.correctOptionKeyList]
      : [],
    correctAnswerText: form.correctAnswerText,
    answerAnalysis: form.answerAnalysis,
    optionList: isChoiceType.value
      ? form.optionList.map((item, index) => ({
          optionContent: item.optionContent,
          sortOrder: index + 1,
        }))
      : [],
  })
}

function handleClose() {
  formRef.value?.clearValidate?.()
}

watch(
  () => form.questionType,
  (value) => {
    normalizeAnswerStateByType(Number(value))
  }
)

watch(
  () => props.show,
  async (show) => {
    if (!show) {
      return
    }
    resetFormState(props.model)
    normalizeAnswerStateByType(Number(form.questionType))
    await loadSelectedImages()
  }
)
</script>

<style lang="scss" scoped>
.question-form {
  padding-right: 6px;
}

.question-image-panel {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 10px;
}

.question-image-panel__list {
  display: flex;
  flex-wrap: nowrap;
  gap: 14px;
}

.question-image-panel__hint {
  font-size: 12px;
  line-height: 1.5;
}

.question-image-card {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 148px;
  min-width: 148px;
  max-width: 148px;
  height: 168px;
  flex: 0 0 148px;
  gap: 10px;
  border: 1px solid #e2e9f5;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(30, 49, 86, 0.06);
}

.question-image-card__remove {
  position: absolute;
  top: -8px;
  right: -8px;
  padding: 0;
  line-height: 1;

  .iconfont {
    display: inline-flex;
    width: 18px;
    height: 18px;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    color: #f56c6c;
    font-size: 12px;
  }
}

.question-image-card__preview {
  display: flex;
  width: 100%;
  height: 104px;
  flex: 0 0 104px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 6px 6px 0px 0px;
  background: #f4f8ff;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.question-image-card__fallback {
  color: #7c8fb3;
  font-size: 14px;
  font-weight: 600;
}

.question-image-card__name {
  display: -webkit-box;
  min-height: 40px;
  overflow: hidden;
  color: #24304a;
  font-size: 13px;
  line-height: 1.5;
  padding: 0px 5px 5px 5px;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.question-image-card--adder {
  align-items: center;
  justify-content: center;
  border-style: dashed;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f9ff 100%);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;

  .iconfont {
    color: #4d7fff;
    font-size: 18px;
    line-height: 1;
  }

  span {
    color: #3b69d1;
    font-size: 14px;
    font-weight: 600;
  }

  small {
    color: #8b97ad;
    font-size: 12px;
    line-height: 1.4;
  }

  &:hover {
    border-color: #a9c5ff;
    box-shadow: 0 12px 26px rgba(77, 127, 255, 0.12);
  }
}

.option-editor {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 12px;
}

.option-editor__row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-editor__key {
  display: inline-flex;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #eef4ff;
  color: #4f74c8;
  font-size: 13px;
  font-weight: 700;
}
</style>
