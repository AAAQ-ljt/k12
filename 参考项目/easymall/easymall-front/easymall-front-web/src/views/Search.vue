<template>
  <div class="search-panel">
    <div :class="['btn', sortField == 'composite' ? 'btn-active' : '']" @click="sortFieldHandler('composite')">综合</div>
    <div :class="['btn', sortField == 'sale' ? 'btn-active' : '']" @click="sortFieldHandler('sale')">销量</div>
    <el-dropdown>
      <div :class="['btn btn-price', sortField == 'price' ? 'btn-active' : '']">价格 <span
          class="iconfont icon-down"></span>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="sortTypeHandler('price', 'asc')">
            <div :class="[sortType == 'asc' ? 'sort-active' : '']">从低到高</div>
          </el-dropdown-item>
          <el-dropdown-item @click="sortTypeHandler('price', 'desc')">
            <div :class="[sortType == 'desc' ? 'sort-active' : '']">从高到低</div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <el-popover placement="bottom" :width="250" trigger="click" ref="popoverRef">
      <template #reference>
        <div :class="['btn btn-price', priceFrom || priceTo ? 'btn-active' : '']">价格区间 <span
            class="iconfont icon-down"></span>
        </div>
      </template>
      <div class="price-input-panel">
        <div class="input-panel">
          <!--input输入-->
          <el-input clearable placeholder="最低价" v-model="priceFrom" />
          <div class="line">-</div>
          <el-input clearable placeholder="最高价" v-model="priceTo" />
        </div>
        <div class="price-input-btn">
          <div class="btn-reset" @click="priceInputCancelHandler">重置</div>
          <div class="btn-ok" @click="priceInputOkHandler">确定</div>
        </div>
      </div>
    </el-popover>
  </div>
  <div v-if="!keyWords" class="no-keywords">
    <NoData msg="请输入你想购买的商品"></NoData>
  </div>
  <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadProduct" layoutType="grid" :gridCount="5"
    :showLoadAll="false" v-else :initData="false">
    <template #default="{ data, index }">
      <ProductGridItem :data="data"></ProductGridItem>
    </template>
  </DataLoadMoreList>
</template>

<script setup>
import ProductGridItem from '@/views/product/ProductGridItem.vue'
import { ref, reactive, getCurrentInstance, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

//关键字
const keyWords = ref()
//排序顺序
const sortType = ref() //asc 由低到高  desc 由高到底
//排序字段
const sortField = ref('composite') //sale:销量  price:价格

const priceFrom = ref()
const priceTo = ref()

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])

const sortFieldHandler = (field) => {
  sortField.value = field
  loadProduct(true)
}
const sortTypeHandler = (field, type) => {
  sortField.value = field
  sortType.value = type
  loadProduct(true)
}

const priceInputCancelHandler = () => {
  priceFrom.value = null
  priceTo.value = null
}

const popoverRef = ref()
const priceInputOkHandler = () => {
  if (priceFrom.value && !proxy.Verify.checkAmount(priceFrom.value)) {
    proxy.Message.warning('请输入正确的最低价')
    return
  }
  if (priceTo.value && !proxy.Verify.checkAmount(priceTo.value)) {
    proxy.Message.warning('请输入正确的最高价')
    return
  }
  if (
    priceFrom.value &&
    priceTo.value &&
    parseFloat(priceFrom.value) > parseFloat(priceTo.value)
  ) {
    proxy.Message.warning('最高价必须高于最低价')
    return
  }

  loadProduct(true)
  popoverRef.value.hide()
}

const loadProduct = async (reload = false) => {
  if (!keyWords.value) {
    return
  }
  if (reload) {
    dataSource.value.pageNo = 0
  }
  if (
    Object.keys(dataSource.value).length > 0 &&
    dataSource.value.pageNo == dataSource.value.pageTotal &&
    !reload
  ) {
    return
  }
  loading.value = true
  let pageNo = dataSource.value.pageNo || 0
  pageNo++
  let result = await proxy.Request({
    url: proxy.Api.search,
    showLoading: false,
    params: {
      pageNo,
      keyWords: keyWords.value,
      sortType: sortType.value,
      sortField: sortField.value,
      priceFrom: priceFrom.value,
      priceTo: priceTo.value,
    },
  })
  loading.value = false
  if (!result) {
    return
  }
  if (result.data.pageNo == 1) {
    dataList.value = result.data.list
  } else {
    dataList.value = dataList.value.concat(result.data.list)
  }
  result.data.list = dataList.value
  dataSource.value = result.data
}

watch(
  () => route.query.keyWords,
  (newVal, oldVal) => {
    if (!newVal) {
      return
    }
    keyWords.value = decodeURIComponent(newVal)
    loadProduct(true)
  },
  { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
.search-panel {
  margin: 10px 0px;
  display: flex;

  .btn {
    border: 1px solid #ddd;
    border-radius: 5px;
    cursor: pointer;
    margin-right: 10px;
    padding: 5px 10px;
    line-height: normal;
    color: var(--text);
  }

  .btn-price {
    display: flex;
    align-items: center;

    .icon-down {
      margin-left: 3px;
    }
  }
}

.btn-active {
  border-color: var(--pink) !important;
  color: var(--pink) !important;
}

.sort-active {
  color: var(--pink) !important;
}

.no-keywords {
  height: 400px;
}

.price-input-panel {
  .input-panel {
    display: flex;
    align-items: center;

    .line {
      margin: 0px 10px;
    }
  }

  .price-input-btn {
    display: flex;
    align-items: center;
    margin-top: 10px;

    .btn-reset {
      border: 1px solid #ddd;
      border-radius: 5px;
      cursor: pointer;
      margin-right: 10px;
      padding: 8px 10px;
      line-height: normal;
      color: var(--text);
    }

    .btn-ok {
      flex: 1;
      color: #fff;
      background: var(--pink);
      border-radius: 5px;
      padding: 8px 10px;
      text-align: center;
      cursor: pointer;
    }
  }
}
</style>
