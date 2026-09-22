import request from '@/utils/request'

export function bookPageList(params) {
  return request({
    url: '/api/book/bookPageList',
    method: 'get',
    params
  })
}

export function bookDetail(params) {
  return request({
    url: '/api/book/bookDetail',
    method: 'get',
    params
  })
}

export function createBook(data) {
  return request({
    url: '/api/book/createBook',
    method: 'post',
    data
  })
}

export function updateBook(data) {
  return request({
    url: '/api/book/updateBook',
    method: 'post',
    data
  })
}

export function deleteBook(data) {
  return request({
    url: '/api/book/deleteBook',
    method: 'post',
    data
  })
}
