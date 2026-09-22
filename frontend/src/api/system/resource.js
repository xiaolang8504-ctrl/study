import request from '@/utils/request'

export function resourceList(params) {
  return request({
    url: '/api/resource/resourceList',
    method: 'get',
    params
  })
}

export function resourceTreeData() {
  return request({
    url: '/api/resource/resourceTreeData',
    method: 'get'
  })
}

export function createResource(data) {
  return request({
    url: '/api/resource/createResource',
    method: 'post',
    data
  })
}

export function updateResource(data) {
  return request({
    url: '/api/resource/updateResource',
    method: 'post',
    data
  })
}

export function deleteResource(data) {
  return request({
    url: '/api/resource/deleteResource',
    method: 'post',
    data
  })
}

export function createPre() {
  return request({
    url: '/api/resource/createPre',
    method: 'post'
  })
}
