<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">校园一卡通 · 卡片管理</h2>

    <!-- 查询和操作区域 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center; justify-content: center;">
      <el-input
        v-model="searchUserId"
        placeholder="输入用户ID查询卡片"
        clearable
        style="width: 250px;"
        @keyup.enter="searchByUserId"
      />
      <el-button type="primary" @click="searchByUserId">按用户查询</el-button>
      <el-button @click="loadCards">查询全部</el-button>
    </div>

    <!-- 卡片列表 -->
    <el-table
      :data="pagedCards"
      border
      stripe
      v-loading="loading"
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="cardNumber" label="卡号" width="145" sortable="custom" align="center" />
      <el-table-column prop="userId" label="用户ID" width="100" sortable="custom" align="center" />
      <el-table-column prop="userName" label="持卡人" width="90" align="center" />

      <el-table-column label="卡片状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.cardStatus)">
            {{ getStatusText(row.cardStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="balance" label="余额(元)" width="115" sortable="custom" align="center" />

      <el-table-column prop="issueDate" label="办卡时间" width="185" sortable="custom" align="center">
        <template #default="{ row }">
          {{ formatTime(row.issueDate) }}
        </template>
      </el-table-column>

      <el-table-column prop="cancelDate" label="注销时间" width="185" sortable="custom" align="center">
        <template #default="{ row }">
          {{ row.cancelDate ? formatTime(row.cancelDate) : '-' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" fixed="right" align="center">
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

    <!-- 分页（与用户管理页面完全一致） -->
    <div style="display: flex; justify-content: center; margin-top: 20px;">
      <el-config-provider :locale="zhCn">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="cardList.length"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </el-config-provider>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const API_BASE = 'http://localhost:8080/api/card'

const loading = ref(false)
const cardList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)

const searchUserId = ref('')

// 排序状态
const sortProp = ref('')
const sortOrder = ref('')

const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop || ''
  sortOrder.value = order || ''
  pageNum.value = 1
}

// 全表排序
const sortedCardList = computed(() => {
  if (!sortProp.value || !sortOrder.value) {
    return cardList.value
  }
  return [...cardList.value].sort((a, b) => {
    const va = a[sortProp.value] ?? ''
    const vb = b[sortProp.value] ?? ''
    if (va < vb) return sortOrder.value === 'ascending' ? -1 : 1
    if (va > vb) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

// 当前页数据（基于排序后的列表和分页参数）
const pagedCards = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return sortedCardList.value.slice(start, start + pageSize.value)
})

// 分页事件处理
const handlePageChange = (page) => {
  pageNum.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1 // 改变每页条数时回到第一页
}

// 加载全部卡片
const loadCards = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE}/list`)
    cardList.value = response.data
    searchUserId.value = ''
    pageNum.value = 1 // 刷新数据后重置页码
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
    pageNum.value = 1 // 查询后重置页码
  } catch (error) {
    console.error(error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 挂失
const lostCard = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要挂失卡片【${row.cardNumber}】吗？`,
      '挂失确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
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
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
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
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>