import request from '@/api/request'

export const login = (params) =>
  request.post('/login/doLogin', params, {
    showLoading: false,
  })

export const getCaptcha = () =>
  request.post('/login/getCaptcha', {}, {
    showLoading: false,
    showError: false,
  })

export const getLoginInfo = () =>
  request.post('/login/getLoginInfo', {}, {
    showLoading: false,
    showError: false,
  })

export const logout = () =>
  request.post('/login/logout', {}, {
    showLoading: false,
    showError: false,
  })
