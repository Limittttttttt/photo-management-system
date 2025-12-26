<template>
  <div class="gallery-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>图片库</h1>
      <div class="header-actions">
        <!-- 显示模式切换 -->
        <div class="view-mode-toggle">
          <button 
            class="btn-icon" 
            :class="{ active: viewMode === 'grid' }"
            @click="viewMode = 'grid'"
            title="网格视图"
          >
            <i class="icon-grid"></i>
          </button>
          <button 
            class="btn-icon" 
            :class="{ active: viewMode === 'thumbnail' }"
            @click="viewMode = 'thumbnail'"
            title="缩略图视图"
          >
            <i class="icon-thumbnail"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 搜索和排序卡片 -->
    <div class="search-sort-card">
      <div class="card-header">
        <h3>搜索与排序</h3>
      </div>
      <div class="search-sort-content">
        <!-- 搜索框 -->
        <div class="search-section">
          <div class="search-box">
            <input 
              v-model="filters.keyword" 
              placeholder="搜索图片名称、描述、标签等相关信息" 
              @input="onSearchInput"
            />
          </div>
        </div>

        <!-- 排序选项 -->
        <div class="sort-section">
          <label class="sort-label">排序方式:</label>
          <div class="sort-options">
            <button 
              class="sort-btn" 
              :class="{ active: sortBy === 'id' }"
              @click="setSort('id')"
            >
              上传时间
              <i v-if="sortBy === 'id'" class="icon-sort" :class="{ desc: sortOrder === 'desc' }"></i>
            </button>
            <button 
              class="sort-btn" 
              :class="{ active: sortBy === 'name' }"
              @click="setSort('name')"
            >
              图片名称
              <i v-if="sortBy === 'name'" class="icon-sort" :class="{ desc: sortOrder === 'desc' }"></i>
            </button>            
          </div>
        </div>
      </div>
    </div>

    <!-- 动态筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-main">
        <div class="filter-controls">
          <select v-model="newFilter.type" class="filter-type">
            <option value="">选择筛选类型</option>
            <option value="exif">EXIF信息</option>
            <option value="customTag">自定义标签</option>
          </select>

          <!-- EXIF筛选条件 -->
          <select v-if="newFilter.type === 'exif'" v-model="newFilter.exifField" class="filter-field">
            <option value="">选择EXIF字段</option>
            <option value="exifCameraModel">相机型号</option>
            <option value="exifLens">镜头信息</option>
            <option value="exifAperture">光圈</option>
            <option value="exifShutter">快门速度</option>
            <option value="exifISO">ISO</option>
            <option value="exifFocalLength">焦距</option>
          </select>

          <!-- 值输入 -->
          <input 
            v-if="newFilter.type && (newFilter.exifField || newFilter.type === 'customTag')"
            v-model="newFilter.value"
            :placeholder="getValuePlaceholder()"
            class="filter-value"
            @keyup.enter="addFilter"
          />

          <button 
            class="btn-primary btn-small" 
            @click="addFilter"
            :disabled="!canAddFilter"
          >
            添加条件
          </button>
        </div>
      </div>

      <!-- 当前筛选条件 -->
      <div v-if="activeFilters.length > 0" class="active-filters">
        <div class="filters-header">
          <span class="filters-title">当前筛选条件：</span>
          <button class="btn-secondary btn-small" @click="clearAllFilters">
            <i class="icon-clear"></i>
            清除所有
          </button>
        </div>
        <div class="filters-list">
          <div 
            v-for="(filter, index) in activeFilters" 
            :key="filter.id"
            class="filter-tag"
          >
            <span class="filter-text">
              {{ getFilterDisplayText(filter) }}
            </span>
            <i class="icon-close-small" @click="removeFilter(index)"></i>
          </div>
        </div>
        
        <!-- 筛选结果统计 -->
        <div class="filter-stats">
          <span class="stats-text">找到 {{ filteredPhotos.length }} 张图片</span>
          <span class="sort-indicator" v-if="sortBy !== 'id'">
            (按{{ getSortDisplayName() }}{{ sortOrder === 'desc' ? '降序' : '升序' }}排列)
          </span>
        </div>
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="photo-grid">
      <div 
        v-for="photo in filteredPhotos" 
        :key="photo.id"
        class="photo-card"
        @click="openPhotoDetail(photo.id)"
      >
        <div class="photo-thumbnail">
          <img :src="photo.image" :alt="photo.name" />
          <div class="photo-overlay">
            <div class="photo-info">
              <h4>{{ photo.name }}</h4>
              <p v-if="photo.exifCameraModel||photo.exifFocalLength" >{{ photo.exifCameraModel }} • {{ photo.exifFocalLength }}</p>
            </div>
          </div>
        </div>
        
        <div class="photo-meta">
          <div class="meta-item">
            <span class="label" v-if="photo.exifShootTime">拍摄时间</span>
            <span class="value">{{ photo.exifShootTime }}</span>
          </div>
          <div class="tags-preview">
            <span 
              v-for="tag in getPreviewTags(photo)" 
              :key="tag"
              class="tag-preview"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 缩略图视图 -->
    <div v-if="viewMode === 'thumbnail'" class="photo-thumbnails">
      <div 
        v-for="photo in filteredPhotos" 
        :key="photo.id"
        class="thumbnail-card"
        @click="openPhotoDetail(photo.id)"
      >
        <div class="thumbnail-image">
          <img :src="photo.thumbnail" :alt="photo.name" />
        </div>
        <div class="thumbnail-title">
          {{ photo.name }}
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredPhotos.length === 0" class="empty-state">
      <i class="icon-empty"></i>
      <h3 v-if="activeFilters.length > 0">没有找到符合条件的图片</h3>
      <h3 v-else>暂无图片</h3>
      <p v-if="activeFilters.length > 0">尝试调整筛选条件或清除所有筛选</p>
      <p v-else>点击上传按钮添加您的第一张图片</p>
      <button v-if="activeFilters.length > 0" class="btn-secondary" @click="clearAllFilters">
        <i class="icon-clear"></i>
        清除筛选条件
      </button>
      <button v-else class="btn-primary" @click="$router.push('/upload/' + userData.id)">
        <i class="icon-upload"></i>
        立即上传
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Gallery',
  data() {
    return {
      viewMode: 'grid',
      sortBy: 'id',
      sortOrder: 'desc',
      filters: {
        keyword: ''
      },
      newFilter: {
        type: '',
        exifField: '',
        value: ''
      },
      activeFilters: [],
      photos: [],
      allPhotos: [],
      userData: null
    }
  },
  
  computed: {
    canAddFilter() {
      if (!this.newFilter.type || !this.newFilter.value.trim()) return false
      
      if (this.newFilter.type === 'exif') {
        return this.newFilter.exifField && this.newFilter.value.trim()
      }
      return true
    },
    
    filteredPhotos() {
      let filtered = [...this.allPhotos]
      
      // 关键词搜索（名称、描述、标签的模糊搜索）
      if (this.filters.keyword.trim()) {
        const keyword = this.filters.keyword.trim().toLowerCase()
        filtered = filtered.filter(photo => {
          const nameMatch = photo.name && photo.name.toLowerCase().includes(keyword)
          const descMatch = photo.description && photo.description.toLowerCase().includes(keyword)
          const customTagMatch = photo.tags && photo.tags.some(tag => 
            tag.toLowerCase().includes(keyword)
          )
          
          return nameMatch || descMatch || customTagMatch
        })
      }
      
      // 动态筛选条件
      if (this.activeFilters.length > 0) {
        filtered = filtered.filter(photo => {
          return this.activeFilters.every(filter => {
            switch (filter.type) {
              case 'exif':
                const exifValue = photo[filter.exifField]
                if (!exifValue) return false
                return exifValue.toString().toLowerCase().includes(filter.value.toLowerCase())
                
              case 'customTag':
                if (!photo.tags || !photo.tags.length) return false
                return photo.tags.some(tag => 
                  tag.toLowerCase().includes(filter.value.toLowerCase())
                )
                
              default:
                return true
            }
          })
        })
      }
      
      // 排序
      return this.sortPhotos(filtered)
    }
  },
  
  created() {
    this.loadUserData()
    this.fetchPhotos()
  },

  methods: {
    loadUserData() {
      const user = localStorage.getItem('user')
      if (user) {
        this.userData = JSON.parse(user)
      }
    },

    openPhotoDetail(photoId) {
      const userId = this.userData.id
      this.$router.push(`/${userId}/photo/${photoId}`)
    },
    
    async fetchPhotos() {
      if (!this.userData) return
      
      this.loading = true
      try {
        const userId = this.userData.id
        const response = await axios.get(`/gallery/user/${userId}`)
        
        if (response.data) {
          const photos = response.data.map(image => ({
            id: image.id,
            name: image.name || '未命名图片',
            description: image.description || '',
            size: image.size || 0,
            image: image.image ? `data:image/jpeg;base64,${image.image}` : '',
            thumbnail: image.thumbnail ? `data:image/jpeg;base64,${image.thumbnail}` : '',
            exifShootTime: image.exifShootTime || '',
            exifLocation: image.exifLocation || '',
            exifResolution: image.exifResolution || '',
            exifCameraModel: image.exifCameraModel || '',
            exifLens: image.exifLens || '',
            exifAperture: image.exifAperture || '',
            exifShutter: image.exifShutter || '',
            exifISO: image.exifISO || '',
            exifFocalLength: image.exifFocalLength || '',
            tags: image.tags ? image.tags.map(tag => tag.tagName) : []
          }))
          
          this.allPhotos = photos
        }
      } catch (error) {
        console.error('Error fetching photos:', error)
        ElMessage.error('获取图片失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    sortPhotos(photos = this.allPhotos) {
      return [...photos].sort((a, b) => {
        let aValue, bValue
        
        switch (this.sortBy) {
          case 'id':
            aValue = parseInt(a.id) || 0
            bValue = parseInt(b.id) || 0
            break
          case 'name':
            const aName = a.name || ''
            const bName = b.name || ''
            aValue = aName.toString().toLowerCase()
            bValue = bName.toString().toLowerCase()
            break
          default:
            return 0
        }
        
        if (this.sortOrder === 'desc') {
          return aValue < bValue ? 1 : aValue > bValue ? -1 : 0
        } else {
          return aValue > bValue ? 1 : aValue < bValue ? -1 : 0
        }
      })
    },
    
    setSort(type) {
      if (this.sortBy === type) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortBy = type
        this.sortOrder = 'desc'
      }
    },
    
    getSortDisplayName() {
      const names = {
        id: '上传时间',
        name: '图片名称'
      }
      return names[this.sortBy] || this.sortBy
    },
    
    getValuePlaceholder() {
      if (this.newFilter.type === 'exif') {
        const fieldNames = {
          exifCameraModel: '相机型号',
          exifLens: '镜头信息',
          exifAperture: '光圈',
          exifShutter: '快门速度',
          exifISO: 'ISO',
          exifFocalLength: '焦距'
        }
        return `输入${fieldNames[this.newFilter.exifField] || this.newFilter.exifField}值`
      } else if (this.newFilter.type === 'customTag') {
        return '输入自定义标签'
      }
      return '输入值'
    },
    
    addFilter() {
      if (!this.canAddFilter) return
      
      const newFilter = {
        id: Date.now(),
        type: this.newFilter.type,
        exifField: this.newFilter.exifField,
        value: this.newFilter.value.trim()
      }
      
      this.activeFilters.push(newFilter)
      this.newFilter.type = ''
      this.newFilter.exifField = ''
      this.newFilter.value = ''
    },
    
    removeFilter(index) {
      this.activeFilters.splice(index, 1)
    },
    
    clearAllFilters() {
      this.activeFilters = []
      this.filters.keyword = ''
    },
    
    getFilterDisplayText(filter) {
      if (filter.type === 'exif') {
        const fieldNames = {
          exifCameraModel: '相机型号',
          exifLens: '镜头信息',
          exifAperture: '光圈',
          exifShutter: '快门速度',
          exifISO: 'ISO',
          exifFocalLength: '焦距'
        }
        return `${fieldNames[filter.exifField] || filter.exifField}: ${filter.value}`
      } else if (filter.type === 'customTag') {
        return `自定义标签: ${filter.value}`
      }
      return ''
    },
    
    onSearchInput() {
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        // 搜索在计算属性中处理
      }, 500)
    },
    
    getPreviewTags(photo) {
      return photo.tags ? photo.tags.slice(0, 3) : []
    }
  }
}
</script>

<style scoped>
.gallery-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #0a0a0a;
  color: #e0e0e0;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #333;
}

.page-header h1 {
  color: #ffffff;
  font-size: 2rem;
  margin: 0;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

/* 搜索和排序卡片 */
.search-sort-card {
  background: #1a1a1a;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  margin-bottom: 1.5rem;
  overflow: hidden;
  border: 1px solid #333;
}

.search-sort-card .card-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #333;
  background: #2a2a2a;
}

.search-sort-card .card-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 1.1rem;
  font-weight: 600;
}

.search-sort-content {
  padding: 1.5rem;
  display: flex;
  gap: 2rem;
  align-items: center;
}

.search-section {
  flex: 1;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid #444;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: #2a2a2a;
  color: #e0e0e0;
}

.search-box input:focus {
  border-color: #8a2be2;
  box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}

.search-box input::placeholder {
  color: #666;
}

.search-box .icon-search {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.sort-label {
  color: #b0b0b0;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.sort-options {
  display: flex;
  gap: 0.5rem;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 1px solid #444;
  border-radius: 6px;
  background: #2a2a2a;
  color: #b0b0b0;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  white-space: nowrap;
}

.sort-btn:hover {
  border-color: #8a2be2;
  color: #8a2be2;
}

.sort-btn.active {
  background: #8a2be2;
  color: white;
  border-color: #8a2be2;
  box-shadow: 0 0 8px rgba(138, 43, 226, 0.3);
}

.icon-sort {
  transition: transform 0.3s ease;
  margin-left: 0.25rem;
}

.icon-sort.desc {
  transform: rotate(180deg);
}

/* 显示模式切换 */
.view-mode-toggle {
  display: flex;
  background: #2a2a2a;
  border-radius: 8px;
  padding: 0.25rem;
  border: 1px solid #444;
}

.btn-icon {
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  color: #b0b0b0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: #333;
  color: #e0e0e0;
}

.btn-icon.active {
  background: #8a2be2;
  color: white;
  box-shadow: 0 0 8px rgba(138, 43, 226, 0.3);
}

/* 筛选工具栏 */
.filter-toolbar {
  background: #1a1a1a;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  margin-bottom: 2rem;
  overflow: hidden;
  border: 1px solid #333;
}

.filter-main {
  padding: 1.5rem;
  border-bottom: 1px solid #333;
}

.filter-controls {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.filter-type,
.filter-field,
.filter-value {
  padding: 0.5rem 0.75rem;
  border: 1px solid #444;
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 120px;
  background: #2a2a2a;
  color: #e0e0e0;
}

.filter-value {
  flex: 1;
  min-width: 150px;
}

.filter-type:focus,
.filter-field:focus,
.filter-value:focus {
  border-color: #8a2be2;
  outline: none;
}

/* 当前筛选条件 */
.active-filters {
  padding: 1.5rem;
  background: #2a2a2a;
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.filters-title {
  font-weight: 500;
  color: #e0e0e0;
}

.filters-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #1a1a1a;
  padding: 0.4rem 0.8rem;
  border-radius: 16px;
  border: 1px solid #444;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.filter-tag:hover {
  border-color: #8a2be2;
  box-shadow: 0 0 8px rgba(138, 43, 226, 0.2);
}

.filter-text {
  color: #e0e0e0;
}

.filter-stats {
  text-align: center;
}

.stats-text {
  color: #b0b0b0;
  font-size: 0.9rem;
}

.sort-indicator {
  color: #8a2be2;
  font-size: 0.8rem;
  margin-left: 0.5rem;
}

/* 网格视图 */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.photo-card {
  background: #1a1a1a;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #333;
}

.photo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.7);
  border-color: #8a2be2;
}

.photo-thumbnail {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.photo-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.photo-card:hover .photo-thumbnail img {
  transform: scale(1.05);
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: flex-end;
  padding: 1rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.photo-card:hover .photo-overlay {
  opacity: 1;
}

.photo-info {
  color: white;
}

.photo-info h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
}

.photo-info p {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.8;
}

.photo-meta {
  padding: 1rem;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-item .label {
  color: #b0b0b0;
  font-size: 0.8rem;
}

.meta-item .value {
  color: #e0e0e0;
  font-size: 0.8rem;
  font-weight: 500;
}

.tags-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-top: 0.5rem;
}

.tag-preview {
  background: rgba(138, 43, 226, 0.2);
  color: #b57aff;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  border: 1px solid rgba(138, 43, 226, 0.4);
}

/* 列表视图 */
.photo-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.list-item {
  display: flex;
  background: #1a1a1a;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #333;
}

.list-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.7);
  border-color: #8a2be2;
}

.list-thumbnail {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.list-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  margin-top: 0.5rem;
  margin-left: 0.5rem;
}

.list-content {
  flex: 1;
  display: flex;
  padding: 1rem;
  gap: 1.5rem;
}

.list-main {
  flex: 1;
}

.list-title {
  margin: 0 0 0.5rem 0;
  color: #ffffff;
  font-size: 1.1rem;
}

.list-description {
  margin: 0 0 0.75rem 0;
  color: #b0b0b0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.list-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.list-tag {
  background: rgba(138, 43, 226, 0.2);
  color: #b57aff;
  padding: 0.3rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  border: 1px solid rgba(138, 43, 226, 0.4);
}

.list-meta {
  min-width: 200px;
  border-left: 1px solid #333;
  padding-left: 1.5rem;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.meta-row:last-child {
  margin-bottom: 0;
}

.meta-label {
  color: #b0b0b0;
  font-size: 0.8rem;
  font-weight: 500;
}

.meta-value {
  color: #e0e0e0;
  font-size: 0.8rem;
}

/* 缩略图视图 */
.photo-thumbnails {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.thumbnail-card {
  background: #1a1a1a;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  border: 1px solid #333;
}

.thumbnail-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.7);
  border-color: #8a2be2;
}

.thumbnail-image {
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.thumbnail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.thumbnail-card:hover .thumbnail-image img {
  transform: scale(1.05);
}

.thumbnail-title {
  padding: 0.75rem;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 500;
  color: #e0e0e0;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #b0b0b0;
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 1rem;
  display: block;
  color: #666;
}

.empty-state h3 {
  color: #ffffff;
  margin: 0 0 1rem 0;
}

.empty-state p {
  margin: 0 0 2rem 0;
}

/* 按钮样式 */
.btn-primary, .btn-secondary {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: #8a2be2;
  color: white;
  box-shadow: 0 4px 15px rgba(138, 43, 226, 0.3);
}

.btn-primary:hover {
  background: #9b45f0;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(138, 43, 226, 0.4);
}

.btn-secondary {
  background: #2a2a2a;
  color: #e0e0e0;
  border: 1px solid #444;
}

.btn-secondary:hover {
  background: #333;
  border-color: #666;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-small {
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
}

/* 图标样式 */
[class^="icon-"] {
  font-style: normal;
}

.icon-close-small:before { content: "✕"; font-size: 0.7rem; color: #999; cursor: pointer; }
.icon-close-small:hover:before { color: #ff4757; }
.icon-grid:before { content: "▦"; }
.icon-thumbnail:before { content: "☰"; font-size: 0.9rem; }
.icon-sort:before { content: "↓"; font-size: 0.7rem; }

/* 响应式设计 */
@media (max-width: 1024px) {
  .photo-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
  
  .photo-thumbnails {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
  
  .list-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .list-meta {
    border-left: none;
    border-top: 1px solid #333;
    padding-left: 0;
    padding-top: 1rem;
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .gallery-page {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .search-sort-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .search-box {
    max-width: none;
  }
  
  .sort-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .sort-options {
    width: 100%;
    justify-content: space-between;
  }
  
  .sort-btn {
    flex: 1;
    justify-content: center;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-type,
  .filter-field,
  .filter-value {
    width: 100%;
  }
  
  .photo-grid {
    grid-template-columns: 1fr;
  }
  
  .photo-thumbnails {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.75rem;
  }
  
  .thumbnail-image {
    height: 100px;
  }
  
  .thumbnail-title {
    padding: 0.5rem;
    font-size: 0.75rem;
  }
  
  .list-item {
    flex-direction: column;
  }
  
  .list-thumbnail {
    width: 100%;
    height: 150px;
  }
}

@media (max-width: 480px) {
  .photo-thumbnails {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0.5rem;
  }
  
  .thumbnail-image {
    height: 80px;
  }
  
  .thumbnail-title {
    padding: 0.4rem;
    font-size: 0.7rem;
  }
}
</style>