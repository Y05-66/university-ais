<template>
  <div class="main-layout">
    <div class="sidebar" :class="{ collapsed: !sidebarOpened }">
      <div id="sidebar-logo" class="logo">
        <el-icon size="22" :style="{ color: 'rgba(255,255,255,0.85)' }"><School /></el-icon>
        <span v-show="sidebarOpened" class="logo-text" :style="{ color: 'rgba(255,255,255,0.85)' }">教务系统</span>
      </div>
      <el-menu ref="menuRef" :default-active="activeMenu" :collapse="!sidebarOpened" :collapse-transition="false" @select="handleMenuSelect" class="sidebar-menu">
        <template v-if="menuList.length > 0">
          <template v-for="menu in menuList" :key="menu.path">
            <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
              <template #title>
                <el-icon><component :is="menu.icon || 'Folder'" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path.startsWith('/') ? child.path : (menu.path + '/' + child.path)">
                <el-icon><component :is="child.icon || 'Document'" /></el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="menu.icon || 'Document'" /></el-icon>
              <template #title>{{ menu.title }}</template>
            </el-menu-item>
          </template>
        </template>
        <template v-else>
          <el-menu-item disabled><el-icon><Loading /></el-icon><span>加载中...</span></el-menu-item>
        </template>
      </el-menu>
    </div>
    <div class="main-wrapper">
      <div class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar"><Fold v-if="sidebarOpened" /><Expand v-else /></el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">{{ item.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button circle class="icon-btn" @click="toggleTheme">
            <el-icon><component :is="appStore.theme === 'light' ? 'Moon' : 'Sunny'" /></el-icon>
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar || undefined" icon="User" />
              <span class="username">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-tag v-if="userStore.isAdmin()" type="danger" size="small" class="role-tag">管理员</el-tag>
              <el-tag v-else-if="userStore.isTeacher()" type="success" size="small" class="role-tag">教师</el-tag>
              <el-tag v-else type="warning" size="small" class="role-tag">学生</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User" @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="tags-view" v-if="showTagsView">
        <el-tag v-for="tag in tagsViewStore.visitedViews" :key="tag.path" :closable="tag.name !== 'Dashboard'" :type="tag.path === $route.path ? 'primary' : ''" size="small" @click="router.push(tag.path)" @close="tagsViewStore.delView(tag)">{{ tag.title }}</el-tag>
      </div>
      <div class="main-content"><router-view /></div>
    </div>
    <AiFloatingAvatar />
  </div>
</template>

<script setup>
import { computed, watch, ref, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useTagsViewStore } from '@/store/tagsView'
import { useAppStore } from '@/store/app'
import AiFloatingAvatar from '@/components/AiFloatingAvatar.vue'
import { School, Fold, Expand, ArrowDown, SwitchButton, House, Folder, Document, User, Avatar, Bell, Select, EditPen, DataAnalysis, Calendar, Star, Setting, OfficeBuilding, Loading } from '@element-plus/icons-vue'
import { markNavigation } from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tagsViewStore = useTagsViewStore()
const appStore = useAppStore()

const sidebarOpened = computed(() => appStore.sidebarOpened)
const activeMenu = computed(() => route.path)
const showTagsView = computed(() => !route.meta.hidden)
const menuRef = ref(null)
const isMenuReady = ref(false)
const isNavigating = ref(false)

watch(() => userStore.menus, (menus) => {
  if (!menus.length || isMenuReady.value) return
  isMenuReady.value = true
  nextTick(() => { if (menuRef.value) menuRef.value.openedMenus = [] })
})

const DEFAULT_MENUS = [
  { path: '/dashboard', title: '首页', icon: 'House', children: [] },
  { path: '/basic', title: '基础管理', icon: 'Folder', roles: ['ROLE_ADMIN'], children: [{ path: '/basic/college', title: '学院管理', icon: 'Document' }, { path: '/basic/major', title: '专业管理', icon: 'Document' }, { path: '/basic/class', title: '班级管理', icon: 'Document' }] },
  { path: '/student/list', title: '学生管理', icon: 'User', roles: ['ROLE_ADMIN'], children: [{ path: '/student/list', title: '学生列表', icon: 'Document' }] },
  { path: '/teacher/list', title: '教师管理', icon: 'Avatar', roles: ['ROLE_ADMIN'], children: [{ path: '/teacher/list', title: '教师列表', icon: 'Document' }] },
  { path: '/program', title: '教务管理', icon: 'Document', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'], children: [{ path: '/program/course', title: '课程管理', icon: 'Document' }, { path: '/program/offering', title: '开课管理', icon: 'EditPen' }] },
  { path: '/enrollment', title: '选课管理', icon: 'Select', roles: ['ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT'], children: [{ path: '/enrollment/my', title: '我的选课', icon: 'Select', roles: ['ROLE_STUDENT'] }, { path: '/enrollment/select', title: '选择课程', icon: 'Document', roles: ['ROLE_STUDENT'] }, { path: '/enrollment/roster', title: '选课名单', icon: 'Document', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }, { path: '/enrollment/timetable', title: '我的课表', icon: 'Calendar', roles: ['ROLE_TEACHER', 'ROLE_STUDENT'] }] },
  { path: '/score', title: '成绩管理', icon: 'DataAnalysis', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'], children: [{ path: '/score/student-input', title: '成绩录入', icon: 'EditPen', roles: ['ROLE_TEACHER', 'ROLE_ADMIN'] }] },
  { path: '/score', title: '成绩管理', icon: 'DataAnalysis', roles: ['ROLE_STUDENT'], children: [{ path: '/score/my', title: '我的成绩', icon: 'DataAnalysis', roles: ['ROLE_STUDENT'] }] },
  { path: '/exam', title: '考试管理', icon: 'Calendar', roles: ['ROLE_ADMIN'], children: [{ path: '/exam/arrange', title: '考试安排', icon: 'Calendar' }, { path: '/exam/room', title: '考场分配', icon: 'OfficeBuilding' }, { path: '/exam/invigilate', title: '监考安排', icon: 'User' }] },
  { path: '/exam/my-invigilate', title: '我的监考', icon: 'User', roles: ['ROLE_TEACHER'], children: [] },
  { path: '/exam/my-exam', title: '我的考试安排', icon: 'Calendar', roles: ['ROLE_STUDENT'], children: [] },
  { path: '/evaluation/submit', title: '教学评价', icon: 'Star', roles: ['ROLE_STUDENT'], children: [{ path: '/evaluation/submit', title: '教学评价', icon: 'Star' }] },
  { path: '/notice', title: '通知公告', icon: 'Bell', children: [{ path: '/notice/list', title: '公告管理', icon: 'Bell', roles: ['ROLE_ADMIN'] }, { path: '/notice/my', title: '我的通知', icon: 'Bell' }] },
  { path: '/system', title: '系统管理', icon: 'Setting', roles: ['ROLE_ADMIN'], children: [{ path: '/system/user', title: '用户管理', icon: 'User' }, { path: '/system/role', title: '角色管理', icon: 'Avatar' }, { path: '/system/menu', title: '菜单管理', icon: 'Document' }, { path: '/system/dict', title: '数据字典', icon: 'Document' }, { path: '/system/log', title: '操作日志', icon: 'Document' }] },
]

const menuList = computed(() => {
  const serverMenus = userStore.menus || []
  if (serverMenus.length > 0) return buildMenuTree(serverMenus)
  return DEFAULT_MENUS.filter(m => {
    if (m.roles) return m.roles.some(r => userStore.roles.includes(r))
    if (m.children) { const visible = m.children.filter(c => !c.roles || c.roles.some(r => userStore.roles.includes(r))); if (visible.length === 0) return false }
    return true
  })
})

function buildMenuTree(items) {
  return [...items].sort((a, b) => (a.sort || 0) - (b.sort || 0)).filter(item => item.visible !== 0 && item.visible !== false).map(item => ({ path: item.path || `/${item.id}`, title: item.name, icon: item.icon || 'Document', children: item.children ? buildMenuTree(item.children) : [] }))
}

const breadcrumbs = computed(() => route.matched.filter(item => item.meta?.title).map(item => ({ path: item.path, title: item.meta.title })))
const toggleFullscreen = () => document.documentElement[document.fullscreenElement ? 'exitFullscreen' : 'requestFullscreen']()
const toggleTheme = () => appStore.toggleTheme()
const toggleSidebar = () => appStore.toggleSidebar()

// 菜单选择处理 - 防止重复点击导致的闪烁问题
const handleMenuSelect = (index) => {
  // 如果正在导航中，直接返回
  if (isNavigating.value) return

  const cleanIndex = index.replace(/\/$/, '')
  if (!cleanIndex.startsWith('/')) return

  // 已经在目标页面则不重复导航
  if (route.path === cleanIndex) return

  isNavigating.value = true
  router.push(cleanIndex).catch(() => {}).finally(() => {
    setTimeout(() => { isNavigating.value = false }, 200)
  })
}

const handleCommand = (command) => command === 'logout' ? userStore.logoutAsync() : router.push('/profile')
</script>

<style lang="scss">
.main-layout { display: flex; height: 100vh; overflow: hidden; }
.sidebar { width: 220px; min-width: 220px; display: flex; flex-direction: column; transition: width 0.3s ease; overflow: hidden; flex-shrink: 0; box-shadow: 2px 0 8px rgba(0,0,0,0.15); background: #304156 !important; }
.sidebar.collapsed { width: 64px; min-width: 64px; }
#sidebar-logo { color: #ffffff !important; }
#sidebar-logo .el-icon,
#sidebar-logo .logo-text { color: #ffffff !important; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(255,255,255,0.1); flex-shrink: 0; gap: 10px; }
.dark-theme .logo { border-color: #21262d; }
.logo-text { white-space: nowrap; letter-spacing: 1px; font-size: 16px; font-weight: 600; }
.sidebar-menu { flex: 1; overflow-y: auto; overflow-x: hidden; border-right: none; &::-webkit-scrollbar { width: 0; } }
:deep(.sidebar-menu) {
  .light-theme & {
    .el-menu-item, .el-sub-menu__title { color: #ffffff !important; .el-icon { color: #ffffff !important; } &:hover { background: rgba(255,255,255,0.12) !important; color: #fff !important; } }
    .el-menu-item.is-active { background: #409eff !important; color: #fff !important; border-left: 3px solid #fff; }
    .el-sub-menu.is-opened > .el-sub-menu__title { color: rgba(255,255,255,0.95) !important; }
    .el-menu--inline { background: rgba(0,0,0,0.15) !important; }
  }
  .dark-theme & {
    background: transparent !important;
    .el-menu-item, .el-sub-menu__title { color: #8b949e !important; .el-icon { color: #8b949e !important; } &:hover { background: rgba(255,255,255,0.05) !important; color: #e6edf3 !important; } }
    .el-menu-item.is-active { background: #1f6feb !important; color: #fff !important; border-left: 3px solid #58a6ff; }
    .el-sub-menu.is-opened > .el-sub-menu__title { color: #c9d1d9 !important; }
    .el-menu--inline { background: rgba(0,0,0,0.2) !important; }
  }
}
.main-wrapper { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.header { height: 56px; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; flex-shrink: 0; border-bottom: 1px solid; }
.light-theme .header { background: #fff; border-color: #e4e7ed; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.dark-theme .header { background: #161b22; border-color: #21262d; box-shadow: 0 1px 3px rgba(0,0,0,0.3); }
.header-left { display: flex; align-items: center; gap: 8px; }
.collapse-btn { font-size: 20px; cursor: pointer; padding: 4px; border-radius: 4px; transition: all 0.2s; }
.light-theme .collapse-btn { color: #606266; &:hover { color: #409eff; background: #ecf5ff; } }
.dark-theme .collapse-btn { color: #8b949e; &:hover { color: #58a6ff; background: rgba(88,166,255,0.1); } }
.header-right { display: flex; align-items: center; gap: 8px; }
.icon-btn { .light-theme & { color: #606266; border-color: #dcdfe6; &:hover { color: #409eff; border-color: #409eff; } } .dark-theme & { color: #8b949e; border-color: #30363d; &:hover { color: #58a6ff; border-color: #58a6ff; } } }
.user-info { display: flex; align-items: center; gap: 6px; cursor: pointer; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; }
.light-theme .user-info:hover { background: #f5f7fa; }
.dark-theme .user-info:hover { background: #21262d; }
.username { font-size: 14px; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.light-theme .username { color: #303133; }
.dark-theme .username { color: #c9d1d9; }
.role-tag { margin-left: 2px; }
.tags-view { height: 36px; display: flex; align-items: center; gap: 6px; padding: 0 12px; overflow-x: auto; flex-shrink: 0; border-bottom: 1px solid; }
.light-theme .tags-view { background: #fff; border-color: #e4e7ed; }
.dark-theme .tags-view { background: #161b22; border-color: #21262d; }
.tags-view::-webkit-scrollbar { height: 0; }
.main-content { flex: 1; overflow-y: auto; padding: 16px; min-height: 0; }
.light-theme .main-content { background: #f0f2f5; }
.dark-theme .main-content { background: #0d1117; }
</style>
