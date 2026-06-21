const TOKEN_KEY = 'llcb_token'
const USER_KEY = 'llcb_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setUser(user: any): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getUser(): any {
  const str = localStorage.getItem(USER_KEY)
  return str ? JSON.parse(str) : null
}
