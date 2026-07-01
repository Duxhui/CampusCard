<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的消费记录</span>
          <el-button type="primary" size="small" @click="loadMyConsumptionRecords">
            刷新
          </el-button>
        </div>
      </template>

      <el-table
        v-if="consumptionList.length > 0"
        :data="consumptionList"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="consumptionId" label="消费编号" width="110" />
        <el-table-column prop="cardNumber" label="卡号" min-width="220" />
        <el-table-column prop="merchantId" label="商户编号" width="110" />
        <el-table-column prop="merchantName" label="商户名称" width="150" />

        <el-table-column prop="amount" label="消费金额(元)" width="140">
          <template #default="{ row }">
            <span class="amount">￥{{ row.amount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="消费时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.consumptionTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="isRefunded(row) ? 'info' : 'success'">
              {{ isRefunded(row) ? '已退款' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="note" label="备注" min-width="200" />
      </el-table>

      <el-empty
        v-else
        description="当前用户暂无消费记录"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const props = defineProps({
  currentUser: {
    type: Object,
    required: true
  }
})

const loading = ref(false)
const consumptionList = ref([])

const loadMyConsumptionRecords = async () => {
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

    const cards = cardResponse.data

    if (!cards || cards.length === 0) {
      consumptionList.value = []
      return
    }

    // 第二步：根据每张卡号查询消费记录
    const requests = cards.map(card =>
      axios.get(`http://localhost:8080/api/consumption/card/${card.cardNumber}`)
    )

    const responses = await Promise.all(requests)

    // 第三步：合并多张卡的消费记录
    const records = responses.flatMap(response => response.data)

    // 第四步：按照消费时间倒序排列
    consumptionList.value = records.sort((a, b) => {
      return new Date(b.consumptionTime) - new Date(a.consumptionTime)
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('我的消费记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const isRefunded = (row) => {
  return row.note && row.note.includes('已退款')
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadMyConsumptionRecords()
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
  color: #f56c6c;
  font-weight: bold;
}
</style>