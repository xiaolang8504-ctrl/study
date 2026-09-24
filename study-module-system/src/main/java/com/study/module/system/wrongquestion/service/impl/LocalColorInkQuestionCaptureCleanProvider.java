package com.study.module.system.wrongquestion.service.impl;

import com.study.module.system.wrongquestion.domain.QuestionCaptureCleanResult;
import com.study.module.system.wrongquestion.service.QuestionCaptureCleanProvider;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * 保守的本地清理器：擦除红、蓝、紫等高饱和彩色笔迹，以及常见的中灰铅笔笔迹；
 * 深黑印刷文字与深黑手写在无版面模型时不可可靠区分，因此继续保留，避免误删题干。
 */
@Component
public class LocalColorInkQuestionCaptureCleanProvider implements QuestionCaptureCleanProvider {

    @Override
    public String providerCode() {
        return "LOCAL_COLOR_INK";
    }

    @Override
    public String algorithmVersion() {
        return "color-pencil-mask-v2";
    }

    @Override
    public QuestionCaptureCleanResult clean(BufferedImage source) {
        BufferedImage cleaned = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        long removedPixels = 0L;
        long totalPixels = (long) source.getWidth() * source.getHeight();
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color color = new Color(source.getRGB(x, y), true);
                if (isLikelyColoredHandwriting(color) || isLikelyPencilHandwriting(color)) {
                    cleaned.setRGB(x, y, Color.WHITE.getRGB());
                    removedPixels++;
                } else {
                    int gray = (int) Math.min(255, Math.round(color.getRed() * 0.299D
                            + color.getGreen() * 0.587D + color.getBlue() * 0.114D));
                    cleaned.setRGB(x, y, new Color(gray, gray, gray).getRGB());
                }
            }
        }
        return new QuestionCaptureCleanResult(cleaned,
                totalPixels == 0 ? 0D : (double) removedPixels / totalPixels);
    }

    private boolean isLikelyColoredHandwriting(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        if (max - min < 55 || max > 245) {
            return false;
        }
        return (blue >= red + 35 && blue >= green + 15)
                || (red >= green + 45 && red >= blue + 35)
                || (blue >= green + 25 && red >= green + 25);
    }

    /**
     * 只处理非纯黑、低饱和的中灰笔迹。扫描件中的印刷黑字通常亮度更低；
     * 仍无法判断的深黑痕迹保留给原图/人工确认，不以“去笔迹成功”冒充。
     */
    private boolean isLikelyPencilHandwriting(Color color) {
        int red = color.getRed(); int green = color.getGreen(); int blue = color.getBlue();
        int max = Math.max(red, Math.max(green, blue)); int min = Math.min(red, Math.min(green, blue));
        int brightness = (red + green + blue) / 3;
        return max - min <= 18 && brightness >= 105 && brightness <= 190;
    }
}
