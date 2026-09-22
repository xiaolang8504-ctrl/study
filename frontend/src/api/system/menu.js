import request from '@/utils/request'

export function menuList(params) {
  return request({
    url: '/api/menu/menuList',
    method: 'get',
    params
  })
}

export function createMenu(data) {
  return request({
    url: '/api/menu/createMenu',
    method: 'post',
    data
  })
}

export function updateMenu(data) {
  return request({
    url: '/api/menu/updateMenu',
    method: 'post',
    data
  })
}

export function updateMenuResource(data) {
  return request({
    url: '/api/menu/updateMenuResource',
    method: 'post',
    data
  })
}

export function deleteMenu(data) {
  return request({
    url: '/api/menu/deleteMenu',
    method: 'post',
    data
  })
}

export function menuDetail(params) {
  return request({
    url: '/api/menu/menuDetail',
    method: 'get',
    params
  })
}

export function menuTreeData() {
  return request({
    url: '/api/menu/menuTreeData',
    method: 'get'
  })
}

export function currentMenuTree() {
  return request({
    url: '/api/menu/currentMenuTree',
    method: 'get'
  })
}

export function createMenCode(data) {
  return request({
    url: '/api/menu/createMenCode',
    method: 'post',
    data
  })
}
