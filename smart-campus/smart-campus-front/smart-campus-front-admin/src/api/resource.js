import request from '@/api/request'

export const RESOURCE_TYPE_OPTIONS = [
  { label: '视频', value: 1 },
  { label: '图片', value: 2 },
  { label: '文档', value: 3 },
  { label: '压缩包', value: 4 },
  { label: '其他', value: 5 },
]

export const NODE_TYPE = {
  FOLDER: 1,
  RESOURCE: 2,
}

const RESOURCE_TYPE_MAP = RESOURCE_TYPE_OPTIONS.reduce((map, item) => {
  map[item.value] = item.label
  return map
}, {})

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const normalizeTreeNode = (item = {}) => ({
  ...normalizeResourceNode(item),
  children: Array.isArray(item.children) ? item.children.map((child) => normalizeTreeNode(child)) : [],
})

export const normalizeResourceNode = (item = {}) => ({
  ...item,
  id: item.resourceId ?? item.id,
  resourceId: item.resourceId ?? item.id,
  parentId: Number(item.parentId ?? 0),
  nodeType: Number(item.nodeType ?? NODE_TYPE.RESOURCE),
  resourceType: item.resourceType == null ? undefined : Number(item.resourceType),
  resourceTypeText:
    Number(item.nodeType) === NODE_TYPE.FOLDER
      ? '目录'
      : RESOURCE_TYPE_MAP[Number(item.resourceType)] || '其他',
  status: Number(item.status ?? 1),
  fileSize: Number(item.fileSize ?? 0),
  createTime: item.createTime ?? '',
  updateTime: item.updateTime ?? '',
})

export const getResourceList = async (params = {}, options = {}) => {
  const result = await request.post('/resourceInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    parentId: params.parentId ?? 0,
    resourceNameFuzzy: params.keyword,
    resourceType: params.resourceType,
  }, options)
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeResourceNode(item)),
  }
}

export const getFolderTree = async () => {
  const result = await request.post('/resourceInfo/loadFolderTree', {}, { showLoading: false })
  return Array.isArray(result) ? result.map((item) => normalizeTreeNode(item)) : []
}

export const getResourceListByIds = async (ids = []) => {
  const idList = Array.isArray(ids)
    ? ids.map((item) => Number(item)).filter((item) => !Number.isNaN(item))
    : []
  if (!idList.length) {
    return []
  }
  const result = await request.post(
    '/resourceInfo/getResourceListByIds',
    { ids: idList.join(',') },
    { showLoading: false }
  )
  return Array.isArray(result)
    ? result.map((item) => normalizeResourceNode(item))
    : []
}

export const createFolder = async (payload = {}) => {
  const result = await request.post('/resourceInfo/addFolder', {
    parentId: payload.parentId ?? 0,
    resourceName: payload.resourceName,
  })
  return result ?? true
}

export const renameResource = async (payload = {}) => {
  const result = await request.post('/resourceInfo/rename', {
    resourceId: payload.resourceId ?? payload.id,
    resourceName: payload.resourceName,
  })
  return result ?? true
}

export const moveResource = async (payload = {}) => {
  const result = await request.post('/resourceInfo/move', {
    resourceId: payload.resourceId ?? payload.id,
    targetParentId: payload.targetParentId ?? 0,
  })
  return result ?? true
}

export const deleteResources = async (ids = []) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/resourceInfo/deleteBatch', { ids: ids.join(',') })
  return result ?? true
}

export const initChunkUpload = async (payload = {}) =>
  request.post('/resourceInfo/initUpload', payload, { dataType: 'form', showLoading: false })

export const uploadChunk = async (payload = {}, onProgress) => {
  const result = await request.post('/resourceInfo/uploadChunk', payload, {
    showLoading: false,
    dataType: 'form',
    uploadProgressCallback: onProgress,
  })
  return result ?? true
}
