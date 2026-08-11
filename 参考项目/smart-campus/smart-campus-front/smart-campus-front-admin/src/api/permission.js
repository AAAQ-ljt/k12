import request from '@/api/request'

export const loadRoleList = () => request.post('/permission/loadRoleList')

export const loadMenuTree = () => request.post('/permission/loadMenuTree')

export const getRolePermission = (roleType) => request.post('/permission/getRolePermission', { roleType })

export const saveRolePermission = (payload) => request.post('/permission/saveRolePermission', payload, { dataType: 'json' })

export const getCurrentMenuList = () => request.post('/permission/getCurrentMenuList')
