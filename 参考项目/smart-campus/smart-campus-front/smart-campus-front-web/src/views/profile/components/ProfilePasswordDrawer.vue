<template>
  <BaseDrawer
    :show="show"
    title="修改密码"
    width="460px"
    :padding="20"
    :buttons="drawerButtons"
    @update:show="emit('update:show', $event)"
  >
    <el-form label-position="top" class="password-form">
      <el-form-item label="原密码">
        <el-input
          v-model.trim="passwordForm.oldPassword"
          type="password"
          show-password
          maxlength="20"
          placeholder="请输入当前登录密码"
        />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input
          v-model.trim="passwordForm.newPassword"
          type="password"
          show-password
          maxlength="20"
          placeholder="请输入新的登录密码"
        />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input
          v-model.trim="passwordForm.confirmPassword"
          type="password"
          show-password
          maxlength="20"
          placeholder="请再次输入新密码"
        />
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
})

const emit = defineEmits(['update:show', 'save'])

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

watch(
  () => props.show,
  (visible) => {
    if (!visible) {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  }
)

const drawerButtons = computed(() => [
  {
    text: '确认修改',
    type: 'primary',
    click: () => submit(),
  },
])

function submit() {
  if (!passwordForm.oldPassword) {
    Message.warning('请输入原密码')
    return
  }
  if (!passwordForm.newPassword) {
    Message.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    Message.warning('新密码长度不能少于 6 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.warning('两次输入的新密码不一致')
    return
  }
  emit('save', {
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
  })
}
</script>

<style scoped lang="scss">
.password-form {
  :deep(.el-input__wrapper) {
    min-height: 40px;
  }
}
</style>
