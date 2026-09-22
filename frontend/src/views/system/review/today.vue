<template>
  <div v-loading="loading" class="today-review">
    <section class="today-hero">
      <div>
        <div class="hero-date">{{ displayDate }}</div>
        <h1>今日复习</h1>
        <p>先在脑中主动回忆，再查看答案并反馈掌握程度，效果会比重复阅读更好。</p>
      </div>
      <div class="hero-progress">
        <el-progress
          type="circle"
          :percentage="progressPercentage"
          :width="116"
          :stroke-width="10"
          color="#ffffff"
        />
        <span>今日进度</span>
      </div>
    </section>

    <div class="subject-filter">
      <span>复习科目</span>
      <el-radio-group v-model="selectedSubject" size="small" @change="handleSubjectChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="item in home.subjectSettings" :key="item.subject" :label="item.subject">
          {{ item.subjectName }}
        </el-radio-button>
      </el-radio-group>
      <small v-if="currentSubjectSetting">今日上限 {{ currentSubjectSetting.dailyLimit }} 道</small>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col v-for="item in summaryList" :key="item.label" :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div :class="['summary-icon', item.type]"><i :class="item.icon" /></div>
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="task-card">
      <div slot="header" class="task-header">
        <div>
          <strong>今日复习题单</strong>
          <span>逾期优先 · 难题优先 · 遗忘次数优先</span>
        </div>
        <div>
          <el-button icon="el-icon-refresh" @click="loadHome">刷新</el-button>
          <el-button type="primary" :disabled="!home.taskList.length" @click="startReview">
            开始复习
          </el-button>
        </div>
      </div>

      <div v-if="home.taskList.length" class="task-list">
        <article v-for="(task, index) in home.taskList" :key="task.reviewItemId" class="task-item">
          <div class="task-order">{{ String(index + 1).padStart(2, '0') }}</div>
          <div class="task-main">
            <div class="task-tags">
              <el-tag v-if="task.overdue === 1" size="mini" type="danger">已逾期</el-tag>
              <el-tag size="mini">{{ task.subjectName || '未设置科目' }}</el-tag>
              <el-tag size="mini" type="info">阶段 {{ task.stage }}</el-tag>
              <el-tag size="mini" :type="masteryTagType(task.masteryScore)">掌握 {{ task.masteryScore || 0 }}分</el-tag>
              <el-tag v-if="task.level" size="mini" type="warning">难度 {{ task.level }}</el-tag>
            </div>
            <h3>{{ task.questionTitle || '未命名错题' }}</h3>
            <p>{{ task.learningPoint || '未设置知识点' }}</p>
            <div class="task-meta">
              <span><i class="el-icon-time" /> 到期：{{ task.nextReviewTime }}</span>
              <span><i class="el-icon-refresh-left" /> 已复习 {{ task.reviewCount }} 次</span>
              <span><i class="el-icon-check" /> 连对 {{ task.correctStreak || 0 }} 次</span>
              <span><i class="el-icon-close" /> 连错 {{ task.wrongStreak || 0 }} 次</span>
              <span><i class="el-icon-warning-outline" /> 遗忘 {{ task.lapseCount }} 次</span>
            </div>
          </div>
          <el-button type="text" @click="previewTask(index)">查看题目 <i class="el-icon-arrow-right" /></el-button>
        </article>
      </div>
      <el-empty v-else description="今天没有待复习题目，去订正新的错题吧" />
    </el-card>

    <el-dialog
      title="主动回忆"
      :visible.sync="previewVisible"
      width="720px"
      append-to-body
      class="recall-dialog"
    >
      <template v-if="currentTask">
        <div class="recall-tip"><i class="el-icon-view" /> 暂时不要查看答案，先独立回忆解题过程。</div>
        <div class="recall-tags">
          <el-tag size="mini">{{ currentTask.subjectName || '未设置科目' }}</el-tag>
          <el-tag size="mini" type="info">{{ currentTask.questionTypeName || '未设置题型' }}</el-tag>
          <el-tag v-if="currentTask.learningPoint" size="mini" type="success">{{ currentTask.learningPoint }}</el-tag>
        </div>
        <h2>{{ currentTask.questionTitle || '未命名错题' }}</h2>
        <div class="question-content">{{ currentTask.questionContent || '暂无题目文字内容，请查看题目图片。' }}</div>
        <div v-if="questionImages.length" class="question-images">
          <el-image
            v-for="image in questionImages"
            :key="image"
            :src="image"
            :preview-src-list="questionImages"
            fit="contain"
          />
        </div>
        <div class="student-answer-panel">
          <div class="answer-input-title"><strong>我的本次答案</strong><span>答案会随复习记录保存</span></div>
          <el-input
            v-model.trim="studentAnswer"
            type="textarea"
            :rows="4"
            :disabled="Boolean(answer)"
            maxlength="4000"
            show-word-limit
            placeholder="请先独立写下答案或关键解题步骤，再查看标准答案"
          />
        </div>
        <div v-if="answer" class="answer-panel">
          <div class="answer-section wrong-answer">
            <span>我的错误答案</span>
            <p>{{ answer.wrongAnswer || '未记录错误答案' }}</p>
          </div>
          <div class="answer-section correct-answer">
            <span>正确答案</span>
            <p>{{ answer.correctAnswer || '未记录正确答案' }}</p>
          </div>
          <div class="answer-section analysis-answer">
            <span>题目解析</span>
            <p>{{ answer.analysis || '暂无题目解析' }}</p>
          </div>
          <div class="self-judge">
            <strong>对照答案后，本次作答是否正确？</strong>
            <span>客观题最终以系统自动判定为准</span>
            <el-radio-group v-model="selfCorrect" size="medium">
              <el-radio-button :label="1">答对了</el-radio-button>
              <el-radio-button :label="0">答错了</el-radio-button>
            </el-radio-group>
          </div>
          <div class="feedback-title">这道题你回忆得怎么样？</div>
          <div class="feedback-list">
            <button
              v-for="item in feedbackOptions"
              :key="item.value"
              :class="['feedback-button', item.type]"
              type="button"
              :disabled="feedbackSubmitting"
              @click="submitFeedback(item)"
            >
              <i :class="item.icon" />
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </button>
          </div>
        </div>
      </template>
      <div slot="footer">
        <span class="dialog-count">{{ currentTaskIndex + 1 }} / {{ home.taskList.length }}</span>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button v-if="!answer" type="primary" :loading="answerLoading" @click="revealAnswer">
          查看答案
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { reviewAnswer, saveReviewAnswerDraft, submitReviewFeedback, todayReviewHome } from '@/api/system/review'
import { downloadUrl } from '@/api/system/file'

const WRONG_QUESTION_UPLOAD_TYPE = 'wrongQuestion'
const IMAGE_FIELDS = ['imageUrl', 'imageUrl2', 'imageUrl3', 'imageUrl4']

const emptyHome = () => ({
  reviewDate: '',
  totalCount: 0,
  completedCount: 0,
  remainingCount: 0,
  overdueCount: 0,
  estimatedMinutes: 0,
  progressRate: 0,
  continuousReviewDays: 0,
  dailyLimit: 20,
  selectedSubject: '',
  subjectSettings: [],
  taskList: []
})

export default {
  name: 'TodayReviewPage',
  data() {
    return {
      loading: false,
      previewVisible: false,
      currentTaskIndex: 0,
      reviewStartTime: '',
      feedbackRequestId: '',
      answerLoading: false,
      feedbackSubmitting: false,
      answer: null,
      studentAnswer: '',
      selfCorrect: null,
      selectedSubject: this.$route.query.subject || '',
      home: emptyHome()
    }
  },
  computed: {
    displayDate() {
      if (!this.home.reviewDate) return ''
      const date = new Date(`${this.home.reviewDate}T00:00:00`)
      const weekNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      return `${this.home.reviewDate.replaceAll('-', ' / ')} · ${weekNames[date.getDay()]}`
    },
    progressPercentage() {
      return Math.min(100, Math.max(0, Number(this.home.progressRate || 0)))
    },
    currentSubjectSetting() {
      return this.home.subjectSettings.find(item => item.subject === this.selectedSubject) || null
    },
    summaryList() {
      return [
        { label: '今日任务', value: this.home.totalCount, unit: '道', icon: 'el-icon-tickets', type: 'primary' },
        { label: '已经完成', value: this.home.completedCount, unit: '道', icon: 'el-icon-circle-check', type: 'success' },
        { label: '逾期任务', value: this.home.overdueCount, unit: '道', icon: 'el-icon-warning-outline', type: 'warning' },
        { label: '预计用时', value: this.home.estimatedMinutes, unit: '分钟', icon: 'el-icon-alarm-clock', type: 'purple' }
      ]
    },
    currentTask() {
      return this.home.taskList[this.currentTaskIndex] || null
    },
    questionImages() {
      if (!this.currentTask) return []
      return [this.currentTask.imageUrl, this.currentTask.imageUrl2,
        this.currentTask.imageUrl3, this.currentTask.imageUrl4].filter(Boolean)
    },
    feedbackOptions() {
      return [
        { value: 0, label: '忘记', description: '掌握度-20', icon: 'el-icon-close', type: 'forgot' },
        { value: 1, label: '困难', description: '掌握度-10', icon: 'el-icon-warning-outline', type: 'difficult' },
        { value: 2, label: '掌握', description: '掌握度+15', icon: 'el-icon-check', type: 'mastered' },
        { value: 3, label: '很简单', description: '掌握度+20', icon: 'el-icon-star-off', type: 'easy' }
      ]
    }
  },
  created() {
    this.loadHome()
  },
  watch: {
    '$route.query.subject'(subject) {
      const nextSubject = subject || ''
      if (nextSubject !== this.selectedSubject) {
        this.selectedSubject = nextSubject
        this.previewVisible = false
        this.answer = null
        this.loadHome()
      }
    }
  },
  methods: {
    // 刷新今日统计和待复习任务，并将图片文件 ID 转换为可访问地址。
    async loadHome() {
      this.loading = true
      try {
        const data = await todayReviewHome({ subject: this.selectedSubject || undefined })
        this.home = Object.assign(emptyHome(), data || {})
        this.selectedSubject = this.home.selectedSubject || ''
        await this.fillTaskImageUrls()
      } finally {
        this.loading = false
      }
    },
    // 从优先级最高的任务开始今日复习。
    startReview() {
      this.previewTask(0)
    },
    handleSubjectChange(subject) {
      this.previewVisible = false
      this.answer = null
      this.currentTaskIndex = 0
      this.$router.replace({ query: subject ? { subject } : {}})
      this.loadHome()
    },
    // 进入主动回忆阶段，同时固定本题开始时间和幂等请求号。
    previewTask(index) {
      this.currentTaskIndex = index
      this.reviewStartTime = this.formatDateTime(new Date())
      this.feedbackRequestId = this.createRequestId(this.currentTask.reviewItemId)
      this.answer = null
      this.studentAnswer = ''
      this.selfCorrect = null
      this.previewVisible = true
    },
    // 学生主动回忆后再解锁答案，服务端返回的时间用于反馈校验。
    async revealAnswer() {
      if (!this.currentTask) return
      if (!this.studentAnswer) {
        this.$message.warning('请先填写本次答案再查看解析')
        return
      }
      this.answerLoading = true
      try {
        await saveReviewAnswerDraft({ reviewItemId: this.currentTask.reviewItemId, studentAnswer: this.studentAnswer })
        this.answer = await reviewAnswer({ reviewItemId: this.currentTask.reviewItemId })
      } finally {
        this.answerLoading = false
      }
    },
    // 保存四级反馈，刷新动态排期后自动进入下一道到期题目。
    async submitFeedback(feedback) {
      if (!this.currentTask || !this.answer) return
      if (this.selfCorrect === null) {
        this.$message.warning('请先确认本次作答是否正确')
        return
      }
      this.feedbackSubmitting = true
      try {
        const result = await submitReviewFeedback({
          requestId: this.feedbackRequestId,
          reviewItemId: this.currentTask.reviewItemId,
          startTime: this.reviewStartTime,
          revealToken: this.answer.revealToken,
          studentAnswer: this.studentAnswer,
          selfCorrect: this.selfCorrect,
          feedback: feedback.value
        })
        const masteredText = result.mastered ? '，该错题已标记为掌握' : ''
        const deltaText = result.masteryScoreDelta > 0 ? `+${result.masteryScoreDelta}` : result.masteryScoreDelta
        this.$message.success(`掌握度 ${result.masteryScoreAfter}分（${deltaText}），下次复习：${result.nextReviewTime}${masteredText}`)
        this.answer = null
        this.studentAnswer = ''
        this.selfCorrect = null
        await this.loadHome()
        if (this.home.taskList.length) {
          this.previewTask(0)
        } else {
          this.previewVisible = false
        }
      } finally {
        this.feedbackSubmitting = false
      }
    },
    // 并行解析任务图片，避免多图题目逐张等待下载地址。
    async fillTaskImageUrls() {
      await Promise.all(this.home.taskList.map(async task => {
        await Promise.all(IMAGE_FIELDS.map(async field => {
          task[field] = await this.resolveFileUrl(task[field])
        }))
      }))
    },
    // 兼容历史完整 URL 和当前文件 ID 两种图片数据格式。
    resolveFileUrl(fileId) {
      if (!fileId) return Promise.resolve('')
      const fileIdNumber = Number(fileId)
      if (!Number.isFinite(fileIdNumber)) return Promise.resolve(fileId)
      return downloadUrl({
        fileId: fileIdNumber,
        uploadType: WRONG_QUESTION_UPLOAD_TYPE
      }).catch(() => '')
    },
    // 同一道题的单次展示周期复用该请求号，避免重复点击造成重复记档。
    createRequestId(reviewItemId) {
      return `${Date.now()}-${reviewItemId}-${Math.random().toString(36).slice(2, 10)}`
    },
    // 按后端 LocalDateTime 可解析格式提交主动回忆开始时间。
    formatDateTime(date) {
      const pad = value => String(value).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
        `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    masteryTagType(score) {
      const value = Number(score || 0)
      if (value < 40) return 'danger'
      if (value < 70) return 'warning'
      if (value < 90) return 'success'
      return ''
    }
  }
}
</script>

<style lang="scss" scoped>
.today-review { padding: 4px; color: #27314a; }
.subject-filter { display: flex; align-items: center; gap: 14px; padding: 12px 16px; margin-bottom: 16px; border-radius: 10px; background: #fff; }
.subject-filter > span { color: #8b94a8; font-size: 13px; }
.subject-filter > small { margin-left: auto; color: #7d879c; }
.today-hero { display: flex; align-items: center; justify-content: space-between; min-height: 164px; padding: 28px 42px; margin-bottom: 18px; color: #fff; border-radius: 14px; background: linear-gradient(125deg, #4357dd, #6d79ef 62%, #866bea); box-shadow: 0 12px 28px rgba(67, 87, 221, .22); }
.hero-date { margin-bottom: 8px; font-size: 13px; letter-spacing: 1px; opacity: .78; }
.today-hero h1 { margin: 0 0 12px; font-size: 30px; }
.today-hero p { max-width: 620px; margin: 0; line-height: 1.8; opacity: .86; }
.hero-progress { display: flex; align-items: center; gap: 14px; }
.hero-progress ::v-deep .el-progress__text { color: #fff; font-size: 20px !important; }
.hero-progress span { font-size: 13px; opacity: .82; }
.summary-row { margin-bottom: 2px; }
.summary-card { margin-bottom: 16px; border: 0; border-radius: 12px; }
.summary-card ::v-deep .el-card__body { display: flex; align-items: center; gap: 14px; padding: 19px; }
.summary-icon { display: flex; align-items: center; justify-content: center; width: 44px; height: 44px; border-radius: 12px; font-size: 20px; }
.summary-icon.primary { color: #5264ee; background: #eef0ff; }
.summary-icon.success { color: #31a77c; background: #e9f8f2; }
.summary-icon.warning { color: #e79a39; background: #fff5e7; }
.summary-icon.purple { color: #8b62d7; background: #f3edff; }
.summary-card span { display: block; margin-bottom: 4px; color: #8e97a9; font-size: 12px; }
.summary-card strong { display: block; font-size: 24px; }
.summary-card small { margin-left: 4px; color: #9ba4b4; font-size: 12px; font-weight: 400; }
.task-card { border: 0; border-radius: 12px; }
.task-card ::v-deep .el-card__header { padding: 17px 20px; border-color: #eef0f5; }
.task-header { display: flex; align-items: center; justify-content: space-between; }
.task-header strong { font-size: 16px; }
.task-header span { margin-left: 10px; color: #99a2b3; font-size: 12px; }
.task-list { padding: 0 4px; }
.task-item { display: flex; align-items: center; gap: 18px; padding: 20px 10px; border-bottom: 1px solid #eef0f5; }
.task-item:last-child { border-bottom: 0; }
.task-order { width: 38px; color: #bec4d0; font-size: 17px; font-weight: 700; }
.task-main { min-width: 0; flex: 1; }
.task-tags .el-tag { margin-right: 6px; }
.task-main h3 { margin: 10px 0 7px; overflow: hidden; font-size: 15px; text-overflow: ellipsis; white-space: nowrap; }
.task-main p { margin: 0 0 10px; color: #758096; font-size: 13px; }
.task-meta { display: flex; flex-wrap: wrap; gap: 18px; color: #9ba3b3; font-size: 12px; }
.task-meta i { margin-right: 3px; }
.recall-tip { padding: 12px 15px; margin-bottom: 18px; color: #5365d7; border-radius: 8px; background: #f0f2ff; }
.recall-tags .el-tag { margin-right: 7px; }
.recall-dialog h2 { margin: 18px 0 12px; font-size: 20px; }
.question-content { min-height: 100px; padding: 18px; color: #414b60; border-radius: 9px; background: #f7f8fb; line-height: 1.9; white-space: pre-wrap; }
.question-images { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-top: 14px; }
.question-images .el-image { height: 210px; border-radius: 8px; background: #f5f6f8; }
.student-answer-panel { padding: 16px; margin-top: 16px; border: 1px solid #e5e8f1; border-radius: 9px; background: #fbfcff; }
.answer-input-title { display: flex; justify-content: space-between; margin-bottom: 10px; }
.answer-input-title span { color: #969fb1; font-size: 12px; }
.self-judge { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 5px 14px; padding: 16px; margin: 16px 0; border-radius: 9px; background: #f5f7ff; }
.self-judge > span { grid-column: 1; color: #8c95a8; font-size: 12px; }
.self-judge .el-radio-group { grid-column: 2; grid-row: 1 / span 2; }
.answer-panel { padding-top: 18px; margin-top: 20px; border-top: 1px solid #eceef3; }
.answer-section { padding: 13px 16px; margin-bottom: 10px; border-radius: 8px; }
.answer-section span { font-size: 12px; font-weight: 600; }
.answer-section p { margin: 7px 0 0; color: #3f485b; line-height: 1.8; white-space: pre-wrap; }
.wrong-answer { background: #fff3f1; }
.wrong-answer span { color: #dc665d; }
.correct-answer { background: #edf9f4; }
.correct-answer span { color: #2e9d76; }
.analysis-answer { background: #f4f5fb; }
.analysis-answer span { color: #6471ca; }
.feedback-title { margin: 22px 0 12px; font-size: 15px; font-weight: 600; text-align: center; }
.feedback-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.feedback-button { padding: 14px 8px; border: 1px solid transparent; border-radius: 9px; background: #f7f8fa; cursor: pointer; transition: all .2s; }
.feedback-button i, .feedback-button strong, .feedback-button span { display: block; }
.feedback-button i { margin-bottom: 7px; font-size: 20px; }
.feedback-button strong { margin-bottom: 5px; color: #354058; }
.feedback-button span { color: #929bad; font-size: 11px; }
.feedback-button:hover { transform: translateY(-2px); box-shadow: 0 6px 14px rgba(43, 52, 74, .1); }
.feedback-button:disabled { cursor: not-allowed; opacity: .55; transform: none; }
.feedback-button.forgot { color: #df625a; border-color: #f4cbc7; background: #fff6f5; }
.feedback-button.difficult { color: #dd9133; border-color: #f3d8b2; background: #fff9ef; }
.feedback-button.mastered { color: #35a57d; border-color: #bfe6d7; background: #f1fbf7; }
.feedback-button.easy { color: #6470dc; border-color: #ccd1f5; background: #f5f6ff; }
.dialog-count { float: left; padding-top: 8px; color: #929bad; }
@media (max-width: 768px) {
  .today-hero { align-items: flex-start; padding: 24px; flex-direction: column; gap: 18px; }
  .hero-progress { align-self: center; }
  .task-header { align-items: flex-start; flex-direction: column; gap: 14px; }
  .task-header span { display: block; margin: 5px 0 0; }
  .task-order { display: none; }
  .task-meta { gap: 8px; flex-direction: column; }
  .feedback-list { grid-template-columns: repeat(2, 1fr); }
  .subject-filter { align-items: flex-start; flex-direction: column; }
  .subject-filter > small { margin-left: 0; }
}
</style>
