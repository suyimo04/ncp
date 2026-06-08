<template>
  <div class="page-wrap">
    <div class="stats">
      <AppCard v-for="item in stats" :key="item.title" class="stat-card">
        <div class="stat-icon" :style="{ color: item.color, background: item.bg }">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <p>{{ item.title }}</p>
          <strong>{{ item.value }}</strong>
        </div>
      </AppCard>
    </div>
    <div class="dashboard-grid">
      <AppCard title="近七日签发趋势">
        <VChart class="chart" :option="lineOption" autoresize />
      </AppCard>
      <AppCard title="检测结论占比">
        <VChart class="chart" :option="pieOption" autoresize />
      </AppCard>
    </div>
    <div class="dashboard-grid">
      <AppCard title="最新区块链存证">
        <div v-for="item in records" :key="item.hash" class="record">
          <el-tag type="success">{{ item.type }}</el-tag>
          <span class="hash-text">{{ item.hash }}</span>
          <span>{{ item.time }}</span>
        </div>
      </AppCard>
      <AppCard title="待处理事项">
        <div v-for="item in todo" :key="item.text" class="todo">
          <span>{{ item.text }}</span>
          <el-tag :type="item.type">{{ item.count }}</el-tag>
        </div>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import AppCard from '@/components/common/AppCard.vue'

use([CanvasRenderer, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent])

const stats = [
  { title: '入驻主体', value: 128, icon: 'User', color: '#1F8A5B', bg: '#e8f3ee' },
  { title: '批次建档', value: 1024, icon: 'Box', color: '#3A7CA5', bg: '#e8f1f6' },
  { title: '检测报告', value: 856, icon: 'DocumentChecked', color: '#E6A23C', bg: '#fff5e5' },
  { title: '签发合格证', value: 742, icon: 'Stamp', color: '#2EAD67', bg: '#e9f7ef' },
  { title: '链上存证', value: 2622, icon: 'Link', color: '#D94841', bg: '#fdebea' }
]

const lineOption = {
  color: ['#1F8A5B'],
  tooltip: { trigger: 'axis' },
  grid: { left: 30, right: 16, top: 30, bottom: 28 },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value' },
  series: [{ name: '签发量', type: 'line', smooth: true, areaStyle: {}, data: [42, 56, 49, 70, 83, 65, 96] }]
}

const pieOption = {
  color: ['#2EAD67', '#D94841', '#E6A23C'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{ type: 'pie', radius: ['42%', '68%'], data: [{ value: 830, name: '合格' }, { value: 12, name: '不合格' }, { value: 14, name: '异常复检' }] }]
}

const records = [
  { type: '合格证', hash: '0x8f7a9b2c3d4e5f6a7b8c', time: '10 分钟前' },
  { type: '检测报告', hash: '0x3c4d5e6f7a8b9c0d1e2f', time: '1 小时前' },
  { type: '批次', hash: '0x1a2b3c4d5e6f7a8b9c0d', time: '2 小时前' }
]

const todo = [
  { text: '待审核经营主体', count: 6, type: 'warning' },
  { text: '待审核合格证', count: 12, type: 'danger' },
  { text: '上链失败记录', count: 2, type: 'info' }
]
</script>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(150px, 1fr));
  gap: 14px;
}

.stat-card :deep(.app-card) {
  min-height: 96px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  display: flex;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 24px;
}

.stat-card p {
  margin: 0 0 5px;
  color: var(--agri-text-secondary);
  font-size: 13px;
}

.stat-card strong {
  font-size: 25px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}

.chart {
  height: 300px;
}

.record,
.todo {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--agri-border);
  font-size: 14px;
}

@media (max-width: 1100px) {
  .stats,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
