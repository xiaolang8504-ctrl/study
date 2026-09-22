package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 服务端导出文档基本格式校验。 */
class PracticePaperDocumentServiceImplTest {
    private final PracticePaperDocumentServiceImpl service = new PracticePaperDocumentServiceImpl();
    @Test void shouldRenderStandardPdfAndDocx() {
        PracticeQuestionResp question = new PracticeQuestionResp(); question.setQuestionTitle("Addition"); question.setQuestionContent("1 + 1 = ?"); question.setCorrectAnswer("2");
        PracticeSessionDetailResp paper = new PracticeSessionDetailResp(); paper.setTitle("Math practice"); paper.setQuestionCount(1); paper.setQuestionList(Collections.singletonList(question));
        byte[] pdf = service.renderPdf(paper, true); byte[] docx = service.renderDocx(paper, true);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
        assertTrue(docx.length > 4 && docx[0] == 'P' && docx[1] == 'K');
    }
}
