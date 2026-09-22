import request from '@/utils/request'

export function homeWorkPageList(params) {
  return request({
    url: '/api/homeWork/homeWorkPageList',
    method: 'get',
    params
  })
}

export function homeWorkDetail(params) {
  return request({
    url: '/api/homeWork/homeWorkDetail',
    method: 'get',
    params
  })
}

export function createHomeWork(data) {
  return request({
    url: '/api/homeWork/createHomeWork',
    method: 'post',
    data
  })
}

export function updateHomeWork(data) {
  return request({
    url: '/api/homeWork/updateHomeWork',
    method: 'post',
    data
  })
}

export function deleteHomeWork(data) {
  return request({
    url: '/api/homeWork/deleteHomeWork',
    method: 'post',
    data
  })
}
