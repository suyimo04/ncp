<template>
  <main class="app-main">
    <router-view />
  </main>
</template>

<style scoped>
.app-main {
  flex: 1;
  min-height: 0;
  padding: 16px;
  overflow: auto;
  background: var(--agri-bg);
}
</style>
