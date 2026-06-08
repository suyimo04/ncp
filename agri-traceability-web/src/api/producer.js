import request from '@/utils/request'

export const producerApi = {
  page: (params) => request.get('/producer/page', { params }),
  detail: (id) => request.get(`/producer/${id}`),
  create: (data) => request.post('/producer', data),
  update: (id, data) => request.put(`/producer/${id}`, data),
  remove: (id) => request.delete(`/producer/${id}`),
  audit: (id, data) => request.put(`/producer/${id}/audit`, data),
  current: () => request.get('/producer/current')
}
