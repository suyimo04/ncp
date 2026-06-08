import request from '@/utils/request'

export const chainApi = {
  page: (params) => request.get('/chain/evidence/page', { params }),
  detail: (id) => request.get(`/chain/evidence/${id}`),
  verify: (id) => request.post(`/chain/evidence/${id}/verify`),
  config: () => request.get('/chain/contract/config'),
  saveConfig: (configs) => request.put('/chain/contract/config', { configs })
}
