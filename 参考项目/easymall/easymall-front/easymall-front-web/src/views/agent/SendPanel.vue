<template>
  <div class="send-panel">
    <div class="tips-panel">
      <div class="tip-title">
        试试这样说
      </div>
      <el-tag v-for="tip in tips" class="tip-tag" type="success" @click="addTip(tip)">{{ tip }}</el-tag>
    </div>
    <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
      <!--input输入-->
      <el-form-item label="" prop="">
        <el-input clearable placeholder="请输入你的问题" type="textarea" v-model.trim="formData.message" resize="none"
          :maxlength="1000" :autosize="{ minRows: 3, maxRows: 5 }" @keyup.enter="sendMessage"></el-input>
      </el-form-item>
      <div class="op-panel">
        <div class="op-panel-right">
          <div class="iconfont icon-send" @click="sendMessage" v-if="!answering">
          </div>
          <div class="iconfont icon-stop" @click="stop" v-else></div>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { mitter } from '@/eventbus/eventBus'

const tips = ref([
  '有新颖的玩具',
  '我的订单',
  '我要申请退款',
  '我的订单到哪里了',
  '我要给我的订单一个好评',
])

const formData = ref({})
const formDataRef = ref()
const rules = {
  message: [{ required: true, message: '请输入内容' }],
}

const answering = ref(false)

const messageId = ref()

const sendMessage = async () => {
  let result = await proxy.Request({
    url: proxy.Api.sendMessage,
    showLoading: false,
    params: {
      message: formData.value.message,
    },
  })
  if (!result) {
    return
  }
  result.data.assistantMessage = ''
  formData.value.message = ''
  messageId.value = result.data.messageId

  answering.value = true
  mitter.emit('sendMessage', {
    ...result.data,
  })
}

const stop = async () => {
  let result = await proxy.Request({
    url: proxy.Api.cancelMessage,
    showLoading: true,
    params: {
      messageId: messageId.value,
    },
  })
  if (!result) {
    return
  }
  answering.value = false

  mitter.emit('cancelMessage', {
    messageId: messageId.value,
  })
}

const addTip = (tip) => {
  formData.value.message = tip
}

onMounted(() => {
  mitter.on('answering', (_answering) => {
    answering.value = _answering
  })
})

onUnmounted(() => {
  mitter.off('answering')
  mitter.off('cancelMessage');
})
</script>

<style lang="scss" scoped>
.send-panel {
  border-top: 1px solid #e0e0e0;
  padding: 10px;

  :deep(.el-textarea__inner) {
    border: none;
    box-shadow: none;
  }

  .tips-panel {
    display: flex;
    align-items: center;
    margin-bottom: 5px;

    .tip-title {
      padding-left: 10px;
      margin-right: 10px;
      color: var(--text2);
    }

    .tip-tag {
      margin-right: 5px;
      cursor: pointer;
    }
  }

  .op-panel {
    display: flex;
    align-items: center;
    justify-content: flex-end;

    .iconfont {
      cursor: pointer;
    }

    .icon-send {
      color: #fff;
      border-radius: 50%;
      padding: 5px;
      font-size: 20px;
      background: var(--green);
    }

    .icon-stop {
      font-size: 31px;
      color: red;
      border-radius: 50%;
    }
  }
}
</style>
