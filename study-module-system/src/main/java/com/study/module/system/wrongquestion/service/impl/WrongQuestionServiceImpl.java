package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.user.service.UserService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 初中生错题归档服务
 */
@Service
public class WrongQuestionServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionService {

    @Autowired
    DictDataService dictDataService;

    @Autowired
    UserService userService;

    /**
     * 校验错题
     */
    @Override
    public WrongQuestion checkWrongQuestion(Long id) {
        WrongQuestion wrongQuestion = this.lambdaQuery()
                .eq(WrongQuestion::getId, id)
                .eq(WrongQuestion::getCreateId, userService.getUserIdByToken())
                .one();
        if (wrongQuestion == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_NOT_EXIST);
        }
        return wrongQuestion;
    }

    /**
     * 填充字典名称
     */
    @Override
    public void fillDictNames(WrongQuestion wrongQuestion) {
        DictData grade = checkDictData(WrongQuestionDictType.GRADE, wrongQuestion.getGrade());
        DictData subject = checkDictData(WrongQuestionDictType.SUBJECT, wrongQuestion.getSubject());
        DictData questionType = checkDictData(WrongQuestionDictType.QUESTION_TYPE, wrongQuestion.getQuestionType());
        DictData source = checkDictData(WrongQuestionDictType.SOURCE, wrongQuestion.getSource());
        wrongQuestion.setGradeName(grade.getDictLabel());
        wrongQuestion.setSubjectName(subject.getDictLabel());
        wrongQuestion.setQuestionTypeName(questionType.getDictLabel());
        wrongQuestion.setSourceName(source.getDictLabel());
    }

    /**
     * 校验字典数据
     */
    private DictData checkDictData(String dictType, String dictValue) {
        return dictDataService.checkDictData(dictType, dictValue, ErrorCodeConstants.INVALID_DICT_DATA_IDS);
    }
}
