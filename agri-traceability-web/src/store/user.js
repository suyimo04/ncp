import { defineStore } from 'pinia'
import Cookies from 'js-cookie'
import { getInfo, login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get('agri_token') || '',
    userInfo: null,
    roles: [],
    producerId: null,
    menus: []
  }),
  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.token
      Cookies.set('agri_token', res.token)
      await this.fetchInfo()
    },
    async fetchInfo() {
      const res = await getInfo()
      this.userInfo = res.userInfo
      this.roles = res.roles || []
      this.menus = res.menus || []
      this.producerId = res.producerId
      return res
    },
    async logout() {
      if (this.token) {
        logoutApi().catch(() => {})
      }
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.menus = []
      this.producerId = null
      Cookies.remove('agri_token')
    }
  }
})
