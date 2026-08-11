<template>
  <div class="product-panel">
    <div class="product-panel-title">以下是为你推荐的商品</div>
    <div class="product-list">
      <ProductGridItem v-for="item in data" :data="item" />
    </div>
    <div v-if="data.length == 0" class="no-product">
      <NoData msg="抱歉没有搜索到商品"></NoData>
    </div>
  </div>
</template>

<script setup>
import NoData from '@/components/NoData.vue';
import ProductGridItem from '@/views/product/ProductGridItem.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})
</script>

<style lang="scss" scoped>
.product-panel {
  border: 1px solid #ddd;
  border-radius: 5px;
  overflow: hidden;

  .product-panel-title {
    background: #f8f8f8;
    padding: 5px 10px;
  }

  .product-list {
    max-height: 300px;
    padding: 10px;
    overflow: auto;
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    grid-gap: 20px;
    padding-bottom: 10px;

    &::-webkit-scrollbar {
      width: 4px;
      height: 4px;
    }
  }

  .no-product {
    padding: 10px 0px;
  }
}
</style>
