<template>
  <div class="practice-page">
    <section class="practice-hero">
      <div>
        <h1>个人组卷与练习</h1>
        <p>按知识点、错因或科目组卷；可打印题目版、答案版或导出 Word，纸面完成后用短码回填。</p>
      </div>
      <div><el-button @click="openExportHistory">导出历史</el-button><el-button type="primary" icon="el-icon-plus" @click="openCreate">新建练习</el-button></div>
    </section>

    <section class="stats-grid">
      <div class="stat-item">
        <span>练习次数</span>
        <strong>{{ statistics.sessionCount || 0 }}</strong>
        <small>完成 {{ statistics.finishedSessionCount || 0 }} 次</small>
      </div>
      <div class="stat-item">
        <span>累计作答</span>
        <strong>{{ statistics.answeredCount || 0 }}</strong>
        <small>题目 {{ statistics.questionCount || 0 }} 道</small>
      </div>
      <div class="stat-item">
        <span>平均正确率</span>
        <strong>{{ statistics.averageAccuracyRate || 0 }}%</strong>
        <small>正确 {{ statistics.correctCount || 0 }} · 错误 {{ statistics.wrongCount || 0 }}</small>
      </div>
      <div class="stat-item">
        <span>薄弱知识点</span>
        <strong>{{ firstWeakPoint }}</strong>
        <small>{{ firstWeakPointTip }}</small>
      </div>
      <div class="stat-item">
        <span>7天趋势</span>
        <strong>{{ trendText(7) }}</strong>
        <small>{{ trendTip(7) }}</small>
      </div>
      <div class="stat-item">
        <span>30天趋势</span>
        <strong>{{ trendText(30) }}</strong>
        <small>{{ trendTip(30) }}</small>
      </div>
    </section>

    <el-card shadow="never" class="content-card">
      <div class="filter-bar">
        <el-input v-model.trim="query.keyWord" clearable prefix-icon="el-icon-search" placeholder="搜索标题/知识点/错因" @keyup.enter.native="loadHistory" />
        <el-select v-model="query.practiceType" clearable placeholder="练习类型" @change="search">
          <el-option label="知识点专项" value="KNOWLEDGE" />
          <el-option label="错因练习" value="ERROR_LABEL" />
          <el-option label="典型错题" value="TYPICAL" />
        </el-select>
        <el-select v-model="query.subject" clearable placeholder="科目" @change="search">
          <el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
        <el-input v-model.trim="paperCode" clearable class="paper-code-input" placeholder="纸面短码，如 P0000000012" @keyup.enter.native="openPaperCode" />
        <el-button @click="openPaperCode">回填短码</el-button>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      </div>

      <el-table v-loading="historyLoading" :data="page.list">
        <el-table-column prop="title" label="练习" min-width="180" />
        <el-table-column label="类型" width="105"><template slot-scope="scope">{{ typeLabel(scope.row.practiceType) }}</template></el-table-column>
        <el-table-column prop="subjectName" label="科目" width="100" />
        <el-table-column prop="learningPoint" label="知识点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="errorLabel" label="错因" min-width="120" show-overflow-tooltip />
        <el-table-column label="结果" width="150">
          <template slot-scope="scope">{{ scope.row.correctCount || 0 }}/{{ scope.row.questionCount || 0 }} · {{ scope.row.accuracyRate || 0 }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope"><el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'warning'">{{ scope.row.status === 1 ? '已完成' : '进行中' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="168" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope"><el-button type="text" @click="openSession(scope.row.id)">进入练习</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination background layout="total, prev, pager, next" :current-page="query.current" :page-size="query.pageSize" :total="page.total" @current-change="changePage" />
    </el-card>

    <el-dialog title="新建练习卷" :visible.sync="createVisible" width="760px" append-to-body>
      <el-form :model="form" label-width="88px">
        <el-form-item label="练习卷名"><el-input v-model.trim="form.title" maxlength="100" show-word-limit placeholder="如：九月函数错题重练" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.practiceType" @change="resetBasket">
            <el-radio-button label="KNOWLEDGE">知识点</el-radio-button>
            <el-radio-button label="ERROR_LABEL">错因</el-radio-button>
            <el-radio-button label="TYPICAL">典型题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题源">
          <el-radio-group v-model="form.questionSource" @change="resetBasket">
            <el-radio-button label="WRONG_QUESTION">错题本</el-radio-button>
            <el-radio-button label="QUESTION_BANK">题库</el-radio-button>
            <el-radio-button label="MIXED">混合</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="科目"><el-select v-model="form.subject" clearable placeholder="全部科目" @change="resetBasket"><el-option v-for="item in subjectOptions" :key="item.key" :label="item.value" :value="item.key" /></el-select></el-form-item>
        <el-form-item v-if="form.practiceType === 'KNOWLEDGE'" label="知识点"><el-input v-model.trim="form.learningPoint" placeholder="输入知识点名称" @input="resetBasket" /></el-form-item>
        <el-form-item v-if="form.practiceType === 'ERROR_LABEL'" label="错因"><el-input v-model.trim="form.errorLabel" placeholder="输入错因标签" @input="resetBasket" /></el-form-item>
        <el-form-item label="难度"><el-select v-model="form.difficulty" clearable placeholder="全部难度" @change="resetBasket"><el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <template v-if="form.questionSource !== 'WRONG_QUESTION'">
          <el-row :gutter="12"><el-col :span="12"><el-form-item label="教材版本"><el-input v-model.trim="form.textbookVersion" placeholder="如 人教版·2024" @input="resetBasket" /></el-form-item></el-col><el-col :span="12"><el-form-item label="教材章节"><el-input v-model.trim="form.chapterName" placeholder="如 八上·一次函数" @input="resetBasket" /></el-form-item></el-col></el-row>
          <el-row :gutter="12"><el-col :span="8"><el-form-item label="真题地区"><el-input v-model.trim="form.region" placeholder="如 杭州" @input="resetBasket" /></el-form-item></el-col><el-col :span="8"><el-form-item label="真题年份"><el-input-number v-model="form.examYear" :min="1900" :max="2100" controls-position="right" @change="resetBasket" /></el-form-item></el-col><el-col :span="8"><el-form-item label="卷型"><el-input v-model.trim="form.paperType" placeholder="中考/模拟" @input="resetBasket" /></el-form-item></el-col></el-row>
          <p class="form-tip">仅从审核通过、已启用且授权未到期的题库中选题；教材/地区条件可单独或组合使用。</p>
        </template>
        <el-form-item v-if="form.questionSource !== 'QUESTION_BANK'" label="已掌握题"><el-switch v-model="form.includeMastered" active-text="包含" inactive-text="默认排除" @change="resetBasket" /></el-form-item>
        <el-form-item label="题数"><el-input-number v-model="form.questionCount" :min="1" :max="50" @change="resetBasket" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="10"><el-form-item label="每题留白"><el-input-number v-model="form.blankLineCount" :min="1" :max="10" /> 行</el-form-item></el-col>
          <el-col :span="14"><el-form-item label="答案位置"><el-radio-group v-model="form.answerPosition"><el-radio label="END">卷末集中</el-radio><el-radio label="AFTER_EACH">逐题显示</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="题面图片">
          <el-radio-group v-model="form.imageMode">
            <el-radio label="ORIGINAL">原图题块</el-radio>
            <el-radio label="GRAYSCALE">灰度预览题块</el-radio>
            <el-radio label="TEXT_ONLY">仅文字</el-radio>
          </el-radio-group>
          <p class="form-tip">灰度预览会保留笔迹；题目没有可用灰度图时自动回退原图。</p>
        </el-form-item>
        <el-form-item label="版式"><el-radio-group v-model="form.columnCount"><el-radio :label="1">A4 单栏</el-radio><el-radio :label="2">A4 双栏</el-radio></el-radio-group></el-form-item>
      </el-form>
      <div v-if="preview" class="practice-preview" :class="{ 'is-shortage': preview.shortageQuestionCount > 0 }">
        <strong>组卷预览</strong>
        <span>可组 {{ preview.availableQuestionCount || 0 }}/{{ preview.requestedQuestionCount || form.questionCount }} 题</span>
        <span>错题本 {{ preview.wrongQuestionCount || 0 }} · 题库 {{ preview.bankQuestionCount || 0 }}</span>
        <p v-if="preview.learningPointList && preview.learningPointList.length">覆盖：{{ preview.learningPointList.join('、') }}</p>
        <p>{{ preview.shortageQuestionCount > 0 ? `当前条件还缺 ${preview.shortageQuestionCount} 题，请调整条件或减少题量。` : '题量充足，可以生成练习。' }}</p>
      </div>
      <section v-if="basketQuestions.length" class="paper-basket">
        <div class="paper-basket-head"><strong>组卷篮 · {{ basketQuestions.length }} 题</strong><span>可调题序，也可移除不需要的题目</span></div>
        <div v-for="(item, index) in basketQuestions" :key="`${item.questionSource}:${item.questionId}`" class="paper-basket-item">
          <span class="paper-basket-index">{{ index + 1 }}</span>
          <div><b>{{ item.questionTitle || '未命名题目' }}</b><p>{{ item.subjectName || '未设置科目' }}<template v-if="item.learningPoint"> · {{ item.learningPoint }}</template><template v-if="item.sourceReason"> · {{ item.sourceReason }}</template></p></div>
          <div class="paper-basket-actions"><el-button type="text" :disabled="index === 0" @click="moveBasketQuestion(index, -1)">上移</el-button><el-button type="text" :disabled="index === basketQuestions.length - 1" @click="moveBasketQuestion(index, 1)">下移</el-button><el-button type="text" class="danger-link" @click="removeBasketQuestion(index)">移除</el-button></div>
        </div>
      </section>
      <div slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button :loading="previewing" @click="previewSession(true)">{{ basketQuestions.length ? '重新智能选题' : '预览组卷' }}</el-button>
        <el-button type="primary" :loading="creating" @click="createSession">保存并开始练习</el-button>
      </div>
    </el-dialog>
    <el-dialog title="服务端导出历史" :visible.sync="exportHistoryVisible" width="760px" append-to-body>
      <el-table :data="exportTaskList" size="small"><el-table-column prop="fileName" label="文件" min-width="220" /><el-table-column prop="format" label="格式" width="80" /><el-table-column label="卷版本" width="80"><template slot-scope="{ row }">v{{ row.paperVersion || 1 }}</template></el-table-column><el-table-column label="内容" width="90"><template slot-scope="{ row }">{{ row.answerMode === 1 ? '答案版' : '题目版' }}</template></el-table-column><el-table-column label="状态" width="100"><template slot-scope="{ row }">{{ exportStatusText(row.status) }}</template></el-table-column><el-table-column prop="createTime" label="创建时间" width="170" /><el-table-column label="操作" width="100"><template slot-scope="{ row }"><el-button v-if="row.status === 2" type="text" @click="downloadExportTask(row)">下载</el-button><span v-else-if="row.status === 3" class="export-error" :title="row.errorMessage">失败</span></template></el-table-column></el-table>
    </el-dialog>

    <el-dialog title="纸面练习逐题回填" :visible.sync="paperFillVisible" width="900px" append-to-body>
      <div v-loading="paperFillLoading">
        <template v-if="paperFill.sessionId">
          <div class="paper-fill-summary">
            <span>短码 {{ paperFill.paperCode }}</span><span>练习卷 v{{ paperFill.paperVersion || 1 }}</span><span>题目 {{ paperFill.questionCount || 0 }}</span>
            <span v-if="paperFill.hasPreviousRecord">最近一次：第 {{ paperFill.latestAttemptNo }} 次纸面作答</span>
          </div>
          <el-alert type="info" :closable="false" title="请根据纸面作答逐题自评。上传做完的练习卷只作为附件留存，系统不会据此自动判题。" />
          <el-form class="paper-fill-form" label-width="96px">
            <el-form-item label="重复回填">
              <el-radio-group v-model="paperFillForm.fillMode">
                <el-radio-button label="NEW">新增一次作答</el-radio-button>
                <el-radio-button label="OVERWRITE" :disabled="!paperFill.hasPreviousRecord">覆盖最近一次</el-radio-button>
              </el-radio-group>
              <p class="form-tip">覆盖仅修订最近一次记录；若把原先正确改为错误，会立即重新安排复习，但不会因重复覆盖重复提高掌握度。</p>
            </el-form-item>
            <el-form-item label="实际作答时间"><el-date-picker v-model="paperFillForm.answerTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="留空则记录当前时间" /></el-form-item>
            <el-form-item label="完成卷附件">
              <el-upload action="" :show-file-list="false" accept="image/*,.pdf" :http-request="uploadPaperFillFile" :before-upload="beforePaperFillFileUpload">
                <el-button size="small" :loading="paperFillUploading">上传图片或 PDF</el-button>
              </el-upload>
              <span v-if="paperFillFileName" class="paper-fill-file">{{ paperFillFileName }} <el-button type="text" @click="clearPaperFillFile">移除</el-button></span>
              <p class="form-tip">附件会关联本次回填，不会自动识别或判定答案。</p>
            </el-form-item>
          </el-form>
          <article v-for="(item, index) in paperFill.questionList" :key="item.sessionQuestionId" class="paper-fill-question">
            <div class="question-head"><el-tag size="mini">第 {{ item.sortNo || index + 1 }} 题</el-tag><span>{{ item.questionTitle || '未命名题目' }}</span><small v-if="item.learningPoint">{{ item.learningPoint }}</small></div>
            <question-content-renderer :content="item.questionContent" :content-format="item.contentFormat" :options-json="item.optionsJson" empty-text="纸面练习卷中已有题目内容。" />
            <div v-if="item.latestRecord" class="paper-fill-previous">
              上次：第 {{ item.latestRecord.attemptNo }} 次 · {{ paperAnswerStatusLabel(item.latestRecord.answerStatus) }} · {{ item.latestRecord.answerTime || '-' }}
              <template v-if="item.latestRecord.errorReason"> · 错因：{{ item.latestRecord.errorReason }}</template>
            </div>
            <el-form inline class="paper-fill-answer-row">
              <el-form-item label="结果">
                <el-radio-group v-model="item.answerStatus" size="small">
                  <el-radio-button label="CORRECT">正确</el-radio-button><el-radio-button label="WRONG">错误</el-radio-button><el-radio-button label="UNANSWERED">未作答</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="用时"><el-input-number v-model="item.durationSeconds" :min="0" :max="86400" controls-position="right" /> 秒</el-form-item>
            </el-form>
            <el-input v-model.trim="item.studentAnswer" type="textarea" :rows="2" maxlength="4000" show-word-limit placeholder="可选：记录纸面答案或解题步骤" />
            <el-input v-if="item.answerStatus === 'WRONG'" v-model.trim="item.errorReason" class="paper-fill-error-reason" maxlength="500" show-word-limit placeholder="可选：记录这次错误的原因，如计算、审题或概念遗漏" />
          </article>
        </template>
      </div>
      <div slot="footer"><el-button @click="paperFillVisible = false">取消</el-button><el-button type="primary" :loading="paperFillSubmitting" :disabled="!paperFill.sessionId" @click="submitPaperFill">保存纸面回填</el-button></div>
    </el-dialog>

    <el-dialog :title="session.title || '专项练习'" :visible.sync="sessionVisible" width="860px" append-to-body>
      <div v-if="session.sessionId" class="session-summary">
        <span>短码 {{ currentPaperCode }}</span>
        <span>题目 {{ session.questionCount || 0 }}</span>
        <span>已答 {{ session.answeredCount || 0 }}</span>
        <span>正确 {{ session.correctCount || 0 }}</span>
        <span>正确率 {{ session.accuracyRate || 0 }}%</span>
        <span>题面 {{ imageModeLabel(session.imageMode) }}</span>
        <span v-if="draftSaving">草稿保存中...</span>
        <span v-else-if="draftSavedText">{{ draftSavedText }}</span>
      </div>
      <el-alert v-if="session.generationReason" class="generation-reason" type="info" :closable="false" :title="`组卷依据：${session.generationReason}`" />
      <section v-if="session.sessionId && session.status === 1" class="result-panel">
        <div>
          <span>练习结果</span>
          <strong>{{ resultLevel(session.accuracyRate) }}</strong>
        </div>
        <div>
          <span>需回看题目</span>
          <strong>{{ wrongQuestionList.length }}</strong>
        </div>
        <div>
          <span>复习计划</span>
          <strong>{{ wrongQuestionList.length ? '已提前安排' : '正常巩固' }}</strong>
        </div>
      </section>
      <section v-if="wrongQuestionList.length" class="wrong-review">
        <h4>错题复盘</h4>
        <el-tag v-for="item in wrongQuestionList" :key="item.sessionQuestionId" type="danger" size="mini">
          {{ item.learningPoint || item.questionTitle || '未命名题目' }}
        </el-tag>
      </section>
      <article v-for="(item,index) in session.questionList" :key="item.sessionQuestionId" class="question-card">
        <div class="question-head">
          <el-tag size="mini">第 {{ index + 1 }} 题</el-tag>
          <el-tag v-if="item.answered" size="mini" :type="item.isCorrect === 1 ? 'success' : 'danger'">
            {{ item.isCorrect === 1 ? '正确' : '错误' }}
          </el-tag>
          <el-tag v-else size="mini" type="info">{{ sourceLabel(item.questionSource) }}</el-tag>
        </div>
        <p v-if="item.sourceReason" class="source-reason">入选依据：{{ item.sourceReason }}</p>
        <h3>{{ item.questionTitle || '未命名题目' }}</h3>
        <question-content-renderer :content="item.questionContent" :content-format="item.contentFormat" :options-json="item.optionsJson" class="question-content" empty-text="暂无题干文字，请查看图片。" />
        <div v-if="imageList(item).length" class="question-images"><el-image v-for="image in imageList(item)" :key="image" :src="image" :preview-src-list="imageList(item)" fit="contain" /></div>
        <el-alert class="judge-tip" :closable="false" type="info" :title="isAutoJudge(item) ? '本题将按标准答案自动判定；提交后即可查看解析。' : '本题为主观题，请先作答，查看参考答案后再完成自评。'" />
        <el-input v-model.trim="item.studentAnswer" type="textarea" :rows="3" :disabled="item.answered" placeholder="输入答案或解题步骤" @focus="startAnswerTiming(item)" @blur="pauseAnswerTiming(item, true)" @input="scheduleDraftSave(item)" />
        <section v-if="item.referenceAnswer" class="reference-answer">
          <p><b>标准答案：</b>{{ item.referenceAnswer.correctAnswer || '-' }}</p>
          <p v-if="item.referenceAnswer.analysis"><b>解析：</b>{{ item.referenceAnswer.analysis }}</p>
        </section>
        <div class="answer-actions">
          <el-button v-if="!isAutoJudge(item) && !item.referenceAnswer && !item.answered" size="small" :disabled="!item.studentAnswer" :loading="item.revealing" @click="revealReferenceAnswer(item)">查看参考答案</el-button>
          <el-radio-group v-if="!isAutoJudge(item) && item.referenceAnswer" v-model="item.selfCorrect" size="small" :disabled="item.answered">
            <el-radio-button :label="1">自评正确</el-radio-button>
            <el-radio-button :label="0">自评错误</el-radio-button>
          </el-radio-group>
          <span v-if="isAutoJudge(item)" class="auto-judge-text">系统自动判题</span>
          <el-button type="primary" size="small" :disabled="item.answered || (!isAutoJudge(item) && !item.referenceAnswer)" :loading="item.submitting" @click="submitAnswer(item)">提交</el-button>
        </div>
        <el-alert v-if="item.result" :type="item.result.correct ? 'success' : 'error'" :title="item.result.correct ? '回答正确' : `回答错误，正确答案：${item.result.correctAnswer || '-'}`" :closable="false">
          <div>判定方式：{{ item.result.judgeType === 'AUTO' ? '系统自动判定' : '根据自评结果判定' }}</div>
          <div v-if="item.result.autoCollectedWrongQuestion">已自动加入错题本，并进入复习计划。</div>
          <div v-if="item.result.analysis">解析：{{ item.result.analysis }}</div>
        </el-alert>
        <div v-else-if="item.answered" class="answer-review">
          <p><b>我的答案：</b>{{ item.studentAnswer || '-' }}</p>
          <p><b>正确答案：</b>{{ item.correctAnswer || '-' }}</p>
          <p v-if="item.analysis"><b>解析：</b>{{ item.analysis }}</p>
          <p><b>用时：</b>{{ item.durationSeconds || 0 }} 秒</p>
        </div>
      </article>
      <div slot="footer">
        <el-button @click="sessionVisible = false">关闭</el-button>
        <el-button :disabled="!session.sessionId" icon="el-icon-printer" @click="printPaper(false)">基础打印（题目版）</el-button>
        <el-button :disabled="!session.sessionId" icon="el-icon-printer" @click="printPaper(true)">基础打印（答案版）</el-button>
        <el-dropdown :disabled="!session.sessionId" @command="downloadWord">
          <el-button>基础 HTML Word<i class="el-icon-arrow-down el-icon--right" /></el-button>
          <el-dropdown-menu slot="dropdown"><el-dropdown-item :command="false">题目版</el-dropdown-item><el-dropdown-item :command="true">答案版</el-dropdown-item></el-dropdown-menu>
        </el-dropdown>
        <el-dropdown :disabled="!session.sessionId" @command="createServerExport">
          <el-button>服务端导出<i class="el-icon-arrow-down el-icon--right" /></el-button>
          <el-dropdown-menu slot="dropdown"><el-dropdown-item command="PDF:0">PDF 题目版</el-dropdown-item><el-dropdown-item command="PDF:1">PDF 答案版</el-dropdown-item><el-dropdown-item command="DOCX:0">DOCX 题目版</el-dropdown-item><el-dropdown-item command="DOCX:1">DOCX 答案版</el-dropdown-item></el-dropdown-menu>
        </el-dropdown>
        <el-button :disabled="!session.sessionId || session.status === 1" :loading="draftSaving" @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" :disabled="!canBatchSubmit" :loading="batchSubmitting" @click="batchSubmit">批量提交已作答</el-button>
        <el-button type="success" :disabled="!session.sessionId || session.status === 1" @click="finishSession">完成练习</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { batchSubmitPracticeAnswer, createPracticePaperExportTask, createPracticeSession, finishPracticeSession, practicePaperDetail, practicePaperExportTaskList, practicePaperFillDetail, practiceQuestionAnswer, practiceSessionDetail, practiceSessionPageList, practiceStatistics, previewPracticeSession, savePracticeAnswerDraft, submitPracticeAnswer, submitPracticePaperAnswerFill } from '@/api/system/review'
import { downloadUrl, filePolicy, uploadFile } from '@/api/system/file'
import { dictDataOptions } from '@/api/system/dict'
import { downloadPracticePaperWord, practicePaperCode, printPracticePaper } from '@/utils/practicePaperExport'
import QuestionContentRenderer from '@/components/QuestionContentRenderer.vue'

const emptyPage = () => ({ current: 1, pageSize: 10, total: 0, list: [] })
const emptySession = () => ({ sessionId: null, questionList: [] })
const emptyPaperFill = () => ({ sessionId: null, questionList: [] })
const emptyStatistics = () => ({ sessionCount: 0, finishedSessionCount: 0, questionCount: 0, answeredCount: 0, correctCount: 0, wrongCount: 0, averageAccuracyRate: 0, weakLearningPointList: [], errorLabelList: [], trendList: [] })
const emptyPracticeForm = () => ({ title: '', practiceType: 'KNOWLEDGE', questionSource: 'WRONG_QUESTION', subject: '', learningPoint: '', errorLabel: '', difficulty: null, textbookVersion: '', chapterName: '', region: '', examYear: null, paperType: '', includeMastered: false, questionCount: 5, blankLineCount: 3, answerPosition: 'END', imageMode: 'ORIGINAL', columnCount: 1, selectedQuestionList: [] })
const PAPER_FILL_UPLOAD_TYPE = 'wrongQuestion'

export default {
  name: 'PracticeSessionPage',
  components: { QuestionContentRenderer },
  data() {
    return {
      historyLoading: false,
      creating: false,
      previewing: false,
      batchSubmitting: false,
      draftSaving: false,
      paperFillLoading: false,
      paperFillSubmitting: false,
      paperFillUploading: false,
      exportHistoryVisible: false,
      exportTaskList: [],
      draftSavedText: '',
      draftTimer: null,
      answerResultMap: {},
      referenceAnswerMap: {},
      paperCode: '',
      createVisible: false,
      sessionVisible: false,
      paperFillVisible: false,
      paperFillFileName: '',
      paperFillForm: { fillMode: 'NEW', answerTime: '', answerFileId: null },
      subjectOptions: [],
      basketQuestions: [],
      query: { current: 1, pageSize: 10, keyWord: '', practiceType: '', subject: '' },
      page: emptyPage(),
      statistics: emptyStatistics(),
      paperImageCache: {},
      difficultyOptions: [{ label: '1 - 简单', value: 1 }, { label: '2 - 较易', value: 2 }, { label: '3 - 中等', value: 3 }, { label: '4 - 较难', value: 4 }, { label: '5 - 困难', value: 5 }],
      form: emptyPracticeForm(),
      preview: null,
      session: emptySession(),
      paperFill: emptyPaperFill()
    }
  },
  computed: {
    wrongQuestionList() {
      return (this.session.questionList || []).filter(item => item.answered && item.isCorrect === 0)
    },
    firstWeakPoint() {
      const first = (this.statistics.weakLearningPointList || [])[0]
      return first ? first.name : '-'
    },
    firstWeakPointTip() {
      const first = (this.statistics.weakLearningPointList || [])[0]
      return first ? `正确率 ${first.accuracyRate || 0}% · 错 ${first.wrongCount || 0}` : '暂无薄弱项'
    },
    canBatchSubmit() {
      return this.session.sessionId && this.session.status !== 1
        && (this.session.questionList || []).some(item => !item.answered && item.studentAnswer)
    }
    ,
    currentPaperCode() {
      return this.session.sessionId ? practicePaperCode(this.session) : ''
    }
  },
  created() {
    this.loadSubjectOptions()
    this.loadHistory()
    this.loadStatistics()
    const learningPoint = this.$route.query.learningPoint
    if (learningPoint) {
      this.form.learningPoint = learningPoint
      this.form.subject = this.$route.query.subject || ''
      this.createVisible = true
    }
    const basketIds = String(this.$route.query.basket || '').split(',')
      .map(id => Number(id)).filter(id => Number.isInteger(id) && id > 0).slice(0, 50)
    if (basketIds.length) {
      this.form = Object.assign(emptyPracticeForm(), {
        practiceType: 'TYPICAL',
        questionSource: 'WRONG_QUESTION',
        questionCount: basketIds.length,
        selectedQuestionList: basketIds.map(questionId => ({ questionSource: 'WRONG_QUESTION', questionId }))
      })
      this.createVisible = true
      this.previewSession(false)
    }
  },
  beforeDestroy() {
    if (this.draftTimer) {
      clearTimeout(this.draftTimer)
    }
  },
  methods: {
    openCreate() {
      this.form = emptyPracticeForm()
      this.preview = null
      this.basketQuestions = []
      this.createVisible = true
    },
    resetBasket() {
      this.preview = null
      this.basketQuestions = []
      this.form.selectedQuestionList = []
    },
    syncBasketSelection() {
      this.form.selectedQuestionList = this.basketQuestions.map(item => ({
        questionSource: item.questionSource,
        questionId: item.questionId
      }))
      this.form.questionCount = this.basketQuestions.length || 1
    },
    moveBasketQuestion(index, offset) {
      const target = index + offset
      if (target < 0 || target >= this.basketQuestions.length) return
      const next = this.basketQuestions.slice()
      const current = next.splice(index, 1)[0]
      next.splice(target, 0, current)
      this.basketQuestions = next
      this.syncBasketSelection()
    },
    removeBasketQuestion(index) {
      this.basketQuestions.splice(index, 1)
      this.syncBasketSelection()
      if (!this.basketQuestions.length) this.preview = null
    },
    practiceCreatePayload(useBasket = true) {
      const payload = Object.assign({}, this.form)
      payload.selectedQuestionList = useBasket
        ? (this.basketQuestions.length
          ? this.basketQuestions.map(item => ({ questionSource: item.questionSource, questionId: item.questionId }))
          : (this.form.selectedQuestionList || []))
        : []
      if (payload.selectedQuestionList.length) payload.questionCount = payload.selectedQuestionList.length
      return payload
    },
    async loadSubjectOptions() {
      this.subjectOptions = await dictDataOptions({ dictType: 'subject' }).catch(() => [])
    },
    async loadHistory() {
      this.historyLoading = true
      try {
        this.page = Object.assign(emptyPage(), await practiceSessionPageList(this.query))
      } finally {
        this.historyLoading = false
      }
    },
    async loadStatistics() {
      this.statistics = Object.assign(emptyStatistics(), await practiceStatistics(this.query))
    },
    search() {
      this.query.current = 1
      this.loadHistory()
      this.loadStatistics()
    },
    changePage(page) {
      this.query.current = page
      this.loadHistory()
    },
    async createSession() {
      this.creating = true
      try {
        if (!this.basketQuestions.length) {
          await this.previewSession(true)
        }
        if (!this.basketQuestions.length || (this.preview && this.preview.shortageQuestionCount > 0)) {
          const shortage = this.preview ? this.preview.shortageQuestionCount : this.form.questionCount
          this.$message.warning(`当前条件还缺 ${shortage} 题，请调整条件后再生成`)
          return
        }
        this.session = this.prepareSession(await createPracticeSession(this.practiceCreatePayload(true)))
        this.createVisible = false
        this.sessionVisible = true
        this.loadHistory()
        this.loadStatistics()
      } finally {
        this.creating = false
      }
    },
    async previewSession(resetSelection = false) {
      this.previewing = true
      try {
        if (resetSelection) {
          this.basketQuestions = []
          this.form.selectedQuestionList = []
        }
        this.preview = await previewPracticeSession(this.practiceCreatePayload(!resetSelection))
        if (this.preview.shortageQuestionCount > 0) {
          this.$message.warning(`当前条件还缺 ${this.preview.shortageQuestionCount} 题`)
          return this.preview
        }
        this.basketQuestions = (this.preview.questionList || []).slice()
        this.syncBasketSelection()
        return this.preview
      } finally {
        this.previewing = false
      }
    },
    async openSession(id) {
      this.session = this.prepareSession(await practiceSessionDetail({ sessionId: id }))
      this.sessionVisible = true
    },
    async openPaperCode() {
      const normalized = String(this.paperCode || '').trim().toUpperCase().replace(/^P/, '')
      if (!/^\d+$/.test(normalized) || Number(normalized) < 1) {
        this.$message.warning('请输入有效短码，例如 P0000000012')
        return
      }
      await this.openPaperFill(`P${normalized}`)
      this.paperCode = ''
    },
    async openPaperFill(paperCode) {
      this.paperFillLoading = true
      this.paperFillVisible = true
      this.paperFillForm = { fillMode: 'NEW', answerTime: '', answerFileId: null }
      this.paperFillFileName = ''
      try {
        this.paperFill = this.preparePaperFill(await practicePaperFillDetail({ paperCode }))
      } finally {
        this.paperFillLoading = false
      }
    },
    preparePaperFill(paperFill) {
      const next = Object.assign(emptyPaperFill(), paperFill || {})
      next.questionList = (next.questionList || []).map(item => {
        const recordList = Array.isArray(item.answerRecordList) ? item.answerRecordList : []
        const latestRecord = recordList[0] || null
        return Object.assign({}, item, {
          latestRecord,
          answerStatus: 'UNANSWERED',
          studentAnswer: '',
          errorReason: '',
          durationSeconds: 0
        })
      })
      return next
    },
    beforePaperFillFileUpload(file) {
      const type = String(file.type || '')
      const name = String(file.name || '').toLowerCase()
      if (!type.startsWith('image/') && !name.endsWith('.pdf')) {
        this.$message.error('仅支持图片或 PDF 格式的完成卷附件')
        return false
      }
      if (file.size > 20 * 1024 * 1024) {
        this.$message.error('完成卷附件不能超过 20MB')
        return false
      }
      return true
    },
    uploadPaperFillFile(options) {
      this.paperFillUploading = true
      filePolicy({ uploadType: PAPER_FILL_UPLOAD_TYPE }).then(policy => {
        const formData = new FormData()
        formData.append('file', options.file)
        formData.append('signature', policy.signature)
        formData.append('fileName', options.file.name)
        return uploadFile(formData)
      }).then(file => {
        this.paperFillForm.answerFileId = Number(file.id)
        this.paperFillFileName = file.originName || options.file.name
        options.onSuccess(file)
      }).catch(error => options.onError(error)).finally(() => { this.paperFillUploading = false })
    },
    clearPaperFillFile() {
      this.paperFillForm.answerFileId = null
      this.paperFillFileName = ''
    },
    async submitPaperFill() {
      this.paperFillSubmitting = true
      try {
        const response = await submitPracticePaperAnswerFill({
          paperCode: this.paperFill.paperCode,
          fillMode: this.paperFillForm.fillMode,
          answerFileId: this.paperFillForm.answerFileId,
          answerTime: this.paperFillForm.answerTime || null,
          answerList: (this.paperFill.questionList || []).map(item => ({
            sessionQuestionId: item.sessionQuestionId,
            answerStatus: item.answerStatus,
            studentAnswer: item.studentAnswer || '',
            errorReason: item.answerStatus === 'WRONG' ? item.errorReason || '' : '',
            durationSeconds: Number(item.durationSeconds || 0)
          }))
        })
        this.paperFill = this.preparePaperFill(response)
        this.paperFillForm = { fillMode: 'NEW', answerTime: '', answerFileId: null }
        this.paperFillFileName = ''
        this.$message.success('纸面逐题回填已保存，错误题已同步到后续复习')
        this.loadHistory()
        this.loadStatistics()
      } finally {
        this.paperFillSubmitting = false
      }
    },
    paperAnswerStatusLabel(status) {
      return ({ CORRECT: '正确', WRONG: '错误', UNANSWERED: '未作答' })[status] || '未作答'
    },
    async printPaper(answerMode) {
      const printWindow = window.open('', '_blank')
      if (!printWindow) {
        this.$message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
        return
      }
      try {
        const session = await this.sessionForExport(answerMode)
        if (!printPracticePaper(session, answerMode, printWindow)) {
          this.$message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
        }
      } catch (error) {
        printWindow.close()
      }
    },
    async downloadWord(answerMode) {
      const session = await this.sessionForExport(answerMode)
      downloadPracticePaperWord(session, answerMode)
      this.$message.success(`已导出 ${answerMode ? '答案版' : '题目版'} Word 文件`)
    },
    async createServerExport(command) {
      const [format, answerMode] = String(command).split(':')
      const taskId = await createPracticePaperExportTask({ sessionId: this.session.sessionId, format, answerMode: Number(answerMode) })
      this.$message.success('导出任务已提交，正在后台生成')
      this.pollServerExport(taskId)
    },
    async pollServerExport(taskId) {
      const taskList = await practicePaperExportTaskList()
      const task = (taskList || []).find(item => item.id === taskId)
      if (!task || task.status === 0 || task.status === 1) return setTimeout(() => this.pollServerExport(taskId), 1200)
      if (task.status === 2 && task.fileId) { const url = await downloadUrl({ fileId: task.fileId, uploadType: 'PRACTICE_EXPORT' }); window.open(url, '_blank'); return }
      this.$message.error(task.errorMessage || '导出失败，请重试')
    },
    async openExportHistory() { this.exportTaskList = await practicePaperExportTaskList(); this.exportHistoryVisible = true },
    exportStatusText(status) { return ({ 0: '排队中', 1: '生成中', 2: '已完成', 3: '失败' })[status] || '未知' },
    async downloadExportTask(task) { const url = await downloadUrl({ fileId: task.fileId, uploadType: 'PRACTICE_EXPORT' }); window.open(url, '_blank') },
    async sessionForExport() {
      const session = this.prepareSession(await practicePaperDetail({ sessionId: this.session.sessionId }))
      await this.resolveSessionImages(session)
      return session
    },
    async submitAnswer(item) {
      if (!item.studentAnswer) {
        this.$message.warning('请先输入答案')
        return
      }
      if (!this.isAutoJudge(item) && item.selfCorrect === null) {
        this.$message.warning('请先完成主观题自评')
        return
      }
      this.pauseAnswerTiming(item)
      item.submitting = true
      try {
        const result = await submitPracticeAnswer({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer,
          selfCorrect: item.selfCorrect,
          durationSeconds: item.durationSeconds || 0
        })
        this.$set(this.answerResultMap, item.sessionQuestionId, result)
        this.session = this.prepareSession(await practiceSessionDetail({ sessionId: this.session.sessionId }))
        this.loadStatistics()
      } finally {
        item.submitting = false
      }
    },
    async revealReferenceAnswer(item) {
      if (!item.studentAnswer) {
        this.$message.warning('请先完成作答，再查看参考答案')
        return
      }
      this.pauseAnswerTiming(item, true)
      item.revealing = true
      try {
        const referenceAnswer = await practiceQuestionAnswer({ sessionQuestionId: item.sessionQuestionId })
        this.$set(this.referenceAnswerMap, item.sessionQuestionId, referenceAnswer)
        item.referenceAnswer = referenceAnswer
      } finally {
        item.revealing = false
      }
    },
    scheduleDraftSave(item) {
      if (!this.session.sessionId || item.answered) {
        return
      }
      item.dirty = true
      this.draftSavedText = ''
      if (this.draftTimer) {
        clearTimeout(this.draftTimer)
      }
      this.draftTimer = setTimeout(() => {
        this.saveDraft(true)
      }, 1200)
    },
    startAnswerTiming(item) {
      if (!item.answered && !item.answerStartedAt) {
        item.answerStartedAt = Date.now()
      }
    },
    pauseAnswerTiming(item, saveDraft) {
      if (!item || !item.answerStartedAt) return
      const elapsed = Math.max(0, Math.round((Date.now() - item.answerStartedAt) / 1000))
      item.durationSeconds = Math.min(86400, Number(item.durationSeconds || 0) + elapsed)
      item.answerStartedAt = null
      if (saveDraft) this.scheduleDraftSave(item)
    },
    async saveDraft(silent) {
      if (!this.session.sessionId || this.session.status === 1) {
        return
      }
      const draftList = (this.session.questionList || [])
        .filter(item => !item.answered && (item.dirty || !silent))
        .map(item => ({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer || '',
          durationSeconds: item.durationSeconds || 0
        }))
      if (!draftList.length) {
        if (!silent) this.$message.info('暂无需要保存的草稿')
        return
      }
      this.draftSaving = true
      try {
        this.session = this.prepareSession(await savePracticeAnswerDraft({
          sessionId: this.session.sessionId,
          draftList
        }))
        this.draftSavedText = '草稿已保存'
        if (!silent) this.$message.success('草稿已保存')
      } finally {
        this.draftSaving = false
      }
    },
    async batchSubmit() {
      const currentQuestionList = this.session.questionList || []
      currentQuestionList.forEach(item => this.pauseAnswerTiming(item))
      const answerList = (this.session.questionList || [])
        .filter(item => !item.answered && item.studentAnswer)
        .map(item => ({
          sessionQuestionId: item.sessionQuestionId,
          studentAnswer: item.studentAnswer,
          selfCorrect: item.selfCorrect,
          durationSeconds: item.durationSeconds || 0
        }))
      if (!answerList.length) {
        this.$message.warning('请先填写至少一道题的答案')
        return
      }
      if ((this.session.questionList || []).some(item => !item.answered && item.studentAnswer && !this.isAutoJudge(item) && item.selfCorrect === null)) {
        this.$message.warning('请先完成所有已填写主观题的自评')
        return
      }
      this.batchSubmitting = true
      try {
        this.session = this.prepareSession(await batchSubmitPracticeAnswer({
          sessionId: this.session.sessionId,
          answerList
        }))
        this.$message.success(`已批量提交 ${answerList.length} 道题`)
        this.loadHistory()
        this.loadStatistics()
      } finally {
        this.batchSubmitting = false
      }
    },
    async finishSession() {
      const currentQuestionList = this.session.questionList || []
      currentQuestionList.forEach(item => this.pauseAnswerTiming(item))
      const unfinishedCount = (this.session.questionList || []).filter(item => !item.answered).length
      if (unfinishedCount > 0) {
        try {
          await this.$confirm(`还有 ${unfinishedCount} 道题未提交，结束后将不再允许作答。`, '确认结束练习', { type: 'warning' })
        } catch (error) {
          return
        }
      }
      this.session = this.prepareSession(await finishPracticeSession({ sessionId: this.session.sessionId }))
      this.$message.success(`练习完成，正确率 ${this.session.accuracyRate || 0}%`)
      this.loadHistory()
      this.loadStatistics()
    },
    prepareSession(session) {
      const next = Object.assign(emptySession(), session || {})
      next.questionList = (next.questionList || []).map(item => Object.assign({ studentAnswer: '', selfCorrect: null, submitting: false, revealing: false, dirty: false, answerStartedAt: null, resolvedPaperImages: [] }, item, {
        result: this.answerResultMap[item.sessionQuestionId] || null,
        referenceAnswer: this.referenceAnswerMap[item.sessionQuestionId] || null
      }))
      this.resolveSessionImages(next)
      return next
    },
    resolveSessionImages(session) {
      return Promise.all((session.questionList || []).map(async item => {
        const imageItems = Array.isArray(item.paperImageList) ? item.paperImageList : []
        const urls = await Promise.all(imageItems.map(async image => {
          const cacheKey = [image.uploadType || '', image.fileId || image.imageUrl || '', image.leftPosition, image.topPosition, image.width, image.height].join(':')
          if (!this.paperImageCache[cacheKey]) {
            this.paperImageCache[cacheKey] = (async () => {
              let url = image.imageUrl || ''
              if (!url && image.fileId && image.uploadType) {
                url = await downloadUrl({ fileId: image.fileId, uploadType: image.uploadType }).catch(() => '')
              }
              return url ? this.cropPaperImage(url, image) : ''
            })()
          }
          return this.paperImageCache[cacheKey]
        }))
        this.$set(item, 'resolvedPaperImages', urls.filter(Boolean))
      }))
    },
    cropPaperImage(url, image) {
      const left = Number(image.leftPosition)
      const top = Number(image.topPosition)
      const width = Number(image.width)
      const height = Number(image.height)
      const needsCrop = [left, top, width, height].every(Number.isFinite) && width > 0 && height > 0 &&
        (left > 0 || top > 0 || width < 10000 || height < 10000)
      if (!needsCrop) return Promise.resolve(url)
      return new Promise(resolve => {
        const source = new Image()
        source.crossOrigin = 'anonymous'
        source.onload = () => {
          try {
            const sx = Math.max(0, Math.round(source.naturalWidth * left / 10000))
            const sy = Math.max(0, Math.round(source.naturalHeight * top / 10000))
            const sw = Math.max(1, Math.min(source.naturalWidth - sx, Math.round(source.naturalWidth * width / 10000)))
            const sh = Math.max(1, Math.min(source.naturalHeight - sy, Math.round(source.naturalHeight * height / 10000)))
            const scale = Math.min(1, 1800 / Math.max(sw, sh))
            const canvas = document.createElement('canvas')
            canvas.width = Math.max(1, Math.round(sw * scale))
            canvas.height = Math.max(1, Math.round(sh * scale))
            canvas.getContext('2d').drawImage(source, sx, sy, sw, sh, 0, 0, canvas.width, canvas.height)
            resolve(canvas.toDataURL('image/png'))
          } catch (error) {
            resolve(url)
          }
        }
        source.onerror = () => resolve(url)
        source.src = url
      })
    },
    isAutoJudge(item) {
      if (item.autoJudge !== undefined && item.autoJudge !== null) return item.autoJudge
      return item.judgeMode === 'AUTO' || ['选择题', '判断题', '填空题', '单选题', '多选题'].includes(item.questionTypeName)
    },
    imageList(item) {
      if (Array.isArray(item.paperImageList)) return item.resolvedPaperImages || []
      return [item.imageUrl, item.imageUrl2, item.imageUrl3, item.imageUrl4]
        .filter(value => value && !/^\d+$/.test(String(value)))
    },
    imageModeLabel(imageMode) { return ({ ORIGINAL: '原图题块', GRAYSCALE: '灰度预览题块', TEXT_ONLY: '仅文字' })[imageMode] || '原图题块' },
    typeLabel(type) {
      return { KNOWLEDGE: '知识点', ERROR_LABEL: '错因', TYPICAL: '典型题' }[type] || type
    },
    sourceLabel(source) {
      return { WRONG_QUESTION: '错题本', QUESTION_BANK: '题库', MIXED: '混合' }[source] || '错题本'
    },
    trendText(days) {
      const trend = this.findTrend(days)
      return trend ? `${trend.accuracyRate || 0}%` : '-'
    },
    trendTip(days) {
      const trend = this.findTrend(days)
      if (!trend) return '暂无数据'
      const delta = Number(trend.accuracyRateDelta || 0)
      const deltaText = delta > 0 ? `+${delta}` : String(delta)
      return `${trend.answeredCount || 0}题 · ${deltaText}%`
    },
    findTrend(days) {
      return (this.statistics.trendList || []).find(item => Number(item.periodDays) === days)
    },
    resultLevel(accuracyRate) {
      const rate = Number(accuracyRate || 0)
      if (rate >= 90) return '掌握稳定'
      if (rate >= 70) return '继续巩固'
      return '需要复习'
    }
  }
}
</script>

<style scoped>
.practice-page { padding: 4px; color: #27314a; }
.practice-hero { display:flex; align-items:center; justify-content:space-between; min-height:132px; padding:26px 34px; margin-bottom:14px; color:#fff; border-radius:12px; background:#4f63df; }
.practice-hero h1 { margin:0 0 8px; font-size:28px; }
.practice-hero p { margin:0; opacity:.86; }
.content-card { border:0; border-radius:12px; }
.stats-grid { display:grid; grid-template-columns:repeat(4, minmax(0, 1fr)); gap:12px; margin-bottom:14px; }
.stat-item { min-height:88px; padding:16px; border:1px solid #edf0f5; border-radius:8px; background:#fff; }
.stat-item span { display:block; color:#7b8499; font-size:13px; }
.stat-item strong { display:block; margin:8px 0 4px; color:#27314a; font-size:24px; line-height:1; }
.stat-item small { color:#8b94a8; }
.filter-bar { display:flex; flex-wrap:wrap; gap:10px; margin-bottom:18px; }
.filter-bar .el-input { width:240px; }
.filter-bar .el-select { width:140px; }.filter-bar .paper-code-input { width:190px; }
.el-pagination { margin-top:18px; text-align:right; }
.session-summary { display:flex; gap:18px; padding:12px 16px; margin-bottom:14px; border-radius:8px; background:#f5f7ff; color:#5363d6; }
.practice-preview { padding:12px 14px; margin:0 0 12px; border:1px solid #dce5ff; border-radius:8px; background:#f5f7ff; color:#5363d6; line-height:1.8; }
.practice-preview strong { margin-right:12px; }
.practice-preview span { margin-right:12px; }
.practice-preview p { margin:2px 0 0; color:#7b8499; }
.practice-preview.is-shortage { border-color:#ffd1d1; background:#fff7f7; color:#d9534f; }
.paper-basket { max-height:310px; margin-bottom:12px; overflow:auto; border:1px solid #e4e8f0; border-radius:8px; }
.paper-basket-head { position:sticky; top:0; z-index:1; display:flex; justify-content:space-between; padding:11px 14px; background:#f7f8fc; color:#59647a; }
.paper-basket-head span { color:#8b94a8; font-size:12px; }
.paper-basket-item { display:grid; grid-template-columns:28px minmax(0, 1fr) auto; align-items:center; gap:10px; padding:10px 14px; border-top:1px solid #edf0f5; }
.paper-basket-index { display:inline-flex; align-items:center; justify-content:center; width:24px; height:24px; color:#fff; border-radius:50%; background:#5363d6; font-size:12px; }
.paper-basket-item b { display:block; color:#27314a; }
.paper-basket-item p { margin:4px 0 0; color:#8b94a8; font-size:12px; }
.paper-basket-actions { white-space:nowrap; }
.paper-basket-actions .danger-link { color:#f56c6c; }
.form-tip { margin:4px 0 0; color:#8b94a8; font-size:12px; line-height:1.5; }
.generation-reason { margin-bottom:14px; }
.source-reason { margin:8px 0 0; color:#7b8499; font-size:13px; }
.judge-tip { margin:12px 0 8px; }
.question-options { margin:10px 0; padding-left:24px; line-height:1.9; }
.auto-judge-text { color:#7b8499; font-size:13px; }
.reference-answer { padding:10px 12px; margin-top:10px; border-radius:8px; background:#f8fafc; color:#53606f; line-height:1.7; }
.reference-answer p { margin:0 0 4px; }
.result-panel { display:grid; grid-template-columns:repeat(3, 1fr); gap:10px; margin-bottom:14px; }
.result-panel div { padding:12px; border-radius:8px; background:#f8fafc; }
.result-panel span { display:block; color:#7b8499; font-size:13px; }
.result-panel strong { display:block; margin-top:6px; color:#27314a; }
.wrong-review { padding:12px; margin-bottom:10px; border-radius:8px; background:#fff7f7; }
.wrong-review h4 { margin:0 0 10px; }
.wrong-review .el-tag { margin:0 8px 8px 0; }
.question-card { padding:18px 0; border-bottom:1px solid #edf0f5; }
.question-card:last-child { border-bottom:0; }
.question-head { display:flex; justify-content:space-between; }
.question-card h3 { margin:12px 0; }
.question-content { padding:14px; margin-bottom:12px; border-radius:8px; background:#f7f8fb; white-space:pre-wrap; line-height:1.7; }
.question-images { display:grid; grid-template-columns:repeat(2, 1fr); gap:10px; margin-bottom:12px; }
.question-images .el-image { height:180px; background:#f5f6f8; }
.answer-actions { display:flex; justify-content:space-between; align-items:center; margin:12px 0; }
.answer-review { padding:12px 14px; margin-top:12px; border-radius:8px; background:#f8fafc; line-height:1.7; }
.answer-review p { margin:0 0 6px; }
.paper-fill-summary { display:flex; flex-wrap:wrap; gap:14px; padding:10px 12px; margin-bottom:12px; border-radius:8px; background:#f5f7ff; color:#5363d6; font-size:13px; }
.paper-fill-form { padding:16px 0 2px; }
.paper-fill-file { display:inline-flex; align-items:center; margin-left:10px; color:#606266; font-size:13px; }
.paper-fill-file .el-button { margin-left:4px; }
.paper-fill-question { padding:16px 0; border-bottom:1px solid #edf0f5; }
.paper-fill-question:last-child { border-bottom:0; }
.paper-fill-question .question-head { align-items:center; justify-content:flex-start; gap:8px; }
.paper-fill-question .question-head small { color:#8b94a8; }
.paper-fill-question .question-content { margin-top:10px; }
.paper-fill-previous { padding:8px 10px; margin:8px 0; border-radius:6px; background:#f8fafc; color:#7b8499; font-size:12px; line-height:1.5; }
.paper-fill-answer-row { margin:8px 0 0; }
.paper-fill-answer-row .el-form-item { margin-bottom:8px; }
.paper-fill-error-reason { margin-top:8px; }
@media (max-width: 900px) {
  .stats-grid { grid-template-columns:repeat(2, minmax(0, 1fr)); }
  .result-panel { grid-template-columns:1fr; }
}
</style>
