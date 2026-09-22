package com.study.module.system.user.service.impl;

import com.study.module.system.user.dto.request.SliderCaptchaVerifyReq;
import com.study.module.system.user.dto.response.VerifyResp;
import com.study.module.system.user.service.VerifyService;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码服务实现类
 */
@Service
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    RedisService redisService;

    /**
     * 获取滑块验证码
     */
    @Override
    public VerifyResp verify() {
        VerifyResp verifyResp = new VerifyResp();
        try {
            int offset = ThreadLocalRandom.current().nextInt(38, 279);
            int backgroundType = ThreadLocalRandom.current().nextInt(3);
            int shapeType = ThreadLocalRandom.current().nextInt(6);
            String captchaKey = UUID.randomUUID().toString();
            verifyResp.setBackground(createBackground(offset, backgroundType, shapeType));
            verifyResp.setSliderImage(createSliderImage(backgroundType, shapeType));
            verifyResp.setSliderCaptchaToken(captchaKey);
            redisService.set(RedisKey.SYSTEM_USER_SLIDER_CAPTCHA + ":" + captchaKey, offset, 300L);
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.VERIFY_FAIL);
        }
        return verifyResp;
    }

    /**
     * 校验滑块验证码。校验失败时保留验证码，使用户可以继续拖动重试。
     */
    @Override
    public void verifySliderCaptcha(SliderCaptchaVerifyReq request) {
        String captchaKey = RedisKey.SYSTEM_USER_SLIDER_CAPTCHA + ":" + request.getSliderCaptchaToken();
        Object expectedOffset = redisService.get(captchaKey);
        if (expectedOffset == null) {
            throw new LogicException(ErrorCodeConstants.VERIFY_EXPIRE_FAIL);
        }
        try {
            int offset = Integer.parseInt(request.getSliderCaptchaOffset());
            if (Math.abs(offset - Integer.parseInt(expectedOffset.toString())) > 6) {
                throw new LogicException(ErrorCodeConstants.SLIDER_CAPTCHA_VERIFY_FAIL);
            }
        } catch (NumberFormatException e) {
            throw new LogicException(ErrorCodeConstants.SLIDER_CAPTCHA_VERIFY_FAIL);
        }

        Long expire = redisService.getExpire(captchaKey);
        if (expire == null || expire <= 0) {
            throw new LogicException(ErrorCodeConstants.VERIFY_EXPIRE_FAIL);
        }
        redisService.set(verifiedCaptchaKey(request.getSliderCaptchaToken()), Boolean.TRUE, expire);
        redisService.del(captchaKey);
    }

    /**
     * 消费已通过校验的滑块验证码，避免同一个验证码重复登录。
     */
    @Override
    public void consumeVerifiedSliderCaptcha(String sliderCaptchaToken) {
        String verifiedCaptchaKey = verifiedCaptchaKey(sliderCaptchaToken);
        if (!Boolean.TRUE.equals(redisService.get(verifiedCaptchaKey))) {
            throw new LogicException(ErrorCodeConstants.SLIDER_CAPTCHA_VERIFY_FAIL);
        }
        redisService.del(verifiedCaptchaKey);
    }

    /**
     * 获取已校验滑块验证码的 Redis Key。
     */
    private String verifiedCaptchaKey(String sliderCaptchaToken) {
        return RedisKey.SYSTEM_USER_SLIDER_CAPTCHA_VERIFIED + ":" + sliderCaptchaToken;
    }

    /**
     * 创建业务数据。
     */
    private String createBackground(int offset, int backgroundType, int shapeType) {
        String[] colors = backgroundColors(backgroundType);
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"320\" height=\"150\" viewBox=\"0 0 320 150\">"
                + "<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop stop-color=\"" + colors[0] + "\"/><stop offset=\"1\" stop-color=\"" + colors[1] + "\"/></linearGradient></defs>"
                + "<rect width=\"320\" height=\"150\" rx=\"8\" fill=\"url(#g)\"/>"
                + backgroundDecoration(backgroundType)
                + createShape(shapeType, offset, 50, "#f8fafc", ".9")
                + "<text x=\"160\" y=\"132\" text-anchor=\"middle\" font-size=\"13\" fill=\"#475569\">拖动拼图块，使其与缺口重合</text></svg>";
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建业务数据。
     */
    private String createSliderImage(int backgroundType, int shapeType) {
        String[] colors = backgroundColors(backgroundType);
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"36\" height=\"38\" viewBox=\"0 0 36 38\">"
                + "<defs><linearGradient id=\"p\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop stop-color=\"" + colors[0] + "\"/><stop offset=\"1\" stop-color=\"" + colors[1] + "\"/></linearGradient></defs>"
                + createShape(shapeType, 0, 0, "url(#p)", "1")
                + "</svg>";
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建业务数据。
     */
    private String createShape(int shapeType, int x, int y, String fill, String opacity) {
        String shape;
        switch (shapeType) {
            case 1:
                shape = "<circle cx=\"" + (x + 18) + "\" cy=\"" + (y + 19) + "\" r=\"17\"";
                break;
            case 2:
                shape = "<path d=\"M" + (x + 18) + " " + y + " L" + (x + 36) + " " + (y + 37) + " H" + x + " Z\"";
                break;
            case 3:
                shape = "<polygon points=\"" + (x + 18) + "," + y + " " + (x + 36) + "," + (y + 19) + " " + (x + 18) + "," + (y + 38) + " " + x + "," + (y + 19) + "\"";
                break;
            case 4:
                shape = "<polygon points=\"" + (x + 18) + "," + y + " " + (x + 22) + "," + (y + 13) + " " + (x + 36) + "," + (y + 14) + " " + (x + 25) + "," + (y + 23) + " " + (x + 29) + "," + (y + 37) + " " + (x + 18) + "," + (y + 29) + " " + (x + 7) + "," + (y + 37) + " " + (x + 11) + "," + (y + 23) + " " + x + "," + (y + 14) + " " + (x + 14) + "," + (y + 13) + "\"";
                break;
            case 5:
                shape = "<polygon points=\"" + (x + 10) + "," + y + " " + (x + 26) + "," + y + " " + (x + 36) + "," + (y + 19) + " " + (x + 26) + "," + (y + 38) + " " + (x + 10) + "," + (y + 38) + " " + x + "," + (y + 19) + "\"";
                break;
            default:
                shape = "<rect x=\"" + x + "\" y=\"" + y + "\" width=\"36\" height=\"38\" rx=\"3\"";
                break;
        }
        return shape + " fill=\"" + fill + "\" fill-opacity=\"" + opacity + "\" stroke=\"#475569\" stroke-width=\"2\"/>";
    }

    /**
     * 执行 backgroundColors 辅助处理。
     */
    private String[] backgroundColors(int backgroundType) {
        switch (backgroundType) {
            case 1:
                return new String[]{"#dcfce7", "#ccfbf1"};
            case 2:
                return new String[]{"#fce7f3", "#fef3c7"};
            default:
                return new String[]{"#dbeafe", "#e0e7ff"};
        }
    }

    /**
     * 执行 backgroundDecoration 辅助处理。
     */
    private String backgroundDecoration(int backgroundType) {
        switch (backgroundType) {
            case 1:
                return "<path d=\"M0 108 Q45 62 94 107 T188 90 T320 112 V150 H0Z\" fill=\"#86efac\" opacity=\".55\"/>"
                        + "<path d=\"M18 97 l22-50 20 50z M246 102 l22-58 22 58z\" fill=\"#34d399\" opacity=\".65\"/>"
                        + "<circle cx=\"72\" cy=\"32\" r=\"17\" fill=\"#fde68a\" opacity=\".85\"/>";
            case 2:
                return "<path d=\"M0 116 C48 80 77 131 123 104 S205 77 320 112 V150 H0Z\" fill=\"#f9a8d4\" opacity=\".52\"/>"
                        + "<circle cx=\"44\" cy=\"39\" r=\"18\" fill=\"#fbcfe8\"/><circle cx=\"263\" cy=\"42\" r=\"27\" fill=\"#fde68a\" opacity=\".62\"/>"
                        + "<path d=\"M146 32 l6 12 13 2-10 9 3 13-12-7-11 7 3-13-10-9 13-2z\" fill=\"#f59e0b\" opacity=\".55\"/>";
            default:
                return "<path d=\"M0 112 Q55 72 104 110 T210 92 T320 102 V150 H0Z\" fill=\"#bfdbfe\" opacity=\".72\"/>"
                        + "<circle cx=\"46\" cy=\"38\" r=\"18\" fill=\"#c7d2fe\"/><circle cx=\"260\" cy=\"38\" r=\"25\" fill=\"#bae6fd\"/>";
        }
    }
}
