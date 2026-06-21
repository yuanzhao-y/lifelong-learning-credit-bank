import request from './index'
export function getStatisticsOverview(params?: { start?: string; end?: string }) {
  return request.get('/admin/statistics/overview', { params })
}
