export const STUDENT_TOKEN_KEY = 'studentToken'
export const STUDENT_REMEMBERED_ACCOUNT_KEY = 'studentRememberedAccount'

export const getStudentToken = () => localStorage.getItem(STUDENT_TOKEN_KEY) || ''

export const setStudentToken = (token) => {
  if (!token) {
    localStorage.removeItem(STUDENT_TOKEN_KEY)
    return
  }
  localStorage.setItem(STUDENT_TOKEN_KEY, token)
}

export const removeStudentToken = () => {
  localStorage.removeItem(STUDENT_TOKEN_KEY)
}

export const getRememberedStudentAccount = () => localStorage.getItem(STUDENT_REMEMBERED_ACCOUNT_KEY) || ''

export const setRememberedStudentAccount = (account) => {
  const value = String(account || '').trim()
  if (!value) {
    localStorage.removeItem(STUDENT_REMEMBERED_ACCOUNT_KEY)
    return
  }
  localStorage.setItem(STUDENT_REMEMBERED_ACCOUNT_KEY, value)
}

export const removeRememberedStudentAccount = () => {
  localStorage.removeItem(STUDENT_REMEMBERED_ACCOUNT_KEY)
}
