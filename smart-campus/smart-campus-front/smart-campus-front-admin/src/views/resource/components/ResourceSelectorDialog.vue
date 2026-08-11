<template>
  <BaseDialog v-model:show="visible" title="选择资源" width="80%" :buttons="dialogButtons" :show-cancel="true" :padding="0"
    body-overflow="hidden">
    <div class="resource-selector-shell">
      <ResourceManagement :key="resourceManagerKey" selector-mode :fixed-resource-type="resourceType"
        @select-resource="handleChoose" @selection-resource-change="handleSelectionChange" :extendHeight="150" />
    </div>
    <div class="resource-selector__summary">
      <span class="resource-selector__summary-label">当前选择：</span>
      <span class="resource-selector__summary-value">
        {{ selectedResource?.resourceName || '未选择资源' }}
      </span>
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseDialog from '@/components/BaseDialog.vue'
import ResourceManagement from '@/views/resource/ResourceManagement.vue'

const props = defineProps({
  show: Boolean,
  resourceType: {
    type: Number,
    default: undefined,
  },
})

const emit = defineEmits(['update:show', 'select'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const selectedResource = ref(null)
const resourceManagerKey = ref(0)

const dialogButtons = computed(() => [
  {
    text: '确认选择',
    type: 'primary',
    click: handleConfirm,
  },
])

const handleChoose = (row) => {
  selectedResource.value = row
}

const handleSelectionChange = (row) => {
  selectedResource.value = row
}

const handleConfirm = () => {
  if (!selectedResource.value) {
    ElMessage.warning('请先选择一个资源')
    return
  }
  emit('select', selectedResource.value)
  visible.value = false
}

watch(
  () => props.show,
  (show) => {
    if (show) {
      selectedResource.value = null
      resourceManagerKey.value += 1
    }
  }
)
</script>

<style lang="scss" scoped>
.resource-selector-shell {
  padding: 16px;
}

.resource-selector__summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px 16px;
  color: #6b7a96;
  font-size: 13px;
}

.resource-selector__summary-label {
  flex: 0 0 auto;
}

.resource-selector__summary-value {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
