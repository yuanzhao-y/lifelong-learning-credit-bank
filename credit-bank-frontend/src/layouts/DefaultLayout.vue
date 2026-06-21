<template>
  <div class="layout-container">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="sidebar-logo" @click="router.push('/')">
          <div class="logo-icon">
            <el-icon :size="isCollapsed ? 24 : 28"><TrendCharts /></el-icon>
          </div>
          <transition name="fade">
            <div v-if="!isCollapsed" class="logo-text">
              <span class="gradient-text">学分银行</span>
              <span class="logo-sub">终身学习</span>
            </div>
          </transition>
        </div>

        <!-- User Info -->
        <div class="user-section" v-if="!isCollapsed">
          <div class="user-avatar">
            <el-avatar :size="42" :style="{ background: 'linear-gradient(135deg, #3b82f6, #06b6d4)' }">
              {{ (authStore.user?.username || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          <div class="user-info">
            <span class="user-name">{{ authStore.user?.username || '用户' }}</span>
            <span class="user-role">学习者</span>
          </div>
        </div>

        <!-- Navigation Menu -->
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapsed"
            :collapse-transition="true"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页</template>
            </el-menu-item>

            <el-menu-item index="/outcomes">
              <el-icon><Reading /></el-icon>
              <template #title>成果导览</template>
            </el-menu-item>

            <el-sub-menu index="cert">
              <template #title>
                <el-icon><Stamp /></el-icon>
                <span>认证管理</span>
              </template>
              <el-menu-item index="/cert/apply">提交认证</el-menu-item>
              <el-menu-item index="/cert/list">我的认证</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="credit">
              <template #title>
                <el-icon><Coin /></el-icon>
                <span>学分中心</span>
              </template>
              <el-menu-item index="/credit/account">学分账户</el-menu-item>
              <el-menu-item index="/credit/flows">学分流水</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="conversion">
              <template #title>
                <el-icon><Switch /></el-icon>
                <span>成果转换</span>
              </template>
              <el-menu-item index="/conversion/apply">申请转换</el-menu-item>
              <el-menu-item index="/conversion/list">我的转换</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="profile">
              <template #title>
                <el-icon><User /></el-icon>
                <span>个人档案</span>
              </template>
              <el-menu-item index="/profile">基本信息</el-menu-item>
              <el-menu-item index="/profile/education">教育经历</el-menu-item>
              <el-menu-item index="/profile/work">工作经历</el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/messages">
              <el-icon><ChatDotSquare /></el-icon>
              <template #title>
                <span>消息中心</span>
                <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="menu-badge" />
              </template>
            </el-menu-item>

            <el-sub-menu index="feedback">
              <template #title>
                <el-icon><Comment /></el-icon>
                <span>意见反馈</span>
              </template>
              <el-menu-item index="/feedback/submit">提交反馈</el-menu-item>
              <el-menu-item index="/feedback/list">我的反馈</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>

        <!-- Collapse Button -->
        <div class="sidebar-footer">
          <div class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon :size="18">
              <component :is="isCollapsed ? 'Expand' : 'Fold'" />
            </el-icon>
            <span v-if="!isCollapsed" class="collapse-text">收起菜单</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="main-container" :class="{ expanded: isCollapsed }">
      <!-- Header -->
      <header class="main-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="系统公告" placement="bottom">
            <div class="header-icon-btn" @click="router.push('/messages')">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </div>
          </el-tooltip>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-dropdown-trigger">
              <el-avatar :size="32" :style="{ background: 'linear-gradient(135deg, #3b82f6, #06b6d4)' }">
                {{ (authStore.user?.username || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="dropdown-username">{{ authStore.user?.username || '用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({ path: r.path, title: r.meta.title as string }))
})

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        authStore.logout()
        router.push('/login')
      } catch { /* cancelled */ }
      break
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

/* Sidebar */
.sidebar {
  width: 260px;
  min-height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-inner {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: rgba(17, 24, 39, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-text .gradient-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #22d3ee);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.logo-sub {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 2px;
}

/* User Section */
.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #f1f5f9;
}

.user-role {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

/* Menu */
.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  background: transparent !important;
  padding: 8px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  margin-bottom: 2px;
  color: #94a3b8 !important;
  transition: all 0.25s ease;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.05) !important;
  color: #f1f5f9 !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(6, 182, 212, 0.1)) !important;
  color: #22d3ee !important;
  position: relative;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #3b82f6, #06b6d4);
}

.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 52px !important;
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}

.menu-badge {
  margin-left: 8px;
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.25s ease;
  font-size: 13px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #94a3b8;
}

/* Main Container */
.main-container {
  flex: 1;
  margin-left: 260px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-container.expanded {
  margin-left: 64px;
}

/* Header */
.main-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(17, 24, 39, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: #94a3b8 !important;
  font-weight: 400;
}

.header-left :deep(.el-breadcrumb__separator) {
  color: #475569 !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.25s ease;
}

.header-icon-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #f1f5f9;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.user-dropdown-trigger:hover {
  background: rgba(255, 255, 255, 0.05);
}

.dropdown-username {
  font-size: 14px;
  color: #f1f5f9;
  font-weight: 500;
}

.dropdown-arrow {
  color: #64748b;
  font-size: 12px;
}

/* Content */
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    z-index: 200;
  }
  .sidebar.collapsed {
    width: 260px;
    transform: translateX(0);
  }
  .main-container {
    margin-left: 0 !important;
  }
}
</style>
