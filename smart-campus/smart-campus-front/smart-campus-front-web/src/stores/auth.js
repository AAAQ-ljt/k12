import { defineStore } from 'pinia'
import { getLoginInfo, login as loginRequest, logout as logoutRequest } from '@/api/auth'
import { getStudentToken, removeStudentToken, setStudentToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getStudentToken(),
    userInfo: null,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.userInfo?.realName || '同学',
  },
  actions: {
    normalizeUserInfo(payload) {
      if (!payload || typeof payload !== 'object') {
        return null
      }
      if (payload.userInfo && typeof payload.userInfo === 'object') {
        return payload.userInfo
      }
      if ('realName' in payload || 'phone' in payload || 'userId' in payload) {
        return payload
      }
      return null
    },
    restoreToken() {
      this.token = getStudentToken()
    },
    setLoginInfo(loginInfo) {
      const token = loginInfo?.token || ''
      this.token = token
      setStudentToken(token)
      this.userInfo = this.normalizeUserInfo(loginInfo)
    },
    updateUserInfo(userInfo) {
      const normalizedUserInfo = this.normalizeUserInfo(userInfo)
      if (!normalizedUserInfo) {
        return
      }
      this.userInfo = {
        ...(this.userInfo || {}),
        ...normalizedUserInfo,
      }
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      removeStudentToken()
    },
    async login(formData) {
      const result = await loginRequest(formData)
      if (!result?.token) {
        return null
      }
      this.setLoginInfo(result)
      return result
    },
    async fetchLoginInfo() {
      this.restoreToken()
      if (!this.token) {
        return null
      }
      const result = await getLoginInfo()
      const userInfo = this.normalizeUserInfo(result)
      if (!userInfo) {
        return this.userInfo || {}
      }
      this.userInfo = userInfo
      return userInfo
    },
    async logout() {
      if (this.token) {
        await logoutRequest()
      }
      this.clearAuth()
    },
  },
})
