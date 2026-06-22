<template>
  <div class="reset-container">
    <div class="reset-card glass-card">
      <div class="reset-header">
        <h1 class="gradient-text">重置登录密码</h1>
        <p>通过手机号验证码完成身份校验后设置新密码</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="reset-form"
      >
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入注册手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="请输入短信验证码" />
            <el-button :disabled="cooldown > 0" @click="handleSendCode">
              {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>

        <el-form-item class="actions">
          <el-button type="primary" class="gradient-btn submit-btn" :loading="loading" @click="handleReset">
            确认重置
          </el-button>
          <div class="login-link">
            想起密码？ <router-link to="/login">返回登录</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { resetPassword, sendSmsCode } from '@/api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const cooldown = ref(0)

const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const startTimer = () => {
  cooldown.value = 60
  const timer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) clearInterval(timer)
  }, 1000)
}

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先输入正确手机号')
    return
  }
  const res: any = await sendSmsCode(form.phone, 'reset_password')
  ElMessage.success(res?.devCode ? `开发环境验证码: ${res.devCode}` : '验证码已发送')
  startTimer()
}

const handleReset = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await resetPassword(form.phone, form.code, form.newPassword)
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: transparent;
}

.reset-card {
  width: 100%;
  max-width: 520px;
  padding: 40px;
}

.reset-header {
  text-align: center;
  margin-bottom: 32px;
}

.reset-header h1 {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 8px;
}

.reset-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: var(--radius-md) !important;
}

.actions {
  margin-top: 20px;
}

.login-link {
  width: 100%;
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}

.login-link a {
  color: var(--accent);
  font-weight: 600;
}
</style>
