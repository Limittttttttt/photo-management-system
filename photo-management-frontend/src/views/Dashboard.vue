<template>
  <div class="dashboard-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎回到IManeger！</h1>
        <p>管理您的摄影作品，发现创作灵感</p>
      </div>
      <div class="banner-graphic">
        <i class="el-icon-camera-solid"></i>
      </div>
    </div>

    <!-- 有精选图片时显示图片轮播 -->
    <div v-if="hasSelectedImages && carouselImages.length > 0" class="carousel-section">
      <h2>精选作品</h2>
      <div class="carousel-container">
        <div class="carousel-wrapper">
          <div 
            class="carousel" 
            :style="{ transform: `translateX(-${currentIndex * 100}%)`, transition: transitionEnabled ? 'transform 0.6s ease' : 'none' }"
          >
            <div 
              v-for="(image, index) in displayImages" 
              :key="image.id || index"
              class="carousel-item"
              @click="goToImageDetail(realIndex(index))"
            >
              <img :src="image.url" :alt="image.title" class="carousel-image" />
              <div class="carousel-overlay">
                <h3>{{ image.title }}</h3>
                <p class="carousel-description">{{ image.description }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 轮播指示器 -->
        <div v-if="carouselImages.length > 1" class="carousel-indicators">
          <div 
            v-for="(image, index) in carouselImages" 
            :key="image.id || index"
            class="indicator"
            :class="{ active: currentRealIndex === index }"
            @click="goToImage(index)"
          ></div>
        </div>
        
        <!-- 导航按钮 -->
        <button v-if="carouselImages.length > 1" class="carousel-btn prev" @click="prevImage">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <button v-if="carouselImages.length > 1" class="carousel-btn next" @click="nextImage">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 功能模块 -->
    <div class="features-section">
      <h2>快速开始</h2>
      <div class="features-grid">
        <el-card class="feature-card" shadow="hover" @click="goToUpload">
          <div class="feature-icon" style="background: linear-gradient(135deg, #8a2be2, #4b0082);">
            <i class="el-icon-upload"></i>
          </div>
          <h3>上传编辑</h3>
          <p>上传新图片并进行编辑</p>
          <el-button type="primary" text>开始使用 →</el-button>
        </el-card>

        <el-card class="feature-card" shadow="hover" @click="goToGallery">
          <div class="feature-icon" style="background: linear-gradient(135deg, #ff00ff, #8b008b);">
            <i class="el-icon-picture"></i>
          </div>
          <h3>浏览图库</h3>
          <p>查看和管理所有图片</p>
          <el-button type="primary" text>浏览图片 →</el-button>
        </el-card>

        <el-card class="feature-card" shadow="hover" @click="goToAiSearch">
          <div class="feature-icon" style="background: linear-gradient(135deg, #5bdb00, #4b7000);">
            <i class="el-icon-picture"></i>
          </div>
          <h3>AI搜索</h3>
          <p>智能识别和匹配图片</p>
          <el-button type="primary" text>智能搜索 →</el-button>
        </el-card>

        <el-card class="feature-card" shadow="hover" @click="goToSettings">
          <div class="feature-icon" style="background: linear-gradient(135deg, #00ffff, #008b8b);">
            <i class="el-icon-setting"></i>
          </div>
          <h3>个性设置</h3>
          <p>个性化您的图片管理器</p>
          <el-button type="primary" text>前往设置 →</el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      userId: null,
      currentIndex: 1,
      currentRealIndex: 0,
      transitionEnabled: true,
      autoPlayTimer: null,
      carouselImages: [],
      selectedImageIds: [],
      hasSelectedImages: false,
      baseURL: 'http://localhost:8088',
      // 轮播状态管理
      isCarouselReady: false
    }
  },
  computed: {
    displayImages() {
      if (this.carouselImages.length === 0) {
        return []
      }
      const images = this.carouselImages
      // 克隆最后一张放在开头，克隆第一张放在结尾，实现无缝轮播
      return [
        images[images.length - 1],
        ...images,
        images[0]
      ]
    }
  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      const user = localStorage.getItem('user')
      if (user) {
        const userData = JSON.parse(user)
        this.userId = userData.id
        
        const routeId = parseInt(this.$route.params.id)
        if (routeId !== this.userId) {
          this.$router.push(`/dashboard/${this.userId}`)
          return
        }
        
        await this.loadSelectedImages()
      } else {
        this.$router.push('/login')
      }
    },
    
    async loadSelectedImages() {
      try {
        const response = await fetch(`${this.baseURL}/setting/images/${this.userId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        const data = await response.json()
        
        if (data.success && data.data) {
          this.selectedImageIds = data.data
          this.hasSelectedImages = this.selectedImageIds.length > 0
          
          if (this.hasSelectedImages) {
            await this.loadImageDetails()
          } else {
            this.carouselImages = []
            this.isCarouselReady = false
          }
        }
      } catch (error) {
        console.error('加载精选图片失败:', error)
        this.hasSelectedImages = false
        this.isCarouselReady = false
      }
    },
    
    async loadImageDetails() {
      this.carouselImages = []
      this.isCarouselReady = false
      this.currentIndex = 1
      this.currentRealIndex = 0
      
      // 有图片才加载
      if (this.selectedImageIds.length === 0) {
        this.isCarouselReady = false
        return
      }
      
      const imagePromises = this.selectedImageIds.map(async (imageId) => {
        try {
          const response = await fetch(`${this.baseURL}/setting/${imageId}/basic`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json'
            }
          })
          
          const data = await response.json()
          
          if (data.success && data.data) {
            const imageData = data.data
            
            if (imageData.image) {
              return {
                id: imageId,
                url: this.getImageUrl(imageData),
                title: imageData.name || '未命名',
                description: imageData.description || '暂无描述'
              }
            }
          }
        } catch (error) {
          console.error(`加载图片 ${imageId} 详情失败:`, error)
          return null
        }
      })
      
      const images = await Promise.all(imagePromises)
      const validImages = images.filter(img => img !== null)
      
      if (validImages.length > 0) {
        this.carouselImages = validImages
        this.isCarouselReady = true
        this.hasSelectedImages = true
        
        // 延迟启动自动轮播，确保DOM已经更新
        this.$nextTick(() => {
          setTimeout(() => {
            this.startAutoPlay()
          }, 500)
        })
      } else {
        this.hasSelectedImages = false
        this.isCarouselReady = false
      }
    },
    
    getImageUrl(imageData) {
      if (!imageData || !imageData.image) return ''
      
      // 如果已经是完整的data URL，直接返回
      if (typeof imageData.image === 'string' && imageData.image.startsWith('data:image')) {
        return imageData.image
      }
      
      // 处理可能的base64数据
      let base64Str = ''
      if (typeof imageData.image === 'string') {
        base64Str = imageData.image
      } else if (imageData.image instanceof ArrayBuffer || imageData.image instanceof Uint8Array) {
        // 将二进制数据转换为base64
        const bytes = new Uint8Array(imageData.image)
        let binary = ''
        for (let i = 0; i < bytes.byteLength; i++) {
          binary += String.fromCharCode(bytes[i])
        }
        base64Str = window.btoa(binary)
      }
      
      // 确保是有效的base64数据
      if (base64Str) {
        // 检查是否已经是base64格式
        if (!base64Str.startsWith('data:image')) {
          return `data:image/jpeg;base64,${base64Str}`
        }
        return base64Str
      }
      
      return ''
    },
    
    // 简化图片数据转换方法
    arrayBufferToBase64(buffer) {
      if (!buffer) return ''
      if (typeof buffer === 'string') return buffer
      
      let binary = ''
      const bytes = new Uint8Array(buffer)
      for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i])
      }
      return window.btoa(binary)
    },
    
    // 其他导航方法
    goToUpload() {
      this.$router.push(`/upload/${this.userId}`)
    },
    
    goToGallery() {
      this.$router.push(`/gallery/${this.userId}`)
    },
    
    goToSettings() {
      this.$router.push(`/settings/${this.userId}`)
    },

    goToAiSearch() {
      this.$router.push(`/aisearch/${this.userId}`)
    },
    
    goToImageDetail(index) {
      const imageId = this.selectedImageIds[index]
      if (imageId) {
        if (this.$router.hasRoute('ImageDetail')) {
          this.$router.push({ name: 'ImageDetail', params: { id: imageId } })
        } else {
          this.$router.push(`/${this.userId}/photo/${imageId}`)
        }
      }
    },
    
    realIndex(displayIndex) {
      const total = this.carouselImages.length
      if (displayIndex === 0) return total - 1
      if (displayIndex === this.displayImages.length - 1) return 0
      return displayIndex - 1
    },
    
    nextImage() {
      // 确保有图片且轮播已就绪
      if (!this.isCarouselReady || this.carouselImages.length <= 1) return
      
      this.transitionEnabled = true
      this.currentIndex++
      this.currentRealIndex = this.realIndex(this.currentIndex)
      
      // 检查是否到达克隆的最后一幅图片
      if (this.currentIndex === this.displayImages.length - 1) {
        setTimeout(() => {
          this.transitionEnabled = false
          this.currentIndex = 1
          this.currentRealIndex = this.realIndex(this.currentIndex)
        }, 600) // 等待过渡完成
      }
    },
    
    prevImage() {
      if (!this.isCarouselReady || this.carouselImages.length <= 1) return
      
      this.transitionEnabled = true
      this.currentIndex--
      this.currentRealIndex = this.realIndex(this.currentIndex)
      
      // 检查是否到达克隆的第一幅图片
      if (this.currentIndex === 0) {
        setTimeout(() => {
          this.transitionEnabled = false
          this.currentIndex = this.displayImages.length - 2
          this.currentRealIndex = this.realIndex(this.currentIndex)
        }, 600)
      }
    },
    
    goToImage(index) {
      if (!this.isCarouselReady) return
      
      this.transitionEnabled = true
      this.currentIndex = index + 1
      this.currentRealIndex = index
    },
    
    startAutoPlay() {
      // 停止现有的定时器
      this.stopAutoPlay()
      
      // 只有有图片且多于1张时才启动自动轮播
      if (this.isCarouselReady && this.carouselImages.length > 1) {
        this.autoPlayTimer = setInterval(() => {
          this.nextImage()
        }, 2000) // 每2秒切换一次
      }
    },
    
    stopAutoPlay() {
      if (this.autoPlayTimer) {
        clearInterval(this.autoPlayTimer)
        this.autoPlayTimer = null
      }
    },
    
    // 鼠标悬停暂停功能
    handleCarouselMouseEnter() {
      this.stopAutoPlay()
    },
    
    handleCarouselMouseLeave() {
      this.startAutoPlay()
    }
  },
  mounted() {
    // 等待数据加载完成后再启动轮播
    setTimeout(() => {
      if (this.isCarouselReady) {
        this.startAutoPlay()
      }
    }, 1000)
  },
  beforeUnmount() {
    this.stopAutoPlay()
  },
  
  watch: {
    '$route.params.id': {
      async handler(newId) {
        await this.loadUserInfo()
      },
      immediate: true
    },
    // 监听轮播就绪状态
    isCarouselReady(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.startAutoPlay()
        })
      } else {
        this.stopAutoPlay()
      }
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #0a0a0a;
  color: #e0e0e0;
  min-height: 100vh;
}

.welcome-banner {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  border-radius: 16px;
  padding: 3rem 2rem;
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid #333;
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #8a2be2, #ff00ff, #00ffff);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.banner-content h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.banner-content p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0;
}

.banner-graphic i {
  font-size: 4rem;
  opacity: 0.8;
  color: #8a2be2;
  filter: drop-shadow(0 0 8px rgba(138, 43, 226, 0.7));
}

/* 轮播区域 */
.carousel-section {
  margin-bottom: 3rem;
}

.carousel-section h2 {
  color: #e0e0e0;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.2);
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 450px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.7);
  background: #1a1a1a;
  border: 1px solid #333;
}

.carousel-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel {
  display: flex;
  height: 100%;
  will-change: transform;
}

.carousel-item {
  flex: 0 0 100%;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  min-width: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.8s ease;
}

.carousel-item:hover .carousel-image {
  transform: scale(1.08);
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.85));
  color: white;
  padding: 2.5rem;
  opacity: 0;
  transition: all 0.4s ease;
  transform: translateY(20px);
}

.carousel-item:hover .carousel-overlay {
  opacity: 1;
  transform: translateY(0);
}

.carousel-overlay h3 {
  font-size: 1.6rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
}

.carousel-description {
  margin: 0;
  opacity: 0.95;
  font-size: 1.05rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
}

/* 轮播指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.75rem;
  z-index: 10;
  background: rgba(0, 0, 0, 0.6);
  padding: 0.75rem 1rem;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid #444;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.indicator.active {
  background: #8a2be2;
  transform: scale(1.4);
  box-shadow: 0 0 10px rgba(138, 43, 226, 0.7);
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: scale(1.2);
}

/* 导航按钮 */
.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(30, 30, 30, 0.9);
  border: 1px solid #444;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  z-index: 10;
  backdrop-filter: blur(10px);
}

.carousel-btn:hover {
  background: rgba(50, 50, 50, 0.9);
  transform: translateY(-50%) scale(1.15);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.7);
  border-color: #8a2be2;
}

.carousel-btn:active {
  transform: translateY(-50%) scale(1.05);
}

.carousel-btn.prev {
  left: 2rem;
}

.carousel-btn.next {
  right: 2rem;
}

.carousel-btn svg {
  width: 24px;
  height: 24px;
  color: #e0e0e0;
  transition: transform 0.2s ease;
}

.carousel-btn:hover svg {
  transform: scale(1.1);
  color: #8a2be2;
}

/* 功能模块 */
.features-section {
  margin-bottom: 3rem;
}

.features-section h2 {
  color: #e0e0e0;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.2);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  border-radius: 16px;
  border: 1px solid #333;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 1.5rem;
  background: #1a1a1a;
  color: #e0e0e0;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7);
  border-color: #555;
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.5);
}

.feature-icon i {
  font-size: 2.5rem;
  color: white;
  filter: drop-shadow(0 0 5px rgba(255, 255, 255, 0.5));
}

.feature-card h3 {
  color: #ffffff;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
}

.feature-card p {
  color: #b0b0b0;
  margin-bottom: 1rem;
  line-height: 1.5;
}

/* 按钮样式 */
:deep(.el-button) {
  color: #8a2be2;
}

:deep(.el-button:hover) {
  color: #a64dff;
}

:deep(.el-card) {
  background-color: #1a1a1a;
  border: 1px solid #333;
}

:deep(.el-card:hover) {
  border-color: #555;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-page {
    padding: 1rem;
  }
  
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
    padding: 2rem 1rem;
  }
  
  .banner-content h1 {
    font-size: 2rem;
  }
  
  .carousel-container {
    height: 350px;
  }
  
  .carousel-overlay {
    padding: 1.5rem;
  }
  
  .carousel-overlay h3 {
    font-size: 1.3rem;
  }
  
  .carousel-description {
    font-size: 0.95rem;
  }
  
  .carousel-btn {
    width: 44px;
    height: 44px;
  }
  
  .carousel-btn.prev {
    left: 1rem;
  }
  
  .carousel-btn.next {
    right: 1rem;
  }
  
  .carousel-btn svg {
    width: 20px;
    height: 20px;
  }
  
  .carousel-indicators {
    bottom: 1.5rem;
    padding: 0.5rem 0.75rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>