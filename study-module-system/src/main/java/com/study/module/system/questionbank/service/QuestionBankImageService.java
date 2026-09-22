package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionBankImageSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankImageListResp;
import com.study.module.system.questionbank.entity.QuestionBankImage;

import java.util.List;

public interface QuestionBankImageService extends IService<QuestionBankImage> {
    /**
     * 重写题目的图片关联
     */
    void rewrite(Long questionId, List<QuestionBankImageSaveReq> images, String legacyImageUrls);

    /**
     * 查询题目图片列表
     */
    List<QuestionBankImageListResp> imageList(Long questionId);
}
