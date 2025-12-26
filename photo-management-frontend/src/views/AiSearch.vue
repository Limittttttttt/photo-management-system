<template>
  <div class="ai-search-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-box-wrapper">
        <div class="search-input-group">
          <el-autocomplete
            v-model="searchQuery"
            :fetch-suggestions="fetchSuggestions"
            placeholder="输入描述词搜索图片..."
            size="large"
            class="search-autocomplete"
            @select="handleSelectSuggestion"
            @keyup.enter="performSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #default="{ item }">
              <div class="suggestion-item">
                <el-icon><Pointer /></el-icon>
                <span>{{ item.value }}</span>
              </div>
            </template>
          </el-autocomplete>
          
          <el-button 
            type="primary" 
            size="large" 
            :icon="Search" 
            @click="performSearch"
            :loading="loading"
            class="search-btn"
          >
            AI搜索
          </el-button>
        </div>
        
        <div class="hot-searches">
          <span class="hot-title">搜索参考：</span>
          <el-tag
            v-for="tag in hotTags"
            :key="tag"
            type="info"
            class="hot-tag"
            @click="useHotTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="results-section">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-progress
          :percentage="70"
          :indeterminate="true"
          :show-text="false"
          status="success"
          class="loading-progress"
        />
        <p class="loading-text">AI正在分析图片内容...</p>
      </div>
      
      <!-- 无结果 -->
      <div v-else-if="hasSearched && images.length === 0" class="no-results">
        <div class="no-results-content">
          <el-icon><Search /></el-icon>
          <h3>未找到匹配的图片</h3>
          <p>尝试使用其他关键词，或使用更具体的描述</p>
          <el-button type="primary" @click="resetSearch">
            重新搜索
          </el-button>
        </div>
      </div>
      
      <!-- 有结果 -->
      <div v-else class="results-content">
        <!-- 搜索结果标题 -->
        <div v-if="hasSearched" class="results-header">
          <h2 class="results-title">
            <el-icon><Collection /></el-icon>
            搜索结果
            <span class="query-highlight">"{{ lastQuery }}"</span>
            <span class="results-count">({{ images.length }}张)</span>
          </h2>
        </div>
        
        <!-- 未搜索时的欢迎页 -->
        <div v-else class="welcome-content">
          <div class="welcome-card">
            <div class="welcome-icon">
              <el-icon><MagicStick /></el-icon>
            </div>
            <h2 class="welcome-title">AI 智能图片搜索</h2>
            <p class="welcome-description">
              使用自然语言描述您想找的图片，AI会智能识别图片内容并进行匹配
            </p>
            <div class="welcome-features">
              <div class="feature">
                <el-icon><Cpu /></el-icon>
                <span>智能图像识别</span>
              </div>
              <div class="feature">
                <el-icon><Lightning /></el-icon>
                <span>快速精准匹配</span>
              </div>
              <div class="feature">
                <el-icon><DataAnalysis /></el-icon>
                <span>多维度分析</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 图片网格 -->
        <div v-if="hasSearched" class="images-grid">
          <div 
            v-for="image in images" 
            :key="image.id" 
            class="image-item"
            @click="goToDetail(image)"
          >
            <div class="image-container">
              <img 
                :src="getImageUrl(image)" 
                :alt="image.name"
                class="image-thumbnail"
                @error="handleImageError"
              />
              <div class="image-hover">
                <el-button type="primary" size="small" circle>
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="image-info">
              <h4 class="image-title">{{ image.name || '未命名图片' }}</h4>
              <p v-if="image.description" class="image-desc">
                {{ truncateText(image.description, 35) }}
              </p>
              
              <div class="image-meta">
                <div class="meta-item" v-if="image.exifCameraModel">
                  <el-icon><Camera /></el-icon>
                  <span>{{ image.exifCameraModel }}</span>
                </div>
                <div class="meta-item" v-if="image.exifShootTime">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(image.exifShootTime) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><DataBoard /></el-icon>
                  <span>{{ formatSize(image.size) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { 
  Search, 
  Pointer,
  Collection, 
  MagicStick,
  Cpu,
  Lightning,
  DataAnalysis,
  View,
  Camera,
  Clock,
  DataBoard
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const images = ref([])
const lastQuery = ref('')
const suggestions = ref([])

// 搜索提示标签
const hotTags = ref([
  '风景', '人物', '建筑', '夜景', 
  '美食', '动物', '街景', '室内',
  '自然风光', '城市景观'
])

// 获取用户ID
const getUserId = () => {
  const user = localStorage.getItem('user')
  if (user) {
    try {
      return JSON.parse(user).id
    } catch {
      return null
    }
  }
  return null
}

// 执行搜索
const performSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  const userId = getUserId()
  if (!userId) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  hasSearched.value = true
  lastQuery.value = query

  try {
    const response = await axios.get('http://localhost:8088/aisearch/search', {
      params: { userId, query }
    })

    if (response.data.success) {
      images.value = response.data.images || []
      if (images.value.length === 0) {
        ElMessage.info('未找到相关图片')
      }
    } else {
      ElMessage.error(response.data.message || '搜索失败')
      images.value = []
    }
  } catch (error) {
    console.error('搜索请求失败:', error)
    ElMessage.error('网络请求失败')
    images.value = []
  } finally {
    loading.value = false
  }
}

// 获取搜索建议
const fetchSuggestions = async (query, cb) => {
  const userId = getUserId()
  if (!userId || !query.trim()) {
    cb([])
    return
  }

  try {
    const response = await axios.get('http://localhost:8088/aisearch/suggestions', {
      params: { userId, prefix: query }
    })

    if (response.data.success) {
      const suggestions = (response.data.suggestions || []).map(text => ({
        value: text
      }))
      cb(suggestions)
    } else {
      cb([])
    }
  } catch {
    cb([])
  }
}

// 处理选择建议
const handleSelectSuggestion = (item) => {
  searchQuery.value = item.value
  performSearch()
}

// 使用热门标签
const useHotTag = (tag) => {
  searchQuery.value = tag
  performSearch()
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  hasSearched.value = false
  images.value = []
}

// 查看图片详情
const goToDetail = (image) => {
  const userId = getUserId()
  if (userId && image.id) {
    router.push(`/${userId}/photo/${image.id}`)
  }
}

// 获取图片URL - 处理Base64数据
const getImageUrl = (image) => {
  if (!image || !image.image) {
    return getDefaultPlaceholder()
  }
  
  const imageData = image.image
  
  // 完整的data URL
  if (typeof imageData === 'string' && imageData.startsWith('data:')) {
    return imageData
  }
  
  // Base64字符串但没有前缀
  if (typeof imageData === 'string' && imageData.length > 50) {
    // 尝试检测图片类型
    const imageType = detectImageTypeFromBase64(imageData)
    return `data:image/${imageType};base64,${imageData}`
  }
  
  //返回默认占位图
  return getDefaultPlaceholder()
}

// 检测Base64字符串的图片类型
const detectImageTypeFromBase64 = (base64Str) => {
  if (!base64Str || base64Str.length < 4) return 'jpeg'
  
  const firstChars = base64Str.substring(0, 4)
  
  // 检查Base64开头的标识
  switch (firstChars) {
    // JPEG
    case '/9j/':
      return 'jpeg'
    // PNG
    case 'iVBOR':
      return 'png'
    // GIF
    case 'R0lG':
    case 'R0lGOD':
      return 'gif'
    // BMP
    case 'Qk02':
      return 'bmp'
    default:
      // 尝试通过头部字节检测
      try {
        const binary = atob(base64Str.substring(0, 8))
        const bytes = new Uint8Array(binary.length)
        for (let i = 0; i < binary.length; i++) {
          bytes[i] = binary.charCodeAt(i)
        }
        
        // JPEG
        if (bytes[0] === 0xFF && bytes[1] === 0xD8) {
          return 'jpeg'
        }
        // PNG
        if (bytes[0] === 0x89 && bytes[1] === 0x50 && 
            bytes[2] === 0x4E && bytes[3] === 0x47) {
          return 'png'
        }
        // GIF
        if (bytes[0] === 0x47 && bytes[1] === 0x49 && 
            bytes[2] === 0x46 && bytes[3] === 0x38) {
          return 'gif'
        }
      } catch (e) {
        console.warn('无法检测图片类型:', e)
      }
      
      return 'jpeg'
  }
}

// 获取默认占位图
const getDefaultPlaceholder = () => {
  return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiMxQTFBMUEiLz48cGF0aCBkPSJNNzUgNzVMMTI1IDEyNUwxMjUgNzVMNzUgMTI1IiBzdHJva2U9IiM4QTJCRTIiIHN0cm9rZS13aWR0aD0iMiIvPjwvc3ZnPg=='
}

// 图片加载失败处理
const handleImageError = (event) => {
  event.target.src = getDefaultPlaceholder()
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    // 处理各种时间格式
    let date
    if (typeof timeStr === 'string') {
      date = new Date(timeStr)
    } else if (typeof timeStr === 'object' && timeStr.time) {
      date = new Date(timeStr.time)
    } else {
      return ''
    }
    
    return date.toLocaleDateString('zh-CN')
  } catch {
    return ''
  }
}

// 格式化大小
const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0B'
  
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  if (i === 0) return bytes + sizes[i]
  return (bytes / Math.pow(1024, i)).toFixed(1) + sizes[i]
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 初始化
onMounted(() => {
  const queryParam = route.query.q
  if (queryParam) {
    searchQuery.value = queryParam
    performSearch()
  }
})
</script>

<style scoped>
.ai-search-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #151515 100%);
  color: #e0e0e0;
}

/* 搜索区域 */
.search-section {
  padding: 2rem;
  background: rgba(26, 26, 26, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #2a2a2a;
}

.search-box-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.search-input-group {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-autocomplete {
  flex: 1;
}

.search-autocomplete :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.search-autocomplete :deep(.el-input__wrapper:hover),
.search-autocomplete :deep(.el-input__wrapper.is-focus) {
  border-color: #8a2be2;
  box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2),
              0 8px 32px rgba(138, 43, 226, 0.2);
}

.search-autocomplete :deep(.el-input__prefix) {
  color: #8a2be2;
}

.search-btn {
  background: linear-gradient(135deg, #8a2be2, #6a1bb8);
  border: none;
  border-radius: 12px;
  padding: 0 2rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: linear-gradient(135deg, #9a3cf2, #7a2bc8);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(138, 43, 226, 0.4);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  color: #e0e0e0;
}

.suggestion-item .el-icon {
  color: #8a2be2;
}

/* 搜索提示 */
.hot-searches {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.hot-title {
  color: #888;
  font-size: 0.9rem;
}

.hot-tag {
  background: rgba(138, 43, 226, 0.1);
  border: 1px solid rgba(138, 43, 226, 0.2);
  color: #b0b0b0;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
}

.hot-tag:hover {
  background: rgba(138, 43, 226, 0.2);
  border-color: #8a2be2;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(138, 43, 226, 0.2);
}

/* 结果区域 */
.results-section {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-progress {
  max-width: 400px;
  margin: 0 auto 1.5rem;
}

.loading-text {
  color: #8a2be2;
  font-size: 1.1rem;
}

/* 无结果 */
.no-results {
  padding: 6rem 2rem;
  text-align: center;
}

.no-results-content .el-icon {
  font-size: 4rem;
  color: #666;
  margin-bottom: 1.5rem;
}

.no-results-content h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #fff;
}

.no-results-content p {
  color: #888;
  margin-bottom: 2rem;
}

/* 欢迎内容 */
.welcome-content {
  padding: 3rem 1rem;
}

.welcome-card {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid #333;
  border-radius: 20px;
  padding: 3rem;
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
  backdrop-filter: blur(10px);
}

.welcome-icon {
  font-size: 4rem;
  color: #8a2be2;
  margin-bottom: 1.5rem;
  filter: drop-shadow(0 0 20px rgba(138, 43, 226, 0.5));
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #8a2be2, #6a1bb8);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 1rem;
}

.welcome-description {
  color: #b0b0b0;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.welcome-features {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin-top: 2rem;
}

.feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: #b0b0b0;
  transition: all 0.3s ease;
}

.feature:hover {
  color: #8a2be2;
  transform: translateY(-5px);
}

.feature .el-icon {
  font-size: 2rem;
}

/* 结果头部 */
.results-header {
  margin-bottom: 2rem;
  padding: 1rem;
  background: rgba(26, 26, 26, 0.5);
  border-radius: 12px;
  border: 1px solid #333;
}

.results-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.4rem;
  color: #fff;
}

.results-title .el-icon {
  color: #8a2be2;
}

.query-highlight {
  color: #8a2be2;
  font-weight: 700;
}

.results-count {
  color: #888;
  font-size: 1rem;
  font-weight: normal;
}

/* 图片网格 */
.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.image-item {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid #333;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.image-item:hover {
  transform: translateY(-8px);
  border-color: #8a2be2;
  box-shadow: 0 15px 35px rgba(138, 43, 226, 0.25);
}

.image-container {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.image-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover .image-thumbnail {
  transform: scale(1.1);
}

.image-hover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-hover {
  opacity: 1;
}

.image-info {
  padding: 1.25rem;
}

.image-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.75rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-desc {
  color: #b0b0b0;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
  min-height: 2.7em;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #888;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-item .el-icon {
  flex-shrink: 0;
  font-size: 0.9rem;
}

.meta-item span {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-section {
    padding: 1.5rem 1rem;
  }
  
  .search-input-group {
    flex-direction: column;
  }
  
  .search-autocomplete {
    width: 100%;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .hot-searches {
    justify-content: center;
  }
  
  .results-section {
    padding: 1rem;
  }
  
  .welcome-features {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 480px) {
  .welcome-card {
    padding: 2rem 1rem;
  }
  
  .welcome-title {
    font-size: 1.5rem;
  }
  
  .images-grid {
    grid-template-columns: 1fr;
  }
  
  .image-meta {
    grid-template-columns: 1fr;
  }
}
</style>