<template>
  <div style="padding: 20px">
    <h2>校园一卡通 · 用户管理</h2>

    <!-- 操作栏：搜索 + 新增 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center;">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名/身份证/手机号"
        clearable
        style="width: 280px"
        @clear="searchKeyword = ''"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="searchType" placeholder="用户类型" clearable style="width: 130px" @change="handleSearch">
        <el-option :value="1" label="学生" />
        <el-option :value="2" label="教职工" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="openAddDialog">新增用户</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table
      :data="users"
      border
      stripe
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
      v-loading="loading"
      class="centered-table"
    >
      <el-table-column prop="userId" label="ID" width="60" align="center" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="idNumber" label="身份证号" width="200" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="email" label="邮箱" width="240" />
      <el-table-column prop="userType" label="类型" width="70" align="center">
        <template #default="{ row }">{{ row.userType === 1 ? '学生' : '教职工' }}</template>
      </el-table-column>
      <el-table-column prop="balance" label="余额(元)" width="100" align="right" />
      <el-table-column prop="registerTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="210" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input v-model="form.idNumber" maxlength="18" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit" required>
          <el-input v-model="form.password" show-password />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="form.userType">
            <el-option :value="1" label="学生" />
            <el-option :value="2" label="教职工" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '保存' : '确认新增' }}</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="520px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser.name }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentUser.idNumber }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentUser.userType === 1 ? '学生' : '教职工' }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ currentUser.balance }} 元</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.registerTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const showTable = ref(false)//控制表格显示

const users = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const searchType = ref(null)

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref(null)

const form = reactive({
  userId: null,
  name: '',
  idNumber: '',
  phone: '',
  email: '',
  password: '',
  userType: 1
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/list')
    users.value = res.data
  } catch (err) {
    ElMessage.error('加载失败：' + err.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchType.value !== null && searchType.value !== undefined) params.userType = searchType.value
    const res = await axios.get('http://localhost:8080/api/user/search', { params })
    users.value = res.data
  } catch (err) {
    ElMessage.error('搜索失败：' + err.message)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.userId = row.userId
  form.name = row.name
  form.idNumber = row.idNumber
  form.phone = row.phone
  form.email = row.email
  form.password = ''
  form.userType = row.userType
  dialogVisible.value = true
}

const viewDetail = (row) => {
  currentUser.value = row
  detailVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.idNumber || (!isEdit.value && !form.password)) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (isEdit.value) {
      await axios.put(`http://localhost:8080/api/user/${form.userId}`, form)
      ElMessage.success('更新成功')
    } else {
      await axios.post('http://localhost:8080/api/user', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    resetForm()
    loadUsers()
  } catch (err) {
    ElMessage.error('操作失败：' + (err.response?.data?.message || err.message))
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`http://localhost:8080/api/user/${row.userId}`)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (err) {
      ElMessage.error('删除失败：' + (err.response?.data?.message || err.message))
    }
  }).catch(() => {})
}

const resetForm = () => {
  form.userId = null
  form.name = ''
  form.idNumber = ''
  form.phone = ''
  form.email = ''
  form.password = ''
  form.userType = 1
}


</script>

<style scoped>
/* 让该组件内所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>