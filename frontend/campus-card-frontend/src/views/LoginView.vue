<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">24级二学位校园一卡通系统</h2>
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
          />
        </el-form-item>

        <el-form-item label="验证码">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              clearable
              style="flex: 1"
              @keyup.enter="handleLogin"
            />
            <img :src="captchaImage" alt="验证码" @click="loadCaptcha"
              style="height: 36px; width: 110px; border: 1px solid #ddd; border-radius: 4px; cursor: pointer;"
              title="点击刷新" />
          </div>
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
const captchaToken = ref('')
const captchaImage = ref('')

const loadCaptcha = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/captcha/image')
    captchaToken.value = res.data.token
    captchaImage.value = res.data.imageBase64
  } catch (e) {
    console.error('验证码加载失败', e)
  }
}

const loginForm = reactive({
  phone: '',
  password: '',
  captcha: ''
})

const handleLogin = async () => {
  if (!loginForm.phone || !loginForm.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }

  loading.value = true

  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      phone: loginForm.phone,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaToken: captchaToken.value
    })

    if (response.data.success) {
      ElMessage.success(response.data.message)

      const user = response.data.user
      localStorage.setItem('currentUser', JSON.stringify(user))

      emit('login-success', user)
    } else {
      ElMessage.error(response.data.message)
      loginForm.captcha = ''
      loadCaptcha()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('登录失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

loadCaptcha()
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