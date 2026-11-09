import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.scss' // 全局样式变量和重置

/**
 * 应用初始化入口。
 *
 * <p>配置顺序：
 * <ol>
 *   <li>注册 Pinia 状态管理</li>
 *   <li>注册 Vue Router 路由</li>
 *   <li>注册 Element Plus 组件库及全部图标</li>
 *   <li>挂载到 #app 元素</li>
 * </ol>
 */
const app = createApp(App)

// 注册 Pinia 状态管理（用户认证、主题、标签页等状态）
app.use(createPinia())

// 注册路由（包含登录守卫和权限检查）
app.use(router)

// 注册 Element Plus 组件库
app.use(ElementPlus, { size: 'default', zIndex: 3000 })

// 全局注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载应用到 DOM
app.mount('#app')
