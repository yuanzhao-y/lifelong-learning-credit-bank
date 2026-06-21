import request from './index'
export function getExpertList(params: any) { return request.get('/admin/experts', { params }) }
export function createExpert(data: any) { return request.post('/admin/experts', data) }
export function updateExpert(id: number, data: any) { return request.put(`/admin/experts/${id}`, data) }
export function deleteExpert(id: number) { return request.delete(`/admin/experts/${id}`) }
export function updateExpertStatus(id: number, status: string) { return request.patch(`/admin/experts/${id}/status`, null, { params: { status } }) }
