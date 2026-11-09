/**
 * API请求封装模块。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 15000,
})

// ===== 防止导航期间后台请求误触发登出 =====
let pendingLogout = null
let lastNavTime = 0 // 记录最后一次导航时间

export function markNavigation() {
  lastNavTime = Date.now()
  // 清除之前的待处理登出
  if (pendingLogout) {
    clearTimeout(pendingLogout)
    pendingLogout = null
  }
}

service.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('ueas_access_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (!config.headers['Content-Type']?.includes('multipart')) {
      config.headers['Content-Type'] = 'application/json; charset=UTF-8'
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        handleLogout(response.config?.url)
      } else {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    const status = error.response?.status
    const code = error.response?.data?.code
    const url = error.config?.url
    // AbortError = 被取消的请求，静默忽略
    if (error.name === 'AbortError' || axios.isCancel(error)) {
      return Promise.reject(error)
    }
    if (status === 401 || code === 401) {
      handleLogout(url)
    } else {
      const message = error.response?.data?.msg || error.message || '网络异常'
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

/**
 * 统一处理登出逻辑
 * 使用延迟机制：如果在导航后短时间内收到401，认为是导航期间的旧请求，不触发登出
 * @param {string} url - 触发401的请求URL
 */
function handleLogout(url) {
  const timeSinceLastNav = Date.now() - lastNavTime
  // 如果在最近2秒内有导航行为，忽略这个401（认为是导航期间的旧请求）
  if (timeSinceLastNav < 2000) {
    console.log('[req] 401 suppressed - navigation in progress:', url, timeSinceLastNav + 'ms ago')
    return
  }
  // 否则真正触发登出
  console.warn('[req] 401 triggered logout:', url)
  if (pendingLogout) clearTimeout(pendingLogout)
  pendingLogout = setTimeout(() => {
    const userStore = useUserStore()
    userStore.clearAuthState()
    window.location.href = '/login'
  }, 0)
}

export default service
