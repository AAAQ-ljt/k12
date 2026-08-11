import request from '@/api/request'

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const normalizeRelationResourceId = (value) =>
  value == null || value === '' ? undefined : String(value)

const normalizeNumberValue = (value) =>
  value == null || value === '' ? undefined : Number(value)

const normalizeLesson = (item = {}, index = 0) => ({
  ...item,
  lessonId: item.lessonId ?? '',
  lessonName: item.lessonName ?? '',
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  videoResourceId: normalizeNumberValue(item.videoResourceId),
  videoResourceName: item.videoResourceName ?? '',
  videoFilePath: item.videoFilePath ?? '',
  videoCoverPath: item.videoCoverPath ?? '',
  paperId: item.paperId ?? '',
  paperName: item.paperName ?? '',
  paperType: item.paperType == null ? undefined : Number(item.paperType),
  paperTypeText: item.paperTypeText ?? '',
  coursewareList: Array.isArray(item.coursewareList)
    ? item.coursewareList.map((resource) => ({
        ...resource,
        resourceId: normalizeRelationResourceId(resource.resourceId),
        resourceType:
          resource.resourceType == null
            ? undefined
            : Number(resource.resourceType),
        resourceName: resource.resourceName ?? '',
        filePath: resource.filePath ?? '',
        coverPath: resource.coverPath ?? '',
      }))
    : [],
})

const normalizeChapter = (item = {}, index = 0) => ({
  ...item,
  chapterId: item.chapterId ?? '',
  chapterName: item.chapterName ?? '',
  description: item.description ?? '',
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  lessonList: Array.isArray(item.lessonList)
    ? item.lessonList.map((lesson, lessonIndex) =>
        normalizeLesson(lesson, lessonIndex)
      )
    : [],
})

export const normalizeCourseRecord = (item = {}) => ({
  ...item,
  id: item.courseId ?? item.id,
  courseId: item.courseId ?? item.id,
  coverResourceId:
    item.coverResourceId == null ? undefined : Number(item.coverResourceId),
  teacherId: item.teacherId == null ? undefined : Number(item.teacherId),
  recordStatus: item.recordStatus == null ? 0 : Number(item.recordStatus),
  status: item.status == null ? 1 : Number(item.status),
  classCount: Number(item.classCount ?? 0),
  chapterCount: Number(item.chapterCount ?? 0),
  lessonCount: Number(item.lessonCount ?? 0),
  classIdList: Array.isArray(item.classIdList)
    ? item.classIdList
        .map((classId) => Number(classId))
        .filter((classId) => !Number.isNaN(classId))
    : [],
  chapterList: Array.isArray(item.chapterList)
    ? item.chapterList.map((chapter, index) => normalizeChapter(chapter, index))
    : [],
})

const normalizeHomeworkQuestion = (item = {}, index = 0) => ({
  ...item,
  paperQuestionId:
    item.paperQuestionId == null ? undefined : Number(item.paperQuestionId),
  questionId: item.questionId == null ? undefined : Number(item.questionId),
  questionType: item.questionType == null ? undefined : Number(item.questionType),
  questionTitle: item.questionTitle ?? '',
  questionTypeText: item.questionTypeText ?? '',
  difficultyLevel:
    item.difficultyLevel == null ? undefined : Number(item.difficultyLevel),
  difficultyLevelText: item.difficultyLevelText ?? '',
  correctAnswerText: item.correctAnswerText ?? '',
  answerAnalysis: item.answerAnalysis ?? '',
  questionScore: Number(item.questionScore ?? 0),
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  answerContent: item.answerContent ?? '',
  finalScore: Number(item.finalScore ?? 0),
  judgeStatus: item.judgeStatus == null ? 0 : Number(item.judgeStatus),
  answered: Boolean(item.answered),
  optionList: Array.isArray(item.optionList) ? item.optionList : [],
})

const normalizeHomeworkSection = (item = {}, index = 0) => ({
  ...item,
  sectionId: item.sectionId == null ? undefined : Number(item.sectionId),
  sectionName: item.sectionName ?? '',
  sortOrder: item.sortOrder == null ? index + 1 : Number(item.sortOrder),
  totalScore: Number(item.totalScore ?? 0),
  questionList: Array.isArray(item.questionList)
    ? item.questionList.map((question, questionIndex) =>
        normalizeHomeworkQuestion(question, questionIndex)
      )
    : [],
})

const normalizeHomeworkSubmitItem = (item = {}) => ({
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
  studentNo: item.studentNo ?? '',
  studentName: item.studentName ?? '',
  className: item.className ?? '',
  chapterId: item.chapterId ?? '',
  chapterName: item.chapterName ?? '',
  lessonId: item.lessonId ?? '',
  lessonName: item.lessonName ?? '',
  paperId: item.paperId ?? '',
  paperName: item.paperName ?? '',
  submitStatusText: item.submitStatusText ?? '',
  judgeStatusText: item.judgeStatusText ?? '',
  startedTime: item.startedTime ?? '',
  submitTime: item.submitTime ?? '',
  judgeTime: item.judgeTime ?? '',
})

const normalizeHomeworkSubmitDetail = (item = {}) => ({
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
  questionCount: Number(item.questionCount ?? 0),
  answeredCount: Number(item.answeredCount ?? 0),
  canJudge: Boolean(item.canJudge),
  teacherComment: item.teacherComment ?? '',
  submitContent: item.submitContent ?? '',
  sectionList: Array.isArray(item.sectionList)
    ? item.sectionList.map((section, index) =>
        normalizeHomeworkSection(section, index)
      )
    : [],
})

export const getCourseList = async (params = {}) => {
  const result = await request.post('/courseInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    courseNameFuzzy: params.keyword,
    teacherId: params.teacherId,
    recordStatus: params.recordStatus,
    status: params.status,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeCourseRecord(item)),
  }
}

export const getCourseDetail = async (courseId) => {
  const result = await request.post('/courseInfo/getCourseInfoById', { courseId })
  return result ? normalizeCourseRecord(result) : null
}

export const saveCourse = async (payload = {}) => {
  const submitData = {
    courseId: payload.courseId ?? payload.id,
    courseName: payload.courseName,
    coverResourceId: payload.coverResourceId ?? undefined,
    description: payload.description,
    status: Number(payload.status ?? 1),
    classIdList: Array.isArray(payload.classIdList)
      ? payload.classIdList
          .map((classId) => Number(classId))
          .filter((classId) => !Number.isNaN(classId))
      : [],
  }

  const result = submitData.courseId
    ? await request.post('/courseInfo/updateCourseInfoById', submitData, {
        dataType: 'json',
      })
    : await request.post('/courseInfo/add', submitData, { dataType: 'json' })
  return result ?? true
}

export const finishCourseRecord = async (courseId) => {
  const result = await request.post('/courseInfo/finishRecord', { courseId })
  return result ?? true
}

export const saveCourseStructure = async (payload = {}) => {
  const result = await request.post(
    '/courseInfo/saveStructure',
    {
      courseId: payload.courseId,
      chapterList: Array.isArray(payload.chapterList)
        ? payload.chapterList.map((chapter, chapterIndex) => ({
            chapterId: chapter.chapterId || undefined,
            chapterName: chapter.chapterName,
            description: chapter.description,
            sortOrder:
              chapter.sortOrder == null
                ? chapterIndex + 1
                : Number(chapter.sortOrder),
            lessonList: Array.isArray(chapter.lessonList)
              ? chapter.lessonList.map((lesson, lessonIndex) => ({
                  lessonId: lesson.lessonId || undefined,
                  lessonName: lesson.lessonName,
                  sortOrder:
                    lesson.sortOrder == null
                      ? lessonIndex + 1
                      : Number(lesson.sortOrder),
                  videoResourceId: normalizeNumberValue(lesson.videoResourceId),
                  paperId: lesson.paperId || undefined,
                  coursewareResourceIdList: Array.isArray(lesson.coursewareList)
                    ? lesson.coursewareList
                        .map((resource) => Number(resource.resourceId))
                        .filter((resourceId) => !Number.isNaN(resourceId))
                    : [],
                }))
              : [],
          }))
        : [],
    },
    { dataType: 'json' }
  )
  return result ?? true
}

export const deleteCourses = async (ids = []) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/courseInfo/deleteBatch', {
    ids: ids.join(','),
  })
  return result ?? true
}

export const getCourseHomeworkSubmitList = async (params = {}) => {
  const result = await request.post('/courseInfo/loadHomeworkSubmitList', {
    courseId: params.courseId,
    classId: params.classId,
    chapterId: params.chapterId,
    lessonId: params.lessonId,
    keyword: params.keyword,
    submitStatus: params.submitStatus,
    judgeStatus: params.judgeStatus,
    pageNo: params.pageNo,
    pageSize: params.pageSize,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeHomeworkSubmitItem(item)),
  }
}

export const getCourseHomeworkSubmitDetail = async (params = {}) => {
  const result = await request.post('/courseInfo/getHomeworkSubmitDetail', {
    courseId: params.courseId,
    lessonId: params.lessonId,
    studentId: params.studentId,
  })
  return result ? normalizeHomeworkSubmitDetail(result) : null
}

export const judgeCourseHomeworkSubmit = async (payload = {}) => {
  const result = await request.post(
    '/courseInfo/judgeHomeworkSubmit',
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
