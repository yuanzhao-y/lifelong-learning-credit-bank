<template>
  <div class="conversion-audit-container">
    <page-header title="学分转换认定审核" description="审核认定转换结果，批准并扣减/转存学分额度" />

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="ruleName" label="所用转换规则" min-width="150" />
        <el-table-column prop="sourceOutcomeName" label="来源已认定成果" min-width="150" />
        <el-table-column prop="sourceCredit" label="来源抵扣学分" width="120" />
        <el-table-column prop="targetCredit" label="预计获得学分" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleApprove(row.id)">批准转换</el-button>
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
          @current-change="loadPending"
        />
      </div>
    </div>

    <!-- Reject dialog -->
    <el-dialog v-model="rejectVisible" title="驳回学分转换申请" width="30%">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-position="top">
        <el-form-item label="驳回意见说明" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请填写驳回具体原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleRejectSubmit">驳回申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPendingConversions, approveConversion, rejectConversion } from '@/api/conversion'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const items = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const rejectVisible = ref(false)
const submitting = ref(false)
const currentId = ref<number | null>(null)
const rejectFormRef = ref<FormInstance>()
const rejectForm = reactive({ reason: '' })
const rejectRules = { reason: [{ required: true, message: '请写明驳回意见', trigger: 'blur' }] }

const loadPending = async () => {
  loading.value = true
  try {
    const res: any = await getPendingConversions(query)
    items.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleApprove = (id: number) => {
  ElMessageBox.confirm('批准后，来源学分将被自动结转扣减，目标学分计入其可用账户中，确定吗？', '确认批准', {
    type: 'success'
  }).then(async () => {
    try {
      await approveConversion(id)
      ElMessage.success('转换申请已批准')
      loadPending()
    } catch (e) {}
  }).catch(() => {})
}

const showRejectDialog = (row: any) => {
  currentId.value = row.id
  rejectForm.reason = ''
  rejectVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await rejectConversion(currentId.value!, rejectForm.reason)
        ElMessage.success('申请已被驳回')
        rejectVisible.value = false
        loadPending()
      } catch (e) {}
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadPending()
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
