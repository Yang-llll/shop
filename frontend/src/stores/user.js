import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getUserInfoApi } from '../api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  async function login(data) {
    try {
      const res = await loginApi(data)
      const loginData = res.data || res
      token.value = loginData.token
      sessionStorage.setItem('token', token.value)
      // 保存用户基本信息
      const info = { username: loginData.username, role: loginData.role, userId: loginData.userId }
      userInfo.value = info
      sessionStorage.setItem('userInfo', JSON.stringify(info))
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success('登录成功')
      return true
    } catch (err) {
      return false
    }
  }

  async function register(data) {
    try {
      await registerApi(data)
      return true
    } catch (err) {
      return false
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi()
      userInfo.value = res
      sessionStorage.setItem('userInfo', JSON.stringify(res))
    } catch {
      logout(false)
    }
  }

  function logout(showMessage = true) {
    token.value = ''
    userInfo.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
    if (showMessage) ElMessage.info('已退出登录')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchUserInfo
  }
})
