<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户名"><el-input v-model="searchForm.username" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="searchForm.realName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.userType" placeholder="全部" clearable>
            <el-option label="学生" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.userType === 3 ? 'danger' : row.userType === 2 ? 'success' : 'warning'" size="small">
              {{ ['学生','教师','管理员'][row.userType - 1] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="160" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="warning" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" @current-change="loadData" @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end" />
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增用户" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="类型" prop="userType">
          <el-select v-model="form.userType" style="width: 100%">
            <el-option label="学生" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 系统管理页面 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getUserPage, createUser, deleteUser, resetPassword, updateUser } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ username: '', realName: '', userType: null, status: null })
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({})
const submitLoading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({ current: pagination.value.current, size: pagination.value.size, ...searchForm.value })
    tableData.value = res.data.list || []
    pagination.value.total = res.data.total || 0
  } finally { loading.value = false }
}

const handleAdd = () => {
  form.value = { userType: 1, status: 1 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    await createUser(form.value)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

const handleResetPwd = async (row) => {
  const { value: newPwd } = await ElMessageBox.prompt('请输入新密码（至少6位）', '重置密码', {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /.+/, inputErrorMessage: '请输入密码'
  })
  try {
    await resetPassword(row.id, newPwd)
    ElMessage.success('密码重置成功')
  } catch (e) { ElMessage.error('操作失败') }
}

const handleToggleStatus = async (row) => {
  try {
    await updateUser({ ...row, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('状态已更新')
    loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

const handleDelete = async (row) => {
  if (row.id === 1) { ElMessage.warning('不能删除管理员账号'); return }
  try {
    await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleReset = () => {
  searchForm.value = { username: '', realName: '', userType: null, status: null }
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
