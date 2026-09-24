<template>
  <section v-loading="loading" class="wrong-question-detail-page">
    <div class="page-header">
      <div>
        <el-button type="text" icon="el-icon-arrow-left" @click="backToList">返回我的错题本</el-button>
        <h1>{{ detail.questionTitle || '错题详情' }}</h1>
        <p>完成订正后，系统会将题目加入后续复习计划。</p>
      </div>
      <div class="header-actions">
        <el-tag :type="statusType">{{ statusLabel }}</el-tag>
        <el-button :loading="duplicateScanning" @click="scanDuplicates">检查重复题</el-button>
        <el-button :disabled="!canCorrect" type="primary" @click="focusCorrection">提交订正</el-button>
        <el-button :disabled="detail.status !== 1" @click="startReview">开始复习</el-button>
        <el-button :disabled="detail.status === 0" @click="addToPracticeBasket">加入组卷篮重练</el-button>
      </div>
    </div>

    <el-alert
      v-if="detail.status === 0"
      title="请先完成订正，再开始复习。"
      type="warning"
      :closable="false"
      show-icon
    />
    <el-alert
      v-else-if="detail.status === 1"
      title="订正已提交，下一步请在复习中独立作答；不能通过手动操作标记为已掌握。"
      type="info"
      :closable="false"
      show-icon
    />
    <el-alert
      v-else-if="detail.status === 2"
      title="该题已掌握，可在列表页归档。"
      type="success"
      :closable="false"
      show-icon
    />

    <el-row :gutter="20" class="content-row">
      <el-col :lg="15" :md="14" :xs="24">
        <el-card shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>原题与诊断</span><small>{{ detail.subjectName || detail.subject }} · {{ detail.questionTypeName || detail.questionType }}</small></div>
          <div class="tag-row">
            <el-tag v-for="point in detail.knowledgePointNames || []" :key="point" size="mini">{{ point }}</el-tag>
            <el-tag v-for="label in errorLabels" :key="label" size="mini" type="warning">{{ label }}</el-tag>
            <el-tag size="mini" type="info">能力：{{ abilityLevelLabel }}</el-tag>
          </div>
          <h2>{{ detail.questionTitle || '未命名题目' }}</h2>
          <question-content-renderer :content="detail.questionContent" :content-format="detail.contentFormat" :options-json="detail.optionsJson" class="question-content" empty-text="请根据原题图片完成订正。" />
          <div v-if="imageUrls.length" class="image-grid">
            <figure v-for="image in imageUrls" :key="image.label">
              <figcaption>{{ image.label }}</figcaption>
              <el-image :src="image.url" :preview-src-list="imagePreviewUrls" fit="contain" />
            </figure>
          </div>
          <section v-if="hasCapturePosition && imageUrls.length" class="source-position-panel">
            <div class="source-position-title">原试卷定位 <small>第 {{ detail.captureSourcePageNo || 1 }} 页</small></div>
            <div class="source-position-canvas"><img :src="imageUrls[0].url" alt="原试卷"><span class="source-position-box" :style="captureRegionStyle">本题</span></div>
          </section>
          <el-divider />
          <div class="diagnosis-grid">
            <div><b>我的原答案</b><p>{{ detail.wrongAnswer || '未记录' }}</p></div>
            <div><b>当前独立思路</b><p>{{ correctionForm.thinking || '尚未填写' }}</p></div>
            <div><b>当前自述错因</b><p>{{ correctionForm.errorReason || detail.wrongReason || '尚未填写' }}</p></div>
          </div>
          <div class="layer-actions"><span>需要时逐层查看：</span><el-button v-for="layer in layerDefinitions" :key="layer.key" size="mini" :disabled="!canRevealLayer(layer.key)" :loading="revealingLayer === layer.key" @click="revealLayer(layer)">{{ layer.title }}</el-button></div>
          <section v-for="layer in revealedLayers" :key="layer.layer" class="answer-panel"><b>{{ layer.title }}</b><p>{{ layer.content }}</p></section>
          <div v-if="hasRevealedAnalysis" class="same-point-practice">
            <div><b>解析看懂后，再练一题同知识点</b><p>{{ detail.learningPoint || (detail.knowledgePointNames || []).join('、') || '请先为该题补充知识点' }}</p></div>
            <el-button type="primary" size="mini" :disabled="!practiceLearningPoint" @click="startSamePointPractice">去同知识点重练</el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="card-block" id="correction-form">
          <div slot="header" class="card-title"><span>订正</span><small>提交会留下订正记录并启动复习计划</small></div>
          <el-form ref="correctionForm" :model="correctionForm" :rules="correctionRules" label-width="90px">
            <el-form-item label="独立思路"><el-input v-model="correctionForm.thinking" type="textarea" :rows="3" :disabled="!canCorrect" placeholder="先写已知条件、准备使用的方法和推导思路" /></el-form-item>
            <el-form-item label="我的错因"><el-input v-model="correctionForm.errorReason" type="textarea" :rows="2" :disabled="!canCorrect" placeholder="先用自己的话说明错在哪里" /></el-form-item>
            <el-form-item label="订正答案" prop="correctionAnswer">
              <el-input v-model="correctionForm.correctionAnswer" type="textarea" :rows="3" :disabled="!canCorrect" placeholder="先独立写下正确答案或关键步骤" />
            </el-form-item>
            <el-form-item label="订正解析">
              <el-input v-model="correctionForm.correctionAnalysis" type="textarea" :rows="4" :disabled="!canCorrect" placeholder="记录本题为什么错、正确思路是什么" />
            </el-form-item>
            <el-form-item label="订正图片">
              <el-upload action="" :show-file-list="false" :disabled="!canCorrect" :http-request="uploadCorrectionImage" :before-upload="beforeImageUpload">
                <el-button :disabled="!canCorrect" icon="el-icon-upload2">上传订正图片</el-button>
              </el-upload>
              <el-image v-if="correctionImageUrl" :src="correctionImageUrl" :preview-src-list="[correctionImageUrl]" fit="contain" class="correction-image" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="correctionForm.correctionRemark" type="textarea" :rows="2" :disabled="!canCorrect" />
            </el-form-item>
            <el-form-item>
              <el-button :disabled="!canCorrect" type="primary" :loading="submitting" @click="submitCorrection">提交订正</el-button>
              <span v-if="draftSaving" class="form-tip">草稿保存中…</span><span v-else-if="draftSavedAt" class="form-tip">草稿已保存 {{ draftSavedAt }}</span>
              <span v-if="!canCorrect" class="form-tip">已掌握或已归档题目不能新增订正记录。</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :lg="9" :md="10" :xs="24">
        <el-card shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>学习信息</span></div>
          <dl class="summary-list">
            <dt>年级 / 科目</dt><dd>{{ detail.gradeName || detail.grade || '-' }} / {{ detail.subjectName || detail.subject || '-' }}</dd>
            <dt>来源</dt><dd>{{ detail.sourceName || detail.source || '-' }}</dd>
            <dt>知识点</dt><dd>{{ (detail.knowledgePointNames || []).join('、') || detail.learningPoint || '-' }}</dd>
            <dt>录入时间</dt><dd>{{ detail.createTime || '-' }}</dd>
            <dt>最近更新</dt><dd>{{ detail.updateTime || '-' }}</dd>
          </dl>
        </el-card>
        <el-card shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>历次订正</span><small>{{ records.length }} 个版本</small></div>
          <el-timeline v-if="records.length">
            <el-timeline-item v-for="record in records" :key="record.id" :timestamp="record.createTime" type="success">
              <p><b>第 {{ record.revisionNo || 1 }} 次订正</b></p>
              <p v-if="record.thinking" class="record-line"><span>独立思路：</span>{{ record.thinking }}</p>
              <p v-if="record.errorReason" class="record-line"><span>自述错因：</span>{{ record.errorReason }}</p>
              <p class="record-line"><span>订正答案：</span>{{ record.correctionAnswer || '未填写文字答案' }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="尚未提交订正" :image-size="56" />
        </el-card>
        <el-card shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>同题来源</span><small>{{ occurrenceList.length }} 次错误</small></div>
          <el-timeline v-if="occurrenceList.length"><el-timeline-item v-for="item in occurrenceList" :key="item.id" :timestamp="item.occurredAt"><p>{{ item.sourceName || item.source || '未设置来源' }}<template v-if="item.captureSourcePageNo"> · 第 {{ item.captureSourcePageNo }} 页</template></p><p class="muted">错答：{{ item.wrongAnswer || '未记录' }}</p></el-timeline-item></el-timeline>
          <el-empty v-else description="暂无来源记录" :image-size="56" />
        </el-card>
        <el-card v-if="duplicateList.length" shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>重复与相似题</span><small>相似题不会自动合并</small></div>
          <div v-for="item in duplicateList" :key="item.relationId" class="duplicate-item"><div><b>{{ item.questionTitle || `错题 #${item.questionId}` }}</b><p>{{ duplicateLabel(item.matchType) }} · 相似度 {{ item.similarityScore }}%</p></div><el-button v-if="item.status === 'MERGED'" type="text" @click="undoMerge(item)">撤销归并</el-button><el-button v-else-if="item.matchType !== 'SIMILAR'" type="text" @click="mergeDuplicate(item)">归并来源</el-button><el-tag v-else size="mini" type="info">仅关联</el-tag></div>
        </el-card>
        <el-card shadow="never" class="card-block">
          <div slot="header" class="card-title"><span>学习时间线</span></div>
          <el-timeline v-if="timelineList.length">
            <el-timeline-item v-for="item in timelineList" :key="`${item.eventType}-${item.id}-${item.createTime}`" :timestamp="item.createTime" type="primary">
              <p><b>{{ timelineLabel(item.eventType) }}</b> <el-tag size="mini" type="info">{{ timelineSourceLabel(item.eventSource) }}</el-tag></p>
              <p v-if="item.eventContent">{{ item.eventContent }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="还没有学习记录" :image-size="72" />
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script>
import { filePolicy, uploadFile, downloadUrl } from '@/api/system/file'
import { mergeWrongQuestionDuplicate, revealAnswerLayer, saveCorrectionDraft, scanWrongQuestionDuplicate, submitCorrectionRecord, undoWrongQuestionMerge, wrongQuestionDetail } from '@/api/system/wrongQuestion'
import QuestionContentRenderer from '@/components/QuestionContentRenderer.vue'

const WRONG_QUESTION_UPLOAD_TYPE = 'wrongQuestion'

export default {
  name: 'WrongQuestionDetail',
  components: { QuestionContentRenderer },
  data() {
    return {
      loading: false,
      submitting: false,
      draftSaving: false,
      draftSavedAt: '',
      draftReady: false,
      draftTimer: null,
      draftPromise: null,
      duplicateScanning: false,
      duplicateList: [],
      revealedLayers: [],
      revealingLayer: '',
      layerDefinitions: [{ key: 'KEY_HINT', title: '关键提示' }, { key: 'STEPS', title: '解题步骤' }, { key: 'COMMON_MISTAKE', title: '易错点' }, { key: 'ANALYSIS', title: '完整解析' }, { key: 'ANSWER', title: '参考答案' }],
      detail: {},
      imageUrls: [],
      correctionImageUrl: '',
      correctionForm: { wrongQuestionId: null, thinking: '', errorReason: '', correctionAnswer: '', correctionAnalysis: '', correctionImageUrl: '', correctionRemark: '' },
      correctionRules: { correctionAnswer: [{ required: true, message: '请填写订正答案', trigger: 'blur' }] }
    }
  },
  computed: {
    statusLabel() {
      return ({ 0: '待订正', 1: '已订正，待复习', 2: '已掌握', 3: '已归档' })[this.detail.status] || '未知状态'
    },
    statusType() {
      return ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' })[this.detail.status] || 'info'
    },
    canCorrect() {
      return this.detail.status === 0 || this.detail.status === 1
    },
    records() {
      return this.detail.correctionRecordList || []
    },
    timelineList() {
      const timeline = this.detail.timelineList || []
      if (timeline.length) return timeline
      return this.records.map(record => ({ id: record.id, eventType: 'CORRECTION_SUBMITTED', eventSource: 'STUDENT', eventContent: record.correctionAnswer || '已提交订正', createTime: record.createTime }))
    },
    occurrenceList() { return this.detail.occurrenceList || [] },
    errorLabels() {
      return String(this.detail.errorLabels || '').split(/[,，]/).map(item => item.trim()).filter(Boolean)
    },
    abilityLevelLabel() {
      return ({ FOUNDATION: '基础', APPLICATION: '应用', COMPREHENSIVE: '综合' })[this.detail.abilityLevel] || '未标注'
    },
    imagePreviewUrls() {
      return this.imageUrls.map(item => item.url)
    },
    hasCapturePosition() {
      return Number(this.detail.captureWidth) > 0 && Number(this.detail.captureHeight) > 0
    },
    captureRegionStyle() {
      return { left: `${Number(this.detail.captureLeftPosition || 0) / 100}%`, top: `${Number(this.detail.captureTopPosition || 0) / 100}%`, width: `${Number(this.detail.captureWidth || 0) / 100}%`, height: `${Number(this.detail.captureHeight || 0) / 100}%` }
    },
    hasRevealedAnalysis() {
      return this.revealedLayers.some(item => ['ANALYSIS', 'ANSWER'].includes(item.layer))
    },
    practiceLearningPoint() {
      return this.detail.learningPoint || (this.detail.knowledgePointNames || [])[0] || ''
    }
  },
  watch: {
    '$route.query.id': {
      immediate: true,
      handler() { this.loadDetail() }
    },
    correctionForm: {
      deep: true,
      handler() {
        if (!this.draftReady || !this.canCorrect) return
        clearTimeout(this.draftTimer)
        this.draftTimer = setTimeout(this.persistDraft, 900)
      }
    }
  },
  beforeDestroy() { clearTimeout(this.draftTimer) },
  methods: {
    loadDetail() {
      const id = Number(this.$route.query.id)
      if (!Number.isFinite(id) || id <= 0) {
        this.$message.error('缺少有效的错题编号')
        this.backToList()
        return
      }
      this.loading = true
      this.draftReady = false
      wrongQuestionDetail({ id }).then(data => {
        this.detail = data || {}
        this.correctionForm = Object.assign({ wrongQuestionId: this.detail.id, thinking: '', errorReason: '', correctionAnswer: '', correctionAnalysis: '', correctionImageUrl: '', correctionRemark: '' }, this.detail.correctionDraft || {}, { wrongQuestionId: this.detail.id })
        this.correctionImageUrl = ''
        this.revealedLayers = []
        return Promise.all([this.resolveImages(), this.resolveFileUrl(this.correctionForm.correctionImageUrl).then(url => { this.correctionImageUrl = url })])
      }).catch(() => {}).finally(() => { this.loading = false; this.$nextTick(() => { this.draftReady = true }) })
    },
    resolveImages() {
      if (Array.isArray(this.detail.assetList) && this.detail.assetList.length) {
        return Promise.all(this.detail.assetList.map(asset => {
          const source = asset.imageUrl
            ? Promise.resolve(asset.imageUrl)
            : (asset.fileId ? downloadUrl({ fileId: asset.fileId, uploadType: asset.uploadType || WRONG_QUESTION_UPLOAD_TYPE }).catch(() => '') : Promise.resolve(''))
          return source.then(url => url ? this.cropAsset(url, asset).then(cropped => ({ label: asset.label || '题目素材', url: cropped })) : null)
        })).then(images => { this.imageUrls = images.filter(Boolean) })
      }
      const fields = [
        { key: 'imageUrl', label: this.isChoiceQuestion() ? '选项 A' : '原题图片' },
        { key: 'imageUrl2', label: '选项 B' }, { key: 'imageUrl3', label: '选项 C' }, { key: 'imageUrl4', label: '选项 D' }
      ]
      return Promise.all(fields.filter(item => this.detail[item.key]).map(item => this.resolveFileUrl(this.detail[item.key])
        .then(url => url ? { label: item.label, url } : null))).then(images => { this.imageUrls = images.filter(Boolean) })
    },
    isChoiceQuestion() {
      return ['选择题', '单选题', '多选题'].includes(this.detail.questionTypeName || this.detail.questionType)
    },
    resolveFileUrl(fileId) {
      if (!fileId) return Promise.resolve('')
      const id = Number(fileId)
      if (!Number.isFinite(id)) return Promise.resolve(fileId || '')
      return downloadUrl({ fileId: id, uploadType: WRONG_QUESTION_UPLOAD_TYPE }).catch(() => '')
    },
    cropAsset(url, asset) {
      if (!['QUESTION_CROP', 'CLEANED_QUESTION_CROP'].includes(asset.assetType) || !asset.width || !asset.height) return Promise.resolve(url)
      return new Promise(resolve => {
        const image = new Image()
        image.crossOrigin = 'anonymous'
        image.onload = () => {
          try {
            const sx = Math.max(0, Math.round(image.naturalWidth * Number(asset.leftPosition || 0) / 10000))
            const sy = Math.max(0, Math.round(image.naturalHeight * Number(asset.topPosition || 0) / 10000))
            const sw = Math.max(1, Math.min(image.naturalWidth - sx, Math.round(image.naturalWidth * Number(asset.width) / 10000)))
            const sh = Math.max(1, Math.min(image.naturalHeight - sy, Math.round(image.naturalHeight * Number(asset.height) / 10000)))
            const scale = Math.min(1, 1600 / Math.max(sw, sh))
            const canvas = document.createElement('canvas')
            canvas.width = Math.max(1, Math.round(sw * scale)); canvas.height = Math.max(1, Math.round(sh * scale))
            canvas.getContext('2d').drawImage(image, sx, sy, sw, sh, 0, 0, canvas.width, canvas.height)
            resolve(canvas.toDataURL('image/png'))
          } catch (error) { resolve(url) }
        }
        image.onerror = () => resolve(url)
        image.src = url
      })
    },
    focusCorrection() {
      document.getElementById('correction-form').scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    startReview() {
      this.$router.push({ path: '/system/review/today', query: this.detail.subject ? { subject: this.detail.subject } : {} })
    },
    startSamePointPractice() {
      if (!this.practiceLearningPoint) return
      const query = { learningPoint: this.practiceLearningPoint }
      if (this.detail.subject) query.subject = this.detail.subject
      this.$router.push({ path: '/system/review', query })
    },
    addToPracticeBasket() { this.$router.push({ path: '/system/review', query: { basket: String(this.detail.id) } }) },
    backToList() { this.$router.push('/system/wrongquestion') },
    timelineLabel(eventType) { return ({ CREATED: '录入错题', CAPTURE_CONFIRMED: '确认采集题块', CORRECTION_SUBMITTED: '提交订正', HINT_REVEALED: '查看关键提示', STEPS_REVEALED: '查看解题步骤', COMMON_MISTAKE_REVEALED: '查看易错点', ANALYSIS_REVEALED: '查看完整解析', ANSWER_REVEALED: '查看答案', DUPLICATE_MERGED: '归并重复题', DUPLICATE_MERGE_REVERSED: '撤销题目归并', PAPER_PRACTICE_WRONG: '纸面重练错误', REVIEW_FEEDBACK: '提交复习反馈', STATUS_CHANGED: '状态变更' })[eventType] || '学习动作' },
    timelineSourceLabel(source) { return ({ STUDENT: '学生', OCR: 'OCR', AI: 'AI', SYSTEM: '系统', GUARDIAN: '家长' })[source] || source || '系统' },
    beforeImageUpload(file) {
      const valid = file.type && file.type.startsWith('image/') && file.size / 1024 / 1024 <= 10
      if (!valid) this.$message.error('请上传不超过 10MB 的图片文件')
      return valid
    },
    uploadCorrectionImage(options) {
      filePolicy({ uploadType: WRONG_QUESTION_UPLOAD_TYPE }).then(policy => {
        const formData = new FormData()
        formData.append('file', options.file)
        formData.append('signature', policy.signature)
        formData.append('fileName', options.file.name)
        return uploadFile(formData)
      }).then(file => {
        this.correctionForm.correctionImageUrl = String(file.id)
        return this.resolveFileUrl(file.id)
      }).then(url => {
        this.correctionImageUrl = url
        this.$message.success('订正图片上传成功')
      }).catch(() => {})
    },
    submitCorrection() {
      this.$refs.correctionForm.validate(valid => {
        if (!valid) return
        clearTimeout(this.draftTimer)
        this.draftReady = false
        this.submitting = true
        const pendingDraft = this.draftPromise || Promise.resolve()
        pendingDraft.catch(() => {}).then(() => submitCorrectionRecord(this.correctionForm)).then(() => {
          this.$message.success('订正已提交，已加入复习计划')
          this.loadDetail()
        }).catch(() => { this.draftReady = true }).finally(() => { this.submitting = false })
      })
    },
    persistDraft() {
      if (!this.correctionForm.wrongQuestionId) return
      this.draftSaving = true
      const promise = saveCorrectionDraft(this.correctionForm).then(() => {
        this.draftSavedAt = new Date().toLocaleTimeString('zh-CN', { hour12: false })
      }).catch(() => {}).finally(() => {
        this.draftSaving = false
        if (this.draftPromise === promise) this.draftPromise = null
      })
      this.draftPromise = promise
      return promise
    },
    canRevealLayer(key) {
      const index = this.layerDefinitions.findIndex(item => item.key === key)
      return index === 0 || this.revealedLayers.some(item => item.layer === this.layerDefinitions[index - 1].key)
    },
    revealLayer(layer) {
      if (this.revealedLayers.some(item => item.layer === layer.key)) return
      this.revealingLayer = layer.key
      revealAnswerLayer({ wrongQuestionId: this.detail.id, layer: layer.key }).then(data => { this.revealedLayers.push(data) }).catch(() => {}).finally(() => { this.revealingLayer = '' })
    },
    scanDuplicates() {
      this.duplicateScanning = true
      scanWrongQuestionDuplicate({ id: this.detail.id }).then(rows => { this.duplicateList = rows || []; if (!this.duplicateList.length) this.$message.success('未发现重复或高相似错题') }).catch(() => {}).finally(() => { this.duplicateScanning = false })
    },
    duplicateLabel(type) { return ({ EXACT: '完全相同', SAME_QUESTION_DIFFERENT_SOURCE: '同题不同试卷', SIMILAR: '内容相似' })[type] || type },
    mergeDuplicate(item) {
      this.$confirm('归并后会保留两次错答和原卷定位，另一条错题将从列表隐藏。', '确认归并', { type: 'warning' }).then(() => mergeWrongQuestionDuplicate({ relationId: item.relationId, keepQuestionId: this.detail.id })).then(() => { this.$message.success('重复题来源已归并'); this.loadDetail(); this.scanDuplicates() }).catch(() => {})
    },
    undoMerge(item) {
      undoWrongQuestionMerge({ id: item.relationId }).then(() => { this.$message.success('已撤销归并'); this.loadDetail(); this.scanDuplicates() }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.wrong-question-detail-page { max-width: 1280px; margin: 0 auto; padding: 8px; color: #303133; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-header h1 { margin: 6px 0; font-size: 24px; }.page-header p,.muted,.form-tip { color: #909399; font-size: 13px; }.header-actions { display: flex; align-items: center; gap: 10px; padding-top: 20px; }
.content-row { margin-top: 20px; }.card-block { margin-bottom: 20px; }.card-title { display: flex; justify-content: space-between; align-items: baseline; }.card-title small { color: #909399; font-weight: normal; }
.tag-row .el-tag { margin: 0 6px 8px 0; }.question-content { min-height: 72px; line-height: 1.9; white-space: pre-wrap; word-break: break-word; }.answer-panel { padding: 14px 16px; border-radius: 4px; background: #f7f9fc; line-height: 1.7; }
.detail-options { padding-left:24px; line-height:1.9; }
.diagnosis-grid { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:10px; }.diagnosis-grid > div { min-height:90px; padding:12px; border-radius:6px; background:#f7f9fc; }.diagnosis-grid p { margin:8px 0 0; line-height:1.6; white-space:pre-wrap; }.layer-actions { display:flex; align-items:center; gap:8px; flex-wrap:wrap; margin:16px 0 10px; color:#606266; }.answer-panel + .answer-panel { margin-top:8px; }.same-point-practice { display:flex; align-items:center; justify-content:space-between; gap:14px; padding:12px 16px; margin-top:12px; border-radius:6px; background:#f0f7ff; }.same-point-practice p { margin:5px 0 0; color:#6990ad; font-size:12px; }
.image-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 14px; margin-top: 18px; }.image-grid figure { margin: 0; }.image-grid figcaption { margin-bottom: 6px; color: #606266; font-size: 13px; }.image-grid .el-image,.correction-image { width: 100%; max-height: 420px; border: 1px solid #ebeef5; border-radius: 4px; }.correction-image { display: block; width: 220px; margin-top: 10px; }
.source-position-panel { margin-top: 18px; }.source-position-title { margin-bottom: 8px; font-size: 14px; font-weight: 600; }.source-position-title small { color: #909399; font-weight: normal; }.source-position-canvas { position: relative; max-width: 720px; line-height: 0; }.source-position-canvas img { display: block; width: 100%; border: 1px solid #ebeef5; border-radius: 4px; }.source-position-box { position: absolute; box-sizing: border-box; border: 3px solid #f56c6c; background: rgba(245, 108, 108, .1); color: #fff; font-size: 12px; line-height: 20px; text-align: center; }
.summary-list { display: grid; grid-template-columns: 92px 1fr; gap: 14px 8px; margin: 0; font-size: 14px; }.summary-list dt { color: #909399; }.summary-list dd { margin: 0; word-break: break-word; }.form-tip { margin-left: 12px; }
.duplicate-item { display:flex; align-items:center; justify-content:space-between; gap:10px; padding:10px 0; border-bottom:1px solid #ebeef5; }.duplicate-item:last-child { border-bottom:0; }.duplicate-item p { margin:4px 0 0; color:#909399; font-size:12px; }
.record-line { margin:5px 0; color:#606266; line-height:1.6; white-space:pre-wrap; }.record-line span { color:#909399; }
@media (max-width: 768px) { .page-header { display: block; }.header-actions { padding-top: 8px; flex-wrap: wrap; }.diagnosis-grid { grid-template-columns:1fr; } }
</style>
