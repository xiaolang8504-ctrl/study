<template>
  <div class="session-expired">
    <el-result icon="warning" title="会话已失效" sub-title="请重新登录，登录后会重新加载当前账号的菜单权限。">
      <template slot="extra">
        <el-button type="primary" @click="$router.replace('/login')">重新登录</el-button>
      </template>
    </el-result>
  </div>
</template>

<script>
export default { name: 'SessionExpiredPage' }
</script>

<style scoped>
.session-expired { max-width: 760px; margin: 15vh auto 0; }
</style>
