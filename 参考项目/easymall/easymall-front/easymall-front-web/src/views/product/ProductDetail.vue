<template>
  <div class="product-detail-view">
    <div class="product-left">
      <div class="product-image-panel">
        <div class="image-list">
          <div :class="['image-item', { active: image == selectedImage }]"
            v-for="image in productInfo?.cover?.split(',')">
            <Cover :source="image" :width="100" @click="selectImage(image)"></Cover>
          </div>
        </div>
        <div class="image-view">
          <Vue3ImageMagnifier :src="showImage" :zoom-src="showImage" width="100%" :zoom-width="500" :zoom-scale="2" />
        </div>
      </div>
      <div class="product-desc">
        <div class="product-tabs">
          <el-tabs v-model="activeTab" @tab-click="tabClick">
            <el-tab-pane label="用户评价" name="comment"></el-tab-pane>
            <el-tab-pane label="图文详情" name="desc"></el-tab-pane>
          </el-tabs>
        </div>
        <div id="comment" class="tab-title"></div>
        <ProductComment></ProductComment>
        <div id="desc" class="tab-title">图文详情</div>
        <MarkdownView v-model="productInfo.productDesc"></MarkdownView>
      </div>
    </div>

    <div class="product-info-panel">
      <div class="product-info-panel-innner">
        <div class="product-name">{{ productInfo.productName }}</div>
        <div class="price-panel">
          <Price :price="selectedSku.price" :size="26"></Price>
        </div>
        <div class="property-list">
          <div class="property-item" v-for="property in productPropertyList">
            <div class="property-name">{{ property.propertyName }}</div>
            <div class="property-values">
              <div
                :class="['property-value-panel', { active: selectedProperty[property.propertyId] == value.propertyValueId }]"
                v-for="value in property.propertyValues" @click="selectProperty(property, value)">
                <Cover v-if="value.propertyCover" :source="value.propertyCover" :width="25"></Cover>
                <div class="property-value">
                  {{ value.propertyValue }}
                  <template v-if="value.propertyRemark">({{ value.propertyRemark }})</template>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="stock-panel">
          <div class="stock-label">库存</div>
          <div class="stock-value">{{ selectedSku.stock }}</div>
          <div class="stock-tips" v-if="selectedSku.stock <= 5">库存紧张</div>
        </div>
        <div class="buy-count-panel">
          <div class="buy-count-label">数量</div>
          <div class="buy-count-input">
            <el-input-number v-model="buyCount" :min="1" size="large" />
          </div>
        </div>
        <div class="buy-panel">
          <div class="btn-cart iconfont icon-cart" @click="add2Cart"></div>
          <div class="btn-buy" @click="buy">立即购买</div>
        </div>
      </div>
    </div>
  </div>
  <ProductBuy ref="productBuyRef"></ProductBuy>
</template>

<script setup>
import ProductBuy from '@/views/product/ProductBuy.vue'
import ProductComment from './ProductComment.vue'
import MarkdownView from '@/components/markdown/MarkdownView.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  onMounted,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import Vue3ImageMagnifier from 'vue3-image-magnifier'
import 'vue3-image-magnifier/dist/vue3-image-magnifier.css'
import { useLoginStore } from '@/stores/loginStore.js'
const loginStore = useLoginStore()

const productInfo = ref({})
const productPropertyList = ref([])
const skuList = ref([])
const getProduct = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getProduct,
    params: {
      productId: route.params.productId,
    },
  })
  if (!result) {
    return
  }
  productInfo.value = result.data.productInfo
  //默认给第一个主图
  selectImage(productInfo.value.cover.split(',')[0])

  productPropertyList.value = result.data.productPropertyList
  skuList.value = result.data.skuList
  //初始化默认选中
  initDefaultPropertySelected()
}

const showImage = computed(() => {
  return (
    proxy.Api.sourcePath +
    selectedImage.value?.replace(proxy.imageThumbnailSuffix, '')
  )
})

const selectedImage = ref()
const selectImage = (img) => {
  selectedImage.value = img
}

//已经选择的属性
const selectedSku = ref({})
const selectedProperty = ref({})
const propertyImageMap = ref({})
const initDefaultPropertySelected = () => {
  selectedSku.value = skuList.value[0]
  const propertyValueIdArray = selectedSku.value.propertyValueIds.split('-')
  //初始化选中的图片
  let initSelect = null
  for (let [index, property] of productPropertyList.value.entries()) {
    selectedProperty.value[property.propertyId] = propertyValueIdArray[index]
    for (const prop of property.propertyValues) {
      if (prop.propertyCover) {
        propertyImageMap[prop.propertyValueId] = prop.propertyCover
        //sku有图，默认替换主图
        if (initSelect == null) {
          selectImage(prop.propertyCover)
        }
        initSelect = prop.propertyCover
      }
    }
  }
}

//选择属性
const selectProperty = (property, propertyValue) => {
  const tempSelectedProperty = { ...selectedProperty.value }
  tempSelectedProperty[property.propertyId] = propertyValue.propertyValueId
  //查找匹配的SKU
  const selectedPropertyValueIds = productPropertyList.value
    .map((prop) => tempSelectedProperty[prop.propertyId])
    .join('-')
  const matchedSku = skuList.value.find(
    (sku) => sku.propertyValueIds === selectedPropertyValueIds
  )
  if (!matchedSku) {
    proxy.Message.warning('sku不存在')
    return
  }
  if (matchedSku.stock === 0) {
    proxy.Message.warning('sku对应的库存为0')
    return
  }
  selectedProperty.value[property.propertyId] = propertyValue.propertyValueId
  selectedSku.value = matchedSku
  //获取图片
  const image = propertyImageMap[propertyValue.propertyValueId]
  if (image) {
    selectImage(image)
  }
}

//详情
const activeTab = ref('comment')
const tabClick = (e) => {
  const target = document.getElementById(e.paneName)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth' })
  }
}

//购买
const buyCount = ref(1)
const productBuyRef = ref()
const buy = () => {
  if (Object.keys(loginStore.userInfo).length == 0) {
    loginStore.showLogin = true;
    return;
  }
  if (selectedSku.value?.stock < buyCount.value) {
    proxy.Message.warning('商品库存不足')
    return
  }
  console.log(productPropertyList.value)
  console.log(selectedProperty.value)
  const propertyData = []
  //属性封面
  let propertCover = null
  //遍历选择的属性
  // 按 productPropertyList 的顺序遍历（保证属性顺序正确）
  productPropertyList.value.forEach((propertyItem, index) => {
    const selectedValueId = selectedProperty.value[propertyItem.propertyId]
    if (!selectedValueId) return  // 理论上不会有未选中的情况，但做防御

    const propertyValueItem = propertyItem.propertyValues.find(
      (item) => item.propertyValueId === selectedValueId
    )
    if (propertyValueItem) {
      // 第一个属性且有封面时，作为商品临时封面
      if (index === 0 && propertyValueItem?.propertyCover) {
        propertCover = propertyValueItem.propertyCover
      }
      propertyData.push({
        propertyName: propertyItem.propertyName,
        propertyValue: propertyValueItem.propertyValue,
      })
    }
  })
  const orderData = [
    {
      //商品封面 取属性封面没有就取商品封面图第一个
      productCover: propertCover
        ? propertCover
        : productInfo.value.cover.split(',')[0],
      //sku信息
      ...selectedSku.value,
      //商品信息
      productName: productInfo.value.productName,
      //属性信息
      propertyData,
      buyCount: buyCount.value,
    },
  ]
  console.log(orderData)
  productBuyRef.value.showBuy(orderData, 0)
}

//加入购物车
const add2Cart = async () => {
  if (Object.keys(loginStore.userInfo).length == 0) {
    loginStore.showLogin = true;
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.add2Cart,
    params: {
      productId: productInfo.value.productId,
      buyCount: buyCount.value,
      propertyValueIds: selectedSku.value.propertyValueIds,
    },
  })
  if (!result) {
    return
  }
  proxy.Message.success('加入购物车成功')
}

onMounted(() => {
  getProduct()
})
</script>

<style lang="scss" scoped>
.product-detail-view {
  display: flex;

  .product-left {
    width: 810px;
  }

  .product-image-panel {
    height: 700px;
    display: flex;

    .image-list {
      width: 100px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;

      .image-item {
        margin-bottom: 10px;
        border-radius: 5px;
        overflow: hidden;
        border: 1px solid #fff;

        &:hover {
          border: 1px solid #d62626;
        }
      }

      .active {
        border: 1px solid #d62626;
      }
    }

    .image-view {
      flex: 1;
      width: 0;
      background: #f6f7fb;
      margin-left: 10px;
      display: flex;
      align-items: center;
      justify-content: center;

      :deep(.magnifier-container) {
        height: 100%;

        .zoom-result {
          z-index: 1000;
        }

        img {
          object-fit: contain;
        }
      }
    }
  }

  .product-info-panel {
    flex: 1;
    width: 0;
    border-radius: 5px;
    margin-left: 10px;

    .product-info-panel-innner {
      position: sticky;
      top: 70px;
      z-index: 2;
      height: calc(100vh - 70px);
      padding: 0px 10px 10px;
      overflow: auto;

      &::-webkit-scrollbar {
        width: 4px;
        height: 4px;
        /**/
      }

      .product-name {
        font-size: 24px;
        font-weight: 700;
        color: #333;
        margin-bottom: 15px;
      }

      .price-panel {
        margin-top: 10px;
      }

      .property-list {
        .property-item {
          margin-top: 20px;

          .property-name {
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }

          .property-values {
            display: flex;
            flex-wrap: wrap;
            margin-top: 5px;

            .property-value-panel {
              display: flex;
              align-items: center;
              border: 1px solid #ddd;
              background-color: #f8f8f8;
              border-radius: 6px;
              margin-right: 15px;
              padding: 5px;
              margin-bottom: 10px;

              :deep(.image-panel) {
                margin-right: 5px;
              }

              &:hover {
                border: 1px solid #ff475d;
                background: #ffebf1;
              }

              .property-value {
                cursor: pointer;
                transition: all 0.2s ease;
                font-size: 14px;
                color: #333;
                position: relative;
                padding: 3px;
              }
            }

            .active {
              border: 1px solid #ff475d;
              background: #ffebf1;
            }
          }
        }
      }

      .stock-panel {
        margin-top: 20px;
        display: flex;
        align-items: center;

        .stock-label {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }

        .stock-value {
          margin-left: 10px;
          display: flex;
          align-items: center;
          color: #d62626;
          font-size: 16px;
          font-weight: 600;
        }

        .stock-tips {
          margin-left: 5px;
          color: #ffa202;
        }
      }

      .buy-count-panel {
        margin-top: 20px;

        .buy-count-label {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }

        .buy-count-input {
          margin-top: 5px;
        }
      }

      .buy-panel {
        margin-top: 10px;
        display: flex;
        color: #fff;
        border-radius: 10px;
        cursor: pointer;
        overflow: hidden;

        .btn-cart {
          background: #ffa101;
          padding: 15px 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .btn-buy {
          flex: 1;
          width: 0;
          background: #ff6d00;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}

.product-desc {
  width: 810px;

  .product-tabs {
    position: sticky;
    top: 60px;
    background: #fff;
    z-index: 2;

    :deep(.el-tabs__header) {
      margin: 0px;
    }
  }

  .tab-title {
    font-weight: 600;
    font-size: 20px;
    margin-top: 20px;
    scroll-margin-top: 110px;
    /* 距离顶部80px */
  }
}
</style>
