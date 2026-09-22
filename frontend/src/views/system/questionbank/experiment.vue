<template>
  <div class="app-container"><el-card shadow="never"><div slot="header"><strong>A/B 推荐实验管理</strong></div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="实验名称" prop="experimentName"><el-input v-model="form.experimentName" maxlength="100"/></el-form-item>
      <el-form-item label="启用"><el-switch v-model="form.enable" :active-value="1" :inactive-value="0"/></el-form-item>
      <el-form-item label="A组流量"><el-slider v-model="form.groupATraffic" show-input :min="0" :max="100"/><span>A组 {{ form.groupATraffic }}%，B组 {{ 100-form.groupATraffic }}%</span></el-form-item>
      <el-form-item label="实验时间" prop="timeRange"><el-date-picker v-model="form.timeRange" type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" class="full"/></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea"/></el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存配置</el-button><el-button @click="form=emptyForm()">新建实验</el-button></el-form-item>
    </el-form>
    <el-divider content-position="left">实验历史与效果</el-divider>
    <el-table :data="history" border><el-table-column prop="operationTime" label="变更时间" width="170"/><el-table-column prop="experimentName" label="实验" min-width="150"/><el-table-column label="状态" width="70"><template slot-scope="{row}">{{row.enable===1?'启用':'停用'}}</template></el-table-column><el-table-column label="流量" width="130"><template slot-scope="{row}">A {{row.groupATraffic}}% / B {{row.groupBTraffic}}%</template></el-table-column><el-table-column label="效果" min-width="260"><template slot-scope="{row}">A：{{row.groupAAnsweredCount}}题 / {{row.groupACorrectRate}}%，B：{{row.groupBAnsweredCount}}题 / {{row.groupBCorrectRate}}%<br>P={{row.pValue}}，{{row.significant?'达到显著':'暂未显著'}}</template></el-table-column></el-table>
  </el-card></div>
</template>
<script>
import { questionExperimentDetail, questionExperimentHistory, saveQuestionExperiment } from '@/api/system/questionBank'
export default { name: 'QuestionExperimentAdmin', data() { return { form: this.emptyForm(), history: [], rules: { experimentName: [{ required: true, message: '请输入实验名称', trigger: 'blur' }], timeRange: [{ type: 'array', required: true, message: '请选择实验时间', trigger: 'change' }] } } }, created() { this.load() }, methods: { emptyForm() { return { id: null, experimentName: '相似题推荐策略实验', enable: 0, groupATraffic: 50, timeRange: [], remark: '' } }, load() { Promise.all([questionExperimentDetail(), questionExperimentHistory()]).then(([detail, history]) => { this.form = detail ? Object.assign({}, detail, { timeRange: [detail.startTime, detail.endTime] }) : this.emptyForm(); this.history = history || [] }) }, save() { this.$refs.formRef.validate(ok => { if (!ok) return; const data = Object.assign({}, this.form, { startTime: this.form.timeRange[0], endTime: this.form.timeRange[1] }); delete data.timeRange; delete data.groupBTraffic; delete data.running; saveQuestionExperiment(data).then(() => { this.$message.success('实验配置已保存'); this.load() }) }) } } }
</script>
<style scoped>.full{width:100%}</style>
