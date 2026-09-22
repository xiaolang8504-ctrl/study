package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionCorrectionRecordReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionRecordResp;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionRecord;

import java.util.List;

/**
 * 错题订正记录服务
 */
public interface WrongQuestionCorrectionRecordService extends IService<WrongQuestionCorrectionRecord> {

    /**
     * 提交错题订正记录
     */
    void submitCorrectionRecord(WrongQuestionCorrectionRecordReq request);

    /**
     * 错题订正记录列表
     */
    List<WrongQuestionCorrectionRecordResp> correctionRecordList(Long wrongQuestionId);

    /**
     * 最新错题订正记录
     */
    WrongQuestionCorrectionRecordResp latestCorrectionRecord(Long wrongQuestionId);
}
