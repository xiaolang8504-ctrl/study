package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionAnswerLayerRevealReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionCorrectionDraftSaveReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAnswerLayerResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionDraftResp;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionDraft;

/** 订正草稿与分层查看服务。 */
public interface WrongQuestionCorrectionDraftService extends IService<WrongQuestionCorrectionDraft> {
    WrongQuestionCorrectionDraftResp correctionDraftDetail(Long wrongQuestionId);
    void saveCorrectionDraft(WrongQuestionCorrectionDraftSaveReq request);
    void deleteCorrectionDraft(Long wrongQuestionId, Long userId);
    WrongQuestionAnswerLayerResp revealAnswerLayer(WrongQuestionAnswerLayerRevealReq request);
}
