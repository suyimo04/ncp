export const menuPresets = [
  { menuName: '首页驾驶舱', icon: 'Odometer', path: '/dashboard', component: 'dashboard/index', perms: 'dashboard:view' },
  { menuName: '经营主体管理', icon: 'User', path: '/producer', component: 'producer/index', perms: 'producer:view' },
  { menuName: '农产品批次', icon: 'Box', path: '/batch', component: 'batch/index', perms: 'batch:view' },
  { menuName: '检测报告', icon: 'DocumentChecked', path: '/test-report', component: 'test-report/index', perms: 'report:view' },
  { menuName: '合格证列表', icon: 'Stamp', path: '/certificate', component: 'certificate/index', perms: 'certificate:view' },
  { menuName: '合格证审核', icon: 'Checked', path: '/certificate/audit', component: 'certificate/audit', perms: 'certificate:audit' },
  { menuName: '存证记录', icon: 'Link', path: '/chain/evidence', component: 'chain/Evidence', perms: 'chain:evidence:view' },
  { menuName: '链上核验', icon: 'Connection', path: '/chain/verify', component: 'chain/Verify', perms: 'chain:verify' },
  { menuName: '合约配置', icon: 'Setting', path: '/chain/contract', component: 'chain/Contract', perms: 'chain:contract' },
  { menuName: '用户管理', icon: 'UserFilled', path: '/system/user', component: 'system/User', perms: 'system:user' },
  { menuName: '角色管理', icon: 'Avatar', path: '/system/role', component: 'system/Role', perms: 'system:role' },
  { menuName: '菜单管理', icon: 'Menu', path: '/system/menu', component: 'system/Menu', perms: 'system:menu' }
]

export const menuGroups = [
  { id: 'home', menuName: '首页驾驶舱', icon: 'Odometer', path: '/dashboard' },
  { id: 'producer', menuName: '主体管理', icon: 'User', children: ['/producer'] },
  { id: 'batch', menuName: '批次管理', icon: 'Box', children: ['/batch'] },
  { id: 'report', menuName: '检测管理', icon: 'DocumentChecked', children: ['/test-report'] },
  { id: 'certificate', menuName: '合格证管理', icon: 'Stamp', children: ['/certificate', '/certificate/audit'] },
  { id: 'chain', menuName: '区块链管理', icon: 'Link', children: ['/chain/evidence', '/chain/verify', '/chain/contract'] },
  { id: 'system', menuName: '系统管理', icon: 'Setting', children: ['/system/user', '/system/role', '/system/menu'] }
]

export const menuPatchMap = menuPresets.reduce((map, item, index) => {
  map[item.path] = { id: index + 1, ...item, menuType: 2, sortOrder: index + 1 }
  return map
}, {
  '/': { id: 1, menuName: '首页驾驶舱', icon: 'Odometer', path: '/dashboard', component: 'dashboard/index', perms: 'dashboard:view', menuType: 2, sortOrder: 1 }
})

export function normalizeMenuPath(path) {
  return path === '/' ? '/dashboard' : path
}
