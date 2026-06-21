import request from './index'
export function submitFeedback(data: any) { return request.post('/feedback', data) }
export function getMyFeedbacks(params: any) { return request.get('/feedback/mine', { params }) }
export function getFeedbackList(params: any) { return request.get('/admin/feedback', { params }) }
export function replyFeedback(id: number, replyContent: string) { return request.post(`/admin/feedback/${id}/reply`, { replyContent }) }
