<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">我的消费记录</h2>

    <!-- 筛选区域 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center; justify-content: center; flex-wrap: wrap;">
      <el-date-picker
        v-model="searchMonth"
        type="month"
        format="YYYY年M月"
        value-format="YYYY-MM"
        placeholder="请选择月份"
        :clearable="false"
        style="width: 160px;"
        @change="loadMyConsumptionRecords"
      />
      <el-button type="primary" @click="loadMyConsumptionRecords">查询</el-button>
    </div>

    <!-- 消费记录表格 -->
    <template v-if="consumptionList.length > 0">
      <el-table
        :data="pagedConsumptionList"
        border
        stripe
        v-loading="loading"
        style="width: fit-content; max-width: 100%; margin: 0 auto;"
      >
        <el-table-column prop="consumptionId" label="消费编号" width="110" align="center" />
        <el-table-column prop="cardNumber" label="卡号" width="150" align="center" />
        <el-table-column prop="merchantId" label="商户编号" width="110" align="center" />
        <el-table-column prop="merchantName" label="商户名称" width="150" align="center" />

        <el-table-column prop="amount" label="消费金额(元)" width="140" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">￥{{ row.amount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="消费时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.consumptionTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="isRefunded(row) ? 'info' : 'success'">
              {{ isRefunded(row) ? '已退款' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="note" label="备注" width="200" align="center" />
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

    <el-empty v-else description="当前用户暂无消费记录" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const props = defineProps({
  currentUser: { type: Object, required: true }
})

const loading = ref(false)
const consumptionList = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const total = computed(() => consumptionList.value.length)

const pagedConsumptionList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return consumptionList.value.slice(start, start + pageSize.value)
})

const getCurrentMonth = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const searchMonth = ref(getCurrentMonth())

const loadMyConsumptionRecords = async () => {
  if (!props.currentUser?.userId) {
    ElMessage.error('当前登录用户信息不存在')
    return
  }
  if (!searchMonth.value) {
    ElMessage.warning('请选择消费月份')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(
      `http://localhost:8080/api/consumption/user/${props.currentUser.userId}/month`,
      { params: { month: searchMonth.value } }
    )
    consumptionList.value = Array.isArray(response.data) ? response.data : []
    currentPage.value = 1
  } catch (error) {
    console.error(error)
    ElMessage.error('我的消费记录加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => { currentPage.value = page }
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const isRefunded = (row) => row.note && row.note.includes('已退款')

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadMyConsumptionRecords()
})
</script>

<style scoped>
/* 强制表格单元格居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>