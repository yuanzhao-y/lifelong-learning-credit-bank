<template>
  <div class="cert-audit-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">成果认证审核</h2>
        <p>核算审核学习者提交的证明材料并进行额度确认</p>
      </div>
      <div class="header-actions">
        <el-button type="success" :disabled="selectedIds.length === 0" @click="handleBatchApprove">
          批量通过 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-bar glass-card">
      <el-select v-model="query.certifyType" placeholder="成果类型" clearable class="filter-select" @change="handleSearch">
        <el-option label="课程证书" value="course_cert" />
        <el-option label="毕业证书" value="diploma_cert" />
        <el-option label="学位证书" value="degree_cert" />
        <el-option label="职业资格" value="vocational_qualification" />
        <el-option label="技能等级" value="skill_level" />
        <el-option label="培训证书" value="training_cert" />
      </el-select>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="certs" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="outcomeName" label="成果名称" min-width="150" />
        <el-table-column prop="certifyType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.certifyType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateNo" label="证书编号" width="150" />
        <el-table-column prop="requestedCredit" label="申请学分" width="95" />
        <el-table-column prop="obtainedAt" label="获得日期" width="110">
          <template #default="{ row }">
            {{ formatDate(row.obtainedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="showApproveDialog(row)">通过</el-button>
            <el-button link type="danger" @click="showRejectDialog(row)">驳回</el-button>
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

    <!-- Approve single dialog -->
    <el-dialog v-model="approveVisible" title="确认通过认定申请" width="30%">
      <el-form :model="approveForm" label-position="top">
        <el-form-item label="认定核算学分">
          <el-input-number v-model="approveForm.recognizedCredit" :min="0.1" :precision="1" :step="0.5" />
          <div class="hint">申请学分：{{ currentAuditRow?.requestedCredit }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="success" :loading="auditSaving" @click="handleApproveSubmit">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- Reject single dialog -->
    <el-dialog v-model="rejectVisible" title="驳回认定申请" width="30%">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-position="top">
        <el-form-item label="驳回意见说明" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请阐述驳回理由，如材料模糊或编号错误等..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="auditSaving" @click="handleRejectSubmit">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPendingCerts, approveCert, rejectCert, batchApproveCerts } from '@/api/cert'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const certs = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

const query = reactive({
  page: 1,
  size: 10,
  certifyType: ''
})

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
    const res: any = await getPendingCerts(query)
    certs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadCerts()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// Single Approve
const approveVisible = ref(false)
const auditSaving = ref(false)
const currentAuditRow = ref<any>(null)
const approveForm = reactive({ recognizedCredit: 1.0 })

const showApproveDialog = (row: any) => {
  currentAuditRow.value = row
  approveForm.recognizedCredit = row.requestedCredit
  approveVisible.value = true
}

const handleApproveSubmit = async () => {
  auditSaving.value = true
  try {
    await approveCert(currentAuditRow.value.id, approveForm.recognizedCredit)
    ElMessage.success('审核已通过')
    approveVisible.value = false
    loadCerts()
  } catch (e) {}
  auditSaving.value = false
}

// Single Reject
const rejectVisible = ref(false)
const rejectFormRef = ref<FormInstance>()
const rejectForm = reactive({ reason: '' })
const rejectRules = { reason: [{ required: true, message: '请写明驳回意见', trigger: 'blur' }] }

const showRejectDialog = (row: any) => {
  currentAuditRow.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      auditSaving.value = true
      try {
        await rejectCert(currentAuditRow.value.id, rejectForm.reason)
        ElMessage.success('申请已被驳回')
        rejectVisible.value = false
        loadCerts()
      } catch (e) {}
      auditSaving.value = false
    }
  })
}

// Batch Approve
const handleBatchApprove = () => {
  ElMessageBox.confirm(`确定要批量审核通过选中的 ${selectedIds.value.length} 条申请吗？`, '批量操作', {
    type: 'success'
  }).then(async () => {
    try {
      await batchApproveCerts(selectedIds.value)
      ElMessage.success('批量审核成功')
      loadCerts()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadCerts()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 24px;
}
.filter-bar {
  display: flex;
  gap: 16px;
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
.hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
