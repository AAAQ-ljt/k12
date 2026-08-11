<template>
  <BaseDialog v-model:show="visible" title="移动目录" width="560px" :buttons="dialogButtons">
    <div class="move-dialog">
      <div class="move-dialog__tip">请选择目标目录</div>
      <el-tree
        ref="treeRef"
        :data="filteredTreeData"
        node-key="id"
        :props="treeProps"
        highlight-current
        default-expand-all
        :expand-on-click-node="false"
        @current-change="handleCurrentChange"
      >
        <template #default="{ data }">
          <span>{{ data.resourceName }}</span>
        </template>
      </el-tree>
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'

const props = defineProps({
  show: Boolean,
  currentIds: { type: Array, default: () => [] },
  treeData: { type: Array, default: () => [] },
  currentParentId: { type: Number, default: 0 },
})

const emit = defineEmits(['update:show', 'submit'])
const treeRef = ref()
const selectedTargetId = ref(0)

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const treeProps = {
  label: 'resourceName',
  children: 'children',
}

const filteredTreeData = computed(() => {
  const currentIdSet = new Set((props.currentIds || []).map((item) => Number(item)))
  const walk = (list = []) =>
    list
      .filter((item) => !currentIdSet.has(Number(item.id)))
      .map((item) => ({
        ...item,
        children: walk(item.children || []),
      }))

  return [
    {
      id: 0,
      resourceName: '根目录',
      children: walk(props.treeData),
    },
  ]
})

const dialogButtons = computed(() => [{ text: '确认移动', type: 'primary', click: handleSubmit }])

watch(
  () => [props.show, props.currentParentId],
  () => {
    selectedTargetId.value = Number(props.currentParentId ?? 0)
    if (props.show) {
      setTimeout(() => {
        treeRef.value?.setCurrentKey?.(selectedTargetId.value)
      }, 0)
    }
  },
  { immediate: true },
)

function handleCurrentChange(data) {
  selectedTargetId.value = Number(data?.id ?? 0)
}

function handleSubmit() {
  emit('submit', { targetParentId: selectedTargetId.value })
}
</script>

<style scoped lang="scss">
.move-dialog {
  min-height: 280px;
}

.move-dialog__tip {
  margin-bottom: 10px;
  color: #63718d;
  font-size: 13px;
}
</style>
