<template>
  <div class="history-page">
    <section class="history-hero">
      <div>
        <div class="hero-eyebrow">REVIEW JOURNEY</div>
        <h1>复习历史</h1>
        <p>每一次主动回忆都留下轨迹，回看反馈变化，找到最适合自己的复习节奏。</p>
      </div>
      <div class="hero-reminder" @click="activeTab = 'reminder'">
        <i class="el-icon-bell" />
        <span>站内提醒</span>
        <el-badge :value="reminder.unreadCount" :hidden="!reminder.unreadCount" />
      </div>
    </section>

    <el-tabs v-model="activeTab" class="history-tabs" @tab-click="handleTabClick">
      <el-tab-pane label="复习记录" name="history">
        <el-row :gutter="16" class="metric-row">
          <el-col v-for="item in metrics" :key="item.label" :xs="12" :sm="12" :md="6">
            <el-card shadow="never" class="metric-card">
              <div :class="['metric-icon', item.type]"><i :class="item.icon" /></div>
              <div><span>{{ item.label }}</span><strong>{{ item.value }}<small>{{ item.unit }}</small></strong></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="content-card">
          <div class="filter-bar">
            <el-input v-model.trim="historyQuery.keyWord" clearable prefix-icon="el-icon-search" placeholder="搜索题目" @keyup.enter.native="searchHistory" />
            <el-select v-model="historyQuery.feedback" clearable placeholder="全部反馈" @change="searchHistory">
              <el-option v-for="item in feedbackOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="historyQuery.subject" clearable placeholder="全部科目" @change="searchHistory">
              <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
            </el-select>
            <el-date-picker
              v-model="historyDateRange"
              type="daterange"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="searchHistory"
            />
            <el-button type="primary" icon="el-icon-search" @click="searchHistory">查询</el-button>
          </div>

          <el-table v-loading="historyLoading" :data="history.pageResult.list" class="history-table">
            <el-table-column label="科目" width="100">
              <template slot-scope="scope">{{ scope.row.subjectName || '历史未记录' }}</template>
            </el-table-column>
            <el-table-column label="复习题目" min-width="260">
              <template slot-scope="scope">
                <div class="question-title">{{ scope.row.questionTitle || '未命名题目' }}</div>
                <span v-if="scope.row.isOverdue === 1" class="overdue-mark">逾期完成</span>
              </template>
            </el-table-column>
            <el-table-column label="反馈" width="100">
              <template slot-scope="scope">
                <span :class="['feedback-tag', `feedback-${scope.row.feedback}`]">{{ scope.row.feedbackText }}</span>
              </template>
            </el-table-column>
            <el-table-column label="作答结果" width="110">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.isCorrect === 1" size="mini" type="success">正确</el-tag>
                <el-tag v-else-if="scope.row.isCorrect === 0" size="mini" type="danger">错误</el-tag>
                <span v-else>-</span>
                <small v-if="scope.row.isCorrect !== null && scope.row.isCorrect !== undefined" class="judge-type">
                  {{ scope.row.answerJudgeType === 1 ? '自动' : '自评' }}
                </small>
              </template>
            </el-table-column>
            <el-table-column prop="studentAnswer" label="我的答案" min-width="180" show-overflow-tooltip />
            <el-table-column label="阶段变化" width="110">
              <template slot-scope="scope">S{{ scope.row.stageBefore }} → S{{ scope.row.stageAfter }}</template>
            </el-table-column>
            <el-table-column label="掌握度" width="125">
              <template slot-scope="scope">
                <span>{{ emptyValue(scope.row.masteryScoreBefore) }} → {{ emptyValue(scope.row.masteryScoreAfter) }}</span>
                <small :class="['mastery-delta', Number(scope.row.masteryScoreDelta) >= 0 ? 'up' : 'down']">
                  {{ formatDelta(scope.row.masteryScoreDelta) }}
                </small>
              </template>
            </el-table-column>
            <el-table-column label="连续表现" width="115">
              <template slot-scope="scope">
                <span>对 {{ scope.row.correctStreakAfter || 0 }}</span>
                <span class="streak-split">错 {{ scope.row.wrongStreakAfter || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="回忆用时" width="105">
              <template slot-scope="scope">{{ formatDuration(scope.row.answerDurationSeconds) }}</template>
            </el-table-column>
            <el-table-column label="复习时间" prop="reviewTime" width="168" />
            <el-table-column label="下次复习" prop="nextReviewTime" width="168" />
          </el-table>
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page="historyQuery.current"
            :page-size="historyQuery.pageSize"
            :total="history.pageResult.total"
            @current-change="changeHistoryPage"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane name="reminder">
        <span slot="label">站内提醒 <el-badge :is-dot="reminder.unreadCount > 0" /></span>
        <el-card shadow="never" class="content-card reminder-list-card">
          <div slot="header" class="card-header">
            <div><strong>复习提醒</strong><span>未读 {{ reminder.unreadCount }} 条</span></div>
            <el-button type="text" icon="el-icon-refresh" @click="loadReminder">刷新</el-button>
          </div>
          <div v-loading="reminderLoading">
            <div
              v-for="item in reminder.pageResult.list"
              :key="item.id"
              :class="['reminder-item', { unread: item.isRead === 0 }]"
              @click="markReminderRead(item)"
            >
              <div class="reminder-icon"><i class="el-icon-bell" /></div>
              <div class="reminder-main">
                <div class="reminder-title"><strong>{{ item.title }}</strong><i v-if="item.isRead === 0" /></div>
                <p>{{ item.content }}</p>
                <div class="reminder-meta">
                  <span><i class="el-icon-notebook-2" /> 到期 {{ item.dueCount }} 道</span>
                  <span v-if="item.subjectSummary"><i class="el-icon-collection-tag" /> {{ item.subjectSummary }}</span>
                  <span v-if="item.overdueCount"><i class="el-icon-warning-outline" /> 逾期 {{ item.overdueCount }} 道</span>
                  <span>{{ item.sentTime }}</span>
                </div>
              </div>
              <el-button type="text" @click.stop="$router.push('/system/review/today')">开始复习</el-button>
            </div>
            <el-empty v-if="!reminderLoading && !reminder.pageResult.list.length" description="暂无复习提醒" />
          </div>
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page="reminderQuery.current"
            :page-size="reminderQuery.pageSize"
            :total="reminder.pageResult.total"
            @current-change="changeReminderPage"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { readReviewReminder, reviewHistoryPageList, reviewReminderPageList } from '@/api/system/review'
import { dictDataOptions } from '@/api/system/dict'

const emptyPage = () => ({ page: 0, total: 0, pageSize: 10, current: 1, list: [] })

export default {
  name: 'ReviewHistory',
  data() {
    return {
      activeTab: this.$route.query.tab === 'reminder' ? 'reminder' : 'history',
      historyLoading: false,
      reminderLoading: false,
      historyDateRange: [],
      historyQuery: { current: 1, pageSize: 10, keyWord: '', feedback: null, subject: '' },
      subjectOptions: [],
      reminderQuery: { current: 1, pageSize: 10 },
      history: {
        totalReviewCount: 0,
        weekReviewCount: 0,
        correctRate: 0,
        positiveFeedbackRate: 0,
        averageDurationSeconds: 0,
        pageResult: emptyPage()
      },
      reminder: { unreadCount: 0, pageResult: emptyPage() },
      feedbackOptions: [
        { value: 0, label: '忘记' }, { value: 1, label: '困难' },
        { value: 2, label: '掌握' }, { value: 3, label: '很简单' }
      ]
    }
  },
  computed: {
    metrics() {
      return [
        { label: '累计复习', value: this.history.totalReviewCount, unit: '次', icon: 'el-icon-finished', type: 'primary' },
        { label: '本周复习', value: this.history.weekReviewCount, unit: '次', icon: 'el-icon-date', type: 'success' },
        { label: '真实正确率', value: this.history.correctRate, unit: '%', icon: 'el-icon-circle-check', type: 'success' },
        { label: '正向反馈率', value: this.history.positiveFeedbackRate, unit: '%', icon: 'el-icon-data-line', type: 'purple' },
        { label: '平均回忆用时', value: this.history.averageDurationSeconds, unit: '秒', icon: 'el-icon-timer', type: 'warning' }
      ]
    }
  },
  watch: {
    '$route.query.tab'(tab) {
      if (tab === 'reminder') {
        this.activeTab = 'reminder'
        this.loadReminder()
      }
    }
  },
  created() {
    this.loadSubjectOptions()
    this.loadHistory()
    this.loadReminder()
  },
  methods: {
    async loadSubjectOptions() {
      this.subjectOptions = await dictDataOptions({ dictType: 'subject' }).catch(() => [])
    },
    async loadHistory() {
      this.historyLoading = true
      try {
        const params = Object.assign({}, this.historyQuery)
        if (this.historyDateRange && this.historyDateRange.length === 2) {
          params.startDate = this.historyDateRange[0]
          params.endDate = this.historyDateRange[1]
        }
        const data = await reviewHistoryPageList(params)
        this.history = Object.assign({}, this.history, data || {})
        this.history.pageResult = Object.assign(emptyPage(), this.history.pageResult || {})
      } finally {
        this.historyLoading = false
      }
    },
    async loadReminder() {
      this.reminderLoading = true
      try {
        const data = await reviewReminderPageList(this.reminderQuery)
        this.reminder = Object.assign({ unreadCount: 0, pageResult: emptyPage() }, data || {})
        this.reminder.pageResult = Object.assign(emptyPage(), this.reminder.pageResult || {})
      } finally {
        this.reminderLoading = false
      }
    },
    searchHistory() {
      this.historyQuery.current = 1
      this.loadHistory()
    },
    changeHistoryPage(page) {
      this.historyQuery.current = page
      this.loadHistory()
    },
    changeReminderPage(page) {
      this.reminderQuery.current = page
      this.loadReminder()
    },
    handleTabClick(tab) {
      if (tab.name === 'reminder') this.loadReminder()
    },
    async markReminderRead(item) {
      if (item.isRead === 1) return
      await readReviewReminder({ id: item.id })
      item.isRead = 1
      this.reminder.unreadCount = Math.max(0, this.reminder.unreadCount - 1)
    },
    formatDuration(seconds) {
      const value = Number(seconds || 0)
      return value < 60 ? `${value}秒` : `${Math.floor(value / 60)}分${value % 60}秒`
    },
    emptyValue(value) {
      return value === null || value === undefined ? '-' : value
    },
    formatDelta(value) {
      if (value === null || value === undefined) return ''
      const number = Number(value)
      return number > 0 ? `+${number}` : `${number}`
    }
  }
}
</script>

<style lang="scss" scoped>
.history-page { padding: 4px; color: #27314a; }
.history-hero { display: flex; align-items: center; justify-content: space-between; min-height: 142px; padding: 28px 38px; margin-bottom: 12px; color: #fff; border-radius: 14px; background: linear-gradient(125deg, #445bd5, #6878eb 62%, #806de2); box-shadow: 0 12px 28px rgba(68, 91, 213, .2); }
.hero-eyebrow { margin-bottom: 7px; font-size: 12px; letter-spacing: 2px; opacity: .76; }
.history-hero h1 { margin: 0 0 10px; font-size: 28px; }
.history-hero p { margin: 0; opacity: .86; }
.hero-reminder { display: flex; align-items: center; gap: 9px; padding: 13px 18px; border: 1px solid rgba(255,255,255,.28); border-radius: 10px; background: rgba(255,255,255,.12); cursor: pointer; }
.history-tabs ::v-deep .el-tabs__header { margin-bottom: 16px; padding: 0 4px; }
.metric-row { margin-bottom: 2px; }
.metric-card { margin-bottom: 16px; border: 0; border-radius: 12px; }
.metric-card ::v-deep .el-card__body { display: flex; align-items: center; gap: 13px; padding: 19px; }
.metric-icon { display: flex; align-items: center; justify-content: center; width: 44px; height: 44px; border-radius: 12px; font-size: 20px; }
.metric-icon.primary { color: #5264ee; background: #eef0ff; }
.metric-icon.success { color: #31a77c; background: #e9f8f2; }
.metric-icon.warning { color: #e79a39; background: #fff5e7; }
.metric-icon.purple { color: #8b62d7; background: #f3edff; }
.metric-card span { display: block; margin-bottom: 4px; color: #8e97a9; font-size: 12px; }
.metric-card strong { font-size: 23px; }
.metric-card small { margin-left: 3px; color: #8e97a9; font-size: 12px; font-weight: 400; }
.content-card { border: 0; border-radius: 12px; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 18px; }
.filter-bar .el-input { width: 230px; }
.filter-bar .el-select { width: 130px; }
.history-table { margin-bottom: 18px; }
.question-title { margin-bottom: 5px; font-weight: 500; }
.overdue-mark { color: #e99a38; font-size: 11px; }
.feedback-tag { display: inline-block; padding: 4px 9px; border-radius: 12px; font-size: 12px; }
.feedback-0 { color: #e05e67; background: #fff0f1; }
.feedback-1 { color: #d88a2e; background: #fff5e7; }
.feedback-2 { color: #298e6b; background: #e9f8f2; }
.feedback-3 { color: #5365dc; background: #eef0ff; }
.mastery-delta { display: block; margin-top: 4px; font-size: 11px; }
.mastery-delta.up { color: #2f9b74; }
.mastery-delta.down { color: #df625a; }
.streak-split { margin-left: 8px; color: #8e97a9; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-header strong { font-size: 16px; }
.card-header span { margin-left: 10px; color: #98a1b3; font-size: 12px; }
.reminder-list-card ::v-deep .el-card__body { padding: 4px 22px 20px; }
.reminder-item { display: flex; align-items: center; gap: 14px; padding: 19px 8px; border-bottom: 1px solid #eef0f5; cursor: pointer; }
.reminder-item.unread { background: linear-gradient(90deg, #f7f8ff, transparent); }
.reminder-icon { display: flex; align-items: center; justify-content: center; width: 44px; height: 44px; flex: 0 0 44px; color: #596bf2; border-radius: 50%; background: #eef0ff; }
.reminder-main { flex: 1; min-width: 0; }
.reminder-title { display: flex; align-items: center; gap: 8px; }
.reminder-title i { width: 7px; height: 7px; border-radius: 50%; background: #596bf2; }
.reminder-main p { margin: 7px 0; color: #697388; font-size: 13px; }
.reminder-meta { display: flex; gap: 18px; color: #9aa3b5; font-size: 11px; }
.reminder-meta i { margin-right: 3px; }
.el-pagination { margin-top: 18px; text-align: right; }
@media (max-width: 768px) {
  .history-hero { align-items: flex-start; padding: 24px; flex-direction: column; gap: 20px; }
  .history-hero h1 { font-size: 24px; }
  .filter-bar .el-input, .filter-bar .el-select, .filter-bar .el-date-editor { width: 100%; }
  .reminder-item { align-items: flex-start; }
  .reminder-meta { flex-direction: column; gap: 4px; }
}
</style>
