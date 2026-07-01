<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的卡片</span>
          <el-button type="primary" size="small" @click="loadMyCards">
            刷新
          </el-button>
        </div>
      </template>

      <el-table
        v-if="cardList.length > 0"
        :data="cardList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="cardNumber" label="卡号" min-width="220" />

        <el-table-column label="卡片状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.cardStatus)">
              {{ getStatusText(row.cardStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="balance" label="账户余额(元)" width="140">
          <template #default="{ row }">
            <span class="balance">￥{{ row.balance }}</span>
          </template>
        </el-table-column>

        <el-table-column label="办卡时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.issueDate) }}
          </template>
        </el-table-column>

        <el-table-column label="注销时间" min-width="180">
          <template #default="{ row }">
            {{ row.cancelDate ? formatTime(row.cancelDate) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
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

      <el-empty
        v-else
        description="当前用户暂无卡片"
      />
    </el-card>
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
    const response = await axios.get(
      `${API_BASE}/user/${props.currentUser.userId}`
    )

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
  } catch (e) {}
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
.page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance {
  color: #e6a23c;
  font-weight: bold;
}
</style>