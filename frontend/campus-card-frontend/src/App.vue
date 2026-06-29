<template>
  <div style="padding: 30px">
    <h2>校园一卡通 · 用户管理</h2>
    <el-button type="primary" @click="loadUsers">加载用户列表</el-button>

    <el-table
      v-if="users.length > 0"
      :data="users"
      border
      stripe
      style="width: 900px; margin-top: 16px"
    >
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="idNumber" label="身份证号" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="userType" label="类型" width="80">
        <template #default="{ row }">
          {{ row.userType === 1 ? '学生' : '教职工' }}
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="余额(元)" width="100" align="right" />
      <el-table-column prop="registerTime" label="注册时间" width="180" />
    </el-table>

    <el-empty v-else description="暂无用户数据，请点击按钮加载" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const users = ref([])

const loadUsers = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/list')
    users.value = res.data
  } catch (err) {
    console.error('请求失败:', err)
    alert('加载失败，请确保后端已启动')
  }
}
</script>
