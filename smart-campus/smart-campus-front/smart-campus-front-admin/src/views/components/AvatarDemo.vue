<template>
  <section class="demo-card">
    <div class="demo-card__head">
      <h3>头像组件</h3>
      <p>通过 BaseAvatarGroup 统一渲染成员头像组和协作人员展示。</p>
    </div>
    <div class="demo-card__body avatar-demo">
      <BaseAvatarGroup :list="members" />
      <p>当前展示的是一个可复用的头像组组件，后续可以扩展尺寸、数量和提示信息。</p>
    </div>
  </section>
</template>

<script setup>
import BaseAvatarGroup from '@/components/BaseAvatarGroup.vue'

const members = [
  { name: '安安', color: '#5b8cff' },
  { name: '乐乐', color: '#27c39f' },
  { name: '周周', color: '#ff8f50' },
  { name: '小北', color: '#8c6dff' },
]
</script>

<style lang="scss" scoped>
.demo-card {
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(30, 49, 86, 0.05);
}

.demo-card__head {
  padding: 14px;
  border-bottom: 1px solid #eef2f8;

  h3 {
    margin: 0 0 6px;
    font-size: 15px;
    color: #23324f;
  }

  p {
    margin: 0;
    color: #677793;
    font-size: 13px;
  }
}

.demo-card__body {
  padding: 14px;
}

.avatar-demo {
  display: flex;
  flex-direction: column;
  gap: 16px;

  p {
    margin: 0;
    color: #667692;
    line-height: 1.7;
    font-size: 13px;
  }
}
</style>
