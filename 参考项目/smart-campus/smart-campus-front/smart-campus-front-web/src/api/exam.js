import request from '@/api/request'

const resolveQuestionTypeText = (questionType) => {
  if (Number(questionType) === 1) return '单选题'
  if (Number(questionType) === 2) return '多选题'
  if (Number(questionType) === 3) return '判断题'
  if (Number(questionType) === 4) return '简答题'
  return '题目'
}

const normalizeQuestion = (item = {}, index = 0) => ({
  ...item,
  paperQuestionId: item.paperQuestionId == null ? undefined : Number(item.paperQuestionId),
  questionId: item.questionId == null ? undefined : Number(item.questionId),
  questionType: Number(item.questionType ?? 0),
  questionTitle: item.questionTitle || '',
  questionTypeText: item.questionTypeText || resolveQuestionTypeText(item.questionType),
  questionScore: Number(item.questionScore ?? 0),
  answerContent: item.answerContent || '',
  finalScore: Number(item.finalScore ?? 0),
  judgeStatus: Number(item.judgeStatus ?? 0),
  answered: Boolean(item.answered),
  sortOrder: Number(item.sortOrder ?? index + 1),
  optionList: Array.isArray(item.optionList)
    ? item.optionList.map((option = {}, optionIndex = 0) => ({
        ...option,
        optionId: option.optionId == null ? undefined : String(option.optionId),
        optionKey: option.optionKey || String.fromCharCode(65 + optionIndex),
        optionContent: option.optionContent || '',
      }))
    : [],
})

const normalizeSection = (item = {}, index = 0) => ({
  ...item,
  sectionId: item.sectionId == null ? undefined : Number(item.sectionId),
  sectionName: item.sectionName || '',
  totalScore: Number(item.totalScore ?? 0),
  sortOrder: Number(item.sortOrder ?? index + 1),
  questionList: Array.isArray(item.questionList)
    ? item.questionList.map((question, questionIndex) =>
        normalizeQuestion(question, questionIndex)
      )
    : [],
})

const normalizeExamItem = (item = {}) => ({
  ...item,
  examId: item.examId || '',
  examName: item.examName || '',
  courseId: item.courseId || '',
  courseName: item.courseName || '',
  paperId: item.paperId || '',
  paperName: item.paperName || '',
  submitStatus: Number(item.submitStatus ?? 0),
  judgeStatus: Number(item.judgeStatus ?? 0),
  examStatusText: item.examStatusText || '',
  submitStatusText: item.submitStatusText || '',
  judgeStatusText: item.judgeStatusText || '',
  started: Boolean(item.started),
  submitted: Boolean(item.submitted),
  totalScore: Number(item.totalScore ?? 0),
  finalScore: Number(item.finalScore ?? 0),
  startTime: item.startTime || '',
  endTime: item.endTime || '',
})

const normalizeExamDetail = (item = {}) => ({
  ...item,
  examId: item.examId || '',
  examName: item.examName || '',
  courseId: item.courseId || '',
  courseName: item.courseName || '',
  paperId: item.paperId || '',
  paperName: item.paperName || '',
  remainingSeconds: Number(item.remainingSeconds ?? 0),
  totalScore: Number(item.totalScore ?? 0),
  questionCount: Number(item.questionCount ?? 0),
  answeredCount: Number(item.answeredCount ?? 0),
  submitId: item.submitId == null ? undefined : Number(item.submitId),
  submitStatus: Number(item.submitStatus ?? 0),
  submitStatusText: item.submitStatusText || '',
  judgeStatus: Number(item.judgeStatus ?? 0),
  judgeStatusText: item.judgeStatusText || '',
  usedSeconds: Number(item.usedSeconds ?? 0),
  submitContent: item.submitContent || '',
  teacherComment: item.teacherComment || '',
  editable: Boolean(item.editable),
  started: Boolean(item.started),
  submitted: Boolean(item.submitted),
  examStatusText: item.examStatusText || '',
  startTime: item.startTime || '',
  endTime: item.endTime || '',
  startedTime: item.startedTime || '',
  submitTime: item.submitTime || '',
  sectionList: Array.isArray(item.sectionList)
    ? item.sectionList.map((section, index) => normalizeSection(section, index))
    : [],
})

export const loadMyExamList = () =>
  request.post('/examInfo/loadMyExamList', {}, { showLoading: false })

export const getCourseExamDetail = (params) =>
  request.post('/courseExam/getDetail', params, { showLoading: false })

export const startCourseExam = (params) =>
  request.post('/courseExam/start', params, { showLoading: false })

export const saveCourseExamAnswer = (params) =>
  request.post('/courseExam/saveAnswer', params, {
    showLoading: false,
    showError: false,
  })

export const saveCourseExamDraft = (params) =>
  request.post('/courseExam/saveDraft', params, { showLoading: false })

export const submitCourseExam = (params) =>
  request.post('/courseExam/submit', params, { showLoading: false })

export { normalizeExamItem, normalizeExamDetail }
