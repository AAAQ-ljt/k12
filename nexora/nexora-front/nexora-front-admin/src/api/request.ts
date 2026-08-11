import axios from 'axios';
import { message } from 'antd';
import type { AxiosRequestConfig, AxiosError } from 'axios';
import type { ResponseVO } from '@/types/common';
import { getToken, removeToken } from '@/utils/token';

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

/** 请求拦截器：注入 adminToken */
instance.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers['adminToken'] = token;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

/** 处理登录失效 */
function handleUnauthorized() {
  removeToken();
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(
      window.location.pathname + window.location.search,
    );
    window.location.href = `/login?redirect=${redirect}`;
  }
}

/** 响应拦截器：统一处理业务码 */
instance.interceptors.response.use(
  (response) => {
    const res = response.data as ResponseVO;
    // 非标准 ResponseVO 结构（如文件下载 blob），直接返回原始数据
    if (!res || typeof res !== 'object' || !('code' in res)) {
      return response.data;
    }
    // 成功
    if (res.code === 200) {
      return res.data;
    }
    // 登录失效
    if (res.code === 401) {
      handleUnauthorized();
      return Promise.reject(new Error(res.info || '登录已失效'));
    }
    // 其他业务错误
    message.error(res.info || '操作失败');
    return Promise.reject(new Error(res.info || '操作失败'));
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    if (status === 401) {
      handleUnauthorized();
    } else if (error.code === 'ECONNABORTED') {
      message.error('请求超时，请稍后重试');
    } else if (error.message === 'Network Error') {
      message.error('网络异常，请稍后重试');
    } else {
      message.error('网络异常，请稍后重试');
    }
    return Promise.reject(error);
  },
);

/** 类型安全的请求封装，拦截器已剥离出业务数据，此处直接返回 data */
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config) as unknown as Promise<T>;
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config) as unknown as Promise<T>;
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config) as unknown as Promise<T>;
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config) as unknown as Promise<T>;
  },
};

export default request;
