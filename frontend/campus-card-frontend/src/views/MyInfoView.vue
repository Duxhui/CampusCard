<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的信息</span>

          <div>
            <el-button type="primary" size="small" @click="openEditDialog">
              修改信息
            </el-button>

            <el-button size="small" @click="loadUserInfo">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-descriptions
        v-if="userInfo"
        :column="2"
        border
        class="info-descriptions"
      >
        <el-descriptions-item label="用户ID">
          {{ userInfo.userId }}
        </el-descriptions-item>

        <el-descriptions-item label="姓名">
          {{ userInfo.name }}
        </el-descriptions-item>

        <el-descriptions-item label="身份证号">
          {{ userInfo.idNumber }}
        </el-descriptions-item>

        <el-descriptions-item label="手机号">
          {{ userInfo.phone }}
        </el-descriptions-item>

        <el-descriptions-item label="邮箱">
          {{ userInfo.email }}
        </el-descriptions-item>

        <el-descriptions-item label="用户类型">
          <el-tag :type="getUserTypeTag(userInfo.userType)">
            {{ getUserTypeText(userInfo.userType) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="账户余额">
          <span class="balance">￥{{ userInfo.balance }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="注册时间">
          {{ formatTime(userInfo.registerTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-empty v-else description="暂无用户信息" />

      <div class="note">
        说明：姓名、身份证号、用户类型、账户余额等关键信息由管理员维护；普通用户只能修改手机号、邮箱和登录密码。
      </div>
    </el-card>

    <!-- 修改信息弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改个人信息"
      width="520px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" disabled />
        </el-form-item>

        <el-form-item label="身份证号">
          <el-input v-model="editForm.idNumber" disabled />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input
            v-model="editForm.email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="新密码">
          <el-input
            v-model="editForm.newPassword"
            type="password"
            placeholder="不修改密码可留空"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
            v-model="editForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitting"
          :disabled="submitting"
          @click="saveUserInfo"
        >
          保存修改
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

const userInfo = ref(null)
const editDialogVisible = ref(false)
const submitting = ref(false)

// 保存原密码：如果用户不修改密码，提交时继续使用原密码
const originalPassword = ref('')

const editForm = reactive({
  userId: '',
  name: '',
  idNumber: '',
  phone: '',
  email: '',
  userType: '',
  balance: '',
  registerTime: '',
  newPassword: '',
  confirmPassword: ''
})

const loadUserInfo = async () => {
  if (!props.currentUser || !props.currentUser.userId) {
    ElMessage.error('当前登录用户信息不存在')
    return
  }

  try {
    const response = await axios.get(
      `http://localhost:8080/api/user/${props.currentUser.userId}`
    )

    userInfo.value = response.data
    originalPassword.value = response.data.password || ''
  } catch (error) {
    console.error(error)
    ElMessage.error('用户信息加载失败，请检查后端是否启动')
  }
}

const openEditDialog = () => {
  if (!userInfo.value) {
    ElMessage.warning('用户信息尚未加载')
    return
  }

  editForm.userId = userInfo.value.userId
  editForm.name = userInfo.value.name
  editForm.idNumber = userInfo.value.idNumber
  editForm.phone = userInfo.value.phone
  editForm.email = userInfo.value.email
  editForm.userType = userInfo.value.userType
  editForm.balance = userInfo.value.balance
  editForm.registerTime = userInfo.value.registerTime
  editForm.newPassword = ''
  editForm.confirmPassword = ''

  editDialogVisible.value = true
}

const saveUserInfo = async () => {
  if (submitting.value) {
    return
  }

  if (!editForm.phone) {
    ElMessage.warning('手机号不能为空')
    return
  }

  if (!/^1\d{10}$/.test(editForm.phone)) {
    ElMessage.warning('手机号格式不正确，应为 11 位手机号')
    return
  }

  if (!editForm.email) {
    ElMessage.warning('邮箱不能为空')
    return
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  if (editForm.newPassword || editForm.confirmPassword) {
    if (!editForm.newPassword) {
      ElMessage.warning('请输入新密码')
      return
    }

    if (!editForm.confirmPassword) {
      ElMessage.warning('请确认新密码')
      return
    }

    if (editForm.newPassword !== editForm.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }

    if (editForm.newPassword.length < 6) {
      ElMessage.warning('密码长度不能少于 6 位')
      return
    }
  }

  try {
    await ElMessageBox.confirm(
      '确定要保存修改吗？如果修改了手机号，下次登录需要使用新的手机号。',
      '保存确认',
      {
        confirmButtonText: '确定保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch (error) {
    return
  }

  submitting.value = true

  try {
    const submitData = {
      userId: editForm.userId,
      name: editForm.name,
      idNumber: editForm.idNumber,
      phone: editForm.phone,
      email: editForm.email,
      password: editForm.newPassword || originalPassword.value,
      userType: editForm.userType,
      balance: editForm.balance,
      registerTime: editForm.registerTime
    }

    // 后端 /api/user/{id} PUT 返回的是 User 对象本身（非 {success, message} 格式）
    // 能走到这一步说明 HTTP 200，直接视为成功
    await axios.put(
      `http://localhost:8080/api/user/${editForm.userId}`,
      submitData
    )

    ElMessage.success('个人信息修改成功')

    editDialogVisible.value = false

    // 更新本地登录信息，避免手机号修改后 localStorage 还是旧值
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
      const localUser = JSON.parse(savedUser)
      localUser.phone = editForm.phone
      localUser.email = editForm.email
      localStorage.setItem('currentUser', JSON.stringify(localUser))
    }

    loadUserInfo()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败，请检查后端接口')
  } finally {
    submitting.value = false
  }
}

const getUserTypeText = (type) => {
  if (type === 1) return '学生'
  if (type === 2) return '教职工'
  if (type === 3) return '管理员'
  return '未知'
}

const getUserTypeTag = (type) => {
  if (type === 1) return 'success'
  if (type === 2) return 'primary'
  if (type === 3) return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ')
}

onMounted(() => {
  loadUserInfo()
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

.info-descriptions {
  margin-top: 10px;
}

.balance {
  color: #e6a23c;
  font-weight: bold;
  font-size: 18px;
}

.note {
  margin-top: 18px;
  padding: 12px 14px;
  border-radius: 6px;
  background: #f4f7fb;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}
</style>