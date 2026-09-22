<template>
  <div v-loading="loading" class="study-dashboard">
    <section class="welcome-card">
      <div>
        <span class="eyebrow">{{ todayText }} · 连续复习 {{ home.continuousReviewDays }} 天</span>
        <h1>你好，{{ username }}</h1>
        <p v-if="home.remainingCount">今天还有 {{ home.remainingCount }} 道错题待复习，预计 {{ home.estimatedMinutes }} 分钟。</p>
        <p v-else>今天的复习任务已经完成，保持这个节奏。</p>
        <div class="welcome-actions">
          <el-button type="primary" :disabled="!home.remainingCount" @click="goReview">开始今日复习</el-button>
          <el-button plain @click="$router.push('/system/wrongquestion')">查看错题本</el-button>
        </div>
      </div>
      <div v-if="home.reviewQuestionCount" class="mastery-ring">
        <el-progress type="dashboard" :percentage="masteryRate" :width="142" :stroke-width="11" color="#41b889" />
        <span>总体掌握率</span>
      </div>
      <div v-else class="mastery-ring"><strong>样本不足</strong><span>录入错题后显示掌握率</span></div>
    </section>

    <el-row :gutter="16" class="metric-row">
      <el-col v-for="item in metrics" :key="item.label" :span="6">
        <div class="metric-card">
          <div :class="['metric-icon', item.type]"><i :class="item.icon" /></div>
          <div><span>{{ item.label }}</span><strong>{{ item.value }}<small>{{ item.unit }}</small></strong></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="15">
        <el-card shadow="never" class="dashboard-card">
          <div slot="header" class="card-header"><div><strong>未来 7 天复习安排</strong><small>逾期任务自动计入今天</small></div><el-button type="text" @click="$router.push('/system/review')">查看复习中心</el-button></div>
          <div v-if="home.scheduleList.length" class="schedule-grid">
            <div v-for="(item, index) in home.scheduleList" :key="item.reviewDate" :class="['schedule-day', { today: index === 0 }]">
              <span>{{ index === 0 ? '今天' : weekName(item.reviewDate) }}</span>
              <strong>{{ item.dueCount }}</strong>
              <small>待复习</small>
            </div>
          </div>
          <el-empty v-else description="完成错题订正后会生成复习排期" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="never" class="dashboard-card quick-card">
          <div slot="header" class="card-header"><div><strong>快捷入口</strong><small>从整理到掌握</small></div></div>
          <button class="quick-action" @click="$router.push('/system/wrongquestion')"><i class="el-icon-upload2" /><span><b>导入错题</b><small>图片 OCR 或手动录入</small></span><i class="el-icon-arrow-right" /></button>
          <button class="quick-action" @click="$router.push('/system/review/today')"><i class="el-icon-refresh" /><span><b>遮答案重练</b><small>完成今天的间隔复习</small></span><i class="el-icon-arrow-right" /></button>
          <button class="quick-action" @click="$router.push('/system/review/history')"><i class="el-icon-time" /><span><b>复习记录</b><small>查看每次反馈与掌握变化</small></span><i class="el-icon-arrow-right" /></button>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="dashboard-card point-card">
      <div slot="header" class="card-header"><div><strong>需要优先巩固的知识点</strong><small>按当前掌握率由低到高</small></div><el-button type="text" @click="$router.push('/system/review')">查看完整学情</el-button></div>
      <div v-if="weakPoints.length" class="point-grid">
        <div v-for="item in weakPoints" :key="item.learningPoint" class="point-item">
          <div><b>{{ item.learningPoint || '未标注知识点' }}</b><span>{{ item.masteredCount }}/{{ item.questionCount }} 道已掌握</span></div>
          <el-progress :percentage="Number(item.masteryRate || 0)" :stroke-width="8" :color="pointColor(item.masteryRate)" />
        </div>
      </div>
      <el-empty v-else description="暂无知识点数据，先录入并订正错题吧" :image-size="80" />
    </el-card>
  </div>
</template>

<script>
import { initializeReviewHome } from '@/api/system/review'

const emptyHome = () => ({
  remainingCount: 0, completedCount: 0, overdueCount: 0, estimatedMinutes: 0,
  continuousReviewDays: 0, reviewQuestionCount: 0, masteredCount: 0, masteryRate: 0,
  scheduleList: [], learningPointList: []
})

export default {
  name: 'DashboardPage',
  data() {
    return { loading: false, home: emptyHome() }
  },
  computed: {
    username() { return localStorage.getItem('study_username') || '同学' },
    todayText() { return new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date()) },
    masteryRate() { return Math.min(100, Math.max(0, Number(this.home.masteryRate || 0))) },
    metrics() {
      return [
        { label: '今日待复习', value: this.home.remainingCount, unit: '道', icon: 'el-icon-date', type: 'purple' },
        { label: '今日已完成', value: this.home.completedCount, unit: '道', icon: 'el-icon-circle-check', type: 'green' },
        { label: '已经逾期', value: this.home.overdueCount, unit: '道', icon: 'el-icon-warning-outline', type: 'orange' },
        { label: '进入复习计划', value: this.home.reviewQuestionCount, unit: '道', icon: 'el-icon-notebook-2', type: 'blue' }
      ]
    },
    weakPoints() { return [...(this.home.learningPointList || [])].sort((a, b) => Number(a.masteryRate) - Number(b.masteryRate)).slice(0, 5) }
  },
  created() { this.loadHome() },
  methods: {
    // 首页只依赖复习聚合接口；接口空字段与新用户场景统一回落到默认结构。
    async loadHome() {
      this.loading = true
      try { this.home = Object.assign(emptyHome(), await initializeReviewHome() || {}) } finally { this.loading = false }
    },
    // 复习入口直接进入今日题单，避免学生先经过统计页再开始任务。
    goReview() { this.$router.push('/system/review/today') },
    weekName(date) { return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(`${date}T00:00:00`).getDay()] },
    // 用三档颜色提示薄弱程度，颜色只作辅助，页面仍保留明确百分比文本。
    pointColor(rate) { return Number(rate) < 50 ? '#ef8b55' : Number(rate) < 70 ? '#e9b44c' : '#41b889' }
  }
}
</script>

<style lang="scss" scoped>
.study-dashboard { color: #27314a; }
.welcome-card { display: flex; justify-content: space-between; align-items: center; min-height: 210px; padding: 32px 42px; color: #fff; border-radius: 18px; background: linear-gradient(120deg, #5159d9, #6d75ec 62%, #8973e6); box-shadow: 0 14px 32px rgba(81, 89, 217, .22); }
.eyebrow { color: #e2e4ff; font-size: 13px; letter-spacing: .5px; }
.welcome-card h1 { margin: 10px 0 4px; font-size: 30px; }
.welcome-card p { margin: 0 0 22px; color: #eff0ff; font-size: 15px; }
.welcome-actions .el-button--primary { color: #545bd9; background: #fff; border-color: #fff; }
.welcome-actions .el-button.is-plain { color: #fff; background: transparent; border-color: rgba(255,255,255,.55); }
.mastery-ring { display: flex; flex-direction: column; align-items: center; padding-right: 30px; }
.mastery-ring ::v-deep .el-progress__text { color: #fff; font-weight: 700; }
.mastery-ring span { margin-top: -12px; color: #e8eaff; }
.metric-row { margin: 16px 0; }
.metric-card { display: flex; align-items: center; gap: 15px; min-height: 100px; padding: 20px; background: #fff; border: 1px solid #e8ebf3; border-radius: 14px; }
.metric-card span { display: block; color: #8a93a8; font-size: 13px; }
.metric-card strong { display: block; margin-top: 4px; font-size: 25px; }
.metric-card small { margin-left: 4px; color: #8a93a8; font-size: 12px; font-weight: 400; }
.metric-icon { display: grid; place-items: center; width: 46px; height: 46px; border-radius: 13px; font-size: 21px; }
.metric-icon.purple { color: #5e65dc; background: #eff0ff; }.metric-icon.green { color: #2f9a72; background: #e9f8f2; }.metric-icon.orange { color: #d58138; background: #fff2e5; }.metric-icon.blue { color: #367fc1; background: #eaf4ff; }
.dashboard-card { margin-bottom: 16px; border: 1px solid #e8ebf3; border-radius: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }.card-header strong { display: block; font-size: 16px; }.card-header small { display: block; margin-top: 3px; color: #929aab; }
.schedule-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; padding: 8px 0; }
.schedule-day { display: flex; flex-direction: column; align-items: center; padding: 14px 6px; border-radius: 11px; background: #f6f7fb; }.schedule-day span,.schedule-day small { color: #8c95a8; }.schedule-day strong { margin: 7px 0 1px; font-size: 23px; }.schedule-day.today { color: #fff; background: #5d64de; }.schedule-day.today span,.schedule-day.today small { color: #e5e7ff; }
.quick-card ::v-deep .el-card__body { padding: 6px 20px 13px; }.quick-action { display: grid; grid-template-columns: 34px 1fr 20px; align-items: center; gap: 9px; width: 100%; padding: 12px 0; color: #38425a; text-align: left; background: none; border: 0; border-bottom: 1px solid #eef0f5; cursor: pointer; }.quick-action:last-child { border: 0; }.quick-action > i:first-child { color: #5d64de; font-size: 20px; }.quick-action b,.quick-action small { display: block; }.quick-action small { margin-top: 2px; color: #969daf; }.quick-action:hover b { color: #5d64de; }
.point-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px 26px; }.point-item > div { display: flex; justify-content: space-between; margin-bottom: 8px; }.point-item span { color: #929aab; font-size: 12px; }
@media (max-width: 1200px) { .schedule-grid { grid-template-columns: repeat(4, 1fr); }.point-grid { grid-template-columns: 1fr; } }
</style>
