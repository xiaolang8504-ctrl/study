<template>
  <div class="app-container question-bank-page">
    <el-card shadow="never">
      <div slot="header" class="header-row">
        <div><strong>审核精品题库</strong><span class="hint">只有审核通过且启用的题目会进入相似题推荐</span></div>
        <div>
          <el-button size="small" icon="el-icon-data-analysis" @click="$router.push('/system/question-experiment')">A/B实验</el-button>
          <el-button size="small" icon="el-icon-s-check" @click="openAppeals">教师复核</el-button>
          <el-button size="small" icon="el-icon-document-delete" @click="openDuplicateHistory">重复题清理</el-button>
          <el-button size="small" icon="el-icon-warning-outline" @click="openReports">举报处理</el-button>
          <el-button size="small" @click="openKnowledgeManagement">知识点管理</el-button>
          <el-button size="small" type="warning" icon="el-icon-upload2" @click="openOcrImport">导入A4文件</el-button>
          <el-button size="small" type="primary" icon="el-icon-plus" @click="openForm()">新增题目</el-button>
        </div>
      </div>
      <el-row :gutter="12" class="statistics-row">
        <el-col :span="6"><div class="stat"><b>{{ statistics.exposureCount || 0 }}</b><span>推荐曝光</span></div></el-col>
        <el-col :span="6"><div class="stat"><b>{{ statistics.answerRate || 0 }}%</b><span>作答率</span></div></el-col>
        <el-col :span="6"><div class="stat"><b>{{ statistics.correctRate || 0 }}%</b><span>正确率</span></div></el-col>
        <el-col :span="6"><div class="stat"><b>{{ statistics.reportCount || 0 }}</b><span>累计举报</span></div></el-col>
      </el-row>
      <el-alert class="experiment-alert" type="info" :closable="false" show-icon :title="`A/B推荐实验：A组知识点优先，已作答 ${statistics.groupAAnsweredCount || 0} 题，正确率 ${statistics.groupACorrectRate || 0}%；B组全文优先，已作答 ${statistics.groupBAnsweredCount || 0} 题，正确率 ${statistics.groupBCorrectRate || 0}%`" />
      <el-alert v-if="statistics.pValue !== undefined" class="experiment-alert" :type="statistics.significant ? 'success' : 'warning'" :closable="false" show-icon :title="`显著性分析：B组较A组 ${statistics.lift >= 0 ? '提升' : '下降'} ${Math.abs(statistics.lift || 0)} 个百分点，Z=${statistics.zScore || 0}，P=${statistics.pValue}，${statistics.significant ? '已达到95%统计显著' : '尚未达到95%统计显著'}`" />
      <el-form :inline="true" size="small">
        <el-form-item><el-input v-model="query.keyWord" placeholder="搜索题目" clearable @keyup.enter.native="loadList" /></el-form-item>
        <el-form-item><el-select v-model="query.grade" filterable clearable placeholder="请选择年级"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item><el-select v-model="query.subject" filterable clearable placeholder="请选择科目"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item>
          <el-select v-model="query.reviewStatus" placeholder="审核状态" clearable>
            <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="query.current = 1; loadList()">查询</el-button></el-form-item>
        <el-form-item><el-button type="success" :disabled="selectedQuestionIds.length===0" @click="batchReview(1)">批量通过</el-button><el-button type="warning" :disabled="selectedQuestionIds.length===0" @click="batchReview(2)">批量驳回</el-button></el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="rows" border @selection-change="selection=>selectedQuestionIds=selection.map(item=>item.id)">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="questionTitle" label="题目" min-width="260" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="科目" width="90"><template slot-scope="{row}">{{ row.subjectName || row.subject }}</template></el-table-column>
        <el-table-column prop="gradeName" label="年级" width="90"><template slot-scope="{row}">{{ row.gradeName || row.grade }}</template></el-table-column>
        <el-table-column label="知识点" min-width="160"><template slot-scope="{row}"><el-tag v-for="item in row.knowledgePointNames" :key="item" size="mini">{{ item }}</el-tag></template></el-table-column>
        <el-table-column prop="difficulty" label="难度" width="70" />
        <el-table-column label="审核" width="90"><template slot-scope="{row}"><el-tag :type="statusType(row.reviewStatus)">{{ statusText(row.reviewStatus) }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="75"><template slot-scope="{row}">{{ row.enable === 1 ? '启用' : '停用' }}</template></el-table-column>
        <el-table-column label="操作" width="310" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" @click="openForm(row)">编辑</el-button>
            <el-button type="text" @click="openHistory(row)">历史</el-button>
            <el-button v-if="row.reviewStatus !== 1" type="text" class="success" @click="review(row, 1)">通过</el-button>
            <el-button v-if="row.reviewStatus === 1" type="text" class="warning" @click="review(row, 2)">驳回</el-button>
            <el-button type="text" class="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" background layout="total, prev, pager, next" :total="total" :current-page="query.current" :page-size="query.pageSize" @current-change="page => { query.current = page; loadList() }" />
    </el-card>

    <el-dialog :title="form.id ? '编辑题目（保存后重新审核）' : '新增题目'" :visible.sync="formOpen" width="760px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16"><el-col :span="8"><el-form-item label="年级编码" prop="grade"><el-select v-model="form.grade" filterable class="full" placeholder="请选择年级" @change="changeQuestionScope"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col><el-col :span="8"><el-form-item label="科目编码" prop="subject"><el-select v-model="form.subject" filterable class="full" placeholder="请选择科目" @change="changeQuestionScope"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col><el-col :span="8"><el-form-item label="题型编码" prop="questionType"><el-select v-model="form.questionType" filterable class="full" placeholder="请选择题型"><el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="题目标题" prop="questionTitle"><el-input v-model="form.questionTitle" /></el-form-item>
        <el-form-item label="题目内容" prop="questionContent"><el-input v-model="form.questionContent" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="内容格式"><el-radio-group v-model="form.contentFormat"><el-radio label="TEXT">普通文本</el-radio><el-radio label="LATEX">LaTeX公式</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="form.contentFormat === 'LATEX'" label="公式预览"><div class="latex-preview"><latex-renderer :source="form.questionContent" /></div></el-form-item>
        <el-form-item label="题目图片"><question-image-manager v-model="form.images" /></el-form-item>
        <el-form-item label="题目选项"><question-option-editor v-model="form.optionsJson" /></el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer"><el-input v-model="form.correctAnswer" /></el-form-item>
        <el-form-item label="判题方式" prop="judgeMode"><el-radio-group v-model="form.judgeMode"><el-radio label="AUTO">客观题自动判题</el-radio><el-radio label="SELF">主观题查看答案后自评</el-radio></el-radio-group></el-form-item>
        <el-form-item label="解析"><el-input v-model="form.analysis" type="textarea" :rows="3" /></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="难度"><el-rate v-model="form.difficulty" /></el-form-item></el-col><el-col :span="12"><el-form-item label="来源" prop="source"><el-select v-model="form.source" filterable class="full" placeholder="请选择来源"><el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="知识点"><el-select v-model="form.knowledgePointIds" multiple filterable class="full"><el-option v-for="p in knowledge" :key="p.id" :label="p.pointName" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enable" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="formOpen=false">取消</el-button><el-button type="primary" @click="submit">保存并送审</el-button></div>
    </el-dialog>

    <el-dialog title="A4文件扫描导入题库" :visible.sync="ocrImportOpen" width="680px" append-to-body>
      <el-alert type="info" :closable="false" show-icon title="支持图片、PDF、Word；识别出的多道题会自动进入题库待审核列表。" class="dialog-alert" />
      <el-form ref="ocrImportFormRef" :model="ocrImportForm" :rules="ocrImportRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="年级" prop="grade"><el-select v-model="ocrImportForm.grade" class="full" filterable @change="changeOcrScope"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="科目" prop="subject"><el-select v-model="ocrImportForm.subject" class="full" filterable @change="changeOcrScope"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="默认题型"><el-select v-model="ocrImportForm.questionType" class="full" clearable placeholder="优先自动识别"><el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源" prop="source"><el-select v-model="ocrImportForm.source" class="full" filterable><el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="判题方式" prop="judgeMode"><el-select v-model="ocrImportForm.judgeMode" class="full"><el-option label="主观题自评" value="SELF" /><el-option label="客观题自动判题" value="AUTO" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="默认难度"><el-rate v-model="ocrImportForm.difficulty" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="知识点"><el-select v-model="ocrImportForm.knowledgePointIds" multiple filterable clearable class="full"><el-option v-for="item in ocrKnowledge" :key="item.id" :label="item.pointName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="A4文件" prop="fileId">
          <el-upload action="" accept=".jpg,.jpeg,.png,.webp,.bmp,.pdf,.doc,.docx,image/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" :show-file-list="false" :http-request="uploadOcrFile" :before-upload="beforeOcrFileUpload">
            <el-button icon="el-icon-upload2" :loading="ocrUploading">上传图片/PDF/Word</el-button>
          </el-upload>
          <div v-if="ocrFileName" class="ocr-file"><i class="el-icon-document" /><span>{{ ocrFileName }}</span><el-tag size="mini" type="success">已上传</el-tag></div>
        </el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="ocrImportOpen=false">取消</el-button><el-button type="primary" :loading="ocrImporting" @click="submitOcrImport">扫描并自动入库</el-button></div>
    </el-dialog>

    <el-dialog title="知识点树形维护" :visible.sync="knowledgeOpen" width="920px" append-to-body>
      <el-form :inline="true" size="small" class="knowledge-filter">
        <el-form-item label="年级"><el-select v-model="pointQuery.grade" filterable clearable placeholder="全部年级" @change="loadKnowledgeTree"><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item label="科目"><el-select v-model="pointQuery.subject" filterable clearable placeholder="全部科目" @change="loadKnowledgeTree"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-plus" @click="startCreatePoint(null)">新增根节点</el-button></el-form-item>
      </el-form>
      <el-table :data="knowledgeTree" row-key="id" border default-expand-all :tree-props="{ children: 'children' }" max-height="480">
        <el-table-column prop="pointName" label="知识点名称" min-width="190" />
        <el-table-column prop="pointCode" label="编码" min-width="140" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="subject" label="科目" width="100" />
        <el-table-column prop="sort" label="排序" width="65" />
        <el-table-column label="状态" width="70"><template slot-scope="{row}"><el-tag :type="row.enable === 1 ? 'success' : 'info'" size="mini">{{ row.enable === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="190" fixed="right"><template slot-scope="{row}"><el-button type="text" @click="startCreatePoint(row)">新增下级</el-button><el-button type="text" @click="editPoint(row)">编辑</el-button><el-button type="text" class="danger" @click="removePoint(row)">删除</el-button></template></el-table-column>
      </el-table>
      <el-dialog :title="pointForm.id ? '编辑知识点' : '新增知识点'" :visible.sync="pointFormOpen" width="520px" append-to-body>
        <el-form ref="pointFormRef" :model="pointForm" :rules="pointRules" label-width="95px">
          <el-form-item label="父级节点"><el-select v-model="pointForm.parentId" class="full" clearable><el-option label="根节点" :value="0" /><el-option v-for="item in pointFlatOptions" :key="item.id" :label="item.label" :value="item.id" :disabled="item.id === pointForm.id" /></el-select></el-form-item>
          <el-form-item label="知识点编码" prop="pointCode"><el-input v-model.trim="pointForm.pointCode" maxlength="80" /></el-form-item>
          <el-form-item label="知识点名称" prop="pointName"><el-input v-model.trim="pointForm.pointName" maxlength="100" /></el-form-item>
          <el-form-item label="年级" prop="grade"><el-select v-model="pointForm.grade" class="full" filterable><el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
          <el-form-item label="科目" prop="subject"><el-select v-model="pointForm.subject" class="full" filterable><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="pointForm.sort" :min="0" :max="9999" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="pointForm.enable" :active-value="1" :inactive-value="0" /></el-form-item>
        </el-form>
        <div slot="footer"><el-button @click="pointFormOpen=false">取消</el-button><el-button type="primary" @click="submitPoint">保存</el-button></div>
      </el-dialog>
    </el-dialog>
    <el-dialog :title="`题目历史 · ${historyQuestion.questionTitle || ''}`" :visible.sync="historyOpen" width="960px" append-to-body>
      <el-tabs v-model="historyTab">
        <el-tab-pane label="题目版本" name="version">
          <el-table :data="versions" border max-height="480">
            <el-table-column label="版本" width="80"><template slot-scope="{row}">V{{ row.versionNo }}</template></el-table-column>
            <el-table-column label="操作" width="80"><template slot-scope="{row}">{{ row.operationType === 'CREATE' ? '创建' : '更新' }}</template></el-table-column>
            <el-table-column prop="questionTitle" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="questionContent" label="题干快照" min-width="260" show-overflow-tooltip />
            <el-table-column prop="correctAnswer" label="答案" min-width="110" show-overflow-tooltip />
            <el-table-column prop="operatorId" label="操作人" width="85" />
            <el-table-column prop="createTime" label="保存时间" width="170" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="审核历史" name="review">
          <el-table :data="reviewHistory" border max-height="480">
            <el-table-column label="结果" width="90"><template slot-scope="{row}"><el-tag :type="row.reviewStatus === 1 ? 'success' : 'danger'">{{ row.reviewStatus === 1 ? '通过' : '驳回' }}</el-tag></template></el-table-column>
            <el-table-column prop="reviewRemark" label="审核意见" min-width="300" />
            <el-table-column label="审核人" width="130"><template slot-scope="{row}">{{ row.reviewerName || `用户#${row.reviewerId}` }}</template></el-table-column>
            <el-table-column prop="reviewTime" label="审核时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    <el-dialog title="题目举报处理" :visible.sync="reportOpen" width="900px" append-to-body>
      <el-form :inline="true" size="small"><el-form-item><el-select v-model="reportQuery.status" placeholder="处理状态" clearable><el-option label="待处理" :value="0"/><el-option label="已处理" :value="1"/><el-option label="无效" :value="2"/></el-select></el-form-item><el-form-item><el-button type="primary" @click="loadReports">查询</el-button></el-form-item></el-form>
      <el-table :data="reports" border max-height="460"><el-table-column prop="questionTitle" label="题目" min-width="180" show-overflow-tooltip/><el-table-column prop="reportType" label="类型" width="120"/><el-table-column prop="reportContent" label="问题描述" min-width="220"/><el-table-column label="状态" width="85"><template slot-scope="{row}">{{ ['待处理','已处理','无效'][row.status] }}</template></el-table-column><el-table-column prop="createTime" label="举报时间" width="160"/><el-table-column label="操作" width="145"><template slot-scope="{row}"><el-button v-if="row.status===0" type="text" @click="handleReport(row,1)">处理</el-button><el-button v-if="row.status===0" type="text" @click="handleReport(row,2)">无效</el-button></template></el-table-column></el-table>
      <el-pagination class="pagination" layout="total, prev, pager, next" :total="reportTotal" :page-size="reportQuery.pageSize" @current-change="p=>{reportQuery.current=p;loadReports()}"/>
    </el-dialog>
    <el-dialog title="发现可能重复的题目" :visible.sync="duplicateConfirmOpen" width="820px" append-to-body>
      <el-alert type="warning" :closable="false" show-icon title="请逐条核对。确认不是重复题后，仍可继续保存并进入审核。" />
      <el-table :data="duplicates" border max-height="420" class="dialog-table">
        <el-table-column label="相似度" width="100"><template slot-scope="{row}"><el-tag :type="row.similarity === 100 ? 'danger' : 'warning'">{{ row.similarity }}%</el-tag></template></el-table-column>
        <el-table-column prop="questionTitle" label="已有题目" min-width="180" show-overflow-tooltip />
        <el-table-column prop="questionContent" label="已有题干" min-width="320" show-overflow-tooltip />
        <el-table-column label="类型" width="90"><template slot-scope="{row}">{{ row.matchType === 'EXACT' ? '精确重复' : '高度相似' }}</template></el-table-column>
      </el-table>
      <div slot="footer"><el-button @click="duplicateConfirmOpen=false">返回修改</el-button><el-button type="danger" @click="confirmDuplicateSave">已核对，仍然保存</el-button></div>
    </el-dialog>
    <el-dialog title="历史重复题清理" :visible.sync="duplicateHistoryOpen" width="1050px" append-to-body>
      <el-alert type="info" :closable="false" show-icon title="按年级、科目扫描题干；删除会同时清理题目知识点和图片关联，请先确认保留版本。" />
      <el-form :inline="true" size="small" class="history-filter">
        <el-form-item><el-input v-model="duplicateQuery.grade" clearable placeholder="年级编码（可选）" /></el-form-item>
        <el-form-item><el-input v-model="duplicateQuery.subject" clearable placeholder="科目编码（可选）" /></el-form-item>
        <el-form-item label="阈值"><el-input-number v-model="duplicateQuery.similarityThreshold" :min="50" :max="100" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="duplicateLoading" @click="loadDuplicateHistory">开始扫描</el-button></el-form-item>
      </el-form>
      <el-table v-loading="duplicateLoading" :data="duplicatePairs" border max-height="480" class="dialog-table">
        <el-table-column label="相似度" width="90"><template slot-scope="{row}"><el-tag :type="row.similarity === 100 ? 'danger' : 'warning'">{{ row.similarity }}%</el-tag></template></el-table-column>
        <el-table-column label="题目 A" min-width="300"><template slot-scope="{row}"><b>#{{ row.firstQuestion.id }} {{ row.firstQuestion.questionTitle }}</b><div class="question-preview">{{ row.firstQuestion.questionContent }}</div></template></el-table-column>
        <el-table-column label="题目 B" min-width="300"><template slot-scope="{row}"><b>#{{ row.secondQuestion.id }} {{ row.secondQuestion.questionTitle }}</b><div class="question-preview">{{ row.secondQuestion.questionContent }}</div></template></el-table-column>
        <el-table-column label="人工合并" width="185" fixed="right"><template slot-scope="{row}"><el-button type="text" class="danger" @click="mergePair(row, row.firstQuestion, row.secondQuestion)">合并B到A</el-button><br><el-button type="text" class="danger" @click="mergePair(row, row.secondQuestion, row.firstQuestion)">合并A到B</el-button></template></el-table-column>
      </el-table>
      <el-empty v-if="!duplicateLoading && duplicateScanned && !duplicatePairs.length" description="当前范围未发现重复题" />
    </el-dialog>
    <el-dialog title="A/B推荐实验管理" :visible.sync="experimentOpen" width="900px" append-to-body>
      <el-form ref="experimentFormRef" :model="experimentForm" :rules="experimentRules" label-width="110px">
        <el-form-item label="实验名称" prop="experimentName"><el-input v-model="experimentForm.experimentName" maxlength="100" /></el-form-item>
        <el-row :gutter="16"><el-col :span="8"><el-form-item label="启用" prop="enable"><el-switch v-model="experimentForm.enable" :active-value="1" :inactive-value="0" /></el-form-item></el-col><el-col :span="16"><el-form-item label="流量比例" prop="groupATraffic"><el-slider v-model="experimentForm.groupATraffic" show-input :min="0" :max="100" /><div class="form-tip">A组 {{ experimentForm.groupATraffic }}% · B组 {{ 100 - experimentForm.groupATraffic }}%</div></el-form-item></el-col></el-row>
        <el-form-item label="实验时间" prop="timeRange"><el-date-picker v-model="experimentForm.timeRange" type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" class="full" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="experimentForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div class="experiment-actions"><el-button type="primary" @click="submitExperiment">保存配置</el-button><el-button @click="experimentForm=emptyExperiment()">新建实验</el-button><el-button @click="loadExperimentHistory">刷新历史</el-button></div>
      <el-table :data="experimentHistory" border max-height="300" class="dialog-table">
        <el-table-column prop="operationTime" label="变更时间" width="165" /><el-table-column prop="experimentName" label="实验名称" min-width="150" /><el-table-column label="状态" width="75"><template slot-scope="{row}">{{ row.enable === 1 ? '启用' : '停用' }}</template></el-table-column><el-table-column label="流量" width="120"><template slot-scope="{row}">A {{ row.groupATraffic }}% / B {{ row.groupBTraffic }}%</template></el-table-column><el-table-column label="实验结果" min-width="250"><template slot-scope="{row}">A {{ row.groupAAnsweredCount }}题/{{ row.groupACorrectRate }}% · B {{ row.groupBAnsweredCount }}题/{{ row.groupBCorrectRate }}%<br><span :class="row.significant ? 'success' : 'warning'">P={{ row.pValue }}，{{ row.significant ? '显著' : '不显著' }}</span></template></el-table-column><el-table-column label="实验区间" min-width="280"><template slot-scope="{row}">{{ row.startTime }} ~ {{ row.endTime }}</template></el-table-column><el-table-column prop="operatorId" label="操作人" width="80" />
      </el-table>
    </el-dialog>
    <el-dialog title="主观题申诉 · 教师复核" :visible.sync="appealOpen" width="1050px" append-to-body>
      <el-form :inline="true" size="small"><el-form-item><el-select v-model="appealStatus" clearable placeholder="处理状态"><el-option label="待复核" :value="0"/><el-option label="已复核" :value="1"/></el-select></el-form-item><el-form-item><el-button type="primary" @click="loadAppeals">查询</el-button></el-form-item></el-form>
      <el-table :data="appeals" border max-height="500"><el-table-column prop="questionTitle" label="题目" min-width="180"/><el-table-column prop="studentAnswer" label="学生答案" min-width="150"/><el-table-column prop="correctAnswer" label="标准答案" min-width="150"/><el-table-column prop="appealReason" label="申诉原因" min-width="180"/><el-table-column label="状态" width="80"><template slot-scope="{row}">{{ row.status === 0 ? '待复核' : '已复核' }}</template></el-table-column><el-table-column label="操作" width="150"><template slot-scope="{row}"><el-button v-if="row.status===0" type="text" @click="reviewAppeal(row,true)">判定正确</el-button><el-button v-if="row.status===0" type="text" class="warning" @click="reviewAppeal(row,false)">判定错误</el-button><span v-else>{{ row.reviewCorrect === 1 ? '正确' : '错误' }}</span></template></el-table-column></el-table>
    </el-dialog>
  </div>
</template>

<script>
import { batchReviewQuestionBank, cleanDuplicateQuestion, deleteKnowledgePoint, deleteQuestionBank, duplicateQuestionHistory, duplicateQuestionList, handleQuestionReport, importQuestionBankFile, knowledgePointList, knowledgePointTree, mergeDuplicateQuestion, questionBankPageList, questionBankReviewHistory, questionBankVersionList, questionExperimentDetail, questionExperimentHistory, questionPracticeAppealList, questionPracticeStatistics, questionReportPageList, reviewQuestionBank, reviewQuestionPracticeAppeal, saveKnowledgePoint, saveQuestionBank, saveQuestionExperiment } from '@/api/system/questionBank'
import LatexRenderer from '@/components/LatexRenderer.vue'
import QuestionImageManager from '@/components/QuestionImageManager.vue'
import QuestionOptionEditor from '@/components/QuestionOptionEditor.vue'
import { dictDataOptions } from '@/api/system/dict'
import { filePolicy, uploadFile } from '@/api/system/file'

const OCR_UPLOAD_TYPE = 'wrongQuestion'

export default {
  name: 'QuestionBank',
  components: { LatexRenderer, QuestionImageManager, QuestionOptionEditor },
  data() {
    return {
      loading: false, rows: [], total: 0, query: { current: 1, pageSize: 10, keyWord: '', grade: '', subject: '', reviewStatus: null },
      selectedQuestionIds: [],
      formOpen: false, ocrImportOpen: false, knowledgeOpen: false, pointFormOpen: false, historyOpen: false, reportOpen: false, duplicateConfirmOpen: false, duplicateHistoryOpen: false, experimentOpen: false, appealOpen: false,
      knowledge: [], knowledgeTree: [], pointQuery: { grade: '', subject: '' }, gradeOptions: [], subjectOptions: [], questionTypeOptions: [], sourceOptions: [], form: {}, pointForm: this.emptyPoint(), duplicates: [], duplicatePairs: [], duplicateLoading: false, duplicateScanned: false,
      ocrImportForm: this.emptyOcrImportForm(), ocrKnowledge: [], ocrFileName: '', ocrUploading: false, ocrImporting: false,
      historyQuestion: {}, historyTab: 'version', versions: [], reviewHistory: [],
      duplicateQuery: { grade: '', subject: '', similarityThreshold: 82, limit: 200 },
      experimentForm: this.emptyExperiment(), experimentHistory: [],
      appeals: [], appealStatus: 0,
      experimentRules: { experimentName: [{ required: true, message: '请输入实验名称', trigger: 'blur' }], groupATraffic: [{ required: true, message: '请设置流量比例', trigger: 'change' }], timeRange: [{ type: 'array', required: true, message: '请选择实验时间', trigger: 'change' }] },
      statistics: {}, reports: [], reportTotal: 0, reportQuery: { current: 1, pageSize: 10, status: 0 },
      rules: { grade: [{ required: true, message: '必填', trigger: 'change' }], subject: [{ required: true, message: '必填', trigger: 'change' }], questionType: [{ required: true, message: '必填', trigger: 'change' }], source: [{ required: true, message: '请选择来源', trigger: 'change' }], judgeMode: [{ required: true, message: '请选择判题方式', trigger: 'change' }], questionTitle: [{ required: true, message: '必填', trigger: 'blur' }], questionContent: [{ required: true, message: '必填', trigger: 'blur' }], correctAnswer: [{ required: true, message: '必填', trigger: 'blur' }] },
      pointRules: { pointCode: [{ required: true, message: '请输入知识点编码', trigger: 'blur' }], pointName: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }], grade: [{ required: true, message: '请选择年级', trigger: 'change' }], subject: [{ required: true, message: '请选择科目', trigger: 'change' }] },
      ocrImportRules: { grade: [{ required: true, message: '请选择年级', trigger: 'change' }], subject: [{ required: true, message: '请选择科目', trigger: 'change' }], source: [{ required: true, message: '请选择来源', trigger: 'change' }], judgeMode: [{ required: true, message: '请选择判题方式', trigger: 'change' }], fileId: [{ required: true, message: '请上传A4文件', trigger: 'change' }] }
    }
  },
  created() { this.loadList(); this.loadStatistics(); this.loadDictOptions() },
  methods: {
    emptyForm() { return { id: null, grade: '', subject: '', questionType: '', questionTitle: '', questionContent: '', contentFormat: 'TEXT', imageUrls: '', images: [], optionsJson: '', correctAnswer: '', judgeMode: 'AUTO', analysis: '', difficulty: 3, source: '', enable: 1, knowledgePointIds: [], duplicateConfirmed: false } },
    emptyOcrImportForm() { return { grade: '', subject: '', questionType: '', source: '', fileId: null, judgeMode: 'SELF', difficulty: 3, knowledgePointIds: [] } },
    emptyPoint() { return { id: null, parentId: 0, pointCode: '', pointName: '', grade: '', subject: '', sort: 0, enable: 1 } },
    emptyExperiment() { return { id: null, experimentName: '相似题推荐策略实验', enable: 0, groupATraffic: 50, timeRange: [], remark: '' } },
    loadDictOptions() { Promise.all([dictDataOptions({ dictType: 'grade' }), dictDataOptions({ dictType: 'subject' }), dictDataOptions({ dictType: 'question_type' }), dictDataOptions({ dictType: 'source' })]).then(([grades, subjects, types, sources]) => { this.gradeOptions = grades || []; this.subjectOptions = subjects || []; this.questionTypeOptions = types || []; this.sourceOptions = sources || [] }).catch(() => { this.$message.warning('年级、科目、题型或来源字典加载失败') }) },
    changeQuestionScope() { this.form.knowledgePointIds = []; this.loadKnowledge() },
    loadList() { this.loading = true; questionBankPageList(this.query).then(data => { this.rows = data.list || []; this.total = data.total || 0 }).finally(() => { this.loading = false }) },
    loadStatistics() { questionPracticeStatistics().then(data => { this.statistics = data || {} }) },
    loadKnowledge() { if (!this.form.grade || !this.form.subject) { this.knowledge = []; return } knowledgePointList({ grade: this.form.grade, subject: this.form.subject }).then(data => { this.knowledge = data || [] }) },
    openKnowledgeManagement() { this.knowledgeOpen = true; this.loadKnowledgeTree() },
    loadKnowledgeTree() { knowledgePointTree(this.pointQuery).then(data => { this.knowledgeTree = data || [] }) },
    flattenPoints(points, depth = 0) { return (points || []).reduce((rows, point) => rows.concat([{ id: point.id, label: `${'　'.repeat(depth)}${point.pointName}` }], this.flattenPoints(point.children, depth + 1)), []) },
    startCreatePoint(parent) { this.pointForm = this.emptyPoint(); if (parent) { this.pointForm.parentId = parent.id; this.pointForm.grade = parent.grade; this.pointForm.subject = parent.subject } else { this.pointForm.grade = this.pointQuery.grade || ''; this.pointForm.subject = this.pointQuery.subject || '' } this.pointFormOpen = true },
    editPoint(row) { this.pointForm = Object.assign(this.emptyPoint(), row); delete this.pointForm.children; this.pointFormOpen = true },
    openForm(row) { this.form = Object.assign(this.emptyForm(), row || {}); this.formOpen = true; this.loadKnowledge() },
    openOcrImport() { this.ocrImportForm = this.emptyOcrImportForm(); this.ocrKnowledge = []; this.ocrFileName = ''; this.ocrImportOpen = true; this.$nextTick(() => this.$refs.ocrImportFormRef && this.$refs.ocrImportFormRef.clearValidate()) },
    changeOcrScope() { this.ocrImportForm.knowledgePointIds = []; if (!this.ocrImportForm.grade || !this.ocrImportForm.subject) { this.ocrKnowledge = []; return } knowledgePointList({ grade: this.ocrImportForm.grade, subject: this.ocrImportForm.subject }).then(rows => { this.ocrKnowledge = rows || [] }) },
    beforeOcrFileUpload(file) { const ext = file.name && file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''; const allowed = ['jpg', 'jpeg', 'png', 'webp', 'bmp', 'pdf', 'doc', 'docx']; if (!allowed.includes(ext) && !(file.type && file.type.startsWith('image/'))) { this.$message.warning('请上传图片、PDF或Word文件'); return false } return true },
    uploadOcrFile(options) { this.ocrUploading = true; filePolicy({ uploadType: OCR_UPLOAD_TYPE }).then(policy => { const data = new FormData(); data.append('file', options.file); data.append('signature', policy.signature); data.append('fileName', options.file.name); return uploadFile(data) }).then(file => { this.ocrImportForm.fileId = Number(file.id); this.ocrFileName = file.originName || options.file.name; this.$message.success('A4文件上传成功'); this.$nextTick(() => this.$refs.ocrImportFormRef && this.$refs.ocrImportFormRef.validateField('fileId')) }).catch(() => {}).finally(() => { this.ocrUploading = false }) },
    submitOcrImport() { this.$refs.ocrImportFormRef.validate(ok => { if (!ok) return; this.ocrImporting = true; importQuestionBankFile(this.ocrImportForm).then(data => { this.$message.success(`识别完成，共导入 ${data.importCount || 0} 道待审核题目`); this.ocrImportOpen = false; this.loadList() }).catch(() => {}).finally(() => { this.ocrImporting = false }) }) },
    submit() { this.$refs.formRef.validate(ok => { if (!ok) return; const check = { id: this.form.id, grade: this.form.grade, subject: this.form.subject, questionContent: this.form.questionContent }; duplicateQuestionList(check).then(rows => { this.duplicates = rows || []; if (this.duplicates.length) { this.duplicateConfirmOpen = true; return } this.saveForm(false) }) }) },
    saveForm(duplicateConfirmed) { const data = Object.assign({}, this.form, { duplicateConfirmed }); delete data.gradeName; delete data.subjectName; delete data.questionTypeName; saveQuestionBank(data).then(() => { this.$message.success('已保存并进入待审核'); this.duplicateConfirmOpen = false; this.formOpen = false; this.loadList() }) },
    confirmDuplicateSave() { this.saveForm(true) },
    openDuplicateHistory() { this.duplicateHistoryOpen = true; this.duplicateScanned = false; this.duplicatePairs = [] },
    loadDuplicateHistory() { this.duplicateLoading = true; this.duplicateScanned = true; duplicateQuestionHistory(this.duplicateQuery).then(rows => { this.duplicatePairs = rows || [] }).finally(() => { this.duplicateLoading = false }) },
    cleanPair(pair, keep, remove) { this.$confirm(`确认保留 #${keep.id}，永久删除 #${remove.id} 吗？`, '清理重复题', { type: 'warning' }).then(() => cleanDuplicateQuestion({ keepId: keep.id, deleteId: remove.id })).then(() => { this.$message.success('重复题已清理'); this.duplicatePairs = this.duplicatePairs.filter(item => item !== pair); this.loadList() }).catch(() => {}) },
    mergePair(pair, keep, remove) { this.$confirm(`确认把 #${remove.id} 的练习、举报、申诉、知识点和图片关联合并到 #${keep.id}，并删除副本吗？`, '人工合并高相似题', { type: 'warning' }).then(() => mergeDuplicateQuestion({ keepId: keep.id, deleteId: remove.id })).then(() => { this.$message.success('题目及关联数据已合并'); this.duplicatePairs = this.duplicatePairs.filter(item => item !== pair); this.loadList() }).catch(() => {}) },
    openExperiment() { this.experimentOpen = true; Promise.all([questionExperimentDetail(), questionExperimentHistory()]).then(([detail, history]) => { this.experimentForm = detail ? Object.assign({}, detail, { timeRange: [detail.startTime, detail.endTime] }) : this.emptyExperiment(); this.experimentHistory = history || [] }) },
    loadExperimentHistory() { questionExperimentHistory().then(rows => { this.experimentHistory = rows || [] }) },
    submitExperiment() { this.$refs.experimentFormRef.validate(ok => { if (!ok) return; const data = Object.assign({}, this.experimentForm, { startTime: this.experimentForm.timeRange[0], endTime: this.experimentForm.timeRange[1] }); delete data.timeRange; delete data.groupBTraffic; delete data.running; delete data.updateTime; saveQuestionExperiment(data).then(() => { this.$message.success('实验配置已保存'); this.loadExperimentHistory(); this.loadStatistics() }) }) },
    openAppeals() { this.appealOpen = true; this.loadAppeals() },
    loadAppeals() { questionPracticeAppealList({ status: this.appealStatus }).then(rows => { this.appeals = rows || [] }) },
    reviewAppeal(row, correct) { this.$prompt('请输入教师复核说明', correct ? '判定作答正确' : '判定作答错误', { inputValidator: value => Boolean(value && value.trim()) || '请输入复核说明' }).then(({ value }) => reviewQuestionPracticeAppeal({ id: row.id, correct, reviewRemark: value })).then(() => { this.$message.success('复核完成'); this.loadAppeals(); this.loadStatistics() }).catch(() => {}) },
    review(row, status) { this.$prompt(status === 1 ? '可填写审核备注' : '请填写驳回原因', status === 1 ? '审核通过' : '审核驳回', { inputValue: '' }).then(({ value }) => reviewQuestionBank({ id: row.id, reviewStatus: status, reviewRemark: value })).then(() => { this.$message.success('审核完成'); this.loadList() }).catch(() => {}) },
    batchReview(status) { this.$prompt(status === 1 ? '可填写批量审核备注' : '请填写批量驳回原因', status === 1 ? '批量审核通过' : '批量审核驳回', { inputValue: '' }).then(({ value }) => batchReviewQuestionBank({ ids: this.selectedQuestionIds, reviewStatus: status, reviewRemark: value })).then(() => { this.$message.success(`已批量审核 ${this.selectedQuestionIds.length} 道题`); this.selectedQuestionIds = []; this.loadList() }).catch(() => {}) },
    remove(row) { this.$confirm('确认删除该题及知识点关联吗？', '提示', { type: 'warning' }).then(() => deleteQuestionBank({ id: row.id })).then(() => { this.$message.success('已删除'); this.loadList() }).catch(() => {}) },
    submitPoint() { this.$refs.pointFormRef.validate(ok => { if (!ok) return; saveKnowledgePoint(this.pointForm).then(() => { this.$message.success('知识点已保存'); this.pointFormOpen = false; this.loadKnowledgeTree(); this.loadKnowledge() }) }) },
    removePoint(row) { this.$confirm(`确认删除知识点“${row.pointName}”吗？有下级或题目引用时将无法删除。`, '删除知识点', { type: 'warning' }).then(() => deleteKnowledgePoint({ id: row.id })).then(() => { this.$message.success('知识点已删除'); this.loadKnowledgeTree(); this.loadKnowledge() }).catch(() => {}) },
    openHistory(row) { this.historyQuestion = row; this.historyTab = 'version'; this.historyOpen = true; Promise.all([questionBankVersionList({ id: row.id }), questionBankReviewHistory({ id: row.id })]).then(([versions, reviews]) => { this.versions = versions || []; this.reviewHistory = reviews || [] }) },
    openReports() { this.reportOpen = true; this.loadReports() },
    loadReports() { questionReportPageList(this.reportQuery).then(data => { this.reports = data.list || []; this.reportTotal = data.total || 0 }) },
    handleReport(row, status) { this.$prompt('填写处理备注', status === 1 ? '确认有效问题' : '标记无效', { inputValue: '' }).then(({ value }) => { if (status === 1) return this.$confirm('是否同时停用该题，防止继续推荐？', '题目处理', { confirmButtonText: '停用题目', cancelButtonText: '仅处理举报', distinguishCancelAndClose: true }).then(() => true).catch(action => action === 'cancel' ? false : Promise.reject(action)).then(disable => handleQuestionReport({ id: row.id, status, handleRemark: value, disableQuestion: disable })); return handleQuestionReport({ id: row.id, status, handleRemark: value, disableQuestion: false }) }).then(() => { this.$message.success('举报已处理'); this.loadReports(); this.loadStatistics(); this.loadList() }).catch(() => {}) },
    statusText(v) { return ['待审核', '已通过', '已驳回'][v] || '未知' },
    statusType(v) { return v === 1 ? 'success' : (v === 2 ? 'danger' : 'warning') }
  },
  computed: {
    pointFlatOptions() { return this.flattenPoints(this.knowledgeTree) }
  }
}
</script>

<style scoped>
.header-row { display:flex; align-items:center; justify-content:space-between; }.hint { margin-left:14px; color:#909399; font-size:13px; }.statistics-row { margin:0 0 12px; }.experiment-alert { margin-bottom:12px; }.stat { padding:14px; border-radius:8px; background:#f5f7fa; display:flex; flex-direction:column; }.stat b { font-size:24px; color:#303133; }.stat span { margin-top:5px; color:#909399; font-size:13px; }.latex-preview { padding:12px; margin:0; background:#f5f7fa; border-radius:5px; white-space:pre-wrap; }.pagination { margin-top:20px; text-align:right; }.full { width:100%; }.danger { color:#f56c6c; }.warning { color:#e6a23c; }.success { color:#67c23a; }.el-tag { margin-right:4px; }.dialog-table { margin-top:16px; }.dialog-alert { margin-bottom:16px; }.history-filter { margin-top:16px; }.question-preview { margin-top:6px; color:#606266; line-height:1.45; max-height:44px; overflow:hidden; }.form-tip { color:#909399; font-size:12px; }.experiment-actions { margin-bottom:10px; }.ocr-file { margin-top:10px; display:flex; align-items:center; gap:8px; color:#606266; }
</style>
