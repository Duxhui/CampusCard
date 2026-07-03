<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">校园一卡通 · 消费记录管理</h2>

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
        @change="loadConsumptionRecords"
      />
      <el-input
        v-model="searchCardNumber"
        placeholder="可选：卡号"
        clearable
        style="width: 150px;"
        @keyup.enter="loadConsumptionRecords"
      />
      <el-input
        v-model="searchMerchantId"
        placeholder="可选：商户编号"
        clearable
        style="width: 150px;"
        @keyup.enter="loadConsumptionRecords"
      />
      <el-button type="primary" @click="loadConsumptionRecords">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="openConsumeDialog">模拟消费</el-button>
    </div>

    <!-- 消费记录表格 -->
    <el-table
      :data="pagedConsumptionList"
      border
      stripe
      v-loading="loading"
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="consumptionId" label="消费编号" width="110" sortable="custom" align="center" />
      <el-table-column prop="cardNumber" label="卡号" width="130" sortable="custom" align="center" />
      <el-table-column prop="userId" label="用户ID" width="110" sortable="custom" align="center" />
      <el-table-column prop="userName" label="持卡人" width="120" align="center" />
      <el-table-column prop="merchantId" label="商户编号" width="110" sortable="custom" align="center" />
      <el-table-column prop="merchantName" label="商户名称" width="150" align="center" />
      <el-table-column prop="amount" label="消费金额(元)" width="130" sortable="custom" align="center" />
      <el-table-column prop="consumptionTime" label="消费时间" width="185" sortable="custom" align="center">
        <template #default="{ row }">
          {{ formatTime(row.consumptionTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="note" label="备注" width="100" align="center" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button
            size="small"
            type="warning"
            :disabled="isRefunded(row)"
            @click="refund(row)"
          >
            {{ isRefunded(row) ? '已退款' : '退款' }}
          </el-button>
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

    <!-- 模拟消费弹窗 -->
    <el-dialog v-model="consumeDialogVisible" title="模拟消费" width="480px">
      <el-form :model="consumeForm" label-width="90px">
        <el-form-item label="卡号">
          <el-input v-model="consumeForm.cardNumber" placeholder="请输入消费卡号" />
        </el-form-item>
        <el-form-item label="商户编号">
          <el-input v-model="consumeForm.merchantId" placeholder="请输入商户编号，如 M1001" />
        </el-form-item>
        <el-form-item label="消费金额">
          <el-input-number v-model="consumeForm.amount" :min="0" :precision="2" :step="5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="consumeForm.note" placeholder="如：食堂消费、超市购物" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submitConsume">
          确认消费
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const API_BASE = 'http://localhost:8080/api/consumption'

const loading = ref(false)
const submitting = ref(false)
const consumptionList = ref([])

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
const searchMerchantId = ref('')

const consumeDialogVisible = ref(false)

const sortProp = ref('')
const sortOrder = ref('')

const consumeForm = reactive({
  cardNumber: '',
  merchantId: '',
  amount: 0,
  note: ''
})

// 排序
const sortedConsumptionList = computed(() => {
  if (!sortProp.value || !sortOrder.value) {
    return consumptionList.value
  }
  return [...consumptionList.value].sort((a, b) => {
    const va = a[sortProp.value] ?? ''
    const vb = b[sortProp.value] ?? ''
    if (va < vb) return sortOrder.value === 'ascending' ? -1 : 1
    if (va > vb) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

const total = computed(() => sortedConsumptionList.value.length)

const pagedConsumptionList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedConsumptionList.value.slice(start, start + pageSize.value)
})

const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop || ''
  sortOrder.value = order || ''
  currentPage.value = 1
}

const handlePageChange = (page) => { currentPage.value = page }
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const isRefunded = (row) => {
  return row.note && row.note.includes('已退款')
}

// 查询消费记录
const loadConsumptionRecords = async () => {
  if (!searchMonth.value) {
    ElMessage.warning('请选择消费月份')
    return
  }
  loading.value = true
  try {
    const params = { month: searchMonth.value }
    if (searchCardNumber.value?.trim()) params.cardNumber = searchCardNumber.value.trim()
    if (searchMerchantId.value?.trim()) params.merchantId = searchMerchantId.value.trim()
    const response = await axios.get(`${API_BASE}/month`, { params })
    consumptionList.value = Array.isArray(response.data) ? response.data : []
    currentPage.value = 1
  } catch (error) {
    console.error(error)
    ElMessage.error('消费记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchMonth.value = getCurrentMonth()
  searchCardNumber.value = ''
  searchMerchantId.value = ''
  loadConsumptionRecords()
}

const openConsumeDialog = () => {
  consumeForm.cardNumber = ''
  consumeForm.merchantId = ''
  consumeForm.amount = 0
  consumeForm.note = ''
  consumeDialogVisible.value = true
}

const submitConsume = async () => {
  if (submitting.value) return
  if (!consumeForm.cardNumber) { ElMessage.warning('请输入卡号'); return }
  if (!consumeForm.merchantId) { ElMessage.warning('请输入商户编号'); return }
  if (!consumeForm.amount || consumeForm.amount <= 0) { ElMessage.warning('消费金额必须大于 0'); return }
  submitting.value = true
  try {
    const response = await axios.post(API_BASE, {
      cardNumber: consumeForm.cardNumber,
      merchantId: consumeForm.merchantId,
      amount: consumeForm.amount,
      note: consumeForm.note
    })
    if (response.data.success) {
      ElMessage.success(response.data.message)
      consumeDialogVisible.value = false
      loadConsumptionRecords()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('消费失败，请检查后端接口')
  } finally {
    submitting.value = false
  }
}

const refund = async (row) => {
  if (isRefunded(row)) {
    ElMessage.warning('该消费记录已经退款，不能重复退款')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要对消费记录【${row.consumptionId}】进行退款吗？退款后金额会返还到用户余额。`,
      '退款确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const response = await axios.put(`${API_BASE}/${row.consumptionId}/refund`)
    if (response.data.success) {
      ElMessage.success(response.data.message)
      loadConsumptionRecords()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('退款失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadConsumptionRecords()
})
</script>

<style scoped>
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>