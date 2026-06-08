<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="批次编号/产品/主体" clearable style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增批次</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="batchCode" label="批次编号" min-width="160" />
        <el-table-column prop="productName" label="产品名称" width="130" />
        <el-table-column prop="producerName" label="经营主体" min-width="180" />
        <el-table-column prop="harvestTime" label="采收日期" width="120" />
        <el-table-column label="重量" width="100">
          <template #default="{ row }">{{ row.batchWeight }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="质量状态" width="100">
          <template #default="{ row }">
            <el-tag :type="qualityMap[row.qualityStatus]?.type">{{ qualityMap[row.qualityStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批次哈希" min-width="180">
          <template #default="{ row }"><div class="hash-text">{{ row.batchHash || '未生成' }}</div></template>
        </el-table-column>
        <el-table-column label="上链状态" width="100">
          <template #default="{ row }">
            <el-tag :type="chainMap[row.chainStatus]?.type">{{ chainMap[row.chainStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="primary" @click="makeHash(row)">生成哈希</el-button>
            <el-button link type="success" @click="chain(row)">上链</el-button>
          </template>
        </el-table-column>
      </AppTable>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </AppCard>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑批次' : '新增批次'" width="620px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="经营主体">
          <el-select v-model="form.producerId" filterable style="width: 100%">
            <el-option v-for="item in producers" :key="item.id" :label="item.producerName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="产品类别"><el-input v-model="form.productCategory" /></el-form-item>
        <el-form-item label="产地"><el-input v-model="form.originAddress" /></el-form-item>
        <el-form-item label="采收日期"><el-date-picker v-model="form.harvestTime" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="批次重量">
          <el-input-number v-model="form.batchWeight" :min="0.01" :precision="2" style="width: 220px" />
          <el-input v-model="form.unit" style="width: 90px; margin-left: 10px" />
        </el-form-item>
        <el-form-item label="预计销售"><el-date-picker v-model="form.expectedSaleTime" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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
import { producerApi } from '@/api/producer'

const qualityMap = { 0: { label: '未知', type: 'info' }, 1: { label: '合格', type: 'success' }, 2: { label: '不合格', type: 'danger' } }
const chainMap = { 0: { label: '未上链', type: 'info' }, 1: { label: '上链中', type: 'warning' }, 2: { label: '成功', type: 'success' }, 3: { label: '失败', type: 'danger' } }
const loading = ref(false)
const tableData = ref([])
const producers = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const formVisible = ref(false)
const form = reactive({ unit: 'kg' })

async function loadData() {
  loading.value = true
  try {
    const res = await batchApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadProducers() {
  const res = await producerApi.page({ pageNum: 1, pageSize: 100 })
  producers.value = res.records || []
}

function reset() {
  query.keyword = ''
  query.pageNum = 1
  loadData()
}

function openForm(row = {}) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { unit: 'kg' }, row)
  formVisible.value = true
}

async function save() {
  if (form.id) await batchApi.update(form.id, form)
  else await batchApi.create(form)
  ElMessage.success('保存成功')
  formVisible.value = false
  loadData()
}

async function makeHash(row) {
  await batchApi.hash(row.id)
  ElMessage.success('批次哈希已生成')
  loadData()
}

async function chain(row) {
  await batchApi.chain(row.id)
  ElMessage.success('已提交上链')
  loadData()
}

onMounted(() => {
  loadData()
  loadProducers()
})
</script>

<style scoped>
.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
