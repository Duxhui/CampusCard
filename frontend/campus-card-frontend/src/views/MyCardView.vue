<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">我的卡片</h2>

    <!-- 操作区域 -->
    <div style="display: flex; justify-content: center; margin-bottom: 16px;">
      <el-button type="primary" @click="loadMyCards">刷新</el-button>
    </div>

    <!-- 卡片表格 -->
    <el-table
      v-if="cardList.length > 0"
      :data="cardList"
      border
      stripe
      v-loading="loading"
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
    >
      <el-table-column prop="cardNumber" label="卡号" width="220" align="center" />

      <el-table-column label="卡片状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.cardStatus)">
            {{ getStatusText(row.cardStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="balance" label="账户余额(元)" width="140" align="center">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: bold;">￥{{ row.balance }}</span>
        </template>
      </el-table-column>

      <el-table-column label="办卡时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatTime(row.issueDate) }}
        </template>
      </el-table-column>

      <el-table-column label="注销时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.cancelDate ? formatTime(row.cancelDate) : '-' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            size="small"
            type="warning"
            v-if="row.cardStatus === 1"
            @click="handleLoss(row)"
          >
            申请挂失
          </el-button>
          <el-button
            size="small"
            type="success"
            v-if="row.cardStatus === 2"
            @click="handleUnlock(row)"
          >
            申请解挂
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 无数据时显示 -->
    <el-empty
      v-else
      description="当前用户暂无卡片"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  currentUser: {
    type: Object,
    required: true
  }
})

const loading = ref(false)
const cardList = ref([])

const API_BASE = 'http://localhost:8080/api/card'

const loadMyCards = async () => {
  if (!props.currentUser || !props.currentUser.userId) {
    ElMessage.error('当前登录用户信息不存在')
    return
  }
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE}/user/${props.currentUser.userId}`)
    cardList.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('我的卡片加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  if (status === 1) return '正常'
  if (status === 2) return '挂失'
  if (status === 3) return '注销'
  return '未知'
}

const getStatusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

const handleLoss = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要挂失卡片 ${row.cardNumber} 吗？挂失后卡片将无法使用。`,
      '挂失确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await axios.put(`${API_BASE}/${row.cardNumber}/lost`)
    ElMessage[res.data.success ? 'success' : 'error'](res.data.message)
    loadMyCards()
  } catch (e) {
    // 取消操作不做处理
  }
}

const handleUnlock = async (row) => {
  try {
    const res = await axios.put(`${API_BASE}/${row.cardNumber}/unlock`)
    ElMessage[res.data.success ? 'success' : 'error'](res.data.message)
    loadMyCards()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadMyCards()
})
</script>

<style scoped>
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>