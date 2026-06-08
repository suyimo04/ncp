<template>
  <div class="page-wrap">
    <AppCard title="合约配置">
      <el-form :model="form" label-width="130px" class="config-form">
        <el-form-item label="合约名称"><el-input v-model="form.contractName" /></el-form-item>
        <el-form-item label="合约地址"><el-input v-model="form.contractAddress" /></el-form-item>
        <el-form-item label="群组 ID"><el-input v-model="form.groupId" /></el-form-item>
        <el-form-item label="模拟模式"><el-switch v-model="mockMode" active-text="开启" inactive-text="关闭" /></el-form-item>
        <el-form-item label="合约 ABI"><el-input v-model="form.contractAbi" type="textarea" :rows="8" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存配置</el-button>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/common/AppCard.vue'
import { chainApi } from '@/api/chain'

const form = reactive({})
const mockMode = computed({
  get: () => form.mockMode === 'true',
  set: (value) => { form.mockMode = String(value) }
})

async function loadData() {
  Object.assign(form, await chainApi.config())
}

async function save() {
  await chainApi.saveConfig(form)
  ElMessage.success('配置已保存')
}

onMounted(loadData)
</script>

<style scoped>
.config-form {
  max-width: 760px;
}
</style>
