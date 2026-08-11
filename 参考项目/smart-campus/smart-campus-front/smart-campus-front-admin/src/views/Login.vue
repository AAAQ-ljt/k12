<template>
  <div class="login-page">
    <div class="login-mask" />

    <div class="login-card">
      <div class="card-header">
        <h1>智慧校园管理后台</h1>
      </div>

      <el-form ref="formRef" :model="formData" :rules="rules" @keyup.enter="handleSubmit">
        <template v-if="showPhoneInput">
          <el-form-item prop="phone">
            <el-input v-model.trim="formData.phone" size="large" placeholder="请输入手机号" clearable />
          </el-form-item>
        </template>
        <template v-else>
          <div class="remembered-account">
            <div class="remembered-account__label">已记住账号</div>
            <div class="remembered-account__value">{{ formData.phone }}</div>
            <button type="button" class="remembered-account__switch" @click="handleSwitchAccount">
              切换账号
            </button>
          </div>
        </template>

        <el-form-item prop="password">
          <el-input v-model.trim="formData.password" size="large" type="password" placeholder="请输入密码" show-password
            clearable />
        </el-form-item>

        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model.trim="formData.captchaCode" size="large" placeholder="请输入图片验证码" clearable />
            <button type="button" class="captcha-image-button" :disabled="captchaLoading" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="图片验证码" class="captcha-image" />
              <span v-else class="captcha-placeholder">{{ captchaLoading ? '加载中...' : '点击获取' }}</span>
            </button>
          </div>
        </el-form-item>

        <div class="login-extra">
          <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
          <span class="login-remember-tip">仅记住账号，不保存密码</span>
        </div>

        <el-button class="submit-button" type="primary" size="large" :loading="submitting" @click="handleSubmit">
          登录
        </el-button>
      </el-form>

      <div class="login-tip">默认初始化密码通常为 123456</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Message from '@/utils/Message'
import { useAuthStore } from '@/stores/auth'
import { getCaptcha } from '@/api/auth'
import {
  getRememberedAdminAccount,
  removeRememberedAdminAccount,
  setRememberedAdminAccount,
} from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref()
const submitting = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')
const rememberAccount = ref(false)
const formData = reactive({
  phone: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
})

const showPhoneInput = computed(() => !rememberAccount.value || !formData.phone)

const rules = {
  phone: [{ required: true, message: '请输入登录手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图片验证码', trigger: 'blur' }],
}

const loadCaptcha = async () => {
  if (captchaLoading.value) {
    return
  }
  captchaLoading.value = true
  const result = await getCaptcha()
  captchaLoading.value = false
  formData.captchaKey = result?.captchaKey || ''
  formData.captchaCode = ''
  captchaImage.value = result?.captchaImage || ''
}

const syncRememberedAccount = () => {
  if (rememberAccount.value && formData.phone) {
    setRememberedAdminAccount(formData.phone)
    return
  }
  removeRememberedAdminAccount()
}

const handleSwitchAccount = () => {
  rememberAccount.value = false
  formData.phone = ''
  formRef.value?.clearValidate('phone')
  removeRememberedAdminAccount()
}

const handleSubmit = async () => {
  if (submitting.value) {
    return
  }
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  const result = await authStore.login(formData)
  submitting.value = false
  if (!result?.token) {
    loadCaptcha()
    return
  }
  syncRememberedAccount()
  Message.success('登录成功')
  router.replace(String(route.query.redirect || '/dashboard'))
}

onMounted(() => {
  const rememberedAccount = getRememberedAdminAccount()
  if (rememberedAccount) {
    formData.phone = rememberedAccount
    rememberAccount.value = true
  }
  loadCaptcha()
})
</script>

<style lang="scss" scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(rgba(7, 18, 35, 0.45), rgba(7, 18, 35, 0.58)),
    url('@/assets/bg.jpg') center / cover no-repeat;
}

.login-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      circle at 20% 20%,
      rgba(88, 145, 255, 0.18),
      transparent 30%
    ),
    radial-gradient(
      circle at 80% 80%,
      rgba(19, 196, 168, 0.14),
      transparent 28%
    );
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 36px 32px 28px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 20px 50px rgba(10, 22, 40, 0.28);
  backdrop-filter: blur(14px);
}

.card-header {
  margin-bottom: 24px;
  text-align: center;

  h1 {
    margin: 14px 0 8px;
    color: #183153;
    font-size: 28px;
    line-height: 1.2;
  }

  p {
    margin: 0;
    color: #6f8096;
    font-size: 14px;
  }
}

.card-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(40, 102, 255, 0.1);
  color: #2c65ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.submit-button {
  width: 100%;
  margin-top: 6px;
  border: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #2b63ff, #1d8fcd);
  box-shadow: 0 12px 24px rgba(43, 99, 255, 0.22);
}

.login-extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 0 18px;
}

.login-remember-tip {
  color: #6d80a5;
  font-size: 12px;
}

.remembered-account {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
  padding: 16px 18px;
  border: 1px solid #dbe5f4;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #eff5ff 100%);
}

.remembered-account__label {
  color: #6d80a5;
  font-size: 12px;
}

.remembered-account__value {
  color: #19325d;
  font-size: 18px;
  font-weight: 700;
}

.remembered-account__switch {
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #2d79ff;
  font-size: 13px;
  cursor: pointer;
}

.captcha-row {
  display: flex;
  width: 100%;
  gap: 12px;
}

.captcha-row :deep(.el-input) {
  min-width: 0;
  flex: 1;
}

.captcha-image-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 124px;
  height: 40px;
  padding: 0;
  border: 1px solid #dbe4f2;
  border-radius: 12px;
  background: linear-gradient(180deg, #f9fbff 0%, #eef4ff 100%);
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s ease, transform 0.2s ease;

  &:hover {
    border-color: #8cb0ff;
    transform: translateY(-1px);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.72;
    transform: none;
  }
}

.captcha-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  color: #6f8096;
  font-size: 13px;
}

.login-tip {
  margin-top: 16px;
  color: #74839a;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 640px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 28px 22px 24px;
    border-radius: 20px;
  }

  .card-header h1 {
    font-size: 24px;
  }

  .login-extra {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
