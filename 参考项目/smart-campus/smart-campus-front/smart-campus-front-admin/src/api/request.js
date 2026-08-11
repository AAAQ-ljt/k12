import axios from 'axios'
import { ElLoading } from 'element-plus'
import router from '@/router'
import Message from '@/utils/Message'
import { getAdminToken, removeAdminToken } from '@/utils/auth'

const CONTENT_TYPE_FORM = 'multipart/form-data'
const CONTENT_TYPE_JSON = 'application/json'
const RESPONSE_TYPE_JSON = 'json'

let loadingInstance = null

const instance = axios.create({
  withCredentials: true,
  baseURL: `${import.meta.env.PROD ? import.meta.env.VITE_DOMAIN : ''}/api`,
  timeout: 10 * 1000,
})

const closeLoading = () => {
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

const redirectToLogin = () => {
  removeAdminToken()
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
      return Promise.reject({ showError: false, msg: responseData?.info })
    }

    if (errorCallback) {
      errorCallback(responseData)
    }

    return Promise.reject({
      showError,
      msg: responseData?.info || '请求失败',
    })
  },
  (error) => {
    closeLoading()
    return Promise.reject({
      showError: true,
      msg: error?.message || '网络异常',
    })
  },
)

const buildHeaders = (contentType) => ({
  'Content-Type': contentType,
  'X-Requested-With': 'XMLHttpRequest',
  adminToken: getAdminToken(),
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

const handleError = (error) => {
  if (error?.showError) {
    Message.error(error.msg)
  }
  return null
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
