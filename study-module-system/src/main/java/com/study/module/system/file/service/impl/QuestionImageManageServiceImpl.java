package com.study.module.system.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.file.dto.request.QuestionImagePageListReq;
import com.study.module.system.file.dto.response.QuestionImageCleanResp;
import com.study.module.system.file.dto.response.QuestionImagePageListResp;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.file.service.QuestionImageManageService;
import com.study.module.system.questionbank.entity.QuestionBankImage;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目图片管理服务实现
 */
@Service
public class QuestionImageManageServiceImpl implements QuestionImageManageService {

    private static final String QUESTION_BANK_UPLOAD_TYPE = "questionBank";

    @Autowired
    FileService fileService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    @Autowired
    DownloadService downloadService;

    @Autowired
    LocalStorageService localStorageService;

    /**
     * 分页查询题目图片。
     */
    @Override
    public PageResult<QuestionImagePageListResp> questionImagePageList(QuestionImagePageListReq request) {
        Set<Integer> referencedIds = getReferencedFileIds();
        if (Integer.valueOf(1).equals(request.getReferenced()) && referencedIds.isEmpty()) {
            return emptyPage(request);
        }
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUploadType, QUESTION_BANK_UPLOAD_TYPE)
                .like(StringUtils.hasText(request.getKeyWord()), File::getOriginName, request.getKeyWord())
                .in(Integer.valueOf(1).equals(request.getReferenced()), File::getId, referencedIds)
                .notIn(Integer.valueOf(0).equals(request.getReferenced()) && !referencedIds.isEmpty(),
                        File::getId, referencedIds)
                .orderByDesc(File::getCreateTime, File::getId);
        Page<File> page = new Page<>(request.getCurrent(), request.getPageSize());
        fileService.page(page, wrapper);
        return PageUtils.wrap(page, files -> files.stream().map(file -> toResponse(file,
                referencedIds.contains(file.getId()))).collect(Collectors.toList()));
    }

    /**
     * 删除题目图片。
     */
    @Override
    public QuestionImageCleanResp deleteOrphanQuestionImage(List<Integer> fileIds) {
        List<File> files = fileService.list(new LambdaQueryWrapper<File>()
                .eq(File::getUploadType, QUESTION_BANK_UPLOAD_TYPE)
                .in(File::getId, fileIds));
        return deleteOrphanFiles(files, fileIds.size());
    }

    /**
     * 清理题目图片。
     */
    @Override
    public QuestionImageCleanResp cleanOrphanQuestionImage(Integer retentionHours) {
        int hours = retentionHours == null ? 24 : retentionHours;
        Set<Integer> referencedIds = getReferencedFileIds();
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUploadType, QUESTION_BANK_UPLOAD_TYPE)
                .le(File::getCreateTime, LocalDateTime.now().minusHours(hours));
        if (!referencedIds.isEmpty()) {
            wrapper.notIn(File::getId, referencedIds);
        }
        List<File> files = fileService.list(wrapper);
        return deleteOrphanFiles(files, files.size());
    }

    /**
     * 清理业务数据。
     */
    private QuestionImageCleanResp deleteOrphanFiles(List<File> files, int requestedCount) {
        Set<Integer> referencedIds = getReferencedFileIds();
        List<Integer> deletedIds = new ArrayList<>();
        long releasedBytes = 0L;
        for (File file : files) {
            if (referencedIds.contains(file.getId())) {
                continue;
            }
            localStorageService.delete(file.getFilePath());
            deletedIds.add(file.getId());
            releasedBytes += file.getFileBytes() == null ? 0L : file.getFileBytes();
        }
        if (!deletedIds.isEmpty()) {
            fileService.removeByIds(deletedIds);
        }
        QuestionImageCleanResp response = new QuestionImageCleanResp();
        response.setRequestedCount(requestedCount);
        response.setDeletedCount(deletedIds.size());
        response.setSkippedCount(requestedCount - deletedIds.size());
        response.setReleasedBytes(releasedBytes);
        return response;
    }

    /**
     * 查询业务数据。
     */
    private Set<Integer> getReferencedFileIds() {
        return questionBankImageService.list(new LambdaQueryWrapper<QuestionBankImage>()
                        .isNotNull(QuestionBankImage::getFileId))
                .stream().map(QuestionBankImage::getFileId).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 转换业务数据。
     */
    private QuestionImagePageListResp toResponse(File file, boolean referenced) {
        QuestionImagePageListResp response = new QuestionImagePageListResp();
        response.setId(file.getId());
        response.setOriginName(file.getOriginName());
        response.setFileExtension(file.getFileExtension());
        response.setFileBytes(file.getFileBytes());
        response.setFileSize(file.getFileSize());
        response.setCreateTime(file.getCreateTime());
        response.setReferenced(referenced);
        response.setImageUrl(downloadService.downUrl(file.getId(), QUESTION_BANK_UPLOAD_TYPE));
        return response;
    }

    /**
     * 执行 emptyPage 辅助处理。
     */
    private PageResult<QuestionImagePageListResp> emptyPage(QuestionImagePageListReq request) {
        PageResult<QuestionImagePageListResp> result = new PageResult<>();
        result.setPage(0);
        result.setTotal(0);
        result.setPageSize(request.getPageSize());
        result.setCurrent(request.getCurrent());
        result.setList(Collections.emptyList());
        return result;
    }
}
