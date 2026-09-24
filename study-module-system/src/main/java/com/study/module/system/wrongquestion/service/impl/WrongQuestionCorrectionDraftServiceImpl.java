package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionAnswerLayerRevealReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionCorrectionDraftSaveReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAnswerLayerResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionDraftResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTimelineResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionDraft;
import com.study.module.system.wrongquestion.mapper.WrongQuestionCorrectionDraftMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionDraftService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** 订正草稿与分层查看服务实现。 */
@Service
public class WrongQuestionCorrectionDraftServiceImpl
        extends ServiceImpl<WrongQuestionCorrectionDraftMapper, WrongQuestionCorrectionDraft>
        implements WrongQuestionCorrectionDraftService {

    @Autowired
    WrongQuestionService wrongQuestionService;
    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Override
    public WrongQuestionCorrectionDraftResp correctionDraftDetail(Long wrongQuestionId) {
        wrongQuestionService.checkWrongQuestion(wrongQuestionId);
        WrongQuestionCorrectionDraft draft = lambdaQuery()
                .eq(WrongQuestionCorrectionDraft::getWrongQuestionId, wrongQuestionId)
                .eq(WrongQuestionCorrectionDraft::getUserId, AccountUtils.getUserId()).one();
        if (draft == null) {
            return null;
        }
        WrongQuestionCorrectionDraftResp response = new WrongQuestionCorrectionDraftResp();
        BeanUtils.copyProperties(draft, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCorrectionDraft(WrongQuestionCorrectionDraftSaveReq request) {
        wrongQuestionService.checkWrongQuestion(request.getWrongQuestionId());
        Long userId = AccountUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();
        boolean updated = lambdaUpdate()
                .eq(WrongQuestionCorrectionDraft::getWrongQuestionId, request.getWrongQuestionId())
                .eq(WrongQuestionCorrectionDraft::getUserId, userId)
                .set(WrongQuestionCorrectionDraft::getThinking, request.getThinking())
                .set(WrongQuestionCorrectionDraft::getErrorReason, request.getErrorReason())
                .set(WrongQuestionCorrectionDraft::getCorrectionAnswer, request.getCorrectionAnswer())
                .set(WrongQuestionCorrectionDraft::getCorrectionAnalysis, request.getCorrectionAnalysis())
                .set(WrongQuestionCorrectionDraft::getCorrectionImageUrl, request.getCorrectionImageUrl())
                .set(WrongQuestionCorrectionDraft::getCorrectionRemark, request.getCorrectionRemark())
                .set(WrongQuestionCorrectionDraft::getUpdateTime, now).update();
        if (!updated) {
            WrongQuestionCorrectionDraft draft = new WrongQuestionCorrectionDraft();
            BeanUtils.copyProperties(request, draft);
            draft.setUserId(userId);
            draft.setCreateTime(now);
            draft.setUpdateTime(now);
            try {
                save(draft);
            } catch (DuplicateKeyException duplicate) {
                // 两个自动保存请求同时首次写入时，由唯一键选出一条，再覆盖为最新内容。
                lambdaUpdate()
                        .eq(WrongQuestionCorrectionDraft::getWrongQuestionId, request.getWrongQuestionId())
                        .eq(WrongQuestionCorrectionDraft::getUserId, userId)
                        .set(WrongQuestionCorrectionDraft::getThinking, request.getThinking())
                        .set(WrongQuestionCorrectionDraft::getErrorReason, request.getErrorReason())
                        .set(WrongQuestionCorrectionDraft::getCorrectionAnswer, request.getCorrectionAnswer())
                        .set(WrongQuestionCorrectionDraft::getCorrectionAnalysis, request.getCorrectionAnalysis())
                        .set(WrongQuestionCorrectionDraft::getCorrectionImageUrl, request.getCorrectionImageUrl())
                        .set(WrongQuestionCorrectionDraft::getCorrectionRemark, request.getCorrectionRemark())
                        .set(WrongQuestionCorrectionDraft::getUpdateTime, now).update();
            }
        }
    }

    @Override
    public void deleteCorrectionDraft(Long wrongQuestionId, Long userId) {
        lambdaUpdate().eq(WrongQuestionCorrectionDraft::getWrongQuestionId, wrongQuestionId)
                .eq(WrongQuestionCorrectionDraft::getUserId, userId).remove();
    }

    @Override
    public WrongQuestionAnswerLayerResp revealAnswerLayer(WrongQuestionAnswerLayerRevealReq request) {
        WrongQuestion question = wrongQuestionService.checkWrongQuestion(request.getWrongQuestionId());
        enforceLayerOrder(question.getId(), request.getLayer());
        WrongQuestionAnswerLayerResp response = new WrongQuestionAnswerLayerResp();
        response.setLayer(request.getLayer());
        String eventType;
        switch (request.getLayer()) {
            case "KEY_HINT":
                response.setTitle("关键提示");
                response.setContent(StringUtils.hasText(question.getKeyHint()) ? question.getKeyHint()
                        : StringUtils.hasText(question.getLearningPoint())
                        ? "回到知识点“" + question.getLearningPoint() + "”，先检查条件与目标之间的关系。"
                        : "重新圈出已知条件和所求目标，先确定适用的定义或公式。");
                eventType = "HINT_REVEALED";
                break;
            case "STEPS":
                response.setTitle("解题步骤");
                response.setContent(StringUtils.hasText(question.getSolutionSteps())
                        ? question.getSolutionSteps() : StringUtils.hasText(question.getAnalysis())
                        ? question.getAnalysis() : "当前未录入分步解析，请先写出公式、代入条件并检查结果。");
                eventType = "STEPS_REVEALED";
                break;
            case "COMMON_MISTAKE":
                response.setTitle("易错点");
                response.setContent(commonMistakeContent(question));
                eventType = "COMMON_MISTAKE_REVEALED";
                break;
            case "ANALYSIS":
                response.setTitle("完整解析");
                response.setContent(StringUtils.hasText(question.getAnalysis()) ? question.getAnalysis() : "未录入解析");
                eventType = "ANALYSIS_REVEALED";
                break;
            default:
                response.setTitle("参考答案");
                response.setContent(StringUtils.hasText(question.getCorrectAnswer()) ? question.getCorrectAnswer() : "未录入答案");
                eventType = "ANSWER_REVEALED";
        }
        wrongQuestionTimelineService.record(question.getId(), eventType, "STUDENT",
                "已查看" + response.getTitle(), AccountUtils.getUserId());
        return response;
    }

    /**
     * 老错题还没有单独的易错点字段时，使用已有错因和标签给出可追溯的兼容说明。
     */
    private String commonMistakeContent(WrongQuestion question) {
        if (StringUtils.hasText(question.getCommonMistake())) {
            return question.getCommonMistake();
        }
        if (StringUtils.hasText(question.getWrongReason())) {
            return "本题历史错因：" + question.getWrongReason();
        }
        if (StringUtils.hasText(question.getErrorLabels())) {
            return "做题时重点检查：" + question.getErrorLabels();
        }
        return "注意逐一核对题目条件、公式适用范围、单位和最终结果；完成后再反向代入检查。";
    }

    private void enforceLayerOrder(Long wrongQuestionId, String layer) {
        List<String> layers = Arrays.asList("KEY_HINT", "STEPS", "COMMON_MISTAKE", "ANALYSIS", "ANSWER");
        int index = layers.indexOf(layer);
        if (index <= 0) {
            return;
        }
        String requiredEvent = Arrays.asList("HINT_REVEALED", "STEPS_REVEALED",
                "COMMON_MISTAKE_REVEALED", "ANALYSIS_REVEALED").get(index - 1);
        boolean previousLayerRevealed = wrongQuestionTimelineService.timelineList(wrongQuestionId)
                .stream().map(WrongQuestionTimelineResp::getEventType)
                .anyMatch(requiredEvent::equals);
        if (!previousLayerRevealed) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_ANSWER_LAYER_ORDER_INVALID);
        }
    }
}
