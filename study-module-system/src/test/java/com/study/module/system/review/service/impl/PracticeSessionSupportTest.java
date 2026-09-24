package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.response.PracticeQuestionImageResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 练习卷题面模式与采集图片选择测试。 */
class PracticeSessionSupportTest {

    @Test
    void shouldSelectGrayscaleCapturePageAndKeepRegionCrop() {
        PracticeSession session = session("GRAYSCALE");
        PracticeSessionQuestion sessionQuestion = new PracticeSessionQuestion();
        sessionQuestion.setId(101L);
        sessionQuestion.setWrongQuestionId(1L);
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setId(1L);
        wrongQuestion.setCapturePageId(10L);
        wrongQuestion.setCaptureLeftPosition(1000);
        wrongQuestion.setCaptureTopPosition(2000);
        wrongQuestion.setCaptureWidth(5000);
        wrongQuestion.setCaptureHeight(3000);
        QuestionCapturePage page = new QuestionCapturePage();
        page.setId(10L);
        page.setImageFileId(7L);
        page.setCleanedFileId(8L);
        page.setCleanStatus(2);

        PracticeSessionDetailResp response = PracticeSessionSupport.buildDetail(session,
                Collections.singletonList(sessionQuestion), Collections.singletonList(wrongQuestion),
                Collections.emptyList(), Collections.singletonList(page), Collections.emptyList(), false);

        PracticeQuestionImageResp image = response.getQuestionList().get(0).getPaperImageList().get(0);
        assertEquals("GRAYSCALE", response.getImageMode());
        assertEquals(8, image.getFileId());
        assertEquals("GRAYSCALE", image.getImageType());
        assertEquals(1000, image.getLeftPosition());
        assertEquals(3000, image.getHeight());
    }

    @Test
    void shouldHideAllImagesInTextOnlyMode() {
        PracticeSessionQuestion sessionQuestion = new PracticeSessionQuestion();
        sessionQuestion.setWrongQuestionId(1L);
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setId(1L);
        wrongQuestion.setImageUrl("7");

        PracticeSessionDetailResp response = PracticeSessionSupport.buildDetail(session("TEXT_ONLY"),
                Collections.singletonList(sessionQuestion), Collections.singletonList(wrongQuestion));

        assertTrue(response.getQuestionList().get(0).getPaperImageList().isEmpty());
    }

    @Test
    void shouldPreferVersionedQuestionAndAssetSnapshotsOverChangedSource() {
        PracticeSession session = session("ORIGINAL");
        session.setPaperVersion(3);
        session.setColumnCount(2);
        PracticeSessionQuestion sessionQuestion = new PracticeSessionQuestion();
        sessionQuestion.setId(101L);
        sessionQuestion.setWrongQuestionId(1L);
        sessionQuestion.setQuestionTitleSnapshot("snapshot title");
        sessionQuestion.setQuestionContentSnapshot("snapshot content");
        sessionQuestion.setContentFormatSnapshot("LATEX");
        sessionQuestion.setOptionsJsonSnapshot("{\"A\":\"snapshot option\"}");
        sessionQuestion.setCorrectAnswerSnapshot("snapshot answer");
        sessionQuestion.setAnalysisSnapshot("snapshot analysis");
        sessionQuestion.setAssetSnapshotJson("[{\"fileId\":9,\"uploadType\":\"wrongQuestion\",\"imageType\":\"ORIGINAL\"}]");
        WrongQuestion changedSource = new WrongQuestion();
        changedSource.setId(1L);
        changedSource.setQuestionTitle("changed title");
        changedSource.setQuestionContent("changed content");
        changedSource.setCorrectAnswer("changed answer");

        PracticeSessionDetailResp response = PracticeSessionSupport.buildDetail(session,
                Collections.singletonList(sessionQuestion), Collections.singletonList(changedSource),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true);

        assertEquals(3, response.getPaperVersion());
        assertEquals(2, response.getColumnCount());
        assertEquals("snapshot title", response.getQuestionList().get(0).getQuestionTitle());
        assertEquals("snapshot content", response.getQuestionList().get(0).getQuestionContent());
        assertEquals("snapshot answer", response.getQuestionList().get(0).getCorrectAnswer());
        assertEquals(9, response.getQuestionList().get(0).getPaperImageList().get(0).getFileId());
    }

    private PracticeSession session(String imageMode) {
        PracticeSession session = new PracticeSession();
        session.setId(1L);
        session.setImageMode(imageMode);
        session.setQuestionCount(1);
        return session;
    }
}
