<template>
  <div class="log-list-container">
    <page-header title="系统操作日志" description="审计跟踪系统内核心业务及接口的高级管理员操作记录" />

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="logs" style="width: 100%">
        <el-table-column prop="operatorName" label="操作人" width="130" />
        <el-table-column prop="operationType" label="操作模块/类型" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情 / 变更说明" min-width="250" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="createdAt" label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const logs = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const formatType = (type: string) => {
  const map: Record<string, string> = {
    insert: '新增记录',
    update: '修改更新',
    delete: '删除数据',
    audit: '业务审核',
    login: '用户登录'
  }
  return map[type] || type
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res: any = await getOperationLogs(query)
    logs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.table-card {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
