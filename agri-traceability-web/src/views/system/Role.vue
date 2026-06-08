<template>
  <AppCard title="角色管理">
    <AppTable :data="roles">
      <el-table-column prop="roleCode" label="角色编码" width="180" />
      <el-table-column prop="roleName" label="角色名称" width="180" />
      <el-table-column prop="description" label="说明" />
    </AppTable>
  </AppCard>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { systemApi } from '@/api/system'

const roles = ref([])
onMounted(async () => {
  roles.value = await systemApi.roles()
})
</script>
