<template>
  <div class="chat-body">
    <div class="chat-header">
      <div class="ai-avatar iconfont icon-robot"></div>
      <div class="ai-title">AI智能购物助手</div>
    </div>
    <ChatList class="chat-list-panel"></ChatList>
    <SendPanel></SendPanel>
  </div>
</template>

<script setup>
import ChatList from './ChatList.vue'
import SendPanel from './SendPanel.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  computed,
  onMounted,
  watch,
  onUnmounted,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { initWs, closeWebSocket } from '@/utils/worker/websocket.manager.js'

onMounted(() => {
  initWs()
})

onUnmounted(() => {
  closeWebSocket()
})
</script>

<style lang="scss" scoped>
.chat-body {
  width: 800px;
  margin: 10px auto;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .chat-header {
    background: linear-gradient(135deg, #ff6b6b, #a777e3);
    color: white;
    padding: 10px;
    display: flex;
    align-items: center;

    .ai-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30px;
    }

    .ai-title {
      font-size: 18px;
      font-weight: 600;
      margin-left: 10px;
    }
  }

  .chat-list-panel {
    height: calc(100vh - 310px);
  }
}
</style>
