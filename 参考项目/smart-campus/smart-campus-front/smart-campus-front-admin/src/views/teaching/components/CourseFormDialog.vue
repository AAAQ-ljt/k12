<template>
  <BaseDialog
    v-model:show="visible"
    :title="dialogTitle"
    width="960px"
    :buttons="dialogButtons"
    :show-cancel="!readonly"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :disabled="readonly"
      label-width="92px"
      class="course-form"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="课程名称" prop="courseName">
            <el-input v-model="formData.courseName" placeholder="请输入课程名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="授课教师">
            <el-input :model-value="currentTeacherName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="课程封面">
            <div class="course-cover-selector">
              <div v-if="selectedCoverPreview" class="course-cover-selector__preview">
                <img :src="selectedCoverPreview" alt="cover" />
              </div>
              <div v-else class="course-cover-selector__placeholder">未选择封面</div>
              <div class="course-cover-selector__actions">
                <el-button type="primary" plain @click="openSelector" :disabled="readonly">
                  选择资源
                </el-button>
                <el-button link @click="clearCover" :disabled="readonly || !formData.coverResourceId">
                  清空
                </el-button>
                <span class="course-cover-selector__name">
                  {{ selectedCoverName || '请选择图片资源作为封面' }}
                </span>
              </div>
            </div>
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
              <el-option
                v-for="item in classOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="课程状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="课程简介">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入课程简介"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <ResourceSelectorDialog
      v-model:show="selectorVisible"
      :resource-type="2"
      @select="handleResourceSelect"
    />
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'
import ResourceSelectorDialog from '@/views/resource/components/ResourceSelectorDialog.vue'
import { buildResourceFileUrl } from '@/utils/resource'

const props = defineProps({
  show: Boolean,
  mode: { type: String, default: 'create' },
  model: { type: Object, default: () => ({}) },
  classOptions: { type: Array, default: () => [] },
  currentTeacherName: { type: String, default: '' },
  readonly: Boolean,
})

const emit = defineEmits(['update:show', 'submit'])
const formRef = ref()
const selectorVisible = ref(false)

const createDefaultForm = () => ({
  courseId: undefined,
  courseName: '',
  coverResourceId: undefined,
  coverPath: '',
  coverName: '',
  description: '',
  recordStatus: 0,
  status: 1,
  classIdList: [],
})

const normalizeModel = (model = {}) => ({
  ...createDefaultForm(),
  ...model,
  courseId: model.courseId ?? model.id,
  coverResourceId:
    model.coverResourceId == null ? undefined : Number(model.coverResourceId),
  coverPath: model.coverPath ?? '',
  coverName: model.coverName ?? '',
  recordStatus: model.recordStatus == null ? 0 : Number(model.recordStatus),
  status: model.status == null ? 1 : Number(model.status),
  classIdList: Array.isArray(model.classIdList)
    ? model.classIdList
        .map((classId) => Number(classId))
        .filter((classId) => !Number.isNaN(classId))
    : [],
})

const formData = reactive(createDefaultForm())
const formRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  classIdList: [
    { required: true, message: '请选择授课班级', trigger: 'change' },
  ],
}

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const readonly = computed(() => props.readonly)
const dialogTitle = computed(() =>
  props.mode === 'create'
    ? '新增课程'
    : props.mode === 'view'
      ? '查看课程'
      : '编辑课程'
)
const dialogButtons = computed(() =>
  readonly.value
    ? []
    : [
        {
          text: props.mode === 'create' ? '创建课程' : '保存修改',
          type: 'primary',
          click: handleSubmit,
        },
      ]
)
const selectedCoverPreview = computed(() =>
  formData.coverPath ? buildResourceFileUrl(formData.coverPath) : ''
)
const selectedCoverName = computed(() => formData.coverName || '')

watch(
  () => [props.show, props.model],
  () => {
    Object.assign(formData, normalizeModel(props.model || {}))
    formRef.value?.clearValidate?.()
  },
  { immediate: true }
)

function openSelector() {
  selectorVisible.value = true
}

function clearCover() {
  formData.coverResourceId = undefined
  formData.coverPath = ''
  formData.coverName = ''
}

function handleResourceSelect(resource) {
  formData.coverResourceId = Number(resource.resourceId)
  formData.coverPath = resource.coverPath || resource.filePath || ''
  formData.coverName = resource.resourceName || ''
}

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

<style lang="scss" scoped>
.course-form {
  padding-bottom: 8px;
}

.course-cover-selector {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
}

.course-cover-selector__preview,
.course-cover-selector__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 108px;
  height: 68px;
  flex: 0 0 108px;
  overflow: hidden;
  border: 1px solid #dbe5f3;
  border-radius: 14px;
  background: linear-gradient(135deg, #f1f5ff 0%, #f9fbff 100%);
  color: #7a8aa6;
  font-size: 12px;
}

.course-cover-selector__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-cover-selector__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  min-width: 0;
}

.course-cover-selector__name {
  color: #65758f;
  font-size: 13px;
}
</style>
