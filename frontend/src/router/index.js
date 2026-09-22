import Vue from 'vue'
import Router from 'vue-router'
import Layout from '@/layout/index.vue'
import { buildSidebarMenus, getFirstMenuPath, hasMenuPath } from '@/config/menu'

Vue.use(Router)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/forbidden',
    name: 'Forbidden',
    component: () => import('@/views/forbidden/index.vue'),
    meta: { title: '无权访问' }
  },
  {
    path: '/session-expired',
    name: 'SessionExpired',
    component: () => import('@/views/session-expired/index.vue'),
    meta: { title: '会话已失效' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'el-icon-s-home' }
      },
      {
        path: 'system/wrongquestion',
        name: 'WrongQuestion',
        component: () => import('@/views/system/wrongquestion/index.vue'),
        meta: { title: '错题归档', icon: 'el-icon-notebook-2' }
      },
      {
        path: 'system/wrongquestion/detail',
        name: 'WrongQuestionDetail',
        component: () => import('@/views/system/wrongquestion/detail.vue'),
        meta: { title: '错题详情', icon: 'el-icon-document', menuPath: '/system/wrongquestion' }
      },
      {
        path: 'system/wrongquestion/capture',
        name: 'QuestionCapture',
        component: () => import('@/views/system/wrongquestion/capture.vue'),
        meta: { title: '采集中心', icon: 'el-icon-camera', menuPath: '/system/wrongquestion' }
      },
      {
        path: 'system/review',
        name: 'IntelligentReview',
        component: () => import('@/views/system/review/index.vue'),
        meta: { title: '智能复习', icon: 'el-icon-data-analysis' }
      },
      {
        path: 'system/review/today',
        name: 'TodayReview',
        component: () => import('@/views/system/review/today.vue'),
        meta: { title: '今日复习', icon: 'el-icon-date' }
      },
      {
        path: 'system/review/setting',
        name: 'ReviewPlanSetting',
        component: () => import('@/views/system/review/setting.vue'),
        meta: { title: '计划设置', icon: 'el-icon-setting' }
      },
      {
        path: 'system/review/history',
        name: 'ReviewHistory',
        component: () => import('@/views/system/review/history.vue'),
        meta: { title: '复习历史', icon: 'el-icon-time' }
      },
      {
        path: 'system/review/report',
        name: 'ReviewLearningReport',
        component: () => import('@/views/system/review/report.vue'),
        meta: { title: '学情报告', icon: 'el-icon-data-analysis', menuPath: '/system/review' }
      },
      {
        path: 'system/review/practice',
        name: 'PracticeSession',
        component: () => import('@/views/system/review/practice.vue'),
        meta: { title: '专项练习', icon: 'el-icon-edit-outline', menuPath: '/system/review' }
      },
      {
        path: 'system/guardian',
        name: 'GuardianCollaboration',
        component: () => import('@/views/system/guardian/index.vue'),
        meta: { title: '家庭协作', icon: 'el-icon-s-custom' }
      },
      {
        path: 'system/book',
        name: 'SystemBook',
        component: () => import('@/views/system/book/index.vue'),
        meta: { title: '课本管理', icon: 'el-icon-reading' }
      },
      {
        path: 'system/question-bank',
        name: 'QuestionBank',
        component: () => import('@/views/system/questionbank/index.vue'),
        meta: { title: '题库管理', icon: 'el-icon-collection' }
      },
      {
        path: 'system/question-image',
        name: 'QuestionImage',
        component: () => import('@/views/system/questionimage/index.vue'),
        meta: { title: '图片管理', icon: 'el-icon-picture-outline' }
      },
      {
        path: 'system/similar-practice',
        name: 'SimilarPractice',
        component: () => import('@/views/system/questionbank/practice.vue'),
        meta: { title: '相似题练习', icon: 'el-icon-magic-stick' }
      },
      {
        path: 'system/similar-practice-history',
        name: 'SimilarPracticeHistory',
        component: () => import('@/views/system/questionbank/practice-history.vue'),
        meta: { title: '练习历史', icon: 'el-icon-time' }
      },
      {
        path: 'system/question-experiment',
        name: 'QuestionExperimentAdmin',
        component: () => import('@/views/system/questionbank/experiment.vue'),
        meta: { title: 'A/B实验管理', icon: 'el-icon-data-analysis' }
      },
      {
        path: 'system/homework',
        name: 'SystemHomeWork',
        component: () => import('@/views/system/homework/index.vue'),
        meta: { title: '作业管理', icon: 'el-icon-edit-outline' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'el-icon-user' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'el-icon-s-custom' }
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'el-icon-office-building' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'el-icon-menu' }
      },
      {
        path: 'system/resource',
        name: 'SystemResource',
        component: () => import('@/views/system/resource/index.vue'),
        meta: { title: 'API管理', icon: 'el-icon-connection' }
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字段管理', icon: 'el-icon-collection-tag' }
      }
    ]
  }
]

const router = new Router({
  mode: 'hash',
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta && to.meta.title ? `${to.meta.title} - 错题系统` : '错题系统'
  if (to.path === '/login' || to.path === '/session-expired') {
    next()
    return
  }
  const token = localStorage.getItem('study_access_token')
  if (!token) {
    next('/login')
    return
  }
  if (to.path === '/forbidden') {
    next()
    return
  }
  // 每次导航重新读取服务端菜单，撤权或切换账号后不沿用旧页面权限。
  import('@/api/system/menu').then(({ currentMenuTree }) => currentMenuTree()).then(tree => {
    if (localStorage.getItem('study_access_token') !== token) {
      next('/login')
      return
    }
    const menus = buildSidebarMenus(tree)
    const requiredPath = to.meta.menuPath || to.path
    if (hasMenuPath(menus, requiredPath)) {
      next()
      return
    }
    if (to.path === '/dashboard') {
      const firstPath = getFirstMenuPath(menus)
      if (firstPath) {
        next({ path: firstPath, replace: true })
        return
      }
    }
    next({ path: '/forbidden', replace: true })
  }).catch(() => {
    next(localStorage.getItem('study_access_token') ? '/forbidden' : '/login')
  })
})

export default router
