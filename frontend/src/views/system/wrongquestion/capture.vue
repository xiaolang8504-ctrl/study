<template>
  <div class="app-container">
    <el-card>
      <div slot="header">采集中心</div>
      <el-form :model="form" label-width="90px" size="small" class="capture-form">
        <el-form-item label="年级"><el-select v-model="form.grade" placeholder="请选择年级"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item label="科目"><el-select v-model="form.subject" placeholder="请选择科目"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item label="题型"><el-select v-model="form.questionType" placeholder="请选择题型"><el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item label="来源"><el-select v-model="form.source" placeholder="请选择来源"><el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item label="采集文件">
          <el-upload ref="captureUpload" multiple accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf" :http-request="uploadImage" :before-upload="beforeUpload" :on-change="trackUploadFile" :on-remove="removeImage" :show-file-list="true">
            <el-button size="small">选择图片或 PDF</el-button>
            <span slot="tip" class="el-upload__tip">仅支持 JPG、JPEG、PNG、PDF（单文件不超过 20MB）；PDF 将自动按页渲染和识别。</span>
          </el-upload>
          <div class="capture-file-notice">DOC/DOCX 暂不支持：当前 OCR、分页和原图校验只覆盖图片及 PDF，文档会在文件选择器中被禁选。</div>
          <div v-if="uploadFiles.length" class="upload-file-order"><span>识别顺序：</span><span v-for="(file, index) in uploadFiles" :key="file.uid" class="upload-file-order-item">{{ index + 1 }}. {{ file.name }} <el-button type="text" :disabled="index === 0" @click="moveUploadFile(index, -1)">↑</el-button><el-button type="text" :disabled="index === uploadFiles.length - 1" @click="moveUploadFile(index, 1)">↓</el-button></span></div>
          <div v-if="Object.keys(uploadQualityWarnings).length" class="upload-quality-warnings"><p v-for="(warning, uid) in uploadQualityWarnings" :key="uid">{{ warning }}</p></div>
        </el-form-item>
        <el-button type="primary" :loading="submitting" :disabled="uploading > 0" @click="createTask">开始识别</el-button><span v-if="uploading" class="uploading-tip">正在上传 {{ uploading }} 个文件，请稍候</span>
      </el-form>
    </el-card>

    <el-card class="task-card">
      <div slot="header" class="card-header"><span>采集任务历史</span><el-select v-model="historyStatus" size="mini" clearable placeholder="全部状态" @change="loadHistory(1)"><el-option label="排队中" :value="0" /><el-option label="处理中" :value="1" /><el-option label="待确认" :value="2" /><el-option label="已完成" :value="3" /><el-option label="失败" :value="4" /></el-select></div>
      <el-table :data="history.list" size="mini">
        <el-table-column prop="id" label="任务" width="80" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="subject" label="科目" />
        <el-table-column prop="status" label="状态" width="90"><template slot-scope="scope"><el-tag size="mini" :type="taskStatusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="retryCount" label="重试" width="70" />
        <el-table-column prop="failReason" label="失败原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="90"><template slot-scope="scope"><el-button type="text" @click="openHistoryTask(scope.row)">继续处理</el-button></template></el-table-column>
      </el-table>
      <el-pagination small layout="prev, pager, next" :current-page="history.current" :page-size="history.pageSize" :total="history.total" @current-change="loadHistory" />
    </el-card>

    <el-card v-if="task" class="task-card">
      <div slot="header" class="card-header"><span>任务 #{{ task.id }} <el-tag :type="taskStatusType(task.status)">{{ statusText(task.status) }}</el-tag></span><span><el-button v-if="task.status === 2" size="mini" :disabled="!undoStack.length || hasConfirmedRegion" @click="undo">撤销</el-button><el-button v-if="task.status === 2" size="mini" :disabled="!redoStack.length || hasConfirmedRegion" @click="redo">重做</el-button><el-button v-if="task.status === 2 || task.status === 4" size="mini" @click="retryTask">重新识别全部页面</el-button></span></div>
      <el-alert v-if="task.failReason" :title="task.failReason" type="warning" :closable="false" show-icon />
      <el-alert v-if="task.status === 0 || task.status === 1" title="OCR 正在后台执行，页面会自动刷新。" type="info" :closable="false" show-icon />
      <div v-if="task.totalPageCount" class="task-progress"><span>页面进度：已完成 {{ task.completedPageCount || 0 }}/{{ task.totalPageCount }}<template v-if="task.failedPageCount">，失败 {{ task.failedPageCount }}</template><template v-if="estimatedRemainingText">，{{ estimatedRemainingText }}</template></span><el-progress :percentage="taskProgressPercent" :status="task.failedPageCount ? 'exception' : task.processingPageCount ? '' : 'success'" :stroke-width="8" /></div>

      <div v-if="task.pageList && task.pageList.length" class="capture-workbench">
        <aside class="page-list">
          <div class="page-list-title">采集页面</div>
          <div v-for="page in task.pageList" :key="page.id" class="page-item">
            <el-button size="mini" :type="page.id === currentPageId ? 'primary' : 'default'" @click="selectPage(page)">第 {{ page.pageNo }} 页</el-button>
            <el-tag v-if="page.status === 0" size="mini" type="info">排队</el-tag>
            <el-tag v-else-if="page.status === 1" size="mini" type="warning">处理中</el-tag>
            <el-tag v-else-if="page.status === 2" size="mini" type="success">完成</el-tag>
            <el-tag v-else-if="page.status === 4" size="mini" type="danger">失败</el-tag>
            <el-button v-if="page.status === 4" size="mini" type="text" :disabled="task.status === 1" @click="retryPage(page)">重新识别</el-button>
            <el-button v-if="canSavePageAsImage(page)" size="mini" type="text" @click="savePageAsImage(page)">仅保存图片</el-button>
            <p v-if="page.failReason" class="page-fail-reason">{{ page.failReason }}</p>
          </div>
        </aside>
        <section class="canvas-panel">
          <div class="image-switch"><el-button size="mini" :type="!showCleaned && !compareImages ? 'primary' : 'default'" @click="showPageImage(false)">原图</el-button><el-button size="mini" :disabled="!currentPage || currentPage.cleanStatus !== 2" :type="showCleaned && !compareImages ? 'primary' : 'default'" @click="showPageImage(true)">灰度预览</el-button><el-button size="mini" :disabled="!currentPage || currentPage.cleanStatus !== 2" :type="compareImages ? 'primary' : 'default'" @click="showImageCompare">并排对照</el-button><span v-if="currentPage" class="clean-status">{{ cleanStatusText(currentPage) }}，笔迹仍会保留</span></div>
          <el-alert v-if="currentPage && currentPage.cleanStatus === 4" :title="currentPage.cleanFailReason" type="warning" :closable="false" />
          <div v-if="compareImages && originalPageUrl && cleanedPageUrl" class="image-compare"><figure><figcaption>原图</figcaption><img :src="originalPageUrl"></figure><figure><figcaption>灰度预览（保留笔迹）</figcaption><img :src="cleanedPageUrl"></figure></div>
          <div v-else-if="currentPageUrl" ref="canvas" class="image-canvas" :class="{ 'create-region-mode': creatingRegion }" @mousedown="startCreateRegion"><img :src="currentPageUrl" @load="imageLoaded = true"><div v-for="region in currentPageRegions" :key="region.id" class="region-box" :class="{ active: activeRegionId === region.id, 'low-confidence': isLowConfidence(region), skipped: region.status === 2 }" :style="regionStyle(region)" @mousedown.stop.prevent="startDrag($event, region, 'drag')"><span class="region-label">题块 {{ region.regionNo }}</span><span v-if="region.status === 0" class="resize-handle" @mousedown.stop.prevent="startDrag($event, region, 'resize')" /></div><div v-if="draftRegion" class="region-box active manual-region" :style="regionStyle(draftRegion)"><span class="region-label">新题块</span></div></div>
          <el-empty v-else :description="currentPage && currentPage.status === 4 ? currentPage.failReason : '正在加载原图'" :image-size="70" />
        </section>
        <aside class="region-help"><b>切题操作</b><p>拖动题块移动位置，右下角拖动调整大小；松开鼠标自动保存。</p><el-button size="mini" :type="creatingRegion ? 'primary' : 'default'" @click="toggleCreateRegion">{{ creatingRegion ? '取消新增题块' : '框选新增题块' }}</el-button><el-button size="mini" :disabled="!activeRegionId" @click="splitRegion">按比例拆分当前题块</el-button><el-button size="mini" :disabled="!activeRegionId" @click="deleteRegion">跳过当前题块</el-button><el-button size="mini" :disabled="selected.length < 2" @click="mergeRegions">合并已勾选题块</el-button></aside>
      </div>

      <div class="region-filter"><el-checkbox v-model="lowConfidenceOnly">仅低置信度（&lt; {{ confidenceThreshold }}%）</el-checkbox><el-checkbox v-model="showSkipped">显示已跳过题块</el-checkbox><span>待确认 {{ pendingCount }} 个，低置信度 {{ lowConfidenceCount }} 个</span></div>
      <el-table :data="displayRegions" @selection-change="changeSelection" border>
        <el-table-column type="selection" width="50" :selectable="row => row.status === 0" />
        <el-table-column prop="regionNo" label="题块" width="70" />
        <el-table-column prop="confidence" label="置信度" width="95"><template slot-scope="scope"><el-tag :type="isLowConfidence(scope.row) ? 'warning' : 'success'">{{ scope.row.confidence }}%</el-tag></template></el-table-column>
        <el-table-column label="状态" width="100"><template slot-scope="scope"><el-tag v-if="scope.row.status === 2" type="info">已跳过</el-tag><el-tag v-else-if="scope.row.manuallyCorrected" type="primary">已校正</el-tag><el-tag v-else type="warning">待确认</el-tag></template></el-table-column>
        <el-table-column label="题干" min-width="240"><template slot-scope="scope"><el-input v-model="scope.row.questionContent" :class="{ 'low-confidence-input': isFieldLowConfidence(scope.row.questionContentConfidence) }" :disabled="scope.row.status !== 0" type="textarea" :rows="2" @blur="saveRegion(scope.row)" /></template></el-table-column>
        <el-table-column label="答案" min-width="180"><template slot-scope="scope"><el-input v-model="scope.row.correctAnswer" :class="{ 'low-confidence-input': isFieldLowConfidence(scope.row.correctAnswerConfidence) }" :disabled="scope.row.status !== 0" type="textarea" :rows="2" @blur="saveRegion(scope.row)" /></template></el-table-column>
        <el-table-column label="操作" width="150"><template slot-scope="scope"><el-button v-if="scope.row.status === 2" type="text" @click="restoreRegion(scope.row)">恢复</el-button><template v-else-if="scope.row.status === 0"><el-button type="text" @click="focusRegion(scope.row)">定位</el-button><el-button type="text" @click="editRegion(scope.row)">编辑</el-button></template><el-link v-else-if="scope.row.wrongQuestionId" type="primary" @click="openWrongQuestion(scope.row)">查看错题</el-link></template></el-table-column>
      </el-table>
      <el-button v-if="task.status === 2" type="primary" :disabled="!selected.length" @click="confirm">确认并创建错题</el-button>
    </el-card>

    <el-dialog title="校正题块内容与归类" :visible.sync="regionEditorOpen" width="720px" append-to-body>
      <el-form :model="regionForm" label-width="90px">
        <el-row :gutter="12"><el-col :span="12"><el-form-item label="年级"><el-select v-model="regionForm.grade" clearable placeholder="沿用任务默认值"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="科目"><el-select v-model="regionForm.subject" clearable placeholder="沿用任务默认值"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col></el-row>
        <el-row :gutter="12"><el-col :span="12"><el-form-item label="题型"><el-select v-model="regionForm.questionType" clearable placeholder="沿用任务默认值"><el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="来源"><el-select v-model="regionForm.source" clearable placeholder="沿用任务默认值"><el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="题目标题"><el-input v-model="regionForm.questionTitle" /></el-form-item>
        <el-form-item label="题目内容"><el-input v-model="regionForm.questionContent" :class="{ 'low-confidence-input': isFieldLowConfidence(regionForm.questionContentConfidence) }" type="textarea" :rows="4" /><span class="field-confidence">题干识别置信度 {{ regionForm.questionContentConfidence || 0 }}%</span></el-form-item>
        <el-row :gutter="12"><el-col :span="12"><el-form-item label="错误答案"><el-input v-model="regionForm.wrongAnswer" :class="{ 'low-confidence-input': isFieldLowConfidence(regionForm.wrongAnswerConfidence) }" type="textarea" :rows="2" /><span class="field-confidence">置信度 {{ regionForm.wrongAnswerConfidence || 0 }}%</span></el-form-item></el-col><el-col :span="12"><el-form-item label="参考答案"><el-input v-model="regionForm.correctAnswer" :class="{ 'low-confidence-input': isFieldLowConfidence(regionForm.correctAnswerConfidence) }" type="textarea" :rows="2" /><span class="field-confidence">置信度 {{ regionForm.correctAnswerConfidence || 0 }}%</span></el-form-item></el-col></el-row>
        <el-form-item label="错误原因"><el-input v-model="regionForm.wrongReason" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="题目解析"><el-input v-model="regionForm.analysis" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="知识点"><el-input v-model="regionForm.learningPoint" placeholder="可留空，沿用任务默认值" /></el-form-item>
        <el-form-item label="错因标签"><el-input v-model="regionForm.errorLabels" placeholder="多个标签使用逗号分隔" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="regionEditorOpen = false">取消</el-button><el-button type="primary" @click="saveRegionEditor">保存校正</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { createQuestionCaptureTask, questionCaptureTaskDetail, retryQuestionCaptureTask, retryQuestionCapturePage, saveQuestionCapturePageAsImage, questionCaptureDuplicateList, confirmQuestionCapture, updateQuestionCaptureRegion, createQuestionCaptureRegion, mergeQuestionCaptureRegion, splitQuestionCaptureRegionByRatio, restoreQuestionCaptureRegionSnapshot, deleteQuestionCaptureRegion, restoreQuestionCaptureRegion, questionCaptureTaskPageList } from '@/api/system/wrongQuestion'
import { filePolicy, uploadFile, downloadUrl } from '@/api/system/file'
import { dictDataOptions } from '@/api/system/dict'

const DICT_TYPES = { grade: 'grade', subject: 'subject', questionType: 'question_type', source: 'source' }
const CAPTURE_DRAFT_STORAGE_KEY = 'study.question-capture-draft.v1'
const CAPTURE_DRAFT_TTL_MS = 24 * 60 * 60 * 1000

export default {
  name: 'QuestionCapture',
  data() {
    return { form: { grade: '', subject: '', questionType: '', source: '' }, imageFileIds: [], uploadFiles: [], task: null, selected: [], submitting: false, uploading: 0, uploadQualityWarnings: {}, timer: null, currentPageId: null, currentPageUrl: '', originalPageUrl: '', cleanedPageUrl: '', compareImages: false, activeRegionId: null, imageLoaded: false, pointer: null, creatingRegion: false, draftRegion: null, showCleaned: false, lowConfidenceOnly: false, showSkipped: false, confidenceThreshold: 90, historyStatus: null, history: { list: [], current: 1, pageSize: 10, total: 0 }, gradeOptions: [], subjectOptions: [], questionTypeOptions: [], sourceOptions: [], regionEditorOpen: false, regionForm: {}, undoStack: [], redoStack: [] }
  },
  computed: {
    currentPage() { return this.task && this.currentPageId ? this.task.pageList.find(item => item.id === this.currentPageId) : null },
    currentPageRegions() { return this.displayRegions.filter(item => item.pageId === this.currentPageId) },
    displayRegions() { if (!this.task) return []; return this.task.regionList.filter(item => (this.showSkipped || item.status !== 2) && (!this.lowConfidenceOnly || this.isLowConfidence(item))) },
    pendingCount() { return this.task ? this.task.regionList.filter(item => item.status === 0).length : 0 },
    lowConfidenceCount() { return this.task ? this.task.regionList.filter(item => item.status === 0 && this.isLowConfidence(item)).length : 0 },
    hasConfirmedRegion() { return this.task ? this.task.regionList.some(item => item.status === 1) : false },
    taskProgressPercent() { if (!this.task || !this.task.totalPageCount) return 0; return Math.round(((this.task.completedPageCount || 0) + (this.task.failedPageCount || 0)) * 100 / this.task.totalPageCount) },
    estimatedRemainingText() { if (!this.task || ![0, 1].includes(Number(this.task.status))) return ''; const seconds = Math.max(0, Number(this.task.processingPageCount || 0) * 15); if (!seconds) return '正在分配识别任务'; return `预计剩余约 ${seconds < 60 ? seconds + ' 秒' : Math.ceil(seconds / 60) + ' 分钟'}（按 15 秒/页估算）` }
  },
  created() {
    this.loadDictOptions()
    this.loadHistory(1)
    const taskId = Number(this.$route.query.taskId)
    if (taskId > 0) this.openTask(taskId)
    else this.restoreCaptureDraft()
  },
  beforeDestroy() { this.persistCaptureDraft(); clearTimeout(this.timer); this.timer = null; this.stopPointer() },
  methods: {
    loadDictOptions() { Promise.all([dictDataOptions({ dictType: DICT_TYPES.grade }), dictDataOptions({ dictType: DICT_TYPES.subject }), dictDataOptions({ dictType: DICT_TYPES.questionType }), dictDataOptions({ dictType: DICT_TYPES.source })]).then(([gradeOptions, subjectOptions, questionTypeOptions, sourceOptions]) => { this.gradeOptions = gradeOptions || []; this.subjectOptions = subjectOptions || []; this.questionTypeOptions = questionTypeOptions || []; this.sourceOptions = sourceOptions || [] }).catch(() => this.$message.warning('采集元数据字典加载失败，请刷新后重试')) },
    beforeUpload(file) { const extension = String(file.name || '').split('.').pop().toLowerCase(); if (!['jpg', 'jpeg', 'png', 'pdf'].includes(extension)) { this.$message.error('仅支持 JPG、JPEG、PNG、PDF 文件'); return false } if (file.size > 20 * 1024 * 1024) { this.$message.error('单个文件不能超过 20MB'); return false } if (extension === 'pdf') return true; return this.inspectImageQuality(file) },
    inspectImageQuality(file) { return new Promise(resolve => { const reader = new FileReader(); reader.onload = event => { const image = new Image(); image.onload = () => { if (image.width < 600 || image.height < 600) { this.$message.error(`${file.name} 分辨率过低，请重新拍摄或上传原图`); return resolve(false) } const canvas = document.createElement('canvas'); const scale = Math.min(1, 480 / Math.max(image.width, image.height)); canvas.width = Math.max(1, Math.round(image.width * scale)); canvas.height = Math.max(1, Math.round(image.height * scale)); const context = canvas.getContext('2d'); context.drawImage(image, 0, 0, canvas.width, canvas.height); const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data; let sum = 0; let squareSum = 0; for (let index = 0; index < pixels.length; index += 4) { const gray = pixels[index] * 0.299 + pixels[index + 1] * 0.587 + pixels[index + 2] * 0.114; sum += gray; squareSum += gray * gray } const count = pixels.length / 4; const average = sum / count; const deviation = Math.sqrt(Math.max(0, squareSum / count - average * average)); const warnings = []; if (average < 55) warnings.push('图片偏暗'); if (average > 230) warnings.push('图片过曝'); if (deviation < 20) warnings.push('对比度偏低，可能存在反光或模糊'); if (warnings.length) { this.$set(this.uploadQualityWarnings, file.uid, `${file.name}：${warnings.join('；')}，建议重拍后再识别`); this.$message.warning(`${file.name} 可能影响识别，请查看上传提示`) } resolve(true) }; image.onerror = () => resolve(true); image.src = event.target.result }; reader.onerror = () => resolve(true); reader.readAsDataURL(file) }) },
    trackUploadFile(file) { if (!this.uploadFiles.some(item => item.uid === file.uid)) this.uploadFiles.push(file) },
    moveUploadFile(index, offset) { const target = index + offset; if (target < 0 || target >= this.uploadFiles.length) return; const moved = this.uploadFiles.splice(index, 1)[0]; this.uploadFiles.splice(target, 0, moved) },
    uploadImage(option) { this.uploading += 1; filePolicy({ uploadType: 'wrongQuestion' }).then(policy => { const formData = new FormData(); formData.append('file', option.file); formData.append('signature', policy.signature); return uploadFile(formData) }).then(file => { option.file.captureFileId = file.id; if (!this.imageFileIds.includes(file.id)) this.imageFileIds.push(file.id); this.persistCaptureDraft(); option.onSuccess(file) }).catch(option.onError).finally(() => { this.uploading = Math.max(0, this.uploading - 1) }) },
    removeImage(file) { const fileId = file && file.captureFileId; if (fileId) this.imageFileIds = this.imageFileIds.filter(id => id !== fileId); this.uploadFiles = this.uploadFiles.filter(item => item.uid !== file.uid); if (file && file.uid) this.$delete(this.uploadQualityWarnings, file.uid); this.persistCaptureDraft() },
    createTask() { if (!this.form.grade || !this.form.subject || !this.form.questionType || !this.form.source) return this.$message.warning('请选择年级、科目、题型和来源'); if (this.uploading > 0) return this.$message.warning('文件仍在上传，请稍候'); const orderedFileIds = this.uploadFiles.map(file => file.captureFileId).filter(Boolean); const sourceFileIds = orderedFileIds.length ? orderedFileIds : this.imageFileIds; if (!sourceFileIds.length) return this.$message.warning('请先上传至少一张图片或 PDF'); this.submitting = true; const clientRequestId = `capture-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`; createQuestionCaptureTask(Object.assign({}, this.form, { imageFileIds: sourceFileIds, clientRequestId })).then(id => { this.imageFileIds = []; this.uploadFiles = []; this.uploadQualityWarnings = {}; this.$refs.captureUpload.clearFiles(); this.persistCaptureDraft(id); this.openTask(id); this.loadHistory(1) }).finally(() => { this.submitting = false }) },
    loadHistory(current) { questionCaptureTaskPageList({ current, pageSize: this.history.pageSize, status: this.historyStatus }).then(data => { this.history = data }) },
    openHistoryTask(row) { this.openTask(row.id) },
    openTask(taskId) { this.currentPageId = null; this.task = null; this.undoStack = []; this.redoStack = []; this.persistCaptureDraft(taskId); this.loadTask(taskId) },
    loadTask(taskId) { const id = taskId || (this.task && this.task.id); if (!id) return; clearTimeout(this.timer); this.timer = null; questionCaptureTaskDetail({ id }).then(data => { this.task = data; if (!this.currentPageId && data.pageList.length) this.selectPage(data.pageList[0]); const status = Number(data.status); if (status === 3) this.clearCaptureDraft(); else this.persistCaptureDraft(id); if (status === 0 || status === 1) this.timer = setTimeout(() => this.loadTask(id), 1500) }).catch(() => { this.timer = null }) },
    persistCaptureDraft(taskId) { const id = taskId || (this.task && this.task.id); const fileIds = this.uploadFiles.map(file => file.captureFileId).filter(Boolean); const imageFileIds = fileIds.length ? fileIds : this.imageFileIds; if (!id && !imageFileIds.length) return; localStorage.setItem(CAPTURE_DRAFT_STORAGE_KEY, JSON.stringify({ savedAt: Date.now(), taskId: id || null, form: this.form, imageFileIds, fileNames: this.uploadFiles.map(file => file.name || '已上传文件') })) },
    clearCaptureDraft() { localStorage.removeItem(CAPTURE_DRAFT_STORAGE_KEY) },
    restoreCaptureDraft() { let draft; try { draft = JSON.parse(localStorage.getItem(CAPTURE_DRAFT_STORAGE_KEY) || 'null') } catch (error) { this.clearCaptureDraft(); return } if (!draft || !draft.savedAt || Date.now() - Number(draft.savedAt) > CAPTURE_DRAFT_TTL_MS) { this.clearCaptureDraft(); return } if (draft.taskId) { this.$message.info('已恢复 24 小时内的采集任务草稿'); this.openTask(draft.taskId); return } if (Array.isArray(draft.imageFileIds) && draft.imageFileIds.length) { this.form = Object.assign({}, this.form, draft.form || {}); this.imageFileIds = draft.imageFileIds; this.uploadFiles = draft.imageFileIds.map((id, index) => ({ uid: `draft-${id}`, name: (draft.fileNames || [])[index] || `已上传文件 #${id}`, captureFileId: id })); this.$message.info('已恢复 24 小时内的上传草稿，可继续开始识别') } },
    selectPage(page) { this.currentPageId = page.id; this.showCleaned = false; this.compareImages = false; this.originalPageUrl = ''; this.cleanedPageUrl = ''; if (page.status === 4 && !page.sourcePageNo) { this.currentPageUrl = ''; return } this.showPageImage(false) },
    showPageImage(cleaned) { if (!this.currentPage || (cleaned && this.currentPage.cleanStatus !== 2)) return; const fileId = cleaned ? this.currentPage.cleanedFileId : this.currentPage.imageFileId; this.compareImages = false; this.showCleaned = cleaned; this.currentPageUrl = ''; downloadUrl({ fileId, uploadType: 'wrongQuestion' }).then(url => { this.currentPageUrl = url }) },
    showImageCompare() { if (!this.currentPage || this.currentPage.cleanStatus !== 2) return; this.showCleaned = false; this.compareImages = true; this.currentPageUrl = ''; Promise.all([downloadUrl({ fileId: this.currentPage.imageFileId, uploadType: 'wrongQuestion' }), downloadUrl({ fileId: this.currentPage.cleanedFileId, uploadType: 'wrongQuestion' })]).then(([originalUrl, cleanedUrl]) => { this.originalPageUrl = originalUrl; this.cleanedPageUrl = cleanedUrl }).catch(() => { this.compareImages = false; this.$message.warning('图片对照加载失败，请稍后重试') }) },
    taskStatusType(status) { return status === 4 ? 'danger' : status === 2 ? 'warning' : status === 3 ? 'success' : 'info' },
    statusText(status) { return ['排队中', '处理中', '待确认', '已完成', '失败'][status] || '待处理' },
    cleanStatusText(page) { return ['', '灰度预览生成中', '灰度预览已就绪', '', '灰度预览生成失败'][page.cleanStatus] || '等待生成灰度预览' },
    isLowConfidence(region) { return Number(region.confidence || 0) < this.confidenceThreshold },
    isFieldLowConfidence(confidence) { return Number(confidence || 0) < this.confidenceThreshold },
    pageHasNoRegions(page) { return this.task && !this.task.regionList.some(item => item.pageId === page.id && item.status !== 2) },
    canSavePageAsImage(page) { return this.task && [2, 4].includes(Number(this.task.status)) && page.sourcePageNo && (page.status === 4 || this.pageHasNoRegions(page)) },
    changeSelection(rows) { this.selected = rows },
    regionStyle(region) { return { left: `${region.leftPosition / 100}%`, top: `${region.topPosition / 100}%`, width: `${region.width / 100}%`, height: `${region.height / 100}%` } },
    focusRegion(region) { this.activeRegionId = region.id; if (region.pageId !== this.currentPageId) { const page = this.task.pageList.find(item => item.id === region.pageId); if (page) this.selectPage(page) } },
    startDrag(event, region, mode) { if (region.status !== 0) return; this.activeRegionId = region.id; this.pointer = { mode, region, x: event.clientX, y: event.clientY, left: region.leftPosition, top: region.topPosition, width: region.width, height: region.height }; window.addEventListener('mousemove', this.movePointer); window.addEventListener('mouseup', this.stopPointer) },
    toggleCreateRegion() { this.creatingRegion = !this.creatingRegion; this.draftRegion = null; this.activeRegionId = null },
    startCreateRegion(event) { if (!this.creatingRegion || !this.$refs.canvas || !this.currentPage || this.task.status !== 2) return; const box = this.$refs.canvas.getBoundingClientRect(); const left = Math.max(0, Math.min(10000, Math.round((event.clientX - box.left) / box.width * 10000))); const top = Math.max(0, Math.min(10000, Math.round((event.clientY - box.top) / box.height * 10000))); this.draftRegion = { leftPosition: left, topPosition: top, width: 0, height: 0 }; this.pointer = { mode: 'create', x: event.clientX, y: event.clientY, left, top }; window.addEventListener('mousemove', this.movePointer); window.addEventListener('mouseup', this.stopPointer) },
    movePointer(event) { if (!this.pointer || !this.$refs.canvas) return; const box = this.$refs.canvas.getBoundingClientRect(); const dx = (event.clientX - this.pointer.x) / box.width * 10000; const dy = (event.clientY - this.pointer.y) / box.height * 10000; const point = this.pointer; if (point.mode === 'create') { const right = Math.max(0, Math.min(10000, Math.round(point.left + dx))); const bottom = Math.max(0, Math.min(10000, Math.round(point.top + dy))); this.draftRegion.leftPosition = Math.min(point.left, right); this.draftRegion.topPosition = Math.min(point.top, bottom); this.draftRegion.width = Math.abs(right - point.left); this.draftRegion.height = Math.abs(bottom - point.top); return } if (point.mode === 'drag') { point.region.leftPosition = Math.max(0, Math.min(10000 - point.width, Math.round(point.left + dx))); point.region.topPosition = Math.max(0, Math.min(10000 - point.height, Math.round(point.top + dy))) } else { point.region.width = Math.max(100, Math.min(10000 - point.left, Math.round(point.width + dx))); point.region.height = Math.max(100, Math.min(10000 - point.top, Math.round(point.height + dy))) } },
    stopPointer() { const point = this.pointer; this.pointer = null; window.removeEventListener('mousemove', this.movePointer); window.removeEventListener('mouseup', this.stopPointer); if (!point) return; if (point.mode === 'create') { const region = this.draftRegion; this.draftRegion = null; if (!region || region.width < 100 || region.height < 100) return this.$message.warning('请框选足够大的题目区域'); return this.withSnapshot(() => createQuestionCaptureRegion(Object.assign({ taskId: this.task.id, pageId: this.currentPage.id }, region))).then(() => { this.creatingRegion = false }) } this.saveRegion(point.region) },
    saveRegion(row) { if (row.status !== 0 || !this.task || this.task.status !== 2) return; this.withSnapshot(() => updateQuestionCaptureRegion(row).then(() => { row.manuallyCorrected = 1 })) },
    retryTask() { this.$confirm('重新识别会覆盖所有未确认题块，是否继续？', '提示').then(() => retryQuestionCaptureTask({ id: this.task.id })).then(() => { this.selected = []; this.loadTask() }).catch(() => {}) },
    retryPage(page) { this.$confirm('将覆盖本页未确认题块，是否继续？', '提示').then(() => retryQuestionCapturePage({ id: page.id })).then(() => { this.activeRegionId = null; this.selected = []; this.loadTask() }).catch(() => {}) },
    savePageAsImage(page) { this.$confirm('将以整页原图创建一条待确认错题。你可在确认前补充题干和答案，是否继续？', '仅保存图片').then(() => saveQuestionCapturePageAsImage({ id: page.id })).then(() => { this.activeRegionId = null; this.selected = []; this.loadTask() }).catch(() => {}) },
    mergeRegions() {
      const regionIds = this.selected.map(item => item.id)
      this.withSnapshot(() => mergeQuestionCaptureRegion({ taskId: this.task.id, regionIds }).then(() => { this.selected = [] }))
    },
    splitRegion() { this.$prompt('请输入上半部分占比（10–90）', '按比例拆分题块', { inputValue: 50, inputPattern: /^(?:[1-8]?\d|90)$/, inputErrorMessage: '请输入 10 至 90 的整数' }).then(({ value }) => this.withSnapshot(() => splitQuestionCaptureRegionByRatio({ id: this.activeRegionId, splitRatio: Number(value) }))).catch(() => {}) },
    deleteRegion() { this.withSnapshot(() => deleteQuestionCaptureRegion({ id: this.activeRegionId }).then(() => { this.activeRegionId = null })) },
    restoreRegion(row) { this.withSnapshot(() => restoreQuestionCaptureRegion({ id: row.id })) },
    editRegion(row) { this.regionForm = Object.assign({}, row); this.regionEditorOpen = true },
    saveRegionEditor() { this.withSnapshot(() => updateQuestionCaptureRegion(this.regionForm).then(() => { this.regionEditorOpen = false })) },
    currentSnapshot() { return JSON.parse(JSON.stringify((this.task && this.task.regionList) || [])) },
    withSnapshot(action) { const before = this.currentSnapshot(); return action().then(() => { this.undoStack.push(before); this.redoStack = []; return this.loadTask() }) },
    restoreSnapshot(snapshot) { return restoreQuestionCaptureRegionSnapshot({ taskId: this.task.id, regionList: snapshot }).then(() => this.loadTask()) },
    undo() { const snapshot = this.undoStack.pop(); if (!snapshot) return; const current = this.currentSnapshot(); this.restoreSnapshot(snapshot).then(() => this.redoStack.push(current)).catch(() => this.undoStack.push(snapshot)) },
    redo() { const snapshot = this.redoStack.pop(); if (!snapshot) return; const current = this.currentSnapshot(); this.restoreSnapshot(snapshot).then(() => this.undoStack.push(current)).catch(() => this.redoStack.push(snapshot)) },
    confirm() { const payload = { taskId: this.task.id, regionIds: this.selected.map(item => item.id) }; questionCaptureDuplicateList(payload).then(list => { if (!list || !list.length) return this.confirmCapture(payload); const message = `${list.map(item => `题块 ${item.regionId} 与错题 #${item.wrongQuestionId}「${item.questionTitle || '未命名题目'}」完全相同`).join('；')}。仍要继续创建吗？`; return this.$confirm(message, '发现重复错题', { confirmButtonText: '仍然创建', cancelButtonText: '返回检查', type: 'warning' }).then(() => this.confirmCapture(payload)) }).catch(() => {}) },
    confirmCapture(payload) { return confirmQuestionCapture(payload).then(count => { this.$message.success(`已创建 ${count} 道错题`); this.selected = []; this.loadTask(); this.loadHistory(this.history.current) }) },
    openWrongQuestion(row) { this.$router.push({ path: '/system/wrongquestion', query: { wrongQuestionId: row.wrongQuestionId } }) }
  }
}
</script>

<style scoped>
.task-card { margin-top: 16px; }.card-header { display: flex; justify-content: space-between; align-items: center; }.task-progress { display: flex; align-items: center; gap: 12px; margin: 12px 0; color: #606266; font-size: 13px; }.task-progress .el-progress { width: 260px; }.capture-workbench { display: flex; gap: 16px; margin: 16px 0; }.page-list, .region-help { width: 170px; }.page-item { margin-bottom: 10px; }.page-item .el-button:first-child { width: 90px; }.page-fail-reason { margin: 3px 0 0; color: #f56c6c; font-size: 12px; line-height: 1.4; word-break: break-word; }.canvas-panel { flex: 1; min-width: 0; padding: 12px; background: #f5f7fa; }.image-switch { margin-bottom: 8px; }.clean-status, .uploading-tip, .field-confidence { margin-left: 8px; font-size: 12px; color: #909399; }.capture-file-notice { margin-top: 6px; color: #909399; font-size: 12px; line-height: 1.5; }.upload-file-order { margin-top: 8px; color: #606266; font-size: 12px; }.upload-file-order-item { display: inline-block; margin: 0 8px 4px 0; }.upload-file-order .el-button { padding: 0 3px; }.upload-quality-warnings { margin-top: 8px; color: #e6a23c; font-size: 12px; line-height: 1.5; }.upload-quality-warnings p { margin: 2px 0; }.image-canvas { position: relative; display: inline-block; width: 100%; line-height: 0; }.image-canvas.create-region-mode { cursor: crosshair; }.image-canvas img { width: 100%; max-height: 650px; object-fit: contain; }.image-compare { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }.image-compare figure { min-width: 0; margin: 0; }.image-compare figcaption { padding: 6px 8px; line-height: 20px; background: #ebeef5; color: #606266; font-size: 13px; }.image-compare img { display: block; width: 100%; max-height: 650px; object-fit: contain; background: #fff; }.region-box { position: absolute; box-sizing: border-box; border: 2px solid #409eff; background: rgba(64, 158, 255, .08); cursor: move; line-height: 20px; }.region-box.active { border-color: #f56c6c; }.region-box.manual-region { cursor: crosshair; }.region-box.low-confidence { border-color: #e6a23c; background: rgba(230, 162, 60, .12); }.region-box.skipped { border-style: dashed; opacity: .55; }.region-label { padding: 1px 4px; color: #fff; background: #409eff; font-size: 12px; }.resize-handle { position: absolute; right: -5px; bottom: -5px; width: 10px; height: 10px; background: #409eff; cursor: nwse-resize; }.region-help { font-size: 12px; color: #606266; }.region-help .el-button { display: block; width: 100%; margin: 0 0 8px; }.region-filter { display: flex; gap: 16px; align-items: center; margin: 12px 0; color: #606266; font-size: 13px; }.low-confidence-input ::v-deep textarea, .low-confidence-input ::v-deep input { border-color: #e6a23c; background: #fdf6ec; }
</style>
