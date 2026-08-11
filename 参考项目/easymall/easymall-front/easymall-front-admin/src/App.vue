<template>
  <el-config-provider :locale="zhCn" :message="config">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const config = reactive({
  max: 1,
})
</script>

<style lang="scss" scoped>
</style>
