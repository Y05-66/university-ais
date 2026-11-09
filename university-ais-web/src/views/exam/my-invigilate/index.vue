<!-- 教师个人监考安排页。加载当前教师被分配的监考任务，按列表展示。 -->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的监考</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="classroom" label="监考教室" width="120" />
        <el-table-column prop="seatCount" label="座位数" width="80" />
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无监考安排" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyInvigilation } from '@/api'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyInvigilation()
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载监考数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
