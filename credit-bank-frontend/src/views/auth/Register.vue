<template>
  <div class="register-container">
    <div class="register-card glass-card">
      <div class="register-header">
        <h1 class="gradient-text">注册学分银行账户</h1>
        <p>终身学习成果与学分认定管理系统</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="register-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入电子邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="验证码" prop="code">
              <div class="code-input-wrapper">
                <el-input v-model="form.code" placeholder="请输入验证码" />
                <el-button 
                  class="send-btn" 
                  :disabled="cooldown > 0" 
                  @click="handleSendCode"
                >
                  {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生地" prop="birthPlace">
              <el-input v-model="form.birthPlace" placeholder="如：北京市海淀区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前住址" prop="currentAddress">
              <el-input v-model="form.currentAddress" placeholder="请输入详细居住地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="actions">
          <el-button type="primary" class="gradient-btn submit-btn" :loading="loading" @click="handleRegister">
            立即注册
          </el-button>
          <div class="login-link">
            已有账号？ <router-link to="/login">立即登录</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendSmsCode } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const cooldown = ref(0)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  code: '',
  birthPlace: '',
  currentAddress: ''
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }]
}

const startTimer = () => {
  cooldown.value = 60
  const timer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请输入手机号码')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }
  try {
    const res: any = await sendSmsCode(form.phone, 'register')
    ElMessage.success(`验证码发送成功 (开发环境验证码: ${res.devCode || '已发'})`)
    startTimer()
  } catch (err) {
    // Error is handled globally by request interceptor
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: form.username,
          password: form.password,
          realName: form.realName,
          phone: form.phone,
          email: form.email,
          idCard: form.idCard,
          code: form.code,
          birthPlace: form.birthPlace,
          currentAddress: form.currentAddress
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (err) {
        // Handled globally
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 40px 20px;
}
.register-card {
  width: 100%;
  max-width: 680px;
  padding: 40px;
  border-radius: var(--radius-xl);
}
.register-header {
  text-align: center;
  margin-bottom: 32px;
}
.register-header h1 {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 8px;
}
.register-header p {
  font-size: 14px;
  color: var(--text-secondary);
}
.code-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}
.send-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border);
  color: var(--text-primary);
}
.send-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--accent);
  color: var(--accent);
}
.submit-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: var(--radius-md) !important;
}
.actions {
  margin-top: 24px;
}
.login-link {
  text-align: center;
  margin-top: 16px;
  width: 100%;
  font-size: 14px;
  color: var(--text-secondary);
}
.login-link a {
  color: var(--accent);
  font-weight: 600;
}
</style>
