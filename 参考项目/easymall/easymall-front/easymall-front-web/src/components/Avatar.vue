<template>
  <Cover :source="source" :width="100" :lazy="lazy" borderRadius="50%"></Cover>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()

const props = defineProps({
  avatar: {
    type: String,
    default: 'avatar.png',
  },
  lazy: {
    type: Boolean,
    default: true,
  },
})

const source = computed(() => {
  if (!props.avatar) {
    return 'avatar.png'
  } else {
    return props.avatar
  }
})
</script>
<style lang="scss" scoped></style>
