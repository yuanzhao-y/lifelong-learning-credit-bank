<template>
  <div class="log-list-container">
    <page-header title="系统操作日志" description="审计跟踪系统内核心业务及接口的高级管理员操作记录" />

    <div class="filter-bar glass-card">
      <el-input v-model="query.operatorId" placeholder="操作人ID" clearable class="filter-input" @change="loadLogs" />
      <el-select v-model="query.operationType" placeholder="操作类型" clearable class="filter-select" @change="loadLogs">
        <el-option label="登录" value="login" />
        <el-option label="注册" value="register" />
        <el-option label="提交申请" value="submit" />
        <el-option label="审核通过" value="approve" />
        <el-option label="审核驳回" value="reject" />
        <el-option label="新增" value="insert" />
        <el-option label="更新" value="update" />
        <el-option label="删除" value="delete" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD"
      />
      <el-button type="primary" @click="loadLogs">筛选</el-button>
      <el-button type="info" @click="handleExport">导出日志 CSV</el-button>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="logs" style="width: 100%">
        <el-table-column prop="operatorName" label="操作人" width="130" />
        <el-table-column prop="module" label="业务模块" width="150" />
        <el-table-column prop="operationType" label="操作模块/类型" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作详情 / 变更说明" min-width="250" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="operatedAt" label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.operatedAt || row.createdAt) }}
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
import { exportOperationLogs, getOperationLogs } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const logs = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const query = reactive({
  page: 1,
  size: 10,
  operatorId: '',
  operationType: ''
})

const formatType = (type: string) => {
  const map: Record<string, string> = {
    insert: '新增记录',
    update: '修改更新',
    delete: '删除数据',
    audit: '业务审核',
    login: '用户登录',
    register: '用户注册',
    submit: '提交申请',
    approve: '审核通过',
    reject: '审核驳回',
    withdraw: '撤回申请'
  }
  return map[type] || type
}

const buildLogParams = (withPage = true) => {
  const [startDate, endDate] = dateRange.value || []
  return {
    page: withPage ? query.page : undefined,
    size: withPage ? query.size : undefined,
    operatorId: query.operatorId || undefined,
    operationType: query.operationType || undefined,
    startTime: startDate ? `${startDate}T00:00:00` : undefined,
    endTime: endDate ? `${endDate}T23:59:59` : undefined
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res: any = await getOperationLogs(buildLogParams())
    logs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleExport = async () => {
  const blob: Blob = await exportOperationLogs(buildLogParams(false))
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'operation-logs.csv'
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.table-card {
  padding: 20px;
}
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-input {
  width: 140px;
}
.filter-select {
  width: 160px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
