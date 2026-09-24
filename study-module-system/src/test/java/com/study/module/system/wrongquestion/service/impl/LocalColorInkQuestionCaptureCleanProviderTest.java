package com.study.module.system.wrongquestion.service.impl;

import com.study.module.system.wrongquestion.domain.QuestionCaptureCleanResult;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 本地清理器只能安全移除彩色笔迹，不能误删印刷黑字。 */
class LocalColorInkQuestionCaptureCleanProviderTest {

    @Test
    void shouldRemoveColorInkAndPreserveBlackPrint() {
        BufferedImage source = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, new Color(0, 0, 180).getRGB());
        source.setRGB(1, 0, Color.BLACK.getRGB());

        QuestionCaptureCleanResult result = new LocalColorInkQuestionCaptureCleanProvider().clean(source);

        assertTrue(result.getRemovedRatio() > 0.4D);
        assertEquals(Color.WHITE.getRGB(), result.getImage().getRGB(0, 0));
        assertEquals(Color.BLACK.getRGB(), result.getImage().getRGB(1, 0));
    }
}
