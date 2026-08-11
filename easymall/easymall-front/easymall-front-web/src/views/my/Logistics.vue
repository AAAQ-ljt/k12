<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
    :showCancel="false" @close="dialogConfig.show = false">
    <div class="logistics-company">{{logistics.logisticsCompany}} {{logistics.logisticsNo}}</div>
    <el-timeline>
      <el-timeline-item :timestamp="item.recordTime" hide-timestamp placement="top"
        v-for="(item,index) in logistics.recordList">
        <div class="time-panel">
          <div class="status-name" v-if="index==0">{{logistics.logisticsStatusName}}</div>
          <div class="time">{{item.recordTime}}</div>
        </div>
        <div class="address">{{item.recordAddress}}</div>
      </el-timeline-item>
    </el-timeline>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

const dialogConfig = ref({
  show: false,
  title: '物流信息',
  buttons: [
    {
      type: 'primary',
      text: '关闭',
      click: (e) => {
        dialogConfig.value.show = false
      },
    },
  ],
})

const logistics = ref({})
const show = async (orderId) => {
  dialogConfig.value.show = true
  let result = await proxy.Request({
    url: proxy.Api.getLogistics,
    params: {
      orderId,
    },
  })
  if (!result) {
    return
  }
  logistics.value = result.data
}

defineExpose({
  show,
})
</script>

<style lang="scss" scoped>
.logistics-company {
  margin-bottom: 20px;
  padding-left: 35px;
  font-size: 16px;
  color: var(--text);
}
.time-panel {
  display: flex;
  align-items: center;
  font-size: 14px;
  .status-name {
    color: var(--pink);
    margin-right: 5px;
  }
}
.address {
  margin-top: 10px;
  font-size: 12px;
  color: var(--text2);
}
</style>
