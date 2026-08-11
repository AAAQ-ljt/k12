import request from '@/api/request'

export const loadMessageCenterDashboard = (params) =>
  request.post('/messageCenter/loadDashboard', params, {
    showLoading: false,
  })

export const readMessage = (messageId) =>
  request.post('/messageCenter/readMessage', { messageId }, {
    showLoading: false,
  })
