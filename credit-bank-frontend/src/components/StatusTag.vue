<template>
  <el-tag :type="tagType" :effect="effect || 'light'" class="status-tag">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
  effect?: 'dark' | 'light' | 'plain'
}>()

const statusMap: Record<string, { type: string; label: string }> = {
  // Application / Review statuses
  pending: { type: 'warning', label: '待审核' },
  approved: { type: 'success', label: '已通过' },
  rejected: { type: 'danger', label: '已驳回' },
  withdrawn: { type: 'info', label: '已撤回' },
  
  // User statuses
  enabled: { type: 'success', label: '启用' },
  disabled: { type: 'danger', label: '停用' },
  frozen: { type: 'warning', label: '冻结' },

  // Rule statuses
  draft: { type: 'info', label: '草稿' },
  reviewing: { type: 'warning', label: '审核中' },
  effective: { type: 'success', label: '生效' },
  abolished: { type: 'danger', label: '废止' },
  
  // Expert statuses
  available: { type: 'success', label: '可用' },
  leave: { type: 'warning', label: '请假' },
  removed: { type: 'danger', label: '已移除' },
  
  // Review Opinions
  support: { type: 'success', label: '支持' },
  oppose: { type: 'danger', label: '反对' },
  reserve: { type: 'warning', label: '保留' }
}

const tagType = computed(() => {
  return statusMap[props.status.toLowerCase()]?.type || 'info'
})

const label = computed(() => {
  return statusMap[props.status.toLowerCase()]?.label || props.status
})
</script>

<style scoped>
.status-tag {
  border-radius: var(--radius-sm);
  font-weight: 500;
}
</style>
