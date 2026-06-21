import request from './index'
export function getCreditAccount() { return request.get('/credits/account') }
export function getCreditFlows(params: any) { return request.get('/credits/flows', { params }) }
