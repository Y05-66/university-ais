<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的选课</span>
          <el-button type="primary" @click="router.push('/enrollment/select')">选择课程</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credits" label="学分" width="60" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="schedule" label="上课时间" width="180" />
        <el-table-column prop="weeks" label="周次" width="100" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column label="成绩" width="80">
          <template #default="{ row }">
            <span v-if="row.score != null">{{ row.score }}</span>
            <span v-else style="color: #909399">--</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已选' : '已退' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              link
              size="small"
              @click="handleDrop(row)"
            >退课</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEnrollmentMy, dropCourse } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEnrollmentMy({
      current: pagination.value.current,
      size: pagination.value.size
    })
    tableData.value = res.data?.records || res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleDrop = async (row) => {
  try {
    await ElMessageBox.confirm('确定要退选这门课程吗？', '提示', { type: 'warning' })
    await dropCourse(row.courseOfferId)
    ElMessage.success('退课成功')
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
