<template>
  <aside :class="['sidebar', { collapsed: !appStore.sidebarOpened }]">
    <div class="logo">
      <el-icon :size="24"><Platform /></el-icon>
      <span v-if="appStore.sidebarOpened">农产存证追溯系统</span>
    </div>
    <el-menu
      :default-active="route.path"
      :collapse="!appStore.sidebarOpened"
      background-color="#173B2F"
      text-color="#bfcbd9"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <template v-for="item in menus" :key="item.path">
        <el-menu-item v-if="!item.meta.hidden" :index="item.path">
          <el-icon><component :is="item.meta.icon || 'Menu'" /></el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { constantRoutes } from '@/router'

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()

const menus = computed(() => {
  const layout = constantRoutes.find((item) => item.path === '/')
  return layout.children.filter((item) => {
    if (item.meta.hidden) return false
    if (!item.meta.roles) return true
    return item.meta.roles.some((role) => userStore.roles.includes(role))
  })
})
</script>

<style scoped>
.sidebar {
  width: 220px;
  height: 100%;
  overflow: hidden;
  color: #fff;
  background: var(--agri-sidebar-bg);
  transition: width 0.2s;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  display: flex;
  height: 56px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
}

.logo .el-icon {
  color: var(--agri-success);
}

.sidebar-menu {
  border-right: 0;
}

:deep(.el-menu-item.is-active) {
  background: var(--agri-primary) !important;
}

:deep(.el-menu-item:hover) {
  background: #1a4436 !important;
}
</style>
