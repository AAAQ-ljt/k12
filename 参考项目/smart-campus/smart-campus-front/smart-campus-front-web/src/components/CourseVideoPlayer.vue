<template>
  <div class="course-video-player">
    <div v-if="!url" class="course-video-player__empty">
      <i class="iconfont icon-xinrenkecheng" />
      <span>当前课时暂未配置视频资源</span>
    </div>
    <div v-else ref="playerRef" class="course-video-player__inner" />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Artplayer from 'artplayer'
import Hls from 'hls.js'

const props = defineProps({
  url: {
    type: String,
    default: '',
  },
  poster: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    default: '',
  },
  autoplay: {
    type: Boolean,
    default: false,
  },
  initialTime: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['ready', 'timeupdate', 'ended'])

const playerRef = ref(null)

let artInstance = null
let hlsInstance = null
let seekLocked = false
let lastAllowedTime = 0

const destroyHls = () => {
  if (hlsInstance) {
    hlsInstance.destroy()
    hlsInstance = null
  }
}

const destroyPlayer = () => {
  destroyHls()
  if (artInstance) {
    artInstance.destroy(false)
    artInstance = null
  }
}

const buildHlsType = (url) => {
  const normalizedUrl = String(url || '').toLowerCase()
  return normalizedUrl.includes('.m3u8') ? 'm3u8' : 'auto'
}

const attachHls = (video, url, art) => {
  destroyHls()
  video.playsInline = true
  video.setAttribute('playsinline', 'true')
  video.setAttribute('webkit-playsinline', 'true')
  if (Hls.isSupported()) {
    hlsInstance = new Hls({
      enableWorker: true,
    })
    hlsInstance.loadSource(url)
    hlsInstance.attachMedia(video)
    hlsInstance.on(Hls.Events.MANIFEST_PARSED, () => {
      if (props.autoplay) {
        video.play().catch(() => {})
      }
    })
    hlsInstance.on(Hls.Events.ERROR, (_event, data) => {
      if (data?.fatal && art?.notice) {
        art.notice.show = '视频流加载失败'
      }
    })
    if (art) {
      art.hls = hlsInstance
    }
    return
  }
  if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = url
    return
  }
  video.src = url
}

const bindPlayerEvents = () => {
  if (!artInstance) {
    return
  }
  artInstance.on('ready', () => {
    const normalizedInitialTime = Math.max(0, Number(props.initialTime || 0))
    if (normalizedInitialTime > 0) {
      seekLocked = true
      lastAllowedTime = normalizedInitialTime
      artInstance.currentTime = normalizedInitialTime
      window.setTimeout(() => {
        seekLocked = false
      }, 60)
    }
    emit('ready', artInstance)
  })

  artInstance.on('video:timeupdate', () => {
    if (!artInstance || seekLocked || artInstance.video.seeking) {
      return
    }
    lastAllowedTime = Math.max(lastAllowedTime, Number(artInstance.currentTime || 0))
    emit('timeupdate', {
      currentTime: Number(artInstance.currentTime || 0),
      duration: Number(artInstance.duration || 0),
    })
  })

  artInstance.on('video:seeking', () => {
    if (!artInstance || seekLocked) {
      return
    }
    const currentTime = Number(artInstance.currentTime || 0)
    if (Math.abs(currentTime - lastAllowedTime) < 1) {
      return
    }
    seekLocked = true
    artInstance.currentTime = lastAllowedTime
    window.setTimeout(() => {
      seekLocked = false
    }, 30)
  })

  artInstance.on('video:ended', () => {
    emit('ended')
  })
}

const createPlayer = async () => {
  destroyPlayer()
  lastAllowedTime = 0
  if (!props.url || !playerRef.value) {
    return
  }

  await nextTick()
  artInstance = new Artplayer({
    container: playerRef.value,
    url: props.url,
    poster: props.poster,
    title: props.title,
    autoplay: props.autoplay,
    autoSize: false,
    aspectRatio: true,
    fullscreen: true,
    fullscreenWeb: true,
    setting: true,
    playbackRate: true,
    hotkey: false,
    pip: false,
    playsInline: true,
    screenshot: false,
    miniProgressBar: false,
    mutex: true,
    type: buildHlsType(props.url),
    customType: {
      m3u8(video, url, art) {
        attachHls(video, url, art)
      },
    },
  })

  bindPlayerEvents()
}

watch(
  () => [props.url, props.poster, props.title],
  () => {
    createPlayer()
  },
)

onMounted(() => {
  createPlayer()
})

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<style scoped>
.course-video-player {
  width: 100%;
}

.course-video-player__inner,
.course-video-player__empty {
  width: 100%;
  min-height: 460px;
  aspect-ratio: 16 / 9;
  border-radius: 6px;
  overflow: hidden;
  background: #081a35;
}

.course-video-player__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
}

.course-video-player__empty .iconfont {
  font-size: 28px;
}

.course-video-player :deep(.art-control-progress),
.course-video-player :deep(.art-progress),
.course-video-player :deep(.art-bottom .art-progress) {
  pointer-events: none !important;
}

.course-video-player :deep(.art-video-player) {
  border-radius: 6px;
}

.course-video-player :deep(.art-video-player video) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #081a35;
}

@media (max-width: 960px) {
  .course-video-player__inner,
  .course-video-player__empty {
    min-height: 260px;
  }
}
</style>
