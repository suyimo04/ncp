<template>
  <div class="not-found">
    <el-empty description="页面不存在">
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: var(--agri-bg);
}
</style>
