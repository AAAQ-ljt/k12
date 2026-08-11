<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="960px" :buttons="dialogButtons" :show-cancel="!readonly">
    <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="readonly" label-width="92px" class="dialog-form notice-form">
      <el-row :gutter="16">
        <el-col :span="16">
          <el-form-item label="公告标题" prop="noticeTitle">
            <el-input v-model="formData.noticeTitle" placeholder="请输入公告标题" maxlength="100" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="是否置顶" prop="isTop">
            <el-radio-group v-model="formData.isTop">
              <el-radio :value="1">置顶</el-radio>
              <el-radio :value="0">不置顶</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发布范围" prop="targetType">
            <el-select v-model="formData.targetType" placeholder="请选择发布范围" @change="handleTargetTypeChange">
              <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="formData.targetType === 2" :span="12">
          <el-form-item label="指定班级" prop="targetIdList">
            <el-select v-model="formData.targetIdList" placeholder="请选择班级" multiple filterable collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="String(item.value)" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="formData.targetType === 3" :span="12">
          <el-form-item label="指定专业" prop="targetIdList">
            <el-select v-model="formData.targetIdList" placeholder="请选择专业" multiple filterable collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="String(item.value)" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="公告内容" prop="noticeContent">
            <MdPreview v-if="readonly" :model-value="formData.noticeContent || '暂无内容'" />
            <MdEditor v-else v-model="formData.noticeContent" class="notice-form__editor" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import BaseDialog from '@/components/BaseDialog.vue'

const props = defineProps({
  show: Boolean,
  mode: { type: String, default: 'create' },
  model: { type: Object, default: () => ({}) },
  classOptions: { type: Array, default: () => [] },
  majorOptions: { type: Array, default: () => [] },
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()
const targetTypeOptions = [
  { label: '全部学生', value: 1 },
  { label: '指定班级', value: 2 },
  { label: '指定专业', value: 3 },
]

const createDefaultForm = () => ({
  id: undefined,
  noticeId: undefined,
  noticeTitle: '',
  noticeContent: '',
  targetType: 1,
  isTop: 0,
  targetIdList: [],
})
const formData = reactive(createDefaultForm())
const validateTargetIdList = (_rule, value, callback) => {
  if ((formData.targetType === 2 || formData.targetType === 3) && (!Array.isArray(value) || value.length === 0)) {
    callback(new Error(formData.targetType === 2 ? '请选择发布班级' : '请选择发布专业'))
    return
  }
  callback()
}
const formRules = {
  noticeTitle: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeContent: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  targetType: [{ required: true, message: '请选择发布范围', trigger: 'change' }],
  targetIdList: [{ validator: validateTargetIdList, trigger: 'change' }],
}

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const readonly = computed(() => props.readonly)
const dialogTitle = computed(() => (props.mode === 'create' ? '新增公告' : props.mode === 'view' ? '查看公告' : '编辑公告'))
const dialogButtons = computed(() => (
  readonly.value ? [] : [{ text: props.mode === 'create' ? '创建公告' : '保存修改', type: 'primary', click: handleSubmit }]
))

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, createDefaultForm(), props.model || {})
    formData.targetType = Number(formData.targetType || 1)
    formData.isTop = Number(formData.isTop || 0)
    formData.targetIdList = Array.isArray(formData.targetIdList)
      ? formData.targetIdList.map((item) => String(item))
      : []
    formRef.value?.clearValidate?.()
  },
  { immediate: true },
)

function handleTargetTypeChange() {
  formData.targetIdList = []
  formRef.value?.clearValidate?.('targetIdList')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    ...formData,
    targetType: Number(formData.targetType),
    isTop: Number(formData.isTop),
    targetIdList: formData.targetIdList.map((item) => String(item)),
  })
}
</script>

<style lang="scss" scoped>
.notice-form {
  :deep(.md-editor) {
    height: 420px;
  }

  :deep(.md-editor-preview-wrapper) {
    min-height: 320px;
    padding: 18px;
    border: 1px solid #e5eaf3;
    border-radius: 10px;
  }
}
</style>
