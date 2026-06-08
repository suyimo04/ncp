<template>
  <el-button v-bind="$attrs" :type="type">
    <slot />
  </el-button>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'primary'
  }
})
</script>
