<template>
  <div class="user-panel">
    <Avatar :avatar="loginStore.userInfo.avatar|| undefined" :width="120"></Avatar>
    <div class="name-panel">
      <div class="nick-name">
        {{loginStore.userInfo.nickName}}
        <span class="iconfont icon-edit" @click="proxy.Utils.jump('/my/userInfo')"></span>
      </div>
      <div class="address" @click="proxy.Utils.jump('/my/address')">收货地址</div>
    </div>
  </div>
  <div class="order-panel">
    <div class="my-order">我的订单</div>
    <div class="order-item-list">
      <div class="order-item" v-for="item in orderItemList" @click="proxy.Utils.jump(`/my/orders?status=${item.code}`)">
        <div class="count">{{countMap.get(item.code)?.count||0}}</div>
        <div class="name">{{item.name}}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { useLoginStore } from '@/stores/loginStore.js'
const loginStore = useLoginStore()

const orderItemList = ref([
  {
    name: '待付款',
    code: 'pendingPayment',
  },
  {
    name: '待发货',
    code: 'pendingShipment',
  },
  {
    name: '待收货',
    code: 'pendingReceipt',
  },
  {
    name: '待评价',
    code: 'pendingComment',
  },
])

const countMap = ref(new Map())
const getOrderCountInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getOrderCountInfo,
  })
  if (!result) {
    return
  }
  countMap.value = new Map(result.data.map((item) => [item.code, item]))
}

onMounted(() => {
  getOrderCountInfo()
})
</script>

<style lang="scss" scoped>
.user-panel {
  display: flex;
  background: #fff4ed;
  padding: 10px 20px;
  align-items: center;
  border-radius: 10px;
  .name-panel {
    margin-left: 10px;
    .nick-name {
      font-weight: 600;
      font-size: 20px;
      .iconfont {
        color: var(--text2);
        margin-left: 5px;
        cursor: pointer;
      }
    }
    .address {
      margin-top: 5px;
      color: var(--text2);
      cursor: pointer;
    }
  }
}
.order-panel {
  background: #f6f6f7;
  padding: 10px;
  margin-top: 20px;
  border-radius: 10px;
  .my-order {
    font-size: 20px;
    cursor: pointer;
    &:hover {
      color: var(--pink);
    }
  }
  .order-item-list {
    display: grid;
    grid-gap: 20px;
    grid-template-columns: repeat(4, 1fr);
    margin-top: 20px;
    .order-item {
      width: 200px;
      text-align: center;
      cursor: pointer;
      color: var(--text);
      &:hover {
        color: var(--pink);
      }
      .count {
        font-weight: 600;
        font-size: 16px;
      }
      .name {
        margin-top: 5px;
        font-size: 18px;
      }
    }
  }
}
</style>
