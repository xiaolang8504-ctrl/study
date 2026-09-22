<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header" class="header"><strong>相似题练习历史</strong><el-button size="small" @click="$router.push('/system/similar-practice')">返回练习</el-button></div>
      <el-form :inline="true" size="small">
        <el-form-item label="作答结果"><el-select v-model="query.isCorrect" clearable placeholder="全部"><el-option label="正确" :value="1"/><el-option label="错误" :value="0"/></el-select></el-form-item>
        <el-form-item label="实验分组"><el-select v-model="query.experimentGroup" clearable placeholder="全部"><el-option label="A组" value="A"/><el-option label="B组" value="B"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="wrongQuestionTitle" label="来源错题" min-width="170" show-overflow-tooltip/>
        <el-table-column prop="questionTitle" label="练习题" min-width="190" show-overflow-tooltip/>
        <el-table-column label="匹配" width="90"><template slot-scope="{row}">{{ Math.round(row.recommendScore || 0) }}%</template></el-table-column>
        <el-table-column prop="experimentGroup" label="实验组" width="75"/>
        <el-table-column prop="studentAnswer" label="我的答案" min-width="130" show-overflow-tooltip/>
        <el-table-column label="结果" width="80"><template slot-scope="{row}"><el-tag v-if="row.isCorrect !== null" :type="row.isCorrect === 1 ? 'success' : 'danger'">{{ row.isCorrect === 1 ? '正确' : '错误' }}</el-tag><span v-else>未作答</span></template></el-table-column>
        <el-table-column label="耗时" width="90"><template slot-scope="{row}">{{ row.durationSeconds == null ? '-' : `${row.durationSeconds}秒` }}</template></el-table-column>
        <el-table-column prop="exposureTime" label="推荐时间" width="170"/>
        <el-table-column prop="answerTime" label="作答时间" width="170"/>
      </el-table>
      <el-pagination class="pagination" background layout="total, prev, pager, next" :total="total" :current-page="query.current" :page-size="query.pageSize" @current-change="page=>{query.current=page;load()}"/>
    </el-card>
  </div>
</template>
<script>
import { questionPracticeHistoryPageList } from '@/api/system/questionBank'
export default { name: 'SimilarPracticeHistory', data() { return { loading: false, rows: [], total: 0, query: { current: 1, pageSize: 20, isCorrect: null, experimentGroup: '' } } }, created() { this.load() }, methods: { search() { this.query.current = 1; this.load() }, load() { this.loading = true; questionPracticeHistoryPageList(this.query).then(data => { this.rows = data.list || []; this.total = Number(data.total || 0) }).finally(() => { this.loading = false }) } } }
</script>
<style scoped>.header{display:flex;justify-content:space-between;align-items:center}.pagination{margin-top:18px;text-align:right}</style>
