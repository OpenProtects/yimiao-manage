import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userType = computed(() => userInfo.value?.userType || 0)
  const userId = computed(() => userInfo.value?.userId || null)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  async function login(loginData) {
    const res = await userApi.login(loginData)
    if (res.code === 200) {
      setToken(res.data.token)
      setUserInfo({
        userId: res.data.userId,
        username: res.data.username,
        userType: res.data.userType
      })
    }
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userType,
    userId,
    setToken,
    setUserInfo,
    login,
    logout
  }
})
