import request from './index'
export function getCurrentUser() { return request.get('/users/me') }
export function updateProfile(data: any) { return request.put('/users/me', data) }
export function changePassword(oldPassword: string, newPassword: string) { return request.put('/users/me/password', { oldPassword, newPassword }) }
export function getCurrentMenus() { return request.get('/users/me/menus') }
export function getUserList(params: any) { return request.get('/admin/users', { params }) }
export function updateUserStatus(id: number, status: string) { return request.patch(`/admin/users/${id}/status`, { status }) }
export function assignRoles(id: number, roleIds: number[]) { return request.put(`/admin/users/${id}/roles`, { roleIds }) }
