<template>
  <div class="page-wrap">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="账号/姓名" clearable style="width: 240px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增用户</el-button>
    </div>
    <AppCard>
      <AppTable :data="tableData" v-loading="loading">
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" @click="openForm(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
      </AppTable>
    </AppCard>
    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" placeholder="留空则不修改" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="enabled" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="item in roles" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { systemApi } from '@/api/system'

const loading = ref(false)
const visible = ref(false)
const tableData = ref([])
const roles = ref([])
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive({ roleIds: [] })
const enabled = computed({ get: () => form.status !== 0, set: (val) => { form.status = val ? 1 : 0 } })

async function loadData() {
  loading.value = true
  try {
    const res = await systemApi.userPage(query)
    tableData.value = res.records || []
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  roles.value = await systemApi.roles()
}

function openForm(row = {}) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { status: 1, roleIds: [] }, row)
  visible.value = true
}

async function save() {
  if (form.id) await systemApi.updateUser(form.id, form)
  else await systemApi.createUser(form)
  ElMessage.success('保存成功')
  visible.value = false
  loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 ${row.username}？`, '提示')
  await systemApi.removeUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>
