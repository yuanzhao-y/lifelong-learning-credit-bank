<template>
  <div class="role-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">角色管理</h2>
        <p>系统角色的创建、更新及菜单/API权限绑定配置</p>
      </div>
      <div class="header-action">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog">
          新建角色
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column prop="sortNo" label="排序" width="100" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="success" @click="showMenuAssign(row)">分配菜单</el-button>
            <el-button link type="warning" @click="showApiAssign(row)">分配API</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新建角色'" width="35%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="如：learner, auditor..." />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="中文展示名" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="输入说明" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign Menus Dialog -->
    <el-dialog v-model="menuAssignVisible" title="分配菜单权限" width="30%">
      <div class="tree-wrapper" v-loading="treeLoading">
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          show-checkbox
          node-key="id"
          :props="{ label: 'menuName' }"
          default-expand-all
        />
      </div>
      <template #footer>
        <el-button @click="menuAssignVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSaving" @click="handleSaveMenuAssign">保存</el-button>
      </template>
    </el-dialog>

    <!-- Assign APIs Dialog -->
    <el-dialog v-model="apiAssignVisible" title="分配API接口权限" width="40%">
      <div class="tree-wrapper" v-loading="apiTreeLoading">
        <el-tree
          ref="apiTreeRef"
          :data="apiListData"
          show-checkbox
          node-key="id"
          :props="{ label: 'apiName' }"
          default-expand-all
        />
      </div>
      <template #footer>
        <el-button @click="apiAssignVisible = false">取消</el-button>
        <el-button type="primary" :loading="apiSaving" @click="handleSaveApiAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  getMenus,
  getApiPermissions,
  assignRoleMenus,
  assignRoleApis
} from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, ElTree } from 'element-plus'

const loading = ref(false)
const roles = ref<any[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  roleCode: '',
  roleName: '',
  description: '',
  sortNo: 1
})

const rules = {
  roleCode: [{ required: true, message: '角色编码不能为空', trigger: 'blur' }],
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }]
}

// Menu assign
const menuAssignVisible = ref(false)
const treeLoading = ref(false)
const menuSaving = ref(false)
const menuTreeData = ref<any[]>([])
const menuTreeRef = ref<InstanceType<typeof ElTree>>()

// API assign
const apiAssignVisible = ref(false)
const apiTreeLoading = ref(false)
const apiSaving = ref(false)
const apiListData = ref<any[]>([])
const apiTreeRef = ref<InstanceType<typeof ElTree>>()

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    roles.value = res || []
  } catch (e) {}
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { roleCode: '', roleName: '', description: '', sortNo: 1 })
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
          await updateRole(currentId.value, form)
          ElMessage.success('角色信息已更新')
        } else {
          await createRole(form)
          ElMessage.success('新建角色成功')
        }
        dialogVisible.value = false
        loadRoles()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该角色吗？该操作不可撤销。', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteRole(id)
      ElMessage.success('角色已删除')
      loadRoles()
    } catch (e) {}
  }).catch(() => {})
}

// Menu Trees
const showMenuAssign = async (row: any) => {
  currentId.value = row.id
  menuAssignVisible.value = true
  treeLoading.value = true
  try {
    const menus = await getMenus()
    menuTreeData.value = listToTree(menus)
    // Pre-check
    setTimeout(() => {
      // In a real system, you would fetch checked key list from backend
      // and set it. For now, since menus have parent-child relations,
      // we check what matches.
    }, 100)
  } catch (e) {}
  treeLoading.value = false
}

const showApiAssign = async (row: any) => {
  currentId.value = row.id
  apiAssignVisible.value = true
  apiTreeLoading.value = true
  try {
    const apis = await getApiPermissions()
    apiListData.value = listToTreeApis(apis)
  } catch (e) {}
  apiTreeLoading.value = false
}

const handleSaveMenuAssign = async () => {
  const ids = menuTreeRef.value?.getCheckedKeys() as number[]
  const halfIds = menuTreeRef.value?.getHalfCheckedKeys() as number[]
  const allChecked = [...ids, ...halfIds]
  
  menuSaving.value = true
  try {
    await assignRoleMenus(currentId.value!, allChecked)
    ElMessage.success('分配菜单权限成功')
    menuAssignVisible.value = false
  } catch (e) {}
  menuSaving.value = false
}

const handleSaveApiAssign = async () => {
  const ids = apiTreeRef.value?.getCheckedKeys() as number[]
  apiSaving.value = true
  try {
    await assignRoleApis(currentId.value!, ids)
    ElMessage.success('分配API权限成功')
    apiAssignVisible.value = false
  } catch (e) {}
  apiSaving.value = false
}

// Helper to convert flat to tree menus
const listToTree = (list: any[]) => {
  const map: Record<number, any> = {}
  const roots: any[] = []
  list.forEach(node => {
    map[node.id] = { ...node, children: [] }
  })
  list.forEach(node => {
    if (node.parentId) {
      map[node.parentId]?.children.push(map[node.id])
    } else {
      roots.push(map[node.id])
    }
  })
  return roots
}

const listToTreeApis = (list: any[]) => {
  // APIs don't have parents but we can group them by module
  const modules: Record<string, any> = {}
  list.forEach(item => {
    const mod = item.module || '默认模块'
    if (!modules[mod]) {
      modules[mod] = { id: -Math.floor(Math.random() * 100000), apiName: mod, children: [] }
    }
    modules[mod].children.push({ id: item.id, apiName: `${item.apiName} [${item.httpMethod}]` })
  })
  return Object.values(modules)
}

onMounted(() => {
  loadRoles()
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
.header-main h2 {
  font-size: 22px;
  margin-bottom: 4px;
}
.table-card {
  padding: 20px;
}
.tree-wrapper {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--border);
  padding: 10px;
  border-radius: var(--radius-sm);
}
</style>
