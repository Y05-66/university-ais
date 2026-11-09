<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知公告</span>
          <el-button type="primary" :icon="Plus" size="small" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable>
            <el-option label="系统公告" :value="1" />
            <el-option label="教务通知" :value="2" />
            <el-option label="选课通知" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="danger" size="small">系统公告</el-tag>
            <el-tag v-else-if="row.type === 2" type="warning" size="small">教务通知</el-tag>
            <el-tag v-else type="info" size="small">选课通知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetRole" label="目标角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="!row.targetRole" type="info" size="small">全部</el-tag>
            <el-tag v-else-if="row.targetRole === 'student'" type="success" size="small">学生</el-tag>
            <el-tag v-else-if="row.targetRole === 'teacher'" type="warning" size="small">教师</el-tag>
            <el-tag v-else-if="row.targetRole === 'admin'" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">{{ row.targetRole }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column prop="expireTime" label="过期时间" width="160">
          <template #default="{ row }">
            <span v-if="row.expireTime">{{ row.expireTime }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" :value="1" />
            <el-option label="教务通知" :value="2" />
            <el-option label="选课通知" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标角色" prop="targetRole">
          <el-select v-model="form.targetRole" placeholder="请选择" clearable style="width: 100%">
            <el-option label="全部用户" value="" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看公告详情对话框 -->
    <el-dialog v-model="viewVisible" :title="viewTitle" width="600px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ viewForm.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="viewForm.type === 1" type="danger">系统公告</el-tag>
          <el-tag v-else-if="viewForm.type === 2" type="warning">教务通知</el-tag>
          <el-tag v-else type="info">选课通知</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标角色">
          <el-tag v-if="!viewForm.targetRole" type="info">全部</el-tag>
          <el-tag v-else-if="viewForm.targetRole === 'student'" type="success">学生</el-tag>
          <el-tag v-else-if="viewForm.targetRole === 'teacher'" type="warning">教师</el-tag>
          <el-tag v-else-if="viewForm.targetRole === 'admin'" type="danger">管理员</el-tag>
          <el-tag v-else type="info">{{ viewForm.targetRole }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewForm.status === 1 ? 'success' : 'info'">
            {{ viewForm.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ viewForm.publishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; min-height: 80px;">{{ viewForm.content || '暂无内容' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(viewForm)">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 通知公告页面 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getNoticePage, addNotice, updateNotice, deleteNotice, getNoticeDetail } from '@/api'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ title: '', type: null, status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const formRef = ref(null)
const form = reactive({ id: null, title: '', type: 2, content: '', status: 1, targetRole: '' })
const submitLoading = ref(false)

const viewVisible = ref(false)
const viewTitle = ref('')
const viewForm = reactive({
  id: null,
  title: '',
  type: 2,
  content: '',
  status: 1,
  targetRole: '',
  publishTime: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticePage({ current: pagination.value.current, size: pagination.value.size, ...searchForm.value })
    tableData.value = res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, title: '', type: 2, content: '', status: 1, targetRole: '' })
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

const handleView = (row) => {
  Object.assign(viewForm, row)
  viewTitle.value = row.title
  viewVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const apiFn = form.id ? updateNotice : addNotice
    await apiFn(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReset = () => {
  searchForm.value = { title: '', type: null, status: null }
  loadData()
}

onMounted(() => {
  loadData()
  // 如果URL有id参数，自动打开查看详情
  const id = route.query.id
  if (id) {
    getNoticeDetail(id).then(res => {
      if (res.data) {
        Object.assign(viewForm, res.data)
        viewTitle.value = res.data.title
        viewVisible.value = true
      }
    }).catch(() => {})
  }
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
