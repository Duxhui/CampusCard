<template>
  <div class="page">
    <!-- 查询和操作区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="用户ID">
          <el-input
            v-model="searchUserId"
            placeholder="输入用户ID查询卡片"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="searchByUserId">按用户查询</el-button>
          <el-button @click="loadCards">查询全部</el-button>
          <el-button type="success" @click="openIssueDialog">发卡</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 卡片列表 -->
    <el-card class="table-card">
      <el-table
        :data="pagedCards"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="cardNumber" label="卡号" min-width="220" sortable />
        <el-table-column prop="userId" label="用户ID" width="100" sortable />
        <el-table-column prop="userName" label="持卡人" width="120" />

        <el-table-column label="卡片状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.cardStatus)">
              {{ getStatusText(row.cardStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="balance" label="余额(元)" width="120" sortable />

        <el-table-column prop="issueDate" label="办卡时间" min-width="180" sortable>
          <template #default="{ row }">
            {{ formatTime(row.issueDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="cancelDate" label="注销时间" min-width="180" sortable>
          <template #default="{ row }">
            {{ row.cancelDate ? formatTime(row.cancelDate) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="warning"
              :disabled="row.cardStatus !== 1"
              @click="lostCard(row)"
            >
              挂失
            </el-button>

            <el-button
              size="small"
              type="success"
              :disabled="row.cardStatus !== 2"
              @click="unlockCard(row)"
            >
              解挂
            </el-button>

            <el-button
              size="small"
              type="danger"
              :disabled="row.cardStatus === 3"
              @click="cancelCard(row)"
            >
              注销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="cardList.length"
        style="margin-top: 16px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :page-sizes="[10]"
      />
    </el-card>

    <!-- 发卡弹窗 -->
    <el-dialog v-model="issueDialogVisible" title="发卡" width="420px">
      <el-form label-width="90px">
        <el-form-item label="用户ID">
          <el-input
            v-model="issueUserId"
            placeholder="请输入要发卡的用户ID"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="issueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="issueCard">确认发卡</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api/card'

const loading = ref(false)
const cardList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)

const pagedCards = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return cardList.value.slice(start, start + pageSize.value)
})
const searchUserId = ref('')

const issueDialogVisible = ref(false)
const issueUserId = ref('')

// 加载全部卡片
const loadCards = async () => {
  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/list`)
    cardList.value = response.data
    searchUserId.value = ''
  } catch (error) {
    console.error(error)
    ElMessage.error('卡片列表加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

// 按用户ID查询卡片
const searchByUserId = async () => {
  if (!searchUserId.value) {
    ElMessage.warning('请输入用户ID')
    return
  }

  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/user/${searchUserId.value}`)
    cardList.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 打开发卡弹窗
const openIssueDialog = () => {
  issueUserId.value = ''
  issueDialogVisible.value = true
}

// 发卡
const issueCard = async () => {
  if (!issueUserId.value) {
    ElMessage.warning('请输入用户ID')
    return
  }

  try {
    const response = await axios.post(`${API_BASE}/issue/${issueUserId.value}`)

    if (response.data.success) {
      ElMessage.success(`${response.data.message}，卡号：${response.data.cardNumber}`)
      issueDialogVisible.value = false
      loadCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('发卡失败，请检查后端接口')
  }
}

// 挂失
const lostCard = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要挂失卡片【${row.cardNumber}】吗？`,
      '挂失确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await axios.put(`${API_BASE}/${row.cardNumber}/lost`)

    if (response.data.success) {
      ElMessage.success(response.data.message)
      loadCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('挂失失败')
    }
  }
}

// 解挂
const unlockCard = async (row) => {
  try {
    const response = await axios.put(`${API_BASE}/${row.cardNumber}/unlock`)

    if (response.data.success) {
      ElMessage.success(response.data.message)
      loadCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('解挂失败')
  }
}

// 注销
const cancelCard = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要注销卡片【${row.cardNumber}】吗？注销后用户余额会清零。`,
      '注销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await axios.put(`${API_BASE}/${row.cardNumber}/cancel`)

    if (response.data.success) {
      ElMessage.success(response.data.message)
      loadCards()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('注销失败')
    }
  }
}

// 状态文字
const getStatusText = (status) => {
  if (status === 1) return '正常'
  if (status === 2) return '挂失'
  if (status === 3) return '注销'
  return '未知'
}

// 状态颜色
const getStatusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'danger'
  return 'info'
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadCards()
})
</script>

<style scoped>
.page {
  width: 100%;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-top: 16px;
}
</style>