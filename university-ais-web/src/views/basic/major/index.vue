<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>专业管理</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="专业名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="searchForm.collegeId" placeholder="请选择" clearable filterable>
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="专业名称" />
        <el-table-column prop="code" label="代码" width="100" />
        <el-table-column prop="degree" label="学位" width="80" />
        <el-table-column prop="years" label="学制" width="80" />
        <el-table-column prop="totalCredits" label="总学分" width="80" />
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
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入专业代码" />
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学位">
          <el-input v-model="form.degree" placeholder="如：工学学士" />
        </el-form-item>
        <el-form-item label="学制">
          <el-input-number v-model="form.years" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="总学分">
          <el-input-number v-model="form.totalCredits" :min="0" :precision="1" :step="0.5" />
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
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getMajorPage, addMajor, updateMajor, deleteMajor, getCollegeList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const colleges = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ name: '', collegeId: null })
const dialogVisible = ref(false)
const dialogTitle = ref('新增专业')
const formRef = ref(null)
const form = ref({})
const submitLoading = ref(false)

const rules = {
  name: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入专业代码', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMajorPage({ current: pagination.value.current, size: pagination.value.size, ...searchForm.value })
    tableData.value = res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadColleges = async () => {
  try {
    const res = await getCollegeList()
    colleges.value = res.data || []
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增专业'
  form.value = { status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑专业'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const apiFn = form.value.id ? updateMajor : addMajor
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
    await ElMessageBox.confirm('确定删除该专业？', '提示', { type: 'warning' })
    await deleteMajor(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { name: '', collegeId: null }
  loadData()
}

onMounted(() => { loadColleges(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
