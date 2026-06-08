<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon"><Link /></div>
        <h1>农产品承诺达标合格证</h1>
        <p>可信存证与追溯系统</p>
      </div>
      <el-form :model="form" size="large" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登录系统</el-button>
      </el-form>
      <div class="account-tip">
        默认账号：admin / regulator / producer，密码均为 admin123
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link, Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123'
})

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect === '/' ? '/dashboard' : route.query.redirect
    router.replace(redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(31, 138, 91, 0.08), rgba(242, 184, 75, 0.08)),
    var(--agri-bg);
}

.login-card {
  width: min(440px, calc(100vw - 32px));
  padding: 34px;
  background: #fff;
  border: 1px solid var(--agri-border);
  border-radius: 8px;
  box-shadow: 0 12px 30px rgba(23, 59, 47, 0.12);
}

.brand {
  margin-bottom: 24px;
  text-align: center;
}

.brand-icon {
  display: inline-flex;
  width: 58px;
  height: 58px;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #fff;
  background: var(--agri-primary);
  border-radius: 8px;
}

.brand-icon :deep(svg) {
  width: 30px;
  height: 30px;
}

h1 {
  margin: 0 0 6px;
  font-size: 24px;
}

p {
  margin: 0;
  color: var(--agri-text-secondary);
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.account-tip {
  margin-top: 18px;
  color: var(--agri-text-secondary);
  font-size: 12px;
  text-align: center;
}
</style>
