<template>
  <div class="dashboard">
    <!-- 管理员视图：全校统计 -->
    <template v-if="userStore.isAdmin()">
      <el-row :gutter="16">
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover" @click="navigateTo('/student/list')">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount || 0 }}</div>
              <div class="stat-label">学生人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover" @click="navigateTo('/teacher/list')">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon size="28"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherCount || 0 }}</div>
              <div class="stat-label">教师人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover" @click="navigateTo('/program/course')">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount || 0 }}</div>
              <div class="stat-label">课程数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
              <el-icon size="28"><School /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalEnrollment || 0 }}</div>
              <div class="stat-label">选课人次</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgScore || 0 }}</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a18cd1, #fbc2eb)">
              <el-icon size="28"><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.passRate || '0%' }}</div>
              <div class="stat-label">及格率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 抢课开关控制 -->
      <el-card shadow="hover" style="margin-top: 16px;">
        <template #header>
          <span class="card-title">抢课功能管理</span>
          <el-tag :type="enrollmentEnabled ? 'success' : 'danger'" size="small" style="margin-left: 12px;">
            {{ enrollmentEnabled ? '已开启' : '已关闭' }}
          </el-tag>
        </template>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span>控制学生是否可以抢课</span>
          <el-switch
            v-model="enrollmentEnabled"
            @change="toggleEnrollment"
            :active-text="'抢课已开启'"
            :inactive-text="'抢课已关闭'"
          />
        </div>
      </el-card>

      <!-- 图表区域 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">成绩分布</span></template>
            <div ref="scoreChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">学期成绩趋势</span></template>
            <div ref="adminTrendChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门课程 & 快捷入口 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">热门课程 TOP5</span></template>
            <el-table :data="stats.popularCourses" stripe size="small">
              <el-table-column type="index" label="#" width="40" />
              <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip />
              <el-table-column prop="enrollmentCount" label="选课人数" width="100" sortable />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">快捷入口</span></template>
            <div class="quick-links">
              <div v-for="item in adminQuickLinks" :key="item.path" class="quick-item" :class="{ disabled: item.disabled }" @click="navigateTo(item.path)">
                <div class="quick-icon" :style="{ background: item.color }"><el-icon size="20"><component :is="item.icon" /></el-icon></div>
                <span class="quick-text">{{ item.text }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 教师视图：个人教学统计 -->
    <template v-else-if="userStore.isTeacher()">
      <el-row :gutter="16">
        <el-col :span="5">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherCourseCount || 0 }}</div>
              <div class="stat-label">授课课程数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherStudentCount || 0 }}</div>
              <div class="stat-label">授课学生数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherAvgScore?.toFixed(2) || '0.00' }}</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a18cd1, #fbc2eb)">
              <el-icon size="28"><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherPassRate || '0%' }}</div>
              <div class="stat-label">及格率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover" @click="navigateTo('/score/student-input')">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
              <el-icon size="28"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">录入成绩</div>
              <div class="stat-label">快捷操作</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门课程 & 快捷入口 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">授课课程选课排行</span></template>
            <el-table :data="stats.teacherPopularCourses" stripe size="small">
              <el-table-column type="index" label="#" width="40" />
              <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip />
              <el-table-column prop="enrollmentCount" label="选课人数" width="100" sortable />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">快捷入口</span></template>
            <div class="quick-links">
              <div v-for="item in teacherQuickLinks" :key="item.path" class="quick-item" :class="{ disabled: item.disabled }" @click="navigateTo(item.path)">
                <div class="quick-icon" :style="{ background: item.color }"><el-icon size="20"><component :is="item.icon" /></el-icon></div>
                <span class="quick-text">{{ item.text }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 学生视图：个人学习统计 -->
    <template v-else-if="userStore.isStudent()">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCourseCount || 0 }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentAvgScore || '0.00' }}</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a18cd1, #fbc2eb)">
              <el-icon size="28"><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentPassRate || '0%' }}</div>
              <div class="stat-label">及格率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover" @click="navigateTo('/enrollment/my')">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
              <el-icon size="28"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">我的选课</div>
              <div class="stat-label">快捷操作</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 学期成绩趋势 & 快捷入口 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">学期成绩趋势</span></template>
            <div ref="studentTrendChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span class="card-title">快捷入口</span></template>
            <div class="quick-links">
              <div v-for="item in studentQuickLinks" :key="item.path" class="quick-item" :class="{ disabled: item.disabled }" @click="navigateTo(item.path)">
                <div class="quick-icon" :style="{ background: item.color }"><el-icon size="20"><component :is="item.icon" /></el-icon></div>
                <span class="quick-text">{{ item.text }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 教学评价提示 -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="24">
          <el-alert title="提示：请及时完成本学期的教学评价" type="info" :closable="false" show-icon />
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getDashboardStats, getEnrollmentEnabled, setEnrollmentEnabled } from '@/api'
import { User, Avatar, Document, School, TrendCharts, Finished, EditPen, Select, Calendar, Bell, SwitchButton } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { markNavigation } from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const scoreChartRef = ref(null)
const adminTrendChartRef = ref(null)
const studentTrendChartRef = ref(null)
let scoreChart = null
let adminTrendChart = null
let studentTrendChart = null

// 统一的导航函数：先验证用户信息有效，再执行路由跳转
const isNavigating = ref(false)

const navigateTo = async (path) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  if (isNavigating.value) return
  isNavigating.value = true
  // 标记导航开始，后续2秒内的401请求会被忽略
  markNavigation()
  try {
    await router.replace(path)
  } catch (err) {
    console.warn('[navigateTo] 导航失败:', err.message)
    if (err.name !== 'NavigationDuplicated') {
      ElMessage.error('页面跳转失败，请重试')
    }
  } finally {
    setTimeout(() => { isNavigating.value = false }, 300)
  }
}

const stats = ref({})
const enrollmentEnabled = ref(false)

// 窗口resize处理（提升到模块级别以避免作用域问题）
const handleResize = () => {
  scoreChart?.resize()
  adminTrendChart?.resize()
  studentTrendChart?.resize()
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

// 切换抢课开关
const toggleEnrollment = async (val) => {
  try {
    await setEnrollmentEnabled(val)
    ElMessage.success(val ? '抢课功能已开启' : '抢课功能已关闭')
  } catch (e) {
    enrollmentEnabled.value = !val
    ElMessage.error('操作失败')
  }
}

// 管理员快捷入口
const adminQuickLinks = ref([
  { text: '学生管理', path: '/student/list', icon: 'User', color: 'linear-gradient(135deg, #667eea, #764ba2)', disabled: false },
  { text: '教师管理', path: '/teacher/list', icon: 'Avatar', color: 'linear-gradient(135deg, #f093fb, #f5576c)', disabled: false },
  { text: '课程管理', path: '/program/course', icon: 'Document', color: 'linear-gradient(135deg, #4facfe, #00f2fe)', disabled: false },
  { text: '开课管理', path: '/program/offering', icon: 'EditPen', color: 'linear-gradient(135deg, #43e97b, #38f9d7)', disabled: false },
  { text: '选课管理', path: '/enrollment/my', icon: 'Select', color: 'linear-gradient(135deg, #fa709a, #fee140)', disabled: false },
  { text: '成绩录入', path: '/score/student-input', icon: 'EditPen', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)', disabled: false },
  { text: '考试安排', path: '/exam/arrange', icon: 'Calendar', color: 'linear-gradient(135deg, #ffecd2, #fcb69f)', disabled: false },
  { text: '公告管理', path: '/notice/list', icon: 'Bell', color: 'linear-gradient(135deg, #89f7fe, #66a6ff)', disabled: false },
])

// 教师快捷入口
const teacherQuickLinks = ref([
  { text: '我的成绩录入', path: '/score/student-input', icon: 'EditPen', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)', disabled: false },
  { text: '我的课表', path: '/enrollment/timetable', icon: 'Calendar', color: 'linear-gradient(135deg, #fa709a, #fee140)', disabled: false },
  { text: '我的监考安排', path: '/exam/my-invigilate', icon: 'Document', color: 'linear-gradient(135deg, #667eea, #764ba2)', disabled: false },
  { text: '我的通知', path: '/notice/my', icon: 'Bell', color: 'linear-gradient(135deg, #89f7fe, #66a6ff)', disabled: false },
])

// 学生快捷入口
const studentQuickLinks = ref([
  { text: '我的选课', path: '/enrollment/my', icon: 'Select', color: 'linear-gradient(135deg, #fa709a, #fee140)', disabled: false },
  { text: '选课列表', path: '/enrollment/select', icon: 'Document', color: 'linear-gradient(135deg, #4facfe, #00f2fe)', disabled: false },
  { text: '我的成绩', path: '/score/my', icon: 'TrendCharts', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)', disabled: false },
  { text: '我的考试安排', path: '/exam/my-exam', icon: 'Calendar', color: 'linear-gradient(135deg, #ffecd2, #fcb69f)', disabled: false },
])

const initCharts = () => {
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
  }
  if (adminTrendChartRef.value) {
    adminTrendChart = echarts.init(adminTrendChartRef.value)
  }
  if (studentTrendChartRef.value) {
    studentTrendChart = echarts.init(studentTrendChartRef.value)
  }
}

const updateCharts = () => {
  // 管理员：成绩分布 + 学期趋势
  if (userStore.isAdmin()) {
    if (scoreChart) {
      const dist = stats.value.scoreDistribution || {}
      scoreChart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['40%', '70%'],
          data: [
            { value: dist.a_count || 0, name: 'A (90+)' },
            { value: dist.b_count || 0, name: 'B (80-89)' },
            { value: dist.c_count || 0, name: 'C (70-79)' },
            { value: dist.d_count || 0, name: 'D (60-69)' },
            { value: dist.f_count || 0, name: 'F (<60)' }
          ],
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
        }]
      })
    }
    if (adminTrendChart) {
      const trend = stats.value.semesterScoreTrend || []
      adminTrendChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: trend.map(t => t.semesterName || t.name || '') },
        yAxis: { type: 'value', min: 0, max: 100 },
        series: [{
          type: 'line', data: trend.map(t => parseFloat(t.avgScore) || 0),
          smooth: true, areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#409eff' }
        }]
      })
    }
  }
  // 学生：学期趋势
  else if (userStore.isStudent()) {
    if (studentTrendChart) {
      const trend = stats.value.studentScoreTrend || []
      studentTrendChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: trend.map(t => t.semesterName || t.name || '') },
        yAxis: { type: 'value', min: 0, max: 100 },
        series: [{
          type: 'line', data: trend.map(t => parseFloat(t.avgScore) || 0),
          smooth: true, areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#409eff' }
        }]
      })
    }
  }
}

const loadData = async () => {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.value = res.data
      updateCharts()
    }
  } catch (e) {
    console.error('Failed to load dashboard:', e)
  }
}

onMounted(() => {
  initCharts()
  loadData()
  loadEnrollmentStatus()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  scoreChart?.dispose()
  adminTrendChart?.dispose()
  studentTrendChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.dashboard { padding: 16px; }
.stat-card {
  display: flex; align-items: center; padding: 20px; cursor: pointer; transition: all 0.3s;
  min-height: 100px;
  box-sizing: border-box;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
  .stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-info { margin-left: 16px; flex: 1; display: flex; flex-direction: column; justify-content: center; }
  .stat-value { font-size: 28px; font-weight: bold; color: #303133; line-height: 1.2; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
}
.el-col { height: 100%; }
.el-col > .el-card { height: 100%; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.quick-links { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; padding: 8px 0; }
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px; border-radius: 12px; cursor: pointer; transition: all 0.2s; background: #f5f7fa; }
.quick-item:hover:not(.disabled) { background: #ecf5ff; transform: translateY(-2px); }
.quick-item.disabled { opacity: 0.4; cursor: not-allowed; }
.quick-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; }
.quick-text { font-size: 12px; color: #606266; text-align: center; }
</style>
