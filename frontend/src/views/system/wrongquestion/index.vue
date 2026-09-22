<template>
  <div class="app-container wrong-question-page">
    <el-form :inline="true" :model="queryParams" size="small" class="filter-form">
      <el-form-item label="年级">
        <el-select v-model="queryParams.grade" clearable placeholder="请选择年级" class="filter-item" @change="loadQueryKnowledgePoints">
          <el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="科目">
        <el-select v-model="queryParams.subject" clearable placeholder="请选择科目" class="filter-item" @change="loadQueryKnowledgePoints">
          <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="题型">
        <el-select v-model="queryParams.questionType" clearable placeholder="请选择题型" class="filter-item">
          <el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="queryParams.source" clearable placeholder="请选择来源" class="filter-item">
          <el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="filter-item">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="知识点">
        <el-select v-model="queryParams.knowledgePointId" clearable filterable placeholder="请选择知识点" class="filter-item">
          <el-option v-for="item in queryKnowledgePointOptions" :key="item.id" :label="item.pointName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="错因">
        <el-select v-model="queryParams.errorLabel" clearable placeholder="请选择错因" class="filter-item">
          <el-option v-for="item in errorLabelOptions" :key="item.value" :label="item.value" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyWord" clearable placeholder="题目标题/内容" class="filter-item" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.CREATE)" type="success" icon="el-icon-plus" @click="handleCreate">录入错题</el-button>
        <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.IMPORT)" type="warning" icon="el-icon-upload" @click="handleImport">导入A4文件</el-button>
        <el-button
          v-if="hasMenuCode(menuCode.WRONG_QUESTION.EXPORT)"
          icon="el-icon-printer"
          :loading="exportLoading"
          :disabled="selectedRows.length === 0"
          @click="handleExportPrint"
        >
          导出PDF/打印
        </el-button>
        <el-button
          v-if="hasMenuCode(menuCode.WRONG_QUESTION.BATCH_DELETE)"
          type="danger"
          icon="el-icon-delete"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="statistics-row">
      <el-col :span="12">
        <el-card shadow="never" class="statistics-card">
          <div slot="header" class="statistics-header">
            <span>薄弱知识点</span>
            <el-button type="text" @click="loadStatistics">刷新</el-button>
          </div>
          <div v-if="knowledgePointStatistics.length === 0" class="statistics-empty">暂无知识点统计</div>
          <div v-for="item in knowledgePointStatistics.slice(0, 5)" :key="item.knowledgePointId" class="statistics-item">
            <div class="statistics-main">
              <span>{{ item.knowledgePointName || '未命名知识点' }}</span>
              <strong>{{ item.wrongQuestionCount }}题</strong>
              <small>待订正 {{ item.pendingCorrectionCount }} · 已掌握 {{ item.masteredCount }}</small>
            </div>
            <el-button type="text" size="mini" @click="handleWeakPointPractice(item)">专项练习</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="statistics-card">
          <div slot="header" class="statistics-header">
            <span>高频错因</span>
            <el-button type="text" @click="loadStatistics">刷新</el-button>
          </div>
          <div v-if="errorAnalysisStatistics.length === 0" class="statistics-empty">暂无错因统计</div>
          <div v-for="item in errorAnalysisStatistics.slice(0, 5)" :key="item.errorLabel" class="statistics-item">
            <span>{{ item.errorLabel }}</span>
            <strong>{{ item.wrongQuestionCount }}题</strong>
            <small>待订正 {{ item.pendingCorrectionCount }} · 已掌握 {{ item.masteredCount }}</small>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column label="年级" width="100">
        <template slot-scope="{ row }">{{ row.gradeName || row.grade }}</template>
      </el-table-column>
      <el-table-column label="科目" width="100">
        <template slot-scope="{ row }">{{ row.subjectName || row.subject }}</template>
      </el-table-column>
      <el-table-column label="题型" width="100">
        <template slot-scope="{ row }">{{ row.questionTypeName || row.questionType }}</template>
      </el-table-column>
      <el-table-column label="来源" width="100">
        <template slot-scope="{ row }">{{ row.sourceName || row.source }}</template>
      </el-table-column>
      <el-table-column prop="questionTitle" label="题目标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="知识点" min-width="140" show-overflow-tooltip><template slot-scope="{row}">{{ (row.knowledgePointNames || []).join('、') || row.learningPoint || '-' }}</template></el-table-column>
      <el-table-column prop="errorLabels" label="错误标签" min-width="140" show-overflow-tooltip />
      <el-table-column prop="wrongAnswer" label="错误答案" min-width="160" show-overflow-tooltip />
      <el-table-column prop="correctAnswer" label="正确答案" min-width="160" show-overflow-tooltip />
      <el-table-column label="错图" width="90">
        <template slot-scope="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imagePreviewUrl"
            :preview-src-list="[row.imagePreviewUrl]"
            fit="cover"
            class="question-image"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录入时间" width="170" />
      <el-table-column v-if="hasWrongQuestionRowAction" label="操作" fixed="right" width="500">
        <template slot-scope="{ row }">
          <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.DETAIL)" type="text" icon="el-icon-view" @click="handleDetail(row)">详情</el-button>
          <el-button type="text" icon="el-icon-edit-outline" @click="handleCorrection(row)">订正</el-button>
          <el-button type="text" icon="el-icon-magic-stick" @click="handleSimilarPractice(row)">练同类</el-button>
          <el-button type="text" icon="el-icon-collection-tag" @click="handleBindKnowledgePoint(row)">知识点</el-button>
          <el-button type="text" icon="el-icon-warning-outline" @click="handleErrorAnalysis(row)">错因</el-button>
          <el-dropdown trigger="click" @command="command => handleStatusCommand(row, command)">
            <el-button type="text">状态<i class="el-icon-arrow-down el-icon--right" /></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-if="row.status === 1" :command="0">重新待订正</el-dropdown-item>
              <el-dropdown-item v-if="row.status === 2" :command="3">归档</el-dropdown-item>
              <el-dropdown-item v-if="row.status !== 1 && row.status !== 2" disabled>暂无可执行状态操作</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.UPDATE)" type="text" icon="el-icon-edit" @click="handleUpdate(row)">修改</el-button>
          <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.UPDATE_IMAGE)" type="text" icon="el-icon-picture" @click="handleImage(row)">错图</el-button>
          <el-button v-if="hasMenuCode(menuCode.WRONG_QUESTION.DELETE)" type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="queryParams.current"
      :page-size="queryParams.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog :title="dialogTitle" :visible.sync="formOpen" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" @change="loadKnowledgePoints">
                <el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="科目" prop="subject">
              <el-select v-model="form.subject" placeholder="请选择科目" @change="loadKnowledgePoints">
                <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="form.questionType" placeholder="请选择题型" @change="handleQuestionTypeChange">
                <el-option v-for="item in questionTypeOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题目标题" prop="questionTitle">
          <el-input v-model="form.questionTitle" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="题目内容" prop="questionContent">
          <el-input v-model="form.questionContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="错误答案">
          <el-input v-model="form.wrongAnswer" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="正确答案">
          <el-input v-model="form.correctAnswer" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="错误原因">
          <el-input v-model="form.wrongReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="题目解析">
          <el-input v-model="form.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择来源">
                <el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="知识点">
          <el-select v-model="form.knowledgePointIds" multiple filterable clearable class="full-width" placeholder="请选择标准知识点">
            <el-option v-for="item in knowledgePointOptions" :key="item.id" :label="item.pointName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="错误标签">
          <el-select v-model="form.errorLabelValues" multiple filterable allow-create default-first-option clearable class="full-width" placeholder="请选择或输入错因标签">
            <el-option v-for="item in errorLabelOptions" :key="item.value" :label="item.value" :value="item.value" />
          </el-select>
        </el-form-item>
        <div v-if="isChoiceQuestion" class="choice-image-grid">
          <el-form-item
            v-for="item in optionImageFields"
            :key="item.prop"
            :label="item.label"
          >
            <el-upload
              action=""
              :show-file-list="false"
              :http-request="options => uploadFormImage(item.prop, options)"
              :before-upload="beforeImageUpload"
            >
              <el-button icon="el-icon-upload2">上传图片</el-button>
            </el-upload>
            <el-image
              v-if="optionImagePreviewUrls[item.prop]"
              :src="optionImagePreviewUrls[item.prop]"
              :preview-src-list="[optionImagePreviewUrls[item.prop]]"
              fit="contain"
              class="choice-image-preview"
            />
          </el-form-item>
        </div>
        <el-form-item v-else label="题目图片">
          <el-upload
            action=""
            :show-file-list="false"
            :http-request="options => uploadFormImage('imageUrl', options)"
            :before-upload="beforeImageUpload"
          >
            <el-button icon="el-icon-upload2">上传图片</el-button>
          </el-upload>
          <el-image
            v-if="optionImagePreviewUrls.imageUrl"
            :src="optionImagePreviewUrls.imageUrl"
            :preview-src-list="[optionImagePreviewUrls.imageUrl]"
            fit="contain"
            class="single-image-preview"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="formOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="错图修改" :visible.sync="imageOpen" width="620px" append-to-body>
      <el-form :model="imageForm" label-width="90px">
        <el-form-item v-for="item in imageDialogFields" :key="item.prop" :label="item.label">
          <el-upload
            action=""
            :show-file-list="false"
            :http-request="options => uploadDialogImage(item.prop, options)"
            :before-upload="beforeImageUpload"
          >
            <el-button icon="el-icon-upload2">上传图片</el-button>
          </el-upload>
          <div v-if="imageDialogPreviewUrls[item.prop]" class="image-dialog-preview">
            <el-image
              :src="imageDialogPreviewUrls[item.prop]"
              :preview-src-list="[imageDialogPreviewUrls[item.prop]]"
              fit="contain"
              class="image-preview"
            />
            <el-button type="text" icon="el-icon-delete" class="danger-action" @click="clearDialogImage(item.prop)">移除</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="imageOpen = false">取消</el-button>
        <el-button type="primary" @click="submitImage">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="导入A4文件" :visible.sync="importOpen" width="620px" append-to-body>
      <el-form ref="importFormRef" :model="importForm" :rules="importRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="importForm.grade" placeholder="请选择年级">
                <el-option v-for="item in gradeOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目" prop="subject">
              <el-select v-model="importForm.subject" placeholder="请选择科目">
                <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="importForm.source" placeholder="请选择来源">
                <el-option v-for="item in sourceOptions" :key="item.key" :label="item.value" :value="item.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="importForm.status" placeholder="请选择状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="知识点">
          <el-input v-model="importForm.learningPoint" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="错误标签">
          <el-select v-model="importForm.errorLabelValues" multiple filterable allow-create default-first-option clearable class="full-width" placeholder="请选择或输入错因标签">
            <el-option v-for="item in errorLabelOptions" :key="item.value" :label="item.value" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="A4文件" prop="imageFileId">
          <el-upload
            action=""
            accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf"
            :show-file-list="false"
            :http-request="uploadImportImage"
            :before-upload="beforeImportFileUpload"
          >
            <el-button icon="el-icon-upload2">上传图片或 PDF</el-button>
          </el-upload>
          <el-image
            v-if="importImagePreviewUrl"
            :src="importImagePreviewUrl"
            :preview-src-list="[importImagePreviewUrl]"
            fit="contain"
            class="image-preview"
          />
          <div v-else-if="importFileName" class="file-preview">
            <i class="el-icon-document" />
            <span>{{ importFileName }}</span>
          </div>
          <div class="form-tip">DOC/DOCX 暂不支持：当前识别链路只处理可稳定分页与校验的 JPG、JPEG、PNG、PDF。</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="importOpen = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">扫描导入</el-button>
      </div>
    </el-dialog>

    <el-dialog title="错题详情" :visible.sync="detailOpen" width="720px" append-to-body>
      <div v-if="detail" class="detail-actions">
        <el-button
          size="small"
          :icon="showAnswerDetails ? 'el-icon-arrow-up' : 'el-icon-view'"
          @click="showAnswerDetails = !showAnswerDetails"
        >
          {{ showAnswerDetails ? '隐藏答案与解析' : '查看答案与解析' }}
        </el-button>
      </div>
      <table v-if="detail" class="detail-table">
        <colgroup>
          <col class="detail-label-column">
          <col class="detail-value-column">
          <col class="detail-label-column">
          <col class="detail-value-column">
          <col class="detail-label-column">
          <col class="detail-value-column">
          <col class="detail-label-column">
          <col class="detail-value-column">
        </colgroup>
        <tbody>
          <tr>
            <th>年级</th>
            <td>{{ detail.gradeName || detail.grade }}</td>
            <th>科目</th>
            <td>{{ detail.subjectName || detail.subject }}</td>
            <th>题型</th>
            <td>{{ detail.questionTypeName || detail.questionType }}</td>
            <th>来源</th>
            <td>{{ detail.sourceName || detail.source }}</td>
          </tr>
          <tr>
            <th>状态</th>
            <td>{{ getStatusLabel(detail.status) }}</td>
            <th>知识点</th>
            <td colspan="2">{{ (detail.knowledgePointNames || []).join('、') || detail.learningPoint || '-' }}</td>
            <th>错误标签</th>
            <td colspan="2">{{ detail.errorLabels || '-' }}</td>
          </tr>
          <tr>
            <th colspan="8">题目标题</th>
          </tr>
          <tr>
            <td colspan="8">{{ detail.questionTitle }}</td>
          </tr>
          <tr>
            <th colspan="8">题目内容</th>
          </tr>
          <tr>
            <td colspan="8">
              <div class="rich-content" v-html="formatRichContent(detail.questionContent)" />
            </td>
          </tr>
          <tr v-if="isDetailChoiceQuestion && detailOptionImageFields.length">
            <th>选项图片</th>
            <td colspan="7">
              <div class="detail-option-images">
                <div v-for="item in detailOptionImageFields" :key="item.prop" class="detail-option-image">
                  <span>{{ item.label }}</span>
                  <el-image
                    v-if="detail.optionImagePreviewUrls && detail.optionImagePreviewUrls[item.prop]"
                    :src="detail.optionImagePreviewUrls[item.prop]"
                    :preview-src-list="[detail.optionImagePreviewUrls[item.prop]]"
                    fit="contain"
                    class="choice-image-preview"
                  />
                </div>
              </div>
            </td>
          </tr>
          <tr v-if="!isDetailChoiceQuestion">
            <th>题目图片</th>
            <td colspan="7">
              <el-image
                v-if="detail.imageUrl"
                :src="detail.imagePreviewUrl"
                :preview-src-list="[detail.imagePreviewUrl]"
                class="detail-image"
              />
              <span v-else>-</span>
            </td>
          </tr>
          <tr v-if="showAnswerDetails">
            <th>错误答案</th>
            <td colspan="7">{{ detail.wrongAnswer }}</td>
          </tr>
          <tr v-if="showAnswerDetails">
            <th>正确答案</th>
            <td colspan="7">{{ detail.correctAnswer }}</td>
          </tr>
          <tr v-if="showAnswerDetails">
            <th>错误原因</th>
            <td colspan="7">{{ detail.wrongReason }}</td>
          </tr>
          <tr v-if="showAnswerDetails">
            <th>题目解析</th>
            <td colspan="7">{{ detail.analysis }}</td>
          </tr>
          <tr v-if="detail.latestCorrectionRecord">
            <th>最新订正</th>
            <td colspan="7">
              <div>{{ detail.latestCorrectionRecord.correctionAnswer || '-' }}</div>
              <div class="muted-text">{{ detail.latestCorrectionRecord.correctionRemark || '' }}</div>
            </td>
          </tr>
          <tr v-if="detail.correctionRecordList && detail.correctionRecordList.length">
            <th>订正历史</th>
            <td colspan="7">
              <el-timeline class="correction-timeline">
                <el-timeline-item v-for="record in detail.correctionRecordList" :key="record.id" :timestamp="record.createTime">
                  <div><b>订正答案：</b>{{ record.correctionAnswer || '-' }}</div>
                  <div v-if="record.correctionAnalysis"><b>订正解析：</b>{{ record.correctionAnalysis }}</div>
                  <div v-if="record.correctionRemark" class="muted-text">{{ record.correctionRemark }}</div>
                </el-timeline-item>
              </el-timeline>
            </td>
          </tr>
          <tr>
            <th>录入时间</th>
            <td colspan="3">{{ detail.createTime || '-' }}</td>
            <th>更新时间</th>
            <td colspan="3">{{ detail.updateTime || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </el-dialog>

    <el-dialog title="提交订正" :visible.sync="correctionOpen" width="640px" append-to-body>
      <el-form ref="correctionFormRef" :model="correctionForm" :rules="correctionRules" label-width="90px">
        <el-form-item label="订正答案" prop="correctionAnswer">
          <el-input v-model="correctionForm.correctionAnswer" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="订正解析">
          <el-input v-model="correctionForm.correctionAnalysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="订正图片">
          <el-upload
            action=""
            :show-file-list="false"
            :http-request="uploadCorrectionImage"
            :before-upload="beforeImageUpload"
          >
            <el-button icon="el-icon-upload2">上传图片</el-button>
          </el-upload>
          <el-image
            v-if="correctionImagePreviewUrl"
            :src="correctionImagePreviewUrl"
            :preview-src-list="[correctionImagePreviewUrl]"
            fit="contain"
            class="image-preview"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="correctionForm.correctionRemark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="correctionOpen = false">取消</el-button>
        <el-button type="primary" @click="submitCorrection">提交订正</el-button>
      </div>
    </el-dialog>

    <el-dialog title="绑定知识点" :visible.sync="bindOpen" width="560px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="知识点">
          <el-select v-model="bindForm.knowledgePointIds" multiple filterable clearable class="full-width" placeholder="请选择标准知识点">
            <el-option v-for="item in bindKnowledgePointOptions" :key="item.id" :label="item.pointName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="bindOpen = false">取消</el-button>
        <el-button type="primary" @click="submitBindKnowledgePoint">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="错因分析" :visible.sync="analysisOpen" width="620px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="错因标签">
          <el-select v-model="analysisForm.errorLabelValues" multiple filterable allow-create default-first-option clearable class="full-width" placeholder="请选择或输入错因标签">
            <el-option v-for="item in errorLabelOptions" :key="item.value" :label="item.value" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="错误原因">
          <el-input v-model="analysisForm.wrongReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="analysisOpen = false">取消</el-button>
        <el-button type="primary" @click="submitErrorAnalysis">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  bindWrongQuestionKnowledgePoint,
  batchDeleteWrongQuestion,
  createWrongQuestion,
  deleteWrongQuestion,
  importWrongQuestionImage,
  submitCorrectionRecord,
  updateWrongQuestion,
  updateWrongQuestionErrorAnalysis,
  updateWrongQuestionImage,
  updateWrongQuestionStatus,
  wrongQuestionDetail,
  wrongQuestionErrorAnalysisStatistics,
  wrongQuestionKnowledgePointStatistics,
  wrongQuestionPageList
} from '@/api/system/wrongQuestion'
import { downloadUrl, filePolicy, uploadFile } from '@/api/system/file'
import { dictDataOptions } from '@/api/system/dict'
import { getCurrentUserInfo } from '@/api/system/user'
import { MENU_CODE } from '@/config/menu'
import { knowledgePointList } from '@/api/system/questionBank'
import { createWrongQuestionPrintWindow, renderWrongQuestionPrint } from '@/utils/wrongQuestionPrint'

const WRONG_QUESTION_UPLOAD_TYPE = 'wrongQuestion'
const DICT_TYPES = {
  grade: 'grade',
  subject: 'subject',
  questionType: 'question_type',
  source: 'source',
  errorLabel: 'wrong_question_error_label'
}

export default {
  name: 'WrongQuestion',
  data() {
    return {
      loading: false,
      exportLoading: false,
      menuCode: MENU_CODE,
      menuCodes: [],
      total: 0,
      tableData: [],
      selectedRows: [],
      formOpen: false,
      imageOpen: false,
      detailOpen: false,
      correctionOpen: false,
      bindOpen: false,
      analysisOpen: false,
      showAnswerDetails: false,
      importOpen: false,
      importLoading: false,
      dialogTitle: '错题录入',
      detail: null,
      optionImagePreviewUrls: this.getDefaultOptionImagePreviewUrls(),
      imageDialogPreviewUrls: this.getDefaultOptionImagePreviewUrls(),
      importImagePreviewUrl: '',
      correctionImagePreviewUrl: '',
      importFileName: '',
      gradeOptions: [],
      subjectOptions: [],
      questionTypeOptions: [],
      sourceOptions: [],
      knowledgePointOptions: [],
      queryKnowledgePointOptions: [],
      bindKnowledgePointOptions: [],
      errorLabelOptions: [],
      knowledgePointStatistics: [],
      errorAnalysisStatistics: [],
      statusOptions: [
        { label: '待改', value: 0 },
        { label: '已改', value: 1 },
        { label: '已掌握', value: 2 },
        { label: '已归档', value: 3 }
      ],
      optionImageFields: [
        { label: 'A项图片', prop: 'imageUrl' },
        { label: 'B项图片', prop: 'imageUrl2' },
        { label: 'C项图片', prop: 'imageUrl3' },
        { label: 'D项图片', prop: 'imageUrl4' }
      ],
      queryParams: {
        current: 1,
        pageSize: 10,
        grade: '',
        subject: '',
        questionType: '',
        source: '',
        status: null,
        knowledgePointId: null,
        errorLabel: '',
        keyWord: ''
      },
      form: this.getDefaultForm(),
      correctionForm: this.getDefaultCorrectionForm(),
      bindForm: {
        id: null,
        knowledgePointIds: []
      },
      analysisForm: {
        id: null,
        errorLabelValues: [],
        wrongReason: ''
      },
      imageForm: {
        id: null,
        questionType: '',
        questionTypeName: '',
        imageUrl: '',
        imageUrl2: '',
        imageUrl3: '',
        imageUrl4: ''
      },
      importForm: this.getDefaultImportForm(),
      rules: {
        grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
        subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
        questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
        questionTitle: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
        questionContent: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
        source: [{ required: true, message: '请选择来源', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      importRules: {
        grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
        subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
        source: [{ required: true, message: '请选择来源', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        imageFileId: [{ required: true, message: '请上传A4文件', trigger: 'change' }]
      },
      correctionRules: {
        correctionAnswer: [{ required: true, message: '请输入订正答案', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadMenuCodes()
    this.loadDictOptions()
    this.getList()
  },
  computed: {
    isChoiceQuestion() {
      return this.form.questionTypeName === '选择题'
    },
    isDetailChoiceQuestion() {
      return Boolean(this.detail && (this.detail.questionTypeName || this.detail.questionType) === '选择题')
    },
    detailOptionImageFields() {
      if (!this.detail) {
        return []
      }
      return this.optionImageFields.filter(item => this.detail[item.prop])
    },
    isImageChoiceQuestion() {
      return (this.imageForm.questionTypeName || this.imageForm.questionType) === '选择题'
    },
    imageDialogFields() {
      return this.isImageChoiceQuestion
        ? this.optionImageFields
        : [{ label: '题目图片', prop: 'imageUrl' }]
    },
    hasWrongQuestionRowAction() {
      return [
        this.menuCode.WRONG_QUESTION.DETAIL,
        this.menuCode.WRONG_QUESTION.UPDATE,
        this.menuCode.WRONG_QUESTION.UPDATE_IMAGE,
        this.menuCode.WRONG_QUESTION.DELETE,
        'wrongQuestion-correction',
        'wrongQuestion-analysis'
      ].some(code => this.hasMenuCode(code))
    }
  },
  methods: {
    loadMenuCodes() {
      getCurrentUserInfo().then(data => {
        this.menuCodes = Array.from((data && data.menu) || [])
      }).catch(() => {
        this.menuCodes = []
      })
    },
    hasMenuCode(code) {
      return this.menuCodes.includes(this.menuCode.ADMIN) || this.menuCodes.includes(code)
    },
    loadDictOptions() {
      Promise.all([
        dictDataOptions({ dictType: DICT_TYPES.grade }),
        dictDataOptions({ dictType: DICT_TYPES.subject }),
        dictDataOptions({ dictType: DICT_TYPES.questionType }),
        dictDataOptions({ dictType: DICT_TYPES.source }),
        dictDataOptions({ dictType: DICT_TYPES.errorLabel })
      ]).then(([gradeOptions, subjectOptions, questionTypeOptions, sourceOptions, errorLabelOptions]) => {
        this.gradeOptions = gradeOptions || []
        this.subjectOptions = subjectOptions || []
        this.questionTypeOptions = questionTypeOptions || []
        this.sourceOptions = sourceOptions || []
        this.errorLabelOptions = errorLabelOptions || []
      }).catch(() => {
        this.gradeOptions = []
        this.subjectOptions = []
        this.questionTypeOptions = []
        this.sourceOptions = []
        this.errorLabelOptions = []
        this.$message.warning('年级、科目、题目类型或来源字典加载失败')
      })
    },
    getDefaultForm() {
      return {
        id: null,
        grade: '',
        subject: '',
        questionType: '',
        questionTypeName: '',
        questionTitle: '',
        questionContent: '',
        wrongAnswer: '',
        correctAnswer: '',
        wrongReason: '',
        analysis: '',
        learningPoint: '',
        knowledgePointIds: [],
        errorLabels: '',
        errorLabelValues: [],
        source: '',
        sourceName: '',
        imageUrl: '',
        imageUrl2: '',
        imageUrl3: '',
        imageUrl4: '',
        status: 0
      }
    },
    getDefaultCorrectionForm() {
      return {
        wrongQuestionId: null,
        correctionAnswer: '',
        correctionAnalysis: '',
        correctionImageUrl: '',
        correctionRemark: ''
      }
    },
    getDefaultOptionImagePreviewUrls() {
      return {
        imageUrl: '',
        imageUrl2: '',
        imageUrl3: '',
        imageUrl4: ''
      }
    },
    getDefaultImportForm() {
      return {
        grade: '',
        subject: '',
        source: '',
        learningPoint: '',
        errorLabels: '',
        errorLabelValues: [],
        imageFileId: null,
        status: 0
      }
    },
    getList() {
      // 分页数据先展示文本，再异步换取图片预览地址，避免文件下载阻塞整张列表。
      this.loading = true
      wrongQuestionPageList(this.queryParams).then(response => {
        const data = this.resolveData(response)
        this.tableData = data.list || []
        this.total = data.total || 0
        this.selectedRows = []
        this.fillListImagePreview()
        this.loadStatistics()
      }).catch(() => {
        this.tableData = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        current: 1,
        pageSize: 10,
        grade: '',
        subject: '',
        questionType: '',
        source: '',
        status: null,
        knowledgePointId: null,
        errorLabel: '',
        keyWord: ''
      }
      this.queryKnowledgePointOptions = []
      this.getList()
    },
    handleSizeChange(pageSize) {
      this.queryParams.pageSize = pageSize
      this.queryParams.current = 1
      this.getList()
    },
    handleCurrentChange(current) {
      this.queryParams.current = current
      this.getList()
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    handleExportPrint() {
      // 列表只含摘要字段，打印前逐题读取详情并准备富文本与图片。
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要导出的错题')
        return
      }
      const printWindow = createWrongQuestionPrintWindow()
      if (!printWindow) {
        this.$message.warning('请允许浏览器打开打印窗口')
        return
      }
      this.exportLoading = true
      Promise.all(this.selectedRows.map(row => wrongQuestionDetail({ id: row.id })))
        .then(responses => Promise.all(responses.map(response => {
          return this.preparePrintQuestion(this.resolveData(response))
        })))
        .then(questions => renderWrongQuestionPrint(printWindow, questions))
        .catch(() => {
          printWindow.close()
          this.$message.error('打印版生成失败')
        }).finally(() => {
          this.exportLoading = false
        })
    },
    preparePrintQuestion(question) {
      const isChoiceQuestion = (question.questionTypeName || question.questionType) === '选择题'
      const imageFields = isChoiceQuestion ? this.optionImageFields : this.optionImageFields.slice(0, 1)
      return Promise.all(imageFields.map(item => {
        if (!question[item.prop]) {
          return Promise.resolve(null)
        }
        return this.resolveFileUrl(question[item.prop]).then(url => url ? { label: item.label, url } : null)
      })).then(images => Object.assign({}, question, {
        questionContentHtml: this.formatRichContent(question.questionContent),
        statusName: this.getStatusLabel(question.status),
        images: images.filter(Boolean)
      }))
    },
    handleCreate() {
      this.dialogTitle = '错题录入'
      this.form = this.getDefaultForm()
      this.optionImagePreviewUrls = this.getDefaultOptionImagePreviewUrls()
      this.formOpen = true
      this.loadKnowledgePoints()
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleImport() {
      this.importForm = this.getDefaultImportForm()
      this.importImagePreviewUrl = ''
      this.importFileName = ''
      this.importOpen = true
      this.$nextTick(() => this.$refs.importFormRef && this.$refs.importFormRef.clearValidate())
    },
    handleUpdate(row) {
      wrongQuestionDetail({ id: row.id }).then(response => {
        this.dialogTitle = '错题修改'
        this.form = Object.assign(this.getDefaultForm(), this.resolveData(response))
        this.form.errorLabelValues = this.parseErrorLabels(this.form.errorLabels)
        this.optionImagePreviewUrls = this.getDefaultOptionImagePreviewUrls()
        this.fillOptionImagePreviewUrls(this.form, this.optionImagePreviewUrls)
        this.formOpen = true
        this.loadKnowledgePoints()
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
      }).catch(() => {})
    },
    handleImage(row) {
      this.imageForm = {
        id: row.id,
        questionType: row.questionType || '',
        questionTypeName: row.questionTypeName || '',
        imageUrl: row.imageUrl || '',
        imageUrl2: row.imageUrl2 || '',
        imageUrl3: row.imageUrl3 || '',
        imageUrl4: row.imageUrl4 || ''
      }
      this.imageDialogPreviewUrls = this.getDefaultOptionImagePreviewUrls()
      this.fillOptionImagePreviewUrls(this.imageForm, this.imageDialogPreviewUrls)
      this.imageOpen = true
    },
    handleDetail(row) {
      this.$router.push({ path: '/system/wrongquestion/detail', query: { id: row.id } })
    },
    handleCorrection(row) {
      this.correctionForm = this.getDefaultCorrectionForm()
      this.correctionForm.wrongQuestionId = row.id
      this.correctionImagePreviewUrl = ''
      this.correctionOpen = true
      this.$nextTick(() => this.$refs.correctionFormRef && this.$refs.correctionFormRef.clearValidate())
    },
    handleSimilarPractice(row) {
      this.$router.push({
        path: '/system/similar-practice',
        query: { wrongQuestionId: row.id }
      })
    },
    handleWeakPointPractice(item) {
      if (!item.knowledgePointName) {
        this.$message.warning('该知识点名称为空，暂无法生成专项练习')
        return
      }
      const query = { learningPoint: item.knowledgePointName }
      if (this.queryParams.subject) {
        query.subject = this.queryParams.subject
      }
      this.$router.push({ path: '/system/review', query })
    },
    handleBindKnowledgePoint(row) {
      wrongQuestionDetail({ id: row.id }).then(response => {
        const detail = this.resolveData(response)
        this.bindForm = {
          id: row.id,
          knowledgePointIds: detail.knowledgePointIds || []
        }
        this.bindKnowledgePointOptions = []
        this.loadBindKnowledgePoints(detail.grade, detail.subject)
        this.bindOpen = true
      }).catch(() => {})
    },
    handleErrorAnalysis(row) {
      wrongQuestionDetail({ id: row.id }).then(response => {
        const detail = this.resolveData(response)
        this.analysisForm = {
          id: row.id,
          errorLabelValues: this.parseErrorLabels(detail.errorLabels),
          wrongReason: detail.wrongReason || ''
        }
        this.analysisOpen = true
      }).catch(() => {})
    },
    handleStatusCommand(row, status) {
      const targetLabel = this.getStatusLabel(status)
      this.$confirm(`确认将该错题状态改为“${targetLabel}”吗？`, '状态流转', { type: 'warning' }).then(() => {
        return updateWrongQuestionStatus({ id: row.id, status })
      }).then(() => {
        this.$message.success('状态已更新')
        this.getList()
      }).catch(() => {})
    },
    resolveData(response) {
      if (response && response.data && response.data.data) {
        return response.data.data
      }
      if (response && response.data) {
        return response.data
      }
      return response || {}
    },
    formatRichContent(content) {
      // OCR 内容可能包含 HTML；展示前移除可执行标签和事件属性，保留安全排版。
      if (!content) {
        return '-'
      }
      const container = document.createElement('div')
      container.innerHTML = content
      container.querySelectorAll('script, style, iframe, object, embed').forEach(node => node.remove())
      container.querySelectorAll('*').forEach(node => {
        Array.from(node.attributes).forEach(attribute => {
          const name = attribute.name.toLowerCase()
          const value = attribute.value || ''
          if (name.startsWith('on') || /javascript:/i.test(value)) {
            node.removeAttribute(attribute.name)
          }
        })
        if (node.tagName === 'IMG') {
          const src = node.getAttribute('src') || ''
          if (src && !/^(https?:)?\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
            node.setAttribute('src', `/${src}`)
          }
        }
      })
      return container.innerHTML
    },
    beforeImageUpload(file) {
      // 在客户端提前拒绝非图片文件，减少无效上传和后端校验压力。
      const isImage = file.type && file.type.startsWith('image/')
      if (!isImage) {
        this.$message.warning('请上传图片文件')
        return false
      }
      return true
    },
    beforeImportFileUpload(file) {
      const ext = file.name && file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
      const allowedExts = ['jpg', 'jpeg', 'png', 'pdf']
      const isAllowed = (file.type && file.type.startsWith('image/')) || allowedExts.includes(ext)
      if (!isAllowed) {
        this.$message.warning('仅支持 JPG、JPEG、PNG、PDF；DOC/DOCX 暂不支持识别')
        return false
      }
      return true
    },
    handleDictChange(nameField, options, key) {
      const option = options.find(item => item.key === key)
      this.form[nameField] = option ? option.value : ''
    },
    parseErrorLabels(errorLabels) {
      if (!errorLabels) {
        return []
      }
      return Array.from(new Set(String(errorLabels).split(/[,，]/).map(item => item.trim()).filter(Boolean)))
    },
    stringifyErrorLabels(errorLabelValues) {
      return (errorLabelValues || []).map(item => String(item).trim()).filter(Boolean).join(',')
    },
    getStatusLabel(status) {
      const option = this.statusOptions.find(item => item.value === status)
      return option ? option.label : '-'
    },
    getStatusType(status) {
      const types = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
      return Object.prototype.hasOwnProperty.call(types, status) ? types[status] : 'info'
    },
    handleQuestionTypeChange(questionType) {
      this.handleDictChange('questionTypeName', this.questionTypeOptions, questionType)
      if (!this.isChoiceQuestion) {
        this.optionImageFields.slice(1).forEach(item => {
          this.form[item.prop] = ''
          this.$set(this.optionImagePreviewUrls, item.prop, '')
        })
      }
    },
    uploadFormImage(prop, options) {
      this.uploadWrongQuestionImage(options.file).then(fileData => {
        this.form[prop] = String(fileData.id)
        this.resolveFileUrl(this.form[prop]).then(url => {
          this.$set(this.optionImagePreviewUrls, prop, url)
        })
        this.$message.success('图片上传成功')
      }).catch(() => {})
    },
    uploadDialogImage(prop, options) {
      this.uploadWrongQuestionImage(options.file).then(fileData => {
        this.imageForm[prop] = String(fileData.id)
        this.resolveFileUrl(this.imageForm[prop]).then(url => {
          this.$set(this.imageDialogPreviewUrls, prop, url)
        })
        this.$message.success('图片上传成功')
      }).catch(() => {})
    },
    clearDialogImage(prop) {
      this.imageForm[prop] = ''
      this.$set(this.imageDialogPreviewUrls, prop, '')
    },
    uploadImportImage(options) {
      this.uploadWrongQuestionImage(options.file).then(fileData => {
        this.importForm.imageFileId = Number(fileData.id)
        this.importFileName = fileData.originName || options.file.name
        if (options.file.type && options.file.type.startsWith('image/')) {
          this.resolveFileUrl(this.importForm.imageFileId).then(url => {
            this.importImagePreviewUrl = url
          })
        } else {
          this.importImagePreviewUrl = ''
        }
        this.$message.success('A4文件上传成功')
        this.$nextTick(() => this.$refs.importFormRef && this.$refs.importFormRef.validateField('imageFileId'))
      }).catch(() => {})
    },
    uploadCorrectionImage(options) {
      this.uploadWrongQuestionImage(options.file).then(fileData => {
        this.correctionForm.correctionImageUrl = String(fileData.id)
        this.resolveFileUrl(this.correctionForm.correctionImageUrl).then(url => {
          this.correctionImagePreviewUrl = url
        })
        this.$message.success('订正图片上传成功')
      }).catch(() => {})
    },
    uploadWrongQuestionImage(file) {
      // 先获取短期上传凭证，再把文件和签名一起交给统一文件服务。
      return filePolicy({ uploadType: WRONG_QUESTION_UPLOAD_TYPE }).then(policy => {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('signature', policy.signature)
        formData.append('fileName', file.name)
        return uploadFile(formData)
      })
    },
    resolveFileUrl(fileId) {
      if (!fileId) {
        return Promise.resolve('')
      }
      const fileIdNumber = Number(fileId)
      if (!Number.isFinite(fileIdNumber)) {
        return Promise.resolve(fileId)
      }
      return downloadUrl({
        fileId: fileIdNumber,
        uploadType: WRONG_QUESTION_UPLOAD_TYPE
      }).catch(() => '')
    },
    fillListImagePreview() {
      // 使用 Vue.set 回写异步字段，确保 Vue 2 能追踪新增的预览地址属性。
      this.tableData.forEach(row => {
        if (!row.imageUrl) {
          this.$set(row, 'imagePreviewUrl', '')
          return
        }
        this.resolveFileUrl(row.imageUrl).then(url => {
          this.$set(row, 'imagePreviewUrl', url)
        })
      })
    },
    fillOptionImagePreviewUrls(data, previewUrls, callback) {
      const target = previewUrls || this.getDefaultOptionImagePreviewUrls()
      this.optionImageFields.forEach(item => {
        if (!data[item.prop]) {
          this.$set(target, item.prop, '')
          return
        }
        this.resolveFileUrl(data[item.prop]).then(url => {
          this.$set(target, item.prop, url)
          if (callback) {
            callback(Object.assign({}, target))
          }
        })
      })
      if (callback) {
        callback(Object.assign({}, target))
      }
    },
    validateChoiceImages() {
      // 选择题采用四张选项图存储，提交前必须保证 A-D 图片完整。
      if (!this.isChoiceQuestion) {
        return true
      }
      const missingOption = this.optionImageFields.find(item => !this.form[item.prop])
      if (missingOption) {
        this.$message.warning(`请上传${missingOption.label}`)
        return false
      }
      return true
    },
    submitForm() {
      // 新增与编辑复用表单；清除仅用于展示的字典名称，避免覆盖后端标准值。
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.form.id && !this.validateChoiceImages()) {
          return
        }
        const request = this.form.id ? updateWrongQuestion : createWrongQuestion
        const requestData = Object.assign({}, this.form)
        requestData.errorLabels = this.stringifyErrorLabels(this.form.errorLabelValues)
        delete requestData.errorLabelValues
        delete requestData.gradeName
        delete requestData.subjectName
        delete requestData.questionTypeName
        delete requestData.sourceName
        request(requestData).then(() => {
          this.$message.success('保存成功')
          this.formOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    loadKnowledgePoints() {
      if (!this.form.grade || !this.form.subject) {
        this.knowledgePointOptions = []
        return
      }
      knowledgePointList({ grade: this.form.grade, subject: this.form.subject }).then(rows => {
        this.knowledgePointOptions = rows || []
      })
    },
    loadQueryKnowledgePoints() {
      if (!this.queryParams.grade || !this.queryParams.subject) {
        this.queryKnowledgePointOptions = []
        this.queryParams.knowledgePointId = null
        return
      }
      knowledgePointList({ grade: this.queryParams.grade, subject: this.queryParams.subject }).then(rows => {
        this.queryKnowledgePointOptions = rows || []
      })
    },
    loadBindKnowledgePoints(grade, subject) {
      if (!grade || !subject) {
        this.bindKnowledgePointOptions = []
        return
      }
      knowledgePointList({ grade, subject }).then(rows => {
        this.bindKnowledgePointOptions = rows || []
      })
    },
    loadStatistics() {
      const params = {
        grade: this.queryParams.grade,
        subject: this.queryParams.subject,
        status: this.queryParams.status
      }
      wrongQuestionKnowledgePointStatistics(params).then(rows => {
        this.knowledgePointStatistics = rows || []
      }).catch(() => {
        this.knowledgePointStatistics = []
      })
      wrongQuestionErrorAnalysisStatistics(params).then(rows => {
        this.errorAnalysisStatistics = rows || []
      }).catch(() => {
        this.errorAnalysisStatistics = []
      })
      this.loadQueryKnowledgePoints()
    },
    submitImage() {
      const requestData = Object.assign({}, this.imageForm)
      delete requestData.questionType
      delete requestData.questionTypeName
      if (!this.isImageChoiceQuestion) {
        requestData.imageUrl2 = ''
        requestData.imageUrl3 = ''
        requestData.imageUrl4 = ''
      }
      updateWrongQuestionImage(requestData).then(() => {
        this.$message.success('错图修改成功')
        this.imageOpen = false
        this.getList()
      }).catch(() => {})
    },
    submitImport() {
      // OCR 导入是异步耗时操作，提交期间锁定按钮并在完成后刷新当前列表。
      this.$refs.importFormRef.validate(valid => {
        if (!valid) {
          return
        }
        this.importLoading = true
        const requestData = Object.assign({}, this.importForm)
        requestData.errorLabels = this.stringifyErrorLabels(this.importForm.errorLabelValues)
        delete requestData.errorLabelValues
        importWrongQuestionImage(requestData).then(data => {
          this.importOpen = false
          if (data.taskId) {
            this.$message.success('已创建识别任务，请在采集中心确认题块')
            this.$router.push({ name: 'QuestionCapture', query: { taskId: data.taskId } })
            return
          }
          this.$message.success(`导入成功，共导入${data.importCount || 0}道错题`)
          this.getList()
        }).catch(() => {}).finally(() => {
          this.importLoading = false
        })
      })
    },
    submitCorrection() {
      this.$refs.correctionFormRef.validate(valid => {
        if (!valid) {
          return
        }
        submitCorrectionRecord(this.correctionForm).then(() => {
          this.$message.success('订正已提交，错题已进入复习计划')
          this.correctionOpen = false
          this.getList()
        }).catch(() => {})
      })
    },
    submitBindKnowledgePoint() {
      bindWrongQuestionKnowledgePoint(this.bindForm).then(() => {
        this.$message.success('知识点已绑定')
        this.bindOpen = false
        this.getList()
      }).catch(() => {})
    },
    submitErrorAnalysis() {
      updateWrongQuestionErrorAnalysis({
        id: this.analysisForm.id,
        errorLabels: this.stringifyErrorLabels(this.analysisForm.errorLabelValues),
        wrongReason: this.analysisForm.wrongReason
      }).then(() => {
        this.$message.success('错因分析已保存')
        this.analysisOpen = false
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该错题吗？', '提示', { type: 'warning' }).then(() => {
        return deleteWrongQuestion({ id: row.id })
      }).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleBatchDelete() {
      const ids = this.selectedRows.map(row => row.id)
      if (ids.length === 0) {
        this.$message.warning('请选择要删除的错题')
        return
      }
      this.$confirm(`确认删除选中的 ${ids.length} 道错题吗？`, '提示', { type: 'warning' }).then(() => {
        return batchDeleteWrongQuestion({ ids })
      }).then(() => {
        this.$message.success('批量删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.wrong-question-page .filter-item {
  width: 180px;
}

.question-image {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.statistics-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.statistics-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px 10px;
  flex: 1;
  min-width: 0;
}

.statistics-main span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.statistics-main small {
  grid-column: 1 / -1;
  color: #909399;
}

.danger-action {
  color: #f56c6c;
}

.image-preview {
  width: 100%;
  max-height: 320px;
  margin-top: 12px;
}

.choice-image-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.choice-image-preview {
  width: 100%;
  height: 120px;
  margin-top: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.single-image-preview {
  width: 100%;
  max-height: 240px;
  margin-top: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.detail-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
  color: #606266;
  font-size: 14px;
}

.detail-actions {
  margin-bottom: 10px;
  text-align: right;
}

.detail-table th,
.detail-table td {
  padding: 12px 10px;
  border: 1px solid #ebeef5;
  line-height: 1.5;
  text-align: left;
  vertical-align: top;
  word-break: break-word;
}

.detail-table th {
  color: #909399;
  background: #fafafa;
  font-weight: 700;
}

.detail-label-column {
  width: 80px;
}

.detail-value-column {
  width: 83px;
}

.detail-option-images {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-option-image {
  min-width: 0;
}

.detail-option-image > span:first-child {
  display: block;
  margin-bottom: 6px;
  color: #606266;
}

.detail-image {
  max-width: 100%;
  max-height: 360px;
}

.rich-content {
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.rich-content ::v-deep img {
  display: inline-block;
  max-width: 100%;
  height: auto;
  vertical-align: middle;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  color: #606266;
}
</style>
