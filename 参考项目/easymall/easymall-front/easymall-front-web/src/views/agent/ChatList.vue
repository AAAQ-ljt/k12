<template>
  <div class="message-list" @scroll="handleScroll" ref="scrollbarRef">
    <div v-if="
      dataSource.pageNo >= dataSource.pageTotal &&
      !loadingHistory &&
      dataSource.list.length > 0
    " class="reach-top">
      没有更多数据了
    </div>
    <div class="loading-data" v-if="loadingHistory">
      <img :src="proxy.Utils.getLocalResource('loading.gif')" />数据加载中....
    </div>
    <ChatItem v-for="item in dataSource.list" :data="item"></ChatItem>
    <img :src="proxy.Utils.getLocalResource('loading.gif')" v-if="loading" class="loading">
    <div class="no-data-tips" v-if="dataSource.list.length == 0">我是您的专属购物助手，有什么可以帮您？</div>
  </div>
</template>

<script setup>
import ChatItem from './ChatItem.vue'
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  watch,
} from 'vue'
import { useRouter, useRoute } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

import { mitter } from '@/eventbus/eventBus'
import { useMessageStore } from '@/stores/messageStore'
const messageStore = useMessageStore()

const currentMessage = ref({})
const answering = ref(false)
const scrollbarRef = ref()
const loading = ref(false)

watch(
  () => messageStore.message,
  (newVal, oldVal) => {
    if (!newVal) {
      return
    }
    loading.value = false
    if (newVal.outPutType === 1) {
      answering.value = false
      mitter.emit('answering', false)
      if (newVal.bizType) {
        const lastMessage = {
          ...currentMessage.value,
          ...newVal,
        }
        dataSource.value.list[dataSource.value.list.length - 1] = lastMessage
      }
    } else if (newVal.outPutType === 2) {
      answering.value = false
      mitter.emit('answering', false)
      currentMessage.value.assistantMessage = '服务器返回错误，请联系管理员'
    } else {
      const message =
        currentMessage.value.assistantMessage + newVal.assistantMessage
      currentMessage.value.assistantMessage = message
    }
    goBottom()
  },
  { immediate: true, deep: true }
)

const goBottom = async (forceBottom = false) => {
  await nextTick()
  const distanceToBottom =
    scrollbarRef.value.scrollHeight -
    (scrollbarRef.value.scrollTop + scrollbarRef.value.clientHeight)
  if (!forceBottom && distanceToBottom > 200) {
    return
  }
  scrollbarRef.value.scrollTo({
    top: scrollbarRef.value.scrollHeight,
  })
}

const onSendMessage = (message) => {
  currentMessage.value = { ...message }
  dataSource.value.list.push(currentMessage.value)
  goBottom(true)
  answering.value = true
  loading.value = true
}

const onCancelMessage = () => {
  answering.value = false
  loading.value = false
  currentMessage.value.status = 0
}

//获取历史数据
//滚动到顶部
const gotoTop = async () => {
  await nextTick()
  const newScrollHeight = scrollbarRef.value.scrollHeight
  scrollbarRef.value.scrollTop = newScrollHeight - oldScrollHeight - 40
}

let oldScrollHeight = 0
const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (loadingHistory.value) {
    return
  }
  if (scrollTop == 0) {
    oldScrollHeight = scrollHeight
    loadHistoryMessage()
    return
  }
}

const loadingHistory = ref(false)
const dataSource = ref({ list: [] })
let maxMessageId = null
const loadHistoryMessage = async () => {
  let pageNo = dataSource.value.pageNo || 0
  pageNo++
  if (pageNo > dataSource.value.pageTotal) {
    return
  }
  loadingHistory.value = true
  let result = await proxy.Request({
    url: proxy.Api.loadHistoryMessage,
    params: {
      pageNo,
      maxMessageId,
    },
    showLoading: false,
  })
  loadingHistory.value = false
  if (!result) {
    return
  }
  //记录第一页的最大id
  if (result.data.pageNo == 1 && result.data.list.length > 0) {
    maxMessageId = result.data.list[0].messageId
  }
  let list = dataSource.value.list || []
  //数据库倒序取数据，页面顺序排列展示
  result.data.list.sort((a, b) => a.messageId - b.messageId)
  //向最前面追加数据
  list.unshift(...result.data.list)
  //最终的list赋值给data.list
  result.data.list = list
  //将最终的数据赋值给dataSource
  dataSource.value = result.data
  dataSource.value.list = [...result.data.list]
  const lastMessage = dataSource.value.list[dataSource.value.list.length - 1]
  if (lastMessage?.status == 1) {
    currentMessage.value =
      dataSource.value.list[dataSource.value.list.length - 1]
  }
  if (dataSource.value.pageNo > 1) {
    gotoTop()
  } else {
    goBottom(true)
  }
}

onMounted(() => {
  loadHistoryMessage()
  mitter.on('sendMessage', onSendMessage)
  mitter.on('cancelMessage', onCancelMessage)
})

onUnmounted(() => {
  mitter.off('sendMessage')
  mitter.off('cancelMessage')
})
</script>

<style lang="scss" scoped>
.message-list {
  height: 100%;
  padding: 10px 15px;
  overflow: auto;

  &::-webkit-scrollbar {
    width: 4px;
    height: 4px;
  }

  .reach-top {
    text-align: center;
    height: 30px;
    color: var(--text3);
  }

  .loading-data {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text);
    font-size: 13px;

    img {
      width: 16px;
      margin-right: 5px;
    }
  }

  .loading {
    width: 16px;
    height: 16px;
  }
}

.no-data-tips {
  text-align: center;
  color: var(--text2);
}
</style>
