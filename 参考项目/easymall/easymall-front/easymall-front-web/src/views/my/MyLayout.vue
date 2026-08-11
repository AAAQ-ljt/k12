<template>
  <div class="my-layout">
    <div class="left-side">
      <div :class="['menu-item', { 'active': route.path == item.path }]" v-for="item in menus"
        @click="proxy.Utils.jump(`${item.path}`, !item.target)">{{ item.name }}</div>
    </div>
    <div class="body-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const menus = [
  {
    name: '我的商城',
    path: '/my/shop',
  },
  {
    name: '我的购物车',
    path: '/shopCart',
    target: '_blank',
  },
  {
    name: '我的订单',
    path: '/my/orders',
  },
  {
    name: '评价管理',
    path: '/my/comment',
  },
  {
    name: '收货地址',
    path: '/my/address',
  },
  {
    name: '个人资料',
    path: '/my/userInfo',
  },
]
</script>

<style lang="scss" scoped>
.my-layout {
  display: flex;
  align-items: flex-start;

  .left-side {
    width: 200px;

    .menu-item {
      padding: 10px;
      border-radius: 5px;
      cursor: pointer;
      margin: 5px 0px;

      &:hover {
        background: #f3f6f8;
      }
    }

    .active {
      background: #ffefe6;
      color: var(--pink);
    }
  }

  .body-content {
    margin-left: 10px;
    flex: 1;
    width: 0;
  }
}
</style>
