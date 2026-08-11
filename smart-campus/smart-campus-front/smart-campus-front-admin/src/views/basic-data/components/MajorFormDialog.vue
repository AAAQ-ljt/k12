<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="700px" :buttons="dialogButtons" :show-cancel="!readonly">
    <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="readonly" label-width="92px" class="dialog-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="专业名称" prop="majorName">
            <el-input v-model="formData.majorName" placeholder="请输入专业名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专业编码" prop="majorCode">
            <el-input v-model="formData.majorCode" placeholder="请输入专业编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属院系" prop="departmentId">
            <el-select v-model="formData.departmentId" placeholder="请选择院系" filterable>
              <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学制" prop="educationalSystemType">
            <el-select v-model="formData.educationalSystemType" placeholder="请选择学制" filterable>
              <el-option label="3年制" :value="3" />
              <el-option label="4年制" :value="4" />
            </el-select>
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
          <el-form-item label="专业简介" prop="description">
            <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入专业简介" />
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
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()
const createDefaultForm = () => ({
  id: undefined,
  majorId: undefined,
  majorName: '',
  majorCode: '',
  departmentId: undefined,
  educationalSystemType: 4,
  status: 1,
  description: '',
})
const formData = reactive(createDefaultForm())
const formRules = {
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  majorCode: [{ required: true, message: '请输入专业编码', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属院系', trigger: 'change' }],
}
const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const readonly = computed(() => props.readonly)
const dialogTitle = computed(() => (props.mode === 'create' ? '新增专业' : props.mode === 'view' ? '查看专业' : '编辑专业'))
const dialogButtons = computed(() => (
  readonly.value ? [] : [{ text: props.mode === 'create' ? '创建专业' : '保存修改', type: 'primary', click: handleSubmit }]
))

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, createDefaultForm(), props.model || {})
    formRef.value?.clearValidate?.()
  },
  { immediate: true },
)

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    ...formData,
    departmentId: Number(formData.departmentId),
    educationalSystemType: Number(formData.educationalSystemType),
    status: Number(formData.status),
  })
}
</script>
