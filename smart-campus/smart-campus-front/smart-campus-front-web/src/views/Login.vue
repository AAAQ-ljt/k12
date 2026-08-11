<template>
  <div class="student-login">
    <div class="student-login__shell">
      <section class="student-login__brand">
        <header class="student-login__brand-header">
          <div class="student-login__logo">
            <div class="student-login__logo-mark">
              <span />
            </div>
            <div>
              <strong>智慧校园学习平台</strong>
              <p>Smart Campus Learning Platform</p>
            </div>
          </div>
        </header>

        <div class="student-login__hero">
          <h1>让学习连接未来</h1>
          <p>优质课程 | 在线学习 | 智能分析 | 高效成长</p>
        </div>

        <div class="student-login__visual" aria-hidden="true">
          <img src="@/assets/bg.png" alt="" class="student-login__visual-image">
        </div>

        <div class="student-login__feature-list">
          <div class="student-login__feature-card">
            <span class="student-login__feature-icon">课</span>
            <div>
              <strong>海量课程资源</strong>
              <p>课程内容聚合展示，学习入口更清晰。</p>
            </div>
          </div>
          <div class="student-login__feature-card">
            <span class="student-login__feature-icon">学</span>
            <div>
              <strong>随时随地学习</strong>
              <p>兼顾电脑与移动端，进度同步更轻松。</p>
            </div>
          </div>
          <div class="student-login__feature-card">
            <span class="student-login__feature-icon">析</span>
            <div>
              <strong>学习数据分析</strong>
              <p>围绕学习表现做记录，帮助持续提升。</p>
            </div>
          </div>
        </div>

        <footer class="student-login__footer">
          © 2026 智慧校园学习平台 版权所有
        </footer>
      </section>

      <section class="student-login__panel">
        <div class="student-login__card">
          <div class="student-login__card-header">
            <h2>欢迎登录</h2>
          </div>
          <el-form ref="formRef" :model="formData" :rules="rules" class="student-login__form"
            @keyup.enter="handleSubmit">
            <template v-if="showPhoneInput">
              <el-form-item prop="phone">
                <el-input v-model.trim="formData.phone" size="large" placeholder="请输入账号" clearable>
                  <template #prefix>
                    <span class="iconfont icon-phone"></span>
                  </template>
                </el-input>
              </el-form-item>
            </template>
            <template v-else>
              <div class="student-login__remembered-account">
                <div class="student-login__remembered-label">已记住账号</div>
                <div class="student-login__remembered-phone">{{ formData.phone }}</div>
                <button type="button" class="student-login__switch-account" @click="handleSwitchAccount">
                  切换账号
                </button>
              </div>
            </template>

            <el-form-item prop="password">
              <el-input v-model.trim="formData.password" size="large" type="password" placeholder="请输入密码" show-password
                clearable>
                <template #prefix>
                  <span class="iconfont icon-password"></span>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="student-login__captcha-row">
                <el-input v-model.trim="formData.captchaCode" size="large" placeholder="请输入图片验证码" clearable>
                  <template #prefix>
                    <span class="iconfont icon-checkcode"></span>
                  </template>
                </el-input>
                <button type="button" class="student-login__captcha-button" :disabled="captchaLoading"
                  @click="loadCaptcha">
                  <img v-if="captchaImage" :src="captchaImage" alt="图片验证码" class="student-login__captcha-image">
                  <span v-else class="student-login__captcha-placeholder">
                    {{ captchaLoading ? '加载中...' : '点击获取' }}
                  </span>
                </button>
              </div>
            </el-form-item>

            <div class="student-login__extra">
              <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
              <span class="student-login__remember-tip">仅记住账号，不保存密码</span>
            </div>

            <el-button class="student-login__submit" type="primary" size="large" :loading="submitting"
              @click="handleSubmit">
              登录
            </el-button>
          </el-form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCaptcha } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import Message from '@/utils/Message'
import {
  getRememberedStudentAccount,
  removeRememberedStudentAccount,
  setRememberedStudentAccount,
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
  phone: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
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
    setRememberedStudentAccount(formData.phone)
    return
  }
  removeRememberedStudentAccount()
}

const handleSwitchAccount = () => {
  rememberAccount.value = false
  formData.phone = ''
  formRef.value?.clearValidate('phone')
  removeRememberedStudentAccount()
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
  try {
    const result = await authStore.login(formData)
    if (!result?.token) {
      loadCaptcha()
      return
    }
    syncRememberedAccount()
    Message.success('登录成功')
    router.replace(String(route.query.redirect || '/'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const rememberedAccount = getRememberedStudentAccount()
  if (rememberedAccount) {
    formData.phone = rememberedAccount
    rememberAccount.value = true
  }
  loadCaptcha()
})
</script>

<style scoped>
.student-login {
  min-height: 100vh;
  padding: 28px 36px;
  background:
    radial-gradient(circle at 15% 28%, rgba(116, 162, 255, 0.12), transparent 24%),
    radial-gradient(circle at 82% 18%, rgba(116, 162, 255, 0.1), transparent 20%),
    linear-gradient(180deg, #edf4ff 0%, #f5f8ff 100%);
}

.student-login__shell {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  align-items: center;
  min-height: calc(100vh - 56px);
  max-width: 1480px;
  margin: 0 auto;
  padding: 42px 52px 38px 100px;
  gap: 36px;
}

.student-login__brand {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 100%;
  padding-right: 18px;
}

.student-login__brand-header {
  margin-bottom: 64px;
}

.student-login__logo {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  color: #0d1f44;
}

.student-login__logo strong {
  display: block;
  margin-bottom: 6px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.student-login__logo p {
  margin: 0;
  color: #58709d;
  font-size: 14px;
}

.student-login__logo-mark {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 6px;
  background: linear-gradient(145deg, #4992ff 0%, #2068f2 100%);
  box-shadow: 0 12px 24px rgba(36, 110, 243, 0.28);
}

.student-login__logo-mark::before,
.student-login__logo-mark::after,
.student-login__logo-mark span {
  position: absolute;
  content: '';
}

.student-login__logo-mark::before {
  left: 11px;
  right: 11px;
  top: 15px;
  height: 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.94);
  clip-path: polygon(0 38%, 50% 0, 100% 38%, 100% 100%, 0 100%);
}

.student-login__logo-mark::after {
  left: 18px;
  right: 18px;
  top: 20px;
  height: 5px;
  border-radius: 6px;
  background: #2f7bf7;
}

.student-login__logo-mark span {
  width: 8px;
  height: 8px;
  left: 22px;
  bottom: 11px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.95);
}

.student-login__hero {
  position: relative;
  z-index: 2;
  color: #0a1d46;
}

.student-login__hero h1 {
  margin: 0 0 18px;
  font-size: 62px;
  line-height: 1.1;
  letter-spacing: 0.02em;
}

.student-login__hero p {
  margin: 0;
  color: #556b96;
  font-size: 22px;
  line-height: 1.6;
}

.student-login__visual {
  position: absolute;
  left: 50px;
  bottom: 82px;
  z-index: 1;
  width: 680px;
  pointer-events: none;
  overflow: hidden;
}

.student-login__visual-image {
  display: block;
  width: 100%;
  height: auto;
  opacity: 0.98;
}

.student-login__feature-list {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 760px;
  margin-top: auto;
  padding-top: 320px;
}

.student-login__feature-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px 16px 18px;
  border: 1px solid rgba(214, 226, 246, 0.95);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 8px 20px rgba(132, 159, 208, 0.07);
}

.student-login__feature-icon {
  flex: 0 0 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 6px;
  background: linear-gradient(145deg, #eef5ff, #ffffff);
  color: #2d79ff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px rgba(59, 121, 255, 0.12);
}

.student-login__feature-card strong {
  display: block;
  margin-bottom: 6px;
  color: #19325d;
  font-size: 16px;
}

.student-login__feature-card p {
  margin: 0;
  color: #6f83aa;
  font-size: 12px;
  line-height: 1.6;
}

.student-login__footer {
  position: relative;
  z-index: 2;
  margin-top: 34px;
  color: #7f91b3;
  font-size: 13px;
}

.student-login__panel {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.student-login__card {
  width: 100%;
  max-width: 400px;
  padding: 34px 30px 30px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 16px 36px rgba(125, 153, 201, 0.14);
}

.student-login__card-header {
  margin-bottom: 22px;
}

.student-login__card-header h2 {
  margin: 0 0 8px;
  color: #0a1d46;
  font-size: 26px;
  line-height: 1.14;
}

.student-login__card-header p {
  margin: 0;
  color: #7083a6;
  font-size: 13px;
}

.student-login__tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding-bottom: 8px;
  margin-bottom: 20px;
  color: #2d79ff;
  font-size: 14px;
  font-weight: 600;
}

.student-login__tab::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 3px;
  border-radius: 6px;
  background: #2d79ff;
}

.student-login__form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.student-login__form :deep(.el-input__wrapper) {
  min-height: 44px;
  padding: 0 14px;
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dbe5f4 inset;
}

.student-login__form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2d79ff inset;
}

.student-login__form :deep(.el-input__inner) {
  font-size: 13px;
  color: #20365c;
}

.student-login__form :deep(.el-input__inner::placeholder) {
  color: #95a4bf;
}

.student-login__input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  color: #8ea0be;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.student-login__remembered-account {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding: 16px 18px;
  border: 1px solid #dbe5f4;
  border-radius: 6px;
  background: linear-gradient(180deg, #f8fbff 0%, #eff5ff 100%);
}

.student-login__remembered-label {
  color: #6d80a5;
  font-size: 12px;
}

.student-login__remembered-phone {
  color: #19325d;
  font-size: 18px;
  font-weight: 700;
}

.student-login__switch-account {
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #2d79ff;
  font-size: 13px;
  cursor: pointer;
}

.student-login__captcha-row {
  display: flex;
  width: 100%;
  gap: 12px;
}

.student-login__captcha-row :deep(.el-input) {
  min-width: 0;
  flex: 1;
}

.student-login__captcha-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 124px;
  height: 44px;
  padding: 0;
  border: 1px solid #dbe4f2;
  border-radius: 6px;
  background: linear-gradient(180deg, #f9fbff 0%, #eef4ff 100%);
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.student-login__captcha-button:hover {
  border-color: #8cb0ff;
}

.student-login__captcha-button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
  transform: none;
}

.student-login__captcha-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-login__captcha-placeholder {
  color: #6f8096;
  font-size: 13px;
}

.student-login__extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 0 18px;
}

.student-login__remember-tip {
  color: #6d80a5;
  font-size: 12px;
}

.student-login__submit {
  width: 100%;
  min-height: 44px;
  border: 0;
  border-radius: 6px;
  background: linear-gradient(135deg, #3181ff 0%, #2367f2 100%);
  box-shadow: 0 10px 20px rgba(45, 121, 255, 0.2);
  font-size: 15px;
  font-weight: 600;
}

.student-login__submit:hover,
.student-login__submit:focus {
  background: linear-gradient(135deg, #3f8aff 0%, #2b71fb 100%);
}

@media (max-width: 1280px) {
  .student-login__shell {
    grid-template-columns: minmax(0, 1fr) 390px;
    padding: 36px;
  }

  .student-login__hero h1 {
    font-size: 52px;
  }

  .student-login__hero p {
    font-size: 20px;
  }

  .student-login__visual {
    left: -72px;
    bottom: 96px;
    width: 700px;
  }

  .student-login__feature-list {
    padding-top: 280px;
  }
}

@media (max-width: 960px) {
  .student-login {
    padding: 18px 20px;
  }

  .student-login__shell {
    grid-template-columns: 1fr;
    min-height: calc(100vh - 36px);
    padding: 24px 10px 28px;
  }

  .student-login__brand-header {
    margin-bottom: 36px;
  }

  .student-login__hero {
    margin-bottom: 24px;
  }

  .student-login__hero h1 {
    font-size: 40px;
  }

  .student-login__hero p {
    font-size: 17px;
  }

  .student-login__visual {
    position: relative;
    left: auto;
    bottom: auto;
    width: 100%;
    max-width: 560px;
    margin: 24px auto 0;
    order: 2;
  }

  .student-login__feature-list {
    grid-template-columns: 1fr;
    max-width: none;
    margin-top: 24px;
    padding-top: 0;
  }

  .student-login__panel {
    order: 3;
  }

  .student-login__card {
    max-width: 380px;
    padding: 28px 22px 24px;
  }

  .student-login__card-header {
    margin-bottom: 18px;
  }

  .student-login__card-header h2 {
    font-size: 24px;
  }

  .student-login__card-header p,
  .student-login__tab {
    font-size: 14px;
  }

  .student-login__submit {
    min-height: 42px;
    font-size: 14px;
  }
}

@media (max-width: 640px) {
  .student-login {
    padding: 16px;
  }

  .student-login__shell {
    padding: 18px 4px 24px;
  }

  .student-login__logo {
    gap: 12px;
  }

  .student-login__logo strong {
    font-size: 18px;
  }

  .student-login__logo p,
  .student-login__footer {
    font-size: 12px;
  }

  .student-login__hero h1 {
    font-size: 32px;
  }

  .student-login__feature-card {
    padding: 16px;
  }

  .student-login__captcha-row {
    gap: 10px;
  }

  .student-login__captcha-button {
    width: 116px;
  }

  .student-login__extra {
    align-items: flex-start;
    flex-direction: column;
  }

  .student-login__form :deep(.el-input__wrapper) {
    min-height: 46px;
  }
}
</style>
