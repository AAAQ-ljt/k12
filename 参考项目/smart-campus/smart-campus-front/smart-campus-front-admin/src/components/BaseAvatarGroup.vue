<template>
  <div class="avatar-group">
    <div
      v-for="item in list"
      :key="item.name"
      class="avatar-item"
      :title="item.name"
      :style="{ background: item.color }"
    >
      {{ item.name.slice(0, 1) }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  list: { type: Array, default: () => [] },
})
</script>

<style lang="scss" scoped>
.avatar-group {
  display: flex;
  align-items: center;
}

.avatar-item {
  display: grid;
  width: 44px;
  height: 44px;
  margin-left: -8px;
  place-items: center;
  border: 2px solid #fff;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 8px 16px rgba(45, 68, 114, 0.12);

  &:first-child {
    margin-left: 0;
  }
}
</style>
