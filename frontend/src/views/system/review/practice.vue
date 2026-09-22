<template>
  <div class="practice-page">
    <section class="practice-hero">
      <div>
        <h1>个人组卷与练习</h1>
        <p>按知识点、错因或科目组卷；可打印题目版、答案版或导出 Word，纸面完成后用短码回填。</p>
      </div>
      <div><el-button @click="openExportHistory">导出历史</el-button><el-button type="primary" icon="el-icon-plus" @click="createVisible = true">新建练习</el-button></div>
    </section>

    <section class="stats-grid">
      <div class="stat-item">
        <span>练习次数</span>
        <strong>{{ statistics.sessionCount || 0 }}</strong>
        <small>完成 {{ statistics.finishedSessionCount || 0 }} 次</small>
      </div>
      <div class="stat-item">
        <span>累计作答</span>
        <strong>{{ statistics.answeredCount || 0 }}</strong>
        <small>题目 {{ statistics.questionCount || 0 }} 道</small>
      </div>
      <div class="stat-item">
        <span>平均正确率</span>
        <strong>{{ statistics.averageAccuracyRate || 0 }}%</strong>
        <small>正确 {{ statistics.correctCount || 0 }} · 错误 {{ statistics.wrongCount || 0 }}</small>
      </div>
      <div class="stat-item">
        <span>薄弱知识点</span>
        <strong>{{ firstWeakPoint }}</strong>
        <small>{{ firstWeakPointTip }}</small>
      </div>
      <div class="stat-item">
        <span>7天趋势</span>
        <strong>{{ trendText(7) }}</strong>
        <small>{{ trendTip(7) }}</small>
      </div>
      <div class="stat-item">
        <span>30天趋势</span>
        <strong>{{ trendText(30) }}</strong>
        <small>{{ trendTip(30) }}</small>
      </div>
    </section>

    <el-card shadow="never" class="content-card">
      <div class="filter-bar">
        <el-input v-model.trim="query.keyWord" clearable prefix-icon="el-icon-search" placeholder="搜索标题/知识点/错因" @keyup.enter.native="loadHistory" />
        <el-select v-model="query.practiceType" clearable placeholder="练习类型" @change="search">
          <el-option label="知识点专项" value="KNOWLEDGE" />
          <el-option label="错因练习" value="ERROR_LABEL" />
          <el-option label="典型错题" value="TYPICAL" />
        </el-select>
        <el-select v-model="query.subject" clearable placeholder="科目" @change="search">
          <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
        <el-input v-model.trim="paperCode" clearable class="paper-code-input" placeholder="纸面短码，如 P0000000012" @keyup.enter.native="openPaperCode" />
        <el-button @click="openPaperCode">回填短码</el-button>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      </div>

      <el-table v-loading="historyLoading" :data="page.list">
        <el-table-column prop="title" label="练习" min-width="180" />
        <el-table-column label="类型" width="105"><template slot-scope="scope">{{ typeLabel(scope.row.practiceType) }}</template></el-table-column>
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column prop="learningPoint" label="知识点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="errorLabel" label="错因" min-width="120" show-overflow-tooltip />
        <el-table-column label="结果" width="150">
          <template slot-scope="scope">{{ scope.row.correctCount || 0 }}/{{ scope.row.questionCount || 0 }} · {{ scope.row.accuracyRate || 0 }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope"><el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'warning'">{{ scope.row.status === 1 ? '已完成' : '进行中' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="168" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope"><el-button type="text" @click="openSession(scope.row.id)">进入练习</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination background layout="total, prev, pager, next" :current-page="query.current" :page-size="query.pageSize" :total="page.total" @current-change="changePage" />
    </el-card>

    <el-dialog title="新建专项练习" :visible.sync="createVisible" width="560px" append-to-body>
      <el-form :model="form" label-width="88px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.practiceType">
            <el-radio-button label="KNOWLEDGE">知识点</el-radio-button>
            <el-radio-button label="ERROR_LABEL">错因</el-radio-button>
            <el-radio-button label="TYPICAL">典型题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题源">
          <el-radio-group v-model="form.questionSource">
            <el-radio-button label="WRONG_QUESTION">错题本</el-radio-button>
            <el-radio-button label="QUESTION_BANK">题库</el-radio-button>
            <el-radio-button label="MIXED">混合</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="科目"><el-select v-model="form.subject" clearable placeholder="全部科目"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item v-if="form.practiceType === 'KNOWLEDGE'" label="知识点"><el-input v-model.trim="form.learningPoint" placeholder="输入知识点名称" /></el-form-item>
        <el-form-item v-if="form.practiceType === 'ERROR_LABEL'" label="错因"><el-input v-model.trim="form.errorLabel" placeholder="输入错因标签" /></el-form-item>
        <el-form-item label="难度"><el-select v-model="form.difficulty" clearable placeholder="全部难度"><el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item v-if="form.questionSource !== 'QUESTION_BANK'" label="已掌握题"><el-switch v-model="form.includeMastered" active-text="包含" inactive-text="默认排除" /></el-form-item>
        <el-form-item label="题数"><el-input-number v-model="form.questionCount" :min="1" :max="50" /></el-form-item>
      </el-form>
      <div v-if="preview" class="practice-preview" :class="{ 'is-shortage': preview.shortageQuestionCount > 0 }">
        <strong>组卷预览</strong>
        <span>可组 {{ preview.availableQuestionCount || 0 }}/{{ preview.requestedQuestionCount || form.questionCount }} 题</span>
        <span>错题本 {{ preview.wrongQuestionCount || 0 }} · 题库 {{ preview.bankQuestionCount || 0 }}</span>
        <p v-if="preview.learningPointList && preview.learningPointList.length">覆盖：{{ preview.learningPointList.join('、') }}</p>
        <p>{{ preview.shortageQuestionCount > 0 ? `当前条件还缺 ${preview.shortageQuestionCount} 题，请调整条件或减少题量。` : '题量充足，可以生成练习。' }}</p>
      </div>
      <div slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button :loading="previewing" @click="previewSession">预览组卷</el-button>
        <el-button type="primary" :loading="creating" @click="createSession">生成练习</el-button>
      </div>
    </el-dialog>
    <el-dialog title="服务端导出历史" :visible.sync="exportHistoryVisible" width="760px" append-to-body>
      <el-table :data="exportTaskList" size="small"><el-table-column prop="fileName" label="文件" min-width="220" /><el-table-column prop="format" label="格式" width="80" /><el-table-column label="版本" width="90"><template slot-scope="{ row }">{{ row.answerMode === 1 ? '答案版' : '题目版' }}</template></el-table-column><el-table-column label="状态" width="100"><template slot-scope="{ row }">{{ exportStatusText(row.status) }}</template></el-table-column><el-table-column prop="createTime" label="创建时间" width="170" /><el-table-column label="操作" width="100"><template slot-scope="{ row }"><el-button v-if="row.status === 2" type="text" @click="downloadExportTask(row)">下载</el-button><span v-else-if="row.status === 3" class="export-error" :title="row.errorMessage">失败</span></template></el-table-column></el-table>
    </el-dialog>

    <el-dialog :title="session.title || '专项练习'" :visible.sync="sessionVisible" width="860px" append-to-body>
      <div v-if="session.sessionId" class="session-summary">
        <span>短码 {{ currentPaperCode }}</span>
        <span>题目 {{ session.questionCount || 0 }}</span>
        <span>已答 {{ session.answeredCount || 0 }}</span>
        <span>正确 {{ session.correctCount || 0 }}</span>
        <span>正确率 {{ session.accuracyRate || 0 }}%</span>
        <span v-if="draftSaving">草稿保存中...</span>
        <span v-else-if="draftSavedText">{{ draftSavedText }}</span>
      </div>
      <el-alert v-if="session.generationReason" class="generation-reason" type="info" :closable="false" :title="`组卷依据：${session.generationReason}`" />
      <section v-if="session.sessionId && session.status === 1" class="result-panel">
        <div>
          <span>练习结果</span>
          <strong>{{ resultLevel(session.accuracyRate) }}</strong>
        </div>
        <div>
          <span>需回看题目</span>
          <strong>{{ wrongQuestionList.length }}</strong>
        </div>
        <div>
          <span>复习计划</span>
          <strong>{{ wrongQuestionList.length ? '已提前安排' : '正常巩固' }}</strong>
        </div>
      </section>
      <section v-if="wrongQuestionList.length" class="wrong-review">
        <h4>错题复盘</h4>
        <el-tag v-for="item in wrongQuestionList" :key="item.sessionQuestionId" type="danger" size="mini">
          {{ item.learningPoint || item.questionTitle || '未命名题目' }}
        </el-tag>
      </section>
      <article v-for="(item,index) in session.questionList" :key="item.sessionQuestionId" class="question-card">
        <div class="question-head">
          <el-tag size="mini">第 {{ index + 1 }} 题</el-tag>
          <el-tag v-if="item.answered" size="mini" :type="item.isCorrect === 1 ? 'success' : 'danger'">
            {{ item.isCorrect === 1 ? '正确' : '错误' }}
          </el-tag>
          <el-tag v-else size="mini" type="info">{{ sourceLabel(item.questionSource) }}</el-tag>
        </div>
        <p v-if="item.sourceReason" class="source-reason">入选依据：{{ item.sourceReason }}</p>
        <h3>{{ item.questionTitle || '未命名题目' }}</h3>
        <div class="question-content">{{ item.questionContent || '暂无题干文字，请查看图片。' }}</div>
        <div v-if="imageList(item).length" class="question-images"><el-image v-for="image in imageList(item)" :key="image" :src="image" :preview-src-list="imageList(item)" fit="contain" /></div>
        <el-alert class="judge-tip" :closable="false" type="info" :title="isAutoJudge(item) ? '本题将按标准答案自动判定；提交后即可查看解析。' : '本题为主观题，请先作答，查看参考答案后再完成自评。'" />
        <el-input v-model.trim="item.studentAnswer" type="textarea" :rows="3" :disabled="item.answered" placeholder="输入答案或解题步骤" @focus="startAnswerTiming(item)" @blur="pauseAnswerTiming(item, true)" @input="scheduleDraftSave(item)" />
        <section v-if="item.referenceAnswer" class="reference-answer">
          <p><b>标准答案：</b>{{ item.referenceAnswer.correctAnswer || '-' }}</p>
          <p v-if="item.referenceAnswer.analysis"><b>解析：</b>{{ item.referenceAnswer.analysis }}</p>
        </section>
        <div class="answer-actions">
          <el-button v-if="!isAutoJudge(item) && !item.referenceAnswer && !item.answered" size="small" :disabled="!item.studentAnswer" :loading="item.revealing" @click="revealReferenceAnswer(item)">查看参考答案</el-button>
          <el-radio-group v-if="!isAutoJudge(item) && item.referenceAnswer" v-model="item.selfCorrect" size="small" :disabled="item.answered">
            <el-radio-button :label="1">自评正确</el-radio-button>
            <el-radio-button :label="0">自评错误</el-radio-button>
          </el-radio-group>
          <span v-if="isAutoJudge(item)" class="auto-judge-text">系统自动判题</span>
          <el-button type="primary" size="small" :disabled="item.answered || (!isAutoJudge(item) && !item.referenceAnswer)" :loading="item.submitting" @click="submitAnswer(item)">提交</el-button>
        </div>
        <el-alert v-if="item.result" :type="item.result.correct ? 'success' : 'error'" :title="item.result.correct ? '回答正确' : `回答错误，正确答案：${item.result.correctAnswer || '-'}`" :closable="false">
          <div>判定方式：{{ item.result.judgeType === 'AUTO' ? '系统自动判定' : '根据自评结果判定' }}</div>
          <div v-if="item.result.autoCollectedWrongQuestion">已自动加入错题本，并进入复习计划。</div>
          <div v-if="item.result.analysis">解析：{{ item.result.analysis }}</div>
        </el-alert>
        <div v-else-if="item.answered" class="answer-review">
          <p><b>我的答案：</b>{{ item.studentAnswer || '-' }}</p>
          <p><b>正确答案：</b>{{ item.correctAnswer || '-' }}</p>
          <p v-if="item.analysis"><b>解析：</b>{{ item.analysis }}</p>
          <p><b>用时：</b>{{ item.durationSeconds || 0 }} 秒</p>
        </div>
      </article>
      <div slot="footer">
        <el-button @click="sessionVisible = false">关闭</el-button>
        <el-button :disabled="!session.sessionId" icon="el-icon-printer" @click="printPaper(false)">基础打印（题目版）</el-button>
        <el-button :disabled="!session.sessionId" icon="el-icon-printer" @click="printPaper(true)">基础打印（答案版）</el-button>
        <el-dropdown :disabled="!session.sessionId" @command="downloadWord">
          <el-button>基础 HTML Word<i class="el-icon-arrow-down el-icon--right" /></el-button>
          <el-dropdown-menu slot="dropdown"><el-dropdown-item :command="false">题目版</el-dropdown-item><el-dropdown-item :command="true">答案版</el-dropdown-item></el-dropdown-menu>
        </el-dropdown>
        <el-dropdown :disabled="!session.sessionId" @command="createServerExport">
          <el-button>服务端导出<i class="el-icon-arrow-down el-icon--right" /></el-button>
          <el-dropdown-menu slot="dropdown"><el-dropdown-item command="PDF:0">PDF 题目版</el-dropdown-item><el-dropdown-item command="PDF:1">PDF 答案版</el-dropdown-item><el-dropdown-item command="DOCX:0">DOCX 题目版</el-dropdown-item><el-dropdown-item command="DOCX:1">DOCX 答案版</el-dropdown-item></el-dropdown-menu>
        </el-dropdown>
        <el-button :disabled="!session.sessionId || session.status === 1" :loading="draftSaving" @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" :disabled="!canBatchSubmit" :loading="batchSubmitting" @click="batchSubmit">批量提交已作答</el-button>
        <el-button type="success" :disabled="!session.sessionId || session.status === 1" @click="finishSession">完成练习</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { batchSubmitPracticeAnswer, createPracticePaperExportTask, createPracticeSession, finishPracticeSession, practicePaperDetail, practicePaperExportTaskList, practiceQuestionAnswer, practiceSessionDetail, practiceSessionPageList, practiceStatistics, previewPracticeSession, savePracticeAnswerDraft, submitPracticeAnswer } from '@/api/system/review'
import { downloadUrl } from '@/api/system/file'
import { dictDataOptions } from '@/api/system/dict'
import { downloadPracticePaperWord, practicePaperCode, printPracticePaper } from '@/utils/practicePaperExport'

const emptyPage = () => ({ current: 1, pageSize: 10, total: 0, list: [] })
const emptySession = () => ({ sessionId: null, questionList: [] })
const emptyStatistics = () => ({ sessionCount: 0, finishedSessionCount: 0, questionCount: 0, answeredCount: 0, correctCount: 0, wrongCount: 0, averageAccuracyRate: 0, weakLearningPointList: [], errorLabelList: [], trendList: [] })

export default {
  name: 'PracticeSessionPage',
  data() {
    return {
      historyLoading: false,
      creating: false,
      previewing: false,
      batchSubmitting: false,
      draftSaving: false,
      exportHistoryVisible: false,
      exportTaskList: [],
      draftSavedText: '',
      draftTimer: null,
      answerResultMap: {},
      referenceAnswerMap: {},
      paperCode: '',
      createVisible: false,
      sessionVisible: false,
      subjectOptions: [],
      query: { current: 1, pageSize: 10, keyWord: '', practiceType: '', subject: '' },
      page: emptyPage(),
      statistics: emptyStatistics(),
      difficultyOptions: [{ label: '1 - 简单', value: 1 }, { label: '2 - 较易', value: 2 }, { label: '3 - 中等', value: 3 }, { label: '4 - 较难', value: 4 }, { label: '5 - 困难', value: 5 }],
      form: { practiceType: 'KNOWLEDGE', questionSource: 'WRONG_QUESTION', subject: '', learningPoint: '', errorLabel: '', difficulty: null, includeMastered: false, questionCount: 5 },
      preview: null,
      session: emptySession()
    }
  },
  computed: {
    wrongQuestionList() {
      return (this.session.questionList || []).filter(item => item.answered && item.isCorrect === 0)
    },
    firstWeakPoint() {
      const first = (this.statistics.weakLearningPointList || [])[0]
      return first ? first.name : '-'
    },
    firstWeakPointTip() {
      const first = (this.statistics.weakLearningPointList || [])[0]
      return first ? `正确率 ${first.accuracyRate || 0}% · 错 ${first.wrongCount || 0}` : '暂无薄弱项'
    },
    canBatchSubmit() {
      return this.session.sessionId && this.session.status !== 1
        && (this.session.questionList || []).some(item => !item.answered && item.studentAnswer)
    }
    ,
    currentPaperCode() {
      return this.session.sessionId ? practicePaperCode(this.session) : ''
    }
  },
  created() {
    this.loadSubjectOptions()
    this.loadHistory()
    this.loadStatistics()
    const learningPoint = this.$route.query.learningPoint
    if (learningPoint) {
      this.form.learningPoint = learningPoint
      this.form.subject = this.$route.query.subject || ''
      this.createVisible = true
    }
  },
  beforeDestroy() {
    if (this.draftTimer) {
      clearTimeout(this.draftTimer)
    }
  },
  methods: {
    async loadSubjectOptions() {
      this.subjectOptions = await dictDataOptions({ dictType: 'subject' }).catch(() => [])
    },
    async loadHistory() {
      this.historyLoading = true
      try {
        this.page = Object.assign(emptyPage(), await practiceSessionPageList(this.query))
      } finally {
        this.historyLoading = false
      }
    },
    async loadStatistics() {
      this.statistics = Object.assign(emptyStatistics(), await practiceStatistics(this.query))
    },
    search() {
      this.query.current = 1
      this.loadHistory()
      this.loadStatistics()
    },
    changePage(page) {
      this.query.current = page
      this.loadHistory()
    },
    async createSession() {
      this.creating = true
      try {
        const preview = await previewPracticeSession(this.form)
        this.preview = preview
        if (preview.shortageQuestionCount > 0) {
          this.$message.warning(`当前条件还缺 ${preview.shortageQuestionCount} 题，请调整条件后再生成`)
          return
        }
        this.session = this.prepareSession(await createPracticeSession(this.form))
        this.createVisible = false
        this.sessionVisible = true
        this.loadHistory()
        this.loadStatistics()
      } finally {
        this.creating = false
      }
    },
    async previewSession() {
      this.previewing = true
      try {
        this.preview = await previewPracticeSession(this.form)
        if (this.preview.shortageQuestionCount > 0) {
          this.$message.warning(`当前条件还缺 ${this.preview.shortageQuestionCount} 题`)
        }
      } finally {
        this.previewing = false
      }
    },
    async openSession(id) {
      this.session = this.prepareSession(await practiceSessionDetail({ sessionId: id }))
      this.sessionVisible = true
    },
    async openPaperCode() {
      const normalized = String(this.paperCode || '').trim().toUpperCase().replace(/^P/, '')
      if (!/^\d+$/.test(normalized) || Number(normalized) < 1) {
        this.$message.warning('请输入有效短码，例如 P0000000012')
        return
      }
      await this.openSession(Number(normalized))
      this.paperCode = ''
    },
    async printPaper(answerMode) {
      const printWindow = window.open('', '_blank')
      if (!printWindow) {
        this.$message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
        return
      }
      try {
        const session = await this.sessionForExport(answerMode)
        if (!printPracticePaper(session, answerMode, printWindow)) {
          this.$message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
        }
      } catch (error) {
        printWindow.close()
      }
    },
    async downloadWord(answerMode) {
      const session = await this.sessionForExport(answerMode)
      downloadPracticePaperWord(session, answerMode)
      this.$message.success(`已导出 ${answerMode ? '答案版' : '题目版'} Word 文件`)
    },
    async createServerExport(command) {
      const [format, answerMode] = String(command).split(':')
      const taskId = await createPracticePaperExportTask({ sessionId: this.session.sessionId, format, answerMode: Number(answerMode) })
      this.$message.success('导出任务已提交，正在后台生成')
      this.pollServerExport(taskId)
    },
    async pollServerExport(taskId) {
      const taskList = await practicePaperExportTaskList()
      const task = (taskList || []).find(item => item.id === taskId)
      if (!task || task.status === 0 || task.status === 1) return setTimeout(() => this.pollServerExport(taskId), 1200)
      if (task.status === 2 && task.fileId) { const url = await downloadUrl({ fileId: task.fileId, uploadType: 'PRACTICE_EXPORT' }); window.open(url, '_blank'); return }
      this.$message.error(task.errorMessage || '导出失败，请重试')
    },
    async openExportHistory() { this.exportTaskList = await practicePaperExportTaskList(); this.exportHistoryVisible = true },
    exportStatusText(status) { return ({ 0: '排队中', 1: '生成中', 2: '已完成', 3: '失败' })[status] || '未知' },
    async downloadExportTask(task) { const url = await downloadUrl({ fileId: task.fileId, uploadType: 'PRACTICE_EXPORT' }); window.open(url, '_blank') },
    async sessionForExport(answerMode) {
      if (!answerMode) return this.session
      return practicePaperDetail({ sessionId: this.session.sessionId })
    },
    async submitAnswer(item) {
      if (!item.studentAnswer) {
        this.$message.warning('请先输入答案')
        return
      }
      if (!this.isAutoJudge(item) && item.selfCorrect === null) {
        this.$message.warning('请先完成主观题自评')
        return
      }
      this.pauseAnswerTiming(item)
      item.submitting = true
      try {
        const result = await submitPracticeAnswer({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer,
          selfCorrect: item.selfCorrect,
          durationSeconds: item.durationSeconds || 0
        })
        this.$set(this.answerResultMap, item.sessionQuestionId, result)
        this.session = this.prepareSession(await practiceSessionDetail({ sessionId: this.session.sessionId }))
        this.loadStatistics()
      } finally {
        item.submitting = false
      }
    },
    async revealReferenceAnswer(item) {
      if (!item.studentAnswer) {
        this.$message.warning('请先完成作答，再查看参考答案')
        return
      }
      this.pauseAnswerTiming(item, true)
      item.revealing = true
      try {
        const referenceAnswer = await practiceQuestionAnswer({ sessionQuestionId: item.sessionQuestionId })
        this.$set(this.referenceAnswerMap, item.sessionQuestionId, referenceAnswer)
        item.referenceAnswer = referenceAnswer
      } finally {
        item.revealing = false
      }
    },
    scheduleDraftSave(item) {
      if (!this.session.sessionId || item.answered) {
        return
      }
      item.dirty = true
      this.draftSavedText = ''
      if (this.draftTimer) {
        clearTimeout(this.draftTimer)
      }
      this.draftTimer = setTimeout(() => {
        this.saveDraft(true)
      }, 1200)
    },
    startAnswerTiming(item) {
      if (!item.answered && !item.answerStartedAt) {
        item.answerStartedAt = Date.now()
      }
    },
    pauseAnswerTiming(item, saveDraft) {
      if (!item || !item.answerStartedAt) return
      const elapsed = Math.max(0, Math.round((Date.now() - item.answerStartedAt) / 1000))
      item.durationSeconds = Math.min(86400, Number(item.durationSeconds || 0) + elapsed)
      item.answerStartedAt = null
      if (saveDraft) this.scheduleDraftSave(item)
    },
    async saveDraft(silent) {
      if (!this.session.sessionId || this.session.status === 1) {
        return
      }
      const draftList = (this.session.questionList || [])
        .filter(item => !item.answered && (item.dirty || !silent))
        .map(item => ({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer || '',
          durationSeconds: item.durationSeconds || 0
        }))
      if (!draftList.length) {
        if (!silent) this.$message.info('暂无需要保存的草稿')
        return
      }
      this.draftSaving = true
      try {
        this.session = this.prepareSession(await savePracticeAnswerDraft({
          sessionId: this.session.sessionId,
          draftList
        }))
        this.draftSavedText = '草稿已保存'
        if (!silent) this.$message.success('草稿已保存')
      } finally {
        this.draftSaving = false
      }
    },
    async batchSubmit() {
      const currentQuestionList = this.session.questionList || []
      currentQuestionList.forEach(item => this.pauseAnswerTiming(item))
      const answerList = (this.session.questionList || [])
        .filter(item => !item.answered && item.studentAnswer)
        .map(item => ({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer,
          selfCorrect: item.selfCorrect,
          durationSeconds: item.durationSeconds || 0
        }))
      if (!answerList.length) {
        this.$message.warning('请先填写至少一道题的答案')
        return
      }
      if ((this.session.questionList || []).some(item => !item.answered && item.studentAnswer && !this.isAutoJudge(item) && item.selfCorrect === null)) {
        this.$message.warning('请先完成所有已填写主观题的自评')
        return
      }
      this.batchSubmitting = true
      try {
        this.session = this.prepareSession(await batchSubmitPracticeAnswer({
          sessionId: this.session.sessionId,
          answerList
        }))
        this.$message.success(`已批量提交 ${answerList.length} 道题`)
        this.loadHistory()
        this.loadStatistics()
      } finally {
        this.batchSubmitting = false
      }
    },
    async finishSession() {
      const currentQuestionList = this.session.questionList || []
      currentQuestionList.forEach(item => this.pauseAnswerTiming(item))
      const unfinishedCount = (this.session.questionList || []).filter(item => !item.answered).length
      if (unfinishedCount > 0) {
        try {
          await this.$confirm(`还有 ${unfinishedCount} 道题未提交，结束后将不再允许作答。`, '确认结束练习', { type: 'warning' })
        } catch (error) {
          return
        }
      }
      this.session = this.prepareSession(await finishPracticeSession({ sessionId: this.session.sessionId }))
      this.$message.success(`练习完成，正确率 ${this.session.accuracyRate || 0}%`)
      this.loadHistory()
      this.loadStatistics()
    },
    prepareSession(session) {
      const next = Object.assign(emptySession(), session || {})
      next.questionList = (next.questionList || []).map(item => Object.assign({ studentAnswer: '', selfCorrect: null, submitting: false, revealing: false, dirty: false, answerStartedAt: null }, item, {
        result: this.answerResultMap[item.sessionQuestionId] || null,
        referenceAnswer: this.referenceAnswerMap[item.sessionQuestionId] || null
      }))
      return next
    },
    isAutoJudge(item) {
      if (item.autoJudge !== undefined && item.autoJudge !== null) return item.autoJudge
      return item.judgeMode === 'AUTO' || ['选择题', '判断题', '填空题', '单选题', '多选题'].includes(item.questionTypeName)
    },
    imageList(item) {
      return [item.imageUrl, item.imageUrl2, item.imageUrl3, item.imageUrl4].filter(Boolean)
    },
    typeLabel(type) {
      return { KNOWLEDGE: '知识点', ERROR_LABEL: '错因', TYPICAL: '典型题' }[type] || type
    },
    sourceLabel(source) {
      return { WRONG_QUESTION: '错题本', QUESTION_BANK: '题库', MIXED: '混合' }[source] || '错题本'
    },
    trendText(days) {
      const trend = this.findTrend(days)
      return trend ? `${trend.accuracyRate || 0}%` : '-'
    },
    trendTip(days) {
      const trend = this.findTrend(days)
      if (!trend) return '暂无数据'
      const delta = Number(trend.accuracyRateDelta || 0)
      const deltaText = delta > 0 ? `+${delta}` : String(delta)
      return `${trend.answeredCount || 0}题 · ${deltaText}%`
    },
    findTrend(days) {
      return (this.statistics.trendList || []).find(item => Number(item.periodDays) === days)
    },
    resultLevel(accuracyRate) {
      const rate = Number(accuracyRate || 0)
      if (rate >= 90) return '掌握稳定'
      if (rate >= 70) return '继续巩固'
      return '需要复习'
    }
  }
}
</script>

<style scoped>
.practice-page { padding: 4px; color: #27314a; }
.practice-hero { display:flex; align-items:center; justify-content:space-between; min-height:132px; padding:26px 34px; margin-bottom:14px; color:#fff; border-radius:12px; background:#4f63df; }
.practice-hero h1 { margin:0 0 8px; font-size:28px; }
.practice-hero p { margin:0; opacity:.86; }
.content-card { border:0; border-radius:12px; }
.stats-grid { display:grid; grid-template-columns:repeat(4, minmax(0, 1fr)); gap:12px; margin-bottom:14px; }
.stat-item { min-height:88px; padding:16px; border:1px solid #edf0f5; border-radius:8px; background:#fff; }
.stat-item span { display:block; color:#7b8499; font-size:13px; }
.stat-item strong { display:block; margin:8px 0 4px; color:#27314a; font-size:24px; line-height:1; }
.stat-item small { color:#8b94a8; }
.filter-bar { display:flex; flex-wrap:wrap; gap:10px; margin-bottom:18px; }
.filter-bar .el-input { width:240px; }
.filter-bar .el-select { width:140px; }.filter-bar .paper-code-input { width:190px; }
.el-pagination { margin-top:18px; text-align:right; }
.session-summary { display:flex; gap:18px; padding:12px 16px; margin-bottom:14px; border-radius:8px; background:#f5f7ff; color:#5363d6; }
.practice-preview { padding:12px 14px; margin:0 0 12px; border:1px solid #dce5ff; border-radius:8px; background:#f5f7ff; color:#5363d6; line-height:1.8; }
.practice-preview strong { margin-right:12px; }
.practice-preview span { margin-right:12px; }
.practice-preview p { margin:2px 0 0; color:#7b8499; }
.practice-preview.is-shortage { border-color:#ffd1d1; background:#fff7f7; color:#d9534f; }
.generation-reason { margin-bottom:14px; }
.source-reason { margin:8px 0 0; color:#7b8499; font-size:13px; }
.judge-tip { margin:12px 0 8px; }
.auto-judge-text { color:#7b8499; font-size:13px; }
.reference-answer { padding:10px 12px; margin-top:10px; border-radius:8px; background:#f8fafc; color:#53606f; line-height:1.7; }
.reference-answer p { margin:0 0 4px; }
.result-panel { display:grid; grid-template-columns:repeat(3, 1fr); gap:10px; margin-bottom:14px; }
.result-panel div { padding:12px; border-radius:8px; background:#f8fafc; }
.result-panel span { display:block; color:#7b8499; font-size:13px; }
.result-panel strong { display:block; margin-top:6px; color:#27314a; }
.wrong-review { padding:12px; margin-bottom:10px; border-radius:8px; background:#fff7f7; }
.wrong-review h4 { margin:0 0 10px; }
.wrong-review .el-tag { margin:0 8px 8px 0; }
.question-card { padding:18px 0; border-bottom:1px solid #edf0f5; }
.question-card:last-child { border-bottom:0; }
.question-head { display:flex; justify-content:space-between; }
.question-card h3 { margin:12px 0; }
.question-content { padding:14px; margin-bottom:12px; border-radius:8px; background:#f7f8fb; white-space:pre-wrap; line-height:1.7; }
.question-images { display:grid; grid-template-columns:repeat(2, 1fr); gap:10px; margin-bottom:12px; }
.question-images .el-image { height:180px; background:#f5f6f8; }
.answer-actions { display:flex; justify-content:space-between; align-items:center; margin:12px 0; }
.answer-review { padding:12px 14px; margin-top:12px; border-radius:8px; background:#f8fafc; line-height:1.7; }
.answer-review p { margin:0 0 6px; }
@media (max-width: 900px) {
  .stats-grid { grid-template-columns:repeat(2, minmax(0, 1fr)); }
  .result-panel { grid-template-columns:1fr; }
}
</style>
