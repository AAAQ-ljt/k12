import request from '@/api/request'

export const loadLatestNotices = (params = {}) =>
  request.post('/systemNotice/loadLatest', params, { showLoading: false })

export const getNoticeDetail = (noticeId) =>
  request.post('/systemNotice/getDetail', { noticeId }, { showLoading: false })
