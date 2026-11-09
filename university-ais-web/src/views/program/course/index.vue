<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="课程编号">
          <el-input v-model="searchForm.courseNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable>
            <el-option label="必修" value="1" />
            <el-option label="选修" value="2" />
            <el-option label="公选" value="3" />
            <el-option label="实践" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="englishName" label="英文名称" />
        <el-table-column prop="collegeName" label="开课学院" />
        <el-table-column label="类别" width="80">
          <template #default="{ row }">
            <el-tag :type="['', 'primary', 'warning', 'success', 'info'][row.category]" size="small">
              {{ ['', '必修', '选修', '公选', '实践'][row.category] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credits" label="学分" width="60" />
        <el-table-column prop="totalHours" label="学时" width="60" />
        <el-table-column label="考核方式" width="80">
          <template #default="{ row }">{{ row.examType === 1 ? '考试' : '考查' }}</template>
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
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="课程编号" prop="courseNo">
          <el-input v-model="form.courseNo" placeholder="如CS101" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="form.englishName" placeholder="请输入英文名称" />
        </el-form-item>
        <el-form-item label="开课学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择" filterable>
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="必修" value="1" />
            <el-option label="选修" value="2" />
            <el-option label="公选" value="3" />
            <el-option label="实践" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="form.credits" :min="0" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="总学时" prop="totalHours">
          <el-input-number v-model="form.totalHours" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="理论学时">
          <el-input-number v-model="form.theoryHours" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="实践学时">
          <el-input-number v-model="form.practiceHours" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="考核方式" prop="examType">
          <el-select v-model="form.examType" placeholder="请选择">
            <el-option label="考试" value="1" />
            <el-option label="考查" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="form.maxStudents" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入课程简介" />
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
/** 教务管理页面 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePage, deleteCourse, addCourse, updateCourse, getCollegeList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ courseNo: '', name: '', category: null })
const colleges = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  courseNo: '',
  name: '',
  englishName: '',
  collegeId: null,
  category: 1,
  credits: 3,
  totalHours: 48,
  theoryHours: 36,
  practiceHours: 12,
  examType: 1,
  maxStudents: 60,
  description: ''
})

const rules = {
  courseNo: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  examType: [{ required: true, message: '请选择考核方式', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoursePage({
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
  dialogTitle.value = '新增课程'
  Object.assign(form, {
    id: null, courseNo: '', name: '', englishName: '', collegeId: null,
    category: 1, credits: 3, totalHours: 48, theoryHours: 36, practiceHours: 12,
    examType: 1, maxStudents: 60, description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateCourse(form)
      ElMessage.success('修改成功')
    } else {
      await addCourse(form)
      ElMessage.success('新增成功')
    }
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
    await ElMessageBox.confirm('确定删除该课程？', '提示', { type: 'warning' })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { courseNo: '', name: '', category: null }
  loadData()
}

onMounted(() => { loadColleges(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
