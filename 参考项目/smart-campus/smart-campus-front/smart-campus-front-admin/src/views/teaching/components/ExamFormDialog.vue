<template>
  <BaseDialog
    v-model:show="visible"
    :title="dialogTitle"
    width="760px"
    :buttons="dialogButtons"
    :show-cancel="!readonly"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :disabled="readonly"
      label-width="92px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="考试名称" prop="examName">
            <el-input v-model="formData.examName" placeholder="请输入考试名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属课程" prop="courseId">
            <el-select v-model="formData.courseId" placeholder="请选择课程" filterable>
              <el-option
                v-for="item in courseOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="考试试卷" prop="paperId">
            <el-select v-model="formData.paperId" placeholder="请选择试卷" filterable>
              <el-option
                v-for="item in paperOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="formData.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="formData.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="考试班级" prop="classIdList">
            <el-select
              v-model="formData.classIdList"
              multiple
              collapse-tags
              collapse-tags-tooltip
              filterable
              placeholder="请选择考试班级"
            >
              <el-option
                v-for="item in classOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="考试说明">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请输入考试说明"
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
  readonly: Boolean,
  courseOptions: { type: Array, default: () => [] },
  paperOptions: { type: Array, default: () => [] },
  classOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()

const createDefaultForm = () => ({
  examId: '',
  examName: '',
  courseId: '',
  paperId: '',
  startTime: '',
  endTime: '',
  description: '',
  classIdList: [],
})

const normalizeModel = (model = {}) => ({
  ...createDefaultForm(),
  ...model,
  classIdList: Array.isArray(model.classIdList)
    ? model.classIdList
        .map((classId) => Number(classId))
        .filter((classId) => !Number.isNaN(classId))
    : [],
})

const formData = reactive(createDefaultForm())
const formRules = {
  examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  paperId: [{ required: true, message: '请选择考试试卷', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  classIdList: [{ required: true, message: '请选择考试班级', trigger: 'change' }],
}

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const readonly = computed(() => props.readonly)
const dialogTitle = computed(() =>
  props.mode === 'create' ? '新增考试' : props.mode === 'view' ? '查看考试' : '编辑考试'
)
const dialogButtons = computed(() =>
  readonly.value
    ? []
    : [
        {
          text: props.mode === 'create' ? '创建考试' : '保存修改',
          type: 'primary',
          click: handleSubmit,
        },
      ]
)

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, normalizeModel(props.model || {}))
    formRef.value?.clearValidate?.()
  },
  { immediate: true }
)

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', {
    ...formData,
    classIdList: [...formData.classIdList],
  })
}
</script>
