import request from './index'
export function uploadFile(file: File, bizType = 'common', bizId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  if (bizId) formData.append('bizId', String(bizId))
  return request.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
