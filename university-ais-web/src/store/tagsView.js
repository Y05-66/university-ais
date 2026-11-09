/**
 * 标签页状态管理。
 *
 * <p>管理侧边栏顶部标签导航栏（tags-view）的状态：
 * <ul>
 *   <li>visitedViews — 已打开的标签页列表</li>
 *   <li>cachedViews — keep-alive 缓存的组件名称列表</li>
 * </ul>
 *
 * <p>标签页通过路由导航自动添加，通过关闭按钮删除。
 */
import { defineStore } from 'pinia'
import { ref, toRaw } from 'vue'
import { useRouter } from 'vue-router'

export const useTagsViewStore = defineStore('tagsView', () => {
  /** 已打开的标签页列表 */
  const visitedViews = ref([])
  /** keep-alive 缓存的组件名称列表 */
  const cachedViews = ref([])

  const addView = (view) => {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push({
      ...view,
      title: view.meta?.title || 'no-name',
    })
    if (view.meta?.affix) return
    if (!cachedViews.value.includes(view.name)) {
      cachedViews.value.push(view.name)
    }
  }

  const delView = (view) => {
    const idx = visitedViews.value.findIndex(v => v.path === view.path)
    if (idx > -1) {
      const item = visitedViews.value.splice(idx, 1)[0]
      const name = item.name
      if (name && cachedViews.value.includes(name)) {
        const cachedIdx = cachedViews.value.indexOf(name)
        cachedViews.value.splice(cachedIdx, 1)
      }
    }
  }

  const delVisitedView = () => {
    const route = useRouter().currentRoute
    return new Promise(resolve => {
      const currentPath = route.value.path
      const view = toRaw(visitedViews.value.find(v => v.path === currentPath))
      if (view) {
        delView(view)
      }
      resolve([...visitedViews.value])
    })
  }

  return {
    visitedViews,
    cachedViews,
    addView,
    delView,
    delVisitedView,
  }
})
