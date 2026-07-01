<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">校园一卡通管理系统</h2>
      <p class="subtitle">用户登录</p>

      <el-form :model="loginForm" label-width="70px">
        <el-form-item label="手机号">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          type="primary"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>

      <div class="tips">
        <p>管理员：13800000015 / admin123</p>
        <p>普通用户：13800000001 / 123456</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['login-success'])

const loading = ref(false)

const loginForm = reactive({
  phone: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.phone || !loginForm.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }

  loading.value = true

  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', loginForm)

    if (response.data.success) {
      ElMessage.success(response.data.message)

      const user = response.data.user
      localStorage.setItem('currentUser', JSON.stringify(user))

      emit('login-success', user)
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('登录失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2f4056, #409eff);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 12px;
}

.title {
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

.tips {
  margin-top: 20px;
  font-size: 13px;
  color: #888;
  line-height: 1.6;
}
</style>