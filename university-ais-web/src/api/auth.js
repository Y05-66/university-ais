/**
 * 认证相关API接口。
 *
 * <p>提供用户登录、登出、获取用户信息、修改密码、上传头像等功能。
 */
import request from '@/api/request'

/**
 * 获取图形验证码。
 *
 * @returns {Promise<{key: string, image: string}>} key为验证码标识，image为Base64 PNG数据
 */
export function getCaptcha() {
  return request.get('/captcha')
}

/**
 * 用户登录。
 *
 * @param data 登录表单数据 { username, password, captchaKey, captcha }
 * @returns 包含 token、用户信息、菜单的响应
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 用户登出。
 *
 * @returns 操作结果
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户详细信息。
 *
 * <p>返回用户基本信息、角色列表和权限菜单树。
 * 用于登录后初始化用户状态和侧边栏菜单。
 *
 * @param {object} [config] axios 请求配置，如 { suppressRedirect: true }
 * @returns 用户详细信息，包含 userId, username, realName, userType, roles, menus 等
 */
export function getUserInfo(config) {
  return request.get('/auth/info', config)
}

/**
 * 更新用户个人资料。
 *
 * @param data 更新数据 { realName, phone, email, avatar }
 * @returns 操作结果
 */
export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

/**
 * 修改用户密码。
 *
 * @param data 密码数据 { oldPassword, newPassword }
 * @returns 操作结果
 */
export function updatePassword(data) {
  return request.put('/auth/password', data)
}

/**
 * 上传头像到OSS。
 *
 * <p>上传成功后返回OSS公网访问URL，需手动调用 updateProfile 保存到数据库。
 *
 * @param file 图片文件
 * @returns OSS访问URL
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
