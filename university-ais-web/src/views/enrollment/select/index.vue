<template>
  <div class="page-container">
    <el-card>
      <template #header><span>选课</span></template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="请选择" filterable @change="loadData">
            <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credits" label="学分" width="60" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="schedule" label="上课时间" width="180" />
        <el-table-column prop="weeks" label="周次" width="100" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column label="已选/容量" width="100">
          <template #default="{ row }">
            {{ row.enrolled }} / {{ row.maxStudents }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.available > 0 ? 'success' : 'danger'" size="small">
              {{ row.available > 0 ? '可选' : '已满' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :disabled="row.available <= 0"
              @click="handleSelect(row)"
            >选课</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: center;"
      />
    </el-card>
  </div>
</template>

<script setup>
/** 选课管理页面 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCourses, selectCourse, getSemesterList, getEnrollmentEnabled } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const semesters = ref([])
const searchForm = ref({ semesterId: null })
const pagination = ref({ current: 1, size: 20, total: 0 })
const enrollmentEnabled = ref(false)

const loadData = async () => {
  if (!searchForm.value.semesterId) return
  loading.value = true
  try {
    const res = await getAvailableCourses(searchForm.value.semesterId)
    tableData.value = res.data || []
    pagination.value.total = res.data?.length || 0
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const loadSemesters = async () => {
  try {
    const res = await getSemesterList()
    semesters.value = res.data || []
    // 默认选中第一个学期
    if (semesters.value.length > 0 && !searchForm.value.semesterId) {
      searchForm.value.semesterId = semesters.value[0].id
      loadData()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 加载抢课开关状态
const loadEnrollmentStatus = async () => {
  try {
    const res = await getEnrollmentEnabled()
    enrollmentEnabled.value = res.data || false
  } catch (e) {
    enrollmentEnabled.value = false
  }
}

const handleSelect = async (row) => {
  if (!enrollmentEnabled.value) {
    ElMessage.warning('抢课功能暂未开启，请等待管理员通知')
    return
  }
  try {
    await selectCourse(row.id)
    ElMessage.success('选课成功')
    loadData()
  } catch (e) {
    // 后端返回 BizException 时，e.message 包含具体错误信息
    const msg = e?.response?.data?.msg || e?.message || '选课失败'
    ElMessage.warning(msg)
    // 课程已满/冲突时刷新列表
    if (msg !== '选课失败') loadData()
  }
}

onMounted(() => {
  loadEnrollmentStatus()
  loadSemesters()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 16px; }
</style>
