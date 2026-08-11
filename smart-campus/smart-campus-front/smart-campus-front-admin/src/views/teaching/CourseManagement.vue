<template>
  <div class="basic-page">
    <section class="toolbar-panel">
      <div class="toolbar-panel__top">
        <div class="toolbar-panel__filters">
          <el-form :model="filters" inline label-width="68px">
            <el-form-item label="课程信息">
              <el-input v-model="filters.keyword" placeholder="请输入课程名称" clearable @keyup.enter="handleSearch"/>
            </el-form-item>
            <el-form-item label="录制状态">
              <el-select v-model="filters.recordStatus" placeholder="请选择录制状态" clearable filterable
                         @change="handleSearch">
                <el-option label="录制中" :value="0"/>
                <el-option label="录制完成" :value="1"/>
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filters.status" placeholder="请选择状态" clearable filterable @change="handleSearch">
                <el-option label="正常" :value="1"/>
                <el-option label="停用" :value="0"/>
              </el-select>
            </el-form-item>
            <el-form-item class="toolbar-panel__search-actions">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="toolbar-panel__actions">
          <el-button-group>
            <el-button type="primary" @click="handleCreate">新增课程</el-button>
            <el-button type="danger" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
              批量删除
            </el-button>
          </el-button-group>
        </div>
      </div>
    </section>

    <BaseDataTable :columns="columns" :data="tableData" :header-height="40" selection
                   :selected-row-keys="selectedRowKeys" @update:selectedRowKeys="selectedRowKeys = $event"
                   @update:pageNo="pageNo = $event" @update:pageSize="pageSize = $event" :rowHeight="160">
      <template #cell-courseInfo="{ row }">
        <div class="course-info-cell">
          <div class="course-info-cell__cover">
            <img v-if="row.coverPath" :src="buildResourceFileUrl(row.coverPath)" :alt="row.courseName"/>
            <span v-else>课程</span>
          </div>
          <div class="info-cell">
            <strong>{{ row.courseName }}</strong>
            <span>{{ row.teacherName || authStore.displayName || '-' }}</span>
          </div>
        </div>
      </template>

      <template #cell-classInfo="{ row }">
        <div class="info-cell">
          <button type="button" class="class-count-btn" @click.stop="handleShowClassList(row)">
            {{ row.classCount || 0 }} 个班级
          </button>
        </div>
      </template>

      <template #cell-courseSummary="{ row }">
        <div class="info-cell">
          <strong>
            {{ row.chapterCount || 0 }} 个章节 / {{ row.lessonCount || 0 }} 个课时
          </strong>
          <span :title="row.description || '-'">{{ row.description || '-' }}</span>
        </div>
      </template>

      <template #cell-recordStatus="{ row }">
        <span :class="['status-tag', getRecordStatusClass(row.recordStatus)]">
          {{ getRecordStatusText(row.recordStatus) }}
        </span>
      </template>

      <template #cell-status="{ row }">
        <span :class="['status-tag', getStatusClass(row.status)]">
          {{ getStatusText(row.status) }}
        </span>
      </template>

      <template #cell-actions="{ row }">
        <div class="action-group">
          <el-button v-if="Number(row.recordStatus) === 0" link type="success" @click.stop="handleFinishRecord(row)">
            录制完成
          </el-button>
          <el-button link type="primary" @click.stop="handleManageChapter(row)">
            {{ Number(row.recordStatus) === 0 ? '章节管理' : '查看章节' }}
          </el-button>
          <el-button link type="primary" @click.stop="handleManageHomework(row)">
            学生作业
          </el-button>
          <el-button link @click.stop="handleView(row)">查看</el-button>
          <el-button link type="primary" @click.stop="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
        </div>
      </template>
    </BaseDataTable>

    <CourseFormDialog v-model:show="dialogVisible" :mode="dialogMode" :model="currentRecord"
                      :class-options="classOptions" :current-teacher-name="authStore.displayName" :readonly="dialogMode === 'view'"
                      @submit="handleSubmit"/>

    <CourseChapterDialog v-model:show="chapterDialogVisible" :course-id="currentChapterCourse.courseId"
                         :course-name="currentChapterCourse.courseName"
                         :teacher-name="currentChapterCourse.teacherName || authStore.displayName"
                         :readonly="Number(currentChapterCourse.recordStatus) === 1" @saved="handleChapterSaved"/>

    <CourseHomeworkManagement v-if="homeworkDrawerVisible" v-model:show="homeworkDrawerVisible"
                              :course-id="currentHomeworkCourse.courseId || ''" :class-id="currentHomeworkCourse.classId"
                              @update:show="handleHomeworkDrawerToggle"/>

    <BaseDialog v-model:show="classListDialogVisible" title="班级列表" width="520px" :buttons="[]" :show-cancel="false">
      <div class="class-list-dialog">
        <div class="class-list-dialog__title">
          {{ currentClassCourseName || '当前课程' }}
        </div>
        <div v-if="!currentClassList.length" class="class-list-dialog__empty">
          暂无班级数据
        </div>
        <div v-else class="class-list-dialog__list">
          <div v-for="(className, index) in currentClassList" :key="`${className}-${index}`"
               class="class-list-dialog__item">
            {{ className }}
          </div>
        </div>
      </div>
    </BaseDialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {useAuthStore} from '@/stores/auth'
import BaseDialog from '@/components/BaseDialog.vue'
import BaseDataTable from '@/components/BaseDataTable.vue'
import {getClassSortList} from '@/api/basicData'
import {deleteCourses, finishCourseRecord, getCourseDetail, getCourseList, saveCourse,} from '@/api/course'
import {buildResourceFileUrl} from '@/utils/resource'
import CourseChapterDialog from '@/views/teaching/components/CourseChapterDialog.vue'
import CourseFormDialog from '@/views/teaching/components/CourseFormDialog.vue'
import CourseHomeworkManagement from '@/views/teaching/components/CourseHomeworkManagement.vue'

const pageNo = ref(1)
const pageSize = ref(15)
const authStore = useAuthStore()
const selectedRowKeys = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentRecord = ref({})
const chapterDialogVisible = ref(false)
const currentChapterCourse = ref({})
const homeworkDrawerVisible = ref(false)
const currentHomeworkCourse = ref({})
const classListDialogVisible = ref(false)
const currentClassCourseName = ref('')
const currentClassList = ref([])
const tableData = ref({totalCount: 0, pageNo: 1, pageSize: 15, list: []})
const classOptions = ref([])

const filters = reactive({
  keyword: '',
  recordStatus: undefined,
  status: undefined,
})

const columns = [
  {
    key: 'courseInfo',
    label: '课程信息',
    width: 500,
    slot: 'cell-courseInfo',
  },
  {
    key: 'classInfo',
    label: '授课班级',
    width: 100,
    slot: 'cell-classInfo',
  },
  {
    key: 'courseSummary',
    label: '课程概览',
    slot: 'cell-courseSummary',
  },
  {
    key: 'recordStatus',
    label: '录制状态',
    width: 120,
    align: 'center',
    slot: 'cell-recordStatus',
  },
  {
    key: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    slot: 'cell-status',
  },
  {
    key: 'updateTime',
    prop: 'updateTime',
    label: '更新时间',
    width: 160,
  },
  {
    key: 'actions',
    label: '操作',
    width: 420,
    align: 'center',
    slot: 'cell-actions',
    fixed: 'right',
  },
]

const loadTableData = async () => {
  tableData.value =
      (await getCourseList({
        ...filters,
        pageNo: pageNo.value,
        pageSize: pageSize.value,
      })) || tableData.value
}

const loadOptions = async () => {
  const [classList] = await Promise.all([getClassSortList({status: 1})])

  classOptions.value = (classList || []).map((item) => ({
    value: item.classId,
    label: `${item.className} / ${item.majorName || '-'} / ${
        item.departmentName || '-'
    }`,
  }))
}

const getStatusText = (status) => (Number(status) === 1 ? '正常' : '停用')
const getStatusClass = (status) =>
    Number(status) === 1 ? 'enabled' : 'disabled'
const getRecordStatusText = (status) =>
    Number(status) === 0 ? '录制中' : '录制完成'
const getRecordStatusClass = (status) =>
    Number(status) === 0 ? 'processing' : 'enabled'

const buildClassNameList = (row = {}) => {
  const classIdSet = new Set(
      Array.isArray(row.classIdList)
          ? row.classIdList
              .map((item) => Number(item))
              .filter((item) => !Number.isNaN(item))
          : []
  )
  const optionLabels = classOptions.value
      .filter((item) => classIdSet.has(Number(item.value)))
      .map((item) => item.label)

  if (optionLabels.length) {
    return optionLabels
  }

  return String(row.classNames || '')
      .split('、')
      .map((item) => item.trim())
      .filter(Boolean)
}

watch([pageNo, pageSize], loadTableData)

const handleSearch = async () => {
  pageNo.value = 1
  await loadTableData()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  currentRecord.value = {}
  dialogVisible.value = true
}

const openDetailDialog = async (row, mode) => {
  const detail = await getCourseDetail(row.courseId)
  if (!detail) {
    return
  }
  dialogMode.value = mode
  currentRecord.value = detail
  dialogVisible.value = true
}

const handleView = (row) => openDetailDialog(row, 'view')
const handleEdit = (row) => openDetailDialog(row, 'edit')
const handleManageChapter = (row) => {
  currentChapterCourse.value = {...row}
  chapterDialogVisible.value = true
}

const handleManageHomework = (row) => {
  currentHomeworkCourse.value = {
    ...row,
    classId: Array.isArray(row.classIdList) ? row.classIdList[0] : undefined,
  }
  homeworkDrawerVisible.value = true
}

const handleShowClassList = (row) => {
  currentClassCourseName.value = row.courseName || ''
  currentClassList.value = buildClassNameList(row)
  classListDialogVisible.value = true
}

const handleHomeworkDrawerToggle = (value) => {
  homeworkDrawerVisible.value = value
  if (!value) {
    currentHomeworkCourse.value = {}
  }
}

const handleFinishRecord = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定将课程“${row.courseName}”标记为录制完成吗？`,
        '录制完成确认',
        {
          type: 'warning',
        }
    )
  } catch {
    return
  }

  const result = await finishCourseRecord(row.courseId)
  if (!result) {
    return
  }

  ElMessage.success('课程已标记为录制完成')
  await loadTableData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除课程“${row.courseName}”吗？`,
        '删除确认',
        {
          type: 'warning',
        }
    )
  } catch {
    return
  }

  const result = await deleteCourses([row.courseId])
  if (result === null) {
    return
  }

  selectedRowKeys.value = selectedRowKeys.value.filter(
      (item) => item !== row.courseId
  )
  ElMessage.success('课程已删除')
  await loadTableData()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
        `确定删除已选中的 ${selectedRowKeys.value.length} 门课程吗？`,
        '批量删除确认',
        {type: 'warning'}
    )
  } catch {
    return
  }

  const result = await deleteCourses(selectedRowKeys.value)
  if (result === null) {
    return
  }

  selectedRowKeys.value = []
  ElMessage.success('批量删除成功')
  await loadTableData()
}

const handleSubmit = async (payload) => {
  const result = await saveCourse(payload)
  if (!result) {
    return
  }
  dialogVisible.value = false
  ElMessage.success(
      dialogMode.value === 'create' ? '课程创建成功' : '课程信息已更新'
  )
  pageNo.value = 1
  await loadTableData()
}

const handleChapterSaved = async () => {
  await loadTableData()
}

onMounted(async () => {
  await loadOptions()
  await loadTableData()
})
</script>

<style lang="scss" scoped src="@/assets/styles/basic-data.scss"></style>
<style lang="scss" scoped>
.course-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-info-cell__cover {
  display: flex;
  height: 188px;
  width: 300px;
  flex: 0 0 300px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #dbe5f3;
  border-radius: 6px;
  background: linear-gradient(135deg, #f0f5ff 0%, #f9fbff 100%);
  color: #6f83ab;
  font-size: 12px;
  font-weight: 600;

  img {
    height: 100%;
    width: 100%;
    object-fit: cover;
  }
}

:deep(.status-tag.processing) {
  color: #d17c18;
  background: #fff5e8;
}

.class-count-btn {
  padding: 0;
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 14px;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.class-list-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.class-list-dialog__title {
  color: #24304a;
  font-size: 14px;
  font-weight: 600;
}

.class-list-dialog__empty {
  color: #909399;
  font-size: 13px;
}

.class-list-dialog__list {
  display: flex;
  max-height: 360px;
  flex-direction: column;
  gap: 10px;
  overflow: auto;
}

.class-list-dialog__item {
  padding: 10px 12px;
  border: 1px solid #e5ebf5;
  border-radius: 8px;
  background: #f8fbff;
  color: #24304a;
  font-size: 13px;
  line-height: 1.5;
}
</style>
