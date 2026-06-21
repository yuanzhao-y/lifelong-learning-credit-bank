<template>
  <div class="dashboard-container">
    <div class="welcome-section glass-card">
      <div class="welcome-text">
        <h1 class="gradient-text">欢迎回来，{{ user?.realName || user?.username }}</h1>
        <p>今天是 {{ currentDate }}，开始您今天的学分积攒之旅吧！</p>
      </div>
      <div class="welcome-action">
        <el-button type="primary" class="gradient-btn" @click="$router.push('/cert/apply')">
          申请成果认证
        </el-button>
      </div>
    </div>

    <!-- Stat Cards Grid -->
    <el-row :gutter="20" class="stat-grid">
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card" @click="$router.push('/credit/account')">
          <div class="stat-icon credit"><el-icon><coin /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">学分余额</span>
            <span class="stat-value">{{ creditAccount?.balance || 0 }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card" @click="$router.push('/cert/list')">
          <div class="stat-icon cert"><el-icon><medal /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">已认证成果</span>
            <span class="stat-value">{{ certsTotal }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card" @click="$router.push('/cert/list')">
          <div class="stat-icon pending"><el-icon><timer /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">待审核申请</span>
            <span class="stat-value">{{ pendingTotal }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card" @click="$router.push('/messages')">
          <div class="stat-icon message"><el-icon><bell /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">未读消息</span>
            <span class="stat-value">{{ unreadMessages }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Main Content Split -->
    <el-row :gutter="20" class="content-row">
      <!-- Recent Activity -->
      <el-col :span="16" :xs="24" :sm="24" :md="16">
        <div class="activity-card glass-card">
          <div class="card-header">
            <h3>最近成果认证记录</h3>
            <el-button link type="primary" @click="$router.push('/cert/list')">查看全部</el-button>
          </div>
          <el-table :data="recentCerts" style="width: 100%" v-loading="loading">
            <el-table-column prop="outcomeName" label="成果名称" min-width="150" />
            <el-table-column prop="certifyType" label="类型">
              <template #default="{ row }">
                <el-tag size="small">{{ formatType(row.certifyType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="requestedCredit" label="申请学分" width="100" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <status-tag :status="row.status" />
              </template>
            </el-table-column>
            <el-table-column prop="obtainedAt" label="获得日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.obtainedAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- Announcements -->
      <el-col :span="8" :xs="24" :sm="24" :md="8">
        <div class="announcements-card glass-card">
          <div class="card-header">
            <h3>系统最新公告</h3>
          </div>
          <div class="announcement-list" v-loading="annLoading">
            <div 
              v-for="item in announcements" 
              :key="item.id" 
              class="announcement-item"
              @click="showAnnouncement(item)"
            >
              <div class="ann-title">{{ item.title }}</div>
              <div class="ann-meta">
                <span>{{ formatDate(item.publishAt) }}</span>
                <span><el-icon><view-icon /></el-icon> {{ item.views || 0 }}</span>
              </div>
            </div>
            <div v-if="announcements.length === 0" class="empty-ann">
              暂无公告
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Announcement Detail Dialog -->
    <el-dialog v-model="annDialogVisible" :title="currentAnn?.title" width="50%">
      <div class="ann-dialog-content">
        <div class="ann-dialog-meta">
          <span>发布时间：{{ formatDate(currentAnn?.publishAt) }}</span>
          <span>浏览量：{{ currentAnn?.views }}</span>
        </div>
        <el-divider />
        <div class="ann-dialog-body" v-html="currentAnn?.content"></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="annDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getCreditAccount } from '@/api/credit'
import { getMyCerts } from '@/api/cert'
import { getMyMessages } from '@/api/message'
import { getPublicAnnouncements, getPublicAnnouncementDetail } from '@/api/announcement'
import { getUser } from '@/utils/auth'
import { formatDate } from '@/utils/format'
import { View as ViewIcon } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'

const user = ref(getUser())
const loading = ref(false)
const annLoading = ref(false)
const creditAccount = ref<any>(null)
const recentCerts = ref<any[]>([])
const certsTotal = ref(0)
const pendingTotal = ref(0)
const unreadMessages = ref(0)
const announcements = ref<any[]>([])

const annDialogVisible = ref(false)
const currentAnn = ref<any>(null)

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
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

const loadDashboardData = async () => {
  loading.value = true
  try {
    const creditRes = await getCreditAccount()
    creditAccount.value = creditRes
  } catch (e) {}

  try {
    const certsRes: any = await getMyCerts({ page: 1, size: 5 })
    recentCerts.value = certsRes.records || []
    
    // Load totals
    const allCertsRes: any = await getMyCerts({ page: 1, size: 1 })
    certsTotal.value = allCertsRes.total || 0

    const pendingCertsRes: any = await getMyCerts({ page: 1, size: 1, status: 'pending' })
    pendingTotal.value = pendingCertsRes.total || 0
  } catch (e) {}

  try {
    const msgRes: any = await getMyMessages({ page: 1, size: 1, readStatus: 'unread' })
    unreadMessages.value = msgRes.total || 0
  } catch (e) {}

  loading.value = false
}

const loadAnnouncements = async () => {
  annLoading.value = true
  try {
    const res: any = await getPublicAnnouncements({ page: 1, size: 5 })
    announcements.value = res.records || []
  } catch (e) {}
  annLoading.value = false
}

const showAnnouncement = async (ann: any) => {
  try {
    const detail = await getPublicAnnouncementDetail(ann.id)
    currentAnn.value = detail
    annDialogVisible.value = true
    // refresh
    loadAnnouncements()
  } catch (e) {}
}

onMounted(() => {
  loadDashboardData()
  loadAnnouncements()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
}
.welcome-text h1 {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 8px;
}
.welcome-text p {
  font-size: 14px;
  color: var(--text-secondary);
}
.stat-grid {
  margin-top: 10px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  cursor: pointer;
}
.stat-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 22px;
}
.stat-icon.credit { background: rgba(34, 211, 238, 0.1); color: var(--accent); }
.stat-icon.cert { background: rgba(16, 185, 129, 0.1); color: var(--success); }
.stat-icon.pending { background: rgba(245, 158, 11, 0.1); color: var(--warning); }
.stat-icon.message { background: rgba(239, 68, 68, 0.1); color: var(--danger); }

.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-header h3 {
  font-size: 16px;
  font-weight: 600;
}
.activity-card {
  padding: 24px;
  min-height: 380px;
}
.announcements-card {
  padding: 24px;
  min-height: 380px;
}
.announcement-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: var(--transition);
}
.announcement-item:hover .ann-title {
  color: var(--accent);
}
.announcement-item:last-child {
  border-bottom: none;
}
.ann-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ann-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
}
.empty-ann {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
.ann-dialog-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-muted);
}
.ann-dialog-body {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.8;
}
</style>
