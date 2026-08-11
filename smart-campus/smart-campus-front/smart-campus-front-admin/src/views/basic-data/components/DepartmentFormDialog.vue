<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="680px" :buttons="dialogButtons" :show-cancel="!readonly">
    <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="readonly" label-width="92px" class="dialog-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="院系名称" prop="departmentName">
            <el-input v-model="formData.departmentName" placeholder="请输入院系名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="院系编码" prop="departmentCode">
            <el-input v-model="formData.departmentCode" placeholder="请输入院系编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人" prop="leaderName">
            <el-input v-model="formData.leaderName" placeholder="请输入负责人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
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
          <el-form-item label="院系说明" prop="description">
            <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入院系说明" />
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
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])

const formRef = ref()
const createDefaultForm = () => ({
  id: undefined,
  departmentId: undefined,
  departmentName: '',
  departmentCode: '',
  leaderName: '',
  contactPhone: '',
  status: 1,
  description: '',
})
const formData = reactive(createDefaultForm())
const formRules = {
  departmentName: [{ required: true, message: '请输入院系名称', trigger: 'blur' }],
  departmentCode: [{ required: true, message: '请输入院系编码', trigger: 'blur' }],
  leaderName: [{ required: true, message: '请输入负责人姓名', trigger: 'blur' }],
}

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const readonly = computed(() => props.readonly)
const dialogTitle = computed(() => (props.mode === 'create' ? '新增院系' : props.mode === 'view' ? '查看院系' : '编辑院系'))
const dialogButtons = computed(() =>
  readonly.value ? [] : [{ text: props.mode === 'create' ? '创建院系' : '保存修改', type: 'primary', click: handleSubmit }],
)

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
    status: Number(formData.status),
  })
}
</script>
