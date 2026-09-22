package com.study.module.system.homework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.homework.entity.HomeWork;
import com.study.module.system.homework.constants.HomeWorkDictType;
import com.study.module.system.homework.mapper.HomeWorkMapper;
import com.study.module.system.homework.service.HomeWorkService;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 作业公共服务实现
 */
@Service
public class HomeWorkServiceImpl extends ServiceImpl<HomeWorkMapper, HomeWork> implements HomeWorkService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 校验作业
     */
    @Override
    public HomeWork checkHomeWork(Long id) {
        HomeWork homeWork = this.getById(id);
        if (homeWork == null) {
            throw new LogicException(ErrorCodeConstants.HOME_WORK_NOT_EXIST);
        }
        return homeWork;
    }

    /**
     * 填充字典名称
     */
    @Override
    public void fillDictNames(HomeWork homeWork) {
        DictData grade = checkDictData(HomeWorkDictType.GRADE, homeWork.getGrade());
        DictData subject = checkDictData(HomeWorkDictType.SUBJECT, homeWork.getSubject());
        homeWork.setGradeName(grade.getDictLabel());
        homeWork.setSubjectName(subject.getDictLabel());
    }

    /**
     * 校验字典数据
     */
    private DictData checkDictData(String dictType, String dictValue) {
        return dictDataService.checkDictData(dictType, dictValue, ErrorCodeConstants.INVALID_DICT_DATA_IDS);
    }
}
