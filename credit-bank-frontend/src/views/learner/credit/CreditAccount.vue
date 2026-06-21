<template>
  <div class="credit-account-container" v-loading="loading">
    <page-header title="学分账户中心" description="查看您的学分余额，了解学分累计及冻结详情" />

    <!-- Hero Glass Card -->
    <div class="account-hero glass-card">
      <div class="hero-main">
        <span class="lbl">可用学分余额</span>
        <h1 class="balance-val gradient-text">{{ account?.balance || 0.0 }}</h1>
      </div>
      <div class="hero-stats">
        <div class="stats-item">
          <span class="s-label">已冻结学分</span>
          <span class="s-value warning">{{ account?.frozenCredit || 0.0 }}</span>
        </div>
        <el-divider direction="vertical" />
        <div class="stats-item">
          <span class="s-label">累计获取学分</span>
          <span class="s-value success">{{ account?.totalEarned || 0.0 }}</span>
        </div>
        <el-divider direction="vertical" />
        <div class="stats-item">
          <span class="s-label">累计扣减学分</span>
          <span class="s-value danger">{{ account?.totalDeducted || 0.0 }}</span>
        </div>
      </div>
    </div>

    <!-- Quick Actions Card -->
    <div class="actions-card glass-card">
      <h3>学分助手</h3>
      <div class="actions-grid">
        <div class="action-btn-wrapper" @click="$router.push('/credit/flows')">
          <el-icon class="act-icon"><list /></el-icon>
          <span>学分流水账单</span>
        </div>
        <div class="action-btn-wrapper" @click="$router.push('/cert/apply')">
          <el-icon class="act-icon"><medal /></el-icon>
          <span>申报新成果</span>
        </div>
        <div class="action-btn-wrapper" @click="$router.push('/conversion/apply')">
          <el-icon class="act-icon"><refresh /></el-icon>
          <span>学分转换申请</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCreditAccount } from '@/api/credit'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const account = ref<any>(null)

const loadAccount = async () => {
  loading.value = true
  try {
    const res = await getCreditAccount()
    account.value = res
  } catch (e) {}
  loading.value = false
}

onMounted(() => {
  loadAccount()
})
</script>

<style scoped>
.account-hero {
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}
.hero-main {
  text-align: center;
  margin-bottom: 32px;
}
.hero-main .lbl {
  font-size: 14px;
  color: var(--text-secondary);
}
.balance-val {
  font-size: 64px;
  font-weight: 800;
  margin-top: 8px;
}
.hero-stats {
  display: flex;
  justify-content: space-around;
  width: 100%;
  max-width: 600px;
  align-items: center;
}
.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.s-label {
  font-size: 12px;
  color: var(--text-muted);
}
.s-value {
  font-size: 20px;
  font-weight: 700;
}
.s-value.warning { color: var(--warning); }
.s-value.success { color: var(--success); }
.s-value.danger { color: var(--danger); }

.el-divider--vertical {
  height: 40px !important;
  border-color: var(--border) !important;
}

.actions-card {
  padding: 24px;
}
.actions-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}
.actions-grid {
  display: flex;
  gap: 20px;
}
.action-btn-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
  gap: 12px;
}
.action-btn-wrapper:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--accent);
}
.action-btn-wrapper:hover .act-icon {
  color: var(--accent);
}
.act-icon {
  font-size: 28px;
  color: var(--text-secondary);
  transition: var(--transition);
}
.action-btn-wrapper span {
  font-size: 14px;
  font-weight: 500;
}
</style>
