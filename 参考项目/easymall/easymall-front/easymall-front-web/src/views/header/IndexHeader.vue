<template>
  <div class="header">
    <div class="header-content" :style="{ width: `${proxy.bodyWidth}px` }">
      <router-link class="logo" to="/">
        <img src="../../assets/logo.png" />
      </router-link>
      <router-link class="logo-name" to="/">EasyMall智慧商城</router-link>
      <div class="search">
        <input placeholder="搜索商品" v-model="keyWords" @keyup.enter="search" />
        <div class="search-btn iconfont icon-search" @click="search"></div>
      </div>
      <div class="right-panel">
        <div :class="['mode-change', route.path == '/agent' ? 'active-agent' : '']" @click="proxy.Utils.jump(`/agent`)">
          <span class="iconfont icon-robot"></span>AI智能购物
        </div>
        <div :class="['shop-cart iconfont icon-cart', route.path == '/shopCart' ? 'active' : '']" @click="goShopCart">
        </div>
        <div class="login-btn" v-if="Object.keys(loginStore.userInfo).length == 0" @click="loginStore.showLogin = true">
          登录/注册
        </div>
        <el-dropdown>
          <Avatar v-if="Object.keys(loginStore.userInfo).length != 0" :avatar="loginStore.userInfo.avatar || undefined"
            :width="50">
          </Avatar>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="proxy.Utils.jump(`/my`)">个人中心</el-dropdown-item>
              <el-dropdown-item @click="updatePassword">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="header-category" v-if="route.meta.showCategory">
      <div class="header-category-content" :style="{ width: `${proxy.bodyWidth}px` }">
        <el-menu ellipsis mode="horizontal" :popper-offset="16" class="custom-menu">
          <el-sub-menu :index="category.categoryId" v-for="category in categoryStore.categoryList"
            :class="[category.categoryId == route.params.pCategoryId ? 'is-active' : '']">
            <template #title>
              {{ category.categoryName }}
            </template>
            <el-menu-item :index="category.categoryId"
              :class="[category.categoryId == route.params.pCategoryId && !route.params.categoryId ? 'is-active' : '']"
              @click="proxy.Utils.jump(`/product/${category.categoryId}`)">全部商品</el-menu-item>
            <el-menu-item :index="sub.categoryId" v-for="sub in category.children"
              @click="proxy.Utils.jump(`/product/${sub.pCategoryId}/${sub.categoryId}`)"
              :class="[sub.categoryId == route.params.categoryId ? 'is-active' : '']">{{ sub.categoryName
              }}</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
    </div>
  </div>
  <Account />
  <UpdatePassword ref="updatePasswordRef" />
</template>
<script setup>
import Avatar from '@/components/Avatar.vue'
import Account from '@/views/account/Account.vue'
import UpdatePassword from '@/views/account/UpdatePassword.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  computed,
  watch,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { useCategoryStore } from '@/stores/categoryStore.js'
const categoryStore = useCategoryStore()

import { useLoginStore } from '@/stores/loginStore.js'
const loginStore = useLoginStore()

const updatePasswordRef = ref()
const updatePassword = () => {
  updatePasswordRef.value.show()
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
      if (route.meta.checkLogin) {
        router.push('/')
      }
      setTimeout(() => {
        loginStore.saveUserInfo({})
        localStorage.removeItem('token')
      }, 100)
    },
  })
}

const keyWords = ref()
const search = () => {
  router.push({
    path: '/search',
    query: {
      keyWords: encodeURIComponent(keyWords.value),
    },
  })
}
const goShopCart = () => {
  if (Object.keys(loginStore.userInfo).length == 0) {
    loginStore.showLogin = true;
    return;
  }
  router.push('/shopCart');
}

watch(
  () => route.query.keyWords,
  (newVal, oldVal) => {
    keyWords.value = decodeURIComponent(newVal || '')
  },
  { immediate: true, deep: true }
)
</script>
<style lang="scss" scoped>
.header {
  top: 0;
  width: 100%;
  position: fixed;
  box-shadow: 0 5px 5px -5px #00000080;
  z-index: 20;
  background: #fff;

  .header-content {
    margin: 0 auto;
    align-items: center;
    justify-content: left;
    height: 60px;
    display: flex;
    background: #fff;

    .logo {
      width: 30px;
      cursor: pointer;

      img {
        max-width: 100%;
      }
    }

    .logo-name {
      font-weight: bold;
      font-size: 16px;
      margin-left: 10px;
      cursor: pointer;
      text-decoration: none;
      color: var(--text);
    }

    .search {
      width: 400px;
      margin: 0px auto;
      position: relative;

      input {
        border: none;
        outline: none;
        border: 1px solid #ddd;
        border-radius: 5px;
        width: 100%;
        padding: 5px 40px 5px 10px;
        height: 40px;
        padding-right: 40px;

        &:focus {
          border: 2px solid var(--pink);
        }
      }

      .search-btn {
        cursor: pointer;
        background: var(--pink);
        border-radius: 25%;
        padding: 8px;
        position: absolute;
        top: 4px;
        right: 5px;
      }

      .iconfont {
        color: #fff;
      }
    }

    .right-panel {
      display: flex;
      align-items: center;

      .mode-change {
        font-size: 14px;
        background: #dfdfdf;
        padding: 5px 10px;
        border-radius: 20px;
        cursor: pointer;

        .icon-robot {
          margin-right: 5px;
        }
      }

      .icon-cart {
        margin: 0px 20px;
        cursor: pointer;
      }

      .user-info {
        cursor: pointer;
      }

      .active-agent {
        background: var(--pink);
        color: #fff;
      }

      .active {
        color: var(--pink);
      }

      .login-btn {
        cursor: pointer;
        color: var(--pink);
      }
    }
  }

  .header-category {
    border-top: 1px solid #ddd;
    height: 60px;

    .header-category-content {
      margin: 0px auto;
    }
  }
}
</style>
