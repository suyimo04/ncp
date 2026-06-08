<template>
  <section class="app-card">
    <div v-if="title || $slots.extra" class="card-head">
      <h3 v-if="title">{{ title }}</h3>
      <div><slot name="extra" /></div>
    </div>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: String
})
</script>

<style scoped>
.app-card {
  padding: 16px;
  background: var(--agri-card-bg);
  border: 1px solid var(--agri-border);
  border-radius: var(--agri-radius);
  box-shadow: var(--agri-shadow);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

h3 {
  margin: 0;
  font-size: 16px;
  color: var(--agri-text);
}
</style>
