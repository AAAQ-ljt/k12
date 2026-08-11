import request from '@/api/request'

export const QUESTION_TYPE_OPTIONS = [
  { label: '单选题', value: 1 },
  { label: '多选题', value: 2 },
  { label: '判断题', value: 3 },
  { label: '简答题', value: 4 },
]

export const DIFFICULTY_LEVEL_OPTIONS = [
  { label: '简单', value: 1 },
  { label: '较易', value: 2 },
  { label: '中等', value: 3 },
  { label: '较难', value: 4 },
  { label: '困难', value: 5 },
]

export const QUESTION_TYPE_JUDGE = 3
export const QUESTION_TYPE_ESSAY = 4
export const QUESTION_TYPE_SINGLE = 1
export const QUESTION_TYPE_MULTI = 2

const QUESTION_TYPE_TEXT_MAP = QUESTION_TYPE_OPTIONS.reduce((map, item) => {
  map[item.value] = item.label
  return map
}, {})

const DIFFICULTY_LEVEL_TEXT_MAP = DIFFICULTY_LEVEL_OPTIONS.reduce(
  (map, item) => {
    map[item.value] = item.label
    return map
  },
  {}
)

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const normalizeQuestionOption = (item = {}, index = 0) => ({
  optionId: item.optionId == null ? undefined : Number(item.optionId),
  optionKey: item.optionKey || String.fromCharCode(65 + index),
  optionContent: item.optionContent || '',
  sortOrder: Number(item.sortOrder ?? index + 1),
})

export const normalizeQuestionDetail = (item = {}) => ({
  questionId: item.questionId == null ? undefined : Number(item.questionId),
  questionType: Number(item.questionType ?? 1),
  questionTitle: item.questionTitle || '',
  questionImageResourceIdList: Array.isArray(item.questionImageResourceIdList)
    ? item.questionImageResourceIdList
        .map((value) => Number(value))
        .filter((value) => !Number.isNaN(value))
    : [],
  difficultyLevel: Number(item.difficultyLevel ?? 3),
  correctAnswerText:
    Number(item.questionType ?? 1) === 3
      ? String(item.correctAnswerText || '').toUpperCase()
      : item.correctAnswerText || '',
  correctOptionKeyList: Array.isArray(item.correctOptionKeyList)
    ? item.correctOptionKeyList.filter(Boolean)
    : [],
  answerAnalysis: item.answerAnalysis || '',
  optionList: Array.isArray(item.optionList)
    ? item.optionList.map((option, index) =>
        normalizeQuestionOption(option, index)
      )
    : [],
  createTime: item.createTime || '',
  updateTime: item.updateTime || '',
})

export const getQuestionTypeText = (value) =>
  QUESTION_TYPE_TEXT_MAP[Number(value)] || '未知'

export const getDifficultyLevelText = (value) =>
  DIFFICULTY_LEVEL_TEXT_MAP[Number(value)] || '未知'

export const formatQuestionAnswerText = (questionType, correctAnswerText) => {
  const answerText = String(correctAnswerText || '').trim()
  if (!answerText) {
    return ''
  }
  if (Number(questionType) === QUESTION_TYPE_JUDGE) {
    if (answerText.toUpperCase() === 'T') {
      return '正确'
    }
    if (answerText.toUpperCase() === 'F') {
      return '错误'
    }
  }
  return answerText
}

export const getQuestionList = async (params = {}) => {
  const result = await request.post('/questionInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    questionTitleFuzzy: params.keyword,
    questionType: params.questionType,
    difficultyLevel: params.difficultyLevel,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => ({
      ...item,
      questionId:
        item.questionId == null ? undefined : Number(item.questionId),
      questionType: Number(item.questionType ?? 1),
      questionTypeText:
        item.questionTypeText || getQuestionTypeText(item.questionType),
      difficultyLevel: Number(item.difficultyLevel ?? 3),
      difficultyLevelText:
        item.difficultyLevelText ||
        getDifficultyLevelText(item.difficultyLevel),
      imageCount: Number(item.imageCount ?? 0),
      optionCount: Number(item.optionCount ?? 0),
      correctAnswerText: item.correctAnswerText || '',
      optionList: Array.isArray(item.optionList)
        ? item.optionList.map((option, index) =>
            normalizeQuestionOption(option, index)
          )
        : [],
      answerDisplayText: formatQuestionAnswerText(
        item.questionType,
        item.correctAnswerText
      ),
      updateTime: item.updateTime || '',
    })),
  }
}

export const getQuestionDetail = async (questionId) => {
  const result = await request.post('/questionInfo/getQuestionInfoById', {
    questionId,
  })
  return result ? normalizeQuestionDetail(result) : null
}

export const saveQuestion = async (payload = {}) => {
  const url = payload.questionId
    ? '/questionInfo/updateQuestionInfoById'
    : '/questionInfo/add'
  const result = await request.post(url, payload, { dataType: 'json' })
  return result ?? true
}

export const deleteQuestions = async (ids = []) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/questionInfo/deleteBatch', {
    ids: ids.join(','),
  })
  return result ?? true
}
