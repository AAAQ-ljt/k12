<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="720px" :buttons="dialogButtons" :show-cancel="!readonly">
    <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="readonly" label-width="96px" class="dialog-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="班级名称" prop="className">
            <el-input v-model="formData.className" placeholder="请输入班级名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属院系" prop="departmentId">
            <el-select v-model="formData.departmentId" placeholder="请选择院系" filterable @change="handleDepartmentChange">
              <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属专业" prop="majorId">
            <el-select v-model="formData.majorId" placeholder="请选择专业" filterable @change="handleMajorChange">
              <el-option v-for="item in filteredMajorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学制" prop="grade">
            <el-select v-model="formData.grade" placeholder="跟随专业学制" filterable disabled>
              <el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="辅导员" prop="counselorName">
            <el-input v-model="formData.counselorName" placeholder="请输入辅导员姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班主任" prop="headTeacherName">
            <el-input v-model="formData.headTeacherName" placeholder="请输入班主任姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="班级说明" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="请输入班级说明"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'

const props = defineProps({
  show: Boolean,
  mode: { type: String, default: 'create' },
  model: { type: Object, default: () => ({}) },
  departmentOptions: { type: Array, default: () => [] },
  majorOptions: { type: Array, default: () => [] },
  gradeOptions: { type: Array, default: () => [] },
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()
const createDefaultForm = () => ({
  id: undefined,
  classId: undefined,
  className: '',
  departmentId: undefined,
  majorId: undefined,
  grade: undefined,
  counselorName: '',
  headTeacherName: '',
  status: 1,
  description: '',
})
const formData = reactive(createDefaultForm())
const formRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属院系', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
  counselorName: [{ required: true, message: '请输入辅导员姓名', trigger: 'blur' }],
  headTeacherName: [{ required: true, message: '请输入班主任姓名', trigger: 'blur' }],
}
const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const readonly = computed(() => props.readonly)
const filteredMajorOptions = computed(() => {
  if (formData.departmentId == null) {
    return props.majorOptions
  }
  return props.majorOptions.filter((item) => Number(item.extra?.departmentId) === Number(formData.departmentId))
})
const dialogTitle = computed(() => (props.mode === 'create' ? '新增班级' : props.mode === 'view' ? '查看班级' : '编辑班级'))
const dialogButtons = computed(() => (
  readonly.value ? [] : [{ text: props.mode === 'create' ? '创建班级' : '保存修改', type: 'primary', click: handleSubmit }]
))

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, createDefaultForm(), props.model || {})
    formRef.value?.clearValidate?.()
  },
  { immediate: true },
)

watch(
  () => [formData.departmentId, props.show],
  () => {
    if (formData.majorId != null && !filteredMajorOptions.value.some((item) => Number(item.value) === Number(formData.majorId))) {
      formData.majorId = undefined
    }
  },
  { immediate: true, deep: true },
)

watch(
  () => [formData.majorId, props.majorOptions],
  () => {
    const currentMajor = props.majorOptions.find((item) => Number(item.value) === Number(formData.majorId))
    formData.grade = currentMajor?.extra?.educationalSystemType
  },
  { immediate: true, deep: true },
)

function handleDepartmentChange() {
  formData.majorId = undefined
  formData.grade = undefined
}

function handleMajorChange() {
  const currentMajor = props.majorOptions.find((item) => Number(item.value) === Number(formData.majorId))
  formData.grade = currentMajor?.extra?.educationalSystemType
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    ...formData,
    departmentId: Number(formData.departmentId),
    majorId: Number(formData.majorId),
    status: Number(formData.status),
  })
}
</script>
