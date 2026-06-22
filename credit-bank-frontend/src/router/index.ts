import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { guest: true } },
    { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue'), meta: { guest: true } },
    { path: '/reset-password', name: 'ResetPassword', component: () => import('@/views/auth/ResetPassword.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'Dashboard', component: () => import('@/views/learner/Dashboard.vue'), meta: { title: '首页' } },
        { path: 'outcomes', name: 'OutcomeList', component: () => import('@/views/learner/outcome/OutcomeList.vue'), meta: { title: '成果导览' } },
        { path: 'outcomes/:id', name: 'OutcomeDetail', component: () => import('@/views/learner/outcome/OutcomeDetail.vue'), meta: { title: '成果详情' } },
        { path: 'cert/apply', name: 'CertApply', component: () => import('@/views/learner/cert/CertApply.vue'), meta: { title: '提交认证' } },
        { path: 'cert/list', name: 'CertList', component: () => import('@/views/learner/cert/CertList.vue'), meta: { title: '我的认证' } },
        { path: 'credit/account', name: 'CreditAccount', component: () => import('@/views/learner/credit/CreditAccount.vue'), meta: { title: '学分账户' } },
        { path: 'credit/flows', name: 'CreditFlows', component: () => import('@/views/learner/credit/CreditFlows.vue'), meta: { title: '学分流水' } },
        { path: 'conversion/apply', name: 'ConversionApply', component: () => import('@/views/learner/conversion/ConversionApply.vue'), meta: { title: '申请转换' } },
        { path: 'conversion/rules', name: 'ConversionRuleBrowse', component: () => import('@/views/learner/conversion/RuleBrowse.vue'), meta: { title: '规则库' } },
        { path: 'conversion/list', name: 'ConversionList', component: () => import('@/views/learner/conversion/ConversionList.vue'), meta: { title: '我的转换' } },
        { path: 'profile', name: 'Profile', component: () => import('@/views/learner/profile/Profile.vue'), meta: { title: '个人信息' } },
        { path: 'profile/education', name: 'Education', component: () => import('@/views/learner/profile/Education.vue'), meta: { title: '教育经历' } },
        { path: 'profile/work', name: 'Work', component: () => import('@/views/learner/profile/Work.vue'), meta: { title: '工作经历' } },
        { path: 'messages', name: 'Messages', component: () => import('@/views/learner/message/MessageList.vue'), meta: { title: '消息中心' } },
        { path: 'announcements', name: 'LearnerAnnouncements', component: () => import('@/views/learner/announcement/AnnouncementList.vue'), meta: { title: '系统公告' } },
        { path: 'feedback/submit', name: 'FeedbackSubmit', component: () => import('@/views/learner/feedback/FeedbackSubmit.vue'), meta: { title: '提交反馈' } },
        { path: 'feedback/list', name: 'FeedbackList', component: () => import('@/views/learner/feedback/FeedbackList.vue'), meta: { title: '我的反馈' } }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['admin'] },
      children: [
        { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '控制台' } },
        { path: 'users', name: 'UserList', component: () => import('@/views/admin/user/UserList.vue'), meta: { title: '用户管理' } },
        { path: 'roles', name: 'RoleList', component: () => import('@/views/admin/role/RoleList.vue'), meta: { title: '角色管理' } },
        { path: 'menus', name: 'MenuList', component: () => import('@/views/admin/menu/MenuList.vue'), meta: { title: '菜单管理' } },
        { path: 'dicts', name: 'DictList', component: () => import('@/views/admin/dict/DictList.vue'), meta: { title: '字典管理' } },
        { path: 'api-permissions', name: 'ApiPermissionList', component: () => import('@/views/admin/api-permission/ApiPermissionList.vue'), meta: { title: 'API权限' } },
        { path: 'outcomes', name: 'AdminOutcomeList', component: () => import('@/views/admin/outcome/OutcomeList.vue'), meta: { title: '成果目录' } },
        { path: 'cert/audit', name: 'CertAudit', component: () => import('@/views/admin/cert/CertAudit.vue'), meta: { title: '认证审核' } },
        { path: 'conversion/rules', name: 'RuleList', component: () => import('@/views/admin/conversion/RuleList.vue'), meta: { title: '转换规则' } },
        { path: 'conversion/audit', name: 'ConversionAudit', component: () => import('@/views/admin/conversion/ConversionAudit.vue'), meta: { title: '转换审核' } },
        { path: 'experts', name: 'ExpertList', component: () => import('@/views/admin/expert/ExpertList.vue'), meta: { title: '专家管理' } },
        { path: 'messages', name: 'MessageManage', component: () => import('@/views/admin/message/MessageManage.vue'), meta: { title: '消息管理' } },
        { path: 'announcements', name: 'AnnouncementList', component: () => import('@/views/admin/announcement/AnnouncementList.vue'), meta: { title: '公告管理' } },
        { path: 'feedback', name: 'AdminFeedbackList', component: () => import('@/views/admin/feedback/FeedbackList.vue'), meta: { title: '反馈管理' } },
        { path: 'statistics', name: 'Statistics', component: () => import('@/views/admin/statistics/Overview.vue'), meta: { title: '统计分析' } },
        { path: 'logs', name: 'LogList', component: () => import('@/views/admin/log/LogList.vue'), meta: { title: '操作日志' } }
      ]
    },
    {
      path: '/auditor',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['auditor'] },
      children: [
        { path: 'cert/audit', name: 'AuditorCertAudit', component: () => import('@/views/admin/cert/CertAudit.vue'), meta: { title: '认证审核' } },
        { path: 'conversion/audit', name: 'AuditorConversionAudit', component: () => import('@/views/admin/conversion/ConversionAudit.vue'), meta: { title: '转换审核' } }
      ]
    },
    {
      path: '/expert',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ['expert'] },
      children: [
        { path: 'reviews', name: 'ExpertReviews', component: () => import('@/views/expert/Reviews.vue'), meta: { title: '评审任务' } }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
