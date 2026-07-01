<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的充值记录</span>

          <div>
            <el-button type="success" size="small" @click="openRechargeDialog">
              自助充值
            </el-button>

            <el-button type="primary" size="small" @click="loadMyRechargeRecords">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-if="rechargeList.length > 0"
        :data="rechargeList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="rechargeId" label="充值编号" width="110" />
        <el-table-column prop="cardNumber" label="卡号" min-width="220" />

        <el-table-column prop="amount" label="充值金额(元)" width="140">
          <template #default="{ row }">
            <span class="amount">￥{{ row.amount }}</span>
          </template>
        </el-table-column>

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

        <el-table-column label="充值时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.rechargeTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-else
        description="当前用户暂无充值记录"
      />
    </el-card>

    <!-- 自助充值弹窗 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="自助充值"
      width="500px"
    >
      <el-form :model="rechargeForm" label-width="90px">
        <el-form-item label="本人卡号">
          <div class="my-card-text">
            <span v-if="myCards.length > 0">
              {{ myCards.map(card => card.cardNumber).join('，') }}
            </span>
            <span v-else>暂无本人卡片</span>
          </div>
        </el-form-item>

        <el-form-item label="充值卡号">
          <el-input
            v-model="rechargeForm.cardNumber"
            placeholder="请输入要充值的卡号，如 CARD1001"
            clearable
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
          <el-tag type="success">线上充值</el-tag>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitting"
          :disabled="submitting"
          @click="submitSelfRecharge"
        >
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  currentUser: {
    type: Object,
    required: true
  }
})

const loading = ref(false)
const submitting = ref(false)
const rechargeList = ref([])
const myCards = ref([])

const rechargeDialogVisible = ref(false)

// 当前弹窗内，卡号不一致提醒是否已经确认过
const mismatchConfirmed = ref(false)

const rechargeForm = reactive({
  cardNumber: '',
  amount: 0
})

// 加载当前用户的充值记录
const loadMyRechargeRecords = async () => {
  if (!props.currentUser || !props.currentUser.userId) {
    ElMessage.error('当前登录用户信息不存在')
    return
  }

  loading.value = true

  try {
    // 第一步：根据当前用户ID查询该用户名下卡片
    const cardResponse = await axios.get(
      `http://localhost:8080/api/card/user/${props.currentUser.userId}`
    )

    const cards = cardResponse.data || []
    myCards.value = cards

    if (cards.length === 0) {
      rechargeList.value = []
      return
    }

    // 第二步：根据每张卡号查询充值记录
    const requests = cards.map(card =>
      axios.get(`http://localhost:8080/api/recharge/card/${card.cardNumber}`)
    )

    const responses = await Promise.all(requests)

    // 第三步：合并多张卡的充值记录
    const records = responses.flatMap(response => response.data)

    // 第四步：按照充值时间倒序排列
    rechargeList.value = records.sort((a, b) => {
      return new Date(b.rechargeTime) - new Date(a.rechargeTime)
    })
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

  // 打开弹窗前刷新一下本人卡片，确保判断准确
  await loadMyCardsOnly()

  rechargeDialogVisible.value = true
}

// 只刷新本人卡片，不刷新充值记录
const loadMyCardsOnly = async () => {
  if (!props.currentUser || !props.currentUser.userId) {
    return
  }

  try {
    const response = await axios.get(
      `http://localhost:8080/api/card/user/${props.currentUser.userId}`
    )

    myCards.value = response.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('本人卡片信息加载失败')
  }
}

// 判断输入卡号是否属于本人名下卡片
const isMyCardNumber = (cardNumber) => {
  return myCards.value.some(card => card.cardNumber === cardNumber)
}

// 提交自助充值
const submitSelfRecharge = async () => {
  if (submitting.value) {
    return
  }

  const inputCardNumber = rechargeForm.cardNumber.trim()

  if (!inputCardNumber) {
    ElMessage.warning('请输入充值卡号')
    return
  }

  if (!rechargeForm.amount || rechargeForm.amount <= 0) {
    ElMessage.warning('充值金额必须大于 0')
    return
  }

  // 如果输入卡号不是本人名下卡号，并且本次弹窗还没有提醒过，则弹出确认框
  if (!isMyCardNumber(inputCardNumber) && !mismatchConfirmed.value) {
    try {
      await ElMessageBox.confirm(
        '当前输入的卡号与本人名下卡号不一致，可能是在为他人充值。请确认卡号无误后继续操作。',
        '卡号确认提示',
        {
          confirmButtonText: '确认继续充值',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      mismatchConfirmed.value = true
    } catch (error) {
      return
    }
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

      // 充值成功后刷新自己的充值记录
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

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadMyRechargeRecords()
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

.amount {
  color: #67c23a;
  font-weight: bold;
}

.my-card-text {
  color: #606266;
  line-height: 32px;
}
</style>