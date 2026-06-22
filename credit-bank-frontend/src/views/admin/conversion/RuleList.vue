<template>
  <div class="rule-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">转换规则配置</h2>
        <p>配置并维护各类学习成果之间互认、学分兑换的基础转换规则</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          新建转换规则
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="rules" style="width: 100%">
        <el-table-column prop="ruleCode" label="规则代码" width="120" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column label="来源成果目录" min-width="150">
          <template #default="{ row }">{{ row.sourceCatalogName || `目录 #${row.sourceCatalogId}` }}</template>
        </el-table-column>
        <el-table-column label="目标成果目录" min-width="150">
          <template #default="{ row }">{{ row.targetCatalogName || `目录 #${row.targetCatalogId}` }}</template>
        </el-table-column>
        <el-table-column prop="conversionRatio" label="兑换比率" width="100" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <status-tag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button 
              v-if="row.status === 'draft'" 
              link 
              type="warning" 
              @click="handleSubmitReview(row.id)"
            >
              送审
            </el-button>
            <el-button 
              v-if="row.status === 'reviewing'" 
              link 
              type="success" 
              @click="showExpertDialog(row)"
            >
              分配专家
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadRules"
        />
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '创建规则'" width="45%">
      <el-form :model="form" ref="formRef" :rules="rulesValidate" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则代码" prop="ruleCode">
              <el-input v-model="form.ruleCode" :disabled="isEdit" placeholder="如：RULE-CO-DI" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="form.ruleName" placeholder="输入名称描述" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="来源成果目录ID" prop="sourceCatalogId">
              <el-input-number v-model="form.sourceCatalogId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标成果目录ID" prop="targetCatalogId">
              <el-input-number v-model="form.targetCatalogId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学分换算比率" prop="conversionRatio">
              <el-input-number v-model="form.conversionRatio" :min="0.1" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="规则描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="填写换算规则描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign Expert Dialog -->
    <el-dialog v-model="expertVisible" title="指派评审专家" width="30%">
      <div v-loading="expertLoading">
        <el-select v-model="selectedExpertId" placeholder="选择受托专家" class="full-width">
          <el-option
            v-for="item in experts"
            :key="item.id"
            :label="`${item.expertName} [方向：${item.professionalDirection || '未填'} | 用户ID：${item.userId}]`"
            :value="item.id"
          />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="expertVisible = false">取消</el-button>
        <el-button type="primary" :loading="expertSaving" @click="handleAssignSubmit">指派专家</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getConversionRules,
  createConversionRule,
  updateConversionRule,
  deleteConversionRule,
  submitRuleReview,
  assignExpert
} from '@/api/conversion'
import { getExpertList } from '@/api/expert'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const rules = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  ruleCode: '',
  ruleName: '',
  sourceCatalogId: 1,
  targetCatalogId: 2,
  conversionRatio: 1.0,
  description: ''
})

const rulesValidate = {
  ruleCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
  ruleName: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
}

// Expert Assign
const expertVisible = ref(false)
const expertLoading = ref(false)
const expertSaving = ref(false)
const experts = ref<any[]>([])
const selectedExpertId = ref<number | null>(null)

const loadRules = async () => {
  loading.value = true
  try {
    const res: any = await getConversionRules(query)
    rules.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { ruleCode: '', ruleName: '', sourceCatalogId: 1, targetCatalogId: 2, conversionRatio: 1.0, description: '' })
  dialogVisible.value = true
}

const showEditDialog = (row: any) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        if (isEdit.value && currentId.value) {
          await updateConversionRule(currentId.value, form)
          ElMessage.success('规则配置已保存')
        } else {
          await createConversionRule(form)
          ElMessage.success('规则已添加为草稿')
        }
        dialogVisible.value = false
        loadRules()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleSubmitReview = async (id: number) => {
  try {
    await submitRuleReview(id)
    ElMessage.success('已提请专家评审')
    loadRules()
  } catch (e) {}
}

const showExpertDialog = async (row: any) => {
  currentId.value = row.id
  selectedExpertId.value = null
  expertVisible.value = true
  expertLoading.value = true
  try {
    const res: any = await getExpertList({ page: 1, size: 100, status: 'available' })
    experts.value = res.records || []
  } catch (e) {}
  expertLoading.value = false
}

const handleAssignSubmit = async () => {
  if (!selectedExpertId.value) {
    ElMessage.warning('请选择一位评审专家')
    return
  }
  expertSaving.value = true
  try {
    await assignExpert(currentId.value!, selectedExpertId.value)
    ElMessage.success('成功指派审核专家')
    expertVisible.value = false
    loadRules()
  } catch (e) {}
  expertSaving.value = false
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该规则吗？', '提示').then(async () => {
    try {
      await deleteConversionRule(id)
      ElMessage.success('规则已废除')
      loadRules()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 24px;
}
.table-card {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.full-width {
  width: 100%;
}
</style>
