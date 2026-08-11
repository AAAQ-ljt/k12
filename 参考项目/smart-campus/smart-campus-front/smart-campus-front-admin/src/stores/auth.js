import { defineStore } from 'pinia'
import { getLoginInfo, login as loginRequest, logout as logoutRequest } from '@/api/auth'
import { getAdminToken, removeAdminToken, setAdminToken } from '@/utils/auth'
import { getCurrentMenuList } from '@/api/permission'

const collectMenuCodes = (menus = []) => {
  const codes = []
  menus.forEach((item) => {
    if (item?.menuCode) {
      codes.push(item.menuCode)
    }
    if (Array.isArray(item?.children)) {
      codes.push(...collectMenuCodes(item.children))
    }
  })
  return [...new Set(codes)]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getAdminToken(),
    userInfo: null,
    menuList: [],
    menuCodes: [],
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.userInfo?.realName || '未登录用户',
  },
  actions: {
    normalizeUserInfo(payload) {
      if (!payload || typeof payload !== 'object') {
        return null
      }
      if (payload.userInfo && typeof payload.userInfo === 'object') {
        return payload.userInfo
      }
      if ('realName' in payload || 'phone' in payload || 'roleType' in payload || 'userId' in payload) {
        return payload
      }
      return null
    },
    restoreToken() {
      this.token = getAdminToken()
    },
    setLoginInfo(loginInfo) {
      const token = loginInfo?.token || this.token || ''
      this.token = token
      if (token) {
        setAdminToken(token)
      }
      this.userInfo = this.normalizeUserInfo(loginInfo)
      this.menuList = Array.isArray(loginInfo?.menuList) ? loginInfo.menuList : []
      this.menuCodes = Array.isArray(loginInfo?.menuCodes)
        ? loginInfo.menuCodes
        : collectMenuCodes(this.menuList)
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      this.menuList = []
      this.menuCodes = []
      removeAdminToken()
    },
    hasMenu(menuCode) {
      if (!menuCode) {
        return true
      }
      return this.menuCodes.includes(menuCode)
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
      this.setLoginInfo(result)
      if (!this.menuCodes.length) {
        this.menuList = await getCurrentMenuList() || []
        this.menuCodes = collectMenuCodes(this.menuList)
      }
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
