<template>
  <div class="expert-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">评审专家库</h2>
        <p>系统聘书专家聘用、领域专长及审核可用状态管理表</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          特聘新专家
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="experts" style="width: 100%">
        <el-table-column prop="realName" label="专家名称" width="130" />
        <el-table-column prop="title" label="技术职称" width="140" />
        <el-table-column prop="expertField" label="专长领域" min-width="150" />
        <el-table-column prop="institution" label="工作单位" min-width="180" />
        <el-table-column prop="status" label="评审状态" width="120">
          <template #default="{ row }">
            <el-select 
              v-model="row.status" 
              size="small"
              @change="(val: any) => handleStatusChange(row.id, val)"
            >
              <el-option label="可用" value="available" />
              <el-option label="请假/非空" value="leave" />
              <el-option label="已解聘" value="removed" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑信息</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">解聘</el-button>
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
          @current-change="loadExperts"
        />
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑专家档案' : '聘用专家'" width="40%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专家用户名 (系统内已注册用户)" prop="username" v-if="!isEdit">
              <el-input v-model="form.username" placeholder="请输入其登录用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="技术职称" prop="title">
              <el-input v-model="form.title" placeholder="如：教授、研究员、高级工程师..." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作单位" prop="institution">
              <el-input v-model="form.institution" placeholder="挂靠高校或企事业单位" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="专长研究领域描述" prop="expertField">
          <el-input v-model="form.expertField" placeholder="如：计算机科学与技术、教育学等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存专家信息</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getExpertList, createExpert, updateExpert, deleteExpert, updateExpertStatus } from '@/api/expert'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const experts = ref<any[]>([])
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
  username: '',
  realName: '',
  title: '',
  institution: '',
  expertField: ''
})

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }]
}

const loadExperts = async () => {
  loading.value = true
  try {
    const res: any = await getExpertList(query)
    experts.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleStatusChange = async (id: number, val: string) => {
  try {
    await updateExpertStatus(id, val)
    ElMessage.success('更新专家状态成功')
  } catch (e) {
    loadExperts()
  }
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { username: '', realName: '', title: '', institution: '', expertField: '' })
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
          await updateExpert(currentId.value, form)
          ElMessage.success('保存成功')
        } else {
          await createExpert(form)
          ElMessage.success('专家录用成功')
        }
        dialogVisible.value = false
        loadExperts()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('解聘专家将收回其评审任务，确定吗？', '提示').then(async () => {
    try {
      await deleteExpert(id)
      ElMessage.success('已解聘')
      loadExperts()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadExperts()
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
