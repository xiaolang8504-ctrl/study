package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.entity.QuestionBank;

import java.util.List;

public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 校验字典键值并填充标准名称。
     */
    void fillDictNames(QuestionBank questionBank);

    /**
     * 查询知识点匹配的推荐候选题
     */
    List<QuestionBank> selectKnowledgeCandidates(Long userId, String grade, String subject, String questionType,
                                                 Integer difficulty, List<Long> pointIds, Integer limit);

    /**
     * 查询同知识点的跨情境候选题，优先返回与原题题型不同的题目。
     */
    List<QuestionBank> selectKnowledgeContextCandidates(Long userId, String grade, String subject,
                                                        String sourceQuestionType, Integer difficulty,
                                                        List<Long> pointIds, Integer limit);

    /**
     * 查询文本相似的推荐候选题
     */
    List<QuestionBank> selectSimilarCandidates(Long userId, String grade, String subject, String questionType,
                                               Integer difficulty, String keyword, Integer limit);

    /**
     * 查询降级推荐候选题
     */
    List<QuestionBank> selectFallbackCandidates(Long userId, String grade, String subject, String questionType,
                                                Integer difficulty, boolean strictType, boolean excludeRecent,
                                                Integer limit);
}
