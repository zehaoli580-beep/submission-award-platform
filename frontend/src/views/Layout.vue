<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { auth } from '../store/auth'
import { ROLE_CN } from '../utils/format'

const router = useRouter()
const route = useRoute()
const pageTitle = computed(() => route.meta?.title || '')

async function handleLogout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">数字媒体作品投稿/评奖平台</div>
      <div class="header-right">
        <router-link to="/awards" class="nav-link">获奖公示</router-link>
        <el-tag :type="auth.user?.role === 'ADMIN' ? 'danger' : auth.user?.role === 'REVIEWER' ? 'warning' : 'success'">
          {{ ROLE_CN[auth.user?.role] || auth.user?.role }}
        </el-tag>
        <span class="nickname">{{ auth.user?.nickname }}</span>
        <el-button link type="primary" @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <div class="page-title">{{ pageTitle }}</div>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  font-size: 17px;
  font-weight: 600;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand::before {
  content: '🎬';
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  font-size: 14px;
  color: #606266;
}

.nav-link {
  font-size: 14px;
  color: #606266;
  text-decoration: none;
}

.nav-link:hover {
  color: #409eff;
}

.main {
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}
</style>
