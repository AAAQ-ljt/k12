<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="440px"
    :showCancel="false" @close="dialogConfig.show = false">
    <el-button @click="buttonClick(1)" type="primary">同步统计数据</el-button>
    <el-button @click="buttonClick(2)" type="primary">同步商品数据</el-button>
    <el-button @click="buttonClick(3)" type="primary">同步RAG数据</el-button>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
const dialogConfig = ref({
  show: false,
  title: "小工具",
});

const show = () => {
  dialogConfig.value.show = true;
}
defineExpose({
  show
})

const API_MAP = {
  1: "/tool/statistics",
  2: "/tool/productData",
  3: "/tool/ragData",
}
const buttonClick = async (type) => {
  let result = await proxy.Request({
    url: API_MAP[type],
  })
  if (!result) {
    return;
  }
  proxy.Message.success("操作成功");
};
</script>

<style lang="scss" scoped></style>
