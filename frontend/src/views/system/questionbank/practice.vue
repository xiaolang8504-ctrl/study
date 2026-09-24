<template>
  <div class="app-container similar-practice-page">
    <el-card shadow="never" class="source-card">
      <div class="hero">
        <div><h2>从一道错题，练到真正会做</h2><p>先回到原题独立重做，再做同型变式，最后用不同题型检验知识迁移。</p></div>
        <div><el-button @click="$router.push('/system/similar-practice-history')">练习历史</el-button><el-button v-if="activeLevel !== 'ORIGINAL_REDO'" type="primary" :loading="generating" :disabled="!wrongQuestionId" @click="generate">生成本层题目</el-button></div>
      </div>
      <el-select v-model="wrongQuestionId" filterable placeholder="请选择一道个人错题" class="wrong-select">
        <el-option v-for="item in wrongQuestions" :key="item.id" :label="`${item.subjectName || item.subject} · ${item.questionTitle}`" :value="item.id" />
      </el-select>
      <el-tabs v-model="activeLevel" class="layer-tabs" @tab-click="changeLevel">
        <el-tab-pane label="1. 原题重做" name="ORIGINAL_REDO"><span slot="label">1. 原题重做</span></el-tab-pane>
        <el-tab-pane label="2. 同型变式" name="SAME_PATTERN"><span slot="label">2. 同型变式</span></el-tab-pane>
        <el-tab-pane label="3. 跨情境应用" name="CROSS_CONTEXT"><span slot="label">3. 跨情境应用</span></el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card v-if="activeLevel === 'ORIGINAL_REDO' && wrongQuestionId" shadow="never" class="original-redo-card">
      <el-tag type="primary">第一层 · 原题重做</el-tag>
      <h3>{{ selectedWrongQuestion.questionTitle || '原错题' }}</h3>
      <p>不更换数字、不看答案，先回到这道错题独立写出思路和答案。提交订正或完成复习后，再进入同型变式。</p>
      <el-button type="primary" @click="openOriginalRedo">打开原题独立重做</el-button>
      <el-button @click="activeLevel = 'SAME_PATTERN'; generate()">直接进入同型变式</el-button>
    </el-card>
    <el-empty v-if="generated && questions.length === 0" description="暂无匹配的审核题，请先完善题库和知识点关联" />
    <el-card v-for="(item,index) in questions" :key="item.recommendationId" shadow="hover" class="question-card">
      <div class="question-head"><div><el-tag size="mini">{{ item.recommendationLevelName || '同型变式' }}</el-tag><el-tag size="mini" type="info">第 {{ index + 1 }} 题</el-tag><span class="score">匹配度 {{ Math.round(item.recommendScore) }}%</span></div><el-button type="text" @click="report(item)">题目纠错</el-button></div>
      <h3>{{ item.questionTitle }}</h3><question-content-renderer :content="item.questionContent" :content-format="item.contentFormat" :options-json="item.optionsJson" class="content" />
      <div v-if="imageList(item).length" class="question-images"><el-image v-for="image in imageList(item)" :key="image.fileId || image.imageUrl" :src="image.imageUrl" :preview-src-list="imagePreviewList(item)" fit="contain" /></div>
      <div class="tags"><el-tag v-for="point in item.knowledgePointNames" :key="point" size="mini" type="info">{{ point }}</el-tag><span>难度 {{ item.difficulty }}/5</span><span class="reason"><i class="el-icon-magic-stick" /> {{ item.recommendReason }}</span></div>
      <el-input v-model="item.studentAnswer" :disabled="!!item.result" placeholder="输入你的答案" class="answer-input" @focus="startTiming(item)" />
      <template v-if="!item.result && item.judgeMode === 'AUTO'"><el-button type="primary" size="small" :loading="item.submitting" @click="submit(item)">提交答案</el-button></template>
      <template v-else-if="!item.result && !item.answerView"><el-button type="primary" size="small" :loading="item.submitting" @click="viewAnswer(item)">提交作答并查看标准答案</el-button></template>
      <template v-else-if="!item.result"><el-alert type="info" :closable="false" :title="`标准答案：${item.answerView.correctAnswer}`"><div v-if="item.answerView.analysis">解析：{{ item.answerView.analysis }}</div></el-alert><div class="self-actions"><span>对照答案后，请评价本次作答：</span><el-button type="success" size="small" :loading="item.submitting" @click="submit(item,true)">自评正确</el-button><el-button type="warning" size="small" :loading="item.submitting" @click="submit(item,false)">自评错误</el-button></div></template>
      <el-alert v-else :type="item.result.correct ? 'success' : 'error'" :title="item.result.correct ? '回答正确' : `回答错误，正确答案：${item.result.correctAnswer}`" :closable="false" show-icon>
        <div v-if="item.result.analysis">解析：{{ item.result.analysis }}</div>
        <div v-if="item.result.autoCollectedWrongQuestion" class="collect-tip">
          已自动加入错题本，可完成订正后进入智能复习。
          <el-button type="text" @click="$router.push('/system/wrongquestion')">去订正</el-button>
        </div>
      </el-alert>
      <el-button v-if="item.result && item.result.judgeType === 'SELF' && !item.appealed" type="text" class="appeal-button" @click="appeal(item)">对自评结果有异议，申请教师复核</el-button>
      <el-tag v-if="item.appealed" size="mini" type="warning">已申请教师复核</el-tag>
    </el-card>
  </div>
</template>

<script>
import { wrongQuestionPageList } from '@/api/system/wrongQuestion'
import { createQuestionPracticeAppeal, reportQuestion, similarQuestionList, submitQuestionPractice, viewQuestionPracticeAnswer } from '@/api/system/questionBank'
import QuestionContentRenderer from '@/components/QuestionContentRenderer.vue'

export default {
  name: 'SimilarPractice',
  components: { QuestionContentRenderer },
  data() { return { wrongQuestions: [], wrongQuestionId: null, activeLevel: 'ORIGINAL_REDO', questions: [], generating: false, generated: false } },
  computed: {
    selectedWrongQuestion() { return this.wrongQuestions.find(item => Number(item.id) === Number(this.wrongQuestionId)) || {} }
  },
  watch: {
    wrongQuestionId() {
      this.questions = []
      this.generated = false
      if (this.activeLevel !== 'ORIGINAL_REDO') this.generate()
    }
  },
  created() {
    wrongQuestionPageList({ current: 1, pageSize: 100 }).then(data => {
      this.wrongQuestions = data.list || []
      const queryWrongQuestionId = this.$route.query.wrongQuestionId ? Number(this.$route.query.wrongQuestionId) : null
      const matched = queryWrongQuestionId && this.wrongQuestions.some(item => Number(item.id) === queryWrongQuestionId)
      if (matched) {
        this.wrongQuestionId = queryWrongQuestionId
      } else if (this.wrongQuestions.length) {
        this.wrongQuestionId = this.wrongQuestions[0].id
      }
    })
  },
  methods: {
    changeLevel() { this.questions = []; this.generated = false; if (this.activeLevel !== 'ORIGINAL_REDO' && this.wrongQuestionId) this.generate() },
    generate() {
      if (!this.wrongQuestionId || this.activeLevel === 'ORIGINAL_REDO') return
      this.generating = true; this.generated = false
      similarQuestionList({ wrongQuestionId: this.wrongQuestionId, limit: 5, recommendationLevel: this.activeLevel }).then(data => { this.questions = (data || []).map(item => Object.assign(item, { studentAnswer: '', submitting: false, result: null, answerView: null, appealed: false, startTime: null })); this.generated = true }).finally(() => { this.generating = false })
    },
    openOriginalRedo() { this.$router.push({ path: '/system/wrongquestion/detail', query: { id: this.wrongQuestionId, redo: '1' } }) },
    imageList(item) { if (item.images && item.images.length) return item.images; return (item.imageUrls || '').split(',').map(url => url.trim()).filter(Boolean).map(url => ({ imageUrl: url })) },
    imagePreviewList(item) { return this.imageList(item).map(image => image.imageUrl).filter(Boolean) },
    startTiming(item) { if (!item.startTime) item.startTime = Date.now() },
    submit(item, selfCorrect) { if (!item.studentAnswer.trim()) { this.$message.warning('请先输入答案'); return } item.submitting = true; const seconds = item.startTime ? Math.round((Date.now() - item.startTime) / 1000) : null; submitQuestionPractice({ recommendationId: item.recommendationId, studentAnswer: item.studentAnswer, durationSeconds: seconds, selfCorrect }).then(data => { item.result = data; if (data.correct) { this.$message.success('回答正确') } else if (data.autoCollectedWrongQuestion) { this.$message.warning('回答错误，已自动加入错题本') } else { this.$message.warning('再看看解析') } }).finally(() => { item.submitting = false }) },
    viewAnswer(item) { if (!item.studentAnswer.trim()) { this.$message.warning('请先完成作答'); return } item.submitting = true; const seconds = item.startTime ? Math.round((Date.now() - item.startTime) / 1000) : null; viewQuestionPracticeAnswer({ recommendationId: item.recommendationId, studentAnswer: item.studentAnswer, durationSeconds: seconds }).then(data => { item.answerView = data }).finally(() => { item.submitting = false }) },
    appeal(item) { this.$prompt('请说明需要教师复核的原因', '申请教师复核', { inputValidator: value => Boolean(value && value.trim()) || '请输入申诉原因' }).then(({ value }) => createQuestionPracticeAppeal({ recommendationId: item.recommendationId, appealReason: value })).then(() => { item.appealed = true; this.$message.success('申诉已提交，等待教师复核') }).catch(() => {}) },
    report(item) { this.$prompt('请描述题目问题', '题目纠错', { inputPlaceholder: '例如：答案错误、题干不完整、解析有误' }).then(({ value }) => reportQuestion({ bankQuestionId: item.questionId, reportType: 'CONTENT_ERROR', reportContent: value })).then(() => this.$message.success('已提交，感谢反馈')).catch(() => {}) }
  }
}
</script>

<style scoped>
.source-card { margin-bottom:20px; }.hero { display:flex; justify-content:space-between; align-items:center; }.hero h2 { margin:0 0 8px; color:#1f2937; }.hero p { margin:0 0 18px; color:#6b7280; }.wrong-select { width:520px; max-width:100%; }.layer-tabs { margin-top:18px; }.original-redo-card { margin-bottom:16px; background:#f3f7ff; }.original-redo-card h3 { margin:12px 0 8px; }.original-redo-card p { max-width:760px; color:#606266; line-height:1.8; }.question-card { margin-bottom:16px; }.question-head { display:flex; justify-content:space-between; }.question-head .el-tag + .el-tag { margin-left:6px; }.score { margin-left:12px; color:#67c23a; font-size:13px; }.content { line-height:1.8; white-space:pre-wrap; }.latex-content { background:#f5f7fa; padding:12px; border-radius:5px; }.question-images { display:flex; gap:10px; margin:12px 0; }.question-images .el-image { width:180px; height:120px; border:1px solid #ebeef5; }.options { margin:14px 0; line-height:2; }.tags { display:flex; flex-wrap:wrap; gap:6px; align-items:center; color:#909399; font-size:13px; margin:14px 0; }.reason { color:#409eff; }.answer-input { width:70%; margin-right:10px; }
.self-actions { margin-top:12px; display:flex; align-items:center; gap:8px; }.appeal-button { margin-top:8px; }.collect-tip { margin-top:8px; }
</style>
