import request from '@/api/request'

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const normalizeExamRecord = (item = {}) => ({
  ...item,
  examId: item.examId || '',
  examName: item.examName || '',
  courseId: item.courseId || '',
  courseName: item.courseName || '',
  paperId: item.paperId || '',
  paperName: item.paperName || '',
  teacherId: item.teacherId == null ? undefined : Number(item.teacherId),
  teacherName: item.teacherName || '',
  status: Number(item.status ?? 0),
  statusText: item.statusText || (Number(item.status) === 1 ? '已发布' : '草稿'),
  classNames: item.classNames || '',
  classIdList: Array.isArray(item.classIdList)
    ? item.classIdList
        .map((classId) => Number(classId))
        .filter((classId) => !Number.isNaN(classId))
    : [],
  startTime: item.startTime || '',
  endTime: item.endTime || '',
  updateTime: item.updateTime || '',
  description: item.description || '',
})

const normalizeExamSubmitClass = (item = {}) => ({
  ...item,
  classId: item.classId == null ? undefined : Number(item.classId),
  className: item.className || '',
  majorName: item.majorName || '',
  departmentName: item.departmentName || '',
  studentCount: Number(item.studentCount ?? 0),
  submittedCount: Number(item.submittedCount ?? 0),
  waitJudgeCount: Number(item.waitJudgeCount ?? 0),
})

const normalizeExamQuestion = (item = {}, index = 0) => ({
  ...item,
  paperQuestionId:
    item.paperQuestionId == null ? undefined : Number(item.paperQuestionId),
  questionId: item.questionId == null ? undefined : Number(item.questionId),
  questionType: item.questionType == null ? undefined : Number(item.questionType),
  questionTitle: item.questionTitle || '',
  questionTypeText: item.questionTypeText || '',
  difficultyLevel:
    item.difficultyLevel == null ? undefined : Number(item.difficultyLevel),
  difficultyLevelText: item.difficultyLevelText || '',
  correctAnswerText: item.correctAnswerText || '',
  answerAnalysis: item.answerAnalysis || '',
  questionScore: Number(item.questionScore ?? 0),
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  answerContent: item.answerContent || '',
  finalScore: Number(item.finalScore ?? 0),
  judgeStatus: item.judgeStatus == null ? 0 : Number(item.judgeStatus),
  answered: Boolean(item.answered),
  optionList: Array.isArray(item.optionList) ? item.optionList : [],
})

const normalizeExamSection = (item = {}, index = 0) => ({
  ...item,
  sectionId: item.sectionId == null ? undefined : Number(item.sectionId),
  sectionName: item.sectionName || '',
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  totalScore: Number(item.totalScore ?? 0),
  questionList: Array.isArray(item.questionList)
    ? item.questionList.map((question, questionIndex) =>
        normalizeExamQuestion(question, questionIndex)
      )
    : [],
})

const normalizeExamSubmitItem = (item = {}) => ({
  ...item,
  studentId: item.studentId == null ? undefined : Number(item.studentId),
  classId: item.classId == null ? undefined : Number(item.classId),
  submitId: item.submitId == null ? undefined : Number(item.submitId),
  submitStatus: item.submitStatus == null ? 0 : Number(item.submitStatus),
  judgeStatus: item.judgeStatus == null ? 0 : Number(item.judgeStatus),
  usedSeconds: Number(item.usedSeconds ?? 0),
  totalScore: Number(item.totalScore ?? 0),
  objectiveScore: Number(item.objectiveScore ?? 0),
  subjectiveScore: Number(item.subjectiveScore ?? 0),
  finalScore: Number(item.finalScore ?? 0),
  studentNo: item.studentNo || '',
  studentName: item.studentName || '',
  className: item.className || '',
  lessonId: item.lessonId || '',
  lessonName: item.lessonName || '',
  paperId: item.paperId || '',
  paperName: item.paperName || '',
  submitStatusText: item.submitStatusText || '',
  judgeStatusText: item.judgeStatusText || '',
  startedTime: item.startedTime || '',
  submitTime: item.submitTime || '',
  judgeTime: item.judgeTime || '',
})

const normalizeExamSubmitDetail = (item = {}) => ({
  ...normalizeExamSubmitItem(item),
  questionCount: Number(item.questionCount ?? 0),
  answeredCount: Number(item.answeredCount ?? 0),
  canJudge: Boolean(item.canJudge),
  teacherComment: item.teacherComment || '',
  submitContent: item.submitContent || '',
  sectionList: Array.isArray(item.sectionList)
    ? item.sectionList.map((section, index) => normalizeExamSection(section, index))
    : [],
})

export const getExamList = async (params = {}) => {
  const result = await request.post('/examInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    examNameFuzzy: params.keyword,
    courseId: params.courseId,
    status: params.status,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeExamRecord(item)),
  }
}

export const getExamDetail = async (examId) => {
  const result = await request.post('/examInfo/getExamInfoById', { examId })
  return result ? normalizeExamRecord(result) : null
}

export const saveExam = async (payload = {}) => {
  const url = payload.examId ? '/examInfo/updateExamInfoById' : '/examInfo/add'
  const result = await request.post(
    url,
    {
      examId: payload.examId,
      examName: payload.examName,
      courseId: payload.courseId,
      paperId: payload.paperId,
      startTime: payload.startTime,
      endTime: payload.endTime,
      description: payload.description,
      classIdList: Array.isArray(payload.classIdList)
        ? payload.classIdList
            .map((classId) => Number(classId))
            .filter((classId) => !Number.isNaN(classId))
        : [],
    },
    { dataType: 'json' }
  )
  return result ?? true
}

export const publishExam = async (examId) => {
  const result = await request.post('/examInfo/publish', { examId })
  return result ?? true
}

export const deleteExams = async (ids = []) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/examInfo/deleteBatch', {
    ids: ids.join(','),
  })
  return result ?? true
}

export const getExamSubmitClassList = async (examId) => {
  const result = await request.post('/examInfo/loadExamSubmitClassList', {
    examId,
  })
  return Array.isArray(result) ? result.map(normalizeExamSubmitClass) : []
}

export const getExamSubmitList = async (params = {}) => {
  const result = await request.post('/examInfo/loadExamSubmitList', {
    examId: params.examId,
    classId: params.classId,
    keyword: params.keyword,
    submitStatus: params.submitStatus,
    judgeStatus: params.judgeStatus,
    pageNo: params.pageNo,
    pageSize: params.pageSize,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeExamSubmitItem(item)),
  }
}

export const getExamSubmitDetail = async (params = {}) => {
  const result = await request.post('/examInfo/getExamSubmitDetail', {
    examId: params.examId,
    studentId: params.studentId,
  })
  return result ? normalizeExamSubmitDetail(result) : null
}

export const judgeExamSubmit = async (payload = {}) => {
  const result = await request.post(
    '/examInfo/judgeExamSubmit',
    {
      submitId: payload.submitId,
      teacherComment: payload.teacherComment,
      questionScoreList: Array.isArray(payload.questionScoreList)
        ? payload.questionScoreList.map((item) => ({
            questionId: item.questionId,
            score: Number(item.score ?? 0),
          }))
        : [],
    },
    { dataType: 'json' }
  )
  return result ?? true
}
