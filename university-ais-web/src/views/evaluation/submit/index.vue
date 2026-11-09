<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教学评价</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="问卷名称" />
        <el-table-column prop="teacherName" label="被评教师" />
        <el-table-column prop="courseName" label="课程" />
        <el-table-column label="时间范围" width="200">
          <template #default="{ row }">
            {{ row.startTime?.slice(0, 10) }} ~ {{ row.endTime?.slice(0, 10) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :disabled="row.status !== 1"
              @click="handleEvaluate(row)"
            >去评价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 评价弹窗 -->
    <el-dialog v-model="dialogVisible" title="教学评价" width="500px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="score" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="comment" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 通知公告页面 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEvaluationPage, submitEvaluation } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const score = ref(5)
const comment = ref('')
const currentQuestionnaire = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEvaluationPage({ current: 1, size: 20 })
    tableData.value = res.data?.records || res.data?.list || []
  } finally {
    loading.value = false
  }
}

const handleEvaluate = (row) => {
  currentQuestionnaire.value = row
  score.value = 5
  comment.value = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await submitEvaluation({
      questionnaireId: currentQuestionnaire.value.id,
      score: score.value,
      comment: comment.value,
    })
    ElMessage.success('评价提交成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
