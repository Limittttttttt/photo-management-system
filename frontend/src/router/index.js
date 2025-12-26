import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register', 
    component: () => import('@/views/Register.vue')
  },
  // 所有需要登录的页面都需要用户ID
  {
    path: '/dashboard/:id',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/upload/:id',
    name: 'Upload',
    component: () => import('@/views/Upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/gallery/:id',
    name: 'Gallery',
    component: () => import('@/views/Gallery.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:id/photo/:photoId',
    name: 'Photodetail',
    component: () => import('@/views/Photodetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings/:id',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/aisearch/:id',
    name: 'AiSearch',
    component: () => import('@/views/AiSearch.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 公开页面
  const publicPages = ['/login', '/register']
  
  // 检查当前页面是否需要登录
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // 检查用户是否已登录
  const user = localStorage.getItem('user')
  const userData = user ? JSON.parse(user) : null
  
  if (requiresAuth) {
    if (!userData) {
      // 需要登录但未登录，跳转到登录页
      ElMessage.warning('请先登录')
      return next('/login')
    }
    
    // 验证用户ID是否匹配
    const routeUserId = to.params.id
    if (parseInt(routeUserId) !== userData.id) {
      // ID不匹配，重定向到用户自己的页面
      ElMessage.warning('无权限访问其他用户的页面')
      
      // 根据路由名称重定向到正确的用户页面
      if (to.name === 'Photodetail') {
        // 如果是照片详情页，重定向到用户的图库
        return next(`/gallery/${userData.id}`)
      } else {
        // 其他页面重定向到对应的用户页面
        return next({ name: to.name, params: { id: userData.id } })
      }
    }
  }
  
  next()
})

export default router