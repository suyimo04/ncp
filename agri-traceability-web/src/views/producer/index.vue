<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="主体名称/信用代码/编号" clearable style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增主体</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="producerCode" label="主体编号" min-width="150" />
        <el-table-column prop="producerName" label="主体名称" min-width="180" />
        <el-table-column prop="producerType" label="类型" width="110" />
        <el-table-column prop="creditCode" label="信用代码" min-width="180" />
        <el-table-column prop="townName" label="所属乡镇" width="120" />
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.auditStatus]?.type">{{ statusMap[row.auditStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-permission="['ADMIN', 'REGULATOR']" link type="success" @click="openAudit(row)">审核</el-button>
            <el-button v-permission="'ADMIN'" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </AppTable>
      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        v-model:current-page="query.pageNum"
        @current-change="loadData"
      />
    </AppCard>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑经营主体' : '新增经营主体'" width="620px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="主体名称"><el-input v-model="form.producerName" /></el-form-item>
        <el-form-item label="主体类型">
          <el-select v-model="form.producerType" style="width: 100%">
            <el-option label="企业" value="企业" />
            <el-option label="合作社" value="合作社" />
            <el-option label="家庭农场" value="家庭农场" />
          </el-select>
        </el-form-item>
        <el-form-item label="信用代码"><el-input v-model="form.creditCode" /></el-form-item>
        <el-form-item label="法人"><el-input v-model="form.legalPerson" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="所属乡镇"><el-input v-model="form.townName" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="经营范围"><el-input v-model="form.businessScope" type="textarea" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="经营主体审核" width="460px">
      <el-form :model="auditForm" label-width="90px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见"><el-input v-model="auditForm.opinion" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { producerApi } from '@/api/producer'

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '通过', type: 'success' },
  2: { label: '驳回', type: 'danger' }
}
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const formVisible = ref(false)
const auditVisible = ref(false)
const form = reactive({})
const auditForm = reactive({ id: null, status: 1, opinion: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await producerApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.pageNum = 1
  loadData()
}

function openForm(row = {}) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row)
  formVisible.value = true
}

async function save() {
  if (form.id) {
    await producerApi.update(form.id, form)
  } else {
    await producerApi.create(form)
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  loadData()
}

function openAudit(row) {
  auditForm.id = row.id
  auditForm.status = 1
  auditForm.opinion = ''
  auditVisible.value = true
}

async function submitAudit() {
  await producerApi.audit(auditForm.id, auditForm)
  ElMessage.success('审核完成')
  auditVisible.value = false
  loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 ${row.producerName}？`, '提示')
  await producerApi.remove(row.id)
  ElMessage.success('删除成功')
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
