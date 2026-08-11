<template>
  <div class="order-item">
    <div class="title-panel">
      <div class="label product-name">
        商品信息
      </div>
      <div class="label product-property">
        商品属性
      </div>
      <div class="label product-count">
        数量
      </div>
      <div class="label product-price">
        价格
      </div>
      <div class="label product-op" v-if="productCart">
        操作
      </div>
    </div>
    <div class="data-panel">
      <div class="label product-name data-product-name">
        <el-checkbox v-model="data.checked" size="large" v-if="productCart" class="product-check" :true-value="1"
          :false-value="0" @change="checkedChangeHandler(data, $event)" :disabled="cantBuy" />
        <router-link :to="`/detail/${data.productId}`" target="_blank">
          <Cover :source="data.productCover" :width="80"></Cover>
        </router-link>
        <router-link :class="['product-name-value', cantBuy ? 'cant-buy' : '']" :to="`/detail/${data.productId}`"
          target="_blank">{{ data.productName }}</router-link>
      </div>
      <div class="label product-property">
        <div :class="['product-property-value', cantBuy ? 'cant-buy' : '']" v-for="property in data.propertyData">
          {{ property.propertyName }}: {{ property.propertyValue }}
        </div>
      </div>
      <div class="label product-count">
        <el-input-number v-model="data.buyCount" :min="1" />
      </div>
      <div class="label product-price">
        <Price :price="data.price * data.buyCount" :size="16"></Price>
      </div>
      <div class="label product-op product-del" @click="delCart(data.cartId)" v-if="productCart">
        删除
      </div>
    </div>
    <div class="order-remark" v-if="!productCart">
      <div class="label">订单备注</div>
      <div class="remark-input">
        <el-input clearable placeholder="请输入，付款后商家可见，建议和商家协商一致" v-model="data.remark"></el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
  productCart: {
    type: Boolean,
    default: false,
  },
})

const cantBuy = computed(() => {
  return (
    props.productCart && (!props.data.productOnSale || props.data.stock == 0)
  )
})

const emit = defineEmits(['delCart', 'checkedChange'])

const delCart = (cartId) => {
  emit('delCart', cartId)
}

const checkedChangeHandler = (data, e) => {
  emit('checkedChange', { data, checked: e == 1 })
}
</script>

<style lang="scss" scoped>
.order-item {
  margin-top: 10px;
  border: 1px solid #ddd;
  border-radius: 10px;
  overflow: hidden;

  .title-panel,
  .data-panel {
    display: flex;
    padding: 10px 0px;
    align-items: center;

    .label {
      padding: 0px 8px;
    }
  }

  .title-panel {
    border-bottom: 1px solid #ddd;
  }

  .product-name {
    flex: 1;
    width: 0;
    text-decoration: none;
  }

  .product-property {
    width: 300px;
  }

  .product-count {
    width: 200px;
  }

  .product-price {
    width: 100px;
  }

  .data-product-name {
    display: flex;
    align-items: center;

    .product-check {
      margin-right: 5px;
    }

    .product-name-value {
      margin-left: 10px;
      text-decoration: none;
      color: #1f1f1f;

      &:hover {
        color: var(--pink);
      }
    }
  }

  .product-op {
    width: 50px;
  }

  .product-del {
    color: #7a7a7a;
    cursor: pointer;
  }

  .product-property-value {
    color: #7a7a7a;
    font-size: 12px;
    margin: 3px 0px;
  }

  .order-remark {
    background: #f7f7f7;
    padding: 10px;
    display: flex;
    align-items: center;

    .label {
      font-weight: 600;
      margin-right: 10px;
    }

    .remark-input {
      width: 500px;
    }
  }

  .cant-buy {
    color: #cccccc !important;
  }
}
</style>
