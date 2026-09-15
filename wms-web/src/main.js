import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as EPIcons from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'
import axios from 'axios'
import App from './App.vue'
import router from './router'
import { useMenuStore } from './stores/menu'
import './assets/global.css'

const app = createApp(App)

// 全局属性：$aiUrl 空串 = 走同源代理（开发由 vite.config.js proxy、生产由 Nginx 转发到 AI 服务）
app.config.globalProperties.$axios = axios

// axios 全局配置：跨域请求携带 Cookie（Spring Session 鉴权必需）
axios.defaults.withCredentials = true

// 响应拦截器：401 未授权 → 清除本地状态 → 跳转登录页
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem('CurUser')
      sessionStorage.removeItem('MenuList')
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)
app.config.globalProperties.$httpUrl = import.meta.env.VITE_API_URL || 'http://localhost:8090'
const rawAiUrl = import.meta.env.VITE_AI_URL
app.config.globalProperties.$aiUrl = (rawAiUrl != null && rawAiUrl.trim() !== '') ? rawAiUrl : ''

const pinia = createPinia()
app.use(pinia)
useMenuStore(pinia).restoreFromStorage() // 刷新后恢复菜单与动态路由

app.use(router)

// Element Plus：中文语言包必须显式配置，size:'small' 等价原 Vue.use(ElementUI,{size:'small'})
app.use(ElementPlus, { locale: zhCn, size: 'small' })

// 全局方法兜底：保证 this.$message/$confirm 等调用行为与 Vue 2 版一致
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$confirm = ElMessageBox.confirm
app.config.globalProperties.$alert = ElMessageBox.alert
app.config.globalProperties.$prompt = ElMessageBox.prompt
app.config.globalProperties.$notify = ElNotification
app.config.globalProperties.$loading = ElLoading.service

// 全局注册全部 Element Plus 图标组件
for (const [name, comp] of Object.entries(EPIcons)) {
  app.component(name, comp)
}

app.mount('#app')
