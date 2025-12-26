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
const getApiBaseURL = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
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
axios.defaults.timeout = 10000
axios.defaults.headers.common['Content-Type'] = 'application/json;charset=UTF-8'
app.config.globalProperties.$axios = axios

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.mount('#app')