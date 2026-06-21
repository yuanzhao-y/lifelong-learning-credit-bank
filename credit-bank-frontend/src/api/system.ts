import request from './index'
// 角色
export function getRoles() { return request.get('/admin/roles') }
export function createRole(data: any) { return request.post('/admin/roles', data) }
export function updateRole(id: number, data: any) { return request.put(`/admin/roles/${id}`, data) }
export function deleteRole(id: number) { return request.delete(`/admin/roles/${id}`) }
export function assignRoleMenus(id: number, ids: number[]) { return request.put(`/admin/roles/${id}/menus`, { ids }) }
export function assignRoleApis(id: number, ids: number[]) { return request.put(`/admin/roles/${id}/apis`, { ids }) }
// 菜单
export function getMenus() { return request.get('/admin/menus') }
export function createMenu(data: any) { return request.post('/admin/menus', data) }
export function updateMenu(id: number, data: any) { return request.put(`/admin/menus/${id}`, data) }
export function deleteMenu(id: number) { return request.delete(`/admin/menus/${id}`) }
// API权限
export function getApiPermissions() { return request.get('/admin/api-permissions') }
export function createApiPermission(data: any) { return request.post('/admin/api-permissions', data) }
export function updateApiPermission(id: number, data: any) { return request.put(`/admin/api-permissions/${id}`, data) }
export function deleteApiPermission(id: number) { return request.delete(`/admin/api-permissions/${id}`) }
// 字典
export function getDicts() { return request.get('/admin/dicts') }
export function createDict(data: any) { return request.post('/admin/dicts', data) }
export function updateDict(id: number, data: any) { return request.put(`/admin/dicts/${id}`, data) }
export function deleteDict(id: number) { return request.delete(`/admin/dicts/${id}`) }
export function getDictItems(dictId: number) { return request.get(`/admin/dicts/${dictId}/items`) }
export function createDictItem(dictId: number, data: any) { return request.post(`/admin/dicts/${dictId}/items`, data) }
export function updateDictItem(id: number, data: any) { return request.put(`/admin/dict-items/${id}`, data) }
export function deleteDictItem(id: number) { return request.delete(`/admin/dict-items/${id}`) }
// 字典缓存
export function getDictItemsByCode(dictCode: string) { return request.get(`/dicts/${dictCode}/items`) }
export function evictDictCache(dictCode: string) { return request.delete(`/dicts/${dictCode}/cache`) }
// 操作日志
export function getOperationLogs(params: any) { return request.get('/admin/operation-logs', { params }) }
