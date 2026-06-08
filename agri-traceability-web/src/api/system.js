import request from '@/utils/request'

export const systemApi = {
  userPage: (params) => request.get('/system/user/page', { params }),
  createUser: (data) => request.post('/system/user', data),
  updateUser: (id, data) => request.put(`/system/user/${id}`, data),
  removeUser: (id) => request.delete(`/system/user/${id}`),
  roles: () => request.get('/system/role/list'),
  menus: () => request.get('/system/menu/list'),
  createMenu: (data) => request.post('/system/menu', data),
  updateMenu: (id, data) => request.put(`/system/menu/${id}`, data),
  removeMenu: (id) => request.delete(`/system/menu/${id}`)
}
