import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { getToken } from '@/utils/token';
import { useAuthStore } from '@/stores/auth';
import { useUiStore } from '@/stores/ui';
import type { ResponseVO } from '@/types/common';

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

/** 请求拦截器：携带 studentToken */
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      config.headers['studentToken'] = token;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

/** 登录失效：清登录态并弹出全局登录弹窗（Codex 模式，不跳转） */
function handleUnauthorized() {
  useAuthStore.getState().clear();
  useUiStore.getState().openLoginModal();
}

/** 响应拦截器：统一处理业务码 */
instance.interceptors.response.use(
  (response) => {
    const res = response.data as ResponseVO;
    // 非 JSON 响应（如文件下载）直接返回
    if (res === null || typeof res !== 'object' || res.code === undefined) {
      return response.data;
    }
    if (res.code === 200) {
      return res.data;
    }
    if (res.code === 401) {
      handleUnauthorized();
      return Promise.reject(new Error(res.info || '登录已失效'));
    }
    message.error(res.info || '请求失败');
    return Promise.reject(new Error(res.info || '请求失败'));
  },
  (error) => {
    if (error?.response?.status === 401) {
      handleUnauthorized();
    } else if (error?.code === 'ECONNABORTED') {
      message.error('请求超时，请稍后重试');
    } else {
      message.error('网络异常');
    }
    return Promise.reject(error);
  },
);

/** 封装 GET 请求 */
export function get<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<T> {
  return instance.get(url, { params, ...config }) as unknown as Promise<T>;
}

/** 封装 POST 请求 */
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.post(url, data, config) as unknown as Promise<T>;
}

/** 封装 PUT 请求 */
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.put(url, data, config) as unknown as Promise<T>;
}

/** 封装 DELETE 请求 */
export function del<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<T> {
  return instance.delete(url, { params, ...config }) as unknown as Promise<T>;
}

export default instance;
