<template>
  <div v-loading="loading" class="learning-report-page">
    <section class="report-hero">
      <div>
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/system/review')">返回复习概览</el-button>
        <h1>我的学情报告</h1>
        <p>基于错题状态与近 30 天独立作答结果生成，帮助你确定下一步该练什么。</p>
      </div>
      <el-select v-model="subject" clearable placeholder="全部科目" @change="loadReport">
        <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
      </el-select>
    </section>

    <section class="metric-grid">
      <article><span>错题总数</span><strong>{{ report.wrongQuestionCount }}</strong><small>待订正 {{ report.pendingCorrectionCount }}</small></article>
      <article><span>掌握率</span><strong>{{ report.wrongQuestionCount ? `${report.masteryRate}%` : '样本不足' }}</strong><small>已掌握 {{ report.masteredCount }} 道</small></article>
      <article><span>30 天到期完成</span><strong>{{ report.dueReviewCompletedCount30Days }}</strong><small>新增错题 {{ report.newWrongQuestionCount30Days }}</small></article>
      <article class="retention-metric"><span>30 天保持率</span><strong>{{ report.retentionRateReliable ? `${report.retentionRate30Days}%` : '-' }}</strong><small>有效样本 {{ report.retentionSampleCount30Days || 0 }} 次</small><el-button type="text" :loading="actionLoading" @click="showRetentionQuestions">查看保持薄弱题</el-button></article>
    </section>

    <el-card shadow="never" class="content-card trend-card">
      <div slot="header" class="card-header"><div><strong>近 30 日学习趋势</strong><span>掌握率来自每日快照；当天复习按已提交反馈统计</span></div></div>
      <div v-if="report.dailyMetricList.length" class="trend-list">
        <article v-for="item in report.dailyMetricList" :key="item.metricDate" class="trend-item">
          <time>{{ formatMetricDate(item.metricDate) }}</time>
          <div class="trend-rate"><span>掌握率 {{ item.masteryRate || 0 }}%</span><el-progress :percentage="item.masteryRate || 0" :show-text="false" :stroke-width="8" :color="trendColor(item.masteryRate)" /></div>
          <div class="trend-values"><span>复习 {{ item.reviewCount || 0 }}</span><span>独立正确 {{ item.correctAnswerCount || 0 }}/{{ item.judgedAnswerCount || 0 }}</span></div>
        </article>
      </div>
      <el-empty v-else description="学习趋势将在每日快照生成后出现；继续完成订正和复习即可积累数据。" :image-size="80" />
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="content-card">
          <div slot="header" class="card-header"><div><strong>薄弱知识点 TOP10</strong><span>掌握率低、错题多的知识点排在前面</span></div></div>
          <div v-if="report.weakLearningPointList.length" class="point-list">
            <article v-for="item in report.weakLearningPointList" :key="item.knowledgePointId" class="point-item">
              <div class="point-head"><strong>{{ item.knowledgePointName || '未命名知识点' }}</strong><span>{{ item.masteredCount || 0 }}/{{ item.wrongQuestionCount || 0 }} 已掌握</span></div>
              <el-progress :percentage="masteryRate(item)" :stroke-width="9" :color="progressColor(item)" />
              <div class="point-footer"><span>待订正 {{ item.pendingCorrectionCount || 0 }} · 复习中 {{ item.correctedCount || 0 }}</span><div><el-button type="text" :loading="actionLoading" @click="showKnowledgePointQuestions(item)">查看题目</el-button><el-button type="text" @click="startPractice(item)">生成专项练习</el-button></div></div>
            </article>
          </div>
          <el-empty v-else description="订正并关联知识点后会生成薄弱点报告" :image-size="90" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="content-card">
          <div slot="header" class="card-header"><div><strong>错因分布</strong><span>优先解决出现最多的错误习惯</span></div></div>
          <div v-if="report.errorAnalysisList.length" class="error-list">
            <div v-for="item in report.errorAnalysisList" :key="item.errorLabel" class="error-item">
              <div><strong>{{ item.errorLabel }}</strong><span>{{ item.wrongQuestionCount }} 道</span></div>
              <el-progress :percentage="errorPercent(item)" :show-text="false" :stroke-width="8" color="#e6a23c" />
              <el-button type="text" :loading="actionLoading" @click="showErrorLabelQuestions(item)">查看对应题目</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无错因标注" :image-size="90" />
        </el-card>
        <el-card shadow="never" class="content-card ability-card">
          <div slot="header" class="card-header"><div><strong>能力层级</strong><span>由学生人工标注，可随时修正</span></div></div>
          <div v-if="report.abilityLevelStatisticsList.length" class="ability-list">
            <div v-for="item in report.abilityLevelStatisticsList" :key="item.abilityLevel" class="ability-item">
              <div><strong>{{ item.abilityLevelName }}</strong><span>{{ item.wrongQuestionCount }} 道</span></div>
              <small>待订正 {{ item.pendingCorrectionCount }} · 已掌握 {{ item.masteredCount }}</small>
            </div>
          </div>
          <el-empty v-else description="在错题中补充能力层级后，这里会展示分布" :image-size="75" />
        </el-card>
        <el-card shadow="never" class="content-card action-card">
          <h3>下一步建议</h3>
          <p v-if="report.overdueReviewCount">优先完成 {{ report.overdueReviewCount }} 道逾期复习，再安排新的专项练习。</p>
          <p v-else-if="report.pendingCorrectionCount">先完成 {{ report.pendingCorrectionCount }} 道待订正错题，订正后才能进入有效复习。</p>
          <p v-else-if="report.weakLearningPointList.length">从「{{ report.weakLearningPointList[0].knowledgePointName }}」开始做 5 道专项练习。</p>
          <p v-else>继续录入、订正和复习，系统会逐步形成个性化建议。</p>
          <el-button type="primary" @click="goToReview">查看今日复习</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-alert v-if="report.reviewCount30Days && !report.retentionRateReliable" class="sample-alert" type="info" :closable="false" title="近 30 天有效作答少于 3 次，暂不对保持率作强结论；继续完成独立作答后将生成稳定指标。" />

    <el-dialog :title="actionDetail.title || '报告行动题目'" :visible.sync="actionVisible" width="760px" append-to-body>
      <div v-loading="actionLoading">
        <el-alert :title="actionDetail.description || '正在整理可执行的练习题目。'" type="info" :closable="false" />
        <p class="action-summary">共匹配 {{ actionDetail.questionCount || 0 }} 道，当前展示前 {{ actionDetail.questionList.length }} 道；勾选后将带入可编辑组卷篮。</p>
        <el-checkbox-group v-if="actionDetail.questionList.length" v-model="selectedActionQuestionIds" class="action-question-list">
          <article v-for="item in actionDetail.questionList" :key="item.wrongQuestionId" class="action-question-item">
            <el-checkbox :label="item.wrongQuestionId"><strong>{{ item.questionTitle || '未命名题目' }}</strong></el-checkbox>
            <div class="action-question-meta"><el-tag size="mini">{{ item.subjectName || item.subject || '未设置科目' }}</el-tag><el-tag v-if="item.learningPoint" size="mini" type="info">{{ item.learningPoint }}</el-tag><el-tag size="mini" :type="statusType(item.status)">{{ statusText(item.status) }}</el-tag><span v-if="actionDetail.actionType === 'LOW_RETENTION'">独立正确 {{ item.independentCorrectCount30Days || 0 }}/{{ item.independentReviewCount30Days || 0 }}</span><span v-else-if="item.masteryScore !== null && item.masteryScore !== undefined">掌握度 {{ item.masteryScore }}%</span></div>
            <p>{{ item.selectedReason }}</p>
            <el-button type="text" size="mini" @click="openWrongQuestion(item.wrongQuestionId)">查看错题详情</el-button>
          </article>
        </el-checkbox-group>
        <el-empty v-else description="没有可加入组卷篮的题目" :image-size="80" />
      </div>
      <div slot="footer" class="action-dialog-footer"><span>已选 {{ selectedActionQuestionIds.length }} 道</span><el-button @click="actionVisible = false">取消</el-button><el-button type="primary" :disabled="!selectedActionQuestionIds.length" @click="startBasketPractice">加入组卷篮重练</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { dictDataOptions } from '@/api/system/dict'
import { learningReport } from '@/api/system/review'

const emptyReport = () => ({ wrongQuestionCount: 0, pendingCorrectionCount: 0, correctedCount: 0, masteredCount: 0, archivedCount: 0, masteryRate: 0, reviewCount30Days: 0, newWrongQuestionCount30Days: 0, dueReviewCompletedCount30Days: 0, independentCorrectCount30Days: 0, overdueReviewCount: 0, retentionRate30Days: 0, retentionSampleCount30Days: 0, retentionRateReliable: false, weakLearningPointList: [], errorAnalysisList: [], abilityLevelStatisticsList: [], dailyMetricList: [] })
const emptyActionDetail = () => ({ actionType: '', title: '', description: '', questionCount: 0, questionList: [] })

export default {
  name: 'ReviewLearningReportPage',
  data() { return { loading: false, actionLoading: false, actionVisible: false, selectedActionQuestionIds: [], actionDetail: emptyActionDetail(), subject: this.$route.query.subject || '', subjectOptions: [], report: emptyReport() } },
  created() { this.loadSubjectOptions(); this.loadReport() },
  methods: {
    async loadSubjectOptions() { this.subjectOptions = await dictDataOptions({ dictType: 'subject' }).catch(() => []) },
    async loadReport() {
      this.loading = true
      try { this.report = Object.assign(emptyReport(), await learningReport({ subject: this.subject || undefined })) } finally { this.loading = false }
    },
    masteryRate(item) {
      const total = Number(item.wrongQuestionCount || 0)
      return total ? Math.round(Number(item.masteredCount || 0) * 100 / total) : 0
    },
    progressColor(item) { return this.masteryRate(item) < 40 ? '#f56c6c' : this.masteryRate(item) < 70 ? '#e6a23c' : '#67c23a' },
    trendColor(rate) { return Number(rate || 0) < 40 ? '#f56c6c' : Number(rate || 0) < 70 ? '#e6a23c' : '#67c23a' },
    formatMetricDate(value) { return value ? String(value).slice(5).replace('-', '/') : '-' },
    errorPercent(item) { return this.report.wrongQuestionCount ? Math.min(100, Math.round(Number(item.wrongQuestionCount || 0) * 100 / this.report.wrongQuestionCount)) : 0 },
    startPractice(item) { this.$router.push({ path: '/system/review/practice', query: { learningPoint: item.knowledgePointName, subject: this.subject || item.subject || '' } }) },
    async showKnowledgePointQuestions(item) {
      await this.showActionDetail({ actionType: 'KNOWLEDGE_POINT', knowledgePointId: item.knowledgePointId })
    },
    async showErrorLabelQuestions(item) {
      await this.showActionDetail({ actionType: 'ERROR_LABEL', errorLabel: item.errorLabel })
    },
    async showRetentionQuestions() {
      await this.showActionDetail({ actionType: 'LOW_RETENTION' })
    },
    async showActionDetail(actionParams) {
      this.actionLoading = true
      try {
        const response = await learningReport(Object.assign({ subject: this.subject || undefined }, actionParams))
        this.report = Object.assign(emptyReport(), response)
        this.actionDetail = Object.assign(emptyActionDetail(), response.actionDetail || {})
        this.selectedActionQuestionIds = this.actionDetail.questionList.map(item => item.wrongQuestionId)
        this.actionVisible = true
      } finally { this.actionLoading = false }
    },
    startBasketPractice() {
      this.$router.push({ path: '/system/review/practice', query: { basket: this.selectedActionQuestionIds.join(',') } })
    },
    openWrongQuestion(id) { this.$router.push({ path: '/system/wrongquestion/detail', query: { id } }) },
    statusText(status) { return ({ 0: '待订正', 1: '复习中', 2: '已掌握', 3: '已归档' })[status] || '未设置状态' },
    statusType(status) { return ({ 0: 'danger', 1: 'warning', 2: 'success', 3: 'info' })[status] || 'info' },
    goToReview() { this.$router.push({ path: '/system/review/today', query: this.subject ? { subject: this.subject } : {} }) }
  }
}
</script>

<style scoped>
.learning-report-page { max-width: 1220px; padding: 8px; margin: 0 auto; color: #303133; }.report-hero { display: flex; justify-content: space-between; align-items: center; padding: 28px 32px; margin-bottom: 16px; color: #fff; border-radius: 14px; background: linear-gradient(120deg, #4f60dd, #8072e9); }.report-hero h1 { margin: 5px 0 8px; font-size: 26px; }.report-hero p { margin: 0; opacity: .86; font-size: 14px; }.metric-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }.metric-grid article { padding: 20px; border-radius: 10px; background: #fff; box-shadow: 0 2px 12px rgba(0, 0, 0, .04); }.metric-grid span,.metric-grid small { display: block; color: #909399; font-size: 13px; }.metric-grid strong { display: block; margin: 8px 0; font-size: 28px; color: #4f60dd; }.retention-metric .el-button { padding: 4px 0 0; }.content-card { min-height: 300px; margin-bottom: 16px; border: 0; border-radius: 10px; }.card-header { display: flex; justify-content: space-between; }.card-header span { margin-left: 8px; color: #909399; font-size: 12px; }.trend-card { min-height: 0; }.trend-list { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }.trend-item { padding: 12px; border: 1px solid #edf0f6; border-radius: 8px; }.trend-item time { display: block; margin-bottom: 8px; color: #606266; font-size: 13px; }.trend-rate > span { display: block; margin-bottom: 5px; color: #4f60dd; font-size: 12px; }.trend-values { display: flex; justify-content: space-between; gap: 8px; margin-top: 8px; color: #909399; font-size: 12px; }.point-item { padding: 14px 0; border-bottom: 1px solid #f0f2f5; }.point-item:last-child { border: 0; }.point-head,.point-footer,.error-item > div,.ability-item > div { display: flex; justify-content: space-between; gap: 12px; }.point-head { margin-bottom: 8px; }.point-head span,.point-footer,.error-item span,.ability-item span,.ability-item small { color: #909399; font-size: 12px; }.point-footer { align-items: center; margin-top: 6px; }.error-item { margin-bottom: 19px; }.error-item > div { margin-bottom: 7px; }.error-item .el-button { padding: 4px 0 0; }.ability-card { min-height: 0; }.ability-item { padding: 9px 0; border-bottom: 1px solid #f0f2f5; }.ability-item:last-child { border-bottom: 0; }.ability-item strong { color: #4f60dd; }.action-card { background: #f5f7ff; }.action-card h3 { margin-top: 0; }.action-card p { min-height: 45px; color: #606266; line-height: 1.7; }.sample-alert { margin-top: 2px; }.action-summary { margin: 12px 0; color: #909399; font-size: 13px; }.action-question-list { display: block; max-height: 440px; overflow-y: auto; }.action-question-item { padding: 12px 0; border-bottom: 1px solid #ebeef5; }.action-question-item:last-child { border-bottom: 0; }.action-question-meta { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; margin: 8px 0 0 24px; color: #909399; font-size: 12px; }.action-question-item p { margin: 7px 0 0 24px; color: #606266; font-size: 13px; }.action-question-item .el-button { margin-left: 24px; padding: 4px 0; }.action-dialog-footer { display: flex; align-items: center; justify-content: flex-end; gap: 10px; }.action-dialog-footer > span { margin-right: auto; color: #909399; font-size: 13px; } @media (max-width: 768px) { .report-hero { display: block; padding: 22px; }.report-hero .el-select { width: 100%; margin-top: 16px; }.metric-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }.trend-list { grid-template-columns: 1fr; } }
</style>
