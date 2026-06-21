<template>
  <div class="menu-list-container">
    <div class="page-header glass-card">
      <div class="header-main">
        <h2 class="gradient-text">菜单管理</h2>
        <p>配置系统侧边导航菜单、页面目录及功能按钮级权限</p>
      </div>
      <div class="header-action">
        <el-button type="primary" class="gradient-btn" @click="showAddDialog(null)">
          新建一级菜单
        </el-button>
      </div>
    </div>

    <!-- Tree Table -->
    <div class="table-card glass-card" v-loading="loading">
      <el-table
        :data="menuTree"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="150" />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getMenuTypeTag(row.menuType)" size="small">
              {{ formatMenuType(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuCode" label="编码" width="120" />
        <el-table-column prop="routePath" label="路由路径" width="150" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showAddDialog(row.id)">新增子菜单</el-button>
            <el-button link type="success" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新建菜单'" width="40%">
      <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上级菜单ID">
              <el-input-number v-model="form.parentId" disabled style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="menuType">
              <el-select v-model="form.menuType" style="width: 100%">
                <el-option label="目录" value="catalog" />
                <el-option label="菜单" value="menu" />
                <el-option label="按钮" value="button" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单代码 / 唯一键" prop="menuCode">
              <el-input v-model="form.menuCode" placeholder="如：SysUser" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称 / 标题" prop="menuName">
              <el-input v-model="form.menuName" placeholder="如：用户管理" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.menuType !== 'button'">
          <el-col :span="12">
            <el-form-item label="路由地址" prop="routePath">
              <el-input v-model="form.routePath" placeholder="如：/admin/users" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单图标" prop="icon">
              <el-input v-model="form.icon" placeholder="Element Icon名称，如 User, Menu" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="按钮权限标识 (仅按钮类型有效)" prop="permissionCode">
              <el-input v-model="form.permissionCode" placeholder="如：sys:user:add" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sortNo">
              <el-input-number v-model="form.sortNo" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { getMenus, createMenu, updateMenu, deleteMenu } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const menuTree = ref<any[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive({
  parentId: undefined as number | undefined,
  menuCode: '',
  menuName: '',
  menuType: 'menu',
  routePath: '',
  icon: '',
  permissionCode: '',
  sortNo: 1
})

const rules = {
  menuCode: [{ required: true, message: '菜单编码不能为空', trigger: 'blur' }],
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  menuType: [{ required: true, message: '菜单类型不能为空', trigger: 'change' }]
}

const getMenuTypeTag = (type: string) => {
  if (type === 'catalog') return 'primary'
  if (type === 'menu') return 'success'
  return 'info'
}

const formatMenuType = (type: string) => {
  if (type === 'catalog') return '目录'
  if (type === 'menu') return '菜单'
  return '按钮'
}

const loadMenus = async () => {
  loading.value = true
  try {
    const res = await getMenus()
    menuTree.value = listToTree(res || [])
  } catch (e) {}
  loading.value = false
}

const showAddDialog = (parentId: number | null) => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, {
    parentId: parentId || undefined,
    menuCode: '',
    menuName: '',
    menuType: 'menu',
    routePath: '',
    icon: '',
    permissionCode: '',
    sortNo: 1
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
          await updateMenu(currentId.value, form)
          ElMessage.success('菜单修改成功')
        } else {
          await createMenu(form)
          ElMessage.success('菜单创建成功')
        }
        dialogVisible.value = false
        loadMenus()
      } catch (e) {}
      saving.value = false
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该菜单吗？如有子菜单一并删除且不可恢复！', '严重警告', {
    type: 'error',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteMenu(id)
      ElMessage.success('菜单已删除')
      loadMenus()
    } catch (e) {}
  }).catch(() => {})
}

// Convert flat menus to nested structure
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
  // clean children empty arrays
  const cleanTree = (nodes: any[]) => {
    nodes.forEach(node => {
      if (node.children.length === 0) {
        delete node.children
      } else {
        cleanTree(node.children)
      }
    })
  }
  cleanTree(roots)
  return roots
}

onMounted(() => {
  loadMenus()
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
</style>
