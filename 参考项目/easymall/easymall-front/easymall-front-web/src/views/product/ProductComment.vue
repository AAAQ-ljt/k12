<template>
  <div class="comment-title">用户评价</div>
  <div class="comment-list">
    <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadComments" layoutType="line"
      :gridCount="5" :showLoadAll="false">
      <template #default="{ data, index }">
        <ProductCommentItem :comment="data">
        </ProductCommentItem>
      </template>
    </DataLoadMoreList>
  </div>
</template>

<script setup>
import ProductCommentItem from './ProductCommentItem.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadComments = async (reload = false) => {
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
    url: proxy.Api.loadComment,
    showLoading: false,
    params: {
      pageNo,
      productId: route.params.productId,
    },
  })

  loading.value = false
  if (!result) {
    return
  }
  if (result.data.pageNo == 1) {
    dataSource.value = result.data
  } else {
    dataSource.value = {
      ...result.data,
      list: dataSource.value.list.concat(result.data.list),
    }
  }
}
</script>

<style lang="scss" scoped>
.comment-title {
  margin-top: 20px;
  font-size: 20px;
  font-weight: 600;
}
</style>
