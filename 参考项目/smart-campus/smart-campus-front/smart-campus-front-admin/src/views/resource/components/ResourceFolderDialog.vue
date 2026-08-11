<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="520px" :buttons="dialogButtons">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="92px" class="dialog-form">
      <el-form-item :label="labelText" prop="resourceName">
        <el-input
          v-model="formData.resourceName"
          :maxlength="100"
          show-word-limit
          :placeholder="placeholderText"
        />
      </el-form-item>
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
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()
const formData = reactive({
  resourceName: '',
})

const formRules = {
  resourceName: [{ required: true, message: '请输入目录名称', trigger: 'blur' }],
}

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const dialogTitle = computed(() => (props.mode === 'rename' ? '重命名' : '新建目录'))
const labelText = computed(() => (props.mode === 'rename' ? '新名称' : '目录名称'))
const placeholderText = computed(() => (props.mode === 'rename' ? '请输入新的目录名称' : '请输入目录名称'))
const dialogButtons = computed(() => [
  {
    text: props.mode === 'rename' ? '保存修改' : '创建目录',
    type: 'primary',
    click: handleSubmit,
  },
])

watch(
  () => [props.show, props.model],
  () => {
    formData.resourceName = props.model?.resourceName ?? ''
    formRef.value?.clearValidate?.()
  },
  { immediate: true },
)

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', {
    ...props.model,
    resourceName: formData.resourceName.trim(),
  })
}
</script>
