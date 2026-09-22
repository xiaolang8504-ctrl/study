package com.study.module.system.wrongquestion.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.wrongquestion.domain.WrongQuestionOcrData;
import com.study.module.system.wrongquestion.config.WrongQuestionOcrConfig;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.wrongquestion.dto.request.ImportWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.response.ImportWrongQuestionImageResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionImageOcrService;
import com.study.module.system.wrongquestion.service.WrongQuestionImportService;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.api.dto.request.FileUrlData;
import com.study.api.provider.FileProvider;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.component.JwtUtils;
import com.yunshang.budget.common.security.config.JwtTokenConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 初中生错题导入服务
 */
@Slf4j
@Service
public class WrongQuestionImportServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionImportService {

    private static final Pattern HTML_IMAGE_SRC_PATTERN = Pattern.compile("(<img\\b[^>]*\\bsrc=[\"'])([^\"']+)([\"'][^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final String WRONG_QUESTION_FILE_PROTOCOL = "wrong-question-file://";

    @Autowired
    WrongQuestionImageOcrService wrongQuestionImageOcrService;

    @DubboReference
    FileProvider fileProvider;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    JwtTokenConfig jwtTokenConfig;

    @Autowired
    WrongQuestionOcrConfig wrongQuestionOcrConfig;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    DictDataService dictDataService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    /**
     * A4图片识别导入错题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportWrongQuestionImageResp importWrongQuestionImage(ImportWrongQuestionImageReq request) {
        Long userId = getUserIdByToken();
        List<WrongQuestionOcrData> ocrDataList = wrongQuestionImageOcrService.recognize(request.getImageFileId(), userId);
        if (CollUtil.isEmpty(ocrDataList)) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_EMPTY);
        }
        LocalDateTime now = LocalDateTime.now();
        List<WrongQuestion> wrongQuestionList = ocrDataList.stream()
                .map(ocrData -> buildWrongQuestion(request, ocrData, userId, now))
                .collect(Collectors.toList());
        if (!this.saveBatch(wrongQuestionList)) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        // 导入数据若已经完成订正，保存成功后立即幂等创建对应复习任务。
        wrongQuestionList.forEach(reviewEnrollmentService::syncWrongQuestionReview);
        ImportWrongQuestionImageResp response = new ImportWrongQuestionImageResp();
        response.setImportCount(wrongQuestionList.size());
        return response;
    }

    /**
     * 组装错题入库数据
     */
    private WrongQuestion buildWrongQuestion(ImportWrongQuestionImageReq request, WrongQuestionOcrData ocrData,
                                             Long userId, LocalDateTime now) {
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setGrade(request.getGrade());
        wrongQuestion.setSubject(request.getSubject());
        wrongQuestion.setQuestionType(resolveQuestionTypeKey(request, ocrData));
        wrongQuestion.setSource(request.getSource());
        wrongQuestionService.fillDictNames(wrongQuestion);
        wrongQuestion.setQuestionTitle(defaultText(ocrData.getQuestionTitle(), "图片识别错题"));
        wrongQuestion.setQuestionContent(resolveContentImages(defaultText(ocrData.getQuestionContent(), wrongQuestion.getQuestionTitle()), userId));
        wrongQuestion.setWrongAnswer(resolveContentImages(ocrData.getWrongAnswer(), userId));
        wrongQuestion.setCorrectAnswer(resolveContentImages(ocrData.getCorrectAnswer(), userId));
        wrongQuestion.setWrongReason(resolveContentImages(ocrData.getWrongReason(), userId));
        wrongQuestion.setAnalysis(resolveContentImages(ocrData.getAnalysis(), userId));
        wrongQuestion.setLearningPoint(request.getLearningPoint());
        wrongQuestion.setErrorLabels(request.getErrorLabels());
        wrongQuestion.setStatus(request.getStatus());
        wrongQuestion.setCreateId(userId);
        wrongQuestion.setCreateTime(now);
        wrongQuestion.setUpdateTime(now);
        return wrongQuestion;
    }

    /**
     * OCR返回题型名称，入库前转换为题目类型字典键值
     */
    private String resolveQuestionTypeKey(ImportWrongQuestionImageReq request, WrongQuestionOcrData ocrData) {
        if (StrUtil.isBlank(ocrData.getQuestionType()) && StrUtil.isNotBlank(request.getQuestionType())) {
            return request.getQuestionType();
        }
        String questionTypeName = defaultText(ocrData.getQuestionType(), "其他");
        Map<String, String> questionTypeMap = dictDataService.dictDataIdMap(WrongQuestionDictType.QUESTION_TYPE);
        return questionTypeMap.entrySet().stream()
                .filter(entry -> questionTypeName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> questionTypeMap.entrySet().stream()
                        .filter(entry -> "其他".equals(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow(() -> new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS)));
    }

    /**
     * 将题目内容中的OCR图片转存到文件服务，并替换成文件服务下载地址
     */
    private String resolveContentImages(String content, Long userId) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        Matcher matcher = HTML_IMAGE_SRC_PATTERN.matcher(content);
        StringBuffer buffer = new StringBuffer();
        Map<String, String> imageUrlCache = new HashMap<>();
        while (matcher.find()) {
            String imageSrc = matcher.group(2);
            String resolvedImageSrc = resolveImageSrc(imageSrc, userId, imageUrlCache);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(1) + resolvedImageSrc + matcher.group(3)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 单张图片转存，已是本系统下载地址或不可转存地址时保持原样
     */
    private String resolveImageSrc(String imageSrc, Long userId, Map<String, String> imageUrlCache) {
        if (StrUtil.isBlank(imageSrc) || !isRemoteImageSrc(imageSrc) || imageSrc.contains("/api/file/downloadFile")) {
            return imageSrc;
        }
        if (imageUrlCache.containsKey(imageSrc)) {
            return imageUrlCache.get(imageSrc);
        }
        FileUrlData fileUrlData = new FileUrlData();
        fileUrlData.setFileUrl(imageSrc);
        fileUrlData.setFileName(buildImageFileName(imageSrc));
        fileUrlData.setRequestHeaders(buildImageRequestHeaders());
        try {
            String fileIds = fileProvider.createUrlFile(UploadType.WRONG_QUESTION, Collections.singletonList(fileUrlData), userId);
            String fileId = fileIds.split(",")[0];
            String fileProtocolUrl = WRONG_QUESTION_FILE_PROTOCOL + fileId;
            imageUrlCache.put(imageSrc, fileProtocolUrl);
            return fileProtocolUrl;
        } catch (Exception e) {
            log.warn("OCR图片转存失败，保留原始图片地址: {}", imageSrc, e);
            imageUrlCache.put(imageSrc, imageSrc);
            return imageSrc;
        }
    }

    /**
     * 当前文件服务按URL下载远程图片，非远程地址保留原地址
     */
    private boolean isRemoteImageSrc(String imageSrc) {
        return imageSrc.startsWith("http://") || imageSrc.startsWith("https://");
    }

    /**
     * 从图片URL生成文件名
     */
    private String buildImageFileName(String imageSrc) {
        try {
            String path = URI.create(imageSrc).getPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (StrUtil.isNotBlank(fileName) && fileName.contains(".")) {
                return fileName;
            }
        } catch (Exception ignored) {
        }
        return "wrong-question-ocr-image.jpg";
    }

    /**
     * 生成OCR结果图片下载请求头
     */
    private Map<String, String> buildImageRequestHeaders() {
        if (StrUtil.isBlank(wrongQuestionOcrConfig.getToken())) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + wrongQuestionOcrConfig.getToken());
        return headers;
    }

    /**
     * 文本兜底，避免OCR分割结果为空导致入库失败
     */
    private String defaultText(String text, String defaultText) {
        return StrUtil.isBlank(text) ? defaultText : text;
    }

    /**
     * 从请求TOKEN中获取当前账号ID
     */
    private Long getUserIdByToken() {
        Long userId = JwtUtils.getUserIdByToken(
                httpServletRequest,
                jwtTokenConfig.getTokenHeader(),
                jwtTokenConfig.getTokenHead(),
                jwtTokenConfig.getSecretKey()
        );
        if (userId == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        return userId;
    }
}
