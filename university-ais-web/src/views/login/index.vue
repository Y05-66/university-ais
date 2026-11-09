<template>
  <div class="login-container">
    <div class="login-bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon size="40"><School /></el-icon>
        </div>
        <h1>大学教务管理系统</h1>
        <p class="subtitle">University Educational Administration System</p>
      </div>

      <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            clearable
            :disabled="loading"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :disabled="loading"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              size="large"
              clearable
              :disabled="loading"
              @keyup.enter="handleLogin"
              class="captcha-input"
            />
            <div class="captcha-image" @click="refreshCaptcha" :title="'点击刷新验证码'">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else class="captcha-placeholder">加载中...</span>
            </div>
          </div>
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
          <el-link type="primary" underline="never" @click="showForgotPwd = true">忘记密码?</el-link>
        </div>

        <el-button
          :loading="loading"
          type="primary"
          size="large"
          class="login-btn"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <div class="login-tip">
        <p class="tip-title">演示账号</p>
        <el-space wrap :size="8">
          <el-tag size="small" type="danger" effect="dark">管理员 admin / admin123</el-tag>
          <el-tag size="small" type="success">教师 teacher01 / teacher123</el-tag>
          <el-tag size="small" type="warning">学生 student01 / student123</el-tag>
        </el-space>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="showForgotPwd" title="联系管理员" width="400px" :close-on-click-modal="false">
      <el-result icon="info" title="忘记密码请联系管理员重置" sub-title="系统管理员可以为您重置密码。">
        <template #extra>
          <el-button type="primary" @click="showForgotPwd = false">知道了</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
/** 页面组件 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const showForgotPwd = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')
const isMounted = ref(true)

// 从localStorage恢复记住的密码
const savedUsername = localStorage.getItem('ueas_remember_username')
const loginForm = reactive({
  username: savedUsername || '',
  password: '',
  rememberMe: !!savedUsername,
  captcha: '',
  captchaKey: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位字符', trigger: 'blur' }
  ],
}

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaKey.value = res.data.key
    captchaImage.value = 'data:image/png;base64,' + res.data.image
    loginForm.captchaKey = res.data.key
  } catch (e) {
    console.warn('获取验证码失败:', e)
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  fetchCaptcha()
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 记住密码
    if (loginForm.rememberMe) {
      localStorage.setItem('ueas_remember_username', loginForm.username)
    } else {
      localStorage.removeItem('ueas_remember_username')
    }

    await userStore.loginAsync(loginForm)
    await userStore.getInfo()

    // 登录后统一跳转到首页
    router.push('/dashboard')
    ElMessage.success({ message: `欢迎回来，${userStore.userInfo?.realName || '用户'}！`, duration: 2000 })
  } catch (error) {
    console.warn('登录失败:', error)
    // 登录失败时刷新验证码
    refreshCaptcha()
    loginForm.captcha = ''
  } finally {
    if (isMounted.value) loading.value = false
  }
}

// 根据用户类型返回默认跳转路径（已废弃，统一跳转到首页）
const getDefaultPath = (userType) => {
  return '/dashboard'
}

onMounted(() => {
  fetchCaptcha()
  // 如果已有token且有效，自动跳转
  if (userStore.token && userStore.userInfo) {
    router.push(getDefaultPath(userStore.userType))
  }
})

onUnmounted(() => {
  isMounted.value = false
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    animation: float 6s ease-in-out infinite;
  }

  .shape-1 {
    width: 300px;
    height: 300px;
    background: #fff;
    top: -80px;
    left: -80px;
    animation-delay: 0s;
  }
  .shape-2 {
    width: 200px;
    height: 200px;
    background: #fff;
    bottom: -50px;
    right: -50px;
    animation-delay: 2s;
  }
  .shape-3 {
    width: 150px;
    height: 150px;
    background: #fff;
    top: 50%;
    left: 60%;
    animation-delay: 4s;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.login-box {
  width: 400px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(12px);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }

  h1 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 8px;
    font-weight: 600;
  }

  .subtitle {
    font-size: 12px;
    color: #909399;
    letter-spacing: 1px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    font-size: 13px;
  }

  .login-btn {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-size: 15px;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
}

.login-tip {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

  .tip-title {
    font-size: 12px;
    color: #909399;
    margin-bottom: 10px;
  }
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;

  .captcha-input {
    flex: 1;
  }

  .captcha-image {
    width: 140px;
    height: 46px;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.85;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .captcha-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      font-size: 12px;
      color: #909399;
      background: #f5f7fa;
    }
  }
}
</style>
