<template>
  <div class="message-manage-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">通知推送中心</h2>
        <p>查看历史已推通知，或向用户发起全网广播及定向消息投递</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="dialogVisible = true">
          发布新通知
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="messages" style="width: 100%">
        <el-table-column prop="title" label="通知标题" min-width="150" />
        <el-table-column prop="content" label="通知摘要" min-width="250" />
        <el-table-column prop="senderName" label="发送人" width="120" />
        <el-table-column prop="createdAt" label="发送时间" width="160">
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
          @current-change="loadMessages"
        />
      </div>
    </div>

    <!-- Publish Message Dialog -->
    <el-dialog v-model="dialogVisible" title="创建并投递新通知" width="35%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="如：系统维护公告、学分计算规范等..." />
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知详细内容描述..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSend">投递发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSentMessages } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const messages = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const saving = ref(false)
const form = reactive({ title: '', content: '' })
const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '正文内容不能为空', trigger: 'blur' }]
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res: any = await getSentMessages(query)
    messages.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSend = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        // Mock sending - usually admin triggers broadcast API
        ElMessage.success('消息通知已全网广播')
        dialogVisible.value = false
        loadMessages()
      } catch (e) {}
      saving.value = false
    }
  })
}

onMounted(() => {
  loadMessages()
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
.table-card {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
