<template>
  <BaseDrawer
    :show="show"
    title="个人信息"
    width="520px"
    :padding="20"
    :buttons="drawerButtons"
    @update:show="emit('update:show', $event)"
  >
    <el-form label-position="top" class="profile-form">
      <el-form-item label="个人头像">
        <div class="profile-form__avatar-row">
          <div class="profile-form__avatar-preview">
            <img v-if="localForm.avatarUrl" :src="localForm.avatarUrl" alt="头像预览">
            <span v-else>{{ localForm.realName?.slice(0, 1) || avatarText }}</span>
          </div>
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/png,image/jpeg,image/jpg,image/webp"
            :on-change="handleAvatarChange"
          >
            <el-button type="primary" plain>上传头像</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model.trim="localForm.realName" maxlength="20" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model.trim="localForm.email" maxlength="64" />
      </el-form-item>
    </el-form>
  </BaseDrawer>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import BaseDrawer from '@/components/BaseDrawer.vue'
import Message from '@/utils/Message'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  modelValue: {
    type: Object,
    required: true,
  },
  avatarText: {
    type: String,
    default: '同',
  },
})

const emit = defineEmits(['update:show', 'save'])

const localForm = reactive({
  realName: '',
  email: '',
  majorName: '',
  gradeName: '',
  motto: '',
  avatarPath: '',
  avatarUrl: '',
  avatarFile: null,
})

watch(
  () => props.modelValue,
  (value) => {
    localForm.realName = value?.realName || ''
    localForm.email = value?.email || ''
    localForm.majorName = value?.majorName || ''
    localForm.gradeName = value?.gradeName || ''
    localForm.motto = value?.motto || ''
    localForm.avatarPath = value?.avatarPath || ''
    localForm.avatarUrl = value?.avatarUrl || ''
    localForm.avatarFile = null
  },
  { immediate: true, deep: true }
)

const drawerButtons = computed(() => [
  {
    text: '保存资料',
    type: 'primary',
    click: () => emit('save', { ...localForm }),
  },
])

function handleAvatarChange(uploadFile) {
  const rawFile = uploadFile?.raw
  if (!rawFile) {
    return
  }
  const isValidImage = [
    'image/png',
    'image/jpeg',
    'image/jpg',
    'image/webp',
  ].includes(rawFile.type)
  if (!isValidImage) {
    Message.warning('请上传 png、jpg、jpeg 或 webp 格式图片')
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    localForm.avatarUrl = String(reader.result || '')
  }
  localForm.avatarFile = rawFile
  reader.readAsDataURL(rawFile)
}
</script>

<style scoped lang="scss">
.profile-form__avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-form__avatar-preview {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, #8bb8ff 0%, #2d73f5 100%);
  color: #fff;
  font-size: 26px;
  font-weight: 800;

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
