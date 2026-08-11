<template>
  <el-drawer :size="width" :model-value="show" :with-header="true" :close-on-click-modal="false"
    :append-to-body="appendToBody" :direction="direction" header-class="base-drawer__header"
    body-class="base-drawer__body" footer-class="base-drawer__footer" :style="{ background }" @close="handleClose">
    <template #header>
      <div class="base-drawer__title">{{ title }}</div>
    </template>

    <div class="base-drawer__body-content" :style="{ maxHeight: `${bodyMaxHeight}px`, padding: `${padding}px` }">
      <slot />
    </div>

    <template v-if="hasFooter" #footer>
      <el-button v-if="showCancel" link @click="handleClose">取消</el-button>
      <el-button v-for="(btn, index) in buttons" :key="btn.key || btn.text || index" :type="btn.type || 'primary'"
        @click="btn.click">
        {{ btn.text }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
  show: {
    type: Boolean,
    default: false,
  },
  showCancel: {
    type: Boolean,
    default: true,
  },
  width: {
    type: [String, Number],
    default: '50%',
  },
  buttons: {
    type: Array,
    default: () => [],
  },
  background: {
    type: String,
    default: '#fff',
  },
  direction: {
    type: String,
    default: 'rtl',
  },
  appendToBody: {
    type: Boolean,
    default: true,
  },
  padding: {
    type: Number,
    default: 15,
  },
})

const emit = defineEmits(['close', 'update:show'])
const viewportHeight = ref(
  typeof window === 'undefined' ? 900 : window.innerHeight
)
const hasFooter = computed(() => props.showCancel || props.buttons.length > 0)
const bodyMaxHeight = computed(() => {
  const footerOffset = hasFooter.value ? 57 : 0
  return viewportHeight.value - footerOffset - 47
})

const syncViewportHeight = () => {
  if (typeof window === 'undefined') {
    return
  }

  viewportHeight.value = window.innerHeight
}

const handleClose = () => {
  emit('update:show', false)
  emit('close')
}

onMounted(() => {
  syncViewportHeight()
  window.addEventListener('resize', syncViewportHeight)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportHeight)
})
</script>

<style lang="scss">
.base-drawer__header {
  margin-bottom: 0;
  padding: 12px 14px;
  border-bottom: 1px solid #eef2f8;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.base-drawer__title {
  font-size: 16px;
  font-weight: 600;
  color: #24304a;
}

.base-drawer__body {
  min-height: 0;
  padding: 0px;
}

.base-drawer__body-content {
  min-height: 80px;
  overflow: auto;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.base-drawer__footer {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eef2f8;
}

.el-drawer,
.el-drawer__body,
.el-drawer__header,
.el-drawer__footer {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}
</style>
