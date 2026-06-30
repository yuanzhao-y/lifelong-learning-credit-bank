<template>
  <div class="login-page">
    <!-- Left Decorative Panel -->
    <div class="login-left">
      <div class="left-bg">
        <!-- Animated floating shapes -->
        <div class="float-shape shape-1"></div>
        <div class="float-shape shape-2"></div>
        <div class="float-shape shape-3"></div>
        <div class="float-shape shape-4"></div>
        <div class="float-shape shape-5"></div>
      </div>
      <div class="left-content">
        <div class="brand-section">
          <div class="brand-icon">
            <el-icon :size="48"><TrendCharts /></el-icon>
          </div>
          <h1 class="brand-title">终身学习<br/><span class="gradient-text-hero">学分银行</span></h1>
          <p class="brand-subtitle">记录每一次成长，让学习更有价值</p>
        </div>
        <div class="features">
          <div class="feature-item" v-for="(f, i) in features" :key="i" :style="{ animationDelay: `${0.3 + i * 0.15}s` }">
            <div class="feature-icon">
              <el-icon :size="22"><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-text">
              <h4>{{ f.title }}</h4>
              <p>{{ f.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Form Panel -->
    <div class="login-right">
      <div class="form-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的学分银行账户</p>
        </div>

        <!-- Login Type Tabs -->
        <div class="login-tabs">
          <div
            class="tab-item"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >密码登录</div>
          <div
            class="tab-item"
            :class="{ active: loginType === 'sms' }"
            @click="loginType = 'sms'"
          >验证码登录</div>
          <div class="tab-indicator" :style="{ transform: loginType === 'sms' ? 'translateX(100%)' : 'translateX(0)' }"></div>
        </div>

        <!-- Password Login Form -->
        <el-form
          v-if="loginType === 'password'"
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          class="login-form"
          @keyup.enter="handlePasswordLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="pwdForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="pwdForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <div class="form-extra">
            <el-checkbox v-model="rememberMe" label="记住我" />
            <a class="forgot-link" @click="router.push('/reset-password')">忘记密码？</a>
          </div>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handlePasswordLogin"
          >
            <span v-if="!loading">登 录</span>
          </el-button>
        </el-form>

        <!-- SMS Login Form -->
        <el-form
          v-else
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          class="login-form"
          @keyup.enter="handleSmsLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Phone"
              clearable
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-input-group">
              <el-input
                v-model="smsForm.code"
                placeholder="请输入验证码"
                size="large"
                :prefix-icon="Message"
                clearable
              />
              <el-button
                class="send-code-btn"
                size="large"
                :disabled="codeCooldown > 0"
                @click="handleSendCode"
              >
                {{ codeCooldown > 0 ? `${codeCooldown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleSmsLogin"
          >
            <span v-if="!loading">登 录</span>
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>

      <!-- Mobile brand footer -->
      <div class="mobile-brand">
        <span class="gradient-text-hero">学分银行</span> · 终身学习
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Phone, Message, TrendCharts, Stamp, Switch as SwitchIcon, Reading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { passwordLogin, smsLogin, sendSmsCode } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginType = ref<'password' | 'sms'>('password')
const loading = ref(false)
const rememberMe = ref(false)
const codeCooldown = ref(0)

const features = [
  { icon: shallowRef(Stamp), title: '学分认证', desc: '多渠道学习成果认证与学分积累' },
  { icon: shallowRef(SwitchIcon), title: '成果转换', desc: '跨机构学分互认与灵活转换' },
  { icon: shallowRef(Reading), title: '终身学习', desc: '构建个人终身学习电子档案' },
]

// Password form
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ username: '', password: '' })
const pwdRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// SMS form
const smsFormRef = ref<FormInstance>()
const smsForm = reactive({ phone: '', code: '' })
const smsRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

let cooldownTimer: ReturnType<typeof setInterval> | null = null

const redirectAfterLogin = () => {
  const roles = authStore.roles
  if (roles.includes('admin')) {
    router.push('/admin')
  } else if (roles.includes('auditor')) {
    router.push('/auditor/cert/audit')
  } else if (roles.includes('expert')) {
    router.push('/expert/reviews')
  } else {
    router.push('/')
  }
}

const handleSendCode = async () => {
  if (!smsForm.phone || codeCooldown.value > 0) return
  try {
    const res = await sendSmsCode(smsForm.phone, 'login')
    ElMessage.success(res?.devCode ? `开发模式验证码: ${res.devCode}` : '验证码已发送')
    codeCooldown.value = 60
    cooldownTimer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0 && cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  } catch { /* handled by interceptor */ }
}

const handlePasswordLogin = async () => {
  if (!pwdFormRef.value) return
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(() => passwordLogin(pwdForm))
    ElMessage.success('登录成功')
    redirectAfterLogin()
  } catch { /* handled */ }
  finally { loading.value = false }
}

const handleSmsLogin = async () => {
  if (!smsFormRef.value) return
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(() => smsLogin(smsForm))
    ElMessage.success('登录成功')
    redirectAfterLogin()
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

/* Left Panel */
.login-left {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: rgba(10, 14, 26, 0.4);
}

.left-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.float-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  animation: floatShape 20s ease-in-out infinite;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  bottom: -80px;
  right: -60px;
  animation-delay: -5s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #22d3ee, #06b6d4);
  top: 40%;
  left: 60%;
  animation-delay: -10s;
}

.shape-4 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  top: 20%;
  right: 20%;
  animation-delay: -7s;
  animation-duration: 25s;
}

.shape-5 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #10b981, #3b82f6);
  bottom: 20%;
  left: 20%;
  animation-delay: -15s;
  animation-duration: 30s;
}

@keyframes floatShape {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(15px, 15px) scale(1.02); }
}

.left-content {
  position: relative;
  z-index: 1;
  padding: 48px;
  max-width: 520px;
}

.brand-section {
  margin-bottom: 56px;
}

.brand-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.3);
  animation: fadeInUp 0.6s ease-out;
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  color: #f1f5f9;
  line-height: 1.2;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.gradient-text-hero {
  background: linear-gradient(135deg, #3b82f6, #22d3ee, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 17px;
  color: #94a3b8;
  line-height: 1.6;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out both;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(59, 130, 246, 0.2);
  transform: translateX(4px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(6, 182, 212, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #22d3ee;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 15px;
  font-weight: 600;
  color: #f1f5f9;
  margin-bottom: 4px;
}

.feature-text p {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Right Panel */
.login-right {
  width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(17, 24, 39, 0.4);
  border-left: 1px solid rgba(255, 255, 255, 0.06);
}

.form-card {
  width: 100%;
  max-width: 380px;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: #64748b;
}

/* Tabs */
.login-tabs {
  position: relative;
  display: flex;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: color 0.3s ease;
  border-radius: 10px;
}

.tab-item.active {
  color: #f1f5f9;
}

.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 10px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Form */
.login-form {
  animation: fadeInUp 0.4s ease-out;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  height: 48px;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(59, 130, 246, 0.3) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.login-form :deep(.el-input__inner) {
  color: #f1f5f9 !important;
  font-size: 14px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #475569 !important;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: #64748b;
}

.login-form :deep(.el-form-item__error) {
  padding-top: 4px;
}

.code-input-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input-group .el-input {
  flex: 1;
}

.send-code-btn {
  width: 120px;
  flex-shrink: 0;
  border-radius: 12px !important;
  background: rgba(59, 130, 246, 0.15) !important;
  border: 1px solid rgba(59, 130, 246, 0.3) !important;
  color: #3b82f6 !important;
  font-size: 13px !important;
  height: 48px !important;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.25) !important;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-extra :deep(.el-checkbox__label) {
  color: #94a3b8;
  font-size: 13px;
}

.forgot-link {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #60a5fa;
}

/* Login Button */
.login-btn {
  width: 100%;
  height: 48px !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #3b82f6, #06b6d4) !important;
  border: none !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 4px;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(59, 130, 246, 0.4) !important;
}

.login-btn:active {
  transform: translateY(0) scale(0.98);
}

/* Footer */
.form-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #64748b;
}

.register-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s;
}

.register-link:hover {
  color: #60a5fa;
}

.mobile-brand {
  display: none;
  margin-top: 40px;
  font-size: 13px;
  color: #475569;
}

/* Responsive */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  .login-right {
    width: 100%;
    border-left: none;
  }
  .mobile-brand {
    display: block;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 24px;
  }
  .form-card {
    max-width: 100%;
  }
  .form-header h2 {
    font-size: 24px;
  }
}
</style>
