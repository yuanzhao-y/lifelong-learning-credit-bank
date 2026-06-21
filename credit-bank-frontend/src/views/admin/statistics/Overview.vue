<template>
  <div class="statistics-overview-container">
    <page-header title="系统数据分析" description="按特定时间周期分析用户的增长速率与转换率" />

    <div class="filter-bar glass-card">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD"
        @change="handleDateChange"
      />
    </div>

    <el-row :gutter="20" class="charts-wrapper" v-loading="loading">
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>用户活跃变化曲线</h4>
          <div ref="lineChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12" :xs="24">
        <div class="chart-card glass-card">
          <h4>认定学分总额度占比</h4>
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getStatisticsOverview } from '@/api/statistics'
import * as echarts from 'echarts'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const dateRange = ref<any>([])

const lineChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()

let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const initCharts = () => {
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value, 'dark')
    lineChart.setOption({
      backgroundColor: 'transparent',
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
      yAxis: { type: 'value' },
      series: [{
        data: [1500, 2300, 2240, 2180, 1350, 1470],
        type: 'line',
        itemStyle: { color: '#8b5cf6' }
      }]
    })
  }

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value, 'dark')
    pieChart.setOption({
      backgroundColor: 'transparent',
      series: [{
        type: 'pie',
        radius: '55%',
        data: [
          { value: 50, name: '高校学分互认' },
          { value: 30, name: '非学历成果认定' },
          { value: 20, name: '职业标准抵扣' }
        ]
      }]
    })
  }
}

const handleDateChange = async (val: any) => {
  loading.value = true
  try {
    const params = val ? { start: val[0], end: val[1] } : undefined
    await getStatisticsOverview(params)
    // redraw
  } catch (e) {}
  loading.value = false
}

const resizeCharts = () => {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.filter-bar {
  padding: 16px 20px;
  margin-bottom: 24px;
}
.chart-card {
  padding: 24px;
  margin-bottom: 20px;
}
.chart-card h4 {
  font-size: 15px;
  margin-bottom: 16px;
}
.chart-box {
  height: 320px;
  width: 100%;
}
</style>
