<template>
  <div class="shop-cart-panel">
    <div class="data-list">
      <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadProduct" layoutType="line"
        :gridCount="5" :showLoadAll="false">
        <template #default="{ data, index }">
          <ProductSkuItem :data="data" :productCart="true" @delCart="delCartHandler" @checkedChange="productChecked">
          </ProductSkuItem>
        </template>
      </DataLoadMoreList>
    </div>
    <div class="settlement-panel">
      <div class="settlement-title">结算明细</div>
      <el-scrollbar max-height="250px" v-if="selectedProduct?.length>0">
        <div class="select-product-list">
          <div class="select-product-item" v-for="product in selectedProduct">
            <Cover :source="product.productCover"></Cover>
            <div class="btn-cancel" @click="cancelSelect(product)">取消选择</div>
          </div>
        </div>
      </el-scrollbar>
      <div class="no-data-panel" v-else>
        <NoData msg="选择商品查看实际支付价格"></NoData>
      </div>
      <div class="price-panel">
        <div class="price-title">合计：</div>
        <Price :price="totalPrice" :size="20" />
      </div>
      <el-button type="primary" size="large" class="buy-btn"
        @click="postOrder()">提交结算({{selectedProduct?.length}})订单</el-button>
    </div>
  </div>
  <ProductBuy ref="productBuyRef"></ProductBuy>
</template>

<script setup>
import ProductBuy from '@/views/product/ProductBuy.vue'
import ProductSkuItem from '@/views/product/ProductSkuItem.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  watch,
  computed,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadProduct = async (reload = false) => {
  if (reload) {
    dataSource.value.pageNo = 0
  }
  if (
    Object.keys(dataSource.value).length > 0 &&
    dataSource.value.pageNo == dataSource.value.pageTotal
  ) {
    return
  }
  loading.value = true
  let pageNo = dataSource.value.pageNo || 0
  pageNo++
  let result = await proxy.Request({
    url: proxy.Api.loadProductCart,
    showLoading: false,
    params: {
      pageNo,
    },
  })
  loading.value = false
  if (!result) {
    return
  }
  if (result.data.pageNo == 1) {
    dataSource.value = result.data
  } else {
    // dataList.value = dataList.value.concat(result.data.list)
    dataSource.value = {
      ...result.data,
      list: dataSource.value.list.concat(result.data.list),
    }
  }
}

const delCartHandler = (cartId) => {
  const cartData = dataSource.value.list.find((item) => {
    return item.cartId === cartId
  })
  if (!cartData) {
    return
  }
  proxy.Confirm({
    message: `确定要删除【${cartData.productName}】吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.deleteCart,
        params: {
          cartId,
        },
      })
      if (!result) {
        return
      }
      dataSource.value.list = dataSource.value.list.filter((item) => {
        return item.cartId != cartId
      })
    },
  })
}

//选择商品
const selectedProduct = ref([])
const productChecked = ({ data, checked }) => {
  if (checked) {
    selectedProduct.value.push(data)
  } else {
    selectedProduct.value = selectedProduct.value.filter((item) => {
      return item.cartId != data.cartId
    })
  }
}

//总价
const totalPrice = computed(() => {
  return selectedProduct.value?.reduce((total, item) => {
    return total + item.price * item.buyCount
  }, 0)
})
//取消选择
const cancelSelect = (data) => {
  selectedProduct.value = selectedProduct.value.filter((item) => {
    return item.cartId != data.cartId
  })

  const cart = dataSource.value.list.find((item) => {
    return item.cartId == data.cartId
  })
  if (cart) {
    cart.checked = false
  }
}
//结算
const productBuyRef = ref()
const postOrder = async () => {
  if (selectedProduct.value.length == 0) {
    proxy.Message.warning('请选择要购买的商品')
    return
  }
  productBuyRef.value.showBuy(selectedProduct.value, 1)
}
</script>

<style lang="scss" scoped>
.shop-cart-panel {
  display: flex;
  align-items: flex-start;
  .data-list {
    flex: 1;
  }
  .settlement-panel {
    padding: 10px;
    width: 300px;
    position: sticky;
    top: 70px;
    border: 1px solid #ddd;
    border-radius: 10px;
    margin-left: 20px;
    .settlement-title {
      font-weight: 600;
      font-size: 16px;
    }
    .select-product-list {
      margin-top: 10px;
      display: grid;
      grid-gap: 20px;
      grid-template-columns: repeat(3, 1fr);
      .select-product-item {
        position: relative;
        &:hover {
          .btn-cancel {
            display: block;
          }
        }
        .btn-cancel {
          display: none;
          position: absolute;
          top: 0px;
          width: 100%;
          height: 20px;
          background: rgb(000, 000, 000, 0.4);
          color: #fff;
          text-align: center;
          border-radius: 0px 0px 5px 5px;
          cursor: pointer;
        }
      }
    }
    .no-data-panel {
      height: 90px;
    }
    .price-panel {
      margin-top: 10px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 18px;
      .price-title {
        font-weight: 600;
      }
    }
    .buy-btn {
      margin-top: 20px;
      width: 100%;
    }
  }
}
</style>
