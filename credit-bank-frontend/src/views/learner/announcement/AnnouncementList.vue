<template>
  <div class="announcement-list-container">
    <page-header title="系统公告" description="查看平台政策指南、系统通知与学习成果认证动态" />

    <div class="filter-bar glass-card">
      <el-input
        v-model="query.keyword"
        placeholder="搜索公告标题"
        clearable
        @input="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="announcement-grid" v-loading="loading">
      <div
        v-for="item in announcements"
        :key="item.id"
        class="announcement-card glass-card"
        @click="showDetail(item.id)"
      >
        <div class="announcement-title">{{ item.title }}</div>
        <div class="announcement-meta">
          <span>发布时间：{{ formatDate(item.publishAt || item.createdAt) }}</span>
          <span>浏览量：{{ item.viewCount || 0 }}</span>
        </div>
        <p>{{ plainText(item.content) }}</p>
        <el-button link type="primary">查看详情</el-button>
      </div>

      <div v-if="announcements.length === 0 && !loading" class="empty-state glass-card">
        <el-empty description="暂无公告" />
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadAnnouncements"
      />
    </div>

    <el-dialog v-model="detailVisible" title="公告详情" width="680px">
      <div v-if="currentAnnouncement" class="detail-body">
        <h2>{{ currentAnnouncement.title }}</h2>
        <div class="announcement-meta">
          <span>发布时间：{{ formatDate(currentAnnouncement.publishAt || currentAnnouncement.createdAt) }}</span>
          <span>浏览量：{{ currentAnnouncement.viewCount || 0 }}</span>
        </div>
        <div class="content" v-html="currentAnnouncement.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getPublicAnnouncementDetail, getPublicAnnouncements } from '@/api/announcement'
import { formatDate } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const announcements = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentAnnouncement = ref<any>(null)

const query = reactive({
  page: 1,
  size: 8,
  keyword: ''
})

const plainText = (content: string) => (content || '').replace(/<[^>]+>/g, '').slice(0, 120) || '暂无摘要'

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res: any = await getPublicAnnouncements(query)
    announcements.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadAnnouncements()
}

const showDetail = async (id: number) => {
  currentAnnouncement.value = await getPublicAnnouncementDetail(id)
  detailVisible.value = true
}

onMounted(loadAnnouncements)
</script>

<style scoped>
.filter-bar {
  padding: 16px 20px;
  margin-bottom: 24px;
}

.filter-bar .el-input {
  max-width: 360px;
}

.announcement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.announcement-card {
  padding: 22px;
  cursor: pointer;
}

.announcement-title {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 10px;
}

.announcement-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: var(--text-muted);
  font-size: 12px;
  margin-bottom: 12px;
}

.announcement-card p {
  color: var(--text-secondary);
  line-height: 1.7;
  min-height: 48px;
}

.empty-state {
  padding: 40px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-body h2 {
  margin: 0 0 12px;
  color: var(--text-primary);
}

.content {
  color: var(--text-secondary);
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
