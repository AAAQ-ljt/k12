<template>
  <BaseDialog v-model:show="visible" title="选择试卷" width="800px" :buttons="dialogButtons" :show-cancel="true"
    :padding="0" body-overflow="hidden">
    <div class="paper-selector">
      <div class="paper-selector__toolbar">
        <el-input v-model="keyword" placeholder="请输入试卷名称" clearable class="paper-selector__search"
          @keyup.enter="loadDataList" @clear="loadDataList" />
        <el-button type="primary" @click="loadDataList">查询</el-button>
      </div>

      <div class="paper-selector__table">
        <BaseDataTable :columns="columns" :data="tableData" :loading="loading" row-key="paperId" :row-height="62"
          selection :selected-row-keys="selectedRowKeys" @row-click="handleRowClick"
          @selection-change="handleSelectionChange" @pagination-change="handlePaginationChange" :extendHeight="130">
          <template #cell-paperInfo="{ row }">
            <div class="paper-selector__paper-info">
              <strong>{{ row.paperName || '-' }}</strong>
              <span>{{ row.paperTypeText || '未知' }} · {{ row.questionCount }} 题</span>
            </div>
          </template>

          <template #cell-totalScore="{ row }">
            <span>{{ row.totalScore }}</span>
          </template>

          <template #cell-updateTime="{ row }">
            <span>{{ row.updateTime || '-' }}</span>
          </template>
        </BaseDataTable>
      </div>

      <div class="paper-selector__summary">
        <span class="paper-selector__summary-label">当前选择：</span>
        <span class="paper-selector__summary-value">
          {{ selectedPaper?.paperName || '未选择试卷' }}
        </span>
      </div>
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseDataTable from '@/components/BaseDataTable.vue'
import BaseDialog from '@/components/BaseDialog.vue'
import { getPaperList } from '@/api/paper'

const props = defineProps({
  show: Boolean,
  paperType: {
    type: Number,
    default: 1,
  },
})

const emit = defineEmits(['update:show', 'select'])

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value),
})

const loading = ref(false)
const keyword = ref('')
const selectedPaper = ref(null)
const selectedRowKeys = ref([])
const pagination = ref({
  pageNo: 1,
  pageSize: 15,
})
const tableData = ref({
  totalCount: 0,
  pageNo: 1,
  pageSize: 15,
  list: [],
})

const columns = [
  {
    key: 'paperInfo',
    title: '试卷信息',
    slot: 'cell-paperInfo',
  },
  {
    key: 'totalScore',
    title: '总分',
    width: 120,
    slot: 'cell-totalScore',
  },
  {
    key: 'updateTime',
    title: '更新时间',
    width: 180,
    slot: 'cell-updateTime',
  },
]

const dialogButtons = computed(() => [
  {
    text: '确认选择',
    type: 'primary',
    click: handleConfirm,
  },
])

const loadDataList = async () => {
  loading.value = true
  try {
    tableData.value = await getPaperList({
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      keyword: keyword.value,
      paperType: props.paperType,
    })
  } finally {
    loading.value = false
  }
}

const applySelectedPaper = (row) => {
  selectedPaper.value = row || null
  selectedRowKeys.value = row?.paperId ? [row.paperId] : []
}

const handleRowClick = (row) => {
  applySelectedPaper(row)
}

const handleSelectionChange = (rows, rowKeys = []) => {
  if (!rowKeys.length) {
    applySelectedPaper(null)
    return
  }
  const targetKey = rowKeys[rowKeys.length - 1]
  const matchedRow =
    rows.find((item) => item.paperId === targetKey) ||
    tableData.value.list.find((item) => item.paperId === targetKey)
  applySelectedPaper(matchedRow || null)
}

const handlePaginationChange = ({ pageNo, pageSize }) => {
  pagination.value = {
    pageNo,
    pageSize,
  }
  loadDataList()
}

const handleConfirm = () => {
  if (!selectedPaper.value) {
    ElMessage.warning('请先选择一份试卷')
    return
  }
  emit('select', selectedPaper.value)
  visible.value = false
}

watch(
  () => props.show,
  async (show) => {
    if (!show) {
      return
    }
    keyword.value = ''
    applySelectedPaper(null)
    pagination.value = {
      pageNo: 1,
      pageSize: 15,
    }
    await loadDataList()
  }
)
</script>

<style lang="scss" scoped>
.paper-selector {
  display: flex;
  min-height: 560px;
  flex-direction: column;
}

.paper-selector__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #eef2f8;
}

.paper-selector__search {
  width: 280px;
}

.paper-selector__table {
  min-height: 0;
  flex: 1;
  padding: 16px;
}

.paper-selector__paper-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: #2b2f36;
    font-size: 14px;
    font-weight: 700;
  }

  span {
    color: #7e8ca6;
    font-size: 12px;
  }
}

.paper-selector__summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px 16px;
  color: #6b7a96;
  font-size: 13px;
}

.paper-selector__summary-label {
  flex: 0 0 auto;
}

.paper-selector__summary-value {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
