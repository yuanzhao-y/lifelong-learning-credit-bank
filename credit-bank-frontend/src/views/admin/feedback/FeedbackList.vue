<template>
  <div class="feedback-list-container">
    <div class="page-header glass-card">
      <h2 class="gradient-text">意见反馈处理</h2>
      <p>集中处理并答复学习者提交的问题和系统意见建议</p>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="feedbacks" style="width: 100%">
        <el-table-column prop="username" label="反馈用户" width="120" />
        <el-table-column prop="title" label="反馈标题" min-width="150" />
        <el-table-column prop="content" label="反馈详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <status-tag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showReplyDialog(row)">
              {{ row.status === 'processed' ? '查看回复' : '回复' }}
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
          @current-change="loadFeedbacks"
        />
      </div>
    </div>

    <!-- Reply Dialog -->
    <el-dialog v-model="replyVisible" title="反馈答复处理" width="35%">
      <div class="feedback-info" v-if="currentFeedback">
        <div class="info-item"><strong>提问用户：</strong>{{ currentFeedback.username }}</div>
        <div class="info-item"><strong>标题：</strong>{{ currentFeedback.title }}</div>
        <div class="info-item"><strong>描述：</strong>{{ currentFeedback.content }}</div>
        <el-divider />
        
        <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
          <el-form-item label="官方回复内容" prop="replyContent">
            <el-input 
              v-model="form.replyContent" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入回复描述信息..."
              :disabled="currentFeedback.status === 'processed'"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="replyVisible = false">关闭</el-button>
        <el-button 
          v-if="currentFeedback?.status !== 'processed'" 
          type="primary" 
          :loading="saving" 
          @click="handleReplySubmit"
        >
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getFeedbackList, replyFeedback } from '@/api/feedback'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const feedbacks = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const replyVisible = ref(false)
const currentFeedback = ref<any>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)
const form = reactive({ replyContent: '' })
const rules = {
  replyContent: [{ required: true, message: '回复内容不能为空', trigger: 'blur' }]
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const res: any = await getFeedbackList(query)
    feedbacks.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const showReplyDialog = (row: any) => {
  currentFeedback.value = row
  form.replyContent = row.replyContent || ''
  replyVisible.value = true
}

const handleReplySubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await replyFeedback(currentFeedback.value.id, form.replyContent)
        ElMessage.success('反馈意见已妥善回复')
        replyVisible.value = false
        loadFeedbacks()
      } catch (e) {}
      saving.value = false
    }
  })
}

onMounted(() => {
  loadFeedbacks()
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
.feedback-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-item {
  font-size: 14px;
  color: var(--text-secondary);
}
</style>
