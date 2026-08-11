<template>
  <BaseDrawer
    :show="show"
    title="计划详情"
    width="560px"
    :show-cancel="false"
    :padding="20"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <div v-if="loading" class="study-plan-drawer__loading">正在加载计划详情...</div>
    <div v-else-if="detailData" class="plan-detail">
      <div class="plan-detail__head">
        <div class="plan-detail__cover">
          <img
            v-if="buildCoverUrl(detailData.coverPath)"
            :src="buildCoverUrl(detailData.coverPath)"
            :alt="detailData.courseName"
          >
          <span v-else>{{ detailData.courseName?.slice(0, 2) || '课程' }}</span>
        </div>
        <div class="plan-detail__summary">
          <h3>{{ detailData.courseName }}</h3>
          <p>{{ detailData.description || '未填写计划说明' }}</p>
          <div class="plan-detail__meta">
            <el-tag size="small" :type="statusTagType(detailData.status)" effect="light">
              {{ detailData.statusText }}
            </el-tag>
            <span>进度 {{ detailData.progress || 0 }}%</span>
            <span>{{ detailData.completedCount || 0 }}/{{ detailData.taskCount || 0 }} 项</span>
          </div>
        </div>
      </div>

      <div class="plan-detail__list">
        <article
          v-for="item in detailData.itemList || []"
          :key="item.itemId"
          class="plan-detail__item"
        >
          <div class="plan-detail__item-main">
            <strong>{{ item.lessonName || item.chapterName || '未命名课时' }}</strong>
            <p>{{ item.chapterName || '未命名章节' }}</p>
            <p>{{ item.studyDate }} {{ item.timeRangeText }}</p>
          </div>
          <div class="plan-detail__item-side">
            <el-tag size="small" :type="statusTagType(item.status)" effect="light">
              {{ item.statusText }}
            </el-tag>
            <button
              type="button"
              class="study-plan-link"
              @click="emit('toggle-status', item)"
            >
              {{ Number(item.status) === 2 ? '恢复未完成' : '标记完成' }}
            </button>
          </div>
        </article>
      </div>
    </div>
  </BaseDrawer>
</template>

<script setup>
import BaseDrawer from '@/components/BaseDrawer.vue'
import { buildResourceFileUrl } from '@/utils/resource'

defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  detailData: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:show', 'close', 'toggle-status'])

const buildCoverUrl = (path) => buildResourceFileUrl(path)

const statusTagType = (status) => {
  if (Number(status) === 2) {
    return 'success'
  }
  if (Number(status) === 1) {
    return 'warning'
  }
  return 'info'
}
</script>

<style scoped>
.study-plan-drawer__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: #6f839d;
  font-size: 14px;
}

.study-plan-link {
  border: 0;
  background: transparent;
  color: #2d73f5;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.plan-detail__head {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.plan-detail__cover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  overflow: hidden;
  border-radius: 6px;
  background: linear-gradient(135deg, #edf4ff 0%, #dbe8ff 100%);
  color: #2d73f5;
  font-weight: 700;
}

.plan-detail__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.plan-detail__summary h3 {
  margin: 0 0 8px;
  color: #162f58;
  font-size: 20px;
}

.plan-detail__summary p {
  margin: 0 0 10px;
  color: #6f829d;
  font-size: 13px;
  line-height: 1.8;
}

.plan-detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #556984;
  font-size: 13px;
}

.plan-detail__list {
  display: grid;
  gap: 12px;
}

.plan-detail__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid #eef3fb;
}

.plan-detail__item:first-child {
  border-top: 0;
}

.plan-detail__item-main strong {
  display: block;
  margin-bottom: 6px;
  color: #162f58;
  font-size: 15px;
}

.plan-detail__item-main p {
  margin: 0 0 4px;
  color: #6f829d;
  font-size: 13px;
}

.plan-detail__item-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

@media (max-width: 960px) {
  .plan-detail__head {
    display: grid;
    grid-template-columns: 1fr;
  }

  .plan-detail__item {
    flex-direction: column;
    align-items: flex-start;
  }

  .plan-detail__item-side {
    align-items: flex-start;
  }
}
</style>
