import request from '@/utils/request'

export function filePolicy(params) {
  return request({
    url: '/api/file/policy',
    method: 'get',
    params
  })
}

export function uploadFile(data) {
  return request({
    url: '/api/file/uploadFile',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadUrl(params) {
  return request({
    url: '/api/file/downloadUrl',
    method: 'get',
    params
  })
}

export function questionImagePageList(params) {
  return request({ url: '/api/file/questionImagePageList', method: 'get', params })
}

export function deleteOrphanQuestionImage(data) {
  return request({ url: '/api/file/deleteOrphanQuestionImage', method: 'post', data })
}

export function cleanOrphanQuestionImage(data) {
  return request({ url: '/api/file/cleanOrphanQuestionImage', method: 'post', data })
}
