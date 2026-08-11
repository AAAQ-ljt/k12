<template>
  <el-dialog :show-close="showClose" :draggable="draggable" :model-value="show" :close-on-click-modal="false"
    :close-on-press-escape="closeOnPressEscape" :append-to-body="appendToBody" class="base-dialog" :top="`${top}px`"
    :width="width" @close="handleClose">
    <template #header>
      <div v-if="title" class="base-dialog__title">{{ title }}</div>
      <slot v-else name="header" />
    </template>

    <div class="base-dialog__body" :style="{ maxHeight: `${bodyMaxHeight}px`, padding: `${padding}px`}">
      <slot />
    </div>

    <template v-if="hasFooter">
      <div class="base-dialog__footer">
        <el-button v-if="showCancel" link @click="handleClose">取消</el-button>
        <el-button v-for="(btn, index) in buttons" :key="btn.key || btn.text || index" :type="btn.type || 'primary'"
          @click="btn.click">
          {{ btn.text }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  draggable: {
    type: Boolean,
    default: true,
  },
  title: {
    type: String,
    default: '',
  },
  show: {
    type: Boolean,
    default: false,
  },
  showClose: {
    type: Boolean,
    default: true,
  },
  showCancel: {
    type: Boolean,
    default: true,
  },
  closeOnPressEscape: {
    type: Boolean,
    default: true,
  },
  appendToBody: {
    type: Boolean,
    default: true,
  },
  top: {
    type: Number,
    default: 50,
  },
  width: {
    type: [String, Number],
    default: '30%',
  },
  buttons: {
    type: Array,
    default: () => [],
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
  const footerOffset = hasFooter.value ? 132 : 92
  return Math.max(viewportHeight.value - props.top - footerOffset, 120)
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
.base-dialog {
  padding: 0 !important;
  margin-bottom: 5px !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;

  .el-dialog__header {
    padding: 12px 14px;
    margin-right: 0;
    border-bottom: 1px solid #eef2f8;
  }

  .base-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: #24304a;
  }

  .base-dialog__body {
    min-height: 80px;
    overflow: auto;
    overflow-x: hidden;
  }

  .base-dialog__footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 12px 18px;
    border-top: 1px solid #eef2f8;
  }
}

.base-dialog .el-dialog,
.base-dialog .el-dialog__body,
.base-dialog .el-dialog__header,
.base-dialog .el-dialog__footer {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.dialog-fade-enter-active .el-dialog,
.dialog-fade-leave-active .el-dialog {
  animation-duration: 0.16s !important;
}
</style>
