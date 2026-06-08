<template>
  <div class="layout">
    <Sidebar />
    <div class="layout-main">
      <Navbar />
      <TagsView />
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import TagsView from './components/TagsView.vue'
import AppMain from './components/AppMain.vue'
</script>

<style scoped>
.layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--agri-bg);
}

.layout-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}
</style>
