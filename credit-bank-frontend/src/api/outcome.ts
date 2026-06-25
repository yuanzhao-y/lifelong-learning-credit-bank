import request from './index'
export function getPublicOutcomes(params: any) { return request.get('/public/outcomes', { params }) }
export function getPublicOutcomeDetail(id: number) { return request.get(`/public/outcomes/${id}`) }
export function getOutcomeList(params: any) { return request.get('/outcomes', { params }) }
export function getMyLearnerOutcomes(params: any) { return request.get('/learner-outcomes/mine', { params }) }
export function createOutcome(data: any) { return request.post('/outcomes', data) }
export function updateOutcome(id: number, data: any) { return request.put(`/outcomes/${id}`, data) }
export function deleteOutcome(id: number) { return request.delete(`/outcomes/${id}`) }
export function updateOutcomeStatus(id: number, status: string) { return request.patch(`/outcomes/${id}/status`, null, { params: { status } }) }
export function importOutcomes(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/outcomes/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export function exportOutcomes() { return request.get('/outcomes/export', { responseType: 'blob' }) }
