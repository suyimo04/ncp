import request from '@/utils/request'

export function publicVerify(code) {
  return request.get(`/public/verify/${code}`)
}
