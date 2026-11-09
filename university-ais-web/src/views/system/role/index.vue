<template>
  <div class="page-container">
    <el-card>
      <template #header><span>角色管理</span></template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAuth(row)">权限配置</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 权限配置弹窗 -->
    <el-dialog v-model="authDialogVisible" title="权限配置" width="600px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAuth">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 系统管理页面 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, deleteRole, getMenuTree, setRoleMenus, getRoleMenus } from '@/api'

const loading = ref(false)
const tableData = ref([])
const authDialogVisible = ref(false)
const menuTree = ref([])
const treeRef = ref(null)
const currentRoleId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  const res = await getMenuTree()
  menuTree.value = res.data || []
}

const handleAuth = async (row) => {
  currentRoleId.value = row.id
  await loadMenuTree()
  // 查询该角色已分配的菜单 ID
  try {
    const menuRes = await getRoleMenus(row.id)
    const checkedKeys = menuRes.data || []
    setTimeout(() => {
      if (treeRef.value) {
        treeRef.value.setCheckedKeys(checkedKeys)
      }
    }, 100)
  } catch (e) {
    // 无菜单权限时静默处理
  }
  authDialogVisible.value = true
}

const handleSaveAuth = async () => {
  const checkedKeys = treeRef.value.getCheckedKeys()
  try {
    await setRoleMenus(currentRoleId.value, checkedKeys)
    ElMessage.success('权限配置成功')
    authDialogVisible.value = false
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
