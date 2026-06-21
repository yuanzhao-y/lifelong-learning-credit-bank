<template>
  <div class="layout-container">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="sidebar-logo" @click="goHome">
          <div class="logo-icon">
            <el-icon :size="isCollapsed ? 24 : 28"><DataBoard /></el-icon>
          </div>
          <transition name="fade">
            <div v-if="!isCollapsed" class="logo-text">
              <span class="gradient-text">管理中心</span>
              <span class="logo-sub">学分银行</span>
            </div>
          </transition>
        </div>

        <!-- User Info -->
        <div class="user-section" v-if="!isCollapsed">
          <div class="user-avatar">
            <el-avatar :size="42" :style="{ background: 'linear-gradient(135deg, #8b5cf6, #ec4899)' }">
              {{ (authStore.user?.username || 'A').charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          <div class="user-info">
            <span class="user-name">{{ authStore.user?.username || '管理员' }}</span>
            <el-tag size="small" effect="dark" round
              :style="{ background: 'linear-gradient(135deg, #8b5cf6, #ec4899)', border: 'none', color: '#fff' }">
              {{ currentRoleLabel }}
            </el-tag>
          </div>
        </div>

        <!-- Navigation -->
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapsed"
            :collapse-transition="true"
            router
            class="sidebar-menu"
          >
            <!-- Admin menus -->
            <template v-if="authStore.hasRole('admin')">
              <el-menu-item index="/admin">
                <el-icon><DataBoard /></el-icon>
                <template #title>控制台</template>
              </el-menu-item>

              <el-menu-item index="/admin/users">
                <el-icon><User /></el-icon>
                <template #title>用户管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/roles">
                <el-icon><Lock /></el-icon>
                <template #title>角色管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/menus">
                <el-icon><Menu /></el-icon>
                <template #title>菜单管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/dicts">
                <el-icon><Collection /></el-icon>
                <template #title>字典管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/api-permissions">
                <el-icon><Key /></el-icon>
                <template #title>API权限</template>
              </el-menu-item>

              <el-menu-item index="/admin/outcomes">
                <el-icon><Files /></el-icon>
                <template #title>成果目录</template>
              </el-menu-item>

              <el-menu-item index="/admin/cert/audit">
                <el-icon><Stamp /></el-icon>
                <template #title>认证审核</template>
              </el-menu-item>

              <el-sub-menu index="admin-conversion">
                <template #title>
                  <el-icon><Switch /></el-icon>
                  <span>转换管理</span>
                </template>
                <el-menu-item index="/admin/conversion/rules">转换规则</el-menu-item>
                <el-menu-item index="/admin/conversion/audit">转换审核</el-menu-item>
              </el-sub-menu>

              <el-menu-item index="/admin/experts">
                <el-icon><Avatar /></el-icon>
                <template #title>专家管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/messages">
                <el-icon><ChatDotSquare /></el-icon>
                <template #title>消息管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/announcements">
                <el-icon><Bell /></el-icon>
                <template #title>公告管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/feedback">
                <el-icon><Comment /></el-icon>
                <template #title>反馈管理</template>
              </el-menu-item>

              <el-menu-item index="/admin/statistics">
                <el-icon><TrendCharts /></el-icon>
                <template #title>统计分析</template>
              </el-menu-item>

              <el-menu-item index="/admin/logs">
                <el-icon><Document /></el-icon>
                <template #title>操作日志</template>
              </el-menu-item>
            </template>

            <!-- Auditor menus -->
            <template v-if="authStore.hasRole('auditor') && !authStore.hasRole('admin')">
              <el-menu-item index="/auditor/cert/audit">
                <el-icon><Stamp /></el-icon>
                <template #title>认证审核</template>
              </el-menu-item>
              <el-menu-item index="/auditor/conversion/audit">
                <el-icon><Switch /></el-icon>
                <template #title>转换审核</template>
              </el-menu-item>
            </template>

            <!-- Expert menus -->
            <template v-if="authStore.hasRole('expert') && !authStore.hasRole('admin')">
              <el-menu-item index="/expert/reviews">
                <el-icon><Finished /></el-icon>
                <template #title>评审任务</template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>

        <!-- Footer -->
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

    <!-- Main -->
    <div class="main-container" :class="{ expanded: isCollapsed }">
      <header class="main-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">管理中心</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="返回学习者端" placement="bottom" v-if="authStore.hasRole('learner')">
            <div class="header-icon-btn" @click="router.push('/')">
              <el-icon :size="18"><Back /></el-icon>
            </div>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown-trigger">
              <el-avatar :size="32" :style="{ background: 'linear-gradient(135deg, #8b5cf6, #ec4899)' }">
                {{ (authStore.user?.username || 'A').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="dropdown-username">{{ authStore.user?.username }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentRoleLabel = computed(() => {
  if (authStore.hasRole('admin')) return '管理员'
  if (authStore.hasRole('auditor')) return '审核员'
  if (authStore.hasRole('expert')) return '专家'
  return '用户'
})

const breadcrumbs = computed(() => {
  return route.matched
    .filter(r => r.meta?.title)
    .map(r => ({ path: r.path, title: r.meta.title as string }))
})

const goHome = () => {
  if (authStore.hasRole('admin')) router.push('/admin')
  else if (authStore.hasRole('auditor')) router.push('/auditor/cert/audit')
  else if (authStore.hasRole('expert')) router.push('/expert/reviews')
}

const handleCommand = async (cmd: string) => {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      })
      authStore.logout()
      router.push('/login')
    } catch { /* cancelled */ }
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

.sidebar {
  width: 260px;
  min-height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed { width: 64px; }

.sidebar-inner {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: rgba(17, 24, 39, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

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
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.logo-text { display: flex; flex-direction: column; }

.logo-text .gradient-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.logo-sub { font-size: 11px; color: #64748b; letter-spacing: 2px; }

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.user-info { display: flex; flex-direction: column; gap: 4px; }
.user-name { font-size: 14px; font-weight: 600; color: #f1f5f9; }

.menu-scrollbar { flex: 1; overflow: hidden; }

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
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(236, 72, 153, 0.1)) !important;
  color: #c084fc !important;
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
  background: linear-gradient(180deg, #8b5cf6, #ec4899);
}

.sidebar-menu :deep(.el-sub-menu .el-menu) { background: transparent !important; }
.sidebar-menu :deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 52px !important;
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}

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

.main-container {
  flex: 1;
  margin-left: 260px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-container.expanded { margin-left: 64px; }

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

.header-left :deep(.el-breadcrumb__inner) { color: #94a3b8 !important; }
.header-left :deep(.el-breadcrumb__separator) { color: #475569 !important; }

.header-right { display: flex; align-items: center; gap: 8px; }

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

.user-dropdown-trigger:hover { background: rgba(255, 255, 255, 0.05); }
.dropdown-username { font-size: 14px; color: #f1f5f9; font-weight: 500; }
.dropdown-arrow { color: #64748b; font-size: 12px; }

.main-content { flex: 1; padding: 24px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from { opacity: 0; transform: translateY(12px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-8px); }

@media (max-width: 768px) {
  .sidebar { transform: translateX(-100%); z-index: 200; }
  .main-container { margin-left: 0 !important; }
}
</style>
