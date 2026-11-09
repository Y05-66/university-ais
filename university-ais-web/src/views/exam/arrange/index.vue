<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考试安排</span>
          <el-button v-if="userStore.isAdmin()" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="请选择" clearable filterable>
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="searchForm.examType" placeholder="请选择" clearable>
            <el-option label="期末" value="1" />
            <el-option label="期中" value="2" />
            <el-option label="补考" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column label="考试类型" width="80">
          <template #default="{ row }">
            <el-tag :type="['', 'primary', 'success', 'warning'][row.examType]" size="small">
              {{ ['', '期末', '期中', '补考'][row.examType] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="classroom" label="考场" width="100" />
        <el-table-column prop="seatCount" label="座位数" width="80" />
        <el-table-column prop="invigilator" label="监考教师" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="userStore.isAdmin()" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="userStore.isAdmin()" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="开课" prop="courseOfferId">
          <el-select v-model="form.courseOfferId" placeholder="请选择开课" filterable>
            <el-option v-for="o in offerings" :key="o.id" :label="`${o.courseName} (${o.teacherName})`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="form.examType" placeholder="请选择">
            <el-option label="期末" value="1" />
            <el-option label="期中" value="2" />
            <el-option label="补考" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker v-model="form.examDate" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" placeholder="请选择时间" value-format="HH:mm" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" placeholder="请选择时间" value-format="HH:mm" />
        </el-form-item>
        <el-form-item label="考场" prop="classroom">
          <el-input v-model="form.classroom" placeholder="如：教学楼A101" />
        </el-form-item>
        <el-form-item label="座位数" prop="seatCount">
          <el-input-number v-model="form.seatCount" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="监考教师">
          <el-input v-model="form.invigilator" placeholder="多个用逗号分隔" />
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
/** 考试管理页面 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamPage, deleteExam, addExam, updateExam, getCourses, getCourseOfferList } from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ courseId: null, examType: null })
const courses = ref([])
const offerings = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增考试')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  courseOfferId: null,
  examType: 1,
  examDate: '',
  startTime: '',
  endTime: '',
  classroom: '',
  seatCount: 100,
  invigilator: ''
})

const rules = {
  courseOfferId: [{ required: true, message: '请选择开课', trigger: 'change' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExamPage({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = res.data?.records || res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载考试数据失败')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [cRes, oRes] = await Promise.all([
      getCourses({ current: 1, size: 1000 }),
      getCourseOfferList({ current: 1, size: 1000 })
    ])
    courses.value = cRes.data?.records || cRes.data?.list || []
    offerings.value = oRes.data?.records || oRes.data?.list || []
  } catch (e) {
    ElMessage.error('加载选项数据失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增考试'
  Object.assign(form, { id: null, courseOfferId: null, examType: 1, examDate: '', startTime: '', endTime: '', classroom: '', seatCount: 100, invigilator: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑考试'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateExam(form)
      ElMessage.success('修改成功')
    } else {
      await addExam(form)
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
    await ElMessageBox.confirm('确定删除该考试安排？', '提示', { type: 'warning' })
    await deleteExam(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleReset = () => {
  searchForm.value = { courseId: null, examType: null }
  loadData()
}

onMounted(() => { loadOptions(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
