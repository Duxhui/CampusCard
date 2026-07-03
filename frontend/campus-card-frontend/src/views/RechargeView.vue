<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">校园一卡通 · 充值记录管理</h2>

    <!-- 查询与操作区域 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center; justify-content: center; flex-wrap: wrap;">
      <el-date-picker
        v-model="searchMonth"
        type="month"
        format="YYYY年M月"
        value-format="YYYY-MM"
        placeholder="请选择月份"
        :clearable="false"
        style="width: 160px;"
        @change="loadRechargeRecords"
      />
      <el-input
        v-model="searchCardNumber"
        placeholder="可选：卡号"
        clearable
        style="width: 150px;"
        @keyup.enter="loadRechargeRecords"
      />
      <el-button type="primary" @click="loadRechargeRecords">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="openRechargeDialog">模拟充值</el-button>
    </div>

    <!-- 充值记录表格 -->
    <el-table
      :data="pagedRechargeList"
      border
      stripe
      v-loading="loading"
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="rechargeId" label="充值编号" width="110" sortable="custom" align="center" />
      <el-table-column prop="cardNumber" label="卡号" width="150" sortable="custom" align="center" />
      <el-table-column prop="userId" label="用户ID" width="110" sortable="custom" align="center" />
      <el-table-column prop="userName" label="持卡人" width="120" align="center" />
      <el-table-column prop="amount" label="充值金额(元)" width="130" sortable="custom" align="center" />

      <el-table-column label="充值方式" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.rechargeMethod === 1 ? 'success' : 'info'">
            {{ row.rechargeMethod === 1 ? '线上充值' : '线下充值' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="充值状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="rechargeTime" label="充值时间" width="185" sortable="custom" align="center">
        <template #default="{ row }">
          {{ formatTime(row.rechargeTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="display: flex; justify-content: center; margin-top: 20px;">
      <el-config-provider :locale="zhCn">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </el-config-provider>
    </div>

    <!-- 模拟充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="模拟充值" width="460px">
      <el-form :model="rechargeForm" label-width="90px">
        <el-form-item label="卡号">
          <el-input v-model="rechargeForm.cardNumber" placeholder="请输入要充值的卡号" />
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="充值方式">
          <el-radio-group v-model="rechargeForm.rechargeMethod">
            <el-radio :label="0">线下充值</el-radio>
            <el-radio :label="1">线上充值</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="submitting"
          @click="submitRecharge"
        >
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const API_BASE = 'http://localhost:8080/api/recharge'

const loading = ref(false)
const submitting = ref(false)

const rechargeList = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const getCurrentMonth = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const searchMonth = ref(getCurrentMonth())
const searchCardNumber = ref('')

const rechargeDialogVisible = ref(false)

const sortProp = ref('')
const sortOrder = ref('')

const rechargeForm = reactive({
  cardNumber: '',
  amount: 0,
  rechargeMethod: 1
})

// 全表排序
const sortedRechargeList = computed(() => {
  if (!sortProp.value || !sortOrder.value) {
    return rechargeList.value
  }
  return [...rechargeList.value].sort((a, b) => {
    const va = a[sortProp.value] ?? ''
    const vb = b[sortProp.value] ?? ''
    if (va < vb) return sortOrder.value === 'ascending' ? -1 : 1
    if (va > vb) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

const total = computed(() => sortedRechargeList.value.length)

const pagedRechargeList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedRechargeList.value.slice(start, start + pageSize.value)
})

const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop || ''
  sortOrder.value = order || ''
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 查询充值记录：月份必选，卡号为可选
const loadRechargeRecords = async () => {
  if (!searchMonth.value) {
    ElMessage.warning('请选择充值月份')
    return
  }
  loading.value = true
  try {
    const params = { month: searchMonth.value }
    if (searchCardNumber.value && searchCardNumber.value.trim()) {
      params.cardNumber = searchCardNumber.value.trim()
    }
    const response = await axios.get(`${API_BASE}/month`, { params })
    rechargeList.value = Array.isArray(response.data) ? response.data : []
    currentPage.value = 1
  } catch (error) {
    console.error(error)
    ElMessage.error('充值记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchMonth.value = getCurrentMonth()
  searchCardNumber.value = ''
  loadRechargeRecords()
}

const openRechargeDialog = () => {
  rechargeForm.cardNumber = ''
  rechargeForm.amount = 0
  rechargeForm.rechargeMethod = 1
  rechargeDialogVisible.value = true
}

const submitRecharge = async () => {
  if (submitting.value) return
  if (!rechargeForm.cardNumber) {
    ElMessage.warning('请输入卡号')
    return
  }
  if (!rechargeForm.amount || rechargeForm.amount <= 0) {
    ElMessage.warning('充值金额必须大于 0')
    return
  }
  submitting.value = true
  try {
    const response = await axios.post(API_BASE, {
      cardNumber: rechargeForm.cardNumber,
      amount: rechargeForm.amount,
      rechargeMethod: rechargeForm.rechargeMethod
    })
    if (response.data.success) {
      ElMessage.success(response.data.message)
      rechargeDialogVisible.value = false
      loadRechargeRecords()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('充值失败，请检查后端接口')
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadRechargeRecords()
})
</script>

<style scoped>
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>