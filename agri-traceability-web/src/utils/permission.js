import { useUserStore } from '@/store/user'

export function hasRole(role) {
  const userStore = useUserStore()
  return userStore.roles.includes(role) || userStore.roles.includes('ADMIN')
}

export function hasAnyRole(roles) {
  const userStore = useUserStore()
  return userStore.roles.includes('ADMIN') || roles.some((role) => userStore.roles.includes(role))
}

export default {
  mounted(el, binding) {
    const need = binding.value
    if (!need) return
    const allow = Array.isArray(need) ? hasAnyRole(need) : hasRole(need)
    if (!allow) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
