<template>
  <div>
    <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadMyComment" layoutType="line"
      :gridCount="5" :showLoadAll="false">
      <template #default="{ data, index }">
        <CommentItem :data="data" @commentDel="commentDelHandler" @commentRe="commentReHandler" />
      </template>
    </DataLoadMoreList>
  </div>
  <CommentRe ref="commentReRef" @reload="loadMyComment(true)"></CommentRe>
</template>

<script setup>
import CommentItem from './CommentItem.vue'

import CommentRe from './CommentRe.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadMyComment = async (reload = false) => {
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
    url: proxy.Api.loadMyComment,
    showLoading: false,
    params: {
      pageNo,
    },
  })
  loading.value = false
  if (!result) {
    return
  }
  result.data.list.map((item) => {
    item.commentImages = item.commentImages ? item.commentImages.split(',') : []
    item.recommentImages = item.recommentImages
      ? item.recommentImages.split(',')
      : []
  })
  if (result.data.pageNo == 1) {
    dataSource.value = result.data
  } else {
    dataSource.value = {
      ...result.data,
      list: dataSource.value.list.concat(result.data.list),
    }
  }
}

const commentReRef = ref()
const commentReHandler = (orderId) => {
  commentReRef.value.show(orderId)
}

const commentDelHandler = (orderId) => {
  proxy.Confirm({
    message: '确定要删除评论吗?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delMyComment,
        params: {
          orderId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('删除成功')
      loadMyComment(true)
    },
  })
}
</script>

<style lang="scss" scoped></style>
