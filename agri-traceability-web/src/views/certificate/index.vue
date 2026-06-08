<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="合格证编号/批次/主体" clearable style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openApply">申请合格证</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="certificateCode" label="合格证编号" min-width="180">
          <template #default="{ row }">{{ row.certificateCode || '待签发' }}</template>
        </el-table-column>
        <el-table-column prop="batchCode" label="批次编号" min-width="150" />
        <el-table-column prop="reportCode" label="报告编号" min-width="150" />
        <el-table-column prop="producerName" label="经营主体" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusMap[row.issueStatus]?.type">{{ statusMap[row.issueStatus]?.label }}</el-tag></template>
        </el-table-column>
        <el-table-column label="上链" width="90">
          <template #default="{ row }"><el-tag :type="row.chainStatus === 2 ? 'success' : 'info'">{{ row.chainStatus === 2 ? '成功' : '未上链' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="expireTime" label="有效期至" width="120" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/certificate/detail/${row.id}`)">详情</el-button>
            <el-button v-permission="['ADMIN', 'REGULATOR']" v-if="row.issueStatus === 2" link type="success" @click="issue(row)">签发</el-button>
            <el-button v-permission="['ADMIN', 'REGULATOR']" v-if="row.issueStatus === 4" link type="success" @click="chain(row)">上链</el-button>
            <el-button v-permission="['ADMIN', 'REGULATOR']" v-if="row.issueStatus === 4 && row.revokeStatus === 0" link type="danger" @click="openRevoke(row)">作废</el-button>
          </template>
        </el-table-column>
      </AppTable>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </AppCard>

    <el-dialog v-model="applyVisible" title="申请合格证" width="560px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="批次">
          <el-select v-model="applyForm.batchId" filterable style="width: 100%" @change="loadReports">
            <el-option v-for="item in batches" :key="item.id" :label="`${item.batchCode} - ${item.productName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测报告">
          <el-select v-model="applyForm.reportId" filterable style="width: 100%">
            <el-option v-for="item in reports" :key="item.id" :label="`${item.reportCode} - ${item.conclusion}`" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="apply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="revokeVisible" title="作废合格证" width="480px">
      <el-form :model="revokeForm" label-width="90px">
        <el-form-item label="作废原因"><el-input v-model="revokeForm.revokeReason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button type="danger" @click="revoke">确认作废</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { batchApi } from '@/api/batch'
import { certificateApi } from '@/api/certificate'
import { reportApi } from '@/api/report'

const statusMap = {
  1: { label: '待审核', type: 'warning' },
  2: { label: '审核通过', type: 'success' },
  3: { label: '已驳回', type: 'danger' },
  4: { label: '已签发', type: 'success' }
}
const loading = ref(false)
const tableData = ref([])
const batches = ref([])
const reports = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const applyVisible = ref(false)
const revokeVisible = ref(false)
const applyForm = reactive({})
const revokeForm = reactive({ id: null, revokeReason: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await certificateApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function openApply() {
  Object.keys(applyForm).forEach((key) => delete applyForm[key])
  const res = await batchApi.page({ pageNum: 1, pageSize: 100 })
  batches.value = res.records || []
  reports.value = []
  applyVisible.value = true
}

async function loadReports() {
  const res = await reportApi.page({ pageNum: 1, pageSize: 100 })
  reports.value = (res.records || []).filter((item) => item.batchId === applyForm.batchId)
}

async function apply() {
  await certificateApi.apply(applyForm)
  ElMessage.success('申请已提交')
  applyVisible.value = false
  loadData()
}

async function issue(row) {
  await certificateApi.issue(row.id)
  ElMessage.success('合格证已签发')
  loadData()
}

async function chain(row) {
  await certificateApi.chain(row.id)
  ElMessage.success('已提交上链')
  loadData()
}

function openRevoke(row) {
  revokeForm.id = row.id
  revokeForm.revokeReason = ''
  revokeVisible.value = true
}

async function revoke() {
  await certificateApi.revoke(revokeForm.id, { revokeReason: revokeForm.revokeReason })
  ElMessage.success('合格证已作废')
  revokeVisible.value = false
  loadData()
}

function reset() {
  query.keyword = ''
  query.pageNum = 1
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
