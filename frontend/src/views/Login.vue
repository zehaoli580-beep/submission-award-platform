<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api'
import { auth } from '../store/auth'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirm: '', nickname: '', role: 'AUTHOR' })

async function doLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const user = await login({ ...loginForm })
    auth.setUser(user)
    ElMessage.success(`欢迎回来, ${user.nickname}`)
    router.push('/')
  } catch {
    /* 错误提示已在拦截器统一弹出 */
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  const f = regForm
  if (!f.username || !f.password || !f.nickname) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (f.password !== f.confirm) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await register({ username: f.username, password: f.password, nickname: f.nickname, role: f.role })
    ElMessage.success('注册成功, 请登录')
    activeTab.value = 'login'
    loginForm.username = f.username
    loginForm.password = ''
  } catch {
    /* 同上 */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-bg">
    <!-- 左侧品牌展示区(窄屏隐藏) -->
    <div class="brand-panel">
      <div class="brand-inner">
        <div class="brand-logo">媒</div>
        <h1 class="brand-title">数字媒体作品<br />投稿/评奖平台</h1>
        <p class="brand-sub">Digital Media Submission &amp; Award Platform</p>
        <ul class="brand-features">
          <li>🎬 支持视频 / 图片 / 压缩包等大文件作品上传</li>
          <li>🔁 投稿 → 初审 → 盲审 → 终审 全流程状态可查</li>
          <li>🏆 奖项评选: 评委打分 · 去极值均值 · 公示排名</li>
          <li>🔒 状态机防越权 · 操作全程留痕</li>
        </ul>
        <div class="brand-tech">
          <span>Vue 3</span><span>Spring Boot</span><span>MySQL</span>
        </div>
      </div>
    </div>

    <!-- 右侧登录卡片 -->
    <div class="login-card">
      <h1 class="title">欢迎登录</h1>
      <p class="subtitle">Digital Media Submission &amp; Award Platform</p>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form @keyup.enter="doLogin">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" size="large">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password size="large">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doLogin">
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form @keyup.enter="doRegister">
            <el-form-item>
              <el-input v-model="regForm.username" placeholder="用户名(3-50位)" size="large">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.nickname" placeholder="昵称" size="large">
                <template #prefix><el-icon><Postcard /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.password" type="password" placeholder="密码(至少6位)" show-password size="large">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.confirm" type="password" placeholder="确认密码" show-password size="large">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-radio-group v-model="regForm.role">
                <el-radio value="AUTHOR">作者(投稿)</el-radio>
                <el-radio value="REVIEWER">审稿人</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doRegister">
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="demo-tip">
        演示账号:admin / author / reviewer(密码见 README)
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-bg {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1;
  max-width: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 40px;
}

.brand-inner {
  max-width: 400px;
}

.brand-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  font-size: 32px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 22px;
}

.brand-title {
  font-size: 32px;
  line-height: 1.3;
  font-weight: 700;
  margin: 0 0 8px;
}

.brand-sub {
  font-size: 13px;
  opacity: 0.75;
  margin: 0 0 28px;
  letter-spacing: 0.5px;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0 0 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.brand-features li {
  font-size: 14px;
  opacity: 0.92;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  padding: 10px 14px;
}

.brand-tech {
  display: flex;
  gap: 8px;
}

.brand-tech span {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  opacity: 0.85;
}

/* ===== 右侧登录卡片 ===== */
.login-card {
  width: 420px;
  padding: 48px 44px 32px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.15);
}

.title {
  font-size: 22px;
  text-align: center;
  color: #303133;
  margin: 0;
}

.subtitle {
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin: 6px 0 18px;
}

.submit-btn {
  width: 100%;
  margin-top: 4px;
}

.demo-tip {
  margin-top: 14px;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}

/* 窄屏: 隐藏品牌区, 只保留登录卡 */
@media (max-width: 860px) {
  .brand-panel {
    display: none;
  }

  .login-bg {
    align-items: center;
  }

  .login-card {
    width: 92vw;
    max-width: 420px;
    border-radius: 12px;
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
  }
}
</style>
