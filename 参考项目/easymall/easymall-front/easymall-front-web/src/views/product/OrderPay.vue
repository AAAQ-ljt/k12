<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
    :showCancel="false" @close="dialogConfig.show = false">
    <div class="pay-success" v-if="payResult.orderStatus === 1 || payResult.orderStatus === 2">
      <div class="iconfont icon-success">支付成功</div>
      <el-button type="success" class="check-btn" @click="showOrder">查看订单</el-button>
    </div>
    <template v-else>
      <div class="pay-error" v-if="payResult.orderStatus === 0">
        <div class="iconfont icon-error">订单未支付</div>
      </div>
      <div class="pay-check">
        <el-button @click="checkPay" type="primary">我已支付</el-button>
      </div>
    </template>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()

const props = defineProps({
  showType: {
    type: Number,
    default: 0,
  },
})

const dialogConfig = ref({
  show: false,
  title: '支付确认',
})

const payInfo = ref({})
const show = async (data) => {
  const newWindow = window.open('', '_blank')
  newWindow.document.write(data.payInfo)
  newWindow.document.close() // 重要：完成文档写入
  dialogConfig.value.show = true
  await nextTick()
  payInfo.value = data
  await nextTick()
  const form = document.querySelector('form[name="punchout_form"]')
  if (form) {
    form.submit()
  }
}

const payResult = ref({})
const checkPay = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getOrderInfo,
    params: {
      payOrderId: payInfo.value.payOrderId,
    },
  })
  if (!result) {
    return
  }
  payResult.value = result.data
}

const emit = defineEmits(['reload'])
const showOrder = () => {
  if (props.showType == 0) {
    proxy.Utils.jump('/my/orders')
  } else {
    emit("reload")
    dialogConfig.value.show = false
  }
}

defineExpose({
  show,
})
</script>

<style lang="scss" scoped>
.pay-check {
  text-align: center;
}

.pay-success {
  text-align: center;

  .icon-success {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    &::before {
      color: green;
      margin-right: 5px;
      font-size: 30px;
    }
  }

  .check-btn {
    margin: 10px 0px;
    padding: 15px 30px;
  }
}

.pay-error {
  text-align: center;

  .icon-error {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    &::before {
      color: red;
      margin-right: 5px;
      font-size: 30px;
    }
  }
}
</style>
