import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

const app = createApp(App)

// 自动检测API地址：优先使用环境变量，否则根据当前主机名判断
// 如果是localhost，使用localhost；否则使用当前主机名（允许通过IP访问）
// 在Docker环境中，如果端口是80，则使用相对路径通过nginx代理
const getApiBaseURL = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  // 如果当前端口是80（Docker nginx），使用/api/前缀通过nginx代理
  if (window.location.port === '' || window.location.port === '80') {
    return '/api'
  }
  // 在生产环境或通过IP访问时，使用当前主机名
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8088'
  }
  // 使用当前主机名，这样手机可以通过IP访问
  return `http://${hostname}:8088`
}

axios.defaults.baseURL = getApiBaseURL()
// 增加超时时间，等待后端服务启动（Docker环境）
axios.defaults.timeout = 60000
axios.defaults.headers.common['Content-Type'] = 'application/json;charset=UTF-8'
app.config.globalProperties.$axios = axios

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.mount('#app')