package com.study.module.system.questionbank.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.study.api.dto.request.FileUrlData;
import com.study.api.provider.FileProvider;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.questionbank.constants.QuestionBankDictType;
import com.study.module.system.questionbank.dto.request.QuestionBankImageSaveReq;
import com.study.module.system.questionbank.dto.request.QuestionBankImportReq;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankImportResp;
import com.study.module.system.questionbank.service.QuestionBankImportService;
import com.study.module.system.questionbank.service.QuestionBankSaveService;
import com.study.module.system.wrongquestion.config.WrongQuestionOcrConfig;
import com.study.module.system.wrongquestion.domain.WrongQuestionOcrData;
import com.study.module.system.wrongquestion.service.WrongQuestionImageOcrService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A4文件识别导入题库服务实现。
 */
@Slf4j
@Service
public class QuestionBankImportServiceImpl implements QuestionBankImportService {

    private static final String QUESTION_BANK_UPLOAD_TYPE = "questionBank";
    private static final Pattern HTML_IMAGE_PATTERN = Pattern.compile(
            "<img\\b[^>]*\\bsrc=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);

    @Autowired
    WrongQuestionImageOcrService wrongQuestionImageOcrService;

    @Autowired
    QuestionBankSaveService questionBankSaveService;

    @Autowired
    DictDataService dictDataService;

    @Autowired
    WrongQuestionOcrConfig wrongQuestionOcrConfig;

    @DubboReference
    FileProvider fileProvider;

    /**
     * 执行 importQuestionBankFile 业务处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionBankImportResp importQuestionBankFile(QuestionBankImportReq request) {
        List<WrongQuestionOcrData> ocrDataList = wrongQuestionImageOcrService.recognize(request.getFileId(), AccountUtils.getUserId());
        if (CollUtil.isEmpty(ocrDataList)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_IMPORT_EMPTY);
        }
        for (WrongQuestionOcrData ocrData : ocrDataList) {
            questionBankSaveService.saveQuestionBank(buildSaveRequest(request, ocrData));
        }
        QuestionBankImportResp response = new QuestionBankImportResp();
        response.setImportCount(ocrDataList.size());
        return response;
    }

    /**
     * 构建业务处理结果。
     */
    private QuestionBankSaveReq buildSaveRequest(QuestionBankImportReq request, WrongQuestionOcrData ocrData) {
        QuestionBankSaveReq saveRequest = new QuestionBankSaveReq();
        saveRequest.setGrade(request.getGrade());
        saveRequest.setSubject(request.getSubject());
        saveRequest.setQuestionType(resolveQuestionType(request.getQuestionType(), ocrData.getQuestionType()));
        saveRequest.setQuestionTitle(defaultText(ocrData.getQuestionTitle(), "A4文件识别题目"));
        String content = defaultText(ocrData.getQuestionContent(), saveRequest.getQuestionTitle());
        saveRequest.setImages(extractImages(content));
        saveRequest.setQuestionContent(removeImageTags(content));
        saveRequest.setContentFormat("TEXT");
        saveRequest.setCorrectAnswer(defaultText(ocrData.getCorrectAnswer(), "待补充"));
        saveRequest.setAnalysis(ocrData.getAnalysis());
        saveRequest.setJudgeMode(request.getJudgeMode());
        saveRequest.setDifficulty(request.getDifficulty());
        saveRequest.setSource(request.getSource());
        saveRequest.setEnable(1);
        saveRequest.setKnowledgePointIds(request.getKnowledgePointIds());
        saveRequest.setDuplicateConfirmed(true);
        return saveRequest;
    }

    /**
     * 处理业务数据。
     */
    private String resolveQuestionType(String defaultType, String ocrTypeName) {
        if (StrUtil.isBlank(ocrTypeName) && StrUtil.isNotBlank(defaultType)) {
            return defaultType;
        }
        Map<String, String> typeMap = dictDataService.dictDataIdMap(QuestionBankDictType.QUESTION_TYPE);
        String typeName = defaultText(ocrTypeName, "其他");
        return typeMap.entrySet().stream().filter(entry -> typeName.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst()
                .orElseGet(() -> StrUtil.isNotBlank(defaultType) ? defaultType : typeMap.entrySet().stream()
                        .filter(entry -> "其他".equals(entry.getValue())).map(Map.Entry::getKey).findFirst()
                        .orElseThrow(() -> new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS)));
    }

    /**
     * 处理业务数据。
     */
    private List<QuestionBankImageSaveReq> extractImages(String content) {
        Matcher matcher = HTML_IMAGE_PATTERN.matcher(content);
        Set<String> imageUrls = new LinkedHashSet<>();
        while (matcher.find()) {
            if (StrUtil.isNotBlank(matcher.group(1))) {
                imageUrls.add(matcher.group(1));
            }
        }
        List<QuestionBankImageSaveReq> images = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            images.add(createImage(imageUrl, images.isEmpty()));
        }
        return images;
    }

    /**
     * 创建业务数据。
     */
    private QuestionBankImageSaveReq createImage(String imageUrl, boolean cover) {
        QuestionBankImageSaveReq image = new QuestionBankImageSaveReq();
        image.setCover(cover);
        image.setOriginalName(buildImageFileName(imageUrl));
        if (!isRemoteImage(imageUrl)) {
            image.setImageUrl(imageUrl);
            return image;
        }
        FileUrlData fileUrlData = new FileUrlData();
        fileUrlData.setFileUrl(imageUrl);
        fileUrlData.setFileName(image.getOriginalName());
        if (StrUtil.isNotBlank(wrongQuestionOcrConfig.getToken())) {
            fileUrlData.setRequestHeaders(Collections.singletonMap(
                    "Authorization", "bearer " + wrongQuestionOcrConfig.getToken()));
        }
        try {
            String fileIds = fileProvider.createUrlFile(QUESTION_BANK_UPLOAD_TYPE,
                    Collections.singletonList(fileUrlData), AccountUtils.getUserId());
            image.setFileId(Integer.valueOf(fileIds.split(",")[0]));
        } catch (Exception e) {
            log.warn("OCR题内图片转存失败，保留外部地址: {}", imageUrl, e);
            image.setImageUrl(imageUrl);
        }
        return image;
    }

    /**
     * 清理业务数据。
     */
    private String removeImageTags(String content) {
        String result = HTML_IMAGE_PATTERN.matcher(content).replaceAll("").trim();
        return StrUtil.isBlank(result) ? "题目内容见关联图片" : result;
    }

    /**
     * 校验业务数据。
     */
    private boolean isRemoteImage(String imageUrl) {
        return imageUrl.startsWith("http://") || imageUrl.startsWith("https://");
    }

    /**
     * 构建业务处理结果。
     */
    private String buildImageFileName(String imageUrl) {
        try {
            String path = URI.create(imageUrl).getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (StrUtil.isNotBlank(fileName) && fileName.contains(".")) {
                return fileName;
            }
        } catch (Exception ignored) {
        }
        return "question-bank-ocr-image.jpg";
    }

    /**
     * 标准化并计算业务数据。
     */
    private String defaultText(String text, String defaultText) {
        return StrUtil.isBlank(text) ? defaultText : text;
    }
}
