<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="业务编号/交易哈希" clearable style="width: 280px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="businessType" label="业务类型" width="110" />
        <el-table-column prop="businessCode" label="业务编号" min-width="160" />
        <el-table-column label="存证哈希" min-width="220"><template #default="{ row }"><div class="hash-text">{{ row.evidenceHash }}</div></template></el-table-column>
        <el-table-column label="交易哈希" min-width="220"><template #default="{ row }"><div class="hash-text">{{ row.txHash || '-' }}</div></template></el-table-column>
        <el-table-column prop="blockNumber" label="区块高度" width="110" />
        <el-table-column label="上链状态" width="100">
          <template #default="{ row }"><el-tag :type="row.chainStatus === 2 ? 'success' : row.chainStatus === 3 ? 'danger' : 'warning'">{{ row.chainStatus === 2 ? '成功' : row.chainStatus === 3 ? '失败' : '处理中' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="核验状态" width="100">
          <template #default="{ row }"><el-tag :type="row.verifyStatus === 1 ? 'success' : row.verifyStatus === 2 ? 'danger' : 'info'">{{ row.verifyStatus === 1 ? '通过' : row.verifyStatus === 2 ? '失败' : '未核验' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="primary" @click="verify(row)">重新核验</el-button></template>
        </el-table-column>
      </AppTable>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { chainApi } from '@/api/chain'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await chainApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function verify(row) {
  await chainApi.verify(row.id)
  ElMessage.success('核验完成')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
