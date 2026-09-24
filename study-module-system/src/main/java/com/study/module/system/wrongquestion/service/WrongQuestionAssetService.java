package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAssetResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionAsset;

import java.util.List;

/** 错题素材服务。 */
public interface WrongQuestionAssetService extends IService<WrongQuestionAsset> {
    void rewriteAssets(WrongQuestion wrongQuestion);
    List<WrongQuestionAssetResp> wrongQuestionAssetList(Long wrongQuestionId);
}
