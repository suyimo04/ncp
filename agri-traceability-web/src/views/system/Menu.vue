<template>
  <AppCard title="菜单管理">
    <AppTable :data="menus">
      <el-table-column prop="menuName" label="菜单名称" width="180" />
      <el-table-column prop="path" label="路由路径" width="180" />
      <el-table-column prop="component" label="组件" width="220" />
      <el-table-column prop="perms" label="权限标识" />
    </AppTable>
  </AppCard>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AppCard from '@/components/common/AppCard.vue'
import AppTable from '@/components/common/AppTable.vue'
import { systemApi } from '@/api/system'

const menus = ref([])
onMounted(async () => {
  menus.value = await systemApi.menus()
})
</script>
