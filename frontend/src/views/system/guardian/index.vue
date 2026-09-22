<template>
  <div class="app-container guardian-page">
    <el-card shadow="never" class="hero-card">
      <div slot="header" class="card-header">
        <span>家庭协作</span>
        <div>
          <el-button type="primary" size="small" @click="inviteVisible = true">创建家长邀请码</el-button>
          <el-button size="small" @click="acceptVisible = true">接受邀请码</el-button><el-button size="small" @click="openWeeklySetting">周报提醒</el-button><el-button size="small" @click="openAssistedCapture">代上传错题</el-button>
        </div>
      </div>
      <p>学生确认后，家长仅可查看学习汇总。题目原图、作答、答案、订正和掌握反馈均不会在此展示或操作。</p>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header" class="card-header"><span>家长代上传</span><el-button size="mini" @click="loadAssistedCaptures">刷新</el-button></div>
      <p class="tip">家长只能上传材料和整理来源；学生确认前不会创建错题或影响学习画像。</p>
      <el-table :data="assistedCaptures" size="small" empty-text="暂无代上传任务"><el-table-column prop="studentName" label="学生" min-width="110"/><el-table-column prop="subject" label="科目" min-width="100"/><el-table-column prop="createTime" label="提交时间" min-width="165"/><el-table-column label="状态" min-width="115"><template slot-scope="{ row }"><el-tag size="mini" :type="assistedStatusType(row.status)">{{ assistedStatusText(row.status) }}</el-tag></template></el-table-column><el-table-column label="操作" min-width="160"><template slot-scope="{ row }"><el-button v-if="row.status === 0 && row.studentUserId === currentUserId" type="primary" size="mini" @click="confirmAssistedCapture(row)">学生确认</el-button><el-button v-if="row.status === 0" type="text" class="danger" @click="revokeAssistedCapture(row)">撤销</el-button><el-button v-if="row.status === 1" type="text" @click="goCapture(row)">进入采集</el-button></template></el-table-column></el-table>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header" class="card-header"><span>我的绑定</span><el-button size="mini" @click="loadBindings">刷新</el-button></div>
      <el-empty v-if="!bindings.length" description="暂未建立家庭绑定" :image-size="76" />
      <el-table v-else :data="bindings" stripe>
        <el-table-column label="学生" min-width="130"><template slot-scope="{ row }">{{ row.studentName || '-' }}</template></el-table-column>
        <el-table-column label="家长" min-width="130"><template slot-scope="{ row }">{{ row.guardianName || '等待接受邀请码' }}</template></el-table-column>
        <el-table-column label="关系" prop="relationType" min-width="110" />
        <el-table-column label="状态" min-width="120"><template slot-scope="{ row }"><el-tag :type="statusType(row.status)" size="mini">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="学生最近登录" min-width="170"><template slot-scope="{ row }">{{ formatDateTime(row.studentLastLoginTime) }}</template></el-table-column>
        <el-table-column label="操作" min-width="230">
          <template slot-scope="{ row }">
            <el-button v-if="row.currentRole === 'STUDENT' && row.status === 1" type="primary" size="mini" @click="confirm(row)">确认绑定</el-button>
            <el-button v-if="row.currentRole === 'GUARDIAN' && row.status === 2" type="primary" size="mini" @click="viewOverview(row)">查看概览</el-button>
            <el-button v-if="row.status !== 3" type="text" class="danger" @click="revoke(row)">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="overview" shadow="never" class="section-card overview-card">
      <div slot="header" class="card-header"><span>{{ overview.studentName }}的学习概览</span><span><span class="readonly">只读汇总</span><el-button size="mini" @click="viewWeeklyReport">查看本周周报</el-button><el-button size="mini" @click="exportOverview">导出汇总</el-button></span></div>
      <div class="metrics">
        <div class="metric"><b>{{ overview.todayDueCount }}</b><span>今日到期</span></div>
        <div class="metric warning"><b>{{ overview.overdueCount }}</b><span>逾期任务</span></div>
        <div class="metric"><b>{{ overview.sevenDayReviewCount }}</b><span>7日有效复习</span></div>
        <div class="metric"><b>{{ overview.wrongQuestionCount ? `${overview.correctionRate}%` : '样本不足' }}</b><span>订正率</span></div>
        <div class="metric"><b>{{ overview.sevenDayReviewCount ? `${overview.independentCorrectRate}%` : '样本不足' }}</b><span>独立正确率</span></div>
      </div>
      <div class="suggestion"><strong>本周建议：</strong>{{ overview.weeklySuggestion }}</div>
      <div class="weak-title">薄弱知识点（汇总）</div>
      <el-table :data="overview.weakPointList || []" size="small" empty-text="暂无可汇总的薄弱知识点">
        <el-table-column prop="learningPoint" label="知识点" />
        <el-table-column prop="wrongQuestionCount" label="关联错题数" width="130" />
        <el-table-column prop="pendingCorrectionCount" label="待订正数" width="120" />
      </el-table>
    </el-card>

    <el-dialog title="创建家长邀请码" :visible.sync="inviteVisible" width="420px">
      <el-form label-width="90px"><el-form-item label="关系"><el-select v-model="inviteForm.relationType" class="full"><el-option label="监护人" value="GUARDIAN" /><el-option label="父亲" value="FATHER" /><el-option label="母亲" value="MOTHER" /></el-select></el-form-item></el-form>
      <p class="tip">邀请码仅展示一次，有效期 7 天；家长接受后仍需由学生确认。</p>
      <div slot="footer"><el-button @click="inviteVisible = false">取消</el-button><el-button type="primary" @click="createInvite">生成</el-button></div>
    </el-dialog>
    <el-dialog title="家长邀请码" :visible.sync="codeVisible" width="420px"><p>请安全地发送给家长：</p><el-input :value="createdCode" readonly><el-button slot="append" @click="copyCode">复制</el-button></el-input><p class="tip">有效至 {{ createdExpireTime || '-' }}</p></el-dialog>
    <el-dialog title="接受家长邀请码" :visible.sync="acceptVisible" width="420px"><el-input v-model="acceptCode" placeholder="请输入学生提供的邀请码" /><div slot="footer"><el-button @click="acceptVisible = false">取消</el-button><el-button type="primary" @click="acceptInvite">接受并等待学生确认</el-button></div></el-dialog>
    <el-dialog title="家长代上传错题" :visible.sync="assistedVisible" width="560px"><el-form :model="assistedForm" label-width="94px"><el-form-item label="学生"><el-select v-model="assistedForm.studentUserId" class="full"><el-option v-for="item in activeChildren" :key="item.studentUserId" :label="item.studentName" :value="item.studentUserId" /></el-select></el-form-item><el-form-item label="年级"><el-input v-model.trim="assistedForm.grade" placeholder="如：七年级"/></el-form-item><el-form-item label="科目"><el-input v-model.trim="assistedForm.subject" placeholder="如：数学"/></el-form-item><el-form-item label="题型"><el-input v-model.trim="assistedForm.questionType" placeholder="如：选择题"/></el-form-item><el-form-item label="来源"><el-input v-model.trim="assistedForm.source" placeholder="如：日常练习"/></el-form-item><el-form-item label="材料"><el-upload :http-request="uploadAssistedFile" :file-list="assistedFiles" :on-remove="removeAssistedFile" multiple><el-button size="small">上传图片/PDF</el-button></el-upload></el-form-item></el-form><div slot="footer"><el-button @click="assistedVisible = false">取消</el-button><el-button type="primary" :loading="assistedSubmitting" @click="submitAssistedCapture">提交，等待学生确认</el-button></div></el-dialog>
    <el-dialog title="家长周报提醒" :visible.sync="weeklyVisible" width="440px"><el-form label-width="100px"><el-form-item label="学生"><el-select v-model="weeklyForm.studentUserId" class="full"><el-option v-for="item in activeChildren" :key="item.studentUserId" :label="item.studentName" :value="item.studentUserId" /></el-select></el-form-item><el-form-item label="站内提醒"><el-switch v-model="weeklyForm.siteNotificationEnabled" :active-value="1" :inactive-value="0" /></el-form-item><el-form-item label="邮件提醒"><el-switch v-model="weeklyForm.emailEnabled" :active-value="1" :inactive-value="0" /><p class="tip">邮件通道需在部署环境配置后生效；默认使用站内提醒。</p></el-form-item></el-form><div slot="footer"><el-button @click="weeklyVisible = false">取消</el-button><el-button type="primary" @click="saveWeeklySetting">保存</el-button></div></el-dialog>
    <el-dialog :title="`${weeklyReport.studentName || ''}的本周学习周报`" :visible.sync="weeklyReportVisible" width="560px">
      <div v-if="weeklyReport.studentUserId" class="weekly-report">
        <div class="weekly-metrics"><div><b>{{ weeklyReport.newWrongQuestionCount }}</b><span>新增错题</span></div><div><b>{{ weeklyReport.correctedCount }}</b><span>完成订正</span></div><div><b>{{ weeklyReport.effectiveReviewCount }}</b><span>有效复习</span></div><div><b>{{ weeklyReport.effectiveReviewCount ? `${weeklyReport.retentionRate}%` : '样本不足' }}</b><span>保持率</span></div><div class="warning"><b>{{ weeklyReport.overdueCount }}</b><span>逾期任务</span></div></div>
        <div class="suggestion"><strong>下周建议：</strong>{{ weeklyReport.nextWeekSuggestion }}</div>
        <p class="tip">周报仅含学习汇总，不展示题目、原始作答、答案解析或复习反馈。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { currentGuardianBindingList, createGuardianInvitation, acceptGuardianInvitation, confirmGuardianBinding, revokeGuardianBinding, guardianStudentOverview, exportGuardianStudentOverview, guardianWeeklyReport, guardianWeeklyReportSubscriptionList, updateGuardianWeeklyReportSubscription, guardianAssistedCaptureList, createGuardianAssistedCapture, confirmGuardianAssistedCapture, revokeGuardianAssistedCapture } from '@/api/system/guardian'
import { filePolicy, uploadFile } from '@/api/system/file'
import { getCurrentUserInfo } from '@/api/system/user'

export default {
  name: 'GuardianCollaboration',
  data() { return { bindings: [], overview: null, weeklyReport: {}, assistedCaptures: [], currentUserId: null, inviteVisible: false, acceptVisible: false, codeVisible: false, weeklyVisible: false, weeklyReportVisible: false, assistedVisible: false, assistedSubmitting: false, assistedFiles: [], assistedForm: { studentUserId: null, grade: '', subject: '', questionType: '', source: '', imageFileIds: [] }, weeklyForm: { studentUserId: null, siteNotificationEnabled: 1, emailEnabled: 0 }, weeklySubscriptions: [], inviteForm: { relationType: 'GUARDIAN' }, acceptCode: '', createdCode: '', createdExpireTime: '' } },
  created() { this.loadBindings(); this.loadAssistedCaptures(); getCurrentUserInfo().then(data => { this.currentUserId = Number(data.id) }) },
  methods: {
    loadBindings() { currentGuardianBindingList().then(data => { this.bindings = data || [] }) },
    statusText(status) { return ({ 0: '待家长接受', 1: '待学生确认', 2: '有效', 3: '已解绑' })[status] || '未知' },
    statusType(status) { return ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'info' })[status] || 'info' },
    formatDateTime(value) { return value ? String(value).replace('T', ' ') : '-' },
    createInvite() { createGuardianInvitation(this.inviteForm).then(data => { this.inviteVisible = false; this.createdCode = data.invitationCode; this.createdExpireTime = data.expireTime; this.codeVisible = true; this.loadBindings() }) },
    acceptInvite() { if (!this.acceptCode) return this.$message.warning('请输入邀请码'); acceptGuardianInvitation({ invitationCode: this.acceptCode.trim() }).then(() => { this.$message.success('已接受，请等待学生确认'); this.acceptVisible = false; this.acceptCode = ''; this.loadBindings() }) },
    confirm(row) { confirmGuardianBinding({ relationId: row.relationId }).then(() => { this.$message.success('绑定已生效'); this.loadBindings() }) },
    revoke(row) { this.$confirm('解绑后将立即失去该学生的学习数据访问权限，是否继续？', '确认解绑', { type: 'warning' }).then(() => revokeGuardianBinding({ relationId: row.relationId })).then(() => { this.$message.success('已解绑'); this.overview = null; this.loadBindings() }).catch(() => {}) },
    viewOverview(row) { guardianStudentOverview({ studentUserId: row.studentUserId }).then(data => { this.overview = data }) },
    viewWeeklyReport() { guardianWeeklyReport({ studentUserId: this.overview.studentUserId }).then(data => { this.weeklyReport = data || {}; this.weeklyReportVisible = true }) },
    exportOverview() { exportGuardianStudentOverview({ studentUserId: this.overview.studentUserId }).then(data => { const rows = [['学生', data.studentName], ['今日到期', data.todayDueCount], ['逾期任务', data.overdueCount], ['7日有效复习', data.sevenDayReviewCount], ['订正率', data.wrongQuestionCount ? `${data.correctionRate}%` : '样本不足'], ['独立正确率', data.sevenDayReviewCount ? `${data.independentCorrectRate}%` : '样本不足'], ['本周建议', data.weeklySuggestion]]; const blob = new Blob([rows.map(row => row.map(value => `"${String(value == null ? '' : value).replace(/"/g, '""')}"`).join(',')).join('\n')], { type: 'text/csv;charset=utf-8' }); const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = `${data.studentName || '学生'}-学习汇总.csv`; link.click(); URL.revokeObjectURL(link.href); this.$message.success('汇总已导出') }) },
    copyCode() { if (!navigator.clipboard) return this.$message.info('请手动复制邀请码'); navigator.clipboard.writeText(this.createdCode).then(() => this.$message.success('已复制')) }
    ,openWeeklySetting() { guardianWeeklyReportSubscriptionList().then(data => { this.weeklySubscriptions = data || []; const child = this.activeChildren[0]; if (!child) return this.$message.info('请先建立有效的家长绑定'); const old = this.weeklySubscriptions.find(item => item.studentUserId === child.studentUserId); this.weeklyForm = { studentUserId: child.studentUserId, siteNotificationEnabled: old ? old.siteNotificationEnabled : 1, emailEnabled: old ? old.emailEnabled : 0 }; this.weeklyVisible = true }) }
    ,saveWeeklySetting() { updateGuardianWeeklyReportSubscription(this.weeklyForm).then(() => { this.$message.success('周报提醒已保存'); this.weeklyVisible = false }) }
    ,loadAssistedCaptures() { guardianAssistedCaptureList().then(data => { this.assistedCaptures = data || [] }) }
    ,openAssistedCapture() { const child = this.activeChildren[0]; if (!child) return this.$message.info('请先建立有效的家长绑定'); this.assistedForm = { studentUserId: child.studentUserId, grade: '', subject: '', questionType: '', source: '', imageFileIds: [] }; this.assistedFiles = []; this.assistedVisible = true }
    ,uploadAssistedFile(option) { filePolicy({ uploadType: 'wrongQuestion' }).then(policy => { const form = new FormData(); form.append('file', option.file); form.append('signature', policy.signature); return uploadFile(form) }).then(file => { option.file.captureFileId = file.id; this.assistedForm.imageFileIds.push(file.id); option.onSuccess(file) }).catch(option.onError) }
    ,removeAssistedFile(file) { this.assistedForm.imageFileIds = this.assistedForm.imageFileIds.filter(id => id !== file.captureFileId) }
    ,submitAssistedCapture() { const form = this.assistedForm; if (!form.grade || !form.subject || !form.questionType || !form.source || !form.imageFileIds.length) return this.$message.warning('请完整填写信息并上传材料'); this.assistedSubmitting = true; createGuardianAssistedCapture(form).then(() => { this.$message.success('已提交，等待学生确认'); this.assistedVisible = false; this.loadAssistedCaptures() }).finally(() => { this.assistedSubmitting = false }) }
    ,assistedStatusText(status) { return ({ 0: '待学生确认', 1: '已创建采集', 2: '已撤销' })[status] || '未知' }
    ,assistedStatusType(status) { return ({ 0: 'warning', 1: 'success', 2: 'info' })[status] || 'info' }
    ,confirmAssistedCapture(row) { confirmGuardianAssistedCapture({ relationId: row.id }).then(taskId => { this.$message.success('已确认，正在创建采集任务'); this.loadAssistedCaptures(); this.$router.push({ path: '/system/wrongquestion/capture', query: { taskId } }) }) }
    ,revokeAssistedCapture(row) { this.$confirm('撤销后学生将不能再确认该材料，是否继续？', '确认撤销', { type: 'warning' }).then(() => revokeGuardianAssistedCapture({ relationId: row.id })).then(() => { this.$message.success('已撤销'); this.loadAssistedCaptures() }).catch(() => {}) }
    ,goCapture(row) { this.$router.push({ path: '/system/wrongquestion/capture', query: { taskId: row.captureTaskId } }) }
  },
  computed: {
    activeChildren() { return this.bindings.filter(item => item.currentRole === 'GUARDIAN' && item.status === 2) }
  }
}
</script>

<style scoped>
.guardian-page { max-width: 1240px; margin: 0 auto; }.hero-card { background: #f1f7ff; }.section-card { margin-top: 18px; }.card-header { display:flex; align-items:center; justify-content:space-between; font-size:16px; font-weight:600; }.hero-card p,.tip,.readonly { color:#64748b; font-size:13px; }.full { width:100%; }.danger { color:#f56c6c; }.metrics,.weekly-metrics { display:grid; grid-template-columns:repeat(5,1fr); gap:12px; }.metric,.weekly-metrics > div { padding:18px 12px; border-radius:8px; background:#f8fafc; text-align:center; }.metric b,.weekly-metrics b { display:block; color:#1d4ed8; font-size:26px; }.metric span,.weekly-metrics span { color:#64748b; font-size:13px; }.metric.warning b,.weekly-metrics .warning b { color:#dc2626; }.suggestion { margin:22px 0 14px; padding:14px; border-radius:6px; background:#fff7ed; color:#9a3412; }.weak-title { margin:12px 0; font-weight:600; } @media (max-width:900px) { .metrics,.weekly-metrics { grid-template-columns:repeat(2,1fr); } }
</style>
