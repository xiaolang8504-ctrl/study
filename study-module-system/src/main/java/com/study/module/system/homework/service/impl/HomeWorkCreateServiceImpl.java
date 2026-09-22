package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.homework.convert.HomeWorkConvert;
import com.study.module.system.homework.dto.request.CreateHomeWorkReq;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkCreateService;
import com.study.module.system.homework.service.HomeWorkService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * 作业新增服务实现
 */
@Service
public class HomeWorkCreateServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkCreateService {

    @Autowired
    HomeWorkService homeWorkService;

    /**
     * 新增作业
     */
    @Override
    public void createHomeWork(CreateHomeWorkReq request) {
        HomeWork homeWork = HomeWorkConvert.INSTANCE.toHomeWork(request);
        homeWorkService.fillDictNames(homeWork);
        LocalDateTime now = LocalDateTime.now();
        homeWork.setCreateUserId(AccountUtils.getUserId());
        homeWork.setCreateTime(now);
        homeWork.setUpdateTime(now);
        if (!this.save(homeWork)) {
            throw new LogicException(ErrorCodeConstants.CREATE_HOME_WORK_FAIL);
        }
    }
}
