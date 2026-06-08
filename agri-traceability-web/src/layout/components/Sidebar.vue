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
      <template v-for="item in menus" :key="item.id || item.path">
        <el-sub-menu v-if="item.children?.length" :index="String(item.id)">
          <template #title>
            <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
            <span>{{ item.menuName }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.id" :index="child.path">
            <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
            <template #title>{{ child.menuName }}</template>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
          <template #title>{{ item.menuName }}</template>
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

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()

const menus = computed(() => {
  const source = (userStore.menus || [])
    .filter((item) => item.status !== 0 && item.menuType !== 3)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  const map = new Map()
  source.forEach((item) => map.set(item.id, { ...item, children: [] }))
  const tree = []
  map.forEach((item) => {
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId).children.push(item)
    } else {
      tree.push(item)
    }
  })
  return tree
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
