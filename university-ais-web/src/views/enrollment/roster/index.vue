<template>
  <div class="page-container">
    <el-card>
      <template #header><span>选课名单</span></template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="开课">
          <el-select v-model="searchForm.courseOfferId" placeholder="请选择" filterable @change="loadData">
            <el-option v-for="o in offers" :key="o.id" :label="`${o.courseName} (${o.teacherName})`" :value="o.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="selectTime" label="选课时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag type="success" size="small">已选</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
/** 选课管理页面 */
import { ref, onMounted } from 'vue'
import { getCourseOfferList, getEnrollmentRoster } from '@/api'

const loading = ref(false)
const tableData = ref([])
const offers = ref([])
const searchForm = ref({ courseOfferId: null })

const loadData = async () => {
  if (!searchForm.value.courseOfferId) return
  loading.value = true
  try {
    const res = await getEnrollmentRoster(searchForm.value.courseOfferId)
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const loadOffers = async () => {
  try {
    const res = await getCourseOfferList({ current: 1, size: 1000 })
    offers.value = res.data || []
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => { loadOffers() })
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 16px; }
</style>
