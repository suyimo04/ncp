import { defineStore } from 'pinia'

const cachedTags = sessionStorage.getItem('agri_tags')

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarOpened: true,
    tagsViewList: normalizeTags(cachedTags)
  }),
  actions: {
    toggleSidebar() {
      this.sidebarOpened = !this.sidebarOpened
    },
    addTag(route) {
      if (!route.meta?.title || route.path === '/login') return
      if (this.tagsViewList.some((tag) => tag.path === route.path)) return
      this.tagsViewList.push({
        path: route.path,
        title: route.meta.title,
        affix: route.path === '/dashboard'
      })
      this.saveTags()
    },
    removeTag(path) {
      this.tagsViewList = this.tagsViewList.filter((tag) => tag.affix || tag.path !== path)
      this.saveTags()
    },
    closeOthers(path) {
      this.tagsViewList = this.tagsViewList.filter((tag) => tag.affix || tag.path === path)
      this.saveTags()
    },
    closeAll() {
      this.tagsViewList = this.tagsViewList.filter((tag) => tag.affix)
      this.saveTags()
    },
    saveTags() {
      sessionStorage.setItem('agri_tags', JSON.stringify(this.tagsViewList))
    }
  }
})

function normalizeTags(cachedTags) {
  const home = { path: '/dashboard', title: '首页驾驶舱', affix: true }
  if (!cachedTags) {
    return [home]
  }
  try {
    const tags = JSON.parse(cachedTags)
      .map((tag) => ({ ...tag, path: tag.path === '/' ? '/dashboard' : tag.path, affix: tag.path === '/' || tag.path === '/dashboard' }))
      .filter((tag, index, arr) => arr.findIndex((item) => item.path === tag.path) === index)
    return tags.some((tag) => tag.path === '/dashboard') ? tags : [home, ...tags]
  } catch {
    return [home]
  }
}
