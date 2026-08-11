<template>
  <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadProduct" layoutType="grid" :gridCount="5"
    :showLoadAll="false">
    <template #default="{ data, index }">
      <ProductGridItem :data="data"></ProductGridItem>
    </template>
  </DataLoadMoreList>
</template>

<script setup>
import ProductGridItem from './ProductGridItem.vue'
import { ref, reactive, getCurrentInstance, nextTick, watch } from 'vue'
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
    url: proxy.Api.loadProduct,
    showLoading: false,
    params: {
      categoryId: route.params.categoryId || route.params.pCategoryId,
      pageNo,
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
  () => route.params,
  (newVal, oldVal) => {
    if (!newVal || Object.keys(newVal).length == 0) {
      return;
    }
    loadProduct(true)
  },
  { immediate: false, deep: true }
)
</script>

<style lang="scss" scoped></style>
