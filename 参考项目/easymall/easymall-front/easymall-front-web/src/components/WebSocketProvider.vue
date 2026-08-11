<!-- components/WebSocketProvider.vue -->
<template>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  watch,
} from 'vue'
import { useWebSocket } from '@vueuse/core'
import { useMessageStore } from '@/stores/messagestore'
const { proxy } = getCurrentInstance()

const messageStore = useMessageStore()
let wsUrl = import.meta.env.VITE_WS
let retryCount = 0
const closeRef = ref(null) // 用于存储关闭函数

// 获取 token 并连接 WebSocket
const initWebSocket = async () => {
  try {
    const result = await proxy.Request({
      url: proxy.Api.autoLogin,
    })
    if (!result) {
      proxy.Message.error('自动登录失败，无法建立 WebSocket 连接')
      return
    }

    localStorage.setItem('userInfo', JSON.stringify(result.data))
    const token = result.data.token

    const {
      send,
      open,
      close: wsClose,
      status,
    } = useWebSocket(wsUrl + token, {
      onMessage: (ws, { data }) => {
        console.log('websocket链接', data)

        // 处理返回数据事件等
      },
      onDisconnected: () => {
        console.log('websocket断开链接')
        //  handleReconnect() // 断开后，手动再次链接ws
      },
      // 设置自动重连的功能，最多重试 3 次，每次重试之间的延迟为 5 秒。如果重连失败，将显示错误消息。
      autoReconnect: {
        retries: 3,
        delay: 5000,
        onFailed() {
          console.log('websocket链接失败')
        },
      },
      // 配置心跳机制，每 10 秒发送一次 "ping" 消息，如果在 1 秒内没有收到响应，则认为连接失效。
      heartbeat: {
        message: 'ping',
        interval: 1000,
        pongTimeout: 1000,
      },
    })
  } catch (error) {
    console.error('初始化 WebSocket 失败:', error)
    proxy.Message.error('WebSocket 连接初始化失败')
  }
}
// 在组件挂载时初始化
onMounted(() => {
  initWebSocket()
})
</script>