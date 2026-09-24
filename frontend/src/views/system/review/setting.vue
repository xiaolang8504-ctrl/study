<template>
  <div v-loading="loading" class="plan-setting-page">
    <section class="setting-hero">
      <div>
        <div class="hero-eyebrow">REVIEW PLAN</div>
        <h1>复习计划设置</h1>
        <p>设置适合自己的每日任务量和复习提醒。动态复习间隔仍由每次四级反馈自动计算。</p>
      </div>
      <i class="el-icon-setting hero-icon" />
    </section>

    <el-row :gutter="18">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="setting-card">
          <div slot="header" class="card-header">
            <div><strong>基础计划</strong><span>控制每天进入任务列表的最大题量</span></div>
          </div>
          <el-form ref="settingForm" :model="form" :rules="rules" label-position="top">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model.trim="form.planName" maxlength="50" show-word-limit placeholder="例如：我的错题复习" />
            </el-form-item>
            <el-form-item label="每日复习上限" prop="dailyLimit">
              <el-input-number v-model="form.dailyLimit" :min="1" :max="200" :step="5" />
              <span class="field-tip">道 / 天，当日已完成数量会计入上限</span>
            </el-form-item>
            <el-form-item label="复习日" prop="reviewWeekDays">
              <el-checkbox-group v-model="form.reviewWeekDays" class="week-group">
                <el-checkbox-button v-for="item in weekOptions" :key="item.value" :label="item.value">
                  {{ item.label }}
                </el-checkbox-button>
              </el-checkbox-group>
              <div class="form-tip">到期题目不会丢失，未完成任务会在后续复习日继续显示为逾期任务。</div>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="setting-card subject-card">
          <div slot="header" class="card-header">
            <div><strong>科目安排</strong><span>控制各科是否参与复习及每天最多进入的题量</span></div>
          </div>
          <el-empty v-if="!form.subjectSettings.length" description="暂无可用科目，请先维护科目字典" :image-size="72" />
          <div v-else class="subject-list">
            <div v-for="item in form.subjectSettings" :key="item.subject" class="subject-item">
              <div class="subject-info">
                <span class="subject-name">{{ item.subjectName }}</span>
              </div>
              <el-switch
                :value="item.enabled === 1"
                active-color="#596bf2"
                @change="item.enabled = $event ? 1 : 0"
              />
              <div :class="['subject-limit', { disabled: item.enabled !== 1 }]">
                <span>每日最多</span>
                <el-input-number v-model="item.dailyLimit" :disabled="item.enabled !== 1" :min="1" :max="200" :step="5" size="small" />
                <span>道</span>
              </div>
            </div>
          </div>
          <div class="form-tip">科目上限用于分配每日任务，总任务量仍不会超过基础计划中的每日上限。</div>
        </el-card>

        <el-card shadow="never" class="setting-card reminder-card">
          <div slot="header" class="card-header">
            <div><strong>站内提醒</strong><span>在选定复习日按时提醒当天任务</span></div>
            <el-switch v-model="reminderEnabled" active-color="#596bf2" />
          </div>
          <div :class="['reminder-content', { disabled: !reminderEnabled }]">
            <div class="reminder-clock"><i class="el-icon-bell" /></div>
            <div class="reminder-copy">
              <strong>{{ reminderEnabled ? '提醒已开启' : '提醒已关闭' }}</strong>
              <span>{{ reminderEnabled ? '到点后通过站内消息提醒你开始复习' : '仍可在今日复习中主动查看到期任务' }}</span>
            </div>
            <el-time-select
              v-model="form.reminderTime"
              :disabled="!reminderEnabled"
              :picker-options="timeOptions"
              placeholder="选择提醒时间"
            />
          </div>
        </el-card>

        <el-card shadow="never" class="setting-card">
          <div slot="header" class="card-header"><div><strong>学习资料偏好</strong><span>选择当前学习的年级、科目和教材，用于后续采集和组卷推荐。</span></div></div>
          <el-form :model="learningProfile" label-position="top" class="profile-form">
            <el-row :gutter="14">
              <el-col :xs="24" :sm="12"><el-form-item label="年级"><el-select v-model="learningProfile.grade" clearable placeholder="请选择年级" @change="profileScopeChanged"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
              <el-col :xs="24" :sm="12"><el-form-item label="科目"><el-select v-model="learningProfile.subject" clearable placeholder="请选择科目" @change="profileScopeChanged"><el-option v-for="item in profileSubjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
            </el-row>
            <el-form-item label="教材"><el-select v-model="learningProfile.bookId" clearable filterable :disabled="!learningProfile.grade || !learningProfile.subject" placeholder="请先选择年级和科目"><el-option v-for="item in bookOptions" :key="item.id" :label="item.title" :value="item.id" /></el-select><div class="form-tip">教材库暂无匹配记录时，仍可保存年级和科目偏好。</div></el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="saveLearningProfile">保存学习偏好</el-button>
          </el-form>
        </el-card>

        <el-card shadow="never" class="setting-card backup-card">
          <div slot="header" class="card-header"><div><strong>个人数据备份</strong><span>导出本人错题、复习计划、复习记录和个人组卷数据为 JSON 文件。</span></div></div>
          <p>备份仅包含当前登录账号的数据，不包含其他用户和系统管理数据。请在安全的位置保存。</p>
          <el-button icon="el-icon-download" :loading="backupLoading" @click="downloadBackup">下载数据备份</el-button>
        </el-card>

        <el-card shadow="never" class="setting-card sprint-card">
          <div slot="header" class="card-header"><div><strong>考前冲刺</strong><span>从未订正和掌握不稳的个人错题中生成每日短练，不替代正常到期复习。</span></div></div>
          <el-form :model="sprintForm" label-position="top" class="sprint-form">
            <el-row :gutter="14">
              <el-col :xs="24" :sm="8"><el-form-item label="考试科目"><el-select v-model="sprintForm.subject" placeholder="选择科目"><el-option v-for="item in profileSubjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
              <el-col :xs="24" :sm="8"><el-form-item label="考试日期"><el-date-picker v-model="sprintForm.examDate" type="date" value-format="yyyy-MM-dd" :picker-options="sprintDateOptions" placeholder="选择日期" /></el-form-item></el-col>
              <el-col :xs="12" :sm="4"><el-form-item label="每日分钟"><el-input-number v-model="sprintForm.dailyMinutes" :min="5" :max="240" /></el-form-item></el-col>
              <el-col :xs="12" :sm="4"><el-form-item label="每日题数"><el-input-number v-model="sprintForm.targetQuestionCount" :min="1" :max="30" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="考试范围"><el-input v-model.trim="sprintForm.scopeText" maxlength="500" show-word-limit placeholder="例如：二次函数、几何证明" /></el-form-item>
            <el-button type="primary" :loading="sprintSaving" @click="saveSprint">生成冲刺短练</el-button>
          </el-form>
          <div v-if="sprint" class="sprint-result">
            <p><strong>{{ sprint.subject }} · 距考试 {{ sprint.daysRemaining }} 天</strong>，今天建议 {{ sprint.candidates.length }} 题，约 {{ sprint.estimatedMinutes }} 分钟。</p>
            <p class="form-tip">{{ sprint.coordinationNote }}</p>
            <el-tag v-for="item in sprint.candidates" :key="item.wrongQuestionId" class="sprint-question" type="warning">{{ item.questionTitle || ('错题 #' + item.wrongQuestionId) }} · {{ item.priorityReason }}</el-tag>
            <el-button v-if="sprint.candidates.length" size="small" type="success" @click="startSprintPractice">开始这组短练</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="setting-card preview-card">
          <div slot="header" class="card-header"><div><strong>计划预览</strong></div></div>
          <div class="preview-name">{{ form.planName || '我的错题复习' }}</div>
          <div class="preview-row"><span>每日上限</span><strong>{{ form.dailyLimit }} 道</strong></div>
          <div class="preview-row"><span>参与科目</span><strong>{{ enabledSubjectCount }} 科</strong></div>
          <div class="preview-row"><span>每周复习</span><strong>{{ form.reviewWeekDays.length }} 天</strong></div>
          <div class="preview-row"><span>站内提醒</span><strong>{{ reminderEnabled ? form.reminderTime : '已关闭' }}</strong></div>
          <div class="algorithm-tip">
            <i class="el-icon-magic-stick" />
            <div><strong>动态排期</strong><span>忘记、困难、掌握、很简单四级反馈会自动决定下次复习时间。</span></div>
          </div>
          <el-button type="primary" :loading="saving" class="save-button" @click="saveSetting">保存设置</el-button>
          <el-button class="reset-button" @click="resetDefault">恢复推荐设置</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { createPracticeSession, learningDataBackup, learningProfile, reviewExamSprint, reviewPlanSetting, saveReviewExamSprint, updateLearningProfile, updateReviewPlanSetting } from '@/api/system/review'
import { bookPageList } from '@/api/system/book'
import { dictDataOptions } from '@/api/system/dict'

const defaultForm = (subjectSettings = []) => ({
  planName: '我的错题复习',
  dailyLimit: 20,
  reminderEnabled: 1,
  reminderTime: '19:30',
  reviewWeekDays: [1, 2, 3, 4, 5, 6, 7],
  subjectSettings
})

export default {
  name: 'ReviewPlanSetting',
  data() {
    return {
      loading: false,
      saving: false,
      profileSaving: false,
      backupLoading: false,
      form: defaultForm(),
      learningProfile: { grade: '', subject: '', bookId: null },
      gradeOptions: [],
      profileSubjectOptions: [],
      bookOptions: [],
      sprintSaving: false,
      sprint: null,
      sprintForm: { subject: '', examDate: '', scopeText: '', dailyMinutes: 30, targetQuestionCount: 5 },
      sprintDateOptions: { disabledDate: date => date.getTime() < new Date().setHours(0, 0, 0, 0) },
      weekOptions: [
        { value: 1, label: '周一' }, { value: 2, label: '周二' },
        { value: 3, label: '周三' }, { value: 4, label: '周四' },
        { value: 5, label: '周五' }, { value: 6, label: '周六' },
        { value: 7, label: '周日' }
      ],
      timeOptions: { start: '06:00', step: '00:30', end: '23:30' },
      rules: {
        planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
        dailyLimit: [{ required: true, message: '请设置每日复习上限', trigger: 'change' }],
        reviewWeekDays: [{ type: 'array', required: true, min: 1, message: '请至少选择一个复习日', trigger: 'change' }]
      }
    }
  },
  computed: {
    enabledSubjectCount() {
      return this.form.subjectSettings.filter(item => item.enabled === 1).length
    },
    reminderEnabled: {
      get() {
        return this.form.reminderEnabled === 1
      },
      set(value) {
        this.form.reminderEnabled = value ? 1 : 0
      }
    }
  },
  created() {
    this.loadSetting()
    this.loadLearningProfile()
    this.loadProfileOptions()
    this.loadSprint()
  },
  methods: {
    // 读取当前登录学生的唯一复习计划设置。
    async loadSetting() {
      this.loading = true
      try {
        const data = await reviewPlanSetting()
        this.form = Object.assign(defaultForm(), data || {})
        this.form.subjectSettings = (this.form.subjectSettings || []).map(item => ({
          subject: item.subject,
          subjectName: item.subjectName,
          enabled: item.enabled === 0 ? 0 : 1,
          dailyLimit: Number(item.dailyLimit || 5)
        }))
      } catch (error) {
        // 请求拦截器统一提示并处理登录跳转。
      } finally {
        this.loading = false
      }
    },
    // 前后端双重校验后保存，计划归属由服务端根据登录用户确定。
    saveSetting() {
      this.$refs.settingForm.validate(async valid => {
        if (!valid) return
        if (!this.form.subjectSettings.length) {
          this.$message.warning('暂无可设置的科目，请先维护科目字典')
          return
        }
        if (this.enabledSubjectCount === 0) {
          this.$message.warning('请至少启用一个复习科目')
          return
        }
        this.saving = true
        try {
          await updateReviewPlanSetting(this.form)
          this.$message.success('复习计划设置已保存')
          await this.loadSetting()
        } catch (error) {
          // 请求拦截器统一提示并处理登录跳转。
        } finally {
          this.saving = false
        }
      })
    },
    resetDefault() {
      const subjectSettings = this.form.subjectSettings.map(item => ({
        subject: item.subject,
        subjectName: item.subjectName,
        enabled: 1,
        dailyLimit: 5
      }))
      this.form = defaultForm(subjectSettings)
      this.$nextTick(() => this.$refs.settingForm.clearValidate())
    },
    async loadProfileOptions() {
      const [gradeOptions, subjectOptions] = await Promise.all([
        dictDataOptions({ dictType: 'grade' }).catch(() => []),
        dictDataOptions({ dictType: 'subject' }).catch(() => [])
      ])
      this.gradeOptions = gradeOptions
      this.profileSubjectOptions = subjectOptions
    },
    async loadLearningProfile() {
      const profile = await learningProfile().catch(() => ({}))
      this.learningProfile = Object.assign({ grade: '', subject: '', bookId: null }, profile || {})
      await this.loadBookOptions()
      if (!this.sprintForm.subject && this.learningProfile.subject) this.sprintForm.subject = this.learningProfile.subject
    },
    async profileScopeChanged() {
      this.learningProfile.bookId = null
      await this.loadBookOptions()
    },
    async loadBookOptions() {
      if (!this.learningProfile.grade || !this.learningProfile.subject) {
        this.bookOptions = []
        return
      }
      const page = await bookPageList({ current: 1, pageSize: 100, grade: this.learningProfile.grade, subject: this.learningProfile.subject }).catch(() => ({ list: [] }))
      this.bookOptions = page.list || []
    },
    async saveLearningProfile() {
      const profile = this.learningProfile
      if ((profile.grade && !profile.subject) || (!profile.grade && profile.subject)) {
        this.$message.warning('年级和科目需要同时设置')
        return
      }
      this.profileSaving = true
      try {
        await updateLearningProfile(profile)
        this.$message.success('学习偏好已保存')
        await this.loadLearningProfile()
      } finally {
        this.profileSaving = false
      }
    },
    async loadSprint() {
      this.sprint = await reviewExamSprint().catch(() => null)
      if (this.sprint) this.sprintForm = Object.assign({}, this.sprintForm, this.sprint)
    },
    async saveSprint() {
      if (!this.sprintForm.subject || !this.sprintForm.examDate) return this.$message.warning('请选择冲刺科目和考试日期')
      this.sprintSaving = true
      try {
        this.sprint = await saveReviewExamSprint(this.sprintForm)
        this.$message.success('冲刺短练已生成；常规到期复习不会被改写')
      } finally { this.sprintSaving = false }
    },
    async startSprintPractice() {
      const selectedQuestionList = this.sprint.candidates.map(item => ({ questionSource: 'WRONG_QUESTION', questionId: item.wrongQuestionId }))
      const session = await createPracticeSession({ title: `${this.sprint.subject}考前冲刺`, practiceType: 'EXAM_SPRINT', questionSource: 'WRONG_QUESTION', subject: this.sprint.subject, questionCount: selectedQuestionList.length, selectedQuestionList })
      this.$router.push({ path: '/system/review/practice', query: { sessionId: session.id } })
    },
    async downloadBackup() {
      this.backupLoading = true
      try {
        const backup = await learningDataBackup()
        const blob = new Blob([`\ufeff${JSON.stringify(backup, null, 2)}`], { type: 'application/json;charset=utf-8' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `学习数据备份-${new Date().toISOString().slice(0, 10)}.json`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(link.href)
        this.$message.success('数据备份已下载')
      } finally {
        this.backupLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.plan-setting-page { padding: 4px; color: #27314a; }
.setting-hero { display: flex; align-items: center; justify-content: space-between; min-height: 142px; padding: 28px 38px; margin-bottom: 18px; color: #fff; border-radius: 14px; background: linear-gradient(125deg, #5063e9, #737ff3 65%, #8a73e9); box-shadow: 0 12px 28px rgba(80, 99, 233, .2); }
.hero-eyebrow { margin-bottom: 7px; font-size: 12px; letter-spacing: 2px; opacity: .76; }
.setting-hero h1 { margin: 0 0 10px; font-size: 28px; }
.setting-hero p { margin: 0; opacity: .86; }
.hero-icon { margin-right: 20px; font-size: 64px; opacity: .2; }
.setting-card { margin-bottom: 18px; border: 0; border-radius: 12px; }
.setting-card ::v-deep .el-card__header { padding: 18px 22px; border-color: #eef0f5; }
.setting-card ::v-deep .el-card__body { padding: 22px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-header strong, .card-header span { display: block; }
.card-header strong { font-size: 16px; }
.card-header span { margin-top: 5px; color: #99a2b3; font-size: 12px; }
.field-tip { margin-left: 10px; color: #8d96a8; font-size: 13px; }
.week-group { display: flex; flex-wrap: wrap; }
.form-tip { margin-top: 9px; color: #9aa3b5; font-size: 12px; }
.subject-list { display: grid; gap: 12px; }
.subject-item { display: flex; align-items: center; gap: 16px; padding: 14px 16px; border: 1px solid #edf0f5; border-radius: 10px; background: #fafbfe; }
.subject-info { min-width: 130px; flex: 1; }
.subject-name { display: block; }
.subject-name { color: #39435a; font-size: 15px; font-weight: 600; }
.subject-limit { display: flex; align-items: center; gap: 8px; color: #7f899d; font-size: 12px; transition: opacity .2s; }
.subject-limit.disabled { opacity: .5; }
.subject-limit ::v-deep .el-input-number { width: 120px; }
.reminder-content { display: flex; align-items: center; gap: 14px; transition: opacity .2s; }
.reminder-content.disabled { opacity: .62; }
.reminder-clock { display: flex; align-items: center; justify-content: center; width: 48px; height: 48px; color: #596bf2; border-radius: 13px; background: #eef0ff; font-size: 22px; }
.reminder-copy { flex: 1; }
.reminder-copy strong, .reminder-copy span { display: block; }
.reminder-copy span { margin-top: 5px; color: #939cad; font-size: 12px; }
.preview-card { min-height: 430px; }
.preview-name { padding: 17px; margin-bottom: 12px; color: #5364dd; border-radius: 10px; background: #f0f2ff; font-size: 17px; font-weight: 600; }
.preview-row { display: flex; justify-content: space-between; padding: 14px 2px; border-bottom: 1px solid #f0f2f6; font-size: 13px; }
.preview-row span { color: #8c95a7; }
.algorithm-tip { display: flex; gap: 10px; padding: 14px; margin: 18px 0; color: #5969d8; border-radius: 9px; background: #f4f5ff; }
.algorithm-tip i { margin-top: 2px; font-size: 18px; }
.algorithm-tip strong, .algorithm-tip span { display: block; }
.algorithm-tip span { margin-top: 5px; color: #818ba5; font-size: 12px; line-height: 1.6; }
.save-button, .reset-button { width: 100%; margin: 0 0 10px; }
.profile-form ::v-deep .el-select { width: 100%; }.backup-card p { margin-top: 0; color: #7f899d; line-height: 1.7; }
@media (max-width: 768px) {
  .setting-hero { padding: 24px; }
  .setting-hero h1 { font-size: 24px; }
  .hero-icon { display: none; }
  .reminder-content { align-items: flex-start; flex-wrap: wrap; }
  .reminder-copy { min-width: calc(100% - 70px); }
  .subject-item { align-items: flex-start; flex-wrap: wrap; }
  .subject-info { min-width: calc(100% - 70px); }
  .subject-limit { width: 100%; }
}
</style>
