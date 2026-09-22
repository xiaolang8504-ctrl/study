import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

const service = axios.create({
  baseURL: '/',
  timeout: 30000
})

function redirectToExpiredSession() {
  localStorage.removeItem('study_access_token')
  localStorage.removeItem('study_username')
  if (router.currentRoute.path !== '/login' && router.currentRoute.path !== '/session-expired') {
    router.replace('/session-expired').catch(() => {})
  }
}

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('study_access_token')
    if (token) {
      config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const result = response.data
    if (!result || typeof result.code === 'undefined') {
      return response
    }
    if (result.code === 0 || result.code === 200) {
      return result.data
    }
    Message.error(result.msg || '请求失败')
    if (result.code === 2101002) {
      redirectToExpiredSession()
    } else if (result.code === 2101001 && router.currentRoute.path !== '/forbidden') {
      router.replace('/forbidden').catch(() => {})
    }
    const error = new Error(result.msg || '请求失败')
    error.responseData = result
    return Promise.reject(error)
  },
  error => {
    const response = error.response
    const responseData = response && response.data
    const isUnauthorized = response && response.status === 401
    const isForbidden = response && response.status === 403
    const isNetworkError = !response && error.message === 'Network Error'
    const message = responseData && responseData.msg
      ? responseData.msg
      : isNetworkError
        ? '网络连接失败，请检查网络后重试'
        : error.message

    Message.error(message)
    if (isUnauthorized) {
      redirectToExpiredSession()
    } else if (isForbidden && router.currentRoute.path !== '/forbidden') {
      router.replace('/forbidden').catch(() => {})
    }
    return Promise.reject(error instanceof Error ? error : new Error(message))
  }
)

export default service
