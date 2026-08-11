<template>
  <div ref="wrapperRef" v-loading="loading" class="base-data-table" :style="wrapperStyle">
    <div class="base-data-table__inner">
      <el-table ref="tableRef" :data="displayRows" :height="tableHeight" :row-key="resolvedRowKey" highlight-current-row
        class="base-data-table__table" :row-class-name="getRowClassName" @row-click="handleRowClick"
        @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <template #empty>
          <slot name="empty">
            <el-empty :image-size="88" description="暂无数据" />
          </slot>
        </template>

        <el-table-column v-if="selection" type="selection" :width="selectionColumnWidth" align="center" />

        <el-table-column v-if="draggable" label="" width="56" align="center">
          <template #default="{ $index }">
            <div class="base-data-table__drag-handle" @mousedown="pendingDragIndex = $index" @mouseup="resetPendingDrag"
              @click.stop>
              <i class="iconfont icon-drag base-data-table__drag-icon" aria-hidden="true" />
            </div>
          </template>
        </el-table-column>

        <el-table-column v-for="column in normalizedColumns" :key="column.key" :prop="column.dataKey"
          :label="column.title" :width="column.width" :min-width="column.minWidth" :align="column.align"
          :fixed="column.fixed" :sortable="column.sortable ? 'custom' : false">
          <template v-if="hasHeaderSlot(column)" #header="scope">
            <slot :name="column.headerSlot || `header-${column.key}`" v-bind="buildHeaderSlotParams(scope, column)" />
          </template>

          <template #default="scope">
            <slot v-if="hasCellSlot(column)" :name="column.slot || `cell-${column.key}`"
              v-bind="buildCellSlotParams(scope, column)" />
            <RenderCell v-else-if="hasCustomRenderer(column)" :renderer="() => renderCustomCell(scope, column)" />
            <span v-else>{{ formatPlainCell(scope.row, column, scope.$index) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="showPagination" ref="footerRef" class="base-data-table__footer">
      <div class="base-data-table__meta">
        <span>共 {{ totalCount }} 条</span>
        <span v-if="selection && currentSelectedRowKeys.length">已选 {{ currentSelectedRowKeys.length }} 条</span>
      </div>

      <div class="base-data-table__pager">
        <el-select :model-value="safeCurrentPageSize" class="base-data-table__page-size"
          @update:model-value="handlePageSizeChange">
          <el-option v-for="size in normalizedPageSizes" :key="size" :label="`${size} 条/页`" :value="size" />
        </el-select>

        <el-pagination :current-page="safeCurrentPageNo" :page-size="safeCurrentPageSize"
          :page-sizes="normalizedPageSizes" :layout="paginationLayout" :pager-count="pagerCount" :total="totalCount"
          background @current-change="handlePageNoChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  computed,
  defineComponent,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  useSlots,
  watch,
} from 'vue'
import '@/assets/icon/iconfont.css'

const RenderCell = defineComponent({
  name: 'RenderCell',
  props: {
    renderer: {
      type: Function,
      required: true,
    },
  },
  setup(props) {
    return () => props.renderer()
  },
})

const props = defineProps({
  columns: {
    type: Array,
    default: () => [],
  },
  data: {
    type: [Array, Object],
    default: () => ({
      totalCount: 0,
      pageSize: 15,
      pageNo: 1,
      list: [],
    }),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  rowKey: {
    type: [String, Function],
    default: 'id',
  },
  headerHeight: {
    type: Number,
    default: 40,
  },
  rowHeight: {
    type: Number,
    default: 64,
  },
  minHeight: {
    type: Number,
    default: 280,
  },
  minTableHeight: {
    type: Number,
    default: 220,
  },
  bottomOffset: {
    type: Number,
    default: 12,
  },
  showPagination: {
    type: Boolean,
    default: true,
  },
  pageSizes: {
    type: Array,
    default: () => [15, 30, 50, 100],
  },
  pagerCount: {
    type: Number,
    default: 7,
  },
  paginationLayout: {
    type: String,
    default: 'prev, pager, next, jumper',
  },
  selection: {
    type: Boolean,
    default: false,
  },
  selectedRowKeys: {
    type: Array,
    default: undefined,
  },
  selectionColumnWidth: {
    type: Number,
    default: 52,
  },
  draggable: {
    type: Boolean,
    default: false,
  },
  extendHeight: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits([
  'update:pageNo',
  'update:pageSize',
  'update:selectedRowKeys',
  'page-change',
  'size-change',
  'pagination-change',
  'selection-change',
  'row-click',
  'row-sort',
])

const slots = useSlots()
const wrapperRef = ref(null)
const tableRef = ref(null)
const footerRef = ref(null)
const containerHeight = ref(props.minHeight)
const footerHeight = ref(0)
const innerSelectedRowKeys = ref([])
const dragRows = ref([])
const dragFromIndex = ref(-1)
const dragOverIndex = ref(-1)
const pendingDragIndex = ref(-1)
const isSyncingSelection = ref(false)

let wrapperObserver
let footerObserver
let resizeFrame = 0

const normalizedData = computed(() => {
  if (Array.isArray(props.data)) {
    return {
      totalCount: props.data.length,
      pageSize: props.pageSizes[0] || 15,
      pageNo: 1,
      list: props.data,
    }
  }

  return {
    totalCount: Number(
      props.data?.totalCount ??
      props.data?.total ??
      props.data?.list?.length ??
      0
    ),
    pageSize: Number(props.data?.pageSize ?? props.pageSizes[0] ?? 15),
    pageNo: Number(props.data?.pageNo ?? props.data?.pageNum ?? 1),
    list: Array.isArray(props.data?.list) ? props.data.list : [],
  }
})

const rows = computed(() => normalizedData.value.list)
const displayRows = computed(() =>
  props.draggable ? dragRows.value : rows.value
)
const totalCount = computed(() => normalizedData.value.totalCount)
const currentPageNo = computed(() => normalizedData.value.pageNo)
const currentPageSize = computed(() => normalizedData.value.pageSize)
const normalizedPageSizes = computed(() =>
  props.pageSizes
    .map((size) => Number(size))
    .filter((size) => Number.isFinite(size) && size > 0)
)
const safeCurrentPageSize = computed(() => {
  const pageSize = Number(currentPageSize.value)
  if (Number.isFinite(pageSize) && pageSize > 0) {
    return pageSize
  }
  return normalizedPageSizes.value[0] || 15
})
const totalPages = computed(() =>
  Math.max(Math.ceil(totalCount.value / safeCurrentPageSize.value), 1)
)
const safeCurrentPageNo = computed(() => {
  const pageNo = Number(currentPageNo.value)
  if (!Number.isFinite(pageNo) || pageNo < 1) {
    return 1
  }
  return Math.min(pageNo, totalPages.value)
})
const currentSelectedRowKeys = computed(() =>
  Array.isArray(props.selectedRowKeys)
    ? props.selectedRowKeys
    : innerSelectedRowKeys.value
)
const normalizedColumns = computed(() =>
  props.columns
    .filter((column) => !column.hidden)
    .map((column, index) => {
      const key =
        column.key || column.prop || column.dataKey || `column-${index}`
      const dataKey = column.dataKey || column.prop || key
      const width = resolveColumnWidth(column.width)
      const minWidth = resolveColumnWidth(column.minWidth) ?? width ?? 160
      return {
        ...column,
        key,
        dataKey,
        title: column.title || column.label || '',
        width,
        minWidth,
        align: column.align || 'left',
      }
    })
)
const resolvedRowKey = computed(() => props.rowKey)
const tableHeight = computed(() => {
  const usableHeight =
    containerHeight.value - (props.showPagination ? footerHeight.value : 0)
  return Math.max(usableHeight, props.minTableHeight)
})
const wrapperStyle = computed(() => ({
  height: `${containerHeight.value}px`,
  '--base-data-table-header-height': '40px',
  '--base-data-table-row-height': `${props.rowHeight}px`,
}))

const resolveRowKey = (row, rowIndex) => {
  if (typeof props.rowKey === 'function') {
    return props.rowKey(row, rowIndex)
  }
  if (row && props.rowKey in row) {
    return row[props.rowKey]
  }
  return rowIndex
}

const resolveCellValue = (row, column) => {
  const dataKey = column.dataKey || column.prop || column.key
  return dataKey ? row?.[dataKey] : undefined
}

const formatPlainCell = (row, column, rowIndex) => {
  const params = {
    cellData: resolveCellValue(row, column),
    column,
    row,
    rowData: row,
    rowIndex,
  }
  if (typeof column.formatter === 'function') {
    return column.formatter(params)
  }
  return params.cellData ?? '-'
}

const buildCellSlotParams = (scope, column) => ({
  cellData: resolveCellValue(scope.row, column),
  column,
  row: scope.row,
  rowData: scope.row,
  rowIndex: scope.$index,
})

const buildHeaderSlotParams = (scope, column) => ({
  ...scope,
  column,
})

const hasHeaderSlot = (column) =>
  Boolean(slots[column.headerSlot || `header-${column.key}`])

const hasCellSlot = (column) =>
  Boolean(slots[column.slot || `cell-${column.key}`])

const hasCustomRenderer = (column) =>
  typeof column.render === 'function' || typeof column.formatter === 'function'

const renderCustomCell = (scope, column) => {
  const params = buildCellSlotParams(scope, column)
  if (typeof column.render === 'function') {
    return column.render(params)
  }
  return column.formatter(params)
}

const resolveColumnWidth = (value) => {
  const width = Number(value)
  return Number.isFinite(width) && width > 0 ? width : undefined
}

const updateSelectedRowKeys = (nextKeys) => {
  if (!Array.isArray(props.selectedRowKeys)) {
    innerSelectedRowKeys.value = nextKeys
  }
  emit('update:selectedRowKeys', nextKeys)
  emit(
    'selection-change',
    displayRows.value.filter((row, index) =>
      nextKeys.includes(resolveRowKey(row, index))
    ),
    nextKeys
  )
}

const handleSelectionChange = (rows) => {
  if (isSyncingSelection.value) {
    return
  }
  const nextKeys = rows.map((row, index) =>
    resolveRowKey(
      row,
      displayRows.value.indexOf(row) > -1
        ? displayRows.value.indexOf(row)
        : index
    )
  )
  updateSelectedRowKeys(nextKeys)
}

const syncSelectionToTable = async () => {
  if (!props.selection || !tableRef.value) {
    return
  }
  isSyncingSelection.value = true
  tableRef.value.clearSelection()
  const selectedKeySet = new Set(currentSelectedRowKeys.value)
  displayRows.value.forEach((row, index) => {
    if (selectedKeySet.has(resolveRowKey(row, index))) {
      tableRef.value.toggleRowSelection(row, true)
    }
  })
  await nextTick()
  isSyncingSelection.value = false
}

const handleRowClick = (row, column, event) => {
  emit('row-click', row, displayRows.value.indexOf(row), column, event)
}

const getRowClassName = ({ row, rowIndex }) => {
  const classNames = ['base-data-table__row']
  if (props.draggable && dragOverIndex.value === rowIndex) {
    classNames.push('is-drag-over')
  }
  if (props.draggable && dragFromIndex.value === rowIndex) {
    classNames.push('is-dragging')
  }
  return classNames.join(' ')
}

const resetPendingDrag = () => {
  if (dragFromIndex.value < 0) {
    pendingDragIndex.value = -1
  }
}

const handleDragStart = (event, index) => {
  dragFromIndex.value = index
  dragOverIndex.value = index
  pendingDragIndex.value = index
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(index))
  }
}

const handleDragEnd = () => {
  dragFromIndex.value = -1
  dragOverIndex.value = -1
  pendingDragIndex.value = -1
}

const handleRowDrop = (toIndex) => {
  const fromIndex = dragFromIndex.value
  if (fromIndex < 0 || toIndex < 0 || fromIndex === toIndex) {
    handleDragEnd()
    return
  }

  const nextRows = [...dragRows.value]
  const [movedRow] = nextRows.splice(fromIndex, 1)
  nextRows.splice(toIndex, 0, movedRow)
  dragRows.value = nextRows
  emit('row-sort', {
    list: nextRows,
    movedRow,
    oldIndex: fromIndex,
    newIndex: toIndex,
  })
  handleDragEnd()
}

const syncDragRows = async () => {
  if (!props.draggable || !tableRef.value?.$el) {
    return
  }
  await nextTick()
  const rowElements = tableRef.value.$el.querySelectorAll(
    '.el-table__body-wrapper tbody tr'
  )
  rowElements.forEach((rowElement, index) => {
    rowElement.draggable = true
    rowElement.ondragstart = (event) => {
      if (pendingDragIndex.value !== -1 && pendingDragIndex.value !== index) {
        event.preventDefault()
        return
      }
      handleDragStart(event, index)
    }
    rowElement.ondragenter = (event) => {
      event.preventDefault()
      dragOverIndex.value = index
    }
    rowElement.ondragover = (event) => {
      event.preventDefault()
      if (event.dataTransfer) {
        event.dataTransfer.dropEffect = 'move'
      }
      dragOverIndex.value = index
    }
    rowElement.ondragleave = () => {
      if (dragOverIndex.value === index) {
        dragOverIndex.value = -1
      }
    }
    rowElement.ondrop = (event) => {
      event.preventDefault()
      handleRowDrop(index)
    }
    rowElement.ondragend = () => {
      handleDragEnd()
    }
  })
}

const syncLayout = () => {
  if (!wrapperRef.value || typeof window === 'undefined') {
    return
  }

  const rect = wrapperRef.value.getBoundingClientRect()
  containerHeight.value = Math.max(
    window.innerHeight - rect.top - props.bottomOffset - props.extendHeight,
    props.minHeight
  )
  footerHeight.value =
    props.showPagination && footerRef.value ? footerRef.value.offsetHeight : 0
}

const queueLayoutSync = () => {
  cancelAnimationFrame(resizeFrame)
  resizeFrame = requestAnimationFrame(syncLayout)
}

const handlePageNoChange = (pageNo) => {
  const nextPageNo = Math.min(
    Math.max(Number(pageNo) || 1, 1),
    totalPages.value
  )
  emit('update:pageNo', nextPageNo)
  emit('page-change', nextPageNo)
  emit('pagination-change', {
    pageNo: nextPageNo,
    pageSize: safeCurrentPageSize.value,
  })
}

const handlePageSizeChange = (pageSize) => {
  const nextPageSize = Number(pageSize) || normalizedPageSizes.value[0] || 15
  emit('update:pageSize', nextPageSize)
  emit('update:pageNo', 1)
  emit('size-change', nextPageSize)
  emit('pagination-change', { pageNo: 1, pageSize: nextPageSize })
}

const handleSortChange = ({ column, prop, order }) => {
  emit('row-sort', { column, prop, order })
}

watch(
  () => props.selectedRowKeys,
  (value) => {
    if (!Array.isArray(value)) {
      return
    }
    innerSelectedRowKeys.value = value
  },
  { immediate: true }
)

watch(
  () => rows.value,
  (value) => {
    dragRows.value = Array.isArray(value) ? [...value] : []
  },
  { immediate: true, deep: true }
)

watch(
  () => [displayRows.value, currentSelectedRowKeys.value, props.selection],
  async () => {
    await syncSelectionToTable()
    await syncDragRows()
    nextTick(queueLayoutSync)
  },
  { deep: true }
)

watch(
  () => [totalCount.value, safeCurrentPageSize.value, currentPageNo.value],
  () => {
    if (!props.draggable && currentPageNo.value !== safeCurrentPageNo.value) {
      emit('update:pageNo', safeCurrentPageNo.value)
    }
  }
)

onMounted(() => {
  queueLayoutSync()
  window.addEventListener('resize', queueLayoutSync)

  if (typeof ResizeObserver !== 'undefined') {
    wrapperObserver = new ResizeObserver(queueLayoutSync)
    footerObserver = new ResizeObserver(queueLayoutSync)

    if (wrapperRef.value) {
      wrapperObserver.observe(wrapperRef.value)
    }

    if (footerRef.value) {
      footerObserver.observe(footerRef.value)
    }
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', queueLayoutSync)
  cancelAnimationFrame(resizeFrame)
  wrapperObserver?.disconnect()
  footerObserver?.disconnect()
})
</script>

<style lang="scss" scoped>
.base-data-table {
  display: flex;
  width: 100%;
  min-height: 0;
  flex-direction: column;
  border: 1px solid #dbe4f2;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}

.base-data-table__inner {
  min-height: 0;
  flex: 1;
}

.base-data-table__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px;
  background: #fff;
  border-top: 1px solid #f4f4f4;
}

.base-data-table__meta {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #6b7a96;
  font-size: 13px;
}

.base-data-table__pager {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.base-data-table__page-size {
  width: 120px;
}

.base-data-table__drag-handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin: 0 auto;
  border-radius: 8px;
  cursor: grab;
  color: #8ca0c5;
  transition: background 0.2s ease;

  &:hover {
    background: #f0f5ff;
    color: #5477c7;
  }

  &:active {
    cursor: grabbing;
  }
}

.base-data-table__drag-icon {
  font-size: 17px;
  line-height: 1;
}

:deep(.base-data-table__table) {
  width: 100%;
  --el-table-border-color: #edf2fb;
  --el-table-header-bg-color: #f8fbff;
  --el-table-row-hover-bg-color: #f6f9ff;
}

:deep(.base-data-table__table th.el-table__cell) {
  height: var(--base-data-table-header-height);
  background: #f8fbff;
  color: #73829f;
  font-weight: 700;
}

:deep(.base-data-table__table th.el-table__cell .cell) {
  min-height: 40px;
  padding: 0 10px;
  line-height: 40px;
}

:deep(.base-data-table__table .el-table__cell) {
  padding: 0;
}

:deep(.base-data-table__table td.el-table__cell .cell) {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  color: #4c5f7d;
}

:deep(.base-data-table__row.is-drag-over td.el-table__cell) {
  background: #edf4ff !important;
}

:deep(.base-data-table__row.is-dragging td.el-table__cell) {
  opacity: 0.72;
}

:deep(.base-data-table__pager .el-pagination) {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 8px;
}

:deep(.base-data-table__pager .el-pagination__sizes) {
  margin: 0;
}

:deep(.base-data-table__pager .el-pagination__jump) {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  color: #606266;
  white-space: nowrap;
}

:deep(.base-data-table__pager .el-pagination__editor) {
  margin: 0 6px;
}

:deep(.base-data-table__pager .el-pagination__editor .el-input__wrapper) {
  min-width: 44px;
  padding: 0 8px;
}

:deep(.base-data-table__pager .el-select__wrapper) {
  min-width: 120px;
}

:deep(.el-table__row .el-table-column--selection .cell) {
  padding-left: 19px !important;
}

@media (max-width: 1200px) {
  .base-data-table__footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .base-data-table__pager {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
