<template>
  <div class="credit-flows-container">
    <page-header title="学分收支明细" description="查看您的学分流水记录，支持按变动类型及日期过滤" />

    <div class="filter-bar glass-card">
      <el-select
        v-model="query.changeType"
        placeholder="全部变动类型"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <el-option label="学分获取" value="earn" />
        <el-option label="学分扣减" value="deduct" />
        <el-option label="学分冻结" value="freeze" />
        <el-option label="学分解冻" value="unfreeze" />
      </el-select>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        class="filter-date"
        @change="handleDateChange"
      />
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="flows" style="width: 100%">
        <el-table-column prop="changeType" label="变动类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getFlowTagType(row.changeType)" size="small">
              {{ formatChangeType(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="变动数值" width="120">
          <template #default="{ row }">
            <span :class="getAmountClass(row.amount, row.changeType)">
              {{ row.amount > 0 && row.changeType === 'earn' ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="变动后余额" width="130" />
        <el-table-column prop="remark" label="变动原因/备注" min-width="200" />
        <el-table-column prop="createdAt" label="变动时间" width="180">
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
          @current-change="loadFlows"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCreditFlows } from '@/api/credit'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const flows = ref<any[]>([])
const total = ref(0)
const dateRange = ref<any>([])

const query = reactive({
  page: 1,
  size: 10,
  changeType: '',
  start: '',
  end: ''
})

const getFlowTagType = (type: string) => {
  const map: Record<string, string> = {
    earn: 'success',
    deduct: 'danger',
    freeze: 'warning',
    unfreeze: 'info'
  }
  return map[type] || 'info'
}

const formatChangeType = (type: string) => {
  const map: Record<string, string> = {
    earn: '学分获取',
    deduct: '学分扣减',
    freeze: '学分冻结',
    unfreeze: '学分解冻'
  }
  return map[type] || type
}

const getAmountClass = (amount: number, type: string) => {
  if (type === 'earn') return 'amount-val success'
  if (type === 'deduct') return 'amount-val danger'
  if (type === 'freeze') return 'amount-val warning'
  return 'amount-val info'
}

const loadFlows = async () => {
  loading.value = true
  try {
    const res: any = await getCreditFlows(query)
    flows.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadFlows()
}

const handleDateChange = (val: any) => {
  if (val) {
    query.start = val[0]
    query.end = val[1]
  } else {
    query.start = ''
    query.end = ''
  }
  handleSearch()
}

onMounted(() => {
  loadFlows()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-select {
  width: 180px;
}
.filter-date {
  width: 320px;
}
.table-card {
  padding: 20px;
}
.amount-val {
  font-weight: 700;
  font-size: 15px;
}
.amount-val.success { color: var(--success); }
.amount-val.danger { color: var(--danger); }
.amount-val.warning { color: var(--warning); }
.amount-val.info { color: var(--accent); }

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
