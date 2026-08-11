import request from '@/api/request'

let optionsCache = null
let majorDictionaryCache = null
let classDictionaryCache = null

const normalizePageResult = (result) => ({
  totalCount: Number(result?.totalCount ?? 0),
  pageNo: Number(result?.pageNo ?? 1),
  pageSize: Number(result?.pageSize ?? 15),
  list: Array.isArray(result?.list) ? result.list : [],
})

const buildOptionMap = (options = []) =>
  options.reduce((map, item) => {
    map[String(item.value)] = item.label
    return map
  }, {})

const normalizeOptions = (result = {}) => ({
  statusTextMap: result.statusTextMap || { 1: '启用', 0: '停用' },
  genderTextMap: result.genderTextMap || { 0: '未知', 1: '男', 2: '女' },
  departmentOptions: result.departmentOptions || [],
  majorOptions: result.majorOptions || [],
  classOptions: result.classOptions || [],
  gradeOptions: result.gradeOptions || [],
  teacherTitleOptions: result.teacherTitleOptions || [],
})

const loadOptions = async (force = false) => {
  if (!force && optionsCache) {
    return optionsCache
  }
  const result = await request.post('/basicData/getOptions', {}, { showLoading: false })
  optionsCache = normalizeOptions(result)
  return optionsCache
}

const loadMajorDictionary = async (force = false) => {
  if (!force && majorDictionaryCache) {
    return majorDictionaryCache
  }
  const result = await request.post(
    '/majorInfo/loadDataList',
    { pageNo: 1, pageSize: 500 },
    { showLoading: false },
  )
  const pageData = normalizePageResult(result)
  majorDictionaryCache = pageData.list.reduce((map, item) => {
    const normalizedItem = normalizeMajorRecord(item)
    map[String(normalizedItem.id)] = normalizedItem
    return map
  }, {})
  return majorDictionaryCache
}

const loadClassDictionary = async (force = false) => {
  if (!force && classDictionaryCache) {
    return classDictionaryCache
  }
  const result = await request.post(
    '/classInfo/loadDataList',
    { pageNo: 1, pageSize: 500 },
    { showLoading: false },
  )
  const pageData = normalizePageResult(result)
  classDictionaryCache = pageData.list.reduce((map, item) => {
    const normalizedItem = normalizeClassRecord(item)
    map[String(normalizedItem.id)] = normalizedItem
    return map
  }, {})
  return classDictionaryCache
}

const invalidateBasicDataCache = () => {
  optionsCache = null
  majorDictionaryCache = null
  classDictionaryCache = null
}

const downloadBlobFile = (blob, fileName) => {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const parseClassIds = (value) =>
  String(value ?? '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

const uniqueValues = (values = []) => [...new Set(values.filter((item) => item !== undefined && item !== null && item !== ''))]

const buildUserRelationInfo = (item, options, classDictionary) => {
  const departmentMap = buildOptionMap(options.departmentOptions)
  const majorMap = buildOptionMap(options.majorOptions)
  const genderMap = options.genderTextMap || {}

  const classIdList = parseClassIds(item.classId)
  const classItems = classIdList.map((classId) => classDictionary[classId]).filter(Boolean)
  const departmentIds = uniqueValues(classItems.map((classItem) => String(classItem.departmentId)))
  const majorIds = uniqueValues(classItems.map((classItem) => String(classItem.majorId)))

  const classNames = uniqueValues(classItems.map((classItem) => classItem.className))
  const departmentNames = departmentIds.map((id) => departmentMap[id]).filter(Boolean)
  const majorNames = majorIds.map((id) => majorMap[id]).filter(Boolean)

  const firstClass = classItems[0]

  return {
    ...item,
    id: item.userId ?? item.id,
    userId: item.userId ?? item.id,
    departmentId: firstClass?.departmentId ?? item.departmentId,
    majorId: firstClass?.majorId ?? item.majorId,
    classId: classIdList.join(','),
    classIdList: classIdList.map((id) => Number(id)).filter((id) => !Number.isNaN(id)),
    departmentName: departmentNames.join('、') || '-',
    majorName: majorNames.join('、') || '-',
    className: classNames.join('、') || '-',
    genderText: genderMap[item.gender] || genderMap[0] || '未知',
  }
}

const buildStudentSubmitData = async (payload = {}) => {
  const classDictionary = await loadClassDictionary()
  const classId = String(payload.classId ?? '').trim()
  const classInfo = classDictionary[classId] || {}
  return {
    ...payload,
    id: undefined,
    userId: payload.userId ?? payload.id,
    departmentId: classInfo.departmentId ?? payload.departmentId,
    majorId: classInfo.majorId ?? payload.majorId,
    classId,
    gender: Number(payload.gender),
    status: Number(payload.status),
  }
}

const buildTeacherSubmitData = (payload = {}) => {
  const classIdList = Array.isArray(payload.classIdList)
    ? payload.classIdList
    : parseClassIds(payload.classId)
  return {
    ...payload,
    id: undefined,
    userId: payload.userId ?? payload.id,
    classId: classIdList
      .map((item) => String(item).trim())
      .filter(Boolean)
      .join(','),
    gender: Number(payload.gender),
    status: Number(payload.status),
  }
}

export const getBasicDataOptions = async () => loadOptions()

const normalizeDepartmentRecord = (item = {}) => ({
  ...item,
  id: item.departmentId ?? item.id,
  departmentId: item.departmentId ?? item.id,
})

const normalizeMajorRecord = (item = {}) => ({
  ...item,
  id: item.majorId ?? item.id,
  majorId: item.majorId ?? item.id,
})

const normalizeClassRecord = (item = {}) => ({
  ...item,
  id: item.classId ?? item.id,
  classId: item.classId ?? item.id,
})

const buildDepartmentSubmitData = (payload = {}) => {
  const departmentId = payload.departmentId ?? payload.id
  return {
    ...payload,
    id: undefined,
    departmentId,
  }
}

export const getDepartmentList = async (params = {}) => {
  const result = await request.post('/departmentInfo/loadDataList', {
    pageNo: params.pageNo,
    pageSize: params.pageSize,
    keyword: params.keyword,
    leaderName: params.leaderName ?? params.leader,
    status: params.status,
  })
  const pageData = normalizePageResult(result)
  return {
    ...pageData,
    list: pageData.list.map((item) => normalizeDepartmentRecord(item)),
  }
}

export const getDepartmentSortList = async (params = {}) => {
  const result = await request.post('/departmentInfo/loadSortList', {
    keyword: params.keyword,
    leaderName: params.leaderName ?? params.leader,
    status: params.status,
  })
  return (Array.isArray(result) ? result : []).map((item) => normalizeDepartmentRecord(item))
}

export const getDepartmentDetail = async (departmentId) => {
  const result = await request.post('/departmentInfo/getDepartmentInfoById', { departmentId })
  return result ? normalizeDepartmentRecord(result) : null
}

export const saveDepartment = async (payload) => {
  const submitData = buildDepartmentSubmitData(payload)
  const result = submitData.departmentId
    ? await request.post('/departmentInfo/updateDepartmentInfoById', submitData)
    : await request.post('/departmentInfo/add', submitData)
  invalidateBasicDataCache()
  return result ?? true
}

export const deleteDepartments = async (ids) => {
  const result = await request.post('/departmentInfo/deleteBatch', { ids: ids.join(',') })
  invalidateBasicDataCache()
  return result
}

export const updateDepartmentSortOrder = async (ids = []) => {
  if (!Array.isArray(ids) || ids.length < 2) {
    return null
  }
  return request.post('/departmentInfo/updateSortOrder', { ids: ids.join(',') }, { showLoading: false })
}

export const getMajorList = async (params = {}) => {
  const [result, options] = await Promise.all([
    request.post('/majorInfo/loadDataList', {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      departmentId: params.departmentId,
      educationalSystemType: params.educationalSystemType,
      majorNameFuzzy: params.keyword,
      majorCodeFuzzy: params.keyword,
      status: params.status,
    }),
    loadOptions(),
  ])

  const pageData = normalizePageResult(result)
  const departmentMap = buildOptionMap(options.departmentOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)

  return {
    ...pageData,
    list: pageData.list.map((item) => {
      const normalizedItem = normalizeMajorRecord(item)
      return {
        ...normalizedItem,
        departmentName: departmentMap[String(normalizedItem.departmentId)] || '-',
        educationalSystemTypeText: gradeMap[String(normalizedItem.educationalSystemType)] || '-',
      }
    }),
  }
}

export const getMajorSortList = async (params = {}) => {
  const [result, options] = await Promise.all([
    request.post('/majorInfo/loadSortList', {
      departmentId: params.departmentId,
      educationalSystemType: params.educationalSystemType,
      majorNameFuzzy: params.keyword,
      majorCodeFuzzy: params.keyword,
      status: params.status,
    }),
    loadOptions(),
  ])
  const departmentMap = buildOptionMap(options.departmentOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)
  return (Array.isArray(result) ? result : []).map((item) => {
    const normalizedItem = normalizeMajorRecord(item)
    return {
      ...normalizedItem,
      departmentName: departmentMap[String(normalizedItem.departmentId)] || '-',
      educationalSystemTypeText: gradeMap[String(normalizedItem.educationalSystemType)] || '-',
    }
  })
}

export const getMajorDetail = async (majorId) => {
  const [result, options] = await Promise.all([
    request.post('/majorInfo/getMajorInfoById', { majorId }),
    loadOptions(),
  ])
  const departmentMap = buildOptionMap(options.departmentOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)
  if (!result) {
    return null
  }
  const normalizedItem = normalizeMajorRecord(result)
  return {
    ...normalizedItem,
    departmentName: departmentMap[String(normalizedItem.departmentId)] || '-',
    educationalSystemTypeText: gradeMap[String(normalizedItem.educationalSystemType)] || '-',
  }
}

export const saveMajor = async (payload) => {
  const submitData = {
    ...payload,
    id: undefined,
    majorId: payload.majorId ?? payload.id,
  }
  const result = submitData.majorId
    ? await request.post('/majorInfo/updateMajorInfoById', submitData)
    : await request.post('/majorInfo/add', submitData)
  invalidateBasicDataCache()
  return result ?? true
}

export const deleteMajors = async (ids) => {
  const results = await Promise.all(ids.map((majorId) => request.post('/majorInfo/deleteMajorInfoById', { majorId })))
  invalidateBasicDataCache()
  return results
}

export const updateMajorSortOrder = async (ids = []) => {
  if (!Array.isArray(ids) || ids.length < 2) {
    return null
  }
  return request.post('/majorInfo/updateSortOrder', { ids: ids.join(',') }, { showLoading: false })
}

export const getClassList = async (params = {}) => {
  const [result, options, majorDictionary] = await Promise.all([
    request.post('/classInfo/loadDataList', {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      majorId: params.majorId,
      classNameFuzzy: params.keyword,
      status: params.status,
    }),
    loadOptions(),
    loadMajorDictionary(),
  ])

  const pageData = normalizePageResult(result)
  const departmentMap = buildOptionMap(options.departmentOptions)
  const majorMap = buildOptionMap(options.majorOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)

  return {
    ...pageData,
    list: pageData.list
      .filter((item) => {
        const normalizedItem = normalizeClassRecord(item)
        if (!params.grade) {
          return true
        }
        const major = majorDictionary[String(normalizedItem.majorId)]
        return Number(major?.educationalSystemType) === Number(params.grade)
      })
      .map((item) => {
        const normalizedItem = normalizeClassRecord(item)
        const major = majorDictionary[String(normalizedItem.majorId)] || {}
        return {
          ...normalizedItem,
          departmentName:
            departmentMap[String(normalizedItem.departmentId)] || departmentMap[String(major.departmentId)] || '-',
          majorName: majorMap[String(normalizedItem.majorId)] || '-',
          grade: major.educationalSystemType,
          gradeText: gradeMap[String(major.educationalSystemType)] || '-',
        }
      }),
  }
}

export const getClassSortList = async (params = {}) => {
  const [result, options, majorDictionary] = await Promise.all([
    request.post('/classInfo/loadSortList', {
      majorId: params.majorId,
      classNameFuzzy: params.keyword,
      status: params.status,
    }),
    loadOptions(),
    loadMajorDictionary(),
  ])
  const departmentMap = buildOptionMap(options.departmentOptions)
  const majorMap = buildOptionMap(options.majorOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)
  return (Array.isArray(result) ? result : []).map((item) => {
    const normalizedItem = normalizeClassRecord(item)
    const major = majorDictionary[String(normalizedItem.majorId)] || {}
    return {
      ...normalizedItem,
      departmentName:
        departmentMap[String(normalizedItem.departmentId)] || departmentMap[String(major.departmentId)] || '-',
      majorName: majorMap[String(normalizedItem.majorId)] || '-',
      grade: major.educationalSystemType,
      gradeText: gradeMap[String(major.educationalSystemType)] || '-',
    }
  })
}

export const getClassDetail = async (classId) => {
  const [result, options, majorDictionary] = await Promise.all([
    request.post('/classInfo/getClassInfoById', { classId }),
    loadOptions(),
    loadMajorDictionary(),
  ])
  if (!result) {
    return null
  }
  const normalizedItem = normalizeClassRecord(result)
  const departmentMap = buildOptionMap(options.departmentOptions)
  const majorMap = buildOptionMap(options.majorOptions)
  const gradeMap = buildOptionMap(options.gradeOptions)
  const major = majorDictionary[String(normalizedItem.majorId)] || {}
  return {
    ...normalizedItem,
    departmentName:
      departmentMap[String(normalizedItem.departmentId)] || departmentMap[String(major.departmentId)] || '-',
    majorName: majorMap[String(normalizedItem.majorId)] || '-',
    grade: major.educationalSystemType,
    gradeText: gradeMap[String(major.educationalSystemType)] || '-',
  }
}

export const saveClass = async (payload) => {
  const majorDictionary = await loadMajorDictionary()
  const major = majorDictionary[String(payload.majorId)] || {}
  const classId = payload.classId ?? payload.id
  const submitData = {
    ...payload,
    id: undefined,
    classId,
    departmentId: payload.departmentId ?? major.departmentId,
    majorId: Number(payload.majorId),
    status: Number(payload.status),
  }
  const result = submitData.classId
    ? await request.post('/classInfo/updateClassInfoById', submitData)
    : await request.post('/classInfo/add', submitData)
  invalidateBasicDataCache()
  return result ?? true
}

export const deleteClasses = async (ids) => {
  const results = await request.post('/classInfo/deleteBatch', { ids: ids.join(',') })
  invalidateBasicDataCache()
  return results
}

export const updateClassSortOrder = async (ids = []) => {
  if (!Array.isArray(ids) || ids.length < 2) {
    return null
  }
  return request.post('/classInfo/updateSortOrder', { ids: ids.join(',') }, { showLoading: false })
}

export const importClasses = async (file) => {
  const result = await request.post('/classInfo/importExcel', { file })
  invalidateBasicDataCache()
  return result
}

export const exportClasses = async (params = {}) => {
  const blob = await request.post(
    '/classInfo/exportExcel',
    {
      majorId: params.majorId,
      classNameFuzzy: params.keyword,
      status: params.status,
    },
    { responseType: 'blob' },
  )
  if (blob) {
    downloadBlobFile(blob, 'class-export.xlsx')
  }
  return blob
}

export const downloadClassImportTemplate = async () => {
  const blob = await request.post('/classInfo/downloadImportTemplate', {}, { responseType: 'blob', showLoading: false })
  if (blob) {
    downloadBlobFile(blob, 'class-import-template.xlsx')
  }
  return blob
}

const decorateUserList = async (params, mode) => {
  const [result, options, classDictionary] = await Promise.all([
    request.post(mode === 'student' ? '/userInfo/loadStudentList' : '/userInfo/loadTeacherList', {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      departmentId: params.departmentId,
      majorId: params.majorId,
      classId: params.classId,
      titleName: params.titleName,
      status: params.status,
      realNameFuzzy: params.keyword,
      userNoFuzzy: params.keyword,
    }),
    loadOptions(),
    loadClassDictionary(),
  ])

  const pageData = normalizePageResult(result)

  return {
    ...pageData,
    list: pageData.list.map((item) => buildUserRelationInfo(item, options, classDictionary)),
  }
}

const decorateUserDetail = async (id, mode) => {
  const [result, options, classDictionary] = await Promise.all([
    request.post(mode === 'student' ? '/userInfo/getStudentById' : '/userInfo/getTeacherById', { userId: id }),
    loadOptions(),
    loadClassDictionary(),
  ])
  if (!result) {
    return null
  }
  return buildUserRelationInfo(result, options, classDictionary)
}

export const getStudentList = (params) => decorateUserList(params, 'student')
export const getStudentDetail = (id) => decorateUserDetail(id, 'student')

export const saveStudent = async (payload) => {
  const submitData = await buildStudentSubmitData(payload)
  const result = submitData.userId
    ? await request.post('/userInfo/updateStudentById', submitData)
    : await request.post('/userInfo/addStudent', submitData)
  return result ?? true
}

export const deleteStudents = async (ids) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/userInfo/deleteStudentBatch', { ids: ids.join(',') })
  return result ?? true
}

export const getTeacherList = (params) => decorateUserList(params, 'teacher')
export const getTeacherDetail = (id) => decorateUserDetail(id, 'teacher')

export const saveTeacher = async (payload) => {
  const submitData = buildTeacherSubmitData(payload)
  const result = submitData.userId
    ? await request.post('/userInfo/updateTeacherById', submitData)
    : await request.post('/userInfo/addTeacher', submitData)
  return result ?? true
}

export const deleteTeachers = async (ids) => {
  if (!Array.isArray(ids) || !ids.length) {
    return null
  }
  const result = await request.post('/userInfo/deleteTeacherBatch', { ids: ids.join(',') })
  return result ?? true
}
