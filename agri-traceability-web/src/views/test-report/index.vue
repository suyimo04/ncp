<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="报告编号/批次/机构" clearable style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增报告</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="reportCode" label="报告编号" min-width="160" />
        <el-table-column prop="batchCode" label="批次编号" min-width="160" />
        <el-table-column prop="testAgency" label="检测机构" min-width="150" />
        <el-table-column prop="testDate" label="检测日期" width="120" />
        <el-table-column label="结论" width="90">
          <template #default="{ row }"><el-tag :type="row.conclusion === '合格' ? 'success' : 'danger'">{{ row.conclusion }}</el-tag></template>
        </el-table-column>
        <el-table-column label="文件哈希" min-width="180">
          <template #default="{ row }"><div class="hash-text">{{ row.reportFileHash || '未生成' }}</div></template>
        </el-table-column>
        <el-table-column label="上链状态" width="100">
          <template #default="{ row }"><el-tag :type="row.chainStatus === 2 ? 'success' : row.chainStatus === 3 ? 'danger' : 'info'">{{ row.chainStatus === 2 ? '成功' : row.chainStatus === 3 ? '失败' : '未上链' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="makeHash(row)">生成哈希</el-button>
            <el-button link type="success" @click="chain(row)">上链</el-button>
            <el-button v-if="row.reportFileUrl" link type="info" @click="windowOpen(row.reportFileUrl)">查看文件</el-button>
          </template>
        </el-table-column>
      </AppTable>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </AppCard>

    <el-dialog v-model="formVisible" title="新增检测报告" width="650px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="关联批次">
          <el-select v-model="form.batchId" filterable style="width: 100%">
            <el-option v-for="item in batches" :key="item.id" :label="`${item.batchCode} - ${item.productName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测机构"><el-input v-model="form.testAgency" /></el-form-item>
        <el-form-item label="检测类型"><el-input v-model="form.testType" /></el-form-item>
        <el-form-item label="检测日期"><el-date-picker v-model="form.testDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="检测项目"><el-input v-model="form.testItems" type="textarea" /></el-form-item>
        <el-form-item label="检测结果"><el-input v-model="form.testResult" type="textarea" /></el-form-item>
        <el-form-item label="检测结论">
          <el-radio-group v-model="form.conclusion">
            <el-radio label="合格" />
            <el-radio label="不合格" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报告文件">
          <el-upload :auto-upload="false" :show-file-list="true" :limit="1" :on-change="handleFile">
            <el-button>选择 PDF/图片</el-button>
          </el-upload>
          <div v-if="form.reportFileHash" class="hash-text upload-hash">{{ form.reportFileHash }}</div>
        </el-form-item>
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
import { reportApi } from '@/api/report'

const loading = ref(false)
const tableData = ref([])
const batches = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const formVisible = ref(false)
const form = reactive({ conclusion: '合格' })
const selectedFile = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await reportApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadBatches() {
  const res = await batchApi.page({ pageNum: 1, pageSize: 100 })
  batches.value = res.records || []
}

function reset() {
  query.keyword = ''
  query.pageNum = 1
  loadData()
}

function openForm() {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { conclusion: '合格' })
  selectedFile.value = null
  formVisible.value = true
}

function handleFile(uploadFile) {
  selectedFile.value = uploadFile.raw
}

async function save() {
  if (selectedFile.value) {
    const file = await reportApi.upload(selectedFile.value)
    form.reportFileUrl = file.url
    form.reportFileHash = file.hash
  }
  await reportApi.create(form)
  ElMessage.success('保存成功')
  formVisible.value = false
  loadData()
}

async function makeHash(row) {
  await reportApi.hash(row.id)
  ElMessage.success('报告哈希已生成')
  loadData()
}

async function chain(row) {
  await reportApi.chain(row.id)
  ElMessage.success('已提交上链')
  loadData()
}

function windowOpen(url) {
  window.open(url, '_blank')
}

onMounted(() => {
  loadData()
  loadBatches()
})
</script>

<style scoped>
.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.upload-hash {
  margin-top: 8px;
}
</style>
