const RESOURCE_FILE_PREFIX = '/api/resourceFile/'

export const buildResourceFileUrl = (relativePath = '') => {
  const path = String(relativePath || '').trim().replace(/^\/+/, '')
  if (!path) {
    return ''
  }
  const base = import.meta.env.PROD ? import.meta.env.VITE_DOMAIN || '' : ''
  return `${base}${RESOURCE_FILE_PREFIX}${path}`
}

export const isPreviewableVideo = (resource = {}) =>
  Number(resource?.resourceType) === 1 && !!resource?.filePath

export const isPreviewableImage = (resource = {}) =>
  Number(resource?.resourceType) === 2 && !!resource?.filePath

export const canDownloadResource = (resource = {}) =>
  Number(resource?.nodeType) === 2 && !!resource?.filePath

export const downloadResourceFile = (resource = {}) => {
  const url = buildResourceFileUrl(resource?.filePath)
  if (!url) {
    return
  }
  const link = document.createElement('a')
  link.href = url
  link.download = resource?.fileName || resource?.resourceName || 'resource'
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
