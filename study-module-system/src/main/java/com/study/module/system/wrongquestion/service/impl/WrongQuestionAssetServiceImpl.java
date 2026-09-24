package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAssetResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionAsset;
import com.study.module.system.wrongquestion.mapper.WrongQuestionAssetMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionAssetService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** 错题有序素材服务实现。 */
@Service
public class WrongQuestionAssetServiceImpl extends ServiceImpl<WrongQuestionAssetMapper, WrongQuestionAsset>
        implements WrongQuestionAssetService {

    private static final Pattern CONTENT_FILE_PATTERN =
            Pattern.compile("wrong-question-file://(\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    public void rewriteAssets(WrongQuestion wrongQuestion) {
        if (wrongQuestion == null || wrongQuestion.getId() == null || wrongQuestion.getCreateId() == null) {
            return;
        }
        lambdaUpdate().eq(WrongQuestionAsset::getWrongQuestionId, wrongQuestion.getId()).remove();
        List<WrongQuestionAsset> assets = new ArrayList<>();
        int sort = 1;
        String originalPageFile = wrongQuestion.getCaptureOriginalFileId() == null
                ? wrongQuestion.getImageUrl() : String.valueOf(wrongQuestion.getCaptureOriginalFileId());
        if (wrongQuestion.getCapturePageId() != null && parseFileId(originalPageFile) != null) {
            assets.add(asset(wrongQuestion, "SOURCE_PAGE", originalPageFile, "原试卷页", sort++));
            boolean useCleanedCrop = "CLEANED".equals(wrongQuestion.getCaptureImageMode())
                    && wrongQuestion.getCaptureCleanedFileId() != null;
            if (wrongQuestion.getCaptureCleanedFileId() != null) {
                assets.add(asset(wrongQuestion, "CLEANED_PAGE", String.valueOf(wrongQuestion.getCaptureCleanedFileId()),
                        "去笔迹页", sort++));
            }
            WrongQuestionAsset crop = asset(wrongQuestion,
                    useCleanedCrop ? "CLEANED_QUESTION_CROP" : "QUESTION_CROP",
                    useCleanedCrop ? String.valueOf(wrongQuestion.getCaptureCleanedFileId()) : originalPageFile,
                    useCleanedCrop ? "去笔迹题块裁剪图" : "原图题块裁剪图", sort++);
            crop.setLeftPosition(wrongQuestion.getCaptureLeftPosition());
            crop.setTopPosition(wrongQuestion.getCaptureTopPosition());
            crop.setWidth(wrongQuestion.getCaptureWidth());
            crop.setHeight(wrongQuestion.getCaptureHeight());
            assets.add(crop);
        } else {
            sort = addImage(assets, wrongQuestion, wrongQuestion.getImageUrl(),
                    isChoice(wrongQuestion) ? "OPTION_IMAGE" : "QUESTION_IMAGE",
                    isChoice(wrongQuestion) ? "选项A" : "题目图片", sort);
        }
        sort = addImage(assets, wrongQuestion, wrongQuestion.getImageUrl2(), "OPTION_IMAGE", "选项B", sort);
        sort = addImage(assets, wrongQuestion, wrongQuestion.getImageUrl3(), "OPTION_IMAGE", "选项C", sort);
        sort = addImage(assets, wrongQuestion, wrongQuestion.getImageUrl4(), "OPTION_IMAGE", "选项D", sort);
        Matcher matcher = CONTENT_FILE_PATTERN.matcher(StringUtils.hasText(wrongQuestion.getQuestionContent())
                ? wrongQuestion.getQuestionContent() : "");
        while (matcher.find()) {
            assets.add(asset(wrongQuestion, "CONTENT_IMAGE", matcher.group(1), "题干图片", sort++));
        }
        if (!assets.isEmpty()) {
            saveBatch(assets);
        }
    }

    @Override
    public List<WrongQuestionAssetResp> wrongQuestionAssetList(Long wrongQuestionId) {
        return lambdaQuery().eq(WrongQuestionAsset::getWrongQuestionId, wrongQuestionId)
                .orderByAsc(WrongQuestionAsset::getSortNo, WrongQuestionAsset::getId)
                .list().stream().map(item -> {
                    WrongQuestionAssetResp response = new WrongQuestionAssetResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    private int addImage(List<WrongQuestionAsset> assets, WrongQuestion question, String value,
                         String type, String label, int sort) {
        if (StringUtils.hasText(value)) {
            assets.add(asset(question, type, value, label, sort++));
        }
        return sort;
    }

    private WrongQuestionAsset asset(WrongQuestion question, String type, String value,
                                     String label, int sort) {
        WrongQuestionAsset asset = new WrongQuestionAsset();
        asset.setWrongQuestionId(question.getId());
        asset.setUserId(question.getCreateId());
        asset.setAssetType(type);
        Integer fileId = parseFileId(value);
        asset.setFileId(fileId);
        asset.setUploadType(fileId == null ? null : UploadType.WRONG_QUESTION);
        asset.setImageUrl(fileId == null ? value : null);
        asset.setLabel(label);
        asset.setSortNo(sort);
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(asset.getCreateTime());
        return asset;
    }

    private Integer parseFileId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("wrong-question-file://")) {
            normalized = normalized.substring("wrong-question-file://".length());
        }
        try {
            int fileId = Integer.parseInt(normalized);
            return fileId > 0 ? fileId : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isChoice(WrongQuestion question) {
        String type = StringUtils.hasText(question.getQuestionTypeName())
                ? question.getQuestionTypeName() : question.getQuestionType();
        return "选择题".equals(type) || "单选题".equals(type) || "多选题".equals(type);
    }
}
