<template>
  <el-image-viewer v-if="show && isImage && previewUrl" :url-list="[previewUrl]" :initial-index="0" infinite
    close-on-press-escape @close="emit('update:show', false)" />

  <BaseDialog v-if="show && !isImage" :show="show" :title="resource?.resourceName || '资源预览'" width="1300px"
    :show-cancel="false" :buttons="[]" :padding="0" @close="handleClosed" @update:show="emit('update:show', $event)">
    <div ref="playerRef" class="preview-player__canvas" v-if="isVideo" />
    <div v-else class="preview-fallback">
      <el-empty description="当前资源暂不支持弹窗预览" />
      <el-button v-if="previewUrl" type="primary" @click="openInNewTab">新窗口打开</el-button>
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import Artplayer from 'artplayer'
import Hls from 'hls.js'
import BaseDialog from '@/components/BaseDialog.vue'
import {
  buildResourceFileUrl,
  isPreviewableImage,
  isPreviewableVideo,
} from '@/utils/resource'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  resource: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:show'])

const playerRef = ref(null)
let art = null

const isVideo = computed(() => isPreviewableVideo(props.resource))
const isImage = computed(() => isPreviewableImage(props.resource))
const previewUrl = computed(() =>
  buildResourceFileUrl(props.resource?.filePath)
)

const destroyPlayer = () => {
  if (art) {
    art.destroy(false)
    art = null
  }
}

const initPlayer = async () => {
  if (!props.show || !isVideo.value || !previewUrl.value) {
    return
  }
  await nextTick()
  destroyPlayer()
  if (!playerRef.value) {
    return
  }
  art = new Artplayer({
    container: playerRef.value,
    url: previewUrl.value,
    type: 'm3u8',
    aspectRatio: true, //比例
    autoplay: false,
    setting: true,
    fullscreen: true,
    fullscreenWeb: true,
    playbackRate: true,
    pip: true,
    mutex: true,
    autoSize: false,
    autoMini: false,
    screenshot: true,
    customType: {
      m3u8: (video, url) => {
        if (Hls.isSupported()) {
          const hls = new Hls()
          hls.loadSource(url)
          hls.attachMedia(video)
          art.hls = hls
          art.on('destroy', () => hls.destroy())
          return
        }
        if (video.canPlayType('application/vnd.apple.mpegurl')) {
          video.src = url
        }
      },
    },
  })
}

const handleClosed = () => {
  destroyPlayer()
}

const openInNewTab = () => {
  if (!previewUrl.value) {
    return
  }
  window.open(previewUrl.value, '_blank', 'noopener,noreferrer')
}

watch(
  () => [props.show, props.resource?.filePath],
  async () => {
    if (!props.show) {
      destroyPlayer()
      return
    }
    if (isVideo.value) {
      await initPlayer()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<style scoped lang="scss">
.preview-player__canvas {
  width: 100%;
  height: 697px;
  overflow: hidden;
  background: #000;
}

.preview-fallback {
  display: flex;
  min-height: 360px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 16px;
}
</style>
