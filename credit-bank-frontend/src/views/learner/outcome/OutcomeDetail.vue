<template>
  <div class="outcome-detail-container" v-loading="loading">
    <div class="back-nav" @click="goBack">
      <el-icon><arrow-left /></el-icon> <span>返回列表</span>
    </div>

    <div v-if="outcome" class="detail-card glass-card">
      <div class="detail-header">
        <el-tag class="type-tag">{{ formatType(outcome.outcomeType) }}</el-tag>
        <h1 class="gradient-text">{{ outcome.outcomeName }}</h1>
        <div class="outcome-code">标准代码: {{ outcome.outcomeCode }}</div>
      </div>

      <el-row :gutter="40" class="info-grid">
        <el-col :span="16" :xs="24">
          <div class="detail-section">
            <h3>成果简介</h3>
            <p>{{ outcome.intro || '暂无详细介绍' }}</p>
          </div>

          <div class="detail-section">
            <h3>认证材料要求</h3>
            <p class="pre-line">{{ outcome.certificationRequirements || '无特别材料要求' }}</p>
          </div>

          <div class="detail-section">
            <h3>适用范围</h3>
            <p>{{ outcome.applicableScope || '全国通用' }}</p>
          </div>
        </el-col>

        <el-col :span="8" :xs="24">
          <div class="credit-box glass-card">
            <span class="label">可获得标准学分</span>
            <span class="value">{{ outcome.baseCredit }}</span>
            <el-button type="primary" class="gradient-btn apply-btn" @click="applyCert">
              申请认证该成果
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicOutcomeDetail } from '@/api/outcome'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const outcome = ref<any>(null)

const formatType = (type: string) => {
  const map: Record<string, string> = {
    course_cert: '课程证书',
    diploma_cert: '毕业证书',
    degree_cert: '学位证书',
    vocational_qualification: '职业资格证书',
    skill_level: '职业技能等级证书',
    training_cert: '培训证书'
  }
  return map[type] || type
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const res = await getPublicOutcomeDetail(id)
    outcome.value = res
  } catch (e) {}
  loading.value = false
}

const applyCert = () => {
  router.push({
    path: '/cert/apply',
    query: { outcomeId: outcome.value?.id }
  })
}

const goBack = () => {
  router.push('/outcomes')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.back-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
  width: fit-content;
  transition: var(--transition);
}
.back-nav:hover {
  color: var(--accent);
}
.detail-card {
  padding: 40px;
  border-radius: var(--radius-xl);
}
.detail-header {
  margin-bottom: 32px;
}
.type-tag {
  margin-bottom: 12px;
  font-size: 12px;
}
.detail-header h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
}
.outcome-code {
  font-size: 13px;
  color: var(--text-muted);
}
.info-grid {
  margin-top: 20px;
}
.detail-section {
  margin-bottom: 28px;
}
.detail-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  border-left: 3px solid var(--accent);
  padding-left: 10px;
  margin-bottom: 12px;
}
.detail-section p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin: 0;
}
.pre-line {
  white-space: pre-line;
}
.credit-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  text-align: center;
}
.credit-box .label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.credit-box .value {
  font-size: 48px;
  font-weight: 800;
  color: var(--accent);
  margin-bottom: 24px;
}
.apply-btn {
  width: 100%;
  padding: 12px 0;
}
</style>
