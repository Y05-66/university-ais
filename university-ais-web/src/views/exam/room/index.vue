<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考场分配</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增考场
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="考场名称">
          <el-input v-model="searchForm.classroom" placeholder="请输入考场名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="classroom" label="考场名称" width="150" />
        <el-table-column prop="seatCount" label="座位数" width="100" />
        <el-table-column label="已分配考试数" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.examCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '使用中' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="考场名称" prop="classroom">
          <el-input v-model="form.classroom" placeholder="如：教学楼A101" />
        </el-form-item>
        <el-form-item label="座位数" prop="seatCount">
          <el-input-number v-model="form.seatCount" :min="1" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">使用中</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 考试管理页面 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getExamRoomPage, addExamRoom, updateExamRoom, deleteExamRoom } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ classroom: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增考场')
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  classroom: '',
  seatCount: 100,
  status: 1,
  examCount: 0
})

const rules = {
  classroom: [{ required: true, message: '请输入考场名称', trigger: 'blur' }],
  seatCount: [{ required: true, message: '请输入座位数', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExamRoomPage({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = res.data?.records || res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载考场数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增考场'
  Object.assign(form, { id: null, classroom: '', seatCount: 100, status: 1, examCount: 0 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑考场'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateExamRoom(form)
      ElMessage.success('修改成功')
    } else {
      await addExamRoom(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该考场？', '提示', { type: 'warning' })
    await deleteExamRoom(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleReset = () => {
  searchForm.value = { classroom: '' }
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
