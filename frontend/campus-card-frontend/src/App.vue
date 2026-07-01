<template>
  <!-- 未登录：显示登录页面 -->
  <LoginView
    v-if="!currentUser"
    @login-success="handleLoginSuccess"
  />

  <!-- 已登录：显示系统主页面 -->
  <el-container v-else class="layout">
    <!-- 左侧导航栏 -->
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-title">校园一卡通</div>
        <div class="brand-subtitle">Campus Card System</div>
      </div>

      <div class="side-user">
        <div class="avatar">
          {{ currentUser.name ? currentUser.name.slice(0, 1) : '用' }}
        </div>

        <div class="side-user-info">
          <div class="side-user-name">{{ currentUser.name }}</div>
          <div class="side-user-role">{{ roleText }}</div>
        </div>
      </div>

      <el-menu
        class="menu"
        :default-active="activeMenu"
        background-color="#26384f"
        text-color="#d7e2f0"
        active-text-color="#ffffff"
        @select="handleMenuSelect"
      >
        <!-- 管理员菜单 -->
        <template v-if="currentUser.userType === 3">
          <div class="menu-group-title">系统管理</div>
          <el-menu-item index="user">用户管理</el-menu-item>
          <el-menu-item index="card">卡片管理</el-menu-item>
          <el-menu-item index="merchant">商户管理</el-menu-item>
          <el-menu-item index="recharge">充值管理</el-menu-item>
          <el-menu-item index="consumption">消费管理</el-menu-item>
        </template>

        <!-- 普通用户菜单 -->
        <template v-else>
          <div class="menu-group-title">个人中心</div>
          <el-menu-item index="myInfo">我的信息</el-menu-item>
          <el-menu-item index="myCard">我的卡片</el-menu-item>
          <el-menu-item index="myRecharge">我的充值记录</el-menu-item>
          <el-menu-item index="myConsumption">我的消费记录</el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="right-layout">
      <el-header class="header">
        <div>
          <div class="header-title">{{ currentTitle }}</div>
          <div class="header-subtitle">
            校园一卡通消费管理系统
          </div>
        </div>

        <div class="user-area">
          <div class="user-text">
            <span class="user-label">当前用户：</span>
            <span class="user-name">{{ currentUser.name }}</span>
            <el-tag
              size="small"
              :type="currentUser.userType === 3 ? 'danger' : 'success'"
            >
              {{ roleText }}
            </el-tag>
          </div>

          <el-button size="small" type="danger" plain @click="logout">
            退出登录
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <div class="content-wrapper">
          <!-- 管理员页面 -->
          <UserView v-if="activeMenu === 'user'" />
          <CardView v-if="activeMenu === 'card'" />
          <MerchantView v-if="activeMenu === 'merchant'" />
          <RechargeView v-if="activeMenu === 'recharge'" />
          <ConsumptionView v-if="activeMenu === 'consumption'" />

          <!-- 普通用户页面 -->
          <MyInfoView
            v-if="activeMenu === 'myInfo'"
            :current-user="currentUser"
          />

          <MyCardView
            v-if="activeMenu === 'myCard'"
            :current-user="currentUser"
          />

          <MyRechargeView
            v-if="activeMenu === 'myRecharge'"
            :current-user="currentUser"
          />

          <MyConsumptionView
            v-if="activeMenu === 'myConsumption'"
            :current-user="currentUser"
          />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import LoginView from './views/LoginView.vue'
import UserView from './views/UserView.vue'
import MerchantView from './views/MerchantView.vue'
import CardView from './views/CardView.vue'
import RechargeView from './views/RechargeView.vue'
import ConsumptionView from './views/ConsumptionView.vue'
import MyInfoView from './views/MyInfoView.vue'
import MyCardView from './views/MyCardView.vue'
import MyRechargeView from './views/MyRechargeView.vue'
import MyConsumptionView from './views/MyConsumptionView.vue'

const currentUser = ref(null)
const activeMenu = ref('user')

const titleMap = {
  user: '用户管理',
  card: '卡片管理',
  merchant: '商户管理',
  recharge: '充值管理',
  consumption: '消费管理',
  myInfo: '我的信息',
  myCard: '我的卡片',
  myRecharge: '我的充值记录',
  myConsumption: '我的消费记录'
}

const currentTitle = computed(() => titleMap[activeMenu.value] || '')

const roleText = computed(() => {
  if (!currentUser.value) return ''

  if (currentUser.value.userType === 3) {
    return '管理员'
  }

  if (currentUser.value.userType === 2) {
    return '教职工'
  }

  return '学生'
})

const handleLoginSuccess = (user) => {
  currentUser.value = user

  if (user.userType === 3) {
    activeMenu.value = 'user'
  } else {
    activeMenu.value = 'myInfo'
  }
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const logout = () => {
  localStorage.removeItem('currentUser')
  currentUser.value = null
  activeMenu.value = 'user'
}

onMounted(() => {
  const savedUser = localStorage.getItem('currentUser')

  if (savedUser) {
    currentUser.value = JSON.parse(savedUser)

    if (currentUser.value.userType === 3) {
      activeMenu.value = 'user'
    } else {
      activeMenu.value = 'myInfo'
    }
  }
})
</script>

<style scoped>
:global(body) {
  margin: 0;
  background: #eef2f7;
}

:global(#app) {
  min-height: 100vh;
}

.layout {
  min-height: 100vh;
  background: #eef2f7;
}

.aside {
  background: #26384f;
  color: white;
  min-height: 100vh;
  box-shadow: 2px 0 12px rgba(31, 45, 61, 0.16);
}

.brand {
  height: 96px;
  padding: 22px 20px 16px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #26384f, #304b6d);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: #a9bed6;
}

.side-user {
  margin: 18px 18px 12px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.side-user-info {
  min-width: 0;
}

.side-user-name {
  font-size: 15px;
  font-weight: 600;
  color: white;
}

.side-user-role {
  margin-top: 4px;
  font-size: 12px;
  color: #b8c9dd;
}

.menu {
  border-right: none;
  padding-top: 6px;
}

.menu-group-title {
  padding: 16px 24px 8px;
  font-size: 12px;
  color: #8ea6c1;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 8px;
}

:deep(.el-menu-item:hover) {
  background: rgba(64, 158, 255, 0.18);
}

:deep(.el-menu-item.is-active) {
  background: #409eff;
  color: white;
  font-weight: 600;
}

.right-layout {
  min-width: 0;
}

.header {
  height: 76px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(31, 45, 61, 0.04);
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
}

.header-subtitle {
  margin-top: 5px;
  font-size: 13px;
  color: #909399;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.user-label {
  color: #909399;
}

.user-name {
  color: #303133;
  font-weight: 600;
}

.main {
  padding: 24px 28px;
  background: #eef2f7;
}

.content-wrapper {
  width: 100%;
  min-height: calc(100vh - 124px);
}
</style>