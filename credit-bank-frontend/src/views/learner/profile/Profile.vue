<template>
  <div class="profile-container" v-loading="loading">
    <page-header title="个人中心" description="查看或修改您的个人基本资料，保障账号安全" />

    <el-row :gutter="20">
      <!-- Edit Profile -->
      <el-col :span="16" :xs="24">
        <div class="profile-card glass-card">
          <h3>基本资料</h3>
          <el-form :model="form" ref="formRef" :rules="rules" label-position="top" class="profile-form">
            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="用户名">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请填写您的真实姓名" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="手机号码 (已绑定)">
                  <el-input v-model="form.phoneMasked" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="身份证号 (已认定)">
                  <el-input v-model="form.idCardMasked" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="form.email" placeholder="如：example@domain.com" />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="籍贯/出生地" prop="birthPlace">
                  <el-input v-model="form.birthPlace" placeholder="如：北京市海淀区" />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="详细现住址" prop="currentAddress">
                  <el-input v-model="form.currentAddress" placeholder="如：成都市武侯区科华北路X号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button type="primary" class="gradient-btn" :loading="saving" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- Change Password -->
      <el-col :span="8" :xs="24">
        <div class="pwd-card glass-card">
          <h3>修改登录密码</h3>
          <el-form :model="pwdForm" ref="pwdFormRef" :rules="pwdRules" label-position="top">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入安全的新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" :loading="pwdSaving" @click="handleChangePassword">
                确认重置密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCurrentUser, updateProfile, changePassword } from '@/api/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)

const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()

const form = reactive({
  username: '',
  realName: '',
  phoneMasked: '',
  idCardMasked: '',
  email: '',
  birthPlace: '',
  currentAddress: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const validateConfirmPwd = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码不能短于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }]
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res: any = await getCurrentUser()
    Object.assign(form, res)
  } catch (e) {}
  loading.value = false
}

const handleSaveProfile = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await updateProfile({
          realName: form.realName,
          email: form.email,
          birthPlace: form.birthPlace,
          currentAddress: form.currentAddress
        })
        ElMessage.success('个人资料保存成功')
        loadProfile()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdSaving.value = true
      try {
        await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
        ElMessage.success('密码修改成功，请妥善保管')
        pwdFormRef.value?.resetFields()
      } catch (e) {}
      pwdSaving.value = false
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-card, .pwd-card {
  padding: 30px;
  border-radius: var(--radius-xl);
}
.profile-card h3, .pwd-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 24px;
}
.profile-form {
  margin-top: 10px;
}
</style>
