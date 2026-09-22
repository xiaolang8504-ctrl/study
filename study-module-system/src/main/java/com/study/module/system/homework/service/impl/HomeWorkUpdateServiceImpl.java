package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.homework.convert.HomeWorkConvert;
import com.study.module.system.homework.dto.request.UpdateHomeWorkReq;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkService;
import com.study.module.system.homework.service.HomeWorkUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 作业修改服务实现
 */
@Service
public class HomeWorkUpdateServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkUpdateService {

    @Autowired
    HomeWorkService homeWorkService;

    /**
     * 修改作业
     */
    @Override
    public void updateHomeWork(UpdateHomeWorkReq request) {
        homeWorkService.checkHomeWork(request.getId());
        HomeWork homeWork = HomeWorkConvert.INSTANCE.toHomeWork(request);
        homeWorkService.fillDictNames(homeWork);
        homeWork.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(homeWork)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_HOME_WORK_FAIL);
        }
    }
}
