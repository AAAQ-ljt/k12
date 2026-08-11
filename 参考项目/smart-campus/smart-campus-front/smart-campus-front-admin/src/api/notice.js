import request from '@/api/request'

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const normalizeNoticeRecord = (item = {}) => ({
  ...item,
  id: item.noticeId ?? item.id,
  noticeId: item.noticeId ?? item.id,
})

export const getNoticeList = async (params = {}) => {
  const result = await request.post('/systemNotice/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    noticeTitleFuzzy: params.keyword,
    targetType: params.targetType,
    status: params.status,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeNoticeRecord(item)),
  }
}

export const getNoticeDetail = async (noticeId) => {
  const result = await request.post('/systemNotice/getSystemNoticeById', { noticeId })
  return result ? normalizeNoticeRecord(result) : null
}

export const saveNotice = async (payload = {}) => {
  const noticeId = payload.noticeId ?? payload.id
  const submitData = {
    ...payload,
    id: undefined,
    noticeId,
    targetType: Number(payload.targetType),
    isTop: Number(payload.isTop || 0),
    targetIdList: Array.isArray(payload.targetIdList)
      ? payload.targetIdList.map((item) => String(item))
      : [],
  }
  submitData.targetIds = submitData.targetIdList.join(',')
  const result = submitData.noticeId
    ? await request.post('/systemNotice/updateSystemNoticeById', submitData, { dataType: 'json' })
    : await request.post('/systemNotice/add', submitData, { dataType: 'json' })
  return result ?? true
}

export const publishNotice = (noticeId) => request.post('/systemNotice/publish', { noticeId })

export const offlineNotice = (noticeId) => request.post('/systemNotice/offline', { noticeId })

export const deleteNotice = (noticeId) => request.post('/systemNotice/deleteSystemNoticeById', { noticeId })

export const deleteNotices = (ids = []) => request.post('/systemNotice/deleteBatch', { ids: ids.join(',') })
