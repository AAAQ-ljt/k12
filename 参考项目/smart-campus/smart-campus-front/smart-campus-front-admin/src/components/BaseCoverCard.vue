<template>
  <div class="base-cover-card">
    <div class="cover-image" :style="{ backgroundImage: `url(${image})` }"></div>
    <div class="cover-body">
      <h4>{{ title }}</h4>
      <p>{{ description }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  image: { type: String, required: true },
  title: { type: String, default: '封面标题' },
  description: { type: String, default: '用于课程、商品、活动等业务封面展示。' },
})
</script>

<style lang="scss" scoped>
.base-cover-card {
  overflow: hidden;
  border: 1px solid #e3eaf7;
  border-radius: 12px;
  background: #fff;
}

.cover-image {
  height: 160px;
  background-position: center;
  background-size: cover;
}

.cover-body {
  padding: 14px;

  h4 {
    margin: 0 0 8px;
    font-size: 15px;
    color: #263552;
  }

  p {
    margin: 0;
    color: #667692;
    line-height: 1.7;
    font-size: 13px;
  }
}
</style>
