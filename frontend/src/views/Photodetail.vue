<template>
  <div class="photo-detail-page">
    <!-- 头部导航 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回图库
        </el-button>
        <h1>图片详情</h1>
      </div>
      <div class="header-actions">
        <!-- 加入/退出轮播按钮 -->
        <el-button v-if="!isEditing"
          :type="isSelected ? 'warning' : 'primary'" 
          @click="toggleSelection"
          :loading="togglingSelection"
        >
          <el-icon><VideoPlay /></el-icon>
          {{ isSelected ? '退出轮播' : '加入轮播' }}
        </el-button>
        <!-- 编辑/保存切换按钮 -->
        <template v-if="!isEditing">
          <el-button type="primary" @click="startEdit">
            <el-icon><Edit /></el-icon>
            编辑图片
          </el-button>
          <el-button type="danger" @click="showDeleteConfirm">
            <el-icon><Delete /></el-icon>
            删除图片
          </el-button>
        </template>
        <template v-else>
          <el-button type="success" @click="saveAllChanges" :loading="savingEdit">
            <el-icon><Check /></el-icon>
            保存为新图片
          </el-button>
          <el-button @click="cancelEdit">
            <el-icon><Close /></el-icon>
            取消编辑
          </el-button>
        </template>
      </div>
    </div>

    <!-- 主要区域 -->
    <div class="main-content">
      <!-- 左侧图片预览 -->
      <div class="preview-section">
        <div class="preview-card">
          <div class="image-container" ref="imageContainer">
            <img 
              ref="imageElement"
              :src="currentImageData" 
              :alt="editBasicInfo.name" 
              class="detail-image"
              @load="onImageLoad"
            />
            
            <!-- 裁剪框 -->
            <div 
              v-if="isEditing && showCropBox" 
              class="crop-box"
              :style="{
                left: cropBox.x + 'px',
                top: cropBox.y + 'px',
                width: cropBox.width + 'px',
                height: cropBox.height + 'px'
              }"
              @mousedown="startDrag($event)"
            >
              <!-- 裁剪框四个角的控制点 -->
              <div class="crop-handle top-left" @mousedown="startResize($event, 'top-left')"></div>
              <div class="crop-handle top-right" @mousedown="startResize($event, 'top-right')"></div>
              <div class="crop-handle bottom-left" @mousedown="startResize($event, 'bottom-left')"></div>
              <div class="crop-handle bottom-right" @mousedown="startResize($event, 'bottom-right')"></div>
            </div>
          </div>
        </div>

        <!-- EXIF信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>拍摄信息</h3>
          </div>
          <div class="card-body">
            <div class="info-item">
              <span class="label">拍摄时间:</span>
              <span class="value">{{ formatDateTime(imageInfo.exifShootTime) || '未知'}}</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifLocation">
              <span class="label">拍摄地点:</span>
              <span class="value">{{ imageInfo.exifLocation }}</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifCameraModel">
              <span class="label">相机型号:</span>
              <span class="value">{{ imageInfo.exifCameraModel }}</span>
            </div>
            <div class="info-item">
              <span class="label">镜头信息:</span>
              <span class="value">{{ imageInfo.exifLens || '未知'}}</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifAperture">
              <span class="label">光圈:</span>
              <span class="value">f/{{ imageInfo.exifAperture }}</span>
            </div>
            <div class="info-item">
              <span class="label">快门速度:</span>
              <span class="value">{{ imageInfo.exifShutter || '未知'}}</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifISO">
              <span class="label">ISO:</span>
              <span class="value">{{ imageInfo.exifISO }}</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifFocalLength">
              <span class="label">焦距:</span>
              <span class="value">{{ imageInfo.exifFocalLength }}mm</span>
            </div>
            <div class="info-item" v-if="imageInfo.exifResolution">
              <span class="label">分辨率:</span>
              <span class="value">{{ imageInfo.exifResolution }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="info-section">
        
        <!-- AI标签管理卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>AI智能标签</h3>
            <div class="ai-tag-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="generateAITags"
                :loading="generatingTags"
                :disabled="isEditing"
              >
                <el-icon><MagicStick /></el-icon>
                生成AI标签
              </el-button>
            </div>
          </div>
          <div class="card-body">
            <div class="ai-tags-container">
              <el-tag
                v-for="tag in aiTags"
                :key="tag.tagName"
                type="info"
                class="ai-tag-item"
                closable
                @close="deleteAITag(tag)"
                :disabled="isEditing"
              >
                {{ tag.tagName }}
                <span v-if="tag.confidence" class="confidence">
                  ({{ (tag.confidence * 100).toFixed(0) }}%)
                </span>
              </el-tag>
              <div v-if="aiTags.length === 0" class="no-ai-tags">
                <span class="empty-text">暂无AI标签，点击上方按钮生成</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑控制面板 -->
        <div v-if="isEditing" class="edit-controls-panel">
          <div class="edit-tabs">
            <div 
              class="edit-tab"
              :class="{ active: activeEditTab === 'basic' }"
              @click="activeEditTab = 'basic'"
            >
              <el-icon><Document /></el-icon>
              基本信息
            </div>
            <div 
              class="edit-tab"
              :class="{ active: activeEditTab === 'crop' }"
              @click="activeEditTab = 'crop'"
            >
              <el-icon><Crop /></el-icon>
              自由裁剪
            </div>
            <div 
              class="edit-tab"
              :class="{ active: activeEditTab === 'transform' }"
              @click="activeEditTab = 'transform'"
            >
              <el-icon><Refresh /></el-icon>
              旋转调整
            </div>
            <div 
              class="edit-tab"
              :class="{ active: activeEditTab === 'adjust' }"
              @click="activeEditTab = 'adjust'"
            >
              <el-icon><Operation /></el-icon>
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

          <!-- 基本信息编辑 -->
          <div v-if="activeEditTab === 'basic'" class="edit-tool-card">
            <div class="form-group">
              <label>图片名称</label>
              <el-input 
                v-model="editBasicInfo.name" 
                placeholder="请输入图片名称"
                maxlength="100"
                show-word-limit
              />
            </div>
            
            <div class="form-group">
              <label>图片描述</label>
              <el-input 
                v-model="editBasicInfo.description" 
                type="textarea"
                :rows="4"
                placeholder="请输入图片描述"
                maxlength="500"
                show-word-limit
              />
            </div>
          </div>

          <!-- 自由裁剪 -->
          <div v-if="activeEditTab === 'crop'" class="edit-tool-card">
            <div class="control-group">
              <h4>自由裁剪</h4>
              <p class="crop-instructions">
                拖动裁剪框的四个角可以调整大小，拖动框内可以移动位置
              </p>
              
              <div class="crop-controls">
                <el-button 
                  type="primary" 
                  @click="initCropBox" 
                  :disabled="showCropBox"
                  size="small"
                >
                  <el-icon><Crop /></el-icon>
                  开始裁剪
                </el-button>
                
                <el-button 
                  type="success" 
                  @click="applyCrop" 
                  :disabled="!showCropBox"
                  size="small"
                >
                  <el-icon><Check /></el-icon>
                  应用裁剪
                </el-button>
                
                <el-button 
                  type="default" 
                  @click="resetCrop" 
                  :disabled="!showCropBox"
                  size="small"
                >
                  <el-icon><Refresh /></el-icon>
                  重置裁剪
                </el-button>
              </div>
            </div>
          </div>

          <!-- 旋转调整 -->
          <div v-if="activeEditTab === 'transform'" class="edit-tool-card">
            <div class="control-group">
              <h4>旋转角度</h4>
              <div class="slider-container">
                <el-slider 
                  v-model="editParams.rotation" 
                  :min="0" 
                  :max="360"
                  :step="1"
                  show-input
                />
              </div>
              <div class="rotation-buttons">
                <el-button type="default" @click="rotate(-90)">
                  <el-icon><RefreshLeft /></el-icon>
                  左旋转90°
                </el-button>
                <el-button type="default" @click="rotate(90)">
                  <el-icon><RefreshRight /></el-icon>
                  右旋转90°
                </el-button>
              </div>
            </div>
          </div>

          <!-- 基础调整 -->
          <div v-if="activeEditTab === 'adjust'" class="edit-tool-card">
            <div class="control-group" v-for="control in editParams.adjustments" :key="control.name">
              <h4>{{ control.label }}</h4>
              <div class="slider-container">
                <el-slider 
                  v-model="control.value" 
                  :min="control.min" 
                  :max="control.max"
                  :step="control.step"
                  show-input
                />
              </div>
            </div>
          </div>

          <!-- 标签管理 -->
          <div v-if="activeEditTab === 'tags'" class="edit-tool-card">
            <div class="tag-input-group">
              <el-input
                v-model="newTag"
                placeholder="输入新标签，按Enter添加"
                @keyup.enter="addNewTag"
                clearable
              >
                <template #append>
                  <el-button @click="addNewTag" :disabled="!newTag.trim()">
                    <el-icon><CirclePlus /></el-icon>
                    添加
                  </el-button>
                </template>
              </el-input>
            </div>

            <div class="tag-list">
              <div class="list-header">
                <span>已选标签 ({{ editTags.length }})</span>
              </div>
              <div class="selected-tags">
                <el-tag
                  v-for="tag in editTags"
                  :key="tag"
                  closable
                  type="success"
                  @close="removeTag(tag)"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
                <div v-if="editTags.length === 0" class="no-tags">
                  <el-empty description="暂无标签" :image-size="60" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑状态卡片 -->
        <div class="info-card" v-if="isEditing">
          <div class="card-header">
            <h3>编辑状态</h3>
          </div>
          <div class="card-body">
            <div class="info-item">
              <span class="label">旋转角度:</span>
              <span class="value">{{ editParams.rotation }}°</span>
            </div>
            <div class="info-item" v-for="adjust in editParams.adjustments" :key="adjust.name">
              <span class="label">{{ adjust.label }}:</span>
              <span class="value">{{ adjust.value }}</span>
            </div>
            <div class="info-item" v-if="showCropBox">
              <span class="label">裁剪区域:</span>
              <span class="value">{{ Math.round(cropBox.width) }} × {{ Math.round(cropBox.height) }}</span>
            </div>
            <div class="info-item">
              <span class="label">是否有修改:</span>
              <span class="value" :class="{ 'text-green': hasChanges, 'text-gray': !hasChanges }">
                {{ hasChanges ? '是' : '否' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 基本信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>基本信息</h3>
            <el-tag v-if="isEditing" type="warning">编辑中</el-tag>
          </div>
          <div class="card-body">
            <div class="info-item">
              <span class="label">图片名称:</span>
              <span class="value">{{ editBasicInfo.name || '未命名' }}</span>
            </div>
            <div class="info-item">
              <span class="label">图片描述:</span>
              <span class="value description">{{ editBasicInfo.description || '暂无描述' }}</span>
            </div>
          </div>
        </div>

        <!-- 标签信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h3>标签</h3>
            <el-tag v-if="isEditing" type="warning">编辑中</el-tag>
          </div>
          <div class="card-body">
            <div class="tags-container">
              <el-tag
                v-for="tag in editTags"
                :key="tag"
                type="success"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <div v-if="editTags.length === 0" class="no-tags">
                <span class="empty-text">暂无标签</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="确认删除"
      width="400px"
      center
    >
      <div class="delete-confirm">
        <el-icon color="#ff4757" size="60"><Warning /></el-icon>
        <h3>确定要删除这张图片吗？</h3>
        <p>此操作不可撤销，图片及相关标签将被永久删除。</p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteDialog = false">取消</el-button>
          <el-button type="danger" @click="deleteImage" :loading="deleting">
            确定删除
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 保存确认对话框 -->
    <el-dialog
      v-model="showSaveConfirm"
      title="保存为新图片"
      width="500px"
    >
      <div class="save-confirm">
        <el-icon color="#52c41a" size="60"><Check /></el-icon>
        <h3>确认保存编辑结果</h3>
        <div class="save-info">
          <div class="info-item">
            <span class="label">新图片名称:</span>
            <span class="value">{{ editBasicInfo.name}}</span>
          </div>
          <div class="info-item">
            <span class="label">标签数量:</span>
            <span class="value">{{ editTags.length }}个</span>
          </div>
          <div class="info-item">
            <span class="label">编辑项目:</span>
            <span class="value">{{ getEditSummary() }}</span>
          </div>
        </div>
        <p class="tip">这将会创建一张新的图片，原始图片不会被修改。</p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSaveConfirm = false">取消</el-button>
          <el-button type="primary" @click="confirmSaveAllChanges" :loading="savingEdit">
            确认保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 数据状态
const imageInfo = ref({
  id: 0,
  name: '',
  description: '',
  tags: [],
  imageData: '',
  exifShootTime: null,
  exifLocation: '',
  exifCameraModel: '',
  exifLens: '',
  exifAperture: '',
  exifShutter: '',
  exifISO: '',
  exifFocalLength: '',
  exifResolution: ''
})

// AI标签相关
const aiTags = ref([])
const generatingTags = ref(false)

const isSelected = ref(false)
const togglingSelection = ref(false)

const currentImageData = ref('')
const originalImageData = ref('')
const imageElement = ref(null)
const imageContainer = ref(null)
const updatePreviewTimer = ref(null)

// 编辑状态
const isEditing = ref(false)
const savingEdit = ref(false)
const showDeleteDialog = ref(false)
const showSaveConfirm = ref(false)
const deleting = ref(false)
const activeEditTab = ref('basic')

// 编辑数据
const editBasicInfo = ref({
  name: '',
  description: ''
})

const editTags = ref([])
const newTag = ref('')

// 编辑参数
const editParams = ref({
  rotation: 0,
  adjustments: [
    { name: 'brightness', label: '亮度', value: 0, min: -100, max: 100, step: 1 },
    { name: 'contrast', label: '对比度', value: 0, min: -100, max: 100, step: 1 },
    { name: 'saturation', label: '饱和度', value: 0, min: -100, max: 100, step: 1 },
    { name: 'exposure', label: '曝光', value: 0, min: -100, max: 100, step: 1 },
    { name: 'hue', label: '色调', value: 0, min: -180, max: 180, step: 1 }
  ]
})

// 裁剪相关
const showCropBox = ref(false)
const cropBox = ref({
  x: 100,
  y: 100,
  width: 200,
  height: 200
})

// 拖拽状态
const isDragging = ref(false)
const isResizing = ref(false)
const dragType = ref('')
const dragStart = ref({ x: 0, y: 0 })
const cropBoxStart = ref({ x: 0, y: 0, width: 0, height: 0 })

// 计算属性
const hasChanges = computed(() => {
  return editParams.value.rotation !== 0 ||
         editParams.value.adjustments.some(adj => adj.value !== 0) ||
         editBasicInfo.value.name !== imageInfo.value.name ||
         editBasicInfo.value.description !== imageInfo.value.description ||
         JSON.stringify(editTags.value) !== JSON.stringify(
           (imageInfo.value.tags || []).map(tag => typeof tag === 'object' && tag.tagName ? tag.tagName : tag)
         ) ||
         showCropBox.value
})

// 监听编辑参数变化，实时更新预览
watch(
  [
    () => editParams.value.rotation,
    () => editParams.value.adjustments.map(a => a.value)
  ],
  async () => {
    if (isEditing.value) {
      if (updatePreviewTimer.value) {
        clearTimeout(updatePreviewTimer.value)
      }
      updatePreviewTimer.value = setTimeout(updateImagePreview, 200)
    }
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  loadImageDetail()
  loadAITags()
  setupEventListeners()
})

onUnmounted(() => {
  removeEventListeners()
  if (updatePreviewTimer.value) {
    clearTimeout(updatePreviewTimer.value)
  }
})

// 设置事件监听
const setupEventListeners = () => {
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const removeEventListeners = () => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

// 加载图片详情
const loadImageDetail = async () => {
  try {
    const imageId = route.params.photoId
    const userId = route.params.id
    const response = await axios.get(`/detail/${imageId}`)
    
    if (response.data) {
      imageInfo.value = response.data
      currentImageData.value = response.data.imageData
      originalImageData.value = response.data.imageData
      
      editBasicInfo.value = {
        name: response.data.name || '',
        description: response.data.description || ''
      }
      
      if (response.data.tags && Array.isArray(response.data.tags)) {
        editTags.value = response.data.tags.map(tag => {
          if (typeof tag === 'object' && tag.tagName) {
            return tag.tagName
          }
          return tag
        })
      }
      await checkImageSelection(userId, imageId)
    }
  } catch (error) {
    console.error('加载图片详情失败:', error)
    ElMessage.error('加载图片详情失败')
  }
}

// 加载AI标签
const loadAITags = async () => {
  try {
    const imageId = route.params.photoId
    const response = await axios.get(`/aitags/${imageId}`)
    
    if (response.data && Array.isArray(response.data)) {
      aiTags.value = response.data
    }
  } catch (error) {
    console.error('加载AI标签失败:', error)
    // 不显示错误消息，因为可能是第一次使用，还没有AI标签
  }
}

// 生成AI标签
const generateAITags = async () => {
  if (isEditing.value) {
    ElMessage.warning('编辑状态下无法生成AI标签，请先完成编辑')
    return
  }
  
  const imageId = route.params.photoId
  
  generatingTags.value = true
  try {
    const response = await axios.post(`/aitags/generate/${imageId}`)
    
    if (response.data && response.data.success) {
      ElMessage.success('AI标签生成成功')
      // 重新加载AI标签
      await loadAITags()
    } else {
      ElMessage.error(response.data?.message || '生成AI标签失败')
    }
  } catch (error) {
    console.error('生成AI标签失败:', error)
    ElMessage.error('生成AI标签失败: ' + (error.response?.data?.message || error.message))
  } finally {
    generatingTags.value = false
  }
}

// 删除AI标签
const deleteAITag = async (tag) => {
  if (isEditing.value) {
    ElMessage.warning('编辑状态下无法删除AI标签')
    return
  }
  
  try {
    const imageId = route.params.photoId
    const response = await axios.delete(`/aitags/${imageId}`, {
      data: { tagName: tag.tagName }
    })
    
    if (response.data && response.data.success) {
      // 从本地列表中移除
      const index = aiTags.value.findIndex(t => t.tagName === tag.tagName)
      if (index > -1) {
        aiTags.value.splice(index, 1)
      }
      ElMessage.success('AI标签删除成功')
    } else {
      ElMessage.error(response.data?.message || '删除AI标签失败')
    }
  } catch (error) {
    console.error('删除AI标签失败:', error)
    ElMessage.error('删除AI标签失败: ' + (error.response?.data?.message || error.message))
  }
}

// 检查图片是否已被用户选中
const checkImageSelection = async (userId, imageId) => {
  try {
    const response = await axios.get(`/setting/check/${userId}/${imageId}`)
    
    if (response.data && response.data.success) {
      isSelected.value = response.data.exists
    }
  } catch (error) {
    console.error('检查图片选中状态失败:', error)
  }
}

// 切换选中状态
const toggleSelection = async () => {
  const userId = route.params.id
  const imageId = route.params.photoId
  
  if (!userId || !imageId) {
    ElMessage.error('用户ID或图片ID缺失')
    return
  }
  
  togglingSelection.value = true
  
  try {
    if (isSelected.value) {
      // 如果已被选中，则移除关联
      const response = await axios.delete(`/setting/remove/${userId}/${imageId}`)
      
      if (response.data && response.data.success) {
        isSelected.value = false
        ElMessage.success('已从轮播中移除')
      } else {
        ElMessage.error(response.data?.message || '移除失败')
      }
    } else {
      // 如果未被选中，则添加关联
      const response = await axios.post(`/setting/selected/${userId}/${imageId}`)
      
      if (response.data && response.data.success) {
        isSelected.value = true
        ElMessage.success('已加入轮播')
      } else {
        ElMessage.error(response.data?.message || '加入失败')
      }
    }
  } catch (error) {
    console.error('切换选中状态失败:', error)
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
  } finally {
    togglingSelection.value = false
  }
}

// 实时更新图片预览（不旋转画布，只旋转图片）
const updateImagePreview = () => {
  if (!isEditing.value || !originalImageData.value) return
  
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const img = new Image()
  
  img.onload = () => {
    const width = img.width
    const height = img.height
    const rotation = editParams.value.rotation
    
    // 保持画布尺寸与原始图片相同
    canvas.width = width
    canvas.height = height
    
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    
    // 保存当前状态
    ctx.save()
    
    // 移动到图片中心
    ctx.translate(width / 2, height / 2)
    
    // 旋转图片
    ctx.rotate(rotation * Math.PI / 180)
    
    // 绘制图片（绘制后恢复到中心位置）
    ctx.drawImage(img, -width/2, -height/2, width, height)
    
    // 恢复状态
    ctx.restore()
    
    // 应用滤镜
    const adjustments = editParams.value.adjustments
    const filters = {
      brightness: adjustments.find(a => a.name === 'brightness')?.value || 0,
      contrast: adjustments.find(a => a.name === 'contrast')?.value || 0,
      saturation: adjustments.find(a => a.name === 'saturation')?.value || 0,
      exposure: adjustments.find(a => a.name === 'exposure')?.value || 0,
      hue: adjustments.find(a => a.name === 'hue')?.value || 0
    }
    
    if (Object.values(filters).some(v => v !== 0)) {
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      applyFilters(imageData.data, filters)
      ctx.putImageData(imageData, 0, 0)
    }
    
    // 更新预览图片
    currentImageData.value = canvas.toDataURL('image/jpeg', 0.8)
  }
  
  img.src = originalImageData.value
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  activeEditTab.value = 'basic'
  // 初始化预览
  setTimeout(updateImagePreview, 100)
}

// 取消编辑
const cancelEdit = () => {
  ElMessageBox.confirm(
    '确定要取消编辑吗？所有未保存的修改将会丢失。',
    '警告',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }
  ).then(() => {
    // 恢复原始图片
    currentImageData.value = originalImageData.value
    // 重置编辑参数
    editParams.value = {
      rotation: 0,
      adjustments: [
        { name: 'brightness', label: '亮度', value: 0, min: -100, max: 100, step: 1 },
        { name: 'contrast', label: '对比度', value: 0, min: -100, max: 100, step: 1 },
        { name: 'saturation', label: '饱和度', value: 0, min: -100, max: 100, step: 1 },
        { name: 'exposure', label: '曝光', value: 0, min: -100, max: 100, step: 1 },
        { name: 'hue', label: '色调', value: 0, min: -180, max: 180, step: 1 }
      ]
    }
    
    const userId = route.params.id
    router.push(`/gallery/${userId}`)
  })
}

// 裁剪相关方法
const initCropBox = () => {
  if (!imageElement.value) return
  
  const containerRect = imageContainer.value.getBoundingClientRect()
  const imgRect = imageElement.value.getBoundingClientRect()
  
  const imgOffsetX = imgRect.left - containerRect.left
  const imgOffsetY = imgRect.top - containerRect.top
  const cropWidth = imgRect.width * 0.8
  const cropHeight = imgRect.height * 0.8
  
  cropBox.value = {
    x: imgOffsetX + (imgRect.width - cropWidth) / 2,
    y: imgOffsetY + (imgRect.height - cropHeight) / 2,
    width: cropWidth,
    height: cropHeight
  }
  
  showCropBox.value = true
}

const applyCrop = () => {
  if (!showCropBox.value || !imageElement.value) return
  
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const img = new Image()
  
  img.onload = () => {
    const containerRect = imageContainer.value.getBoundingClientRect()
    const imgRect = imageElement.value.getBoundingClientRect()
    
    const imgOffsetX = imgRect.left - containerRect.left
    const imgOffsetY = imgRect.top - containerRect.top
    
    const relativeCropX = cropBox.value.x - imgOffsetX
    const relativeCropY = cropBox.value.y - imgOffsetY
    
    const naturalWidth = img.naturalWidth
    const naturalHeight = img.naturalHeight
    const displayWidth = imgRect.width
    const displayHeight = imgRect.height
    
    const scaleX = naturalWidth / displayWidth
    const scaleY = naturalHeight / displayHeight
    
    const cropX = relativeCropX * scaleX
    const cropY = relativeCropY * scaleY
    const cropWidth = cropBox.value.width * scaleX
    const cropHeight = cropBox.value.height * scaleY
    
    const adjustedCropX = Math.max(0, Math.min(cropX, naturalWidth - 1))
    const adjustedCropY = Math.max(0, Math.min(cropY, naturalHeight - 1))
    const adjustedCropWidth = Math.max(1, Math.min(cropWidth, naturalWidth - adjustedCropX))
    const adjustedCropHeight = Math.max(1, Math.min(cropHeight, naturalHeight - adjustedCropY))
    
    canvas.width = Math.round(adjustedCropWidth)
    canvas.height = Math.round(adjustedCropHeight)
    
    ctx.drawImage(
      img, 
      adjustedCropX, adjustedCropY, adjustedCropWidth, adjustedCropHeight,
      0, 0, canvas.width, canvas.height
    )
    
    currentImageData.value = canvas.toDataURL('image/jpeg', 0.9)
    originalImageData.value = currentImageData.value
    
    resetCrop()
    ElMessage.success('裁剪已应用')
    
    // 更新预览
    updateImagePreview()
  }
  
  img.src = originalImageData.value
}

const resetCrop = () => {
  showCropBox.value = false
  cropBox.value = { x: 0, y: 0, width: 200, height: 200 }
}

// 鼠标事件处理
const startDrag = (event) => {
  if (!showCropBox.value || activeEditTab.value !== 'crop') return
  
  event.preventDefault()
  event.stopPropagation()
  
  const imgRect = imageElement.value.getBoundingClientRect()
  const containerRect = imageContainer.value.getBoundingClientRect()
  
  dragStart.value = {
    x: event.clientX - containerRect.left,
    y: event.clientY - containerRect.top
  }
  
  cropBoxStart.value = { ...cropBox.value }
  isDragging.value = true
  dragType.value = 'move'
}

const startResize = (event, type) => {
  event.preventDefault()
  event.stopPropagation()
  
  const containerRect = imageContainer.value.getBoundingClientRect()
  
  dragStart.value = {
    x: event.clientX - containerRect.left,
    y: event.clientY - containerRect.top
  }
  
  cropBoxStart.value = { ...cropBox.value }
  isResizing.value = true
  dragType.value = type
}

const handleMouseMove = (event) => {
  if (!isDragging.value && !isResizing.value) return
  if (!imageContainer.value || !imageElement.value) return
  
  const containerRect = imageContainer.value.getBoundingClientRect()
  const imgRect = imageElement.value.getBoundingClientRect()
  
  const imgOffsetX = imgRect.left - containerRect.left
  const imgOffsetY = imgRect.top - containerRect.top
  const imgWidth = imgRect.width
  const imgHeight = imgRect.height
  
  const currentX = event.clientX - containerRect.left
  const currentY = event.clientY - containerRect.top
  const deltaX = currentX - dragStart.value.x
  const deltaY = currentY - dragStart.value.y
  
  if (isDragging.value && dragType.value === 'move') {
    let newX = cropBoxStart.value.x + deltaX
    let newY = cropBoxStart.value.y + deltaY
    
    newX = Math.max(imgOffsetX, Math.min(newX, imgOffsetX + imgWidth - cropBox.value.width))
    newY = Math.max(imgOffsetY, Math.min(newY, imgOffsetY + imgHeight - cropBox.value.height))
    
    cropBox.value.x = newX
    cropBox.value.y = newY
  } else if (isResizing.value) {
    let newX = cropBoxStart.value.x
    let newY = cropBoxStart.value.y
    let newWidth = cropBoxStart.value.width
    let newHeight = cropBoxStart.value.height
    
    switch (dragType.value) {
      case 'top-left':
        newX += deltaX
        newY += deltaY
        newWidth -= deltaX
        newHeight -= deltaY
        break
      case 'top-right':
        newY += deltaY
        newWidth += deltaX
        newHeight -= deltaY
        break
      case 'bottom-left':
        newX += deltaX
        newHeight += deltaY
        newWidth -= deltaX
        break
      case 'bottom-right':
        newWidth += deltaX
        newHeight += deltaY
        break
    }
    
    newWidth = Math.max(50, newWidth)
    newHeight = Math.max(50, newHeight)
    newX = Math.max(imgOffsetX, newX)
    newY = Math.max(imgOffsetY, newY)
    
    newWidth = Math.min(newWidth, (imgOffsetX + imgWidth) - newX)
    newHeight = Math.min(newHeight, (imgOffsetY + imgHeight) - newY)
    
    cropBox.value = { 
      x: newX, 
      y: newY, 
      width: newWidth, 
      height: newHeight 
    }
  }
}

const handleMouseUp = () => {
  isDragging.value = false
  isResizing.value = false
  dragType.value = ''
}

// 旋转图片
const rotate = (degrees) => {
  editParams.value.rotation += degrees
  
  // 保持角度在 0-360 度范围内
  if (editParams.value.rotation >= 360) {
    editParams.value.rotation -= 360
  } else if (editParams.value.rotation < 0) {
    editParams.value.rotation += 360
  }
}

// 应用滤镜到像素数据
const applyFilters = (data, filters) => {
  const length = data.length
  
  for (let i = 0; i < length; i += 4) {
    let r = data[i]
    let g = data[i + 1]
    let b = data[i + 2]
    
    if (filters.brightness !== 0) {
      const factor = (filters.brightness + 100) / 100
      r = Math.min(255, Math.max(0, r * factor))
      g = Math.min(255, Math.max(0, g * factor))
      b = Math.min(255, Math.max(0, b * factor))
    }
    
    if (filters.contrast !== 0) {
      const factor = (filters.contrast + 100) / 100
      r = ((r - 128) * factor) + 128
      g = ((g - 128) * factor) + 128
      b = ((b - 128) * factor) + 128
    }
    
    if (filters.saturation !== 0) {
      const gray = 0.2989 * r + 0.5870 * g + 0.1140 * b
      const factor = (filters.saturation + 100) / 100
      r = Math.min(255, Math.max(0, gray + (r - gray) * factor))
      g = Math.min(255, Math.max(0, gray + (g - gray) * factor))
      b = Math.min(255, Math.max(0, gray + (b - gray) * factor))
    }
    
    if (filters.exposure !== 0) {
      const factor = Math.pow(2, filters.exposure / 100)
      r = Math.min(255, r * factor)
      g = Math.min(255, g * factor)
      b = Math.min(255, b * factor)
    }
    
    if (filters.hue !== 0) {
      const rgb = [r / 255, g / 255, b / 255]
      const max = Math.max(...rgb)
      const min = Math.min(...rgb)
      const delta = max - min
      
      let h = 0
      if (delta !== 0) {
        if (max === rgb[0]) {
          h = ((rgb[1] - rgb[2]) / delta) % 6
        } else if (max === rgb[1]) {
          h = ((rgb[2] - rgb[0]) / delta) + 2
        } else {
          h = ((rgb[0] - rgb[1]) / delta) + 4
        }
        h = (h * 60 + 360) % 360
      }
      
      h = (h + filters.hue) % 360
      
      const c = delta
      const x = c * (1 - Math.abs((h / 60) % 2 - 1))
      const m = min
      
      let r1 = 0, g1 = 0, b1 = 0
      if (h < 60) {
        r1 = c; g1 = x; b1 = 0
      } else if (h < 120) {
        r1 = x; g1 = c; b1 = 0
      } else if (h < 180) {
        r1 = 0; g1 = c; b1 = x
      } else if (h < 240) {
        r1 = 0; g1 = x; b1 = c
      } else if (h < 300) {
        r1 = x; g1 = 0; b1 = c
      } else {
        r1 = c; g1 = 0; b1 = x
      }
      
      r = (r1 + m) * 255
      g = (g1 + m) * 255
      b = (b1 + m) * 255
    }
    
    data[i] = Math.min(255, Math.max(0, r))
    data[i + 1] = Math.min(255, Math.max(0, g))
    data[i + 2] = Math.min(255, Math.max(0, b))
  }
}

// 标签管理
const addNewTag = () => {
  const tag = newTag.value.trim()
  if (tag && !editTags.value.includes(tag)) {
    editTags.value.push(tag)
    newTag.value = ''
    ElMessage.success(`已添加标签: ${tag}`)
  } else if (editTags.value.includes(tag)) {
    ElMessage.warning('该标签已存在')
  }
}

const removeTag = (tag) => {
  const index = editTags.value.indexOf(tag)
  if (index > -1) {
    editTags.value.splice(index, 1)
    ElMessage.success(`已移除标签: ${tag}`)
  }
}

// 保存所有更改
const saveAllChanges = () => {
  if (!hasChanges.value) {
    ElMessage.warning('没有需要保存的修改')
    return
  }
  showSaveConfirm.value = true
}

// 确认保存所有更改
const confirmSaveAllChanges = async () => {
  savingEdit.value = true
  try {
    // 使用相同的逻辑保存图片
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    
    await new Promise((resolve) => {
      img.onload = () => {
        const width = img.width
        const height = img.height
        const rotation = editParams.value.rotation
        
        // 保持画布尺寸与原始图片相同
        canvas.width = width
        canvas.height = height
        
        ctx.save()
        ctx.translate(width / 2, height / 2)
        ctx.rotate(rotation * Math.PI / 180)
        ctx.drawImage(img, -width/2, -height/2, width, height)
        ctx.restore()
        
        // 应用滤镜
        const adjustments = editParams.value.adjustments
        const filters = {
          brightness: adjustments.find(a => a.name === 'brightness')?.value || 0,
          contrast: adjustments.find(a => a.name === 'contrast')?.value || 0,
          saturation: adjustments.find(a => a.name === 'saturation')?.value || 0,
          exposure: adjustments.find(a => a.name === 'exposure')?.value || 0,
          hue: adjustments.find(a => a.name === 'hue')?.value || 0
        }
        
        if (Object.values(filters).some(v => v !== 0)) {
          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
          applyFilters(imageData.data, filters)
          ctx.putImageData(imageData, 0, 0)
        }
        
        currentImageData.value = canvas.toDataURL('image/jpeg', 0.9)
        resolve()
      }
      img.src = originalImageData.value
    })
    
    // 构建上传数据
    const uploadData = {
      editedImage: currentImageData.value,
      tags: editTags.value,
      exifData: {
        shootTime: imageInfo.value.exifShootTime,
        location: imageInfo.value.exifLocation,
        resolution: imageInfo.value.exifResolution,
        cameraModel: imageInfo.value.exifCameraModel,
        lens: imageInfo.value.exifLens,
        aperture: imageInfo.value.exifAperture,
        shutter: imageInfo.value.exifShutter,
        iso: imageInfo.value.exifISO,
        focalLength: imageInfo.value.exifFocalLength
      },
      name: editBasicInfo.value.name,
      description: editBasicInfo.value.description,
      size: Math.round(currentImageData.value.length * 0.75)
    }
    
    const userId = route.params.id
    const response = await axios.post(`/upload/${userId}`, uploadData)
    
    
    if (response.data && response.data.imageId) {
      ElMessage.success('图片保存成功')
      showSaveConfirm.value = false
      router.push(`/gallery/${userId}`)
    }
  } catch (error) {
    console.error('保存图片失败:', error)
    ElMessage.error('保存图片失败: ' + (error.response?.data || error.message))
  } finally {
    savingEdit.value = false
  }
}

// 获取编辑摘要
const getEditSummary = () => {
  const summaries = []
  
  if (editParams.value.rotation !== 0) {
    summaries.push(`旋转 ${editParams.value.rotation}°`)
  }
  
  const adjusted = editParams.value.adjustments.filter(a => a.value !== 0)
  if (adjusted.length > 0) {
    summaries.push(`${adjusted.length}项调整`)
  }
  
  if (showCropBox.value) {
    summaries.push('裁剪')
  }
  
  return summaries.join('，') || '无'
}

// 删除图片
const showDeleteConfirm = () => {
  ElMessageBox.confirm(
    '此操作将永久删除该图片，是否继续？',
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    }
  ).then(() => {
    deleteImage()
  })
}

const deleteImage = async () => {
  deleting.value = true
  try {
    const response = await axios.delete(`/detail/${imageInfo.value.id}`)
    
    if (response.data && response.data.success) {
      ElMessage.success('图片删除成功')
      const userId = route.params.id
      router.push(`/gallery/${userId}`)
    } else {
      ElMessage.error(response.data.error || '删除失败')
    }
  } catch (error) {
    console.error('删除图片失败:', error)
    ElMessage.error('删除图片失败: ' + error.message)
  } finally {
    deleting.value = false
  }
}

// 工具函数
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  return dateTimeString.replace('T', ' ')
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.photo-detail-page {
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #0a0a0a;
  color: #e0e0e0;
  min-height: 100vh;
}

/* 头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #333;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-header h1 {
  color: #ffffff;
  font-size: 2rem;
  margin: 0;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.back-btn {
  background: #2a2a2a;
  color: #e0e0e0;
  border: 1px solid #444;
}

.back-btn:hover {
  background: #333;
  border-color: #666;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.main-content {
  display: flex;
  gap: 2rem;
}

/* 桌面端左右布局 */
.preview-section {
  flex: 1;
  position: sticky;
  top: 2rem;
  align-self: start;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-section {
  width: 700px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 移动端垂直布局 */
@media (max-width: 1200px) {
  .main-content {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .preview-section {
    position: static;
    width: 100%;
    order: 1; 
  }
  
  .info-section {
    width: 100%;
    order: 2; 
  }
}

/* 预览卡片 */
.preview-card {
  background: #1a1a1a;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  border: 1px solid #333;
}

/* EXIF信息卡片 */
.info-card {
  background: #1a1a1a;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  border: 1px solid #333;
}

/* 其他样式 */
.image-container {
  padding: 2rem;
  text-align: center;
  background: #2a2a2a;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: default;
}

.detail-image {
  display: block;
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

/* 裁剪框样式 */
.crop-box {
  position: absolute;
  border: 2px solid #8a2be2;
  background: rgba(138, 43, 226, 0.1);
  cursor: move;
  z-index: 10;
  box-sizing: border-box;
}

.crop-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 1px dashed rgba(255, 255, 255, 0.5);
}

.crop-handle {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #8a2be2;
  border: 2px solid white;
  border-radius: 2px;
  z-index: 20;
}

.crop-handle.top-left {
  top: -6px;
  left: -6px;
  cursor: nwse-resize;
}

.crop-handle.top-right {
  top: -6px;
  right: -6px;
  cursor: nesw-resize;
}

.crop-handle.bottom-left {
  bottom: -6px;
  left: -6px;
  cursor: nesw-resize;
}

.crop-handle.bottom-right {
  bottom: -6px;
  right: -6px;
  cursor: nwse-resize;
}

/* 编辑控制面板 */
.edit-controls-panel {
  background: #1a1a1a;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid #333;
  padding: 1.5rem;
}

.edit-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background: #2a2a2a;
  border-radius: 12px;
  padding: 0.5rem;
  border: 1px solid #333;
}

.edit-tab {
  flex: 1;
  padding: 0.8rem;
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
  font-size: 0.9rem;
}

.edit-tab:hover:not(.active) {
  background: #333;
}

.edit-tab.active {
  background: #8a2be2;
  color: white;
  box-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.edit-tool-card {
  background: #2a2a2a;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #333;
  min-height: 200px;
}

.control-group {
  margin-bottom: 1.5rem;
}

.control-group h4 {
  color: #e0e0e0;
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
}

.slider-container {
  margin-bottom: 1rem;
}

/* 裁剪控件样式 */
.crop-instructions {
  color: #b0b0b0;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.crop-controls {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.rotation-buttons {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

/* 表单样式 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #e0e0e0;
  font-weight: 500;
}

/* 标签管理 */
.tag-input-group {
  margin-bottom: 1.5rem;
}

.tag-list {
  margin-top: 1.5rem;
}

.list-header {
  color: #e0e0e0;
  font-weight: 500;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #333;
}

.selected-tags {
  max-height: 200px;
  overflow-y: auto;
  padding: 1rem 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #333;
  background: #2a2a2a;
}

.card-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 1.25rem;
  font-weight: 600;
}

.card-body {
  padding: 1.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem 0;
  border-bottom: 1px solid #333;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #b0b0b0;
  font-weight: 500;
  min-width: 80px;
}

.info-item .value {
  color: #e0e0e0;
  text-align: right;
  flex: 1;
  margin-left: 1rem;
}

.info-item .value.text-green {
  color: #52c41a;
}

.info-item .value.text-gray {
  color: #999;
}

.description {
  line-height: 1.5;
  white-space: pre-wrap;
}

/* 标签样式 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.tag-item {
  margin: 0.25rem;
}

.no-tags {
  text-align: center;
  padding: 1rem;
  color: #b0b0b0;
}

.empty-text {
  font-style: italic;
}

/* 保存确认对话框 */
.save-confirm {
  text-align: center;
  padding: 1rem;
}

.save-info {
  margin: 1.5rem 0;
  text-align: left;
  background: #2a2a2a;
  padding: 1rem;
  border-radius: 8px;
}

.tip {
  color: #b0b0b0;
  font-size: 0.9rem;
  margin-top: 1rem;
}

/* 删除确认对话框 */
.delete-confirm {
  text-align: center;
  padding: 2rem;
}

.delete-confirm h3 {
  color: #ffffff;
  margin: 1rem 0;
}

.delete-confirm p {
  color: #b0b0b0;
  margin: 0;
}

/* 移动端响应式 */
@media (max-width: 1200px) {
  .photo-detail-page {
    padding: 1rem;
  }
  
  /* 头部在小屏幕上垂直排列 */
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 0.5rem;
  }
  
  /* 图片预览区域 */
  .image-container {
    min-height: 300px;
    padding: 1rem;
  }
  
  .detail-image {
    max-height: 300px;
  }
  
  /* 编辑标签页在小屏幕上两列布局 */
  .edit-tabs {
    flex-wrap: wrap;
  }
  
  .edit-tab {
    flex: 1 0 calc(50% - 0.5rem);
    margin-bottom: 0.25rem;
    font-size: 0.85rem;
    padding: 0.6rem;
  }
}

@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
  }
  
  .header-actions .el-button {
    width: 100%;
    margin-bottom: 0.5rem;
  }
  
  /* 卡片内边距 */
  .card-header,
  .card-body {
    padding: 1rem;
  }
  
  .edit-tool-card,
  .edit-controls-panel {
    padding: 1rem;
  }
  
  /* 编辑标签页在超小屏幕上单列布局 */
  .edit-tab {
    flex: 1 0 100%;
  }
  
  /* 信息项在小屏幕上垂直排列 */
  .info-item {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .info-item .value {
    text-align: left;
    margin-left: 0;
  }
  
  /* 裁剪控件和旋转按钮垂直排列 */
  .crop-controls,
  .rotation-buttons {
    flex-direction: column;
  }
  
  .crop-controls .el-button,
  .rotation-buttons .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .main-content {
    gap: 1rem;
  }
  
  .preview-section,
  .info-section {
    gap: 1rem;
  }
  
  /* 圆角 */
  .preview-card,
  .info-card,
  .edit-controls-panel {
    border-radius: 12px;
  }
  
  .edit-tool-card {
    border-radius: 10px;
  }
}

/* AI标签样式 */
.ai-tag-actions {
  display: flex;
  gap: 0.5rem;
}

.ai-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.ai-tag-item {
  margin: 0.25rem;
  position: relative;
  padding-right: 8px;
}

.ai-tag-item .confidence {
  font-size: 0.7rem;
  color: #666;
  margin-left: 2px;
}

.no-ai-tags {
  text-align: center;
  padding: 1rem;
  color: #b0b0b0;
  width: 100%;
}

/* 编辑状态下禁用AI标签操作 */
.ai-tag-item.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-tag-item.is-disabled .el-icon-close {
  display: none;
}
</style>