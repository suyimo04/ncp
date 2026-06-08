<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="合格证/批次/主体" clearable style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
    </div>
    <AppCard>
      <AppTable :data="pendingData" v-loading="loading">
        <el-table-column prop="batchCode" label="批次编号" min-width="160" />
        <el-table-column prop="reportCode" label="报告编号" min-width="160" />
        <el-table-column prop="producerName" label="申请主体" min-width="180" />
        <el-table-column prop="applyTime" label="申请时间" min-width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag type="warning">{{ row.issueStatus === 1 ? '待审核' : '已处理' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/certificate/detail/${row.id}`)">详情</el-button>
            <el-button v-if="row.issueStatus === 1" link type="success" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </AppTable>
    </AppCard>

    <el-dialog v-model="auditVisible" title="合格证审核" width="500px">
      <el-form :model="auditForm" label-width="90px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见"><el-input v-model="auditForm.opinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { certificateApi } from '@/api/certificate'

const loading = ref(false)
const tableData = ref([])
const query = reactive({ pageNum: 1, pageSize: 100, keyword: '' })
const auditVisible = ref(false)
const auditForm = reactive({ id: null, status: 1, opinion: '' })
const pendingData = computed(() => tableData.value.filter((item) => [1, 2, 3].includes(item.issueStatus)))

async function loadData() {
  loading.value = true
  try {
    const res = await certificateApi.page(query)
    tableData.value = res.records || []
  } finally {
    loading.value = false
  }
}

function openAudit(row) {
  auditForm.id = row.id
  auditForm.status = 1
  auditForm.opinion = ''
  auditVisible.value = true
}

async function submitAudit() {
  await certificateApi.audit(auditForm.id, auditForm)
  ElMessage.success('审核完成')
  auditVisible.value = false
  loadData()
}

onMounted(loadData)
</script>
