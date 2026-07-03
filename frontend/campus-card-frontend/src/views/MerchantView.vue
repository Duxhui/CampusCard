<template>
  <div style="padding: 20px; max-width: 1300px; margin: 0 auto;">
    <h2 style="text-align: center;">校园一卡通 · 商户管理</h2>

    <!-- 搜索与操作区域 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; align-items: center; justify-content: center; flex-wrap: wrap;">
      <el-select
        v-model="searchForm.merchantType"
        placeholder="商户类型"
        clearable
        style="width: 150px;"
      >
        <el-option label="食堂" value="食堂" />
        <el-option label="超市" value="超市" />
        <el-option label="书店" value="书店" />
        <el-option label="饮品" value="饮品" />
        <el-option label="咖啡厅" value="咖啡厅" />
        <el-option label="医疗" value="医疗" />
        <el-option label="服务" value="服务" />
        <el-option label="生活服务" value="生活服务" />
      </el-select>

      <el-select
        v-model="searchForm.businessStatus"
        placeholder="营业状态"
        clearable
        style="width: 150px;"
      >
        <el-option label="营业" :value="1" />
        <el-option label="停业" :value="0" />
      </el-select>

      <el-button type="primary" @click="loadMerchants">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="openAddDialog">新增商户</el-button>
    </div>

    <!-- 商户表格 -->
    <el-table
      :data="pagedMerchants"
      border
      stripe
      v-loading="loading"
      style="width: fit-content; max-width: 100%; margin: 0 auto;"
    >
      <el-table-column prop="merchantId" label="商户编号" width="110" sortable align="center" />
      <el-table-column prop="merchantName" label="商户名称" width="150" align="center" />
      <el-table-column prop="merchantType" label="商户类型" width="100" align="center" />
      <el-table-column prop="location" label="位置" width="200" align="center" />

      <el-table-column prop="businessStatus" label="营业状态" width="110" sortable align="center">
        <template #default="{ row }">
          <el-tag :type="row.businessStatus === 1 ? 'success' : 'danger'">
            {{ row.businessStatus === 1 ? '营业' : '停业' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>

          <el-button
            size="small"
            :type="row.businessStatus === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.businessStatus === 1 ? '停业' : '营业' }}
          </el-button>

          <el-button size="small" type="danger" @click="deleteMerchant(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页（中文化 & 可跳转） -->
    <div style="display: flex; justify-content: center; margin-top: 20px;">
      <el-config-provider :locale="zhCn">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="merchantList.length"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </el-config-provider>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商户' : '新增商户'"
      width="500px"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="商户编号">
          <el-input v-model="form.merchantId" placeholder="自动分配" disabled />
        </el-form-item>
        <el-form-item label="商户名称">
          <el-input v-model="form.merchantName" placeholder="请输入商户名称" />
        </el-form-item>
        <el-form-item label="商户类型">
          <el-select v-model="form.merchantType" placeholder="请选择商户类型" style="width: 100%">
            <el-option label="食堂" value="食堂" />
            <el-option label="超市" value="超市" />
            <el-option label="书店" value="书店" />
            <el-option label="饮品" value="饮品" />
            <el-option label="咖啡厅" value="咖啡厅" />
            <el-option label="医疗" value="医疗" />
            <el-option label="服务" value="服务" />
            <el-option label="生活服务" value="生活服务" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入商户位置" />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-radio-group v-model="form.businessStatus">
            <el-radio :label="1">营业</el-radio>
            <el-radio :label="0">停业</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMerchant">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const API_BASE = 'http://localhost:8080/api/merchant'

const loading = ref(false)
const merchantList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)

const pagedMerchants = computed(() => {
  const s = (pageNum.value - 1) * pageSize.value
  return merchantList.value.slice(s, s + pageSize.value)
})

// 分页事件
const handlePageChange = (page) => {
  pageNum.value = page
}
const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
}

const searchForm = reactive({
  merchantType: '',
  businessStatus: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  merchantId: '',
  merchantName: '',
  merchantType: '',
  location: '',
  businessStatus: 1
})

const loadMerchants = async () => {
  loading.value = true
  try {
    const hasSearch =
      searchForm.merchantType !== '' ||
      searchForm.businessStatus !== ''

    let response
    if (hasSearch) {
      const params = {}
      if (searchForm.merchantType !== '') params.merchantType = searchForm.merchantType
      if (searchForm.businessStatus !== '') params.businessStatus = searchForm.businessStatus
      response = await axios.get(`${API_BASE}/search`, { params })
    } else {
      response = await axios.get(`${API_BASE}/list`)
    }
    merchantList.value = response.data
    pageNum.value = 1  // 数据更新后重置到第一页
  } catch (error) {
    console.error(error)
    ElMessage.error('商户列表加载失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.merchantType = ''
  searchForm.businessStatus = ''
  loadMerchants()
}

const openAddDialog = () => {
  isEdit.value = false
  form.merchantId = ''
  form.merchantName = ''
  form.merchantType = ''
  form.location = ''
  form.businessStatus = 1
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.merchantId = row.merchantId
  form.merchantName = row.merchantName
  form.merchantType = row.merchantType
  form.location = row.location
  form.businessStatus = row.businessStatus
  dialogVisible.value = true
}

const saveMerchant = async () => {
  if (!form.merchantName || !form.merchantType) {
    ElMessage.warning('商户名称和商户类型不能为空')
    return
  }
  try {
    let response
    if (isEdit.value) {
      response = await axios.put(`${API_BASE}/${form.merchantId}`, form)
    } else {
      response = await axios.post(API_BASE, form)
    }
    if (response.data.success) {
      ElMessage.success(response.data.message)
      dialogVisible.value = false
      loadMerchants()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败，请检查后端接口')
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.businessStatus === 1 ? 0 : 1
  const text = newStatus === 1 ? '营业' : '停业'
  try {
    const response = await axios.put(
      `${API_BASE}/${row.merchantId}/status`,
      null,
      { params: { businessStatus: newStatus } }
    )
    if (response.data.success) {
      ElMessage.success(`商户已设置为${text}`)
      loadMerchants()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('状态修改失败')
  }
}

const deleteMerchant = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商户【${row.merchantName}】吗？如果存在历史消费记录，系统将不允许删除。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const response = await axios.delete(`${API_BASE}/${row.merchantId}`)
    if (response.data.success) {
      ElMessage.success(response.data.message)
      loadMerchants()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
/* 强制所有表格单元格垂直水平居中 */
:deep(.el-table__cell) {
  text-align: center !important;
  vertical-align: middle !important;
}
</style>