<template>
  <BaseDrawer
    :show="show"
    :title="mode === 'edit' ? '调整学习计划' : '新建学习计划'"
    width="720px"
    :padding="20"
    :buttons="drawerButtons"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <el-form
      ref="formRef"
      :model="formData"
      label-position="top"
      class="plan-form"
    >
      <div class="plan-form__grid">
        <el-form-item label="课程" required>
          <el-select
            v-model="formData.courseId"
            placeholder="选择课程"
            style="width: 100%;"
            :disabled="mode === 'edit'"
            @change="emit('course-change', $event)"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.courseId"
              :label="item.courseName"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="计划说明">
          <el-input
            v-model.trim="formData.description"
            type="textarea"
            :rows="3"
            placeholder="写下这份计划的重点目标，比如准备哪次考试、想优先完成哪些章节。"
          />
        </el-form-item>
      </div>

      <div class="plan-form__section">
        <div class="plan-form__section-head">
          <h4>学习课时安排</h4>
          <span>按章节查看课时，勾选需要学习的课时后设置学习日期和时间</span>
        </div>

        <div v-if="loading" class="study-plan-drawer__loading">正在加载课程章节...</div>
        <div v-else-if="!lessonPlanGroups.length" class="study-plan-drawer__empty">
          请先选择课程
        </div>

        <div v-else class="chapter-group-list">
          <section
            v-for="chapter in lessonPlanGroups"
            :key="chapter.chapterId"
            class="chapter-group"
          >
            <header class="chapter-group__head">
              <strong>{{ chapter.chapterName }}</strong>
              <span>{{ chapter.lessonList.length }} 个课时</span>
            </header>

            <div class="chapter-plan-list">
              <article
                v-for="item in chapter.lessonList"
                :key="item.lessonId"
                class="chapter-plan-item"
                :class="{ 'is-planned': plannedLessonIds.includes(item.lessonId) }"
              >
                <div class="chapter-plan-item__check">
                  <el-checkbox
                    v-model="item.enabled"
                    :disabled="plannedLessonIds.includes(item.lessonId)"
                  />
                </div>

                <div class="chapter-plan-item__content">
                  <strong>{{ item.lessonName }}</strong>
                  <p>
                    {{ chapter.chapterName }}
                    <span v-if="plannedLessonIds.includes(item.lessonId)" class="chapter-plan-item__planned-tag">已有计划</span>
                  </p>
                </div>

                <div class="chapter-plan-item__controls">
                  <el-date-picker
                    v-model="item.studyDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="学习日期"
                    :disabled="!item.enabled || plannedLessonIds.includes(item.lessonId)"
                    :disabled-date="disablePastDate"
                    @change="handleStudyDateChange(item)"
                    style="width: 140px;"
                  />
                  <el-time-picker
                    v-model="item.timeRange"
                    is-range
                    range-separator="-"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    value-format="HH:mm"
                    format="HH:mm"
                    :disabled="!item.enabled || plannedLessonIds.includes(item.lessonId)"
                    :selectable-range="getSelectableRange(item.studyDate)"
                    style="width: 240px;"
                  />
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </el-form>
  </BaseDrawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import BaseDrawer from '@/components/BaseDrawer.vue'

defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'create',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    required: true,
  },
  courseOptions: {
    type: Array,
    default: () => [],
  },
  lessonPlanGroups: {
    type: Array,
    default: () => [],
  },
  plannedLessonIds: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:show', 'close', 'submit', 'course-change'])
const formRef = ref()

const drawerButtons = computed(() => [
  {
    text: '保存计划',
    type: 'primary',
    click: () => emit('submit'),
  },
])

const formatDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const disablePastDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

const getTodayMinTime = () => {
  const now = new Date()
  if (now.getSeconds() > 0 || now.getMilliseconds() > 0) {
    now.setMinutes(now.getMinutes() + 1)
  }
  now.setSeconds(0, 0)
  if (formatDateKey(now) !== formatDateKey(new Date())) {
    return '23:59:59'
  }
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}:00`
}

const getSelectableRange = (studyDate) => {
  const today = formatDateKey(new Date())
  if (studyDate !== today) {
    return '00:00:00 - 23:59:59'
  }
  return `${getTodayMinTime()} - 23:59:59`
}

const isTimeRangeExpired = (studyDate, timeRange) => {
  if (!studyDate || !Array.isArray(timeRange) || timeRange.length !== 2) {
    return false
  }
  const today = formatDateKey(new Date())
  if (studyDate !== today) {
    return false
  }
  const [startTime] = timeRange
  return `${studyDate} ${startTime}` < `${today} ${getTodayMinTime().slice(0, 5)}`
}

const handleStudyDateChange = (item) => {
  if (isTimeRangeExpired(item.studyDate, item.timeRange)) {
    item.timeRange = []
  }
}
</script>

<style scoped>
.study-plan-drawer__loading,
.study-plan-drawer__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: #6f839d;
  font-size: 14px;
}

.plan-form__grid {
  display: grid;
  gap: 12px;
}

.plan-form__section {
  margin-top: 8px;
}

.plan-form__section-head {
  margin-bottom: 14px;
}

.plan-form__section-head h4 {
  margin: 0 0 6px;
  color: #182f56;
  font-size: 16px;
}

.plan-form__section-head span {
  color: #7487a3;
  font-size: 13px;
}

.chapter-group-list {
  display: grid;
  gap: 16px;
}

.chapter-group {
  display: grid;
  gap: 12px;
}

.chapter-group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 2px;
}

.chapter-group__head strong {
  color: #182f56;
  font-size: 15px;
}

.chapter-group__head span {
  color: #7a8ea9;
  font-size: 12px;
}

.chapter-plan-list {
  display: grid;
  gap: 12px;
}

.chapter-plan-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 400px;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border: 1px solid #e9effa;
  border-radius: 12px;
  background: #fff;
}

.chapter-plan-item.is-planned {
  background: #f5f7fa;
  opacity: 0.7;
}

.chapter-plan-item__planned-tag {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #e6e8eb;
  color: #909399;
  font-size: 12px;
}

.chapter-plan-item__check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.chapter-plan-item__content strong {
  display: block;
  margin-bottom: 6px;
  color: #162f58;
  font-size: 15px;
}

.chapter-plan-item__content p {
  margin: 0;
  color: #6e819d;
  font-size: 13px;
}

.chapter-plan-item__controls {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.chapter-plan-item__check .el-checkbox) {
  margin-right: 0;
}

@media (max-width: 1280px) {
  .chapter-plan-item {
    grid-template-columns: 28px minmax(0, 1fr);
  }

  .chapter-plan-item__controls {
    grid-column: 2 / 3;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
