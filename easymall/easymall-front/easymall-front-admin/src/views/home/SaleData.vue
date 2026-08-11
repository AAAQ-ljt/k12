<template>
  <div class="sale-panel">
    <div class="card">
      <ShippedOrder />
    </div>
    <div class="card">
      <LessStockData />
    </div>
  </div>
</template>

<script setup>
import LessStockData from './LessStockData.vue';
import ShippedOrder from './ShippedOrder.vue';
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
  border-radius: 5px;
  overflow: hidden;
  height: calc(100vh - 500px);
  padding: 10px;
}

.sale-panel {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 20px;
}
</style>
