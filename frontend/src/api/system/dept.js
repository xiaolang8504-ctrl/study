import request from '@/utils/request'

export function deptTreeData() {
  return request({
    url: '/api/dept/deptTreeData',
    method: 'get'
  })
}

export function deptList(params) {
  return request({
    url: '/api/dept/deptList',
    method: 'get',
    params
  })
}

export function getDeptDetailById(params) {
  return request({
    url: '/api/dept/getDeptDetailById',
    method: 'get',
    params
  })
}

export function createDept(data) {
  return request({
    url: '/api/dept/createDept',
    method: 'post',
    data
  })
}

export function updateDept(data) {
  return request({
    url: '/api/dept/updateDept',
    method: 'post',
    data
  })
}

export function deleteDept(data) {
  return request({
    url: '/api/dept/deleteDept',
    method: 'post',
    data
  })
}

export function enableDept(data) {
  return request({
    url: '/api/dept/enableDept',
    method: 'post',
    data
  })
}
