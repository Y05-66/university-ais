import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useTagsViewStore } from '@/store/tagsView'
import { markNavigation } from '@/api/request'

/**
 * 所有静态路由定义。
 *
 * <p>路由设计原则：
 * <ul>
 *   <li>登录页(/login)和404页不包裹在 MainLayout 中（meta.hidden=true）</li>
 *   <li>所有业务页面包裹在 MainLayout 中，通过服务端菜单驱动导航</li>
 *   <li>路由守卫负责认证检查和用户信息获取</li>
 * </ul>
 */
export const constantRoutes = [
  /** 登录页 */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true, title: '登录' },
  },
  /** 404 页面 */
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true },
  },
  /**
   * 主布局根路径。
   * 包含侧边栏、顶栏、标签页、主内容区。
   * 默认重定向到首页(/dashboard)。
   */
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: 'dashboard',
    children: [
      /** 首页仪表盘 — 显示统计数据、快捷入口、最新公告 */
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House', affix: true },
      },
      /** 基础管理 — 学院、专业、班级管理 */
      {
        path: 'basic',
        redirect: 'college',
        meta: { title: '基础管理' },
        children: [
          {
            path: 'college',
            name: 'College',
            component: () => import('@/views/basic/college/index.vue'),
            meta: { title: '学院管理' },
          },
          {
            path: 'major',
            name: 'Major',
            component: () => import('@/views/basic/major/index.vue'),
            meta: { title: '专业管理' },
          },
          {
            path: 'class',
            name: 'Class',
            component: () => import('@/views/basic/class/index.vue'),
            meta: { title: '班级管理' },
          },
        ],
      },
      /** 学生管理 */
      {
        path: 'student',
        redirect: 'list',
        meta: { title: '学生管理' },
        children: [
          {
            path: 'list',
            name: 'StudentList',
            component: () => import('@/views/basic/student/index.vue'),
            meta: { title: '学生列表' },
          },
        ],
      },
      /** 教师管理 */
      {
        path: 'teacher',
        redirect: 'list',
        meta: { title: '教师管理' },
        children: [
          {
            path: 'list',
            name: 'TeacherList',
            component: () => import('@/views/basic/teacher/index.vue'),
            meta: { title: '教师列表' },
          },
        ],
      },
      /** 教务管理 — 课程、开课 */
      {
        path: 'program',
        redirect: 'course',
        meta: { title: '教务管理' },
        children: [
          {
            path: 'course',
            name: 'ProgramCourse',
            component: () => import('@/views/program/course/index.vue'),
            meta: { title: '课程管理' },
          },
          {
            path: 'offering',
            name: 'ProgramOffering',
            component: () => import('@/views/program/offering/index.vue'),
            meta: { title: '开课管理' },
          },
        ],
      },
      /** 选课管理 — 我的选课、选择课程、选课名单 */
      {
        path: 'enrollment',
        redirect: 'my',
        meta: { title: '选课管理' },
        children: [
          {
            path: 'my',
            name: 'EnrollmentMy',
            component: () => import('@/views/enrollment/my/index.vue'),
            meta: { title: '我的选课' },
          },
          {
            path: 'select',
            name: 'EnrollmentSelect',
            component: () => import('@/views/enrollment/select/index.vue'),
            meta: { title: '选择课程' },
          },
          {
            path: 'roster',
            name: 'EnrollmentRoster',
            component: () => import('@/views/enrollment/roster/index.vue'),
            meta: { title: '选课名单' },
          },
        ],
      },
      /** 我的课表（教师用） */
      {
        path: 'enrollment/timetable',
        name: 'EnrollmentTimetable',
        component: () => import('@/views/enrollment/timetable/index.vue'),
        meta: { title: '我的课表' },
      },
      /** 课表查询（管理员用） */
      {
        path: 'enrollment/timetable-query',
        name: 'EnrollmentTimetableQuery',
        component: () => import('@/views/enrollment/timetable-query/index.vue'),
        meta: { title: '课表查询' },
      },
      /** 成绩管理 — 录入（管理员/教师按学生视图，后端按角色过滤） */
      {
        path: 'score',
        redirect: 'student-input',
        meta: { title: '成绩管理' },
        children: [
          {
            path: 'student-input',
            name: 'ScoreStudentInput',
            component: () => import('@/views/score/student-input/index.vue'),
            meta: { title: '成绩录入' },
          },
          {
            path: 'my',
            name: 'ScoreMy',
            component: () => import('@/views/score/my/index.vue'),
            meta: { title: '我的成绩' },
          },
        ],
      },
      /** 教学评价（学生用） */
      /** 考试管理（管理员） — 考试安排、考场分配、监考安排 */
      {
        path: 'exam',
        redirect: 'arrange',
        meta: { title: '考试管理' },
        children: [
          {
            path: 'arrange',
            name: 'ExamArrange',
            component: () => import('@/views/exam/arrange/index.vue'),
            meta: { title: '考试安排' },
          },
          {
            path: 'room',
            name: 'ExamRoom',
            component: () => import('@/views/exam/room/index.vue'),
            meta: { title: '考场分配' },
          },
          {
            path: 'invigilate',
            name: 'ExamInvigilate',
            component: () => import('@/views/exam/invigilate/index.vue'),
            meta: { title: '监考安排' },
          },
          /** 我的考试安排（学生用） — 查看当前学生选课的考试安排 */
          {
            path: 'my-exam',
            name: 'MyExam',
            component: () => import('@/views/exam/my-exam/index.vue'),
            meta: { title: '我的考试安排' },
          },
          /** 我的监考（教师用） — 查看当前教师被安排的监考任务 */
          {
            path: 'my-invigilate',
            name: 'MyInvigilate',
            component: () => import('@/views/exam/my-invigilate/index.vue'),
            meta: { title: '我的监考' },
          },
        ],
      },
      {
        path: 'evaluation',
        redirect: 'submit',
        meta: { title: '教学评价' },
        children: [
          {
            path: 'submit',
            name: 'EvaluationSubmit',
            component: () => import('@/views/evaluation/submit/index.vue'),
            meta: { title: '教学评价' },
          },
        ],
      },
      /** 通知公告 — 公告管理（管理员）、我的通知 */
      {
        path: 'notice',
        redirect: 'list',
        meta: { title: '通知公告' },
        children: [
          {
            path: 'list',
            name: 'NoticeList',
            component: () => import('@/views/notice/index.vue'),
            meta: { title: '公告管理' },
          },
          {
            path: 'my',
            name: 'NoticeMy',
            component: () => import('@/views/notice/my/index.vue'),
            meta: { title: '我的通知' },
          },
        ],
      },
      /** 系统管理 — 用户、角色、菜单、字典、日志 */
      {
        path: 'system',
        redirect: 'user',
        meta: { title: '系统管理' },
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理' },
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' },
          },
          {
            path: 'menu',
            name: 'SystemMenu',
            component: () => import('@/views/system/menu/index.vue'),
            meta: { title: '菜单管理' },
          },
          {
            path: 'dict',
            name: 'SystemDict',
            component: () => import('@/views/system/dict/index.vue'),
            meta: { title: '数据字典' },
          },
          {
            path: 'log',
            name: 'SystemLog',
            component: () => import('@/views/system/log/index.vue'),
            meta: { title: '操作日志' },
          },
        ],
      },
      /** 个人中心（不在侧边栏显示） */
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true },
      },
    ],
  },
  /** 通配符路由 — 未匹配的路径重定向到404 */
  { path: '/:pathMatch(.*)*', redirect: '/404', hidden: true },
]

/** 创建 Vue Router 实例 */
const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
})

/**
 * 路由守卫：简化版，避免复杂队列导致的竞态问题。
 *
 * <p>关键设计：
 * <ul>
 *   <li>只在首次加载时调用 /auth/info，后续导航直接使用缓存的用户信息</li>
 *   <li>不依赖复杂的navQueue/penddingNav机制，减少状态管理复杂度</li>
 *   <li>Token无效时清除状态并跳转登录页</li>
 * </ul>
 */
router.beforeEach(async (to, from, next) => {
  // 标记导航开始，防止后续401请求触发登出
  markNavigation()
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - 大学教务管理系统` : '大学教务管理系统'

  const userStore = useUserStore()
  const tagsViewStore = useTagsViewStore()

  // 已登录用户访问登录页 → 重定向到首页
  if (userStore.token && to.path === '/login') {
    tagsViewStore.visitedViews = []
    tagsViewStore.cachedViews = []
    console.log('[guard] already logged in, redirect to dashboard')
    next({ path: '/dashboard', replace: true })
    return
  }

  // 未登录且访问受保护页面 → 跳转到登录页（仅在不在登录页时）
  if (!userStore.token && to.path !== '/login') {
    console.warn('[guard] no token, redirecting to login:', to.fullPath)
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  // 已登录用户访问受保护页面
  if (userStore.token && to.path !== '/login') {
    // 只有当没有用户信息时才重新获取（首次加载/会话恢复）
    // 注意：如果 token 还在但 userInfo 被清空（如导航期间发生401），重新验证 token
    if (!userStore.userInfo) {
      try {
        await userStore.getInfo()
      } catch (err) {
        // Token无效时清除状态并跳转登录
        const status = err.response?.status
        const code = err.response?.data?.code
        console.warn('[guard] getInfo failed:', err.message, 'status:', status, 'code:', code)
        if (status === 401 || code === 401) {
          userStore.clearAuthState()
          tagsViewStore.visitedViews = []
          tagsViewStore.cachedViews = []
          // 使用 replace 避免再次触发守卫
          window.location.replace(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
          return
        }
        // 其他错误允许继续导航
        console.warn('[router guard] getInfo failed:', err.message)
      }
    }
    // 在放行前再次检查 token 是否仍然有效（防止导航过程中 token 被清除）
    if (!userStore.token) {
      console.warn('[guard] token cleared during guard, redirecting to login')
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
    // 正常放行
    console.log('[guard] allowing navigation to:', to.path)
    next()
    return
  }

  // 未登录用户在登录页 → 直接放行
  if (!userStore.token && to.path === '/login') {
    next()
    return
  }

  // 兜底：未处理的情况也放行到登录页（防止无限循环）
  next('/login')
})

/** 路由切换后滚动到页面顶部并更新标签页 */
router.afterEach((to) => {
  window.scrollTo(0, 0)
  const tagsViewStore = useTagsViewStore()
  if (to.path && !to.meta.hidden) {
    tagsViewStore.addView({ path: to.path, name: to.name, meta: to.meta })
  }
})

export default router

// 导出导航状态供外部使用
export const navState = {
  pendingNav: false,
  lastNavigationTime: 0
}
