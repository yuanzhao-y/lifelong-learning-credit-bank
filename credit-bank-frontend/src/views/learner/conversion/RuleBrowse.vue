<template>
  <div class="rule-browse-container">
    <page-header title="转换规则库" description="查看已生效的学习成果转换规则、折算比例与适用条件" />

    <div class="filter-bar glass-card">
      <el-select v-model="query.sourceCatalogId" placeholder="源成果" clearable filterable @change="handleSearch">
        <el-option
          v-for="item in catalogs"
          :key="item.id"
          :label="item.outcomeName"
          :value="item.id"
        />
      </el-select>
      <el-select v-model="query.targetCatalogId" placeholder="目标成果" clearable filterable @change="handleSearch">
        <el-option
          v-for="item in catalogs"
          :key="item.id"
          :label="item.outcomeName"
          :value="item.id"
        />
      </el-select>
      <el-button @click="resetFilters">重置筛选</el-button>
    </div>

    <div class="table-card glass-card" v-loading="loading">
      <el-table :data="rules" style="width: 100%">
        <el-table-column prop="ruleCode" label="规则编号" width="150" />
        <el-table-column prop="ruleName" label="规则名称" min-width="180" />
        <el-table-column label="源成果" min-width="160">
          <template #default="{ row }">{{ catalogName(row.sourceCatalogId) }}</template>
        </el-table-column>
        <el-table-column label="目标成果" min-width="160">
          <template #default="{ row }">{{ catalogName(row.targetCatalogId) }}</template>
        </el-table-column>
        <el-table-column label="折算比例" width="110">
          <template #default="{ row }">{{ row.conversionRatio }}</template>
        </el-table-column>
        <el-table-column label="生效时间" width="170">
          <template #default="{ row }">{{ formatDate(row.effectiveAt || row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="success">已生效</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="rules.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无符合条件的转换规则" />
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadRules"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="转换规则详情" width="680px">
      <el-descriptions v-if="currentRule" :column="2" border>
        <el-descriptions-item label="规则编号">{{ currentRule.ruleCode }}</el-descriptions-item>
        <el-descriptions-item label="规则名称">{{ currentRule.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="源成果">{{ catalogName(currentRule.sourceCatalogId) }}</el-descriptions-item>
        <el-descriptions-item label="目标成果">{{ catalogName(currentRule.targetCatalogId) }}</el-descriptions-item>
        <el-descriptions-item label="折算比例">{{ currentRule.conversionRatio }}</el-descriptions-item>
        <el-descriptions-item label="生效时间">{{ formatDate(currentRule.effectiveAt || currentRule.updatedAt || currentRule.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="适用条件" :span="2">
          {{ currentRule.applicableCondition || '无额外限制' }}
        </el-descriptions-item>
        <el-descriptions-item label="规则说明" :span="2">
          {{ currentRule.description || '暂无说明' }}
        </el-descriptions-item>
        <el-descriptions-item label="专家意见" :span="2">
          规则已完成专家评审并进入生效库，评审记录见专家评审任务与后台规则管理。
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getPublicConversionRules, getPublicRuleDetail } from '@/api/conversion'
import { getPublicOutcomes } from '@/api/outcome'
import { formatDate } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const rules = ref<any[]>([])
const catalogs = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentRule = ref<any>(null)

const query = reactive({
  page: 1,
  size: 10,
  sourceCatalogId: undefined as number | undefined,
  targetCatalogId: undefined as number | undefined
})

const catalogName = (id: number) => catalogs.value.find(item => item.id === id)?.outcomeName || `目录 #${id}`

const loadCatalogs = async () => {
  const res: any = await getPublicOutcomes({ page: 1, size: 100 })
  catalogs.value = res.records || []
}

const loadRules = async () => {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      sourceCatalogId: query.sourceCatalogId,
      targetCatalogId: query.targetCatalogId
    }
    const res: any = await getPublicConversionRules(params)
    rules.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadRules()
}

const resetFilters = () => {
  query.sourceCatalogId = undefined
  query.targetCatalogId = undefined
  handleSearch()
}

const showDetail = async (id: number) => {
  currentRule.value = await getPublicRuleDetail(id)
  detailVisible.value = true
}

onMounted(async () => {
  await loadCatalogs()
  await loadRules()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.filter-bar .el-select {
  width: 220px;
}

.table-card {
  padding: 20px;
}

.empty-state {
  padding: 30px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
