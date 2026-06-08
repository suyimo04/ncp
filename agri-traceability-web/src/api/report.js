import request from '@/utils/request'

export const reportApi = {
  page: (params) => request.get('/test-report/page', { params }),
  detail: (id) => request.get(`/test-report/${id}`),
  create: (data) => request.post('/test-report', data),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/test-report/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  hash: (id) => request.post(`/test-report/${id}/hash`),
  chain: (id) => request.post(`/test-report/${id}/chain`)
}
