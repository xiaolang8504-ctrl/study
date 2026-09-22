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
        <el-button :disabled="!canCorrect" type="primary" @click="focusCorrection">提交订正</el-button>
        <el-button :disabled="detail.status !== 1" @click="startReview">开始复习</el-button>
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
          </div>
          <h2>{{ detail.questionTitle || '未命名题目' }}</h2>
          <div class="question-content" v-html="safeQuestionContent" />
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
          <el-button v-if="!answerVisible" type="text" @click="answerVisible = true">查看原题答案与解析</el-button>
          <div v-else class="answer-panel">
            <p><b>我的原答案：</b>{{ detail.wrongAnswer || '未记录' }}</p>
            <p><b>参考答案：</b>{{ detail.correctAnswer || '未记录' }}</p>
            <p><b>原错误原因：</b>{{ detail.wrongReason || '未记录' }}</p>
            <p><b>题目解析：</b>{{ detail.analysis || '未记录' }}</p>
          </div>
        </el-card>

        <el-card shadow="never" class="card-block" id="correction-form">
          <div slot="header" class="card-title"><span>订正</span><small>提交会留下订正记录并启动复习计划</small></div>
          <el-form ref="correctionForm" :model="correctionForm" :rules="correctionRules" label-width="90px">
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
import { wrongQuestionDetail, submitCorrectionRecord } from '@/api/system/wrongQuestion'

const WRONG_QUESTION_UPLOAD_TYPE = 'wrongQuestion'

export default {
  name: 'WrongQuestionDetail',
  data() {
    return {
      loading: false,
      submitting: false,
      answerVisible: false,
      detail: {},
      imageUrls: [],
      correctionImageUrl: '',
      correctionForm: { wrongQuestionId: null, correctionAnswer: '', correctionAnalysis: '', correctionImageUrl: '', correctionRemark: '' },
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
    errorLabels() {
      return String(this.detail.errorLabels || '').split(/[,，]/).map(item => item.trim()).filter(Boolean)
    },
    imagePreviewUrls() {
      return this.imageUrls.map(item => item.url)
    },
    safeQuestionContent() {
      const container = document.createElement('div')
      container.innerHTML = this.detail.questionContent || '请根据原题图片完成订正。'
      container.querySelectorAll('script, style, iframe, object, embed').forEach(node => node.remove())
      container.querySelectorAll('*').forEach(node => Array.from(node.attributes).forEach(attribute => {
        if (/^on/i.test(attribute.name) || /javascript:/i.test(attribute.value)) node.removeAttribute(attribute.name)
      }))
      return container.innerHTML
    },
    hasCapturePosition() {
      return Number(this.detail.captureWidth) > 0 && Number(this.detail.captureHeight) > 0
    },
    captureRegionStyle() {
      return { left: `${Number(this.detail.captureLeftPosition || 0) / 100}%`, top: `${Number(this.detail.captureTopPosition || 0) / 100}%`, width: `${Number(this.detail.captureWidth || 0) / 100}%`, height: `${Number(this.detail.captureHeight || 0) / 100}%` }
    }
  },
  watch: {
    '$route.query.id': {
      immediate: true,
      handler() { this.loadDetail() }
    }
  },
  methods: {
    loadDetail() {
      const id = Number(this.$route.query.id)
      if (!Number.isFinite(id) || id <= 0) {
        this.$message.error('缺少有效的错题编号')
        this.backToList()
        return
      }
      this.loading = true
      wrongQuestionDetail({ id }).then(data => {
        this.detail = data || {}
        this.correctionForm = { wrongQuestionId: this.detail.id, correctionAnswer: '', correctionAnalysis: '', correctionImageUrl: '', correctionRemark: '' }
        this.correctionImageUrl = ''
        return this.resolveImages()
      }).catch(() => {}).finally(() => { this.loading = false })
    },
    resolveImages() {
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
      const id = Number(fileId)
      if (!Number.isFinite(id)) return Promise.resolve(fileId || '')
      return downloadUrl({ fileId: id, uploadType: WRONG_QUESTION_UPLOAD_TYPE }).catch(() => '')
    },
    focusCorrection() {
      document.getElementById('correction-form').scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    startReview() {
      this.$router.push({ path: '/system/review/today', query: this.detail.subject ? { subject: this.detail.subject } : {} })
    },
    backToList() { this.$router.push('/system/wrongquestion') },
    timelineLabel(eventType) { return ({ CREATED: '录入错题', CAPTURE_CONFIRMED: '确认采集题块', CORRECTION_SUBMITTED: '提交订正', ANSWER_REVEALED: '查看答案', REVIEW_FEEDBACK: '提交复习反馈', STATUS_CHANGED: '状态变更' })[eventType] || '学习动作' },
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
        this.submitting = true
        submitCorrectionRecord(this.correctionForm).then(() => {
          this.$message.success('订正已提交，已加入复习计划')
          this.loadDetail()
        }).catch(() => {}).finally(() => { this.submitting = false })
      })
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
.image-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 14px; margin-top: 18px; }.image-grid figure { margin: 0; }.image-grid figcaption { margin-bottom: 6px; color: #606266; font-size: 13px; }.image-grid .el-image,.correction-image { width: 100%; max-height: 420px; border: 1px solid #ebeef5; border-radius: 4px; }.correction-image { display: block; width: 220px; margin-top: 10px; }
.source-position-panel { margin-top: 18px; }.source-position-title { margin-bottom: 8px; font-size: 14px; font-weight: 600; }.source-position-title small { color: #909399; font-weight: normal; }.source-position-canvas { position: relative; max-width: 720px; line-height: 0; }.source-position-canvas img { display: block; width: 100%; border: 1px solid #ebeef5; border-radius: 4px; }.source-position-box { position: absolute; box-sizing: border-box; border: 3px solid #f56c6c; background: rgba(245, 108, 108, .1); color: #fff; font-size: 12px; line-height: 20px; text-align: center; }
.summary-list { display: grid; grid-template-columns: 92px 1fr; gap: 14px 8px; margin: 0; font-size: 14px; }.summary-list dt { color: #909399; }.summary-list dd { margin: 0; word-break: break-word; }.form-tip { margin-left: 12px; }
@media (max-width: 768px) { .page-header { display: block; }.header-actions { padding-top: 8px; flex-wrap: wrap; } }
</style>
