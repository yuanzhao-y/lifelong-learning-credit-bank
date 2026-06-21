<template>
  <div class="education-container">
    <page-header title="教育经历管理" description="维护您的全日制、非全日制教育轨迹，作为辅助认定学分的背景依据">
      <template #actions>
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          添加经历
        </el-button>
      </template>
    </page-header>

    <!-- Cards Timeline -->
    <div class="timeline-wrapper glass-card" v-loading="loading">
      <el-timeline v-if="items.length > 0">
        <el-timeline-item
          v-for="item in items"
          :key="item.id"
          type="primary"
          hollow
          placement="top"
          :timestamp="`${formatDate(item.enrollmentDate)} ~ ${formatDate(item.graduationDate)}`"
        >
          <div class="timeline-card-content glass-card">
            <div class="card-left">
              <h4>{{ item.school }}</h4>
              <div class="major-degree">{{ item.major }} · {{ item.degree }}</div>
            </div>
            <div class="card-right">
              <el-button link type="primary" @click="showEditDialog(item)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div v-else class="empty-holder">
        暂无登记的教育经历，请点击右上角进行添加
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教育经历' : '添加教育经历'" width="45%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="学校名称" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校官方全称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="专业名称" prop="major">
              <el-input v-model="form.major" placeholder="请输入修读专业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="学历 / 学位" prop="degree">
              <el-input v-model="form.degree" placeholder="如：本科/学士、硕士研究生..." />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="入学时间" prop="enrollmentDate">
              <el-date-picker
                v-model="form.enrollmentDate"
                type="date"
                placeholder="请选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="毕业时间" prop="graduationDate">
              <el-date-picker
                v-model="form.graduationDate"
                type="date"
                placeholder="请选择日期"
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
import { getEducations, createEducation, updateEducation, deleteEducation } from '@/api/profile'
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
  school: '',
  major: '',
  degree: '',
  enrollmentDate: '',
  graduationDate: ''
})

const rules = {
  school: [{ required: true, message: '学校名称不能为空', trigger: 'blur' }],
  major: [{ required: true, message: '专业名称不能为空', trigger: 'blur' }]
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await getEducations()
    items.value = res || []
  } catch (e) {}
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { school: '', major: '', degree: '', enrollmentDate: '', graduationDate: '' })
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
        if (isEdit.value && currentId.value) {
          await updateEducation(currentId.value, form)
          ElMessage.success('更新成功')
        } else {
          await createEducation(form)
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
  ElMessageBox.confirm('确定要删除此条教育经历吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteEducation(id)
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
.major-degree {
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
