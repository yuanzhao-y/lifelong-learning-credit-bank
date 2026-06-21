import request from './index'
export function getMyMessages(params: any) { return request.get('/messages', { params }) }
export function getMessageDetail(id: number) { return request.get(`/messages/${id}`) }
export function markMessageRead(id: number) { return request.post(`/messages/${id}/read`) }
export function markAllRead() { return request.post('/messages/read-all') }
export function getSentMessages(params: any) { return request.get('/admin/messages', { params }) }
