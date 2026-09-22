package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.questionbank.constants.QuestionBankDictType;
import com.study.common.core.enums.ErrorCodeConstants;
import java.util.List;

/**
 * 题库题目公共服务实现
 */
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 执行 fillDictNames 业务处理。
     */
    @Override
    public void fillDictNames(QuestionBank questionBank) {
        questionBank.setGradeName(checkDictData(QuestionBankDictType.GRADE, questionBank.getGrade()).getDictLabel());
        questionBank.setSubjectName(checkDictData(QuestionBankDictType.SUBJECT, questionBank.getSubject()).getDictLabel());
        questionBank.setQuestionTypeName(checkDictData(QuestionBankDictType.QUESTION_TYPE,
                questionBank.getQuestionType()).getDictLabel());
        questionBank.setSourceName(checkDictData(QuestionBankDictType.SOURCE, questionBank.getSource()).getDictLabel());
    }

    /**
     * 校验业务数据。
     */
    private DictData checkDictData(String dictType, String dictValue) {
        return dictDataService.checkDictData(dictType, dictValue, ErrorCodeConstants.INVALID_DICT_DATA_IDS);
    }

    /**
     * 查询知识点匹配的推荐候选题
     */
    @Override
    public List<QuestionBank> selectKnowledgeCandidates(Long userId, String grade, String subject, String questionType,
                                                        Integer difficulty, List<Long> pointIds, Integer limit) {
        return baseMapper.selectKnowledgeCandidates(userId, grade, subject, questionType, difficulty, pointIds, limit);
    }

    /**
     * 查询文本相似的推荐候选题
     */
    @Override
    public List<QuestionBank> selectSimilarCandidates(Long userId, String grade, String subject, String questionType,
                                                      Integer difficulty, String keyword, Integer limit) {
        return baseMapper.selectSimilarCandidates(userId, grade, subject, questionType, difficulty, keyword, limit);
    }

    /**
     * 查询降级推荐候选题
     */
    @Override
    public List<QuestionBank> selectFallbackCandidates(Long userId, String grade, String subject, String questionType,
                                                       Integer difficulty, boolean strictType, boolean excludeRecent,
                                                       Integer limit) {
        return baseMapper.selectFallbackCandidates(userId, grade, subject, questionType, difficulty, strictType,
                excludeRecent, limit);
    }
}
