<template>
  <div class="layout">
    <div class="left-side">
      <div class="left-side-content">
        <div class="logo">EasyMall智能购物商城</div>
        <template v-for="item in menuList">
          <div :class="['menu-item', route.path == item.path ? 'active' : '']" @click="jump(item)">
            <div :class="['iconfont', `icon-${item.icon}`]"></div>
            <div class="menu-name">{{ item.name }}</div>
            <div
              :class="['iconfont', 'icon-right', 'icon-down', item.opened ? 'icon-right-opened' : 'icon-right-closed']"
              v-if="item.children">
            </div>
          </div>
          <div v-if="item.children" :class="['submenu-container', item.opened ? 'submenu-opened' : 'submenu-closed']">
            <div :class="['submenu-item', route.path == sub.path ? 'active' : '']" v-for="sub in item.children"
              @click="jump(sub)">
              {{ sub.name }}
            </div>
          </div>
        </template>
      </div>
      <div class="bg"></div>
    </div>
    <div class="right">
      <div class="top">
        <div class="breadcrumb">
          <el-breadcrumb>
            <el-breadcrumb-item v-for="item in route.meta.itemList">{{ item }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          欢迎，管理员 <span class="logout" @click="logout">退出</span>
          <div class="tool" @click="tool">小工具</div>
        </div>
      </div>
      <div class="right-body" :style="{ background: route.path == '/home' ? '#f8fafc' : '#fff' }">
        <router-view></router-view>
      </div>
    </div>
  </div>
  <Tool ref="toolRef"></Tool>
</template>

<script setup>
import Tool from './Tool.vue'
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const menuList = ref([
  {
    name: '首页',
    icon: 'home',
    path: '/home',
  },
  {
    name: '商品',
    icon: 'product',
    opened: true,
    children: [
      {
        name: '分类管理',
        path: '/product/category',
      },
      {
        name: '商品属性',
        path: '/product/productProperty',
      },
      {
        name: '商品管理',
        path: '/product',
      },
    ],
  },
  {
    name: '订单',
    icon: 'order',
    opened: true,
    children: [
      {
        name: '订单管理',
        path: '/order/orderList',
      },
      {
        name: '订单评论',
        path: '/order/comment',
      },
    ],
  },
  {
    name: '用户管理',
    icon: 'user',
    opened: true,
    children: [
      {
        name: '用户列表',
        path: '/user/userList',
      },
    ],
  },
  {
    name: '系统设置',
    icon: 'setting',
    opened: true,
    children: [
      {
        name: '发货信息管理',
        path: '/setting/logistics',
      },
      {
        name: '提示词管理',
        path: '/setting/prompt',
      },
      {
        name: 'RAG知识库',
        path: '/setting/rag',
      },
    ],
  },
])

const jump = (item) => {
  if (item.children) {
    item.opened = !item.opened
    return
  }
  router.push(item.path)
}

const logout = () => {
  proxy.Confirm({
    message: '确定要退出吗?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.logout,
      })
      if (!result) {
        return
      }
      router.push('/login')
    },
  })
}

const toolRef = ref()
const tool = () => {
  toolRef.value.show()
}
</script>

<style lang="scss" scoped>
.layout {
  display: flex;

  .left-side {
    position: relative;
    border-radius: 0px 10px 10px 0px;
    width: 220px;
    height: calc(100vh);
    overflow: auto;
    background: #030c3b;

    .left-side-content {
      position: absolute;
      top: 0px;
      left: 0px;
      bottom: 0px;
      z-index: 1;
      width: 100%;

      .logo {
        color: #fff;
        padding: 20px 10px;
        font-weight: bold;
        font-size: 18px;
      }

      .active {
        background: #05125c;
        color: #fff !important;
      }

      .menu-item {
        display: flex;
        align-items: center;
        color: rgba(255, 255, 255, 0.65);
        height: 50px;
        font-size: 14px;
        cursor: pointer;
        padding: 0px 15px;

        &:hover {
          background: #05125c;
        }

        .menu-name {
          margin-left: 5px;
          flex: 1;
          width: 0;
        }

        .icon-right {
          font-size: 12px;
          transition: all 0.3s;
        }

        .icon-right-opened {
          transform: rotate(180deg);
        }

        .icon-right-closed {
          transform: rotate(0deg);
        }
      }

      .submenu-container {
        transition: all 0.3s ease;

        .submenu-item {
          display: flex;
          align-items: center;
          color: rgba(255, 255, 255, 0.6);
          height: 40px;
          font-size: 13px;
          cursor: pointer;
          padding: 0px 15px 0px 38px;
          transition: all 0.3s;

          &:hover {
            background: #05125c;
          }
        }
      }

      .submenu-opened {
        max-height: 500px;
        opacity: 1;
      }

      .submenu-closed {
        max-height: 0;
        opacity: 0;
      }
    }

    .bg {
      position: absolute;
      left: 0px;
      bottom: 0px;
      background-size: cover;
      background-position: left bottom;
      background-repeat: no-repeat;
      height: 432px;
      width: 100%;
      background-image: url('../assets/left-side-bg.png');
    }
  }

  .right {
    flex: 1;
    width: 0;
    height: calc(100vh);
    overflow: auto;
    background: #f5f7f9;

    .top {
      padding-left: 10px;
      background: #f5f7f9;
      display: flex;
      align-items: center;

      .breadcrumb {
        flex: 1;

        :deep(.el-breadcrumb) {
          line-height: 40px;
        }
      }

      .user-info {
        color: var(--text2);
        padding: 0px 20px;
        display: flex;
        align-items: center;

        .logout {
          color: var(--link2);
          cursor: pointer;
          margin-left: 5px;
        }

        .tool {
          margin-left: 5px;
          color: var(--pink);
          cursor: pointer;
        }
      }
    }

    .right-body {
      margin: 0px 0px 10px 10px;
      padding: 10px 10px 0px 10px;
      border-radius: 10px;
      background: #fff;
      height: calc(100% - 50px);
      overflow: auto;
    }
  }
}
</style>
