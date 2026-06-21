export function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.toLocaleDateString('zh-CN')} ${d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
}

export function getStatusType(status: string): string {
  const map: Record<string, string> = {
    pending: 'warning', approved: 'success', rejected: 'danger', withdrawn: 'info',
    enabled: 'success', disabled: 'danger', frozen: 'info',
    draft: 'info', reviewing: 'warning', effective: 'success', abolished: 'danger',
    available: 'success', leave: 'warning', removed: 'danger',
    unread: 'warning', read: 'info', processed: 'success',
    earn: 'success', deduct: 'danger', freeze: 'warning', unfreeze: 'info',
    support: 'success', oppose: 'danger', reserve: 'warning'
  }
  return map[status] || 'info'
}

export function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    pending: '待审核', approved: '已通过', rejected: '已驳回', withdrawn: '已撤回',
    enabled: '启用', disabled: '禁用', frozen: '冻结',
    draft: '草稿', reviewing: '审核中', effective: '已生效', abolished: '已废止',
    available: '可用', leave: '休假', removed: '已移除',
    unread: '未读', read: '已读', processed: '已处理',
    earn: '获得', deduct: '扣减', freeze: '冻结', unfreeze: '解冻',
    support: '支持', oppose: '反对', reserve: '保留'
  }
  return map[status] || status
}

export function getOutcomeTypeLabel(type: string): string {
  const map: Record<string, string> = {
    course_cert: '课程证书', diploma_cert: '学历证书', degree_cert: '学位证书',
    vocational_qualification: '职业资格', skill_level: '技能等级', training_cert: '培训证书'
  }
  return map[type] || type
}

export function getCreditChangeTypeLabel(type: string): string {
  const map: Record<string, string> = {
    earn: '获得学分', deduct: '扣减学分', freeze: '冻结学分', unfreeze: '解冻学分'
  }
  return map[type] || type
}
