<template>
  <div class="outcome-list-container">
    <page-header title="学习成果目录" description="在这里导览、查询并选择您需要认证的学习成果" />

    <!-- Filters -->
    <div class="filter-bar glass-card">
      <el-input
        v-model="query.keyword"
        placeholder="搜索成果名称/代码..."
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><search /></el-icon>
        </template>
      </el-input>

      <el-select 
        v-model="query.outcomeType" 
        placeholder="成果类型" 
        clearable 
        class="filter-select"
        @change="handleSearch"
      >
        <el-option label="课程证书" value="course_cert" />
        <el-option label="毕业证书" value="diploma_cert" />
        <el-option label="学位证书" value="degree_cert" />
        <el-option label="职业资格" value="vocational_qualification" />
        <el-option label="技能等级" value="skill_level" />
        <el-option label="培训证书" value="training_cert" />
      </el-select>
    </div>

    <!-- Cards Grid -->
    <el-row :gutter="20" class="cards-grid" v-loading="loading">
      <el-col 
        v-for="item in outcomes" 
        :key="item.id" 
        :span="8" 
        :xs="24" 
        :sm="12" 
        :md="8"
      >
        <div class="outcome-card glass-card" @click="viewDetail(item.id)">
          <div class="card-tag-bar" :class="item.outcomeType"></div>
          <div class="card-content">
            <div class="outcome-type">{{ formatType(item.outcomeType) }}</div>
            <h4 class="outcome-name">{{ item.outcomeName }}</h4>
            <div class="outcome-code">成果代码: {{ item.outcomeCode }}</div>
            <p class="outcome-intro">{{ item.intro || '暂无介绍' }}</p>
            <el-divider />
            <div class="card-footer">
              <span class="base-credit">标准学分: <strong class="val">{{ item.baseCredit }}</strong></span>
              <el-button type="primary" size="small" class="gradient-btn">查看详情</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Empty -->
    <div v-if="outcomes.length === 0 && !loading" class="empty-state glass-card">
      <el-empty description="未找到匹配的学习成果" />
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadOutcomes"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicOutcomes } from '@/api/outcome'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const outcomes = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 9,
  outcomeType: '',
  keyword: ''
})

const formatType = (type: string) => {
  const map: Record<string, string> = {
    course_cert: '课程证书',
    diploma_cert: '毕业证书',
    degree_cert: '学位证书',
    vocational_qualification: '职业资格',
    skill_level: '技能等级',
    training_cert: '培训证书'
  }
  return map[type] || type
}

const loadOutcomes = async () => {
  loading.value = true
  try {
    const res: any = await getPublicOutcomes(query)
    outcomes.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
  loading.value = false
}

const handleSearch = () => {
  query.page = 1
  loadOutcomes()
}

const viewDetail = (id: number) => {
  router.push(`/outcomes/${id}`)
}

onMounted(() => {
  loadOutcomes()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.search-input {
  max-width: 320px;
}
.filter-select {
  width: 180px;
}
.cards-grid {
  margin-top: 10px;
}
.outcome-card {
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  cursor: pointer;
  height: 280px;
}
.card-tag-bar {
  height: 5px;
  width: 100%;
}
.card-tag-bar.course_cert { background: #3b82f6; }
.card-tag-bar.diploma_cert { background: #10b981; }
.card-tag-bar.degree_cert { background: #8b5cf6; }
.card-tag-bar.vocational_qualification { background: #f59e0b; }
.card-tag-bar.skill_level { background: #06b6d4; }
.card-tag-bar.training_cert { background: #ef4444; }

.card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: calc(100% - 5px);
}
.outcome-type {
  font-size: 11px;
  text-transform: uppercase;
  color: var(--text-muted);
  font-weight: 700;
  margin-bottom: 6px;
}
.outcome-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px 0;
  height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.outcome-code {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.outcome-intro {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  height: 60px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.el-divider {
  margin: 12px 0 !important;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}
.base-credit {
  font-size: 13px;
  color: var(--text-secondary);
}
.base-credit .val {
  color: var(--accent);
  font-size: 16px;
}
.empty-state {
  padding: 40px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
