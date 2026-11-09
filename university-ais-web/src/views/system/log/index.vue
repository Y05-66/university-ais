<template>
  <div class="page-container">
    <el-card>
      <template #header><span>操作日志</span></template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作描述" />
        <el-table-column prop="method" label="请求方法" width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" @current-change="loadData" @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end" />
    </el-card>
  </div>
</template>

<script setup>
/** 系统管理页面 */
import { ref, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getLogPage } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 20, total: 0 })
const searchForm = ref({ username: '', status: null })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLogPage({ current: pagination.value.current, size: pagination.value.size, ...searchForm.value })
    tableData.value = res.data.list || []
    pagination.value.total = res.data.total || 0
  } finally { loading.value = false }
}

const handleReset = () => {
  searchForm.value = { username: '', status: null }
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 16px; }
</style>
