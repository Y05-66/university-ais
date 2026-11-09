<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>监考安排</span>
          <el-button v-if="userStore.isAdmin()" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="教师">
          <el-select v-model="searchForm.teacherId" placeholder="请选择" clearable filterable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.examDate" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="teacherName" label="监考教师" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="classroom" label="考场" width="120" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="监考教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择监考教师" filterable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试安排" prop="examId">
          <el-select v-model="form.examId" placeholder="请选择考试安排" filterable>
            <el-option v-for="e in exams" :key="e.id" :label="`${e.courseName} (${e.examDate})`" :value="e.id" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getExamInvigilatePage, addExamInvigilate, updateExamInvigilate, deleteExamInvigilate, getExamPage, getTeachers } from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ teacherId: null, examDate: '' })
const teachers = ref([])
const exams = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增监考')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  teacherId: null,
  examId: null
})

const rules = {
  teacherId: [{ required: true, message: '请选择监考教师', trigger: 'change' }],
  examId: [{ required: true, message: '请选择考试安排', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExamInvigilatePage({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = res.data?.records || res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载监考数据失败')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [tRes, eRes] = await Promise.all([
      getTeachers({ current: 1, size: 1000 }),
      getExamPage({ current: 1, size: 500 })
    ])
    teachers.value = tRes.data?.records || tRes.data?.list || []
    exams.value = eRes.data?.records || eRes.data?.list || []
  } catch (e) {
    ElMessage.error('加载选项数据失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增监考'
  Object.assign(form, { id: null, teacherId: null, examId: null })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑监考'
  Object.assign(form, { id: row.id, teacherId: row.teacherId, examId: row.examId })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateExamInvigilate(form)
      ElMessage.success('修改成功')
    } else {
      await addExamInvigilate(form)
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
    await ElMessageBox.confirm('确定删除该监考安排？', '提示', { type: 'warning' })
    await deleteExamInvigilate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleReset = () => {
  searchForm.value = { teacherId: null, examDate: '' }
  loadData()
}

onMounted(() => { loadOptions(); loadData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
