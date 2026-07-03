<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">我的充值记录</h2>

    <!-- 筛选与操作区域 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center; justify-content: center; flex-wrap: wrap;">
      <el-date-picker
        v-model="searchMonth"
        type="month"
        format="YYYY年M月"
        value-format="YYYY-MM"
        placeholder="请选择月份"
        :clearable="false"
        style="width: 160px;"
        @change="loadMyRechargeRecords"
      />
      <el-button type="primary" @click="loadMyRechargeRecords">查询</el-button>
      <el-button type="success" @click="openRechargeDialog">自助充值</el-button>
    </div>

    <!-- 充值记录表格 -->
    <template v-if="rechargeList.length > 0">
      <el-table
        :data="pagedRechargeList"
        border
        stripe
        v-loading="loading"
        style="width: fit-content; max-width: 100%; margin: 0 auto;"
      >
        <el-table-column prop="rechargeId" label="充值编号" width="110" align="center" />
        <el-table-column prop="cardNumber" label="卡号" width="150" align="center" />

        <el-table-column prop="amount" label="充值金额(元)" width="140" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold;">￥{{ row.amount }}</span>
          </template>
        </el-table-column>

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

        <el-table-column prop="rechargeTime" label="充值时间" width="180" align="center">
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
    </template>

    <!-- 无数据时显示 -->
    <el-empty v-else description="当前用户暂无充值记录" />

    <!-- 自助充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="自助充值" width="500px">
      <el-form :model="rechargeForm" label-width="90px">
        <el-form-item label="本人卡号">
          <div style="color: #606266; line-height: 32px;">
            <span v-if="myCards.length > 0">
              {{ myCards.map(card => card.cardNumber).join('，') }}
            </span>
            <span v-else>暂无本人卡片</span>
          </div>
        </el-form-item>

        <el-form-item label="充值卡号">
          <el-input v-model="rechargeForm.cardNumber" placeholder="请输入要充值的卡号，如 CARD1001" clearable />
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
          <el-tag type="success">线上充值</el-tag>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submitSelfRecharge">
          确认充值
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

const props = defineProps({
  currentUser: { type: Object, required: true }
})

const loading = ref(false)
const submitting = ref(false)

const rechargeList = ref([])
const myCards = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const total = computed(() => rechargeList.value.length)

const pagedRechargeList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return rechargeList.value.slice(start, start + pageSize.value)
})

const getCurrentMonth = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const searchMonth = ref(getCurrentMonth())
const rechargeDialogVisible = ref(false)
const mismatchConfirmed = ref(false)

const rechargeForm = reactive({
  cardNumber: '',
  amount: 0
})

// 加载充值记录
const loadMyRechargeRecords = async () => {
  if (!props.currentUser?.userId) {
    ElMessage.error('当前登录用户信息不存在')
    return
  }
  if (!searchMonth.value) {
    ElMessage.warning('请选择充值月份')
    return
  }
  loading.value = true
  try {
    const response = await axios.get(
      `http://localhost:8080/api/recharge/user/${props.currentUser.userId}/month`,
      { params: { month: searchMonth.value } }
    )
    rechargeList.value = Array.isArray(response.data) ? response.data : []
    currentPage.value = 1
  } catch (error) {
    console.error(error)
    ElMessage.error('我的充值记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

// 打开自助充值弹窗
const openRechargeDialog = async () => {
  rechargeForm.cardNumber = ''
  rechargeForm.amount = 0
  mismatchConfirmed.value = false
  await loadMyCardsOnly()
  rechargeDialogVisible.value = true
}

// 加载本人卡片
const loadMyCardsOnly = async () => {
  if (!props.currentUser?.userId) return
  try {
    const response = await axios.get(`http://localhost:8080/api/card/user/${props.currentUser.userId}`)
    myCards.value = response.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('本人卡片信息加载失败')
  }
}

const isMyCardNumber = (cardNumber) => {
  return myCards.value.some(card => card.cardNumber === cardNumber)
}

// 提交自助充值
const submitSelfRecharge = async () => {
  if (submitting.value) return
  const inputCardNumber = rechargeForm.cardNumber.trim()
  if (!inputCardNumber) { ElMessage.warning('请输入充值卡号'); return }
  if (!rechargeForm.amount || rechargeForm.amount <= 0) { ElMessage.warning('充值金额必须大于 0'); return }

  if (!isMyCardNumber(inputCardNumber) && !mismatchConfirmed.value) {
    try {
      await ElMessageBox.confirm(
        '当前输入的卡号与本人名下卡号不一致，可能是在为他人充值。请确认卡号无误后继续操作。',
        '卡号确认提示',
        { confirmButtonText: '确认继续充值', cancelButtonText: '取消', type: 'warning' }
      )
      mismatchConfirmed.value = true
    } catch (error) { return }
  }

  submitting.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/recharge', {
      cardNumber: inputCardNumber,
      amount: rechargeForm.amount,
      rechargeMethod: 1
    })
    if (response.data.success) {
      ElMessage.success(response.data.message)
      rechargeDialogVisible.value = false
      loadMyRechargeRecords()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('自助充值失败，请检查卡号或后端接口')
  } finally {
    submitting.value = false
  }
}

const handlePageChange = (page) => { currentPage.value = page }
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadMyRechargeRecords()
  loadMyCardsOnly()
})
</script>

<style scoped>
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>
