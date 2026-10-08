import { post } from './request';

/**
 * 前端错误兜底页上报（2026-10-08）：把渲染崩溃的原文送到服务端日志，
 * 线上排查不用再依赖用户截图的「技术细节」。
 */
export function reportClientError(payload: {
  message: string;
  url?: string;
  stage?: string;
}): Promise<void> {
  return post('/clientError/report', {
    message: payload.message,
    url: payload.url || window.location.href,
    stage: payload.stage,
    userAgent: navigator.userAgent,
  });
}
