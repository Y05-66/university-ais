<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
        stripe
      >
        <el-table-column prop="name" label="菜单名称" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="success">目录</el-tag>
            <el-tag v-else-if="row.type === 2" type="primary">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="150" />
        <el-table-column prop="component" label="组件路径" width="200" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" width="150" show-overflow-tooltip />
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
            <el-button type="primary" link size="small" @click="handleAddChild(row)">
              新增子菜单
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级菜单" v-if="form.type !== 1">
          <el-tree-select
            v-model="form.parentId"
            :data="parentMenus"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            placeholder="请选择父级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.type === 2">
          <el-input v-model="form.path" placeholder="如: /student/list" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 2">
          <el-input v-model="form.component" placeholder="如: @/views/student/list/index" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.type === 3">
          <el-input v-model="form.permission" placeholder="如: student:add" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如: User" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 系统管理页面 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  type: 2,
  parentId: 0,
  path: '',
  component: '',
  permission: '',
  icon: '',
  sort: 0,
  status: 1,
  remark: ''
})
const submitLoading = ref(false)
const parentMenus = ref([])

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data || []
    // 用于父级菜单选择（排除自身）
    parentMenus.value = JSON.parse(JSON.stringify(res.data || []))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', type: 2, parentId: 0, path: '', component: '', permission: '', icon: '', sort: 0, status: 1, remark: '' })
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  Object.assign(form, { id: null, name: '', type: 2, parentId: row.id, path: '', component: '', permission: '', icon: '', sort: 0, status: 1, remark: '' })
  dialogTitle.value = `新增子菜单 - ${row.name}`
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (formRef.value) {
    try {
      await formRef.value.validate()
    } catch (e) {
      return
    }
  }
  submitLoading.value = true
  try {
    if (form.id) {
      await updateMenu(form)
      ElMessage.success('修改成功')
    } else {
      await addMenu(form)
      ElMessage.success('新增成功')
    }
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
    await ElMessageBox.confirm('确定删除该菜单？如果存在子菜单将无法删除。', '提示', { type: 'warning' })
    await deleteMenu(row.id)
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
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
