import { defineStore } from 'pinia'

const cachedTags = sessionStorage.getItem('agri_tags')

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarOpened: true,
    tagsViewList: cachedTags
      ? JSON.parse(cachedTags)
      : [{ path: '/', title: '首页驾驶舱', affix: true }]
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
        affix: route.path === '/'
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
