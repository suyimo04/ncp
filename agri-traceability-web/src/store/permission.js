import { defineStore } from 'pinia'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    menus: []
  }),
  actions: {
    setMenus(menus) {
      this.menus = menus || []
    }
  }
})
