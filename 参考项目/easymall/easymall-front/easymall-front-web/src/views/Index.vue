<template>
  <div class="top-panel">
    <div class="category-panel">
      <div class="category-item" v-for="category in categoryStore.categoryList">
        <router-link class="category-name" :to="`/product/${category.categoryId}`">{{ category.categoryName
        }}</router-link>
        <div class="category-sub-list">
          <router-link class="category-sub-item" :to="`/product/${category.categoryId}/${sub.categoryId}`"
            v-for="sub in category.children">{{ sub.categoryName }}</router-link>
        </div>
      </div>
    </div>
    <div class="commend-product-panel">
      <div class="roll-image-panel">
        <el-carousel height="430px" @change="carouselChange" ref="elCarouselRef" indicator-position="none" arrow="never"
          v-if="rollImageList.length > 0">
          <el-carousel-item v-for="(item, index) in rollImageList" :key="index">
            <router-link class="product-name" :to="`/detail/${item.productId}`" target="_blank">
              <img :src="proxy.Api.sourcePath + rollImageCover(item)" class="roll-image">
            </router-link>
          </el-carousel-item>
        </el-carousel>
        <div class="carousel-bottom" v-if="rollImageList.length > 0">
          <div class="name-op">
            <router-link class="product-name" :to="`/detail/${rollImageList[carouselIndex].productId}`"
              target="_blank">{{ rollImageList[carouselIndex].productName }}</router-link>
            <div class="change-btn">
              <span class="iconfont icon-right" @click="preCarousel"></span>
              <span class="iconfont icon-left" @click="nextCarousel"></span>
            </div>
          </div>
          <div class="dtos">
            <div :class="['dto-item', carouselIndex == item - 1 ? 'active' : '']" v-for="item in rollImageList.length"
              @click="setCarousel(item)"></div>
          </div>
        </div>
      </div>
      <div class="commend-list">
        <ProductGridItem :data="data" v-for="data in commendList"></ProductGridItem>
      </div>
    </div>
  </div>
  <div class="hot-product">热门商品</div>
  <div class="product-list">
    <ProductList></ProductList>
  </div>
</template>

<script setup>
import ProductGridItem from '@/views/product/ProductGridItem.vue'
import ProductList from '@/views/product/ProductList.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  computed,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()
import { useCategoryStore } from '@/stores/categoryStore.js'
const categoryStore = useCategoryStore()

const rollImageCount = 5

const rollImageList = computed(() => {
  if (commendProductList.value.length > rollImageCount) {
    return commendProductList.value.slice(0, rollImageCount)
  } else {
    return commendProductList.value
  }
})

const carouselIndex = ref(0)
const carouselChange = (e) => {
  carouselIndex.value = e
}

const elCarouselRef = ref()
const preCarousel = () => {
  elCarouselRef.value.prev()
}

const nextCarousel = () => {
  elCarouselRef.value.next()
}

const setCarousel = (index) => {
  elCarouselRef.value.setActiveItem(index - 1 + '')
}

const commendList = computed(() => {
  if (commendProductList.value.length > rollImageCount) {
    return commendProductList.value.slice(5, 11)
  } else {
    return []
  }
})

const commendProductList = ref([])
const rollImageCover = (productInfo) => {
  const cover = productInfo.cover.split(',')[0]
  return cover.replace(proxy.imageThumbnailSuffix, '')
}

const loadCommendProductList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCommendProduct,
  })
  if (!result) {
    return
  }
  commendProductList.value = result.data
}
onMounted(() => {
  loadCommendProductList()
})
</script>

<style lang="scss" scoped>
.top-panel {
  display: flex;
  height: 430px;
  align-items: flex-start;

  .category-panel {
    &::-webkit-scrollbar {
      display: none;
    }

    max-height: 100%;
    width: 300px;
    overflow: auto;
    background: #f4f4f4;
    border-radius: 5px;
    padding: 10px 0px 10px 10px;
    color: var(--text);

    .category-item {
      margin-bottom: 10px;

      .category-name {
        font-size: 15px;
        text-decoration: none;
        color: var(--text);

        &:hover {
          color: var(--pink);
        }
      }

      .category-sub-list {
        display: flex;
        flex-wrap: wrap;

        .category-sub-item {
          margin-right: 5px;
          font-size: 12px;
          text-decoration: none;
          color: var(--text);

          &:hover {
            color: var(--pink);
          }
        }
      }
    }
  }

  .commend-product-panel {
    display: flex;
    flex: 1;

    .roll-image-panel {
      flex: 1;
      width: 0;
      margin: 0px 20px;
      height: 100%;
      width: 500px;
      overflow: hidden;
      position: relative;
      border-radius: 5px;

      .roll-image {
        object-fit: cover;
        width: 100%;
      }

      .carousel-bottom {
        position: absolute;
        bottom: 0px;
        width: 100%;
        height: 65px;
        background: rgba(0, 0, 0, 0.6);
        padding: 10px;

        .name-op {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .product-name {
            flex: 1;
            color: #ffff;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
          }

          .change-btn {
            margin-left: 10px;
            width: 60px;
            display: flex;
            justify-content: space-between;

            .iconfont {
              cursor: pointer;
              text-align: center;
              width: 25px;
              line-height: 25px;
              font-size: 20px;
              background-color: rgba(255, 255, 255, 0.1);
              border-radius: 5px;
              color: #fff;
            }
          }
        }

        .dtos {
          display: flex;
          margin-top: 5px;
          align-items: center;

          .dto-item {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #b0b0b0;
            cursor: pointer;
            margin-right: 10px;
          }

          .active {
            width: 15px;
            height: 15px;
            background: #fff;
          }
        }
      }
    }

    .commend-list {
      flex: 1;
      display: grid;
      grid-gap: 20px;
      grid-template-columns: repeat(3, 1fr);
    }
  }
}

.hot-product {
  position: relative;
  padding: 10px 0px;
  font-size: 20px;
  font-weight: 600;
  margin-top: 5px;

  &::before {
    content: '';
    background: var(--pink);
    position: absolute;
    width: 40px;
    height: 3px;
    bottom: -3px;
  }
}

.product-list {
  margin-top: 20px;
}
</style>
