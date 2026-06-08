<template>
  <div class="page-wrap">
    <AppCard title="链上核验">
      <div class="verify-box">
        <el-input v-model="keyword" placeholder="输入业务编号或交易哈希" clearable />
        <el-button type="primary" :icon="Search" @click="loadData">查询存证记录</el-button>
      </div>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="businessType" label="类型" width="110" />
        <el-table-column prop="businessCode" label="业务编号" min-width="160" />
        <el-table-column label="交易哈希" min-width="220"><template #default="{ row }"><div class="hash-text">{{ row.txHash || '-' }}</div></template></el-table-column>
        <el-table-column label="核验状态" width="100">
          <template #default="{ row }"><el-tag :type="row.verifyStatus === 1 ? 'success' : row.verifyStatus === 2 ? 'danger' : 'info'">{{ row.verifyStatus === 1 ? '通过' : row.verifyStatus === 2 ? '失败' : '未核验' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="verify(row)">核验</el-button></template></el-table-column>
      </AppTable>
    </AppCard>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { chainApi } from '@/api/chain'

const keyword = ref('')
const loading = ref(false)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await chainApi.page({ pageNum: 1, pageSize: 20, keyword: keyword.value })
    tableData.value = res.records || []
  } finally {
    loading.value = false
  }
}

async function verify(row) {
  await chainApi.verify(row.id)
  ElMessage.success('核验完成')
  loadData()
}
</script>

<style scoped>
.verify-box {
  display: flex;
  gap: 10px;
  max-width: 520px;
  margin-bottom: 16px;
}
</style>
