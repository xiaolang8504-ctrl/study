import request from '@/utils/request'

export function dictList(params) {
  return request({ url: '/api/dict/dictList', method: 'get', params })
}

export function createDict(data) {
  return request({ url: '/api/dict/createDict', method: 'post', data })
}

export function updateDict(data) {
  return request({ url: '/api/dict/updateDict', method: 'post', data })
}

export function deleteDict(data) {
  return request({ url: '/api/dict/deleteDict', method: 'post', data })
}

export function dictOptions() {
  return request({ url: '/api/dict/dictOptions', method: 'post' })
}

export function dictDataPageList(params) {
  return request({ url: '/api/dictData/dictDataPageList', method: 'get', params })
}

export function dictDataOptions(params) {
  return request({ url: '/api/dictData/dictDataOptions', method: 'get', params })
}

export function createDictData(data) {
  return request({ url: '/api/dictData/createDictData', method: 'post', data })
}

export function updateDictData(data) {
  return request({ url: '/api/dictData/updateDictData', method: 'post', data })
}

export function deleteDictData(data) {
  return request({ url: '/api/dictData/deleteDictData', method: 'post', data })
}

export function enableDictData(data) {
  return request({ url: '/api/dictData/enableDictData', method: 'post', data })
}

export function dictDataSort(data) {
  return request({ url: '/api/dictData/dictDataSort', method: 'post', data })
}
