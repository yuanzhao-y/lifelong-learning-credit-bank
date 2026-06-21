<template>
  <div class="api-permission-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">API 接口权限管理</h2>
        <p>注册系统所有的HTTP底层API端点规则，用于与系统角色细粒度关联控制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          注册新接口
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="apis" style="width: 100%">
        <el-table-column prop="apiCode" label="接口代码" width="150" />
        <el-table-column prop="apiName" label="接口说明描述" min-width="150" />
        <el-table-column prop="module" label="所属模块" width="120" />
        <el-table-column prop="httpMethod" label="方法 (Method)" width="120">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.httpMethod)" size="small">
              {{ row.httpMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pathPattern" label="匹配路径规则" min-width="200" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑接口' : '注册接口权限'" width="35%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="接口唯一代码" prop="apiCode">
          <el-input v-model="form.apiCode" placeholder="如：sys:user:list" />
        </el-form-item>
        <el-form-item label="接口说明" prop="apiName">
          <el-input v-model="form.apiName" placeholder="如：获取用户分页列表" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属功能模块" prop="module">
              <el-input v-model="form.module" placeholder="如：用户管理" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="HTTP Method" prop="httpMethod">
              <el-select v-model="form.httpMethod" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Ant匹配路径" prop="pathPattern">
          <el-input v-model="form.pathPattern" placeholder="如：/admin/users/**" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getApiPermissions, createApiPermission, updateApiPermission, deleteApiPermission } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const apis = ref<any[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  apiCode: '',
  apiName: '',
  module: '',
  httpMethod: 'GET',
  pathPattern: ''
})

const rules = {
  apiCode: [{ required: true, message: '接口编码不能为空', trigger: 'blur' }],
  apiName: [{ required: true, message: '说明描述不能为空', trigger: 'blur' }],
  pathPattern: [{ required: true, message: '接口匹配路径规则不能为空', trigger: 'blur' }]
}

const getMethodTagType = (method: string) => {
  const map: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

const loadApis = async () => {
  loading.value = true
  try {
    const res = await getApiPermissions()
    apis.value = res || []
  } catch (e) {}
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { apiCode: '', apiName: '', module: '', httpMethod: 'GET', pathPattern: '' })
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
          await updateApiPermission(currentId.value, form)
          ElMessage.success('接口规则更新成功')
        } else {
          await createApiPermission(form)
          ElMessage.success('接口权限注册成功')
        }
        dialogVisible.value = false
        loadApis()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('注销此接口端点配置可能使相关角色暂时瘫痪，确认注销吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteApiPermission(id)
      ElMessage.success('接口配置已注销')
      loadApis()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadApis()
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
</style>
