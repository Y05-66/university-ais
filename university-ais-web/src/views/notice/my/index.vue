<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的通知</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部通知" name="all">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.type === 1" type="danger" size="small">系统公告</el-tag>
                <el-tag v-else-if="row.type === 2" type="warning" size="small">教务通知</el-tag>
                <el-tag v-else type="info" size="small">选课通知</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="160" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'info' : 'primary'" size="small">
                  {{ row.isRead ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @current-change="loadData"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>

        <el-tab-pane label="未读通知" name="unread">
          <el-empty v-if="tableData.length === 0" description="暂无未读通知" />
          <el-table v-else :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.type === 1" type="danger" size="small">系统公告</el-tag>
                <el-tag v-else-if="row.type === 2" type="warning" size="small">教务通知</el-tag>
                <el-tag v-else type="info" size="small">选课通知</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="160" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="tableData.length > 0"
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @current-change="loadData"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="通知详情" width="600px">
      <div class="notice-detail">
        <h3>{{ currentNotice?.title }}</h3>
        <div class="notice-meta">
          <span>类型：{{ noticeTypeMap[currentNotice?.type]?.label }}</span>
          <span>发布时间：{{ currentNotice?.publishTime }}</span>
        </div>
        <div class="notice-content">{{ currentNotice?.content }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 通知公告页面 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNoticePage } from '@/api'
import { useUserStore } from '@/store/user'

const activeTab = ref('all')
const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 10, total: 0 })
const dialogVisible = ref(false)
const currentNotice = ref(null)

const userStore = useUserStore()

const noticeTypeMap = {
  1: { label: '系统公告', type: 'danger' },
  2: { label: '教务通知', type: 'warning' },
  3: { label: '选课通知', type: 'info' }
}

// 根据用户角色获取应展示的通知目标角色
const userTargetRole = computed(() => {
  if (userStore.isAdmin()) return '' // 管理员看全部
  if (userStore.isTeacher()) return 'teacher'
  if (userStore.isStudent()) return 'student'
  return ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size
    }
    if (activeTab.value === 'unread') {
      params.status = 1
    }
    const res = await getNoticePage(params)
    let list = res.data?.list || []

    // 过滤掉已过期的通知
    const now = new Date()
    list = list.filter(item => {
      if (!item.expireTime) return true // 无过期时间的显示
      return new Date(item.expireTime) > now
    })

    // 按角色过滤
    const role = userTargetRole.value
    if (role) {
      list = list.filter(item => !item.targetRole || item.targetRole === role)
    }

    if (activeTab.value === 'unread') {
      tableData.value = list.filter(item => !item.isRead)
    } else {
      tableData.value = list
    }
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  pagination.value.current = 1
  loadData()
}

const handleView = async (row) => {
  currentNotice.value = row
  dialogVisible.value = true
  // TODO: 标记为已读
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.notice-detail {
  padding: 16px;
}
.notice-detail h3 {
  margin-bottom: 16px;
  font-size: 18px;
  color: #303133;
}
.notice-meta {
  margin-bottom: 16px;
  color: #909399;
  font-size: 14px;
}
.notice-meta span {
  margin-right: 20px;
}
.notice-content {
  line-height: 1.8;
  color: #606266;
}
</style>
