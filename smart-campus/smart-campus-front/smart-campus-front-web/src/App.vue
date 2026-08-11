<template>
  <el-config-provider :locale="zhCn" :message="config">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { reactive } from 'vue'

const config = reactive({
  max: 1,
})
</script>