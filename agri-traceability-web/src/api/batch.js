import request from '@/utils/request'

export const batchApi = {
  page: (params) => request.get('/batch/page', { params }),
  detail: (id) => request.get(`/batch/${id}`),
  create: (data) => request.post('/batch', data),
  update: (id, data) => request.put(`/batch/${id}`, data),
  remove: (id) => request.delete(`/batch/${id}`),
  hash: (id) => request.post(`/batch/${id}/hash`),
  chain: (id) => request.post(`/batch/${id}/chain`)
}
