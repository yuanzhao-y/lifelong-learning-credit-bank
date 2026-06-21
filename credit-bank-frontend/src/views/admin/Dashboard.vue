<template>
  <div class="admin-dashboard-container">
    <div class="page-header glass-card">
      <h2 class="gradient-text">管理系统概览</h2>
      <p>实时监控学分银行系统的用户状态、申请审批及转换率</p>
    </div>

    <!-- Stat Cards Grid -->
    <el-row :gutter="20" class="stat-grid">
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon user"><el-icon><user-icon /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">总注册用户</span>
            <span class="stat-value">{{ stats?.userTotal || 0 }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon cert"><el-icon><medal /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">总认证申请</span>
            <span class="stat-value">{{ stats?.certTotal || 0 }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon rule"><el-icon><setting /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">生效转换规则</span>
            <span class="stat-value">{{ stats?.ruleTotal || 0 }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="6">
        <div class="stat-card glass-card">
          <div class="stat-icon audit"><el-icon><checked /></el-icon></div>
          <div class="stat-info">
            <span class="stat-label">已处理反馈</span>
            <span class="stat-value">{{ stats?.feedbackTotal || 0 }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ECharts Grid -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>用户注册增长趋势 (近7天)</h4>
          <div ref="lineChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>认定成果类型占比</h4>
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>本周成果审批量分布</h4>
          <div ref="barChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>转换规则分类统计</h4>
          <div ref="doughnutChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getStatisticsOverview } from '@/api/statistics'
import { User as UserIcon } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref<any>({
  userTotal: 1240,
  certTotal: 4890,
  ruleTotal: 45,
  feedbackTotal: 112
})

const lineChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
const doughnutChartRef = ref<HTMLDivElement>()

let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let doughnutChart: echarts.ECharts | null = null

const initCharts = () => {
  // Line Chart
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value, 'dark')
    lineChart.setOption({
      backgroundColor: 'transparent',
      grid: { top: 30, right: 20, bottom: 30, left: 40 },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 150, 180, 220, 200, 260, 310],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.4)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0)' }
          ])
        }
      }]
    })
  }

  // Pie Chart
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value, 'dark')
    pieChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      series: [{
        name: '成果类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 1048, name: '课程证书' },
          { value: 735, name: '毕业证书' },
          { value: 580, name: '学位证书' },
          { value: 484, name: '职业资格' },
          { value: 300, name: '技能等级' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // Bar Chart
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value, 'dark')
    barChart.setOption({
      backgroundColor: 'transparent',
      grid: { top: 30, right: 20, bottom: 30, left: 40 },
      xAxis: { type: 'category', data: ['课程类', '学历类', '学位类', '职业资格', '技能等级', '培训类'] },
      yAxis: { type: 'value' },
      series: [{
        data: [320, 240, 150, 80, 70, 110],
        type: 'bar',
        itemStyle: { color: '#06b6d4' }
      }]
    })
  }

  // Doughnut Chart
  if (doughnutChartRef.value) {
    doughnutChart = echarts.init(doughnutChartRef.value, 'dark')
    doughnutChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      series: [{
        name: '规则分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: [
          { value: 20, name: '高校互认规则' },
          { value: 15, name: '校企合作规则' },
          { value: 10, name: '行业认定规则' }
        ]
      }]
    })
  }
}

const resizeCharts = () => {
  lineChart?.resize()
  pieChart?.resize()
  barChart?.resize()
  doughnutChart?.resize()
}

onMounted(async () => {
  try {
    const res = await getStatisticsOverview()
    if (res) {
      stats.value = res
    }
  } catch (e) {}
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
  doughnutChart?.dispose()
})
</script>

<style scoped>
.admin-dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-grid {
  margin-top: 10px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}
.stat-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 22px;
}
.stat-icon.user { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; }
.stat-icon.cert { background: rgba(236, 72, 153, 0.1); color: #ec4899; }
.stat-icon.rule { background: rgba(59, 130, 246, 0.1); color: var(--accent-blue); }
.stat-icon.audit { background: rgba(16, 185, 129, 0.1); color: var(--success); }

.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.chart-row {
  margin-top: 10px;
}
.chart-card {
  padding: 24px;
  margin-bottom: 20px;
}
.chart-card h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}
.chart-box {
  height: 320px;
  width: 100%;
}
</style>
