<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成绩录入</span>
          <el-button type="primary" @click="handleSaveAll" :loading="savingAll" :disabled="!hasData">
            保存全部
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="全部学期" clearable filterable @change="loadData">
            <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentNo" placeholder="请输入" clearable @input="debounceLoadData" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.studentName" placeholder="请输入" clearable @input="debounceLoadData" />
        </el-form-item>
      </el-form>

      <!-- 无数据提示 -->
      <el-empty v-if="!loading && studentList.length === 0" description="暂无学生选课记录" />

      <!-- 学员列表 -->
      <div v-for="student in studentList" :key="student.studentId" class="student-section">
        <el-divider content-position="left">
          <span class="student-title">
            {{ student.studentNo }} {{ student.studentName }}
            <el-tag size="small" type="info">
              {{ student.courses?.length || 0 }}门课程
            </el-tag>
            <el-tag
              size="small"
              :type="getCourseProgress(student) === 100 ? 'success' : 'warning'"
            >
              {{ getGradedCount(student) }}/{{ student.courses?.length || 0 }} 已录
            </el-tag>
          </span>
        </el-divider>

        <!-- 课程成绩表格 -->
        <el-table :data="student.courses" v-loading="student._loading" stripe size="small">
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="credits" label="学分" width="60" />
          <el-table-column prop="teacherName" label="授课教师" width="100" />
          <el-table-column label="成绩" width="150">
            <template #default="{ row }">
              <el-input-number
                v-if="canEdit(row)"
                v-model="row.score"
                :min="0"
                :max="100"
                controls-position="right"
                size="small"
              />
              <span v-else>{{ row.score ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.scoreStatus === 1 ? 'success' : 'info'" size="small">
                {{ row.scoreStatus === 1 ? '已录' : '未录' }}
              </el-tag>
              <el-tag v-if="row.scoreStatus === 1" size="small" type="warning" class="modify-count">
                已改{{ row.scoreModifyCount ?? 0 }}次
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button
                v-if="canEdit(row)"
                type="primary"
                link
                size="small"
                @click="handleSave(row)"
              >{{ row.scoreStatus === 1 ? '保存修改' : '提交' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="pagination.total > pagination.size"
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentScoreInputList, getSemesterList, inputScore } from '@/api'
import { useUserStore } from '@/store/user'

let debounceTimer = null

const userStore = useUserStore()
const loading = ref(false)
const savingAll = ref(false)
const studentList = ref([])
const semesters = ref([])
const searchForm = ref({ semesterId: null, studentNo: '', studentName: '' })
const pagination = ref({ current: 1, size: 20, total: 0 })

// 是否有数据
const hasData = computed(() => studentList.value.length > 0)

// 加载成绩录入数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getStudentScoreInputList({ semesterId: searchForm.value.semesterId })
    let filteredList = (res.data || [])
    if (searchForm.value.studentNo) {
      filteredList = filteredList.filter(s => s.studentNo?.includes(searchForm.value.studentNo))
    }
    if (searchForm.value.studentName) {
      filteredList = filteredList.filter(s => s.studentName?.includes(searchForm.value.studentName))
    }
    studentList.value = filteredList.map(s => ({ ...s, _loading: false }))
    pagination.value.total = studentList.value.length
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getSemesterList()
    semesters.value = res.data || []
    // 默认选中最新学期（status=2表示进行中）
    if (semesters.value.length > 0) {
      const active = semesters.value.find(s => s.status === 2)
      searchForm.value.semesterId = active ? active.id : semesters.value[0].id
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 判断是否可以编辑该成绩
const canEdit = (row) => {
  if (!row.scoreStatus) return true
  const modifyCount = row.scoreModifyCount ?? 0
  return modifyCount < 3
}

// 统计已录入成绩数量
const getGradedCount = (student) => {
  return student.courses?.filter(c => c.scoreStatus === 1).length || 0
}

// 计算进度百分比
const getCourseProgress = (student) => {
  if (!student.courses || student.courses.length === 0) return 0
  return Math.round((getGradedCount(student) / student.courses.length) * 100)
}

// 保存单个学生成绩
const handleSave = async (row) => {
  if (row.score == null) {
    ElMessage.warning('请输入成绩')
    return
  }
  try {
    await inputScore({ id: row.studentCourseId, score: row.score, scoreStatus: 1 })
    ElMessage.success('成绩已保存')
    await loadData()
  } catch (e) {
    if (e?.response?.data?.msg) {
      ElMessage.error(e.response.data.msg)
    }
  }
}

// 批量保存全部可编辑的成绩
const handleSaveAll = async () => {
  const unsaved = []
  for (const student of studentList.value) {
    for (const course of student.courses) {
      if (canEdit(course) && course.score != null) {
        unsaved.push({ id: course.studentCourseId, score: course.score, scoreStatus: 1 })
      }
    }
  }
  if (unsaved.length === 0) {
    ElMessage.info('没有需要保存的成绩')
    return
  }
  savingAll.value = true
  try {
    await Promise.all(unsaved.map(item => inputScore(item)))
    ElMessage.success(`成功保存 ${unsaved.length} 条成绩`)
    await loadData()
  } catch (e) {
    if (e?.response?.data?.msg) {
      ElMessage.error(e.response.data.msg)
    }
  } finally {
    savingAll.value = false
  }
}

onMounted(() => {
  loadSemesters()
  loadData()
})

// 搜索防抖，避免频繁请求
const debounceLoadData = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadData, 300)
}
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.student-section { margin-bottom: 16px; }
.student-title { font-weight: bold; font-size: 14px; }
.modify-count { margin-left: 4px; }
</style>
