<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>开课管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="请选择" clearable filterable>
            <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="searchForm.teacherId" placeholder="请选择" clearable filterable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseNo" label="课程编号" width="100" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="semesterName" label="学期" width="140" />
        <el-table-column prop="classroom" label="教室" width="120" />
        <el-table-column prop="schedule" label="上课时间" />
        <el-table-column prop="weeks" label="周次" width="80" />
        <el-table-column label="选课人数" width="100">
          <template #default="{ row }">
            {{ row.enrolled }} / {{ row.maxStudents }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
              {{ ['正常', '已满', '已取消'][row.status - 1] || '-' }}
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
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" filterable>
            <el-option v-for="c in courses" :key="c.id" :label="`${c.courseNo} ${c.name}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择教师" filterable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" filterable>
            <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="form.classroom" placeholder="如：教学楼A101" />
        </el-form-item>
        <el-form-item label="上课时间" prop="schedule">
          <el-input v-model="form.schedule" placeholder="如：周一3-4节,周三1-2节" />
        </el-form-item>
        <el-form-item label="周次" prop="weeks">
          <el-input v-model="form.weeks" placeholder="如：1-16周" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" :max="500" />
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
import { getCourseOfferPage, deleteCourseOffer, addCourseOffer, updateCourseOffer, getCourses, getTeachers, getSemesterList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ semesterId: null, teacherId: null })
const courses = ref([])
const teachers = ref([])
const semesters = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增开课')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  courseId: null,
  teacherId: null,
  semesterId: null,
  classroom: '',
  schedule: '',
  weeks: '1-16周',
  maxStudents: 60
})

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCourseOfferPage({
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
    const [cRes, tRes, sRes] = await Promise.all([
      getCourses({ current: 1, size: 1000 }),
      getTeachers({ current: 1, size: 1000 }),
      getSemesterList({ current: 1, size: 10 })
    ])
    courses.value = cRes.data?.list || []
    teachers.value = tRes.data?.list || []
    semesters.value = sRes.data?.list || []
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增开课'
  Object.assign(form, { id: null, courseId: null, teacherId: null, semesterId: null, classroom: '', schedule: '', weeks: '1-16周', maxStudents: 60 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑开课'
  Object.assign(form, {
    id: row.id, courseId: row.courseId, teacherId: row.teacherId,
    semesterId: row.semesterId, classroom: row.classroom,
    schedule: row.schedule, weeks: row.weeks, maxStudents: row.maxStudents
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateCourseOffer(form)
      ElMessage.success('修改成功')
    } else {
      await addCourseOffer(form)
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
    await ElMessageBox.confirm('确定删除该开课？', '提示', { type: 'warning' })
    await deleteCourseOffer(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { semesterId: null, teacherId: null }
  loadData()
}

onMounted(() => { loadOptions(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
