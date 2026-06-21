<template>
  <div class="feedback-submit-container">
    <page-header title="提交意见反馈" description="如果您在使用学分银行系统过程中遇到任何问题或有改进意见，请随时反馈给我们" />

    <div class="form-card glass-card">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top" class="feedback-form">
        <el-form-item label="反馈类型" prop="feedbackType">
          <el-select v-model="form.feedbackType" placeholder="请选择问题分类" class="full-width">
            <el-option label="功能异常" value="bug" />
            <el-option label="功能建议" value="suggestion" />
            <el-option label="体验优化" value="experience" />
            <el-option label="其他问题" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="反馈标题" prop="title">
          <el-input v-model="form.title" placeholder="简要描述您的问题或想法（建议20字以内）" />
        </el-form-item>

        <el-form-item label="详细说明" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述问题发生的场景、复现步骤或具体的修改建议，您的反馈对我们非常重要..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="gradient-btn submit-btn" :loading="loading" @click="handleSubmit">
            提交反馈意见
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { submitFeedback } from '@/api/feedback'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  feedbackType: '',
  title: '',
  content: ''
})

const rules = {
  feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { max: 50, message: '标题不要超过 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入详细描述内容', trigger: 'blur' },
    { min: 10, message: '请至少输入 10 个字符描述详细情况', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await submitFeedback(form)
        ElMessage.success('反馈提交成功，感谢您的意见！')
        router.push('/feedback/list')
      } catch (e) {}
      loading.value = false
    }
  })
}
</script>

<style scoped>
.form-card {
  padding: 40px;
  border-radius: var(--radius-xl);
  max-width: 680px;
  margin: 0 auto;
}
.full-width {
  width: 100%;
}
.feedback-form {
  margin-top: 10px;
}
.submit-btn {
  width: 100%;
  padding: 12px 0;
  font-size: 15px;
}
</style>
