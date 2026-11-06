import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 开发期跨域处理: 前端请求 /api/** 与 /files/** 由 Vite 代理转发到后端 8080
      // (报告第四阶段"前后端联调"的核心配置, 就在这里)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/files': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
