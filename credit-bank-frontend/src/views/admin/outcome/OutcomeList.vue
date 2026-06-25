<template>
  <div class="admin-outcome-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">成果目录管理</h2>
        <p>管理系统内的正规与非正规学习成果，配置标准学分比例</p>
      </div>
      <div class="header-actions">
        <el-button @click="openImportDialog">导入 CSV</el-button>
        <el-button type="info" @click="handleExport">导出 CSV</el-button>
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          新建成果目录
        </el-button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-bar glass-card">
      <el-input v-model="query.keyword" placeholder="成果名/代码" class="filter-input" clearable @input="handleSearch" />
      <el-select v-model="query.outcomeType" placeholder="成果类型" clearable class="filter-select" @change="handleSearch">
        <el-option label="课程证书" value="course_cert" />
        <el-option label="毕业证书" value="diploma_cert" />
        <el-option label="学位证书" value="degree_cert" />
        <el-option label="职业资格" value="vocational_qualification" />
        <el-option label="技能等级" value="skill_level" />
        <el-option label="培训证书" value="training_cert" />
      </el-select>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="outcomes" style="width: 100%">
        <el-table-column prop="outcomeCode" label="成果代码" width="130" />
        <el-table-column prop="outcomeName" label="成果名称" min-width="150" />
        <el-table-column prop="outcomeType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.outcomeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseCredit" label="标准学分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              active-value="enabled" 
              inactive-value="disabled"
              @change="(val: any) => handleStatusChange(row.id, val)" 
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
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
          @current-change="loadOutcomes"
        />
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑成果' : '新建成果目录'" width="min(680px, 92vw)">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成果代码" prop="outcomeCode">
              <el-input v-model="form.outcomeCode" :disabled="isEdit" placeholder="如：COUR-001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果名称" prop="outcomeName">
              <el-input v-model="form.outcomeName" placeholder="课程、技能名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成果类型" prop="outcomeType">
              <el-select v-model="form.outcomeType" style="width: 100%">
                <el-option label="课程证书" value="course_cert" />
                <el-option label="毕业证书" value="diploma_cert" />
                <el-option label="学位证书" value="degree_cert" />
                <el-option label="职业资格" value="vocational_qualification" />
                <el-option label="技能等级" value="skill_level" />
                <el-option label="培训证书" value="training_cert" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准认定学分" prop="baseCredit">
              <el-input-number v-model="form.baseCredit" :min="0.1" :precision="1" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="成果介绍" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="成果说明" />
        </el-form-item>

        <el-form-item label="证明材料上传要求说明" prop="certificationRequirements">
          <el-input v-model="form.certificationRequirements" type="textarea" :rows="3" placeholder="要求说明" />
        </el-form-item>

        <el-form-item label="适用范围说明" prop="applicableScope">
          <el-input v-model="form.applicableScope" placeholder="如：全省互认" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入成果目录" width="min(520px, 92vw)">
      <el-alert
        type="info"
        :closable="false"
        title="支持 UTF-8 CSV。相同成果代码将更新，其他有效记录将新增。"
        show-icon
      />
      <div class="import-placeholder">
        <el-upload
          drag
          :auto-upload="true"
          accept=".csv"
          :show-file-list="false"
          :disabled="importing"
          :http-request="handleImportUpload"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽 CSV 到这里，或点击选择文件</div>
          <template #tip>
            <div class="el-upload__tip">文件不超过 5 MB，最多 10000 行；错误行不会影响其他有效记录。</div>
          </template>
        </el-upload>
      </div>
      <el-descriptions v-if="importResult" :column="2" border class="import-result">
        <el-descriptions-item label="Total">{{ importResult.totalRows }}</el-descriptions-item>
        <el-descriptions-item label="Created">{{ importResult.createdCount }}</el-descriptions-item>
        <el-descriptions-item label="Updated">{{ importResult.updatedCount }}</el-descriptions-item>
        <el-descriptions-item label="Skipped">{{ importResult.skippedCount }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="importResult?.errors?.length"
        :data="importResult.errors"
        size="small"
        class="import-errors"
      >
        <el-table-column prop="row" label="Row" width="80" />
        <el-table-column prop="message" label="Message" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getOutcomeList,
  createOutcome,
  updateOutcome,
  deleteOutcome,
  updateOutcomeStatus,
  importOutcomes,
  exportOutcomes
} from '@/api/outcome'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const outcomes = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  outcomeType: '',
  keyword: ''
})

const dialogVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)
const importing = ref(false)
const importResult = ref<any | null>(null)

const form = reactive({
  outcomeCode: '',
  outcomeName: '',
  outcomeType: 'course_cert',
  baseCredit: 1.0,
  intro: '',
  certificationRequirements: '',
  applicableScope: ''
})

const rules = {
  outcomeCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
  outcomeName: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
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

const loadOutcomes = async () => {
  loading.value = true
  try {
    const res: any = await getOutcomeList(query)
    outcomes.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadOutcomes()
}

const handleStatusChange = async (id: number, val: any) => {
  try {
    await updateOutcomeStatus(id, val)
    ElMessage.success('状态更新成功')
  } catch (e) {
    loadOutcomes()
  }
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, {
    outcomeCode: '',
    outcomeName: '',
    outcomeType: 'course_cert',
    baseCredit: 1.0,
    intro: '',
    certificationRequirements: '',
    applicableScope: ''
  })
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
          await updateOutcome(currentId.value, form)
          ElMessage.success('成果目录已保存')
        } else {
          await createOutcome(form)
          ElMessage.success('成果目录已创建')
        }
        dialogVisible.value = false
        loadOutcomes()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该成果目录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOutcome(id)
      ElMessage.success('删除成功')
      loadOutcomes()
    } catch (e) {}
  }).catch(() => {})
}

const openImportDialog = () => {
  importResult.value = null
  importVisible.value = true
}

const handleImportUpload = async (options: any) => {
  importing.value = true
  try {
    const result: any = await importOutcomes(options.file)
    importResult.value = result
    ElMessage.success(`导入完成：新增 ${result.createdCount} 条，更新 ${result.updatedCount} 条`)
    loadOutcomes()
    options.onSuccess?.(result)
  } catch (e) {
    options.onError?.(e)
  } finally {
    importing.value = false
  }
}

const handleExport = async () => {
  const blob: Blob = await exportOutcomes()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'outcome_catalog.csv'
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadOutcomes()
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
.header-actions {
  display: flex;
  gap: 12px;
}
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-input {
  width: 220px;
}
.filter-select {
  width: 180px;
}
.table-card {
  padding: 20px;
  max-width: 100%;
  overflow-x: auto;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.import-result,
.import-errors {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  .header-actions :deep(.el-button) {
    flex: 1 1 96px;
    margin-left: 0;
  }
  .filter-bar {
    flex-direction: column;
    padding: 16px;
  }
  .filter-input,
  .filter-select {
    width: 100%;
  }
  .table-card {
    padding: 12px;
  }
  .pagination-wrapper {
    justify-content: flex-start;
    overflow-x: auto;
  }
  :deep(.el-dialog__body) {
    padding-left: 16px;
    padding-right: 16px;
  }
  :deep(.el-form .el-col-12) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}
</style>
