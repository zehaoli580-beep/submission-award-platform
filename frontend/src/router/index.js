import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../store/auth'

/**
 * 路由与角色守卫:
 * meta.roles 声明允许访问的角色; 未声明则只要登录即可
 * 首页 "/" 登录后按角色重定向到各自工作台
 */
const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        redirect: () => {
          const role = auth.user?.role
          if (role === 'ADMIN') return '/admin'
          if (role === 'REVIEWER') return '/reviewer'
          return '/author'
        }
      },
      {
        path: 'author',
        name: 'author',
        component: () => import('../views/AuthorHome.vue'),
        meta: { roles: ['AUTHOR'], title: '我的工作台' }
      },
      {
        path: 'reviewer',
        name: 'reviewer',
        component: () => import('../views/ReviewerHome.vue'),
        meta: { roles: ['REVIEWER'], title: '审稿工作台' }
      },
      {
        path: 'admin',
        name: 'admin',
        component: () => import('../views/AdminHome.vue'),
        meta: { roles: ['ADMIN'], title: '管理后台' }
      },
      {
        path: 'awards',
        name: 'awards',
        component: () => import('../views/AwardList.vue'),
        meta: { title: '获奖公示' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let restored = false

router.beforeEach(async (to) => {
  // 应用启动后首次导航: 先尝试恢复 Session 登录态(刷新页面不掉线)
  if (!restored) {
    restored = true
    await auth.restore()
  }
  if (to.path === '/login') {
    // 已登录用户不需要再看登录页
    if (auth.user) return false
    return true
  }
  if (!auth.user) {
    return { path: '/login' }
  }
  // 角色守卫: 访问不属于自己角色的页面 -> 回自己工作台
  const roles = to.meta?.roles
  if (roles && !roles.includes(auth.user.role)) {
    return { path: '/' }
  }
  return true
})

export default router
