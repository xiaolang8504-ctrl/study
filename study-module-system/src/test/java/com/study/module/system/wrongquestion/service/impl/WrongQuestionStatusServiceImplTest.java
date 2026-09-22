package com.study.module.system.wrongquestion.service.impl;

import com.study.common.core.exception.LogicException;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 错题状态流转规则测试。
 */
class WrongQuestionStatusServiceImplTest {

    private final WrongQuestionStatusServiceImpl service = new WrongQuestionStatusServiceImpl();

    /**
     * 已订正题目可以重新打开订正，已掌握题目可以归档。
     */
    @Test
    void shouldAllowOnlyReopenCorrectionAndArchiveMasteredQuestion() {
        assertDoesNotThrow(() -> checkStatusFlow(WrongQuestionStatus.CORRECTED,
                WrongQuestionStatus.PENDING_CORRECTION));
        assertDoesNotThrow(() -> checkStatusFlow(WrongQuestionStatus.MASTERED,
                WrongQuestionStatus.ARCHIVED));
    }

    /**
     * 订正和掌握必须分别通过订正记录和复习反馈产生，不能由手动状态接口伪造。
     */
    @Test
    void shouldRejectManualCorrectionAndMasteredTransitions() {
        assertThrows(LogicException.class, () -> checkStatusFlow(WrongQuestionStatus.PENDING_CORRECTION,
                WrongQuestionStatus.CORRECTED));
        assertThrows(LogicException.class, () -> checkStatusFlow(WrongQuestionStatus.CORRECTED,
                WrongQuestionStatus.MASTERED));
        assertThrows(LogicException.class, () -> checkStatusFlow(WrongQuestionStatus.PENDING_CORRECTION,
                WrongQuestionStatus.ARCHIVED));
    }

    private void checkStatusFlow(Integer currentStatus, Integer targetStatus) {
        ReflectionTestUtils.invokeMethod(service, "checkStatusFlow", currentStatus, targetStatus);
    }
}
