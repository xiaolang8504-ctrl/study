<template>
  <div v-loading="loading" class="review-home">
    <div class="subject-filter">
      <span>查看科目</span>
      <el-radio-group v-model="selectedSubject" size="small" @change="handleSubjectChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="item in home.subjectSettings" :key="item.subject" :label="item.subject">
          {{ item.subjectName }}
        </el-radio-button>
      </el-radio-group>
    </div>
    <section class="welcome-panel">
      <div>
        <div class="welcome-eyebrow">INTELLIGENT REVIEW</div>
        <h1>{{ home.planName || '我的错题复习' }}</h1>
        <p>
          今天安排 {{ home.todayTaskCount }} 道，已完成 {{ home.completedCount }} 道。
          按反馈持续复习，记忆会一点点变牢。
        </p>
      </div>
      <div class="welcome-actions">
        <div class="streak"><strong>{{ home.continuousReviewDays }}</strong><span>连续复习天数</span></div>
        <el-button plain @click="openLearningReport">学情报告</el-button>
        <el-button type="primary" :disabled="home.remainingCount === 0" @click="startReview">
          开始今日复习
        </el-button>
      </div>
    </section>

    <el-row :gutter="16" class="metrics-row">
      <el-col v-for="metric in metrics" :key="metric.label" :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="metric-card">
          <div :class="['metric-icon', metric.type]"><i :class="metric.icon" /></div>
          <div>
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-value">{{ metric.value }}<small>{{ metric.unit }}</small></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="content-card schedule-card">
          <div slot="header" class="card-header">
            <div>
              <strong>未来 7 天复习安排</strong>
              <span>逾期任务已计入今天</span>
            </div>
            <el-button type="text" icon="el-icon-refresh" @click="loadHome">刷新</el-button>
          </div>
          <div class="schedule-list">
            <div v-for="(item, index) in home.scheduleList" :key="item.reviewDate" class="schedule-item">
              <div :class="['schedule-date', { today: index === 0 }]">
                <span>{{ index === 0 ? '今天' : weekName(item.reviewDate) }}</span>
                <strong>{{ formatDate(item.reviewDate) }}</strong>
              </div>
              <div class="schedule-bar-wrap">
                <div class="schedule-number">
                  <span>待复习 {{ item.dueCount }}</span>
                  <span>已完成 {{ item.completedCount }}</span>
                </div>
                <el-progress
                  :percentage="schedulePercent(item)"
                  :show-text="false"
                  :stroke-width="8"
                  color="#5b6cf9"
                />
              </div>
            </div>
            <el-empty v-if="!home.scheduleList.length" description="暂无复习排期" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="content-card mastery-card">
          <div slot="header" class="card-header"><strong>总体掌握情况</strong></div>
          <div class="mastery-content">
            <el-progress
              type="dashboard"
              :percentage="masteryPercentage"
              :width="154"
              :stroke-width="12"
              color="#43b88c"
            />
            <p>平均掌握度 {{ home.averageMasteryScore || 0 }} 分</p>
            <span>已掌握 {{ home.masteredCount }} / {{ home.reviewQuestionCount }} 道，低分错题会优先进入今日复习</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="content-card point-card">
          <div slot="header" class="card-header">
            <div><strong>知识点掌握 TOP5</strong><span>优先处理最薄弱与最典型的错题</span></div>
            <el-button type="primary" plain size="small" icon="el-icon-star-off" @click="generateTypical">
              复习典型错题
            </el-button>
          </div>
          <div v-if="home.learningPointList.length" class="point-list">
            <div v-for="item in home.learningPointList" :key="item.learningPoint" class="point-item">
              <div class="point-title">
                <span>{{ item.learningPoint }}</span>
                <span>{{ item.masteredCount }}/{{ item.questionCount }} 道 · {{ item.averageMasteryScore || 0 }}分
                  <el-button type="text" size="mini" @click="generatePractice(item)">生成专项练习</el-button>
                </span>
              </div>
              <el-progress :percentage="Number(item.averageMasteryScore || item.masteryRate)" :show-text="false" :stroke-width="7" color="#43b88c" />
            </div>
          </div>
          <el-empty v-else description="订正错题后将生成知识点统计" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="content-card plan-card">
          <div slot="header" class="card-header">
            <strong>当前计划</strong>
            <el-button type="text" @click="$router.push('/system/review/setting')">设置</el-button>
          </div>
          <div class="plan-line"><span>每日复习上限</span><strong>{{ home.dailyLimit }} 道</strong></div>
          <div class="plan-line"><span>站内提醒</span><strong>{{ home.reminderEnabled === 1 ? '已开启' : '未开启' }}</strong></div>
          <div class="plan-line"><span>提醒时间</span><strong>{{ home.reminderTime || '--:--' }}</strong></div>
          <div class="plan-tip"><i class="el-icon-bell" /> 到期题目会自动进入今日任务。</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card adaptive-card">
      <div slot="header" class="card-header">
        <div><strong>下一步学习内容</strong><span>{{ adaptive.strategyDescription }}</span></div>
        <el-button type="text" icon="el-icon-refresh" @click="loadAdaptiveContent">重新计算</el-button>
      </div>
      <div v-loading="adaptiveLoading">
        <div v-if="adaptive.contentList.length" class="adaptive-grid">
          <article v-for="item in adaptive.contentList" :key="item.wrongQuestionId" class="adaptive-item">
            <div :class="['adaptive-action', item.actionType.toLowerCase()]">{{ item.actionText }}</div>
            <div class="adaptive-main">
              <div><el-tag size="mini">{{ item.subjectName || '未分类' }}</el-tag><span>{{ item.learningPoint || '未标注知识点' }}</span></div>
              <h3>{{ item.questionTitle || '未命名错题' }}</h3>
              <p>{{ item.recommendationReason }}</p>
            </div>
            <el-button type="text" :disabled="!item.learningPoint" @click="generatePractice({ learningPoint: item.learningPoint })">练这一类</el-button>
          </article>
        </div>
        <el-empty v-else-if="!adaptiveLoading" description="完成几次真实作答后，系统会动态推荐下一步内容" :image-size="80" />
      </div>
    </el-card>

    <el-dialog
      :title="practice.title || `${practice.learningPoint || ''}专项练习`"
      :visible.sync="practiceVisible"
      width="820px"
      append-to-body
    >
      <div v-loading="practiceLoading" class="practice-dialog">
        <el-alert
          v-if="practice.recommendation"
          :title="practice.recommendation"
          type="success"
          :closable="false"
          show-icon
        />
        <article v-for="(question, index) in practice.questionList" :key="question.wrongQuestionId" class="practice-question">
          <div class="practice-order">{{ index + 1 }}</div>
          <div class="practice-main">
            <div class="practice-tags">
              <el-tag size="mini">{{ question.subjectName || '未设置科目' }}</el-tag>
              <el-tag size="mini" type="info">{{ question.questionTypeName || '未设置题型' }}</el-tag>
              <el-tag v-if="question.level" size="mini" type="warning">难度 {{ question.level }}</el-tag>
            </div>
            <div v-if="question.representativeReason" class="representative-reason">
              <i class="el-icon-star-on" /> 入选原因：{{ question.representativeReason }}
            </div>
            <h3>{{ question.questionTitle || '未命名题目' }}</h3>
            <div class="practice-content">{{ question.questionContent || '请根据原错题图片完成作答。' }}</div>
            <div class="practice-answer-space">答题区</div>
          </div>
        </article>
        <el-empty v-if="!practiceLoading && !practice.questionList.length" description="该知识点暂无已订正错题" />
      </div>
      <div slot="footer">
        <span class="practice-count">共 {{ practice.questionCount }} 道</span>
        <el-button @click="practiceVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!practice.questionList.length" @click="printPractice">打印练习</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { adaptiveLearningContent, generateTypicalPractice, generateWeakPointPractice, initializeReviewHome } from '@/api/system/review'

const emptyHome = () => ({
  planName: '',
  dailyLimit: 20,
  reminderEnabled: 1,
  reminderTime: '19:30',
  todayTaskCount: 0,
  completedCount: 0,
  remainingCount: 0,
  overdueCount: 0,
  estimatedMinutes: 0,
  continuousReviewDays: 0,
  reviewQuestionCount: 0,
  masteredCount: 0,
  masteryRate: 0,
  averageMasteryScore: 0,
  scheduleList: [],
  learningPointList: [],
  subjectSettings: [],
  selectedSubject: ''
})

export default {
  name: 'IntelligentReviewPage',
  data() {
    return {
      loading: false,
      selectedSubject: this.$route.query.subject || '',
      home: emptyHome(),
      practiceVisible: false,
      practiceLoading: false,
      practice: { title: '', learningPoint: '', recommendation: '', questionCount: 0, questionList: [] },
      adaptiveLoading: false,
      adaptive: { strategyDescription: '', contentList: [] }
    }
  },
  computed: {
    masteryPercentage() {
      return Math.min(100, Math.max(0, Number(this.home.averageMasteryScore || this.home.masteryRate || 0)))
    },
    metrics() {
      return [
        { label: '今日待复习', value: this.home.remainingCount, unit: '道', icon: 'el-icon-time', type: 'primary' },
        { label: '今日已完成', value: this.home.completedCount, unit: '道', icon: 'el-icon-circle-check', type: 'success' },
        { label: '已经逾期', value: this.home.overdueCount, unit: '道', icon: 'el-icon-warning-outline', type: 'warning' },
        { label: '预计用时', value: this.home.estimatedMinutes, unit: '分钟', icon: 'el-icon-alarm-clock', type: 'purple' }
      ]
    }
  },
  created() {
    this.loadHome().then(() => this.openQueryPractice())
    this.loadAdaptiveContent()
  },
  watch: {
    '$route.query.subject'(subject) {
      const nextSubject = subject || ''
      if (nextSubject !== this.selectedSubject) {
        this.selectedSubject = nextSubject
        this.loadHome()
        this.loadAdaptiveContent()
      }
    },
    '$route.query.learningPoint'(learningPoint) {
      if (learningPoint) {
        this.generatePractice({ learningPoint })
      }
    }
  },
  methods: {
    async loadHome() {
      this.loading = true
      try {
        const data = await initializeReviewHome({ subject: this.selectedSubject || undefined })
        this.home = Object.assign(emptyHome(), data || {})
        this.selectedSubject = this.home.selectedSubject || ''
      } finally {
        this.loading = false
      }
    },
    startReview() {
      this.$router.push({ path: '/system/review/today', query: this.selectedSubject
        ? { subject: this.selectedSubject } : {}})
    },
    openLearningReport() {
      this.$router.push({ path: '/system/review/report', query: this.selectedSubject
        ? { subject: this.selectedSubject } : {} })
    },
    handleSubjectChange(subject) {
      this.$router.replace({ query: subject ? { subject } : {}})
      this.loadHome()
      this.loadAdaptiveContent()
    },
    formatDate(date) {
      return date ? date.slice(5).replace('-', '/') : '--/--'
    },
    weekName(date) {
      const names = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return names[new Date(`${date}T00:00:00`).getDay()]
    },
    schedulePercent(item) {
      const total = Number(item.dueCount || 0) + Number(item.completedCount || 0)
      return total === 0 ? 0 : Math.round(Number(item.completedCount || 0) * 100 / total)
    },
    async loadAdaptiveContent() {
      this.adaptiveLoading = true
      try {
        const data = await adaptiveLearningContent({ subject: this.selectedSubject || undefined })
        this.adaptive = Object.assign({ strategyDescription: '', contentList: [] }, data || {})
      } finally {
        this.adaptiveLoading = false
      }
    },
    openQueryPractice() {
      const learningPoint = this.$route.query.learningPoint
      if (learningPoint) {
        this.generatePractice({ learningPoint })
      }
    },
    async generatePractice(item) {
      this.practiceVisible = true
      this.practiceLoading = true
      this.practice = { title: `${item.learningPoint}专项练习`, learningPoint: item.learningPoint, recommendation: '', questionCount: 0, questionList: [] }
      try {
        const data = await generateWeakPointPractice({ learningPoint: item.learningPoint, questionCount: 5 })
        this.practice = Object.assign({}, this.practice, data || {})
      } finally {
        this.practiceLoading = false
      }
    },
    async generateTypical() {
      this.practiceVisible = true
      this.practiceLoading = true
      this.practice = { title: '典型错题复习', learningPoint: '', recommendation: '', questionCount: 0, questionList: [] }
      try {
        const data = await generateTypicalPractice({
          subject: this.selectedSubject || undefined,
          questionCount: 5
        })
        this.practice = Object.assign({}, this.practice, data || {})
      } finally {
        this.practiceLoading = false
      }
    },
    printPractice() {
      window.print()
    }
  }
}
</script>

<style lang="scss" scoped>
.review-home { padding: 4px; color: #27314a; }
.subject-filter { display: flex; align-items: center; gap: 14px; padding: 12px 16px; margin-bottom: 14px; border-radius: 10px; background: #fff; }
.subject-filter > span { color: #8b94a8; font-size: 13px; }
.welcome-panel { display: flex; justify-content: space-between; align-items: center; min-height: 150px; padding: 30px 36px; margin-bottom: 16px; color: #fff; border-radius: 14px; background: linear-gradient(120deg, #5264ee 0%, #7785fa 62%, #8a74ef 100%); box-shadow: 0 10px 24px rgba(82, 100, 238, .2); }
.welcome-eyebrow { margin-bottom: 8px; font-size: 12px; letter-spacing: 2px; opacity: .76; }
.welcome-panel h1 { margin: 0 0 10px; font-size: 28px; }
.welcome-panel p { margin: 0; font-size: 14px; opacity: .86; }
.welcome-actions { display: flex; align-items: center; gap: 24px; }
.welcome-actions .el-button { padding: 13px 22px; color: #5264ee; border-color: #fff; background: #fff; }
.streak { text-align: center; }
.streak strong { display: block; font-size: 30px; }
.streak span { font-size: 12px; opacity: .8; }
.metrics-row { margin-bottom: 16px; }
.metric-card { margin-bottom: 16px; border: 0; border-radius: 12px; }
.metric-card ::v-deep .el-card__body { display: flex; align-items: center; gap: 14px; padding: 20px; }
.metric-icon { display: flex; align-items: center; justify-content: center; width: 46px; height: 46px; flex: 0 0 46px; border-radius: 12px; font-size: 22px; }
.metric-icon.primary { color: #5264ee; background: #eef0ff; }
.metric-icon.success { color: #31a77c; background: #e9f8f2; }
.metric-icon.warning { color: #e79a39; background: #fff5e7; }
.metric-icon.purple { color: #8b62d7; background: #f3edff; }
.metric-label { margin-bottom: 5px; color: #8b94a8; font-size: 13px; }
.metric-value { font-size: 26px; font-weight: 700; }
.metric-value small { margin-left: 4px; color: #8b94a8; font-size: 12px; font-weight: 400; }
.content-card { margin-bottom: 16px; border: 0; border-radius: 12px; }
.content-card ::v-deep .el-card__header { padding: 18px 20px; border-color: #eef0f5; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header strong { font-size: 16px; }
.card-header span { margin-left: 10px; color: #9aa3b5; font-size: 12px; }
.schedule-card, .mastery-card { min-height: 450px; }
.schedule-list { padding: 0 4px; }
.schedule-item { display: flex; align-items: center; padding: 11px 0; border-bottom: 1px solid #f0f2f6; }
.schedule-item:last-child { border-bottom: 0; }
.schedule-date { width: 78px; color: #7d879c; }
.schedule-date span, .schedule-date strong { display: block; }
.schedule-date span { margin-bottom: 3px; font-size: 12px; }
.schedule-date strong { font-size: 14px; }
.schedule-date.today { color: #5264ee; }
.schedule-bar-wrap { flex: 1; }
.schedule-number { display: flex; justify-content: space-between; margin-bottom: 7px; color: #6d768a; font-size: 12px; }
.mastery-content { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 320px; text-align: center; }
.mastery-content p { margin: 18px 0 8px; font-size: 16px; font-weight: 600; }
.mastery-content span { max-width: 220px; color: #98a1b3; font-size: 13px; line-height: 1.7; }
.point-list { min-height: 240px; }
.point-item { margin-bottom: 22px; }
.point-item:last-child { margin-bottom: 4px; }
.point-title { display: flex; justify-content: space-between; margin-bottom: 9px; color: #4c566c; font-size: 13px; }
.point-title span:last-child { color: #939cad; }
.point-title .el-button { margin-left: 8px; padding: 0; }
.practice-dialog { min-height: 240px; }
.practice-question { display: flex; gap: 14px; padding: 20px 0; border-bottom: 1px solid #edf0f5; }
.practice-order { display: grid; place-items: center; width: 30px; height: 30px; flex: 0 0 30px; color: #fff; border-radius: 50%; background: #5b6cf9; font-weight: 700; }
.practice-main { min-width: 0; flex: 1; }
.practice-tags .el-tag { margin-right: 6px; }
.representative-reason { display: inline-block; padding: 5px 9px; margin-top: 9px; color: #8a6530; border-radius: 6px; background: #fff7e8; font-size: 12px; }
.practice-main h3 { margin: 11px 0 8px; }
.practice-content { color: #586276; line-height: 1.8; white-space: pre-wrap; }
.practice-answer-space { min-height: 58px; padding-top: 10px; margin-top: 14px; color: #a0a8b6; border-top: 1px dashed #cfd4df; }
.practice-count { float: left; color: #8992a5; line-height: 40px; }
.adaptive-card { margin-top: 0; }
.adaptive-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.adaptive-item { display: grid; grid-template-columns: 72px 1fr auto; align-items: center; gap: 12px; padding: 15px; border: 1px solid #edf0f5; border-radius: 10px; }
.adaptive-action { padding: 7px 5px; text-align: center; border-radius: 7px; font-size: 12px; font-weight: 600; }
.adaptive-action.consolidate { color: #c97832; background: #fff2e5; }
.adaptive-action.challenge { color: #7a59c6; background: #f2edff; }
.adaptive-action.review { color: #4d65cd; background: #edf1ff; }
.adaptive-main { min-width: 0; }
.adaptive-main > div span { margin-left: 7px; color: #8c95a8; font-size: 12px; }
.adaptive-main h3 { margin: 7px 0 4px; overflow: hidden; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.adaptive-main p { margin: 0; color: #939cad; font-size: 12px; }
.plan-line { display: flex; justify-content: space-between; padding: 13px 0; border-bottom: 1px solid #f0f2f6; font-size: 13px; }
.plan-line span { color: #8b94a8; }
.plan-tip { padding: 14px; margin-top: 18px; color: #5969d8; border-radius: 8px; background: #f0f2ff; font-size: 12px; }
.plan-tip i { margin-right: 5px; }
@media (max-width: 768px) {
  .welcome-panel { align-items: flex-start; padding: 24px; flex-direction: column; gap: 22px; }
  .welcome-actions { width: 100%; justify-content: space-between; }
  .welcome-panel h1 { font-size: 24px; }
  .subject-filter { align-items: flex-start; flex-direction: column; }
}
</style>
