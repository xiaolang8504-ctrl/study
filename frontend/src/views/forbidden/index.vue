<template>
  <div class="access-state">
    <h1>无权访问此页面</h1>
    <p>当前账号没有对应菜单权限，或权限已发生变化。</p>
    <el-button type="primary" @click="$router.replace('/')">返回可用页面</el-button>
    <el-button @click="$router.replace('/login')">切换账号</el-button>
  </div>
</template>

<script>
export default { name: 'ForbiddenPage' }
</script>

<style scoped>
.access-state { max-width: 520px; margin: 15vh auto; padding: 40px; text-align: center; }
.access-state p { color: #606266; margin-bottom: 28px; }
</style>
