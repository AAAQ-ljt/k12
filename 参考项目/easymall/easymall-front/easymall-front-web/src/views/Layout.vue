<template>
  <IndexHeader></IndexHeader>
  <div class="content-body" :style="{ width: `${proxy.bodyWidth}px` }">
    <router-view v-if="haveInit&&showRouterView"></router-view>
  </div>
  <Footer v-if="route.meta.showFooter == null || route.meta.showFooter"></Footer>
</template>

<script setup>
import Footer from '@/views/footer/Footer.vue'
import IndexHeader from '@/views/header/IndexHeader.vue'
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

import { useLoginStore } from '@/stores/loginStore.js'
const loginStore = useLoginStore()

const needLogin = computed(() => {
  const needLogin =
    route.meta.checkLogin != null &&
    route.meta.checkLogin &&
    loginStore.userInfo &&
    Object.keys(loginStore.userInfo).length == 0

  if (needLogin) {
    loginStore.showLogin = true
  }
  return needLogin
})

const showRouterView = computed(() => {
  return route.meta.checkLogin == null || !needLogin.value
})

const haveInit = ref(false)
const autoLogin = async () => {
  if (!localStorage.getItem('token')) {
    haveInit.value = true
    return
  }
  let result = await proxy.Request({
    url: proxy.Api.autoLogin,
  })
  if (!result) {
    return
  }
  haveInit.value = true
  if (result.data == null) {
    return
  }
  localStorage.setItem('token', result.data?.token)
  loginStore.saveUserInfo(result.data)
}

onMounted(() => {
  autoLogin()
})
</script>

<style lang="scss" scoped>
.content-body {
  margin: 0px auto;
  margin-top: 70px;
  min-height: calc(100vh - 290px);
}
</style>
