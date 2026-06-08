import request from '@/utils/request'

export const certificateApi = {
  page: (params) => request.get('/certificate/page', { params }),
  detail: (id) => request.get(`/certificate/${id}`),
  apply: (data) => request.post('/certificate/apply', data),
  audit: (id, data) => request.put(`/certificate/${id}/audit`, data),
  issue: (id) => request.post(`/certificate/${id}/issue`),
  chain: (id) => request.post(`/certificate/${id}/chain`),
  revoke: (id, data) => request.put(`/certificate/${id}/revoke`, data)
}
