import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setToken, removeToken, getToken, setUser, getUser } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<any>(getUser())
  const userInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => user.value?.roles || [])

  async function login(loginFn: () => Promise<any>) {
    const data = await loginFn()
    token.value = data.token
    user.value = { userId: data.userId, username: data.username, roles: data.roles }
    setToken(data.token)
    setUser(user.value)
    return data
  }

  function logout() {
    token.value = null
    user.value = null
    userInfo.value = null
    removeToken()
  }

  function hasRole(role: string) {
    return roles.value.includes(role)
  }

  return { token, user, userInfo, isLoggedIn, roles, login, logout, hasRole }
})
