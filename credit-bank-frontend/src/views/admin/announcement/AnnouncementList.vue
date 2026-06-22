<template>
  <div class="announcement-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">通知公告管理</h2>
        <p>编辑并向公开来宾及学习者发布政策指南、系统动态公告</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          发布新公告
        </el-button>
      </div>
    </div>

    <div class="filter-bar glass-card">
      <el-input v-model="keyword" placeholder="搜索公告标题" clearable />
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="filteredAnnouncements" style="width: 100%">
        <el-table-column prop="title" label="公告标题" min-width="150" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="enabled"
              inactive-value="disabled"
              @change="(val: any) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="publishAt" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.publishAt) }}
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
          @current-change="loadAnnouncements"
        />
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新建公告'" width="45%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="富文本正文内容 (支持HTML/Markdown)" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="输入详细正文说明内容..." />
        </el-form-item>
        <el-form-item label="定时发布时间 (留空为即时发布)" prop="publishAt">
          <el-date-picker
            v-model="form.publishAt"
            type="datetime"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="enabled">启用发布</el-radio>
            <el-radio label="disabled">停用草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确认保存并发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { getAnnouncementList, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const announcements = ref<any[]>([])
const total = ref(0)
const keyword = ref('')

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
  title: '',
  content: '',
  publishAt: '',
  status: 'enabled'
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '正文内容不能为空', trigger: 'blur' }]
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res: any = await getAnnouncementList(query)
    announcements.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const filteredAnnouncements = computed(() => {
  if (!keyword.value) return announcements.value
  return announcements.value.filter(item => item.title?.includes(keyword.value))
})

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { title: '', content: '', publishAt: '', status: 'enabled' })
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
          await updateAnnouncement(currentId.value, form)
          ElMessage.success('公告已修改')
        } else {
          await createAnnouncement(form)
          ElMessage.success('公告发布成功')
        }
        dialogVisible.value = false
        loadAnnouncements()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleStatusChange = async (row: any, status: string) => {
  try {
    await updateAnnouncement(row.id, { ...row, status })
    ElMessage.success(status === 'enabled' ? '公告已启用' : '公告已停用')
  } catch (e) {
    loadAnnouncements()
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示').then(async () => {
    try {
      await deleteAnnouncement(id)
      ElMessage.success('公告已下线删除')
      loadAnnouncements()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadAnnouncements()
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
.filter-bar {
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-bar .el-input {
  max-width: 320px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
