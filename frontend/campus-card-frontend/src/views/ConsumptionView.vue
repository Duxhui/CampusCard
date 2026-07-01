<template>
  <div class="page">
    <!-- 查询与操作区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="卡号">
          <el-input
            v-model="searchCardNumber"
            placeholder="请输入卡号"
            clearable
            style="width: 230px"
          />
        </el-form-item>

        <el-form-item label="商户编号">
          <el-input
            v-model="searchMerchantId"
            placeholder="请输入商户编号"
            clearable
            style="width: 160px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="searchByCardNumber">按卡查询</el-button>
          <el-button type="primary" @click="searchByMerchantId">按商户查询</el-button>
          <el-button @click="loadConsumptionRecords">查询全部</el-button>
          <el-button type="success" @click="openConsumeDialog">模拟消费</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 消费记录表格 -->
    <el-card class="table-card">
      <el-table
        :data="consumptionList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="consumptionId" label="消费编号" width="110" />
        <el-table-column prop="cardNumber" label="卡号" min-width="210" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="userName" label="持卡人" width="120" />
        <el-table-column prop="merchantId" label="商户编号" width="110" />
        <el-table-column prop="merchantName" label="商户名称" width="150" />
        <el-table-column prop="amount" label="消费金额(元)" width="130" />

        <el-table-column label="消费时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.consumptionTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="note" label="备注" min-width="180" />

        <el-table-column label="操作" width="120" fixed="right">
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
    </el-card>

    <!-- 模拟消费弹窗 -->
    <el-dialog v-model="consumeDialogVisible" title="模拟消费" width="480px">
      <el-form :model="consumeForm" label-width="90px">
        <el-form-item label="卡号">
          <el-input
            v-model="consumeForm.cardNumber"
            placeholder="请输入消费卡号"
          />
        </el-form-item>

        <el-form-item label="商户编号">
          <el-input
            v-model="consumeForm.merchantId"
            placeholder="请输入商户编号，如 M001"
          />
        </el-form-item>

        <el-form-item label="消费金额">
          <el-input-number
            v-model="consumeForm.amount"
            :min="0"
            :precision="2"
            :step="5"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="consumeForm.note"
            placeholder="如：食堂消费、超市购物"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="consumeDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="submitting"
          @click="submitConsume"
        >
          确认消费
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE = 'http://localhost:8080/api/consumption'

const loading = ref(false)
const submitting = ref(false)
const consumptionList = ref([])

const searchCardNumber = ref('')
const searchMerchantId = ref('')

const consumeDialogVisible = ref(false)

const consumeForm = reactive({
  cardNumber: '',
  merchantId: '',
  amount: 0,
  note: ''
})

// 判断是否已经退款
const isRefunded = (row) => {
  return row.note && row.note.includes('已退款')
}

// 查询全部消费记录
const loadConsumptionRecords = async () => {
  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/list`)
    consumptionList.value = response.data
    searchCardNumber.value = ''
    searchMerchantId.value = ''
  } catch (error) {
    console.error(error)
    ElMessage.error('消费记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

// 按卡号查询
const searchByCardNumber = async () => {
  if (!searchCardNumber.value) {
    ElMessage.warning('请输入卡号')
    return
  }

  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/card/${searchCardNumber.value}`)
    consumptionList.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('按卡号查询失败')
  } finally {
    loading.value = false
  }
}

// 按商户编号查询
const searchByMerchantId = async () => {
  if (!searchMerchantId.value) {
    ElMessage.warning('请输入商户编号')
    return
  }

  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/merchant/${searchMerchantId.value}`)
    consumptionList.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('按商户查询失败')
  } finally {
    loading.value = false
  }
}

// 打开消费弹窗
const openConsumeDialog = () => {
  consumeForm.cardNumber = ''
  consumeForm.merchantId = ''
  consumeForm.amount = 0
  consumeForm.note = ''
  consumeDialogVisible.value = true
}

// 提交消费
const submitConsume = async () => {
  if (submitting.value) {
    return
  }

  if (!consumeForm.cardNumber) {
    ElMessage.warning('请输入卡号')
    return
  }

  if (!consumeForm.merchantId) {
    ElMessage.warning('请输入商户编号')
    return
  }

  if (!consumeForm.amount || consumeForm.amount <= 0) {
    ElMessage.warning('消费金额必须大于 0')
    return
  }

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

// 退款 / 冲正
const refund = async (row) => {
  if (isRefunded(row)) {
    ElMessage.warning('该消费记录已经退款，不能重复退款')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要对消费记录【${row.consumptionId}】进行退款吗？退款后金额会返还到用户余额。`,
      '退款确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
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

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadConsumptionRecords()
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