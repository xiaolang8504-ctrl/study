package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.homework.convert.HomeWorkConvert;
import com.study.module.system.homework.dto.request.HomeWorkPageListReq;
import com.study.module.system.homework.dto.response.HomeWorkPageListResp;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 作业列表服务实现
 */
@Service
public class HomeWorkListServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkListService {

    @DubboReference
    FileProvider fileProvider;

    /**
     * 分页查询作业
     */
    @Override
    public PageResult<HomeWorkPageListResp> homeWorkPageList(HomeWorkPageListReq request) {
        Page<HomeWork> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, buildQueryWrapper(request));
        PageResult<HomeWorkPageListResp> response = PageUtils.wrap(page, HomeWorkConvert.INSTANCE::toHomeWorkPageListResp);
        fillFileData(response);
        return response;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<HomeWork> buildQueryWrapper(HomeWorkPageListReq request) {
        LambdaQueryWrapper<HomeWork> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getGrade())) {
            queryWrapper.eq(HomeWork::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.eq(HomeWork::getSubject, request.getSubject());
        }
        if (StringUtils.hasText(request.getKeyWord())) {
            queryWrapper.and(wrapper -> wrapper.like(HomeWork::getTitle, request.getKeyWord())
                    .or()
                    .like(HomeWork::getContent, request.getKeyWord()));
        }
        return queryWrapper.orderByDesc(HomeWork::getId);
    }

    /**
     * 填充文件信息
     */
    private void fillFileData(PageResult<HomeWorkPageListResp> response) {
        String fileIds = response.getList().stream()
                .map(HomeWorkPageListResp::getImageUrl)
                .filter(this::isFileId)
                .collect(Collectors.joining(","));
        if (!StringUtils.hasText(fileIds)) {
            return;
        }
        Map<Integer, FileData> fileDataMap = fileProvider.getFileList(UploadType.HOME_WORK, fileIds).stream()
                .collect(Collectors.toMap(FileData::getId, Function.identity(), (first, second) -> first));
        response.getList().forEach(item -> {
            if (!isFileId(item.getImageUrl())) {
                return;
            }
            FileData fileData = fileDataMap.get(Integer.valueOf(item.getImageUrl()));
            if (fileData != null) {
                item.setFileName(fileData.getOriginName());
                item.setFileExtension(fileData.getFileExtension());
                item.setFileSize(fileData.getFileSize());
            }
        });
    }

    /**
     * 判断是否为文件编号
     */
    private boolean isFileId(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
