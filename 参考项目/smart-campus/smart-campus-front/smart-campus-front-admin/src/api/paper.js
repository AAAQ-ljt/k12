import request from '@/api/request'

export const PAPER_TYPE_OPTIONS = [
  { label: '课后习题', value: 1 },
  { label: '考试试卷', value: 2 },
]

const PAPER_TYPE_TEXT_MAP = PAPER_TYPE_OPTIONS.reduce((map, item) => {
  map[item.value] = item.label
  return map
}, {})

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

const normalizeQuestionItem = (item = {}) => ({
  id: item.id == null ? undefined : Number(item.id),
  questionId: item.questionId == null ? undefined : Number(item.questionId),
  questionType: Number(item.questionType ?? 0),
  questionTypeText: item.questionTypeText || '未知',
  questionTitle: item.questionTitle || '',
  difficultyLevel: Number(item.difficultyLevel ?? 0),
  difficultyLevelText: item.difficultyLevelText || '未知',
  questionScore: Number(item.questionScore ?? 0),
  sortOrder: Number(item.sortOrder ?? 0),
  questionImageResourceIdList: Array.isArray(item.questionImageResourceIdList)
    ? item.questionImageResourceIdList
        .map((value) => Number(value))
        .filter((value) => !Number.isNaN(value))
    : [],
  correctAnswerText: item.correctAnswerText || '',
  answerAnalysis: item.answerAnalysis || '',
  optionList: Array.isArray(item.optionList)
    ? item.optionList.map((option, index) =>
        normalizeQuestionOption(option, index)
      )
    : [],
})

const normalizeSectionItem = (item = {}, index = 0) => ({
  id: item.id == null ? undefined : Number(item.id),
  sectionName: item.sectionName || '',
  sortOrder: Number(item.sortOrder ?? index + 1),
  questionList: Array.isArray(item.questionList)
    ? item.questionList.map((question, questionIndex) => ({
        ...normalizeQuestionItem(question),
        sortOrder: Number(question.sortOrder ?? questionIndex + 1),
      }))
    : [],
})

export const normalizePaperDetail = (item = {}) => ({
  paperId: item.paperId || '',
  paperName: item.paperName || '',
  paperType: Number(item.paperType ?? 1),
  paperTypeText:
    item.paperTypeText || PAPER_TYPE_TEXT_MAP[Number(item.paperType)] || '未知',
  description: item.description || '',
  totalScore: Number(item.totalScore ?? 0),
  questionCount: Number(item.questionCount ?? 0),
  sectionCount: Number(item.sectionCount ?? 0),
  sectionList: Array.isArray(item.sectionList)
    ? item.sectionList.map((section, index) =>
        normalizeSectionItem(section, index)
      )
    : [],
  createTime: item.createTime || '',
  updateTime: item.updateTime || '',
})

export const getPaperTypeText = (value) =>
  PAPER_TYPE_TEXT_MAP[Number(value)] || '未知'

export const getPaperList = async (params = {}) => {
  const result = await request.post('/paperInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    paperNameFuzzy: params.keyword,
    paperType: params.paperType,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => ({
      ...item,
      paperId: item.paperId || '',
      paperType: Number(item.paperType ?? 1),
      paperTypeText: item.paperTypeText || getPaperTypeText(item.paperType),
      totalScore: Number(item.totalScore ?? 0),
      questionCount: Number(item.questionCount ?? 0),
      sectionCount: Number(item.sectionCount ?? 0),
      updateTime: item.updateTime || '',
    })),
  }
}

export const getPaperDetail = async (paperId) => {
  const result = await request.post('/paperInfo/getPaperInfoById', { paperId })
  return result ? normalizePaperDetail(result) : null
}

export const savePaper = async (payload = {}) => {
  const url = payload.paperId
    ? '/paperInfo/updatePaperInfoById'
    : '/paperInfo/add'
  const result = await request.post(url, payload, { dataType: 'json' })
  return result ?? true
}

export const savePaperStructure = async (payload = {}) => {
  const result = await request.post('/paperInfo/saveStructure', payload, {
    dataType: 'json',
  })
  return result ?? true
}

export const deletePapers = async (ids = []) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/paperInfo/deleteBatch', {
    ids: ids.join(','),
  })
  return result ?? true
}
