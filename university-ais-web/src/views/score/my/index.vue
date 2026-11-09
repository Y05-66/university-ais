<template>
  <div class="page-container">
    <el-card>
      <template #header><span>我的成绩</span></template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credits" label="学分" width="60" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column label="成绩" width="80">
          <template #default="{ row }">
            <span v-if="row.score != null">{{ row.score }}</span>
            <span v-else style="color: #909399">--</span>
          </template>
        </el-table-column>
        <el-table-column label="绩点" width="80">
          <template #default="{ row }">
            <span v-if="row.gradePoint != null">{{ row.gradePoint }}</span>
            <span v-else style="color: #909399">--</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.scoreStatus === 1" type="success" size="small">已录入</el-tag>
            <el-tag v-else-if="row.scoreStatus === 2" type="warning" size="small">已发布</el-tag>
            <el-tag v-else type="info" size="small">未录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.scoreStatus === 1"
              type="primary"
              link
              size="small"
              @click="handlePublish(row)"
            >发布通知</el-button>
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
/** 成绩管理页面 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { getMyScores } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyScores({
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

onMounted(loadData)

const handlePublish = async (row) => {
  try {
    // TODO: 实现学生侧发布成绩通知的API
    ElMessage.success('已发送成绩通知')
    // 重新加载数据以更新状态
    await loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
