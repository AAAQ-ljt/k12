<template>
  <div class="product-panel">
    <div class="product-panel-title">以下是为你查询的近15天的订单信息</div>
    <div class="product-list">
      <OrderItem :data="item" v-for="item in data" showType="agent">
      </OrderItem>
    </div>
    <div v-if="data.length == 0" class="no-product">
      <NoData msg="没有获取到任何订单"></NoData>
    </div>
  </div>
</template>
<script setup>
import OrderItem from '@/views/my/order/OrderItem.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

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
