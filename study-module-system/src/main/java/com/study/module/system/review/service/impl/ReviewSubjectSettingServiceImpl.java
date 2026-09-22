package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.mapper.ReviewSubjectSettingMapper;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 复习计划科目设置公共服务实现
 */
@Service
public class ReviewSubjectSettingServiceImpl
        extends ServiceImpl<ReviewSubjectSettingMapper, ReviewSubjectSetting>
        implements ReviewSubjectSettingService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 查询生效的科目设置
     */
    @Override
    public List<ReviewSubjectSetting> effectiveSubjectSettings(ReviewPlan reviewPlan) {
        Map<String, ReviewSubjectSetting> savedSettingMap = lambdaQuery()
                .eq(ReviewSubjectSetting::getPlanId, reviewPlan.getId())
                .eq(ReviewSubjectSetting::getUserId, reviewPlan.getUserId())
                .list().stream()
                .collect(Collectors.toMap(ReviewSubjectSetting::getSubject, setting -> setting,
                        (first, second) -> first));
        List<DictData> subjectList = dictDataService.lambdaQuery()
                .eq(DictData::getDictType, WrongQuestionDictType.SUBJECT)
                .eq(DictData::getIsEnable, 1)
                .orderByDesc(DictData::getDictDataSort)
                .list();
        List<ReviewSubjectSetting> result = new ArrayList<>();
        for (DictData subject : subjectList) {
            ReviewSubjectSetting setting = savedSettingMap.get(subject.getDictValue());
            if (setting == null) {
                setting = new ReviewSubjectSetting();
                setting.setPlanId(reviewPlan.getId());
                setting.setUserId(reviewPlan.getUserId());
                setting.setSubject(subject.getDictValue());
                setting.setEnabled(1);
                setting.setDailyLimit(Math.min(reviewPlan.getDailyLimit(),
                        ReviewDefault.SUBJECT_DAILY_LIMIT));
            }
            setting.setSubjectName(subject.getDictLabel());
            result.add(setting);
        }
        return result;
    }

    /**
     * 判断科目是否启用
     */
    @Override
    public boolean isSubjectEnabled(ReviewPlan reviewPlan, String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }
        return effectiveSubjectSettings(reviewPlan).stream()
                .filter(setting -> subject.equals(setting.getSubject()))
                .findFirst()
                .map(setting -> Integer.valueOf(1).equals(setting.getEnabled()))
                .orElse(false);
    }
}
