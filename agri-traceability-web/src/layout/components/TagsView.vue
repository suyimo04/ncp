<template>
  <div class="tags">
    <div
      v-for="tag in appStore.tagsViewList"
      :key="tag.path"
      :class="['tag', { active: route.path === tag.path }]"
      @click="router.push(tag.path)"
      @contextmenu.prevent="openMenu($event, tag)"
    >
      <span>{{ tag.title }}</span>
      <el-icon v-if="!tag.affix" @click.stop="closeTag(tag.path)"><Close /></el-icon>
    </div>
    <ul v-if="menu.visible" class="context-menu" :style="{ left: `${menu.left}px`, top: `${menu.top}px` }">
      <li @click="refresh">刷新当前</li>
      <li @click="closeTag(menu.tag.path)">关闭当前</li>
      <li @click="closeOthers">关闭其他</li>
      <li @click="closeAll">关闭全部</li>
    </ul>
  </div>
</template>

<script setup>
import { nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const menu = reactive({ visible: false, left: 0, top: 0, tag: null })

function openMenu(event, tag) {
  menu.visible = true
  menu.left = event.clientX
  menu.top = event.clientY
  menu.tag = tag
  document.addEventListener('click', closeMenu, { once: true })
}

function closeMenu() {
  menu.visible = false
}

function closeTag(path) {
  appStore.removeTag(path)
  if (route.path === path) {
    const last = appStore.tagsViewList[appStore.tagsViewList.length - 1]
    router.push(last?.path || '/dashboard')
  }
}

function closeOthers() {
  appStore.closeOthers(menu.tag.path)
  router.push(menu.tag.path)
}

function closeAll() {
  appStore.closeAll()
  router.push('/dashboard')
}

function refresh() {
  const current = route.fullPath
  router.replace('/dashboard').then(() => nextTick(() => router.replace(current)))
}
</script>

<style scoped>
.tags {
  position: relative;
  display: flex;
  height: 36px;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  overflow-x: auto;
  background: #fff;
  border-bottom: 1px solid var(--agri-border);
}

.tag {
  display: inline-flex;
  height: 24px;
  align-items: center;
  gap: 6px;
  padding: 0 9px;
  border: 1px solid var(--agri-border);
  border-radius: 4px;
  color: var(--agri-text-secondary);
  font-size: 12px;
  white-space: nowrap;
  cursor: pointer;
}

.tag.active {
  color: #fff;
  background: var(--agri-primary);
  border-color: var(--agri-primary);
}

.context-menu {
  position: fixed;
  z-index: 20;
  width: 104px;
  padding: 4px 0;
  margin: 0;
  list-style: none;
  background: #fff;
  border: 1px solid var(--agri-border);
  border-radius: 6px;
  box-shadow: var(--agri-shadow);
}

.context-menu li {
  padding: 8px 12px;
  font-size: 13px;
  cursor: pointer;
}

.context-menu li:hover {
  color: var(--agri-primary);
  background: var(--agri-bg);
}
</style>
