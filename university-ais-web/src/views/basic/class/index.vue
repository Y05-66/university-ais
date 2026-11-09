<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="searchForm.grade" placeholder="如2024" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="code" label="班级代码" width="120" />
        <el-table-column prop="name" label="班级名称" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="studentCount" label="人数" width="80" />
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="班级代码" prop="code">
          <el-input v-model="form.code" placeholder="如CS202401" />
        </el-form-item>
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="form.name" placeholder="如计科2024-1班" />
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="如2024" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="form.studentCount" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 学院管理页面 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getClassPage, addClass, updateClass, deleteClass, getMajorPage } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ name: '', grade: '' })
const majors = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增班级')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  code: '',
  name: '',
  majorId: null,
  grade: '',
  studentCount: 0
})

const rules = {
  code: [{ required: true, message: '请输入班级代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getClassPage({ current: pagination.value.current, size: pagination.value.size, ...searchForm.value })
    tableData.value = res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const loadMajors = async () => {
  try {
    const res = await getMajorPage({ current: 1, size: 1000 })
    majors.value = res.data?.list || []
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增班级'
  Object.assign(form, { id: null, code: '', name: '', majorId: null, grade: '', studentCount: 0 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑班级'
  Object.assign(form, {
    id: row.id,
    code: row.code,
    name: row.name,
    majorId: row.majorId,
    grade: row.grade,
    studentCount: row.studentCount || 0
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const apiFn = form.id ? updateClass : addClass
    await apiFn(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该班级？', '提示', { type: 'warning' })
    await deleteClass(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { name: '', grade: '' }
  loadData()
}

onMounted(() => { loadMajors(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
