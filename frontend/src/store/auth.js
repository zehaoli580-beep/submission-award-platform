import { reactive } from 'vue'
import { me as fetchMe, logout as doLogout } from '../api'

/**
 * 全局登录态(响应式), 供路由守卫与顶栏使用
 * 用户未登录时 user 为 null
 */
export const auth = reactive({
  user: null,

  /** 应用启动时调用: 尝试恢复 Session 登录态 */
  async restore() {
    try {
      this.user = await fetchMe()
    } catch {
      this.user = null
    }
  },

  setUser(u) {
    this.user = u
  },

  async logout() {
    try {
      await doLogout()
    } finally {
      this.user = null
    }
  }
})
