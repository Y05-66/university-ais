<!-- 学生个人考试安排页。加载当前学生已选课程对应的考试信息，按列表展示。 -->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的考试安排</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="classroom" label="考场" width="120" />
        <el-table-column prop="seatCount" label="座位数" width="80" />
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无考试安排" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyExams } from '@/api'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyExams()
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载考试数据失败')
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
