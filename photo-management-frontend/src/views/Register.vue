<template>
  <div class="register-container">
    <div class="register-background">
      <div class="background-overlay"></div>
      <div class="floating-shutter"></div>
      <div class="floating-aperture"></div>
    </div>
    
    <div class="register-card">
      <div class="card-header">
        <div class="logo">
          <i class="el-icon-camera"></i>
          <span>IManeger</span>
        </div>
        <h2>创建账号</h2>
        <p>开启您的摄影之旅</p>
      </div>

      <el-form :model="registerForm" class="register-form">
        <el-form-item>
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            @blur="checkUsername"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            size="large"
            :prefix-icon="Message"
            @blur="checkEmail"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 已移除服务条款复选框 -->

        <el-button 
          type="primary" 
          size="large" 
          class="register-button"
          @click="handleRegister"
          :loading="loading"
        >
          <el-icon class="el-icon--left"><UserFilled /></el-icon>
          立即注册
        </el-button>

        <div class="divider">
          <span>已有账号？</span>
        </div>

        <el-button 
          size="large" 
          class="login-button"
          @click="$router.push('/login')"
        >
          <el-icon class="el-icon--left"><Promotion /></el-icon>
          去登录
        </el-button>
      </el-form>

      <div class="register-footer">
        <p>加入我们，发现存影的无限乐趣</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Promotion, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const { proxy } = getCurrentInstance()  // 获取组件实例

// 用户输入的数据
const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 邮箱格式验证
const validateEmail = (email) => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

// 检查用户名是否存在
const checkUsername = async () => {
  const username = registerForm.value.username.trim()
  if (!username || username.length < 2) return
  
  try {
    const response = await proxy.$axios.get('/checkUsername', {
      params: { username }
    })
    if (response.data.exists) {
      ElMessage.warning('用户名已存在')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱是否存在
const checkEmail = async () => {
  const email = registerForm.value.email.trim()
  if (!email) return
  
  // 先验证邮箱格式
  if (!validateEmail(email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  try {
    const response = await proxy.$axios.get('/checkEmail', {  // 使用 proxy.$axios
      params: { email }
    })
    if (response.data.exists) {
      ElMessage.warning('邮箱已被注册')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 处理注册
const handleRegister = async () => {
  // 获取用户输入的数据
  const { username, email, password, confirmPassword } = registerForm.value
  
  // 前端验证
  if (!username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  
  if (!email.trim()) {
    ElMessage.error('请输入邮箱')
    return
  }
  
  if (!password) {
    ElMessage.error('请输入密码')
    return
  }
  
  if (!confirmPassword) {
    ElMessage.error('请确认密码')
    return
  }
  
  // 邮箱格式验证
  if (!validateEmail(email)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  
  // 用户名长度验证
  if (username.length < 6) {
    ElMessage.error('用户名长度至少6位')
    return
  }

  // 密码长度验证
  if (password.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }
  
  // 密码一致性验证
  if (password !== confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  // 发送注册请求
  loading.value = true
  try {
    const response = await proxy.$axios.post('/register', {
      username: username.trim(),
      email: email.trim(),
      password: password
    })
    
    if (response.data.success) {
      ElMessage.success('注册成功！')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>

.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  overflow: hidden;
}

.register-background {
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

.floating-shutter, .floating-aperture {
  position: absolute;
  background: rgba(111, 70, 149, 0.3);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

.floating-shutter {
  width: 80px;
  height: 80px;
  top: 25%;
  right: 10%;
  animation-delay: 0s;
}

.floating-aperture {
  width: 120px;
  height: 120px;
  bottom: 20%;
  left: 8%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) scale(1); }
  50% { transform: translateY(-25px) scale(1.1); }
}

.register-card {
  background: rgba(26, 26, 26, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
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

.register-form {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: #2a2a2a;
  border: 1px solid #444;
  margin-bottom: 16px;
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

.register-button {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #8a2be2, #6a1bb8);
  border: none;
  margin-top: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.register-button:hover {
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

.login-button {
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

.login-button:hover {
  background: #8a2be2;
  color: white;
  transform: translateY(-2px);
}

.register-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #333;
}

.register-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-card {
    margin: 20px;
    padding: 30px 25px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
}
</style>