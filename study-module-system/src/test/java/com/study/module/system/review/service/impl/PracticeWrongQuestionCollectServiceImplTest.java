package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

/**
 * 练习错答沉淀测试。
 */
@ExtendWith(MockitoExtension.class)
class PracticeWrongQuestionCollectServiceImplTest {

    @Mock
    private WrongQuestionService wrongQuestionService;
    @Mock
    private ReviewEnrollmentService reviewEnrollmentService;
    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<WrongQuestion> wrongQuestionQuery;
    @InjectMocks
    private PracticeWrongQuestionCollectServiceImpl practiceWrongQuestionCollectService;

    /**
     * 已存在的题库错题应更新为最近一次错误答案；归档错题还需要重新进入复习计划。
     */
    @Test
    void shouldRefreshExistingWrongAnswerAndReactivateArchivedQuestion() {
        WrongQuestion existing = new WrongQuestion();
        existing.setId(12L);
        existing.setStatus(WrongQuestionStatus.ARCHIVED);
        when(wrongQuestionService.lambdaQuery()).thenReturn(wrongQuestionQuery);
        doReturn(existing).when(wrongQuestionQuery).one();

        Long wrongQuestionId = practiceWrongQuestionCollectService.collectBankQuestionWrong(
                questionBank(), 8L, "x = 3");

        assertEquals(12L, wrongQuestionId);
        assertEquals("x = 3", existing.getWrongAnswer());
        assertEquals(WrongQuestionStatus.CORRECTED, existing.getStatus());
        verify(wrongQuestionService).updateById(existing);
        verify(reviewEnrollmentService).syncWrongQuestionReview(existing);
    }

    private QuestionBank questionBank() {
        QuestionBank question = new QuestionBank();
        question.setQuestionTitle("一次函数");
        question.setQuestionContent("求解析式");
        question.setCorrectAnswer("y=x+1");
        return question;
    }
}
