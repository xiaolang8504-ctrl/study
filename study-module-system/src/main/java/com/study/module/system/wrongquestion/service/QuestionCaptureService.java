package com.study.module.system.wrongquestion.service;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureConfirmReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureDuplicateCheckReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionBatchUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionIdsReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSplitReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSnapshotReq;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureDuplicateResp;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskPageListReq;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskPageListResp;
import com.study.common.core.domain.dto.PageResult;
import java.util.List;
/**
 * 题目采集任务服务
 */
public interface QuestionCaptureService {
    /**
     * 创建题目采集任务。
     */
    Long createQuestionCaptureTask(QuestionCaptureTaskCreateReq request);

    /**
     * 为指定用户创建题目采集任务。
     */
    Long createQuestionCaptureTaskByUserId(QuestionCaptureTaskCreateReq request, Long userId);

    /**
     * 分页查询题目采集任务。
     */
    PageResult<QuestionCaptureTaskPageListResp> questionCaptureTaskPageList(QuestionCaptureTaskPageListReq request);

    /**
     * 查询题目采集任务详情。
     */
    QuestionCaptureTaskResp questionCaptureTaskDetail(Long id);

    /**
     * 重新识别采集任务中的全部页面。
     */
    void retryQuestionCaptureTask(Long id);

    /**
     * 更新题块内容与位置。
     */
    void updateQuestionCaptureRegion(QuestionCaptureRegionUpdateReq request);

    /**
     * 批量设置待确认题块的归类信息。
     */
    void batchUpdateQuestionCaptureRegion(QuestionCaptureRegionBatchUpdateReq request);

    /**
     * 人工补充遗漏题块。
     */
    void createQuestionCaptureRegion(QuestionCaptureRegionCreateReq request);

    /**
     * 合并多个题块。
     */
    void mergeQuestionCaptureRegion(QuestionCaptureRegionIdsReq request);

    /**
     * 拆分指定题块。
     */
    void splitQuestionCaptureRegion(Long id);

    /**
     * 按用户指定比例切分题块。
     */
    void splitQuestionCaptureRegion(QuestionCaptureRegionSplitReq request);

    /**
     * 恢复采集工作台中的未确认题块快照。
     */
    void restoreQuestionCaptureRegionSnapshot(QuestionCaptureRegionSnapshotReq request);

    /**
     * 跳过指定题块。
     */
    void deleteQuestionCaptureRegion(Long id);

    /**
     * 恢复已跳过的题块。
     */
    void restoreQuestionCaptureRegion(Long id);

    /**
     * 重新识别指定采集页面。
     */
    void retryQuestionCapturePage(Long id);

    /**
     * OCR 无法使用时，将一页原图降级为可人工确认的整页题块。
     */
    void saveQuestionCapturePageAsImage(Long id);

    /** 请求对当前学生的一页试卷生成可回退的去笔迹图片。 */
    void generateQuestionCaptureCleanImage(Long id);

    /** 放弃当前页的去笔迹版本，确认与裁剪恢复使用原图。 */
    void revertQuestionCaptureCleanImage(Long id);

    /** 使用当前学生上传的人工遮罩整页图替换当前清理版本，原图保持可回退。 */
    void applyQuestionCaptureManualCleanImage(Long id, Long cleanedFileId);

    /**
     * 确认前检查当前学生是否已有题干完全相同的错题。
     */
    List<QuestionCaptureDuplicateResp> questionCaptureDuplicateList(QuestionCaptureDuplicateCheckReq request);

    /**
     * 确认题块并创建错题。
     */
    Integer confirmQuestionCapture(QuestionCaptureConfirmReq request);
}
