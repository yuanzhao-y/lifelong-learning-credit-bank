<template>
  <div class="conversion-list-container">
    <page-header title="学分转换记录" description="跟踪您已提交的学分转换认定申请与状态">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="$router.push('/conversion/apply')">
          申请转换
        </el-button>
      </template>
    </page-header>

    <div class="filter-bar glass-card">
      <el-select 
        v-model="query.status" 
        placeholder="全部状态" 
        clearable 
        class="filter-select"
        @change="handleSearch"
      >
        <el-option label="待审核" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="conversions" style="width: 100%">
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column prop="sourceOutcomeName" label="来源成果" min-width="150" />
        <el-table-column prop="sourceCredit" label="来源扣减学分" width="120" />
        <el-table-column prop="targetCredit" label="预计获得学分" width="120" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <status-tag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请日期" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAuditRecords(row)">审核记录</el-button>
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
          @current-change="loadConversions"
        />
      </div>
    </div>

    <!-- Audit Drawer -->
    <el-drawer v-model="drawerVisible" :title="`转换申请审核历史 - ${currentRuleName}`" size="35%">
      <div class="audit-history" v-loading="recordsLoading">
        <el-timeline v-if="auditRecords.length > 0">
          <el-timeline-item
            v-for="record in auditRecords"
            :key="record.id"
            :type="record.auditStatus === 'approved' ? 'success' : 'danger'"
            :timestamp="formatDateTime(record.createdAt)"
          >
            <h4>{{ record.auditStatus === 'approved' ? '通过' : '驳回' }} (审核人: {{ record.auditorName || '系统' }})</h4>
            <p v-if="record.reason" class="reason">原因/意见: {{ record.reason }}</p>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="empty-records">
          暂无审核操作记录
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMyConversions, getConversionAuditRecords } from '@/api/conversion'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const conversions = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  status: ''
})

const drawerVisible = ref(false)
const recordsLoading = ref(false)
const auditRecords = ref<any[]>([])
const currentRuleName = ref('')

const loadConversions = async () => {
  loading.value = true
  try {
    const res: any = await getMyConversions(query)
    conversions.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadConversions()
}

const viewAuditRecords = async (row: any) => {
  currentRuleName.value = row.ruleName
  drawerVisible.value = true
  recordsLoading.value = true
  try {
    const res: any = await getConversionAuditRecords(row.id)
    auditRecords.value = res || []
  } catch (e) {}
  recordsLoading.value = false
}

onMounted(() => {
  loadConversions()
})
</script>

<style scoped>
.filter-bar {
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-select {
  width: 180px;
}
.table-card {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.audit-history {
  padding: 20px;
}
.reason {
  color: var(--danger);
  font-style: italic;
  margin-top: 4px;
}
.empty-records {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
</style>
