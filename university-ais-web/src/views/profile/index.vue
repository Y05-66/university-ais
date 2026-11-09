<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar || ''" class="avatar">
              <el-icon size="48"><User /></el-icon>
            </el-avatar>
            <div class="avatar-actions">
              <el-upload
                action="#"
                :show-file-list="false"
                :before-upload="handleAvatarUpload"
                accept="image/jpeg,image/png,image/gif,image/webp"
              >
                <el-button type="primary" size="small" :icon="Upload">上传头像</el-button>
              </el-upload>
              <el-button size="small" @click="resetAvatar" :icon="Refresh">重置</el-button>
            </div>
            <p class="avatar-tip">支持 JPG、PNG 格式，建议尺寸 200x200</p>
          </div>
          <div class="user-info-display">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色</span>
              <span class="value">
                <el-tag v-if="userStore.isAdmin()" type="danger" size="small">管理员</el-tag>
                <el-tag v-else-if="userStore.isTeacher()" type="success" size="small">教师</el-tag>
                <el-tag v-else type="warning" size="small">学生</el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">上次登录</span>
              <span class="value">{{ formatTime(userInfo.lastLogin) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 编辑表单 -->
      <el-col :span="16">
        <el-card class="edit-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>编辑资料</span>
              <el-button type="primary" size="small" :icon="Check" @click="handleSave">保存修改</el-button>
            </div>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像URL">
                  <el-input v-model="form.avatar" placeholder="请输入头像图片地址" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="edit-card" shadow="hover" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :icon="Check" @click="handlePasswordChange">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/** 页面组件 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Upload, Refresh, Check } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateProfile, updatePassword, uploadAvatar } from '@/api/auth'

const userStore = useUserStore()
const formRef = ref(null)
const pwdRef = ref(null)

const userInfo = ref({})
const form = reactive({
  realName: '',
  phone: '',
  email: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadData = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    form.realName = res.data.realName || ''
    form.phone = res.data.phone || ''
    form.email = res.data.email || ''
    form.avatar = res.data.avatar || ''
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    await updateProfile(form)
    // 更新 store 中的用户信息
    userInfo.value = { ...userInfo.value, ...form }
    userStore.userInfo = { ...userStore.userInfo, ...form }
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handlePasswordChange = async () => {
  try {
    await pwdRef.value.validate()
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清除登录状态
    userStore.clearAuthState()
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleAvatarUpload = async (file) => {
  try {
    const res = await uploadAvatar(file)
    form.avatar = res.data
    userInfo.value.avatar = res.data
    userStore.userInfo = { ...userStore.userInfo, avatar: res.data }
    // 上传成功后自动保存
    await handleSave()
  } catch (e) {
    ElMessage.error('头像上传失败')
    ElMessage.error('头像上传失败')
  }
  return false // 阻止 el-upload 的默认上传行为
}

const resetAvatar = () => {
  form.avatar = ''
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.profile-card {
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;

    .avatar {
      border: 3px solid #e4e7ed;
      background: #f5f7fa;
    }

    .avatar-actions {
      margin-top: 16px;
      display: flex;
      gap: 10px;
    }

    .avatar-tip {
      margin-top: 12px;
      font-size: 12px;
      color: #909399;
    }
  }

  .user-info-display {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;

      .label {
        color: #909399;
        font-size: 14px;
      }

      .value {
        color: #303133;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

.edit-card {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}
</style>
