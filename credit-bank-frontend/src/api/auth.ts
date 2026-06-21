import request from './index'
export function passwordLogin(data: { username: string; password: string }) { return request.post('/auth/login/password', data) }
export function smsLogin(data: { phone: string; code: string }) { return request.post('/auth/login/sms', data) }
export function sendSmsCode(phone: string, scene: string) { return request.post('/auth/sms-code', { phone, scene }) }
export function register(data: any) { return request.post('/auth/register', data) }
export function resetPassword(phone: string, code: string, newPassword: string) { return request.post('/auth/password/reset', { phone, code, newPassword }) }
