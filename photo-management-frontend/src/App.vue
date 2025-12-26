<template>
  <div id="app">
    <!-- 导航栏 -->
    <nav v-if="isAuthenticated" class="navbar">
      <div class="nav-brand">
        <i class="el-icon-camera"></i>
        IManeger
      </div>
      <div class="nav-links">
        <router-link :to="dashboardRoute">
          <el-icon><House /></el-icon>
          首页
        </router-link>
        <router-link :to="uploadRoute">
          <el-icon><Upload /></el-icon>
          上传编辑
        </router-link>
        <router-link :to="galleryRoute">
          <el-icon><Picture /></el-icon>
          图片库
        </router-link>
        <router-link :to="aiSearchRoute">
          <el-icon><Search /></el-icon>
          AI搜索
        </router-link>
        <router-link :to="settingsRoute">
          <el-icon><Setting /></el-icon>
          设置
        </router-link>
      </div>
    </nav>
    
    <!-- 页面内容 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { House, Upload, Picture, Setting } from '@element-plus/icons-vue'

export default {
  components: {
    House, Upload, Picture, Setting
  },
  computed: {
    isAuthenticated() {
      // 检查用户是否已登录（有用户数据）
      const user = localStorage.getItem('user')
      const hasUser = user && JSON.parse(user).id
      
      // 检查当前路由是否需要显示导航栏
      const currentRoute = this.$route.name
      const isPublicRoute = currentRoute === 'Login' || currentRoute === 'Register'
      
      // 只有用户已登录且不在公开页面时才显示导航栏
      return hasUser && !isPublicRoute
    },
    
    // 获取当前登录用户ID
    userId() {
      const user = localStorage.getItem('user')
      if (user) {
        try {
          const userData = JSON.parse(user)
          return userData.id || null
        } catch (e) {
          console.error('解析用户数据失败:', e)
          return null
        }
      }
      return null
    },
    
    // 计算各个路由链接
    dashboardRoute() {
      return this.userId ? `/dashboard/${this.userId}` : '/login'
    },
    
    uploadRoute() {
      return this.userId ? `/upload/${this.userId}` : '/login'
    },
    
    galleryRoute() {
      return this.userId ? `/gallery/${this.userId}` : '/login'
    },
    
    settingsRoute() {
      return this.userId ? `/settings/${this.userId}` : '/login'
    },

    aiSearchRoute() {
      return this.userId ? `/aisearch/${this.userId}` : '/login'
    }
  },
  
  watch: {
    // 监听路由变化，确保导航栏正确显示
    '$route'(to) {
      // 如果当前路由需要ID但导航链接还没有ID，重定向到登录
      const authRoutes = ['Dashboard', 'Upload', 'Gallery', 'Settings', 'Photoedit', 'Photodetail']
      
      // 检查用户是否登录
      const user = localStorage.getItem('user')
      const hasUser = user && JSON.parse(user).id
      
      if (authRoutes.includes(to.name) && !hasUser) {
        // 用户未登录，跳转到登录页
        this.$router.push('/login')
      } else if (hasUser && authRoutes.includes(to.name)) {
        // 用户已登录，验证路由ID是否匹配
        const userData = JSON.parse(user)
        const routeId = to.params.id
        
        if (routeId && parseInt(routeId) !== userData.id) {
          // ID不匹配，重定向到正确的页面
          this.$router.push({ name: to.name, params: { id: userData.id } })
        }
      }
    }
  }
}
</script>
<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  background-color: #0a0a0a;
  min-height: 100vh;
}

.navbar {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #e0e0e0;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.5);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #333;
}

.nav-brand {
  font-size: 1.4rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffffff;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.nav-brand i {
  font-size: 1.6rem;
  color: #8a2be2;
  filter: drop-shadow(0 0 8px rgba(138, 43, 226, 0.6));
}

.nav-links {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.nav-links a {
  color: #b0b0b0;
  text-decoration: none;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  border: 1px solid transparent;
}

.nav-links a:hover {
  background: rgba(138, 43, 226, 0.1);
  color: #e0e0e0;
  border-color: rgba(138, 43, 226, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(138, 43, 226, 0.2);
}

.nav-links a.router-link-active {
  background: linear-gradient(135deg, #8a2be2, #6a1bb8);
  color: white;
  box-shadow: 0 4px 15px rgba(138, 43, 226, 0.4);
  border-color: #8a2be2;
}

:deep(.el-dropdown-menu) {
  background: #1a1a1a;
  border: 1px solid #333;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  padding: 0.5rem 0;
}

:deep(.el-dropdown-menu__item) {
  color: #e0e0e0;
  padding: 0.75rem 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(138, 43, 226, 0.2);
  color: #ffffff;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background: rgba(138, 43, 226, 0.2);
  color: #ffffff;
}

/* 深色主题消息框 */
:deep(.dark-message-box) {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

:deep(.dark-message-box .el-message-box__header) {
  border-bottom: 1px solid #333;
  padding: 1rem 1.5rem;
}

:deep(.dark-message-box .el-message-box__title) {
  color: #ffffff;
}

:deep(.dark-message-box .el-message-box__content) {
  background: #1a1a1a;
  color: #e0e0e0;
  padding: 1.5rem;
}

:deep(.dark-message-box .el-message-box__btns) {
  border-top: 1px solid #333;
  padding: 1rem 1.5rem;
}

.main-content {
  min-height: calc(100vh - 64px);
  background: #0a0a0a;
}

/* Element Plus 图标颜色适配 */
:deep(.el-icon) {
  color: inherit;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 1rem;
  }
  
  .nav-links {
    gap: 0.25rem;
  }
  
  .nav-links a {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }
  
  .nav-brand {
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  .nav-links a span:not(.el-icon) {
    display: none;
  }
  
  .nav-links a {
    padding: 0.5rem;
  }
}
</style>