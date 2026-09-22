<template>
  <el-popover
    v-model="visible"
    placement="bottom-end"
    width="380"
    trigger="click"
    popper-class="review-reminder-popover"
    @show="loadReminder"
  >
    <div class="reminder-panel">
      <div class="reminder-panel-header">
        <div><strong>复习提醒</strong><span>未读 {{ unreadCount }} 条</span></div>
        <el-button type="text" :loading="loading" @click="loadReminder">刷新</el-button>
      </div>

      <div v-loading="loading" class="reminder-panel-body">
        <button
          v-for="item in reminderList"
          :key="item.id"
          :class="['reminder-row', { unread: item.isRead === 0 }]"
          type="button"
          @click="openReminder(item)"
        >
          <span class="row-icon"><i class="el-icon-bell" /></span>
          <span class="row-content">
            <span class="row-title">{{ item.title }}<i v-if="item.isRead === 0" /></span>
            <span class="row-text">{{ item.content }}</span>
            <span class="row-time">{{ item.sentTime }}</span>
          </span>
        </button>
        <el-empty v-if="!loading && !reminderList.length" description="暂无复习提醒" :image-size="60" />
      </div>

      <div class="reminder-panel-footer">
        <el-button type="text" @click="viewAll">查看全部提醒</el-button>
        <el-button type="primary" size="mini" @click="startReview">开始今日复习</el-button>
      </div>
    </div>

    <button slot="reference" class="reminder-trigger" type="button" aria-label="复习提醒">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <i class="el-icon-bell" />
      </el-badge>
    </button>
  </el-popover>
</template>

<script>
import { readReviewReminder, reviewReminderPageList } from '@/api/system/review'

const REFRESH_INTERVAL = 60 * 1000

export default {
  name: 'ReviewReminderPopover',
  data() {
    return {
      visible: false,
      loading: false,
      unreadCount: 0,
      reminderList: [],
      refreshTimer: null
    }
  },
  created() {
    this.loadReminder()
    this.refreshTimer = window.setInterval(this.loadReminder, REFRESH_INTERVAL)
  },
  beforeDestroy() {
    if (this.refreshTimer) window.clearInterval(this.refreshTimer)
  },
  methods: {
    // 顶部入口只加载最近5条，完整列表由复习历史页面负责。
    async loadReminder() {
      this.loading = true
      try {
        const data = await reviewReminderPageList({ current: 1, pageSize: 5 })
        this.unreadCount = Number(data && data.unreadCount || 0)
        this.reminderList = data && data.pageResult && data.pageResult.list || []
      } finally {
        this.loading = false
      }
    },
    async openReminder(item) {
      if (item.isRead === 0) {
        await readReviewReminder({ id: item.id })
        item.isRead = 1
        this.unreadCount = Math.max(0, this.unreadCount - 1)
      }
      this.viewAll()
    },
    viewAll() {
      this.visible = false
      this.navigate('/system/review/history', { tab: 'reminder' })
    },
    startReview() {
      this.visible = false
      this.navigate('/system/review/today')
    },
    navigate(path, query) {
      if (this.$route.path === path && JSON.stringify(this.$route.query) === JSON.stringify(query || {})) return
      this.$router.push({ path, query: query || {} })
    }
  }
}
</script>

<style lang="scss" scoped>
.reminder-trigger { display: flex; align-items: center; justify-content: center; width: 38px; height: 38px; padding: 0; color: #596579; border: 1px solid #e3e7ee; border-radius: 9px; background: #fff; cursor: pointer; }
.reminder-trigger:hover { color: #5264ee; border-color: #cbd2fb; background: #f7f8ff; }
.reminder-trigger i { font-size: 18px; }
.reminder-trigger ::v-deep .el-badge__content { top: 2px; right: 5px; }
.reminder-panel-header { display: flex; align-items: center; justify-content: space-between; padding: 4px 4px 11px; border-bottom: 1px solid #eef0f5; }
.reminder-panel-header strong { font-size: 15px; }
.reminder-panel-header span { margin-left: 8px; color: #98a1b3; font-size: 11px; }
.reminder-panel-body { min-height: 100px; max-height: 360px; overflow-y: auto; }
.reminder-row { display: flex; width: 100%; gap: 10px; padding: 13px 5px; text-align: left; border: 0; border-bottom: 1px solid #f0f2f6; background: #fff; cursor: pointer; }
.reminder-row:hover, .reminder-row.unread { background: #f7f8ff; }
.row-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; flex: 0 0 34px; color: #596bf2; border-radius: 50%; background: #eef0ff; }
.row-content, .row-title, .row-text, .row-time { display: block; }
.row-content { min-width: 0; flex: 1; }
.row-title { color: #30394c; font-size: 13px; font-weight: 600; }
.row-title i { display: inline-block; width: 6px; height: 6px; margin-left: 6px; border-radius: 50%; background: #596bf2; }
.row-text { margin: 5px 0; overflow: hidden; color: #687286; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.row-time { color: #a0a8b7; font-size: 10px; }
.reminder-panel-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 4px 2px; }
</style>
