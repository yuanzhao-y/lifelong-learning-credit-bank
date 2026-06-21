import request from './index'
export function getPublicConversionRules(params: any) { return request.get('/public/conversion-rules', { params }) }
export function getPublicRuleDetail(id: number) { return request.get(`/public/conversion-rules/${id}`) }
export function getConversionRules(params: any) { return request.get('/conversion-rules', { params }) }
export function createConversionRule(data: any) { return request.post('/conversion-rules', data) }
export function updateConversionRule(id: number, data: any) { return request.put(`/conversion-rules/${id}`, data) }
export function deleteConversionRule(id: number) { return request.delete(`/conversion-rules/${id}`) }
export function submitRuleReview(id: number) { return request.post(`/conversion-rules/${id}/submit-review`) }
export function assignExpert(id: number, expertId: number) { return request.post(`/conversion-rules/${id}/assign-expert`, { expertId }) }
export function matchConversionRules(sourceOutcomeId: number) { return request.get('/conversions/match-rules', { params: { sourceOutcomeId } }) }
export function previewConversion(ruleId: number, sourceOutcomeId: number, sourceCredit: number) { return request.get('/conversions/preview', { params: { ruleId, sourceOutcomeId, sourceCredit } }) }
export function submitConversion(data: any) { return request.post('/conversions', data) }
export function getMyConversions(params: any) { return request.get('/conversions/mine', { params }) }
export function getConversionAuditRecords(id: number) { return request.get(`/conversions/${id}/audit-records`) }
export function getPendingConversions(params: any) { return request.get('/conversions/audit/pending', { params }) }
export function approveConversion(id: number) { return request.post(`/conversions/audit/${id}/approve`) }
export function rejectConversion(id: number, reason: string) { return request.post(`/conversions/audit/${id}/reject`, { reason }) }
export function getCurrentExpert() { return request.get('/experts/me') }
export function getMyReviews() { return request.get('/experts/reviews') }
export function submitReview(id: number, opinion: string, reviewComment?: string) { return request.post(`/experts/reviews/${id}`, { opinion, reviewComment }) }
