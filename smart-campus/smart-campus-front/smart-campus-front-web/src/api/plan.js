import request from '@/api/request'

const buildStudyPlanSaveParams = (params = {}) => {
  const formParams = {}
  if (params.planId) {
    formParams.planId = params.planId
  }
  formParams.courseId = params.courseId || ''
  formParams.description = params.description || ''

  ;(params.itemList || []).forEach((item, index) => {
    formParams[`itemList[${index}].chapterId`] = item.chapterId || ''
    formParams[`itemList[${index}].lessonId`] = item.lessonId || ''
    formParams[`itemList[${index}].studyDate`] = item.studyDate || ''
    formParams[`itemList[${index}].startTime`] = item.startTime || ''
    formParams[`itemList[${index}].endTime`] = item.endTime || ''
  })

  return formParams
}

export const loadStudyPlanDashboard = () =>
  request.post('/studyPlan/loadDashboard', {}, {
    showLoading: false,
  })

export const getStudyPlanDetail = (planId) =>
  request.post('/studyPlan/getDetail', { planId }, {
    showLoading: false,
  })

export const saveStudyPlan = (params) =>
  request.post('/studyPlan/save', buildStudyPlanSaveParams(params), {
    showLoading: false,
  })

export const updateStudyPlanItemStatus = (params) =>
  request.post('/studyPlan/updateItemStatus', params, {
    showLoading: false,
  })

export const getPlannedLessonIds = (courseId, excludePlanId) =>
  request.post('/studyPlan/getPlannedLessonIds', { courseId, excludePlanId }, {
    showLoading: false,
  })
