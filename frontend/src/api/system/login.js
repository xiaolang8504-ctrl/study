import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/login',
    method: 'post',
    data
  })
}

export function getSliderCaptcha() {
  return request({
    url: '/api/verify',
    method: 'get'
  })
}

export function verifySliderCaptcha(data) {
  return request({
    url: '/api/verifySliderCaptcha',
    method: 'post',
    data
  })
}

export function outLogin() {
  return request({
    url: '/api/outLogin',
    method: 'post'
  })
}
