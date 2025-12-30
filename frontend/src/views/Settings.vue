<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>个性化您的存储体验</p>
    </div>

    <div class="settings-container">
      <el-card class="settings-card">
        <el-tabs v-model="activeTab">
          <!-- 个人资料设置 -->
          <el-tab-pane label="个人资料" name="profile">
            <div class="tab-content">
              <h3>个人信息</h3>
              <div class="profile-section">
                <el-form :model="profileForm" label-width="80px" class="profile-form">
                  <el-form-item label="用户名">
                    <el-input 
                      v-model="profileForm.username" 
                      placeholder="请输入您的用户名"
                      :disabled="!isEditing"
                    />
                  </el-form-item>
                  <el-form-item label="邮箱">
                    <el-input 
                      v-model="profileForm.email" 
                      placeholder="请输入您的邮箱"
                      :disabled="!isEditing"
                    />
                  </el-form-item>
                  
                  <div class="form-actions">
                    <el-button 
                      v-if="!isEditing" 
                      type="primary" 
                      @click="startEditing"
                    >
                      <el-icon><Edit /></el-icon>
                      编辑资料
                    </el-button>
                    <template v-else>
                      <el-button type="primary" @click="saveProfile">
                        <el-icon><Check /></el-icon>
                        保存修改
                      </el-button>
                      <el-button @click="cancelEditing">
                        <el-icon><Close /></el-icon>
                        取消
                      </el-button>
                    </template>
                  </div>
                </el-form>
              </div>
            </div>
          </el-tab-pane>

          <!-- 安全设置 -->
          <el-tab-pane label="安全设置" name="security">
            <div class="tab-content">
              <h3>修改密码</h3>
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input 
                    v-model="passwordForm.oldPassword" 
                    type="password" 
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    type="password" 
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    type="password" 
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="passwordLoading">
                    <el-icon><Lock /></el-icon>
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 账户设置 -->
          <el-tab-pane label="账户设置" name="account">
            <div class="tab-content">
              <h3>账户操作</h3>
              <div class="account-actions">
                <el-button type="primary" @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-button>
                <el-button type="danger" @click="showDeleteConfirm">
                  <el-icon><Delete /></el-icon>
                  注销账号
                </el-button>
              </div>
              
              <!-- 注销账户确认对话框 -->
              <el-dialog
                v-model="deleteDialogVisible"
                title="注销账户确认"
                width="400px"
                @close="cancelDeleteAccount"
              >
                <div class="delete-dialog-content">
                  <div class="warning-message">
                    <el-alert
                      title="危险操作警告"
                      type="error"
                      description="此操作将永久删除您的账户和所有数据，且无法恢复！"
                      show-icon
                      :closable="false"
                    />
                  </div>
                  
                  <div class="password-confirm">
                    <p class="confirm-text">为了确认操作，请输入您的密码：</p>
                    <el-form :model="deleteForm" :rules="deleteRules" ref="deleteFormRef">
                      <el-form-item prop="password">
                        <el-input
                          v-model="deleteForm.password"
                          type="password"
                          placeholder="请输入您的密码"
                          show-password
                          @keyup.enter="confirmDeleteAccount"
                        />
                      </el-form-item>
                    </el-form>
                  </div>
                </div>
                
                <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="cancelDeleteAccount" :disabled="deleteLoading">取消</el-button>
                    <el-button type="danger" @click="confirmDeleteAccount" :loading="deleteLoading">
                      确认注销
                    </el-button>
                  </span>
                </template>
              </el-dialog>
            </div>
          </el-tab-pane>

          <!-- 标签管理 -->
          <el-tab-pane label="标签管理" name="tags">
            <div class="tab-content">
              <h3>我的标签</h3>
              
              <!-- 标签列表 -->
              <div class="tags-container">
                <div class="tags-list">
                  <div v-if="tags.length === 0" class="empty-tags">
                    <el-empty description="暂无标签" :image-size="100" />
                  </div>
                  <div v-else class="tag-items">
                    <el-tag
                      v-for="tag in tags"
                      :key="tag.id"
                      :type="getTagType(tag)"
                      closable
                      @close="deleteTag(tag)"
                      class="tag-item"
                      size="large"
                    >
                      {{ tag.tagName }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Edit, Lock, SwitchButton, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const passwordFormRef = ref()
const deleteFormRef = ref()
const activeTab = ref('profile')
const isEditing = ref(false)
const passwordLoading = ref(false)
const deleteLoading = ref(false)
const deleteDialogVisible = ref(false)
// 标签管理相关
const tags = ref([])

// 个人资料数据
const originalProfile = ref({})
const profileForm = reactive({
  username: '',
  email: ''
})

// 密码表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 注销表单数据
const deleteForm = reactive({
  password: ''
})

// 用户信息
const currentUser = ref(null)

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 注销密码验证规则
const deleteRules = {
  password: [
    { required: true, message: '请输入密码确认操作', trigger: 'blur' },
    { min: 1, message: '密码不能为空', trigger: 'blur' }
  ]
}

// 获取标签类型
const getTagType = (tag) => {
  const types = ['', 'success', 'info', 'warning', 'danger']
  const hash = tag.tagName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
  return types[hash % types.length]
}

// 加载标签数据
const loadTags = async () => {
  if (!currentUser.value) return
  
  try {
    const response = await axios.get(`/tags/user/${currentUser.value.id}`)
    if (response.data.success) {
      tags.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  }
}

// 删除标签
const deleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签 "${tag.tagName}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    try {
      const response = await axios.post('/tags/delete', {
        tagId: tag.id,
        userId: currentUser.value.id
      })
      
      if (response.data.success) {
        ElMessage.success('标签删除成功')
        // 从列表中移除
        tags.value = tags.value.filter(t => t.id !== tag.id)
      } else {
        ElMessage.error(response.data.message)
      }
    } catch (error) {
      console.error('删除标签失败:', error)
      ElMessage.error(error.response?.data?.message || '删除标签失败')
    }
    
  } catch {
    // 用户取消删除
  }
}

const loadUserInfo = () => {
  const user = localStorage.getItem('user')
  if (user) {
    currentUser.value = JSON.parse(user)
    profileForm.username = currentUser.value.username || ''
    profileForm.email = currentUser.value.email || ''
    // 加载标签数据
    loadTags()
  } else {
    router.push('/login')
  }
}

// 开始编辑个人资料
const startEditing = () => {
  isEditing.value = true
  // 保存原始数据用于取消编辑时恢复
  originalProfile.value = { ...profileForm }
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
  // 恢复原始数据
  Object.assign(profileForm, originalProfile.value)
}

// 保存个人资料
const saveProfile = async () => {
  if (!profileForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  
  if (!profileForm.email.trim()) {
    ElMessage.error('请输入邮箱')
    return
  }

  // 用户名长度限制
  if (profileForm.username.trim().length < 6) {
    ElMessage.error('用户名长度不能小于6个字符')
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(profileForm.email)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  
  if (!currentUser.value) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    const response = await axios.post('/update-profile', {
      userId: currentUser.value.id,
      username: profileForm.username.trim(),
      email: profileForm.email.trim()
    })
    
    if (response.data.success) {
      ElMessage.success('个人资料更新成功')
      isEditing.value = false
      
      // 更新本地存储的用户信息
      if (response.data.data) {
        currentUser.value = response.data.data
        localStorage.setItem('user', JSON.stringify(response.data.data))
      }
    } else {
      ElMessage.error(response.data.message)
      // 如果保存失败，恢复原始数据
      cancelEditing()
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
    // 如果保存失败，恢复原始数据
    cancelEditing()
  }
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    if (!currentUser.value) {
      ElMessage.error('用户信息获取失败')
      return
    }
    
    passwordLoading.value = true
    
    try {
      const response = await axios.post('/change-password', {
        userId: currentUser.value.id,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })
      
      if (response.data.success) {
        ElMessage.success('密码修改成功')
        
        // 清空表单
        Object.assign(passwordForm, {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        })
        passwordFormRef.value.resetFields()
      } else {
        ElMessage.error(response.data.message)
      }
      
    } catch (error) {
      console.error('密码修改失败:', error)
      ElMessage.error(error.response?.data?.message || '密码修改失败，请稍后重试')
    }
    
  } catch (error) {
    // 表单验证失败
    ElMessage.error('请正确填写表单')
  } finally {
    passwordLoading.value = false
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}

// 显示注销确认对话框
const showDeleteConfirm = async () => {
  try {
    // 先询问是否确认注销
    await ElMessageBox.confirm(
      '此操作将永久删除您的账户和所有数据，且无法恢复。确定要继续吗？',
      '确认注销账户',
      {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 显示密码确认对话框
    deleteDialogVisible.value = true
    deleteForm.password = ''
    
  } catch {
    // 用户取消操作
  }
}

// 取消注销账户
const cancelDeleteAccount = () => {
  deleteDialogVisible.value = false
  deleteForm.password = ''
  if (deleteFormRef.value) {
    deleteFormRef.value.resetFields()
  }
}

// 确认注销账户
const confirmDeleteAccount = async () => {
  try {
    await deleteFormRef.value.validate()
    
    if (!currentUser.value) {
      ElMessage.error('用户信息获取失败')
      return
    }
    
    if (!deleteForm.password) {
      ElMessage.error('请输入密码')
      return
    }
    
    deleteLoading.value = true
    
    try {
      const response = await axios.post('/delete-account', {
        userId: currentUser.value.id,
        password: deleteForm.password
      })
      
      if (response.data.success) {
        ElMessage.success('账户已成功注销')
        localStorage.removeItem('user')
        deleteDialogVisible.value = false
        router.push('/register')
      } else {
        ElMessage.error(response.data.message)
      }
      
    } catch (error) {
      console.error('注销失败:', error)
      ElMessage.error(error.response?.data?.message || '账户注销失败，请稍后重试')
    }
    
  } catch (error) {
    // 表单验证失败
    ElMessage.error('请输入正确的密码')
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.settings-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #0a0a0a;
  color: #e0e0e0;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #ffffff;
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.page-header p {
  color: #b0b0b0;
  font-size: 1.1rem;
}

.settings-card {
  border-radius: 16px;
  border: 1px solid #333;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  background: #1a1a1a;
}

:deep(.el-tabs__header) {
  background: #2a2a2a;
  border-bottom: 1px solid #333;
  margin: 0;
}

:deep(.el-tabs__item) {
  color: #b0b0b0;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #8a2be2;
}

:deep(.el-tabs__active-bar) {
  background-color: #8a2be2;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: #333;
}

.tab-content {
  padding: 1rem 0;
}

.tab-content h3 {
  color: #ffffff;
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  font-weight: 600;
  border-left: 4px solid #8a2be2;
  padding-left: 0.75rem;
}

/* 个人资料样式 */
.profile-section {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.profile-form {
  flex: 1;
}

:deep(.el-form-item__label) {
  color: #e0e0e0;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: #2a2a2a;
  border: 1px solid #444;
  box-shadow: none;
  border-radius: 8px;
}

:deep(.el-input__inner) {
  color: #e0e0e0;
  background: transparent;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #333;
  border-color: #555;
}

.form-actions {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #333;
}

/* 安全设置样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

/* 账户操作样式 */
.account-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.account-actions .el-button {
  min-width: 120px;
}

/* 注销账户对话框样式 */
.delete-dialog-content {
  padding: 0.5rem;
}

.warning-message {
  margin-bottom: 1.5rem;
}

:deep(.el-alert--error) {
  background-color: rgba(255, 71, 87, 0.1);
  border: 1px solid rgba(255, 71, 87, 0.3);
  color: #ff4757;
}

:deep(.el-alert--error .el-alert__title) {
  color: #ff4757;
  font-weight: 600;
}

:deep(.el-alert--error .el-alert__description) {
  color: rgba(255, 71, 87, 0.9);
}

.password-confirm {
  margin-top: 1.5rem;
}

.confirm-text {
  color: #e0e0e0;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

:deep(.el-dialog) {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #333;
  padding: 1rem 1.5rem;
  background: #2a2a2a;
  border-radius: 12px 12px 0 0;
}

:deep(.el-dialog__title) {
  color: #ffffff;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 1.5rem;
  color: #e0e0e0;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #333;
  padding: 1rem 1.5rem;
  background: #2a2a2a;
  border-radius: 0 0 12px 12px;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: #8a2be2;
  border-color: #8a2be2;
  box-shadow: 0 4px 15px rgba(138, 43, 226, 0.3);
}

:deep(.el-button--primary:hover) {
  background: #9b45f0;
  border-color: #9b45f0;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(138, 43, 226, 0.4);
}

:deep(.el-button--danger) {
  background: #ff4757;
  border-color: #ff4757;
}

:deep(.el-button--danger:hover) {
  background: #ff3742;
  border-color: #ff3742;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-page {
    padding: 1rem;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .profile-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .profile-form {
    width: 100%;
  }
  
  .account-actions {
    justify-content: center;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
  }
}

/* 标签管理样式 */
.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.tags-actions {
  display: flex;
  gap: 0.5rem;
}

/* 标签统计样式 */
.tags-stats {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #2a2a2a;
  border-radius: 12px;
}

:deep(.el-statistic__head) {
  color: #b0b0b0;
  font-weight: 500;
}

:deep(.el-statistic__number) {
  color: #8a2be2;
  font-weight: 600;
}

/* 标签管理样式 */
.tags-container {
  margin-top: 1rem;
}

.tags-list {
  margin: 1rem 0 2rem 0;
}

.empty-tags {
  padding: 2rem;
  text-align: center;
  background: #2a2a2a;
  border-radius: 12px;
  border: 1px dashed #444;
}

.tag-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding: 1rem;
  background: #2a2a2a;
  border-radius: 12px;
  min-height: 80px;
}

.tag-item {
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: default;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 不同类型的标签样式 */
:deep(.el-tag) {
  border: none;
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15), rgba(103, 194, 58, 0.25));
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.15), rgba(144, 147, 153, 0.25));
  color: #909399;
  border: 1px solid rgba(144, 147, 153, 0.3);
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15), rgba(230, 162, 60, 0.25));
  color: #e6a23c;
  border: 1px solid rgba(230, 162, 60, 0.3);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15), rgba(245, 108, 108, 0.25));
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.3);
}

:deep(.el-tag--default) {
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.1), rgba(138, 43, 226, 0.2));
  color: #8a2be2;
  border: 1px solid rgba(138, 43, 226, 0.3);
}

/* 删除按钮样式 */
:deep(.el-tag .el-icon-close) {
  color: inherit;
  opacity: 0.7;
}

:deep(.el-tag .el-icon-close:hover) {
  color: #ff4757;
  opacity: 1;
  background: transparent;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tag-items {
    padding: 0.75rem;
    justify-content: center;
  }
  
  .tag-item {
    font-size: 13px;
    padding: 0 10px;
    height: 32px;
  }
}
</style>