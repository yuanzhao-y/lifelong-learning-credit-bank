<template>
  <div class="cert-apply-container">
    <page-header title="学分认定申请" description="提交并核算您获得的学习成果，兑换为系统内的可用学分" />

    <div class="steps-card glass-card">
      <el-steps :active="activeStep" finish-status="success" align-center class="custom-steps">
        <el-step title="选择成果" />
        <el-step title="填写详细信息" />
        <el-step title="上传佐证材料" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: Select Outcome -->
        <div v-if="activeStep === 0" class="step-pane">
          <div class="pane-title">选择成果目录</div>
          <el-select
            v-model="selectedOutcomeId"
            placeholder="请选择学习成果目录"
            filterable
            class="outcome-select"
            @change="handleOutcomeSelect"
          >
            <el-option
              v-for="item in outcomeOptions"
              :key="item.id"
              :label="`${item.outcomeName} (${formatType(item.outcomeType)})`"
              :value="item.id"
            />
          </el-select>
          <div class="info-tip" v-if="selectedOutcome">
            选定成果标准学分：<span class="highlight">{{ selectedOutcome.baseCredit }}</span>
          </div>
        </div>

        <!-- Step 2: Form Details -->
        <div v-if="activeStep === 1" class="step-pane">
          <div class="pane-title">填写成果证书详细信息</div>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="cert-form">
            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="证明名称 (选填)" prop="outcomeName">
                  <el-input v-model="form.outcomeName" placeholder="默认使用成果名，可修改" />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="证书编号 / 证明文号" prop="certificateNo">
                  <el-input v-model="form.certificateNo" placeholder="请输入证书唯一标识码" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="颁发机构" prop="issuingAuthority">
                  <el-input v-model="form.issuingAuthority" placeholder="请输入颁发单位或高校名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="取得日期" prop="obtainedAt">
                  <el-date-picker
                    v-model="form.obtainedAt"
                    type="date"
                    placeholder="选择取得时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="申请学分" prop="requestedCredit">
              <el-input-number v-model="form.requestedCredit" :min="0.1" :precision="1" :step="0.5" />
              <div class="hint">建议不超过标准学分：{{ selectedOutcome?.baseCredit }}</div>
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 3: Upload Materials -->
        <div v-if="activeStep === 2" class="step-pane">
          <div class="pane-title">上传材料 (如证书原件扫描件、学习成绩单等)</div>
          <file-upload @change="handleFileChange" />
          <div class="uploaded-count" v-if="form.materialFileIds.length > 0">
            已上传 <span class="highlight">{{ form.materialFileIds.length }}</span> 个佐证文件
          </div>
        </div>
      </div>

      <div class="steps-action">
        <el-button v-if="activeStep > 0" @click="prev">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="next">下一步</el-button>
        <el-button v-if="activeStep === 2" type="success" class="gradient-btn" :loading="submitting" @click="submit">
          提交申请
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicOutcomes, getPublicOutcomeDetail } from '@/api/outcome'
import { submitCert } from '@/api/cert'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import FileUpload from '@/components/FileUpload.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()

const activeStep = ref(0)
const selectedOutcomeId = ref<string>('')
const outcomeOptions = ref<any[]>([])
const selectedOutcome = ref<any>(null)
const submitting = ref(false)

const form = reactive({
  catalogId: '',
  certifyType: '',
  outcomeName: '',
  certificateNo: '',
  issuingAuthority: '',
  obtainedAt: '',
  requestedCredit: 1.0,
  materialFileIds: [] as number[]
})

const rules = {
  certificateNo: [{ required: true, message: '证书编号不能为空', trigger: 'blur' }],
  issuingAuthority: [{ required: true, message: '颁发机构不能为空', trigger: 'blur' }],
  obtainedAt: [{ required: true, message: '取得日期不能为空', trigger: 'change' }],
  requestedCredit: [{ required: true, message: '申请学分不能为空', trigger: 'blur' }]
}

const formatType = (type: string) => {
  const map: Record<string, string> = {
    course_cert: '课程证书',
    diploma_cert: '毕业证书',
    degree_cert: '学位证书',
    vocational_qualification: '职业资格',
    skill_level: '技能等级',
    training_cert: '培训证书'
  }
  return map[type] || type
}

const loadOutcomeOptions = async () => {
  try {
    const res: any = await getPublicOutcomes({ page: 1, size: 200 })
    outcomeOptions.value = res.records || []
    
    // Check if redirect query exists
    if (route.query.outcomeId) {
      selectedOutcomeId.value = String(route.query.outcomeId)
      handleOutcomeSelect(selectedOutcomeId.value)
    }
  } catch (e) {}
}

const handleOutcomeSelect = async (id: string) => {
  try {
    const detail = await getPublicOutcomeDetail(id)
    selectedOutcome.value = detail
    form.catalogId = detail.id
    form.certifyType = detail.outcomeType
    form.outcomeName = detail.outcomeName
    form.requestedCredit = detail.baseCredit
  } catch (e) {}
}

const handleFileChange = (ids: number[]) => {
  form.materialFileIds = ids
}

const prev = () => {
  if (activeStep.value > 0) activeStep.value--
}

const next = async () => {
  if (activeStep.value === 0) {
    if (!selectedOutcomeId.value) {
      ElMessage.warning('请选择需要认证的成果目录')
      return
    }
    activeStep.value++
  } else if (activeStep.value === 1) {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        activeStep.value++
      }
    })
  }
}

const submit = async () => {
  if (form.materialFileIds.length === 0) {
    ElMessage.warning('请上传至少一份佐证材料（证书照片或扫描件）')
    return
  }
  submitting.value = true
  try {
    await submitCert(form)
    ElMessage.success('申请提交成功')
    router.push('/cert/list')
  } catch (e) {}
  submitting.value = false
}

onMounted(() => {
  loadOutcomeOptions()
})
</script>

<style scoped>
.steps-card {
  padding: 40px;
  border-radius: var(--radius-xl);
}
.custom-steps {
  margin-bottom: 40px;
}
.step-content {
  min-height: 240px;
  padding: 20px 0;
  display: flex;
  justify-content: center;
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
.outcome-select {
  width: 100%;
}
.info-tip {
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}
.highlight {
  color: var(--accent);
  font-weight: 700;
}
.hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
.uploaded-count {
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
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
