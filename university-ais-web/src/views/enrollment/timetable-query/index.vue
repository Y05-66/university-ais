<template>
  <div class="timetable-query-page">
    <el-card shadow="never" class="timetable-card">
      <template #header>
        <span class="card-title">课表查询</span>
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="类型">
              <el-radio-group v-model="queryType" @change="handleTypeChange">
                <el-radio-button label="teacher">教师</el-radio-button>
                <el-radio-button label="student">学生</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="queryType === 'teacher' ? '教师' : '学生'">
              <el-select
                v-model="selectedId"
                :placeholder="queryType === 'teacher' ? '选择教师' : '选择学生'"
                filterable
                clearable
                @change="handleSelectChange"
                style="width:130px"
              >
                <el-option v-for="item in options" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="学期">
              <el-select
                v-model="selectedSemester"
                placeholder="学期"
                clearable
                filterable
                @change="loadData"
                style="width:150px"
              >
                <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <div v-loading="loading" class="timetable-wrapper">
        <table class="timetable-grid">
          <thead>
            <tr>
              <th class="period-col">周 / 节次</th>
              <th v-for="(day, i) in weekDays" :key="i">{{ day }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(period, rowIdx) in periods" :key="rowIdx">
              <td class="period-col">
                <div class="period-name">{{ period.label }}</div>
                <div class="period-time">{{ period.time }}</div>
              </td>
              <td v-for="(day, colIdx) in weekDays" :key="colIdx" class="cell">
                <div
                  v-if="grid[rowIdx][colIdx]"
                  class="course-block"
                  :style="{ background: grid[rowIdx][colIdx].bg, color: grid[rowIdx][colIdx].text }"
                  :title="grid[rowIdx][colIdx].name + '\n' + grid[rowIdx][colIdx].room + '  ' + grid[rowIdx][colIdx].weeks"
                >
                  <div class="block-cat">{{ grid[rowIdx][colIdx].categoryLabel }}</div>
                  <div class="block-name">{{ grid[rowIdx][colIdx].name }}</div>
                  <div class="block-room">{{ grid[rowIdx][colIdx].room }}</div>
                  <div class="block-weeks">{{ grid[rowIdx][colIdx].weeks }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTimetableByTeacher, getTimetableByStudent, getSemesterList, getTeachers, getStudents } from '@/api'

const queryType = ref('teacher')
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const periods = [
  { label: '第一大节', time: '08:00-09:40', start: 1 },
  { label: '第二大节', time: '10:00-11:40', start: 3 },
  { label: '第三大节', time: '14:00-15:40', start: 5 },
  { label: '第四大节', time: '16:00-17:40', start: 7 },
  { label: '第五大节', time: '19:00-20:40', start: 9 },
]

const loading = ref(false)
const semesters = ref([])
const options = ref([])
const selectedId = ref(null)
const selectedSemester = ref(null)
const rawTimetable = ref([])

const categoryColors = {
  1: { bg: '#fecaca', text: '#7f1d1d' },
  2: { bg: '#bfdbfe', text: '#1e3a8a' },
  3: { bg: '#a5f3fc', text: '#155e75' },
  4: { bg: '#bbf7d0', text: '#14532d' },
}
const categoryLabels = { 1: '必修', 2: '选修', 3: '公选', 4: '实践' }

const grid = computed(() => {
  const g = Array.from({ length: 5 }, () => Array(7).fill(null))
  rawTimetable.value.forEach((entry) => {
    const dayIdx = weekDays.indexOf(entry.dayName)
    if (dayIdx < 0) return
    const p = entry.period
    let rowIdx = -1
    if (p === 1 || p === 2) rowIdx = 0
    else if (p === 3 || p === 4) rowIdx = 1
    else if (p === 5 || p === 6) rowIdx = 2
    else if (p === 7 || p === 8) rowIdx = 3
    else if (p === 9 || p === 10) rowIdx = 4
    if (rowIdx >= 0 && !g[rowIdx][dayIdx]) {
      const cat = entry.category || 1
      g[rowIdx][dayIdx] = {
        name: (entry.courseName || '').slice(0, 10),
        room: entry.classroom || '',
        weeks: entry.weeks || '',
        bg: (categoryColors[cat] || categoryColors[1]).bg,
        text: (categoryColors[cat] || categoryColors[1]).text,
        categoryLabel: (categoryLabels[cat] || '必修'),
      }
    }
  })
  return g
})

const loadData = async () => {
  if (!selectedId.value) { rawTimetable.value = []; return }
  loading.value = true
  try {
    const res = queryType.value === 'teacher'
      ? await getTimetableByTeacher(selectedId.value, selectedSemester.value)
      : await getTimetableByStudent(selectedId.value, selectedSemester.value)
    if (res.code === 200) rawTimetable.value = res.data || []
  } catch (e) { ElMessage.error('加载课表失败') }
  finally { loading.value = false }
}

const handleTypeChange = () => {
  selectedId.value = null
  rawTimetable.value = []
  loadOptions()
}

const handleSelectChange = () => { loadData() }

const loadSemesters = async () => {
  try {
    const res = await getSemesterList({ current: 1, size: 20 })
    if (res.code === 200) semesters.value = res.data?.list || []
  } catch (e) { /* ignore */ }
}

const loadOptions = async () => {
  try {
    const res = queryType.value === 'teacher' ? await getTeachers() : await getStudents()
    if (res.code === 200) options.value = res.data || []
  } catch (e) { ElMessage.error('加载列表失败') }
}

onMounted(() => { loadSemesters(); loadOptions() })
</script>

<style scoped>
.timetable-query-page { padding: 16px; }
.timetable-card :deep(.el-card__header) {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.filter-bar { display: flex; align-items: center; }
.filter-form { margin: 0; }
.timetable-wrapper { overflow-x: auto; }
.timetable-grid {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  min-width: 700px;
}
.timetable-grid th,
.timetable-grid td {
  border: 1px solid #e4e7ed;
  text-align: center;
  vertical-align: middle;
  padding: 0;
}
.timetable-grid th {
  background: #e8f0fe;
  color: #606266;
  font-weight: 600;
  padding: 10px 4px;
  height: 36px;
}
.period-col {
  width: 90px;
  background: #e8f0fe !important;
}
.period-name { font-weight: 600; color: #303133; line-height: 1.4; }
.period-time { font-size: 11px; color: #909399; margin-top: 2px; }
.cell {
  height: 72px;
  width: 110px;
  padding: 4px;
  background: #fff;
}
.course-block {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  padding: 4px 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1px;
}
.block-name {
  font-weight: 700;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}
.block-cat {
  font-size: 10px;
  opacity: 0.75;
  margin-bottom: 1px;
}
.block-room { font-size: 11px; opacity: 0.8; }
.block-weeks { font-size: 10px; color: #7f1d1d; opacity: 0.65; }
</style>
