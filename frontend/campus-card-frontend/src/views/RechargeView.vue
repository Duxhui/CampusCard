<template>
  <div class="page">
    <!-- 查询与操作区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="卡号">
          <el-input
            v-model="searchCardNumber"
            placeholder="请输入卡号查询充值记录"
            clearable
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="searchByCardNumber">按卡查询</el-button>
          <el-button @click="loadRechargeRecords">查询全部</el-button>
          <el-button type="success" @click="openRechargeDialog">模拟充值</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 充值记录表格 -->
    <el-card class="table-card">
      <el-table
        :data="rpaged"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="rechargeId" label="充值编号" width="110" sortable />
        <el-table-column prop="cardNumber" label="卡号" min-width="210" sortable />
        <el-table-column prop="userId" label="用户ID" width="100" sortable />
        <el-table-column prop="userName" label="持卡人" width="120" />

        <el-table-column prop="amount" label="充值金额(元)" width="140" sortable />

        <el-table-column label="充值方式" width="120">
          <template #default="{ row }">
            <el-tag :type="row.rechargeMethod === 1 ? 'success' : 'info'">
              {{ row.rechargeMethod === 1 ? '线上充值' : '线下充值' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="充值状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="rechargeTime" label="充值时间" min-width="180" sortable>
          <template #default="{ row }">
            {{ formatTime(row.rechargeTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="rp" v-model:page-size="rps"
        :total="rechargeList.length"
        style="margin-top: 16px; justify-content: flex-end"
        layout="total, prev, pager, next" :page-sizes="[10]"
      />
    </el-card>

    <!-- 模拟充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="模拟充值" width="460px">
      <el-form :model="rechargeForm" label-width="90px">
        <el-form-item label="卡号">
          <el-input
            v-model="rechargeForm.cardNumber"
            placeholder="请输入要充值的卡号"
          />
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

const API_BASE = 'http://localhost:8080/api/recharge'

const loading = ref(false)
const submitting = ref(false)
const rechargeList = ref([])
const rp = ref(1)
const rps = ref(10)

const rpaged = computed(() => {
  const s = (rp.value - 1) * rps.value
  return rechargeList.value.slice(s, s + rps.value)
})
const searchCardNumber = ref('')

const rechargeDialogVisible = ref(false)

const rechargeForm = reactive({
  cardNumber: '',
  amount: 0,
  rechargeMethod: 1
})

// 查询全部充值记录
const loadRechargeRecords = async () => {
  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/list`)
    rechargeList.value = response.data
    searchCardNumber.value = ''
  } catch (error) {
    console.error(error)
    ElMessage.error('充值记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

// 按卡号查询充值记录
const searchByCardNumber = async () => {
  if (!searchCardNumber.value) {
    ElMessage.warning('请输入卡号')
    return
  }

  loading.value = true

  try {
    const response = await axios.get(`${API_BASE}/card/${searchCardNumber.value}`)
    rechargeList.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 打开充值弹窗
const openRechargeDialog = () => {
  rechargeForm.cardNumber = ''
  rechargeForm.amount = 0
  rechargeForm.rechargeMethod = 1
  rechargeDialogVisible.value = true
}

// 提交充值
const submitRecharge = async () => {
  if (submitting.value) {
    return
  }

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

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadRechargeRecords()
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