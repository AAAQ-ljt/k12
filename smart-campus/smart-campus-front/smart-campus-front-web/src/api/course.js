import request from '@/api/request'

export const loadMyCourseList = () =>
  request.post('/courseInfo/loadMyCourseList', {}, {
    showLoading: false,
  })

export const getMyCourseDetail = (courseId) =>
  request.post('/courseInfo/getMyCourseDetail', { courseId }, {
    showLoading: false,
  })

export const reportStudyProgress = (params) =>
  request.post('/courseInfo/reportStudyProgress', params, {
    showLoading: false,
    showError: false,
  })

export const saveCourseCollection = (params) =>
  request.post('/courseInfo/saveCollection', params, {
    showLoading: false,
  })

export const getCourseHomeworkDetail = (params) =>
  request.post('/courseHomework/getDetail', params, {
    showLoading: false,
  })

export const startCourseHomework = (params) =>
  request.post('/courseHomework/start', params, {
    showLoading: false,
  })

export const saveCourseHomeworkAnswer = (params) =>
  request.post('/courseHomework/saveAnswer', params, {
    showLoading: false,
    showError: false,
  })

export const saveCourseHomeworkDraft = (params) =>
  request.post('/courseHomework/saveDraft', params, {
    showLoading: false,
  })

export const submitCourseHomework = (params) =>
  request.post('/courseHomework/submit', params, {
    showLoading: false,
  })
