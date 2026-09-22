package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.homework.convert.HomeWorkConvert;
import com.study.module.system.homework.dto.response.HomeWorkDetailResp;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkDetailService;
import com.study.module.system.homework.service.HomeWorkService;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作业详情服务实现
 */
@Service
public class HomeWorkDetailServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkDetailService {

    @Autowired
    HomeWorkService homeWorkService;

    @DubboReference
    FileProvider fileProvider;

    /**
     * 查询作业详情
     */
    @Override
    public HomeWorkDetailResp homeWorkDetail(Long id) {
        HomeWorkDetailResp response = HomeWorkConvert.INSTANCE.toHomeWorkDetailResp(homeWorkService.checkHomeWork(id));
        fillFileData(response);
        return response;
    }

    /**
     * 填充文件信息
     */
    private void fillFileData(HomeWorkDetailResp response) {
        Integer fileId = parseFileId(response.getImageUrl());
        if (fileId == null) {
            return;
        }
        FileData fileData = fileProvider.getFile(UploadType.HOME_WORK, fileId);
        if (fileData != null) {
            response.setFileName(fileData.getOriginName());
            response.setFileExtension(fileData.getFileExtension());
            response.setFileSize(fileData.getFileSize());
        }
    }

    /**
     * 解析文件编号
     */
    private Integer parseFileId(String fileId) {
        try {
            return fileId == null ? null : Integer.valueOf(fileId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
