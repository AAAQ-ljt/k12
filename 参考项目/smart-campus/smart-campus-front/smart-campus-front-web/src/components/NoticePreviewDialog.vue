<template>
  <BaseDialog v-model:show="visible" :title="noticeTitle" width="760px" :show-cancel="false" :buttons="[]" :padding="0">
    <div class="notice-preview">
      <div class="notice-preview__meta">
        <el-tag v-if="Number(notice.isTop) === 1" type="warning" effect="light">置顶</el-tag>
        <span>{{ notice.publishTime || '暂未发布' }}</span>
        <span v-if="notice.createUserName">发布人：{{ notice.createUserName }}</span>
        <span>浏览 {{ Number(notice.viewCount || 0) }}</span>
      </div>
      <MdPreview :model-value="notice.noticeContent || '暂无内容'" />
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import BaseDialog from '@/components/BaseDialog.vue'

const props = defineProps({
  show: Boolean,
  notice: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:show'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})
const noticeTitle = computed(() => props.notice?.noticeTitle || '公告详情')
</script>

<style lang="scss" scoped>
.notice-preview {
  padding: 20px 24px 26px;
}

.notice-preview__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 14px;
  margin-bottom: 18px;
  color: #71839d;
  font-size: 13px;
}

.notice-preview :deep(.md-editor-preview-wrapper) {
  min-height: 260px;
  padding: 0;
}
</style>
