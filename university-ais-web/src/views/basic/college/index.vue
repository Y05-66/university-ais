<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学院管理</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="学院名称" />
        <el-table-column prop="code" label="代码" width="100" />
        <el-table-column prop="dean" label="院长" width="100" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入学院代码" />
        </el-form-item>
        <el-form-item label="院长">
          <el-input v-model="form.dean" placeholder="请输入院长姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 学院管理页面 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCollegeList, addCollege, updateCollege, deleteCollege } from '@/api'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增学院')
const formRef = ref(null)
const form = ref({})
const submitLoading = ref(false)

const rules = {
  name: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入学院代码', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCollegeList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增学院'
  form.value = { status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学院'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const apiFn = form.value.id ? updateCollege : addCollege
    await apiFn(form.value)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学院吗？', '提示', { type: 'warning' })
    await deleteCollege(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
