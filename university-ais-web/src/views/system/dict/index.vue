<template>
  <div class="page-container">
    <el-card>
      <template #header><span>数据字典</span></template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="dictType" label="字典类型" width="150" />
        <el-table-column prop="label" label="标签" />
        <el-table-column prop="value" label="值" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
/** 系统管理页面 */
import { ref, onMounted } from 'vue'
import { getDictList } from '@/api'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDictList()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
