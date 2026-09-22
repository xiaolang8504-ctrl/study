import request from '@/utils/request'

export function userPageList(params) {
  return request({
    url: '/api/user/userPageList',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/api/user/createUser',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/api/user/updateUser',
    method: 'post',
    data
  })
}

export function deleteUser(data) {
  return request({
    url: '/api/user/deleteUser',
    method: 'post',
    data
  })
}

export function updatePassWord(data) {
  return request({
    url: '/api/user/updatePassWord',
    method: 'post',
    data
  })
}

export function updateUserPassWord(data) {
  return request({
    url: '/api/user/updateUserPassWord',
    method: 'post',
    data
  })
}

export function userUnionRole(data) {
  return request({
    url: '/api/user/userUnionRole',
    method: 'post',
    data
  })
}

export function enableUser(data) {
  return request({
    url: '/api/user/enableUser',
    method: 'post',
    data
  })
}

export function getCurrentUserInfo() {
  return request({
    url: '/api/user/getCurrentUserInfo',
    method: 'get'
  })
}
