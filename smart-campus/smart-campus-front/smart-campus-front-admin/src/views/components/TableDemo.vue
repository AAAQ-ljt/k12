<template>
  <div class="table-page">
    <section class="search-card">
      <el-form :model="filters" inline label-width="68px">
        <el-form-item label="组件名称">
          <el-input v-model="filters.keyword" placeholder="请输入组件名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable filterable>
            <el-option label="已完成" value="done" />
            <el-option label="开发中" value="doing" />
            <el-option label="待接入" value="planned" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属">
          <el-select v-model="filters.owner" placeholder="请选择归属" clearable filterable>
            <el-option label="前端组" value="前端组" />
            <el-option label="业务组" value="业务组" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-card__actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <BaseDataTable :columns="columns" :header-height="40" :data="tableData" selection
      :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
      @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event">
      <template #cell-name="{ row }">
        <div class="name-cell">
          <strong>{{ row.name }}</strong>
          <span>{{ row.code }}</span>
        </div>
      </template>

      <template #cell-status="{ row }">
        <span :class="['status-tag', row.status]">{{ statusText[row.status] }}</span>
      </template>
    </BaseDataTable>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import BaseDataTable from '@/components/BaseDataTable.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const selectedRowKeys = ref([])
const filters = reactive({
  keyword: '',
  status: '',
  owner: '',
})

const statusText = {
  done: '已完成',
  doing: '开发中',
  planned: '待接入',
}

const sourceRows = Array.from({ length: 36 }, (_, index) => {
  const itemNo = index + 1
  const status = itemNo % 3 === 0 ? 'doing' : itemNo % 2 === 0 ? 'done' : 'planned'

  return {
    id: itemNo,
    name: `基础组件 ${itemNo}`,
    code: `COMP-${String(itemNo).padStart(3, '0')}`,
    scene: itemNo % 2 === 0 ? '后台列表页统一渲染' : '中后台详情页组合展示',
    owner: itemNo % 2 === 0 ? '前端组' : '业务组',
    status,
    updatedAt: `2026-04-${String((itemNo % 12) + 1).padStart(2, '0')} 12:${String(itemNo).padStart(2, '0')}`,
  }
})

const columns = [
  { key: 'name', prop: 'name', label: '组件信息', width: 220, slot: 'cell-name' },
  { key: 'scene', prop: 'scene', label: '适用场景', width: 280 },
  { key: 'owner', prop: 'owner', label: '归属', width: 120, align: 'center' },
  { key: 'status', prop: 'status', label: '状态', width: 120, align: 'center', slot: 'cell-status' },
  { key: 'updatedAt', prop: 'updatedAt', label: '更新时间' },
]

const filteredRows = computed(() =>
  sourceRows.filter((item) => {
    const matchKeyword =
      !filters.keyword || item.name.includes(filters.keyword) || item.code.includes(filters.keyword)
    const matchStatus = !filters.status || item.status === filters.status
    const matchOwner = !filters.owner || item.owner === filters.owner

    return matchKeyword && matchStatus && matchOwner
  }),
)

const tableData = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value
  const list = filteredRows.value.slice(start, start + pageSize.value)

  return {
    totalCount: filteredRows.value.length,
    pageSize: pageSize.value,
    pageNo: pageNo.value,
    list,
  }
})

const handleSearch = () => {
  pageNo.value = 1
}

const handleReset = () => {
  filters.keyword = ''
  filters.status = ''
  filters.owner = ''
  pageNo.value = 1
}
</script>

<style lang="scss" scoped>
.table-page {
  display: flex;
  min-height: 0;
  flex-direction: column;
  gap: 10px;
}

.search-card {
  padding: 12px 14px 0;
  border: 1px solid #dde6f5;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(30, 49, 86, 0.05);
}

.search-card__title {
  margin-bottom: 12px;
  color: #23324f;
  font-size: 15px;
  font-weight: 600;
}

.search-card__actions {
  :deep(.el-form-item__content) {
    display: flex;
    gap: 10px;
  }
}

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.5;

  strong {
    color: #24304a;
    font-size: 13px;
  }

  span {
    color: #7c8aa4;
    font-size: 12px;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;

  &.done {
    background: #edf8ee;
    color: #33a25b;
  }

  &.doing {
    background: #eef4ff;
    color: #4970ff;
  }

  &.planned {
    background: #fff4ea;
    color: #df7d32;
  }
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 12px;
  margin-bottom: 12px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-width: 220px;
}
</style>
