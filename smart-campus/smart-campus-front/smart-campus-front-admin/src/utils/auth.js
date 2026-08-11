export const ADMIN_TOKEN_KEY = 'adminToken'
export const ADMIN_REMEMBERED_ACCOUNT_KEY = 'adminRememberedAccount'

export const getAdminToken = () => localStorage.getItem(ADMIN_TOKEN_KEY) || ''

export const setAdminToken = (token) => {
  if (!token) {
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    return
  }
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

export const removeAdminToken = () => {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

export const getRememberedAdminAccount = () => localStorage.getItem(ADMIN_REMEMBERED_ACCOUNT_KEY) || ''

export const setRememberedAdminAccount = (account) => {
  const value = String(account || '').trim()
  if (!value) {
    localStorage.removeItem(ADMIN_REMEMBERED_ACCOUNT_KEY)
    return
  }
  localStorage.setItem(ADMIN_REMEMBERED_ACCOUNT_KEY, value)
}

export const removeRememberedAdminAccount = () => {
  localStorage.removeItem(ADMIN_REMEMBERED_ACCOUNT_KEY)
}
