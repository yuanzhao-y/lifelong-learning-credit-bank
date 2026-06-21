import request from './index'
export function getEducations() { return request.get('/profile/educations') }
export function createEducation(data: any) { return request.post('/profile/educations', data) }
export function updateEducation(id: number, data: any) { return request.put(`/profile/educations/${id}`, data) }
export function deleteEducation(id: number) { return request.delete(`/profile/educations/${id}`) }
export function getWorks() { return request.get('/profile/works') }
export function createWork(data: any) { return request.post('/profile/works', data) }
export function updateWork(id: number, data: any) { return request.put(`/profile/works/${id}`, data) }
export function deleteWork(id: number) { return request.delete(`/profile/works/${id}`) }
