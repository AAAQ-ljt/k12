<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="changeStatusHandler">
    <el-tab-pane :label="item.name" :name="item.code" v-for="item in statusList"></el-tab-pane>
  </el-tabs>
  <DataLoadMoreList :dataSource="dataSource" :loading="loading" @loadData="loadOrders" layoutType="line" :gridCount="5"
    :showLoadAll="false">
    <template #default="{ data, index }">
      <OrderItem :data="data" @reload="loadOrders(true)" @logistics="logisticsHandler" @comment="commentHandler"
        @commentRe="commentReHandler" @payOrder="payOrderHandler">
      </OrderItem>
    </template>
  </DataLoadMoreList>
  <Logistics ref="logisticsRef"></Logistics>
  <Comment ref="commentRef" @reload="loadOrders(true)"></Comment>
  <CommentRe ref="commentReRef" @reload="loadOrders(true)"></CommentRe>
  <OrderPay ref="orderPayRef" :showType="1" @reload="loadOrders(true)"></OrderPay>
</template>

<script setup>
import OrderPay from '@/views/product/OrderPay.vue'
import CommentRe from '../comment/CommentRe.vue'
import Comment from '../comment/Comment.vue'
import Logistics from '../Logistics.vue'
import OrderItem from './OrderItem.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const statusList = ref([
  {
    name: '所有订单',
    status: '',
    code: 'all',
  },
  {
    name: '待付款',
    status: '0',
    code: 'pendingPayment',
  },
  {
    name: '待发货',
    status: '1',
    code: 'pendingShipment',
  },
  {
    name: '待收货',
    status: '2',
    code: 'pendingReceipt',
  },
  {
    name: '待评价',
    status: '3',
    commentStatus: 0,
    code: 'pendingComment',
  },
])

const changeStatusHandler = async (e) => {
  router.push({
    query: {
      status: e.props.name,
    },
  })
  await nextTick()
  loadOrders(true)
}

const activeName = ref(route.query.status || 'all')

const dataSource = ref({})
const loading = ref(false)
const dataList = ref([])
const loadOrders = async (reload = false) => {
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

  const statusItem = statusList.value.find((item) => {
    return item.code == activeName.value
  })
  let result = await proxy.Request({
    url: proxy.Api.loadMyOrder,
    showLoading: false,
    params: {
      pageNo,
      status: statusItem.status,
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

const logisticsRef = ref()
const logisticsHandler = (orderId) => {
  logisticsRef.value.show(orderId)
}

const commentRef = ref()
const commentHandler = (orderId) => {
  commentRef.value.show(orderId)
}

const commentReRef = ref()
const commentReHandler = (orderId) => {
  commentReRef.value.show(orderId)
}

const orderPayRef = ref()
const payOrderHandler = async (orderId) => {
  let result = await proxy.Request({
    url: proxy.Api.getPayInfo,
    params: {
      orderId,
    }
  })
  if (!result) {
    return;
  }
  orderPayRef.value.show(result.data);
}
</script>

<style lang="scss" scoped></style>
