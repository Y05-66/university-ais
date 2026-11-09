<!-- 应用入口组件 -->
<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
onMounted(() => {
  document.body.classList.remove('dark-theme')
  document.body.classList.add('light-theme')
})
</script>
