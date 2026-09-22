package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkDeleteService;
import com.study.module.system.homework.service.HomeWorkService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 作业删除服务实现
 */
@Service
public class HomeWorkDeleteServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkDeleteService {

    @Autowired
    HomeWorkService homeWorkService;

    /**
     * 删除作业
     */
    @Override
    public void deleteHomeWork(Long id) {
        homeWorkService.checkHomeWork(id);
        if (!this.removeById(id)) {
            throw new LogicException(ErrorCodeConstants.DELETE_HOME_WORK_FAIL);
        }
    }
}
