<template>
  <div class="expert-reviews-container">
    <page-header title="专家评审面板" description="由系统指派给您的转换规则论证、专业评审工作任务" />

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="reviews" style="width: 100%">
        <el-table-column label="规则编码" width="150">
          <template #default="{ row }">{{ row.ruleCode || `规则 #${row.ruleId}` }}</template>
        </el-table-column>
        <el-table-column label="规则名称" min-width="180">
          <template #default="{ row }">{{ row.ruleName || '待评审转换规则' }}</template>
        </el-table-column>
        <el-table-column label="来源成果" min-width="150">
          <template #default="{ row }">{{ row.sourceCatalogName || '见规则详情' }}</template>
        </el-table-column>
        <el-table-column label="目标成果" min-width="150">
          <template #default="{ row }">{{ row.targetCatalogName || '见规则详情' }}</template>
        </el-table-column>
        <el-table-column label="建议兑换率" width="110">
          <template #default="{ row }">{{ row.conversionRatio || '待核定' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showReviewDialog(row)">评审认定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Review Submit Dialog -->
    <el-dialog v-model="dialogVisible" title="提交专家学术/专业评审意见" width="35%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="评审结论意见" prop="opinion">
          <el-radio-group v-model="form.opinion">
            <el-radio label="support">支持 (同意此比例)</el-radio>
            <el-radio label="oppose">反对 (比例不妥)</el-radio>
            <el-radio label="reserve">保留 (意见有保留)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="论证说明 / 改进建议" prop="reviewComment">
          <el-input 
            v-model="form.reviewComment" 
            type="textarea" 
            :rows="5" 
            placeholder="请详细阐述您的评审专业意见说明，供委员会参考核定..." 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleReviewSubmit">确认提交结论</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMyReviews, submitReview } from '@/api/conversion'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const reviews = ref<any[]>([])

const dialogVisible = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  opinion: 'support',
  reviewComment: ''
})

const rules = {
  opinion: [{ required: true, message: '请选择评审意见', trigger: 'change' }],
  reviewComment: [{ required: true, message: '论证意见说明不能为空', trigger: 'blur' }]
}

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await getMyReviews()
    reviews.value = res || []
  } catch (e) {}
  loading.value = false
}

const showReviewDialog = (row: any) => {
  currentId.value = row.id
  form.opinion = 'support'
  form.reviewComment = ''
  dialogVisible.value = true
}

const handleReviewSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await submitReview(currentId.value!, form.opinion, form.reviewComment)
        ElMessage.success('感谢您的评审，结论已妥善归档！')
        dialogVisible.value = false
        loadReviews()
      } catch (e) {}
      saving.value = false
    }
  })
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.table-card {
  padding: 20px;
}
</style>
