<template>
  <div class="chapter-page-bridge">
    <CourseChapterDialog
      v-model:show="dialogVisible"
      :course-id="courseId"
      @saved="handleSaved"
      @update:show="handleDialogVisibleChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CourseChapterDialog from '@/views/teaching/components/CourseChapterDialog.vue'

const route = useRoute()
const router = useRouter()
const dialogVisible = ref(false)

const courseId = computed(() => String(route.query.courseId || '').trim())

const goBack = () => {
  router.replace({ name: 'teachingCourse' })
}

const handleDialogVisibleChange = (show) => {
  dialogVisible.value = show
  if (!show) {
    goBack()
  }
}

const handleSaved = () => {
  goBack()
}

onMounted(() => {
  dialogVisible.value = true
})
</script>

<style scoped>
.chapter-page-bridge {
  min-height: 1px;
}
</style>
