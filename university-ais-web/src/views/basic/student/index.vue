<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="searchForm.collegeId" placeholder="请选择" clearable filterable>
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
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
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="collegeName" label="学院" />
        <el-table-column prop="majorName" label="专业" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column label="GPA" width="80">
          <template #default="{ row }">{{ row.gpa ? row.gpa.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column label="学分" width="80">
          <template #default="{ row }">{{ row.totalCredits || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在读' : '其他' }}
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
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="如2024" />
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
import { getStudentPage, deleteStudent, addStudent, updateStudent, getCollegeList, getMajorPage, getClassList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ studentNo: '', name: '', collegeId: null, grade: '' })
const colleges = ref([])
const majors = ref([])
const classes = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  collegeId: null,
  majorId: null,
  classId: null,
  grade: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStudentPage({
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

const loadOptions = async () => {
  try {
    const [cRes, mRes, clRes] = await Promise.all([
      getCollegeList(),
      getMajorPage({ current: 1, size: 1000 }),
      getClassList({ current: 1, size: 1000 })
    ])
    colleges.value = cRes.data || []
    majors.value = mRes.data?.list || []
    classes.value = clRes.data?.list || []
  } catch (e) {
    ElMessage.error('加载选项数据失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增学生'
  Object.assign(form, { id: null, studentNo: '', name: '', collegeId: null, majorId: null, classId: null, grade: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学生'
  Object.assign(form, {
    id: row.id,
    studentNo: row.studentNo,
    name: row.name,
    collegeId: row.collegeId,
    majorId: row.majorId,
    classId: row.classId,
    grade: row.grade
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const apiFn = form.id ? updateStudent : addStudent
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
    await ElMessageBox.confirm('确定删除该学生？', '提示', { type: 'warning' })
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { studentNo: '', name: '', collegeId: null, grade: '' }
  loadData()
}

onMounted(() => { loadOptions(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
