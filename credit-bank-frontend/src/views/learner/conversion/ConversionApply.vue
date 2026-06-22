<template>
  <div class="conversion-apply-container">
    <page-header title="学分转换申请" description="根据规则将您已认定的课程或学位成果转换为其它成果对应的兑换学分" />

    <div class="apply-card glass-card">
      <el-steps :active="activeStep" finish-status="success" align-center class="custom-steps">
        <el-step title="选择来源成果" />
        <el-step title="匹配并选择规则" />
        <el-step title="预览并提交" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: Select Source Certification -->
        <div v-if="activeStep === 0" class="step-pane">
          <div class="pane-title">选择要进行学分转换的已认证成果</div>
          <el-select
            v-model="selectedCertId"
            placeholder="请选择已认定的成果记录"
            class="full-width"
            @change="handleCertSelect"
            v-loading="certsLoading"
          >
            <el-option
              v-for="item in myApprovedCerts"
              :key="item.id"
              :label="`${item.outcomeName} (证书编号: ${item.certificateNo || '无'} | 可用学分: ${item.availableCredit})`"
              :value="item.id"
            />
          </el-select>
        </div>

        <!-- Step 2: Match Rules -->
        <div v-if="activeStep === 1" class="step-pane">
          <div class="pane-title">选择适合此成果的转换规则</div>
          <el-radio-group v-model="selectedRuleId" class="rules-group" v-loading="rulesLoading">
            <div 
              v-for="rule in matchedRules" 
              :key="rule.id" 
              class="rule-item-card glass-card"
              :class="{ active: selectedRuleId === rule.id }"
              @click="selectedRuleId = rule.id"
            >
              <el-radio :label="rule.id">
                <div class="rule-title">{{ rule.ruleName }}</div>
                <div class="rule-ratio">转换比例：<span class="highlight">{{ rule.conversionRatio }}</span></div>
                <div class="rule-desc" v-if="rule.description">{{ rule.description }}</div>
              </el-radio>
            </div>
            <div v-if="matchedRules.length === 0" class="empty-rules">
              该成果暂无适用的已生效转换规则
            </div>
          </el-radio-group>
        </div>

        <!-- Step 3: Preview -->
        <div v-if="activeStep === 2" class="step-pane" v-loading="previewLoading">
          <div class="pane-title">确认您的转换详情</div>
          
          <div class="preview-box glass-card" v-if="previewData">
            <div class="preview-item">
              <span class="lbl">来源成果：</span>
              <span class="val">{{ selectedCert?.outcomeName }}</span>
            </div>
            <div class="preview-item">
              <span class="lbl">使用转换规则：</span>
              <span class="val">{{ selectedRule?.ruleName }}</span>
            </div>
            <div class="preview-item">
              <span class="lbl">扣减来源学分：</span>
              <span class="val warning">{{ selectedCert?.availableCredit }}</span>
            </div>
            <div class="preview-item">
              <span class="lbl">预期获得目标学分：</span>
              <span class="val success font-lg">{{ previewData.targetCredit }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="steps-action">
        <el-button v-if="activeStep > 0" @click="prev">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="next">下一步</el-button>
        <el-button v-if="activeStep === 2" type="success" class="gradient-btn" :loading="submitting" @click="submit">
          确认提交申请
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyLearnerOutcomes } from '@/api/outcome'
import { matchConversionRules, previewConversion, submitConversion } from '@/api/conversion'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const activeStep = ref(0)

const certsLoading = ref(false)
const myApprovedCerts = ref<any[]>([])
const selectedCertId = ref<number | null>(null)

const rulesLoading = ref(false)
const matchedRules = ref<any[]>([])
const selectedRuleId = ref<number | null>(null)

const previewLoading = ref(false)
const previewData = ref<any>(null)
const submitting = ref(false)

const selectedCert = computed(() => {
  return myApprovedCerts.value.find(item => item.id === selectedCertId.value)
})

const selectedRule = computed(() => {
  return matchedRules.value.find(item => item.id === selectedRuleId.value)
})

const loadApprovedCerts = async () => {
  certsLoading.value = true
  try {
    const res: any = await getMyLearnerOutcomes({ page: 1, size: 200 })
    myApprovedCerts.value = res.records || []
  } catch (e) {}
  certsLoading.value = false
}

const handleCertSelect = async (id: number) => {
  matchedRules.value = []
  selectedRuleId.value = null
}

const prev = () => {
  if (activeStep.value > 0) activeStep.value--
}

const next = async () => {
  if (activeStep.value === 0) {
    if (!selectedCertId.value) {
      ElMessage.warning('请先选择来源成果')
      return
    }
    
    // Load matching rules
    rulesLoading.value = true
    activeStep.value++
    try {
      const cert = selectedCert.value
      const rules = await matchConversionRules(cert.id)
      matchedRules.value = rules || []
    } catch (e) {}
    rulesLoading.value = false
  } else if (activeStep.value === 1) {
    if (!selectedRuleId.value) {
      ElMessage.warning('请选择要应用的转换规则')
      return
    }
    activeStep.value++
    
    // Load Preview
    previewLoading.value = true
    try {
      const cert = selectedCert.value
      const res = await previewConversion(selectedRuleId.value!, cert.id, cert.availableCredit)
      previewData.value = res
    } catch (e) {}
    previewLoading.value = false
  }
}

const submit = async () => {
  submitting.value = true
  try {
    const cert = selectedCert.value
    await submitConversion({
      ruleId: selectedRuleId.value,
      sourceOutcomeId: cert.id,
      sourceCredit: cert.availableCredit
    })
    ElMessage.success('学分转换申请已提交，等待审核')
    router.push('/conversion/list')
  } catch (e) {}
  submitting.value = false
}

onMounted(() => {
  loadApprovedCerts()
})
</script>

<style scoped>
.apply-card {
  padding: 40px;
  border-radius: var(--radius-xl);
}
.custom-steps {
  margin-bottom: 40px;
}
.step-content {
  min-height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.step-pane {
  width: 100%;
  max-width: 580px;
}
.pane-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}
.full-width {
  width: 100%;
}
.rules-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}
.rule-item-card {
  padding: 16px 20px;
  cursor: pointer;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  transition: var(--transition);
}
.rule-item-card:hover, .rule-item-card.active {
  border-color: var(--accent);
}
.rule-item-card :deep(.el-radio) {
  width: 100%;
  height: auto;
  align-items: flex-start;
}
.rule-item-card :deep(.el-radio__label) {
  display: block;
}
.rule-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.rule-ratio {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.rule-desc {
  font-size: 12px;
  color: var(--text-muted);
}
.empty-rules {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
.preview-box {
  padding: 24px;
}
.preview-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}
.preview-item:last-child {
  margin-bottom: 0;
}
.preview-item .lbl {
  color: var(--text-secondary);
  width: 150px;
}
.preview-item .val {
  color: var(--text-primary);
  font-weight: 500;
}
.preview-item .val.success { color: var(--success); }
.preview-item .val.warning { color: var(--warning); }
.preview-item .font-lg { font-size: 18px; }

.highlight {
  color: var(--accent);
  font-weight: 700;
}
.steps-action {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
  border-top: 1px solid var(--border);
  padding-top: 24px;
}
</style>
