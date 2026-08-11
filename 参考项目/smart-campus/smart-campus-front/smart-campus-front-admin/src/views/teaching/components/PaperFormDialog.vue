<template>
  <BaseDialog
    v-model:show="visible"
    :title="dialogTitle"
    width="620px"
    :buttons="dialogButtons"
    :show-cancel="!readonly"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="96px"
      class="paper-form"
    >
      <el-form-item label="试卷名称" prop="paperName">
        <el-input
          v-model="form.paperName"
          maxlength="128"
          show-word-limit
          placeholder="请输入试卷名称"
          :disabled="readonly"
        />
      </el-form-item>

      <el-form-item label="试卷类型" prop="paperType">
        <el-select
          v-model="form.paperType"
          placeholder="请选择试卷类型"
          filterable
          :disabled="readonly"
        >
          <el-option
            v-for="item in PAPER_TYPE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="试卷说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请输入试卷说明"
          :disabled="readonly"
        />
      </el-form-item>

      <el-form-item v-if="readonly" label="当前总分">
        <div class="readonly-value">{{ form.totalScore ?? 0 }} 分</div>
      </el-form-item>
    </el-form>
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'
import { normalizePaperDetail, PAPER_TYPE_OPTIONS } from '@/api/paper'

const createDefaultForm = () => ({
  paperId: '',
  paperName: '',
  paperType: 1,
  description: '',
  totalScore: 0,
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
const form = reactive(createDefaultForm())
const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const rules = {
  paperName: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  paperType: [{ required: true, message: '请选择试卷类型', trigger: 'change' }],
}

const dialogTitle = computed(() => {
  if (props.mode === 'view') {
    return '查看试卷'
  }
  return props.mode === 'edit' ? '编辑试卷' : '新增试卷'
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

function resetFormState(detail = {}) {
  const nextState = {
    ...createDefaultForm(),
    ...normalizePaperDetail(detail),
  }
  Object.assign(form, nextState)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', {
    paperId: form.paperId || undefined,
    paperName: form.paperName,
    paperType: form.paperType,
    description: form.description,
  })
}

function handleClose() {
  formRef.value?.clearValidate?.()
}

watch(
  () => props.show,
  (show) => {
    if (!show) {
      return
    }
    resetFormState(props.model)
  }
)
</script>

<style lang="scss" scoped>
.paper-form {
  padding-right: 8px;
}

.readonly-value {
  color: #24304a;
  font-size: 14px;
  font-weight: 600;
}
</style>
