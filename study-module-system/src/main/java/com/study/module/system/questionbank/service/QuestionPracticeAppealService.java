package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealCreateReq;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealReviewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAppealListResp;
import com.study.module.system.questionbank.entity.QuestionPracticeAppeal;
import java.util.List;

/**
 * 主观题作答申诉服务
 */
public interface QuestionPracticeAppealService extends IService<QuestionPracticeAppeal> {
    void createQuestionPracticeAppeal(QuestionPracticeAppealCreateReq request);
    List<QuestionPracticeAppealListResp> questionPracticeAppealList(Integer status);
    void reviewQuestionPracticeAppeal(QuestionPracticeAppealReviewReq request);
}
