import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * 应用状态管理（主题、侧边栏、设备类型）。
 *
 * <p>管理内容：
 * <ul>
 *   <li>theme — 当前主题（light/dark），持久化到 localStorage</li>
 *   <li>sidebarOpened — 侧边栏展开/收起状态</li>
 *   <li>device — 设备类型（desktop/mobile）</li>
 * </ul>
 */
export const useAppStore = defineStore('app', () => {
  /** 侧边栏是否展开 */
  const sidebarOpened = ref(true)

  /** 设备类型：'desktop' | 'mobile' */
  const device = ref('desktop')

  /**
   * 当前主题模式：'light' | 'dark'。
   * 从 localStorage 读取，无值时默认为 'light'（白天模式）。
   */
  const theme = ref(localStorage.getItem('theme') || 'light')

  /**
   * 切换主题。
   * 在 light 和 dark 之间交替切换，并持久化到 localStorage。
   */
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
    applyTheme()
  }

  /**
   * 将主题应用到 DOM：给 body 添加对应的 CSS 类。
   * - light-theme：白天模式
   * - dark-theme：夜间模式
   */
  const applyTheme = () => {
    document.body.classList.remove('light-theme', 'dark-theme')
    document.body.classList.add(`${theme.value}-theme`)
  }

  /** 监听主题变化，自动同步到 DOM */
  watch(theme, (newTheme) => {
    localStorage.setItem('theme', newTheme)
    applyTheme()
  })

  /** 切换侧边栏展开/收起 */
  const toggleSidebar = () => {
    sidebarOpened.value = !sidebarOpened.value
  }

  /** 收起侧边栏 */
  const closeSidebar = () => {
    sidebarOpened.value = false
  }

  return {
    sidebarOpened,
    device,
    theme,
    toggleTheme,
    toggleSidebar,
    closeSidebar,
  }
})
