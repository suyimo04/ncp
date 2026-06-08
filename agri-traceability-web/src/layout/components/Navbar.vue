<template>
  <header class="navbar">
    <div class="left">
      <el-icon class="fold" @click="appStore.toggleSidebar">
        <component :is="appStore.sidebarOpened ? 'Fold' : 'Expand'" />
      </el-icon>
      <Breadcrumb />
    </div>
    <div class="right">
      <el-tooltip content="公众验真页" placement="bottom">
        <el-button :icon="Iphone" circle @click="router.push('/public/verify')" />
      </el-tooltip>
      <el-dropdown>
        <span class="user">
          <span class="avatar">{{ userStore.userInfo?.realName?.slice(0, 1) || '用' }}</span>
          {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>{{ roleText }}</el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Iphone } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import Breadcrumb from '@/components/common/Breadcrumb.vue'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const roleText = computed(() => userStore.roles.join(' / ') || '暂无角色')

async function handleLogout() {
  await userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  height: 56px;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--agri-header-bg);
  border-bottom: 1px solid var(--agri-border);
}

.left,
.right,
.user {
  display: flex;
  align-items: center;
  gap: 14px;
}

.fold {
  color: var(--agri-text-secondary);
  cursor: pointer;
  font-size: 20px;
}

.fold:hover {
  color: var(--agri-primary);
}

.user {
  cursor: pointer;
  font-size: 14px;
  color: var(--agri-text);
}

.avatar {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--agri-primary);
  color: #fff;
}
</style>
