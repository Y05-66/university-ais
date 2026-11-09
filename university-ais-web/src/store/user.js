/**
 * 用户认证状态管理。
 *
 * <p>管理用户登录状态、用户信息、角色权限和菜单数据。
 * 使用 Pinia 实现，状态持久化到 sessionStorage。
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>登录/登出：管理 Token 和用户信息</li>
 *   <li>权限判断：isAdmin()、isTeacher()、isStudent()</li>
 *   <li>菜单管理：加载和构建菜单树</li>
 * </ul>
 *
 * <h3>登录流程</h3>
 * <pre>
 * 1. loginAsync() - 调用登录API，获取Token
 * 2. getInfo() - 调用/auth/info，获取用户信息和菜单
 * 3. 根据 userType 跳转到对应首页
 * </pre>
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import router from '@/router'
import { useTagsViewStore } from '@/store/tagsView'

export const useUserStore = defineStore('user', () => {
  /** 登录 Token，从 sessionStorage 恢复，保证刷新后保持登录状态 */
  const token = ref(sessionStorage.getItem('ueas_access_token') || '')

  /** 当前用户详细信息，从 sessionStorage 恢复，避免每次路由守卫都重新请求 /auth/info */
  const savedUserInfo = sessionStorage.getItem('ueas_user_info')
  const userInfo = ref(savedUserInfo ? JSON.parse(savedUserInfo) : null)

  /** 用户角色列表，如 ['ROLE_TEACHER'] */
  const roles = ref(savedUserInfo ? JSON.parse(savedUserInfo).roles || [] : [])

  /** 用户可见菜单树，由后端生成 */
  const menus = ref(savedUserInfo ? JSON.parse(savedUserInfo).menus || [] : [])

  /**
   * 用户类型：
   * <ul>
   *   <li>1 — 学生</li>
   *   <li>2 — 教师</li>
   *   <li>3 — 管理员</li>
   * </ul>
   */
  const userType = ref(savedUserInfo ? JSON.parse(savedUserInfo).userType || null : null)

  /** 是否正在加载用户信息 */
  const loading = ref(false)

  /**
   * 设置 Token 并同步到 sessionStorage。
   *
   * @param val Token 字符串
   */
  const setToken = (val) => {
    token.value = val
    sessionStorage.setItem('ueas_access_token', val)
  }

  /**
   * 清除所有认证状态。
   *
   * <p>登出时调用，清除本地存储和状态。
   */
  const clearAuthState = () => {
    token.value = ''
    userInfo.value = null
    roles.value = []
    menus.value = []
    userType.value = null
    sessionStorage.removeItem('ueas_access_token')
    sessionStorage.removeItem('ueas_user_info')
  }

  /**
   * 用户登录。
   *
   * @param loginForm 登录表单 { username, password, rememberMe }
   * @returns 登录响应
   */
  const loginAsync = async (loginForm) => {
    const res = await login(loginForm)
    setToken(res.data.token)
    return res
  }

  /**
   * 获取当前用户详细信息。
   *
   * <p>通常在登录后或页面刷新后调用。
   * 获取用户信息、角色、菜单后存入状态。
   */
  const getInfo = async () => {
    loading.value = true
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      roles.value = res.data.roles || []
      menus.value = res.data.menus || []
      userType.value = res.data.userType
      // 同时存到 sessionStorage，页面刷新后可恢复
      sessionStorage.setItem('ueas_user_info', JSON.stringify(res.data))
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户退出登录。
   *
   * <p>调用后端 logout 接口后清除本地状态，跳转到登录页。
   */
  const logoutAsync = async () => {
    try {
      await logout()
    } catch (e) {
      // 忽略网络错误（用户已离线时也允许退出）
    }
    clearAuthState()
    // 清除标签页和 keep-alive 缓存，避免切换账号时残留
    const tagsViewStore = useTagsViewStore()
    tagsViewStore.visitedViews = []
    tagsViewStore.cachedViews = []
    router.push('/login')
  }

  /** 检查用户是否包含指定角色 */
  const hasRole = (role) => roles.value.includes(role)

  /** 是否为管理员 */
  const isAdmin = () => userType.value === 3

  /** 是否为教师 */
  const isTeacher = () => userType.value === 2

  /** 是否为学生 */
  const isStudent = () => userType.value === 1

  return {
    token,
    userInfo,
    roles,
    menus,
    userType,
    loading,
    setToken,
    clearAuthState,
    loginAsync,
    getInfo,
    logoutAsync,
    hasRole,
    isAdmin,
    isTeacher,
    isStudent,
  }
})
