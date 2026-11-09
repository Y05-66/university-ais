<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教师列表</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="工号">
          <el-input v-model="searchForm.teacherNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
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
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="collegeName" label="学院" />
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在职' : '离校' }}
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="form.title" placeholder="请选择" style="width: 100%">
            <el-option label="助教" value="助教" />
            <el-option label="讲师" value="讲师" />
            <el-option label="副教授" value="副教授" />
            <el-option label="教授" value="教授" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="办公室">
          <el-input v-model="form.office" placeholder="请输入办公室" />
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
import { getTeacherPage, deleteTeacher, addTeacher, updateTeacher, getCollegeList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ teacherNo: '', name: '', collegeId: null })
const colleges = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增教师')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  teacherNo: '',
  name: '',
  collegeId: null,
  title: '',
  phone: '',
  email: '',
  office: ''
})

const rules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTeacherPage({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('操作失败')
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
  dialogTitle.value = '新增教师'
  Object.assign(form, { id: null, teacherNo: '', name: '', collegeId: null, title: '', phone: '', email: '', office: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑教师'
  Object.assign(form, {
    id: row.id,
    teacherNo: row.teacherNo,
    name: row.name,
    collegeId: row.collegeId,
    title: row.title,
    phone: row.phone,
    email: row.email,
    office: row.office
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const apiFn = form.id ? updateTeacher : addTeacher
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
    await ElMessageBox.confirm('确定删除该教师？', '提示', { type: 'warning' })
    await deleteTeacher(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { teacherNo: '', name: '', collegeId: null }
  loadData()
}

onMounted(() => { loadColleges(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
