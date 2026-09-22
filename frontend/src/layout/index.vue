<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout-aside">
      <div class="brand"><span class="brand-mark">✓</span><span><strong>智错本</strong><small>学生学习空间</small></span></div>
      <el-menu :default-active="$route.path" router background-color="#1f2937" text-color="#cbd5e1" active-text-color="#ffffff">
        <sidebar-menu-item v-for="menu in sidebarMenus" :key="menu.code" :menu="menu" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="page-title">{{ $route.meta.title }}</div>
        <div class="user-actions">
          <review-reminder-popover />
          <button class="user-info" type="button" @click="openUserInfo">
            <i class="el-icon-user-solid"></i>
            <span>{{ username }}</span>
          </button>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <el-button class="account-button">账号设置<i class="el-icon-arrow-down el-icon--right" /></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="password" icon="el-icon-key">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog title="个人信息" :visible.sync="userInfoOpen" width="560px" append-to-body>
      <el-skeleton :loading="userInfoLoading" animated :rows="6">
        <el-descriptions v-if="userInfo" :column="2" border>
          <el-descriptions-item label="账号">{{ userInfo.userName || username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ userInfo.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="2">
            <el-tag v-for="role in roleNameList" :key="role" size="mini" class="info-tag">{{ role }}</el-tag>
            <span v-if="roleNameList.length === 0">-</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-skeleton>
    </el-dialog>

    <el-dialog title="修改密码" :visible.sync="passwordOpen" width="460px" append-to-body>
      <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="passWord">
          <el-input v-model="passwordForm.passWord" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassWord">
          <el-input v-model="passwordForm.newPassWord" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassWord">
          <el-input v-model="passwordForm.confirmPassWord" show-password />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="passwordOpen = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">保存</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
import { getCurrentUserInfo, updateUserPassWord } from '@/api/system/user'
import { outLogin } from '@/api/system/login'
import { currentMenuTree } from '@/api/system/menu'
import { buildSidebarMenus, getFirstMenuPath, hasMenuPath } from '@/config/menu'
import SidebarMenuItem from './components/SidebarMenuItem.vue'
import ReviewReminderPopover from './components/ReviewReminderPopover.vue'

export default {
  name: 'AppLayout',
  components: { SidebarMenuItem, ReviewReminderPopover },
  data() {
    return {
      sidebarMenus: [],
      userInfoOpen: false,
      userInfoLoading: false,
      userInfo: null,
      passwordOpen: false,
      passwordForm: this.getDefaultPasswordForm(),
      passwordRules: {
        passWord: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassWord: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
        confirmPassWord: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadCurrentMenu()
  },
  watch: {
    '$route.path'() {
      this.loadCurrentMenu()
    }
  },
  computed: {
    username() {
      return localStorage.getItem('study_username') || '当前用户'
    },
    roleNameList() {
      if (!this.userInfo || !this.userInfo.roleNames) {
        return []
      }
      if (Array.isArray(this.userInfo.roleNames)) {
        return this.userInfo.roleNames
      }
      if (typeof this.userInfo.roleNames === 'string') {
        return this.userInfo.roleNames.split(',').filter(Boolean)
      }
      return Array.from(this.userInfo.roleNames)
    }
  },
  methods: {
    handleUserCommand(command) {
      // 账号菜单只负责分发操作，密码校验与退出清理仍由各自函数处理。
      if (command === 'password') {
        this.openPassword()
      } else if (command === 'logout') {
        this.logout()
      }
    },
    loadCurrentMenu() {
      // 菜单以后端权限树为准；当前路由无权访问时自动跳到第一个可用页面。
      currentMenuTree().then(data => {
        this.sidebarMenus = buildSidebarMenus(data)
        const requiredPath = this.$route.meta.menuPath || this.$route.path
        if (!hasMenuPath(this.sidebarMenus, requiredPath)) {
          const firstPath = getFirstMenuPath(this.sidebarMenus)
          if (firstPath) {
            this.$router.replace(firstPath)
          }
        }
      }).catch(() => {
        this.sidebarMenus = []
      })
    },
    getDefaultPasswordForm() {
      return {
        passWord: '',
        newPassWord: '',
        confirmPassWord: ''
      }
    },
    openUserInfo() {
      this.userInfoOpen = true
      this.userInfoLoading = true
      getCurrentUserInfo().then(data => {
        this.userInfo = data || {}
        if (this.userInfo.userName) {
          localStorage.setItem('study_username', this.userInfo.userName)
        }
      }).catch(() => {
        this.userInfo = {
          userName: this.username
        }
      }).finally(() => {
        this.userInfoLoading = false
      })
    },
    openPassword() {
      this.passwordForm = this.getDefaultPasswordForm()
      this.passwordOpen = true
      this.$nextTick(() => this.$refs.passwordRef && this.$refs.passwordRef.clearValidate())
    },
    submitPassword() {
      // 先执行表单规则，再校验两次新密码，避免向后端发送必然失败的请求。
      this.$refs.passwordRef.validate(valid => {
        if (!valid) {
          return
        }
        if (this.passwordForm.newPassWord !== this.passwordForm.confirmPassWord) {
          this.$message.warning('两次密码不一致')
          return
        }
        updateUserPassWord(this.passwordForm).then(() => {
          this.$message.success('密码已修改')
          this.passwordOpen = false
        }).catch(() => {})
      })
    },
    logout() {
      // 无论服务端注销是否成功都清除本地凭证，避免失效令牌继续留在浏览器中。
      outLogin().catch(() => {}).finally(() => {
        localStorage.removeItem('study_access_token')
        localStorage.removeItem('study_username')
        this.$router.replace('/login')
      })
    }
  }
}
</script>
