<template>
  <el-submenu v-if="menu.children.length" :index="`menu-${menu.code}`">
    <template slot="title">
      <i :class="menu.icon"></i>
      <span>{{ menu.title }}</span>
    </template>
    <sidebar-menu-item v-for="child in menu.children" :key="child.code" :menu="child" />
  </el-submenu>
  <el-menu-item v-else :index="menu.path">
    <i :class="menu.icon"></i>
    <span slot="title">{{ menu.title }}</span>
  </el-menu-item>
</template>

<script>
export default {
  name: 'SidebarMenuItem',
  props: {
    menu: {
      type: Object,
      required: true
    }
  }
}
</script>
