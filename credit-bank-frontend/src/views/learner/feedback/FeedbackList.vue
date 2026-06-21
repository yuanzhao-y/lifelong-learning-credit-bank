<template>
  <div class="feedback-list-container">
    <page-header title="我的反馈记录" description="查看您历史提交的意见反馈及其处理状态、答复详情">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="$router.push('/feedback/submit')">
          新建反馈
        </el-button>
      </template>
    </page-header>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="feedbacks" style="width: 100%">
        <el-table-column prop="title" label="反馈标题" min-width="150" />
        <el-table-column prop="feedbackType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.feedbackType) }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
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

    <!-- Feedback Detail Dialog -->
    <el-dialog v-model="dialogVisible" title="反馈详情及回复" width="45%">
      <div class="feedback-detail-dialog" v-if="currentFeedback">
        <div class="section-title">反馈问题</div>
        <div class="qa-item border-bottom">
          <div class="qa-header">
            <el-tag size="small">{{ formatType(currentFeedback.feedbackType) }}</el-tag>
            <span class="time">{{ formatDateTime(currentFeedback.createdAt) }}</span>
          </div>
          <h4>{{ currentFeedback.title }}</h4>
          <p class="desc">{{ currentFeedback.content }}</p>
        </div>

        <div class="section-title reply">官方回复</div>
        <div class="qa-item reply" v-if="currentFeedback.status === 'processed'">
          <div class="qa-header">
            <span class="user">系统管理员</span>
            <span class="time">{{ formatDateTime(currentFeedback.updatedAt) }}</span>
          </div>
          <p class="desc highlight-reply">{{ currentFeedback.replyContent }}</p>
        </div>
        <div class="empty-reply" v-else>
          此意见正在积极处理中，请耐心等待回复。
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMyFeedbacks } from '@/api/feedback'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const feedbacks = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const currentFeedback = ref<any>(null)

const formatType = (type: string) => {
  const map: Record<string, string> = {
    bug: '功能异常',
    suggestion: '功能建议',
    experience: '体验优化',
    other: '其他问题'
  }
  return map[type] || type
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const res: any = await getMyFeedbacks(query)
    feedbacks.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const viewDetail = (row: any) => {
  currentFeedback.value = row
  dialogVisible.value = true
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
.feedback-detail-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
  border-bottom: 1px solid var(--border);
  padding-bottom: 6px;
}
.section-title.reply {
  color: var(--success);
}
.qa-item {
  padding: 10px 0;
}
.qa-item.border-bottom {
  border-bottom: 1px dashed var(--border);
}
.qa-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 10px;
}
.qa-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}
.qa-item .desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
}
.highlight-reply {
  background: rgba(16, 185, 129, 0.05);
  border-left: 3px solid var(--success);
  padding: 12px 16px;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--text-primary) !important;
}
.empty-reply {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
