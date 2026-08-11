<template>
  <div class="product-list">
    <ProductList></ProductList>
  </div>
</template>

<script setup>
import ProductList from '@/views/product/ProductList.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
</script>

<style lang="scss" scoped>
.product-list {
  padding-top: 70px;
  min-height: calc(100vh - 289px);
}
</style>
