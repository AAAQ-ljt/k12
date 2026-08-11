<template>
  <section class="demo-card">
    <div class="demo-card__head">
      <h3>抽屉组件</h3>
      <p>用于详情查看、侧边编辑和轻量操作面板。</p>
    </div>
    <div class="demo-card__body">
      <el-button type="primary" @click="visible = true">打开抽屉</el-button>

      <BaseDrawer v-model="visible" title="订单详情抽屉" />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import BaseDrawer from '@/components/BaseDrawer.vue'

const visible = ref(false)
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
</style>
