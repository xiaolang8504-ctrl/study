package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.questionbank.dto.request.QuestionBankImageSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankImageListResp;
import com.study.module.system.questionbank.entity.QuestionBankImage;
import com.study.module.system.questionbank.mapper.QuestionBankImageMapper;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionBankImageServiceImpl extends ServiceImpl<QuestionBankImageMapper, QuestionBankImage>
        implements QuestionBankImageService {
    private static final String UPLOAD_TYPE = "questionBank";

    @Autowired
    FileService fileService;

    @Autowired
    DownloadService downloadService;

    /**
     * 重写实体关联数据
     */
    @Override
    public void rewrite(Long questionId, List<QuestionBankImageSaveReq> images, String legacyImageUrls) {
        if (images == null) {
            if (!StringUtils.hasText(legacyImageUrls)) return;
            images = Arrays.stream(legacyImageUrls.split(",")).filter(StringUtils::hasText).map(url -> {
                QuestionBankImageSaveReq item = new QuestionBankImageSaveReq();
                item.setImageUrl(url.trim());
                return item;
            }).collect(Collectors.toList());
        }
        remove(new LambdaQueryWrapper<QuestionBankImage>().eq(QuestionBankImage::getQuestionId, questionId));
        boolean hasCover = images.stream().anyMatch(item -> Boolean.TRUE.equals(item.getCover()));
        for (int i = 0; i < images.size(); i++) {
            QuestionBankImageSaveReq item = images.get(i);
            if (item.getFileId() == null && !StringUtils.hasText(item.getImageUrl())) continue;
            if (item.getFileId() != null) fileService.checkFile(item.getFileId(), UPLOAD_TYPE);
            QuestionBankImage entity = new QuestionBankImage();
            entity.setQuestionId(questionId);
            entity.setFileId(item.getFileId());
            entity.setImageUrl(item.getImageUrl());
            entity.setOriginalName(item.getOriginalName());
            entity.setSort(i + 1);
            entity.setIsCover(Boolean.TRUE.equals(item.getCover()) || (!hasCover && i == 0) ? 1 : 0);
            save(entity);
        }
    }

    /**
     * 查询题目图片列表
     */
    @Override
    public List<QuestionBankImageListResp> imageList(Long questionId) {
        return list(new LambdaQueryWrapper<QuestionBankImage>().eq(QuestionBankImage::getQuestionId, questionId)
                .orderByAsc(QuestionBankImage::getSort, QuestionBankImage::getId)).stream().map(image -> {
            QuestionBankImageListResp response = new QuestionBankImageListResp();
            response.setId(image.getId());
            response.setFileId(image.getFileId());
            response.setOriginalName(image.getOriginalName());
            response.setSort(image.getSort());
            response.setCover(Objects.equals(image.getIsCover(), 1));
            response.setImageUrl(image.getFileId() == null ? image.getImageUrl()
                    : downloadService.downUrl(image.getFileId(), UPLOAD_TYPE));
            return response;
        }).collect(Collectors.toList());
    }
}
