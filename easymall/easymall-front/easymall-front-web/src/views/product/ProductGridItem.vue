<template>
  <div class="product-item-panel" @click="proxy.Utils.jump(`/detail/${data.productId}`,false)">
    <Cover :source="data.cover.split(',')[0]" fit="cover" :scale="1" class="cover"></Cover>
    <div class="product-name">{{ data.productName }}</div>
    <div class="price-panel">
      <Price :price="data.minPrice" :size="16"></Price>
      <div class="total-sale">{{ data.totalSale || 0 }}人购买</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
  partType: {
    type: String,
    default: 'normal',
  },
})
</script>

<style lang="scss" scoped>
.product-item-panel {
  min-width: 0;
  aspect-ratio: 1;

  .cover {
    :deep() {
      img {
        transition: transform 0.5s ease;
      }
    }

    &:hover {
      :deep() {
        img {
          transform: scale(1.1);
        }
      }
    }
  }

  .product-name {
    font-size: 15px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    margin-top: 5px;
    cursor: pointer;
  }

  .price-panel {
    margin-top: 5px;
    display: flex;
    justify-content: space-between;

    .total-sale {
      font-size: 14px;
      color: var(--text2);
    }
  }
}
</style>
