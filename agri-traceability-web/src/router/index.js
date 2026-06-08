import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import Layout from '@/layout/index.vue'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/public/verify',
    component: () => import('@/views/public-verify/index.vue'),
    meta: { title: '公众验真', public: true }
  },
  {
    path: '/public/verify/:code',
    component: () => import('@/views/public-verify/index.vue'),
    meta: { title: '公众验真', public: true }
  },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页驾驶舱', icon: 'Odometer' } },
      { path: '/producer', component: () => import('@/views/producer/index.vue'), meta: { title: '经营主体管理', icon: 'User' } },
      { path: '/batch', component: () => import('@/views/batch/index.vue'), meta: { title: '农产品批次', icon: 'Box' } },
      { path: '/test-report', component: () => import('@/views/test-report/index.vue'), meta: { title: '检测报告', icon: 'DocumentChecked' } },
      { path: '/certificate', component: () => import('@/views/certificate/index.vue'), meta: { title: '合格证列表', icon: 'Stamp' } },
      { path: '/certificate/audit', component: () => import('@/views/certificate/audit.vue'), meta: { title: '合格证审核', icon: 'Checked', roles: ['ADMIN', 'REGULATOR'] } },
      { path: '/certificate/detail/:id', component: () => import('@/views/certificate/detail.vue'), meta: { title: '合格证详情', hidden: true } },
      { path: '/chain/evidence', component: () => import('@/views/chain/Evidence.vue'), meta: { title: '存证记录', icon: 'Link', roles: ['ADMIN', 'REGULATOR'] } },
      { path: '/chain/verify', component: () => import('@/views/chain/Verify.vue'), meta: { title: '链上核验', icon: 'Connection', roles: ['ADMIN', 'REGULATOR'] } },
      { path: '/chain/contract', component: () => import('@/views/chain/Contract.vue'), meta: { title: '合约配置', icon: 'Setting', roles: ['ADMIN'] } },
      { path: '/system/user', component: () => import('@/views/system/User.vue'), meta: { title: '用户管理', icon: 'UserFilled', roles: ['ADMIN'] } },
      { path: '/system/role', component: () => import('@/views/system/Role.vue'), meta: { title: '角色管理', icon: 'Avatar', roles: ['ADMIN'] } },
      { path: '/system/menu', component: () => import('@/views/system/Menu.vue'), meta: { title: '菜单管理', icon: 'Menu', roles: ['ADMIN'] } }
    ]
  },
  { path: '/dashboard', redirect: '/' },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/error/404.vue'), meta: { title: '页面不存在', public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

function canVisit(route, roles) {
  const needRoles = route.meta?.roles
  return !needRoles || needRoles.some((role) => roles.includes(role))
}

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
    return
  }
  if (!userStore.token && to.path !== '/login') {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }
  if (userStore.token && to.path === '/login') {
    next('/')
    return
  }
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchInfo()
    } catch {
      await userStore.logout()
      next('/login')
      return
    }
  }
  if (!canVisit(to, userStore.roles)) {
    next('/')
    return
  }
  const appStore = useAppStore()
  appStore.addTag(to)
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
