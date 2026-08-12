<template>
  <el-tooltip effect="dark" :content="tips" placement="top">
    <div class="btn-panel" @click="handleClick">
      <div :class="['iconfont', icon]" :style="{ color: TYPE_MAP[type] }"></div>
    </div>
  </el-tooltip>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

const props = defineProps({
  icon: {
    type: String,
  },
  type: {
    type: String,
    default: "primary"
  },
  tips: {
    type: String,
  },
  fun: {
    type: [String, Function]
  }
})

const TYPE_MAP = {
  primary: '#409EFF',
  success: '#67C23A',
  warning: '#E6A23C',
  danger: '#F56C6C',
  info: '#909399'
}
const emit = defineEmits(['click'])
//el-tooltip 会阻止事件冒泡
const handleClick = (event) => {
  emit('click', event)
}

</script>

<style lang="scss" scoped>
.btn-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #E7F5FF;
  width: 30px;
  height: 30px;
  border-radius: 5px;
}
</style>
