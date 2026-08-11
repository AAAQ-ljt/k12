<template>
  <BaseDialog v-model:show="visible" title="从题库选择题目" width="1080px" :buttons="dialogButtons" @close="handleClose">
    <section class="toolbar-panel toolbar-panel--inner">
      <div class="toolbar-panel__filters">
        <el-form :model="filters" inline label-width="68px">
          <el-form-item label="题目信息">
            <el-input v-model="filters.keyword" placeholder="请输入题目标题" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="题目类型">
            <el-select v-model="filters.questionType" placeholder="请选择题目类型" clearable filterable @change="handleSearch">
              <el-option v-for="item in QUESTION_TYPE_OPTIONS" :key="item.value" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select v-model="filters.difficultyLevel" placeholder="请选择难度等级" clearable filterable
              @change="handleSearch">
              <el-option v-for="item in DIFFICULTY_LEVEL_OPTIONS" :key="item.value" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item class="toolbar-panel__search-actions">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" row-key="questionId" :header-height="40" :row-height="92"
      selection :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event" :extendHeight="130">
      <template #cell-questionInfo="{ row }">
        <div class="info-cell">
          <strong :title="row.questionTitle">{{ row.questionTitle || '-' }}</strong>
          <span>{{ row.questionTypeText || '-' }}</span>
        </div>
      </template>

      <template #cell-metaInfo="{ row }">
        <div class="info-cell">
          <strong>{{ row.difficultyLevelText || '-' }}</strong>
          <span>{{ row.optionCount || 0 }} 个选项 / {{ row.imageCount || 0 }} 张配图</span>
        </div>
      </template>

      <template #cell-answerInfo="{ row }">
        <div class="answer-cell" :title="row.answerDisplayText || '-'">
          {{ row.answerDisplayText || '-' }}
        </div>
      </template>
    </BaseDataTable>
  </BaseDialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import BaseDialog from '@/components/BaseDialog.vue'
import BaseDataTable from '@/components/BaseDataTable.vue'
import {
  DIFFICULTY_LEVEL_OPTIONS,
  formatQuestionAnswerText,
  getQuestionList,
  QUESTION_TYPE_OPTIONS,
} from '@/api/question'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:show', 'confirm'])

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const selectedRowMap = ref(new Map())
const tableData = ref({ totalCount: 0, pageNo: 1, pageSize: 15, list: [] })
const filters = reactive({
  keyword: '',
  questionType: undefined,
  difficultyLevel: undefined,
})

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const dialogButtons = computed(() => [
  {
    text: '确认选择',
    type: 'primary',
    click: handleConfirm,
  },
])

const columns = [
  {
    key: 'questionInfo',
    label: '题目信息',
    width: 360,
    slot: 'cell-questionInfo',
  },
  {
    key: 'metaInfo',
    label: '类型信息',
    width: 220,
    slot: 'cell-metaInfo',
  },
  {
    key: 'answerInfo',
    label: '标准答案',
    slot: 'cell-answerInfo',
  },
  {
    key: 'updateTime',
    prop: 'updateTime',
    label: '更新时间',
    width: 180,
  },
]

const loadTableData = async () => {
  tableData.value =
    (await getQuestionList({
      ...filters,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })) || tableData.value
  tableData.value = {
    ...tableData.value,
    list: (tableData.value.list || []).map((item) => ({
      ...item,
      answerDisplayText: formatQuestionAnswerText(
        item.questionType,
        item.correctAnswerText
      ),
    })),
  }

  tableData.value.list.forEach((item) => {
    if (selectedRowKeys.value.includes(item.questionId)) {
      selectedRowMap.value.set(item.questionId, item)
    }
  })
}

const handleSearch = async () => {
  pageNo.value = 1
  await loadTableData()
}

const handleConfirm = () => {
  const rows = selectedRowKeys.value
    .map((key) => selectedRowMap.value.get(key))
    .filter(Boolean)
  emit('confirm', rows)
  visible.value = false
}

function handleClose() {
  selectedRowKeys.value = []
  selectedRowMap.value = new Map()
}

watch([pageNo, pageSize], loadTableData)

watch(
  () => selectedRowKeys.value,
  (value) => {
    const rowMap = new Map(selectedRowMap.value)
    tableData.value.list.forEach((item) => {
      if (value.includes(item.questionId)) {
        rowMap.set(item.questionId, item)
      }
    })
    selectedRowMap.value = rowMap
  },
  { deep: true }
)

watch(
  () => props.show,
  async (show) => {
    if (!show) {
      return
    }
    await loadTableData()
  }
)
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
<style lang="scss" scoped>
.toolbar-panel--inner {
  margin-bottom: 14px;
}

.answer-cell {
  display: -webkit-box;
  overflow: hidden;
  color: #4c5f7d;
  line-height: 1.6;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
