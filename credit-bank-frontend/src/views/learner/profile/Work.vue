<template>
  <div class="work-container">
    <page-header title="工作经历管理" description="录入和管理您的社会实践及职业履历记录">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          添加经历
        </el-button>
      </template>
    </page-header>

    <!-- Timeline list -->
    <div class="timeline-wrapper glass-card" v-loading="loading">
      <el-timeline v-if="items.length > 0">
        <el-timeline-item
          v-for="item in items"
          :key="item.id"
          type="primary"
          hollow
          placement="top"
          :timestamp="`${formatDate(item.entryDate)} ~ ${item.leaveDate ? formatDate(item.leaveDate) : '至今'}`"
        >
          <div class="timeline-card-content glass-card">
            <div class="card-left">
              <h4>{{ item.companyName }}</h4>
              <div class="position-info">{{ item.position }}</div>
            </div>
            <div class="card-right">
              <el-button link type="primary" @click="showEditDialog(item)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div v-else class="empty-holder">
        暂无登记的工作履历，请点击右上角进行添加
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑工作经历' : '添加工作经历'" width="45%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="企业/单位名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入任职单位全称" />
        </el-form-item>
        <el-form-item label="岗位 / 职务" prop="position">
          <el-input v-model="form.position" placeholder="请输入具体任职岗位名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="入职时间" prop="entryDate">
              <el-date-picker
                v-model="form.entryDate"
                type="date"
                placeholder="请选择入职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="离职时间 (如仍任职可留空)" prop="leaveDate">
              <el-date-picker
                v-model="form.leaveDate"
                type="date"
                placeholder="请选择离职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getWorks, createWork, updateWork, deleteWork } from '@/api/profile'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const saving = ref(false)
const items = ref<any[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  companyName: '',
  position: '',
  entryDate: '',
  leaveDate: ''
})

const rules = {
  companyName: [{ required: true, message: '企业/单位名称不能为空', trigger: 'blur' }],
  position: [{ required: true, message: '任职岗位不能为空', trigger: 'blur' }],
  entryDate: [{ required: true, message: '入职时间不能为空', trigger: 'change' }]
}

const buildPayload = () => ({
  companyName: form.companyName,
  position: form.position,
  entryDate: form.entryDate || null,
  leaveDate: form.leaveDate || null
})

const loadItems = async () => {
  loading.value = true
  try {
    const res = await getWorks()
    items.value = res || []
  } catch (e) {}
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { companyName: '', position: '', entryDate: '', leaveDate: '' })
  dialogVisible.value = true
}

const showEditDialog = (item: any) => {
  isEdit.value = true
  currentId.value = item.id
  Object.assign(form, item)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const payload = buildPayload()
        if (isEdit.value && currentId.value) {
          await updateWork(currentId.value, payload)
          ElMessage.success('更新成功')
        } else {
          await createWork(payload)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadItems()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除此条工作履历吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteWork(id)
      ElMessage.success('删除成功')
      loadItems()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.timeline-wrapper {
  padding: 40px 30px;
}
.timeline-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}
.card-left h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.position-info {
  font-size: 13px;
  color: var(--text-secondary);
}
.card-right {
  display: flex;
  gap: 12px;
}
.empty-holder {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}
</style>
