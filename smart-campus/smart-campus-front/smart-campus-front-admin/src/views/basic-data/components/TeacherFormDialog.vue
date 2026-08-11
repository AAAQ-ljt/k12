<template>
  <BaseDialog v-model:show="visible" :title="dialogTitle" width="760px" :buttons="dialogButtons" :show-cancel="!readonly">
    <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="readonly" label-width="92px" class="dialog-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="教师姓名" prop="realName">
            <el-input v-model="formData.realName" placeholder="请输入教师姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="userNo">
            <el-input v-model="formData.userNo" placeholder="请输入工号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="院系">
            <el-select v-model="formData.departmentId" placeholder="请选择院系" clearable filterable @change="handleDepartmentChange">
              <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专业">
            <el-select v-model="formData.majorId" placeholder="请选择专业" clearable filterable @change="handleMajorChange">
              <el-option v-for="item in filteredMajorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职称" prop="titleName">
            <el-select v-model="formData.titleName" placeholder="请选择职称" filterable>
              <el-option v-for="item in titleOptions" :key="item.value ?? item.label" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="授课班级" prop="classIdList">
            <el-select
              v-model="formData.classIdList"
              multiple
              collapse-tags
              collapse-tags-tooltip
              filterable
              placeholder="请选择授课班级"
            >
              <el-option v-for="item in filteredClassOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
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
  classOptions: { type: Array, default: () => [] },
  titleOptions: { type: Array, default: () => [] },
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()

const createDefaultForm = () => ({
  id: undefined,
  realName: '',
  userNo: '',
  departmentId: undefined,
  majorId: undefined,
  titleName: '',
  classId: '',
  classIdList: [],
  phone: '',
  email: '',
  gender: 1,
  status: 1,
})

const normalizeClassIdList = (model = {}) => {
  if (Array.isArray(model.classIdList)) {
    return model.classIdList.map((item) => Number(item)).filter((item) => !Number.isNaN(item))
  }
  return String(model.classId ?? '')
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => !Number.isNaN(item))
}

const normalizeModel = (model = {}) => ({
  ...createDefaultForm(),
  ...model,
  departmentId: model.departmentId == null ? undefined : Number(model.departmentId),
  majorId: model.majorId == null ? undefined : Number(model.majorId),
  classIdList: normalizeClassIdList(model),
  gender: model.gender == null ? 1 : Number(model.gender),
  status: model.status == null ? 1 : Number(model.status),
})

const formData = reactive(createDefaultForm())
const formRules = {
  realName: [{ required: true, message: '请输入教师姓名', trigger: 'blur' }],
  userNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  titleName: [{ required: true, message: '请选择职称', trigger: 'change' }],
  classIdList: [{ required: true, message: '请选择授课班级', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
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
const filteredClassOptions = computed(() =>
  props.classOptions.filter((item) => {
    if (formData.departmentId != null && Number(item.extra?.departmentId) !== Number(formData.departmentId)) {
      return false
    }
    if (formData.majorId != null && Number(item.extra?.majorId) !== Number(formData.majorId)) {
      return false
    }
    return true
  }),
)
const dialogTitle = computed(() => (props.mode === 'create' ? '新增教师' : props.mode === 'view' ? '查看教师' : '编辑教师'))
const dialogButtons = computed(() =>
  readonly.value ? [] : [{ text: props.mode === 'create' ? '创建教师' : '保存修改', type: 'primary', click: handleSubmit }],
)

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, normalizeModel(props.model || {}))
    formRef.value?.clearValidate?.()
  },
  { immediate: true },
)

watch(
  () => [formData.departmentId, formData.majorId, props.show],
  () => {
    if (formData.majorId != null && !filteredMajorOptions.value.some((item) => Number(item.value) === Number(formData.majorId))) {
      formData.majorId = undefined
    }
    formData.classIdList = formData.classIdList.filter((item) =>
      filteredClassOptions.value.some((option) => Number(option.value) === Number(item)),
    )
  },
  { immediate: true, deep: true },
)

function handleDepartmentChange() {
  formData.majorId = undefined
  formData.classIdList = []
}

function handleMajorChange() {
  formData.classIdList = []
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    ...formData,
    classId: formData.classIdList.map((item) => String(item)).join(','),
    classIdList: [...formData.classIdList],
    gender: Number(formData.gender),
    status: Number(formData.status),
  })
}
</script>
