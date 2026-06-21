<template>
  <div class="dict-list-container">
    <div class="page-header glass-card">
      <h2 class="gradient-text">数据字典管理</h2>
      <p>配置系统中的各类状态值、配置项目及映射关系字典缓存表</p>
    </div>

    <el-row :gutter="20">
      <!-- Dictionary definition (Left Panel) -->
      <el-col :span="10" :xs="24">
        <div class="dict-card glass-card">
          <div class="card-header">
            <h3>字典列表</h3>
            <el-button type="primary" size="small" @click="showDictAddDialog">
              新增字典
            </el-button>
          </div>
          
          <el-table 
            :data="dicts" 
            v-loading="dictsLoading" 
            highlight-current-row 
            @current-change="handleDictSelect"
          >
            <el-table-column prop="dictCode" label="字典编码" width="120" />
            <el-table-column prop="dictName" label="字典名称" width="120" />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="showDictEditDialog(row)">编辑</el-button>
                <el-button link type="danger" @click.stop="handleDictDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- Dictionary Items (Right Panel) -->
      <el-col :span="14" :xs="24">
        <div class="items-card glass-card" v-loading="itemsLoading">
          <div class="card-header">
            <h3>字典项明细：<span class="highlight">{{ selectedDict?.dictName || '请选择字典' }}</span></h3>
            <el-button 
              type="success" 
              size="small" 
              :disabled="!selectedDict" 
              @click="showItemAddDialog"
            >
              添加字典项
            </el-button>
          </div>

          <el-table :data="dictItems">
            <el-table-column prop="itemCode" label="取值 (Code)" width="120" />
            <el-table-column prop="itemName" label="描述 (Name)" width="150" />
            <el-table-column prop="description" label="说明" min-width="120" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="showItemEditDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="handleItemDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- Dict Add/Edit Dialog -->
    <el-dialog v-model="dictDialogVisible" :title="isDictEdit ? '编辑字典定义' : '新增字典定义'" width="30%">
      <el-form :model="dictForm" ref="dictFormRef" :rules="dictRules" label-position="top">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="dictForm.dictCode" :disabled="isDictEdit" placeholder="如：user_status" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictForm.dictName" placeholder="中文展示标题" />
        </el-form-item>
        <el-form-item label="描述说明" prop="description">
          <el-input v-model="dictForm.description" type="textarea" placeholder="关于该字典的详细描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dictSaving" @click="handleDictSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Item Add/Edit Dialog -->
    <el-dialog v-model="itemDialogVisible" :title="isItemEdit ? '编辑字典项' : '添加字典项'" width="30%">
      <el-form :model="itemForm" ref="itemFormRef" :rules="itemRules" label-position="top">
        <el-form-item label="字典项值 (Item Code)" prop="itemCode">
          <el-input v-model="itemForm.itemCode" placeholder="如：enabled, disabled" />
        </el-form-item>
        <el-form-item label="展示名称 (Item Name)" prop="itemName">
          <el-input v-model="itemForm.itemName" placeholder="如：启用, 停用" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortNo">
          <el-input-number v-model="itemForm.sortNo" :min="1" />
        </el-form-item>
        <el-form-item label="关于说明" prop="description">
          <el-input v-model="itemForm.description" placeholder="字典项的具体备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSaving" @click="handleItemSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getDicts,
  createDict,
  updateDict,
  deleteDict,
  getDictItems,
  createDictItem,
  updateDictItem,
  deleteDictItem
} from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const dictsLoading = ref(false)
const dicts = ref<any[]>([])
const selectedDict = ref<any>(null)

const itemsLoading = ref(false)
const dictItems = ref<any[]>([])

// Dict Forms
const dictDialogVisible = ref(false)
const isDictEdit = ref(false)
const dictFormRef = ref<FormInstance>()
const dictSaving = ref(false)
const dictForm = reactive({
  dictCode: '',
  dictName: '',
  description: ''
})

const dictRules = {
  dictCode: [{ required: true, message: '字典编码不能为空', trigger: 'blur' }],
  dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }]
}

// Item Forms
const itemDialogVisible = ref(false)
const isItemEdit = ref(false)
const currentItemId = ref<number | null>(null)
const itemFormRef = ref<FormInstance>()
const itemSaving = ref(false)
const itemForm = reactive({
  itemCode: '',
  itemName: '',
  sortNo: 1,
  description: ''
})

const itemRules = {
  itemCode: [{ required: true, message: '键值不能为空', trigger: 'blur' }],
  itemName: [{ required: true, message: '名称描述不能为空', trigger: 'blur' }]
}

const loadDicts = async () => {
  dictsLoading.value = true
  try {
    const res = await getDicts()
    dicts.value = res || []
  } catch (e) {}
  dictsLoading.value = false
}

const handleDictSelect = async (row: any) => {
  selectedDict.value = row
  if (!row) {
    dictItems.value = []
    return
  }
  itemsLoading.value = true
  try {
    const res = await getDictItems(row.id)
    dictItems.value = res || []
  } catch (e) {}
  itemsLoading.value = false
}

const showDictAddDialog = () => {
  isDictEdit.value = false
  Object.assign(dictForm, { dictCode: '', dictName: '', description: '' })
  dictDialogVisible.value = true
}

const showDictEditDialog = (row: any) => {
  isDictEdit.value = true
  Object.assign(dictForm, row)
  dictDialogVisible.value = true
}

const handleDictSave = async () => {
  if (!dictFormRef.value) return
  await dictFormRef.value.validate(async (valid) => {
    if (valid) {
      dictSaving.value = true
      try {
        if (isDictEdit.value) {
          // Find dict ID
          const dictId = dicts.value.find(d => d.dictCode === dictForm.dictCode)?.id
          await updateDict(dictId, dictForm)
          ElMessage.success('字典保存成功')
        } else {
          await createDict(dictForm)
          ElMessage.success('创建字典成功')
        }
        dictDialogVisible.value = false
        loadDicts()
      } catch (e) {}
      dictSaving.value = false
    }
  })
}

const handleDictDelete = (id: number) => {
  ElMessageBox.confirm('删除字典将同步清空其对应的全部字典项，确定要删除吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDict(id)
      ElMessage.success('字典已删除')
      loadDicts()
      selectedDict.value = null
      dictItems.value = []
    } catch (e) {}
  }).catch(() => {})
}

// Items Handling
const showItemAddDialog = () => {
  isItemEdit.value = false
  currentItemId.value = null
  Object.assign(itemForm, { itemCode: '', itemName: '', sortNo: 1, description: '' })
  itemDialogVisible.value = true
}

const showItemEditDialog = (row: any) => {
  isItemEdit.value = true
  currentItemId.value = row.id
  Object.assign(itemForm, row)
  itemDialogVisible.value = true
}

const handleItemSave = async () => {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate(async (valid) => {
    if (valid) {
      itemSaving.value = true
      try {
        if (isItemEdit.value && currentItemId.value) {
          await updateItem(currentItemId.value, itemForm)
          ElMessage.success('更新字典项成功')
        } else {
          await createDictItem(selectedDict.value.id, itemForm)
          ElMessage.success('字典项添加成功')
        }
        itemDialogVisible.value = false
        handleDictSelect(selectedDict.value)
      } catch (e) {}
      itemSaving.value = false
    }
  })
}

// updateItem helper (mapped to correct put API)
const updateItem = async (id: number, data: any) => {
  return updateDictItem(id, data)
}

const handleItemDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除此条明细项吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictItem(id)
      ElMessage.success('删除明细成功')
      handleDictSelect(selectedDict.value)
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  loadDicts()
})
</script>

<style scoped>
.dict-card, .items-card {
  padding: 20px;
  min-height: 480px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.highlight {
  color: var(--accent);
}
</style>
