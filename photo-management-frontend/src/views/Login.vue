<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-overlay"></div>
      <div class="floating-camera"></div>
      <div class="floating-lens"></div>
    </div>
    
    <div class="login-card">
      <div class="card-header">
        <div class="logo">
          <i class="el-icon-camera"></i>
          <span>IManeger</span>
        </div>
        <h2>欢迎回来</h2>
        <p>请登录您的账号</p>
      </div>

      <el-form :model="loginForm" class="login-form">
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-button 
          type="primary" 
          size="large" 
          class="login-button"
          @click="handleLogin"
          :loading="loading"
        >
          <el-icon class="el-icon--left"><Promotion /></el-icon>
          登录
        </el-button>

        <div class="divider">
          <span>或</span>
        </div>

        <el-button 
          size="large" 
          class="register-button"
          @click="$router.push('/register')"
        >
          <el-icon class="el-icon--left"><UserFilled /></el-icon>
          注册新账号
        </el-button>
      </el-form>

      <div class="login-footer">
        <p>© 2025 IManeger - 让每一张照片都有归属！</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Promotion, UserFilled } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)

// 用户输入的登录数据
const loginForm = ref({
  username: '',
  password: ''
})

// 处理登录
const handleLogin = async () => {
  // 获取用户输入
  const { username, password } = loginForm.value
  
  // 前端验证
  if (!username.trim()) {
    ElMessage.error('请输入用户名或邮箱')
    return
  }
  
  if (!password) {
    ElMessage.error('请输入密码')
    return
  }
  
  // 发送登录请求
  loading.value = true
  try {
    const response = await axios.post('/login', {
      username: username.trim(),
      password: password
    })
    
    if (response.data.success) {
      ElMessage.success('登录成功！')
      const userData = response.data.data
      
      // 保存用户信息
      localStorage.setItem('user', JSON.stringify(response.data.data))

      // 强制刷新页面，确保导航栏正确加载
      setTimeout(() => {
        window.location.href = `/dashboard/${userData.id}`
      }, 500)
      
      // 如果用户选择记住我
      if (loginForm.value.remember) {
        localStorage.setItem('rememberMe', 'true')
      }
      
      // 跳转到主页
      router.push(`/dashboard/${userData.id}`)
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.floating-camera, .floating-lens {
  position: absolute;
  background: rgba(111, 70, 149, 0.3);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.floating-camera {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 15%;
  animation-delay: 0s;
}

.floating-lens {
  width: 80px;
  height: 80px;
  bottom: 30%;
  right: 15%;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(50deg); }
}

.login-card {
  background: rgba(26, 26, 26, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(138, 43, 226, 0.2);
  z-index: 1;
  position: relative;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 700;
  color: #8a2be2;
  margin-bottom: 20px;
}

.logo i {
  font-size: 28px;
}

.card-header h2 {
  color: #ffffff;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.card-header p {
  color: #b0b0b0;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: #2a2a2a;
  border: 1px solid #444;
}

:deep(.el-input__inner) {
  font-size: 16px;
  color: #e0e0e0;
  background: transparent;
}

:deep(.el-input__inner::placeholder) {
  color: #666;
}

:deep(.el-input__prefix) {
  color: #8a2be2;
}

:deep(.el-link) {
  color: #8a2be2;
}

:deep(.el-link:hover) {
  color: #9b45f0;
}

.login-button {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #8a2be2, #6a1bb8);
  border: none;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(138, 43, 226, 0.4);
}

.divider {
  position: relative;
  text-align: center;
  margin: 25px 0;
  color: #666;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #444;
}

.divider span {
  background: #1a1a1a;
  padding: 0 15px;
  position: relative;
  z-index: 1;
}

.register-button {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border: 2px solid #8a2be2;
  color: #8a2be2;
  background: transparent;
  transition: all 0.3s ease;
}

.register-button:hover {
  background: #8a2be2;
  color: white;
  transform: translateY(-2px);
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #333;
}

.login-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    margin: 20px;
    padding: 30px 25px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
}
</style>