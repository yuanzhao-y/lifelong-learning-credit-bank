<template>
  <div class="message-list-container">
    <page-header title="消息中心" description="接收系统的通知、认证审核结论及学分变动通知">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="handleReadAll" v-if="hasUnread">
          全部标记为已读
        </el-button>
      </template>
    </page-header>

    <div class="filter-bar glass-card">
      <el-select 
        v-model="query.readStatus" 
        placeholder="阅读状态" 
        clearable 
        class="filter-select"
        @change="handleSearch"
      >
        <el-option label="未读" value="unread" />
        <el-option label="已读" value="read" />
      </el-select>
    </div>

    <!-- Message List Grid -->
    <div class="messages-wrapper" v-loading="loading">
      <div 
        v-for="item in messages" 
        :key="item.id" 
        class="message-item-card glass-card"
        :class="{ unread: item.readStatus === 'unread' }"
        @click="viewMessageDetail(item)"
      >
        <div class="msg-dot" v-if="item.readStatus === 'unread'"></div>
        <div class="msg-main">
          <div class="msg-header">
            <h4 class="msg-title">{{ item.messageTitle || item.title || `消息 #${item.messageId}` }}</h4>
            <span class="msg-time">{{ formatDateTime(item.sentAt || item.createdAt) }}</span>
          </div>
          <p class="msg-summary">{{ item.messageContent || item.content || `关联消息ID：${item.messageId}，点击查看完整正文` }}</p>
        </div>
      </div>

      <div v-if="messages.length === 0 && !loading" class="empty-holder glass-card">
        <el-empty description="暂无站内信消息" />
      </div>

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

    <!-- Message Detail Drawer -->
    <el-drawer v-model="drawerVisible" :title="currentMsgTitle" size="35%">
      <div class="message-drawer-content" v-if="currentMsg">
        <div class="msg-meta">
          <span>时间：{{ formatDateTime(currentMsg.sentAt || currentMsg.createdAt) }}</span>
        </div>
        <el-divider />
        <div class="msg-body">
          {{ currentMsg.messageContent || currentMsg.content }}
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getMyMessages, getMessageDetail, markAllRead } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const messages = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  readStatus: ''
})

const drawerVisible = ref(false)
const currentMsg = ref<any>(null)

const currentMsgTitle = computed(() => currentMsg.value?.messageTitle || currentMsg.value?.title || '消息详情')

const hasUnread = computed(() => {
  return messages.value.some(item => item.readStatus === 'unread')
})

const loadMessages = async () => {
  loading.value = true
  try {
    const res: any = await getMyMessages(query)
    messages.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadMessages()
}

const viewMessageDetail = async (msg: any) => {
  try {
    // get detail and mark read automatically
    const detail = await getMessageDetail(msg.messageId || msg.id)
    currentMsg.value = detail
    drawerVisible.value = true
    
    // update locally
    const idx = messages.value.findIndex(item => item.id === msg.id)
    if (idx !== -1) {
      messages.value[idx].readStatus = 'read'
    }
  } catch (e) {}
}

const handleReadAll = async () => {
  try {
    await markAllRead()
    ElMessage.success('已全部标记为已读')
    loadMessages()
  } catch (e) {}
}

onMounted(() => {
  loadMessages()
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
.messages-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.message-item-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  cursor: pointer;
  position: relative;
  transition: var(--transition);
}
.message-item-card:hover {
  border-color: var(--accent);
}
.message-item-card.unread {
  background: rgba(34, 211, 238, 0.02);
}
.msg-dot {
  position: absolute;
  left: 12px;
  top: 26px;
  width: 8px;
  height: 8px;
  background: var(--accent);
  border-radius: var(--radius-pill);
}
.msg-main {
  width: 100%;
  padding-left: 12px;
}
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.msg-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.msg-time {
  font-size: 12px;
  color: var(--text-muted);
}
.msg-summary {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.empty-holder {
  padding: 40px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.message-drawer-content {
  padding: 20px;
}
.msg-meta {
  font-size: 12px;
  color: var(--text-muted);
}
.msg-body {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
