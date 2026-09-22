import request from '@/utils/request'

export function roleList(params) {
  return request({
    url: '/api/role/roleList',
    method: 'get',
    params
  })
}

export function createRole(data) {
  return request({
    url: '/api/role/createRole',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/api/role/updateRole',
    method: 'post',
    data
  })
}

export function deleteRole(data) {
  return request({
    url: '/api/role/deleteRole',
    method: 'post',
    data
  })
}

export function roleDetail(params) {
  return request({
    url: '/api/role/roleDetail',
    method: 'get',
    params
  })
}

export function roleOptions() {
  return request({
    url: '/api/role/roleOptions',
    method: 'get'
  })
}

export function roleByMenu(data) {
  return request({
    url: '/api/role/roleByMenu',
    method: 'post',
    data
  })
}
