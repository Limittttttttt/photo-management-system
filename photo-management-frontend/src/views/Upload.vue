<template>
  <div class="upload-page">
    <div class="page-header">
      <h1>图片上传与编辑</h1>
      <p>上传您的摄影作品并进行编辑</p>
    </div>

    <!-- 主功能导航 -->
    <div class="nav-tabs">
      <div 
        class="nav-tab" 
        :class="{ active: activeTab === 'upload' }"
        @click="activeTab = 'upload'"
      >
        <el-icon><Upload /></el-icon>
        图片上传
      </div>
      <div 
        class="nav-tab" 
        :class="[
          { active: activeTab === 'edit' },
          { disabled: !selectedFile }
        ]"
        @click="handleEditTabClick"
      >
        <el-icon><Edit /></el-icon>
        图片编辑
      </div>
    </div>

    <!-- 上传区域 -->
    <div v-if="activeTab === 'upload'" class="upload-section">
      <div class="upload-card">
        <div class="card-header">
          <h3>选择图片</h3>
        </div>

        <div 
          class="upload-area"
          @click="triggerFileInput"
          @drop.prevent="handleDrop"
          @dragover.prevent="handleDragOver"
          :class="{ 'drag-over': isDragOver }"
        >
          <div class="upload-placeholder">
            <el-icon size="60" color="#8a2be2"><UploadFilled /></el-icon>
            <h3>拖拽图片到此处或点击选择</h3>
            <p>支持 JPG、PNG、GIF、WEBP 格式，单文件不超过 10MB</p>
          </div>
        </div>

        <!-- 选择的图片预览 -->
        <div v-if="selectedFile" class="image-preview">
          <div class="preview-header">
            <span>已选择图片</span>
            <div class="preview-actions">
                {{ extractingExif ? '提取EXIF中...' : null }}
              <el-button type="danger" @click="clearSelection">
                <el-icon><Delete /></el-icon>
                清除
              </el-button>
            </div>
          </div>
          <div class="preview-content">
            <img :src="imagePreviewUrl" :alt="selectedFile.name" />
            <div class="image-info">
              <p><strong>文件名:</strong> {{ selectedFile.name }}</p>
              <p><strong>大小:</strong> {{ formatFileSize(selectedFile.size) }}</p>
              <p><strong>类型:</strong> {{ selectedFile.type }}</p>
            </div>
          </div>
        </div>

        <!-- EXIF信息面板 -->
        <div v-if="selectedFile && exifData" class="exif-panel">
          <div class="exif-card">
            <div class="card-header">
              <h3>图片信息编辑</h3>
            </div>
            <div class="exif-content">
            <div class="info-editor">
            <div class="form-group">
              <label for="imageName">图片名称</label>
              <el-input
                id="imageName"
                v-model="imageName"
                placeholder="请输入图片名称"
                clearable
                :maxlength="100"
                show-word-limit
              />
            </div>

            <div class="form-group">
              <label for="imageDescription">图片描述</label>
              <el-input
                id="imageDescription"
                v-model="imageDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入图片描述"
                :maxlength="500"
                show-word-limit
              />
            </div>

            <div class="file-info-summary">
              <div class="info-row">
                <span class="label">原始文件名：</span>
                <span class="value">{{ selectedFile.name }}</span>
              </div>
              <div class="info-row">
                <span class="label">文件大小：</span>
                <span class="value">{{ formatFileSize(selectedFile.size) }}</span>
              </div>
              <div class="info-row">
                <span class="label">文件类型：</span>
                <span class="value">{{ selectedFile.type }}</span>
              </div>
            </div>
          </div>
          </div>

            <div class="card-header">
              <h3>EXIF信息</h3>
            </div>
            <div class="exif-content">
              <div class="exif-group">
                <h4>拍摄信息</h4>
                <div class="exif-item">
                  <span class="label">拍摄时间:</span>
                  <span class="value">{{ formatDateTime(exifData.shootTime) }}</span>
                </div>
                <div class="exif-item" ">
                  <span class="label">拍摄地点:</span>
                  <span class="value">{{ exifData.location || '未知'}}</span>
                </div>
                <div class="exif-item">
                  <span class="label">分辨率:</span>
                  <span class="value">{{ exifData.resolution || '未知' }}</span>
                </div>
              </div>
              
              <div class="exif-group">
                <h4>相机信息</h4>
                <div class="exif-item">
                  <span class="label">相机型号:</span>
                  <span class="value">{{ exifData.cameraModel || '未知' }}</span>
                </div>
                <div class="exif-item">
                  <span class="label">镜头信息:</span>
                  <span class="value">{{ exifData.lens || '未知' }}</span>
                </div>
                <div class="exif-item" v-if="exifData.focalLength">
                  <span class="label">焦距:</span>
                  <span class="value">{{ exifData.focalLength }}mm</span>
                </div>
              </div>
              
              <div class="exif-group">
                <h4>拍摄参数</h4>
                <div class="exif-item" v-if="exifData.aperture">
                  <span class="label">光圈:</span>
                  <span class="value">f/{{ exifData.aperture || '未知'}}</span>
                </div>
                <div class="exif-item" >
                  <span class="label">快门速度:</span>
                  <span class="value">{{ exifData.shutter || '未知'}}</span>
                </div>
                <div class="exif-item">
                  <span class="label">ISO:</span>
                  <span class="value">{{ exifData.iso || '未知'}}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑区域 -->
    <div v-if="activeTab === 'edit' && selectedFile" class="edit-section">
      <!-- 编辑功能导航 -->
      <div class="edit-tabs">
        <div 
          class="edit-tab"
          :class="{ active: activeEditTab === 'transform' }"
          @click="activeEditTab = 'transform'"
        >
          <el-icon><Crop /></el-icon>
          变换调整
        </div>
        <div 
          class="edit-tab"
          :class="{ active: activeEditTab === 'adjust' }"
          @click="activeEditTab = 'adjust'"
        >
          <el-icon><Operation/></el-icon>
          基础调整
        </div>
        <div 
          class="edit-tab"
          :class="{ active: activeEditTab === 'tags' }"
          @click="activeEditTab = 'tags'"
        >
          <el-icon><PriceTag /></el-icon>
          标签管理
        </div>
      </div>

      <div class="edit-layout">
        <!-- 实时预览 -->
        <div class="preview-section">
          <div class="preview-card">
            <div class="card-header">
              <h3>实时预览</h3>
              <div class="action-buttons">
                <el-button type="primary" @click="uploadImage" :loading="uploading">
                  <el-icon><Upload /></el-icon>
                  {{ uploading ? '上传中...' : '上传图片' }}
                </el-button>
              </div>
            </div>
            
            <div class="preview-content-large">
              <canvas ref="previewCanvas" class="preview-canvas"></canvas>
              <div v-if="showEditOverlay" class="preview-overlay">
                <div class="overlay-text">编辑效果预览</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑工具 -->
        <div class="edit-tools-section">
          <!-- 图片变换 -->
          <div v-if="activeEditTab === 'transform'" class="edit-tool-card">
            <div class="card-header">
              <h3>变换调整</h3>
            </div>

            <div class="transform-controls">
              <div class="control-group">
                <h4>缩放比例</h4>
                <div class="slider-container">
                  <el-slider 
                    v-model="scale" 
                    :min="10" 
                    :max="300"
                    :step="1"
                    show-input
                    @input="applyTransform"
                  />
                </div>
                <div class="value-display">{{ scale }}%</div>
              </div>

              <div class="control-group">
                <h4>旋转角度</h4>
                <div class="slider-container">
                  <el-slider 
                    v-model="rotation" 
                    :min="0" 
                    :max="360"
                    :step="1"
                    show-input
                    @input="applyTransform"
                  />
                </div>
                <div class="rotation-buttons">
                  <el-button type="default" @click="rotate(-90)" title="向左旋转90度">
                    <el-icon><RefreshLeft /></el-icon>
                    左旋转90°
                  </el-button>
                  <el-button type="default" @click="rotate(90)" title="向右旋转90度">
                    <el-icon><RefreshRight /></el-icon>
                    右旋转90°
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 基础调整 -->
          <div v-if="activeEditTab === 'adjust'" class="edit-tool-card">
            <div class="card-header">
              <h3>基础调整</h3>
            </div>

            <div class="adjust-controls">
              <div class="control-group" v-for="control in basicControls" :key="control.name">
                <h4>{{ control.label }}</h4>
                <div class="slider-container">
                  <el-slider 
                    v-model="control.value" 
                    :min="control.min" 
                    :max="control.max"
                    :step="control.step"
                    show-input
                    @input="applyFilters"
                  />
                </div>
              </div>

              <div class="control-group" v-for="control in colorControls" :key="control.name">
                <h4>{{ control.label }}</h4>
                <div class="slider-container">
                  <el-slider 
                    v-model="control.value" 
                    :min="control.min" 
                    :max="control.max"
                    :step="control.step"
                    show-input
                    @input="applyFilters"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 标签管理 -->
          <div v-if="activeEditTab === 'tags'" class="edit-tool-card">
            <div class="card-header">
              <h3>标签管理</h3>
            </div>

            <div class="tags-container">
              <!-- 自定义标签 -->
              <div class="tags-section">
                <h4>添加标签</h4>
                <div class="tag-input-group">
                  <el-input
                    v-model="newTag"
                    placeholder="输入新标签，按Enter添加"
                    @keyup.enter="addTag"
                    clearable
                  >
                    <template #append>
                      <el-button @click="addTag" :disabled="!newTag.trim()">
                        <el-icon><CirclePlus /></el-icon>
                        添加
                      </el-button>
                    </template>
                  </el-input>
                </div>
              </div>

              <!-- 已选标签 -->
              <div class="tags-section">
                <h4>已选标签</h4>
                <div v-if="selectedTags.length > 0" class="selected-tags">
                  <el-tag
                    v-for="tag in selectedTags"
                    :key="tag"
                    closable
                    type="success"
                    @close="removeTag(tag)"
                    class="tag-item"
                    style="margin: 4px 6px;"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div v-else class="empty-tags">
                  <el-empty description="暂无标签" :image-size="80" />
                </div>
              </div>

              <!-- 历史标签 -->
              <div class="tags-section">
                <h4>历史标签</h4>
                <div class="history-tags" v-if="userTags.length > 0">
                  <el-tag
                    v-for="tag in userTags"
                    :key="tag.id"
                    type="info"
                    class="tag-item history-tag"
                    @click="selectHistoryTag(tag)"
                  >
                    {{ tag.tagName }}
                  </el-tag>
                </div>
                <div v-else class="empty-tags">
                  <el-empty description="暂无历史标签" :image-size="60" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 文件输入 -->
    <input 
      type="file" 
      ref="fileInput" 
      accept="image/*" 
      @change="handleFileSelect" 
      style="display: none;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Upload, Edit, UploadFilled, Delete, Crop, 
  Operation, PriceTag, CirclePlus, RefreshLeft, 
  RefreshRight
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ExifReader from 'exifreader'

const route = useRoute()
const router = useRouter()
const userId = computed(() => route.params.id)

// Canvas相关
const previewCanvas = ref(null)
const fileInput = ref(null)
let canvasContext = null
let originalImage = null
let canvasInitialized = false

// 状态
const activeTab = ref('upload')
const activeEditTab = ref('transform')
const isDragOver = ref(false)
const selectedFile = ref(null)
const imagePreviewUrl = ref('')
const showEditOverlay = ref(false)
const uploading = ref(false)
const extractingExif = ref(false)

// 编辑参数
const scale = ref(100)
const rotation = ref(0)
const imageName = ref('')
const imageDescription = ref('')

// 基础调整参数
const basicControls = ref([
  { name: 'brightness', label: '亮度', value: 0, min: -100, max: 100, step: 1 },
  { name: 'contrast', label: '对比度', value: 0, min: -100, max: 100, step: 1 },
  { name: 'saturation', label: '饱和度', value: 0, min: -100, max: 100, step: 1 }
])

// 颜色调整参数
const colorControls = ref([
  { name: 'exposure', label: '曝光', value: 0, min: -100, max: 100, step: 1 },
  { name: 'tint', label: '色调', value: 0, min: -100, max: 100, step: 1 }
])

// EXIF数据
const exifData = ref(null)

// 标签相关
const newTag = ref('')
const selectedTags = ref([])
const userTags = ref([])

// 初始化
onMounted(() => {
  loadUserTags()
})

// 组件卸载时清理
onUnmounted(() => {
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
})

// 初始化Canvas尺寸
const initCanvasSize = () => {
  if (!previewCanvas.value || canvasInitialized) return
  
  const canvas = previewCanvas.value
  const container = canvas.parentElement
  
  if (container) {
    // 获取容器尺寸并设置固定的Canvas尺寸
    const containerStyle = window.getComputedStyle(container)
    const paddingLeft = parseFloat(containerStyle.paddingLeft) || 0
    const paddingRight = parseFloat(containerStyle.paddingRight) || 0
    
    // 固定Canvas尺寸为800x400，或者根据容器宽度自适应
    const containerWidth = container.clientWidth - paddingLeft - paddingRight
    canvas.width = Math.min(800, containerWidth)
    canvas.height = 400
    
    canvasInitialized = true
    console.log('Canvas尺寸初始化:', canvas.width, 'x', canvas.height)
  }
}

// 获取Canvas上下文
const getCanvasContext = () => {
  if (!canvasContext && previewCanvas.value) {
    canvasContext = previewCanvas.value.getContext('2d')
  }
  return canvasContext
}

// 绘制占位符
const drawPlaceholder = () => {
  const ctx = getCanvasContext()
  if (!ctx || !previewCanvas.value) return
  
  const canvas = previewCanvas.value
  
  // 确保Canvas已初始化
  if (!canvasInitialized) {
    initCanvasSize()
  }
  
  // 清空Canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 绘制占位符背景
  ctx.fillStyle = '#2a2a2a'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  // 绘制占位符文字
  ctx.fillStyle = '#666'
  ctx.font = '16px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('图片预览区域', canvas.width / 2, canvas.height / 2)
}

// 处理编辑标签页点击
const handleEditTabClick = () => {
  if (selectedFile.value) {
    activeTab.value = 'edit'
  } else {
    ElMessage.warning('请先选择图片')
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持JPG、PNG、GIF、WEBP格式')
    return
  }
  
  selectedFile.value = file

  // 初始化文件名
  const fileName = file.name.substring(0, file.name.lastIndexOf('.')) || file.name
  imageName.value = fileName
  
  // 清空描述
  imageDescription.value = ''
  
  // 创建预览URL
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
  imagePreviewUrl.value = URL.createObjectURL(file)
  
  // 提取EXIF信息
  await extractExifData(file)
  
  // 加载图片到Canvas
  await loadImageToCanvas(file)
}

// 加载图片到Canvas
const loadImageToCanvas = (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      originalImage = new Image()
      originalImage.onload = () => {
        console.log('图片加载完成:', originalImage.width, 'x', originalImage.height)
        
        // 确保Canvas已初始化
        initCanvasSize()
        
        // 绘制图片
        drawImage()
        resolve()
      }
      originalImage.onerror = () => {
        console.error('图片加载失败')
        ElMessage.error('图片加载失败，请重试')
        resolve()
      }
      originalImage.src = e.target.result
    }
    reader.onerror = () => {
      console.error('文件读取失败')
      ElMessage.error('文件读取失败')
      resolve()
    }
    reader.readAsDataURL(file)
  })
}

// 绘制图片到Canvas
const drawImage = () => {
  const ctx = getCanvasContext()
  if (!ctx || !originalImage || !previewCanvas.value) {
    return
  }
  
  const canvas = previewCanvas.value
  
  // 确保Canvas尺寸是固定的
  if (!canvasInitialized) {
    initCanvasSize()
  }
  
  // 获取固定的Canvas尺寸
  const canvasWidth = canvas.width
  const canvasHeight = canvas.height
  
  // 清空Canvas（不改变尺寸）
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
  
  // 原始图片尺寸
  const imgWidth = originalImage.naturalWidth
  const imgHeight = originalImage.naturalHeight
  
  // 计算Canvas内可用的最大显示区域
  const maxDisplayWidth = canvasWidth * 0.9
  const maxDisplayHeight = canvasHeight * 0.9
  
  // 计算原始图片的宽高比
  const imgAspectRatio = imgWidth / imgHeight
  
  // 计算以原比例适配Canvas的初始尺寸
  let displayWidth = maxDisplayWidth
  let displayHeight = maxDisplayWidth / imgAspectRatio
  
  // 如果高度超出限制，重新计算
  if (displayHeight > maxDisplayHeight) {
    displayHeight = maxDisplayHeight
    displayWidth = maxDisplayHeight * imgAspectRatio
  }
  
  // 应用用户设置的缩放比例
  const userScale = scale.value / 100
  displayWidth *= userScale
  displayHeight *= userScale
  
  // 确保显示尺寸不超过Canvas边界
  const finalWidth = Math.min(displayWidth, canvasWidth)
  const finalHeight = Math.min(displayHeight, canvasHeight)
  
  // 计算居中位置
  const centerX = canvasWidth / 2
  const centerY = canvasHeight / 2
  
  // 保存Canvas状态
  ctx.save()
  
  // 移动到中心点
  ctx.translate(centerX, centerY)
  
  // 应用旋转
  const rad = rotation.value * Math.PI / 180
  ctx.rotate(rad)
  
  // 应用滤镜效果
  applyFiltersToContext()
  
  // 绘制图片
  ctx.drawImage(
    originalImage,
    -finalWidth / 2,
    -finalHeight / 2,
    finalWidth,
    finalHeight
  )
  
  // 恢复Canvas状态
  ctx.restore()
  
  console.log('图片绘制完成，尺寸:', finalWidth, 'x', finalHeight, 'Canvas尺寸:', canvasWidth, 'x', canvasHeight)
}

// 应用滤镜到Canvas上下文
const applyFiltersToContext = () => {
  const ctx = getCanvasContext()
  if (!ctx) return
  
  let filterString = ''
  
  // 亮度
  const brightness = basicControls.value.find(c => c.name === 'brightness')
  if (brightness && brightness.value !== 0) {
    filterString += `brightness(${100 + brightness.value}%) `
  }
  
  // 对比度
  const contrast = basicControls.value.find(c => c.name === 'contrast')
  if (contrast && contrast.value !== 0) {
    filterString += `contrast(${100 + contrast.value}%) `
  }
  
  // 饱和度
  const saturation = basicControls.value.find(c => c.name === 'saturation')
  if (saturation && saturation.value !== 0) {
    filterString += `saturate(${100 + saturation.value}%) `
  }
  
  // 曝光（简化处理）
  const exposure = colorControls.value.find(c => c.name === 'exposure')
  if (exposure && exposure.value !== 0) {
    filterString += `brightness(${100 + exposure.value * 0.5}%) `
  }

  // 色调
  const tint = colorControls.value.find(c => c.name === 'tint')
  if (tint && tint.value !== 0) {
    // 将-100到100的值映射到-180度到180度
    const hueRotate = (tint.value / 100) * 180
    filterString += `hue-rotate(${hueRotate}deg) `
  }
  
  if (filterString) {
    ctx.filter = filterString.trim()
  } else {
    ctx.filter = 'none'
  }
}

// 旋转图片
const rotate = (degrees) => {
  rotation.value = (rotation.value + degrees) % 360
  if (rotation.value < 0) rotation.value += 360
  drawImage()
}

// 提取EXIF数据
const extractExifData = async (file) => {
  extractingExif.value = true
  try {
    const tags = await ExifReader.load(file)
    
    const exif = {}
    
    // 拍摄时间
    if (tags.DateTimeOriginal) {
      const dateStr = tags.DateTimeOriginal.description
      exif.shootTime = dateStr
    } else if (tags.DateTime) {
      exif.shootTime = tags.DateTime.description
    }
    
    // GPS位置
    if (tags.GPSLatitude && tags.GPSLongitude) {
      const lat = tags.GPSLatitude.description
      const lon = tags.GPSLongitude.description
      exif.location = `${lat}, ${lon}`
    }
    
    // 分辨率
    if (tags.ImageWidth && tags.ImageHeight) {
      exif.resolution = `${tags.ImageWidth.description} × ${tags.ImageHeight.description}`
    }
    
    // 相机型号
    if (tags.Model) {
      exif.cameraModel = tags.Model.description
    }
    
    // 镜头信息
    if (tags.LensModel) {
      exif.lens = tags.LensModel.description
    } else if (tags.LensType) {
      exif.lens = tags.LensType.description
    }
    
    // 光圈
    if (tags.FNumber) {
      exif.aperture = parseFloat(tags.FNumber.description).toFixed(1)
    } else if (tags.ApertureValue) {
      exif.aperture = parseFloat(tags.ApertureValue.description).toFixed(1)
    }
    
    // 快门速度
    if (tags.ExposureTime) {
      const exposure = parseFloat(tags.ExposureTime.description)
      if (exposure < 1) {
        exif.shutter = `1/${Math.round(1/exposure)}s`
      } else {
        exif.shutter = `${exposure}s`
      }
    } else if (tags.ShutterSpeedValue) {
      exif.shutter = tags.ShutterSpeedValue.description
    }
    
    // ISO
    if (tags.ISOSpeedRatings) {
      exif.iso = `ISO ${tags.ISOSpeedRatings.description}`
    } else if (tags.ISO) {
      exif.iso = `ISO ${tags.ISO.description}`
    }
    
    // 焦距
    if (tags.FocalLength) {
      exif.focalLength = parseFloat(tags.FocalLength.description).toFixed(0)
    }
    
    // 其他信息
    if (tags.Make) {
      exif.make = tags.Make.description
    }
    
    if (tags.Orientation) {
      exif.orientation = tags.Orientation.description
    }
    
    exifData.value = exif
    
  } catch (error) {
    console.error('EXIF提取失败:', error)
    exifData.value = {
      shootTime: new Date(file.lastModified).toLocaleString(),
      resolution: '未知',
      cameraModel: '未知',
      lens: '未知',
      aperture: '未知',
      shutter: '未知',
      iso: '未知',
      focalLength: '未知'
    }
  } finally {
    extractingExif.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '未知'
  
  try {
    if (dateStr.includes(':')) {
      const [datePart, timePart] = dateStr.split(' ')
      const [year, month, day] = datePart.split(':')
      return `${year}-${month}-${day} ${timePart}`
    }
    return dateStr
  } catch (error) {
    return dateStr
  }
}

// 处理拖拽
const handleDragOver = (e) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDrop = (e) => {
  e.preventDefault()
  isDragOver.value = false
  const files = e.dataTransfer.files
  if (files.length > 0) {
    const file = files[0]
    const event = { target: { files: [file] } }
    handleFileSelect(event)
  }
}

// 加载用户标签
const loadUserTags = async () => {
  try {
    const response = await axios.get(`/tags/${userId.value}`)
    if (response.data && Array.isArray(response.data)) {
      userTags.value = response.data
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 添加标签
const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
    newTag.value = ''
    ElMessage.success('标签添加成功')
  }
}

// 移除标签
const removeTag = (tag) => {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
}

// 选择历史标签
const selectHistoryTag = (tag) => {
  if (!selectedTags.value.includes(tag.tagName)) {
    selectedTags.value.push(tag.tagName)
    ElMessage.success('标签添加成功')
  }
}

// 清除选择
const clearSelection = () => {
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
  
  selectedFile.value = null
  imagePreviewUrl.value = ''
  exifData.value = null
  activeTab.value = 'upload'
  originalImage = null
  canvasInitialized = false
  
  // 重置参数
  scale.value = 100
  rotation.value = 0
  basicControls.value.forEach(control => control.value = 0)
  colorControls.value.forEach(control => control.value = 0)
  selectedTags.value = []
  
  // 绘制占位符
  nextTick(() => {
    drawPlaceholder()
  })
}

// 获取编辑后的图片数据（Base64）
const getEditedImageData = () => {
  if (!previewCanvas.value || !originalImage) return null
  
  // 创建一个临时Canvas用于导出高质量图片
  const exportCanvas = document.createElement('canvas')
  exportCanvas.width = originalImage.naturalWidth
  exportCanvas.height = originalImage.naturalHeight
  const exportCtx = exportCanvas.getContext('2d')
  
  // 应用所有编辑效果
  const scaleFactor = scale.value / 100
  const rad = rotation.value * Math.PI / 180
  
  // 移动到中心
  exportCtx.translate(exportCanvas.width / 2, exportCanvas.height / 2)
  exportCtx.rotate(rad)
  
  // 应用滤镜
  let filterString = ''
  basicControls.value.forEach(control => {
    if (control.name === 'brightness' && control.value !== 0) {
      filterString += `brightness(${100 + control.value}%) `
    } else if (control.name === 'contrast' && control.value !== 0) {
      filterString += `contrast(${100 + control.value}%) `
    } else if (control.name === 'saturation' && control.value !== 0) {
      filterString += `saturate(${100 + control.value}%) `
    }
  })
  
  if (filterString) {
    exportCtx.filter = filterString.trim()
  }
  
  // 绘制图片
  exportCtx.drawImage(
    originalImage,
    -originalImage.naturalWidth * scaleFactor / 2,
    -originalImage.naturalHeight * scaleFactor / 2,
    originalImage.naturalWidth * scaleFactor,
    originalImage.naturalHeight * scaleFactor
  )
  
  // 返回Base64格式
  return exportCanvas.toDataURL('image/jpeg', 0.9)
}

// 上传图片
const uploadImage = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  uploading.value = true
  
  try {
    // 获取编辑后的图片数据
    const editedImageData = getEditedImageData()
    if (!editedImageData) {
      throw new Error('无法获取编辑后的图片数据')
    }
    
    // 准备上传数据
    const uploadData = {
      editedImage: editedImageData,
      tags: selectedTags.value,
      exifData: exifData.value,
      name: imageName.value.trim(),
      description: imageDescription.value.trim(),
      size: selectedFile.value.size
    }
    
    // 上传到后端
    const response = await axios.post(
      `/upload/${userId.value}`,
      uploadData,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )
    
    if (response.data && response.data.message === '上传成功') {
      ElMessage.success('图片上传成功！')
      clearSelection()
      router.push(`/gallery/${userId.value}`)
    } else {
      throw new Error(response.data?.message || '上传失败')
    }
    
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败: ' + error.message)
  } finally {
    uploading.value = false
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 监听tab切换
watch(activeTab, (newTab) => {
  if (newTab === 'edit' && originalImage) {
    // 确保Canvas已初始化并绘制图片
    nextTick(() => {
      initCanvasSize()
      setTimeout(() => {
        drawImage()
      }, 50)
    })
  } else if (newTab === 'upload' && !originalImage) {
    // 绘制占位符
    nextTick(() => {
      drawPlaceholder()
    })
  }
})

// 监听编辑参数变化
watch([scale, rotation], () => {
  if (originalImage) {
    drawImage()
  }
})

watch([basicControls, colorControls], () => {
  if (originalImage) {
    drawImage()
  }
}, { deep: true })

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  // 重置Canvas初始化状态，下次绘制时重新计算
  canvasInitialized = false
  if (originalImage) {
    drawImage()
  } else {
    drawPlaceholder()
  }
}
</script>

<style scoped>
.upload-page {
  max-width: 1400px;
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

/* 导航标签 */
.nav-tabs {
  display: flex;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid #333;
}

.nav-tab {
  flex: 1;
  padding: 1rem 1.5rem;
  text-align: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 500;
  color: #e0e0e0;
}

.nav-tab:hover:not(.active):not(.disabled) {
  background: #2a2a2a;
}

.nav-tab.active {
  background: #8a2be2;
  color: white;
  box-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.nav-tab.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 卡片样式 */
.upload-card, .preview-card, .edit-tool-card, .exif-card {
  background: #1a1a1a;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  margin-bottom: 1.5rem;
  overflow: hidden;
  border: 1px solid #333;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #333;
}

.card-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 1.25rem;
  font-weight: 600;
}

/* 按钮样式 */
.btn-primary, .btn-secondary, .btn-danger {
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

.btn-danger {
  background: #ff4757;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 71, 87, 0.3);
}

.btn-danger:hover {
  background: #ff3742;
  transform: translateY(-1px);
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #444;
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #2a2a2a;
  margin: 1.5rem;
}

.upload-area:hover, .upload-area.drag-over {
  border-color: #8a2be2;
  background: rgba(138, 43, 226, 0.1);
}

.upload-placeholder h3 {
  color: #e0e0e0;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
  margin-top: 1rem;
}

.upload-placeholder p {
  color: #b0b0b0;
  margin: 0;
}

/* 图片预览 */
.image-preview {
  margin: 1.5rem;
  border: 1px solid #444;
  border-radius: 12px;
  overflow: hidden;
  background: #2a2a2a;
}

.preview-header {
  background: #333;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #444;
  color: #e0e0e0;
}

.preview-actions {
  display: flex;
  gap: 0.5rem;
}

.preview-content {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
}

.preview-content img {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.image-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.5rem;
}

.image-info p {
  margin: 0;
  color: #b0b0b0;
}

/* 编辑区域 */
.edit-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid #333;
}

.edit-tab {
  flex: 1;
  padding: 1rem 1.5rem;
  text-align: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 500;
  color: #e0e0e0;
}

.edit-tab:hover:not(.active) {
  background: #2a2a2a;
}

.edit-tab.active {
  background: #8a2be2;
  color: white;
  box-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.edit-layout {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 1.5rem;
}

/* 预览区域 */
.preview-content-large {
  position: relative;
  text-align: center;
  padding: 2rem;
  background: #2a2a2a;
  border-radius: 8px;
  margin: 1.5rem;
  border: 1px solid #444;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.preview-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(138, 43, 226, 0.9);
  color: white;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(138, 43, 226, 0.3);
}

/* 编辑工具区域 */
.edit-tools-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 控制组样式 */
.control-group {
  margin-bottom: 1.5rem;
  margin-left: 1.5rem;
  margin-top: 1.5rem;
  margin-right: 1.5rem;
}

.control-group h4 {
  color: #e0e0e0;
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
}

.ratio-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.ratio-btn, .preset-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #444;
  border-radius: 6px;
  background: #2a2a2a;
  color: #e0e0e0;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.ratio-btn:hover, .preset-btn:hover {
  border-color: #8a2be2;
  color: #8a2be2;
}

.ratio-btn.active, .preset-btn.active {
  background: #8a2be2;
  color: white;
  border-color: #8a2be2;
  box-shadow: 0 0 8px rgba(138, 43, 226, 0.3);
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.slider {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: #444;
  outline: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #8a2be2;
  cursor: pointer;
  box-shadow: 0 0 5px rgba(138, 43, 226, 0.5);
}

.slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #8a2be2;
  cursor: pointer;
  border: none;
  box-shadow: 0 0 5px rgba(138, 43, 226, 0.5);
}

.slider-value {
  min-width: 50px;
  text-align: center;
  color: #8a2be2;
  font-weight: 600;
}

/* 预设区域 */
.presets-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #333;
  margin-left: 1.5rem;
  margin-right: 1.5rem;
  margin-bottom: 1.5rem;
}

.presets-section h4 {
  color: #ffffff;
  margin-bottom: 1rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.preset-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

/* 标签管理 */
.tags-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 1.5rem;
}

.tags-section h4 {
  color: #ffffff;
  margin-bottom: 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  border-left: 4px solid #8a2be2;
  padding-left: 0.5rem;
}

.ai-tags-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.tags-display {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tags-group {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.tag-category {
  font-weight: 600;
  color: #b0b0b0;
  min-width: 60px;
  margin-top: 0.25rem;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tag-ai {
  background: rgba(138, 43, 226, 0.2);
  color: #b57aff;
  border: 1px solid rgba(138, 43, 226, 0.4);
}

.tag-ai:hover {
  background: rgba(138, 43, 226, 0.3);
  box-shadow: 0 0 8px rgba(138, 43, 226, 0.3);
}

.tag-custom {
  background: rgba(0, 255, 255, 0.2);
  color: #00ffff;
  border: 1px solid rgba(0, 255, 255, 0.4);
}

.tag-custom:hover {
  background: rgba(0, 255, 255, 0.3);
  box-shadow: 0 0 8px rgba(0, 255, 255, 0.3);
}

.tag-history {
  background: rgba(255, 105, 180, 0.2);
  color: #ff69b4;
  border: 1px solid rgba(255, 105, 180, 0.4);
  cursor: pointer;
}

.tag-history:hover {
  background: rgba(255, 105, 180, 0.3);
  box-shadow: 0 0 8px rgba(255, 105, 180, 0.3);
}

.tag i {
  cursor: pointer;
  opacity: 0.7;
}

.tag i:hover {
  opacity: 1;
}

.empty-tags {
  text-align: center;
  padding: 2rem;
  background: #2a2a2a;
  border-radius: 8px;
  border: 1px solid #444;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.empty-state i {
  font-size: 3rem;
  color: #666;
}

.empty-state p {
  color: #b0b0b0;
  margin: 0;
}

.tag-input {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag-input input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid #444;
  border-radius: 8px;
  outline: none;
  background: #2a2a2a;
  color: #e0e0e0;
}

.tag-input input:focus {
  border-color: #8a2be2;
}

.tag-input input::placeholder {
  color: #666;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

/* EXIF信息面板 */
.exif-panel {
  margin: 1.5rem;
}

.exif-content {
  padding: 1.5rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.info-editor {
  grid-column: span 1;
  padding: 1rem;
  background: rgba(138, 43, 226, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(138, 43, 226, 0.2);
}

.form-hint {
  margin-top: 0.25rem;
  color: #888;
  font-size: 0.85rem;
}

.file-info-summary {
  background: #2a2a2a;
  border-radius: 6px;
  padding: 1rem;
  margin-top: 1rem;
  border: 1px solid #333;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #333;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: #b0b0b0;
  font-size: 0.9rem;
}

.info-row .value {
  color: #e0e0e0;
  font-weight: 500;
  font-size: 0.9rem;
}

.quick-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #333;
  background: #222;
  margin-top: 1rem;
}

.exif-group h4 {
  color: #ffffff;
  margin-bottom: 1rem;
  font-size: 1.1rem;
  font-weight: 600;
  border-left: 4px solid #8a2be2;
  padding-left: 0.5rem;
}

.exif-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #333;
}

.exif-item:last-child {
  border-bottom: none;
}

.exif-item .label {
  color: #b0b0b0;
  font-weight: 500;
}

.exif-item .value {
  color: #e0e0e0;
  font-weight: 600;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-dialog {
  background: #1a1a1a;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  border: 1px solid #333;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #333;
}

.modal-header h3 {
  margin: 0;
  color: #ffffff;
}

.modal-header i {
  cursor: pointer;
  color: #b0b0b0;
  font-size: 1.2rem;
}

.modal-header i:hover {
  color: #ffffff;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #e0e0e0;
  font-weight: 500;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #444;
  border-radius: 8px;
  outline: none;
  background: #2a2a2a;
  color: #e0e0e0;
}

.form-group input:focus, .form-group select:focus {
  border-color: #8a2be2;
  box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}

.form-group input::placeholder {
  color: #666;
}

/* 图标样式 */
[class^="icon-"] {
  font-style: normal;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .edit-layout {
    grid-template-columns: 1fr;
  }
  
  .exif-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .upload-page {
    padding: 1rem;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .preview-content {
    flex-direction: column;
    text-align: center;
  }
  
  .preview-content img {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
  
  .ai-tags-actions {
    flex-direction: column;
  }
  
  .preview-image {
    max-height: 300px;
  }
  
  .nav-tabs, .edit-tabs {
    flex-direction: column;
  }
  
  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>