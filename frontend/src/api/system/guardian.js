import request from '@/utils/request'

export const currentGuardianBindingList = () => request({
  url: '/api/guardianBinding/currentGuardianBindingList', method: 'get'
})

export const createGuardianInvitation = data => request({
  url: '/api/guardianBinding/createGuardianInvitation', method: 'post', data
})

export const acceptGuardianInvitation = data => request({
  url: '/api/guardianBinding/acceptGuardianInvitation', method: 'post', data
})

export const confirmGuardianBinding = data => request({
  url: '/api/guardianBinding/confirmGuardianBinding', method: 'post', data
})

export const revokeGuardianBinding = data => request({
  url: '/api/guardianBinding/revokeGuardianBinding', method: 'post', data
})

export const guardianStudentOverview = params => request({
  url: '/api/guardianOverview/studentOverview', method: 'get', params
})

export const exportGuardianStudentOverview = params => request({
  url: '/api/guardianOverview/exportStudentOverview', method: 'get', params
})

export const guardianWeeklyReport = params => request({
  url: '/api/guardianWeeklyReport/guardianWeeklyReport', method: 'get', params
})

export const guardianWeeklyReportSubscriptionList = () => request({ url: '/api/guardianWeeklyReportSubscription/guardianWeeklyReportSubscriptionList', method: 'get' })
export const updateGuardianWeeklyReportSubscription = data => request({ url: '/api/guardianWeeklyReportSubscription/updateGuardianWeeklyReportSubscription', method: 'post', data })

export const guardianAssistedCaptureList = () => request({ url: '/api/guardianAssistedCapture/guardianAssistedCaptureList', method: 'get' })
export const createGuardianAssistedCapture = data => request({ url: '/api/guardianAssistedCapture/createGuardianAssistedCapture', method: 'post', data })
export const confirmGuardianAssistedCapture = data => request({ url: '/api/guardianAssistedCapture/confirmGuardianAssistedCapture', method: 'post', data })
export const revokeGuardianAssistedCapture = data => request({ url: '/api/guardianAssistedCapture/revokeGuardianAssistedCapture', method: 'post', data })
