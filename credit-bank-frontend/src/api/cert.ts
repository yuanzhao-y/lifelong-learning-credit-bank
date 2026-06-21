import request from './index'
export function submitCert(data: any) { return request.post('/certifications', data) }
export function getMyCerts(params: any) { return request.get('/certifications/mine', { params }) }
export function withdrawCert(id: number) { return request.post(`/certifications/${id}/withdraw`) }
export function getCertAuditRecords(id: number) { return request.get(`/certifications/${id}/audit-records`) }
export function getPendingCerts(params: any) { return request.get('/certifications/audit/pending', { params }) }
export function approveCert(id: number, recognizedCredit?: number) { return request.post(`/certifications/audit/${id}/approve`, recognizedCredit ? { recognizedCredit } : null) }
export function rejectCert(id: number, reason: string) { return request.post(`/certifications/audit/${id}/reject`, { reason }) }
export function batchApproveCerts(ids: number[]) { return request.post('/certifications/audit/batch-approve', { ids }) }
