package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.service.BookService;
import com.study.module.system.review.dto.request.UpdateLearningProfileReq;
import com.study.module.system.review.dto.response.LearningProfileResp;
import com.study.module.system.review.entity.LearningProfile;
import com.study.module.system.review.mapper.LearningProfileMapper;
import com.study.module.system.review.service.LearningProfileService;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import com.study.module.system.dict.service.DictDataService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 学习偏好服务实现。
 */
@Service
public class LearningProfileServiceImpl extends ServiceImpl<LearningProfileMapper, LearningProfile>
        implements LearningProfileService {

    @Autowired
    private BookService bookService;

    @Autowired
    private DictDataService dictDataService;

    @Override
    public LearningProfileResp learningProfile() {
        LearningProfile profile = getCurrentProfile(AccountUtils.getUserId());
        LearningProfileResp response = new LearningProfileResp();
        if (profile != null) {
            BeanUtils.copyProperties(profile, response);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLearningProfile(UpdateLearningProfileReq request) {
        Long userId = AccountUtils.getUserId();
        String grade = trimToNull(request.getGrade());
        String subject = trimToNull(request.getSubject());
        Book book = request.getBookId() == null ? null : bookService.checkBook(request.getBookId());
        if (book != null) {
            validateBookScope(book, grade, subject);
            grade = book.getGrade();
            subject = book.getSubject();
        } else if (StringUtils.hasText(grade) || StringUtils.hasText(subject)) {
            if (!StringUtils.hasText(grade) || !StringUtils.hasText(subject)) {
                throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
            }
            dictDataService.checkDictData(WrongQuestionDictType.GRADE, grade,
                    ErrorCodeConstants.INVALID_DICT_DATA_IDS);
            dictDataService.checkDictData(WrongQuestionDictType.SUBJECT, subject,
                    ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        LearningProfile profile = getCurrentProfile(userId);
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new LearningProfile();
            profile.setUserId(userId);
            profile.setCreateTime(now);
        }
        profile.setGrade(grade);
        profile.setSubject(subject);
        profile.setBookId(book == null ? null : book.getId());
        profile.setBookTitle(book == null ? null : book.getTitle());
        profile.setUpdateTime(now);
        saveOrUpdate(profile);
    }

    private LearningProfile getCurrentProfile(Long userId) {
        return lambdaQuery().eq(LearningProfile::getUserId, userId).one();
    }

    private void validateBookScope(Book book, String grade, String subject) {
        if ((StringUtils.hasText(grade) && !grade.equals(book.getGrade()))
                || (StringUtils.hasText(subject) && !subject.equals(book.getSubject()))) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
