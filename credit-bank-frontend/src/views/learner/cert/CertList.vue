<template>
  <div class="cert-list-container">
    <page-header title="我的成果认证" description="跟踪您提交的成果认证申请状态与历史记录">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="$router.push('/cert/apply')">
          新申请
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
        <el-option label="已撤回" value="withdrawn" />
      </el-select>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="certs" style="width: 100%">
        <el-table-column prop="outcomeName" label="成果名称" min-width="150" />
        <el-table-column prop="certifyType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.certifyType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateNo" label="证书编号" width="150" />
        <el-table-column prop="requestedCredit" label="申请学分" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <status-tag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="obtainedAt" label="获得日期" width="110">
          <template #default="{ row }">
            {{ formatDate(row.obtainedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAuditRecords(row)">审核记录</el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="danger" 
              @click="handleWithdraw(row.id)"
            >
              撤回
            </el-button>
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
          @current-change="loadCerts"
        />
      </div>
    </div>

    <!-- Audit History Drawer -->
    <el-drawer v-model="drawerVisible" :title="`认证申请审核历史 - ${currentCertName}`" size="35%">
      <div class="audit-history" v-loading="recordsLoading">
        <el-timeline v-if="auditRecords.length > 0">
          <el-timeline-item
            v-for="record in auditRecords"
            :key="record.id"
            :type="record.auditStatus === 'approved' ? 'success' : 'danger'"
            :timestamp="formatDateTime(record.createdAt)"
          >
            <h4>{{ record.auditStatus === 'approved' ? '通过' : '驳回' }} (审核人: {{ record.auditorName || '系统' }})</h4>
            <p v-if="record.recognizedCredit !== null">认定学分: <span class="highlight">{{ record.recognizedCredit }}</span></p>
            <p v-if="record.reason" class="reason">驳回理由: {{ record.reason }}</p>
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
import { getMyCerts, withdrawCert, getCertAuditRecords } from '@/api/cert'
import { formatDate, formatDateTime } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const certs = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  status: ''
})

const drawerVisible = ref(false)
const recordsLoading = ref(false)
const auditRecords = ref<any[]>([])
const currentCertName = ref('')

const formatType = (type: string) => {
  const map: Record<string, string> = {
    course_cert: '课程证书',
    diploma_cert: '毕业证书',
    degree_cert: '学位证书',
    vocational_qualification: '职业资格证书',
    skill_level: '职业技能等级证书',
    training_cert: '培训证书'
  }
  return map[type] || type
}

const loadCerts = async () => {
  loading.value = true
  try {
    const res: any = await getMyCerts(query)
    certs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadCerts()
}

const handleWithdraw = (id: number) => {
  ElMessageBox.confirm('确定要撤回该认证申请吗？撤回后可修改重新提交。', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await withdrawCert(id)
      ElMessage.success('撤回成功')
      loadCerts()
    } catch (e) {}
  }).catch(() => {})
}

const viewAuditRecords = async (row: any) => {
  currentCertName.value = row.outcomeName
  drawerVisible.value = true
  recordsLoading.value = true
  try {
    const res: any = await getCertAuditRecords(row.id)
    auditRecords.value = res || []
  } catch (e) {}
  recordsLoading.value = false
}

onMounted(() => {
  loadCerts()
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
.highlight {
  color: var(--accent);
  font-weight: 700;
}
.empty-records {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
</style>
