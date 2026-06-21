<template>
  <div class="user-list-container">
    <div class="page-header glass-card">
      <h2 class="gradient-text">用户管理</h2>
      <p>维护系统所有注册账户的状态、详情和角色配置</p>
    </div>

    <!-- Filters -->
    <div class="filter-bar glass-card">
      <el-input v-model="query.username" placeholder="搜索用户名" class="filter-input" clearable @input="handleSearch" />
      <el-input v-model="query.realName" placeholder="搜索真实姓名" class="filter-input" clearable @input="handleSearch" />
      <el-select v-model="query.status" placeholder="用户状态" clearable class="filter-select" @change="handleSearch">
        <el-option label="启用" value="enabled" />
        <el-option label="停用" value="disabled" />
        <el-option label="冻结" value="frozen" />
      </el-select>
    </div>

    <!-- Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phoneMasked" label="手机号码" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
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
        <el-table-column prop="roles" label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag 
              v-for="role in row.roles" 
              :key="role" 
              size="small" 
              class="role-tag"
            >
              {{ formatRole(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showRoleDialog(row)">分配角色</el-button>
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
          @current-change="loadUsers"
        />
      </div>
    </div>

    <!-- Assign Role Dialog -->
    <el-dialog v-model="roleDialogVisible" title="分配用户角色" width="30%">
      <div class="role-dialog-content" v-loading="dialogLoading">
        <el-checkbox-group v-model="selectedRoleIds">
          <el-checkbox 
            v-for="role in allRoles" 
            :key="role.id" 
            :label="role.id"
            class="role-checkbox"
          >
            {{ role.roleName }} ({{ role.roleCode }})
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssignRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, updateUserStatus, assignRoles } from '@/api/user'
import { getRoles } from '@/api/system'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const users = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  status: ''
})

const roleDialogVisible = ref(false)
const dialogLoading = ref(false)
const assigning = ref(false)
const currentUser = ref<any>(null)
const selectedRoleIds = ref<number[]>([])
const allRoles = ref<any[]>([])

const formatRole = (role: string) => {
  const map: Record<string, string> = {
    learner: '学习者',
    auditor: '审核员',
    expert: '专家',
    admin: '管理员'
  }
  return map[role] || role
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res: any = await getUserList(query)
    users.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadUsers()
}

const handleStatusChange = async (id: number, val: any) => {
  try {
    await updateUserStatus(id, val)
    ElMessage.success('用户状态修改成功')
  } catch (e) {
    loadUsers() // Revert
  }
}

const showRoleDialog = async (row: any) => {
  currentUser.value = row
  selectedRoleIds.value = []
  roleDialogVisible.value = true
  dialogLoading.value = true
  try {
    const rolesRes: any = await getRoles()
    allRoles.value = rolesRes || []
    
    // Map current user roles codes to IDs
    const matched = allRoles.value
      .filter(r => row.roles.includes(r.roleCode))
      .map(r => r.id)
    selectedRoleIds.value = matched
  } catch (e) {}
  dialogLoading.value = false
}

const handleAssignRoles = async () => {
  if (!currentUser.value) return
  assigning.value = true
  try {
    await assignRoles(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('分配角色成功')
    roleDialogVisible.value = false
    loadUsers()
  } catch (e) {}
  assigning.value = false
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.filter-input {
  width: 200px;
}
.filter-select {
  width: 150px;
}
.table-card {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.role-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}
.role-checkbox {
  display: block;
  margin-bottom: 12px;
}
</style>
