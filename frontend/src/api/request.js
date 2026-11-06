import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * axios 统一封装(报告第三阶段"前端搭建"的要点之一):
 * 1. baseURL 为空 -> 走 Vite 开发代理(见 vite.config.js), 避免 CORS 问题
 * 2. withCredentials -> Session 登录态依赖 Cookie, 必须开启
 * 3. 响应拦截器统一处理后端 Result 结构 {code, message, data}:
 *    code === 0 才把 data 抛给调用方, 否则弹错误提示
 */
const request = axios.create({
  baseURL: '',
  timeout: 30000,
  withCredentials: true
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准 Result 结构(如静态文件)直接放行
    if (res === null || res === undefined || res.code === undefined) {
      return res
    }
    if (res.code === 0) {
      return res.data
    }
    // 未登录 -> 跳登录页; 其他业务错误 -> 弹提示
    if (res.message === '请先登录') {
      router.push('/login')
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    ElMessage.error('网络异常, 请检查后端服务是否已启动')
    return Promise.reject(error)
  }
)

export default request
