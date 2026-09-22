import request from '@/utils/request'

export function initializeReviewHome(params) {
  return request({
    url: '/api/review/initializeReviewHome',
    method: 'post',
    params
  })
}

export function todayReviewHome(params) {
  return request({
    url: '/api/review/todayReviewHome',
    method: 'get',
    params
  })
}

export function reviewAnswer(data) {
  return request({
    url: '/api/review/reviewAnswer',
    method: 'post',
    data
  })
}

export function saveReviewAnswerDraft(data) {
  return request({
    url: '/api/review/saveReviewAnswerDraft',
    method: 'post',
    data
  })
}

export function submitReviewFeedback(data) {
  return request({
    url: '/api/review/submitReviewFeedback',
    method: 'post',
    data
  })
}

export function generateWeakPointPractice(data) {
  return request({
    url: '/api/review/generateWeakPointPractice',
    method: 'post',
    data
  })
}

export function generateTypicalPractice(data) {
  return request({
    url: '/api/review/generateTypicalPractice',
    method: 'post',
    data
  })
}

export function adaptiveLearningContent(params) {
  return request({
    url: '/api/review/adaptiveLearningContent',
    method: 'get',
    params
  })
}

export function learningReport(params) {
  return request({
    url: '/api/review/learningReport',
    method: 'get',
    params
  })
}

export function createPracticeSession(data) {
  return request({
    url: '/api/review/createPracticeSession',
    method: 'post',
    data
  })
}

export function previewPracticeSession(data) {
  return request({
    url: '/api/review/previewPracticeSession',
    method: 'post',
    data
  })
}

export function practiceSessionDetail(params) {
  return request({
    url: '/api/review/practiceSessionDetail',
    method: 'get',
    params
  })
}

export function practicePaperDetail(params) {
  return request({
    url: '/api/review/practicePaperDetail',
    method: 'get',
    params
  })
}

export function createPracticePaperExportTask(data) {
  return request({ url: '/api/review/createPracticePaperExportTask', method: 'post', data })
}

export function practicePaperExportTaskList() {
  return request({ url: '/api/review/practicePaperExportTaskList', method: 'get' })
}

export function submitPracticeAnswer(data) {
  return request({
    url: '/api/review/submitPracticeAnswer',
    method: 'post',
    data
  })
}

export function practiceQuestionAnswer(params) {
  return request({
    url: '/api/review/practiceQuestionAnswer',
    method: 'get',
    params
  })
}

export function batchSubmitPracticeAnswer(data) {
  return request({
    url: '/api/review/batchSubmitPracticeAnswer',
    method: 'post',
    data
  })
}

export function savePracticeAnswerDraft(data) {
  return request({
    url: '/api/review/savePracticeAnswerDraft',
    method: 'post',
    data
  })
}

export function finishPracticeSession(data) {
  return request({
    url: '/api/review/finishPracticeSession',
    method: 'post',
    data
  })
}

export function practiceSessionPageList(params) {
  return request({
    url: '/api/review/practiceSessionPageList',
    method: 'get',
    params
  })
}

export function practiceStatistics(params) {
  return request({
    url: '/api/review/practiceStatistics',
    method: 'get',
    params
  })
}

export function reviewPlanSetting() {
  return request({
    url: '/api/review/reviewPlanSetting',
    method: 'get'
  })
}

export function updateReviewPlanSetting(data) {
  return request({
    url: '/api/review/updateReviewPlanSetting',
    method: 'post',
    data
  })
}

export function learningProfile() {
  return request({
    url: '/api/review/learningProfile',
    method: 'get'
  })
}

export function updateLearningProfile(data) {
  return request({
    url: '/api/review/updateLearningProfile',
    method: 'post',
    data
  })
}

export function learningDataBackup() {
  return request({
    url: '/api/review/learningDataBackup',
    method: 'get'
  })
}

export function reviewHistoryPageList(params) {
  return request({
    url: '/api/review/reviewHistoryPageList',
    method: 'get',
    params
  })
}

export function reviewReminderPageList(params) {
  return request({
    url: '/api/review/reviewReminderPageList',
    method: 'get',
    params
  })
}

export function readReviewReminder(data) {
  return request({
    url: '/api/review/readReviewReminder',
    method: 'post',
    data
  })
}
