<template>
  <div class="page-header glass-card">
    <div class="header-content">
      <h2 class="gradient-text"><slot name="title">{{ title }}</slot></h2>
      <p class="description"><slot name="description">{{ description }}</slot></p>
    </div>
    <div class="header-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 24px;
  border-radius: var(--radius-md);
}
.header-content h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px 0;
}
.description {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 12px;
}
</style>
