import axios from 'axios'
import { ElLoading } from 'element-plus'
import router from '@/router'
import Message from '@/utils/Message'
import { getStudentToken, removeStudentToken } from '@/utils/auth'

const CONTENT_TYPE_FORM = 'multipart/form-data'
const CONTENT_TYPE_JSON = 'application/json'
const RESPONSE_TYPE_JSON = 'json'

let loadingInstance = null

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const REQUEST_TIMEOUT = Number(import.meta.env.VITE_REQUEST_TIMEOUT || 10000)

const instance = axios.create({
  withCredentials: true,
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
})

const closeLoading = () => {
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

const redirectToLogin = () => {
  removeStudentToken()
  const currentPath = router.currentRoute.value.fullPath
  if (router.currentRoute.value.path === '/login') {
    return
  }
  router.replace({
    path: '/login',
    query: currentPath ? { redirect: currentPath } : undefined,
  })
}

instance.interceptors.request.use(
  (config) => {
    if (config.showLoading) {
      closeLoading()
      loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
      })
    }
    return config
  },
  (error) => {
    closeLoading()
    Message.error('请求发送失败')
    return Promise.reject(error)
  },
)

instance.interceptors.response.use(
  (response) => {
    closeLoading()
    const { responseType, errorCallback, showError = true } = response.config
    const responseData = response.data

    if (responseType === 'arraybuffer' || responseType === 'blob') {
      return responseData
    }

    if (responseData?.code === 200) {
      return responseData?.data
    }

    if (responseData?.code === 901) {
      redirectToLogin()
      return Promise.reject({ showError: false, msg: responseData?.info, code: responseData?.code })
    }

    if (errorCallback) {
      errorCallback(responseData)
    }

    return Promise.reject(buildBusinessError(responseData, showError))
  },
  (error) => {
    closeLoading()
    return Promise.reject(buildNetworkError(error))
  },
)

const buildHeaders = (contentType) => ({
  'Content-Type': contentType,
  'X-Requested-With': 'XMLHttpRequest',
  studentToken: getStudentToken(),
})

const buildFormData = (params = {}) => {
  const formData = new FormData()
  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => formData.append(key, item ?? ''))
      return
    }
    formData.append(key, value ?? '')
  })
  return formData
}

const normalizeRequestError = (error, defaultMessage = '请求失败') => ({
  showError: error?.showError !== false,
  msg: error?.msg || error?.message || defaultMessage,
  data: error?.data,
  code: error?.code,
  originalError: error,
})

const buildBusinessError = (responseData, showError) => ({
  showError,
  msg: responseData?.info || responseData?.msg || '请求失败',
  data: responseData?.data,
  code: responseData?.code,
})

const buildNetworkError = (error) => {
  if (error?.code === 'ECONNABORTED') {
    return normalizeRequestError(error, '请求超时，请稍后重试')
  }
  if (!window.navigator.onLine) {
    return normalizeRequestError(error, '网络连接不可用，请检查网络')
  }
  const status = error?.response?.status
  const statusMessageMap = {
    400: '请求参数错误',
    401: '登录状态已失效，请重新登录',
    403: '没有权限访问该资源',
    404: '请求资源不存在',
    500: '服务器异常，请稍后重试',
  }
  return normalizeRequestError(error, statusMessageMap[status] || '网络异常，请稍后重试')
}

const handleError = (error) => {
  const requestError = normalizeRequestError(error)
  if (requestError.showError) {
    Message.error(requestError.msg)
  }
  return Promise.reject(requestError)
}

const sendRequest = async ({
  method = 'post',
  url,
  params,
  data,
  dataType,
  showLoading = true,
  responseType = RESPONSE_TYPE_JSON,
  showError = true,
  errorCallback,
  uploadProgressCallback,
}) => {
  const isJson = dataType === 'json'
  const contentType = isJson ? CONTENT_TYPE_JSON : CONTENT_TYPE_FORM
  const requestData = method === 'get'
    ? undefined
    : isJson
      ? (data ?? params ?? {})
      : buildFormData(data ?? params ?? {})

  try {
    return await instance.request({
      url,
      method,
      params: method === 'get' ? params : undefined,
      data: requestData,
      headers: buildHeaders(contentType),
      responseType,
      showLoading,
      showError,
      errorCallback,
      onUploadProgress: uploadProgressCallback,
    })
  } catch (error) {
    return handleError(error)
  }
}

const request = {
  get(url, params = {}, options = {}) {
    return sendRequest({ ...options, method: 'get', url, params })
  },
  post(url, params = {}, options = {}) {
    return sendRequest({ ...options, method: 'post', url, params })
  },
  put(url, data = {}, options = {}) {
    return sendRequest({ ...options, method: 'put', url, data, dataType: options.dataType || 'json' })
  },
  delete(url, data = {}, options = {}) {
    return sendRequest({ ...options, method: 'delete', url, data, dataType: options.dataType || 'json' })
  },
}

export default request
