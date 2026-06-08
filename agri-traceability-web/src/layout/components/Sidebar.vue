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

const menuGroups = [
  { id: 'home', menuName: '首页驾驶舱', icon: 'Odometer', path: '/' },
  { id: 'producer', menuName: '主体管理', icon: 'User', children: ['/producer'] },
  { id: 'batch', menuName: '批次管理', icon: 'Box', children: ['/batch'] },
  { id: 'report', menuName: '检测管理', icon: 'DocumentChecked', children: ['/test-report'] },
  { id: 'certificate', menuName: '合格证管理', icon: 'Stamp', children: ['/certificate', '/certificate/audit'] },
  { id: 'chain', menuName: '区块链管理', icon: 'Link', children: ['/chain/evidence', '/chain/verify', '/chain/contract'] },
  { id: 'system', menuName: '系统管理', icon: 'Setting', children: ['/system/user', '/system/role', '/system/menu'] }
]

const menus = computed(() => {
  const source = normalizeMenus(userStore.menus || [])
    .filter((item) => item.status !== 0 && item.menuType !== 3)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  if (!source.some((item) => item.menuType === 1)) {
    return buildGroupedMenus(source)
  }
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

function normalizeMenus(source) {
  const menuMap = {
    '/': { id: 1, menuName: '首页驾驶舱', icon: 'Odometer', menuType: 2, sortOrder: 1 },
    '/producer': { id: 11, menuName: '经营主体管理', icon: 'User', menuType: 2, sortOrder: 1 },
    '/batch': { id: 21, menuName: '农产品批次', icon: 'Box', menuType: 2, sortOrder: 1 },
    '/test-report': { id: 31, menuName: '检测报告', icon: 'DocumentChecked', menuType: 2, sortOrder: 1 },
    '/certificate': { id: 41, menuName: '合格证列表', icon: 'Stamp', menuType: 2, sortOrder: 1 },
    '/certificate/audit': { id: 42, menuName: '合格证审核', icon: 'Checked', menuType: 2, sortOrder: 2 },
    '/chain/evidence': { id: 51, menuName: '存证记录', icon: 'Link', menuType: 2, sortOrder: 1 },
    '/chain/verify': { id: 52, menuName: '链上核验', icon: 'Connection', menuType: 2, sortOrder: 2 },
    '/chain/contract': { id: 53, menuName: '合约配置', icon: 'Setting', menuType: 2, sortOrder: 3 },
    '/system/user': { id: 61, menuName: '用户管理', icon: 'UserFilled', menuType: 2, sortOrder: 1 },
    '/system/role': { id: 62, menuName: '角色管理', icon: 'Avatar', menuType: 2, sortOrder: 2 },
    '/system/menu': { id: 63, menuName: '菜单管理', icon: 'Menu', menuType: 2, sortOrder: 3 }
  }
  return source.map((item) => {
    const patch = menuMap[item.path]
    return patch ? { ...patch, ...item, menuName: patch.menuName, icon: patch.icon } : item
  })
}

function buildGroupedMenus(source) {
  const byPath = new Map(source.map((item) => [item.path, item]))
  return menuGroups
    .map((group, index) => {
      if (group.path) {
        return byPath.has(group.path) ? { ...byPath.get(group.path), ...group, sortOrder: index + 1 } : null
      }
      const children = group.children.map((path) => byPath.get(path)).filter(Boolean)
      return children.length ? { ...group, sortOrder: index + 1, children } : null
    })
    .filter(Boolean)
}
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
