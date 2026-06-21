import request from './index'
export function getPublicAnnouncements(params: any) { return request.get('/public/announcements', { params }) }
export function getPublicAnnouncementDetail(id: number) { return request.get(`/public/announcements/${id}`) }
export function getAnnouncementList(params: any) { return request.get('/admin/announcements', { params }) }
export function createAnnouncement(data: any) { return request.post('/admin/announcements', data) }
export function updateAnnouncement(id: number, data: any) { return request.put(`/admin/announcements/${id}`, data) }
export function deleteAnnouncement(id: number) { return request.delete(`/admin/announcements/${id}`) }
