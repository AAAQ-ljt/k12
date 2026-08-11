<template>
  <div class="load-more-data-list">
    <div :class="[layoutType == 'grid' ? 'data-list-grad' : '']" :style="{
      'grid-template-columns': layoutType == 'grid' ? `repeat(${gridCount}, 1fr)` : ''
    }">
      <template v-for="(item, index) in dataSource.list">
        <slot :data="item" :index="index"></slot>
      </template>
    </div>
    <div class="bottom-loading" v-if="loading">
      <img :src="proxy.Utils.getLocalResource('loading.gif')" />数据加载中....
    </div>
    <div v-if="
      dataSource.pageNo >= dataSource.pageTotal &&
      !loading &&
      dataSource.list.length > 0 &&
      showLoadAll
    " class="reach-bottom">
      没有更多数据了
    </div>
    <NoData v-if="dataSource.list && dataSource.list.length == 0 && !loading" msg="暂无数据">
    </NoData>
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
  watch,
} from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

const props = defineProps({
  dataSource: {
    type: Object,
  },
  loading: {
    type: Boolean,
  },
  layoutType: {
    type: String,
    default: 'line',
  },
  gridCount: {
    type: Number,
    default: 3,
  },
  showLoadAll: {
    type: Boolean,
    default: true,
  },
  // 加载阈值（距离底部多少像素触发）
  loadThreshold: {
    type: Number,
    default: 200,
  },
  initData: {
    type: Boolean,
    default: true,
  },
})

const isReachBottom = ref(false)
const emit = defineEmits(['loadData'])
const handleScroll = () => {
  if (props.loading) {
    return
  }
  const { scrollHeight, scrollTop, clientHeight } = document.documentElement
  if (scrollHeight - (scrollTop + clientHeight) < props.loadThreshold) {
    if (
      !isReachBottom.value &&
      props.dataSource.pageNo < props.dataSource.pageTotal
    ) {
      isReachBottom.value = true
      emit('loadData')
    }
  } else {
    isReachBottom.value = false
  }
}

defineExpose({
  handleScroll,
})

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  if (!props.initData) {
    return
  }
  emit('loadData')
})
</script>

<style lang="scss" scoped>
.load-more-data-list {
  width: 100%;

  .reach-bottom {
    text-align: center;
    line-height: 40px;
    color: var(--text);
    font-size: 12px;
  }

  .bottom-loading {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text);
    font-size: 13px;

    img {
      width: 20px;
      margin-right: 10px;
    }
  }

  .data-list-grad {
    display: grid;
    grid-gap: 20px;
    padding-bottom: 10px;
  }
}
</style>
