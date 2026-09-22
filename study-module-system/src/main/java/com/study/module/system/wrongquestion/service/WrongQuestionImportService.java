package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.ImportWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.response.ImportWrongQuestionImageResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 初中生错题导入服务
 */
public interface WrongQuestionImportService extends IService<WrongQuestion> {

    /**
     * A4图片识别导入错题
     */
    ImportWrongQuestionImageResp importWrongQuestionImage(ImportWrongQuestionImageReq request);
}
