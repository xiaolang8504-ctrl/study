import request from '@/utils/request'

export function wrongQuestionPageList(params) {
  return request({
    url: '/api/wrongQuestion/wrongQuestionPageList',
    method: 'get',
    params
  })
}

export function wrongQuestionDetail(params) {
  return request({
    url: '/api/wrongQuestion/wrongQuestionDetail',
    method: 'get',
    params
  })
}

export function createWrongQuestion(data) {
  return request({
    url: '/api/wrongQuestion/createWrongQuestion',
    method: 'post',
    data
  })
}

export function importWrongQuestionImage(data) {
  return request({
    url: '/api/wrongQuestion/importWrongQuestionImage',
    method: 'post',
    data
  })
}

export function updateWrongQuestion(data) {
  return request({
    url: '/api/wrongQuestion/updateWrongQuestion',
    method: 'post',
    data
  })
}

export function updateWrongQuestionImage(data) {
  return request({
    url: '/api/wrongQuestion/updateWrongQuestionImage',
    method: 'post',
    data
  })
}

export function deleteWrongQuestion(data) {
  return request({
    url: '/api/wrongQuestion/deleteWrongQuestion',
    method: 'post',
    data
  })
}

export function batchDeleteWrongQuestion(data) {
  return request({
    url: '/api/wrongQuestion/batchDeleteWrongQuestion',
    method: 'post',
    data
  })
}

export function batchOrganizeWrongQuestion(data) { return request({ url: '/api/wrongQuestion/batchOrganizeWrongQuestion', method: 'post', data }) }
export function wrongQuestionTagList() { return request({ url: '/api/wrongQuestion/wrongQuestionTagList', method: 'get' }) }
export function savedWrongQuestionFilterList() { return request({ url: '/api/wrongQuestion/savedWrongQuestionFilterList', method: 'get' }) }
export function saveWrongQuestionFilter(data) { return request({ url: '/api/wrongQuestion/saveWrongQuestionFilter', method: 'post', data }) }
export function deleteWrongQuestionFilter(data) { return request({ url: '/api/wrongQuestion/deleteWrongQuestionFilter', method: 'post', data }) }

export function updateWrongQuestionStatus(data) {
  return request({
    url: '/api/wrongQuestion/updateWrongQuestionStatus',
    method: 'post',
    data
  })
}

export function submitCorrectionRecord(data) {
  return request({
    url: '/api/wrongQuestion/submitCorrectionRecord',
    method: 'post',
    data
  })
}

export function correctionRecordList(params) {
  return request({
    url: '/api/wrongQuestion/correctionRecordList',
    method: 'get',
    params
  })
}

export function saveCorrectionDraft(data) {
  return request({ url: '/api/wrongQuestion/saveCorrectionDraft', method: 'post', data })
}

export function revealAnswerLayer(data) {
  return request({ url: '/api/wrongQuestion/revealAnswerLayer', method: 'post', data })
}

export function scanWrongQuestionDuplicate(data) {
  return request({ url: '/api/wrongQuestion/scanWrongQuestionDuplicate', method: 'post', data })
}

export function mergeWrongQuestionDuplicate(data) {
  return request({ url: '/api/wrongQuestion/mergeWrongQuestionDuplicate', method: 'post', data })
}

export function undoWrongQuestionMerge(data) {
  return request({ url: '/api/wrongQuestion/undoWrongQuestionMerge', method: 'post', data })
}

export function bindWrongQuestionKnowledgePoint(data) {
  return request({
    url: '/api/wrongQuestion/bindWrongQuestionKnowledgePoint',
    method: 'post',
    data
  })
}

export function wrongQuestionKnowledgePointStatistics(params) {
  return request({
    url: '/api/wrongQuestion/wrongQuestionKnowledgePointStatistics',
    method: 'get',
    params
  })
}

export function updateWrongQuestionErrorAnalysis(data) {
  return request({
    url: '/api/wrongQuestion/updateWrongQuestionErrorAnalysis',
    method: 'post',
    data
  })
}

export function wrongQuestionErrorAnalysisStatistics(params) {
  return request({
    url: '/api/wrongQuestion/wrongQuestionErrorAnalysisStatistics',
    method: 'get',
    params
  })
}

export function createQuestionCaptureTask(data) { return request({ url: '/api/questionCapture/createQuestionCaptureTask', method: 'post', data }) }
export function questionCaptureTaskDetail(params) { return request({ url: '/api/questionCapture/questionCaptureTaskDetail', method: 'get', params }) }
export function retryQuestionCaptureTask(data) { return request({ url: '/api/questionCapture/retryQuestionCaptureTask', method: 'post', data }) }
export function confirmQuestionCapture(data) { return request({ url: '/api/questionCapture/confirmQuestionCapture', method: 'post', data }) }
export function updateQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/updateQuestionCaptureRegion', method: 'post', data }) }
export function batchUpdateQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/batchUpdateQuestionCaptureRegion', method: 'post', data }) }
export function createQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/createQuestionCaptureRegion', method: 'post', data }) }
export function mergeQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/mergeQuestionCaptureRegion', method: 'post', data }) }
export function splitQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/splitQuestionCaptureRegion', method: 'post', data }) }
export function splitQuestionCaptureRegionByRatio(data) { return request({ url: '/api/questionCapture/splitQuestionCaptureRegionByRatio', method: 'post', data }) }
export function restoreQuestionCaptureRegionSnapshot(data) { return request({ url: '/api/questionCapture/restoreQuestionCaptureRegionSnapshot', method: 'post', data }) }
export function deleteQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/deleteQuestionCaptureRegion', method: 'post', data }) }
export function restoreQuestionCaptureRegion(data) { return request({ url: '/api/questionCapture/restoreQuestionCaptureRegion', method: 'post', data }) }
export function questionCaptureTaskPageList(params) { return request({ url: '/api/questionCapture/questionCaptureTaskPageList', method: 'get', params }) }
export function retryQuestionCapturePage(data) { return request({ url: '/api/questionCapture/retryQuestionCapturePage', method: 'post', data }) }
export function saveQuestionCapturePageAsImage(data) { return request({ url: '/api/questionCapture/saveQuestionCapturePageAsImage', method: 'post', data }) }
export function generateQuestionCaptureCleanImage(data) { return request({ url: '/api/questionCapture/generateQuestionCaptureCleanImage', method: 'post', data }) }
export function revertQuestionCaptureCleanImage(data) { return request({ url: '/api/questionCapture/revertQuestionCaptureCleanImage', method: 'post', data }) }
export function applyQuestionCaptureManualCleanImage(data) { return request({ url: '/api/questionCapture/applyQuestionCaptureManualCleanImage', method: 'post', data }) }
export function questionCaptureDuplicateList(data) { return request({ url: '/api/questionCapture/questionCaptureDuplicateList', method: 'post', data }) }
